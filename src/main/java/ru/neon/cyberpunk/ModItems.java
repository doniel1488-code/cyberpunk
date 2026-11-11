package ru.neon.cyberpunk;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;

public final class ModItems {
    public static final Item NEURAL_ACCELERATOR = registerImplant("neural_accelerator", 0);
    public static final Item OPTICAL_CAMO = registerImplant("optical_camo", 1);
    public static final Item BERSERK_MODULE = registerImplant("berserk_module", 2);
    public static final Item SANDEVISTAN = registerImplant("sandevistan", 3);
    public static final Item MANTIS_ARMS = registerImplant("mantis_arms", 4);
    public static final Item GORILLA_ARMS = registerImplant("gorilla_arms", 5);
    public static final Item KERENZIKOV = registerImplant("kerenzikov", 6);
    public static final Item SUBDERMAL_ARMOR = registerImplant("subdermal_armor", 7);
    public static final Item BLOOD_PUMP = registerImplant("blood_pump", 8);

    public static final Item CYBERNETIC_ALLOY = registerPart("cybernetic_alloy");
    public static final Item SERVO_MOTOR = registerPart("servo_motor");
    public static final Item NEURAL_PROCESSOR = registerPart("neural_processor");
    public static final Item SYNTHETIC_FIBER = registerPart("synthetic_fiber");
    public static final Item POWER_CELL = registerPart("power_cell");

    public static final Item CYBERWARE_HELMET = registerArmor("cyberware_helmet", EquipmentType.HELMET);
    public static final Item CYBERWARE_CHESTPLATE = registerArmor("cyberware_chestplate", EquipmentType.CHESTPLATE);
    public static final Item CYBERWARE_LEGGINGS = registerArmor("cyberware_leggings", EquipmentType.LEGGINGS);
    public static final Item CYBERWARE_BOOTS = registerArmor("cyberware_boots", EquipmentType.BOOTS);

    private ModItems() {
    }

    private static Item registerImplant(String path, int implantType) {
        Identifier id = CyberpunkMod.id(path);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        Item.Settings settings = new Item.Settings().registryKey(key).maxCount(1);
        return Registry.register(Registries.ITEM, id, new ImplantItem(settings, implantType));
    }

    private static Item registerPart(String path) {
        Identifier id = CyberpunkMod.id(path);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        return Registry.register(Registries.ITEM, id,
                new Item(new Item.Settings().registryKey(key).maxCount(64)));
    }

    private static Item registerArmor(String path, EquipmentType type) {
        Identifier id = CyberpunkMod.id(path);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        Item.Settings settings = new Item.Settings().registryKey(key).armor(ModArmorMaterials.CYBERWARE, type);
        return Registry.register(Registries.ITEM, id, new Item(settings));
    }

    public static void initialize() {
        RegistryKey<ItemGroup> cyberwareKey =
                RegistryKey.of(RegistryKeys.ITEM_GROUP, CyberpunkMod.id("cyberware"));
        ItemGroup cyberwareGroup = FabricItemGroup.builder()
                .displayName(Text.translatable("itemGroup.cyberpunk.cyberware"))
                .icon(() -> new ItemStack(SANDEVISTAN))
                .entries((context, entries) -> {
                    // Coreware implants first, then fabrication parts, then the complete armor set.
                    entries.add(NEURAL_ACCELERATOR);
                    entries.add(OPTICAL_CAMO);
                    entries.add(BERSERK_MODULE);
                    entries.add(SANDEVISTAN);
                    entries.add(MANTIS_ARMS);
                    entries.add(GORILLA_ARMS);
                    entries.add(KERENZIKOV);
                    entries.add(SUBDERMAL_ARMOR);
                    entries.add(BLOOD_PUMP);

                    entries.add(CYBERNETIC_ALLOY);
                    entries.add(SERVO_MOTOR);
                    entries.add(NEURAL_PROCESSOR);
                    entries.add(SYNTHETIC_FIBER);
                    entries.add(POWER_CELL);

                    entries.add(CYBERWARE_HELMET);
                    entries.add(CYBERWARE_CHESTPLATE);
                    entries.add(CYBERWARE_LEGGINGS);
                    entries.add(CYBERWARE_BOOTS);
                })
                .build();
        Registry.register(Registries.ITEM_GROUP, cyberwareKey, cyberwareGroup);
    }
}
