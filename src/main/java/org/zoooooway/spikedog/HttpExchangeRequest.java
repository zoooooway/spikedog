package org.zoooooway.spikedog;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author zoooooway
 */
public interface HttpExchangeRequest {

    InputStream getInputStream();

    String getRequestURI();

    String getMethod();

    int getContentLength() throws IOException;

    String getParameter(String name);
}
