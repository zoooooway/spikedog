package org.zoooooway.spikedog.connector;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zoooooway.spikedog.servlet.ServletContextImpl;
import org.zoooooway.spikedog.session.SessionManager;

import java.io.IOException;
import java.util.EventListener;
import java.util.List;

/**
 * @author zoooooway
 */
public class HttpConnector implements HttpHandler {
    private final static Logger log = LoggerFactory.getLogger(HttpConnector.class);

    protected final ServletContextImpl servletContext;

    public HttpConnector(List<Class<? extends Servlet>> servletClasses, List<Class<? extends Filter>> filterClasses, List<Class<? extends EventListener>> listenerClasses) {
        this.servletContext = new ServletContextImpl();
        this.servletContext.setSessionManager(new SessionManager(this.servletContext));

        this.servletContext.init(servletClasses, filterClasses, listenerClasses);
    }

    @Override
    public void handle(HttpExchange exchange) {
        HttpExchangeAdapter exchangeAdapter = new HttpExchangeAdapter(exchange);
        HttpServletResponseImpl response = new HttpServletResponseImpl(exchangeAdapter);
        HttpServletRequestImpl request = new HttpServletRequestImpl(exchangeAdapter, response, this.servletContext);
        try {
            this.servletContext.process(request, response);
        } catch (Exception e) {
            log.error("Servlet process exception", e);
        }
    }
}
