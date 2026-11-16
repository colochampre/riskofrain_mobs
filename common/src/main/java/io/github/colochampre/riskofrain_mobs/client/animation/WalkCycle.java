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

  /**
   * An asymmetric one-sided bump in [0, 1] over half of the cycle: it starts at 0 when {@code position + phase = 0},
   * rises to 1 during the first {@code rise} fraction of the half cycle, then falls back to 0 over the remaining
   * fraction, arriving at {@code position + phase = PI}; it is 0 during the other half. Both ends are eased (zero
   * slope), so a small {@code rise} gives a quick lift-off and a long, gentle set-down.
   */
  public static float bump(float position, float phase, float rise) {
    float u = halfCycleProgress(position + phase);
    if (u < 0.0F) {
      return 0.0F;
    }
    return u < rise ? smooth(u / rise) : 1.0F - smooth((u - rise) / (1.0F - rise));
  }

  /**
   * A flex that builds over the half cycle where {@code position + phase} is in [PI, 2 PI] (0 at PI, 1 at 2 PI) and
   * then releases over the first {@code release} fraction of the next half cycle (1 at 0, back to 0), staying 0 for
   * the rest. Continuous around the whole cycle; use it for a joint that flexes while the leg pulls.
   */
  public static float pull(float position, float phase, float release) {
    float x = wrap(position + phase);
    if (x >= Mth.PI) {
      return smooth((x - Mth.PI) / Mth.PI);
    }
    float u = x / Mth.PI;
    return u < release ? 1.0F - smooth(u / release) : 0.0F;
  }

  // Progress in [0, 1) through the half cycle that starts at x = 0, or -1 during the other half.
  private static float halfCycleProgress(float x) {
    float w = wrap(x);
    return w < Mth.PI ? w / Mth.PI : -1.0F;
  }

  private static float wrap(float x) {
    float twoPi = 2.0F * Mth.PI;
    return x - twoPi * (float) Math.floor(x / twoPi);
  }

  private static float smooth(float s) {
    float c = Mth.clamp(s, 0.0F, 1.0F);
    return c * c * (3.0F - 2.0F * c);
  }
}
