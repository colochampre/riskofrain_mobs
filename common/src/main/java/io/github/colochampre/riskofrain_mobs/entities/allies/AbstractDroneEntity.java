package io.github.colochampre.riskofrain_mobs.entities.allies;

import com.google.common.collect.Sets;
import io.github.colochampre.riskofrain_mobs.utils.EntityUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractDroneEntity extends TamableAnimal {

  public static final int TYPE_LAND = 0;
  public static final int TYPE_FLYING = 1;
  public static final int MIN_FLIGHT_HEIGHT = 3;
  public static final int MAX_FLIGHT_HEIGHT = 8;
  public static final Set<EntityType<?>> DO_NOT_ATTACK = Sets.newHashSet(EntityType.CREEPER, EntityType.PIGLIN, EntityType.PIGLIN_BRUTE, EntityType.ZOMBIFIED_PIGLIN, EntityType.HOGLIN, EntityType.ZOGLIN);
  private static final Set<Item> TAME_ITEMS = Sets.newHashSet(Items.GOLD_BLOCK, Items.RAW_GOLD_BLOCK, Items.GOLD_INGOT, Items.RAW_GOLD, Items.GOLD_NUGGET);
  private static final Set<Item> REPAIR_ITEMS = Sets.newHashSet(Items.IRON_INGOT, Items.IRON_NUGGET, Items.RAW_IRON);
  private static final EntityDataAccessor<Integer> DATA_PRICE = SynchedEntityData.defineId(AbstractDroneEntity.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Integer> DATA_ID_ATTACK_TARGET = SynchedEntityData.defineId(AbstractDroneEntity.class, EntityDataSerializers.INT);
  private static final Map<Item, Integer> dronePriceMap = new HashMap<>();
  private LivingEntity clientSideCachedAttackTarget;
  private float bodyXRot;
  private float bodyZRot;
  private int flyingSound;
  private int underWaterTicks;

  static {
    dronePriceMap.put(Items.GOLD_BLOCK, 81);
    dronePriceMap.put(Items.RAW_GOLD_BLOCK, 54);
    dronePriceMap.put(Items.GOLD_INGOT, 9);
    dronePriceMap.put(Items.RAW_GOLD, 6);
    dronePriceMap.put(Items.GOLD_NUGGET, 1);
  }

  protected AbstractDroneEntity(EntityType<? extends AbstractDroneEntity> entityType, Level level) {
    super(entityType, level);
    if (this.getDroneType() == TYPE_FLYING) {
      this.moveControl = new FlyingMoveControl(this, 16, true);
      this.setPathfindingMalus(BlockPathTypes.COCOA, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.DAMAGE_OTHER, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.FENCE, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 16.0F);
    }
  }

  protected abstract int getDroneType();

  protected abstract int getDronePrice();

  @Override
  protected void defineSynchedData() {
    super.defineSynchedData();
    Difficulty difficulty = this.level().getDifficulty();
    int initialGold = difficulty == Difficulty.HARD ? (int) (this.getDronePrice() * 1.5) : this.getDronePrice();
    this.entityData.define(DATA_PRICE, initialGold);
    this.entityData.define(DATA_ID_ATTACK_TARGET, 0);
  }

  @Override
  public void addAdditionalSaveData(@NotNull CompoundTag tag) {
    super.addAdditionalSaveData(tag);
    tag.putByte("GoldPrice", (byte) this.getCurrentGoldPrice());
  }

  @Override
  public void readAdditionalSaveData(@NotNull CompoundTag tag) {
    super.readAdditionalSaveData(tag);
    if (tag.contains("GoldPrice", 99)) {
      this.setCurrentGoldPrice(tag.getInt("GoldPrice"));
    }
  }

  @Override
  public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> accessor) {
    super.onSyncedDataUpdated(accessor);
    if (DATA_ID_ATTACK_TARGET.equals(accessor)) {
      this.clientSideCachedAttackTarget = null;
    }
  }

  public int getCurrentGoldPrice() {
    return this.entityData.get(DATA_PRICE);
  }

  public void setCurrentGoldPrice(int i) {
    this.entityData.set(DATA_PRICE, i);
  }

  @Override
  public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
    if (this.getCurrentGoldPrice() > 0) {
      String price = String.valueOf(this.getCurrentGoldPrice());
      Component component = Component.literal(price).withStyle(ChatFormatting.YELLOW);
      this.setCustomName(component);
      this.setCustomNameVisible(true);
    }
    return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
  }

  @Override
  public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
    ItemStack itemstack = player.getItemInHand(hand);
    Item item = itemstack.getItem();
    if (this.isTame()) {
      // Repair
      if (this.getHealth() < this.getMaxHealth()) {
        if (REPAIR_ITEMS.contains(itemstack.getItem())) {
          if (!player.getAbilities().instabuild) {
            itemstack.shrink(1);
          }
          this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.IRON_GOLEM_REPAIR, this.getSoundSource(), 0.5F, 1.25F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
          if (itemstack.getItem().equals(Items.IRON_INGOT)) {
            this.heal(18.0F);
          } else if (itemstack.getItem().equals(Items.RAW_IRON)) {
            this.heal(12.0F);
          } else {
            this.heal(2.0F);
          }
          return InteractionResult.SUCCESS;
        }
      }
      // Set sitting
      if (!(item instanceof DyeItem) && this.isOwnedBy(player)) {
        this.setOrderedToSit(!this.isOrderedToSit());
        this.navigation.stop();
        this.setTarget(null);
        //this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundInit.DRONE_REPAIR.get(), this.getSoundSource(), 0.2F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
        return InteractionResult.SUCCESS;
      }
    } else if (!this.isTame()) {
      // Not gold
      if (!TAME_ITEMS.contains(itemstack.getItem())) {
        Component goldMessage = Component.translatable("message.riskofrain_mobs.not_gold").withStyle(ChatFormatting.YELLOW);
        //this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundInit.INSUFFICIENT_FOUNDS_PROC.get(), this.getSoundSource(), 0.5F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
        if (!this.level().isClientSide) {
          player.sendSystemMessage(goldMessage);
        }
        return InteractionResult.SUCCESS;
        // Taming
      } else if (TAME_ITEMS.contains(itemstack.getItem())) {
        if (!player.getAbilities().instabuild) {
          itemstack.shrink(1);
        }
        //this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundInit.COIN_PROC.get(), this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
        this.updateGoldPrice(itemstack);
        String price = String.valueOf(this.getCurrentGoldPrice());
        Component priceName = Component.literal(price).withStyle(ChatFormatting.YELLOW);
        this.setCustomName(priceName);
        this.setCustomNameVisible(true);
        if (!this.level().isClientSide) {
          if (this.getCurrentGoldPrice() <= 0) {
            this.tame(player);
            //this.level().playSound((Player) null, this.getX(), this.getY(), this.getZ(), SoundInit.DRONE_REPAIR.get(), this.getSoundSource(), 0.6F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
            this.level().broadcastEntityEvent(this, (byte) 7);
            this.setCustomName(null);
            this.setCustomNameVisible(false);
            this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
          } else {
            this.level().broadcastEntityEvent(this, (byte) 6);
          }
        }
        return InteractionResult.SUCCESS;
      }
    }
    return super.mobInteract(player, hand);
  }

  private void updateGoldPrice(ItemStack itemstack) {
    int priceReduction = dronePriceMap.get(itemstack.getItem());
    this.setCurrentGoldPrice(this.getCurrentGoldPrice() - priceReduction);
  }

  @Override
  protected void spawnTamingParticles(boolean tamed) {
    SimpleParticleType particle = tamed ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.SMOKE;
    EntityUtils.doParticlesAtEntity(this, particle, 5);
  }

  @Nullable
  @Override
  public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
    return null;
  }
}
