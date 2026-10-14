package com.simplemotionblur;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class BlurConfigScreen extends Screen {
    private final Screen parent;

    public BlurConfigScreen(Screen parent) {
        super(Text.literal("Simple Motion Blur"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        BlurConfig cfg = SimpleMotionBlur.CONFIG;
        int cx = this.width / 2 - 100;
        int cy = this.height / 2;

        addDrawableChild(ButtonWidget.builder(toggleText(cfg), b -> {
            cfg.enabled = !cfg.enabled;
            b.setMessage(toggleText(cfg));
        }).dimensions(cx, cy - 30, 200, 20).build());

        addDrawableChild(new SliderWidget(cx, cy, 200, 20, strengthText(cfg.strength), cfg.strength) {
            @Override
            protected void updateMessage() {
                setMessage(strengthText((float) this.value));
            }

            @Override
            protected void applyValue() {
                cfg.strength = (float) this.value;
            }
        });

        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, b -> close())
                .dimensions(cx, cy + 40, 200, 20).build());
    }

    private static Text toggleText(BlurConfig cfg) {
        return Text.literal("Motion Blur: " + (cfg.enabled ? "ON" : "OFF"));
    }

    private static Text strengthText(float v) {
        return Text.literal("Strength: " + Math.round(v * 100) + "%");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
    }

    @Override
    public void close() {
        SimpleMotionBlur.CONFIG.save();
        this.client.setScreen(parent);
    }
}
