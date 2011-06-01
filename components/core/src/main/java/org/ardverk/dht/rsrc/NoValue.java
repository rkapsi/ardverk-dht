package org.ardverk.dht.rsrc;

import java.io.IOException;
import java.io.InputStream;

public class NoValue extends DefaultValue {

    public static final Value EMPTY = new NoValue();
    
    private final InputStream in = new InputStream() {
        @Override
        public int read() throws IOException {
            return -1;
        }
    };
    
    private NoValue() {}
    
    @Override
    public InputStream getContent() {
        return in;
    }
}
