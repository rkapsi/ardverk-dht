/*
 * Copyright 2009-2011 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.dht.storage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ardverk.dht.KUID;
import org.ardverk.io.IoUtils;
import org.ardverk.security.MessageDigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleDatabase extends AbstractDatabase {

    private static final Logger LOG 
        = LoggerFactory.getLogger(SimpleDatabase.class);
    
    private final DatabaseConfig config = new DefaultDatabaseConfig();
    
    private final File dir;
    
    public SimpleDatabase(File dir) {
        this.dir = dir;
    }
    
    @Override
    public DatabaseConfig getDatabaseConfig() {
        return config;
    }
    
    private File toFile(Key key) {
        return new File(dir, KeyUtils.getKeyPath(key));
    }
    
    private static boolean mkdirs(File file) {
        File parent = file.getParentFile();
        if (!parent.exists()) {
            return parent.mkdirs();            
        }
        return true;
    }

    @Override
    public Value store(Key key, Value value) {
        File file = toFile(key);
        
        if (!mkdirs(file)) {
            return Status.FAILURE;
        }
        
        InputStream in = null;
        OutputStream out = null;
        
        try {
            in = value.getContent();
            out = new BufferedOutputStream(new FileOutputStream(file));
            
            MessageDigest md5 = MessageDigestUtils.createMD5();
            MessageDigest sha1 = MessageDigestUtils.createSHA1();
            
            out = new DigestOutputStream(out, md5);
            out = new DigestOutputStream(out, sha1);
            
            byte[] buffer = new byte[8 * 1024];
            int len = -1;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            
            out.flush();
            
            writeDigest(file, "md5", md5.digest());
            writeDigest(file, "sha1", sha1.digest());
            
        } catch (IOException err) {
            LOG.error("IOException", err);
        } finally {
            IoUtils.closeAll(in, out);
        }
        
        return Status.SUCCESS;
    }

    private static void writeDigest(File file, 
            String ext, byte[] digest) throws IOException {
        
        File dst = new File(file.getParentFile(), 
                file.getName() + "." + ext);
        
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(
                    new FileOutputStream(dst));
            out.write(digest);
        } finally {
            IoUtils.close(out);
        }
    }
    
    @Override
    public Value get(Key key) {
        File file = toFile(key);
        if (!file.exists()) {
            return null;
        }
        
        return new FileValue(file);
    }

    @Override
    public Iterable<Key> keys() {
        return keys(dir, dir, new ArrayList<Key>());
    }
    
    private static List<Key> keys(final File root, File dir, final List<Key> dst) {
        dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File path) {
                if (path.isDirectory()) {
                    keys(root, path, dst);
                } else {
                    dst.add(createKey(root, path));
                }
                
                return false;
            }
        });
        return dst;
    }

    @Override
    public Iterable<Key> keys(KUID lookupId, KUID lastId) {
        List<Key> dst = new ArrayList<Key>();
        for (Key key : keys()) {
            if (lookupId.isCloserTo(key.getId(), lastId)) {
                dst.add(key);
            }
        }
        return dst;
    }

    @Override
    public int size() {
        return ((Collection<?>)keys()).size();
    }
    
    private static Key createKey(File root, File file) {
        String rootPath = root.getAbsolutePath();
        String filePath = file.getAbsolutePath();
        
        String relative = filePath.substring(rootPath.length());
        if (!relative.startsWith("/")) {
            relative = "/" + relative;
        }
        
        return KeyFactory.parseKey("ardverk://" + relative);
    }
}
