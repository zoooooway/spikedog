package org.zoooooway.spikedog.servlet;

import jakarta.servlet.Servlet;

import java.util.regex.Pattern;

/**
 * @author zoooooway
 */
public class ServletMapping {
    final Pattern pattern;
    final Servlet servlet;

    public ServletMapping(String urlPattern, Servlet servlet) {
        this.pattern = Pattern.compile(urlPattern);
        this.servlet = servlet;
    }

    public Servlet getServlet() {
        return servlet;
    }

    public boolean match(String path) {
        return this.pattern.matcher(path).matches();
    }
}
