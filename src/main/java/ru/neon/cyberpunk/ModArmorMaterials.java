package ru.neon.cyberpunk;

import net.minecraft.item.Item;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvents;
import net.minecraft.registry.tag.TagKey;

import java.util.Map;

public final class ModArmorMaterials {
    public static final int BASE_DURABILITY = 38;
    public static final RegistryKey<EquipmentAsset> ASSET_ID = EquipmentAssetKeys.register("cyberware");
    public static final TagKey<Item> REPAIR_INGREDIENT =
            TagKey.of(RegistryKeys.ITEM, CyberpunkMod.id("repairs_cyberware_armor"));

    public static final ArmorMaterial CYBERWARE = new ArmorMaterial(
            BASE_DURABILITY,
            Map.of(
                    EquipmentType.HELMET, 3,
                    EquipmentType.CHESTPLATE, 8,
                    EquipmentType.LEGGINGS, 6,
                    EquipmentType.BOOTS, 3
            ),
            18,
            SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
            2.0f,
            0.12f,
            REPAIR_INGREDIENT,
            ASSET_ID
    );

    private ModArmorMaterials() {
    }
}
