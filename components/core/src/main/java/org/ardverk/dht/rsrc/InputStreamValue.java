package org.ardverk.dht.rsrc;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamValue extends DefaultValue implements Closeable {
    
    private final InputStream in;
    
    public InputStreamValue(InputStream in) {
        this.in = in;
    }
    
    @Override
    public InputStream getContent() {
        return in;
    }
    
    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public boolean isStreaming() {
        return true;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
