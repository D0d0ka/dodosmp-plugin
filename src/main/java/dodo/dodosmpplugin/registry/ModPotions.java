package dodo.dodosmpplugin.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;

import dodo.dodosmpplugin.DodosmpPlugin;

/**
 * Kõik modifikatsioonipotionid registreeritakse siin.
 * Registreerimine toimub ainult siis, kui vastav feature on lubatud.
 */
public class ModPotions {

    /** Vodka potion: iiveldus (nausea) 1200 tick (60 sekundit) */
    public static Holder<Potion> VODKA = null;

    /** Registreerib vodka potioni BuiltInRegistries.POTION registrisse. */
    public static void registerVodka() {
        VODKA = Registry.registerForHolder(
                BuiltInRegistries.POTION,
                Identifier.fromNamespaceAndPath(DodosmpPlugin.MOD_ID, "vodka"),
                new Potion("vodka", new MobEffectInstance(MobEffects.NAUSEA, 1200, 0))
        );
        DodosmpPlugin.LOGGER.info("[DodoSMP] Vodka potion registreeritud.");
    }
}
