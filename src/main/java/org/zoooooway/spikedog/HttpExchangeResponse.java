package org.zoooooway.spikedog;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author zoooooway
 */
public interface HttpExchangeResponse {
    void setHeader(String name, String value);

    void setStatus(int sc);

    void setContentLength(int len);

    void flushBuffer() throws IOException;

    PrintWriter getWriter();
}
