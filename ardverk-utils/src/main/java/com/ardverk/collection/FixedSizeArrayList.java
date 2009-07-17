package com.ardverk.collection;

import java.util.ArrayList;
import java.util.Collection;

public class FixedSizeArrayList<E> extends ArrayList<E> implements FixedSize {

    private static final long serialVersionUID = -8835179064931233224L;
    
    private final int maxSize;

    public FixedSizeArrayList(int maxSize) {
        super();
        
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize=" + maxSize);
        }
        
        this.maxSize = maxSize;
    }

    public FixedSizeArrayList(Collection<? extends E> c, int maxSize) {
        super(c);
        
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize=" + maxSize);
        }
        
        this.maxSize = maxSize;
    }

    public FixedSizeArrayList(int initialCapacity, int maxSize) {
        super(initialCapacity);
        
        if (maxSize < 0) {
            throw new IllegalArgumentException("maxSize=" + maxSize);
        }
        
        this.maxSize = maxSize;
    }
    
    @Override
    public int getMaxSize() {
        return maxSize;
    }
    
    @Override
    public boolean isFull() {
        return size() >= getMaxSize();
    }

    @Override
    public boolean add(E e) {
        adjustSize(size(), e);
        return super.add(e);
    }

    @Override
    public void add(int index, E element) {
        if (index < 0 || size() < index) {
            throw new IllegalArgumentException("index=" + index);
        }
        
        adjustSize(index, element);
        super.add(index, element);
    }
    
    private void adjustSize(int index, E e) {
        if (isFull()) {
            E old = remove(eject(index, e));
            removed(old);
        }
    }
    
    protected int eject(int index, E e) {
        return 0;
    }
    
    protected void removed(E element) {
        // OVERRIDE
    }
}
