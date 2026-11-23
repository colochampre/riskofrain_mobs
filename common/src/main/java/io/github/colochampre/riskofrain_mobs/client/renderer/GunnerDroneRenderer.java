package io.github.colochampre.riskofrain_mobs.client.renderer;

import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.client.models.GunnerDroneModel;
import io.github.colochampre.riskofrain_mobs.client.renderer.layers.GunnerDroneEyeLayer;
import io.github.colochampre.riskofrain_mobs.entities.allies.GunnerDroneEntity;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityRendering;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@Environment(EnvType.CLIENT)
public class GunnerDroneRenderer extends MobRenderer<GunnerDroneEntity, GunnerDroneModel<GunnerDroneEntity>> {

  private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(RoRMod.MOD_ID, "textures/entity/gunner_drone/gunner_drone_default.png");
  private static final String COLORED_TEXTURE = "textures/entity/gunner_drone/gunner_drone_%s.png";

  public GunnerDroneRenderer(EntityRendererProvider.Context context) {
    super(context, new GunnerDroneModel<>(context.bakeLayer(RoREntityRendering.GUNNER_DRONE_LAYER)), 0.30F);
    this.addLayer(new GunnerDroneEyeLayer(this));
  }

  @Override
  public @NotNull ResourceLocation getTextureLocation(GunnerDroneEntity entity) {
    DyeColor color = entity.getBodyColor();
    if (color != DyeColor.LIGHT_BLUE) {
      return new ResourceLocation(RoRMod.MOD_ID, String.format(COLORED_TEXTURE, color.getName().toLowerCase(Locale.ROOT)));
    }
    return DEFAULT_TEXTURE;
  }
}
