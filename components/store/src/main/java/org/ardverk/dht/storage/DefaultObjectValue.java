package org.ardverk.dht.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.protocol.HTTP;
import org.ardverk.dht.KUID;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.rsrc.ByteArrayValue;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.dht.storage.io.ValueInputStream;
import org.ardverk.dht.storage.io.ValueOutputStream;
import org.ardverk.io.IoUtils;
import org.ardverk.io.StreamUtils;
import org.ardverk.utils.StringUtils;
import org.ardverk.version.VectorClock;

public class DefaultObjectValue extends ContextValue {

    public static final String CREATOR_KEY = "X-Ardverk-Creator";
    
    public static final String VECTOR_CLOCK_KEY = "X-Ardverk-VectorClock";
    
    public DefaultObjectValue(Contact creator, 
            VectorClock<KUID> clock, byte[] value) {
        super(new ByteArrayValue(value));
        
        Context context = getContext();

        if (creator != null) {
            context.addHeader(CREATOR_KEY, encodeContact(creator));
            
            if (clock == null) {
                clock = VectorClock.create(creator.getId());
            }
        }
        
        if (clock != null) {
            context.addHeader(VECTOR_CLOCK_KEY, encodeVectorClock(clock));
        }
        
        context.addHeader(HTTP.CONTENT_LEN, Long.toString(value.length));
    }
    
    private DefaultObjectValue(Context context, Value value) {
        super(context, value);
    }

    public Contact getCreator() {
        return decodeContact(getContext().getStringValue(CREATOR_KEY));
    }
    
    public static DefaultObjectValue valueOf(Value value) throws IOException {
        InputStream in = value.getContent();
        try {
            return valueOf(in);
        } finally {
            IoUtils.close(in);
        }
    }
    
    public static DefaultObjectValue valueOf(InputStream in) throws IOException {
        Context context = Context.valueOf(in);
        
        long length = 0L;
        if (context.containsHeader(HTTP.CONTENT_LEN)) {
            length = context.getContentLength();
        }
        
        byte[] data = new byte[(int)length];
        StreamUtils.readFully(in, data);
        
        return new DefaultObjectValue(context, new ByteArrayValue(data));
    }
    
    private static Contact decodeContact(String value) {
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
    
    public static VectorClock<KUID> getVectorClock(ContextValue value) {
        return getVectorClock(value.getContext());
    }
    
    public static VectorClock<KUID> getVectorClock(Context context) {
        String value = context.getStringValue(VECTOR_CLOCK_KEY);
        return decodeVectorClock(value);
    }
}
