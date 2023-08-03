package org.zoooooway.spikedog;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author zoooooway
 */
public class HttpExchangeAdapter implements HttpExchangeRequest, HttpExchangeResponse {
    final HttpExchange httpExchange;
    int status;
    int responseLength;

    public HttpExchangeAdapter(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
    }

    @Override
    public InputStream getInputStream() {
        return this.httpExchange.getRequestBody();
    }

    @Override
    public String getRequestURI() {
        return httpExchange.getRequestURI().getPath();
    }

    @Override
    public String getMethod() {
        return httpExchange.getRequestMethod();
    }

    @Override
    public int getContentLength() throws IOException {
        return this.httpExchange.getRequestBody().available();
    }

    @Override
    public String getParameter(String name) {
        String query = this.httpExchange.getRequestURI().getRawQuery();
        if (query != null) {
            Map<String, String> params = parseQuery(query);
            return params.get(name);
        }
        return null;
    }

    Map<String, String> parseQuery(String query) {
        if (query == null || query.isEmpty()) {
            return Map.of();
        }
        String[] ss = Pattern.compile("\\&").split(query);
        Map<String, String> map = new HashMap<>();
        for (String s : ss) {
            int n = s.indexOf('=');
            if (n >= 1) {
                String key = s.substring(0, n);
                String value = s.substring(n + 1);
                map.putIfAbsent(key, URLDecoder.decode(value, StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    @Override
    public void setHeader(String name, String value) {
        this.httpExchange.getResponseHeaders().set(name, value);
    }

    @Override
    public void setStatus(int sc) {
        this.status = sc;
    }

    @Override
    public void setContentLength(int len) {
        this.responseLength = len;
    }

    @Override
    public void flushBuffer() throws IOException {
        this.httpExchange.sendResponseHeaders(status, responseLength);
    }

    @Override
    public PrintWriter getWriter() {
        OutputStream os = this.httpExchange.getResponseBody();
        return new PrintWriter(os);
    }
}
