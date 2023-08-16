package org.zoooooway.spikedog.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zoooooway
 */
@WebListener
public class ServletContextListenerImpl implements ServletContextListener {
    static final Logger log = LoggerFactory.getLogger(ServletContextListenerImpl.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("ServletContext: [{}] created.", sce.getServletContext().getServletContextName());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("ServletContext: [{}] destroyed.", sce.getServletContext().getServletContextName());
    }
}
