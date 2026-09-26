package dodo.dodosmpplugin.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dodo.dodosmpplugin.DodoConfig;
import dodo.dodosmpplugin.DodoFeature;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.Permissions;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DodoCommand {

    private static final PermissionCheck PERMISSION_CHECK =
            new PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER);

    // Teadaolevad seaded koos kirjelduse ja vaikeväärtusega
    private static final Map<String, String> KNOWN_SETTINGS = new LinkedHashMap<>();
    static {
        KNOWN_SETTINGS.put("auto_reload", "Jooksutab /reload automaatselt, kui crafting feature'i toggledatakse");
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                CommandBuildContext buildContext,
                                Commands.CommandSelection selection,
                                List<DodoFeature> features) {

        dispatcher.register(
            Commands.literal("dodosmpplugin")
                .requires(source -> PERMISSION_CHECK.check(source.permissions()))
                .then(Commands.literal("list")
                    .executes(ctx -> executeList(ctx, features)))
                .then(Commands.literal("enable")
                    .then(Commands.argument("feature_id", StringArgumentType.word())
                        .suggests((ctx, b) -> { features.forEach(f -> b.suggest(f.getId())); return b.buildFuture(); })
                        .executes(ctx -> executeToggle(ctx, true, features))))
                .then(Commands.literal("disable")
                    .then(Commands.argument("feature_id", StringArgumentType.word())
                        .suggests((ctx, b) -> { features.forEach(f -> b.suggest(f.getId())); return b.buildFuture(); })
                        .executes(ctx -> executeToggle(ctx, false, features))))
                .then(Commands.literal("settings")
                    .then(Commands.literal("list")
                        .executes(DodoCommand::executeSettingsList))
                    .then(Commands.literal("set")
                        .then(Commands.argument("key", StringArgumentType.word())
                            .suggests((ctx, b) -> { KNOWN_SETTINGS.keySet().forEach(b::suggest); return b.buildFuture(); })
                            .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(DodoCommand::executeSettingsSet)))))
        );
    }

    private static int executeList(CommandContext<CommandSourceStack> ctx, List<DodoFeature> features) {
        DodoConfig config = DodoConfig.getInstance();
        StringBuilder sb = new StringBuilder("=== DodoSMP Features ===\n");
        for (DodoFeature feature : features) {
            sb.append(config.isEnabled(feature.getId()) ? "[ON]  " : "[OFF] ")
              .append(feature.getId()).append("\n");
        }
        ctx.getSource().sendSystemMessage(Component.literal(sb.toString().trim()));
        return 1;
    }

    private static int executeToggle(CommandContext<CommandSourceStack> ctx,
                                     boolean enable, List<DodoFeature> features) {
        String featureId = StringArgumentType.getString(ctx, "feature_id");
        if (features.stream().noneMatch(f -> f.getId().equals(featureId))) {
            ctx.getSource().sendSystemMessage(Component.literal(
                "Unknown feature: " + featureId + ". Use /dodosmpplugin list to see available features."));
            return 0;
        }

        DodoConfig config = DodoConfig.getInstance();
        config.setEnabled(featureId, enable);
        config.save();

        String action = enable ? "enabled" : "disabled";
        boolean needsReload = featureId.equals("cheaper_golden_apple") || featureId.equals("craftable_god_apple");

        if (needsReload && config.getSetting("auto_reload", false)) {
            ctx.getSource().sendSystemMessage(Component.literal(
                capitalize(featureId) + " " + action + ". Running /reload..."));
            ReloadCommand.reloadPacks(
                ctx.getSource().getServer().getPackRepository().getSelectedPacks()
                    .stream().map(p -> p.getId()).toList(),
                ctx.getSource()
            );
        } else {
            String suffix = needsReload ? " Run /reload for this to take effect." : " Change takes effect immediately.";
            ctx.getSource().sendSystemMessage(Component.literal(capitalize(featureId) + " " + action + "." + suffix));
        }
        return 1;
    }

    private static int executeSettingsList(CommandContext<CommandSourceStack> ctx) {
        DodoConfig config = DodoConfig.getInstance();
        StringBuilder sb = new StringBuilder("=== DodoSMP Settings ===\n");
        for (Map.Entry<String, String> entry : KNOWN_SETTINGS.entrySet()) {
            boolean val = config.getSetting(entry.getKey(), false);
            sb.append(val ? "[ON]  " : "[OFF] ").append(entry.getKey())
              .append(" — ").append(entry.getValue()).append("\n");
        }
        ctx.getSource().sendSystemMessage(Component.literal(sb.toString().trim()));
        return 1;
    }

    private static int executeSettingsSet(CommandContext<CommandSourceStack> ctx) {
        String key = StringArgumentType.getString(ctx, "key");
        boolean value = BoolArgumentType.getBool(ctx, "value");

        if (!KNOWN_SETTINGS.containsKey(key)) {
            ctx.getSource().sendSystemMessage(Component.literal(
                "Unknown setting: " + key + ". Use /dodosmpplugin settings list to see available settings."));
            return 0;
        }

        DodoConfig config = DodoConfig.getInstance();
        config.setSetting(key, value);
        config.save();
        ctx.getSource().sendSystemMessage(Component.literal(
            "Setting '" + key + "' set to " + value + "."));
        return 1;
    }

    private static String capitalize(String id) {
        if (id == null || id.isEmpty()) return id;
        return id.substring(0, 1).toUpperCase() + id.substring(1).replace("_", " ");
    }
}
