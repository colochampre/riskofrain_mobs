package io.github.colochampre.riskofrain_mobs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class BulletRenderer extends EntityRenderer<Entity> {

  private final ItemRenderer itemRenderer;
  private final float scale;


  public BulletRenderer(EntityRendererProvider.Context context) {
    super(context);
    this.itemRenderer = context.getItemRenderer();
    this.scale = 0.5f;
  }

  @Override
  public void render(Entity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    poseStack.pushPose();
    
    poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
    poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
    
    poseStack.scale(this.scale, this.scale, this.scale);
    
    this.itemRenderer.renderStatic(new ItemStack(Items.IRON_NUGGET), ItemDisplayContext.GROUND,
            packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId()
    );
    poseStack.popPose();
    super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
  }

  @Override
  public @NotNull ResourceLocation getTextureLocation(Entity entity) {
    return TextureAtlas.LOCATION_BLOCKS;
    // return new ResourceLocation("textures/atlas/blocks.png");
  }
}
