package org.ardverk.dht.storage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.protocol.HTTP;

public class FileValueEntity extends AbstractValueEntity {

    private final File file;
    
    public FileValueEntity(File file) {
        super(HTTP.OCTET_STREAM_TYPE, file.length());
        this.file = file;
    }

    @Override
    public InputStream getContent() throws IOException {
        return new BufferedInputStream(new FileInputStream(file));
    }
}
