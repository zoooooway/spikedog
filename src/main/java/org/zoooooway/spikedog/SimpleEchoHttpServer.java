package org.zoooooway.spikedog;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

/**
 * 简单的echo http server实现
 *
 * @author zoooooway
 */
public class SimpleEchoHttpServer implements HttpHandler, AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(SimpleEchoHttpServer.class);



    public static void main(String[] args)  {
        try (SimpleEchoHttpServer echoServer = new SimpleEchoHttpServer("localhost", 8080)) {
            echoServer.httpServer.start();
            log.debug("start server!");
            for (;;) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    log.error("server interrupted!", e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } catch (Exception e) {
            log.error("create server failed!", e);
        }
    }

    final HttpServer httpServer;

    public SimpleEchoHttpServer(String host, int port) throws IOException {
        this.httpServer = HttpServer.create(new InetSocketAddress(host, port), 0);
        this.httpServer.createContext("/", this);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        URI requestURI = exchange.getRequestURI();
        log.debug("method: {}, url: {}", requestMethod, requestURI.toString());

        try (InputStream requestBody = exchange.getRequestBody()) {
            try (OutputStream responseBody = exchange.getResponseBody()) {
                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.add("Content-Type", "text/html; charset=utf-8");
                responseHeaders.add("Cache-Control", "no-cache");
                exchange.sendResponseHeaders(200, requestBody.available());
                requestBody.transferTo(responseBody);
            }
        }
    }

    @Override
    public void close() throws Exception {
        this.httpServer.stop(3);
        log.debug("stop server!");
    }
}
