package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import fun.moystudio.openlink.OpenLink;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;

import java.util.List;

//是哪个大聪明想出来让我写折线统计图的？？？给我滚出来（zirran不要删注释后面调试用，这玩意真的难写）
public class LineChartWidget extends GuiComponent implements Widget{
    public List<Integer> dataPoints;
    //左上——右下
    public int x1,y1,x2,y2,width,height;
    public Font font;
    public Component labelX,labelY;

    public LineChartWidget(Font font, int x1, int y1, int x2, int y2, Component labelX,Component labelY, List<Integer> dataPoints){
        this.font=font;
        this.dataPoints=dataPoints;
        this.labelX=labelX;
        this.labelY=labelY;
        this.x1=x1;
        this.y1=y1;
        this.x2=x2;
        this.y2=y2;
        this.width=x2-x1+1;
        this.height=y2-y1+1;
    }

    private void drawLine(PoseStack poseStack, int x1, int y1, int x2, int y2, long color) {
        BufferBuilder bufferBuilder=Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        float g = (float)(color >> 16 & 255) / 255.0F;
        float h = (float)(color >> 8 & 255) / 255.0F;
        float o = (float)(color & 255) / 255.0F;
        float f = (float)(color >> 24 & 255) / 255.0F;
        bufferBuilder.begin(VertexFormat.Mode.LINES,DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(poseStack.last().pose(),(float)x1,(float)y1,0.0F).color(g,h,o,f).endVertex();
        bufferBuilder.vertex(poseStack.last().pose(),(float)x2,(float)y2,0.0F).color(g,h,o,f).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }


    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        RenderSystem.enableBlend();
        //左下——右上
        int beginX=x1,beginY=y2,endX=x2,endY=y1;
        hLine(poseStack,beginX,endX,beginY,0xffffffff);//x轴
        vLine(poseStack,beginX,beginY,endY,0xffffffff);//y轴
        drawCenteredString(poseStack,font,labelX,x2-10,y2+5,0xffffff);//x轴标签
        drawCenteredString(poseStack,font,labelY,x1,y1-5,0xffffff);//y轴标签
        int maxDataVal=dataPoints.stream().max(Integer::compareTo).orElse(1);//最大值
        float xScale=(float)(width-5)/(dataPoints.size() - 1);//x轴数据放大倍数（留了5的空间）
        float yScale=(float)(height-5)/maxDataVal;//y轴数据放大倍数（留了5的空间）
        for(int k=0;k<dataPoints.size()-1;k++){//size-1条线（打OI打的：越界会RE的）
            int x1=(int)(beginX+5+k*xScale);
            int y1=(int)(beginY-dataPoints.get(k)*yScale);
            int x2=(int)(beginX+5+(k+1)*xScale);
            int y2=(int)(beginY-dataPoints.get(k+1)*yScale);
            drawLine(poseStack,x1,y1,x2,y2, 0xFFFFFFFFL);//颜色后面再改，先用着白色
        }
        for (int k=0;k<dataPoints.size();k++) {//size个点
            int pointX=(int)(beginX+5+k*xScale);
            int pointY=(int)(beginY-dataPoints.get(k)*yScale);
            fill(poseStack,pointX-2,pointY-2,pointX+2,pointY+2,0xffff0000);//颜色后面再改，先用着红色
        }
        //x轴和y轴数据先搁着
    }
}
