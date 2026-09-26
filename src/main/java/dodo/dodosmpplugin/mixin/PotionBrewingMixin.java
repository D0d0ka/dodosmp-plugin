package dodo.dodosmpplugin.mixin;

import dodo.dodosmpplugin.DodoConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

/**
 * Lisab vodka pruulimisretsepti ilma custom Potion registry entryta.
 * Vesi + nisu → vodka (PotionContents komponendiga), kliendid ei vaja moди.
 */
@Mixin(net.minecraft.world.item.alchemy.PotionBrewing.class)
public class PotionBrewingMixin {

    /** Teeb nisu kehtivaks pruulimiskoostisosaks, kui vodka on lubatud. */
    @Inject(method = "isIngredient", at = @At("HEAD"), cancellable = true)
    private void dodoIsIngredient(ItemStack stack,
                                  CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(Items.WHEAT) && DodoConfig.getInstance().isEnabled("vodka")) {
            cir.setReturnValue(true);
        }
    }

    /** Tunneb ära vesi + nisu kombinatsiooni pruulimiseks. */
    @Inject(method = "hasMix", at = @At("HEAD"), cancellable = true)
    private void dodoHasMix(ItemStack ingredient, ItemStack input,
                             CallbackInfoReturnable<Boolean> cir) {
        if (isVodkaMix(ingredient, input)) {
            cir.setReturnValue(true);
        }
    }

    /** Loob vodka ItemStack-i PotionContents komponendiga (ilma registry entryta). */
    @Inject(method = "mix", at = @At("HEAD"), cancellable = true)
    private void dodoMix(ItemStack ingredient, ItemStack input,
                          CallbackInfoReturnable<ItemStack> cir) {
        if (isVodkaMix(ingredient, input)) {
            cir.setReturnValue(buildVodkaStack(input.getItem()));
        }
    }

    private static boolean isVodkaMix(ItemStack ingredient, ItemStack input) {
        if (!ingredient.is(Items.WHEAT)) return false;
        if (!DodoConfig.getInstance().isEnabled("vodka")) return false;
        if (!input.is(Items.POTION)) return false;
        // Kontrollime, et tegemist on veepudeliga (mitte mõne muu potioniga)
        PotionContents contents = input.get(DataComponents.POTION_CONTENTS);
        return contents != null && contents.is(Potions.WATER);
    }

    /** Vodka = Items.POTION + iivelduse efekt, custom nimi, ilma registreeritud Potion-ita. */
    private static ItemStack buildVodkaStack(Item potionItem) {
        PotionContents contents = new PotionContents(
                Optional.empty(),
                Optional.empty(),
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
