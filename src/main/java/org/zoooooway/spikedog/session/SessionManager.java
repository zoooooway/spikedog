package org.zoooooway.spikedog.session;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import org.zoooooway.spikedog.HttpExchangeRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author zoooooway
 */
public class SessionManager {
    final ServletContext servletContext;
    Map<String, HttpSession> sessionMap = new HashMap<>();
    int interval;

    public SessionManager(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public HttpSession getSession(HttpExchangeRequest request) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if ("".equals(cookie.getName())) {

            }
        }
        return null;
    }

    public HttpSession getSession(String sessionId) {
        if (this.sessionMap.get(sessionId) == null) {

        }
        return null;
    }

    private HttpSession createSession() {
        UUID uuid = UUID.randomUUID();
        String sessionId = uuid.toString();
        HttpSessionImpl httpSession = new HttpSessionImpl(sessionId, interval, new HashMap<>(), servletContext);
        sessionMap.put(sessionId, httpSession);
        return httpSession;
    }


}
