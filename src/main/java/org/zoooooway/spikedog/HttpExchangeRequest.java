package org.zoooooway.spikedog;

import com.sun.net.httpserver.Headers;
import jakarta.servlet.http.Cookie;

import java.io.InputStream;
import java.net.URI;

/**
 * @author zoooooway
 */
public interface HttpExchangeRequest {

    URI getRequestURI();

    String getMethod();

    String getRequestHeader(String name);

    Cookie[] getCookies();

    InputStream getRequestBody();

    String getRequestMethod();

    Headers getRequestHeaders();
}
