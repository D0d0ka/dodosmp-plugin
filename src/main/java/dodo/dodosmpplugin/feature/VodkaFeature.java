package dodo.dodosmpplugin.feature;

import dodo.dodosmpplugin.DodoFeature;
import dodo.dodosmpplugin.DodosmpPlugin;

/**
 * Vodka feature: vesi + nisu → vodka (iiveldus 60s).
 * Retsept käsitletakse PotionBrewingMixin kaudu — ei lisa midagi kliendile sünkroositavatesse
 * registritesse, seega vanilla kliendid saavad liituda.
 * Splash/lingering variandid töötavad automaatselt (vanilla gunpowder/dragon's breath mekaanika).
 * TOGGLE: Jõustub koheselt (config loetakse pruulimisel).
 */
public class VodkaFeature implements DodoFeature {

    @Override
    public String getId() {
        return "vodka";
    }

    @Override
    public void register() {
        // Pruulimisloogika on PotionBrewingMixin-is — siin pole eraldi registreerimist vaja
        DodosmpPlugin.LOGGER.info("[DodoSMP] Feature '{}' laaditud.", getId());
    }
}


