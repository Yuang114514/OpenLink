package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.platform.NativeImage;
import fun.moystudio.openlink.OpenLink;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebTextureResourceLocation {
    public String url;
    public ResourceLocation location;
    public byte[] data = null;

    public WebTextureResourceLocation(String url, ResourceLocation def) {
        this.url = url;
        this.location = def;
        this.load();
    }

    public void load() {
        try {
            URL url1 = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) url1.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");
            InputStream stream = connection.getInputStream();
            BufferedImage bufferedImage = ImageIO.read(stream);
            System.out.println(bufferedImage);
            ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", byteArrayOut);
            data = byteArrayOut.toByteArray();
            byteArrayOut.close();
        } catch (Exception e) {
            OpenLink.LOGGER.error("", e);
            OpenLink.LOGGER.error("Error on loading avatar web texture.");
        }
    }

    public void read() {
        if (this.data == null) {
            OpenLink.LOGGER.error("{} cannot be read before loading!", this);
            return;
        }
        NativeImage image = null;
        try {
            image = NativeImage.read(new ByteArrayInputStream(data));
        } catch (Exception e) {
            OpenLink.LOGGER.error("", e);
            OpenLink.LOGGER.error("Error on reading avatar web texture.");
        }
        if (image == null) return;
        location = Minecraft.getInstance().getTextureManager().register("avatar", new SelfCleaningDynamicTexture(image));
    }
}
