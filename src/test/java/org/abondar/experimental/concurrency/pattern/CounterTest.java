package org.abondar.experimental.concurrency.pattern;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CounterTest {

    @Test
    public void testIncrement() throws InterruptedException {
        Counter counter = new Counter();

        var thread100 = Thread.ofVirtual()
                .start(() -> {
                    for (int i = 0; i < 100; i++) {
                        counter.increment();
                    }
                });

        var thread1 = Thread.ofVirtual()
                .start(counter::increment);

        thread100.join();
        thread1.join();

        assertEquals(101, counter.getCount());
    }

    @Test
    public void testDecrement() throws InterruptedException {
        Counter counter = new Counter();

        var thread100 = Thread.ofVirtual()
                .start(() -> {
                    for (int i = 0; i < 100; i++) {
                        counter.increment();
                    }
                });

        var threadDec = Thread.ofVirtual()
                .start(counter::decrement);

        thread100.join();
        threadDec.join();

        assertEquals(99, counter.getCount());
    }

    @Test
    public void testReset() throws InterruptedException {
        Counter counter = new Counter();

        Thread.ofVirtual()
                .start(() -> {
                    for (int i = 0; i < 100; i++) {
                        counter.increment();
                    }
                })
                .join();

        Thread.ofVirtual()
                .start(counter::reset)
                .join();

        assertEquals(0, counter.getCount());
    }


    @Test
    public void testDelta() throws InterruptedException {
        Counter counter = new Counter();

        var thread100 = Thread.ofVirtual()
                .start(() -> {
                    for (int i = 0; i < 100; i++) {
                        counter.increment();
                    }
                });

        var threadAdd = Thread.ofVirtual()
                .start(() -> counter.add(50));

        thread100.join();
        threadAdd.join();

        assertEquals(150, counter.getCount());
    }
}
