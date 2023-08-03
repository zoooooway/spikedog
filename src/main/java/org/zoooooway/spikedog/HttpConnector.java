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
import java.io.PrintWriter;
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
            if (servletMapping.match(uri)) {
                servletMapping.getServlet().service(request, response);
                return;
            }
        }

        // 未匹配到servlet，返回404
        try (PrintWriter writer = response.getWriter()) {
            writer.write("<h1>404 Not Found</h1><p>No mapping for URL: " + uri + "</p>");
        }
    }

}
