package io.github.colochampre.riskofrain_mobs.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.client.models.GunnerTurretModel;
import io.github.colochampre.riskofrain_mobs.entities.allies.GunnerTurretEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import java.util.Locale;

public class GunnerTurretBodyLayer extends RenderLayer<GunnerTurretEntity, GunnerTurretModel<GunnerTurretEntity>> {

  private static final String TEXTURE_PATH = "textures/entity/gunner_turret_%s.png";
  //private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(RoRMod.MOD_ID, "textures/entity/gunner_turret/gunner_turret_color.png");

  public GunnerTurretBodyLayer(RenderLayerParent<GunnerTurretEntity, GunnerTurretModel<GunnerTurretEntity>> renderer) {
    super(renderer);
  }

  @Override
  public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, GunnerTurretEntity entity,
                     float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
    if (entity.isTame() && !entity.isInvisible()) {
      DyeColor color = entity.getBodyColor();
      if (color != DyeColor.LIGHT_BLUE) {
        ResourceLocation texture = new ResourceLocation(RoRMod.MOD_ID, String.format(TEXTURE_PATH, color.getName().toLowerCase(Locale.ROOT)));
        renderColoredCutoutModel(this.getParentModel(), texture, poseStack, buffer, packedLight, entity, 1.0F, 1.0F, 1.0F);
      }
    }
  }
}
