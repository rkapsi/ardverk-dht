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
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.protocol.HTTP;
import org.ardverk.coding.CodingUtils;
import org.ardverk.dht.KUID;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.dht.storage.Index.Values;
import org.ardverk.dht.storage.message.Context;
import org.ardverk.dht.storage.message.FileValueEntity;
import org.ardverk.dht.storage.message.Request;
import org.ardverk.dht.storage.message.Response;
import org.ardverk.dht.storage.message.ResponseFactory;
import org.ardverk.dht.storage.message.StatusLine;
import org.ardverk.dht.storage.sql.DefaultIndex;
import org.ardverk.io.FileUtils;
import org.ardverk.io.IoUtils;
import org.ardverk.io.StreamUtils;
import org.ardverk.security.MessageDigestUtils;

public class IndexDatastore extends AbstractIndexDatastore implements Closeable {
    
    private static final String LIST = "list";
    
    private static final String VALUE_ID = "valueId";
    
    private static final String MARKER = "marker";
    
    private static final String MAX_COUNT = "max-count";
    
    private final Index index;
    
    private final File content;
    
    public IndexDatastore(String path) throws IOException {
        this(new File(path));
    }
    
    public IndexDatastore(File directory) throws IOException {
        this.content = FileUtils.mkdirs(directory, "content", true);
        
        index = DefaultIndex.create(directory);
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
            
            upsertVclock(key, context);
            
            try {
                index.add(key, context, valueId);
            } catch (Exception err) {
                throw new IOException("Exception", err);
            }
            
            success = true;
            return ResponseFactory.ok();
            
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

    private static Vclock upsertVclock(Key key, Context context) throws IOException {
        Vclock vclock = VclockUtils.valueOf(key, context);
        context.addHeader(Constants.VCLOCK, vclock.toString());
        context.addHeader(Constants.VTAG, vclock.vtag64());
        return vclock;
    }
    
    @Override
    protected Response handleDelete(Contact src, Key key, Request request,
            InputStream in) throws IOException {
        
        Map<String, String> query = key.getQueryString();
        if (query != null && !query.isEmpty()) {
            if (query.containsKey(VALUE_ID)) {
                return delete(src, key, query);
            }
        }
        
        Values values = listValues(src, key, query);
        if (values == null) {
            return ResponseFactory.notFound();
        }
        
        if (values.size() == 1) {
            Map.Entry<KUID, Context> value = values.firstEntry();
            return delete(src, key, value.getKey());
        }
        
        return ResponseFactory.list(
                StatusLine.MULTIPLE_CHOICES, key, values);
    }
    
    private Response delete(Contact src, Key key, Map<String, String> query) throws IOException {
        KUID valueId = getValueId(query);
        return delete(src, key, valueId);
    }
    
    private Response delete(Contact src, Key key, KUID valueId) throws IOException {
        boolean success = false;
        
        try {
            success = index.delete(key, valueId);
        } catch (Exception err) {
            throw new IOException("Exception", err);
        }
        
        if (!success) {
            return ResponseFactory.notFound();
        }
        
        File value = mkContentFile(key, valueId, false);
        deleteAll(value);
        
        return ResponseFactory.ok();
    }
    
    @Override
    protected Response handleHead(Contact src, Key key, Request request,
            InputStream in) throws IOException {
        
        Map<String, String> query = key.getQueryString();
        if (query != null && !query.isEmpty()) {
            if (query.containsKey(VALUE_ID)) {
                return head(src, key, query);
            }
        }
        
        Values values = listValues(src, key, query);
        if (values == null) {
            return ResponseFactory.notFound();
        }
        
        if (values.size() == 1) {
            Map.Entry<KUID, Context> value = values.firstEntry();
            return head(src, key, value.getKey());
        }
        
        return ResponseFactory.list(
                StatusLine.MULTIPLE_CHOICES, key, values);
    }

    private Response head(Contact src, Key key, Map<String, String> query) throws IOException {
        KUID valueId = getValueId(query);
        return head(src, key, valueId);
    }

    private Response head(Contact src, Key key, KUID valueId) throws IOException {
        Context context = null;
        try {
            context = index.get(key, valueId);
        } catch (Exception err) {
            throw newIoException("Exception", err);
        }
        
        if (context == null) {
            return ResponseFactory.notFound();
        }
        
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
        
        return ResponseFactory.list(
                StatusLine.MULTIPLE_CHOICES, key, values);
    }
    
    private Values listValues(Contact src, Key key, Map<String, String> query) throws IOException {
        KUID marker = getMarker(query);
        int maxCount = getMaxCount(query, 1000);
        
        try {
            return index.values(key, marker, maxCount);
        } catch (Exception err) {
            throw newIoException("Exception", err);
        }
    }
    
    private Response list(Contact src, Key key, Map<String, String> query) throws IOException {
        Values values = listValues(src, key, query);
        if (values != null) {
            return ResponseFactory.list(StatusLine.OK, key, values);
        }
        return null;
    }
    
    private Response value(Contact src, Key key, Map<String, String> query) throws IOException {
        KUID valueId = getValueId(query);
        
        Context context = null;
        try {
            context = index.get(key, valueId);
        } catch (Exception err) {
            throw newIoException("Exception", err);
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
    
    private static IOException newIoException(String message, Throwable t) {
        if (t instanceof IOException) {
            return (IOException)t;
        }
        
        return new IOException(message, t);
    }
}
