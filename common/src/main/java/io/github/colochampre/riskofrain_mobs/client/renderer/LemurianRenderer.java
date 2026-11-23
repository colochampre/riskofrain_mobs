package io.github.colochampre.riskofrain_mobs.client.renderer;

import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.client.models.LemurianModel;
import io.github.colochampre.riskofrain_mobs.client.renderer.layers.LemurianEyesLayer;
import io.github.colochampre.riskofrain_mobs.entities.enemies.LemurianEntity;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityRendering;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class LemurianRenderer extends MobRenderer<LemurianEntity, LemurianModel<LemurianEntity>> {

  private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(RoRMod.MOD_ID, "textures/entity/lemurian/lemurian_default.png");
  private static final ResourceLocation EVOLVED_TEXTURE = new ResourceLocation(RoRMod.MOD_ID, "textures/entity/lemurian/lemurian_evolved.png");
  private static final ResourceLocation GOLDEN_ARMOR = new ResourceLocation(RoRMod.MOD_ID, "textures/entity/lemurian/lemurian_golden_armor.png");

  public LemurianRenderer(EntityRendererProvider.Context context) {
    super(context, new LemurianModel<>(context.bakeLayer(RoREntityRendering.LEMURIAN_LAYER)), 0.4F);
    this.addLayer(new LemurianEyesLayer(this));
  }

  @Override
  public @NotNull ResourceLocation getTextureLocation(LemurianEntity entity) {
    if (entity.getLemurianType() == LemurianEntity.Type.EVOLVED) {
      return LemurianRenderer.EVOLVED_TEXTURE;
    } else if (entity.getLemurianType() == LemurianEntity.Type.GOLDEN_ARMOR) {
      return LemurianRenderer.GOLDEN_ARMOR;
    } else {
      return LemurianRenderer.DEFAULT_TEXTURE;
    }
  }
}
