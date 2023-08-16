package org.zoooooway.spikedog.listener;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zoooooway
 */
@WebListener
public class HttpSessionListenerImpl implements HttpSessionListener {
    static final Logger log = LoggerFactory.getLogger(HttpSessionListenerImpl.class);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        log.info("Session: [{}] created.", se.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        log.info("Session: [{}] destroyed.", se.getSession().getId());
    }
}
