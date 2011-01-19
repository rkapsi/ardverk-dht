/*
 * Copyright 2009-2010 Roger Kapsi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ardverk.dht.storage;

import java.util.concurrent.TimeUnit;

import org.ardverk.lang.TimeStamp;

/**
 * An abstract implementation of {@link ValueTuple}.
 */
abstract class AbstractValueTuple implements ValueTuple {

    private final TimeStamp creationTime = TimeStamp.now();
    
    @Override
    public long getCreationTime() {
        return creationTime.getCreationTime();
    }
    
    @Override
    public long getAge(TimeUnit unit) {
        return creationTime.getAge(unit);
    }

    @Override
    public long getAgeInMillis() {
        return creationTime.getAgeInMillis();
    }

    @Override
    public long getContentLength() {
        return getValue().getContentLength();
    }
    
    @Override
    public boolean isEmpty() {
        return getContentLength() == 0;
    }
}