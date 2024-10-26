/**
 * MIT License
 * <p>
 * Copyright (c) 2021 - 2023 Kyle Hicks
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.untouchedodin0.privatemines.playershops;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Shop {
    //todo Create a shop
    // A shop needs to hold the following things
    // Owner - UUID
    // Prices - Map of Materials and Doubles

    private final UUID owner;
    private final Map<Material, Double> materialPrices = new HashMap<>();

    public Shop(UUID owner) {
        this.owner = owner;
    }

    public UUID getOwner() {
        return owner;
    }

    public Map<Material, Double> getMaterialPrices() {
        return materialPrices;
    }

    public void setPrice(Material material, Double price) {
        Player player = Bukkit.getPlayer(getOwner());
        if (!materialPrices.containsKey(material)) return;
        materialPrices.put(material, price);
        if (player == null) return;

        player.sendMessage(ChatColor.GREEN + "Updated prices of " + material + " to $" + price);
    }

    public void addMaterialPrices(Map<Material, Double> materialPrices) {
        this.materialPrices.putAll(materialPrices);
    }
}
