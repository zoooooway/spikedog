package org.zoooooway.spikedog.listener;

import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zoooooway
 */
@WebListener
public class ServletRequestListenerImpl implements ServletRequestListener {
    static final Logger log = LoggerFactory.getLogger(ServletRequestListenerImpl.class);

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        log.info("ServletRequest: [{}] destroyed.", sre.getServletRequest().getRequestId());
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        log.info("ServletRequest: [{}] initialized.", sre.getServletRequest().getRequestId());
    }
}
