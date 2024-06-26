package org.zoooooway.spikedog.connector;

import com.sun.net.httpserver.Headers;
import jakarta.annotation.Nullable;
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

    @Nullable
    Cookie[] getCookies();

    InputStream getRequestBody();

    String getRequestMethod();

    Headers getRequestHeaders();

    String getProtocol();
}
