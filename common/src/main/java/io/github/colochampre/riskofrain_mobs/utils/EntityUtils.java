package io.github.colochampre.riskofrain_mobs.utils;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.LivingEntity;

import java.util.concurrent.ThreadLocalRandom;

public class EntityUtils {

  public static void doParticlesAtEntity(LivingEntity entity, SimpleParticleType particle, int amount) {
    if (entity.level().isClientSide) {
      for (int i = 0; i < amount; ++i) {
        double randomX = entity.getX((2.0D * ThreadLocalRandom.current().nextDouble() - 1.0D) * 0.5);
        double randomY = entity.getY(ThreadLocalRandom.current().nextDouble());
        double randomZ = entity.getZ((2.0D * ThreadLocalRandom.current().nextDouble() - 1.0D) * 0.5);
        entity.level().addParticle(particle, randomX, randomY, randomZ, 0.0D, 0.0D, 0.0D);
      }
    }
  }
}
