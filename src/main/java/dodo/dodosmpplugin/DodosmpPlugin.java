package dodo.dodosmpplugin;

import dodo.dodosmpplugin.command.DodoCommand;
import dodo.dodosmpplugin.feature.CheaperGoldenAppleFeature;
import dodo.dodosmpplugin.feature.CraftableGodAppleFeature;
import dodo.dodosmpplugin.feature.VodkaFeature;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * DodoSMP Mod — serveri-poolne plugin SMP serveri lisafunktsioonide jaoks.
 * Uue feature'i lisamiseks: loo uus klass mis implementeerib DodoFeature,
 * seejärel lisa see FEATURES listi siia faili.
 */
public class DodosmpPlugin implements ModInitializer {

    public static final String MOD_ID = "dodosmp-plugin";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[DodoSMP] Plugin laaditakse...");

        // Laadi config fail (loob vaikimisi faili, kui seda ei ole)
        DodoConfig config = DodoConfig.getInstance();

        // Kõik feature'id — lisamiseks lisa siia uus rida
        List<DodoFeature> features = List.of(
                new VodkaFeature(),
                new CheaperGoldenAppleFeature(),
                new CraftableGodAppleFeature()
        );

        // Registreeri kõik feature'id (igaüks kontrollib ise, kas on config-is lubatud)
        features.forEach(DodoFeature::register);

        // Registreeri /dodosmpplugin käsk
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, buildContext, selection) ->
                        DodoCommand.register(dispatcher, buildContext, selection, features)
        );

        LOGGER.info("[DodoSMP] Plugin edukalt laaditud. {} feature(t) registreeritud.", features.size());
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
