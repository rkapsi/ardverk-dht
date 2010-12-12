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

package com.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import org.ardverk.lang.Arguments;

public abstract class AbstractEntity implements Entity {

    protected final long time;
    
    protected final TimeUnit unit;
    
    public AbstractEntity(long time, TimeUnit unit) {
        this.time = Arguments.notNegative(time, "time");
        this.unit = Arguments.notNull(unit, "unit");
    }
    
    @Override
    public long getTime(TimeUnit unit) {
        return unit.convert(time, this.unit);
    }
    
    @Override
    public long getTimeInMillis() {
        return getTime(TimeUnit.MILLISECONDS);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " (" + time + ", " + unit + ")";
    }
}