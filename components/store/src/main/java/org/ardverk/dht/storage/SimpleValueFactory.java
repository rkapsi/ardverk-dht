package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.InputStream;

import org.ardverk.dht.codec.bencode.MessageInputStream;
import org.ardverk.dht.storage.SimpleValue.ValueType;

public class SimpleValueFactory implements ValueFactory {

    @Override
    public SimpleValue parse(ValueFuture future, InputStream in, long contentLength) throws IOException {
        return parse(future, wrap(in), contentLength);
    }
    
    public SimpleValue parse(ValueFuture future, 
            MessageInputStream in, long contentLength) throws IOException {
        
        int version = in.readUnsignedByte();
        if (version != SimpleValue.VERSION) {
            throw new IOException();
        }
        
        ValueType type = ValueType.valueOf(in.readInt());
        switch (type) {
            case STATUS:
                return Status.valueOf(in);
            case KEY_LIST:
                return KeyList.valueOf(in);
            case BLOB:
                return BlobValue.valueOf(in);
            default:
                throw new IOException();
        }
    }
    
    private MessageInputStream wrap(InputStream in) {
        if (!(in instanceof MessageInputStream)) {
            in = new MessageInputStream(in, this);
        }
        return (MessageInputStream)in;
    }
}
