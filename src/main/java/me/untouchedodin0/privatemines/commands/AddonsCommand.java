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

import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.untouchedodin0.kotlin.utils.AudienceUtils;
import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.utils.addon.Addon;
import me.untouchedodin0.privatemines.utils.addon.AddonManager;
import me.untouchedodin0.privatemines.utils.file.FileWalkerFilter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class AddonsCommand {
    private static PrivateMines PLUGIN_INSTANCE = PrivateMines.getInstance();

    public static void registerCommands() {
        LifecycleEventManager<Plugin> lifecycleManager = PLUGIN_INSTANCE.getLifecycleManager();
        lifecycleManager.registerEventHandler(
                LifecycleEvents.COMMANDS,
                event -> event.registrar().register(buildCommand())
        );
    }

    private static LiteralCommandNode<CommandSourceStack> buildCommand() {
        return literal("privatemines").then(addonsCommand()).then(reloadCommand()).then(disableCommand()).build();
    }

    private static ArgumentBuilder<CommandSourceStack, ?> disableCommand() {
        return literal("disable").then(argument("addon", StringArgumentType.word())).executes(context -> {
            CommandSender sender = context.getSource().getSender();
            String addonNameInput = context.getArgument("addon", String.class);

            Addon addon = AddonManager.get(addonNameInput);

            sender.sendRichMessage("<gold>Disabling " + addon.getName());
            addon.onDisable();
            AddonManager.remove(addonNameInput);
            sender.sendRichMessage("<green>Successfully disabled " + addonNameInput);
            return SINGLE_SUCCESS;
        });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> reloadCommand() {
        return literal("reload").then(argument("addonList", StringArgumentType.word())).executes(context -> {
            CommandSender sender = context.getSource().getSender();

            String addonsInput = context.getArgument("addonsList", String.class);
            String[] serializedAddons = addonsInput.split(",");

            for (String addonName : serializedAddons) {
                String trimmedAddonName = addonName.trim();
                Addon addon = AddonManager.get(addonName);

                sender.sendRichMessage("<gold>Reloading " + trimmedAddonName);
                addon.onReload();
                sender.sendRichMessage("<green>Successfully reloaded " + trimmedAddonName);
            }

            return SINGLE_SUCCESS;
        });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> addonsCommand() {
        return literal("addons").executes(context -> {

        });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createCommand(JavaPlugin epicMines) {
        return literal("create").then(argument("name", StringArgumentType.word()).then(createWithSchematicArgumentLogic(
                epicMines)));
    }

    Component component;

    @Default
    public void send(Player player) {
        MiniMessage miniMessage = MiniMessage.miniMessage();
        this.component = miniMessage.deserialize("<green>Addons: ");
        BukkitAudiences bukkitAudiences = PLUGIN_INSTANCE.getAdventure();
        Audience audience = bukkitAudiences.player(player);

        Map<String, Addon> addons = AddonManager.getAddons();

        if (addons.isEmpty()) {
            Component noAddons = miniMessage.deserialize("<green>Addons: <red>None");
            audience.sendMessage(noAddons);
        } else {

            int count = 0;
            Builder message = Component.text().content(String.format("Addons: (%d): ", addons.size()));
            for (Map.Entry<String, Addon> entry : addons.entrySet()) {
                String name = entry.getKey();
                Addon addon = entry.getValue();

                TextComponent addonComponent = Component.text()
                        .content(name)
                        .color(NamedTextColor.GREEN)
                        .hoverEvent(HoverEvent.showText(Component.text()
                                .content("Name: " + addon.getName() + "\n")
                                .color(NamedTextColor.AQUA)
                                .append(Component.text("Version: " + addon.getVersion() + "\n")
                                        .color(NamedTextColor.AQUA))
                                .append(Component.text("Description: " + addon.getDescription())
                                        .color(NamedTextColor.AQUA))))
                        .build();
                message.append(addonComponent);
                if (++count < addons.size()) {
                    message.append(Component.text(", "));
                }
            }
            audience.sendMessage(message);
        }
    }

    @Subcommand("load")
    public void load(Player player, String search) {
        AudienceUtils audienceUtils = new AudienceUtils();
        Path addonsFolder = PLUGIN_INSTANCE.getAddonsDirectory();

        FileWalkerFilter fileWalkerFilter = new FileWalkerFilter(search);
        List<Path> results = Lists.newArrayList();

        try {
            Files.walkFileTree(addonsFolder, fileWalkerFilter);
            results.addAll(fileWalkerFilter.getMatches());
        } catch (IOException e) {
            PLUGIN_INSTANCE.getLogger().severe();
        }

        TextComponent.Builder messageBuilder = Component.text()
                .append(Component.text("Addon Files:", NamedTextColor.GOLD))
                .decorate(TextDecoration.BOLD)
                .append(Component.newline());

        for (Path filePath : matchingFiles) {
            String fileName = filePath.getFileName().toString();
            TextComponent fileComponent = Component.text()
                    .append(Component.text("- " + fileName + " ", NamedTextColor.YELLOW)
                            .hoverEvent(Component.text("Click to load " + fileName, NamedTextColor.GREEN)))
                    .append(Component.text("[Load]", NamedTextColor.GREEN))
                    .clickEvent(ClickEvent.suggestCommand("/addons load " + fileName))
                    .append(Component.newline())
                    .build();
            messageBuilder.append(fileComponent);
        }

        if (matchingFiles.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Unable to find any files matching that file name!");
        } else {
            TextComponent message = messageBuilder.build();
            audienceUtils.sendMessage(player, message);
        }
    }

    @Subcommand("loadfile")
    public void loadfile(Player player, String string) {
        final String fileRegex = ".+\\.jar$";
        Path addonsFolder = PLUGIN_INSTANCE.getAddonsDirectory();

        if (!string.matches(fileRegex)) {
            player.sendMessage(ChatColor.RED + "That's a incorrect file format!");
            return;
        }

        try {
            Files.walkFileTree(addonsFolder, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path filePath, BasicFileAttributes attributes) {
                    String fileName = filePath.getFileName().toString().toLowerCase(); // Convert to lowercase
                    String search = string.toLowerCase(); // Convert search string to lowercase

                    if (fileName.contains(search) && fileName.matches(fileRegex)) {
                        File file = filePath.toFile();
                        Bukkit.broadcastMessage("jar file: " + file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
