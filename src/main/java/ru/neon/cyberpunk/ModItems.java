package ru.neon.cyberpunk;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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
    public static final Item PROJECTILE_LAUNCHER = registerImplant("projectile_launcher", 9);
    public static final Item REINFORCED_TENDONS = registerImplant("reinforced_tendons", 10);
    public static final Item BIOMONITOR = registerImplant("biomonitor", 11);
    public static final Item SMART_LINK = registerImplant("smart_link", 12);
    public static final Item MONOWIRE = registerImplant("monowire", 13);

    public static final Item CYBERNETIC_ALLOY = registerPart("cybernetic_alloy");
    public static final Item SERVO_MOTOR = registerPart("servo_motor");
    public static final Item NEURAL_PROCESSOR = registerPart("neural_processor");
    public static final Item SYNTHETIC_FIBER = registerPart("synthetic_fiber");
    public static final Item POWER_CELL = registerPart("power_cell");
    public static final Item NEURAL_CABLE = registerPart("neural_cable");
    public static final Item OPTIC_LENS = registerPart("optic_lens");
    public static final Item HYDRAULIC_ACTUATOR = registerPart("hydraulic_actuator");
    public static final Item TITANIUM_PLATING = registerPart("titanium_plating");
    public static final Item SYNTHETIC_MUSCLE = registerPart("synthetic_muscle");
    public static final Item POWER_REGULATOR = registerPart("power_regulator");
    public static final Item COMBAT_PROCESSOR = registerPart("combat_processor");

    public static final Item CYBERWARE_HELMET = registerArmor("cyberware_helmet", EquipmentType.HELMET);
    public static final Item CYBERWARE_CHESTPLATE = registerArmor("cyberware_chestplate", EquipmentType.CHESTPLATE);
    public static final Item CYBERWARE_LEGGINGS = registerArmor("cyberware_leggings", EquipmentType.LEGGINGS);
    public static final Item CYBERWARE_BOOTS = registerArmor("cyberware_boots", EquipmentType.BOOTS);
    public static final Item TACTICAL_HELMET = registerArmor("tactical_helmet", EquipmentType.HELMET, ModArmorMaterials.ASSAULT);
    public static final Item TACTICAL_CHESTPLATE = registerArmor("tactical_chestplate", EquipmentType.CHESTPLATE, ModArmorMaterials.ASSAULT);
    public static final Item TACTICAL_LEGGINGS = registerArmor("tactical_leggings", EquipmentType.LEGGINGS, ModArmorMaterials.ASSAULT);
    public static final Item TACTICAL_BOOTS = registerArmor("tactical_boots", EquipmentType.BOOTS, ModArmorMaterials.ASSAULT);

    private ModItems() { }

    private static Item registerImplant(String path, int type) {
        Identifier id = CyberpunkMod.id(path);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        return Registry.register(Registries.ITEM, id,
                new ImplantItem(new Item.Settings().registryKey(key).maxCount(1), type));
    }

    private static Item registerPart(String path) {
        Identifier id = CyberpunkMod.id(path);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        return Registry.register(Registries.ITEM, id,
                new Item(new Item.Settings().registryKey(key).maxCount(64)));
    }

    private static Item registerArmor(String path, EquipmentType type) {
        return registerArmor(path, type, ModArmorMaterials.CYBERWARE);
    }

    private static Item registerArmor(String path, EquipmentType type, ArmorMaterial material) {
        Identifier id = CyberpunkMod.id(path);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        return Registry.register(Registries.ITEM, id,
                new Item(new Item.Settings().registryKey(key).armor(material, type)));
    }

    public static void initialize() {
        RegistryKey<ItemGroup> key = RegistryKey.of(RegistryKeys.ITEM_GROUP, CyberpunkMod.id("cyberware"));
        ItemGroup group = FabricItemGroup.builder()
                .displayName(Text.translatable("itemGroup.cyberpunk.cyberware"))
                .icon(() -> new ItemStack(SANDEVISTAN))
                .entries((context, entries) -> {
                    entries.add(NEURAL_ACCELERATOR);
                    entries.add(OPTICAL_CAMO);
                    entries.add(BERSERK_MODULE);
                    entries.add(SANDEVISTAN);
                    entries.add(MANTIS_ARMS);
                    entries.add(GORILLA_ARMS);
                    entries.add(KERENZIKOV);
                    entries.add(SUBDERMAL_ARMOR);
                    entries.add(BLOOD_PUMP);
                    entries.add(PROJECTILE_LAUNCHER);
                    entries.add(REINFORCED_TENDONS);
                    entries.add(BIOMONITOR);
                    entries.add(SMART_LINK);
                    entries.add(MONOWIRE);

                    entries.add(CYBERNETIC_ALLOY);
                    entries.add(SERVO_MOTOR);
                    entries.add(NEURAL_PROCESSOR);
                    entries.add(SYNTHETIC_FIBER);
                    entries.add(POWER_CELL);
                    entries.add(NEURAL_CABLE);
                    entries.add(OPTIC_LENS);
                    entries.add(HYDRAULIC_ACTUATOR);
                    entries.add(TITANIUM_PLATING);
                    entries.add(SYNTHETIC_MUSCLE);
                    entries.add(POWER_REGULATOR);
                    entries.add(COMBAT_PROCESSOR);

                    entries.add(CYBERWARE_HELMET);
                    entries.add(CYBERWARE_CHESTPLATE);
                    entries.add(CYBERWARE_LEGGINGS);
                    entries.add(CYBERWARE_BOOTS);
                    entries.add(TACTICAL_HELMET);
                    entries.add(TACTICAL_CHESTPLATE);
                    entries.add(TACTICAL_LEGGINGS);
                    entries.add(TACTICAL_BOOTS);
                })
                .build();
        Registry.register(Registries.ITEM_GROUP, key, group);
    }
}
