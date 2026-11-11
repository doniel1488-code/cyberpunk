package ru.neon.cyberpunk;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public final class CyberwareScreen extends Screen {
    private static final int PANEL_WIDTH = 446;
    private static final int PANEL_HEIGHT = 350;
    private static final int CARD_WIDTH = 134;
    private static final int CARD_HEIGHT = 72;
    private static final int CARD_GAP_X = 8;
    private static final int CARD_GAP_Y = 8;

    private static final ImplantCard[] CARDS = {
            new ImplantCard(ModItems.NEURAL_ACCELERATOR, "item.cyberpunk.neural_accelerator",
                    "description.cyberpunk.neural_accelerator", "key.cyberpunk.neural_accelerator"),
            new ImplantCard(ModItems.OPTICAL_CAMO, "item.cyberpunk.optical_camo",
                    "description.cyberpunk.optical_camo", "key.cyberpunk.optical_camo"),
            new ImplantCard(ModItems.BERSERK_MODULE, "item.cyberpunk.berserk_module",
                    "description.cyberpunk.berserk", "key.cyberpunk.berserk"),
            new ImplantCard(ModItems.SANDEVISTAN, "item.cyberpunk.sandevistan",
                    "description.cyberpunk.sandevistan", "key.cyberpunk.sandevistan"),
            new ImplantCard(ModItems.MANTIS_ARMS, "item.cyberpunk.mantis_arms",
                    "description.cyberpunk.mantis_arms", "key.cyberpunk.mantis_arms"),
            new ImplantCard(ModItems.GORILLA_ARMS, "item.cyberpunk.gorilla_arms",
                    "description.cyberpunk.gorilla_arms", "key.cyberpunk.gorilla_arms"),
            new ImplantCard(ModItems.KERENZIKOV, "item.cyberpunk.kerenzikov",
                    "description.cyberpunk.kerenzikov", "key.cyberpunk.kerenzikov"),
            new ImplantCard(ModItems.SUBDERMAL_ARMOR, "item.cyberpunk.subdermal_armor",
                    "description.cyberpunk.subdermal_armor", "message.cyberpunk.passive"),
            new ImplantCard(ModItems.BLOOD_PUMP, "item.cyberpunk.blood_pump",
                    "description.cyberpunk.blood_pump", "key.cyberpunk.blood_pump")
    };

    private static CyberwareScreen activeScreen;
    private final Screen parent;
    private int installedMask;
    private int neuralLoad;
    private int psychosisTicks;
    private boolean receivedState;

    public CyberwareScreen(Screen parent) {
        super(Text.translatable("gui.cyberpunk.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        activeScreen = this;
        int x = panelX();
        int y = panelY();
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cyberpunk.close"),
                button -> client.setScreen(parent))
                .dimensions(x + PANEL_WIDTH - 78, y + 10, 66, 18)
                .build());
        if (client != null && client.getNetworkHandler() != null) {
            ClientPlayNetworking.send(new CyberwareActionPayload(0, -1));
        }
    }

    public static void acceptState(CyberwareStatePayload payload) {
        if (activeScreen == null) return;
        activeScreen.installedMask = payload.installedMask();
        activeScreen.neuralLoad = Math.max(0, Math.min(100, payload.neuralLoad()));
        activeScreen.psychosisTicks = Math.max(0, payload.psychosisTicks());
        activeScreen.receivedState = true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xB900050A);

        int x = panelX();
        int y = panelY();
        int right = x + PANEL_WIDTH;
        int bottom = y + PANEL_HEIGHT;

        context.fill(x - 2, y - 2, right + 2, bottom + 2, 0xFF02070D);
        context.fill(x, y, right, bottom, 0xF00B131D);
        context.fill(x, y, right, y + 3, 0xFF13D8E6);
        context.fill(x, bottom - 2, right, bottom, 0xFF126A83);
        context.fill(x, y, x + 2, bottom, 0xFF126A83);
        context.fill(right - 2, y, right, bottom, 0xFF126A83);

        context.drawText(textRenderer, Text.literal("CYBERPUNK // NEURAL INTERFACE"),
                x + 16, y + 12, 0xFFE7FBFF, false);
        context.drawText(textRenderer, Text.translatable("gui.cyberpunk.subtitle"),
                x + 16, y + 26, 0xFF70A7B9, false);

        int loadColor = neuralLoad >= 65 ? 0xFFFF5368 : neuralLoad >= 35 ? 0xFFFFC45B : 0xFF23D6C8;
        context.drawText(textRenderer,
                Text.translatable("gui.cyberpunk.load", neuralLoad),
                x + 16, y + 42, loadColor, false);
        int barX = x + 128;
        int barY = y + 45;
        int barWidth = 180;
        context.fill(barX, barY, barX + barWidth, barY + 6, 0xFF162B38);
        if (neuralLoad > 0) {
            context.fill(barX, barY, barX + Math.max(1, barWidth * neuralLoad / 100),
                    barY + 6, loadColor);
        }
        if (!receivedState) {
            context.drawText(textRenderer, Text.translatable("gui.cyberpunk.sync"),
                    barX + barWidth + 12, y + 42, 0xFF8BB7C7, false);
        } else if (psychosisTicks > 0) {
            context.drawText(textRenderer,
                    Text.translatable("gui.cyberpunk.overload", (psychosisTicks + 19) / 20),
                    barX + barWidth + 12, y + 42, 0xFFFF5268, false);
        }

        for (int i = 0; i < CARDS.length; i++) {
            drawCard(context, x, y, i, mouseX, mouseY);
        }

        context.drawText(textRenderer, Text.translatable("gui.cyberpunk.footer"),
                x + 16, bottom - 22, 0xFF789BAA, false);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawCard(DrawContext context, int panelX, int panelY, int index, int mouseX, int mouseY) {
        int column = index % 3;
        int row = index / 3;
        int x = panelX + 16 + column * (CARD_WIDTH + CARD_GAP_X);
        int y = panelY + 62 + row * (CARD_HEIGHT + CARD_GAP_Y);
        int right = x + CARD_WIDTH;
        int bottom = y + CARD_HEIGHT;
        boolean installed = (installedMask & (1 << index)) != 0;
        boolean hovered = mouseX >= x + 6 && mouseX <= right - 6
                && mouseY >= y + 54 && mouseY <= y + 69;

        context.fill(x, y, right, bottom, installed ? 0xFF102631 : 0xFF101923);
        context.fill(x, y, right, y + 1, installed ? 0xFF20D9D2 : 0xFF31414D);
        context.fill(x, bottom - 1, right, bottom, installed ? 0xFF126C75 : 0xFF253744);
        context.drawItem(new ItemStack(CARDS[index].item()), x + 7, y + 7);
        context.drawText(textRenderer,
                Text.literal(textRenderer.trimToWidth(Text.translatable(CARDS[index].nameKey()).getString(), 96)),
                x + 30, y + 8, 0xFFE6F4F8, false);
        context.drawText(textRenderer,
                Text.literal(textRenderer.trimToWidth("[" + CyberpunkClient.getActivationKeyLabel(index) + "]", 96)),
                x + 30, y + 21, 0xFF82B7C6, false);
        context.drawText(textRenderer,
                Text.literal(textRenderer.trimToWidth(Text.translatable(CARDS[index].descriptionKey()).getString(), 120)),
                x + 7, y + 35, 0xFF9AAEB8, false);
        context.drawText(textRenderer, Text.translatable(installed
                        ? "message.cyberpunk.status_installed" : "message.cyberpunk.status_missing"),
                x + 7, y + 47, installed ? 0xFF44E0B7 : 0xFF8395A2, false);

        int actionColor = installed ? 0xFF632A38 : 0xFF174A5B;
        context.fill(x + 6, y + 54, right - 6, y + 69, hovered
                ? (installed ? 0xFF8A3547 : 0xFF1E6C82) : actionColor);
        String actionText = Text.translatable(installed
                ? "gui.cyberpunk.remove" : "gui.cyberpunk.install").getString();
        context.drawCenteredTextWithShadow(textRenderer, actionText,
                x + CARD_WIDTH / 2, y + 55, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = panelX();
            int y = panelY();
            for (int i = 0; i < CARDS.length; i++) {
                int column = i % 3;
                int row = i / 3;
                int cardX = x + 16 + column * (CARD_WIDTH + CARD_GAP_X);
                int cardY = y + 62 + row * (CARD_HEIGHT + CARD_GAP_Y);
                if (mouseX >= cardX + 6 && mouseX <= cardX + CARD_WIDTH - 6
                        && mouseY >= cardY + 54 && mouseY <= cardY + 69) {
                    if (client != null && client.getNetworkHandler() != null) {
                        ClientPlayNetworking.send(new CyberwareActionPayload(1, i));
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        activeScreen = null;
        if (client != null) client.setScreen(parent);
    }

    @Override
    public void removed() {
        if (activeScreen == this) activeScreen = null;
        super.removed();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private int panelX() {
        return (width - PANEL_WIDTH) / 2;
    }

    private int panelY() {
        return (height - PANEL_HEIGHT) / 2;
    }

    private record ImplantCard(Item item, String nameKey, String descriptionKey, String keyKey) {
    }
}
