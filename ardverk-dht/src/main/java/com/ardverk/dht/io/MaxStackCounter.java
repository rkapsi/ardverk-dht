package com.ardverk.dht.io;

/**
 * 
 */
class MaxStackCounter {
    
    private final int max;
    
    private int counter = 0;
    
    public MaxStackCounter(int max) {
        if (max < 0) {
            throw new IllegalArgumentException("max=" + max);
        }
        
        this.max = max;
    }
    
    public boolean hasNext() {
        return counter < max;
    }
    
    public boolean push() {
        if (counter < max) {
            ++counter;
            return true;
        }
        return false;
    }
    
    public void pop() {
        if (0 < counter) {
            --counter;
        }
    }
    
    public int getCount() {
        return counter;
    }
}
