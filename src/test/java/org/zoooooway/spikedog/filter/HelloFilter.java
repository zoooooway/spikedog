package org.zoooooway.spikedog.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author zoooooway
 */
@WebFilter(filterName = "helloFilter", urlPatterns = "/hello")
public class HelloFilter implements Filter {
    private final Logger log = LoggerFactory.getLogger(HelloFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest && response instanceof HttpServletResponse)) {
            throw new ServletException("non-HTTP request or response");
        }

        String uri = ((HttpServletRequest) request).getRequestURI();
        if ("/hello".equals(uri)) {
            String name = request.getParameter("name");
            if (!"spike".equals(name)) {
                ((HttpServletResponse) response).sendError(403, "Forbidden");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
