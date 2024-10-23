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

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.checkerframework.checker.i18nformatter.qual.I18nConversionCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.apache.commons.lang3.text.WordUtils.capitalize;

public class Utils {

    public static Location toLocation(BlockVector3 vector3, org.bukkit.World world) {
        return new Location(world, vector3.getX(), vector3.getY(), vector3.getZ());
    }

    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static List<String> color(List<String> list) {
        List<String> stringList = new ArrayList<>();
        list.forEach(string -> stringList.add(color(string)));
        return stringList;
    }

    public static String colorBukkit(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String format(Material material) {
        return capitalize(material.name().toLowerCase().replaceAll("_", " "));
    }

    public static String mapToString(Map<Material, Double> map) {
        if (map.isEmpty()) return "";

        StringBuilder stringBuilder = new StringBuilder("{");
        for (Entry<Material, Double> entry : map.entrySet()) {
            stringBuilder.append(entry.getKey());
            stringBuilder.append("=");
            stringBuilder.append(entry.getValue());
            stringBuilder.append(", ");
        }

        stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}
