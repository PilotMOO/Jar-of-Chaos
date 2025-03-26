package mod.pilot.jar_of_chaos.systems.ModelDisplay;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

import java.awt.*;
import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public class InWorldDisplayManager {
    public static void Setup(){
        MinecraftForge.EVENT_BUS.addListener(InWorldDisplayManager::ClientTick);
    }

    protected static final ArrayList<RenderPackage> QuedPackages = new ArrayList<>();
    protected static final ArrayList<RenderPackage> DiscardedPackages = new ArrayList<>();
    public static void addRenderPackage(RenderPackage _package){QuedPackages.add(_package);}
    public static void removeRenderPackage(RenderPackage _package){DiscardedPackages.add(_package);}

    private static final ArrayList<RenderPackage> renderPackages = new ArrayList<>();
    private static ArrayList<RenderPackage> _volatile = new ArrayList<>();
    private static boolean _updateVolatile;
    private static void _volatileChanged(){
        _updateVolatile = true;
    }
    public static void RenderModelHook(PoseStack poseStack, int packedLight, int packedOverlay){
        System.out.println("actual Render hook called");
        if (renderPackages.isEmpty()) return;
        else if (_updateVolatile) {
            _volatile = new ArrayList<>(renderPackages);
            _updateVolatile = false;
        }
        System.out.println("Render hook passed empty check! Amount: " + renderPackages.size());

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        for (RenderPackage rP : _volatile){
            rP.render(poseStack, bufferbuilder, packedLight, packedOverlay);
            System.out.println("Rendering a package");
        }
    }
    public static void ClientTick(TickEvent.ClientTickEvent event){
        if (!QuedPackages.isEmpty()) {
            renderPackages.addAll(QuedPackages);
            QuedPackages.clear();
            _volatileChanged();
        }
        if (!DiscardedPackages.isEmpty()) {
            renderPackages.removeAll(DiscardedPackages);
            DiscardedPackages.clear();
            _volatileChanged();
        }
        renderPackages.forEach(RenderPackage::tick);
    }

    public static class RenderPackage{
        public RenderPackage(ModelDisplay display){
            this(display, Vec3.ZERO);
        }
        public RenderPackage(ModelDisplay display, Vec3 position){
            this(display, position, Color.WHITE);
        }
        public RenderPackage(ModelDisplay display, Vec3 position, Color color){
            this.modelDisplay = display;
            this.position = position;
            this.color = color;
        }

        public final ModelDisplay modelDisplay;
        public Vec3 position;
        public Color color;
        public int age;
        public boolean disableRendering = false;
        public final boolean shouldRender(){
            return !disableRendering && renderCheck();
        }
        public boolean renderCheck(){
            return true;
        }

        public void tick(){
            this.age++;
        }

        public void render(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay){
            if (shouldRender()) this.modelDisplay.render(position, poseStack, consumer, packedLight, packedOverlay, color);
        }
    }
    public record ModelDisplay(Model model){
        public void render(Vec3 position, PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay){
            this.render(position, poseStack, consumer, packedLight, packedOverlay,
                    255f, 255f, 255f, 255f);
        }
        public void render(Vec3 position, PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, Color color){
            this.render(position, poseStack, consumer, packedLight, packedOverlay,
                    color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        }
        public void render(Vec3 position, PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, float r, float g, float b, float a){
            poseStack.pushPose();
            poseStack.translate(position.x, position.y, position.z);
            model.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, r, g, b, a);
            poseStack.popPose();
        }
    }
}
