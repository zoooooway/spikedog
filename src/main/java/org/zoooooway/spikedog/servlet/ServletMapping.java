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
        this.pattern = buildPattern(urlPattern);
        this.servlet = servlet;
    }

    Pattern buildPattern(String urlPattern) {
        StringBuilder sb = new StringBuilder(urlPattern.length() + 16);
        sb.append('^');
        for (int i = 0; i < urlPattern.length(); i++) {
            char ch = urlPattern.charAt(i);
            if (ch == '*') {
                sb.append(".*");
            } else if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9') {
                sb.append(ch);
            } else {
                sb.append('\\').append(ch);
            }
        }
        sb.append('$');
        return Pattern.compile(sb.toString());
    }

    public Servlet getServlet() {
        return servlet;
    }

    public boolean match(String path) {
        return this.pattern.matcher(path).matches();
    }
}
