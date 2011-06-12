package org.ardverk.dht.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.protocol.HTTP;
import org.ardverk.coding.CodingUtils;
import org.ardverk.dht.KUID;
import org.ardverk.dht.routing.Contact;
import org.ardverk.dht.rsrc.Key;
import org.ardverk.io.DataUtils;
import org.ardverk.io.FileUtils;
import org.ardverk.io.IoUtils;
import org.ardverk.io.StreamUtils;
import org.ardverk.io.Writable;
import org.ardverk.security.MessageDigestUtils;
import org.ardverk.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectDatabase2 extends AbstractObjectDatabase {

    private static final Logger LOG 
        = LoggerFactory.getLogger(ObjectDatabase2.class);

    private static final AtomicInteger COUNTER = new AtomicInteger();
    
    private static final String LIST = "list";
    
    private static final String VTAG = "vtag";
    
    private final File directory;
    
    private final File index;
    
    private final File context;
    
    private final File content;
    
    public ObjectDatabase2() throws IOException {
        this("data/" + COUNTER.incrementAndGet());
    }
    
    public ObjectDatabase2(String path) throws IOException {
        this(new File(path));
    }
    
    public ObjectDatabase2(File directory) throws IOException {
        this.directory = directory;
        
        this.index = FileUtils.mkdirs(directory, "index", true);
        this.context = FileUtils.mkdirs(directory, "context", true);
        this.content = FileUtils.mkdirs(directory, "content", true);
    }
    
    private File mkIndexFile(Key key, boolean mkdirs) {
        File file = new File(index, key.getPath());
        if (mkdirs) {
            file.getParentFile().mkdirs();
        }
        return file;
    }
    
    private File mkContextFile(Key key, KUID valueId, boolean mkdirs) {
        return mkContextFile(key, valueId.toHexString(), mkdirs);
    }
    
    private File mkContextFile(Key key, String valueId, boolean mkdirs) {
        return mkfile(context, key, valueId, mkdirs);
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
    
    private static Index index(File file, Key key) throws IOException {
        if (file.exists()) {
            return Index.valueOf(file);
        }
        
        return new Index(key.getPath());
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
        
        Map<String, String> query = key.getQueryString();
        if (query != null && !query.isEmpty()) {
            if (query.containsKey(LIST)) {
                return list(src, key, query);
            } else if (query.containsKey(VTAG)) {
                return vtag(src, key, query);
            }
        }
            
        File indexFile = mkIndexFile(key, false);
        if (!indexFile.exists()) {
            return null;
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
    
    private static class Index implements Writable {

        private static final int VERSION = 0;
        
        private final String path;
        
        private final Map<String, String> properties;
        
        public Index(String path) {
            this(path, new HashMap<String, String>());
        }
        
        public Index(String path, Map<String, String> properties) {
            this.path = path;
            this.properties = properties;
        }
        
        public String put(String key, String value) {
            return properties.put(key, value);
        }
        
        public String get(String key) {
            return properties.get(key);
        }
        
        public String remove(String key) {
            return properties.remove(key);
        }
        
        @Override
        public int hashCode() {
            return path.hashCode();
        }
        
        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof Index)) {
                return false;
            }
            
            Index other = (Index)o;
            return path.equals(other.path);
        }
        
        @Override
        public void writeTo(OutputStream out) throws IOException {
            out.write(VERSION);
            
            StringUtils.writeString(path, out);
            
            int size = properties.size();
            DataUtils.int2beb(size, out);
            
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                StringUtils.writeString(entry.getKey(), out);
                StringUtils.writeString(entry.getValue(), out);
            }
        }
        
        public static Index valueOf(File file) throws IOException {
            InputStream in = new BufferedInputStream(
                    new FileInputStream(file));
            try {
                return valueOf(in);
            } finally {
                IoUtils.close(in);
            }
        }

        public static Index valueOf(InputStream in) throws IOException {
            int version = DataUtils.read(in);
            if (version != VERSION) {
                throw new IOException();
            }
            
            String path = StringUtils.readString(in);
            
            int size = DataUtils.beb2int(in);
            Map<String, String> properties 
                = new HashMap<String, String>(size);
            
            while (0 < size) {
                String key = StringUtils.readString(in);
                String value = StringUtils.readString(in);
                
                properties.put(key, value);
                --size;
            }
            
            return new Index(path, properties);
        }
    }
}
