package com.ardverk.dht.routing;

import java.util.Arrays;
import java.util.Comparator;

import org.ardverk.utils.LongComparator;
import org.ardverk.utils.ReverseComparator;

public class LongevityUtils {
    
    public static final Comparator<Longevity> TIMESTAMP_ASCENDING 
        = new Comparator<Longevity>() {
            @Override
            public int compare(Longevity o1, Longevity o2) {
                // NOTE: We're swapping the arguments!
                return LongComparator.compare(
                        o2.getTimeStamp(), 
                        o1.getTimeStamp());
            }
        };
        
    public static final Comparator<Longevity> TIMESTAMP_DESCENDING 
        = new ReverseComparator<Longevity>(TIMESTAMP_ASCENDING);
    
    
    public static final Comparator<Longevity> CREATION_TIME_ASCENDING 
        = new Comparator<Longevity>() {
            @Override
            public int compare(Longevity o1, Longevity o2) {
                return LongComparator.compare(
                        o1.getCreationTime(), 
                        o2.getCreationTime());
            }
        };
        
    public static final Comparator<Longevity> CREATION_TIME_DESCENDING 
        = new ReverseComparator<Longevity>(CREATION_TIME_ASCENDING);
    
    private LongevityUtils() {}
    
    /**
     * Sorts the given {@link Longevity}'s by their time stamp in ascending order.
     * 
     * IMPORTANT NOTE: Ascending order means from most recently seen
     * to least recently seen.
     */
    public static <T extends Longevity> T[] byTimeStamp(T[] values) {
        return byTimeStamp(values, true);
    }
    
    /**
     * Sorts the given {@link Longevity}'s by their time stamp
     * 
     * IMPORTANT NOTE: Ascending order means from most recently seen
     * to least recently seen.
     */
    public static <T extends Longevity> T[] byTimeStamp(T[] values, boolean ascending) {
        Arrays.sort(values, ascending ? TIMESTAMP_ASCENDING : TIMESTAMP_DESCENDING);
        return values;
    }
    
    /**
     * Sorts the given {@link Longevity}'s by their creation time.
     */
    public static <T extends Longevity> T[] byCreationTime(T[] values) {
        return byCreationTime(values, true);
    }
    
    /**
     * Sorts the given {@link Longevity}'s by their creation time.
     */
    public static <T extends Longevity> T[] byCreationTime(T[] values, boolean ascending) {
        Arrays.sort(values, ascending ? CREATION_TIME_ASCENDING : CREATION_TIME_DESCENDING);
        return values;
    }
}
