package org.zoooooway.spikedog;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author zoooooway
 */
public interface HttpExchangeRequest {

    InputStream getInputStream();

    int getContentLength() throws IOException;

}
