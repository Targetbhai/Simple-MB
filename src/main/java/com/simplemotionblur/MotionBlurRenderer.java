package com.simplemotionblur;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Camera-based motion blur using only vanilla Framebuffer + vanilla shaders,
 * so it works the same with Sodium and VulkanMod (no custom GL calls).
 *
 * Each frame: blend the previous frame over the new one (weight depends on how fast
 * the camera moved/turned), then save the result as the next "previous frame".
 */
public final class MotionBlurRenderer {
    private static Framebuffer history;
    private static boolean valid = false;

    private static boolean hasLast = false;
    private static float lastYaw, lastPitch;
    private static Vec3d lastPos = Vec3d.ZERO;
    private static long lastNanos;

    private MotionBlurRenderer() {}

    public static void apply(MinecraftClient mc) {
        BlurConfig cfg = SimpleMotionBlur.CONFIG;
        if (cfg == null || !cfg.enabled || mc.world == null) {
            valid = false;
            hasLast = false;
            return;
        }

        Framebuffer main = mc.getFramebuffer();
        int w = main.textureWidth;
        int h = main.textureHeight;

        if (history == null || history.textureWidth != w || history.textureHeight != h) {
            if (history != null) history.delete();
            history = new SimpleFramebuffer(w, h, false, MinecraftClient.IS_SYSTEM_MAC);
            history.setClearColor(0f, 0f, 0f, 1f);
            history.clear(MinecraftClient.IS_SYSTEM_MAC);
            main.beginWrite(true); // clear() unbinds, rebind the main framebuffer
            valid = false;
        }

        // --- how much did the camera move since last frame? ---
        Camera cam = mc.gameRenderer.getCamera();
        long now = System.nanoTime();
        float weight = 0f;

        if (hasLast) {
            float dt = MathHelper.clamp((now - lastNanos) / 1.0e9f, 0.001f, 0.1f);
            float dYaw = Math.abs(MathHelper.wrapDegrees(cam.getYaw() - lastYaw));
            float dPitch = Math.abs(cam.getPitch() - lastPitch);
            double dist = cam.getPos().distanceTo(lastPos);

            if (dist > 8.0) {
                valid = false; // teleport / dimension change
            }

            float angPerSec = (dYaw + dPitch) / dt;           // degrees / second
            float movePerSec = (float) (dist / dt);           // blocks / second
            float rot = MathHelper.clamp((angPerSec - 20f) / 400f, 0f, 1f);
            float move = MathHelper.clamp(movePerSec / 40f, 0f, 1f) * 0.5f;
            float t = Math.max(rot, move);

            // persistence per 1/60s, then made frame-rate independent
            float a = (0.15f + 0.8f * cfg.strength) * t;
            weight = (float) Math.pow(a, dt * 60.0f);
        }

        lastYaw = cam.getYaw();
        lastPitch = cam.getPitch();
        lastPos = cam.getPos();
        lastNanos = now;
        hasLast = true;

        float qw = mc.getWindow().getScaledWidth();
        float qh = mc.getWindow().getScaledHeight();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        // 1) blend previous frame over current frame
        if (valid && weight > 0.02f) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.colorMask(true, true, true, false);
            drawQuad(history.getColorAttachment(), weight, qw, qh);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.disableBlend();
        }

        // 2) save current result as history
        history.beginWrite(true);
        RenderSystem.disableBlend();
        RenderSystem.colorMask(true, true, true, false);
        drawQuad(main.getColorAttachment(), 1f, qw, qh);
        RenderSystem.colorMask(true, true, true, true);
        main.beginWrite(true);
        valid = true;

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private static void drawQuad(int texture, float alpha, float qw, float qh) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        BufferBuilder b = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        b.vertex(0f, qh, 0f).texture(0f, 0f);
        b.vertex(qw, qh, 0f).texture(1f, 0f);
        b.vertex(qw, 0f, 0f).texture(1f, 1f);
        b.vertex(0f, 0f, 0f).texture(0f, 1f);
        BufferRenderer.drawWithGlobalProgram(b.end());
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
}
