package de.christensen.httpserver.controller;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class FileControllerTest {

    @Before
    public void setUp(){
        FileController.initialize("./src/test/resources");
    }

    @Test
    public void testHandleStaticContent() throws IOException {
        final FileController fileController = FileController.getInstance();
        final String staticContent = fileController.getStaticContent("/index.html");
        String expectedResult = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Title</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
        assertThat(staticContent.startsWith("HTTP/1.1 200 OK")).isTrue();
        assertThat(staticContent.endsWith(expectedResult)).isTrue();
    }

    @Test
    public void testHandleStaticContentNotFound() throws IOException {
        final FileController fileController = FileController.getInstance();
        final String staticContent = fileController.getStaticContent("/nofound.html");
        assertThat(staticContent.startsWith("HTTP/1.1 404 Not Found")).isTrue();
    }

    @Test
    public void testGetFile(){
        final FileController fileController = FileController.getInstance();
        String request =  "GET /root/test_dir/test_file1 HTTP/1.1";
        final String fileResponse = fileController.getFile(request);
        assertThat(fileResponse.startsWith("HTTP/1.1 200 OK")).isTrue();
        String expectedResult= "Text from file 1";
        assertThat(fileResponse.endsWith(expectedResult)).isTrue();
    }

    @Test
    public void testHeadFile(){
        final FileController fileController = FileController.getInstance();
        String request =  "HEAD /root/test_dir/test_file1 HTTP/1.1";
        final String fileResponse = fileController.headFile(request);
        assertThat(fileResponse.startsWith("HTTP/1.1 200 OK")).isTrue();
        String expectedResult= "Connection: Closed\n\n";
        assertThat(fileResponse.endsWith(expectedResult)).isTrue();
    }

    @Test
    public void testGetDirectory(){
        final FileController fileController = FileController.getInstance();
        String request =  "GET /root/test_dir HTTP/1.1";
        final String fileResponse = fileController.getFile(request);
        assertThat(fileResponse.startsWith("HTTP/1.1 200 OK")).isTrue();
        String expectedResult= "{\"test_dir\":[\"test_file2\",{\"test_sub_dir\":[\"test_sub_file1\",\"test_sub_file2\"]},\"test_file1\"]}";
        assertThat(fileResponse.endsWith(expectedResult)).isTrue();
    }

    @Test
    public void testHeadDirectory(){
        final FileController fileController = FileController.getInstance();
        String request =  "HEAD /root/test_dir HTTP/1.1";
        final String fileResponse = fileController.headFile(request);
        assertThat(fileResponse.startsWith("HTTP/1.1 200 OK")).isTrue();
        String expectedResult= "Connection: Closed\n\n";
        assertThat(fileResponse.endsWith(expectedResult)).isTrue();
    }

}