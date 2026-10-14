package com.simplemotionblur;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BlurConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean enabled = true;
    /** 0.0 (light) to 1.0 (strong) */
    public float strength = 0.5f;

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("simplemotionblur.json");
    }

    public static BlurConfig load() {
        try {
            Path p = path();
            if (Files.exists(p)) {
                BlurConfig c = GSON.fromJson(Files.readString(p), BlurConfig.class);
                if (c != null) {
                    c.strength = Math.max(0f, Math.min(1f, c.strength));
                    return c;
                }
            }
        } catch (Exception ignored) {
        }
        BlurConfig c = new BlurConfig();
        c.save();
        return c;
    }

    public void save() {
        try {
            Files.writeString(path(), GSON.toJson(this));
        } catch (IOException ignored) {
        }
    }
}
