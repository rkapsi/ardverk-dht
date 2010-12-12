/*
 * Copyright 2010 Roger Kapsi
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

package com.ardverk.dht.storage;

import java.util.concurrent.TimeUnit;

/**
 * An abstract implementation of {@link ValueTuple}.
 */
abstract class AbstractValueTuple implements ValueTuple {

    private final long creationTime = System.currentTimeMillis();
    
    @Override
    public long getCreationTime() {
        return creationTime;
    }
    
    @Override
    public long getAge(TimeUnit unit) {
        long age = System.currentTimeMillis() - creationTime;
        return unit.convert(age, TimeUnit.MILLISECONDS);
    }

    @Override
    public long getAgeInMillis() {
        return getAge(TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
}