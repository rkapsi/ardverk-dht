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
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.protocol.HTTP;
import org.ardverk.coding.CodingUtils;
import org.ardverk.dht.KUID;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.io.FileUtils;
import org.ardverk.io.IoUtils;
import org.ardverk.io.StreamUtils;
import org.ardverk.io.Writable;
import org.ardverk.security.MessageDigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectDatastore2 extends AbstractObjectDatastore implements Closeable {

    private static final Logger LOG 
        = LoggerFactory.getLogger(ObjectDatastore2.class);

    private static final AtomicInteger COUNTER = new AtomicInteger();
    
    private static final String LIST = "list";
    
    private static final String VTAG = "vtag";
    
    private final Index index = DefaultIndex.create(null);
    
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
        File contextFile = null;
        
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
            
            Vclock vclock = upsertVclock(context);
            
            contextFile = mkContextFile(key, valueId, true);
            write(contextFile, context);
            
            File indexFile = mkIndexFile(key, true);
            Index index = index(indexFile, key);
            
            index.put("current", valueId.toHexString());
            index.put(vclock.getVTag(), valueId.toHexString());
            
            write(indexFile, index);
            
            success = true;
            return ResponseFactory.createOk();
            
        } finally {
            if (!success) {
                deleteAll(contentFile, contextFile);
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
    
    private static void write(File file, Writable writable) throws IOException {
        OutputStream out = new BufferedOutputStream(
                new FileOutputStream(file));
        try {
            writable.writeTo(out);
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
        
        File indexFile = mkIndexFile(key, false);
        if (!indexFile.exists()) {
            return ResponseFactory.createNotFound();
        }
        
        Index index = Index.valueOf(indexFile);
        String valueId = index.get("current");
        if (valueId == null) {
            return ResponseFactory.createNotFound();
        }
        
        File contextFile = mkContextFile(key, valueId, false);
        if (!contextFile.exists()) {
            return ResponseFactory.createNotFound();
        }
        
        Context context = Context.valueOf(contextFile);
        
        if (context.containsHeader(Constants.TOMBSTONE)) {
            return ResponseFactory.createNotFound();
        }
        
        context.addHeader(Constants.tombstone());
        
        // TODO: Vclock!!!
        write(contextFile, context);
        
        File contentFile = mkContentFile(key, valueId, false);
        deleteAll(contentFile);
        
        return ResponseFactory.createOk();
    }
    
    @Override
    protected Response handleHead(Contact src, Key key, Request request,
            InputStream in) throws IOException {
        
        File indexFile = mkIndexFile(key, false);
        if (!indexFile.exists()) {
            return ResponseFactory.createNotFound();
        }
        
        Index index = Index.valueOf(indexFile);
        String valueId = index.get("current");
        if (valueId == null) {
            return ResponseFactory.createNotFound();
        }
        
        File contextFile = mkContextFile(key, valueId, false);
        if (!contextFile.exists()) {
            return ResponseFactory.createNotFound();
        }
        
        Context context = Context.valueOf(contextFile);
        if (context.containsHeader(Constants.TOMBSTONE)) {
            return ResponseFactory.createNotFound();
        }
        
        context.addHeader(Constants.NO_CONTENT);
        return new Response(StatusLine.OK, context);
    }

    @Override
    protected Response handleGet(Contact src, 
            Key key, boolean store) throws IOException {
        
        File indexFile = mkIndexFile(key, false);
        if (!indexFile.exists()) {
            return null;
        }
        
        Map<String, String> query = key.getQueryString();
        if (query != null && !query.isEmpty()) {
            if (query.containsKey(LIST)) {
                return list(src, key, query);
            } else if (query.containsKey(VTAG)) {
                return vtag(src, key, query);
            }
        }
        
        Index index = Index.valueOf(indexFile);
        String valueId = index.get("current");
        if (valueId == null) {
            return null;
        }
        
        File contextFile = mkContextFile(key, valueId, false);
        if (!contextFile.exists()) {
            return null;
        }
        
        Context context = Context.valueOf(contextFile);
        if (context.containsHeader(Constants.TOMBSTONE)) {
            return null;
        }
        
        File contentFile = mkContentFile(key, valueId, false);
        if (!contentFile.exists()) {
            return null;
        }
        
        return new Response(StatusLine.OK, context, 
                new FileValueEntity(contentFile));
    }
    
    protected Response list(Contact src, Key key, Map<String, String> query) {
        return null;
    }
    
    protected Response vtag(Contact src, Key key, Map<String, String> query) {
        return null;
    }
    
    private static void deleteAll(File... files) {
        for (File file : files) {
            org.apache.commons.io.FileUtils.deleteQuietly(file);
        }
    }
}
