package ru.neon.cyberpunk;

import net.minecraft.item.Item;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvents;

import java.util.Map;

public final class ModArmorMaterials {
    public static final RegistryKey<EquipmentAsset> CYBERWARE_ASSET =
            RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, CyberpunkMod.id("cyberware"));
    public static final RegistryKey<EquipmentAsset> ASSAULT_ASSET =
            RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, CyberpunkMod.id("tactical_exosuit"));
    public static final TagKey<Item> REPAIR_INGREDIENT =
            TagKey.of(RegistryKeys.ITEM, CyberpunkMod.id("repairs_cyberware_armor"));

    public static final ArmorMaterial CYBERWARE = new ArmorMaterial(
            42,
            Map.of(EquipmentType.HELMET, 3, EquipmentType.CHESTPLATE, 8,
                    EquipmentType.LEGGINGS, 6, EquipmentType.BOOTS, 3),
            18, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, 2.5f, 0.16f,
            REPAIR_INGREDIENT, CYBERWARE_ASSET
    );

    public static final ArmorMaterial ASSAULT = new ArmorMaterial(
            52,
            Map.of(EquipmentType.HELMET, 4, EquipmentType.CHESTPLATE, 9,
                    EquipmentType.LEGGINGS, 7, EquipmentType.BOOTS, 4),
            16, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, 4.0f, 0.25f,
            REPAIR_INGREDIENT, ASSAULT_ASSET
    );

    private ModArmorMaterials() { }
}
