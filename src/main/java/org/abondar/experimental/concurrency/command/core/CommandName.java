package org.abondar.experimental.concurrency.command.core;

public enum CommandName {
    HELP("List available commands");

    private final String description;

    CommandName(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
