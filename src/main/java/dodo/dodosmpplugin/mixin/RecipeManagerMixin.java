package dodo.dodosmpplugin.mixin;

import dodo.dodosmpplugin.DodoConfig;
import dodo.dodosmpplugin.DodosmpPlugin;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Filtreerib välja keelatud feature'ite crafting retseptid pärast iga RecipeManager reload'i.
 * See tähendab, et /reload käsk on piisav retseptide enable/disable jaoks (restart ei ole vajalik).
 *
 * Mixin süstib end RecipeManager.apply() meetodi lõppu, et eemaldada keelatud retseptid.
 */
@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    /** Shadow 'recipes' väljale — selle kaudu asendatakse RecipeMap filtreerituga. */
    @Shadow @Mutable
    private RecipeMap recipes;

    /**
     * Pärast kõigi retseptide laadimist eemaldatakse keelatud feature'ite retseptid.
     * Meetod on protected apply(RecipeMap, ResourceManager, ProfilerFiller).
     */
    @Inject(
        method = "apply(Lnet/minecraft/world/item/crafting/RecipeMap;" +
                 "Lnet/minecraft/server/packs/resources/ResourceManager;" +
                 "Lnet/minecraft/util/profiling/ProfilerFiller;)V",
        at = @At("TAIL")
    )
    private void dodoFilterDisabledRecipes(RecipeMap recipeMap,
                                           ResourceManager resourceManager,
                                           ProfilerFiller profiler,
                                           CallbackInfo ci) {
        DodoConfig config = DodoConfig.getInstance();

        List<RecipeHolder<?>> filtered = recipes.values().stream()
                .filter(holder -> shouldKeepRecipe(holder.id().identifier(), config))
                .collect(Collectors.toList());

        if (filtered.size() < recipes.values().size()) {
            int removed = recipes.values().size() - filtered.size();
            this.recipes = RecipeMap.create(filtered);
            DodosmpPlugin.LOGGER.info("[DodoSMP] {} keelatud retsepti eemaldati.", removed);
        }
    }

    /** Kontrollib, kas retsept peaks laadituks jääma vastavalt config'ile. */
    private static boolean shouldKeepRecipe(Identifier id, DodoConfig config) {
        if (id.getNamespace().equals("minecraft")
                && id.getPath().equals("golden_apple")
                && config.isEnabled("cheaper_golden_apple")) {
            return false;
        }

        if (!DodosmpPlugin.MOD_ID.equals(id.getNamespace())) {
            return true;
        }
        return switch (id.getPath()) {
            case "cheaper_golden_apple" -> config.isEnabled("cheaper_golden_apple");
            case "craftable_god_apple"  -> config.isEnabled("craftable_god_apple");
            default -> true;
        };
    }
}
