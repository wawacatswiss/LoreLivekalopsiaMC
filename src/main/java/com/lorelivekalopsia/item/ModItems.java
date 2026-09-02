package com.lorelivekalopsia.item;

import com.lorelivekalopsia.LoreLivekalopsia;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item CANON_HEART = registerItem("canon_heart", new CanonHeartItem());

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(LoreLivekalopsia.MOD_ID, name), item);
    }

    public static void registerModItems() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(CANON_HEART);
        });
    }
}
