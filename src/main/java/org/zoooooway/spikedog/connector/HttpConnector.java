package org.zoooooway.spikedog.connector;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import org.zoooooway.spikedog.HttpServletRequestImpl;
import org.zoooooway.spikedog.HttpServletResponseImpl;
import org.zoooooway.spikedog.servlet.ServletContextImpl;
import org.zoooooway.spikedog.session.SessionManager;

import java.io.IOException;
import java.util.List;

/**
 * @author zoooooway
 */
public class HttpConnector implements HttpHandler {

    protected final ServletContextImpl servletContext;

    public HttpConnector(List<Class<? extends Servlet>> servletClasses, List<Class<? extends Filter>> filterClasses) {
        this.servletContext = new ServletContextImpl();
        this.servletContext.setSessionManager(new SessionManager(this.servletContext));

        this.servletContext.init(servletClasses, filterClasses);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpExchangeAdapter exchangeAdapter = new HttpExchangeAdapter(exchange);
        HttpServletResponseImpl response = new HttpServletResponseImpl(exchangeAdapter);
        HttpServletRequestImpl request = new HttpServletRequestImpl(exchangeAdapter, response, this.servletContext);
        try {
            this.servletContext.process(request, response);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }
}
