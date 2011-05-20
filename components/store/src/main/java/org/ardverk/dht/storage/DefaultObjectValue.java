package org.ardverk.dht.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.HeaderGroup;
import org.apache.http.protocol.HTTP;
import org.ardverk.dht.KUID;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.rsrc.ByteArrayValue;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.dht.storage.io.ValueInputStream;
import org.ardverk.dht.storage.io.ValueOutputStream;
import org.ardverk.io.IoUtils;
import org.ardverk.utils.StringUtils;
import org.ardverk.version.VectorClock;

public class DefaultObjectValue extends BasicObjectValue {

    public static final String CREATOR_KEY = "X-Ardverk-Creator";
    
    public static final String VECTOR_CLOCK_KEY = "X-Ardverk-VectorClock";
    
    private final Value value;
    
    public DefaultObjectValue(Map<String, String> properties, byte[] value) {
        this(null, null, contentLength(properties, value.length), 
                new ByteArrayValue(value));
    }
    
    public DefaultObjectValue(Contact creator, VectorClock<KUID> clock, byte[] value) {
        this(creator, clock, new ByteArrayValue(value), value.length);
    }
    
    public DefaultObjectValue(Contact creator, VectorClock<KUID> clock, 
            Value value, long length) {
        this(creator, clock, contentLength(length), value);
    }
    
    public DefaultObjectValue(Contact creator, VectorClock<KUID> clock,
            Map<String, String> props, Value value) {
        this(properties(creator, clock, props), value);
    }
    
    private DefaultObjectValue(HeaderGroup properties, Value value) {
        super(properties);
        
        if (!containsHeader(HTTP.CONTENT_LEN)) {
            throw new IllegalArgumentException();
        }
        
        this.value = value;
    }
    
    public Contact getCreator() {
        Header header = getFirstHeader(CREATOR_KEY);
        if (header != null) {
            return decodeContact(header.getValue());
        }
        return null;
    }
    
    public VectorClock<KUID> getVectorClock() {
        return getVectorClock(this);
    }
    
    public static VectorClock<KUID> getVectorClock(ObjectValue value) {
        Header header = value.getFirstHeader(VECTOR_CLOCK_KEY);
        if (header != null) {
            return decodeVectorClock(header.getValue());
        }
        return null;
    }
    
    public long getContentLength() {
        Header header = getFirstHeader(HTTP.CONTENT_LEN);
        return Long.parseLong(header.getValue());
    }
    
    @Override
    protected void writeContent(ValueOutputStream out) throws IOException {
        value.writeTo(out);
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
        ValueInputStream vis = new ValueInputStream(in);
        HeaderGroup headers = vis.readHeaderGroup();
        
        Header header = headers.getFirstHeader(HTTP.CONTENT_LEN);
        long length = Long.parseLong(header.getValue());
        
        byte[] value = new byte[(int)length];
        vis.readFully(value);
        
        return new DefaultObjectValue(headers, new ByteArrayValue(value));
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
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(value).append(" @ ").append(headers);
        return sb.toString();
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
    
    private static Map<String, String> contentLength(long length) {
        return contentLength(null, length);
    }

    private static Map<String, String> contentLength(Map<String, String> dst, long length) {
        if (dst == null) {
            return Collections.singletonMap(HTTP.CONTENT_LEN, Long.toString(length));
        }
        
        if (!dst.containsKey(HTTP.CONTENT_LEN)) {
            dst.put(HTTP.CONTENT_LEN, Long.toString(length));
        }
        return dst;
    }
    
    private static ValueInputStream newValueInputStream(byte[] data) {
        return new ValueInputStream(new InflaterInputStream(new ByteArrayInputStream(data)));
    }
    
    private static ValueOutputStream newValueOutputStream(OutputStream out) {
        return new ValueOutputStream(new DeflaterOutputStream(out));
    }
    
    private static HeaderGroup properties(Contact creator, 
            VectorClock<KUID> clock, Map<String, String> props) {
        
        HeaderGroup group = new HeaderGroup();
        
        if (props != null) {
            for (Map.Entry<String, String> entry : props.entrySet()) {
                group.addHeader(new BasicHeader(entry.getKey(), entry.getValue()));
            }
        }
        
        if (creator != null) {
            group.updateHeader(new BasicHeader(CREATOR_KEY, encodeContact(creator)));
            
            if (clock == null) {
                clock = VectorClock.create(creator.getId());
            }
        }
        
        if (clock != null) {
            group.updateHeader(new BasicHeader(VECTOR_CLOCK_KEY, encodeVectorClock(clock)));
        }
        
        if (!group.containsHeader(HTTP.CONTENT_TYPE)) {
            group.addHeader(new BasicHeader(HTTP.CONTENT_TYPE, HTTP.DEFAULT_CONTENT_TYPE));
        }
        
        return group;
    }
}
