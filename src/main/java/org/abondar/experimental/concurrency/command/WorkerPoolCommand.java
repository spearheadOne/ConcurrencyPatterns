package org.abondar.experimental.concurrency.command;

import org.abondar.experimental.concurrency.command.core.Command;
import org.abondar.experimental.concurrency.command.core.CommandName;
import org.abondar.experimental.concurrency.pattern.WorkerPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerPoolCommand implements Command {

    private final Logger log = LoggerFactory.getLogger(WorkerPoolCommand.class);

    @Override
    public CommandName name() {
        return CommandName.POOL;
    }

    @Override
    public void run() {
       var pool = new WorkerPool(10, 100);
       pool.runPool();

       for (int i = 0; i < 500; i++) {
           pool.submitTask("Task-"+i);
       }

       try {
           Thread.sleep(5000);
       } catch (InterruptedException e) {
           log.error("Error while sleeping", e);
       }

       pool.shutdownPool();
    }
}
