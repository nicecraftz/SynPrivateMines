package me.untouchedodin0.privatemines.commands.logic;

import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import me.untouchedodin0.privatemines.commands.CommandLogic;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

@RequiredArgsConstructor
public class VersionLogic implements CommandLogic {
    private final String version;

    @Override
    public CommandNode<CommandSourceStack> logic() {
        return name("version").executes(context -> {
            context.getSource().getSender().sendRichMessage("<green>Private Mines is running v" + version);
            return SINGLE_SUCCESS;
        }).build();
    }
}
