package org.zoooooway.spikedog.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.descriptor.JspConfigDescriptor;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author zoooooway
 */
public class ServletContextImpl implements ServletContext {

    List<ServletMapping> servletMappingList = new ArrayList<>();
    Map<String, ServletRegistration.Dynamic> servletRegistrationMap = new HashMap<>();
    Map<String, String> initParameters = new HashMap<>();

    public void initialize(List<Class<? extends Servlet>> servletClasses)  {
        for (var servletClass : servletClasses) {
            // 创建servlet
            Constructor<? extends Servlet> constructor;
            Servlet servlet;
            try {
                constructor = servletClass.getConstructor();
                servlet = constructor.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("create servlet failed", e);
            }

            WebServlet ws = servletClass.getAnnotation(WebServlet.class);
            String[] urlPatterns = ws.urlPatterns();
            WebInitParam[] webInitParams = ws.initParams();
            HashMap<String, String> initParameters = new HashMap<>();
            for (WebInitParam webInitParam : webInitParams) {
                initParameters.put(webInitParam.name(), webInitParam.value());
            }
            String servletName = ws.name();
            try {
                servlet.init(new ServletConfigImpl(servletName, this, initParameters));
            } catch (ServletException e) {
                throw new RuntimeException("init servlet failed", e);
            }

            // 注册servlet到容器中
            ServletRegistration.Dynamic dynamic = this.addServlet(servletName, servlet);
            dynamic.addMapping(urlPatterns);

            for (String urlPattern : urlPatterns) {
                ServletMapping servletMapping = new ServletMapping(urlPattern, servlet);
                servletMappingList.add(servletMapping);
            }
        }

    }

    public List<ServletMapping> getServletMappingList() {
        return servletMappingList;
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        try {
            Class<?> servletClass = Class.forName(className);
            return addServlet(servletName, servletClass.asSubclass(Servlet.class));

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        if (servletRegistrationMap.containsKey(servletName)) {
            return null;
        }
        var dynamic = new ServletRegistrationImpl(servlet, new HashSet<>());
        dynamic.setInitParameters(this.initParameters);
        servletRegistrationMap.put(servletName, dynamic);
        return dynamic;
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        try {
            Constructor<? extends Servlet> constructor = servletClass.getConstructor();
            Servlet servlet = constructor.newInstance();
            return addServlet(servletName, servlet);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public ServletContext getContext(String uripath) {
        return this;
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return 0;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return 0;
    }

    @Override
    public String getMimeType(String file) {
        return null;
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        return null;
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        return null;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        return null;
    }

    @Override
    public void log(String msg) {

    }

    @Override
    public void log(String message, Throwable throwable) {

    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public String getServerInfo() {
        return null;
    }

    @Override
    public String getInitParameter(String name) {
        return this.initParameters.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        Iterator<String> iterator = this.initParameters.keySet().iterator();
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
    public boolean setInitParameter(String name, String value) {
        if (this.initParameters.containsKey(name)) {
            return false;
        }
        this.initParameters.put(name, value);
        return false;
    }

    @Override
    public Object getAttribute(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public void setAttribute(String name, Object object) {

    }

    @Override
    public void removeAttribute(String name) {

    }

    @Override
    public String getServletContextName() {
        return null;
    }

    @Override
    public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
        return null;
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        return null;
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        return null;
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        return null;
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        return null;
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        return null;
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return null;
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return null;
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {

    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return null;
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return null;
    }

    @Override
    public void addListener(String className) {

    }

    @Override
    public <T extends EventListener> void addListener(T t) {

    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {

    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        return null;
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public void declareRoles(String... roleNames) {

    }

    @Override
    public String getVirtualServerName() {
        return null;
    }

    @Override
    public int getSessionTimeout() {
        return 0;
    }

    @Override
    public void setSessionTimeout(int sessionTimeout) {

    }

    @Override
    public String getRequestCharacterEncoding() {
        return null;
    }

    @Override
    public void setRequestCharacterEncoding(String encoding) {

    }

    @Override
    public String getResponseCharacterEncoding() {
        return null;
    }

    @Override
    public void setResponseCharacterEncoding(String encoding) {

    }
}
