package com.ardverk.dht.entity;

class EntityUtils {

    private EntityUtils() {}

    static long getTimeInMillis(Entity... entities) {
        long time = 0L;
        for (Entity entity : entities) {
            time += entity.getTimeInMillis();
        }
        return time;
    }
}
