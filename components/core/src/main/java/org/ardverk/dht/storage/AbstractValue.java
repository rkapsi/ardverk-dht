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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ardverk.dht.concurrent.DHTFuture;
import org.ardverk.io.IoUtils;

public abstract class AbstractValue implements Value {
    
    @Override
    public DHTFuture<Void> getContentFuture() {
        return DEFAULT_FUTURE;
    }
    
    @Override
    public void writeTo(OutputStream out) throws IOException {
        InputStream in = getContent();
        try {
            byte[] buffer = new byte[8 * 1024];
            int len = -1;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } finally {
            IoUtils.close(in);
        }
    }
}