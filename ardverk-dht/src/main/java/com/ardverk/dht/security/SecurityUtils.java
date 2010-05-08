package com.ardverk.dht.security;

import java.security.SecureRandom;
import java.util.Random;

public class SecurityUtils {

    private SecurityUtils() {}
    
    public static Random createSecureRandom() {
        return new SecureRandom();
    }
}
