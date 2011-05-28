package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;
import org.ardverk.coding.CodingUtils;
import org.ardverk.dht.KUID;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.io.DataUtils;
import org.ardverk.io.IoUtils;
import org.ardverk.io.StreamUtils;
import org.ardverk.security.MessageDigestUtils;
import org.ardverk.utils.StringUtils;
import org.ardverk.version.VectorClock;


public class Status extends ContextValue {
    
    public static final Status OK = new Status(
            HttpStatus.SC_OK, "OK");
    
    public static final Status INTERNAL_SERVER_ERROR = new Status(
            HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
    
    public static final Status LENGTH_REQUIRED = new Status(
            HttpStatus.SC_LENGTH_REQUIRED, "Length Required");
    
    public static Status createOk(VectorClock<KUID> vclock) {
        Status status = new Status(HttpStatus.SC_OK, "OK");
        status.setHeader(Constants.VCLOCK, VclockUtils.toString(vclock));
        return status;
    }
    
    private final int code;
    
    private final String message;
    
    private Status(int code, String message) {
        this.code = code;
        this.message = message;
        
        setHeader(Constants.NO_CONTENT);
    }
    
    private Status(int code, String message, Context context) {
        super(context);
        this.code = code;
        this.message = message;
        
        setHeader(Constants.NO_CONTENT);
    }
    
    @Override
    protected void writeContext(OutputStream out) throws IOException {
        DataUtils.short2beb(code, out);
        StringUtils.writeString(message, out);
        
        super.writeContext(out);
    }

    @Override
    public String toString() {
        return code + " " + message + " - " + super.toString();
    }
    
    public static Status valueOf(Value value) throws IOException {
        InputStream in = value.getContent();
        try {
            return Status.valueOf(in);
        } finally {
            IoUtils.close(in);
        }
    }
    
    public static Status valueOf(InputStream in) throws IOException {
        
        int code = DataUtils.beb2ushort(in);
        String message = StringUtils.readString(in);
        
        Context context = Context.valueOf(in);
        
        long length = 0L;
        if (context.containsHeader(HTTP.CONTENT_LEN)) {
            length = context.getContentLength();
        }
        
        byte[] data = new byte[(int)length];
        
        MessageDigest md = MessageDigestUtils.createMD5();
        DigestInputStream dis = new DigestInputStream(in, md);
        StreamUtils.readFully(dis, data);
        
        /*Header etag = context.getFirstHeader(Constants.ETAG);
        if (etag != null) {
            String actual = "\"" + CodingUtils.encodeBase16(md.digest()) + "\"";
            String expected = etag.getValue();
            
            if (!actual.equalsIgnoreCase(expected)) {
                throw new IOException("Mismatching ETags: " + actual + " vs. " + expected);
            }
        }*/
        
        return new Status(code, message, context);
    }
}
