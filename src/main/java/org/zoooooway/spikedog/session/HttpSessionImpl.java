package org.zoooooway.spikedog.session;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zoooooway.spikedog.servlet.ServletContextImpl;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author zoooooway
 */
public class HttpSessionImpl implements HttpSession {
    private static final Logger log = LoggerFactory.getLogger(SessionManager.class.getName());

    String sessionId;
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
        if (value instanceof HttpSessionBindingListener boundValue) {
            boundValue.valueBound(new HttpSessionBindingEvent(this, name, value));
        }

        Object old = this.attributes.put(name, value);
        if (old == null) {
            this.invokeSessionAttributeAdded(this, name, value);
            return;
        }

        // replaced
        if (old instanceof HttpSessionBindingListener oldValue) {
            oldValue.valueUnbound(new HttpSessionBindingEvent(this, name, old));
        }
        this.invokeSessionAttributeReplaced(this, name, old);
    }

    @Override
    public void removeAttribute(String name) {
        Object remove = this.attributes.remove(name);
        this.invokeSessionAttributeRemoved(this, name, remove);
    }

    @Override
    public void invalidate() {
        this.servletContext.getSessionManager().invalidate(this);
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

    void invokeSessionAttributeAdded(HttpSession session, String name, Object value) {
        log.info("Invoke session attribute removed listener. SessionId: {}, name: {}, value: {}", session.getId(), name, value);

        List<HttpSessionAttributeListener> listeners = this.servletContext.getHttpSessionAttributeListeners();
        if (listeners.isEmpty()) {
            return;
        }

        HttpSessionBindingEvent event = new HttpSessionBindingEvent(session, name, value);
        for (HttpSessionAttributeListener listener : listeners) {
            listener.attributeAdded(event);
        }
    }

    void invokeSessionAttributeRemoved(HttpSession session, String name, Object value) {
        log.info("Invoke session attribute removed listener. Session id: {}, name: {}, value: {}", session.getId(), name, value);

        List<HttpSessionAttributeListener> listeners = this.servletContext.getHttpSessionAttributeListeners();
        if (listeners.isEmpty()) {
            return;
        }

        HttpSessionBindingEvent event = new HttpSessionBindingEvent(session, name, value);
        for (HttpSessionAttributeListener listener : listeners) {
            listener.attributeRemoved(event);
        }
    }

    void invokeSessionAttributeReplaced(HttpSession session, String name, Object value) {
        log.debug("Invoke session attribute removed listener. Session id: {}, name: {}, value: {}", session.getId(), name, value);

        List<HttpSessionAttributeListener> listeners = this.servletContext.getHttpSessionAttributeListeners();
        if (listeners.isEmpty()) {
            return;
        }

        HttpSessionBindingEvent event = new HttpSessionBindingEvent(session, name, value);
        for (HttpSessionAttributeListener listener : listeners) {
            listener.attributeReplaced(event);
        }
    }
}
