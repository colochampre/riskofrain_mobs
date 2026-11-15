package io.github.colochampre.riskofrain_mobs.entities.enemies;

import io.github.colochampre.riskofrain_mobs.utils.ItemPickup;
import io.github.colochampre.riskofrain_mobs.utils.CarriedItemsSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.item.ItemStack;
import io.github.colochampre.riskofrain_mobs.registry.RoRConfigs;
import io.github.colochampre.riskofrain_mobs.registry.RoRSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import org.jetbrains.annotations.Nullable;

public class BeetleEntity extends Monster implements InventoryCarrier, CarriedItemsSource {
  private static final EntityDataAccessor<CompoundTag> DATA_CARRIED_ITEMS = SynchedEntityData.defineId(BeetleEntity.class, EntityDataSerializers.COMPOUND_TAG);
  private final SimpleContainer inventory = ItemPickup.createInventory(this, DATA_CARRIED_ITEMS);

  // Ram timing in ticks since the attack started, read by the server hit and the client model (S6):
  // wind-up 0-8 (neck and head pulled back), strike 8-10 (fast ram), contact at 10, hold 10-11, recovery 11-18.
  // The vanilla MeleeAttackGoal cooldown (20) must stay at least ATTACK_DURATION.
  public static final int ATTACK_WINDUP_END = 8;
  public static final int ATTACK_IMPACT_TICK = 10;
  public static final int ATTACK_HOLD_END = 11;
  public static final int ATTACK_DURATION = 18;
  private int attackTick;
  // Server only, not saved: an attack in progress is lost on unload
  @Nullable
  private LivingEntity pendingTarget;

  public BeetleEntity(EntityType<? extends Monster> type, Level level) {
    super(type, level);
    this.xpReward = 8;
    this.setPathfindingMalus(BlockPathTypes.POWDER_SNOW, -1.0F);
    this.setPathfindingMalus(BlockPathTypes.DANGER_POWDER_SNOW, -1.0F);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Monster.createMonsterAttributes()
            .add(Attributes.ARMOR, 2.0D)
            .add(Attributes.ATTACK_DAMAGE, 2.5D)
            .add(Attributes.FOLLOW_RANGE, 32.0D)
            .add(Attributes.MAX_HEALTH, 20.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.21D);
  }

  @Override
  protected void registerGoals() {
    this.goalSelector.addGoal(1, new FloatGoal(this));
    this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
    this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
    this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
    this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, WanderingTrader.class, true));
  }

  @Override
  protected void defineSynchedData() {
    super.defineSynchedData();
    ItemPickup.registerCarriedItems(this, DATA_CARRIED_ITEMS);
  }

  @Override
  public void aiStep() {
    super.aiStep();
    this.tickPendingHit();
    if (this.isAlive()) {
      if (this.isImmobile()) {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
      } else {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.21D);
      }
      if (this.attackTick > 0) {
        --this.attackTick;
      }
    }
  }

  @Nullable
  @Override
  public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType type, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
    this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(RoRConfigs.get().MOBS.BEETLES.ATTACK_DAMAGE);
    this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(RoRConfigs.get().MOBS.BEETLES.MAX_HEALTH);
    this.setHealth(this.getMaxHealth());
    return super.finalizeSpawn(level, difficulty, type, spawnData, dataTag);
  }

  @Override
  public boolean doHurtTarget(Entity target) {
    this.attackTick = ATTACK_DURATION;
    this.pendingTarget = target instanceof LivingEntity living ? living : null;
    this.level().broadcastEntityEvent(this, (byte) 4);
    this.playSound(this.getAttackSound(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
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
  }

  /**
   * Same reach and distance measure as vanilla 1.20.1 MeleeAttackGoal, which drives this mob:
   * getPerceivedTargetDistanceSquareForMeleeAttack(target) <= bbWidth * 2 * bbWidth * 2 + target.bbWidth.
   */
  public boolean isInMeleeReach(LivingEntity target) {
    double reachSqr = this.getBbWidth() * 2.0F * this.getBbWidth() * 2.0F + target.getBbWidth();
    return this.getPerceivedTargetDistanceSquareForMeleeAttack(target) <= reachSqr;
  }

  public float getAttackDamage() {
    // Difficulty scaling is left to vanilla, which already multiplies mob damage to players on HARD
    return (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
  }

  protected boolean isImmobile() {
    return super.isImmobile() || this.attackTick > 0;
  }

  @Override
  public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
    this.playSound(this.getStepSound(), 0.8F, 1.0F);
    this.playSound(this.getStepSound(), 0.8F, 1.0F);
    return super.causeFallDamage(fallDistance, multiplier, source);
  }

  public int getAttackTick() {
    return this.attackTick;
  }

  @Override
  public void handleEntityEvent(byte b) {
    if (b == 4) {
      this.attackTick = ATTACK_DURATION;
    } else {
      super.handleEntityEvent(b);
    }
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
    return !ItemPickup.hasItems(this) && RoRConfigs.get().MOBS.BEETLES.ENABLE_DESPAWN;
  }

  @Override
  protected SoundEvent getAmbientSound() {
    return RoRSounds.BEETLE_AMBIENT.get();
  }

  protected SoundEvent getAttackSound() {
    return RoRSounds.BEETLE_ATTACK.get();
  }

  @Override
  protected SoundEvent getDeathSound() {
    return RoRSounds.BEETLE_DEATH.get();
  }

  @Override
  protected SoundEvent getHurtSound(DamageSource source) {
    return RoRSounds.BEETLE_HURT.get();
  }

  protected SoundEvent getStepSound() {
    return RoRSounds.BEETLE_STEP.get();
  }

  protected void playStepSound(BlockPos pos, BlockState blockState) {
    this.playSound(this.getStepSound(), 0.25F, 1.0F);
  }

  @Override
  protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
    return 1.4375F;
  }

  public static boolean isMoving(LivingEntity entity) {
    return entity.getX() != entity.xOld || entity.getZ() != entity.zOld;
  }
}
