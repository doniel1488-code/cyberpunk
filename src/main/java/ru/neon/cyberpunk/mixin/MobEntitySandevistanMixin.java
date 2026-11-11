package ru.neon.cyberpunk.mixin;

import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.neon.cyberpunk.CyberwareManager;

@Mixin(MobEntity.class)
public abstract class MobEntitySandevistanMixin {
    @Unique
    private int cyberpunk$sandevistanTickCounter;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void cyberpunk$applyTimeDilation(CallbackInfo callbackInfo) {
        MobEntity mob = (MobEntity) (Object) this;

        if (!CyberwareManager.isTimeDilated(mob)) {
            cyberpunk$sandevistanTickCounter = 0;
            return;
        }

        cyberpunk$sandevistanTickCounter++;
        if (cyberpunk$sandevistanTickCounter < 7) {
            callbackInfo.cancel();
        } else {
            cyberpunk$sandevistanTickCounter = 0;
        }
    }
}
