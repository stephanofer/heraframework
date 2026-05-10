package com.stephanofer.hera.command.paper;

import java.util.List;

public interface CommandNodeSpec {

    String description();

    String usage();

    String permission();

    boolean restricted();

    SenderPolicy senderPolicy();

    ExecutorPolicy executorPolicy();

    CommandRequirement requirement();

    CommandHandler handler();

    List<CommandNodeSpec> children();
}
