package org.zoooooway.spikedog.connector;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zoooooway.spikedog.classloader.WebAppClassLoader;
import org.zoooooway.spikedog.servlet.ServletContextImpl;
import org.zoooooway.spikedog.session.SessionManager;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarFile;

/**
 * @author zoooooway
 */
public class HttpConnector implements HttpHandler {
    private final static Logger log = LoggerFactory.getLogger(HttpConnector.class);

    protected final ServletContextImpl servletContext;
    protected final WebAppClassLoader classLoader;

    public HttpConnector(WebAppClassLoader classLoader, Set<Class<? extends Servlet>> servletSet, Set<Class<? extends Filter>> filterSet, Set<Class<? extends EventListener>> listenerSet) {
        this.servletContext = new ServletContextImpl();
        this.classLoader = classLoader;
        this.servletContext.setSessionManager(new SessionManager(this.servletContext));
        this.servletContext.init(servletSet, filterSet, listenerSet);
    }


//    public HttpConnector(WebAppClassLoader classLoader, List<Class<?>> scannedClass) {
//        this.servletContext = new ServletContextImpl();
//        this.classLoader = classLoader;
//        this.servletContext.setSessionManager(new SessionManager(this.servletContext));
//        this.servletContext.init(scannedClass);
//    }


    @Override
    public void handle(HttpExchange exchange) {
        HttpExchangeAdapter exchangeAdapter = new HttpExchangeAdapter(exchange);
        HttpServletResponseImpl response = new HttpServletResponseImpl(exchangeAdapter);
        HttpServletRequestImpl request = new HttpServletRequestImpl(exchangeAdapter, response, this.servletContext);

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            this.servletContext.process(request, response);
        } catch (Exception e) {
            log.error("Servlet process exception", e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }
}
