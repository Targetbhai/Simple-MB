package com.simplemotionblur.mixin;

import com.simplemotionblur.MotionBlurRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    // Runs after the world is drawn but before the HUD, so only the 3D world gets blurred.
    @Inject(method = "render", at = @At("HEAD"))
    private void simplemotionblur$apply(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MotionBlurRenderer.apply(MinecraftClient.getInstance());
    }
}
