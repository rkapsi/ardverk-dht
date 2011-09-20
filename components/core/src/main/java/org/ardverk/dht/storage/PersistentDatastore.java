package org.ardverk.dht.storage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

import org.ardverk.coding.CodingUtils;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.rsrc.FileValue;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.rsrc.Value;
import org.ardverk.io.IoUtils;
import org.ardverk.security.MessageDigestUtils;
import org.ardverk.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A very simple implementation of {@link Datastore}.
 */
public class PersistentDatastore extends SimpleDatastore {

    private static final Logger LOG 
        = LoggerFactory.getLogger(PersistentDatastore.class);
    
    private final File store;
    
    private final File tmp;
    
    public PersistentDatastore(File dir, long frequency, TimeUnit unit) {
        this(dir, frequency, frequency, unit);
    }
    
    public PersistentDatastore(File dir, long frequency, 
            long timeout, final TimeUnit unit) {
        super(frequency, timeout, unit);
        
        this.store = mkdirs(new File(dir, "store"));
        this.tmp = mkdirs(new File(dir, "tmp"));
    }
    
    @Override
    protected void evict(long timeout, TimeUnit unit) {
        final long now = System.currentTimeMillis();
        final long timeoutInMillis = unit.toMillis(timeout);
        
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isFile()) {
                    if ((now - file.lastModified()) >= timeoutInMillis) {
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("Deleting: " + file);
                        }
                        file.delete();
                    }
                }
                return false;
            }
        };
        
        store.listFiles(filter);
        tmp.listFiles(filter);
    }
    
    @Override
    public Value store(Contact src, Key key, Value value) {
        Value response = OK;
        try {
            String name = createFileName(key);
            File tmp = createTmpFile(name);
            try {
                
                long length = consume(value, tmp);
                File file = createStoreFile(name);
                
                if (length == 0L) {
                    boolean success = file.delete();
                    if (!success) {
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("Not Found: " + file);
                        }
                        response = NOT_FOUND;
                    }
                } else {
                    boolean success = tmp.renameTo(file);
                    if (!success) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Failed to rename " + tmp + " to " + file);
                        }
                        response = INTERNAL_ERROR;
                    }
                }
            } finally {
                tmp.delete();
            }
            
        } catch (IOException err) {
            LOG.error("IOException", err);
            response = INTERNAL_ERROR;
        }
        
        return response;
    }

    @Override
    public Value get(Contact src, Key key) {
        String name = createFileName(key);
        File file = createStoreFile(name);
        if (file.exists()) {
            return new FileValue(file);
        }
        return null;
    }
    
    private File createStoreFile(String name) {
        return new File(store, name);
    }
    
    private File createTmpFile(String name) throws IOException {
        return File.createTempFile(name + "-", ".tmp", tmp);
    }
    
    private static long consume(Value value, File dst) throws IOException {
        
        BufferedOutputStream out 
            = new BufferedOutputStream(
                new FileOutputStream(dst));
        
        try {
            InputStream in = value.getContent();
            try {
                long length = 0;
                
                byte[] buffer = new byte[4*1024];
                int len = -1;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                    length += len;
                }
                return length;
            } finally {
                IoUtils.close(in);
            }
        } finally {
            IoUtils.close(out);
        }
    }
    
    private static File mkdirs(File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
            
            if (!dir.exists()) {
                throw new IllegalStateException(dir.getAbsolutePath());
            }
        }
        return dir;
    }
    
    private static String createFileName(Key key) {
        MessageDigest md = MessageDigestUtils.createSHA1();
        md.update(StringUtils.getBytes(key.getPath()));
        return CodingUtils.encodeBase16(md.digest());
    }
}
