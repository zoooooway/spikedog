package org.zoooooway.spikedog.session;

import org.zoooooway.spikedog.servlet.ServletContextImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zoooooway
 */
public class SessionManager {
    final ServletContextImpl servletContext;
    Map<String, HttpSessionImpl> sessionMap = new ConcurrentHashMap<>();
    int interval;

    public SessionManager(ServletContextImpl servletContext) {
        this.servletContext = servletContext;
    }

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

    private HttpSessionImpl createSession(String sessionId) {
        HttpSessionImpl httpSession = new HttpSessionImpl(sessionId, this.interval, new ConcurrentHashMap<>(), this.servletContext);
        sessionMap.put(sessionId, httpSession);
        return httpSession;
    }


    public void remove(HttpSessionImpl httpSession) {
        this.sessionMap.remove(httpSession.getId());
    }
}
