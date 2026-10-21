package io.github.colochampre.riskofrain_mobs.entities.goals;

import io.github.colochampre.riskofrain_mobs.entities.allies.GunnerTurretEntity;
import io.github.colochampre.riskofrain_mobs.registry.RoRSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class GunnerTurretAttackGoal extends Goal {

  // Distance from the turret's eye towards the target where the muzzle flash appears
  private static final double MUZZLE_OFFSET = 1.0D;
  private final GunnerTurretEntity drone;
  private final float maxAttackDistance;
  private int attackStep;
  private int attackTime;

  public GunnerTurretAttackGoal(GunnerTurretEntity entity, float maxAttackDistance) {
    this.drone = entity;
    this.maxAttackDistance = maxAttackDistance * maxAttackDistance;
    this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
  }

  @Override
  public boolean canUse() {
    LivingEntity target = this.drone.getTarget();
    return target != null && target.isAlive() && this.drone.canAttack(target);
  }

  @Override
  public void start() {
    super.start();
    this.drone.setAggressive(true);
    this.attackStep = 0;
  }

  @Override
  public void stop() {
    this.drone.setActiveAttackTarget(0);
    this.drone.setTarget(null);
    this.drone.setAggressive(false);
  }

  @Override
  public boolean requiresUpdateEveryTick() {
    return true;
  }

  @Override
  public void tick() {
    super.tick();
    LivingEntity target = this.drone.getTarget();
    --this.attackTime;
    if (target != null) {
      boolean canSee = this.drone.getSensing().hasLineOfSight(target);
      double distance = this.drone.distanceToSqr(target);
      if (canSee) {
        this.drone.getLookControl().setLookAt(target, 30.0F, 90.0F);
        this.drone.setActiveAttackTarget(target.getId());
        if (distance < (double) this.maxAttackDistance) {
          shootTarget(target);
        }
      }
    }
  }

  private void shootTarget(LivingEntity target) {
    if (this.attackTime <= 0) {
      ++this.attackStep;
      if (this.attackStep == 1) {
        this.attackTime = 40;
      } else if (this.attackStep <= 40) {
        this.attackTime = 2;
      } else {
        this.attackTime = 40;
        this.attackStep = 0;
        this.drone.setActiveAttackTarget(0);
        this.drone.setTarget(null);
      }
      if (this.attackStep > 1) {
        if (!this.drone.isSilent()) {
          this.drone.playSound(this.getShootSound(), 1.5F, 1.6F);
          // Goals only tick on the server, where addParticle is a no-op: send the particle to clients instead
          if (this.drone.level() instanceof ServerLevel serverLevel) {
            Vec3 muzzle = this.drone.getEyePosition()
                    .add(target.getEyePosition().subtract(this.drone.getEyePosition()).normalize().scale(MUZZLE_OFFSET));
            serverLevel.sendParticles(ParticleTypes.CRIT, muzzle.x, muzzle.y, muzzle.z, 4, 0.05D, 0.05D, 0.05D, 0.1D);
          }
        }
        for (int i = 0; i < 1; ++i) {
          this.drone.performRangedAttack(target, this.maxAttackDistance);
        }
      }
      this.drone.getLookControl().setLookAt(target, 30.0F, 90.0F);
    }
  }

  protected SoundEvent getShootSound() {
    return RoRSounds.DRONE_SHOOT.get();
  }
}
