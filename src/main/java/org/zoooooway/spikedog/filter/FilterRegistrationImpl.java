package org.zoooooway.spikedog.filter;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;

import java.util.*;

/**
 * @author zoooooway
 */
public class FilterRegistrationImpl implements FilterRegistration.Dynamic {

    final String filterName;
    final Filter filter;
    final Set<String> mappings;
    final Map<String, String> initParameters = new HashMap<>();

    public FilterRegistrationImpl(String filterName, Filter filter, Set<String> mappings) {
        this.filterName = filterName;
        this.filter = filter;
        this.mappings = mappings;
    }

    @Override
    public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames) {

    }

    @Override
    public Collection<String> getServletNameMappings() {
        return null;
    }

    @Override
    public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {

    }

    @Override
    public Collection<String> getUrlPatternMappings() {
        return this.mappings;
    }

    @Override
    public void setAsyncSupported(boolean isAsyncSupported) {

    }

    @Override
    public String getName() {
        return this.filterName;
    }

    @Override
    public String getClassName() {
        return this.filter.getClass().getName();
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
}
