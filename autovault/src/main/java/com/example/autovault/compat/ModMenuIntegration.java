package com.example.autovault.compat;

import com.example.autovault.gui.AutoVaultConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * This is what makes the blue/black screen appear when you click this mod
 * in the "Mods" button on the title screen / pause menu (ModMenu adds that
 * button). Works the same in singleplayer and multiplayer since it's a
 * purely client-side screen.
 */
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return AutoVaultConfigScreen::new;
    }
}
