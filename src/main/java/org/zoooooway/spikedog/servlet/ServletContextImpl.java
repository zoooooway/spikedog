package org.zoooooway.spikedog.servlet;

import jakarta.annotation.Nonnull;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zoooooway.spikedog.filter.FilterChainImpl;
import org.zoooooway.spikedog.filter.FilterConfigImpl;
import org.zoooooway.spikedog.filter.FilterMapping;
import org.zoooooway.spikedog.filter.FilterRegistrationImpl;
import org.zoooooway.spikedog.session.SessionManager;

import java.io.*;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zoooooway
 */
public class ServletContextImpl implements ServletContext, AutoCloseable {

    Logger log = LoggerFactory.getLogger(this.getClass());

    List<ServletMapping> servletMappingList = new ArrayList<>();
    List<FilterMapping> filterMappingList = new ArrayList<>();
    Map<String, ServletRegistration.Dynamic> servletRegistrationMap = new HashMap<>();
    Map<String, FilterRegistration.Dynamic> filterRegistrationMap = new HashMap<>();
    Map<String, String> initParameters = new HashMap<>();
    Map<String, Object> attributes = new HashMap<>();

    boolean initialized = false;

    // listener++

    List<HttpSessionListener> httpSessionListeners = new ArrayList<>();
    List<HttpSessionActivationListener> httpSessionActivationListeners = new ArrayList<>();
    List<HttpSessionAttributeListener> httpSessionAttributeListeners = new ArrayList<>();
    List<HttpSessionIdListener> httpSessionIdListeners = new ArrayList<>();

    List<ServletContextListener> servletContextListeners = new ArrayList<>();
    List<ServletRequestListener> servletRequestListeners = new ArrayList<>();
    List<ServletContextAttributeListener> servletContextAttributeListeners = new ArrayList<>();
    List<ServletRequestAttributeListener> servletRequestAttributeListeners = new ArrayList<>();

    // listener--

    SessionManager sessionManager;
    final Path webRoot;

    public ServletContextImpl(Path webRoot) {
        this.webRoot = webRoot;
    }


    public void init(Set<Class<? extends Servlet>> servletSet, Set<Class<? extends Filter>> filterSet, Set<Class<? extends EventListener>> listenerSet) {
        this.initServlets(servletSet);
        this.initFilters(filterSet);
        this.initListener(listenerSet);

        // notify listeners
        this.invokeServletContextInitialized(this);

        this.initialized = true;
    }

    @Nonnull
    private List<Class<? extends Filter>> getFilterClass(List<Class<?>> scannedClass) {
        return Collections.emptyList();
    }

    @Nonnull
    private List<Class<? extends EventListener>> getListenerClass(List<Class<?>> scannedClass) {
        return Collections.emptyList();
    }

    @Nonnull
    private List<Class<? extends Servlet>> getServletClass(List<Class<?>> scannedClass) {
        return Collections.emptyList();
    }


    public void destroy() {
        // clean
        servletMappingList.clear();
        servletRegistrationMap.clear();
        filterMappingList.clear();
        filterRegistrationMap.clear();

        // notify listeners
        this.invokeServletContextDestroyed(this);
    }

