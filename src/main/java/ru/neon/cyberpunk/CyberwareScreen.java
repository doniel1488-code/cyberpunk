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
    private static final int MAX_W=510, MAX_H=358, TAB_COUNT=3;
    private static final ImplantCard[] IMPLANTS={
        new ImplantCard(ModItems.NEURAL_ACCELERATOR,"item.cyberpunk.neural_accelerator","description.cyberpunk.neural_accelerator",0),
        new ImplantCard(ModItems.OPTICAL_CAMO,"item.cyberpunk.optical_camo","description.cyberpunk.optical_camo",1),
        new ImplantCard(ModItems.BERSERK_MODULE,"item.cyberpunk.berserk_module","description.cyberpunk.berserk",2),
        new ImplantCard(ModItems.SANDEVISTAN,"item.cyberpunk.sandevistan","description.cyberpunk.sandevistan",3),
        new ImplantCard(ModItems.MANTIS_ARMS,"item.cyberpunk.mantis_arms","description.cyberpunk.mantis_arms",4),
        new ImplantCard(ModItems.GORILLA_ARMS,"item.cyberpunk.gorilla_arms","description.cyberpunk.gorilla_arms",5),
        new ImplantCard(ModItems.KERENZIKOV,"item.cyberpunk.kerenzikov","description.cyberpunk.kerenzikov",6),
        new ImplantCard(ModItems.SUBDERMAL_ARMOR,"item.cyberpunk.subdermal_armor","description.cyberpunk.subdermal_armor",7),
        new ImplantCard(ModItems.BLOOD_PUMP,"item.cyberpunk.blood_pump","description.cyberpunk.blood_pump",8)
    };
    private static final PartCard[] PARTS={
        new PartCard(ModItems.CYBERNETIC_ALLOY,"item.cyberpunk.cybernetic_alloy","description.cyberpunk.cybernetic_alloy"),
        new PartCard(ModItems.SERVO_MOTOR,"item.cyberpunk.servo_motor","description.cyberpunk.servo_motor"),
        new PartCard(ModItems.NEURAL_PROCESSOR,"item.cyberpunk.neural_processor","description.cyberpunk.neural_processor"),
        new PartCard(ModItems.SYNTHETIC_FIBER,"item.cyberpunk.synthetic_fiber","description.cyberpunk.synthetic_fiber"),
        new PartCard(ModItems.POWER_CELL,"item.cyberpunk.power_cell","description.cyberpunk.power_cell"),
        new PartCard(ModItems.NEURAL_CABLE,"item.cyberpunk.neural_cable","description.cyberpunk.neural_cable"),
        new PartCard(ModItems.OPTIC_LENS,"item.cyberpunk.optic_lens","description.cyberpunk.optic_lens"),
        new PartCard(ModItems.HYDRAULIC_ACTUATOR,"item.cyberpunk.hydraulic_actuator","description.cyberpunk.hydraulic_actuator"),
        new PartCard(ModItems.TITANIUM_PLATING,"item.cyberpunk.titanium_plating","description.cyberpunk.titanium_plating"),
        new PartCard(ModItems.SYNTHETIC_MUSCLE,"item.cyberpunk.synthetic_muscle","description.cyberpunk.synthetic_muscle"),
        new PartCard(ModItems.POWER_REGULATOR,"item.cyberpunk.power_regulator","description.cyberpunk.power_regulator"),
        new PartCard(ModItems.COMBAT_PROCESSOR,"item.cyberpunk.combat_processor","description.cyberpunk.combat_processor")
    };
    private static final ArmorCard[] ARMOR={
        new ArmorCard(ModItems.CYBERWARE_HELMET,"item.cyberpunk.cyberware_helmet",EquipmentSlot.HEAD,3,"gui.cyberpunk.armor.standard"),
        new ArmorCard(ModItems.CYBERWARE_CHESTPLATE,"item.cyberpunk.cyberware_chestplate",EquipmentSlot.CHEST,8,"gui.cyberpunk.armor.standard"),
        new ArmorCard(ModItems.CYBERWARE_LEGGINGS,"item.cyberpunk.cyberware_leggings",EquipmentSlot.LEGS,6,"gui.cyberpunk.armor.standard"),
        new ArmorCard(ModItems.CYBERWARE_BOOTS,"item.cyberpunk.cyberware_boots",EquipmentSlot.FEET,3,"gui.cyberpunk.armor.standard"),
        new ArmorCard(ModItems.TACTICAL_HELMET,"item.cyberpunk.tactical_helmet",EquipmentSlot.HEAD,3,"gui.cyberpunk.armor.assault"),
        new ArmorCard(ModItems.TACTICAL_CHESTPLATE,"item.cyberpunk.tactical_chestplate",EquipmentSlot.CHEST,8,"gui.cyberpunk.armor.assault"),
        new ArmorCard(ModItems.TACTICAL_LEGGINGS,"item.cyberpunk.tactical_leggings",EquipmentSlot.LEGS,6,"gui.cyberpunk.armor.assault"),
        new ArmorCard(ModItems.TACTICAL_BOOTS,"item.cyberpunk.tactical_boots",EquipmentSlot.FEET,3,"gui.cyberpunk.armor.assault")
    };

    private static CyberwareScreen activeScreen;
    private final Screen parent;
    private int activeTab, installedMask, neuralLoad, psychosisTicks;
    private boolean receivedState;

    public CyberwareScreen(Screen parent) {
        super(Text.translatable("gui.cyberpunk.title"));
        this.parent=parent;
    }

    @Override protected void init() {
        activeScreen=this;
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cyberpunk.close"),b->client.setScreen(parent))
            .dimensions(panelX()+panelW()-78,panelY()+9,66,18).build());
        if(client!=null&&client.getNetworkHandler()!=null)
            ClientPlayNetworking.send(new CyberwareActionPayload(0,-1));
    }

    public static void acceptState(CyberwareStatePayload p) {
        if(activeScreen==null)return;
        activeScreen.installedMask=p.installedMask();
        activeScreen.neuralLoad=Math.max(0,Math.min(100,p.neuralLoad()));
        activeScreen.psychosisTicks=Math.max(0,p.psychosisTicks());
        activeScreen.receivedState=true;
    }

    @Override public void render(DrawContext c,int mouseX,int mouseY,float delta) {
        c.fill(0,0,width,height,0xC800050A);
        int x=panelX(),y=panelY(),w=panelW(),h=panelH(),right=x+w,bottom=y+h;
        c.fill(x-2,y-2,right+2,bottom+2,0xFF02070D);
        c.fill(x,y,right,bottom,0xF00B131D);
        c.fill(x,y,right,y+3,0xFF19D8E6);
        c.fill(x,bottom-2,right,bottom,0xFF126A83);
        c.fill(x,y,x+2,bottom,0xFF126A83);
        c.fill(right-2,y,right,bottom,0xFF126A83);

        c.drawText(textRenderer,Text.literal("CYBERPUNK // NEURAL INTERFACE"),x+16,y+10,0xFFE7FBFF,false);
        c.drawText(textRenderer,Text.translatable("gui.cyberpunk.subtitle"),x+16,y+24,0xFF70A7B9,false);
        int loadColor=neuralLoad>=65?0xFFFF5368:neuralLoad>=35?0xFFFFC45B:0xFF23D6C8;
        c.drawText(textRenderer,Text.translatable("gui.cyberpunk.load",neuralLoad),x+16,y+42,loadColor,false);
        int bx=x+126, bw=Math.max(70,w-252);
        c.fill(bx,y+45,bx+bw,y+51,0xFF162B38);
        if(neuralLoad>0)c.fill(bx,y+45,bx+Math.max(1,bw*neuralLoad/100),y+51,loadColor);
        if(!receivedState)c.drawText(textRenderer,Text.translatable("gui.cyberpunk.sync"),bx+bw+10,y+42,0xFF8BB7C7,false);
        else if(psychosisTicks>0)c.drawText(textRenderer,Text.translatable("gui.cyberpunk.overload",(psychosisTicks+19)/20),bx+bw+10,y+42,0xFFFF5268,false);

        drawTabs(c,mouseX,mouseY);
        int top=y+88, available=Math.max(1,h-118);
        if(activeTab==0)drawImplants(c,x,top,w,available,mouseX,mouseY);
        else if(activeTab==1)drawParts(c,x,top,w,available);
        else drawArmor(c,x,top,w,available);
        c.drawText(textRenderer,Text.translatable("gui.cyberpunk.footer"),x+16,bottom-18,0xFF789BAA,false);
        super.render(c,mouseX,mouseY,delta);
    }

    private void drawTabs(DrawContext c,int mx,int my) {
        int x=panelX()+16,y=panelY()+60,tw=(panelW()-44)/3;
        String[] labels={"gui.cyberpunk.tab.implants","gui.cyberpunk.tab.parts","gui.cyberpunk.tab.armor"};
        for(int i=0;i<3;i++){
            int left=x+i*(tw+6); boolean selected=activeTab==i,hover=mx>=left&&mx<=left+tw&&my>=y&&my<=y+20;
            c.fill(left,y,left+tw,y+20,selected?0xFF174354:hover?0xFF1A2D39:0xFF111C27);
            c.fill(left,y,left+tw,y+1,selected?0xFF21DCE7:0xFF334754);
            c.drawCenteredTextWithShadow(textRenderer,Text.translatable(labels[i]),left+tw/2,y+6,selected?0xFFBDFBFF:0xFF91A9B6);
        }
    }

    private void drawImplants(DrawContext c,int px,int top,int pw,int ah,int mx,int my) {
        int gapX=8,gapY=6,cw=(pw-48)/3,ch=Math.max(28,(ah-12)/3);
        for(int i=0;i<IMPLANTS.length;i++){
            int x=px+16+(i%3)*(cw+gapX),y=top+(i/3)*(ch+gapY),r=x+cw,b=y+ch;
            boolean installed=(installedMask&(1<<i))!=0,compact=ch<56;
            boolean hover=mx>=x+6&&mx<=r-6&&my>=b-18&&my<=b-3;
            c.fill(x,y,r,b,installed?0xFF102832:0xFF101923);
            c.fill(x,y,r,y+1,installed?0xFF20D9D2:0xFF31414D);
            c.drawItem(new ItemStack(IMPLANTS[i].item()),x+7,y+6);
            c.drawText(textRenderer,Text.literal(textRenderer.trimToWidth(Text.translatable(IMPLANTS[i].nameKey()).getString(),cw-39)),x+29,y+7,0xFFE6F4F8,false);
            c.drawText(textRenderer,Text.literal("["+CyberpunkClient.getActivationKeyLabel(IMPLANTS[i].keyIndex())+"]"),x+29,y+19,0xFF82B7C6,false);
            if(!compact)c.drawText(textRenderer,Text.literal(textRenderer.trimToWidth(Text.translatable(IMPLANTS[i].descriptionKey()).getString(),cw-14)),x+7,y+36,0xFF9AAEB8,false);
            c.drawText(textRenderer,Text.translatable(installed?"message.cyberpunk.status_installed":"message.cyberpunk.status_missing"),x+7,b-29,installed?0xFF44E0B7:0xFF8395A2,false);
            c.fill(x+6,b-18,r-6,b-3,hover?(installed?0xFF8A3547:0xFF1E6C82):(installed?0xFF632A38:0xFF174A5B));
            c.drawCenteredTextWithShadow(textRenderer,Text.translatable(installed?"gui.cyberpunk.remove":"gui.cyberpunk.install"),x+cw/2,b-16,0xFFFFFFFF);
        }
    }

    private void drawParts(DrawContext c,int px,int top,int pw,int ah) {
        int cols=4,gx=6,gy=6,cw=(pw-32-gx*3)/4,ch=Math.max(28,(ah-gy*2)/3);
        for(int i=0;i<PARTS.length;i++){
            int x=px+16+(i%cols)*(cw+gx),y=top+(i/cols)*(ch+gy),r=x+cw,b=y+ch;
            int count=countItem(PARTS[i].item());
            c.fill(x,y,r,b,0xFF101923); c.fill(x,y,r,y+1,0xFF315061);
            c.drawItem(new ItemStack(PARTS[i].item()),x+7,y+7);
            c.drawText(textRenderer,Text.literal(textRenderer.trimToWidth(Text.translatable(PARTS[i].nameKey()).getString(),cw-34)),x+29,y+7,0xFFE6F4F8,false);
            c.drawText(textRenderer,Text.translatable("gui.cyberpunk.in_inventory",count),x+29,y+20,count>0?0xFF56DDBE:0xFF8296A2,false);
            if(ch>=58)c.drawText(textRenderer,Text.literal(textRenderer.trimToWidth(Text.translatable(PARTS[i].descriptionKey()).getString(),cw-14)),x+7,y+43,0xFF9AAEB8,false);
        }
    }

    private void drawArmor(DrawContext c,int px,int top,int pw,int ah) {
        int cols=4,gx=6,gy=8,cw=(pw-32-gx*3)/4,ch=Math.max(48,(ah-gy)/2);
        for(int i=0;i<ARMOR.length;i++){
            int x=px+16+(i%cols)*(cw+gx),y=top+(i/cols)*(ch+gy),r=x+cw,b=y+ch;
            boolean equipped=client!=null&&client.player!=null&&client.player.getEquippedStack(ARMOR[i].slot()).isOf(ARMOR[i].item());
            int count=countItem(ARMOR[i].item());
            c.fill(x,y,r,b,equipped?0xFF192A27:0xFF101923);c.fill(x,y,r,y+1,equipped?0xFF51D5A4:0xFF315061);
            c.drawItem(new ItemStack(ARMOR[i].item()),x+7,y+8);
            c.drawText(textRenderer,Text.literal(textRenderer.trimToWidth(Text.translatable(ARMOR[i].nameKey()).getString(),cw-34)),x+29,y+8,0xFFE6F4F8,false);
            c.drawText(textRenderer,Text.translatable(ARMOR[i].setKey()),x+29,y+22,0xFF8FAEBC,false);
            c.drawText(textRenderer,Text.translatable("gui.cyberpunk.defense",ARMOR[i].defense()),x+7,y+43,0xFFFFC66E,false);
            if(ch>=78)c.drawText(textRenderer,Text.translatable(equipped?"gui.cyberpunk.equipped":"gui.cyberpunk.in_inventory",count),x+7,y+57,equipped?0xFF5EE0B5:0xFF96AAB4,false);
        }
    }

    private int countItem(Item item){
        if(client==null||client.player==null)return 0;
        int count=0;
        for(int i=0;i<client.player.getInventory().size();i++){
            ItemStack s=client.player.getInventory().getStack(i);
            if(s.isOf(item))count+=s.getCount();
        }
        return count;
    }

    @Override public boolean mouseClicked(double mx,double my,int button){
        if(button==0){
            int tx=panelX()+16,ty=panelY()+60,tw=(panelW()-44)/3;
            for(int i=0;i<3;i++){
                int left=tx+i*(tw+6);
                if(mx>=left&&mx<=left+tw&&my>=ty&&my<=ty+20){activeTab=i;return true;}
            }
            if(activeTab==0){
                int top=panelY()+88,ah=Math.max(1,panelH()-118),gapX=8,gapY=6,cw=(panelW()-48)/3,ch=Math.max(28,(ah-12)/3);
                for(int i=0;i<IMPLANTS.length;i++){
                    int x=panelX()+16+(i%3)*(cw+gapX),y=top+(i/3)*(ch+gapY);
                    if(mx>=x+6&&mx<=x+cw-6&&my>=y+ch-18&&my<=y+ch-3){
                        if(client!=null&&client.getNetworkHandler()!=null)ClientPlayNetworking.send(new CyberwareActionPayload(1,i));
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mx,my,button);
    }

    @Override public void close(){activeScreen=null;if(client!=null)client.setScreen(parent);}
    @Override public void removed(){if(activeScreen==this)activeScreen=null;super.removed();}
    @Override public boolean shouldPause(){return false;}
    private int panelW(){return Math.min(MAX_W,Math.max(280,width-12));}
    private int panelH(){return Math.min(MAX_H,Math.max(210,height-12));}
    private int panelX(){return(width-panelW())/2;}
    private int panelY(){return(height-panelH())/2;}

    private record ImplantCard(Item item,String nameKey,String descriptionKey,int keyIndex){}
    private record PartCard(Item item,String nameKey,String descriptionKey){}
    private record ArmorCard(Item item,String nameKey,EquipmentSlot slot,int defense,String setKey){}
}
