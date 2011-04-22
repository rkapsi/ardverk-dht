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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.ardverk.io.IoUtils;

public class InputStreamValue extends ByteArrayValue {
    
    public InputStreamValue(InputStream in) {
        super(consume(in));
    }
    
    private static byte[] consume(InputStream in) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4 * 1024];
            int len = -1;
            while ((len = in.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            baos.close();
            return baos.toByteArray();
        } catch (IOException err) {
            throw new IllegalStateException(err);
        } finally {
            IoUtils.close(in);
        }
    }
}
