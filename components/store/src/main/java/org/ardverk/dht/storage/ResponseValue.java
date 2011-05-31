package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ardverk.dht.KUID;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.io.DataUtils;
import org.ardverk.io.IoUtils;
import org.ardverk.version.VectorClock;

public class ResponseValue extends AbstractContextValue {

    public static final ResponseValue OK = new ResponseValue(StatusLine.OK);
    
    public static final ResponseValue MULTIPLE_CHOICES = new ResponseValue(
            StatusLine.MULTIPLE_CHOICES);
    
    public static final ResponseValue NOT_FOUND = new ResponseValue(
            StatusLine.NOT_FOUND);
    
    public static final ResponseValue INTERNAL_SERVER_ERROR = new ResponseValue(
            StatusLine.INTERNAL_SERVER_ERROR);
    
    public static final ResponseValue LENGTH_REQUIRED = new ResponseValue(
            StatusLine.LENGTH_REQUIRED);
    
    public static ResponseValue createOk(VectorClock<KUID> vclock) {
        ResponseValue response = new ResponseValue(StatusLine.OK);
        response.setHeader(Constants.VCLOCK, VclockUtils.toString(vclock));
        return response;
    }
    
    public static ResponseValue createMultipleChoice(ContextValue... values) {
        ResponseValue response = new ResponseValue(StatusLine.MULTIPLE_CHOICES);
        
        return response;
    }
    
    private final StatusLine status;
    
    private final Value value;
    
    public ResponseValue(StatusLine status) {
        this(status, new Context());
    }

    public ResponseValue(StatusLine status, Context context) {
        this(status, context, null);
    }
    
    public ResponseValue(StatusLine status, 
            Context context, Value value) {
        super(context);
        
        this.status = status;
        this.value = value;
        
        Constants.init(context);
        
        if (value == null) {
            setHeader(Constants.NO_CONTENT);
        }
    }
    
    public Value getValue() {
        return value;
    }
    
    @Override
    protected void writeContext(OutputStream out) throws IOException {
        status.writeTo(out);
        super.writeContext(out);
    }
    
    @Override
    protected void writeValue(OutputStream out) throws IOException {
        DataUtils.bool(value != null, out);
        
        if (value != null) {
            value.writeTo(out);
        }
    }

    @Override
    public String toString() {
        return status + " - " + super.toString();
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
        StatusLine status = StatusLine.valueOf(in);
        Context context = Context.valueOf(in);
        
        Value value = null;
        
        if (DataUtils.bool(in)) {
            
        }
        
        return new ResponseValue(status, context);
    }
}
