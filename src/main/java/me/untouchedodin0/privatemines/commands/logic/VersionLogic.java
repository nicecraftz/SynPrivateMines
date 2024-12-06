package me.untouchedodin0.privatemines.commands.logic;

import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.untouchedodin0.privatemines.commands.CommandLogic;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class VersionLogic extends CommandLogic {
    private final String version = privateMines.getDescription().getVersion();

    @Override
    public String name() {
        return "version";
    }

    @Override
    public CommandNode<CommandSourceStack> logic() {
        return literal(name()).executes(context -> {
            context.getSource().getSender().sendRichMessage("<green>Private Mines is running v" + version);
            return SINGLE_SUCCESS;
        }).build();
    }
}
