package org.ardverk.dht.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.ardverk.dht.KUID;
import org.ardverk.io.IoUtils;
import org.ardverk.utils.ArrayUtils;
import org.ardverk.version.Occured;
import org.ardverk.version.Vector;
import org.ardverk.version.VectorClock;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

public class VclockUtils {
    
    private static final String VCLOCK = VclockUtils.toString(
            VectorClock.<KUID>create());
    
    private static final Header[] INIT = new Header[] {
        new BasicHeader(Constants.VCLOCK, VCLOCK)
    };
    
    private VclockUtils() {}
    
    public static <K> Occured compare(VectorClock<K> existing, 
            VectorClock<K> clock) {
        
        if (existing == null || existing.isEmpty()
                || clock == null || clock.isEmpty()) {
            return Occured.AFTER;
        }
        
        return clock.compareTo(existing);
    }
    
    public static String toString(VectorClock<KUID> vclock) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(
                new DeflaterOutputStream(baos));
        
        int size = 0;
        if (vclock != null) {
            size = vclock.size();
        }
        
        try {
            dos.writeShort(size);
            
            if (0 < size) {
                dos.writeLong(vclock.getCreationTime());
                for (Map.Entry<? extends KUID, ? extends Vector> entry 
                        : vclock.entrySet()) {
                    KUID contactId = entry.getKey();
                    Vector vector = entry.getValue();
                    
                    dos.write(contactId.length());
                    contactId.writeTo(dos);
                    
                    dos.writeLong(vector.getTimeStamp());
                    dos.writeInt(vector.getValue());
                }
            }
        } catch (IOException err) {
            throw new IllegalStateException(err);
        } finally {
            IoUtils.close(dos);
        }
        
        return Base64.encode(baos.toByteArray());
    }
    
    public static VectorClock<KUID> valueOf(String value) {
        ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(value));
        DataInputStream dis = new DataInputStream(new InflaterInputStream(bais));
        
        try {
            int size = dis.readUnsignedShort();
            if (0 < size) {
                long creationTime = dis.readLong();
                Map<KUID, Vector> map = new TreeMap<KUID, Vector>();
                
                for (int i = 0; i < size; i++) {
                    byte[] key = new byte[dis.readUnsignedByte()];
                    dis.readFully(key);
                    
                    long timeStamp = dis.readLong();
                    int vector = dis.readInt();
                    
                    map.put(KUID.create(key), new Vector(timeStamp, vector));
                }
                
                return VectorClock.create(creationTime, map);
            }
        } catch (IOException err) {
            throw new IllegalStateException(err);
        } finally {
            IoUtils.close(dis);
        }
        
        return VectorClock.create();
    }
    
    public static VectorClock<KUID> valueOf(Properties properties) {
        Header[] clientIds = properties.removeHeaders(Constants.CLIENT_ID);
        if (ArrayUtils.isEmpty(clientIds)) {
            throw new NoSuchElementException(Constants.CLIENT_ID);
        }
        
        Header[] vclocks = properties.removeHeaders(Constants.VCLOCK);
        return valueOf(vclocks, clientIds);
    }
    
    public static VectorClock<KUID> valueOf(Header[] vclocks, Header[] clientIds) {
        VectorClock<KUID> vclock = null;
        
        if (!ArrayUtils.isEmpty(vclocks)) {
            vclock = valueOf(vclocks[0].getValue());
        } else {
            vclock = VectorClock.create();
        }
        
        KUID clientId = KUID.create(clientIds[0].getValue(), 16);
        return vclock.update(clientId);
    }
}
