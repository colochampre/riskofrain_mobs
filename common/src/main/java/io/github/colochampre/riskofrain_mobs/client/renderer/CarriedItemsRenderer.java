package io.github.colochampre.riskofrain_mobs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.colochampre.riskofrain_mobs.registry.RoRConfigs;
import io.github.colochampre.riskofrain_mobs.utils.CarriedItemsSource;
import io.github.colochampre.riskofrain_mobs.utils.ItemPickup;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Draws the items a mob carries as billboarded icons above its head, in rows of {@link #ITEMS_PER_ROW}. Called from the
 * render method of each carrier's renderer after the model is drawn. Client only.
 */
@Environment(EnvType.CLIENT)
public final class CarriedItemsRenderer {

  public static final int ITEMS_PER_ROW = 4;

  private static final float ICON_SIZE = 0.4F;
  private static final float ICON_GAP = 0.12F;
  private static final float COUNT_GAP = 0.04F;
  private static final float ROW_HEIGHT = 0.45F;
  private static final float TEXT_SCALE = 0.03F;
  private static final float FONT_HEIGHT = 8.0F;
  // Vanilla name tags hang this far below their anchor (9 px at 0.025 per px)
  private static final float NAME_TAG_HEIGHT = 0.225F;
  private static final float GAP_ABOVE_MODEL = 0.2F;
  private static final float GAP_ABOVE_NAME = 0.05F;
  private static final int MAX_CACHED_ENTITIES = 128;

  private record Carried(ItemStack stack, String label) {
  }

  private record Parsed(CompoundTag source, List<Carried> items) {
  }

  // The synced tag is a new instance each time the server sends an update, so identity tells when to parse again
  private static final Map<Integer, Parsed> CACHE = new HashMap<>();

  private CarriedItemsRenderer() {
  }

  /**
   * @param nameShown   whether the renderer is drawing the entity's name tag, so the rows go above it
   * @param renderScale scale the renderer applied to the model and name tag, 1 when it applies none
   */
  public static <T extends Entity & CarriedItemsSource> void render(T entity, boolean nameShown, float renderScale,
                                                                    PoseStack poseStack, MultiBufferSource buffer) {
    RoRConfigs.ItemPickupConfig config = RoRConfigs.get().ITEM_PICKUP;
    Minecraft minecraft = Minecraft.getInstance();
    if (!config.SHOW_CARRIED_ITEMS || minecraft.options.hideGui || entity.isInvisible()) {
      return;
    }
    List<Carried> items = getCarried(entity);
    if (items.isEmpty()) {
      return;
    }
    double maxDistance = config.DISPLAY_DISTANCE;
    if (minecraft.getEntityRenderDispatcher().distanceToSqr(entity) > maxDistance * maxDistance) {
      return;
    }

    Font font = minecraft.font;
    float baseY = nameShown
            ? entity.getNameTagOffsetY() * renderScale + GAP_ABOVE_NAME
            : entity.getBbHeight() * renderScale + GAP_ABOVE_MODEL;
    int background = (int) (minecraft.options.getBackgroundOpacity(0.25F) * 255.0F) << 24;

    poseStack.pushPose();
    poseStack.translate(0.0F, baseY, 0.0F);
    poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
    // The camera orientation leaves +x pointing to the viewer's left, as vanilla name tags compensate with a negative scale
    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

    for (int rowStart = 0; rowStart < items.size(); rowStart += ITEMS_PER_ROW) {
      List<Carried> row = items.subList(rowStart, Math.min(rowStart + ITEMS_PER_ROW, items.size()));
      float rowY = (rowStart / ITEMS_PER_ROW) * ROW_HEIGHT + ICON_SIZE / 2.0F;
      float x = -rowWidth(row, font) / 2.0F;
      for (Carried carried : row) {
        renderIcon(entity, carried.stack(), x + ICON_SIZE / 2.0F, rowY, poseStack, buffer);
        x += ICON_SIZE;
        if (carried.label() != null) {
          renderLabel(font, carried.label(), x + COUNT_GAP, rowY, background, poseStack, buffer);
          x += COUNT_GAP + font.width(carried.label()) * TEXT_SCALE;
        }
        x += ICON_GAP;
      }
    }
    poseStack.popPose();
  }

  private static float rowWidth(List<Carried> row, Font font) {
    float width = -ICON_GAP;
    for (Carried carried : row) {
      width += ICON_SIZE + ICON_GAP;
      if (carried.label() != null) {
        width += COUNT_GAP + font.width(carried.label()) * TEXT_SCALE;
      }
    }
    return width;
  }

  private static void renderIcon(Entity entity, ItemStack stack, float x, float y, PoseStack poseStack, MultiBufferSource buffer) {
    poseStack.pushPose();
    poseStack.translate(x, y, 0.0F);
    poseStack.scale(ICON_SIZE, ICON_SIZE, ICON_SIZE);
    Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GUI, LightTexture.FULL_BRIGHT,
            OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
    poseStack.popPose();
  }

  // Same two passes as a vanilla name tag: a faint see-through one with the background, then the solid text
  private static void renderLabel(Font font, String label, float x, float y, int background, PoseStack poseStack, MultiBufferSource buffer) {
    poseStack.pushPose();
    poseStack.translate(x, y + FONT_HEIGHT * TEXT_SCALE / 2.0F, 0.0F);
    poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
    Matrix4f matrix = poseStack.last().pose();
    font.drawInBatch(label, 0.0F, 0.0F, 0x20FFFFFF, false, matrix, buffer, Font.DisplayMode.SEE_THROUGH, background, LightTexture.FULL_BRIGHT);
    font.drawInBatch(label, 0.0F, 0.0F, -1, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
    poseStack.popPose();
  }

  private static List<Carried> getCarried(Entity entity) {
    CompoundTag tag = ((CarriedItemsSource) entity).getCarriedItemsTag();
    if (tag.isEmpty()) {
      CACHE.remove(entity.getId());
      return List.of();
    }
    Parsed cached = CACHE.get(entity.getId());
    if (cached != null && cached.source() == tag) {
      return cached.items();
    }
    if (CACHE.size() >= MAX_CACHED_ENTITIES) {
      CACHE.clear();
    }
    Parsed parsed = new Parsed(tag, parse(tag));
    CACHE.put(entity.getId(), parsed);
    return parsed.items();
  }

  private static List<Carried> parse(CompoundTag tag) {
    List<Carried> items = new ArrayList<>();
    ListTag list = tag.getList(ItemPickup.SUMMARY_ITEMS, Tag.TAG_COMPOUND);
    for (int i = 0; i < list.size(); i++) {
      CompoundTag entry = list.getCompound(i);
      ResourceLocation id = ResourceLocation.tryParse(entry.getString(ItemPickup.SUMMARY_ID));
      if (id == null) {
        continue;
      }
      // An item whose mod was removed is skipped rather than drawn as air
      Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
      int count = entry.getInt(ItemPickup.SUMMARY_COUNT);
      if (item == null || count < 1) {
        continue;
      }
      items.add(new Carried(new ItemStack(item), count > 1 ? "x" + count : null));
    }
    return items;
  }
}
