package org.abondar.experimental.concurrency.pattern;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CustomSemaphore {

    private final int maxPermits;
    private final Lock lock;
    private final Condition isAvailable;
    private int permits;

    public CustomSemaphore(int permits) {
        if (permits <= 0) throw new IllegalArgumentException("Permits must be positive");

        this.permits = permits;
        this.maxPermits = permits;
        this.lock = new ReentrantLock();
        this.isAvailable = lock.newCondition();
    }

    public void acquire() throws InterruptedException {
        lock.lock();
        try {
            while (permits == 0) {
                isAvailable.await();
            }

            permits--;
        } finally {
            lock.unlock();
        }
    }

    public void release() {
        lock.lock();
        try {
            if (permits == maxPermits) {
                throw new IllegalStateException();
            } else {
                permits++;
                isAvailable.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    public int availablePermits(){
        lock.lock();
        try {
            return permits;
        } finally {
            lock.unlock();
        }
    }
}
