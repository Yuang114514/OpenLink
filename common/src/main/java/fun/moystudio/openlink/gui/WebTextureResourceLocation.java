package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.platform.NativeImage;
import fun.moystudio.openlink.OpenLink;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.net.URL;

public class WebTextureResourceLocation {
    public String url;
    public ResourceLocation location;
    public WebTextureResourceLocation(String url){
        this.url=url;
        this.load();
    }
    public void load(){
        try{
            URL url1=new URL(url);
            HttpsURLConnection connection=(HttpsURLConnection) url1.openConnection();
            InputStream stream=connection.getInputStream();
            NativeImage image=NativeImage.read(stream);
            location=Minecraft.getInstance().getTextureManager().register("avatar",new SelfCleaningDynamicTexture(image));
            stream.close();
        } catch (Exception e){
            e.printStackTrace();
            OpenLink.LOGGER.error("Error on loading avatar web texture!");
        }
    }
}
