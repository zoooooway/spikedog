package org.zoooooway.spikedog;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zoooooway.spikedog.classloader.WebAppClassLoader;
import org.zoooooway.spikedog.connector.HttpConnector;
import org.zoooooway.spikedog.filter.HelloFilter;
import org.zoooooway.spikedog.filter.LogFilter;
import org.zoooooway.spikedog.listener.HttpSessionListenerImpl;
import org.zoooooway.spikedog.listener.ServletContextListenerImpl;
import org.zoooooway.spikedog.listener.ServletRequestListenerImpl;
import org.zoooooway.spikedog.servlet.HelloServlet;
import org.zoooooway.spikedog.servlet.IndexServlet;
import org.zoooooway.spikedog.servlet.LoginServlet;
import org.zoooooway.spikedog.servlet.LogoutServlet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * 简单的echo http server实现
 *
 * @author zoooooway
 */
public class SimpleEchoHttpServer extends HttpConnector implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(SimpleEchoHttpServer.class);


    public static void main(String[] args) {
        // todo 从命令行中解析启动war参数， 获取classpath和libPath
        Path classPath = null;
        Path libPath = null;
        // todo 通过自定义classloader扫描所有class文件

        // todo 启动server
        try (SimpleEchoHttpServer echoServer = new SimpleEchoHttpServer("localhost", 8080, classPath, libPath)) {
            echoServer.httpServer.start();
            log.debug("start server!");
            for (; ; ) {
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

    public SimpleEchoHttpServer(String host, int port, Path classpath, Path libPath) throws IOException {
        super(new WebAppClassLoader(classpath, libPath), Set.of(IndexServlet.class, HelloServlet.class, LoginServlet.class, LogoutServlet.class), Set.of(HelloFilter.class, LogFilter.class)
                , Set.of(HttpSessionListenerImpl.class, ServletRequestListenerImpl.class, ServletContextListenerImpl.class));
        this.servletContext.getSessionManager().setInterval(10);
        this.httpServer = HttpServer.create(new InetSocketAddress(host, port), 0);
        this.httpServer.createContext("/", this);
    }

    @Override
    public void handle(HttpExchange exchange) {
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
