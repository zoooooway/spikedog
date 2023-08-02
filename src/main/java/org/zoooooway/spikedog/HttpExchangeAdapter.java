package org.zoooooway.spikedog;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

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
        return httpExchange.getRequestURI().toString();
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
