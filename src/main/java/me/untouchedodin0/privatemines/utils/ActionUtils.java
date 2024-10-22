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

package me.untouchedodin0.privatemines.utils;

import me.untouchedodin0.kotlin.menu.Menu;
import me.untouchedodin0.kotlin.mine.storage.MineStorage;
import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.config.MenuConfig;
import me.untouchedodin0.privatemines.mine.Mine;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ActionUtils {

  public static void handleClick(Player player, String clickAction) {
    ActionType actionType = ActionType.getByStart(clickAction);
    MineStorage mineStorage = PrivateMines.getInstance().getMineStorage();
    Mine mine = mineStorage.get(player.getUniqueId());
    Menu ownMine = MenuConfig.getMenus().get("personalMenu");
    Menu publicMines = MenuConfig.getMenus().get("publicMines");

    if (mine == null) {
      player.sendMessage(ChatColor.RED + "You don't own a mine!");
      player.closeInventory();
    } else if (actionType != null) {
      switch (actionType) {
        case RESET -> mine.reset();
        case RESET_TELEPORT -> {
          mine.reset();
          mine.teleport(player);
        }
        case TELEPORT -> mine.teleport(player);
        case OWNMINE -> {
          player.closeInventory();
          ownMine.open(player);
        }
        case PUBLICMINES -> publicMines.open(player);
        case EXPAND -> mine.expand();
      }
    }
  }
}
