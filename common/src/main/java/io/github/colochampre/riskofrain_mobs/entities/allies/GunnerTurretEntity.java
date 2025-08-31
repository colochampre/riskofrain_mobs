package io.github.colochampre.riskofrain_mobs.entities.allies;

import io.github.colochampre.riskofrain_mobs.entities.goals.GunnerTurretAttackGoal;
import io.github.colochampre.riskofrain_mobs.entities.projectiles.BulletEntity;
import io.github.colochampre.riskofrain_mobs.registry.RoRConfigs;
import io.github.colochampre.riskofrain_mobs.registry.RoRItems;
import io.github.colochampre.riskofrain_mobs.utils.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GunnerTurretEntity extends AbstractDroneEntity implements RangedAttackMob {

  private static final EntityDataAccessor<Integer> DATA_BODY_COLOR = SynchedEntityData.defineId(GunnerTurretEntity.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(GunnerTurretEntity.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Integer> DATA_ID_HURTDIR = SynchedEntityData.defineId(GunnerTurretEntity.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(GunnerTurretEntity.class, EntityDataSerializers.FLOAT);
  private static final float MAX_ROTATION_SPEED = Mth.PI * 0.3F;
  private static final float ROTATION_ACCELERATION = 0.16F;
  private static final float ROTATION_DECELERATION = 0.012F;
  private final GunnerTurretAttackGoal attackGoal = new GunnerTurretAttackGoal(this, 24.0F);
  private float gunAngle;
  private float prevGunAngle;
  private float gunSpeed;
  private float sitProgress;
  private float prevSitProgress;
  private float tameProgress;
  private float prevTameProgress;

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
  protected void registerGoals() {
    this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
    this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
    this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Mob.class, 5, false, false, (entity)
            -> entity instanceof Enemy && !(DO_NOT_ATTACK.contains(entity.getType()))));
  }

  @Override
  public int getDroneType() {
    return TYPE_LAND;
  }

  @Override
  public int getDronePrice() {
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
  public void aiStep() {
    super.aiStep();
    this.updateGun();
  }

  @Override
  public void tick() {
    if (this.getHurtTime() > 0) {
      this.setHurtTime(this.getHurtTime() - 1);
    }
    if (this.getDamage() > 0.0F) {
      this.setDamage(this.getDamage() - 1.0F);
    }
    if (this.getXRot() > 0.0F) {
      this.setXRot(this.getXRot() - 0.25F);
    } else if (this.getXRot() < 0.0F) {
      this.setXRot(this.getXRot() + 0.25F);
    }
    super.tick();
  }

  @Override
  public void setTame(boolean tamed) {
    super.setTame(tamed);
    if (tamed) {
      this.goalSelector.addGoal(3, attackGoal);
    }
  }

  @Override
  public void removeGoals() {
    super.removeGoals();
    this.goalSelector.removeGoal(attackGoal);
  }

  @Override
  public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType type, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
    this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(RoRConfigs.get().MOBS.DRONES.BULLETS_DAMAGE);
    if (RoRConfigs.get().MOBS.DRONES.GUNNER_TURRET_MAX_HEALTH > 0) {
      this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(RoRConfigs.get().MOBS.DRONES.GUNNER_TURRET_MAX_HEALTH);
    }
    if (dataTag != null && dataTag.contains("TurretHealth")) {
      this.setHealth(dataTag.getFloat("TurretHealth"));
    } else {
      this.setHealth(this.getMaxHealth());
    }
    return super.finalizeSpawn(level, difficulty, type, spawnData, dataTag);
  }

  public static boolean checkDroneSpawnRules(EntityType<GunnerTurretEntity> entity, LevelAccessor level, MobSpawnType type, BlockPos pos, RandomSource randomSource) {
    var block = level.getBlockState(pos.below());
    return (block.is(BlockTags.ANIMALS_SPAWNABLE_ON)
            || block.is(BlockTags.RABBITS_SPAWNABLE_ON))
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

  @Override
  public boolean hurt(DamageSource source, float damage) {
    if (this.isInvulnerableTo(source)) {
      return false;
    } else if (source.getDirectEntity() instanceof Player && this.isTame()) {
      Player player = (Player) source.getEntity();
      if (player != null && player.getUUID() != this.getOwnerUUID()) {
        return super.hurt(source, damage);
      }
      if (!this.level().isClientSide && !this.isRemoved()) {
        this.setHurtDir((int) -this.getHurtDir());
        this.setHurtTime(10);
        this.setDamage(this.getDamage() + damage * 10.0F);
        this.setXRot(this.getXRot() + (this.getHurtDir() == 1 ? this.getDamage() * 2 : -this.getDamage() * 2)); // Forward-backward inclination
        boolean isCreativeMode = ((Player) source.getEntity()).getAbilities().instabuild;
        if (isCreativeMode || this.getDamage() > 40.0F) {
          if (!isCreativeMode && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.dropAsItem();
          }
          this.discard();
        }
      }
      return true;
    } else {
      return super.hurt(source, damage);
    }
  }

  protected void dropAsItem() {
    ItemStack stack = new ItemStack(getDropItem());
    this.tagItemStack(stack);
    this.spawnAtLocation(stack);
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

  public DyeColor getBodyColor() {
    return DyeColor.byId(this.entityData.get(DATA_BODY_COLOR));
  }

  public void setBodyColor(DyeColor color) {
    this.entityData.set(DATA_BODY_COLOR, color.getId());
  }

  public void setDamage(float damage) {
    this.entityData.set(DATA_ID_DAMAGE, damage);
  }

  public float getDamage() {
    return this.entityData.get(DATA_ID_DAMAGE);
  }

  public void animateHurt(float p_265761_) {
    this.setHurtDir((int) -this.getHurtDir());
    this.setHurtTime(10);
    this.setDamage(this.getDamage() * 11.0F);
  }


  @Override
  public void performRangedAttack(LivingEntity target, float velocity) {
    BulletEntity bullet = new BulletEntity(this.level(), this);
    double d0 = target.getX() - this.getX();
    double d1 = target.getEyeY() - bullet.getY();
    double d2 = target.getZ() - this.getZ();
    bullet.shoot(d0, d1, d2, 3.0F, 1.0F);
    this.level().addFreshEntity(bullet);
  }

  @Override
  public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
    if (target instanceof Wolf) {
      Wolf wolf = (Wolf) target;
      return !wolf.isTame() || wolf.getOwner() != owner;
    } /* else if (target instanceof Player && owner instanceof Player && !((Player) owner).canHarmPlayer((Player) target)) {
      return false;
    } */ else if (target instanceof AbstractHorse && ((AbstractHorse) target).isTamed()) {
      return false;
    } else if (target instanceof AbstractDroneEntity && ((AbstractDroneEntity) target).isTame()) {
      return false;
    } else {
      return !(target instanceof TamableAnimal) || !((TamableAnimal) target).isTame();
    }
  }

  private void updateGun() {
    LivingEntity target = this.getActiveAttackTarget();
    this.prevGunAngle = this.gunAngle;
    if (target != null && this.isTame()) {
      this.gunSpeed = Math.min(this.gunSpeed + ROTATION_ACCELERATION, MAX_ROTATION_SPEED);
    } else {
      this.gunSpeed = Math.max(this.gunSpeed - ROTATION_DECELERATION, 0.0F);
      if (this.gunSpeed == 0) {
        gunAngle = EntityUtils.normalizeAngle(this.gunAngle);
      }
    }
    this.gunAngle += this.gunSpeed;
  }

  private void setHurtTime(int time) {
    this.entityData.set(DATA_ID_HURT, time);
  }

  public int getHurtTime() {
    return this.entityData.get(DATA_ID_HURT);
  }

  public void setHurtDir(int dir) {
    this.entityData.set(DATA_ID_HURTDIR, dir);
  }

  public float getHurtDir() {
    return this.entityData.get(DATA_ID_HURTDIR);
  }

  public float getGunAngle() {
    return this.gunAngle;
  }

  public float getPrevGunAngle() {
    return this.prevGunAngle;
  }

  public float getGunSpeed() {
    return this.gunSpeed;
  }

  public void setSitProgress(float progress) {
    this.sitProgress = progress;
  }

  public void setPrevSitProgress(float progress) {
    this.prevSitProgress = progress;
  }

  public float getSitProgress() {
    return this.sitProgress;
  }

  public float getPrevSitProgress() {
    return this.prevSitProgress;
  }

  public void setTameProgress(float progress) {
    this.tameProgress = progress;
  }

  public void setPrevTameProgress(float progress) {
    this.prevTameProgress = progress;
  }

  public float getTameProgress() {
    return this.tameProgress;
  }

  public float getPrevTameProgress() {
    return this.prevTameProgress;
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
