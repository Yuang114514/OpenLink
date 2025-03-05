package fun.moystudio.openlink.gui;

import fun.moystudio.openlink.OpenLink;
import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.logic.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;

import java.util.Arrays;
import java.util.List;

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
        no=Button.builder(CommonComponents.GUI_NO,button -> {Frpc.hasUpdate=false;this.onClose();}).tooltip(getTooltip()).bounds(this.width/4*3-40,this.height/5*4-10,80,20).build();
        if(Frpc.FRPC_VERSION.length()<6){
            no.active=false;
        }
        text=MultiLineLabel.create(this.font, Utils.translatableText("text.openlink.updatefrpc", Frpc.latestVersion, Frpc.FRPC_VERSION.length()<6 ? "does not exist" : Frpc.FRPC_VERSION),this.width-50);
        this.addRenderableWidget(yes);
        this.addRenderableWidget(no);
        this.addRenderableWidget(Button.builder(Utils.translatableText("text.openlink.openstoragedir"), button -> {
            try{
                if(Frpc.osName.equals("windows")){
                    Runtime.getRuntime().exec(new String[]{"explorer", "/root,"+OpenLink.EXECUTABLE_FILE_STORAGE_PATH});
                } else if (Frpc.osName.equals("darwin")) {
                    Runtime.getRuntime().exec(new String[]{"open", OpenLink.EXECUTABLE_FILE_STORAGE_PATH});
                } else {
                    Runtime.getRuntime().exec(new String[]{"xdg-open", OpenLink.EXECUTABLE_FILE_STORAGE_PATH});
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).bounds(this.width/2-60, this.height/5*4-10, 120, 20).build());
        //以下为原版语言按钮(修改了一下位置)
        this.addRenderableWidget(new ImageButton(this.width/4-70, this.height/5*4-10, 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, (button) -> this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())), Utils.translatableText("narrator.button.language")));
    }

    private Tooltip getTooltip(){
        List<String> strings=Arrays.asList(Utils.translatableText("text.openlink.nofrpcfile").getString().split("\n"));
        MutableComponent component=Utils.literalText("");
        if(component.getSiblings().isEmpty()||component.getSiblings().get(0).equals(Utils.literalText("text.openlink.nofrpcfile"))){
            strings.forEach((String)-> component.append(Utils.literalText(String)));
            if(Frpc.FRPC_VERSION.length()<6){
                return Tooltip.create(component);
            }
        }
        return Tooltip.create(Utils.emptyText());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderBackground(guiGraphics);
        text.renderCentered(guiGraphics,this.width/2,this.height/10,16,0xffffff);
        super.render(guiGraphics,i,j,f);
    }
}
