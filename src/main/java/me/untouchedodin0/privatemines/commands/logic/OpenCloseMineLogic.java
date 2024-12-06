package me.untouchedodin0.privatemines.commands.logic;

import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import me.untouchedodin0.privatemines.commands.CommandLogic;
import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineService;
import org.bukkit.entity.Player;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

@RequiredArgsConstructor
public class OpenCloseMineLogic implements CommandLogic {
    private final MineService mineService;
    private final String literal;
    private final boolean state;

    @Override
    public CommandNode<CommandSourceStack> logic() {
        return name(literal).requires(commandSourceStack -> {
            return commandSourceStack.getSender().hasPermission("privatemines." + literal);
        }).executes(context -> {
            Player player = (Player) context.getSource().getSender();

            if (!mineService.has(player.getUniqueId())) {
                player.sendRichMessage("<red>You do not own a mine!");
                return SINGLE_SUCCESS;
            }

            Mine mine = mineService.get(player.getUniqueId());
            mine.getMineSettings().setOpen(state);

            String message = state ? "opened" : "closed";
            player.sendRichMessage("<green>Mine " + message + " successfully!");
            return SINGLE_SUCCESS;
        }).build();
    }
}
