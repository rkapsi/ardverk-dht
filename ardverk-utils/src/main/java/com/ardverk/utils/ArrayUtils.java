package com.ardverk.utils;

import java.security.SecureRandom;
import java.util.Random;

public class ArrayUtils {

    private static final Random GENERATOR = new SecureRandom();
    
    private ArrayUtils() {}
    
    public static <T> void swap(T[] elements, int i, int j) {
        T element = elements[i];
        elements[i] = elements[j];
        elements[j] = element;
    }
    
    public static <T> T[] shuffle(T[] elements) {
        for (int i = 0; i < elements.length; i++) {
            swap(elements, i, GENERATOR.nextInt(elements.length));
        }
        return elements;
    }
}
