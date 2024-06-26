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
        // 在链的末尾调用 Servlet 来处理请求
        if (filters.size() == index) {
            servlet.service(request, response);
            return;
        }
        Filter filter = filters.get(index);
        index++;
        filter.doFilter(request, response, this);
    }
}
