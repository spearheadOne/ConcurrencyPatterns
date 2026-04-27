package org.abondar.experimental.concurrency.pattern;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

public class RwLock implements ReadWriteLock {

    private final Lock monitor;
    private final Condition readAvailable;
    private final Condition writeAvailable;
    private final Lock readLock;
    private final Lock writeLock;
    private int activeReaders;
    private int waitingWriters;
    private boolean isActiveWriter;

    public RwLock() {
        this.monitor = new ReentrantLock();
        this.activeReaders = 0;
        this.waitingWriters = 0;
        this.isActiveWriter = false;
        this.writeAvailable = monitor.newCondition();
        this.readAvailable = monitor.newCondition();
        this.readLock = new ReadLock();
        this.writeLock = new WriteLock();

    }

    @Override
    public Lock readLock() {
        return readLock;
    }

    @Override
    public Lock writeLock() {
        return writeLock;
    }


    private final class ReadLock implements Lock {

        @Override
        public void lock() {
            monitor.lock();
            try {
                while (isActiveWriter || waitingWriters > 0) {
                    readAvailable.awaitUninterruptibly();
                }
                activeReaders++;
            } finally {
                monitor.unlock();
            }
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean tryLock() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void unlock() {
            monitor.lock();
            try {
                activeReaders--;
                if (activeReaders == 0) {
                    writeAvailable.signal();
                }
            } finally {
                monitor.unlock();
            }
        }

        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }
    }


    private final class WriteLock implements Lock {

        @Override
        public void lock() {
            monitor.lock();
            try {
                waitingWriters++;
                while (activeReaders > 0 || isActiveWriter) {
                    writeAvailable.awaitUninterruptibly();
                }
                waitingWriters--;
                isActiveWriter = true;
            } finally {
                monitor.unlock();
            }
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean tryLock() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void unlock() {
            monitor.lock();
            try {
                isActiveWriter = false;
                if (waitingWriters > 0) {
                    writeAvailable.signal();
                } else {
                    readAvailable.signalAll();
                }
            } finally {
                monitor.unlock();
            }
        }

        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }
    }
}

