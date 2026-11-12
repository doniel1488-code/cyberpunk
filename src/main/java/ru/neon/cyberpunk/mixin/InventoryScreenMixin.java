package ru.neon.cyberpunk.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.neon.cyberpunk.CyberwareScreen;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void cyberpunk$addCyberwareTab(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        int centerX = client.getWindow().getScaledWidth() / 2;
        int centerY = client.getWindow().getScaledHeight() / 2;
        ButtonWidget button = ButtonWidget.builder(Text.translatable("gui.cyberpunk.tab.short"),
                        clicked -> client.setScreen(new CyberwareScreen((Screen) (Object) this)))
                .dimensions(centerX + 88, centerY - 14, 56, 22)
                .build();
        ((ScreenAccessor) this).cyberpunk$addDrawableChild(button);
    }
}
