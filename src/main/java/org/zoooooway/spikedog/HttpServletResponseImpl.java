package org.zoooooway.spikedog;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.zoooooway.spikedog.connector.HttpExchangeResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;

/**
 * @author zoooooway
 */
public class HttpServletResponseImpl implements HttpServletResponse {
    final HttpExchangeResponse response;

    ServletOutputStream output;
    PrintWriter writer;
    boolean callOutput;
    boolean callWriter;

    boolean committed;
    int status;

    public HttpServletResponseImpl(HttpExchangeResponse httpExchangeResponse) {
        this.response = httpExchangeResponse;
    }

    @Override
    public void addCookie(Cookie cookie) {
        checkNotCommitted();
        this.response.addHeader("Set-Cookie", cookie.getValue());
    }

    @Override
    public boolean containsHeader(String name) {
        return false;
    }

    @Override
    public String encodeURL(String url) {
        return null;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return null;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        checkNotCommitted();
        this.status = sc;
        commitHeaders(-1);
        PrintWriter pw = getWriter();
        pw.write(String.format("<h1>%d %s</h1>", sc, msg));
        pw.close();
    }

    @Override
    public void sendError(int sc) throws IOException {
        checkNotCommitted();
        this.response.sendResponseHeaders(sc, -1);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        checkNotCommitted();
        this.status = 302;
        this.response.setHeader("Location", location);
        commitHeaders(-1);
    }

    @Override
    public void setDateHeader(String name, long date) {

    }

    @Override
    public void addDateHeader(String name, long date) {

    }

    @Override
    public void setHeader(String name, String value) {
        checkNotCommitted();
        this.response.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        checkNotCommitted();
        this.response.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {

    }

    @Override
    public void addIntHeader(String name, int value) {

    }

    @Override
    public void setStatus(int sc) {
        checkNotCommitted();
        this.status = sc;
    }

    @Override
    public int getStatus() {
        return this.status ;
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (this.callWriter) {
            throw new IllegalStateException("Cannot open output stream when writer is opened.");
        }

        if (this.callOutput) {
            return this.output;
        }

        OutputStream os = this.response.getResponseBody();
        commitHeaders(0);
        this.output = new ServletOutputStreamImpl(os);
        this.callOutput = true;
        return this.output;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (this.callOutput) {
            throw new IllegalStateException("Cannot open writer when writer output stream is opened.");
        }

        if (this.callWriter) {
            return this.writer;
        }

        OutputStream os = this.response.getResponseBody();
        commitHeaders(0);
        this.writer = new PrintWriter(os, true, StandardCharsets.UTF_8);
        this.callWriter = true;
        return this.writer;
    }


    void commitHeaders(long length) throws IOException {
        this.response.sendResponseHeaders(this.status, length);
        this.committed = true;
    }

    @Override
    public void setCharacterEncoding(String charset) {
    }

    @Override
    public void setContentLength(int len) {
    }

    @Override
    public void setContentLengthLong(long len) {

    }

    @Override
    public void setContentType(String type) {

    }

    @Override
    public void setBufferSize(int size) {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {
        commitHeaders(0);
        if (this.callOutput) {
            this.output.flush();
        }

        if (this.callWriter) {
            this.writer.flush();
        }
    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
        return this.committed;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale loc) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    void checkNotCommitted() {
        if (this.committed) {
            throw new IllegalStateException("Response is committed.");
        }
    }
}
