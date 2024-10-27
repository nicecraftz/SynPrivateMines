/**
 * MIT License
 * <p>
 * Copyright (c) 2021 - 2023 Kyle Hicks
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package me.untouchedodin0.privatemines.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.ScrollType;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.ScrollingGui;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.mine.Mine;
import me.untouchedodin0.privatemines.mine.MineData;
import me.untouchedodin0.privatemines.mine.MineService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class PublicMinesCommand {
    private static final PrivateMines PLUGIN_INSTANCE = PrivateMines.inst();
    private static final MineService MINE_SERVICE = PLUGIN_INSTANCE.getMineService();
    private static String TAX_STRING_FORMAT = "Tax: %.2f%%";

    public void registerCommands() {
        LifecycleEventManager<Plugin> lifecycleManager = PLUGIN_INSTANCE.getLifecycleManager();
        lifecycleManager.registerEventHandler(
                LifecycleEvents.COMMANDS,
                event -> event.registrar().register(buildCommand())
        );
    }

    private LiteralCommandNode<CommandSourceStack> buildCommand() {
        return literal("publicmines").executes(defaultCommand()).build();
    }

    private Command<CommandSourceStack> defaultCommand() {
        return context -> {
            CommandSender sender = context.getSource().getSender();

            if (!(sender instanceof Player player)) {
                sender.sendRichMessage("<red>Only a player can execute this command!");
                return SINGLE_SUCCESS;
            }

            ScrollingGui scrollingGui = Gui.scrolling()
                    .title(Component.text("Public Mines"))
                    .rows(6)
                    .pageSize(45)
                    .scrollType(ScrollType.VERTICAL)
                    .create();

            int count = 0;
            for (Mine mine : MINE_SERVICE.getMines().values()) {
                MineData mineData = mine.getMineData();
                if (!mineData.isOpen()) continue;

                count++;

                String ownerName = Bukkit.getOfflinePlayer(mineData.getMineOwner()).getName();
                if (ownerName == null || ownerName.isEmpty()) continue;
                String formattedTax = String.format(TAX_STRING_FORMAT, mineData.getTax());

                GuiItem guiItem = ItemBuilder.from(Material.EMERALD)
                        .name(Component.text(ownerName).color(NamedTextColor.GREEN))
                        .lore(List.of(
                                Component.text(ChatColor.GREEN + "Click to teleport"),
                                Component.text(ChatColor.GREEN + formattedTax)
                        ))
                        .asGuiItem(event -> {
                            event.setCancelled(true);
                            mine.teleport(player);
                        });

                scrollingGui.addItem(guiItem);
            }

            if (count == 0) {
                Gui gui = Gui.gui().title(Component.text("No Open Mines").color(NamedTextColor.DARK_RED)).create();
                gui.open(player);
                return SINGLE_SUCCESS;
            }

            addDirectionalItems(scrollingGui);
            scrollingGui.open(player);
            return SINGLE_SUCCESS;
        };
    }

    private void addDirectionalItems(ScrollingGui scrollingGui) {
        GuiItem back = ItemBuilder.from(Material.ARROW)
                .name(Component.text(ChatColor.GREEN + "<-"))
                .lore(Component.text(ChatColor.GRAY + "Go Back"))
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    scrollingGui.previous();
                });
        scrollingGui.setItem(48, back);

        GuiItem forward = ItemBuilder.from(Material.ARROW)
                .name(Component.text(ChatColor.GREEN + "->"))
                .lore(Component.text(ChatColor.GRAY + "Go Forward"))
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    scrollingGui.next();
                });
        scrollingGui.setItem(50, forward);

    }
}
