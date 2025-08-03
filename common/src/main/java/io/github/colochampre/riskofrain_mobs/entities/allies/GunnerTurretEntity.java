package io.github.colochampre.riskofrain_mobs.entities.allies;

import io.github.colochampre.riskofrain_mobs.registry.RoRItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.NotNull;

public class GunnerTurretEntity extends AbstractDroneEntity {

  private static final EntityDataAccessor<Integer> DATA_BODY_COLOR = SynchedEntityData.defineId(GunnerTurretEntity.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(GunnerTurretEntity.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Integer> DATA_ID_HURTDIR = SynchedEntityData.defineId(GunnerTurretEntity.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(GunnerTurretEntity.class, EntityDataSerializers.FLOAT);
  private static final float MAX_ROTATION_SPEED = Mth.PI * 0.3F;
  private static final float ROTATION_ACCELERATION = 0.16F;
  private static final float ROTATION_DECELERATION = 0.012F;
  private float gunAngle;
  private float prevGunAngle;
  private float gunSpeed;

  public GunnerTurretEntity(EntityType<? extends AbstractDroneEntity> entityType, Level level) {
    super(entityType, level);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Mob.createMobAttributes()
            .add(Attributes.ARMOR, 4.0D)
            .add(Attributes.ATTACK_DAMAGE, 2.0D)
            .add(Attributes.FLYING_SPEED, 1.0D)
            .add(Attributes.FOLLOW_RANGE, 24.0D)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
            .add(Attributes.MAX_HEALTH, 26.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.23D);
  }

  @Override
  protected int getDroneType() {
    return TYPE_LAND;
  }

  @Override
  protected int getDronePrice() {
    return 36;
  }

  @Override
  protected void defineSynchedData() {
    super.defineSynchedData();
    this.entityData.define(DATA_BODY_COLOR, DyeColor.LIGHT_BLUE.getId());
    this.entityData.define(DATA_ID_HURT, 0);
    this.entityData.define(DATA_ID_HURTDIR, 1);
    this.entityData.define(DATA_ID_DAMAGE, 0.0F);
  }

  @Override
  public void addAdditionalSaveData(@NotNull CompoundTag tag) {
    super.addAdditionalSaveData(tag);
    tag.putByte("BodyColor", (byte) this.getBodyColor().getId());
  }

  @Override
  public void readAdditionalSaveData(@NotNull CompoundTag tag) {
    super.readAdditionalSaveData(tag);
    if (tag.contains("BodyColor", 99)) {
      this.setBodyColor(DyeColor.byId(tag.getInt("BodyColor")));
    }
  }

  @Override
  public void setTame(boolean tamed) {
    // GunnerTurretAttackGoal attackGoal = new GunnerTurretAttackGoal(this, 24.0F);
    super.setTame(tamed);
    if (tamed) {
      //this.goalSelector.addGoal(3, attackGoal);
    }
  }

  public static boolean checkDroneSpawnRules(EntityType<GunnerTurretEntity> entity, LevelAccessor level, MobSpawnType type, BlockPos pos, RandomSource randomSource) {
    return (level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON)
            || level.getBlockState(pos.below()).is(BlockTags.RABBITS_SPAWNABLE_ON))
            && isBrightEnoughToSpawn(level, pos);
  }

  @Override
  public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
    ItemStack itemstack = player.getItemInHand(hand);
    Item item = itemstack.getItem();
    // Set body color
    if (item instanceof DyeItem && this.isTame()) {
      DyeColor dyecolor = ((DyeItem) item).getDyeColor();
      if (dyecolor != this.getBodyColor()) {
        this.setBodyColor(dyecolor);
        if (!player.getAbilities().instabuild) {
          itemstack.shrink(1);
        }
        return InteractionResult.SUCCESS;
      }
    }
    return super.mobInteract(player, hand);
  }

  public DyeColor getBodyColor() {
    return DyeColor.byId(this.entityData.get(DATA_BODY_COLOR));
  }

  public void setBodyColor(DyeColor color) {
    this.entityData.set(DATA_BODY_COLOR, color.getId());
  }

  public ItemStack getPickResult() {
    ItemStack stack = new ItemStack(getDropItem());
    this.tagItemStack(stack);
    return stack;
  }

  private Item getDropItem() {
    int colorId = this.entityData.get(DATA_BODY_COLOR);
    DyeColor color = DyeColor.byId(colorId);
    return RoRItems.getTurretItemForColor(color);
  }

  private void tagItemStack(ItemStack stack) {
    CompoundTag tag = new CompoundTag();
    tag.putFloat("TurretHealth", this.getHealth());
    if (this.getOwnerUUID() != null) {
      tag.putUUID("OwnerUUID", this.getOwnerUUID());
    }
    int colorId = this.entityData.get(DATA_BODY_COLOR);
    tag.putInt("BodyColor", colorId);
    stack.setTag(tag);
  }

  @Override
  protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
    return 1.15625F;
  }

  @Override
  public boolean isPushable() {
    return !this.isTame() || this.isInSittingPose();
  }
}
