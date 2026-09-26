package dodo.dodosmpplugin.feature;

import dodo.dodosmpplugin.DodoFeature;
import dodo.dodosmpplugin.DodosmpPlugin;

/**
 * Craftable God Apple feature: teeb Enchanted Golden Apple craftitavaks.
 * Pattern (3x3):
 *   "GGG"
 *   "GAG"
 *   "GGG"
 * G = gold_block, A = apple (täisring)
 *
 * Retsept on defineeritud JSON-failina resources/data/dodosmp-plugin/recipe/craftable_god_apple.json.
 * TOGGLE: RecipeManagerMixin eemaldab retsepti iga reload'i ajal, kui feature on keelatud.
 *         Muutus jõustub /reload käsuga (restart ei ole vajalik).
 */
public class CraftableGodAppleFeature implements DodoFeature {

    @Override
    public String getId() {
        return "craftable_god_apple";
    }

    @Override
    public void register() {
        // Crafting retsept on JSON-fail — RecipeManagerMixin käsitleb enable/disable loogikat
        DodosmpPlugin.LOGGER.info("[DodoSMP] Feature '{}' laaditud (retsept kontrollitakse reload'il).", getId());
    }
}
