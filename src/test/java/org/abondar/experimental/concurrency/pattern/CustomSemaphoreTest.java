package org.abondar.experimental.concurrency.pattern;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CustomSemaphoreTest {

    @Test
    public void testNegativePermits(){
        assertThrows(IllegalArgumentException.class,()-> new CustomSemaphore(-1));
    }

    @Test
    public void testZeroPermits(){
        assertThrows(IllegalArgumentException.class,()-> new CustomSemaphore(0));
    }

    @Test
    public void testAcquireAndRelease() throws Exception{
        var semaphore = new CustomSemaphore(1);

        semaphore.acquire();
        var permits = semaphore.availablePermits();
        assertEquals(0, permits);

        semaphore.release();
        permits = semaphore.availablePermits();
        assertEquals(1,permits);
    }

    @Test
    public void testAcquireWaitsUntilRelease() throws Exception{
        var semaphore = new CustomSemaphore(1);

        semaphore.acquire();
        var permits = semaphore.availablePermits();
        assertEquals(0, permits);

        var waiter = Thread.ofVirtual().start(()->{
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        Thread.sleep(100);

        //release main thread
        semaphore.release();

        //waiter acquires semaphore
        waiter.join();
        assertEquals(0, semaphore.availablePermits());

        //waiter releases semaphore
        semaphore.release();
        assertEquals(1, semaphore.availablePermits());
    }

    @Test
    public void testRejectRelease(){
        var semaphore = new CustomSemaphore(1);
        assertThrows(IllegalStateException.class, semaphore::release);
    }

}
