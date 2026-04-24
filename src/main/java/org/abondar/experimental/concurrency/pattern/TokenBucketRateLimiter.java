package org.abondar.experimental.concurrency.pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TokenBucketRateLimiter {


    private final Logger log = LoggerFactory.getLogger(TokenBucketRateLimiter.class);
    private final int bucketCapacity;
    private final Lock lock;
    private final int refillIntervalMs;
    private int tokens;

    private final Thread refillThread;


    public TokenBucketRateLimiter(int bucketCapacity, int refillIntervalMs) {
        if (bucketCapacity <= 0) {
            throw new IllegalArgumentException("Bucket capacity must be positive");
        }

        if (refillIntervalMs <= 0) {
            throw new IllegalArgumentException("Refill interval must be positive");
        }

        this.bucketCapacity = bucketCapacity;
        this.refillIntervalMs = refillIntervalMs;
        this.tokens = bucketCapacity;
        lock = new ReentrantLock();

        refillThread = Thread.ofVirtual()
                .name("TokenBucketRefillThread")
                .start(() -> {
                    while (!Thread.currentThread().isInterrupted()) {
                        refreshBucket();
                    }
                });
    }


    public boolean allowRequest() {
        lock.lock();
        try {
            if (tokens > 0) {
                tokens--;
                return true;
            }
        } finally {
            lock.unlock();
        }

        return false;
    }

    public void shutdown() {
        refillThread.interrupt();
        try {
            refillThread.join();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.info("Refill thread stopped");
        }

    }

    public int availableTokens() {
        lock.lock();

        try {
            return tokens;
        } finally {
            lock.unlock();
        }

    }

    private void refreshBucket() {
        try {
            Thread.sleep(refillIntervalMs);

            lock.lock();
            try {
                if (tokens < bucketCapacity) {
                    tokens++;
                }

            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("Interrupted while waiting for refill thread to stop");
        }

    }

}
