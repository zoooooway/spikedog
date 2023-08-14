package org.zoooooway.spikedog.connector;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author zoooooway
 */
public interface HttpExchangeResponse {
    void setHeader(String name, String value);

    void addHeader(String name, String value);

    void sendResponseHeaders(int rCode, long responseLength) throws IOException;

    OutputStream getResponseBody();

}
