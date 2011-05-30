package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpStatus;
import org.ardverk.dht.KUID;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.io.DataUtils;
import org.ardverk.io.IoUtils;
import org.ardverk.utils.StringUtils;
import org.ardverk.version.VectorClock;

public class ResponseValue extends AbstractContextValue {

    public static final ResponseValue OK = new ResponseValue(
            HttpStatus.SC_OK, "OK");
    
    public static final ResponseValue MULTIPLE_CHOICES = new ResponseValue(
            HttpStatus.SC_MULTIPLE_CHOICES, "Multiple Choices");
    
    public static final ResponseValue NOT_FOUND = new ResponseValue(
            HttpStatus.SC_NOT_FOUND, "Not Found");
    
    public static final ResponseValue INTERNAL_SERVER_ERROR = new ResponseValue(
            HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
    
    public static final ResponseValue LENGTH_REQUIRED = new ResponseValue(
            HttpStatus.SC_LENGTH_REQUIRED, "Length Required");
    
    public static ResponseValue createOk(VectorClock<KUID> vclock) {
        ResponseValue response = new ResponseValue(HttpStatus.SC_OK, "OK");
        response.setHeader(Constants.VCLOCK, VclockUtils.toString(vclock));
        return response;
    }
    
    private final int code;
    
    private final String message;
    
    private Value value;
    
    public ResponseValue(int code, String message) {
        super();
        
        this.code = code;
        this.message = message;
    }

    public ResponseValue(int code, String message, 
            Context context) {
        super(context);
        
        this.code = code;
        this.message = message;
    }
    
    public ResponseValue(int code, String message, 
            Context context, Value value) {
        super(context);
        
        this.code = code;
        this.message = message;
        
        this.value = value;
    }
    
    @Override
    protected void writeContext(OutputStream out) throws IOException {
        DataUtils.short2beb(code, out);
        StringUtils.writeString(message, out);
        
        super.writeContext(out);
    }
    
    @Override
    protected void writeValue(OutputStream out) throws IOException {
        DataUtils.bool(value != null, out);
        
        if (value != null) {
            value.writeTo(out);
        }
    }

    public static ResponseValue valueOf(Value value) throws IOException {
        InputStream in = value.getContent();
        try {
            return valueOf(in);
        } finally {
            IoUtils.close(in);
        }
    }
    
    public static ResponseValue valueOf(InputStream in) throws IOException {
        int code = DataUtils.beb2ushort(in);
        String message = StringUtils.readString(in);
        
        Context context = Context.valueOf(in);
        
        Value value = null;
        
        if (DataUtils.bool(in)) {
            
        }
        
        return new ResponseValue(code, message, context);
    }
}
