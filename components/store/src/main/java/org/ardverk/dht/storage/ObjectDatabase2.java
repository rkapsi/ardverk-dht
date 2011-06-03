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
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.protocol.HTTP;
import org.ardverk.coding.CodingUtils;
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
        
        this.index = FileUtils.mkdirs(directory, "index");
        this.context = FileUtils.mkdirs(directory, "context");
        this.content = FileUtils.mkdirs(directory, "content");
    }
    
    private File mkIndexFile(Key key, boolean mkdirs) {
        return mkfile(index, key.getPath(), mkdirs);
    }
    
    private File mkContextFile(Key key, String vtag, boolean mkdirs) {
        return mkfile(context, key.getPath() + "/" + vtag, mkdirs);
    }
    
    private File mkContentsFile(Key key, String vtag, boolean mkdirs) {
        return mkfile(content, key.getPath() + "/" + vtag, mkdirs);
    }
    
    private static File mkfile(File dir, String path, boolean mkdirs) {
        File file = new File(dir, path);
        if (mkdirs) {
            file.getParentFile().mkdirs();
        }
        return file;
    }
    
    private File mkTmpFile(Key key) throws IOException {
        String hash = hashPath(key);
        
        File dir = new File(tmp, hash.substring(0, 2));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        String bucketId = key.getId().toHexString();
        return File.createTempFile(bucketId, null, dir);
    }
    
    private static String hashPath(Key key) {
        byte[] path = StringUtils.getBytes(key.getPath());
        MessageDigest md = MessageDigestUtils.createSHA1();
        byte[] digest = md.digest(path);
        return CodingUtils.encodeBase16(digest);
    }
    
    private Index index(Key key) throws IOException {
        File file = mkIndexFile(key, false);
        if (file.exists()) {
            return Index.valueOf(file);
        }
        
        return new Index(key.getPath());
    }
    
    @Override
    protected Response handlePut(Contact src, Key key, Request request,
            InputStream in) throws IOException {
        
        Index index = index(key);
        
        Context context = request.getContext();
        
        MessageDigest md5 = MessageDigestUtils.createMD5();
        MessageDigest sha1 = MessageDigestUtils.createSHA1();
        
        DigestInputStream dis = new DigestInputStream(
                in, MessageDigestUtils.wrap(md5, sha1));
        
        File tmpFile = writeContent(key, context, dis);
        File file = null;
        
        boolean success = false;
        try {
            if (!digest(context, Constants.CONTENT_MD5, md5)) {
                return Response.INTERNAL_SERVER_ERROR;
            }
            
            if (!digest(context, Constants.CONTENT_SHA1, sha1)) {
                return Response.INTERNAL_SERVER_ERROR;
            }
            
            upsertVclock(context);
            
            file = mkContentsFile(key, true);
            FileUtils.renameTo(tmpFile, file);
            
            writeContext(key, context);
            
            success = true;
            return ResponseFactory.createOk();
            
        } finally {
            if (!success) {
                FileUtils.deleteAll(tmpFile, file);
            }
        }
    }
    
    private File writeContent(Key key, Context context, InputStream in) throws IOException {
        File file = mkTmpFile(key);
        
        boolean success = false;
        try {
            OutputStream out = new BufferedOutputStream(
                    new FileOutputStream(file));
            try {
                long length = context.getLongValue(HTTP.CONTENT_LEN, 0L);
                StreamUtils.copy(in, out, length);
                success = true;
            } finally {
                IoUtils.close(out);
            }
        } finally {
            if (!success) {
                FileUtils.delete(file);
            }
        }
        
        return file;
    }
    
    private File writeContext(Key key, Context context) throws IOException {
        File file = mkContextFile(key, true);
        return writeContext(file, context);
    }
    
    private static File writeContext(File file, Context context) throws IOException {
        boolean success = false;
        try {
            OutputStream out = new BufferedOutputStream(
                    new FileOutputStream(file));
            try {
                context.writeTo(out);
                success = true;
            } finally {
                IoUtils.close(out);
            }
        } finally {
            if (!success) {
                FileUtils.delete(file);
            }
        }
        
        return file;
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
        
        File file = mkContextFile(key, false);
        if (!file.exists()) {
            return ResponseFactory.createNotFound();
        }
        
        Context context = Context.valueOf(file);
        
        if (context.containsHeader(Constants.TOMBSTONE)) {
            return ResponseFactory.createNotFound();
        }
        
        context.addHeader(Constants.tombstone());
        
        // TODO: Vclock!!!
        writeContext(file, context);
        
        File contents = mkContentsFile(key, false);
        FileUtils.delete(contents);
        
        return ResponseFactory.createOk();
    }
    
    @Override
    protected Response handleHead(Contact src, Key key, Request request,
            InputStream in) throws IOException {
        
        File file = mkContextFile(key, false);
        if (!file.exists()) {
            return ResponseFactory.createNotFound();
        }
        
        Context context = Context.valueOf(file);
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
            
        File file = mkContextFile(key, false);
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        
        Context context = Context.valueOf(file);
        if (context.containsHeader(Constants.TOMBSTONE)) {
            return null;
        }
        
        File contents = mkContentsFile(key, false);
        if (!contents.exists()) {
            return null;
        }
        
        return new Response(StatusLine.OK, context, 
                new FileValueEntity(contents));
    }
    
    protected Response list(Contact src, Key key, Map<String, String> query) {
        return null;
    }
    
    protected Response vtag(Contact src, Key key, Map<String, String> query) {
        return null;
    }
    
    private static class Index implements Writable {

        private static final int VERSION = 0;
        
        private final String path;
        
        public Index(String path) {
            this.path = path;
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
            
            return new Index(path);
        }
    }
}
