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

package com.ardverk.dht.lang;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.ardverk.utils.LongComparator;

/**
 * A {@link TimeStamp} is a relative point in the JVM's time. It's based on
 * {@link System#nanoTime()} and like nano time it's only good for measuring 
 * elapsed time. It's not related to any other notion of system or wall-clock 
 * time.
 */
public class TimeStamp implements Epoch, Age, Comparable<TimeStamp>, Serializable {
    
    private static final long serialVersionUID = -981788126324372167L;

    /**
     * The initial system time (UTC). We use/need it to terminate the 
     * {@link TimeStamp}'s creation time.
     */
    private static final long INIT_SYSTEM_TIME = SystemUtils.currentTimeMillis();
    
    /**
     * The initial JVM time. We use/need it to terminate the {@link TimeStamp}'s
     * creation time.
     */
    private static final long INIT_TIME_STAMP = SystemUtils.nanoTime();
    
    /**
     * Creates and returns a {@link TimeStamp}.
     */
    public static TimeStamp now() {
        return new TimeStamp();
    }
    
    /**
     * The JVM time when this {@link TimeStamp} was created.
     */
    private final long timeStamp = SystemUtils.nanoTime() - INIT_TIME_STAMP;
    
    private TimeStamp() {}
    
    /**
     * Returns the {@link TimeStamp}'s value
     * 
     * @see System#nanoTime()
     */
    public long getTimeStamp() {
        return INIT_TIME_STAMP + timeStamp;
    }
    
    @Override
    public long getCreationTime() {
        return INIT_SYSTEM_TIME + TimeUnit.NANOSECONDS.toMillis(timeStamp);
    }
    
    @Override
    public long getAge(TimeUnit unit) {
        return unit.convert(SystemUtils.nanoTime() - getTimeStamp(), TimeUnit.NANOSECONDS);
    }
    
    @Override
    public long getAgeInMillis() {
        return getAge(TimeUnit.MILLISECONDS);
    }
    
    /**
     * Returns the difference in time between this {@link TimeStamp} and
     * the provided {@link TimeStamp} in the given {@link TimeUnit}.
     */
    public long getTime(TimeStamp ts, TimeUnit unit) {
        return unit.convert(timeStamp - ts.timeStamp, TimeUnit.NANOSECONDS);
    }
    
    @Override
    public int compareTo(TimeStamp other) {
        return LongComparator.compare(timeStamp, other.timeStamp);
    }
    
    @Override
    public int hashCode() {
        return (int)timeStamp;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof TimeStamp)) {
            return false;
        }
        
        TimeStamp other = (TimeStamp)o;
        return timeStamp == other.timeStamp;
    }
    
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Creation Time: ").append(new Date(getCreationTime()))
            .append(", Age=").append(getAgeInMillis()).append("ms");
        return buffer.toString();
    }
}
