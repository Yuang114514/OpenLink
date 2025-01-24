package fun.moystudio.openlink.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import fun.moystudio.openlink.json.JsonNewProxy;
import fun.moystudio.openlink.json.JsonNode;
import fun.moystudio.openlink.logic.Utils;
import fun.moystudio.openlink.network.Request;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NodeSelectionScreen extends Screen {
    Screen lastscreen;
    NodeSelectionList selectionList = null;
    boolean isGettingNodeList=false, haveGotNodeList=false;
    Button done;
    public NodeSelectionScreen(Screen lastscreen) {
        super(Utils.translatableText("gui.openlink.nodeselectionscreentitle"));
        this.lastscreen=lastscreen;
    }

    @Override
    public void onClose(){
        this.minecraft.setScreen(lastscreen);
    }

    @Override
    protected void init() {
        if(this.selectionList!=null){
            this.selectionList.changePos(this.width, this.height, 32, this.height-65+4);
        }
        this.addWidget(done=new Button(this.width / 2 - 100, this.height - 38, 200, 20, CommonComponents.GUI_DONE, (button) -> {
            if(selectionList==null||selectionList.getSelected()==null||selectionList.getSelected().node.id==-1){
                //TODO:传-1
                return;
            }
            long nodeId=selectionList.getSelected().node.id;
            //TODO:传node
        }));
    }

    @Override
    public void tick(){
        if(!haveGotNodeList&&!isGettingNodeList){
            new Thread(()->{
                List<JsonNode> nodes;
                try {
                    isGettingNodeList=true;
                    nodes=Request.getNodeList().data.list;
                    this.addWidget(this.selectionList=new NodeSelectionList(this.minecraft,nodes));
                    isGettingNodeList=false;
                    haveGotNodeList=true;
                } catch (Exception e) {
                    e.printStackTrace();
                    this.minecraft.setScreen(lastscreen);
                }
            },"Request thread").start();
        }
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        super.render(poseStack, i, j, f);
        this.renderDirtBackground(0);
        if(selectionList!=null){
            selectionList.render(poseStack,i,j,f);
        }
        drawCenteredString(poseStack,this.font,this.title,this.width/2,16,0xffffff);
        done.render(poseStack,i,j,f);
    }

    class NodeSelectionList extends ObjectSelectionList<NodeSelectionList.Entry>{
        public NodeSelectionList(Minecraft minecraft, List<JsonNode> nodes) {
            super(minecraft, NodeSelectionScreen.this.width, NodeSelectionScreen.this.height, 32, NodeSelectionScreen.this.height-65+4, 40);
            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }
            JsonNode nothing=new JsonNode();
            nothing.name=CommonComponents.GUI_BACK.getString();
            nothing.id=-1;
            this.addEntry(new Entry(nothing));
            for(JsonNode node:nodes){
                this.addEntry(new Entry(node));
            }
        }

        public void changePos(int width, int height, int y0, int y1){
            this.width=width;
            this.height=height;
            this.y0=y0;
            this.y1=y1;
        }

        @Override
        protected boolean isFocused(){
            return NodeSelectionScreen.this.getFocused() == this;
        }

        @Override
        protected int getScrollbarPosition() {
            return super.getScrollbarPosition() + 20;
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        @Override
        protected void renderBackground(PoseStack poseStack) {
            NodeSelectionScreen.this.renderBackground(poseStack);
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, float f) {
            super.render(poseStack, i, j, f);
        }

        public class Entry extends ObjectSelectionList.Entry<Entry>{
            JsonNode node;
            public Entry(JsonNode node){
                this.node=node;
            }

            @Override
            public @NotNull Component getNarration() {
                return Utils.translatableText("narrator.select",this.node.name);
            }

            public boolean mouseClicked(double d, double e, int i) {
                if (i == 0) {
                    this.select();
                    return true;
                } else {
                    return false;
                }
            }

            private void select(){
                NodeSelectionList.this.setSelected(this);
            }

            @Override
            public void render(PoseStack poseStack, int i, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float f) {
                fill(poseStack,x,y,x+entryWidth,y+entryHeight,0x8f2b2b2b);
                String group=null;
                if(this.node.group!=null){
                    group = this.node.group.split(";")[0];
                    group=Character.toUpperCase(group.charAt(0))+group.substring(1);
                }
                drawString(poseStack, NodeSelectionScreen.NodeSelectionList.this.minecraft.font, "#"+this.node.id+" "+this.node.name+(group!=null&&!group.equals("Admin")&&!group.equals("Dev")?" "+group:""), x + 4, y + 4, 0x8fffffff);
                drawString(poseStack, NodeSelectionScreen.NodeSelectionList.this.minecraft.font, this.node.description, x + 4, y + 4 + (entryHeight-4) / 2, 0x8fffffff);
                drawString(poseStack, NodeSelectionScreen.NodeSelectionList.this.minecraft.font, this.node.fullyLoaded||this.node.status!=200?Utils.translatableText("text.openlink.node_unavailable"):(this.node.needRealname?Utils.translatableText("text.openlink.node_needrealname"):Utils.EMPTY), x + entryWidth - 4 - NodeSelectionScreen.NodeSelectionList.this.minecraft.font.width(this.node.fullyLoaded||this.node.status!=200?Utils.translatableText("text.openlink.node_unavailable"):(this.node.needRealname?Utils.translatableText("text.openlink.node_needrealname"):Utils.EMPTY)), y + 4, 0x8fffffff);
                drawString(poseStack, NodeSelectionScreen.NodeSelectionList.this.minecraft.font, this.node.bandwidth+"Mbps"+(this.node.bandwidthMagnification>1?" * "+this.node.bandwidthMagnification:""), x + entryWidth - 4 - NodeSelectionScreen.NodeSelectionList.this.minecraft.font.width(this.node.bandwidth+"Mbps"+(this.node.bandwidthMagnification>1?" * "+this.node.bandwidthMagnification:"")), y + 4 + (entryHeight-4) / 2, 0x8fffffff);
            }
        }
    }
}
