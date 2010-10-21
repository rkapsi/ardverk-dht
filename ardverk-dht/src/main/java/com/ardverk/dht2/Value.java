package com.ardverk.dht2;

import java.io.IOException;
import java.io.InputStream;

public interface Value {
    
    public long getContentLength();
    
    public InputStream getContent() throws IOException;
}