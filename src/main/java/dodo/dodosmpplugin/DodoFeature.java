package dodo.dodosmpplugin;

/**
 * Iga feature implementeerib selle liidese.
 * Uue feature lisamiseks: 1 uus klass + 1 rida DodosmpPlugin.java registreerimislistis.
 */
public interface DodoFeature {

    /** Feature unikaalne id, nt "vodka", "cheaper_golden_apple" */
    String getId();

    /**
     * Registreerib kõik vajalikud ressursid (potionid, brewing retseptid jne).
     * Kutsutakse onInitialize() ajal ainult siis, kui feature on config-is lubatud.
     * MÄRKUS: Crafting retseptid on JSON-failid — need eemaldatakse RecipeManagerMixin kaudu
     * vastavalt config'ile iga reload'i ajal (sh /reload). Potioni muutused vajavad restarti.
     */
    void register();
}
