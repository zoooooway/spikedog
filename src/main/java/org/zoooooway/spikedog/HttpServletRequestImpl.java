package org.zoooooway.spikedog;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zoooooway.spikedog.connector.HttpExchangeRequest;
import org.zoooooway.spikedog.servlet.ServletContextImpl;
import org.zoooooway.spikedog.session.HttpSessionImpl;
import org.zoooooway.spikedog.util.Parameters;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;

/**
 * @author zoooooway
 */
public class HttpServletRequestImpl implements HttpServletRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpServletRequestImpl.class);

    final HttpExchangeRequest request;
    final HttpServletResponse response;
    final ServletContextImpl servletContext;
    final Parameters parameters;
    final Map<String, Object> attributes;
    final String requestId;

    ServletInputStream input;
    BufferedReader reader;
    boolean callInput;
    boolean callReader;

    public HttpServletRequestImpl(HttpExchangeRequest request, HttpServletResponse response, ServletContextImpl servletContext) {
        this(request, response, servletContext, new HashMap<>());
    }

    public HttpServletRequestImpl(HttpExchangeRequest request, HttpServletResponse response, ServletContextImpl servletContext, Map<String, Object> attributes) {
        this.request = request;
        this.response = response;
        this.servletContext = servletContext;
        this.attributes = attributes;
        this.requestId = UUID.randomUUID().toString();
        this.parameters = new Parameters(request, StandardCharsets.UTF_8);
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        return this.request.getCookies();
    }

    @Override
    public long getDateHeader(String name) {
        return 0;
    }

    @Override
    public String getHeader(String name) {
        return this.request.getRequestHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return null;
    }

    @Override
    public int getIntHeader(String name) {
        return 0;
    }

    @Override
    public String getMethod() {
        return this.request.getMethod();
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public String getQueryString() {
        return null;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        Cookie[] cookies = this.getCookies();
        if (cookies == null) {
            return null;
        }

        String sessionId = null;
        for (Cookie cookie : cookies) {
            if ("JSESSIONID".equals(cookie.getName())) {
                sessionId = cookie.getValue();
                break;
            }
        }
        return sessionId;
    }

    @Override
    public String getRequestURI() {
        return this.request.getRequestURI().getPath();
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    @Override
    public String getServletPath() {
        return null;
    }

    @Override
    public HttpSession getSession(boolean create) {
        String sessionId = this.getRequestedSessionId();
        if (sessionId == null) {
            if (create) {
                sessionId = UUID.randomUUID().toString();
                return getOrCreateSession(true, sessionId);
            }

            return null;
        }

        return getOrCreateSession(create, sessionId);
    }

    private HttpSessionImpl getOrCreateSession(boolean create, String sessionId) {
        HttpSessionImpl session = this.servletContext.getSessionManager().getSession(sessionId, create);
        if (session == null) {
            return null;
        }

        // 写入响应
        if (response.isCommitted()) {
            // 请求已提交
            throw new IllegalStateException("Cannot create session for response is committed.");
        }

        String cookieName = sessionId;
        String cookieValue = "JSESSIONID=" + sessionId + "; Path=/; SameSite=Strict; HttpOnly";
        Cookie cookie = new Cookie(cookieName, cookieValue);

        this.response.addCookie(cookie);
        return session;
    }

    @Override
    public HttpSession getSession() {
        return this.getSession(true);
    }

    @Override
    public String changeSessionId() {
        HttpSession session = this.getSession(false);
        if (session == null) {
            throw new IllegalStateException("There is no session associated with the request.");
        }
        return this.servletContext.getSessionManager().changeSessionId((HttpSessionImpl) session);
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
        try {
            if (this.input == null) {
                return -1;
            }

            return this.input.available();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getContentLengthLong() {
        return 0;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (callReader) {
            throw new IllegalStateException("Cannot open input stream when reader is opened.");
        }

        if (callInput) {
            return this.input;
        }

        InputStream is = this.request.getRequestBody();
        this.input = new ServletInputStreamImpl(is);
        this.callInput = true;
        return this.input;
    }

    @Override
    public String getParameter(String name) {
        return this.parameters.getParameter(name);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return this.parameters.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String name) {
        return this.parameters.getParameterValues(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return this.parameters.getParameterMap();
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getServerName() {
        return null;
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (callInput) {
            throw new IllegalStateException("Cannot open reader when input stream is opened.");
        }

        if (callReader) {
            return this.reader;
        }

        this.reader = new BufferedReader(new InputStreamReader(this.request.getRequestBody()));
        this.callReader = true;
        return this.reader;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public void setAttribute(String name, Object o) {
        Object old = this.attributes.put(name, o);
        if (old == null) {
            this.invokeServletRequestAttributeAdded(this.getServletContext(), this, name, o);
            return;
        }

        // replaced
        this.invokeServletRequestAttributeReplaced(this.getServletContext(), this, name, old);
    }

    @Override
    public void removeAttribute(String name) {
        Object remove = this.attributes.remove(name);
        this.invokeServletRequestAttributeRemoved(this.getServletContext(), this, name, remove);
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }

    @Override
    public String getRequestId() {
        return this.requestId;
    }

    @Override
    public String getProtocolRequestId() {
        return null;
    }

    @Override
    public ServletConnection getServletConnection() {
        return null;
    }


    void invokeServletRequestAttributeAdded(ServletContext sc, ServletRequest request, String name, Object value) {
        log.debug("Invoke servlet request attribute added listener.  Request: {}, Name: {}, value: {}", request.getRequestId(), name, value);

        List<ServletRequestAttributeListener> listeners = this.servletContext.getServletRequestAttributeListeners();
        if (listeners.isEmpty()) {
            return;
        }

        ServletRequestAttributeEvent event = new ServletRequestAttributeEvent(sc, request, name, value);
        for (ServletRequestAttributeListener listener : listeners) {
            listener.attributeAdded(event);
        }
    }

    void invokeServletRequestAttributeRemoved(ServletContext sc, ServletRequest request, String name, Object value) {
        log.debug("Invoke servlet request attribute removed listener. Request: {}, Name: {}, value: {}", request.getRequestId(), name, value);

        List<ServletRequestAttributeListener> listeners = this.servletContext.getServletRequestAttributeListeners();
        if (listeners.isEmpty()) {
            return;
        }

        ServletRequestAttributeEvent event = new ServletRequestAttributeEvent(sc, request, name, value);
        for (ServletRequestAttributeListener listener : listeners) {
            listener.attributeRemoved(event);
        }
    }

    void invokeServletRequestAttributeReplaced(ServletContext sc, ServletRequest request, String name, Object value) {
        log.debug("Invoke servlet request attribute replaced listener. Request: {}, Name: {}, value: {}", request.getRequestId(), name, value);

        List<ServletRequestAttributeListener> listeners = this.servletContext.getServletRequestAttributeListeners();
        if (listeners.isEmpty()) {
            return;
        }

        ServletRequestAttributeEvent event = new ServletRequestAttributeEvent(sc, request, name, value);
        for (ServletRequestAttributeListener listener : listeners) {
            listener.attributeReplaced(event);
        }
    }

}