    public void initServlets(Set<Class<? extends Servlet>> servletClasses) {
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

    public void initFilters(Set<Class<? extends Filter>> filterClasses) {
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

    private void initListener(Set<Class<? extends EventListener>> listenerClasses) {
        for (var listenerClass : listenerClasses) {
            EventListener listener;
            try {
                listener = listenerClass.getConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(String.format("Create listener [%s] failed.", listenerClass.getName()), e);
            }
            this.addListener(listener);
        }
    }

    public List<ServletMapping> getServletMappingList() {
        return servletMappingList;
    }

    public List<FilterMapping> getFilterMappingList() {
        return filterMappingList;
    }

    public void process(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String uri = request.getRequestURI();
        Servlet servlet;
        for (ServletMapping servletMapping : this.getServletMappingList()) {
            if (servletMapping.match(uri)) {
                List<Filter> filterList = findFilters(uri);
                servlet = servletMapping.getServlet();
                FilterChain filterChain = new FilterChainImpl(filterList, servlet);
                try {
                    this.invokeServletRequestInitialized(this, request);
                    filterChain.doFilter(request, response);
                    return;
                } finally {
                    this.invokeServletRequestDestroyed(this, request);
                }
            }
        }


        // 未匹配到servlet，返回404
        try (PrintWriter writer = response.getWriter()) {
            response.setStatus(404);
            writer.write("<h1>404 Not Found</h1><p>No mapping for URL: " + uri + "</p>");
            response.flushBuffer();
        }
    }

    private List<Filter> findFilters(String uri) {
        List<FilterMapping> filterMappingList = this.getFilterMappingList();
        return filterMappingList.stream().filter(f -> f.match(uri)).map(FilterMapping::getFilter).collect(Collectors.toList());
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
        if (initialized) {
            throw new IllegalArgumentException("This ServletContext has already been initialized.");
        }

        if (servletName == null || "".equals(servletName)) {
            throw new IllegalArgumentException("Servlet name can not be null or empty string");
        }

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
        try {
            Class<?> filterClass = Class.forName(className);
            return addFilter(filterName, filterClass.asSubclass(Filter.class));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        if (initialized) {
            throw new IllegalArgumentException("This ServletContext has already been initialized.");
        }

        if (filterName == null || "".equals(filterName)) {
            throw new IllegalArgumentException("Filter name can not be null or empty string");
        }

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
        try {
            Constructor<? extends Filter> constructor = filterClass.getConstructor();
            Filter filter = constructor.newInstance();
            return addFilter(filterName, filter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getContextPath() {
        return "/";
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
        String originPath = path;
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        Path loc = this.webRoot.resolve(path).normalize();
        if (loc.startsWith(this.webRoot)) {
            if (Files.isDirectory(loc)) {
                try {
                    return Files.list(loc).map(p -> p.getFileName().toString()).collect(Collectors.toSet());
                } catch (IOException e) {
                    log.warn("list files failed for path: {}", originPath);
                }
            }
        }
        return null;
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        String originPath = path;
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        Path loc = this.webRoot.resolve(path).normalize();
        if (loc.startsWith(this.webRoot)) {
            return loc.toUri().toURL();
        }
        throw new MalformedURLException("Path not found: " + originPath);
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        Path loc = this.webRoot.resolve(path).normalize();
        if (loc.startsWith(this.webRoot)) {
            if (Files.isReadable(loc)) {
                try {
                    return new BufferedInputStream(new FileInputStream(loc.toFile()));
                } catch (FileNotFoundException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
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
        log.info(msg);
    }

    @Override
    public void log(String message, Throwable throwable) {
        log.error(message, throwable);
    }

    @Override
    public String getRealPath(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        Path loc = this.webRoot.resolve(path).normalize();
        if (loc.startsWith(this.webRoot)) {
            return loc.toString();
        }
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
        return true;
    }

    @Override
    public Object getAttribute(String name) {
        Objects.requireNonNull(name);
        return this.attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(this.attributes.keySet());
    }

    @Override
    public void setAttribute(String name, Object value) {
        Objects.requireNonNull(name);

        Object old = this.attributes.put(name, value);
        if (old == null) {
            this.invokeServletAttributeAdded(this, name, value);
            return;
        }

        // replaced
        this.invokeServletAttributeReplaced(this, name, old);
    }

    @Override
    public void removeAttribute(String name) {
        Objects.requireNonNull(name);

        Object remove = this.attributes.remove(name);
        this.invokeServletAttributeRemoved(this, name, remove);
    }

    @Override
    public String getServletContextName() {
        return "Spikedog-ServletContext";
    }

    @Override
    public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
        return null;
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new ServletException(e);
        }
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
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        return this.filterRegistrationMap.get(filterName);
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
        Class<?> aClass;
        try {
            aClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        addListener(aClass.asSubclass(EventListener.class));
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        try {
            Constructor<? extends EventListener> constructor = listenerClass.getConstructor();
            EventListener listener = constructor.newInstance();
            addListener(listener);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
        if (t instanceof ServletContextAttributeListener l) {
            this.servletContextAttributeListeners.add(l);
        } else if (t instanceof ServletContextListener l) {
            this.servletContextListeners.add(l);
        } else if (t instanceof ServletRequestListener l) {
            this.servletRequestListeners.add(l);
        } else if (t instanceof ServletRequestAttributeListener l) {
            this.servletRequestAttributeListeners.add(l);
        } else if (t instanceof HttpSessionAttributeListener l) {
            this.httpSessionAttributeListeners.add(l);
        } else if (t instanceof HttpSessionIdListener l) {
            this.httpSessionIdListeners.add(l);
        } else if (t instanceof HttpSessionListener l) {
            this.httpSessionListeners.add(l);
        } else {
            throw new IllegalArgumentException(String.format("The listener '%s' is not a target interface instance that can be added.", t.getClass().toString()));
        }
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        if (
                ServletContextAttributeListener.class.isAssignableFrom(clazz) ||
                        ServletContextListener.class.isAssignableFrom(clazz) ||
                        ServletRequestListener.class.isAssignableFrom(clazz) ||
                        ServletRequestAttributeListener.class.isAssignableFrom(clazz) ||
                        HttpSessionAttributeListener.class.isAssignableFrom(clazz) ||
                        HttpSessionIdListener.class.isAssignableFrom(clazz) ||
                        HttpSessionListener.class.isAssignableFrom(clazz)

        ) {
            try {
                return clazz.getConstructor().newInstance();
            } catch (Exception e) {
                throw new ServletException(e);
            }
        } else {
            throw new IllegalArgumentException(String.format("The listener '%s' is not a target interface instance.", clazz.toString()));
        }
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

    // invoke servlet listeners++

    void invokeServletContextInitialized(ServletContext sc) {
        log.debug("Invoke servlet context initialized listener. Servlet context name: {}", sc.getServletContextName());

        List<ServletContextListener> listeners = this.servletContextListeners;
        if (listeners.isEmpty()) {
            return;
        }

        ServletContextEvent event = new ServletContextEvent(this);
        for (ServletContextListener listener : listeners) {
            listener.contextInitialized(event);
        }
    }

    void invokeServletContextDestroyed(ServletContext sc) {
        log.debug("Invoke servlet context destroyed listener. Servlet context name: {}", sc.getServletContextName());

        List<ServletContextListener> listeners = this.servletContextListeners;
        if (listeners.isEmpty()) {
            return;
        }

        ServletContextEvent event = new ServletContextEvent(this);
        // 以相反顺序调用
        for (int i = listeners.size() - 1; i >= 0; i--) {
            listeners.get(i).contextDestroyed(event);
        }
    }


    void invokeServletAttributeAdded(ServletContext sc, String name, Object value) {
        log.debug("Invoke servlet attribute added listener. Name: {}, value: {}", name, value);

        List<ServletContextAttributeListener> listeners = this.servletContextAttributeListeners;
        if (listeners.isEmpty()) {
            return;
        }

        ServletContextAttributeEvent event = new ServletContextAttributeEvent(sc, name, value);
        for (ServletContextAttributeListener listener : listeners) {
            listener.attributeAdded(event);
        }
    }

    void invokeServletAttributeRemoved(ServletContext sc, String name, Object value) {
        log.debug("Invoke servlet attribute removed listener. Name: {}, value: {}", name, value);

        List<ServletContextAttributeListener> listeners = this.servletContextAttributeListeners;
        if (listeners.isEmpty()) {
            return;
        }

        ServletContextAttributeEvent event = new ServletContextAttributeEvent(sc, name, value);
        for (ServletContextAttributeListener listener : this.servletContextAttributeListeners) {
            listener.attributeRemoved(event);
        }
    }

    void invokeServletAttributeReplaced(ServletContext sc, String name, Object value) {
        log.debug("Invoke servlet attribute replaced listener. Name: {}, value: {}", name, value);

        List<ServletContextAttributeListener> listeners = this.servletContextAttributeListeners;
        if (listeners.isEmpty()) {
            return;
        }

        ServletContextAttributeEvent event = new ServletContextAttributeEvent(sc, name, value);
        for (ServletContextAttributeListener listener : this.servletContextAttributeListeners) {
            listener.attributeReplaced(event);
        }
    }

    void invokeServletRequestInitialized(ServletContext sc, ServletRequest request) {
        log.debug("Invoke servlet request initialized listener. Servlet request id: {}", request.getRequestId());

        List<ServletRequestListener> listeners = this.servletRequestListeners;
        if (listeners.isEmpty()) {
            return;
        }

        ServletRequestEvent event = new ServletRequestEvent(sc, request);
        for (ServletRequestListener listener : listeners) {
            listener.requestInitialized(event);
        }
    }

    void invokeServletRequestDestroyed(ServletContext sc, ServletRequest request) {
        log.debug("Invoke servlet request destroyed listener. Servlet request id: {}", request.getRequestId());

        List<ServletRequestListener> listeners = this.servletRequestListeners;
        if (listeners.isEmpty()) {
            return;
        }

        ServletRequestEvent event = new ServletRequestEvent(sc, request);
        for (ServletRequestListener listener : listeners) {
            listener.requestDestroyed(event);
        }
    }

    // invoke listeners--


    public List<HttpSessionListener> getHttpSessionListeners() {
        return httpSessionListeners;
    }

    public List<HttpSessionActivationListener> getHttpSessionActivationListeners() {
        return httpSessionActivationListeners;
    }

    public List<HttpSessionAttributeListener> getHttpSessionAttributeListeners() {
        return httpSessionAttributeListeners;
    }

    public List<HttpSessionIdListener> getHttpSessionIdListeners() {
        return httpSessionIdListeners;
    }

    public List<ServletContextListener> getServletContextListeners() {
        return servletContextListeners;
    }

    public List<ServletRequestListener> getServletRequestListeners() {
        return servletRequestListeners;
    }

    public List<ServletContextAttributeListener> getServletContextAttributeListeners() {
        return servletContextAttributeListeners;
    }

    public List<ServletRequestAttributeListener> getServletRequestAttributeListeners() {
        return servletRequestAttributeListeners;
    }

    @Override
    public void close() throws Exception {
        this.destroy();
    }
}
