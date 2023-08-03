package org.zoooooway.spikedog;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zoooooway.spikedog.filter.HelloFilter;
import org.zoooooway.spikedog.filter.LogFilter;
import org.zoooooway.spikedog.servlet.HelloServlet;
import org.zoooooway.spikedog.servlet.IndexServlet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * 简单的echo http server实现
 *
 * @author zoooooway
 */
public class SimpleEchoHttpServer extends HttpConnector implements AutoCloseable {
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
        super(List.of(IndexServlet.class, HelloServlet.class), List.of(HelloFilter.class, LogFilter.class));
        this.httpServer = HttpServer.create(new InetSocketAddress(host, port), 0);
        this.httpServer.createContext("/", this);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        super.handle(exchange);
    }

//    @Override
//    public void process(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        String requestMethod = request.getMethod();
//        String uri = request.getRequestURI();
//        log.debug("method: {}, url: {}", requestMethod, uri);
//
//        try (BufferedReader reader = request.getReader()) {
//            try (PrintWriter writer = response.getWriter()) {
//                response.setHeader("Content-Type", "text/html; charset=utf-8");
//                response.setHeader("Cache-Control", "no-cache");
//                response.setStatus(200);
//                response.setContentLength(request.getContentLength());
//                reader.lines().forEach(writer::write);
//                response.flushBuffer();
//            }
//        }
//    }

    @Override
    public void close() throws Exception {
        this.httpServer.stop(3);
        log.debug("stop server!");
    }
}
