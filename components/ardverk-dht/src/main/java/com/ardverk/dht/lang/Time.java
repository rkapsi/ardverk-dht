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
