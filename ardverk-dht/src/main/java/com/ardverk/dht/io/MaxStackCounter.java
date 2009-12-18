package com.ardverk.dht.io;

/**
 * TODO: Find a better name for this
 */
class MaxStackCounter {
    
    private final int maxStack;
    
    private int stack = 0;
    
    private int count = 0;
    
    public MaxStackCounter(int maxStack) {
        if (maxStack < 0) {
            throw new IllegalArgumentException("maxStack=" + maxStack);
        }
        
        this.maxStack = maxStack;
    }
    
    public boolean hasNext() {
        return stack < maxStack;
    }
    
    public boolean push() {
        if (stack < maxStack) {
            ++stack;
            ++count;
            return true;
        }
        return false;
    }
    
    public void pop() {
        if (0 < stack) {
            --stack;
        }
    }
    
    public int getStack() {
        return stack;
    }
    
    public int getCount() {
        return count;
    }
}
