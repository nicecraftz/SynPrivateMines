package me.untouchedodin0.privatemines.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.commands.logic.*;
import me.untouchedodin0.privatemines.configuration.ConfigurationInstanceRegistry;
import me.untouchedodin0.privatemines.configuration.ConfigurationValueType;
import me.untouchedodin0.privatemines.configuration.Entry;
import me.untouchedodin0.privatemines.mine.MineService;
import org.bukkit.plugin.Plugin;

import static io.papermc.paper.command.brigadier.Commands.literal;


public class PrivateMinesCommand {
    private final PrivateMines privateMines;
    private final MineService mineService;

    @Entry(key = "reset-cooldown", section = "mine", value = "15", type = ConfigurationValueType.INT) private int resetCooldown;

    public PrivateMinesCommand(PrivateMines privateMines) {
        this.privateMines = privateMines;
        this.mineService = privateMines.getMineService();
        ConfigurationInstanceRegistry.registerInstance(this);
    }

    public void registerCommands() {
        LifecycleEventManager<Plugin> lifecycleManager = privateMines.getLifecycleManager();
        lifecycleManager.registerEventHandler(
                LifecycleEvents.COMMANDS,
                event -> event.registrar().register(buildCommand())
        );
    }

    private LiteralCommandNode<CommandSourceStack> buildCommand() {
        CommandLogic[] logics = {new BanLogic(mineService), new UnbanLogic(mineService), new DeleteMineLogic(mineService), new ExpandMineLogic(
                mineService), new ForceResetMineLogic(mineService), new ResetMineLogic(
                mineService,
                resetCooldown
        ), new GiveLogic(
                mineService,
                privateMines.getMineTemplateRegistry()
        ), new GoMineLogic(mineService), new OpenCloseMineLogic(mineService, "open", true), new OpenCloseMineLogic(
                mineService,
                "close",
                false
        ), new TaxLogic(mineService), new VersionLogic(privateMines.getDescription()
                .getVersion())};

        LiteralArgumentBuilder<CommandSourceStack> privatemine = literal("privatemine");

        for (CommandLogic commandLogic : logics) {
            privatemine.then(commandLogic.logic());
        }

        return privatemine.build();
    }
}