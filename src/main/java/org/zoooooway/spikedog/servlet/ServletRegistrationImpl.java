package org.zoooooway.spikedog.servlet;

import jakarta.servlet.*;

import java.util.*;

/**
 * @author zoooooway
 */
public class ServletRegistrationImpl implements ServletRegistration.Dynamic {

    final Servlet servlet;
    final String servletName;
//    final ServletConfig servletConfig;
    final Set<String> mappings;
    final Map<String, String> initParameters = new HashMap<>();

    public ServletRegistrationImpl(ServletContext servletContext, String servletName, Servlet servlet, Set<String> mappings) {
        this.servletName = servletName;
        this.servlet = servlet;
        this.mappings = mappings;
        Enumeration<String> enumeration = servletContext.getInitParameterNames();
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            this.initParameters.put(key, servletContext.getInitParameter(key));
        }
//        this.servletConfig = new ServletConfigImpl(servletName, servletContext, this.initParameters);
    }

    @Override
    public Set<String> addMapping(String... urlPatterns) {
        this.mappings.addAll(Arrays.asList(urlPatterns));
        return this.mappings;
    }

    @Override
    public Collection<String> getMappings() {
        return this.mappings;
    }

    @Override
    public String getName() {
        return this.servletName;
    }

    @Override
    public String getClassName() {
        return this.servlet.getClass().getName();
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        if (this.initParameters.containsKey(name)) {
            return false;
        }

        this.initParameters.put(name, value);
        return true;
    }

    @Override
    public String getInitParameter(String name) {
        return this.initParameters.get(name);
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters) {
        HashSet<String> conflicts = new HashSet<>();
        initParameters.forEach((key, value) -> {
            if (!this.setInitParameter(key, value)) {
                conflicts.add(key);
            }
        });
        return conflicts;
    }

    @Override
    public Map<String, String> getInitParameters() {
        return this.initParameters;
    }

    @Override
    public void setLoadOnStartup(int loadOnStartup) {

    }

    @Override
    public Set<String> setServletSecurity(ServletSecurityElement constraint) {
        return null;
    }

    @Override
    public void setMultipartConfig(MultipartConfigElement multipartConfig) {

    }

    @Override
    public void setRunAsRole(String roleName) {

    }

    @Override
    public void setAsyncSupported(boolean isAsyncSupported) {

    }

    @Override
    public String getRunAsRole() {
        return null;
    }

}
