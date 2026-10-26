package io.github.colochampre.riskofrain_mobs.entities.allies;

import io.github.colochampre.riskofrain_mobs.entities.goals.GunnerDroneAttackGoal;
import io.github.colochampre.riskofrain_mobs.registry.RoRConfigs;
import io.github.colochampre.riskofrain_mobs.utils.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GunnerDroneEntity extends AbstractDroneEntity {
  private final GunnerDroneAttackGoal attackGoal = new GunnerDroneAttackGoal(this, 16.0F);
  private float propellerSpeed;
  private float propellerAngle;
  private float prevPropellerAngle;

  public GunnerDroneEntity(EntityType<? extends AbstractDroneEntity> entityType, Level level) {
    super(entityType, level);
  }

  @Override
  public int getDroneType() {
    return TYPE_FLYING;
  }

  @Override
  public int getDronePrice() {
    return 36;
  }

  @Override
  protected Goal getAttackGoal() {
    return this.attackGoal;
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Mob.createMobAttributes()
            .add(Attributes.ARMOR, 2.0D)
            .add(Attributes.ATTACK_DAMAGE, 2.0D)
            .add(Attributes.FLYING_SPEED, 1.0D)
            .add(Attributes.FOLLOW_RANGE, 16.0D)
            .add(Attributes.MAX_HEALTH, 20.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.0D);
  }

  @Override
  public void aiStep() {
    super.aiStep();
    this.updatePropeller();
  }

  @Override
  public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType type, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
    this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(RoRConfigs.get().MOBS.DRONES.BULLETS_DAMAGE);
    if (RoRConfigs.get().MOBS.DRONES.GUNNER_DRONE_MAX_HEALTH > 0) {
      this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(RoRConfigs.get().MOBS.DRONES.GUNNER_DRONE_MAX_HEALTH);
    }
    this.setHealth(this.getMaxHealth());
    return super.finalizeSpawn(level, difficulty, type, spawnData, dataTag);
  }

  public static boolean checkDroneSpawnRules(EntityType<GunnerDroneEntity> entity, LevelAccessor level, MobSpawnType type, BlockPos pos, RandomSource randomSource) {
    return (level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON)
            || level.getBlockState(pos.below()).is(BlockTags.RABBITS_SPAWNABLE_ON))
            && isBrightEnoughToSpawn(level, pos);
  }

  @Override
  public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
    return false;
  }

  @Override
  protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
  }

  private void updatePropeller() {
    this.prevPropellerAngle = this.propellerAngle;
    if (this.isFlying()) {
      this.propellerSpeed = Math.min(this.propellerSpeed + ROTATION_ACCELERATION, MAX_ROTATION_SPEED);
    } else if (this.onGround() || !this.isTame()) {
      this.propellerSpeed = Math.max(this.propellerSpeed - ROTATION_DECELERATION, 0.0F);
      if (this.propellerSpeed == 0) {
        propellerAngle = EntityUtils.normalizeAngle(this.propellerAngle);
      }
    }
    this.propellerAngle += this.propellerSpeed;
  }

  public float getPropellerAngle() {
    return this.propellerAngle;
  }

  public float getPrevPropellerAngle() {
    return this.prevPropellerAngle;
  }

  public float getPropellerSpeed() {
    return this.propellerSpeed;
  }

  @Override
  protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
    return 0.055F;
  }

  @Override
  protected @NotNull Vec3 getLeashOffset() {
    return new Vec3(0.0D, (0.6F * this.getEyeHeight()), (this.getBbWidth() * 0.2F));
  }
}
