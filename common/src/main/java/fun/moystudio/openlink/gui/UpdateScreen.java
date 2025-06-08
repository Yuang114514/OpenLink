package fun.moystudio.openlink.gui;

import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpcimpl.FrpcManager;
import fun.moystudio.openlink.logic.EventCallbacks;
import fun.moystudio.openlink.logic.Utils;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

public class UpdateScreen extends Screen {
    public UpdateScreen() {
        super(Utils.translatableText("gui.openlink.updatefrpctitle"));
    }
    public Button yes;
    public Button no;
    public MultiLineLabel text;

    @Override
    protected void init(){
        yes=Button.builder(CommonComponents.GUI_YES,button -> this.minecraft.setScreen(new UpdatingScreen())).bounds(this.width/4-40,this.height/5*4-10,80,20).build();
        no=Button.builder(CommonComponents.GUI_NO,button -> {
            if(!FrpcManager.getInstance().isExecutableFileExist(FrpcManager.getInstance().getCurrentFrpcId())){
                OpenLink.disabled=true;
            }
            EventCallbacks.hasUpdate=false;
            this.onClose();
        }).tooltip(getTooltip()).bounds(this.width/4*3-40,this.height/5*4-10,80,20).build();
        text=MultiLineLabel.create(this.font, Utils.translatableText("text.openlink.updatefrpc"),this.width-50);
        this.addRenderableWidget(yes);
        this.addRenderableWidget(no);
        this.addRenderableWidget(Button.builder(Utils.translatableText("text.openlink.openstoragedir"), button -> {
            Util.getPlatform().openFile(FrpcManager.getInstance().getFrpcStoragePathById(FrpcManager.getInstance().getCurrentFrpcId()).toFile());
        }).tooltip(getTooltip()).bounds(this.width/2-60, this.height/5*4-10, 120, 20).build());
        //以下为原版语言按钮(修改了一下位置)
        this.addRenderableWidget(new ImageButton(this.width/4-70, this.height/5*4-10, 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, (button) -> this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())), Utils.translatableText("narrator.button.language")));
    }

    private Tooltip getTooltip(){
        if(!FrpcManager.getInstance().isExecutableFileExist(FrpcManager.getInstance().getCurrentFrpcId())) return Tooltip.create(Utils.translatableText("text.openlink.nofrpcfile"));
        return Tooltip.create(Utils.emptyText());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderBackground(guiGraphics);
        text.renderCentered(guiGraphics,this.width/2,this.height/10,16,0xffffff);
        super.render(guiGraphics,i,j,f);
    }

    @Override
    public void onClose(){
        this.minecraft.setScreen(null);
    }
}
