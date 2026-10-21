package io.github.colochampre.riskofrain_mobs.entities.projectiles;

import io.github.colochampre.riskofrain_mobs.entities.allies.AbstractDroneEntity;
import io.github.colochampre.riskofrain_mobs.registry.RoRConfigs;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityTypes;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.UUID;

public class BulletEntity extends ThrowableProjectile {
  private float damage = (float) RoRConfigs.get().MOBS.DRONES.BULLETS_DAMAGE;
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
      this.discard();
    }
  }

  @Override
  protected boolean canHitEntity(Entity entity) {
    return super.canHitEntity(entity) && !this.isFriendly(entity);
  }

  // Shots from a tamed drone pass through its owner and the owner's other pets, so they never start infighting
  private boolean isFriendly(Entity entity) {
    if (!(this.getOwner() instanceof AbstractDroneEntity shooter) || !shooter.isTame()) {
      return false;
    }
    UUID ownerId = shooter.getOwnerUUID();
    if (ownerId == null) {
      return false;
    }
    return entity == shooter
        || ownerId.equals(entity.getUUID())
        || (entity instanceof TamableAnimal pet && ownerId.equals(pet.getOwnerUUID()));
  }

  @Override
  protected void onHitEntity(EntityHitResult result) {
    super.onHitEntity(result);
    if (!this.level().isClientSide) {
      if (result.getEntity() instanceof LivingEntity target && target != this.owner) {
        target.hurt(this.damageSources().thrown(this, this.owner), damage);
      }
      this.discard();
    }
  }

  @Override
  public void tick() {
    super.tick();
    // Remove the bullet if it's been alive for too long
    if (this.tickCount > 100) {
      this.discard();
    }
  }

  @Override
  protected void defineSynchedData() {
  }
}
