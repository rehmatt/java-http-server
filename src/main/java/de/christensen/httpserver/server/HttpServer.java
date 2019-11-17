package de.christensen.httpserver.server;

import de.christensen.httpserver.controller.CommentController;
import de.christensen.httpserver.controller.FileController;
import de.christensen.httpserver.controller.HttpController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Properties;

/*
 * This class provides a non-blocking http server.
 */
public class HttpServer {

    private static final Logger LOG = LoggerFactory.getLogger(HttpServer.class);

    private static final HttpServer instance = new HttpServer();
    private static final int REQUEST_CHUNK_SIZE = 16;
    private final FileController fileController;
    private final CommentController commentController;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    private HttpServer() {
        fileController = FileController.getInstance();
        commentController = CommentController.getInstance();
    }

    public static HttpServer getInstance() {
        return instance;
    }

    public void start(Properties properties) throws IOException {
        openSelector();
        openChannel(properties);
        registerKey(serverSocketChannel, SelectionKey.OP_ACCEPT);
        while (selector.isOpen() && serverSocketChannel.isOpen()) {
            selector.selectNow(this::handle);
        }
    }

    private void openSelector() throws IOException {
        if (selector == null || !selector.isOpen()) {
            selector = Selector.open();
        }
    }

    private void openChannel(Properties properties) throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        final String host = properties.getProperty("server.host");
        final int port = Integer.parseInt(properties.getProperty("server.port"));
        serverSocketChannel.bind(new InetSocketAddress(host, port));
    }

    private void registerKey(SelectableChannel socketChannel, int key) throws ClosedChannelException {
        socketChannel.register(selector, key);
    }

    private void handle(SelectionKey selectionKey) {
        try {
            if (selectionKey.isAcceptable()) {
                acceptConnection();
            }
            if (selectionKey.isReadable()) {
                handleRequest(selectionKey);
            }
        } catch (Exception e) {
            LOG.error("Error occurred during reading the request and creating the response", e);
            closeChannel(selectionKey.channel());
        }
    }

    private void closeChannel(SelectableChannel channel) {
        try {
            channel.close();
        } catch (IOException e) {
            LOG.error("Error occurred during closing the channel", e);
        }
    }

    private void acceptConnection() throws IOException {
        SocketChannel sc = serverSocketChannel.accept();
        sc.configureBlocking(false);
        registerKey(sc, SelectionKey.OP_READ);
        LOG.debug("Connection accepted: {}", sc.getLocalAddress());
    }

    private void handleRequest(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        String request = readRequest(socketChannel);
        LOG.debug("Request: {}", request);

        final String response;
        if (request.startsWith("GET /root")) {
            response = fileController.getFile(request);
        } else if (request.startsWith("HEAD /root")) {
            response = fileController.headFile(request);
        } else if (request.startsWith("GET /api/comments ")) {
            response = commentController.getComments();
        } else if (request.startsWith("POST /api/comments/comment")) {
            response = commentController.postComment(request);
        } else if (request.startsWith("GET / ") ||
                request.startsWith("GET /index.html ")) {
            response = fileController.getStaticContent("/index.html");
        } else if (request.startsWith("GET /")) {
            final String staticResource = HttpController.getRequestUri(request, 4);
            response = fileController.getStaticContent(staticResource);
        } else {
            response = HttpController.createErrorResponse(HttpStatus.NOT_FOUND);
        }
        writeResponse(socketChannel, response);

        closeChannel(socketChannel);
        LOG.debug("Client connection closed");
    }

    private void writeResponse(SocketChannel socketChannel, String response) throws IOException {
        final byte[] bytes = response.getBytes();
        int writtenBytes = 0;
        do {
            //write response to socketChannel until it's completely written
            final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, writtenBytes, bytes.length - writtenBytes);
            writtenBytes += socketChannel.write(byteBuffer);
        } while (writtenBytes != bytes.length);
    }

    private String readRequest(SocketChannel socketChannel) throws IOException {
        final StringBuilder request = new StringBuilder();
        int readBytes = 0;
        do {
            //read request from socketChannel until it's completely read
            ByteBuffer byteBuffer = ByteBuffer.allocate(REQUEST_CHUNK_SIZE);
            readBytes = socketChannel.read(byteBuffer);
            byteBuffer.flip();
            request.append(new String(byteBuffer.array()));
        } while (readBytes > 0);

        return request.toString().trim();
    }
}
