package com.stephanofer.hera.command.paper;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperCommandModule {

    private final Map<String, CommandSpec> commands = new LinkedHashMap<>();
    private final PaperCommandCompiler compiler = new PaperCommandCompiler();
    private final CommandVisibilityRefresher visibilityRefresher;

    public PaperCommandModule(JavaPlugin plugin) {
        this(plugin.getLifecycleManager());
    }

    public PaperCommandModule(BootstrapContext context) {
        this(context.getLifecycleManager());
    }

    public PaperCommandModule(LifecycleEventManager<?> lifecycleManager) {
        Objects.requireNonNull(lifecycleManager, "lifecycleManager");
        this.visibilityRefresher = new ThrottledCommandVisibilityRefresher(Duration.ofMillis(250));
        registerLifecycleHandler(lifecycleManager);
    }

    public synchronized PaperCommandModule register(CommandSpec commandSpec) {
        Objects.requireNonNull(commandSpec, "commandSpec");
        if (this.commands.containsKey(commandSpec.name())) {
            throw new IllegalArgumentException("Duplicate root command: " + commandSpec.name());
        }
        if (commandSpec.aliases().contains(commandSpec.name())) {
            throw new IllegalArgumentException("Command alias cannot duplicate root name: " + commandSpec.name());
        }
        for (CommandSpec existing : this.commands.values()) {
            if (existing.name().equals(commandSpec.name()) || existing.aliases().contains(commandSpec.name())) {
                throw new IllegalArgumentException("Command label already reserved: " + commandSpec.name());
            }
            if (commandSpec.aliases().contains(existing.name())) {
                throw new IllegalArgumentException("Alias collides with existing command: " + existing.name());
            }
            for (String alias : commandSpec.aliases()) {
                if (existing.aliases().contains(alias)) {
                    throw new IllegalArgumentException("Duplicate alias across registered commands: " + alias);
                }
            }
        }

        this.commands.put(commandSpec.name(), commandSpec);
        return this;
    }

    public synchronized PaperCommandModule registerAll(Collection<CommandSpec> commandSpecs) {
        Objects.requireNonNull(commandSpecs, "commandSpecs");
        for (CommandSpec commandSpec : commandSpecs) {
            register(commandSpec);
        }
        return this;
    }

    public CommandVisibilityRefresher visibilityRefresher() {
        return this.visibilityRefresher;
    }

    private synchronized void registerInto(Commands commandsRegistrar) {
        List<CommandSpec> snapshot = new ArrayList<>(this.commands.values());
        for (CommandSpec spec : snapshot) {
            commandsRegistrar.register(
                this.compiler.compile(spec),
                spec.description(),
                List.copyOf(spec.aliases())
            );
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void registerLifecycleHandler(LifecycleEventManager<?> lifecycleManager) {
        ((LifecycleEventManager) lifecycleManager).registerEventHandler(LifecycleEvents.COMMANDS, event -> this.registerInto(((io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent<Commands>) event).registrar()));
    }
}
