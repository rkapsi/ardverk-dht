package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ardverk.dht.KUID;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.io.IoUtils;
import org.ardverk.version.VectorClock;

public class ResponseValue extends PropertiesValue {

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
        response.setHeader(Constants.XML);
        return response;
    }
    
    private final StatusLine status;
    
    public ResponseValue(StatusLine status) {
        this(status, new Context());
    }

    public ResponseValue(StatusLine status, Context context) {
        super(context);
        
        this.status = status;
        
        Constants.init(context);
        setHeader(Constants.NO_CONTENT);
    }

    @Override
    protected void writeContext(OutputStream out) throws IOException {
        status.writeTo(out);
        super.writeContext(out);
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
        
        return new ResponseValue(status, context);
    }
}
