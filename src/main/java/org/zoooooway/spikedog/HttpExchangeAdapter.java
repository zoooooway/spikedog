package org.zoooooway.spikedog;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import org.zoooooway.spikedog.session.SessionManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
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
    int status;
    int responseLength;

    SessionManager sessionManager;

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
    public HttpSession getSession(boolean create) {
        this.sessionManager.getSession(this);
        return null;
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
