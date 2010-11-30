package com.ardverk.dht.io;

/**
 * A counter that keeps track of in parallel running processes
 */
class ProcessCounter {
    
    private final int maxProcesses;
    
    private int processes = 0;
    
    private int count = 0;
    
    public ProcessCounter(int maxProcesses) {
        if (maxProcesses < 0) {
            throw new IllegalArgumentException(
                    "maxProcesses=" + maxProcesses);
        }
        
        this.maxProcesses = maxProcesses;
    }
    
    public boolean hasNext() {
        return processes < maxProcesses;
    }
    
    public boolean increment() {
        return increment(false);
    }
    
    public boolean increment(boolean force) {
        if (processes < maxProcesses || force) {
            ++processes;
            ++count;
            return true;
        }
        return false;
    }
    
    public void decrement() {
        if (0 < processes) {
            --processes;
        }
    }
    
    public int getProcesses() {
        return processes;
    }
    
    public int getCount() {
        return count;
    }
}
