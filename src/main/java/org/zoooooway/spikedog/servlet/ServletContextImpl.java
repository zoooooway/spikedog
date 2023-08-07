package org.zoooooway.spikedog.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import org.zoooooway.spikedog.filter.FilterConfigImpl;
import org.zoooooway.spikedog.filter.FilterMapping;
import org.zoooooway.spikedog.filter.FilterRegistrationImpl;
import org.zoooooway.spikedog.session.SessionManager;

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
    List<FilterMapping> filterMappingList = new ArrayList<>();
    Map<String, ServletRegistration.Dynamic> servletRegistrationMap = new HashMap<>();
    Map<String, FilterRegistration.Dynamic> filterRegistrationMap = new HashMap<>();
    Map<String, String> initParameters = new HashMap<>();

    SessionManager sessionManager;

    public void initServlets(List<Class<? extends Servlet>> servletClasses) {
        for (var servletClass : servletClasses) {
            // 创建servlet
            Constructor<? extends Servlet> constructor;
            Servlet servlet;
            try {
                constructor = servletClass.getConstructor();
                servlet = constructor.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(String.format("create servlet [%s] failed", servletClass.getName()), e);
            }

            WebServlet ws = servletClass.getAnnotation(WebServlet.class);
            // 注册servlet到容器中
            String servletName = ws.name();
            ServletRegistration.Dynamic dynamic = this.addServlet(servletName, servlet);
            String[] urlPatterns = ws.urlPatterns();
            dynamic.addMapping(urlPatterns);

            WebInitParam[] webInitParams = ws.initParams();
            HashMap<String, String> initParameters = new HashMap<>();
            for (WebInitParam webInitParam : webInitParams) {
                initParameters.put(webInitParam.name(), webInitParam.value());
            }
            try {
                // init servlet
                servlet.init(new ServletConfigImpl(servletName, this, initParameters));
            } catch (ServletException e) {
                throw new RuntimeException(String.format("init servlet [%s] failed", servletName), e);
            }

            for (String urlPattern : urlPatterns) {
                ServletMapping servletMapping = new ServletMapping(urlPattern, servlet);
                servletMappingList.add(servletMapping);
            }
        }

    }


    public void initFilters(List<Class<? extends Filter>> filterClasses) {
        for (var filterClass : filterClasses) {
            Constructor<? extends Filter> constructor;
            Filter filter;
            try {
                constructor = filterClass.getConstructor();
                filter = constructor.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(String.format("create filter [%s] failed", filterClass.getName()), e);
            }


            // 注册filter到容器中
            WebFilter wf = filterClass.getAnnotation(WebFilter.class);
            String filterName = wf.filterName();
            FilterRegistration.Dynamic dynamic = this.addFilter(filterName, filter);
            String[] urlPatterns = wf.urlPatterns();
            EnumSet<DispatcherType> dispatcherTypes = EnumSet.noneOf(DispatcherType.class);
            dispatcherTypes.addAll(List.of(wf.dispatcherTypes()));
            dynamic.addMappingForUrlPatterns(dispatcherTypes, false, urlPatterns);

            WebInitParam[] webInitParams = wf.initParams();
            HashMap<String, String> initParameters = new HashMap<>();
            for (WebInitParam webInitParam : webInitParams) {
                initParameters.put(webInitParam.name(), webInitParam.value());
            }
            try {
                filter.init(new FilterConfigImpl(filterName, this, initParameters));
            } catch (ServletException e) {
                throw new RuntimeException(String.format("init filter [%s] failed", filterName), e);
            }

            for (String urlPattern : urlPatterns) {
                FilterMapping filterMapping = new FilterMapping(urlPattern, filter);
                filterMappingList.add(filterMapping);
            }
        }
    }

    public List<ServletMapping> getServletMappingList() {
        return servletMappingList;
    }

    public List<FilterMapping> getFilterMappingList() {
        return filterMappingList;
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
        var dynamic = new ServletRegistrationImpl(this, servletName, servlet, new HashSet<>());
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
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        return null;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        if (filterRegistrationMap.containsKey(filterName)) {
            return null;
        }
        var dynamic = new FilterRegistrationImpl(filterName, filter, new HashSet<>());
        dynamic.setInitParameters(this.initParameters);
        filterRegistrationMap.put(filterName, dynamic);
        return dynamic;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        return null;
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

    public SessionManager getSessionManager() {
        return this.sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
}
