package dodo.dodosmpplugin.mixin;

import dodo.dodosmpplugin.DodoConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

/**
 * Vodka pruulimisretsept ilma custom Potion registry entryta (vanilla kliendid saavad liituda).
 * OLULINE: hasMix(potion, ingredient) aga mix(ingredient, potion) — Minecraft API ebakõla!
 */
@Mixin(net.minecraft.world.item.alchemy.PotionBrewing.class)
public class PotionBrewingMixin {

    @Inject(method = "isIngredient", at = @At("HEAD"), cancellable = true)
    private void dodoIsIngredient(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(Items.WHEAT) && DodoConfig.getInstance().isEnabled("vodka")) {
            cir.setReturnValue(true);
        }
    }

    // isBrewable kutsub: hasMix(potionSlotItem, ingredientSlotItem) — potion on arg[0]!
    @Inject(method = "hasMix", at = @At("HEAD"), cancellable = true)
    private void dodoHasMix(ItemStack potionArg, ItemStack ingredientArg,
                             CallbackInfoReturnable<Boolean> cir) {
        if (isVodkaMix(ingredientArg, potionArg)) {
            cir.setReturnValue(true);
        }
    }

    // doBrew kutsub: mix(ingredientSlotItem, potionSlotItem) — ingredient on arg[0]!
    @Inject(method = "mix", at = @At("HEAD"), cancellable = true)
    private void dodoMix(ItemStack ingredientArg, ItemStack potionArg,
                         CallbackInfoReturnable<ItemStack> cir) {
        if (isVodkaMix(ingredientArg, potionArg)) {
            cir.setReturnValue(buildVodkaStack(potionArg.getItem()));
        }
    }

    private static boolean isVodkaMix(ItemStack ingredient, ItemStack potion) {
        if (!ingredient.is(Items.WHEAT)) return false;
        if (!DodoConfig.getInstance().isEnabled("vodka")) return false;
        if (!potion.is(Items.POTION)) return false;
        PotionContents contents = potion.get(DataComponents.POTION_CONTENTS);
        return contents != null && contents.is(Potions.WATER);
    }

    private static ItemStack buildVodkaStack(Item potionItem) {
        PotionContents contents = new PotionContents(
                Optional.empty(),
                Optional.of(0xFFFFFF), // valge värvus
                List.of(new MobEffectInstance(MobEffects.NAUSEA, 1200, 0)),
                Optional.empty()
        );
        ItemStack stack = new ItemStack(potionItem);
        stack.set(DataComponents.POTION_CONTENTS, contents);
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Vodka").withStyle(s -> s.withItalic(false)));
        return stack;
    }
}
