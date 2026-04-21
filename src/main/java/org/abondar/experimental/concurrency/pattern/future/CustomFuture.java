package org.abondar.experimental.concurrency.pattern.future;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CustomFuture<T> {

    private final Lock lock;
    private final Condition isCompleted;
    private FutureState state;
    private T value;
    private Throwable ex;

    public CustomFuture() {
        lock = new ReentrantLock();
        isCompleted = lock.newCondition();
        state = FutureState.PENDING;
    }

    public T get() throws InterruptedException {
        lock.lock();
        try {
            while (state == FutureState.PENDING) {
                isCompleted.await();
            }

           return switch (state) {
                case COMPLETED -> value;
                case FAILED -> throw new FutureException(ex);
                case CANCELLED -> throw new FutureException("Future was cancelled");
                case PENDING -> throw new IllegalStateException("Unexpected state");
            };

        } finally {
            lock.unlock();
        }
    }

    public boolean complete(T value) {
        lock.lock();
        try {
            if (state != FutureState.PENDING) {
                return false;
            }

            this.value = value;
            state = FutureState.COMPLETED;
            isCompleted.signalAll();
            return true;

        } finally {
            lock.unlock();
        }

    }

    public boolean isCompleted() {
        lock.lock();

        try {
            return state == FutureState.COMPLETED;
        } finally {
            lock.unlock();
        }
    }


    public boolean fail(Throwable ex) {
        lock.lock();
        try {
            if (state != FutureState.PENDING) {
                return false;
            }
            if (ex == null) {
                ex = new FutureException("Failed with null exception");
            }

            this.ex = ex;
            state = FutureState.FAILED;
            isCompleted.signalAll();
            return true;
        } finally {
            lock.unlock();
        }
    }

    public FutureState getState() {
        lock.lock();

        try {
            return state;
        } finally {
            lock.unlock();
        }
    }

    public boolean cancel() {
        lock.lock();
        try {
            if (state != FutureState.PENDING) {
                return false;
            }

            state = FutureState.CANCELLED;
            isCompleted.signalAll();

            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean isCancelled() {
        lock.lock();

        try {
            return state == FutureState.CANCELLED;
        } finally {
            lock.unlock();
        }
    }

}
