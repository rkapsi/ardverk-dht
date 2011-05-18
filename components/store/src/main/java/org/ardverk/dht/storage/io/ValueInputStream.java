package org.ardverk.dht.storage.io;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.HeaderGroup;
import org.ardverk.coding.BencodingInputStream;
import org.ardverk.dht.codec.bencode.MessageInputStream;
import org.ardverk.dht.rsrc.Key;

public class ValueInputStream extends MessageInputStream {

    private static final Header[] EMPTY_HEADERS_ARRAY = new Header[0];
    
    private static ObjectFactory<Header> HEADER_FACTORY 
            = new ObjectFactory<Header>() {
        
        @Override
        public Header read(BencodingInputStream in) throws IOException {
            String name = in.readString();
            String value = in.readString();
            return new BasicHeader(name, value);
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
    
    public Header[] readHeaders() throws IOException {
        return readList(HEADER_FACTORY).toArray(EMPTY_HEADERS_ARRAY);
    }
    
    public HeaderGroup readHeaderGroup() throws IOException {
        HeaderGroup headers = new HeaderGroup();
        headers.setHeaders(readHeaders());
        return headers;
    }
    
    public Key[] readKeys() throws IOException {
        return readList(KEY_FACTORY).toArray(new Key[0]);
    }
}
