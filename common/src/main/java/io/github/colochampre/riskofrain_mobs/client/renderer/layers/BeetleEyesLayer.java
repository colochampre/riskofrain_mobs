package io.github.colochampre.riskofrain_mobs.client.renderer.layers;

import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.client.models.BeetleModel;
import io.github.colochampre.riskofrain_mobs.entities.enemies.BeetleEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class BeetleEyesLayer extends EyesLayer<BeetleEntity, BeetleModel<BeetleEntity>> {
  static final RenderType BEETLE_EYES = RenderType.eyes(new ResourceLocation(RoRMod.MOD_ID, "textures/entity/beetle/beetle_eyes.png"));

  public BeetleEyesLayer(RenderLayerParent<BeetleEntity, BeetleModel<BeetleEntity>> layer) {
    super(layer);
  }

  @Override
  public @NotNull RenderType renderType() {
    return BEETLE_EYES;
  }
}
