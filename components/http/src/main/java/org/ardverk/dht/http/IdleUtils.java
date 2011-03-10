package org.ardverk.dht.http;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

class IdleUtils {

    private static final Timer TIMER = new HashedWheelTimer(new ThreadFactory() {
        
        private final AtomicInteger counter = new AtomicInteger();
        
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "IdleThread-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    });
    
    public static final IdleStateHandler DEFAULT 
        = createIdleStateHandler(30L, TimeUnit.SECONDS);
    
    public static IdleStateHandler createIdleStateHandler(
            long timeout, TimeUnit unit) {
        return createIdleStateHandler(0L, 0L, timeout, unit);
    }
    
    public static IdleStateHandler createIdleStateHandler(
            long r, long w, TimeUnit unit) {
        return createIdleStateHandler(r, w, 0L, unit);
    }
    
    public static IdleStateHandler createIdleStateHandler(
            long r, long w, long rw, TimeUnit unit) {
        return new IdleStateHandler(TIMER, r, w, rw, unit);
    }
}
