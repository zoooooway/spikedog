package org.zoooooway.spikedog.connector;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author zoooooway
 */
public class ServletOutputStreamImpl extends ServletOutputStream {
    final OutputStream os;

    boolean ready;
    WriteListener writeListener;

    public ServletOutputStreamImpl(OutputStream os) {
        this.os = os;
    }

    @Override
    public boolean isReady() {
        if (!this.ready) {
            if (writeListener != null) {
                try {
                    writeListener.onWritePossible();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return this.ready;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        this.writeListener = writeListener;
    }

    @Override
    public void write(int b) throws IOException {
        try {
            this.os.write(b);
        } catch (IOException e) {
            if (this.writeListener != null) {
                this.writeListener.onError(e);
            }
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        this.os.close();
    }
}
