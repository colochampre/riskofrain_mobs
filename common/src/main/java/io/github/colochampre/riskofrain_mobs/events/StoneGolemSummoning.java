package io.github.colochampre.riskofrain_mobs.events;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;

/**
 * Summons a hostile Stone Golem when a redstone block is placed on top of a blackstone T,
 * mirroring the vanilla iron golem build.
 */
public class StoneGolemSummoning {

  private static BlockPattern pattern;

  public static void register() {
    // PLACE fires before the block is in the world, so the pattern check is deferred to a queued server task.
    // tell() always queues; execute() runs inline when called on the server thread outside a task
    BlockEvent.PLACE.register((level, pos, state, placer) -> {
      if (level instanceof ServerLevel serverLevel && state.is(Blocks.REDSTONE_BLOCK)) {
        BlockPos headPos = pos.immutable();
        MinecraftServer server = serverLevel.getServer();
        server.tell(new TickTask(server.getTickCount(), () -> trySummon(serverLevel, headPos)));
      }
      return EventResult.pass();
    });
  }

  private static void trySummon(ServerLevel level, BlockPos headPos) {
    BlockPattern.BlockPatternMatch match = getPattern().find(level, headPos);
    if (match == null) return;

    BlockPos spawnPos = match.getBlock(1, 2, 0).getPos();
    clearPatternBlocks(level, match);
    RoREntityTypes.STONE_GOLEM.get().spawn(level, spawnPos, MobSpawnType.MOB_SUMMONED);
    updatePatternBlocks(level, match);
  }

  private static void clearPatternBlocks(ServerLevel level, BlockPattern.BlockPatternMatch match) {
    for (int i = 0; i < match.getWidth(); i++) {
      for (int j = 0; j < match.getHeight(); j++) {
        BlockInWorld block = match.getBlock(i, j, 0);
        BlockState state = block.getState();
        // Leave whatever fills the gaps around the T (grass, water...) in place
        if (state.canBeReplaced()) continue;
        level.setBlock(block.getPos(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        level.levelEvent(2001, block.getPos(), Block.getId(state));
      }
    }
  }

  private static void updatePatternBlocks(ServerLevel level, BlockPattern.BlockPatternMatch match) {
    for (int i = 0; i < match.getWidth(); i++) {
      for (int j = 0; j < match.getHeight(); j++) {
        level.blockUpdated(match.getBlock(i, j, 0).getPos(), Blocks.AIR);
      }
    }
  }

  private static BlockPattern getPattern() {
    if (pattern == null) {
      pattern = BlockPatternBuilder.start()
              .aisle("~^~", "###", "~#~")
              .where('^', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.REDSTONE_BLOCK)))
              .where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.BLACKSTONE)))
              // Unlike the vanilla iron golem, the gaps also accept replaceable blocks such as grass, dead bushes or water
              .where('~', block -> block.getState().canBeReplaced())
              .build();
    }
    return pattern;
  }
}
