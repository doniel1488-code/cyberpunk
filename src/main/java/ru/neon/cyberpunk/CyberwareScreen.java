package ru.neon.cyberpunk;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public final class CyberwareScreen extends Screen {
    private static final int[] SLOT_ORDER = {
            CyberwareManager.NEURAL_SLOT, CyberwareManager.OPTICS_SLOT,
            CyberwareManager.OPERATING_SYSTEM_SLOT, CyberwareManager.ARMS_SLOT,
            CyberwareManager.INTEGUMENTARY_SLOT, CyberwareManager.CIRCULATORY_SLOT,
            CyberwareManager.LEGS_SLOT
    };
    private static final ImplantCard[] IMPLANTS = {
            card(ModItems.NEURAL_ACCELERATOR,0), card(ModItems.OPTICAL_CAMO,1),
            card(ModItems.BERSERK_MODULE,2), card(ModItems.SANDEVISTAN,3),
            card(ModItems.MANTIS_ARMS,4), card(ModItems.GORILLA_ARMS,5),
            card(ModItems.KERENZIKOV,6), card(ModItems.SUBDERMAL_ARMOR,7),
            card(ModItems.BLOOD_PUMP,8), card(ModItems.PROJECTILE_LAUNCHER,9),
            card(ModItems.REINFORCED_TENDONS,10), card(ModItems.BIOMONITOR,11),
            card(ModItems.SMART_LINK,12), card(ModItems.MONOWIRE,13)
    };
    private static final PartCard[] PARTS = {
            new PartCard(ModItems.CYBERNETIC_ALLOY,"cybernetic_alloy"),
            new PartCard(ModItems.SERVO_MOTOR,"servo_motor"),
            new PartCard(ModItems.NEURAL_PROCESSOR,"neural_processor"),
            new PartCard(ModItems.SYNTHETIC_FIBER,"synthetic_fiber"),
            new PartCard(ModItems.POWER_CELL,"power_cell"),
            new PartCard(ModItems.NEURAL_CABLE,"neural_cable"),
            new PartCard(ModItems.OPTIC_LENS,"optic_lens"),
            new PartCard(ModItems.HYDRAULIC_ACTUATOR,"hydraulic_actuator"),
            new PartCard(ModItems.TITANIUM_PLATING,"titanium_plating"),
            new PartCard(ModItems.SYNTHETIC_MUSCLE,"synthetic_muscle"),
            new PartCard(ModItems.POWER_REGULATOR,"power_regulator"),
            new PartCard(ModItems.COMBAT_PROCESSOR,"combat_processor")
    };
    private static final ArmorCard[] ARMOR = {
            new ArmorCard(ModItems.CYBERWARE_HELMET,"cyberware_helmet",EquipmentSlot.HEAD,3,false),
            new ArmorCard(ModItems.CYBERWARE_CHESTPLATE,"cyberware_chestplate",EquipmentSlot.CHEST,8,false),
            new ArmorCard(ModItems.CYBERWARE_LEGGINGS,"cyberware_leggings",EquipmentSlot.LEGS,6,false),
            new ArmorCard(ModItems.CYBERWARE_BOOTS,"cyberware_boots",EquipmentSlot.FEET,3,false),
            new ArmorCard(ModItems.TACTICAL_HELMET,"tactical_helmet",EquipmentSlot.HEAD,4,true),
            new ArmorCard(ModItems.TACTICAL_CHESTPLATE,"tactical_chestplate",EquipmentSlot.CHEST,9,true),
            new ArmorCard(ModItems.TACTICAL_LEGGINGS,"tactical_leggings",EquipmentSlot.LEGS,7,true),
            new ArmorCard(ModItems.TACTICAL_BOOTS,"tactical_boots",EquipmentSlot.FEET,4,true)
    };

    private static CyberwareScreen activeScreen;
    private final Screen parent;
    private int activeTab;
    private int selectedImplant = 0;
    private int installedMask;
    private int neuralLoad;
    private int psychosisTicks;
    private boolean receivedState;

    public CyberwareScreen(Screen parent) {
        super(Text.translatable("gui.cyberpunk.title"));
        this.parent = parent;
    }

    private static ImplantCard card(Item item, int type) {
        return new ImplantCard(item, type, CyberwareManager.getImplantNameKey(type));
    }

    @Override
    protected void init() {
        activeScreen = this;
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cyberpunk.close"),
                        button -> close())
                .dimensions(panelX() + panelW() - 82, panelY() + 10, 70, 20).build());
        requestState();
    }

    private void requestState() {
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
        context.fill(0, 0, width, height, 0xD900050A);
        int x = panelX(), y = panelY(), w = panelW(), h = panelH();
        int right = x + w, bottom = y + h;
        context.fill(x - 2, y - 2, right + 2, bottom + 2, 0xFF02060B);
        context.fill(x, y, right, bottom, 0xF00B131D);
        context.fill(x, y, right, y + 3, 0xFF21DCE7);
        context.fill(x, bottom - 2, right, bottom, 0xFF126A83);
        context.fill(x, y, x + 2, bottom, 0xFF126A83);
        context.fill(right - 2, y, right, bottom, 0xFF126A83);

        context.drawText(textRenderer, Text.literal("CYBERPUNK // NEURAL INTERFACE"),
                x + 16, y + 10, 0xFFE7FBFF, false);
        context.drawText(textRenderer, Text.translatable("gui.cyberpunk.subtitle"),
                x + 16, y + 25, 0xFF70A7B9, false);
        int loadColor = neuralLoad >= 65 ? 0xFFFF5368 : neuralLoad >= 35 ? 0xFFFFC45B : 0xFF23D6C8;
        context.drawText(textRenderer, Text.translatable("gui.cyberpunk.load", neuralLoad),
                x + 16, y + 43, loadColor, false);
        int barX = x + 150, barW = Math.max(65, w - 315);
        context.fill(barX, y + 46, barX + barW, y + 52, 0xFF162B38);
        if (neuralLoad > 0) {
            context.fill(barX, y + 46, barX + Math.max(1, barW * neuralLoad / 100), y + 52, loadColor);
        }
        if (!receivedState) {
            context.drawText(textRenderer, Text.translatable("gui.cyberpunk.sync"),
                    barX + barW + 9, y + 43, 0xFF8BB7C7, false);
        } else if (psychosisTicks > 0) {
            context.drawText(textRenderer, Text.translatable("gui.cyberpunk.overload",
                    (psychosisTicks + 19) / 20), barX + barW + 9, y + 43, 0xFFFF5268, false);
        }
        drawTabs(context, mouseX, mouseY);

        if (activeTab == 0) drawImplants(context, mouseX, mouseY);
        else if (activeTab == 1) drawSockets(context);
        else if (activeTab == 2) drawParts(context);
        else drawArmor(context);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawTabs(DrawContext context, int mouseX, int mouseY) {
        int x = panelX() + 16, y = panelY() + 64, gap = 5;
        int tabWidth = (panelW() - 42) / 4;
        String[] keys = {"gui.cyberpunk.tab.implants", "gui.cyberpunk.tab.sockets",
                "gui.cyberpunk.tab.parts", "gui.cyberpunk.tab.armor"};
        for (int i = 0; i < 4; i++) {
            int left = x + i * (tabWidth + gap);
            boolean selected = activeTab == i;
            boolean hover = mouseX >= left && mouseX <= left + tabWidth
                    && mouseY >= y && mouseY <= y + 21;
            context.fill(left, y, left + tabWidth, y + 21,
                    selected ? 0xFF174354 : hover ? 0xFF1A2D39 : 0xFF111C27);
            context.fill(left, y, left + tabWidth, y + 1,
                    selected ? 0xFF21DCE7 : 0xFF334754);
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable(keys[i]),
                    left + tabWidth / 2, y + 7, selected ? 0xFFBDFBFF : 0xFF91A9B6);
        }
    }

    private void drawImplants(DrawContext context, int mouseX, int mouseY) {
        int x = panelX() + 16, top = panelY() + 96;
        int bottom = panelY() + panelH() - 58;
        int gap = 5, columns = 3, rows = 5;
        int cardWidth = (panelW() - 32 - gap * (columns - 1)) / columns;
        int rowHeight = Math.min(32, Math.max(16, (bottom - top - 12) / rows - 3));

        context.drawText(textRenderer, Text.translatable("gui.cyberpunk.catalog"),
                x, panelY() + 86, 0xFF8DDDE7, false);
        for (int i = 0; i < IMPLANTS.length; i++) {
            int col = i / rows, row = i % rows;
            int cx = x + col * (cardWidth + gap);
            int cy = top + row * (rowHeight + 3);
            boolean selected = selectedImplant == i;
            boolean installed = (installedMask & (1 << i)) != 0;
            context.fill(cx, cy, cx + cardWidth, cy + rowHeight,
                    selected ? 0xFF174354 : installed ? 0xFF112B2E : 0xFF101923);
            context.fill(cx, cy, cx + cardWidth, cy + 1,
                    selected ? 0xFF22DCE7 : installed ? 0xFF3BBD9A : 0xFF334754);
            context.drawItem(new ItemStack(IMPLANTS[i].item()), cx + 5, cy + (rowHeight - 16) / 2);
            int textX = cx + 25;
            String label = textRenderer.trimToWidth(Text.translatable(IMPLANTS[i].nameKey()).getString(),
                    cardWidth - 34);
            context.drawText(textRenderer, Text.literal(label), textX, cy + 4,
                    selected ? 0xFFE9FDFF : 0xFFD6E8EC, false);
            String state = installed ? Text.translatable("gui.cyberpunk.installed.short").getString()
                    : CyberpunkClient.getActivationKeyLabel(i);
            context.drawText(textRenderer, Text.literal(textRenderer.trimToWidth(state, cardWidth - 31)),
                    textX, cy + Math.min(17, rowHeight - 9), installed ? 0xFF40E2C7 : 0xFF82B7C6, false);
        }

        int infoY = bottom + 5;
        context.fill(x, infoY, panelX() + panelW() - 16, panelY() + panelH() - 18, 0xFF0A171F);
        int available = panelW() - 48;
        String description = Text.translatable("description.cyberpunk." + implantPath(selectedImplant)).getString();
        context.drawText(textRenderer, Text.literal(textRenderer.trimToWidth(description, available)),
                x + 6, infoY + 4, 0xFF9BAFB9, false);
        boolean installed = (installedMask & (1 << selectedImplant)) != 0;
        String action = installed ? "gui.cyberpunk.already.installed" : "gui.cyberpunk.install.selected";
        context.drawText(textRenderer, Text.translatable(action), x + 6, infoY + 17,
                installed ? 0xFF40E2C7 : 0xFF55DCCA, false);
        context.drawText(textRenderer, Text.translatable("gui.cyberpunk.slot_hint"),
                x + Math.min(180, available / 2), infoY + 17, 0xFF6E8C99, false);
    }

    private void drawSockets(DrawContext context) {
        int x = panelX() + 16, top = panelY() + 100;
        int bottom = panelY() + panelH() - 20;
        int slotGap = 3;
        int rowHeight = Math.min(34, Math.max(15, (bottom - top - slotGap * 6) / SLOT_ORDER.length));
        context.drawText(textRenderer, Text.translatable("gui.cyberpunk.slots"), x,
                panelY() + 86, 0xFF8DDDE7, false);
        String selectedName = Text.translatable(IMPLANTS[selectedImplant].nameKey()).getString();
        context.drawText(textRenderer, Text.translatable("gui.cyberpunk.selected_implant", selectedName),
                x + 170, panelY() + 86, 0xFF9BBBC5, false);

        for (int row = 0; row < SLOT_ORDER.length; row++) {
            int slot = SLOT_ORDER[row];
            int sy = top + row * (rowHeight + slotGap);
            int type = installedTypeForSlot(slot);
            boolean canInstall = type < 0 && CyberwareManager.getSlotForImplant(selectedImplant) == slot
                    && countItem(IMPLANTS[selectedImplant].item()) > 0;
            context.fill(x, sy, panelX() + panelW() - 16, sy + rowHeight,
                    type >= 0 ? 0xFF12272D : 0xFF101923);
            context.fill(x, sy, x + 3, sy + rowHeight,
                    type >= 0 ? 0xFF22D9C2 : canInstall ? 0xFFE8A54C : 0xFF314854);
            String slotLabel = Text.translatable(CyberwareManager.getSlotTranslationKey(slot)).getString();
            context.drawText(textRenderer, Text.literal(textRenderer.trimToWidth(slotLabel, Math.min(145, panelW() - 250))),
                    x + 10, sy + Math.max(2, (rowHeight - 9) / 2), 0xFFB7D1DA, false);
            String contents = type >= 0
                    ? Text.translatable(CyberwareManager.getImplantNameKey(type)).getString()
                    : Text.translatable("gui.cyberpunk.slot.empty").getString();
            context.drawText(textRenderer, Text.literal(textRenderer.trimToWidth(contents, Math.max(45, Math.min(190, panelW() - 267)))),
                    x + 165, sy + Math.max(2, (rowHeight - 9) / 2), type >= 0 ? 0xFF40E2C7 : 0xFF738D99, false);
            String action = type >= 0 ? Text.translatable("gui.cyberpunk.remove.short").getString()
                    : canInstall ? Text.translatable("gui.cyberpunk.install.short").getString()
                    : Text.translatable("gui.cyberpunk.unavailable.short").getString();
            int actionColor = type >= 0 ? 0xFFFF8190 : canInstall ? 0xFFE8A54C : 0xFF536873;
            context.drawText(textRenderer, Text.literal(action),
                    panelX() + panelW() - 86, sy + Math.max(2, (rowHeight - 9) / 2), actionColor, false);
        }
        context.drawText(textRenderer, Text.translatable("gui.cyberpunk.socket_instruction"),
                x, panelY() + panelH() - 14, 0xFF7896A3, false);
    }

    private int installedTypeForSlot(int slot) {
        for (int type = 0; type < IMPLANTS.length; type++) {
            if (CyberwareManager.getSlotForImplant(type) == slot && (installedMask & (1 << type)) != 0) {
                return type;
            }
        }
        return -1;
    }

    private static String implantPath(int type) {
        return switch (type) {
            case 0 -> "neural_accelerator";
            case 1 -> "optical_camo";
            case 2 -> "berserk";
            case 3 -> "sandevistan";
            case 4 -> "mantis_arms";
            case 5 -> "gorilla_arms";
            case 6 -> "kerenzikov";
            case 7 -> "subdermal_armor";
            case 8 -> "blood_pump";
            case 9 -> "projectile_launcher";
            case 10 -> "reinforced_tendons";
            case 11 -> "biomonitor";
            case 12 -> "smart_link";
            case 13 -> "monowire";
            default -> "neural_accelerator";
        };
    }

    private void drawParts(DrawContext context) {
        int x = panelX() + 16, top = panelY() + 98;
        int width = panelW() - 32;
        int gapX = 7, gapY = 7, cols = 3;
        int cardWidth = (width - gapX * (cols - 1)) / cols;
        int cardHeight = Math.min(66, Math.max(30, (panelH() - 114 - gapY * 3) / 4));
        for (int i = 0; i < PARTS.length; i++) {
            int cx = x + (i % cols) * (cardWidth + gapX);
            int cy = top + (i / cols) * (cardHeight + gapY);
            int count = countItem(PARTS[i].item());
            context.fill(cx, cy, cx + cardWidth, cy + cardHeight, 0xFF101923);
            context.fill(cx, cy, cx + cardWidth, cy + 1, 0xFF315061);
            context.drawItem(new ItemStack(PARTS[i].item()), cx + 7, cy + Math.max(2, (cardHeight - 16) / 2));
            String name = Text.translatable("item.cyberpunk." + PARTS[i].path()).getString();
            context.drawText(textRenderer, Text.literal(textRenderer.trimToWidth(name,
                    cardWidth - 34)), cx + 29, cy + 5, 0xFFE6F4F8, false);
            context.drawText(textRenderer, Text.translatable("gui.cyberpunk.in_inventory", count),
                    cx + 29, cy + 19, count > 0 ? 0xFF56DDBE : 0xFF8296A2, false);
            if (cardHeight >= 55) {
                String desc = Text.translatable("description.cyberpunk." + PARTS[i].path()).getString();
                context.drawText(textRenderer, Text.literal(textRenderer.trimToWidth(desc,
                        cardWidth - 12)), cx + 7, cy + 45, 0xFF9AAEB8, false);
            }
        }
    }

    private void drawArmor(DrawContext context) {
        int x = panelX() + 16, top = panelY() + 98;
        int width = panelW() - 32;
        int cols = 2, gapX = 8, gapY = 8;
        int cardWidth = (width - gapX) / 2;
        int cardHeight = Math.min(57, Math.max(30, (panelH() - 114 - gapY * 3) / 4));
        for (int i = 0; i < ARMOR.length; i++) {
            ArmorCard armor = ARMOR[i];
            int cx = x + (i % cols) * (cardWidth + gapX);
            int cy = top + (i / cols) * (cardHeight + gapY);
            boolean equipped = client != null && client.player != null
                    && client.player.getEquippedStack(armor.slot()).isOf(armor.item());
            int count = countItem(armor.item());
            context.fill(cx, cy, cx + cardWidth, cy + cardHeight,
                    equipped ? 0xFF192A27 : 0xFF101923);
            context.fill(cx, cy, cx + cardWidth, cy + 1,
                    armor.assault() ? 0xFFE8A54C : 0xFF23D9E0);
            context.drawItem(new ItemStack(armor.item()), cx + 8, cy + 8);
            String name = Text.translatable("item.cyberpunk." + armor.path()).getString();
            context.drawText(textRenderer, Text.literal(textRenderer.trimToWidth(name,
                    cardWidth - 40)), cx + 32, cy + 7, 0xFFE6F4F8, false);
            String setName = Text.translatable(armor.assault()
                    ? "gui.cyberpunk.armor.assault" : "gui.cyberpunk.armor.standard").getString();
            if (cardHeight >= 48) {
                context.drawText(textRenderer, Text.literal(setName), cx + 32, cy + 21,
                        armor.assault() ? 0xFFE8A54C : 0xFF50D7D2, false);
            }
            int detailY = cy + cardHeight - 13;
            context.drawText(textRenderer, Text.translatable("gui.cyberpunk.defense", armor.defense()),
                    cx + 8, detailY, 0xFFFFC66E, false);
            context.drawText(textRenderer, Text.translatable(equipped
                    ? "gui.cyberpunk.equipped" : "gui.cyberpunk.in_inventory", count),
                    cx + Math.min(100, cardWidth / 2), detailY,
                    equipped ? 0xFF5EE0B5 : 0xFF96AAB4, false);
        }
        if (cardHeight >= 52) {
            int bonusY = top + 4 * (cardHeight + gapY) + 3;
            context.drawText(textRenderer, Text.translatable("gui.cyberpunk.armor.setbonus"),
                    x, bonusY, 0xFF68D8D0, false);
            context.drawText(textRenderer, Text.translatable("gui.cyberpunk.armor.bonus.standard"),
                    x, bonusY + 12, 0xFF50D7D2, false);
            context.drawText(textRenderer, Text.translatable("gui.cyberpunk.armor.bonus.assault"),
                    x, bonusY + 24, 0xFFE8A54C, false);
        }
    }

    private int countItem(Item item) {
        if (client == null || client.player == null) return 0;
        int count = 0;
        for (int i = 0; i < client.player.getInventory().size(); i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.isOf(item)) count += stack.getCount();
        }
        return count;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int tx = panelX() + 16, ty = panelY() + 64, gap = 5;
            int tabWidth = (panelW() - 42) / 4;
            for (int i = 0; i < 4; i++) {
                int left = tx + i * (tabWidth + gap);
                if (mouseX >= left && mouseX <= left + tabWidth && mouseY >= ty && mouseY <= ty + 21) {
                    activeTab = i;
                    return true;
                }
            }

            if (activeTab == 0) {
                int x = panelX() + 16, top = panelY() + 96;
                int gapX = 5, columns = 3, rows = 5;
                int cardWidth = (panelW() - 32 - gapX * (columns - 1)) / columns;
                int bottom = panelY() + panelH() - 58;
                int rowHeight = Math.min(32, Math.max(16, (bottom - top - 12) / rows - 3));
                for (int i = 0; i < IMPLANTS.length; i++) {
                    int col = i / rows, row = i % rows;
                    int cx = x + col * (cardWidth + gapX);
                    int cy = top + row * (rowHeight + 3);
                    if (mouseX >= cx && mouseX <= cx + cardWidth
                            && mouseY >= cy && mouseY <= cy + rowHeight) {
                        selectedImplant = i;
                        return true;
                    }
                }
                int infoY = bottom + 5;
                if (mouseX >= x && mouseX <= panelX() + panelW() - 16
                        && mouseY >= infoY && mouseY <= panelY() + panelH() - 18) {
                    if ((installedMask & (1 << selectedImplant)) == 0
                            && client != null && client.getNetworkHandler() != null) {
                        ClientPlayNetworking.send(new CyberwareActionPayload(1, selectedImplant));
                    }
                    return true;
                }
            } else if (activeTab == 1) {
                int x = panelX() + 16, top = panelY() + 100;
                int bottom = panelY() + panelH() - 20, socketGap = 3;
                int rowHeight = Math.min(34, Math.max(15, (bottom - top - socketGap * 6) / SLOT_ORDER.length));
                for (int row = 0; row < SLOT_ORDER.length; row++) {
                    int sy = top + row * (rowHeight + socketGap);
                    if (mouseX < x || mouseX > panelX() + panelW() - 16
                            || mouseY < sy || mouseY > sy + rowHeight) continue;
                    int slot = SLOT_ORDER[row];
                    int installed = installedTypeForSlot(slot);
                    if (installed >= 0) {
                        ClientPlayNetworking.send(new CyberwareActionPayload(2, slot));
                    } else if (CyberwareManager.getSlotForImplant(selectedImplant) == slot
                            && countItem(IMPLANTS[selectedImplant].item()) > 0
                            && client != null && client.getNetworkHandler() != null) {
                        ClientPlayNetworking.send(new CyberwareActionPayload(1, selectedImplant));
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

    private int panelW() { return Math.min(650, Math.max(360, width - 12)); }
    private int panelH() { return Math.min(410, Math.max(260, height - 12)); }
    private int panelX() { return (width - panelW()) / 2; }
    private int panelY() { return (height - panelH()) / 2; }

    private record ImplantCard(Item item, int type, String nameKey) { }
    private record PartCard(Item item, String path) { }
    private record ArmorCard(Item item, String path, EquipmentSlot slot, int defense, boolean assault) { }
}
