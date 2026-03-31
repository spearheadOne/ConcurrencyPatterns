package org.abondar.experimental.concurrency.queue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class CustomBlockingQueueTest {


    @Test
    public void testQueueMultiThread() throws InterruptedException {

        var queue = new CustomBlockingQueue<String>(3);
        var results = Collections.synchronizedList(new ArrayList<String>());


        var producer1 = createProducer(queue, "one");
        var producer2 = createProducer(queue, "two");
        var producer3 = createProducer(queue, "three");
        var producer4 = createProducer(queue, "four");

        var consumer = Thread.ofVirtual().start(()->{
           try {
               for (int i = 0; i < 4; i++) {
                   results.add(queue.take());
               }
           } catch (InterruptedException e) {
               throw new RuntimeException(e);
           }
        });

        producer1.join();
        producer2.join();
        producer3.join();
        producer4.join();
        consumer.join();

        assertEquals(4, results.size());
        assertTrue(results.contains("one"));
        assertTrue(results.contains("two"));
        assertTrue(results.contains("three"));
        assertTrue(results.contains("four"));

        assertTrue(queue.isEmpty());

    }

    private Thread createProducer(CustomBlockingQueue<String> queue, String data) {
        return Thread.ofVirtual().start(()->{
            try {
                queue.add(data);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }


}
