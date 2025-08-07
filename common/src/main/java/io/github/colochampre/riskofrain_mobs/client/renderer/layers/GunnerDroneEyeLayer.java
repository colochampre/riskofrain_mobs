package io.github.colochampre.riskofrain_mobs.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.client.models.GunnerDroneModel;
import io.github.colochampre.riskofrain_mobs.entities.allies.GunnerDroneEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GunnerDroneEyeLayer extends EyesLayer<GunnerDroneEntity, GunnerDroneModel<GunnerDroneEntity>> {

  static final RenderType GUNNER_DRONE_EYE = RenderType.eyes(new ResourceLocation(RoRMod.MOD_ID, "textures/entity/gunner_drone/gunner_drone_eye.png"));

  public GunnerDroneEyeLayer(RenderLayerParent<GunnerDroneEntity, GunnerDroneModel<GunnerDroneEntity>> renderer) {
    super(renderer);
  }

  @Override
  public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, GunnerDroneEntity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
    if (entity.isTame() && !entity.isInSittingPose()) {
      super.render(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
    }
  }

  @Override
  public @NotNull RenderType renderType() {
    return GUNNER_DRONE_EYE;
  }
}

