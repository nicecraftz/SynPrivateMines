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
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.untouchedodin0.privatemines.PrivateMines;
import me.untouchedodin0.privatemines.utils.addon.Addon;
import me.untouchedodin0.privatemines.utils.addon.AddonManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class AddonsCommand {
    private static final PrivateMines PLUGIN_INSTANCE = PrivateMines.getInstance();
    private static final String ADDONS_LIST_FORMAT = "<green>Name: %s\n<aqua>Version: %s\n<aqua>Description: %s";

    private AddonsCommand() {
    }

    public static void registerCommands() {
        LifecycleEventManager<Plugin> lifecycleManager = PLUGIN_INSTANCE.getLifecycleManager();
        lifecycleManager.registerEventHandler(
                LifecycleEvents.COMMANDS,
                event -> event.registrar().register(buildCommand())
        );
    }

    private static LiteralCommandNode<CommandSourceStack> buildCommand() {
        return literal("addons").executes(noArgsCommand())
                .then(addonsLoadWithSearchLogic())
                .then(reloadCommandLogic())
                .then(disableCommandLogic())
                .build();
    }

    private static Command<CommandSourceStack> noArgsCommand() {
        return context -> {
            CommandSender commandSender = context.getSource().getSender();
            Map<String, Addon> addons = AddonManager.getAddons();

            if (addons.isEmpty()) {
                commandSender.sendRichMessage("<green>Addons: <red>(None)");
                return SINGLE_SUCCESS;
            }

            StringBuilder stringBuilder = new StringBuilder(", ");
            commandSender.sendRichMessage(String.format("<green>Addons: <red>(%s)", addons.size()));

            for (Map.Entry<String, Addon> entry : addons.entrySet()) {
                String name = entry.getKey();
                Addon addon = entry.getValue();

                stringBuilder.append(String.format(
                        ADDONS_LIST_FORMAT,
                        name,
                        addon.getVersion(),
                        addon.getDescription()
                ));
            }

            commandSender.sendRichMessage(stringBuilder.toString());
            return SINGLE_SUCCESS;
        };
    }

    private static ArgumentBuilder<CommandSourceStack, ?> disableCommandLogic() {
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

    private static ArgumentBuilder<CommandSourceStack, ?> reloadCommandLogic() {
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

    private static ArgumentBuilder<CommandSourceStack, ?> addonsLoadWithSearchLogic() {
        return literal("load").then(argument("search", StringArgumentType.word()).executes(context -> {
            CommandSender sender = context.getSource().getSender();
            String searchInput = context.getArgument("search", String.class);
            File addonsDirectory = PLUGIN_INSTANCE.getAddonsDirectory();

            FileWalkerFilter fileWalkerFilter = new FileWalkerFilter(searchInput);

            try {
                Files.walkFileTree(addonsDirectory.toPath(), fileWalkerFilter);
            } catch (IOException e) {
                PLUGIN_INSTANCE.logError("There was an error while trying to walk the addons directory path: ", e);
                return SINGLE_SUCCESS;
            }

            TextComponent.Builder messageBuilder = Component.text()
                    .append(Component.text("Addon Files:", NamedTextColor.GOLD))
                    .decorate(TextDecoration.BOLD)
                    .append(Component.newline());


            List<Path> matches = fileWalkerFilter.getMatches();
            if (matches.isEmpty()) {
                sender.sendMessage("<red>Unable to find any files matching that file name!");
            }

            for (Path filePath : matches) {
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

            TextComponent message = messageBuilder.build();
            sender.sendMessage(message);
            return SINGLE_SUCCESS;
        }));
    }
}
