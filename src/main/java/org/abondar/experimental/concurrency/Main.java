package org.abondar.experimental.concurrency;

import org.abondar.experimental.concurrency.command.core.CommandName;
import org.abondar.experimental.concurrency.command.core.CommandRunner;

import java.util.Locale;


public class Main {
    static void main(String[] args) {
        var commandRunner = new CommandRunner();

        commandRunner.run(args[0]);
    }
}
