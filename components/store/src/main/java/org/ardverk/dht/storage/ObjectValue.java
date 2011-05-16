package org.ardverk.dht.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.ardverk.collection.CollectionUtils;
import org.ardverk.dht.KUID;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.rsrc.ByteArrayValue;
import org.ardverk.dht.rsrc.InputStreamValue;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.io.InputOutputStream;
import org.ardverk.version.VectorClock;

public class ObjectValue extends SimpleValue {

    private static final Comparator<String> COMPARATOR 
            = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            if (o1 == null || o2 == null) {
                throw new NullPointerException();
            }
            
            return o1.compareToIgnoreCase(o1);
        }
    };

    public static final String CREATOR_KEY = "X-Ardverk-Creator";
    
    public static final String VECTOR_CLOCK_KEY = "X-Ardverk-VectorClock";
    
    private final Map<String, Property> properties 
        = new TreeMap<String, Property>(COMPARATOR);
    
    private final Value value;
    
    public ObjectValue(Map<String, String> properties, Value value) {
        this(null, null, properties, value);
    }
    
    public ObjectValue(Contact creator, VectorClock<KUID> clock, byte[] value) {
        this(creator, clock, new ByteArrayValue(value));
    }
    
    public ObjectValue(Contact creator, VectorClock<KUID> clock, Value value) {
        this(creator, clock, null, value);
    }
    
    public ObjectValue(Contact creator, VectorClock<KUID> clock,
            Map<String, String> properties, Value value) {
        super(ValueType.OBJECT);
        
        if (properties != null) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                addProperty(entry.getKey(), entry.getValue());
            }
        }
        
        if (creator != null) {
            setProperty(CREATOR_KEY, encode(creator));
        }
        
        if (clock != null) {
            setProperty(CREATOR_KEY, encode(clock));
        }
        
        this.value = value;
    }
    
    private ObjectValue(Property[] properties, Value value) {
        super(ValueType.OBJECT);
        
        for (Property property : properties) {
            String key = property.getName().toLowerCase(Locale.US);
            this.properties.put(key, property);
        }
        
        this.value = value;
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
        return null;
    }
    
    public VectorClock<KUID> getVectorClock() {
        String value = getProperty(VECTOR_CLOCK_KEY);
        return null;
    }

    @Override
    public InputStream getContent() throws IOException {
        return new InputOutputStream() {
            @Override
            protected void produce(OutputStream out) throws IOException {
                DataOutputStream dos = new DataOutputStream(out);
                
                writeHeader(dos);
                
                dos.writeInt(properties.size());
                for (Property property : properties.values()) {
                    dos.writeUTF(property.getName());
                    
                    dos.writeInt(property.size());
                    for (String value : property) {
                        dos.writeUTF(value);
                    }
                }
                
                value.writeTo(dos);
                dos.close();
            }
        };
    }
    
    public static ObjectValue valueOf(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        
        Property[] properties = new Property[dis.readInt()];
        for (int i = 0; i < properties.length; i++) {
            String name = dis.readUTF();
            
            String[] values = new String[dis.readInt()];
            for (int j = 0; j < values.length; j++) {
                values[j] = dis.readUTF();
            }
            
            properties[i] = new Property(name, Arrays.asList(values));
        }
        
        return new ObjectValue(properties, new InputStreamValue(dis));
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
        return properties.values().toString();
    }
    
    private static String encode(Object value) {
        return value.toString();
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
    }
}
