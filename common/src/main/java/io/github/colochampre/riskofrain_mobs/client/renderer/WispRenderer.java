package io.github.colochampre.riskofrain_mobs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.client.models.WispModel;
import io.github.colochampre.riskofrain_mobs.entities.enemies.WispEntity;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityRendering;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class WispRenderer extends MobRenderer<WispEntity, WispModel<WispEntity>> {
  private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(RoRMod.MOD_ID, "textures/entity/wisp/wisp_default.png");

  public WispRenderer(EntityRendererProvider.Context context) {
    super(context, new WispModel<>(context.bakeLayer(RoREntityRendering.WISP_LAYER)), 0.33F);
  }

  @Override
  public void render(WispEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    poseStack.pushPose();
    float scale = 0.75F;
    poseStack.scale(scale, scale, scale);
    super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    poseStack.popPose();
  }

  @Override
  public @NotNull ResourceLocation getTextureLocation(WispEntity entity) {
    return DEFAULT_TEXTURE;
  }
}
