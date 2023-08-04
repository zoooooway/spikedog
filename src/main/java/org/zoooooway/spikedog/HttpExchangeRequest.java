package org.zoooooway.spikedog;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author zoooooway
 */
public interface HttpExchangeRequest {

    InputStream getInputStream();

    String getRequestURI();

    String getMethod();

    HttpSession getSession(boolean create);

    int getContentLength() throws IOException;

    String getParameter(String name);

    Cookie[] getCookies();
}
