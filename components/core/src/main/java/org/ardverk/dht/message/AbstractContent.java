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
        return DEFAULT_FUTURE;
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