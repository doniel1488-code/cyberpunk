package ru.neon.cyberpunk;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
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
        String commandTag = CyberwareManager.getInstallTag(implantType);
        if (user.getCommandTags().contains(commandTag)) {
            // Another chip of the same type is a removal tool and is not consumed.
            CyberwareManager.uninstall(serverPlayer, implantType);
            return ActionResult.SUCCESS_SERVER;
        }

        user.addCommandTag(commandTag);
        stack.decrement(1);
        user.sendMessage(Text.translatable(
                "message.cyberpunk.implant_installed",
                getName(),
                Text.translatable(CyberwareManager.getActivationKeyTranslation(implantType))
        ), true);
        world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT,
                SoundCategory.PLAYERS, 0.8f, 1.2f);
        return ActionResult.SUCCESS_SERVER;
    }
}
