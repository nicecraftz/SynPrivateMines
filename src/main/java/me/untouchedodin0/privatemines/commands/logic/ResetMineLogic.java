package me.untouchedodin0.privatemines.commands.logic;

import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.untouchedodin0.privatemines.commands.CommandLogic;
import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineService;
import me.untouchedodin0.privatemines.utils.Cooldowns;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class ResetMineLogic extends CommandLogic {
    private final int resetCooldown;

    public ResetMineLogic(int resetCooldown) {
        this.resetCooldown = resetCooldown;
    }

    @Override
    public String name() {
        return "reset";
    }

    @Override
    public CommandNode<CommandSourceStack> logic() {
        return literal(name()).requires(commandSourceStack -> {
            CommandSender commandSender = commandSourceStack.getSender();
            return commandSender.hasPermission("privatemines.reset") && commandSender instanceof Player;
        }).executes(context -> {
            Player sender = (Player) context.getSource().getSender();
            UUID uuid = sender.getUniqueId();

            if (!mineService.has(uuid)) {
                sender.sendRichMessage("<red>You do not own a mine!");
                return SINGLE_SUCCESS;
            }

            Mine mine = mineService.get(uuid);

            if (resetCooldown == 0) {
                mineService.reset(mine);
                return SINGLE_SUCCESS;
            }

            if (!Cooldowns.hasExpiredCooldown(uuid)) {
                int timeLeft = Cooldowns.get(uuid);
                sender.sendRichMessage("<red>Please wait " + timeLeft + " seconds to reset your mine again!");
                return SINGLE_SUCCESS;
            }

            Cooldowns.set(uuid, resetCooldown);

            mineService.reset(mine);
            return SINGLE_SUCCESS;
        }).build();
    }
}
