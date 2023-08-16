package org.zoooooway.spikedog.connector;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author zoooooway
 */
public class ServletInputStreamImpl extends ServletInputStream {
    final InputStream is;

    boolean ready;
    boolean finished;
    ReadListener readListener;

    public ServletInputStreamImpl(InputStream is) {
        this.is = is;
    }

    @Override
    public boolean isFinished() {
        return this.finished;
    }

    @Override
    public boolean isReady() {
        if (!this.ready) {
            if (readListener != null) {
                try {
                    readListener.onDataAvailable();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return this.ready;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        this.readListener = readListener;
    }

    @Override
    public int read() throws IOException {
        try {
            return this.is.read();
        } catch (IOException e) {
            if (this.readListener != null) {
                this.readListener.onError(e);
            }
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        this.is.close();
    }
}
