package io.github.colochampre.riskofrain_mobs.mixin;

import io.github.colochampre.riskofrain_mobs.events.ModEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Hooks the difficulty change itself: the integrated server does not tick while the pause menu,
// where the difficulty is changed, is open, so a per-tick check misses it
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
  @Unique
  private Difficulty riskofrain_mobs$previousDifficulty;

  @Inject(method = "setDifficulty", at = @At("HEAD"))
  private void riskofrain_mobs$rememberDifficulty(Difficulty difficulty, boolean forced, CallbackInfo ci) {
    this.riskofrain_mobs$previousDifficulty = ((MinecraftServer) (Object) this).getWorldData().getDifficulty();
  }

  // TAIL skips the early return taken when the difficulty is locked
  @Inject(method = "setDifficulty", at = @At("TAIL"))
  private void riskofrain_mobs$onDifficultyChanged(Difficulty difficulty, boolean forced, CallbackInfo ci) {
    MinecraftServer server = (MinecraftServer) (Object) this;
    if (server.getWorldData().getDifficulty() != this.riskofrain_mobs$previousDifficulty) {
      ModEvents.playDifficultyChangeSound(server);
    }
  }
}
