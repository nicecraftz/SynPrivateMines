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
import java.util.Map;
import java.util.Optional;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class AddonsCommand {
    private static final String ADDONS_LIST_FORMAT = "<green>Name: %s\n<aqua>Version: %s\n<aqua>Description: %s";
    private final PrivateMines privateMines;
    private final AddonManager addonManager;

    public AddonsCommand(PrivateMines privateMines) {
        this.privateMines = privateMines;
        this.addonManager = privateMines.getAddonManager();
    }


    public void registerCommands() {
        LifecycleEventManager<Plugin> lifecycleManager = privateMines.getLifecycleManager();
        lifecycleManager.registerEventHandler(
                LifecycleEvents.COMMANDS,
                event -> event.registrar().register(buildCommand())
        );
    }

    private LiteralCommandNode<CommandSourceStack> buildCommand() {
        return literal("addons").executes(noArgsCommand())
                .then(addonsLoadWithSearchLogic())
                .then(reloadCommandLogic())
                .then(disableCommandLogic())
                .build();
    }

    private Command<CommandSourceStack> noArgsCommand() {
        return context -> {
            CommandSender commandSender = context.getSource().getSender();
            Map<String, Addon> addons = privateMines.getAddonManager().getAddons();

            if (addons.isEmpty()) {
                commandSender.sendRichMessage("<green>Addons: <red>(None)");
                return SINGLE_SUCCESS;
            }

            StringBuilder stringBuilder = new StringBuilder(", ");
            commandSender.sendRichMessage(String.format("<green>Addons: <red>(%s)", addons.size()));

            for (Map.Entry<String, Addon> entry : addons.entrySet()) {
                String name = entry.getKey();
                Addon addon = entry.getValue();
                Addon.AddonDescription addonDescription = addon.addonDescription();

                stringBuilder.append(String.format(
                        ADDONS_LIST_FORMAT,
                        name,
                        addonDescription.version(),
                        addonDescription.description()
                ));
            }

            commandSender.sendRichMessage(stringBuilder.toString());
            return SINGLE_SUCCESS;
        };
    }

    private ArgumentBuilder<CommandSourceStack, ?> disableCommandLogic() {
        return literal("disable").then(argument("addon", StringArgumentType.word())).executes(context -> {
            CommandSender sender = context.getSource().getSender();
            String addonNameInput = context.getArgument("addon", String.class);

            Optional<Addon> removedAddonOptional = addonManager.remove(addonNameInput);

            if (removedAddonOptional.isEmpty()) {
                sender.sendRichMessage("<red>Failed to disable " + addonNameInput + " as it does not exist");
                return SINGLE_SUCCESS;
            }

            Addon removedAddon = removedAddonOptional.get();
            sender.sendRichMessage("<gold>Disabling " + removedAddon.addonDescription().description());
            removedAddon.disable();
            sender.sendRichMessage("<green>Successfully disabled " + addonNameInput);
            return SINGLE_SUCCESS;
        });
    }

    private ArgumentBuilder<CommandSourceStack, ?> reloadCommandLogic() {
        return literal("reload").then(argument("addonList", StringArgumentType.word())).executes(context -> {
            CommandSender sender = context.getSource().getSender();

            String addonsInput = context.getArgument("addonsList", String.class);
            String[] serializedAddons = addonsInput.split(",");

            for (String addonName : serializedAddons) {
                String trimmedAddonName = addonName.trim();
                Optional<Addon> addonOptional = addonManager.get(addonName);

                if (addonOptional.isEmpty()) {
                    sender.sendRichMessage("<red>Failed to reload " + trimmedAddonName + " as it does not exist");
                    return SINGLE_SUCCESS;
                }

                Addon addon = addonOptional.get();
                sender.sendRichMessage("<gold>Reloading " + trimmedAddonName);
                addon.reload();
                sender.sendRichMessage("<green>Successfully reloaded " + trimmedAddonName);
            }

            return SINGLE_SUCCESS;
        });
    }

    private ArgumentBuilder<CommandSourceStack, ?> addonsLoadWithSearchLogic() {
        return literal("load").then(argument("search", StringArgumentType.word()).executes(context -> {
            CommandSender sender = context.getSource().getSender();
            String searchInput = context.getArgument("search", String.class);
            File addonsDirectory = privateMines.getAddonsDirectory();
            TextComponent.Builder messageBuilder = Component.text()
                    .append(Component.text("Addon Files:", NamedTextColor.GOLD))
                    .decorate(TextDecoration.BOLD)
                    .append(Component.newline());

            for (File file : addonsDirectory.listFiles((dir, name) -> name.contains(searchInput))) {
                String fileName = file.getName();

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
