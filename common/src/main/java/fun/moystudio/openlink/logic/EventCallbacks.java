package fun.moystudio.openlink.logic;

import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.gui.NewShareToLanScreen;
import fun.moystudio.openlink.mixin.IShareToLanLastScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;

public class EventCallbacks {
    public static void onScreenInit(Minecraft minecraft, Screen screen){
        if(screen instanceof ShareToLanScreen shareToLanScreen){
            minecraft.setScreen(new NewShareToLanScreen(((IShareToLanLastScreenAccessor)shareToLanScreen).getLastScreen()));
        }
    }
    public static void onClientStop(){
        Frpc.stopFrpc();
    }

}
