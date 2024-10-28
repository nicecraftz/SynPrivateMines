package me.untouchedodin0.privatemines.commands;

import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;

public interface CommandLogic {

    CommandNode<CommandSourceStack> logic();
}
