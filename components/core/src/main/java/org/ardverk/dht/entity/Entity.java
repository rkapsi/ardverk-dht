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

package org.ardverk.dht.entity;

import java.util.concurrent.TimeUnit;

import org.ardverk.lang.Precoditions;

public abstract class Entity {

    protected final long time;
    
    protected final TimeUnit unit;
    
    public Entity(long time, TimeUnit unit) {
        this.time = time;
        this.unit = Precoditions.notNull(unit, "unit");
    }
    
    public long getTime(TimeUnit unit) {
        return unit.convert(time, this.unit);
    }
    
    public long getTimeInMillis() {
        return getTime(TimeUnit.MILLISECONDS);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + " (" + time + ", " + unit + ")";
    }
}