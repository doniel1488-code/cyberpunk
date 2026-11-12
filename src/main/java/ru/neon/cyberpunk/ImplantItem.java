package ru.neon.cyberpunk;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public final class ImplantItem extends Item {
    private final int implantType;

    public ImplantItem(Settings settings, int implantType) {
        super(settings);
        this.implantType = implantType;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient || !(user instanceof ServerPlayerEntity serverPlayer)) {
            return ActionResult.PASS;
        }
        ItemStack stack = user.getStackInHand(hand);
        CyberwareManager.install(serverPlayer, implantType, stack);
        return ActionResult.SUCCESS_SERVER;
    }
}
