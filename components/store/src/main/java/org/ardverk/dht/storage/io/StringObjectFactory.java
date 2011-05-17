package org.ardverk.dht.storage.io;

import java.io.IOException;

import org.ardverk.coding.BencodingInputStream;
import org.ardverk.coding.BencodingInputStream.ObjectFactory;

public class StringObjectFactory implements ObjectFactory<String> {

    public static final ObjectFactory<String> STRING 
        = new StringObjectFactory();
    
    private StringObjectFactory() {
    }

    @Override
    public String read(BencodingInputStream in) throws IOException {
        return in.readString();
    }
}
