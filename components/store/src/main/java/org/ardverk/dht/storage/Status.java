package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpStatus;
import org.apache.http.message.HeaderGroup;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.dht.storage.io.ValueInputStream;
import org.ardverk.dht.storage.io.ValueOutputStream;
import org.ardverk.io.IoUtils;

public class Status extends BasicObjectValue {
    
    public static final Status OK = new Status(
            HttpStatus.SC_OK, "OK");
    
    public static final Status INTERNAL_SERVER_ERROR = new Status(
            HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
    
    public static final Status LENGTH_REQUIRED = new Status(
            HttpStatus.SC_LENGTH_REQUIRED, "Length Required");
    
    private final int code;
    
    private final String message;
    
    private Status(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    private Status(HeaderGroup headers, int code, String message) {
        super(headers);
        this.code = code;
        this.message = message;
    }
    
    @Override
    protected void writeTo(ValueOutputStream out) throws IOException {
        super.writeTo(out);
        
        out.writeInt(code);
        out.writeString(message);
    }

    @Override
    public String toString() {
        return code + " " + message + " - " + super.toString();
    }
    
    public static Status valueOf(Value value) throws IOException {
        InputStream in = value.getContent();
        try {
            return valueOf(in);
        } finally {
            IoUtils.close(in);
        }
    }
    
    public static Status valueOf(InputStream in) throws IOException {
        ValueInputStream vis = new ValueInputStream(in);
        
        HeaderGroup headers = vis.readHeaderGroup();
        
        int code = vis.readInt();
        String message = vis.readString();
        
        return new Status(headers, code, message);
    }
}
