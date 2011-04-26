package org.ardverk.dht.message;

import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.dht.concurrent.DHTValueFuture;
import org.ardverk.io.IoUtils;

public class ExternalContent extends AbstractContent implements Closeable {

    private final long contentLength;
    
    private final InputStream in;
    
    private final DHTValueFuture<Void> future 
            = new DHTValueFuture<Void>() {
        @Override
        protected void done() {
            super.done();
            IoUtils.close(in);
        }
    };
    
    public ExternalContent(long contentLength, InputStream in) {
        if (in != null) {
            in = new ContentInputStream(in);
        }
        
        this.contentLength = contentLength;
        this.in = in;
        
        if (contentLength == 0L) {
            complete();
        }
    }
    
    @Override
    public DHTFuture<Void> getContentFuture() {
        return future;
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public InputStream getContent() {
        return in;
    }
    
    @Override
    public void close() {
        IoUtils.close(in);
    }

    private void complete() {
        future.setValue(null);
    }
    
    private class ContentInputStream extends FilterInputStream {

        public ContentInputStream(InputStream in) {
            super(in);
        }

        @Override
        public int read() throws IOException {
            int value = super.read();
            if (value < 0) {
                complete();
            }
            return value;
        }
        
        @Override
        public int read(byte[] b) throws IOException {
            int value = super.read(b);
            if (value < 0) {
                complete();
            }
            return value;
        }
        
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int value = super.read(b, off, len);
            if (value < 0) {
                complete();
            }
            return value;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                complete();
            }
        }
    }
}