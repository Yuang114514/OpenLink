package fun.moystudio.openlink.forge;

import com.mojang.brigadier.CommandDispatcher;
import fun.moystudio.openlink.frpc.Frpc;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import fun.moystudio.openlink.OpenLink;

@Mod.EventBusSubscriber
@Mod(OpenLink.MOD_ID)
public final class OpenLinkForge {
    public OpenLinkForge() throws Exception {
        // Run our common setup.
        OpenLink.init();
    }
    @SubscribeEvent
    public static void onClientCommandRegistering(RegisterClientCommandsEvent event){
        CommandDispatcher<CommandSourceStack> dispatcher=event.getDispatcher();
        dispatcher.register(Commands.literal("proxyrestart")
                .executes(context -> Frpc.openFrp(Minecraft.getInstance().getSingleplayerServer().getPort(),"")?1:0));
    }
}
