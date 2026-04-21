package org.abondar.experimental.concurrency.pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FanInOut {

    private final AtomicInteger counter;
    private final Logger log = LoggerFactory.getLogger(FanInOut.class);
    private final ExecutorService executorService;
    private final List<Callable<Task>> tasks;

    public FanInOut() {
        executorService = Executors.newFixedThreadPool(5);
        counter = new AtomicInteger();
        tasks = new ArrayList<>();
    }

    public void fanOut(List<String> data) {
        var dataTasks = data.stream()
                .map(item -> (Callable<Task>) () -> {
                    log.info("Task {} started", item);
                    TimeUnit.MILLISECONDS.sleep(300);
                    return new Task(item, counter.incrementAndGet());
                })
                .toList();
        tasks.addAll(dataTasks);
    }

    public List<String> fanIn() {
        if (tasks.isEmpty()) {
            log.info("No tasks to execute");
            return List.of();
        }

        try {
            List<Future<Task>> futures = executorService.invokeAll(tasks);

            return futures.stream()
                    .map(f -> {
                        try {
                            return f.get().toString();
                        } catch (InterruptedException  ex) {
                            Thread.currentThread().interrupt();
                            log.error("Interrupted while getting result", ex);
                            throw new RuntimeException(ex);
                        } catch ( ExecutionException ex) {
                            log.error("Error while executing tasks", ex);
                            throw new RuntimeException(ex);
                            }
                    })
                    .toList();

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while getting result", ex);
            throw new RuntimeException(ex);
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }

    private record Task(String task, int number) {
        @Override
        public String toString() {
            return task + " is completed " + number;
        }
    }

}
