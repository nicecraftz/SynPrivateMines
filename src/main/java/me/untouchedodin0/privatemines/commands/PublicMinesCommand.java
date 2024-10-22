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

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.ScrollType;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.ScrollingGui;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import me.untouchedodin0.kotlin.mine.data.MineData;
import me.untouchedodin0.kotlin.mine.storage.MineStorage;
import me.untouchedodin0.privatemines.PrivateMines;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@CommandAlias("publicmines")
public class PublicMinesCommand extends BaseCommand {

  PrivateMines privateMines = PrivateMines.getInstance();
  MineStorage mineStorage = privateMines.getMineStorage();

  @Default
  public void defaultCommand(Player player) {
    AtomicInteger openAmount = new AtomicInteger();

    ScrollingGui scrollingGui = Gui.scrolling().title(Component.text("Public Mines")).rows(6)
        .pageSize(45).scrollType(ScrollType.VERTICAL).create();

    GuiItem back = ItemBuilder.from(Material.ARROW).name(Component.text(ChatColor.GREEN + "<-"))
        .lore(Component.text(ChatColor.GRAY + "Go Back")).asGuiItem(event -> {
          event.setCancelled(true);
          scrollingGui.previous();
        });

    GuiItem forward = ItemBuilder.from(Material.ARROW).name(Component.text(ChatColor.GREEN + "->"))
        .lore(Component.text(ChatColor.GRAY + "Go Forward")).asGuiItem(event -> {
          event.setCancelled(true);
          scrollingGui.next();
        });

    mineStorage.getMines().forEach((uuid, mine) -> {
      MineData mineData = mine.getMineData();
      boolean isOpen = mineData.isOpen();
      if (isOpen) {
        openAmount.getAndIncrement();
      }
    });

    mineStorage.getMines().forEach((uuid, mine) -> {
      MineData mineData = mine.getMineData();
      boolean isOpen = mineData.isOpen();
      if (isOpen) {
        UUID owner = mineData.getMineOwner();
        String name = Bukkit.getOfflinePlayer(owner).getName();
        String taxString = String.format("Tax: %.2f%%", mineData.getTax());

        GuiItem guiItem;
        if (name != null) {
          guiItem = ItemBuilder.from(Material.EMERALD)
              .name(Component.text(name).color(NamedTextColor.GREEN)).lore(
                  List.of(Component.text(ChatColor.GREEN + "Click to teleport"),
                      Component.text(ChatColor.GREEN + taxString))).asGuiItem(event -> {
                event.setCancelled(true);
                mine.teleport(player);
              });
          scrollingGui.addItem(guiItem);
        }
      }
    });

    if (openAmount.get() > 0) {
      scrollingGui.setItem(48, back);
      scrollingGui.setItem(50, forward);
      scrollingGui.open(player);
    } else {
      Gui gui = Gui.gui().title(Component.text("No Open Mines").color(NamedTextColor.DARK_RED))
          .create();
      gui.open(player);
    }
  }
}
