package org.zoooooway.spikedog.session;

import jakarta.annotation.Nullable;
import org.zoooooway.spikedog.servlet.ServletContextImpl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @author zoooooway
 */
public class SessionManager implements Runnable {
    static Logger log = Logger.getLogger(SessionManager.class.getName());

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
                log.warning("Session cleanup thread has been interrupted.");
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
        return httpSession;
    }


    public void remove(HttpSessionImpl httpSession) {
        this.sessionMap.remove(httpSession.getId());
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
