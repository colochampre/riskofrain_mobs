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
  /**
   * Walk speed (vanilla smoothed {@code walkAnimation.speed()}) under which no step sound is played, so a mob that is
   * stopping does not click while its legs barely move.
   */
  public static final float MIN_STEP_SPEED = 0.1F;

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

  /**
   * Whether the last {@link #tick} moved the clock across a foot contact. Contacts are at
   * {@code offset + n * interval} clock units for any integer n, so a model whose legs use {@code cos(clock * k)}
   * has its leg extremes (a footfall) every {@code PI / k} units with offset 0.
   */
  public boolean crossedContact(float interval, float offset) {
    return Mth.floor((this.position - offset) / interval) > Mth.floor((this.positionOld - offset) / interval);
  }
}
