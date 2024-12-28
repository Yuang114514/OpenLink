package fun.moystudio.openlink.forge;

import com.mojang.brigadier.CommandDispatcher;
import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.logic.EventCallbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import fun.moystudio.openlink.OpenLink;
import net.minecraftforge.versions.forge.ForgeVersion;

@Mod.EventBusSubscriber
@Mod(OpenLink.MOD_ID)
public final class OpenLinkForge {
    public OpenLinkForge() throws Exception {
        // Run our common setup.
        OpenLink.init(ModList.get().getModFileById(OpenLink.MOD_ID).versionString(),"Forge", ForgeVersion.getVersion());
    }

    @SubscribeEvent
    public static void onClientScreenInit(ScreenEvent.InitScreenEvent event){
        EventCallbacks.onScreenInit(event.getScreen().getMinecraft(), event.getScreen());
    }

    @SubscribeEvent
    public static void onClientCommandRegistering(RegisterClientCommandsEvent event){
        CommandDispatcher<CommandSourceStack> dispatcher=event.getDispatcher();
        dispatcher.register(Commands.literal("proxyrestart")
                .executes(context -> Frpc.openFrp(Minecraft.getInstance().getSingleplayerServer().getPort(),"")?1:0));
    }
}
