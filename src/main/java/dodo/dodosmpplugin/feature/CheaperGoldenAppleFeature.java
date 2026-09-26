package dodo.dodosmpplugin.feature;

import dodo.dodosmpplugin.DodoFeature;
import dodo.dodosmpplugin.DodosmpPlugin;

/**
 * Cheaper Golden Apple feature: lisab odavama crafting retsepti kuldõunale.
 * Pattern (3x3):
 *   " G "
 *   "GAG"
 *   " G "
 * G = gold_ingot, A = apple (nurgad tühjad)
 *
 * Retsept on defineeritud JSON-failina resources/data/dodosmp-plugin/recipe/cheaper_golden_apple.json.
 * TOGGLE: RecipeManagerMixin eemaldab retsepti iga reload'i ajal, kui feature on keelatud.
 *         Muutus jõustub /reload käsuga (restart ei ole vajalik).
 */
public class CheaperGoldenAppleFeature implements DodoFeature {

    @Override
    public String getId() {
        return "cheaper_golden_apple";
    }

    @Override
    public void register() {
        // Crafting retsept on JSON-fail — RecipeManagerMixin käsitleb enable/disable loogikat
        DodosmpPlugin.LOGGER.info("[DodoSMP] Feature '{}' laaditud (retsept kontrollitakse reload'il).", getId());
    }
}
