package org.ardverk.dht.message;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileContent extends AbstractContent {
    
    private final File file;
    
    public FileContent(File file) {
        this.file = file;
    }
    
    @Override
    public long getContentLength() {
        return file.length();
    }

    @Override
    public InputStream getContent() throws IOException {
        return new FileInputStream(file);
    }
}
