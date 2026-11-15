package io.github.colochampre.riskofrain_mobs.client.animation;

import net.minecraft.util.Mth;

/**
 * Per-entity walk cycle position whose advance per tick is capped, so a speed boost does not make the walk
 * cycle faster. Vanilla's {@code WalkAnimationState.position} advances by the uncapped smoothed speed
 * ({@code min(distance * 4, 1)}), which grows with movement speed.
 * <p>
 * Plain data with no client-only imports, so common entity code can hold one. Tick it once per client tick from the
 * entity (after the vanilla walk animation was updated) and read it from the model with the render partial tick.
 */
public final class WalkClock {
  private float positionOld;
  private float position;

  /**
   * Advances the clock by {@code min(walkSpeed, maxSpeed)}, the same step vanilla applies to its own position but
   * capped. Call exactly once per client tick.
   */
  public void tick(float walkSpeed, float maxSpeed) {
    this.positionOld = this.position;
    this.position += Math.min(walkSpeed, maxSpeed);
  }

  /** Position for the model, interpolated between the last two ticks. */
  public float position(float partialTick) {
    return Mth.lerp(partialTick, this.positionOld, this.position);
  }
}
