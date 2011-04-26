package org.ardverk.dht.message;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.io.IoUtils;

public abstract class AbstractContent implements Content {
    
    private volatile boolean consumed = false;
    
    private volatile byte[] content = null;
   
    @Override
    public DHTFuture<Void> getContentFuture() {
        return FUTURE;
    }
    
    @Override
    public byte[] getContentAsBytes() throws IOException {
        if (!consumed) {
            consumed = true;
            
            long contentLength = getContentLength();
            if (Integer.MAX_VALUE < contentLength) {
                throw new IOException("contentLength=" + contentLength);
            }
            
            ByteArrayOutputStream baos 
                = new ByteArrayOutputStream((int)contentLength);
            
            InputStream in = getContent();
            try {
                byte[] buffer = new byte[4 * 1024];
                int len = -1;
                while ((len = in.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
            } finally {
                IoUtils.closeAll(in, baos);
            }
            
            content = baos.toByteArray();
        }
        
        if (content == null) {
            throw new EOFException();
        }
        
        return content;
    }
}