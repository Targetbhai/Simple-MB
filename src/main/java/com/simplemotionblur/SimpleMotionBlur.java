package com.simplemotionblur;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class SimpleMotionBlur implements ClientModInitializer {
    public static BlurConfig CONFIG;
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        CONFIG = BlurConfig.load();

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.simplemotionblur.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.simplemotionblur"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                CONFIG.enabled = !CONFIG.enabled;
                CONFIG.save();
                if (client.player != null) {
                    client.player.sendMessage(
                            Text.literal("Motion Blur: " + (CONFIG.enabled ? "ON" : "OFF")), true);
                }
            }
        });
    }
}
