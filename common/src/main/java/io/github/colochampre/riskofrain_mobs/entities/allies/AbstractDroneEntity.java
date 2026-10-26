package io.github.colochampre.riskofrain_mobs.entities.allies;

import com.google.common.collect.Sets;
import io.github.colochampre.riskofrain_mobs.entities.goals.DroneFollowOwnerGoal;
import io.github.colochampre.riskofrain_mobs.entities.projectiles.BulletEntity;
import io.github.colochampre.riskofrain_mobs.registry.RoRSounds;
import io.github.colochampre.riskofrain_mobs.utils.EntityUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractDroneEntity extends TamableAnimal implements RangedAttackMob {

  public static final int TYPE_LAND = 0;
  public static final int TYPE_FLYING = 1;
  public static final int MIN_FLIGHT_HEIGHT = 3;
  public static final int MAX_FLIGHT_HEIGHT = 8;
  protected static final float MAX_ROTATION_SPEED = Mth.PI * 0.3F;
  protected static final float ROTATION_ACCELERATION = 0.16F;
  protected static final float ROTATION_DECELERATION = 0.012F;
  private static final double ATTACKER_SEARCH_RADIUS = 64.0D;
  public static final Set<EntityType<?>> DO_NOT_ATTACK = Sets.newHashSet(EntityType.CREEPER, EntityType.PIGLIN,
          EntityType.PIGLIN_BRUTE, EntityType.ZOMBIFIED_PIGLIN, EntityType.HOGLIN, EntityType.ZOGLIN);
  private static final Set<Item> TAME_ITEMS = Sets.newHashSet(Items.GOLD_BLOCK, Items.RAW_GOLD_BLOCK, Items.GOLD_INGOT,
          Items.RAW_GOLD, Items.GOLD_NUGGET);
  private static final Set<Item> REPAIR_ITEMS = Sets.newHashSet(Items.IRON_INGOT, Items.IRON_NUGGET, Items.RAW_IRON);
  private static final EntityDataAccessor<Integer> DATA_PRICE = SynchedEntityData.defineId(AbstractDroneEntity.class,
          EntityDataSerializers.INT);
  private static final EntityDataAccessor<Integer> DATA_ID_ATTACK_TARGET = SynchedEntityData
          .defineId(AbstractDroneEntity.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Integer> DATA_BODY_COLOR = SynchedEntityData
          .defineId(AbstractDroneEntity.class, EntityDataSerializers.INT);
  private static final Map<Item, Integer> dronePriceMap = new HashMap<>();
  private LivingEntity clientSideCachedAttackTarget;
  private final RandomLookAroundGoal randomLookAroundGoal = new RandomLookAroundGoal(this);
  private final DroneFollowOwnerGoal followOwnerGoal = new DroneFollowOwnerGoal(this, 1.0D, 8.0F, 4.0F, true);
  private final WaterAvoidingRandomFlyingGoal randomFlyingGoal = new WaterAvoidingRandomFlyingGoal(this, 0.5D);
  private final SitWhenOrderedToGoal sitGoal = new SitWhenOrderedToGoal(this);
  private float bodyXRot;
  private float bodyZRot;
  private float gunSpeed;
  private float gunAngle;
  private float prevGunAngle;
  private int flyingSound;
  private int underWaterTicks;

  static {
    dronePriceMap.put(Items.GOLD_BLOCK, 81);
    dronePriceMap.put(Items.RAW_GOLD_BLOCK, 54);
    dronePriceMap.put(Items.GOLD_INGOT, 9);
    dronePriceMap.put(Items.RAW_GOLD, 6);
    dronePriceMap.put(Items.GOLD_NUGGET, 1);
  }

  protected AbstractDroneEntity(EntityType<? extends AbstractDroneEntity> entityType, Level level) {
    super(entityType, level);
    if (this.getDroneType() == TYPE_FLYING) {
      this.moveControl = new FlyingMoveControl(this, 16, true);
      this.setPathfindingMalus(BlockPathTypes.COCOA, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.DAMAGE_OTHER, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.FENCE, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 16.0F);
    }
  }

  public abstract int getDroneType();

  public abstract int getDronePrice();

  // Added while tamed and removed while untamed, like the other tame goals
  protected abstract Goal getAttackGoal();

  @Override
  protected void registerGoals() {
    this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
    this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
    this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Mob.class, 5, false, false, (entity)
            -> entity instanceof Enemy && !(DO_NOT_ATTACK.contains(entity.getType()))));
  }

  @Override
  protected void defineSynchedData() {
    super.defineSynchedData();
    this.entityData.define(DATA_PRICE, this.getInitialGoldPrice());
    this.entityData.define(DATA_ID_ATTACK_TARGET, 0);
    this.entityData.define(DATA_BODY_COLOR, DyeColor.LIGHT_BLUE.getId());
  }

  private int getInitialGoldPrice() {
    Difficulty difficulty = this.level().getDifficulty();
    return difficulty == Difficulty.HARD ? (int) (this.getDronePrice() * 1.5) : this.getDronePrice();
  }

  @Override
  public void addAdditionalSaveData(@NotNull CompoundTag tag) {
    super.addAdditionalSaveData(tag);
    tag.putByte("GoldPrice", (byte) this.getCurrentGoldPrice());
    tag.putByte("BodyColor", (byte) this.getBodyColor().getId());
  }

  @Override
  public void readAdditionalSaveData(@NotNull CompoundTag tag) {
    super.readAdditionalSaveData(tag);
    if (tag.contains("GoldPrice", 99)) {
      this.setCurrentGoldPrice(tag.getInt("GoldPrice"));
    }
    if (tag.contains("BodyColor", 99)) {
      this.setBodyColor(DyeColor.byId(tag.getInt("BodyColor")));
    }
  }

  @Override
  public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> accessor) {
    super.onSyncedDataUpdated(accessor);
    if (DATA_ID_ATTACK_TARGET.equals(accessor)) {
      this.clientSideCachedAttackTarget = null;
    }
  }

  public int getCurrentGoldPrice() {
    return this.entityData.get(DATA_PRICE);
  }

  public void setCurrentGoldPrice(int i) {
    this.entityData.set(DATA_PRICE, i);
  }

  @Override
  public void setTame(boolean tamed) {
    super.setTame(tamed);
    if (!tamed) {
      this.removeGoals();
      return;
    }
    int type = this.getDroneType();
    this.goalSelector.addGoal(2, sitGoal);
    if (type == TYPE_LAND) {
      this.addLandDroneGoals();
    }
    if (type == TYPE_FLYING) {
      this.addFlyingDroneGoals();
      this.setNoGravity(true);
    }
    this.goalSelector.addGoal(3, this.getAttackGoal());
  }

  private void addLandDroneGoals() {
    this.goalSelector.addGoal(8, randomLookAroundGoal);
  }

  private void addFlyingDroneGoals() {
    this.goalSelector.addGoal(4, followOwnerGoal);
    this.goalSelector.addGoal(5, randomFlyingGoal);
  }

  public void removeGoals() {
    int type = this.getDroneType();
    if (type == TYPE_LAND) {
      this.goalSelector.removeGoal(randomLookAroundGoal);
    }
    if (type == TYPE_FLYING) {
      this.goalSelector.removeGoal(followOwnerGoal);
      this.goalSelector.removeGoal(randomFlyingGoal);
      this.setNoGravity(false);
    }
    this.goalSelector.removeGoal(sitGoal);
    this.goalSelector.removeGoal(this.getAttackGoal());
  }

  @Override
  protected @NotNull PathNavigation createNavigation(Level level) {
    if (this.getDroneType() == TYPE_FLYING) {
      FlyingPathNavigation flyingPathNavigation = new FlyingPathNavigation(this, level) {
        public boolean isStableDestination(BlockPos pos) {
          return !this.level.getBlockState(pos.below()).isAir();
        }
      };
      flyingPathNavigation.setCanOpenDoors(false);
      flyingPathNavigation.setCanFloat(false);
      flyingPathNavigation.setCanPassDoors(true);
      return flyingPathNavigation;
    } else {
      return super.createNavigation(level);
    }
  }

  @Override
  public float getWalkTargetValue(BlockPos pos, LevelReader level) {
    if (this.getDroneType() == TYPE_FLYING) {
      return level.getBlockState(pos).isAir() ? 20.0F : 0.0F;
    } else {
      return super.getWalkTargetValue(pos, level);
    }
  }

  @Override
  public void aiStep() {
    super.aiStep();
    if (this.isTame()) {
      this.smokeIfLowHealth();
      if (this.getDroneType() == TYPE_FLYING) {
        this.doFlyingSound();
        this.landIfOrderedToSit();
      }
    }
    this.updateGun();
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

  @Override
  public void tick() {
    super.tick();
    if (this.isTame()) {
      this.takeWaterDamage();
      if (this.getDroneType() == TYPE_FLYING) {
        EntityUtils.updateMovementInclinations(this, this.bodyXRot, this.bodyZRot,
                newBodyXRot -> this.bodyXRot = newBodyXRot, newBodyZRot -> this.bodyZRot = newBodyZRot);
      }
    }
  }

  private void doFlyingSound() {
    if (!this.isTame() || this.onGround() || this.isInSittingPose() || this.isOrderedToSit()) {
      flyingSound = 0;
    } else {
      flyingSound = (flyingSound % 30) + 1;
    }
    if (flyingSound == 1) {
      this.playSound(this.getFlyingSound(), 0.05F, 1.0F);
    }
  }

  private void landIfOrderedToSit() {
    Vec3 vec3 = this.getDeltaMovement();
    if (this.isOrderedToSit()) {
      this.setDeltaMovement(this.getDeltaMovement().add(0.0D, ((double) -0.1F - vec3.y), 0.0D));
      this.hasImpulse = true;
    } else {
      this.stayElevated();
    }
  }

  private void stayElevated() {
    LivingEntity target = this.getTarget();
    double heightAboveGround = EntityUtils.getHeightAboveSurface(this);
    Vec3 vec3a = this.getDeltaMovement();
    if (heightAboveGround < MIN_FLIGHT_HEIGHT) {
      this.setDeltaMovement(this.getDeltaMovement().add(0.0D, ((double) 0.2F - vec3a.y) * (double) 0.2F, 0.0D));
      this.hasImpulse = true;
    } else if (this.getOwner() != null && !(this.getOwner().getEyeY() > this.getEyeY()) && target == null
            && heightAboveGround > MAX_FLIGHT_HEIGHT) {
      this.setDeltaMovement(this.getDeltaMovement().add(0.0D, ((double) -0.1F - vec3a.y), 0.0D));
      this.hasImpulse = true;
    }
  }

  private void smokeIfLowHealth() {
    if (this.isLowHealth() && this.tickCount % 2 == 0) {
      float f0 = ThreadLocalRandom.current().nextFloat();
      SimpleParticleType particle = f0 > 0.1 ? ParticleTypes.SMOKE : ParticleTypes.ELECTRIC_SPARK;
      EntityUtils.doParticlesAtEntity(this, particle, 2);
    }
  }

  public boolean isLowHealth() {
    double health = this.getHealth();
    return health < this.getMaxHealth() / 2;
  }

  private void takeWaterDamage() {
    if (this.isInWaterOrBubble()) {
      ++this.underWaterTicks;
    } else {
      this.underWaterTicks = 0;
    }
    if (this.underWaterTicks > 20) {
      this.hurt(this.damageSources().drown(), 1.0F);
    }
  }

  @Override
  public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                      @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
    if (this.getCurrentGoldPrice() > 0) {
      this.setPriceName();
    }
    return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
  }

  public void setPriceName() {
    String price = String.valueOf(this.getCurrentGoldPrice());
    Component component = Component.literal(price).withStyle(ChatFormatting.YELLOW);
    this.setCustomName(component);
    this.setCustomNameVisible(true);
  }

  @Override
  public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
    ItemStack itemstack = player.getItemInHand(hand);
    Item item = itemstack.getItem();
    if (this.isTame()) {
      // Set body color
      if (item instanceof DyeItem dyeItem && this.isOwnedBy(player) && dyeItem.getDyeColor() != this.getBodyColor()) {
        this.setBodyColor(dyeItem.getDyeColor());
        if (!player.getAbilities().instabuild) {
          itemstack.shrink(1);
        }
        return InteractionResult.SUCCESS;
      }
      // Repair
      if (this.getHealth() < this.getMaxHealth()) {
        if (REPAIR_ITEMS.contains(itemstack.getItem())) {
          if (!player.getAbilities().instabuild) {
            itemstack.shrink(1);
          }
          this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.IRON_GOLEM_REPAIR,
                  this.getSoundSource(), 0.5F, 1.25F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
          // Use the item captured before shrink(): an emptied stack reports AIR
          if (item == Items.IRON_INGOT) {
            this.heal(18.0F);
          } else if (item == Items.RAW_IRON) {
            this.heal(12.0F);
          } else {
            this.heal(2.0F);
          }
          return InteractionResult.SUCCESS;
        }
      }
      // Set sitting
      if (!(item instanceof DyeItem) && this.isOwnedBy(player)) {
        this.setOrderedToSit(!this.isOrderedToSit());
        this.navigation.stop();
        this.setTarget(null);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), RoRSounds.DRONE_REPAIR.get(),
                this.getSoundSource(), 0.2F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
        return InteractionResult.SUCCESS;
      }
    } else if (!this.isTame()) {
      // Not gold
      if (!TAME_ITEMS.contains(itemstack.getItem())) {
        Component goldMessage = Component.translatable("message.riskofrain_mobs.not_gold")
                .withStyle(ChatFormatting.YELLOW);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), RoRSounds.INSUFFICIENT_FOUNDS.get(),
                this.getSoundSource(), 0.5F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
        if (!this.level().isClientSide) {
          player.sendSystemMessage(goldMessage);
        }
        return InteractionResult.SUCCESS;
        // Taming
      } else if (TAME_ITEMS.contains(itemstack.getItem())) {
        if (!player.getAbilities().instabuild) {
          itemstack.shrink(1);
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), RoRSounds.COIN_PROC.get(),
                this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
        this.updateGoldPrice(item);
        String price = String.valueOf(this.getCurrentGoldPrice());
        Component priceName = Component.literal(price).withStyle(ChatFormatting.YELLOW);
        this.setCustomName(priceName);
        this.setCustomNameVisible(true);
        if (!this.level().isClientSide) {
          if (this.getCurrentGoldPrice() <= 0) {
            this.tame(player);
            this.level().playSound((Player) null, this.getX(), this.getY(), this.getZ(), RoRSounds.DRONE_REPAIR.get(),
                    this.getSoundSource(), 0.6F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
            this.level().broadcastEntityEvent(this, (byte) 7);
            this.setCustomName(null);
            this.setCustomNameVisible(false);
          } else {
            this.level().broadcastEntityEvent(this, (byte) 6);
          }
        }
        return InteractionResult.SUCCESS;
      }
    }
    return super.mobInteract(player, hand);
  }

  private void updateGoldPrice(Item item) {
    int priceReduction = dronePriceMap.getOrDefault(item, 0);
    this.setCurrentGoldPrice(this.getCurrentGoldPrice() - priceReduction);
  }

  @Override
  protected void spawnTamingParticles(boolean tamed) {
    SimpleParticleType particle = tamed ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.SMOKE;
    EntityUtils.doParticlesAtEntity(this, particle, 5);
  }

  @Override
  public boolean canBeLeashed(Player player) {
    return (this.isTame() && !this.isInSittingPose());
  }

  @Override
  public boolean hurt(DamageSource source, float damage) {
    if (this.isInvulnerableTo(source)) {
      return false;
    } else {
      Entity entity = source.getEntity();
      if (!this.level().isClientSide) {
        this.setOrderedToSit(false);
      }
      if (entity != null && !(entity instanceof Player) && !(entity instanceof AbstractArrow)) {
        damage = (damage + 1.0F) / 2.0F;
      }
      return super.hurt(source, damage);
    }
  }

  @Override
  protected int decreaseAirSupply(int currentAir) {
    return currentAir;
  }

  @Override
  public boolean canBeAffected(MobEffectInstance effect) {
    if (effect.getEffect() == MobEffects.POISON) {
      return false;
    }
    return super.canBeAffected(effect);
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
    } else if (target instanceof Player player && owner instanceof Player ownerPlayer && !ownerPlayer.canHarmPlayer(player)) {
      // Respects server PvP and team friendly fire, like tamed wolves
      return false;
    } else if (target instanceof AbstractHorse && ((AbstractHorse) target).isTamed()) {
      return false;
    } else if (target instanceof AbstractDroneEntity && ((AbstractDroneEntity) target).isTame()) {
      return false;
    } else {
      return !(target instanceof TamableAnimal) || !((TamableAnimal) target).isTame();
    }
  }

  public boolean isFlying() {
    return this.getDroneType() == TYPE_FLYING && !this.onGround();
  }

  @Override
  public void die(@NotNull DamageSource source) {
    this.playSound(this.getShutDownSound(), 1.0F, 1.0F);
    // Tamed drones break down instead of dying, unless the damage bypasses invulnerability (/kill, the void).
    // Returning before super.die also skips the owner death message (vanilla sends it before super.die).
    // Server only: the client runs die() from entity event 3 for a real death, and must play the death animation
    if (!this.level().isClientSide() && this.isTame() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
      this.breakDown();
      return;
    }
    super.die(source);
  }

  // Back to the untamed, for-sale state, with nothing left over from the fight or the previous owner
  private void breakDown() {
    this.setTame(false);
    this.setOwnerUUID(null);
    this.setOrderedToSit(false);
    this.setInSittingPose(false);
    this.navigation.stop();
    this.setTarget(null);
    this.setActiveAttackTarget(0);
    this.calmAttackers();
    this.setLastHurtByMob(null);
    // Clear the fields directly: setLastHurtByPlayer(null) restarts the player-kill XP window
    this.lastHurtByPlayer = null;
    this.lastHurtByPlayerTime = 0;
    if (this.isLeashed()) {
      this.dropLeash(true, true);
    }
    this.clearFire();
    this.removeAllEffects();
    this.setHealth(this.getMaxHealth());
    this.setCurrentGoldPrice(this.getInitialGoldPrice());
    this.setPriceName();
  }

  // Untamed drones are inert shop items. Running target goals fall back to their cached target when the mob's
  // target is cleared, and only let go once canAttack fails, which ends in this check
  @Override
  public boolean canBeSeenAsEnemy() {
    return this.isTame() && super.canBeSeenAsEnemy();
  }

  // A broken drone is still alive, so mobs fighting it would keep hitting it: make them drop it as a target
  private void calmAttackers() {
    AABB area = this.getBoundingBox().inflate(ATTACKER_SEARCH_RADIUS);
    for (Mob mob : this.level().getEntitiesOfClass(Mob.class, area, mob -> mob.getTarget() == this)) {
      if (mob instanceof NeutralMob neutralMob && this.getUUID().equals(neutralMob.getPersistentAngerTarget())) {
        neutralMob.stopBeingAngry();
      }
      if (mob.getLastHurtByMob() == this) {
        mob.setLastHurtByMob(null);
      }
      mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
      mob.setTarget(null);
    }
  }

  @Nullable
  public LivingEntity getActiveAttackTarget() {
    if (!this.hasActiveAttackTarget()) {
      return null;
    } else if (this.level().isClientSide) {
      if (this.clientSideCachedAttackTarget != null) {
        return this.clientSideCachedAttackTarget;
      } else {
        Entity entity = this.level().getEntity(this.entityData.get(DATA_ID_ATTACK_TARGET));
        if (entity instanceof LivingEntity) {
          this.clientSideCachedAttackTarget = (LivingEntity) entity;
          return this.clientSideCachedAttackTarget;
        } else {
          return null;
        }
      }
    } else {
      return this.getTarget();
    }
  }

  public void setActiveAttackTarget(int id) {
    this.entityData.set(DATA_ID_ATTACK_TARGET, id);
  }

  public boolean hasActiveAttackTarget() {
    return this.entityData.get(DATA_ID_ATTACK_TARGET) != 0;
  }

  @Override
  protected SoundEvent getDeathSound() {
    return RoRSounds.DRONE_DEATH.get();
  }

  protected SoundEvent getShutDownSound() {
    return RoRSounds.DRONE_BREAKS.get();
  }

  protected SoundEvent getFlyingSound() {
    return RoRSounds.DRONE_FLYING.get();
  }

  public DyeColor getBodyColor() {
    return DyeColor.byId(this.entityData.get(DATA_BODY_COLOR));
  }

  public void setBodyColor(DyeColor color) {
    this.entityData.set(DATA_BODY_COLOR, color.getId());
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

  public float getBodyXRot() {
    return this.bodyXRot;
  }

  public float getBodyZRot() {
    return this.bodyZRot;
  }

  @Nullable
  @Override
  public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
    return null;
  }
}
