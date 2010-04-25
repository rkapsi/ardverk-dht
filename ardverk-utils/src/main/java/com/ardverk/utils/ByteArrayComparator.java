/*
 * Copyright 2010 Roger Kapsi
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.ardverk.utils;

import java.io.Serializable;
import java.util.Comparator;

public class ByteArrayComparator implements Comparator<byte[]>, Serializable {

    private static final long serialVersionUID = 6625000716332463624L;

    public static final ByteArrayComparator COMPARATOR = new ByteArrayComparator();

    @Override
    public int compare(byte[] o1, byte[] o2) {
        if (o1.length != o2.length) {
            return o1.length - o2.length;
        }
        
        for (int i = 0; i < o1.length; i++) {
            int diff = (o1[i] & 0xFF) - (o2[i] & 0xFF);
            if (diff != 0) {
                return diff;
            }
        }

        return 0;
    }
}
