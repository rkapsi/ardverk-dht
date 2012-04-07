/*
 * Copyright 2009-2012 Roger Kapsi
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

package org.ardverk.dht.routing;

import java.util.Arrays;
import java.util.Comparator;

import org.ardverk.utils.ReverseComparator;

/**
 * This class provides various utility methods to work 
 * with {@link Longevity} objects.
 */
public class LongevityUtils {
  
  public static final Comparator<Longevity> TIMESTAMP_ASCENDING 
    = new Comparator<Longevity>() {
      @Override
      public int compare(Longevity o1, Longevity o2) {
        // NOTE: We're swapping the arguments!
        return o2.getTimeStamp().compareTo(o1.getTimeStamp());
      }
    };
    
  public static final Comparator<Longevity> TIMESTAMP_DESCENDING 
    = new ReverseComparator<Longevity>(TIMESTAMP_ASCENDING);
  
  
  public static final Comparator<Longevity> CREATION_TIME_ASCENDING 
    = new Comparator<Longevity>() {
      @Override
      public int compare(Longevity o1, Longevity o2) {
        return o1.getCreationTime().compareTo(o2.getCreationTime());
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