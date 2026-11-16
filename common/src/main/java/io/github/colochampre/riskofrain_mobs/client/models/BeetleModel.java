package io.github.colochampre.riskofrain_mobs.client.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.colochampre.riskofrain_mobs.client.animation.AttackAnimation;
import io.github.colochampre.riskofrain_mobs.client.animation.WalkCycle;
import io.github.colochampre.riskofrain_mobs.entities.enemies.BeetleEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class BeetleModel<T extends BeetleEntity> extends EntityModel<T> {
  // Ram pose offsets from the rest/walk pose, in radians and model units. Axis conventions (model space: -Z is
  // front, +Y is down, rotation order Rz*Ry*Rx), derived from the geometry and the old attack:
  //  - head_axis/chest_axis xRot: positive tips the front down (the old attack went to +1.0 head, +0.5 chest), so
  //    negative rears up and back.
  //  - the Beetle has no neck part: the neck is the head_axis pivot at the top of the chest, so retracting the neck
  //    is moving head_axis along Z (+Z back, -Z forward).
  //  - arm_axis xRot: positive swings the arm back, negative forward. The forearm points forward from the elbow, so its
  //    xRot positive tips the claw down (towards the ground) and negative lifts it.
  //  - leg_1_axis xRot: positive swings the foot back and down; leg_2_axis xRot positive swings the lower leg back and
  //    down (foot lowered), negative folds it forward and up (foot lifted).
  //  - core.y positive is down; hips_axis xRot positive drops the tail end; chest_axis xRot positive drops the front.
  // Wind-up keyframe: neck pulled back, head pitched up, chest rearing, arms braced back.
  private static final float WINDUP_HEAD_X = -0.45F;
  private static final float WINDUP_NECK_Z = 2.5F;
  private static final float WINDUP_CHEST_X = -0.25F;
  private static final float WINDUP_ARM_X = 0.3F;
  // Strike keyframe (contact): head driven forward and down a little past the old extreme, chest lunging.
  private static final float STRIKE_HEAD_X = 1.1F;
  private static final float STRIKE_NECK_Z = -2.5F;
  private static final float STRIKE_CHEST_X = 0.55F;
  private static final float STRIKE_ARM_X = -0.6F;
  private static final float NECK_REST_Z = -1.5F;
  // Walk gait: a symmetric bound. The front pair swings together, the hind pair together, half a cycle apart. With the
  // position p = limbSwing the front pair lands (foot farthest forward) at p = PI and the hind pair at p = 0, so a
  // foot contact is every PI (BeetleEntity.STEP_INTERVAL) with offset 0. Each pair swings forward while p moves from
  // its landing back to the next one: the front pair for p in (0, PI), the hind pair for p in (PI, 2 PI).
  private static final float HIND_PHASE = (float) Math.PI;
  // Front legs, a reach-and-pull stroke (p = position, front pair lands at p = PI):
  //  - lift-off (p = 0): the arm is farthest back with the elbow flexed from the pull; the arm raises forward (reach) and
  //    the forearm lifts the claw quickly (FRONT_REACH_RISE of the swing).
  //  - reach: the leg stays extended forward while the arm keeps sweeping forward.
  //  - set-down (to p = PI): the reach and the lift fall back slowly with eased ends, so the claw arrives near the
  //    ground before the forward extreme and lands with almost no downward speed, forearm open (rest angle).
  //  - pull (p in PI..2 PI): the arm sweeps back and the elbow flexes progressively (FRONT_PULL_FLEX), the claw
  //    stays near the ground; the body surges forward meanwhile (BODY_SURGE).
  // Each side has a factor on purpose: identical front legs looked unnatural, so the left one moves wider than the
  // right as a deliberate imperfection.
  private static final float FRONT_SWING = 0.95F;
  // Extra forward raise of the arm during the swing, at its peak.
  private static final float FRONT_REACH = 2.0F;
  // Fraction of the swing spent lifting off and reaching up (the rest is the slow set-down).
  private static final float FRONT_REACH_RISE = 0.30F;
  // Forearm lift at the peak of the reach (positive lifts the claw; the forearm xRot goes negative).
  private static final float FRONT_LIFT = 1.2F;
  // Elbow flex reached at the end of the pull, released over the first FRONT_PULL_RELEASE of the swing as the lift
  // takes over.
  private static final float FRONT_PULL_FLEX = 1.0F;
  private static final float FRONT_PULL_RELEASE = FRONT_REACH_RISE;
  private static final float FRONT_LEFT_FACTOR = 1.3F;
  private static final float FRONT_RIGHT_FACTOR = 0.7F;
  // Hind legs: leg_1 swings, leg_2 folds (foot lifted) only while the leg swings forward.
  private static final float HIND_SWING = 0.30F;
  private static final float HIND_FOLD = 1.6F;
  // Body: the weight transfers while the legs pull, so the chest pitch and the bob are lowest a little AFTER each
  // landing instead of on it (a dip on the landing read as an impact). The pitch and shell counter-rotation follow
  // the front landing (chest down at p = PI + PITCH_LAG), the hips follow the hind landing (hips down at p = 0), the
  // bob runs twice per cycle and is lowest at each landing plus BOB_LAG.
  private static final float CHEST_PITCH = 0.25F;
  private static final float HEAD_PITCH = 0.24F;
  private static final float SHELL_PITCH = 0.20F;
  private static final float HIPS_PITCH = 0.40F;
  private static final float BODY_BOB = 1.2F;
  // Delay of the chest pitch, in walk position radians.
  private static final float PITCH_LAG = (float) Math.PI / 4.0F;
  // Delay of the bob, in walk position radians.
  private static final float BOB_LAG = (float) Math.PI / 6.0F;
  // Surge of the body and head along Z, once per cycle, as the push-off.
  private static final float BODY_SURGE = 3.5F;
  private static final float HEAD_SURGE = 1.5F;

  // Attack progress in elapsed ticks for the current frame, or -1 when not attacking. Set in prepareMobModel.
  private float attackElapsed = -1.0F;
  /** Walk cycle position (vanilla limbSwing) of the entity being rendered, set in prepareMobModel. */
  private float walkPosition;
  // Walk swing amount for every part, legs and body, capped at BeetleEntity.WALK_MAX_AMOUNT.
  private float walkAmount;

  protected final ModelPart core;
  private final ModelPart chest_axis;
  private final ModelPart chest;
  private final ModelPart chest_shell_axis;
  private final ModelPart chest_shell;
  private final ModelPart head_axis;
  private final ModelPart head;
  private final ModelPart head_shell_axis;
  private final ModelPart head_shell;
  private final ModelPart left_arm_axis;
  private final ModelPart left_arm;
  private final ModelPart left_forearm_axis;
  private final ModelPart left_forearm;
  private final ModelPart right_arm_axis;
  private final ModelPart right_arm;
  private final ModelPart right_forearm_axis;
  private final ModelPart right_forearm;
  private final ModelPart hips_axis;
  private final ModelPart hips;
  private final ModelPart tail_axis;
  private final ModelPart tail;
  private final ModelPart left_leg_1_axis;
  private final ModelPart left_leg_1;
  private final ModelPart left_leg_2_axis;
  private final ModelPart left_leg_2;
  private final ModelPart right_leg_1_axis;
  private final ModelPart right_leg_1;
  private final ModelPart right_leg_2_axis;
  private final ModelPart right_leg_2;

  public BeetleModel(ModelPart root) {
    this.core = root.getChild("core");
    this.chest_axis = core.getChild("chest_axis");
    this.chest = chest_axis.getChild("chest");
    this.chest_shell_axis = chest.getChild("chest_shell_axis");
    this.chest_shell = chest_shell_axis.getChild("chest_shell");
    this.head_axis = chest.getChild("head_axis");
    this.head = head_axis.getChild("head");
    this.head_shell_axis = head.getChild("head_shell_axis");
    this.head_shell = head_shell_axis.getChild("head_shell");
    this.left_arm_axis = chest.getChild("left_arm_axis");
    this.left_arm = left_arm_axis.getChild("left_arm");
    this.left_forearm_axis = left_arm.getChild("left_forearm_axis");
    this.left_forearm = left_forearm_axis.getChild("left_forearm");
    this.right_arm_axis = chest.getChild("right_arm_axis");
    this.right_arm = right_arm_axis.getChild("right_arm");
    this.right_forearm_axis = right_arm.getChild("right_forearm_axis");
    this.right_forearm = right_forearm_axis.getChild("right_forearm");
    this.hips_axis = core.getChild("hips_axis");
    this.hips = hips_axis.getChild("hips");
    this.tail_axis = hips.getChild("tail_axis");
    this.tail = tail_axis.getChild("tail");
    this.left_leg_1_axis = hips.getChild("left_leg_1_axis");
    this.left_leg_1 = left_leg_1_axis.getChild("left_leg_1");
    this.left_leg_2_axis = left_leg_1.getChild("left_leg_2_axis");
    this.left_leg_2 = left_leg_2_axis.getChild("left_leg_2");
    this.right_leg_1_axis = hips.getChild("right_leg_1_axis");
    this.right_leg_1 = right_leg_1_axis.getChild("right_leg_1");
    this.right_leg_2_axis = right_leg_1.getChild("right_leg_2_axis");
    this.right_leg_2 = right_leg_2_axis.getChild("right_leg_2");
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition mesh = new MeshDefinition();
    PartDefinition parts = mesh.getRoot();

    PartDefinition core = parts.addOrReplaceChild("core", CubeListBuilder.create().texOffs(81, 113).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, 0.0F));
    PartDefinition chest_axis = core.addOrReplaceChild("chest_axis", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -3.0F, -2.0F, 0.0873F, 0.0F, 0.0F));
    PartDefinition chest = chest_axis.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(53, 0).addBox(-6.75F, -6.5F, -4.0F, 13.5F, 10.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(0, 49).addBox(-3.75F, -8.0F, -3.0F, 7.5F, 13.5F, 5.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0).addBox(-7.75F, -4.0F, -4.5F, 15.5F, 4.5F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition chest_shell_axis = chest.addOrReplaceChild("chest_shell_axis", CubeListBuilder.create(), PartPose.offset(0.0F, -9.0F, 3.0F));
    PartDefinition chest_shell = chest_shell_axis.addOrReplaceChild("chest_shell", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition chest_shell_5_r1 = chest_shell.addOrReplaceChild("chest_shell_5_r1", CubeListBuilder.create().texOffs(0, 83).addBox(-4.0F, 0.0F, -1.0F, 8.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 7.0F, 8.0F, -0.9163F, 0.0F, 0.0F));
    PartDefinition chest_shell_4_r1 = chest_shell.addOrReplaceChild("chest_shell_4_r1", CubeListBuilder.create().texOffs(83, 69).addBox(-8.0F, -0.75F, -1.0F, 8.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, 4.0F, -0.7854F, 0.0873F, -0.2618F));
    PartDefinition chest_shell_3_r1 = chest_shell.addOrReplaceChild("chest_shell_3_r1", CubeListBuilder.create().texOffs(112, 69).addBox(0.0F, -0.75F, -1.0F, 8.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, 4.0F, -0.7854F, -0.0873F, 0.2618F));
    PartDefinition chest_shell_2_r1 = chest_shell.addOrReplaceChild("chest_shell_2_r1", CubeListBuilder.create().texOffs(27, 49).addBox(-8.5F, -1.5F, -2.0F, 9.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.5672F, -0.0873F, -0.1309F));
    PartDefinition chest_shell_1_r1 = chest_shell.addOrReplaceChild("chest_shell_1_r1", CubeListBuilder.create().texOffs(27, 59).addBox(-0.5F, -1.5F, -2.0F, 9.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.5672F, 0.0873F, 0.1309F));
    PartDefinition head_axis = chest.addOrReplaceChild("head_axis", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -9.0F, -1.5F, -0.0873F, 0.0F, 0.0F));
    PartDefinition head = head_axis.addOrReplaceChild("head", CubeListBuilder.create().texOffs(117, 34).addBox(-4.0F, -5.0F, -6.0F, 8.0F, 6.0F, 7.5F, new CubeDeformation(0.0F))
            .texOffs(63, 83).addBox(-2.25F, -6.0F, -4.5F, 4.5F, 7.5F, 4.5F, new CubeDeformation(0.0F))
            .texOffs(0, 69).addBox(-2.5F, -4.0F, -6.5F, 5.0F, 4.0F, 8.5F, new CubeDeformation(0.0F))
            .texOffs(0, 34).addBox(-5.5F, -4.0F, -6.0F, 11.0F, 4.0F, 7.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition head_shell_axis = head.addOrReplaceChild("head_shell_axis", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -5.0F, -2.0F, -0.0873F, 0.0F, 0.0F));
    PartDefinition head_shell = head_shell_axis.addOrReplaceChild("head_shell", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition head_shell_6_r1 = head_shell.addOrReplaceChild("head_shell_6_r1", CubeListBuilder.create().texOffs(43, 19).addBox(-6.5F, -3.5F, -6.5F, 9.0F, 2.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 4.0F, -0.48F, 0.9163F, -0.4843F));
    PartDefinition head_shell_5_r1 = head_shell.addOrReplaceChild("head_shell_5_r1", CubeListBuilder.create().texOffs(84, 19).addBox(-2.5F, -3.5F, -6.5F, 9.0F, 2.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 4.0F, -0.48F, -0.9163F, 0.4843F));
    PartDefinition head_shell_4_r1 = head_shell.addOrReplaceChild("head_shell_4_r1", CubeListBuilder.create().texOffs(39, 34).addBox(-9.5F, -1.5F, -4.0F, 11.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -1.0F, 0.5F, 0.0436F, 0.1745F, -0.1745F));
    PartDefinition head_shell_3_r1 = head_shell.addOrReplaceChild("head_shell_3_r1", CubeListBuilder.create().texOffs(78, 34).addBox(-1.5F, -1.5F, -4.0F, 11.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -1.0F, 0.5F, 0.0436F, -0.1745F, 0.1745F));
    PartDefinition head_shell_2_r1 = head_shell.addOrReplaceChild("head_shell_2_r1", CubeListBuilder.create().texOffs(0, 19).addBox(-5.0F, -0.75F, -3.0F, 8.0F, 1.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, -4.0F, 0.5236F, -0.8727F, -0.6109F));
    PartDefinition head_shell_1_r1 = head_shell.addOrReplaceChild("head_shell_1_r1", CubeListBuilder.create().texOffs(98, 0).addBox(-3.0F, -0.75F, -3.0F, 8.0F, 1.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, -4.0F, 0.5236F, 0.8727F, 0.6109F));
    PartDefinition left_arm_axis = chest.addOrReplaceChild("left_arm_axis", CubeListBuilder.create(), PartPose.offsetAndRotation(8.0F, -1.0F, 0.0F, 0.0436F, 0.0F, 0.0F));
    PartDefinition left_arm = left_arm_axis.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 113).addBox(-2.25F, -1.5F, -1.0F, 4.0F, 2.5F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(46, 83).addBox(-0.25F, 1.0F, -1.5F, 3.0F, 12.0F, 4.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition left_forearm_axis = left_arm.addOrReplaceChild("left_forearm_axis", CubeListBuilder.create(), PartPose.offsetAndRotation(1.0F, 12.0F, 0.0F, 0.1745F, 0.0F, 0.0F));
    PartDefinition left_forearm = left_forearm_axis.addOrReplaceChild("left_forearm", CubeListBuilder.create().texOffs(0, 101).addBox(-1.0F, -1.5F, -5.0F, 3.0F, 3.5F, 6.5F, new CubeDeformation(0.0F))
            .texOffs(57, 101).addBox(-1.0F, -1.25F, -10.0F, 3.0F, 2.5F, 5.0F, new CubeDeformation(0.0F))
            .texOffs(70, 113).addBox(-0.5F, -1.25F, -13.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(89, 101).addBox(0.0F, -1.25F, -15.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition right_arm_axis = chest.addOrReplaceChild("right_arm_axis", CubeListBuilder.create(), PartPose.offsetAndRotation(-8.0F, -1.0F, 1.0F, 0.0436F, 0.0F, 0.0F));
    PartDefinition right_arm = right_arm_axis.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(104, 101).addBox(-1.75F, -1.5F, -2.0F, 4.0F, 2.5F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(29, 83).addBox(-2.75F, 1.0F, -2.5F, 3.0F, 12.0F, 4.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition right_forearm_axis = right_arm.addOrReplaceChild("right_forearm_axis", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0F, 12.0F, -1.0F, 0.1745F, 0.0F, 0.0F));
    PartDefinition right_forearm = right_forearm_axis.addOrReplaceChild("right_forearm", CubeListBuilder.create().texOffs(84, 83).addBox(-2.0F, -1.5F, -5.0F, 3.0F, 3.5F, 6.5F, new CubeDeformation(0.0F))
            .texOffs(40, 101).addBox(-2.0F, -1.25F, -10.0F, 3.0F, 2.5F, 5.0F, new CubeDeformation(0.0F))
            .texOffs(59, 113).addBox(-1.5F, -1.25F, -13.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
            .texOffs(74, 101).addBox(-1.0F, -1.25F, -15.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition hips_axis = core.addOrReplaceChild("hips_axis", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 1.0F));
    PartDefinition hips = hips_axis.addOrReplaceChild("hips", CubeListBuilder.create().texOffs(60, 49).addBox(-2.0F, -1.0F, -0.5F, 4.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition tail_axis = hips.addOrReplaceChild("tail_axis", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 7.0F));
    PartDefinition tail = tail_axis.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(21, 101).addBox(-2.5F, -2.5F, -2.0F, 5.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition left_leg_1_axis = hips.addOrReplaceChild("left_leg_1_axis", CubeListBuilder.create(), PartPose.offsetAndRotation(3.0F, 0.0F, 3.0F, -0.3491F, 0.0F, 0.0F));
    PartDefinition left_leg_1 = left_leg_1_axis.addOrReplaceChild("left_leg_1", CubeListBuilder.create().texOffs(56, 69).addBox(-1.0F, -1.5F, -1.0F, 2.5F, 2.5F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition left_leg_2_axis = left_leg_1.addOrReplaceChild("left_leg_2_axis", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 8.5F));
    PartDefinition left_leg_2 = left_leg_2_axis.addOrReplaceChild("left_leg_2", CubeListBuilder.create().texOffs(28, 113).addBox(-1.0F, -1.5F, -1.0F, 2.5F, 5.5F, 2.5F, new CubeDeformation(0.0F))
            .texOffs(50, 113).addBox(-0.5F, 3.5F, -0.5F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition right_leg_1_axis = hips.addOrReplaceChild("right_leg_1_axis", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.0F, 0.0F, 3.0F, -0.3491F, 0.0F, 0.0F));
    PartDefinition right_leg_1 = right_leg_1_axis.addOrReplaceChild("right_leg_1", CubeListBuilder.create().texOffs(29, 69).addBox(-1.5F, -1.5F, -1.0F, 2.5F, 2.5F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition right_leg_2_axis = right_leg_1.addOrReplaceChild("right_leg_2_axis", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 8.5F));
    PartDefinition right_leg_2 = right_leg_2_axis.addOrReplaceChild("right_leg_2", CubeListBuilder.create().texOffs(15, 113).addBox(-1.5F, -1.5F, -1.0F, 2.5F, 5.5F, 2.5F, new CubeDeformation(0.0F))
            .texOffs(41, 113).addBox(-1.5F, 3.5F, -0.5F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

    return LayerDefinition.create(mesh, 160, 160);
  }

  @Override
  public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
    core.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
  }

  @Override
  public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTick) {
    // Vanilla cadence, capped swing amount for every part.
    this.walkPosition = limbSwing;
    this.walkAmount = Math.min(limbSwingAmount, BeetleEntity.WALK_MAX_AMOUNT);
    // Rest/walk values of every part the ram touches, set every frame so nothing stays stuck after an attack.
    this.core.z = 0 - Mth.cos(this.walkPosition) * BODY_SURGE * this.walkAmount;
    this.head.z = 0 - Mth.cos(this.walkPosition) * HEAD_SURGE * this.walkAmount;
    this.head_axis.z = NECK_REST_Z;
    float pitch = Mth.cos(this.walkPosition - PITCH_LAG);
    this.chest_axis.xRot = 0.0873F - pitch * CHEST_PITCH * this.walkAmount;
    this.head_axis.xRot = -0.0873F + pitch * HEAD_PITCH * this.walkAmount;

    int attackTick = entity.getAttackTick();
    this.attackElapsed = attackTick > 0
            ? AttackAnimation.progress(attackTick, partialTick, BeetleEntity.ATTACK_DURATION)
            : -1.0F;
  }

  @Override
  public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch) {
    getLookAnim(entity, headYaw, headPitch);
    getIdleAnim(entity, ageInTicks);
    getWalkAnim(entity, this.walkPosition, this.walkAmount);
    if (this.attackElapsed >= 0.0F) {
      getAttackAnim(this.attackElapsed);
    }
  }

  private void getLookAnim(BeetleEntity entity, float headYaw, float headPitch) {
    this.head.xRot = headPitch * 0.023271058F / 2;
    this.head.yRot = headYaw * 0.017453292F / 2;
    this.chest.yRot = 0;
    if (!entity.isMoving(entity)) {
      this.chest.yRot = headYaw * 0.017453292F / 3;
    }
  }

  private void getIdleAnim(BeetleEntity entity, float ageInTicks) {
    this.head_axis.yRot = -Mth.cos(ageInTicks * 0.04F) * 0.06F;
    this.head_axis.zRot = Mth.cos(ageInTicks * 0.04F) * 0.06F;
    this.tail.xRot = Mth.cos(ageInTicks * 0.08F) * 0.09F;
    this.tail.zRot = -Mth.cos(ageInTicks * 0.12F) * 0.06F;
    if (!entity.isMoving(entity)) {
      this.head_axis.xRot = Mth.cos(ageInTicks * 0.06F) * 0.06F;
      this.chest.xRot = Mth.cos(ageInTicks * 0.05F) * 0.06F;
      this.left_arm.xRot = -Mth.cos(ageInTicks * 0.05F) * 0.06F;
      this.right_arm.xRot = -Mth.cos(ageInTicks * 0.05F) * 0.06F;
    }
  }

  private void getWalkAnim(BeetleEntity entity, float limbSwing, float walkAmount) {
    this.core.y = 12 + Mth.cos((limbSwing - BOB_LAG) * 2.0F) * BODY_BOB * walkAmount;
    this.chest_shell_axis.xRot = Mth.cos(limbSwing) * SHELL_PITCH * walkAmount;
    this.hips_axis.xRot = Mth.cos(limbSwing) * HIPS_PITCH * walkAmount;
    walkFrontLeg(this.left_arm_axis, this.left_forearm_axis, limbSwing, walkAmount * FRONT_LEFT_FACTOR);
    walkFrontLeg(this.right_arm_axis, this.right_forearm_axis, limbSwing, walkAmount * FRONT_RIGHT_FACTOR);
    walkHindLeg(this.left_leg_1_axis, this.left_leg_2_axis, limbSwing, walkAmount);
    walkHindLeg(this.right_leg_1_axis, this.right_leg_2_axis, limbSwing, walkAmount);
  }

  private static void walkFrontLeg(ModelPart arm, ModelPart forearm, float limbSwing, float walkAmount) {
    arm.xRot = 0.0436F + WalkCycle.swing(limbSwing, 0.0F) * FRONT_SWING * walkAmount
        - WalkCycle.bump(limbSwing, 0.0F, FRONT_REACH_RISE) * FRONT_REACH * walkAmount;
    forearm.xRot = 0.1745F
        - (WalkCycle.pull(limbSwing, 0.0F, FRONT_PULL_RELEASE) * FRONT_PULL_FLEX
        + WalkCycle.bump(limbSwing, 0.0F, FRONT_REACH_RISE) * FRONT_LIFT) * walkAmount;
  }

  private static void walkHindLeg(ModelPart upper, ModelPart lower, float limbSwing, float walkAmount) {
    upper.xRot = -0.3491F + WalkCycle.swing(limbSwing, HIND_PHASE) * HIND_SWING * walkAmount;
    lower.xRot = -WalkCycle.fold(limbSwing, HIND_PHASE) * HIND_FOLD * walkAmount;
  }

  /**
   * Ram as phases over the elapsed ticks (BeetleEntity ATTACK_* constants): wind-up (neck and head pulled back, chest
   * rearing), strike (head and neck driven forward and down), hold, recovery. The offsets are added to the walk/idle
   * pose and scaled by a weight that is 0 at the start and at the end, so the attack never snaps.
   */
  private void getAttackAnim(float elapsed) {
    float windupEnd = BeetleEntity.ATTACK_WINDUP_END;
    float impact = BeetleEntity.ATTACK_IMPACT_TICK;
    float holdEnd = BeetleEntity.ATTACK_HOLD_END;
    float end = BeetleEntity.ATTACK_DURATION;

    float windup = AttackAnimation.easeInOut(AttackAnimation.segment(elapsed, 0.0F, windupEnd));
    float recovery = AttackAnimation.easeInOut(AttackAnimation.segment(elapsed, holdEnd, end));
    // 0 = wind-up pose, 1 = contact. Ease in: the ram builds speed and lands at impact.
    float strike = AttackAnimation.easeIn(AttackAnimation.segment(elapsed, windupEnd, impact));
    float weight = windup * (1.0F - recovery);

    this.head_axis.xRot += weight * AttackAnimation.lerp(strike, WINDUP_HEAD_X, STRIKE_HEAD_X);
    this.head_axis.z += weight * AttackAnimation.lerp(strike, WINDUP_NECK_Z, STRIKE_NECK_Z);
    this.chest_axis.xRot += weight * AttackAnimation.lerp(strike, WINDUP_CHEST_X, STRIKE_CHEST_X);
    float arm = weight * AttackAnimation.lerp(strike, WINDUP_ARM_X, STRIKE_ARM_X);
    this.left_arm_axis.xRot += arm;
    this.right_arm_axis.xRot += arm;
  }
}
