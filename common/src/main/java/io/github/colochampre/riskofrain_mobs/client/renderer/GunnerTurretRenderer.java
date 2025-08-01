package io.github.colochampre.riskofrain_mobs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.entities.GunnerTurretEntity;
import io.github.colochampre.riskofrain_mobs.client.models.GunnerTurretModel;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityRendering;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class GunnerTurretRenderer extends MobRenderer<GunnerTurretEntity, GunnerTurretModel<GunnerTurretEntity>> {
  private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(RoRMod.MOD_ID, "textures/entity/gunner_turret/gunner_turret_default.png");

  public GunnerTurretRenderer(EntityRendererProvider.Context context) {
    super(context, new GunnerTurretModel<>(context.bakeLayer(RoREntityRendering.GUNNER_TURRET_LAYER)), 0.40F);
    //this.addLayer(new GunnerTurretBodyLayer(this));
    //this.addLayer(new GunnerTurretEyeLayer(this));
  }

  @Override
  public void render(@NotNull GunnerTurretEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
    poseStack.pushPose();
    float scale = 0.80F;
    poseStack.scale(scale, scale, scale);
    super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    poseStack.popPose();
  }

  @Override
  public @NotNull ResourceLocation getTextureLocation(@NotNull GunnerTurretEntity entity) {
    return DEFAULT_TEXTURE;
  }
}
