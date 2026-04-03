package org.abondar.experimental.concurrency.pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ProducerConsumer {

    private final int numProducers;
    private final int numConsumers;
    private final ConcurrentLinkedQueue<String> tasks;
    private final BlockingQueue<String> buffer;
    private final List<Thread> producers = new ArrayList<>();
    private final List<Thread> consumers = new ArrayList<>();

    private static final String STOP = "STOP";

    private final Logger log = LoggerFactory.getLogger(ProducerConsumer.class);

    public ProducerConsumer(int numProducers, int numConsumers, int numTasks) {
        this.numProducers = numProducers;
        this.numConsumers = numConsumers;

        buffer = new ArrayBlockingQueue<>(100);

        tasks = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < numTasks; i++) {
            tasks.add("Task-" + i);
        }

    }


    public void runProducers() throws InterruptedException {
        for (int i = 0; i < numProducers; i++) {
            var producer = Thread.ofVirtual()
                    .name("Producer-" + i)
                    .start(
                            () -> {
                                while (true) {
                                    var task = tasks.poll();
                                    if (task == null) {
                                        log.info("No tasks left");
                                        break;
                                    } else {
                                        log.info("Produced: {}", task);
                                        try {
                                            buffer.put(task);
                                        } catch (InterruptedException e) {
                                            log.error("Error while putting", e);
                                            break;
                                        }
                                    }
                                }
                            });

            producers.add(producer);
        }

        for (Thread p : producers) {
            try {
                p.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        for (int i = 0; i < numConsumers; i++) {
            buffer.put(STOP);
        }
    }

    public void runConsumers() throws InterruptedException {
        for (int i = 0; i < numConsumers; i++) {
            var consumer = Thread.ofVirtual()
                    .name("Consumer-" + i)
                    .start(() -> {

                        while (true) {
                            try {
                                var task = buffer.take();
                                if (STOP.equals(task)) {
                                    log.info("Stop signal received");
                                    break;
                                } else {
                                    log.info("Consumed: {}", task);
                                }
                            } catch (InterruptedException e) {
                                log.error("Error while taking", e);
                                break;
                            }
                        }
                    });
            consumers.add(consumer);
        }

        for (Thread c : consumers) {
            try {
                c.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}

