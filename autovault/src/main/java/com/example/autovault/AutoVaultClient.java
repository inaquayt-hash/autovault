package com.example.autovault;

import com.example.autovault.config.AutoVaultConfig;
import com.mojang.blaze3d.platform.InputUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil.Type;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AutoVaultClient implements ClientModInitializer {

    // Shows up in Options -> Controls -> "Auto Vault Key" category.
    public static KeyBinding toggleKey;
    public static KeyBinding switchTriggerItemKey;

    @Override
    public void onInitializeClient() {
        AutoVaultConfig.load();

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autovault.toggle",
                Type.KEYSYM,
                InputUtil.UNKNOWN_KEY.getCode(), // unbound by default; set it in Controls
                "category.autovault"
        ));

        switchTriggerItemKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autovault.switch_trigger",
                Type.KEYSYM,
                InputUtil.UNKNOWN_KEY.getCode(), // unbound by default; set it in Controls
                "category.autovault"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            handleKeybinds(client);
            VaultOpener.onClientTick(client);
        });
    }

    private void handleKeybinds(MinecraftClient client) {
        while (toggleKey.wasPressed()) {
            AutoVaultConfig cfg = AutoVaultConfig.get();
            cfg.toggleEnabled();
            if (client.player != null) {
                client.player.sendMessage(
                        Text.literal("[AutoVault] " + (cfg.enabled ? "Enabled" : "Disabled"))
                                .formatted(cfg.enabled ? Formatting.GREEN : Formatting.RED),
                        true);
            }
        }

        while (switchTriggerItemKey.wasPressed()) {
            AutoVaultConfig cfg = AutoVaultConfig.get();
            cfg.toggleOminousTrigger();
            if (client.player != null) {
                String name = cfg.ominousTrigger == AutoVaultConfig.OminousTrigger.HEAVY_CORE
                        ? "Heavy Core" : "Enchanted Golden Apple";
                client.player.sendMessage(
                        Text.literal("[AutoVault] Ominous vault now triggers on: " + name)
                                .formatted(Formatting.AQUA),
                        true);
            }
        }
    }
}
