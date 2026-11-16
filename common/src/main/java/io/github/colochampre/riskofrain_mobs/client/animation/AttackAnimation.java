package io.github.colochampre.riskofrain_mobs.client.animation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;

/**
 * Helpers to build attack animations out of phases (wind-up, strike, recovery) laid over the elapsed ticks of the
 * attack, instead of a repeating wave. Each phase is a {@link #segment} of the elapsed time, shaped by an easing
 * function and blended between two poses with {@link #lerp}.
 */
@Environment(EnvType.CLIENT)
public final class AttackAnimation {
  private AttackAnimation() {
  }

  /**
   * Ticks elapsed since the attack started, as a float: 0 at the start, {@code duration} at the end.
   * {@code attackTick} counts down from {@code duration} to 0, so this flips it and adds the partial tick for smoothness.
   */
  public static float progress(int attackTick, float partialTick, int duration) {
    return Mth.clamp(duration - (attackTick - partialTick), 0.0F, (float) duration);
  }

  /**
   * Local progress of one phase: 0 before {@code start}, 1 after {@code end}, linear in between.
   * Both bounds are in elapsed ticks, so a phase is just "from this tick to that tick".
   */
  public static float segment(float elapsed, float start, float end) {
    if (end <= start) {
      return elapsed >= end ? 1.0F : 0.0F;
    }
    return Mth.clamp((elapsed - start) / (end - start), 0.0F, 1.0F);
  }

  /** Quadratic ease in, t in 0..1: starts slow and accelerates. Good for a strike that builds speed. */
  public static float easeIn(float t) {
    return t * t;
  }

  /** Quadratic ease out, t in 0..1: starts fast and decelerates. Good for a movement that settles into a pose. */
  public static float easeOut(float t) {
    float u = 1.0F - t;
    return 1.0F - u * u;
  }

  /** Quadratic ease in and out, t in 0..1: slow start, fast middle, slow end. Good for a wind-up or a recovery. */
  public static float easeInOut(float t) {
    return t < 0.5F ? 2.0F * t * t : 1.0F - 2.0F * (1.0F - t) * (1.0F - t);
  }

  /** Linear blend from {@code a} (t = 0) to {@code b} (t = 1). */
  public static float lerp(float t, float a, float b) {
    return a + (b - a) * t;
  }
}
