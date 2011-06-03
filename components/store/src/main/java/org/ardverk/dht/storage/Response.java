package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang.ArrayUtils;
import org.apache.http.Header;
import org.apache.http.protocol.HTTP;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.io.IoUtils;
import org.ardverk.io.StreamUtils;

public class Response extends ContextValue {

    public static final Response OK = new Response(StatusLine.OK);
    
    public static final Response MULTIPLE_CHOICES = new Response(
            StatusLine.MULTIPLE_CHOICES);
    
    public static final Response NOT_FOUND = new Response(
            StatusLine.NOT_FOUND);
    
    public static final Response INTERNAL_SERVER_ERROR = new Response(
            StatusLine.INTERNAL_SERVER_ERROR);
    
    public static final Response LENGTH_REQUIRED = new Response(
            StatusLine.LENGTH_REQUIRED);
    
    public static Response createOk(Header... headers) {
        Response response = new Response(StatusLine.OK);
        response.setHeaders(headers);
        return response;
    }
    
    public static Response createOk(Context context, ValueEntity value) {
        return new Response(StatusLine.OK, context, value);
    }
    
    private final StatusLine status;
    
    public Response(StatusLine status) {
        this.status = status;
    }
    
    private Response(StatusLine status, Context context) {
        super(context);
        this.status = status;
    }
    
    private Response(StatusLine status, Context context, ValueEntity entity) {
        super(context, entity);
        this.status = status;
    }

    @Override
    protected void writeHeader(OutputStream out) throws IOException {
        status.writeTo(out);
        super.writeHeader(out);
    }
    
    @Override
    public String toString() {
        return status + " - " + super.toString();
    }
    
    public static Response valueOf(Value value) throws IOException {
        InputStream in = value.getContent();
        try {
            return valueOf(in);
        } finally {
            IoUtils.close(in);
        }
    }
    
    public static Response valueOf(InputStream in) throws IOException {
        StatusLine status = StatusLine.valueOf(in);
        Context context = Context.valueOf(in);
        
        long length = context.getContentLength();
        byte[] data = new byte[(int)Math.max(0L, length)];
        StreamUtils.readFully(in, data);
        
        ValueEntity entity = null;
        if (!ArrayUtils.isEmpty(data)) {
            
            String contentType = context.getStringValue(
                    HTTP.CONTENT_TYPE, HTTP.OCTET_STREAM_TYPE);
            
            entity = new ByteArrayValueEntity(contentType, data);
        }
        
        return new Response(status, context, entity);
    }
}
