package org.zoooooway.spikedog;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.zoooooway.spikedog.filter.FilterChainImpl;
import org.zoooooway.spikedog.filter.FilterMapping;
import org.zoooooway.spikedog.servlet.ServletContextImpl;
import org.zoooooway.spikedog.servlet.ServletMapping;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zoooooway
 */
public class HttpConnector implements HttpHandler {

    final ServletContextImpl servletContext;

    public HttpConnector(List<Class<? extends Servlet>> servletClasses, List<Class<? extends Filter>> filterClasses) {
        this.servletContext = new ServletContextImpl();
        this.servletContext.initServlets(servletClasses);
        this.servletContext.initFilters(filterClasses);
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
                List<Filter> filterList = findFilters(uri);
                FilterChain filterChain = new FilterChainImpl(filterList, servletMapping.getServlet());
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 未匹配到servlet，返回404
        try (PrintWriter writer = response.getWriter()) {
            writer.write("<h1>404 Not Found</h1><p>No mapping for URL: " + uri + "</p>");
            response.setStatus(404);
            response.flushBuffer();
        }
    }

    private List<Filter> findFilters(String uri) {
        List<FilterMapping> filterMappingList = this.servletContext.getFilterMappingList();
        return filterMappingList.stream().filter(f -> f.match(uri)).map(FilterMapping::getFilter).collect(Collectors.toList());
    }

}
