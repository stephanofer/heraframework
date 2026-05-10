package com.stephanofer.hera.sandbox;

import com.stephanofer.hera.command.paper.PaperCommandModule;
import com.stephanofer.hera.sandbox.command.HeraSandboxCommands;
import org.bukkit.plugin.java.JavaPlugin;

public final class HeraSandboxPlugin extends JavaPlugin {

    private PaperCommandModule commandModule;

    @Override
    public void onEnable() {
        this.commandModule = new PaperCommandModule(this);
        // this.commandModule.register(HeraSandboxCommands.createDemo(this.commandModule.visibilityRefresher()));
        this.commandModule.register(HeraSandboxCommands.createDemo(this.commandModule.visibilityRefresher()));

    }
}
