package mod.pilot.jar_of_chaos.entities.client.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mod.pilot.jar_of_chaos.entities.misc.SpecialItemEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.awt.*;

public class SpecialItemRenderer extends ItemEntityRenderer {
    public SpecialItemRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.itemRenderer = pContext.getItemRenderer();
    }
    public final ItemRenderer itemRenderer;

    @Override
    public void render(@NotNull ItemEntity item, float yaw, float partial, @NotNull PoseStack stack,
                       @NotNull MultiBufferSource buffer, int packedLight) {
        if (!(item instanceof SpecialItemEntity sItem)) return;
        float size = sItem.getSize();

        //originally referenced the death time, changed to tick count (prob temporary tho)
        //Seems to influence the amount of rays...?
        float f5 = ((float)item.tickCount + partial) / 200.0F;
        float f7 = Math.min((f5 - 0.8F) / 0.2F, 1.0F);

        RandomSource randomsource = RandomSource.create(432L);
        VertexConsumer vertexconsumer2 = buffer.getBuffer(RenderType.lightning());
        stack.pushPose();
        float disOffGround = this.itemRenderer.getModel(item.getItem(), item.level(), null, item.getId()).getTransforms().getTransform(ItemDisplayContext.GROUND).scale.y();
        stack.translate(0.0F, (disOffGround / 2)
                + (Mth.sin(((float)item.getAge() + partial) / 10.0F + item.bobOffs) * 0.1F + 0.1F) / 4, 0F);

        for(int i = 0; (float)i < Math.min((f5 + f5 * f5) / 2.0F * 60.0F, 6); ++i) {
            stack.mulPose(Axis.XP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            stack.mulPose(Axis.YP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            stack.mulPose(Axis.ZP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            stack.mulPose(Axis.XP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            stack.mulPose(Axis.YP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            stack.mulPose(Axis.ZP.rotationDegrees(randomsource.nextFloat() * 360.0F + f5 * 90.0F));
            float f3 = (randomsource.nextFloat() + 1.0F + f7) * (0.25f + (size / 60));
            float f4 = (randomsource.nextFloat() * 0.1F + 0.2F + f7) * (0.1f + (size / 30));

            System.out.println("Color is " + sItem.getColor() + ", " + sItem.getEndColor());
            Color endColor = new Color(sItem.getEndColor());
            Color startColor = new Color(sItem.getColor());

            Matrix4f matrix4f = stack.last().pose();
            int alpha = 255;
            vertex01(vertexconsumer2, matrix4f, startColor, alpha);
            vertex2(vertexconsumer2, matrix4f, f3, f4, endColor);
            vertex3(vertexconsumer2, matrix4f, f3, f4, endColor);
            vertex01(vertexconsumer2, matrix4f, startColor, alpha);
            vertex3(vertexconsumer2, matrix4f, f3, f4, endColor);
            vertex4(vertexconsumer2, matrix4f, f3, f4, endColor);
            vertex01(vertexconsumer2, matrix4f, startColor, alpha);
            vertex4(vertexconsumer2, matrix4f, f3, f4, endColor);
            vertex2(vertexconsumer2, matrix4f, f3, f4, endColor);
        }

        stack.popPose();

        stack.scale(size, size, size);
        super.render(item, yaw, partial, stack, buffer, packedLight);
    }

    //Stole a lot from the Enderdragon renderer lmao
    //Not gonna pretend like I know what half of it means
    //Same goes for the mess inside the render method, that is also stolen :P
    private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0D) / 2.0D);
    private static void vertex01(VertexConsumer pConsumer, Matrix4f pMatrix, Color color, int pAlpha) {
        pConsumer.vertex(pMatrix, 0.0F, 0.0F, 0.0F).color(color.getRed(), color.getGreen(), color.getGreen(), pAlpha).endVertex();
    }

    private static void vertex2(VertexConsumer pConsumer, Matrix4f pMatrix, float p_253704_, float p_253701_, Color color) {
        pConsumer.vertex(pMatrix, -HALF_SQRT_3 * p_253701_, p_253704_, -0.5F * p_253701_).color(color.getRed(), color.getGreen(), color.getGreen(), 0).endVertex();
    }

    private static void vertex3(VertexConsumer pConsumer, Matrix4f pMatrix, float p_253729_, float p_254030_, Color color) {
        pConsumer.vertex(pMatrix, HALF_SQRT_3 * p_254030_, p_253729_, -0.5F * p_254030_).color(color.getRed(), color.getGreen(), color.getGreen(), 0).endVertex();
    }

    private static void vertex4(VertexConsumer pConsumer, Matrix4f pMatrix, float p_253649_, float p_253694_, Color color) {
        pConsumer.vertex(pMatrix, 0.0F, p_253649_, p_253694_).color(color.getRed(), color.getGreen(), color.getGreen(), 0).endVertex();
    }
}
