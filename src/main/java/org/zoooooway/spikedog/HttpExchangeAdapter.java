package org.zoooooway.spikedog;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import jakarta.servlet.http.Cookie;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author zoooooway
 */
public class HttpExchangeAdapter implements HttpExchangeRequest, HttpExchangeResponse {
    final HttpExchange httpExchange;

    public HttpExchangeAdapter(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
    }

    @Override
    public URI getRequestURI() {
        return httpExchange.getRequestURI();
    }

    @Override
    public String getMethod() {
        return httpExchange.getRequestMethod();
    }

    @Override
    public String getRequestHeader(String name) {
        Headers requestHeaders = this.httpExchange.getRequestHeaders();
        List<String> list = requestHeaders.get(name);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public Cookie[] getCookies() {
        Headers requestHeaders = this.httpExchange.getRequestHeaders();
        List<String> cookieValues = requestHeaders.get("cookie");
        List<Cookie> cookieList = new ArrayList<>();
        for (String ck : cookieValues) {
            String[] cookieItems = ck.split(";");
            for (String cookieItem : cookieItems) {
                String[] split = cookieItem.split("=");
                Cookie cookie = new Cookie(split[0], split[1]);
                cookieList.add(cookie);
            }
        }
        return cookieList.toArray(new Cookie[0]);
    }

    @Override
    public InputStream getRequestBody() {
        return this.httpExchange.getRequestBody();
    }

    @Override
    public String getRequestMethod() {
        return this.httpExchange.getRequestMethod();
    }

    @Override
    public Headers getRequestHeaders() {
        return this.httpExchange.getRequestHeaders();
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
    public void addHeader(String name, String value) {
        this.httpExchange.getResponseHeaders().add(name, value);
    }

    @Override
    public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
        this.httpExchange.sendResponseHeaders(rCode, responseLength);
    }

    @Override
    public OutputStream getResponseBody() {
        return this.httpExchange.getResponseBody();
    }
}
