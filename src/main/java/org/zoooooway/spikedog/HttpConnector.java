package org.zoooooway.spikedog;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.zoooooway.spikedog.servlet.HelloServlet;
import org.zoooooway.spikedog.servlet.IndexServlet;
import org.zoooooway.spikedog.servlet.ServletContextImpl;
import org.zoooooway.spikedog.servlet.ServletMapping;

import java.io.IOException;
import java.util.List;

/**
 * @author zoooooway
 */
public class HttpConnector implements HttpHandler {

    final ServletContextImpl servletContext;

    public HttpConnector() {
        this.servletContext = new ServletContextImpl();
        this.servletContext.initialize(List.of(IndexServlet.class, HelloServlet.class));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpExchangeAdapter exchangeAdapter = new HttpExchangeAdapter(exchange);
        try {
            process(new HttpExchangeRequestImpl(exchangeAdapter), new HttpExchangeResponseImpl(exchangeAdapter));
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }


    public void process(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String uri = request.getRequestURI();
        List<ServletMapping> servletMappingList = servletContext.getServletMappingList();
        for (ServletMapping servletMapping : servletMappingList) {
            if (servletMapping.getPattern().matcher(uri).matches()) {
                servletMapping.getServlet().service(request, response);
                return;
            }
        }
    }

}
