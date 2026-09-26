package dodo.dodosmpplugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Config laaditakse serveri käivitumisel failist config/dodosmp.json.
 * Failita loob vaikimisi config kõigi feature'itega lubatuna.
 */
public class DodoConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("dodosmp.json");

    private static DodoConfig instance;

    // feature_id -> lubatud/keelatud
    private Map<String, Boolean> features = new LinkedHashMap<>();

    /** Tagastab singleton-instants, laadib vajadusel failist. */
    public static DodoConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    /** Kas feature on lubatud. Kui config'is kirjet pole, on vaikimisi true. */
    public boolean isEnabled(String featureId) {
        return features.getOrDefault(featureId, true);
    }

    /** Seab feature'i lubatuse ja salvestab koheselt faili. */
    public void setEnabled(String featureId, boolean enabled) {
        features.put(featureId, enabled);
    }

    /** Loeb kõik feature id-d ja nende olekud. */
    public Map<String, Boolean> getFeatures() {
        return Collections.unmodifiableMap(features);
    }

    /** Loab või loob config faili. */
    public static DodoConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                DodoConfig loaded = GSON.fromJson(reader, DodoConfig.class);
                if (loaded != null) {
                    if (loaded.features == null) loaded.features = new LinkedHashMap<>();
                    instance = loaded;
                    return loaded;
                }
            } catch (IOException e) {
                DodosmpPlugin.LOGGER.error("[DodoSMP] Config'i laadimine ebaõnnestus: {}", e.getMessage());
            }
        }
        // Loo vaikimisi config (kõik feature'id on lubatud, kirjeid pole veel)
        instance = new DodoConfig();
        instance.save();
        return instance;
    }

    /** Salvestab config faili. */
    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            DodosmpPlugin.LOGGER.error("[DodoSMP] Config'i salvestamine ebaõnnestus: {}", e.getMessage());
        }
    }
}
