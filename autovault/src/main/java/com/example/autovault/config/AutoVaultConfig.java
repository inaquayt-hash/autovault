package com.example.autovault.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple JSON-backed settings for the mod. Nothing fancy: just enough state
 * to remember whether the mod is on, which item triggers the ominous vault,
 * and the detection range.
 */
public class AutoVaultConfig {

    public enum OminousTrigger {
        HEAVY_CORE,
        ENCHANTED_GOLDEN_APPLE
    }

    private static final Path PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("autovault.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean enabled = true;
    public OminousTrigger ominousTrigger = OminousTrigger.HEAVY_CORE;
    public boolean watchTridentForNormalVault = true;
    // How many blocks away to notice a dropped item entity as a "trigger".
    public double itemDetectionRadius = 6.0;

    private static AutoVaultConfig instance;

    public static AutoVaultConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public static AutoVaultConfig load() {
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
                AutoVaultConfig cfg = GSON.fromJson(reader, AutoVaultConfig.class);
                if (cfg != null) {
                    return cfg;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        AutoVaultConfig fresh = new AutoVaultConfig();
        fresh.save();
        return fresh;
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void toggleEnabled() {
        enabled = !enabled;
        save();
    }

    public void toggleOminousTrigger() {
        ominousTrigger = (ominousTrigger == OminousTrigger.HEAVY_CORE)
                ? OminousTrigger.ENCHANTED_GOLDEN_APPLE
                : OminousTrigger.HEAVY_CORE;
        save();
    }
}
