package org.ardverk.dht.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.commons.codec.binary.Base64;
import org.ardverk.coding.CodingUtils;
import org.ardverk.dht.KUID;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.io.DataUtils;
import org.ardverk.io.IoUtils;
import org.ardverk.io.NopOutputStream;
import org.ardverk.io.Writable;
import org.ardverk.security.MessageDigestUtils;
import org.ardverk.utils.StringUtils;
import org.ardverk.version.Occured;
import org.ardverk.version.Vector;
import org.ardverk.version.VectorClock;
import org.ardverk.version.Version;

public class Vclock implements Version<Vclock>, Writable {
    
    public static Vclock create(Key key) {
        return new Vclock(key, VectorClock.<String>create());
    }
    
    private final Key key;
    
    private final VectorClock<String> vclock;
    
    private byte[] vtag = null;
    
    private Vclock(Key key, VectorClock<String> vclock) {
        this.key = key;
        this.vclock = vclock;
    }

    private byte[] calculate() {
        if (vtag == null) {
            MessageDigest md = MessageDigestUtils.createSHA1();
            
            String uri = key.strip().getURI().toString();
            md.update(StringUtils.getBytes(uri));
            
            DigestOutputStream out = new DigestOutputStream(
                    NopOutputStream.OUT, md);
            
            try {
                
                DataUtils.short2beb(vclock.size(), out);
                
                for (Map.Entry<String, ? extends Vector> entry 
                        : vclock.entrySet()) {
                    String key = entry.getKey();
                    Vector value = entry.getValue();
                    
                    StringUtils.writeString(key, out);
                    DataUtils.int2beb(value.getValue(), out);
                }
                
            } catch (IOException err) {
                throw new IllegalStateException("IOException", err);
            } finally {
                IoUtils.close(out);
            }
            
            vtag = md.digest();
        }
        return vtag;
    }
    
    public KUID vtag() {
        return KUID.create(calculate());
    }
    
    public String vtag16() {
        return CodingUtils.encodeBase16(calculate());
    }
    
    public String vtag64() {
        return Base64.encodeBase64URLSafeString(calculate());
    }
    
    public long getCreationTime() {
        return vclock.getCreationTime();
    }
    
    public long getLastModified() {
        return vclock.getLastModified();
    }
    
    public Vclock update(String id) {
        return new Vclock(key, vclock.update(id));
    }
    
    public int size() {
        return vclock.size();
    }
    
    public boolean isEmpty() {
        return vclock.isEmpty();
    }
    
    @Override
    public Occured compareTo(Vclock other) {
        return vclock.compareTo(other.vclock);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(calculate());
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Vclock)) {
            return false;
        }
        
        Vclock other = (Vclock)o;
        return Arrays.equals(calculate(), other.calculate());
    }
    
    @Override
    public String toString() {
        return toString(true);
    }
    
    public String toString(boolean encode) {
        if (encode) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DeflaterOutputStream deflater = new DeflaterOutputStream(baos);
            
            try {
                writeTo(deflater);
            } catch (IOException err) {
                throw new IllegalStateException(err);
            } finally {
                IoUtils.close(deflater);
            }
            
            return Base64.encodeBase64String(baos.toByteArray());
        }
        
        return vtag16() + "/" + vclock.toString();
    }
    
    @Override
    public void writeTo(OutputStream out) throws IOException {
        int size = vclock.size();
        DataUtils.short2beb(size, out);
        
        if (size != 0) {
            DataUtils.long2beb(vclock.getCreationTime(), out);
            
            for (Map.Entry<String, ? extends Vector> entry 
                    : vclock.entrySet()) {
                String key = entry.getKey();
                Vector value = entry.getValue();
                
                StringUtils.writeString(key, out);
                
                DataUtils.long2beb(value.getTimeStamp(), out);
                DataUtils.int2beb(value.getValue(), out);
            }
        }
    }
    
    public static Vclock valueOf(Key key, String value) throws IOException {
        byte[] data = Base64.decodeBase64(value);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        InflaterInputStream inflater = new InflaterInputStream(bais);
        
        try {
            return valueOf(key, inflater);
        } finally {
            IoUtils.close(inflater);
        }
    }
    
    public static Vclock valueOf(Key key, InputStream in) throws IOException {
        
        long creationTime = System.currentTimeMillis();
        SortedMap<String, Vector> map = new TreeMap<String, Vector>();
        
        int size = DataUtils.beb2ushort(in);
        
        if (size != 0) {
            
            creationTime = DataUtils.beb2long(in);
            
            for (int i = 0; i < size; i++) {
                String id = StringUtils.readString(in);
                
                long timeStamp = DataUtils.beb2long(in);
                int value = DataUtils.beb2int(in);
                
                map.put(id, new Vector(timeStamp, value));
            }
        }
        
        VectorClock<String> vclock = VectorClock.create(creationTime, map);
        return new Vclock(key, vclock);
    }
}
