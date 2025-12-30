package io.github.colochampre.riskofrain_mobs.client.renderer.layers;

import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.client.models.StoneGolemModel;
import io.github.colochampre.riskofrain_mobs.entities.enemies.StoneGolemEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class StoneGolemEyeLayer extends EyesLayer<StoneGolemEntity, StoneGolemModel<StoneGolemEntity>> {
  static final RenderType STONE_GOLEM_EYE = RenderType.eyes(new ResourceLocation(RoRMod.MOD_ID, "textures/entity/stone_golem/stone_golem_eye.png"));

  public StoneGolemEyeLayer(RenderLayerParent<StoneGolemEntity, StoneGolemModel<StoneGolemEntity>> layer) {
    super(layer);
  }

  @Override
  public @NotNull RenderType renderType() {
    return STONE_GOLEM_EYE;
  }
}
