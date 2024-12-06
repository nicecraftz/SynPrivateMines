package me.untouchedodin0.privatemines.commands.logic;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import me.untouchedodin0.privatemines.commands.CommandLogic;
import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;

@RequiredArgsConstructor
public class TaxLogic implements CommandLogic {
    private final MineService mineService;

    @Override
    public CommandNode<CommandSourceStack> logic() {
        return name("tax").requires(commandSourceStack -> {
            CommandSender commandSender = commandSourceStack.getSender();
            return commandSender.hasPermission("privatemines.tax") && commandSender instanceof Player;
        }).then(argument("tax", IntegerArgumentType.integer(0, 100)).executes(context -> {
            Player player = (Player) context.getSource().getSender();
            double tax = context.getArgument("tax", Double.class);

            if (!mineService.has(player.getUniqueId())) {
                player.sendRichMessage("<red>You do not own a mine!");
                return SINGLE_SUCCESS;
            }

            Mine mine = mineService.get(player.getUniqueId());
            mine.getMineSettings().setTax(tax);
            player.sendRichMessage("<green>Successfully updated tax to " + tax + "%");
            return SINGLE_SUCCESS;
        })).build();
    }
}
