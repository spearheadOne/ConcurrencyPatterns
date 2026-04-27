package org.abondar.experimental.concurrency.command;

import org.abondar.experimental.concurrency.command.core.Command;
import org.abondar.experimental.concurrency.command.core.CommandName;
import org.abondar.experimental.concurrency.pattern.RwLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class ReadWriteLockCommand implements Command {

    private final Logger log = LoggerFactory.getLogger(ReadWriteLockCommand.class);

    @Override
    public CommandName name() {
        return CommandName.RWLOCK;
    }

    @Override
    public void run() {
        var rwLock = new RwLock();

        var readLock = rwLock.readLock();
        var writeLock = rwLock.writeLock();

        Runnable reader = () -> {
            readLock.lock();

            try {
                log.info("{} acquired read lock", Thread.currentThread().getName());
                TimeUnit.MILLISECONDS.sleep(500);
                log.info("{} finished reading", Thread.currentThread().getName());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("{} interrupted", Thread.currentThread().getName(), e);

            } finally {
                readLock.unlock();
                log.info("{} released read lock", Thread.currentThread().getName());
            }

        };

        Runnable writer = () -> {
            writeLock.lock();

            try {
                log.info("{} acquired write lock", Thread.currentThread().getName());
                TimeUnit.MILLISECONDS.sleep(700);
                log.info("{} finished writing", Thread.currentThread().getName());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("{} interrupted", Thread.currentThread().getName(), e);

            } finally {
                writeLock.unlock();
                log.info("{} released write lock", Thread.currentThread().getName());
            }

        };

        var reader1 = Thread.ofVirtual().name("reader-1").start(reader);
        var reader2 = Thread.ofVirtual().name("reader-2").start(reader);

        sleep(100);

        var writer1 = Thread.ofVirtual().name("writer-1").start(writer);
        sleep(100);

        var reader3 = Thread.ofVirtual().name("reader-3").start(reader);

        join(reader1);
        join(reader2);
        join(writer1);
        join(reader3);
    }

    private void sleep(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(e);
        }

    }

    private void join(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(e);
        }

    }
}
