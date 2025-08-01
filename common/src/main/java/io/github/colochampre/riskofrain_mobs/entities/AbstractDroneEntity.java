package io.github.colochampre.riskofrain_mobs.entities;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class AbstractDroneEntity extends TamableAnimal {

  protected AbstractDroneEntity(EntityType<? extends AbstractDroneEntity> entityType, Level level) {
    super(entityType, level);
  }

  @Nullable
  @Override
  public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
    return null;
  }
}
