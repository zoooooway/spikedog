package org.zoooooway.spikedog.session;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import java.util.Enumeration;

/**
 * @author zoooooway
 */
public class HttpSessionWrapper implements HttpSession {
    final HttpSession session;
    protected boolean valid = false;

    public HttpSessionWrapper(HttpSession session) {
        this.session = session;
    }

    protected void checkValid() {
        if (!this.valid || System.currentTimeMillis() - this.session.getLastAccessedTime() > this.session.getMaxInactiveInterval()) {
            throw new IllegalStateException("Invalid session.");
        }
    }

    @Override
    public long getCreationTime() {
        checkValid();
        return this.session.getCreationTime();
    }

    @Override
    public String getId() {
        return this.session.getId();
    }

    @Override
    public long getLastAccessedTime() {
        checkValid();
        return this.session.getLastAccessedTime();
    }

    @Override
    public ServletContext getServletContext() {
        return this.session.getServletContext();
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.session.setMaxInactiveInterval(interval);
    }

    @Override
    public int getMaxInactiveInterval() {
        return this.session.getMaxInactiveInterval();
    }

    @Override
    public Object getAttribute(String name) {
        checkValid();
        return this.session.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        checkValid();
        return this.session.getAttributeNames();
    }

    @Override
    public void setAttribute(String name, Object value) {
        checkValid();
        this.session.setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        checkValid();
        this.session.removeAttribute(name);
    }

    @Override
    public void invalidate() {
        checkValid();
        this.session.invalidate();
    }

    @Override
    public boolean isNew() {
        checkValid();
        return this.session.isNew();
    }
}
