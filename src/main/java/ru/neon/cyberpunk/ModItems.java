package ru.neon.cyberpunk;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class ModItems {
    public static final Item NEURAL_ACCELERATOR = registerImplant("neural_accelerator", 0);
    public static final Item OPTICAL_CAMO = registerImplant("optical_camo", 1);
    public static final Item BERSERK_MODULE = registerImplant("berserk_module", 2);
    public static final Item SANDEVISTAN = registerImplant("sandevistan", 3);

    private ModItems() {
    }

    private static Item registerImplant(String path, int implantType) {
        Identifier id = CyberpunkMod.id(path);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        Item.Settings settings = new Item.Settings().registryKey(key).maxCount(1);
        return Registry.register(Registries.ITEM, id, new ImplantItem(settings, implantType));
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(NEURAL_ACCELERATOR);
            entries.add(OPTICAL_CAMO);
            entries.add(BERSERK_MODULE);
            entries.add(SANDEVISTAN);
        });
    }
}
