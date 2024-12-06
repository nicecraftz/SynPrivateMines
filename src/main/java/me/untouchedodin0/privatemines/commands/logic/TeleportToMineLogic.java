package me.untouchedodin0.privatemines.commands.logic;

import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import me.untouchedodin0.privatemines.commands.CommandLogic;
import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

@RequiredArgsConstructor
public class TeleportToMineLogic implements CommandLogic {
    private final MineService mineService;

    @Override
    public CommandNode<CommandSourceStack> logic() {
        return name("teleport").requires(commandSourceStack -> {
            CommandSender commandSender = commandSourceStack.getSender();
            return commandSender.hasPermission("privatemines.teleport") && commandSender instanceof Player;
        }).executes(context -> {
            Player player = (Player) context.getSource().getSender();
            if (!mineService.has(player.getUniqueId())) {
                player.sendRichMessage("<red>You do not own a mine!");
                return SINGLE_SUCCESS;
            }

            Mine mine = mineService.get(player.getUniqueId());
            mine.teleport(player);
            return SINGLE_SUCCESS;
        }).build();
    }
}
