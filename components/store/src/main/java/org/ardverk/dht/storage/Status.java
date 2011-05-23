package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.io.DataUtils;
import org.ardverk.io.IoUtils;
import org.ardverk.io.StreamUtils;
import org.ardverk.utils.StringUtils;

public class Status extends ContextValue {
    
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
    
    private Status(Context context, int code, String message) {
        super(context);
        this.code = code;
        this.message = message;
    }
    
    @Override
    public void writeTo(OutputStream out) throws IOException {
        super.writeTo(out);
        
        DataUtils.short2beb(code);
        StringUtils.writeString(message, out);
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
        Context context = Context.valueOf(in);
        
        long length = 0L;
        if (context.containsHeader(HTTP.CONTENT_LEN)) {
            length = context.getContentLength();
        }
        
        byte[] data = new byte[(int)length];
        StreamUtils.readFully(in, data);
        
        int code = DataUtils.beb2ushort(in);
        String message = StringUtils.readString(in);
        
        return new Status(context, code, message);
    }
}
