package io.github.colochampre.riskofrain_mobs.entities.enemies;

import io.github.colochampre.riskofrain_mobs.utils.ItemPickup;
import io.github.colochampre.riskofrain_mobs.utils.CarriedItemsSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.item.ItemStack;
import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.entities.goals.StoneGolemAttackGoal;
import io.github.colochampre.riskofrain_mobs.client.animation.WalkClock;
import io.github.colochampre.riskofrain_mobs.registry.RoRConfigs;
import io.github.colochampre.riskofrain_mobs.registry.RoRSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public class StoneGolemEntity extends Monster implements InventoryCarrier, CarriedItemsSource {
  private static final EntityDataAccessor<CompoundTag> DATA_CARRIED_ITEMS = SynchedEntityData.defineId(StoneGolemEntity.class, EntityDataSerializers.COMPOUND_TAG);
  private final SimpleContainer inventory = ItemPickup.createInventory(this, DATA_CARRIED_ITEMS);

  private static final int TARGET_UNSEEN_MEMORY_TICKS = 20;
  private static final EntityDataAccessor<Integer> DATA_ID_ATTACK_TARGET = SynchedEntityData.defineId(StoneGolemEntity.class, EntityDataSerializers.INT);
  private LivingEntity clientSideCachedAttackTarget;
  public int clientSideAttackTime;
  // Melee attack timeline in ticks since the attack started; the model reads these, so retiming is done here.
  // Wind-up (arms open) 0..ATTACK_WINDUP_END, strike (arms close) ..ATTACK_IMPACT_TICK, hold ..ATTACK_HOLD_END,
  // then recovery to the rest pose until ATTACK_DURATION. The attack goal cooldown (40) must stay above the duration.
  public static final int ATTACK_WINDUP_END = 9;
  public static final int ATTACK_IMPACT_TICK = 13;
  public static final int ATTACK_HOLD_END = 15;
  public static final int ATTACK_DURATION = 22;
  private int attackTick;
  /**
   * Cap for the walk swing amount of every walk part (torso, arms and legs): the steady-state
   * `min(distance * 4, 1)` at the normal chase speed (speed 0.25 x 0.8, about 0.345).
   */
  public static final float WALK_MAX_AMOUNT = 0.35F;
  /** Vanilla walk position units between footfalls: the legs use cos(limbSwing * 0.5), one footfall every 2 PI. */
  public static final float STEP_INTERVAL = (float) (2.0D * Math.PI);
  /** Vanilla walk position of the first footfall, so a contact lands at a leg extreme. */
  public static final float STEP_OFFSET = 0.0F;
  /** Volume of the walking step, played by {@link #playWalkStep()} instead of the vanilla distance based step. */
  private static final float STEP_VOLUME = 3.0F;
  // Server only, not saved: an attack in progress is lost on unload
  @Nullable
  private LivingEntity pendingTarget;

  public StoneGolemEntity(EntityType<? extends Monster> type, Level level) {
    super(type, level);
    this.setMaxUpStep(1.0F);
    this.xpReward = 24;
    this.setPathfindingMalus(BlockPathTypes.LEAVES, 0.0F);
  }

  @Override
  protected void registerGoals() {
    this.goalSelector.addGoal(4, new StoneGolemAttackGoal(this, 0.8D, true));
    this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6D));
    this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
    this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    // Short unseen memory so a hidden target is dropped quickly in favor of a visible one
    this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
      @Override
      public void start() {
        super.start();
        // Vanilla start() hardcodes 300 ticks of memory, overriding setUnseenMemoryTicks
        this.unseenMemoryTicks = TARGET_UNSEEN_MEMORY_TICKS;
      }
    });
    this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true).setUnseenMemoryTicks(TARGET_UNSEEN_MEMORY_TICKS));
    this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true).setUnseenMemoryTicks(TARGET_UNSEEN_MEMORY_TICKS));
    this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, WanderingTrader.class, true).setUnseenMemoryTicks(TARGET_UNSEEN_MEMORY_TICKS));
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Monster.createMonsterAttributes()
            .add(Attributes.ARMOR, 2.0D)
            .add(Attributes.ATTACK_DAMAGE, 24.0D)
            .add(Attributes.ATTACK_KNOCKBACK, 2.0D)
            .add(Attributes.FOLLOW_RANGE, 24.0D)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
            .add(Attributes.MAX_HEALTH, 100.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.25D);
  }

  @Override
  protected void defineSynchedData() {
    super.defineSynchedData();
    this.entityData.define(DATA_ID_ATTACK_TARGET, 0);
    ItemPickup.registerCarriedItems(this, DATA_CARRIED_ITEMS);
  }

  @Override
  public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
    super.onSyncedDataUpdated(accessor);
    if (DATA_ID_ATTACK_TARGET.equals(accessor)) {
      this.clientSideAttackTime = 0;
      this.clientSideCachedAttackTarget = null;
    }
  }

  @Override
  public void aiStep() {
    super.aiStep();
    if (this.level().isClientSide) {
      this.playWalkStep();
    }
    this.tickPendingHit();
    if (this.isAlive()) {
      if (this.isImmobile()) {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
      } else {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.25D);
      }
      if (this.attackTick > 0) {
        --this.attackTick;
      }
      destroyLeavesBlocks();
      doFloorParticleEffect();
      doLaserParticleEffects();
    }
  }

  @Override
  public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
    this.playSound(this.getStepSound(), 3, 1.0F);
    this.playSound(this.getStepSound(), 3, 1.2F);
    return false;
  }

  @Override
  protected int decreaseAirSupply(int currentAir) {
    return currentAir;
  }

  private void destroyLeavesBlocks() {
    if (this.horizontalCollision && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
      boolean flag = false;
      AABB aabb = this.getBoundingBox().inflate(0.2D);
      for (BlockPos blockpos : BlockPos.betweenClosed(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ), Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ))) {
        BlockState blockstate = this.level().getBlockState(blockpos);
        Block block = blockstate.getBlock();
        if (block instanceof LeavesBlock) {
          flag = this.level().destroyBlock(blockpos, true, this) || flag;
        }
      }
    }
  }

  @Override
  public boolean doHurtTarget(Entity target) {
    this.attackTick = ATTACK_DURATION;
    this.pendingTarget = target instanceof LivingEntity living ? living : null;
    this.level().broadcastEntityEvent(this, (byte) 4);
    this.playSound(RoRSounds.STONE_GOLEM_CLAP.get(), 3.0F, 1.0F);
    return true;
  }

  /**
   * Server only. Runs in aiStep before the attackTick decrement, so on the tick doHurtTarget starts the attack
   * the elapsed time is 0, and the hit fires ATTACK_IMPACT_TICK ticks after that tick.
   */
  private void tickPendingHit() {
    if (this.pendingTarget == null) {
      return;
    }
    if (!this.isAlive()) {
      this.pendingTarget = null;
    } else if (ATTACK_DURATION - this.attackTick >= ATTACK_IMPACT_TICK) {
      LivingEntity target = this.pendingTarget;
      this.pendingTarget = null;
      this.performHit(target);
    }
  }

  private void performHit(LivingEntity target) {
    if (!target.isAlive() || target.isRemoved() || !this.isInMeleeReach(target)) {
      return;
    }
    target.hurt(this.damageSources().mobAttack(this), this.getAttackDamage());
    this.strongKnockback(target);
  }

  /** Single source of the melee reach, shared by the attack goal and the deferred hit check. */
  public double getAttackReachSqr(LivingEntity target) {
    return this.getBbWidth() * 1.5F * this.getBbWidth() * 1.5F + target.getBbWidth();
  }

  public boolean isInMeleeReach(LivingEntity target) {
    return this.getPerceivedTargetDistanceSquareForMeleeAttack(target) <= this.getAttackReachSqr(target);
  }

  private void doFloorParticleEffect() {
    if (this.getDeltaMovement().horizontalDistanceSqr() > (double) 2.5000003E-7F && this.random.nextInt(5) == 0) {
      int i = Mth.floor(this.getX());
      int j = Mth.floor(this.getY() - (double) 0.2F);
      int k = Mth.floor(this.getZ());
      BlockPos pos = new BlockPos(i, j, k);
      BlockState blockstate = this.level().getBlockState(pos);
      if (!blockstate.isAir()) {
        this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate), false, this.getX() + ((double) this.random.nextFloat() - 0.5D) * (double) this.getBbWidth(), this.getY() + 0.1D, this.getZ() + ((double) this.random.nextFloat() - 0.5D) * (double) this.getBbWidth(), 4.0D * ((double) this.random.nextFloat() - 0.5D), 0.5D, ((double) this.random.nextFloat() - 0.5D) * 4.0D);
      }
    }
  }

  private void doLaserParticleEffects() {
    if (this.isAlive()) {
      if (this.level().isClientSide) {
        if (this.hasActiveAttackTarget()) {
          if (this.clientSideAttackTime < this.getAttackDuration()) {
            ++this.clientSideAttackTime;
          }
          LivingEntity livingentity = this.getActiveAttackTarget();
          if (livingentity != null) {
            this.getLookControl().setLookAt(livingentity, 90.0F, 90.0F);
            this.getLookControl().tick();
            double d5 = this.getAttackAnimationScale(0.0F);
            double d0 = livingentity.getX() - this.getX();
            double d1 = livingentity.getY(0.5D) - this.getEyeY();
            double d2 = livingentity.getZ() - this.getZ();
            double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
            d0 /= d3;
            d1 /= d3;
            d2 /= d3;
            double d4 = ThreadLocalRandom.current().nextDouble();
            while (d4 < d3 - 2) {
              d4 += 1.8D - d5 + ThreadLocalRandom.current().nextDouble() * (1.7D - d5);
              this.level().addParticle(ParticleTypes.SMOKE, this.getX() + d0 * d4, this.getEyeY() + d1 * d4, this.getZ() + d2 * d4, 0.0D, 0.0D, 0.0D);
            }
          }
        }
      }
      if (this.hasActiveAttackTarget()) {
        this.setYRot(this.yHeadRot);
      }
    }
  }

  @Override
  protected boolean isImmobile() {
    return super.isImmobile() || this.attackTick > 0;
  }

  @Nullable
  @Override
  public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
    this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(RoRConfigs.get().MOBS.STONE_GOLEMS.ATTACK_DAMAGE);
    this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(RoRConfigs.get().MOBS.STONE_GOLEMS.MAX_HEALTH);
    this.setHealth(this.getMaxHealth());
    double d0 = RoRConfigs.get().MOBS.STONE_GOLEMS.SPAWN_VOLUME;
    if (d0 > 0) {
      this.playSound(this.getSpawnSound(), (float) ((d0 * 3) / 100), 1.0F);
    }
    return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
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

  public float getAttackAnimationScale(float scale) {
    return ((float) this.clientSideAttackTime + scale) / (float) this.getAttackDuration();
  }

  public int getAttackDuration() {
    return 75;
  }

  public float getAttackDamage() {
    // Difficulty scaling is left to vanilla, which already multiplies mob damage to players on HARD
    return (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
  }

  public int getAttackTick() {
    return this.attackTick;
  }

  @Override
  protected SoundEvent getAmbientSound() {
    return null;
  }

  protected SoundEvent getAttackSound() {
    return RoRSounds.STONE_GOLEM_CLAP.get();
  }

  @Override
  protected SoundEvent getDeathSound() {
    return RoRSounds.STONE_GOLEM_DEATH.get();
  }

  protected SoundEvent getDeathVoiceSound() {
    return RoRSounds.STONE_GOLEM_GROWL.get();
  }

  @Override
  public void die(@NotNull DamageSource src) {
    this.playSound(this.getDeathVoiceSound(), 1.5F, 1.0F);
    super.die(src);
  }

  @Override
  protected SoundEvent getHurtSound(@NotNull DamageSource source) {
    return RoRSounds.STONE_GOLEM_HURT.get();
  }

  protected SoundEvent getSpawnSound() {
    return RoRSounds.STONE_GOLEM_SPAWN.get();
  }

  protected SoundEvent getStepSound() {
    return RoRSounds.STONE_GOLEM_STEP.get();
  }

  /** Vanilla's distance based walking step is off: steps follow the walk animation, see {@link #playWalkStep()}. */
  @Override
  protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState blockState) {
  }

  /**
   * Client only. Entity.playSound has a null player and is silent on the client, so the step is played locally when
   * the vanilla walk position crosses a foot contact. aiStep has already advanced the position this tick (travel
   * calls calculateEntityAnimation), so the previous position is position - speed. The server keeps emitting the
   * step game event (sculk vibrations).
   */
  private void playWalkStep() {
    float speed = this.walkAnimation.speed();
    if (this.isSilent() || !this.onGround() || speed <= WalkClock.MIN_STEP_SPEED) {
      return;
    }
    float position = this.walkAnimation.position();
    if (WalkClock.crossedContact(position - speed, position, STEP_INTERVAL, STEP_OFFSET)) {
      this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), this.getStepSound(), this.getSoundSource(),
          STEP_VOLUME, 1.0F, false);
    }
  }

  @Override
  protected @NotNull ResourceLocation getDefaultLootTable() {
    return new ResourceLocation(RoRMod.MOD_ID, "entities/stone_golem");
  }

  @Override
  public int getMaxFallDistance() {
    return 24;
  }

  @Override
  protected float getStandingEyeHeight(@NotNull Pose pose, @NotNull EntityDimensions dimensions) {
    return 3.5F;
  }

  @Override
  public void handleEntityEvent(byte b) {
    if (b == 4) {
      this.attackTick = ATTACK_DURATION;
      this.playSound(RoRSounds.STONE_GOLEM_CLAP.get(), 3.0F, 1.0F);
    } else {
      super.handleEntityEvent(b);
    }
  }

  @Override
  public boolean isInvulnerableTo(DamageSource source) {
    // Check the damage type: an identity check misses damage built from another DamageSource instance
    return source.is(DamageTypes.FREEZE) ||
            source.is(DamageTypes.HOT_FLOOR) ||
            source.is(DamageTypes.IN_WALL) ||
            source.is(DamageTypes.IN_FIRE) ||
            source.is(DamageTypes.LAVA) ||
            source.is(DamageTypes.ON_FIRE) ||
            super.isInvulnerableTo(source);
  }

  @Override
  public boolean canBeAffected(MobEffectInstance effect) {
    if (effect.getEffect() == MobEffects.POISON) {
      return false;
    }
    return super.canBeAffected(effect);
  }

  public static boolean isMoving(LivingEntity entity) {
    return entity.getX() != entity.xOld || entity.getZ() != entity.zOld;
  }

  @Override
  public boolean isOnFire() {
    return false;
  }

  public boolean isPushedByFluid() {
    return false;
  }

  @Override
  public void addAdditionalSaveData(CompoundTag nbt) {
    super.addAdditionalSaveData(nbt);
    ItemPickup.save(this, nbt);
  }

  @Override
  public void readAdditionalSaveData(CompoundTag nbt) {
    super.readAdditionalSaveData(nbt);
    ItemPickup.load(this, this, nbt);
  }

  @Override
  public CompoundTag getCarriedItemsTag() {
    return this.entityData.get(DATA_CARRIED_ITEMS);
  }

  @Override
  public SimpleContainer getInventory() {
    return this.inventory;
  }

  // Read at runtime so a change in the config GUI applies without a restart
  @Override
  public boolean canPickUpLoot() {
    return ItemPickup.enemiesCanPickUp();
  }

  @Override
  public boolean wantsToPickUp(ItemStack stack) {
    return ItemPickup.wantsToPickUp(this, stack);
  }

  @Override
  protected void pickUpItem(ItemEntity itemEntity) {
    ItemPickup.pickUp(this, this, itemEntity);
  }

  @Override
  protected void dropEquipment() {
    super.dropEquipment();
    ItemPickup.dropAll(this, this);
  }

  @Override
  public void checkDespawn() {
    ItemPickup.dropBeforePeacefulDespawn(this, this);
    super.checkDespawn();
  }

  @Override
  public boolean removeWhenFarAway(double distanceToClosestPlayer) {
    // A mob that carries an item never despawns, whatever the setting says
    return !ItemPickup.hasItems(this) && RoRConfigs.get().MOBS.STONE_GOLEMS.ENABLE_DESPAWN;
  }

  public void strongKnockback(Entity entity) {
    double d0 = entity.getX() - this.getX();
    double d1 = entity.getZ() - this.getZ();
    double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
    entity.push(d0 / d2 * 4.0D, 0.2D, d1 / d2 * 4.0D);
  }
}
