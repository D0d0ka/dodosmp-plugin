package dodo.dodosmpplugin.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dodo.dodosmpplugin.DodoConfig;
import dodo.dodosmpplugin.DodoFeature;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.Permissions;

import java.util.List;

/**
 * /dodosmpplugin käsk adminidele (permission level 2 = gamemaster/op).
 * Alamkäsud:
 *   list           — kuvab kõik feature'id ja nende oleku
 *   enable <id>    — lülitab feature sisse
 *   disable <id>   — lülitab feature välja
 */
public class DodoCommand {

    /** Gamemaster (op level 2) õiguse kontroll, sama pattern nagu vanilla GameModeCommand. */
    private static final PermissionCheck PERMISSION_CHECK =
            new PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER);

    /** Registreerib käsu dispatcher'isse. Kutsutakse CommandRegistrationCallback kaudu. */
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
                        .suggests((ctx, builder) -> {
                            features.forEach(f -> builder.suggest(f.getId()));
                            return builder.buildFuture();
                        })
                        .executes(ctx -> executeToggle(ctx, true, features))))
                .then(Commands.literal("disable")
                    .then(Commands.argument("feature_id", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            features.forEach(f -> builder.suggest(f.getId()));
                            return builder.buildFuture();
                        })
                        .executes(ctx -> executeToggle(ctx, false, features))))
        );
    }

    /** Kuvab kõik feature'id ja nende on/off oleku. */
    private static int executeList(CommandContext<CommandSourceStack> ctx,
                                   List<DodoFeature> features) {
        DodoConfig config = DodoConfig.getInstance();
        StringBuilder sb = new StringBuilder("=== DodoSMP Features ===\n");
        for (DodoFeature feature : features) {
            boolean enabled = config.isEnabled(feature.getId());
            sb.append(enabled ? "[ON]  " : "[OFF] ").append(feature.getId()).append("\n");
        }
        ctx.getSource().sendSystemMessage(Component.literal(sb.toString().trim()));
        return 1;
    }

    /** Lülitab feature sisse või välja ning salvestab config'i. */
    private static int executeToggle(CommandContext<CommandSourceStack> ctx,
                                     boolean enable,
                                     List<DodoFeature> features) {
        String featureId = StringArgumentType.getString(ctx, "feature_id");

        boolean exists = features.stream().anyMatch(f -> f.getId().equals(featureId));
        if (!exists) {
            ctx.getSource().sendSystemMessage(
                Component.literal("Unknown feature: " + featureId +
                    ". Use /dodosmpplugin list to see available features.")
            );
            return 0;
        }

        DodoConfig config = DodoConfig.getInstance();
        config.setEnabled(featureId, enable);
        config.save();

        String action = enable ? "enabled" : "disabled";
        String msg = capitalize(featureId) + " " + action + ". ";

        // Crafting retseptid jõustuvad /reload käsuga; vodka jõustub kohe
        if (featureId.equals("cheaper_golden_apple") || featureId.equals("craftable_god_apple")) {
            msg += "Run /reload for this to take effect.";
        } else {
            msg += "Change takes effect immediately.";
        }

        ctx.getSource().sendSystemMessage(Component.literal(msg));
        return 1;
    }

    private static String capitalize(String id) {
        if (id == null || id.isEmpty()) return id;
        return id.substring(0, 1).toUpperCase() + id.substring(1).replace("_", " ");
    }
}
