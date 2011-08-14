package org.ardverk.dht.storage;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.protocol.HTTP;
import org.ardverk.coding.CodingUtils;
import org.ardverk.dht.KUID;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.storage.sql.DefaultIndex2;
import org.ardverk.dht.storage.sql.DefaultIndex2.Values;
import org.ardverk.io.FileUtils;
import org.ardverk.io.IoUtils;
import org.ardverk.io.StreamUtils;
import org.ardverk.security.MessageDigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectDatastore2 extends AbstractObjectDatastore implements Closeable {

    private static final Logger LOG 
        = LoggerFactory.getLogger(ObjectDatastore2.class);

    private static final AtomicInteger COUNTER = new AtomicInteger();
    
    private static final String LIST = "list";
    
    private static final String VTAG = "vtag";
    
    private static final String VALUE_ID = "valueId";
    
    private static final String MARKER = "marker";
    
    private static final String MAX_COUNT = "max-count";
    
    private final DefaultIndex2 index;
    
    private final File directory;
    
    private final File content;
    
    public ObjectDatastore2() throws IOException {
        this("data/" + COUNTER.incrementAndGet());
    }
    
    public ObjectDatastore2(String path) throws IOException {
        this(new File(path));
    }
    
    public ObjectDatastore2(File directory) throws IOException {
        this.directory = directory;
        this.content = FileUtils.mkdirs(directory, "content", true);
        
        index = DefaultIndex2.create(directory);
    }
    
    @Override
    public void close() throws IOException {
        index.close();
    }
    
    private File mkContentFile(Key key, KUID valueId, boolean mkdirs) {
        return mkContentFile(key, valueId.toHexString(), mkdirs);
    }
    
    private File mkContentFile(Key key, String valueId, boolean mkdirs) {
        return mkfile(content, key, valueId, mkdirs);
    }
    
    private static File mkfile(File parent, Key key, String name, boolean mkdirs) {
        File dir = new File(parent, key.getPath());
        File file = new File(dir, name);
        
        if (mkdirs) {
            dir.mkdirs();
        }
        
        return file;
    }
    
    @Override
    protected Response handlePut(Contact src, Key key, Request request,
            InputStream in) throws IOException {
        
        Context context = request.getContext();
        
        MessageDigest md5 = MessageDigestUtils.createMD5();
        MessageDigest sha1 = MessageDigestUtils.createSHA1();
        
        DigestInputStream dis = new DigestInputStream(
                in, MessageDigestUtils.wrap(md5, sha1));
        
        // Create a random ID
        KUID valueId = KUID.createRandom(key.getId());
        
        File contentFile = null;
        
        boolean success = false;
        try {
            
            contentFile = mkContentFile(key, valueId, true);
            writeContent(context, contentFile, dis);
            
            if (!digest(context, Constants.CONTENT_MD5, md5)) {
                return Response.INTERNAL_SERVER_ERROR;
            }
            
            if (!digest(context, Constants.CONTENT_SHA1, sha1)) {
                return Response.INTERNAL_SERVER_ERROR;
            }
            
            context.addHeader(Constants.VALUE_ID, 
                    valueId.toHexString());
            
            Vclock vclock = upsertVclock(context);
            
            /*File indexFile = mkIndexFile(key, true);
            Index index = index(indexFile, key);
            
            index.put("current", valueId.toHexString());
            index.put(vclock.getVTag(), valueId.toHexString());
            
            write(indexFile, index);*/
            
            try {
                index.add(key, context, valueId);
            } catch (Exception err) {
                throw new IOException("Exception", err);
            }
            
            success = true;
            return ResponseFactory.createOk();
            
        } finally {
            if (!success) {
                deleteAll(contentFile);
            }
        }
    }
    
    private static void writeContent(Context context, 
            File file, InputStream in) throws IOException {
        
        OutputStream out = new BufferedOutputStream(
                new FileOutputStream(file));
        try {
            long length = context.getLongValue(HTTP.CONTENT_LEN, 0L);
            StreamUtils.copy(in, out, length);
        } finally {
            IoUtils.close(out);
        }
    }
    
    private static boolean digest(Context context, 
            String name, MessageDigest md) {
        
        byte[] actual = md.digest();
        
        String expected = context.getStringValue(name);
        if (expected != null) {
            byte[] decoded = Base64.decodeBase64(expected);
            if (!Arrays.equals(decoded, actual)) {
                return false;
            }
        } else {
            context.addHeader(name, 
                    Base64.encodeBase64String(actual));
        }
        
        if (name.equals(Constants.CONTENT_MD5)) {
            String etag = "\"" + CodingUtils.encodeBase16(actual) + "\"";
            context.addHeader(Constants.ETAG, etag);
        }
        
        return true;
    }

    private static Vclock upsertVclock(Context context) throws IOException {
        Vclock vclock = VclockUtils.valueOf(context);
        context.addHeader(Constants.VCLOCK, vclock.toString());
        context.addHeader(Constants.VTAG, vclock.getVTag());
        return vclock;
    }
    
    @Override
    protected Response handleDelete(Contact src, Key key, Request request,
            InputStream in) throws IOException {
        
        Map.Entry<KUID, Context> value = null;
        try {
            value = index.getCurrent(key);
        } catch (Exception err) {
            throw new IOException("Exception", err);
        }
        
        if (value == null) {
            return ResponseFactory.createNotFound();
        }
        
        // TODO
        KUID valueId = value.getKey();
        Context context = value.getValue();
        
        if (context.containsHeader(Constants.TOMBSTONE)) {
            return ResponseFactory.createNotFound();
        }
        
        context.addHeader(Constants.tombstone());
        
        File contentFile = mkContentFile(key, valueId, false);
        deleteAll(contentFile);
        
        // TODO: Write the Vclock and a Tombstone instead
        try {
            index.delete(key, Collections.singleton(valueId));
        } catch (Exception err) {
            throw new IOException("Exception", err);
        }
        
        return ResponseFactory.createOk();
    }
    
    @Override
    protected Response handleHead(Contact src, Key key, Request request,
            InputStream in) throws IOException {
        
        Map.Entry<KUID, Context> value = null;
        try {
            value = index.getCurrent(key);
        } catch (Exception err) {
            throw new IOException("Exception", err);
        }
        
        if (value == null) {
            return ResponseFactory.createNotFound();
        }
        
        // TODO
        KUID valueId = value.getKey();
        Context context = value.getValue();
        
        if (context.containsHeader(Constants.TOMBSTONE)) {
            return ResponseFactory.createNotFound();
        }
        
        context.addHeader(Constants.NO_CONTENT);
        return new Response(StatusLine.OK, context);
    }

    @Override
    protected Response handleGet(Contact src, 
            Key key, boolean store) throws IOException {
        
        Map<String, String> query = key.getQueryString();
        if (query != null && !query.isEmpty()) {
            if (query.containsKey(VALUE_ID)) {
                return value(src, key, query);
            } else if (query.containsKey(LIST)) {
                return list(src, key, query);
            }
        }
        
        Values values = listValues(src, key, query);
        if (values == null) {
            return null;
        }
        
        if (values.size() == 1) {
            Map.Entry<KUID, Context> value = values.firstEntry();
            return value(src, key, value.getKey(), value.getValue());
        }
        
        return ListValuesResponse.create(
                StatusLine.MULTIPLE_CHOICES, key, values);
        
    }
    
    private Values listValues(Contact src, Key key, Map<String, String> query) throws IOException {
        KUID marker = getMarker(query);
        int maxCount = getMaxCount(query, 1000);
        
        try {
            return index.listValues(key, marker, maxCount);
        } catch (SQLException err) {
            throw new IOException("SQLException", err);
        }
    }
    
    private Response list(Contact src, Key key, Map<String, String> query) throws IOException {
        Values values = listValues(src, key, query);
        if (values != null) {
            return ListValuesResponse.create(StatusLine.OK, key, values);
        }
        return null;
    }
    
    private Response value(Contact src, Key key, Map<String, String> query) throws IOException {
        KUID valueId = getValueId(query);
        
        Context context = null;
        try {
            context = index.get(key, valueId);
        } catch (SQLException err) {
            throw new IOException("SQLException", err);
        }
        
        if (context != null) {
            return value(src, key, valueId, context);
        }
        
        return null;
    }
    
    private Response value(Contact src, Key key, KUID valueId, Context context) {
        File contentFile = mkContentFile(key, valueId, false);
        if (!contentFile.exists()) {
            return null;
        }
        
        return new Response(StatusLine.OK, context, 
                new FileValueEntity(contentFile));
    }
    
    private static void deleteAll(File... files) {
        for (File file : files) {
            org.apache.commons.io.FileUtils.deleteQuietly(file);
        }
    }
    
    private static KUID getMarker(Map<String, String> query) {
        return getKUID(MARKER, query);
    }
    
    private static KUID getValueId(Map<String, String> query) {
        return getKUID(VALUE_ID, query);
    }
    
    private static KUID getKUID(String key, Map<String, String> query) {
        String marker = query != null ? query.get(key) : null;
        return marker != null ? KUID.create(marker, 16) : null;
    }
    
    private static int getMaxCount(Map<String, String> query, int defaultValue) {
        String maxCount = query != null ? query.get(MAX_COUNT) : null;
        if (maxCount != null) {
            return Math.min(defaultValue, Integer.parseInt(maxCount));
        }
        return defaultValue;
    }
}
