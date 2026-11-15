package io.github.colochampre.riskofrain_mobs.client.animation;

import net.minecraft.util.Mth;

/**
 * Foot-contact detection on vanilla's walk animation position ({@code WalkAnimationState.position}), used to play
 * step sounds in sync with the walk cycle of a model whose legs follow {@code cos(position * k)}.
 * <p>
 * Plain static helpers with no client-only imports, so common entity code can call them.
 */
public final class WalkClock {
  /**
   * Walk speed (vanilla smoothed {@code walkAnimation.speed()}) under which no step sound is played, so a mob that is
   * stopping does not click while its legs barely move.
   */
  public static final float MIN_STEP_SPEED = 0.1F;

  private WalkClock() {
  }

  /**
   * Whether a walk position moving from {@code previous} to {@code current} crossed a foot contact. Contacts are at
   * {@code offset + n * interval} position units for any integer n, so a model whose legs use {@code cos(position * k)}
   * has its leg extremes (a footfall) every {@code PI / k} units with offset 0.
   */
  public static boolean crossedContact(float previous, float current, float interval, float offset) {
    return Mth.floor((current - offset) / interval) > Mth.floor((previous - offset) / interval);
  }
}
