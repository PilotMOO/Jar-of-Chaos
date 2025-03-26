package mod.pilot.jar_of_chaos.systems.ModelDisplay.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.pilot.jar_of_chaos.JarOfChaos;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class BeeModel extends Model {
	public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(JarOfChaos.MOD_ID, "textures/misc/bee.png");
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(TEXTURE_LOCATION, "main");
	private final ModelPart bee;
	private final ModelPart wingleft;
	private final ModelPart wingrgiuht;

	public BeeModel(ModelPart root) {
		super(RenderType::entityTranslucent);
		this.bee = root.getChild("bee");
		this.wingleft = this.bee.getChild("wingleft");
		this.wingrgiuht = this.bee.getChild("wingrgiuht");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bee = partdefinition.addOrReplaceChild("bee", CubeListBuilder.create().texOffs(1, 1).addBox(0.0F, -1.75F, -3.75F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.2F))
				.texOffs(1, 1).addBox(-1.0F, -1.75F, -3.75F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.2F))
				.texOffs(2, 2).addBox(-1.0F, -2.0F, -3.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(0, 9).addBox(-0.5F, -1.5F, 1.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition annetnia_r1 = bee.addOrReplaceChild("annetnia_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-0.45F, -0.5F, -1.75F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(0.75F, -1.75F, -3.4F, -0.5672F, -0.3927F, 0.0F));

		PartDefinition annetnia_r2 = bee.addOrReplaceChild("annetnia_r2", CubeListBuilder.create().texOffs(0, 3).addBox(-0.55F, -0.5F, -1.75F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(-0.75F, -1.75F, -3.4F, -0.5672F, 0.3927F, 0.0F));

		PartDefinition wingleft = bee.addOrReplaceChild("wingleft", CubeListBuilder.create(), PartPose.offset(0.25F, -1.75F, -1.0F));

		PartDefinition Wing_r1 = wingleft.addOrReplaceChild("Wing_r1", CubeListBuilder.create().texOffs(0, 9).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5672F, 0.829F, 0.0F));

		PartDefinition wingrgiuht = bee.addOrReplaceChild("wingrgiuht", CubeListBuilder.create(), PartPose.offset(-0.25F, -1.75F, -1.0F));

		PartDefinition wiong_r1 = wingrgiuht.addOrReplaceChild("wiong_r1", CubeListBuilder.create().texOffs(6, 0).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5672F, -0.829F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bee.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}