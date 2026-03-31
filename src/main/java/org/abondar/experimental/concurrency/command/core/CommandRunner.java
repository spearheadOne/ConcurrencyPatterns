package org.abondar.experimental.concurrency.command.core;

import org.abondar.experimental.concurrency.command.BlockingQueueCommand;
import org.abondar.experimental.concurrency.command.HelpCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class CommandRunner {
    private final Logger log = LoggerFactory.getLogger(CommandRunner.class);

    private final Map<CommandName, Command> registry = new EnumMap<>(CommandName.class);

    public CommandRunner() {
        register(new HelpCommand());
        register(new BlockingQueueCommand());
    }

    private void register(Command cmd) {
        registry.putIfAbsent(cmd.name(), cmd);
        log.info("Registered command {} -> {}", cmd.name(), cmd.getClass().getSimpleName());
    }


    public void run(String name) {
        CommandName key;
        try {
            key = CommandName.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            log.warn("Unknown command: {}", name);
            Command help = registry.get(CommandName.HELP);
            if (help != null) help.run();
            return;
        }

      registry.get(key).run();
    }
}