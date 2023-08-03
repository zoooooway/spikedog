package org.zoooooway.spikedog.filter;

import jakarta.servlet.Filter;

import java.util.regex.Pattern;

/**
 * @author zoooooway
 */
public class FilterMapping {
    final Pattern pattern;
    final Filter filter;

    public FilterMapping(String urlPattern, Filter filter) {
        this.pattern = buildPattern(urlPattern);
        this.filter = filter;
    }

    Pattern buildPattern(String urlPattern) {
        StringBuilder sb = new StringBuilder(urlPattern.length() + 16);
        sb.append('^');
        for (int i = 0; i < urlPattern.length(); i++) {
            char ch = urlPattern.charAt(i);
            if (ch == '*') {
                sb.append(".*");
            } else if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9') {
                sb.append(ch);
            } else {
                sb.append('\\').append(ch);
            }
        }
        sb.append('$');
        return Pattern.compile(sb.toString());
    }


    public Filter getFilter() {
        return filter;
    }

    public boolean match(String path) {
        return this.pattern.matcher(path).matches();
    }
}
