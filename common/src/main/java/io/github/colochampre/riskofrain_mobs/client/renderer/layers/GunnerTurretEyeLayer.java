package io.github.colochampre.riskofrain_mobs.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.client.models.GunnerTurretModel;
import io.github.colochampre.riskofrain_mobs.entities.allies.GunnerTurretEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GunnerTurretEyeLayer extends EyesLayer<GunnerTurretEntity, GunnerTurretModel<GunnerTurretEntity>> {

  static final RenderType GUNNER_TURRET_EYE = RenderType.eyes(new ResourceLocation(RoRMod.MOD_ID, "textures/entity/gunner_turret/gunner_turret_eye.png"));

  public GunnerTurretEyeLayer(RenderLayerParent<GunnerTurretEntity, GunnerTurretModel<GunnerTurretEntity>> renderer) {
    super(renderer);
  }

  @Override
  public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, GunnerTurretEntity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
    if (entity.isTame() && !entity.isInSittingPose()) {
      super.render(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
    }
  }

  @Override
  public @NotNull RenderType renderType() {
    return GUNNER_TURRET_EYE;
  }
}
