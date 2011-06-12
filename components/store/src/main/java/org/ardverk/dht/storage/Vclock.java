package org.ardverk.dht.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.commons.codec.binary.Base64;
import org.ardverk.coding.CodingUtils;
import org.ardverk.io.DataUtils;
import org.ardverk.io.IoUtils;
import org.ardverk.io.Writable;
import org.ardverk.security.MessageDigestUtils;
import org.ardverk.utils.StringUtils;
import org.ardverk.version.Occured;
import org.ardverk.version.Vector;
import org.ardverk.version.VectorClock;
import org.ardverk.version.Version;

public class Vclock implements Version<Vclock>, Writable, Serializable {
    
    private static final long serialVersionUID = -8465932555084986397L;
    
    public static Vclock create() {
        return new Vclock(VectorClock.<String>create());
    }
    
    private final byte[] vtag;
    
    private final VectorClock<String> vclock;
    
    private Vclock(VectorClock<String> vclock) {
        this(vtag(vclock), vclock);
    }
    
    private Vclock(byte[] vtag, VectorClock<String> vclock) {
        this.vtag = vtag;
        this.vclock = vclock;
    }
    
    public Vclock update(String key) {
        return new Vclock(vclock.update(key));
    }
    
    public String getVTag() {
        return Base64.encodeBase64URLSafeString(vtag);
    }
    
    public long getCreationTime() {
        return vclock.getCreationTime();
    }
    
    public long getLastModified() {
        return vclock.getLastModified();
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
        return Arrays.hashCode(vtag);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Vclock)) {
            return false;
        }
        
        Vclock other = (Vclock)o;
        return Arrays.equals(vtag, other.vtag);
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
        
        return CodingUtils.encodeBase16(vtag) + "/" + vclock.toString();
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
    
    public static Vclock valueOf(String value) throws IOException {
        byte[] data = Base64.decodeBase64(value);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        InflaterInputStream inflater = new InflaterInputStream(bais);
        
        try {
            return valueOf(inflater);
        } finally {
            IoUtils.close(inflater);
        }
    }
    
    public static Vclock valueOf(InputStream in) throws IOException {
        
        long creationTime = System.currentTimeMillis();
        SortedMap<String, Vector> map = new TreeMap<String, Vector>();
        
        // Calculate the VTag on-the-fly!
        MessageDigest md = MessageDigestUtils.createSHA1();
        DigestInputStream dis = new DigestInputStream(in, md);
        
        int size = DataUtils.beb2ushort(dis);
        
        if (size != 0) {
            
            // Ignore the creationTime
            dis.on(false);
            creationTime = DataUtils.beb2long(dis);
            dis.on(true);
            
            for (int i = 0; i < size; i++) {
                String key = StringUtils.readString(in);
                
                // Ignore the timeStamp
                dis.on(false);
                long timeStamp = DataUtils.beb2long(dis);
                dis.on(true);
                
                int value = DataUtils.beb2int(dis);
                
                map.put(key, new Vector(timeStamp, value));
            }
        }
        
        byte[] vtag = md.digest();
        VectorClock<String> vclock = VectorClock.create(creationTime, map);
        
        return new Vclock(vtag, vclock);
    }
    
    /**
     * Calculates and returns the VTag.
     */
    private static byte[] vtag(VectorClock<String> vclock) {
        final MessageDigest md = MessageDigestUtils.createSHA1();
        
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                md.update((byte)b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                md.update(b, off, len);
            }
        };
        
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
        
        return md.digest();
    }
}
