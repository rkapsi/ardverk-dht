package org.ardverk.dht.storage.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.ardverk.coding.BencodingInputStream;
import org.ardverk.dht.codec.bencode.MessageInputStream;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.storage.ObjectValue.Property;

public class ValueInputStream extends MessageInputStream {

    private static ObjectFactory<Property> PROPERTY_FACTORY 
            = new ObjectFactory<Property>() {
        
        @Override
        public Property read(BencodingInputStream in) throws IOException {
            String name = in.readString();
            List<String> values = in.readList(StringObjectFactory.STRING);
            return new Property(name, values);
        }
    };
    
    private static ObjectFactory<Key> KEY_FACTORY
            = new ObjectFactory<Key>() {
        @Override
        public Key read(BencodingInputStream in) throws IOException {
            return ((ValueInputStream)in).readKey();
        }
    };
    
    public ValueInputStream(InputStream in) {
        super(in);
    }
    
    public Property[] readProperties() throws IOException {
        return readList(PROPERTY_FACTORY).toArray(new Property[0]);
    }
    
    public Key[] readKeys() throws IOException {
        return readList(KEY_FACTORY).toArray(new Key[0]);
    }
}
