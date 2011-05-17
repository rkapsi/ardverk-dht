package org.ardverk.dht.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.ardverk.collection.CollectionUtils;
import org.ardverk.dht.KUID;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.rsrc.ByteArrayValue;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.dht.storage.io.ValueInputStream;
import org.ardverk.dht.storage.io.ValueOutputStream;
import org.ardverk.io.InputOutputStream;
import org.ardverk.io.IoUtils;
import org.ardverk.utils.StringUtils;
import org.ardverk.version.VectorClock;

public class ObjectValue extends SimpleValue {

    private static final Comparator<String> COMPARATOR 
            = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            if (o1 == null || o2 == null) {
                throw new NullPointerException();
            }
            
            return o1.compareToIgnoreCase(o2);
        }
    };
    
    public static final String CONTENT_LENGTH = "Content-Length";
    
    public static final String CONTENT_TYPE = "Content-Type";
    
    public static final String BINARY = "binary/octet-stream";
    
    public static final String CREATOR_KEY = "X-Ardverk-Creator";
    
    public static final String VECTOR_CLOCK_KEY = "X-Ardverk-VectorClock";
    
    private final Map<String, Property> properties 
        = new TreeMap<String, Property>(COMPARATOR);
    
    private final Value value;
    
    public ObjectValue(Map<String, String> properties, byte[] value) {
        this(null, null, contentLength(properties, value.length), 
                new ByteArrayValue(value));
    }
    
    public ObjectValue(Contact creator, VectorClock<KUID> clock, byte[] value) {
        this(creator, clock, new ByteArrayValue(value), value.length);
    }
    
    public ObjectValue(Contact creator, VectorClock<KUID> clock, 
            Value value, long length) {
        this(creator, clock, contentLength(length), value);
    }
    
    public ObjectValue(Contact creator, VectorClock<KUID> clock,
            Map<String, String> props, Value value) {
        super(ValueType.OBJECT);
        
        if (props != null) {
            for (Map.Entry<String, String> entry : props.entrySet()) {
                addProperty(entry.getKey(), entry.getValue());
            }
        }
        
        if (creator != null) {
            setProperty(CREATOR_KEY, encodeContact(creator));
            
            if (clock == null) {
                clock = VectorClock.create(creator.getId());
            }
        }
        
        if (clock != null) {
            setProperty(VECTOR_CLOCK_KEY, encodeVectorClock(clock));
        }
        
        if (!hasProperty(CONTENT_TYPE)) {
            setProperty(CONTENT_TYPE, BINARY);
        }
        
        if (!hasProperty(CONTENT_LENGTH)) {
            throw new IllegalArgumentException();
        }
        
        this.value = value;
    }
    
    private ObjectValue(Property[] properties, Value value) {
        super(ValueType.OBJECT);
        
        for (Property property : properties) {
            this.properties.put(property.getName(), property);
        }
        
        this.value = value;
    }
    
    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }
    
    public void setProperty(String name, String value) {
        properties.put(name, new Property(name, value));
    }
    
    public void addProperty(String name, String value) {
        Property property = properties.get(name);
        if (property == null) {
            property = new Property(name);
            properties.put(name, property);
        }
        property.add(value);
    }
    
    public String getProperty(String name) {
        Property property = properties.get(name);
        if (property != null) {
            return property.value();
        }
        return null;
    }
    
    public String[] getProperies(String name) {
        Property property = properties.get(name);
        if (property != null) {
            return property.values();
        }
        return null;
    }
    
    public Contact getCreator() {
        String value = getProperty(CREATOR_KEY);
        if (value != null) {
            return decodeContact(value);
        }
        return null;
    }
    
    public VectorClock<KUID> getVectorClock() {
        String value = getProperty(VECTOR_CLOCK_KEY);
        if (value != null) {
            return decodeVectorClock(value);
        }
        return null;
    }
    
    public long getContentLength() {
        String value = getProperty(CONTENT_LENGTH);
        return Long.parseLong(value);
    }

    @Override
    public InputStream getContent() throws IOException {
        return new InputOutputStream() {
            @Override
            protected void produce(OutputStream out) throws IOException {
                ValueOutputStream vos = new ValueOutputStream(out);
                writeHeader(vos);
                
                vos.writeCollection(properties.values());
                value.writeTo(vos);
                
                vos.close();
            }
        };
    }
    
    public static ObjectValue valueOf(InputStream in) throws IOException {
        ValueInputStream vis = new ValueInputStream(in);
        Property[] properties = vis.readProperties();
        long length = Property.getContentLength(properties);
        
        byte[] value = new byte[(int)length];
        vis.readFully(value);
        
        return new ObjectValue(properties, new ByteArrayValue(value));
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
        sb.append(value).append(" @ ").append(properties.values());
        return sb.toString();
    }
    
    private static Contact decodeContact(String value) {
        byte[] data = decode(value);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        
        ValueInputStream vis = null;
        try {
            vis = new ValueInputStream(new GZIPInputStream(bais));
            return vis.readContact();
        } catch (IOException err) {
            throw new IllegalStateException(err);
        } finally {
            IoUtils.close(vis);
        }
    }
    
    private static String encodeContact(Contact contact) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ValueOutputStream vos = null;
        try {
            vos = new ValueOutputStream(new GZIPOutputStream(baos));
            vos.writeContact(contact);
        } catch (IOException err) {
            throw new IllegalStateException(err);
        } finally {
            IoUtils.close(vos);
        }
        
        return encode(baos.toByteArray());
    }
    
    private static VectorClock<KUID> decodeVectorClock(String value) {
        byte[] data = decode(value);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        
        ValueInputStream vis = null;
        try {
            vis = new ValueInputStream(new GZIPInputStream(bais));
            return vis.readVectorClock();
        } catch (IOException err) {
            throw new IllegalStateException(err);
        } finally {
            IoUtils.close(vis);
        }
    }
    
    private static String encodeVectorClock(VectorClock<KUID> clock) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        ValueOutputStream vos = null;
        try {
            vos = new ValueOutputStream(new GZIPOutputStream(baos));
            vos.writeVectorClock(clock);
        } catch (IOException err) {
            throw new IllegalStateException(err);
        } finally {
            IoUtils.close(vos);
        }
        
        return encode(baos.toByteArray());
    }
    
    private static byte[] decode(String value) {
        Base64 decoder = new Base64(true);
        return decoder.decode(StringUtils.getBytes(value));
    }
    
    private static String encode(byte[] value) {
        byte[] base64 = Base64.encodeBase64(value, false, true);
        return StringUtils.toString(base64);
    }
    
    private static Map<String, String> contentLength(long length) {
        return contentLength(null, length);
    }

    private static Map<String, String> contentLength(Map<String, String> dst, long length) {
        if (dst == null) {
            return Collections.singletonMap(CONTENT_LENGTH, Long.toString(length));
        }
        
        if (!dst.containsKey(CONTENT_LENGTH)) {
            dst.put(CONTENT_LENGTH, Long.toString(length));
        }
        return dst;
    }
    
    public static class Property implements Iterable<String> {
        
        private static final Comparator<String> COMPARATOR 
                = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (o1 == null || o2 == null) {
                    throw new NullPointerException();
                }
                
                return o1.compareTo(o2);
            }
        };
        
        private final String name;
        
        private final Set<String> values = new TreeSet<String>(COMPARATOR);
        
        public Property(String name) {
            this.name = name;
        }
        
        public Property(String name, String value) {
            this(name);
            add(value);
        }
        
        public Property(String name, Collection<String> values) {
            this(name);
            addAll(values);
        }
        
        public String getName() {
            return name;
        }
        
        public int size() {
            return values.size();
        }
        
        @Override
        public Iterator<String> iterator() {
            return values.iterator();
        }

        public void addAll(Collection<String> c) {
            values.addAll(c);
        }
        
        public void add(String value) {
            values.add(value);
        }
        
        public String value() {
            return CollectionUtils.first(values);
        }
        
        public String[] values() {
            return values.toArray(new String[0]);
        }
        
        @Override
        public String toString() {
            return name + "=" + values.toString();
        }
        
        public static Property getProperty(String key, Property... properties) {
            for (Property property : properties) {
                if (property.getName().equalsIgnoreCase(key)) {
                    return property;
                }
            }
            return null;
        }
        
        public static long getContentLength(Property... properties) {
            Property property = getProperty(CONTENT_LENGTH, properties);
            return Long.parseLong(property.value());
        }
    }
}
