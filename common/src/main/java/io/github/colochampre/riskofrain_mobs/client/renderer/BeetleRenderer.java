package io.github.colochampre.riskofrain_mobs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.client.models.BeetleModel;
import io.github.colochampre.riskofrain_mobs.client.renderer.layers.BeetleEyesLayer;
import io.github.colochampre.riskofrain_mobs.entities.enemies.BeetleEntity;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityRendering;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class BeetleRenderer extends MobRenderer<BeetleEntity, BeetleModel<BeetleEntity>> {
  private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(RoRMod.MOD_ID, "textures/entity/beetle/beetle_default.png");

  public BeetleRenderer(EntityRendererProvider.Context context) {
    super(context, new BeetleModel<>(context.bakeLayer(RoREntityRendering.BEETLE_LAYER)), 0.66F);
    this.addLayer(new BeetleEyesLayer(this));
  }

  @Override
  public void render(BeetleEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    poseStack.pushPose();
    float scale = 0.90F;
    poseStack.scale(scale, scale, scale);
    super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    poseStack.popPose();
  }

  @Override
  public @NotNull ResourceLocation getTextureLocation(BeetleEntity entity) {
    return BeetleRenderer.DEFAULT_TEXTURE;
  }
}
