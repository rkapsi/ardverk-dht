package org.ardverk.dht.storage.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.ardverk.dht.codec.bencode.MessageInputStream;
import org.ardverk.dht.storage.ObjectValue.Property;

public class ValueInputStream extends MessageInputStream {

    public ValueInputStream(InputStream in) {
        super(in);
    }

    public Property readProperty() throws IOException {
        String name = readString();
        List<String> values = readList(String.class);
        
        return new Property(name, values);
    }
    
    public Property[] readProperties() throws IOException {
        return null;
    }
}
