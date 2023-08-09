package org.zoooooway.spikedog.session;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import org.zoooooway.spikedog.servlet.ServletContextImpl;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

/**
 * @author zoooooway
 */
public class HttpSessionImpl implements HttpSession {
    final String sessionId;
    final long creationTime;
    final Map<String, Object> attributes;
    final ServletContextImpl servletContext;

    long lastAccessedTime;
    int maxInactiveInterval;

    public HttpSessionImpl(String sessionId, int maxInactiveInterval, Map<String, Object> attributes, ServletContextImpl servletContext) {
        this.sessionId = sessionId;
        this.attributes = attributes;
        this.servletContext = servletContext;
        this.maxInactiveInterval = maxInactiveInterval;
        long t = System.currentTimeMillis();
        this.creationTime = t;
        this.lastAccessedTime = t;
    }

    @Override
    public long getCreationTime() {
        return this.creationTime;
    }

    @Override
    public String getId() {
        return this.sessionId;
    }

    @Override
    public long getLastAccessedTime() {
        return this.lastAccessedTime;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    @Override
    public int getMaxInactiveInterval() {
        return this.maxInactiveInterval;
    }

    @Override
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        Iterator<String> iterator = this.attributes.keySet().iterator();
        return new Enumeration<>() {
            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public String nextElement() {
                return iterator.next();
            }
        };
    }

    @Override
    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    @Override
    public void invalidate() {
        this.servletContext.getSessionManager().remove(this);
    }

    @Override
    public boolean isNew() {
        return false;
    }

    public boolean isValid() {
        if (System.currentTimeMillis() - this.getLastAccessedTime() > this.getMaxInactiveInterval() * 1000L) {
            return false;
        }
        return true;
    }
}
