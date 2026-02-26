package org.abondar.experimental.concurrency.command.core;

public interface Command {

    CommandName name();

    void run();
}