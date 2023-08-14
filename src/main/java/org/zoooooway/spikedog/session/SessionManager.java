package org.zoooooway.spikedog.session;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zoooooway.spikedog.servlet.ServletContextImpl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zoooooway
 */
public class SessionManager implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(SessionManager.class.getName());

    final ServletContextImpl servletContext;
    Map<String, HttpSessionImpl> sessionMap = new SessionMap<>();
    int interval = Integer.MAX_VALUE;

    public SessionManager(ServletContextImpl servletContext) {
        this.servletContext = servletContext;
        Thread t = new Thread(this, "Session-Cleanup-Thread");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void run() {
        // clean invalidated session
        for (; ; ) {
            Set<String> keySet = this.sessionMap.keySet();
            for (String key : keySet) {
                HttpSessionImpl session = this.sessionMap.get(key);
                if (session == null || !session.isValid()) {
                    this.sessionMap.remove(key);
                }
            }

            try {
                Thread.sleep(60_000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Session cleanup thread has been interrupted.");
                break;
            }
        }
    }

    @Nullable
    public HttpSessionImpl getSession(String sessionId, boolean create) {
        HttpSessionImpl session = this.sessionMap.get(sessionId);
        if (session == null) {
            if (create) {
                return createSession(sessionId);
            }

            return null;
        }

        session.lastAccessedTime = System.currentTimeMillis();
        return session;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    private HttpSessionImpl createSession(String sessionId) {
        HttpSessionImpl httpSession = new HttpSessionImpl(sessionId, this.interval, new ConcurrentHashMap<>(), this.servletContext);
        this.sessionMap.put(sessionId, httpSession);

        // notify listener
        this.invokeSessionCreated(httpSession);

        return httpSession;
    }


    void invalidate(HttpSessionImpl httpSession) {
        // notify listener
        this.invokeSessionDestroyed(httpSession);

        this.sessionMap.remove(httpSession.getId());
    }

    /**
     * 更改与此请求关联的当前会话的session id，并返回新的session id。
     */
    public String changeSessionId(HttpSessionImpl session) {
        if (session == null) {
            throw new IllegalStateException("Session is null.");
        }

        String oldSessionId = session.getId();
        String newSessionId = UUID.randomUUID().toString();
        this.sessionMap.remove(oldSessionId);
        session.sessionId = newSessionId;
        this.sessionMap.put(newSessionId, session);

        // notify listener
        this.invokeSessionIdChanged(session, oldSessionId);

        return newSessionId;
    }


    void invokeSessionCreated(HttpSession session) {
        log.info("Invoke session created listener. Session id: {}", session.getId());

        List<HttpSessionListener> listeners = this.servletContext.getHttpSessionListeners();
        if (listeners.isEmpty()) {
            return;
        }

        HttpSessionEvent se = new HttpSessionEvent(session);
        for (HttpSessionListener listener : listeners) {
            listener.sessionCreated(se);
        }
    }

    void invokeSessionDestroyed(HttpSession session) {
        log.info("Invoke session destroyed listener. Session id: {}", session.getId());

        List<HttpSessionListener> listeners = this.servletContext.getHttpSessionListeners();
        if (listeners.isEmpty()) {
            return;
        }

        HttpSessionEvent se = new HttpSessionEvent(session);
        for (HttpSessionListener listener : listeners) {
            listener.sessionDestroyed(se);
        }
    }


    void invokeSessionWillPassivate(HttpSessionEvent se) {
        log.info("Invoke session will passivate listener. Session id: {}", se.getSession().getId());

        List<HttpSessionActivationListener> listeners = this.servletContext.getHttpSessionActivationListeners();
        if (listeners.isEmpty()) {
            return;
        }

        for (HttpSessionActivationListener listener : listeners) {
            listener.sessionWillPassivate(se);
        }
    }

    void invokeSessionDidActivate(HttpSessionEvent se) {
        log.info("Invoke session did activate listener. Session id: {}", se.getSession().getId());

        List<HttpSessionActivationListener> listeners = this.servletContext.getHttpSessionActivationListeners();
        if (listeners.isEmpty()) {
            return;
        }

        for (HttpSessionActivationListener listener : listeners) {
            listener.sessionDidActivate(se);
        }
    }


    void invokeSessionIdChanged(HttpSession session, String oldSessionId) {
        log.info("Invoke session id changed listener. Old session id: {}, new session id: {}", oldSessionId, session.getId());

        List<HttpSessionIdListener> listeners = this.servletContext.getHttpSessionIdListeners();
        if (listeners.isEmpty()) {
            return;
        }

        HttpSessionEvent event = new HttpSessionEvent(session);
        for (HttpSessionIdListener listener : listeners) {
            listener.sessionIdChanged(event, oldSessionId);
        }
    }

    static class SessionMap<String, V extends HttpSessionImpl> extends ConcurrentHashMap<String, V> {

        /**
         * 在获取value时判断其是否过期
         */
        @Override
        public V get(Object key) {
            V session = super.get(key);
            if (session == null) {
                return null;
            }

            if (!session.isValid()) {
                this.remove(key);
                return null;
            }

            return session;
        }
    }
}
