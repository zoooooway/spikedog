package org.zoooooway.spikedog.filter;

import jakarta.servlet.*;

import java.io.IOException;
import java.util.List;

/**
 * @author zoooooway
 */
public class FilterChainImpl implements FilterChain {

    final List<Filter> filters;
    final Servlet servlet;
    int index;

    public FilterChainImpl(List<Filter> filters, Servlet servlet) {
        this.filters = filters;
        this.servlet = servlet;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if (filters.size() == index) {
            servlet.service(request, response);
        }
        Filter filter = filters.get(index);
        index++;
        filter.doFilter(request, response, this);
    }
}
