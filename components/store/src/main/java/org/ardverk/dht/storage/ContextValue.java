package org.ardverk.dht.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.protocol.HTTP;
import org.ardverk.dht.rsrc.AbstractValue;
import org.ardverk.dht.rsrc.ByteArrayValue;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.io.IoUtils;
import org.ardverk.io.StreamUtils;

public class ContextValue extends AbstractValue {

    //public static final String CREATOR_KEY = "X-Ardverk-Creator";
    
    //public static final String VECTOR_CLOCK_KEY = "X-Ardverk-VectorClock";
    
    private final Context context;
    
    private final Value value;
    
    public ContextValue(Value value) {
        this(new Context(), value);
        
        init();
    }
    
    public ContextValue(Context context, Value value) {
        this.context = context;
        this.value = value;
        
        init();
    }
    
    private void init() {
        
    }
    
    public Context getContext() {
        return context;
    }
    
    public Value getValue() {
        return value;
    }

    @Override
    public boolean isRepeatable() {
        return value.isRepeatable();
    }

    @Override
    public boolean isStreaming() {
        return value.isStreaming();
    }
    
    @Override
    public void writeTo(OutputStream out) throws IOException {
        context.writeTo(out);
        value.writeTo(out);
    }
    
    public static ContextValue valueOf(Value value) throws IOException {
        InputStream in = value.getContent();
        try {
            return valueOf(in);
        } finally {
            IoUtils.close(in);
        }
    }
    
    public static ContextValue valueOf(InputStream in) throws IOException {
        Context context = Context.valueOf(in);
        
        long length = 0L;
        if (context.containsHeader(HTTP.CONTENT_LEN)) {
            length = context.getContentLength();
        }
        
        byte[] data = new byte[(int)length];
        StreamUtils.readFully(in, data);
        
        return new ContextValue(context, new ByteArrayValue(data));
    }
    
    /*private static Contact decodeContact(String value) {
        byte[] data = decodeBase64(value);
        
        ValueInputStream vis = newValueInputStream(data);
        try {
            return vis.readContact();
        } catch (IOException err) {
            throw new IllegalStateException(err);
        } finally {
            IoUtils.close(vis);
        }
    }
    
    private static String encodeContact(Contact contact) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ValueOutputStream vos = newValueOutputStream(baos);
        try {
            vos.writeContact(contact);
        } catch (IOException err) {
            throw new IllegalStateException(err);
        } finally {
            IoUtils.close(vos);
        }
        
        return encodeBase64(baos.toByteArray());
    }
    
    private static VectorClock<KUID> decodeVectorClock(String value) {
        byte[] data = decodeBase64(value);
        
        ValueInputStream vis = newValueInputStream(data);
        try {
            return vis.readVectorClock();
        } catch (IOException err) {
            throw new IllegalStateException(err);
        } finally {
            IoUtils.close(vis);
        }
    }
    
    private static String encodeVectorClock(VectorClock<KUID> clock) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ValueOutputStream vos = newValueOutputStream(baos);
        try {
            vos.writeVectorClock(clock);
        } catch (IOException err) {
            throw new IllegalStateException(err);
        } finally {
            IoUtils.close(vos);
        }
        
        return encodeBase64(baos.toByteArray());
    }
    
    private static byte[] decodeBase64(String value) {
        Base64 decoder = new Base64(true);
        return decoder.decode(StringUtils.getBytes(value));
    }
    
    private static String encodeBase64(byte[] value) {
        byte[] base64 = Base64.encodeBase64(value, false, true);
        return StringUtils.toString(base64);
    }
    
    private static ValueInputStream newValueInputStream(byte[] data) {
        return new ValueInputStream(new InflaterInputStream(new ByteArrayInputStream(data)));
    }
    
    private static ValueOutputStream newValueOutputStream(OutputStream out) {
        return new ValueOutputStream(new DeflaterOutputStream(out));
    }
    
    private static HeaderGroup create(Contact creator, 
            VectorClock<KUID> clock, long length) {
        
        HeaderGroup headers = new HeaderGroup();
        
        if (creator != null) {
            headers.updateHeader(new BasicHeader(CREATOR_KEY, encodeContact(creator)));
            
            if (clock == null) {
                clock = VectorClock.create(creator.getId());
            }
        }
        
        if (clock != null) {
            headers.updateHeader(new BasicHeader(VECTOR_CLOCK_KEY, encodeVectorClock(clock)));
        }
        
        headers.addHeader(new BasicHeader(HTTP.CONTENT_TYPE, HTTP.DEFAULT_CONTENT_TYPE));
        
        if (length >= 0L) {
            headers.addHeader(new BasicHeader(HTTP.CONTENT_LEN, Long.toString(length)));
        }
        
        return headers;
    }*/
}
