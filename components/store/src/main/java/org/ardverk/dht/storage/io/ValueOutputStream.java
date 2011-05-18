package org.ardverk.dht.storage.io;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.message.HeaderGroup;
import org.ardverk.dht.codec.bencode.MessageOutputStream;

public class ValueOutputStream extends MessageOutputStream {

    public ValueOutputStream(OutputStream out) {
        super(out);
    }
    
    @Override
    protected void writeCustom(Object obj) throws IOException {
        if (obj instanceof Header) {
            writeHeader((Header)obj);
        } else if (obj instanceof HeaderGroup) {
            writeHeaderGroup((HeaderGroup)obj);
        } else {
            super.writeCustom(obj);
        }
    }
    
    public void writeHeader(Header header) throws IOException {
        writeString(header.getName());
        writeString(header.getValue());
    }
    
    public void writeHeaderGroup(HeaderGroup headers) throws IOException {
        writeArray(headers.getAllHeaders());
    }
}
