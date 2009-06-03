package com.ardverk.net;

public class NetworkConstants {

    /**
     * A {@link NetworkMask} that does nothing
     */
    public static final NetworkMask NOP 
        = new NetworkMask(new byte[0]);
    
    /**
     * A {@link NetworkMask} for Class-C networks
     */
    public static final NetworkMask CLASS_C 
        = new NetworkMask(new byte[] { 0x00 });

    private NetworkConstants() {}
}
