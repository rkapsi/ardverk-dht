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

package com.ardverk.dht.lang;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.ardverk.utils.LongComparator;

public class Time implements ElapsedTime, Comparable<Time>, Serializable {

    private static final long serialVersionUID = 4534002981967946023L;
    
    private final long elapsedTime;
    
    public Time(long time, TimeUnit unit) {
        this.elapsedTime = unit.toNanos(time);
    }

    @Override
    public long getTime(TimeUnit unit) {
        return unit.convert(elapsedTime, TimeUnit.NANOSECONDS);
    }

    @Override
    public long getTimeInMillis() {
        return getTime(TimeUnit.MILLISECONDS);
    }
    
    @Override
    public int compareTo(Time other) {
        return LongComparator.compare(elapsedTime, other.elapsedTime);
    }
    
    @Override
    public int hashCode() {
        return (int)elapsedTime;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Time)) {
            return false;
        }
        
        Time other = (Time)o;
        return elapsedTime == other.elapsedTime;
    }
    
    @Override
    public String toString() {
        return getTimeInMillis() + "ms";
    }
}
