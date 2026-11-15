package io.github.colochampre.riskofrain_mobs.client.animation;

import net.minecraft.util.Mth;

/**
 * Gait curves for walk cycles, as a function of the vanilla walk position ({@code limbSwing}). A model multiplies the
 * result by an amplitude and by the (capped) swing amount, like it did with a bare {@code cos(limbSwing)}.
 * <p>
 * Stateless static helpers; pass {@code position + PI} as the position of the opposite leg.
 */
public final class WalkCycle {
  private WalkCycle() {
  }

  /**
   * A cosine shifted in time, in [-1, 1]: the same swing as {@code cos(position)} but arriving {@code phase} radians
   * later. A joint that lags its parent by a quarter step uses {@code phase = -PI / 2}.
   */
  public static float swing(float position, float phase) {
    return Mth.cos(position + phase);
  }

  /**
   * A one-sided bend in [0, 1]: it bends only one way, folding during half of the cycle (peak when
   * {@code position + phase = PI / 2}) and staying straight (0) during the other half. The smoothstep shaping keeps
   * the fold from starting or ending with a kink.
   */
  public static float fold(float position, float phase) {
    float s = Math.max(0.0F, Mth.sin(position + phase));
    return s * s * (3.0F - 2.0F * s);
  }
}
