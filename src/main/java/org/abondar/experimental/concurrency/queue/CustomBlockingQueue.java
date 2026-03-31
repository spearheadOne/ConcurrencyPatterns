package org.abondar.experimental.concurrency.queue;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CustomBlockingQueue<T> {

    private final Lock lock;
    private final int capacity;
    private final Condition notEmpty;
    private final Condition notFull;
    private QueueNode<T> head;
    private QueueNode<T> tail;
    private int size;

    public CustomBlockingQueue(int capacity) {
        lock = new ReentrantLock();
        this.capacity = capacity;

        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }

        this.size = 0;

        notEmpty = lock.newCondition();
        notFull = lock.newCondition();
    }

    public void add(T item) throws InterruptedException {
        lock.lock();
        try {

            while (size == capacity) {
                notFull.await();
            }

            var newTail = new QueueNode<>(item);
            if (tail != null) {
                tail.next = newTail;
            }

            tail = newTail;
            if (head == null) {
                head = tail;
            }
            size++;

            notEmpty.signal();

        } finally {
            lock.unlock();
        }
    }

    public T take() throws InterruptedException {
        lock.lock();

        try {

            while (size == 0) {
                notEmpty.await();
            }

            T item = head.data;
            head = head.next;
            size--;

            notFull.signal();

            if (head == null) {
                tail = null;
            }

            return item;
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        lock.lock();
        try {
            return size == 0;
        } finally {
            lock.unlock();
        }
    }

    public Integer size() {
        lock.lock();
        try {
            return size;
        } finally {
            lock.unlock();
        }
    }

    private static class QueueNode<T> {
        private final T data;
        private QueueNode<T> next;

        public QueueNode(T data) {
            this.data = data;
        }
    }
}
