package org.zoooooway.spikedog.servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

/**
 * @author zoooooway
 */
public class ServletConfigImpl implements ServletConfig {
    final String servletName;
    final ServletContext servletContext;
    final Map<String, String> initParameters;

    public ServletConfigImpl(String servletName, ServletContext servletContext, Map<String, String> initParameters) {
        this.servletName = servletName;
        this.servletContext = servletContext;
        this.initParameters = initParameters;
    }

    @Override
    public String getServletName() {
        return this.servletName;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public String getInitParameter(String name) {
        return this.initParameters.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        Iterator<String> iterator = initParameters.keySet().iterator();
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
}