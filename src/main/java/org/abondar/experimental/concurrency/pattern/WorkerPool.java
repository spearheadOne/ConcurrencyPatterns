package org.abondar.experimental.concurrency.pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WorkerPool {

    private static final Runnable STOP = () -> {};

    private final int numWorkers;
    private final BlockingQueue<Runnable> tasks;
    private final List<Thread> workers;
    private boolean isShutdown;
    private final Logger log = LoggerFactory.getLogger(WorkerPool.class);
    private final Lock lock;


    public WorkerPool(int numWorkers, int numTasks) {
        this.numWorkers = numWorkers;
        workers = new ArrayList<>(numWorkers);
        tasks = new ArrayBlockingQueue<>(numTasks);
        lock = new ReentrantLock();
        isShutdown = false;
    }

    public void runPool() {
        for (int i = 0; i < numWorkers; i++) {
            var worker = Thread.ofVirtual()
                    .name("Worker-" + i)
                    .start(() -> {
                        while (true) {
                            try {
                                var task = tasks.take();
                                if (task == STOP) {
                                    log.info("Shutting down");
                                    break;
                                }
                                try {
                                    task.run();
                                } catch (Throwable t) {
                                    log.error("Error while executing task", t);
                                }
                            } catch (InterruptedException e) {
                                log.error("Error while taking", e);
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    });
            workers.add(worker);
        }
    }

    public void shutdownPool() {
        sendTerminationSignal();
        awaitTermination();
    }

    private void sendTerminationSignal() {
        lock.lock();
        try {
            if (isShutdown) {
                return;
            }

            isShutdown = true;
            for (int i = 0; i < numWorkers; i++) {
                tasks.put(STOP);
            }
        } catch (InterruptedException e) {
            log.error("Error while sending stop", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    private void awaitTermination() {
        for (Thread t : workers) {
            try {
                t.join();
            } catch (InterruptedException e) {
                log.error("Error while joining", e);
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }


    public void submitTask(String task) {
        lock.lock();
        try {
            if (isShutdown) {
                log.info("Pool is shutdown. Cannot submit task {}", task);
                return;
            }
            tasks.put((Runnable) () -> {
                log.info("Worker {} got task {}", Thread.currentThread().getName(), task);

                try {
                    Thread.sleep(1000); //simulate work
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                log.info("Task {} processed", task);
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }


}
