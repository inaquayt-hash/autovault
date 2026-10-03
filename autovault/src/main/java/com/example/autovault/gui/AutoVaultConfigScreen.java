package com.example.autovault.gui;

import com.example.autovault.config.AutoVaultConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * A small hand-rolled config screen (no Cloth Config dependency) themed in
 * blue/black to match what was asked for. Reachable from ModMenu's "Mods"
 * list, or you can open it yourself with:
 *   MinecraftClient.getInstance().setScreen(new AutoVaultConfigScreen(parent));
 */
public class AutoVaultConfigScreen extends Screen {

    // Theme colours (ARGB).
    private static final int COLOR_BG_TOP = 0xF0050914;      // near-black with a blue tint
    private static final int COLOR_BG_BOTTOM = 0xF0000000;   // pure black fade
    private static final int COLOR_PANEL = 0xE00B1230;       // dark navy panel
    private static final int COLOR_PANEL_BORDER = 0xFF2F6FED; // bright blue border
    private static final int COLOR_TITLE = 0xFF5DA8FF;        // light blue title text

    private final Screen parent;
    private ButtonWidget enabledButton;
    private ButtonWidget triggerButton;
    private ButtonWidget tridentButton;

    public AutoVaultConfigScreen(Screen parent) {
        super(Text.translatable("autovault.screen.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        AutoVaultConfig cfg = AutoVaultConfig.get();

        int centerX = this.width / 2;
        int top = this.height / 2 - 50;
        int buttonWidth = 240;
        int buttonHeight = 20;
        int spacing = 26;

        enabledButton = ButtonWidget.builder(enabledLabel(cfg), btn -> {
                    cfg.toggleEnabled();
                    btn.setMessage(enabledLabel(cfg));
                })
                .dimensions(centerX - buttonWidth / 2, top, buttonWidth, buttonHeight)
                .build();

        triggerButton = ButtonWidget.builder(triggerLabel(cfg), btn -> {
                    cfg.toggleOminousTrigger();
                    btn.setMessage(triggerLabel(cfg));
                })
                .dimensions(centerX - buttonWidth / 2, top + spacing, buttonWidth, buttonHeight)
                .build();

        tridentButton = ButtonWidget.builder(tridentLabel(cfg), btn -> {
                    cfg.watchTridentForNormalVault = !cfg.watchTridentForNormalVault;
                    cfg.save();
                    btn.setMessage(tridentLabel(cfg));
                })
                .dimensions(centerX - buttonWidth / 2, top + spacing * 2, buttonWidth, buttonHeight)
                .build();

        ButtonWidget doneButton = ButtonWidget.builder(Text.translatable("gui.done"),
                        btn -> this.close())
                .dimensions(centerX - buttonWidth / 2, top + spacing * 4, buttonWidth, buttonHeight)
                .build();

        addDrawableChild(enabledButton);
        addDrawableChild(triggerButton);
        addDrawableChild(tridentButton);
        addDrawableChild(doneButton);
    }

    private Text enabledLabel(AutoVaultConfig cfg) {
        return Text.translatable(cfg.enabled ? "autovault.screen.enabled_on" : "autovault.screen.enabled_off")
                .formatted(cfg.enabled ? Formatting.GREEN : Formatting.RED);
    }

    private Text triggerLabel(AutoVaultConfig cfg) {
        return Text.translatable(cfg.ominousTrigger == AutoVaultConfig.OminousTrigger.HEAVY_CORE
                ? "autovault.screen.trigger_core" : "autovault.screen.trigger_apple")
                .formatted(Formatting.AQUA);
    }

    private Text tridentLabel(AutoVaultConfig cfg) {
        return Text.translatable(cfg.watchTridentForNormalVault
                ? "autovault.screen.trident_on" : "autovault.screen.trident_off")
                .formatted(cfg.watchTridentForNormalVault ? Formatting.GREEN : Formatting.RED);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Blue-black vertical gradient background instead of the vanilla one.
        context.fillGradient(0, 0, this.width, this.height, COLOR_BG_TOP, COLOR_BG_BOTTOM);

        // A bordered panel behind the buttons for a bit of visual structure.
        int panelWidth = 280;
        int panelTop = this.height / 2 - 70;
        int panelBottom = this.height / 2 + 66;
        int panelLeft = this.width / 2 - panelWidth / 2;
        int panelRight = this.width / 2 + panelWidth / 2;

        context.fill(panelLeft, panelTop, panelRight, panelBottom, COLOR_PANEL);
        context.fill(panelLeft, panelTop, panelRight, panelTop + 1, COLOR_PANEL_BORDER);
        context.fill(panelLeft, panelBottom - 1, panelRight, panelBottom, COLOR_PANEL_BORDER);
        context.fill(panelLeft, panelTop, panelLeft + 1, panelBottom, COLOR_PANEL_BORDER);
        context.fill(panelRight - 1, panelTop, panelRight, panelBottom, COLOR_PANEL_BORDER);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title,
                this.width / 2, panelTop + 10, COLOR_TITLE);

        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("autovault.screen.hint").formatted(Formatting.GRAY),
                this.width / 2, panelBottom - 20, 0xFFAAAAAA);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}
