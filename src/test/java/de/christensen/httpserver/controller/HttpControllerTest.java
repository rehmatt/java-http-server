package de.christensen.httpserver.controller;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpControllerTest {

    @Test
    public void testGetRequestUriGET() {
        String expectedUri = "/test/uri";
        final String request = "GET /root"+expectedUri+" HTTP/1.1\n" +
                "Content-Length: 12\n" +
                "Content-Type: text/plain\n" +
                "\n" +
                "123456789123";
        final String requestUri = HttpController.getRequestUri(request, 9);
        assertThat(requestUri).isEqualTo(expectedUri);
    }

    @Test
    public void testGetRequestUriHEADWithPendingSlash() {
        String expectedUri = "/test/uri";
        final String request = "HEAD /root"+expectedUri+"/ HTTP/1.1\n" +
                "Content-Length: 12\n" +
                "Content-Type: text/plain\n" +
                "\n" +
                "123456789123";
        final String requestUri = HttpController.getRequestUri(request, 10);
        assertThat(requestUri).isEqualTo(expectedUri);
    }

}
