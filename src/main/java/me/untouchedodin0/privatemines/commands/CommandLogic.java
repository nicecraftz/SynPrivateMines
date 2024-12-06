package me.untouchedodin0.privatemines.commands;

import com.mojang.brigadier.tree.CommandNode;
import com.sk89q.worldedit.entity.Player;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.mine.MineService;

import java.util.UUID;
import java.util.function.Predicate;

@RequiredArgsConstructor
public abstract class CommandLogic {
    protected final PrivateMines privateMines = PrivateMines.inst();
    protected final MineService mineService = privateMines.getMineService();

    public abstract String name();

    public abstract CommandNode<CommandSourceStack> logic();

    protected Predicate<CommandSourceStack> hasPermission(String literal) {
        return commandSourceStack -> {
            return commandSourceStack.getSender().hasPermission("privatemines." + literal);
        };
    }

    protected Predicate<CommandSourceStack> isPlayer() {
        return commandSourceStack -> {
            return commandSourceStack.getSender() instanceof Player;
        };
    }

    protected Predicate<CommandSourceStack> isPlayerAndHasPermission(String literal) {
        return commandSourceStack -> isPlayer().and(hasPermission(literal)).test(commandSourceStack);
    }

    protected Predicate<CommandSourceStack> isPlayerHasPermissionAndHasMine(String literal) {
        return commandSourceStack -> isPlayerAndHasPermission(literal).and(hasMine(((Player) commandSourceStack.getSender()).getUniqueId())).test(commandSourceStack);
    }

    protected Predicate<CommandSourceStack> hasMine(UUID uuid) {
        return commandSourceStack -> mineService.has(uuid);
    }
}
