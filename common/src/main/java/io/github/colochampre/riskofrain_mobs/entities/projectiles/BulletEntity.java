package io.github.colochampre.riskofrain_mobs.entities.projectiles;

import io.github.colochampre.riskofrain_mobs.registry.RoREntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class BulletEntity extends ThrowableProjectile {
  private float damage = 2.0f;
  private LivingEntity owner;

  public BulletEntity(EntityType<? extends BulletEntity> type, Level level) {
    super(type, level);
  }

  public BulletEntity(Level level, LivingEntity owner) {
    this(RoREntityTypes.DRONE_BULLET_ENTITY.get(), level);
    this.owner = owner;
    this.setOwner(owner);
    this.setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
  }

  @Override
  protected void onHit(HitResult result) {
    super.onHit(result);
    if (!this.level().isClientSide) {
      // Add your hit logic here
      this.discard();
    }
  }

  @Override
  protected void onHitEntity(EntityHitResult result) {
    super.onHitEntity(result);
    if (!this.level().isClientSide) {
      // Add your entity hit logic here
      if (result.getEntity() instanceof LivingEntity target && target != this.owner) {
        target.hurt(this.damageSources().thrown(this, this.owner), damage);
      }
      this.discard();
    }
  }

  @Override
  public void tick() {
    super.tick();

    // Add particle effect
    if (this.level().isClientSide) {
      // Add particle effect every 4 ticks
      /*if (this.tickCount % 4 == 0) {
        this.level().addParticle(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
      }*/
    }
    // Remove the bullet if it's been alive for too long
    if (this.tickCount > 100) {
      this.discard();
    }
  }

  @Override
  protected void defineSynchedData() {
    // Required for entity data synchronization
  }
}
