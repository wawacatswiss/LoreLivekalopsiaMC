package com.lorelivekalopsia.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static KeyBinding TOGGLE_LORE_KEY;

    public static void register() {
        TOGGLE_LORE_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.lorelivekalopsia.toggle_lore",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.lorelivekalopsia.general"
        ));
    }
}
