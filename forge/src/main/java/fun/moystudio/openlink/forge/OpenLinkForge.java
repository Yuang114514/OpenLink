package fun.moystudio.openlink.forge;

import com.mojang.brigadier.CommandDispatcher;
import fun.moystudio.openlink.frpc.Frpc;
import fun.moystudio.openlink.logic.EventCallbacks;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
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
        OpenLink.init(ModList.get().getModContainerById(OpenLink.MOD_ID).get().getModInfo().getVersion().getQualifier(),"Forge", ForgeVersion.getVersion());
    }

    @SubscribeEvent
    public static void onClientScreenInit(GuiScreenEvent.InitGuiEvent event){
        EventCallbacks.onScreenInit(event.getGui().getMinecraft(), event.getGui());
    }

    @SubscribeEvent
    public static void onClientCommandRegistering(RegisterCommandsEvent event){
        event.getDispatcher().register(Commands.literal("proxyrestart")
                .executes(context -> Frpc.openFrp(Minecraft.getInstance().getSingleplayerServer().getPort(),"")?1:0));
    }

    @SubscribeEvent
    public static void onLevelClear(WorldEvent.Unload event){
        EventCallbacks.onLevelClear();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event){
        EventCallbacks.onClientTick(Minecraft.getInstance());
    }
}
