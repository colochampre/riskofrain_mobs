package io.github.colochampre.riskofrain_mobs.client.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.colochampre.riskofrain_mobs.client.animation.AttackAnimation;
import io.github.colochampre.riskofrain_mobs.client.animation.WalkCycle;
import io.github.colochampre.riskofrain_mobs.entities.enemies.LemurianEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class LemurianModel<T extends LemurianEntity> extends EntityModel<T> {
  // Punch pose angles in radians. Axis conventions (model space: +X is the Lemurian's left, -Z is front, +Y is down,
  // rotation order Rz*Ry*Rx), derived from the geometry and the old punch:
  //  - arm/forearm xRot: negative swings the limb forward and up, positive back. The forearm xRot is relative to
  //    the arm, so arm + forearm is where the forearm points; 0 is a straight arm.
  //  - arm yRot: with the arm raised forward, positive sweeps the fist toward the entity's right (-X). "side" below
  //    is +1 for the left arm and -1 for the right arm, so inward yaw is side * value.
  //  - rib cage / stomach yRot: positive turns the chest toward the entity's right and moves the right shoulder
  //    back, so the turn that pulls the punching shoulder back is -side * value and into the punch is +side * value.
  //  - rib cage xRot: positive leans the chest forward.
  // Wind-up keyframe: punching shoulder back, elbow bent with the fist near the chest, elbow slightly out.
  private static final float WINDUP_ARM_X = 0.5F;
  private static final float WINDUP_FOREARM_X = -2.5F;
  private static final float WINDUP_ARM_YAW_OUT = 0.15F;
  private static final float WINDUP_RIB_YAW = 0.4F;
  private static final float WINDUP_STOMACH_YAW = 0.2F;
  private static final float WINDUP_RIB_X = -0.05F;
  // Guard arm (the one that does not punch) comes forward and bends a little as a counterbalance.
  private static final float WINDUP_GUARD_ARM_X = -0.5F;
  private static final float WINDUP_GUARD_FOREARM_X = -0.5F;
  // Strike keyframe (contact): arm level with the shoulders and straight, fist slightly toward the middle.
  private static final float STRIKE_ARM_X = -1.4F;
  private static final float STRIKE_FOREARM_X = -0.05F;
  private static final float STRIKE_ARM_YAW_IN = 0.12F;
  private static final float STRIKE_RIB_YAW = 0.45F;
  private static final float STRIKE_STOMACH_YAW = 0.25F;
  private static final float STRIKE_RIB_X = 0.15F;
  private static final float STRIKE_GUARD_ARM_X = 0.1F;
  private static final float STRIKE_GUARD_FOREARM_X = -0.2F;

  // Leg gait (radians, before the walk amount, max 0.59), tune here. Axis notes, from createBodyLayer: a leg xRot
  // positive swings the lower end backward (+Z) and negative forward, relative to its parent. Rest angles are
  // hip -0.349 (thigh slopes forward), knee +0.960 (shin goes back, so the knee points forward), hock -0.873 (the
  // lower leg comes forward again), foot +0.349; summed down the chain the foot is at about +0.09, flat on the
  // ground. That zigzag is the deer hind leg. So, relative to rest:
  //  - hip: -swing is forward (foot reaches ahead at the heel strike, position 0), +swing is backward;
  //  - knee: + folds (foot lifts back and up); hock: - folds (the lower leg comes forward and up); the two together
  //    shorten the leg during the swing, the foot stays near level through LEG_FOOT_LEVEL and toes down at push-off.
  // The walk helper in client/animation/WalkCycle drives them; the foot contact of the left leg is at position 0
  // and of the right leg at PI, which is the step interval (PI) and offset (0) of LemurianEntity.
  private static final float LEG_HIP_REST = -0.34906584F;
  private static final float LEG_KNEE_REST = 0.9599311F;
  private static final float LEG_HOCK_REST = -0.87266463F;
  private static final float LEG_FOOT_REST = 0.34906584F;
  private static final float LEG_HIP_SWING = 1.1F;
  private static final float LEG_KNEE_FOLD = 1.1F;
  private static final float LEG_HOCK_FOLD = 1.0F;
  /** Share of the rotation accumulated by the hip, knee and hock that the foot cancels (1 keeps it level). */
  private static final float LEG_FOOT_LEVEL = 0.6F;
  private static final float LEG_FOOT_PUSH = 0.6F;
  /** Phase of the push-off, as an advance of the foot flick over the support half (PI / 4 peaks at 3/8 cycle). */
  private static final float PUSH_PHASE = (float) Math.PI / 4.0F;

  // Sidestep (strafing), blended in by the share of the movement that is lateral. It runs on the same leg phases as
  // the forward gait (left leg at the walk position, right leg PI later), so the foot contacts keep their interval
  // (PI) and offset (0). Axis notes, from createBodyLayer: with +Y down and +X the Lemurian's left, a leg zRot
  // positive swings the foot toward -X (the right) and negative toward +X (the left); a part's top leans toward +X
  // with positive zRot. So a foot reaching toward the direction of travel d (+1 left, -1 right) is zRot = -d * reach
  // for both legs, and a leg is abducted (foot out) when its side s (+1 left leg, -1 right leg) matches d.
  //  - hip zRot: -d * LEG_SIDE_SWING * cos(position): the foot reaches furthest toward travel at the contact
  //    (position 0), then sweeps back through support, then comes forward while lifted (same lift as the forward gait);
  //  - a foot moving toward the middle (adduction) is scaled by LEG_SIDE_CLOSE so it closes in under the body
  //    instead of crossing the other leg;
  //  - the lift of the knee and hock is scaled by LEG_SIDE_LIFT; the hip xRot swing and the push-off belong to the
  //    forward gait and fade out with the lateral share;
  //  - stomach and rib cage zRot lean the torso toward travel (BODY_SIDE_LEAN) so it stays over the leading leg.
  private static final float LEG_SIDE_SWING = 0.8F;
  private static final float LEG_SIDE_CLOSE = 1.6F;
  private static final float LEG_SIDE_LIFT = 0.8F;
  private static final float BODY_SIDE_LEAN = 0.12F;
  /** Share of the arm swing kept while moving purely sideways (1 keeps it, 0 stills the arms). */
  private static final float ARM_SIDE_SWING = 0.35F;
  /** Below this smoothed speed (blocks per tick) the movement direction is unknown and the forward gait is used. */
  private static final float MIN_DIRECTION_SPEED = 1.0E-4F;


  private final ModelPart core;
  private final ModelPart stomach_axis;
  private final ModelPart stomach;
  private final ModelPart rib_cage_axis;
  private final ModelPart rib_cage;
  private final ModelPart neck_axis;
  private final ModelPart neck;
  private final ModelPart head_axis;
  private final ModelPart head;
  private final ModelPart top_head;
  private final ModelPart top_mouth;
  private final ModelPart low_mouth_axis;
  private final ModelPart low_mouth;
  private final ModelPart left_arm_axis;
  private final ModelPart left_arm;
  private final ModelPart left_shoulder_plate;
  private final ModelPart left_forearm_axis;
  private final ModelPart left_forearm;
  private final ModelPart left_claws;
  private final ModelPart right_arm_axis;
  private final ModelPart right_arm;
  private final ModelPart right_shoulder_plate;
  private final ModelPart right_forearm_axis;
  private final ModelPart right_forearm;
  private final ModelPart right_claws;
  private final ModelPart tail_1_axis;
  private final ModelPart tail_1;
  private final ModelPart tail_2_axis;
  private final ModelPart tail_2;
  private final ModelPart tail_3_axis;
  private final ModelPart tail_3;
  private final ModelPart left_leg_1_axis;
  private final ModelPart left_leg_1;
  private final ModelPart left_leg_2_axis;
  private final ModelPart left_leg_2;
  private final ModelPart left_leg_3_axis;
  private final ModelPart left_leg_3;
  private final ModelPart left_foot_axis;
  private final ModelPart left_foot;
  private final ModelPart right_leg_1_axis;
  private final ModelPart right_leg_1;
  private final ModelPart right_leg_2_axis;
  private final ModelPart right_leg_2;
  private final ModelPart right_leg_3_axis;
  private final ModelPart right_leg_3;
  private final ModelPart right_foot_axis;
  private final ModelPart right_foot;
  // Attack progress in elapsed ticks for the current frame, or -1 when not attacking. Set in prepareMobModel.
  private float attackElapsed = -1.0F;
  /** Walk position and capped swing amount of the entity being rendered, set in prepareMobModel. */
  private float walkPosition;
  private float walkAmount;
  /**
   * Blend of the forward gait and the sidestep for the entity being rendered, set in prepareMobModel: the shares of
   * the movement that are forward (forwardShare) and lateral (sideShare, they add up to 1), and the direction of each
   * part: forwardDir -1 runs the forward gait backward, sideDir is +1 toward the Lemurian's left.
   */
  private float forwardShare = 1.0F;
  private float sideShare;
  private float forwardDir = 1.0F;
  private float sideDir = 1.0F;
  // Arm that punches in the current attack: +1 left arm (+X), -1 right arm. Set in prepareMobModel.
  private float punchSide = 1.0F;

  public LemurianModel(ModelPart root) {
    this.core = root.getChild("core");
    this.stomach_axis = core.getChild("stomach_axis");
    this.stomach = stomach_axis.getChild("stomach");
    this.rib_cage_axis = stomach.getChild("rib_cage_axis");
    this.rib_cage = rib_cage_axis.getChild("rib_cage");
    this.neck_axis = rib_cage.getChild("neck_axis");
    this.neck = neck_axis.getChild("neck");
    this.head_axis = neck.getChild("head_axis");
    this.head = head_axis.getChild("head");
    this.top_head = head.getChild("top_head");
    this.top_mouth = head.getChild("top_mouth");
    this.low_mouth_axis = head.getChild("low_mouth_axis");
    this.low_mouth = low_mouth_axis.getChild("low_mouth");
    this.left_arm_axis = rib_cage.getChild("left_arm_axis");
    this.left_arm = left_arm_axis.getChild("left_arm");
    this.left_shoulder_plate = left_arm.getChild("left_shoulder_plate");
    this.left_forearm_axis = left_arm.getChild("left_forearm_axis");
    this.left_forearm = left_forearm_axis.getChild("left_forearm");
    this.left_claws = left_forearm.getChild("left_claws");
    this.right_arm_axis = rib_cage.getChild("right_arm_axis");
    this.right_arm = right_arm_axis.getChild("right_arm");
    this.right_shoulder_plate = right_arm.getChild("right_shoulder_plate");
    this.right_forearm_axis = right_arm.getChild("right_forearm_axis");
    this.right_forearm = right_forearm_axis.getChild("right_forearm");
    this.right_claws = right_forearm.getChild("right_claws");
    this.tail_1_axis = core.getChild("tail_1_axis");
    this.tail_1 = tail_1_axis.getChild("tail_1");
    this.tail_2_axis = tail_1.getChild("tail_2_axis");
    this.tail_2 = tail_2_axis.getChild("tail_2");
    this.tail_3_axis = tail_2.getChild("tail_3_axis");
    this.tail_3 = tail_3_axis.getChild("tail_3");
    this.left_leg_1_axis = core.getChild("left_leg_1_axis");
    this.left_leg_1 = left_leg_1_axis.getChild("left_leg_1");
    this.left_leg_2_axis = left_leg_1.getChild("left_leg_2_axis");
    this.left_leg_2 = left_leg_2_axis.getChild("left_leg_2");
    this.left_leg_3_axis = left_leg_2.getChild("left_leg_3_axis");
    this.left_leg_3 = left_leg_3_axis.getChild("left_leg_3");
    this.left_foot_axis = left_leg_3.getChild("left_foot_axis");
    this.left_foot = left_foot_axis.getChild("left_foot");
    this.right_leg_1_axis = core.getChild("right_leg_1_axis");
    this.right_leg_1 = right_leg_1_axis.getChild("right_leg_1");
    this.right_leg_2_axis = right_leg_1.getChild("right_leg_2_axis");
    this.right_leg_2 = right_leg_2_axis.getChild("right_leg_2");
    this.right_leg_3_axis = right_leg_2.getChild("right_leg_3_axis");
    this.right_leg_3 = right_leg_3_axis.getChild("right_leg_3");
    this.right_foot_axis = right_leg_3.getChild("right_foot_axis");
    this.right_foot = right_foot_axis.getChild("right_foot");
  }

  public static LayerDefinition createBodyLayer() {
    var mesh = new MeshDefinition();
    PartDefinition parts = mesh.getRoot();

    PartDefinition core = parts.addOrReplaceChild("core", CubeListBuilder.create().texOffs(1, 58).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 10.5F, 0.0F));
    PartDefinition stomach_axis = core.addOrReplaceChild("stomach_axis", CubeListBuilder.create().texOffs(10, 60).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.08726646F, 0.0F, 0.0F));
    PartDefinition stomach = stomach_axis.addOrReplaceChild("stomach", CubeListBuilder.create().texOffs(28, 10).addBox(-4.0F, -6.0F, -1.5F, 8.0F, 7.3F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition rib_cage_axis = stomach.addOrReplaceChild("rib_cage_axis", CubeListBuilder.create().texOffs(15, 60).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.0F, -0.25F, 0.17453292F, 0.0F, 0.0F));
    PartDefinition rib_cage = rib_cage_axis.addOrReplaceChild("rib_cage", CubeListBuilder.create().texOffs(1, 10).addBox(-4.5F, -5.0F, -2.0F, 9.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition neck_axis = rib_cage.addOrReplaceChild("neck_axis", CubeListBuilder.create().texOffs(20, 60).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.5F, 0.0F, 0.2617994F, 0.0F, 0.0F));
    PartDefinition neck = neck_axis.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(1, 1).addBox(-3.0F, -4.0F, -1.5F, 6.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition head_axis = neck.addOrReplaceChild("head_axis", CubeListBuilder.create().texOffs(25, 60).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.5F, 0.5F, -0.5235988F, 0.0F, 0.0F));
    PartDefinition head = head_axis.addOrReplaceChild("head", CubeListBuilder.create().texOffs(20, 2).addBox(-2.5F, 0.0F, -4.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.25F, 0.25F));
    PartDefinition top_head = head.addOrReplaceChild("top_head", CubeListBuilder.create().texOffs(77, 3).addBox(-2.0F, -0.5F, -2.5F, 4.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.25F, -3.5F));
    PartDefinition top_mouth = head.addOrReplaceChild("top_mouth", CubeListBuilder.create().texOffs(60, 4).addBox(-2.5F, -1.0F, -3.0F, 5.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, -4.0F));
    PartDefinition low_mouth_axis = head.addOrReplaceChild("low_mouth_axis", CubeListBuilder.create().texOffs(30, 60).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -2.0F, 0.08726646F, 0.0F, 0.0F));
    PartDefinition low_mouth = low_mouth_axis.addOrReplaceChild("low_mouth", CubeListBuilder.create().texOffs(39, 3).addBox(-2.5F, 0.0F, -4.0F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -1.0F));
    PartDefinition left_arm_axis = rib_cage.addOrReplaceChild("left_arm_axis", CubeListBuilder.create().texOffs(35, 60).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, -0.08726646F, 0.0F, 0.0F));
    PartDefinition left_arm = left_arm_axis.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(1, 31).addBox(-1.0F, -1.0F, -1.5F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 0.5F, 0.0F));
    PartDefinition left_shoulder_plate = left_arm.addOrReplaceChild("left_shoulder_plate", CubeListBuilder.create().texOffs(14, 36).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -0.5F, 0.0F));
    PartDefinition left_forearm_axis = left_arm.addOrReplaceChild("left_forearm_axis", CubeListBuilder.create().texOffs(40, 60).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 9.0F, 0.0F, -0.2617994F, 0.0F, 0.0F));
    PartDefinition left_forearm = left_forearm_axis.addOrReplaceChild("left_forearm", CubeListBuilder.create().texOffs(31, 36).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.0F));
    PartDefinition left_claws = left_forearm.addOrReplaceChild("left_claws", CubeListBuilder.create().texOffs(40, 36).addBox(-0.5F, 0.0F, -1.5F, 2.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.75F, 0.0F));
    PartDefinition right_arm_axis = rib_cage.addOrReplaceChild("right_arm_axis", CubeListBuilder.create().texOffs(45, 60).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, -0.08726646F, 0.0F, 0.0F));
    PartDefinition right_arm = right_arm_axis.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(51, 31).addBox(-2.0F, -1.0F, -1.5F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 0.5F, 0.0F));
    PartDefinition right_shoulder_plate = right_arm.addOrReplaceChild("right_shoulder_plate", CubeListBuilder.create().texOffs(64, 36).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -0.5F, 0.0F));
    PartDefinition right_forearm_axis = right_arm.addOrReplaceChild("right_forearm_axis", CubeListBuilder.create().texOffs(50, 60).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 9.0F, 0.0F, -0.2617994F, 0.0F, 0.0F));
    PartDefinition right_forearm = right_forearm_axis.addOrReplaceChild("right_forearm", CubeListBuilder.create().texOffs(81, 36).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 1.0F, 0.0F));
    PartDefinition right_claws = right_forearm.addOrReplaceChild("right_claws", CubeListBuilder.create().texOffs(90, 36).addBox(-1.5F, 0.0F, -1.5F, 2.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.75F, 0.0F));
    PartDefinition tail_1_axis = core.addOrReplaceChild("tail_1_axis", CubeListBuilder.create().texOffs(95, 60).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 1.5F, 0.34906584F, 0.0F, 0.0F));
    PartDefinition tail_1 = tail_1_axis.addOrReplaceChild("tail_1", CubeListBuilder.create().texOffs(1, 21).addBox(-2.0F, 0.0F, -3.0F, 4.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition tail_2_axis = tail_1.addOrReplaceChild("tail_2_axis", CubeListBuilder.create().texOffs(100, 60).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, -0.5F, 0.2617994F, 0.0F, 0.0F));
    PartDefinition tail_2 = tail_2_axis.addOrReplaceChild("tail_2", CubeListBuilder.create().texOffs(16, 23).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition tail_3_axis = tail_2.addOrReplaceChild("tail_3_axis", CubeListBuilder.create().texOffs(105, 60).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.5F, -0.75F, 0.2617994F, 0.0F, 0.0F));
    PartDefinition tail_3 = tail_3_axis.addOrReplaceChild("tail_3", CubeListBuilder.create().texOffs(27, 25).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition left_leg_1_axis = core.addOrReplaceChild("left_leg_1_axis", CubeListBuilder.create().texOffs(75, 60).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 0.75F, 0.0F, -0.43633232F, 0.0F, 0.0F));
    PartDefinition left_leg_1 = left_leg_1_axis.addOrReplaceChild("left_leg_1", CubeListBuilder.create().texOffs(49, 46).addBox(-1.0F, -1.0F, -1.75F, 4.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition left_leg_2_axis = left_leg_1.addOrReplaceChild("left_leg_2_axis", CubeListBuilder.create().texOffs(80, 60).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 6.0F, -0.5F, 0.9599311F, 0.0F, 0.0F));
    PartDefinition left_leg_2 = left_leg_2_axis.addOrReplaceChild("left_leg_2", CubeListBuilder.create().texOffs(64, 50).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition left_leg_3_axis = left_leg_2.addOrReplaceChild("left_leg_3_axis", CubeListBuilder.create().texOffs(85, 60).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, -0.87266463F, 0.0F, 0.0F));
    PartDefinition left_leg_3 = left_leg_3_axis.addOrReplaceChild("left_leg_3", CubeListBuilder.create().texOffs(75, 51).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition left_foot_axis = left_leg_3.addOrReplaceChild("left_foot_axis", CubeListBuilder.create().texOffs(90, 60).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 3.25F, 0.0F, 0.34906584F, 0.0F, 0.0F));
    PartDefinition left_foot = left_foot_axis.addOrReplaceChild("left_foot", CubeListBuilder.create().texOffs(84, 52).addBox(-1.5F, 0.0F, -3.5F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition right_leg_1_axis = core.addOrReplaceChild("right_leg_1_axis", CubeListBuilder.create().texOffs(55, 60).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 0.75F, 0.0F, -0.43633232F, 0.0F, 0.0F));
    PartDefinition right_leg_1 = right_leg_1_axis.addOrReplaceChild("right_leg_1", CubeListBuilder.create().texOffs(1, 46).addBox(-3.0F, -1.0F, -1.75F, 4.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition right_leg_2_axis = right_leg_1.addOrReplaceChild("right_leg_2_axis", CubeListBuilder.create().texOffs(60, 60).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 6.0F, -0.5F, 0.9599311F, 0.0F, 0.0F));
    PartDefinition right_leg_2 = right_leg_2_axis.addOrReplaceChild("right_leg_2", CubeListBuilder.create().texOffs(16, 50).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition right_leg_3_axis = right_leg_2.addOrReplaceChild("right_leg_3_axis", CubeListBuilder.create().texOffs(65, 60).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, -0.87266463F, 0.0F, 0.0F));
    PartDefinition right_leg_3 = right_leg_3_axis.addOrReplaceChild("right_leg_3", CubeListBuilder.create().texOffs(27, 51).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
    PartDefinition right_foot_axis = right_leg_3.addOrReplaceChild("right_foot_axis", CubeListBuilder.create().texOffs(70, 60).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 3.25F, 0.0F, 0.34906584F, 0.0F, 0.0F));
    PartDefinition right_foot = right_foot_axis.addOrReplaceChild("right_foot", CubeListBuilder.create().texOffs(36, 52).addBox(-1.5F, 0.0F, -3.5F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

    return LayerDefinition.create(mesh, 110, 64);
  }

  @Override
  public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
    core.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
  }

  @Override
  public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch) {
    getLookAnim(headYaw, headPitch);
    getIdleAnim(entity, ageInTicks);
    getWalkAnim(this.walkPosition, this.walkAmount);
    // After walking: the punch is blended onto the walk/rest values, so it starts and ends without a snap.
    if (this.attackElapsed >= 0.0F) {
      getAttackAnim(this.attackElapsed, this.punchSide);
    }
  }

  @Override
  public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTick) {
    // Vanilla cadence, but the swing amount is capped (see LemurianEntity.WALK_MAX_AMOUNT).
    this.walkPosition = limbSwing;
    this.walkAmount = Math.min(limbSwingAmount, LemurianEntity.WALK_MAX_AMOUNT);
    limbSwing = this.walkPosition;
    limbSwingAmount = this.walkAmount;
    // Rest/walk values of every part the punch touches, set every frame so nothing stays stuck after an attack.
    float forward = entity.getMoveForward(partialTick);
    float lateral = entity.getMoveLateral(partialTick);
    float speed = Math.abs(forward) + Math.abs(lateral);
    if (speed > MIN_DIRECTION_SPEED) {
      this.forwardShare = Math.abs(forward) / speed;
      this.sideShare = 1.0F - this.forwardShare;
      this.forwardDir = forward >= 0.0F ? 1.0F : -1.0F;
      this.sideDir = lateral >= 0.0F ? 1.0F : -1.0F;
    } else {
      this.forwardShare = 1.0F;
      this.sideShare = 0.0F;
      this.forwardDir = 1.0F;
      this.sideDir = 1.0F;
    }
    // The arms keep part of their swing while sidestepping.
    float armSwing = limbSwingAmount * armShare();
    this.left_arm_axis.xRot = -0.08726646F + Mth.cos(limbSwing) * 0.90F * armSwing;
    this.left_forearm_axis.xRot = -0.2617994F + Mth.cos(limbSwing) * 0.75F * armSwing;
    this.right_arm_axis.xRot = -0.08726646F - Mth.cos(limbSwing) * 0.90F * armSwing;
    this.right_forearm_axis.xRot = -0.2617994F - Mth.cos(limbSwing) * 0.75F * armSwing;
    this.head_axis.xRot = -0.5235988F - Mth.cos(limbSwing * 2F) * 0.3F * limbSwingAmount;
    this.neck_axis.xRot = 0.2617994F + Mth.cos(limbSwing * 2F) * 0.3F * limbSwingAmount;
    this.left_arm_axis.zRot = 0.0F;
    this.right_arm_axis.zRot = 0.0F;
    this.rib_cage_axis.xRot = 0.17453292F;
    // Torso leans toward the direction of travel while sidestepping.
    this.rib_cage_axis.zRot = this.sideDir * BODY_SIDE_LEAN * this.sideShare * limbSwingAmount;
    this.stomach_axis.xRot = 0.08726646F;
    this.stomach_axis.zRot = this.sideDir * BODY_SIDE_LEAN * this.sideShare * limbSwingAmount;

    int attackTick = entity.getAttackTick();
    if (attackTick > 0) {
      // The hand is chosen once, on the first frame of the attack, and kept until it ends: the entity flips its
      // flag only while "selecting", and selecting is re-armed on every frame without an attack. The flag is read
      // from the entity (not stored in this shared model) so several Lemurians do not mix hands.
      // As before, the flag is true when the left arm punches (the names are inverted but behavior is kept).
      if (entity.getIsSelectedHand()) {
        entity.getIsRightHandSelected();
        entity.setIsSelectingHand(false);
      }
      this.punchSide = entity.peekRightHandSelected() ? 1.0F : -1.0F;
      this.attackElapsed = AttackAnimation.progress(attackTick, partialTick, LemurianEntity.ATTACK_DURATION);
    } else {
      entity.setIsSelectingHand(true);
      this.attackElapsed = -1.0F;
    }
  }

  /** Share of the arm swing that is kept: all of it walking forward, ARM_SIDE_SWING of it sidestepping. */
  private float armShare() {
    return this.forwardShare + this.sideShare * ARM_SIDE_SWING;
  }

  private void getLookAnim(float headYaw, float headPitch) {
    this.head.xRot = headPitch * 0.023271058F;
    this.head.yRot = headYaw * 0.017453292F / 2.0F;
    this.rib_cage.yRot = headYaw * 0.017453292F / 5.0F;
    this.stomach.yRot = headYaw * 0.017453292F / 5.0F;
  }

  private void getIdleAnim(LemurianEntity entity, float ageInTicks) {
    this.low_mouth.xRot = Mth.cos(ageInTicks * 0.06F) * 0.09F;
    this.neck.xRot = Mth.cos(ageInTicks * 0.06F) * 0.06F;
    this.neck.yRot = Mth.cos(ageInTicks * 0.04F) * 0.06F;
    this.neck.zRot = -Mth.cos(ageInTicks * 0.04F) * 0.06F;
    this.rib_cage.xRot = -Mth.cos(ageInTicks * 0.06F) * 0.06F;
    this.tail_1.xRot = 0.34906584F + Mth.cos(ageInTicks * 0.08F) * 0.09F;
    this.tail_1.zRot = Mth.cos(ageInTicks * 0.12F) * 0.06F;
    this.tail_2.zRot = Mth.cos(ageInTicks * 0.12F) * 0.06F;
    this.tail_3.zRot = Mth.cos(ageInTicks * 0.12F) * 0.06F;
    if (!entity.isMoving(entity)) {
      this.head_axis.xRot = -0.5235988F + Mth.cos(ageInTicks * 0.06F) * 0.06F;
      this.head_axis.yRot = Mth.cos(ageInTicks * 0.05F) * 0.06F;
      this.head_axis.zRot = -Mth.cos(ageInTicks * 0.05F) * 0.06F;
      this.left_arm.xRot = -0.17453292F - Mth.cos(ageInTicks * 0.06F) * 0.03F;
      this.left_arm.zRot = Mth.cos(ageInTicks * 0.09F) * 0.03F;
      this.right_arm.xRot = -0.17453292F + Mth.cos(ageInTicks * 0.06F) * 0.03F;
      this.right_arm.zRot = -Mth.cos(ageInTicks * 0.09F) * 0.03F;
    }
  }

  private void getWalkAnim(float limbSwing, float limbSwingAmount) {
    this.stomach_axis.y = 0 + Mth.cos(limbSwing * 2.0F) * 1.8F * limbSwingAmount;
    this.tail_1_axis.y = -1.0F + Mth.cos(limbSwing * 2.0F) * 1.8F * limbSwingAmount;

    // The torso twist is part of the forward gait.
    float twist = limbSwingAmount * this.forwardShare;
    this.neck_axis.yRot = 0 + Mth.cos(limbSwing) * 0.25F * twist;
    this.rib_cage_axis.yRot = 0 - Mth.cos(limbSwing) * 0.25F * twist;
    this.stomach_axis.yRot = 0 - Mth.cos(limbSwing) * 0.15F * twist;

    this.left_arm_axis.yRot = 0 - Mth.cos(limbSwing) * 0.8F * limbSwingAmount * armShare();
    this.right_arm_axis.yRot = 0 - Mth.cos(limbSwing) * 0.8F * limbSwingAmount * armShare();

    // Left leg at the walk position, right leg half a cycle later: identical curves, so no limp. Walking backward
    // runs the forward gait with the position reversed (the contacts stay at multiples of PI).
    float forwardPosition = this.forwardDir * limbSwing;
    walkLeg(this.left_leg_1_axis, this.left_leg_2_axis, this.left_leg_3_axis, this.left_foot_axis,
        forwardPosition, limbSwing, 1.0F, limbSwingAmount);
    walkLeg(this.right_leg_1_axis, this.right_leg_2_axis, this.right_leg_3_axis, this.right_foot_axis,
        forwardPosition + (float) Math.PI, limbSwing + (float) Math.PI, -1.0F, limbSwingAmount);

    this.tail_1_axis.zRot = Mth.cos(limbSwing) * 0.33F * limbSwingAmount;
    this.tail_2_axis.zRot = Mth.cos(limbSwing) * 0.33F * limbSwingAmount;
    this.tail_3_axis.zRot = Mth.cos(limbSwing) * 0.33F * limbSwingAmount;
  }

  /**
   * One digitigrade leg (hind leg of a deer) at {@code position}; see the axis notes at the top. The hip is a plain
   * swing, farthest forward at position 0 (heel strike) and farthest back half a cycle later. The knee, hock and foot
   * fold together during the second half of the cycle, while the leg swings forward through the air, and are at rest
   * (extended) during the first half, while it supports and pushes back.
   */
  private void walkLeg(ModelPart hip, ModelPart knee, ModelPart hock, ModelPart foot, float position,
      float sidePosition, float side, float amount) {
    float hipSwing = WalkCycle.swing(position, 0.0F);
    // 1 during the swing half of the cycle (peak at 3/4), 0 while the foot is on the ground.
    float lift = WalkCycle.fold(position, (float) Math.PI);
    // Peak late in the support half (3/8 of the cycle), the push-off.
    float push = WalkCycle.fold(position, -PUSH_PHASE) * this.forwardShare;
    // The sidestep lifts on the same half of the cycle as the unreversed forward gait, so the contacts match.
    float sideLift = WalkCycle.fold(sidePosition, (float) Math.PI);
    float liftBlend = lift * this.forwardShare + sideLift * LEG_SIDE_LIFT * this.sideShare;

    float hipDelta = -LEG_HIP_SWING * hipSwing * this.forwardShare;
    float kneeDelta = LEG_KNEE_FOLD * liftBlend;
    float hockDelta = -LEG_HOCK_FOLD * liftBlend;
    // Cancels part of the rotation accumulated down the chain so the sole stays near level, then pitches the toes
    // down at push-off.
    float footDelta = -LEG_FOOT_LEVEL * (hipDelta + kneeDelta + hockDelta) + LEG_FOOT_PUSH * push;

    hip.xRot = LEG_HIP_REST + hipDelta * amount;
    knee.xRot = LEG_KNEE_REST + kneeDelta * amount;
    hock.xRot = LEG_HOCK_REST + hockDelta * amount;
    foot.xRot = LEG_FOOT_REST + footDelta * amount;

    // Sidestep reach toward the direction of travel (see the axis notes); a foot moving toward the middle (outward
    // below zero for this leg's side) is shortened so it closes in without crossing the other leg.
    float travel = this.sideDir * WalkCycle.swing(sidePosition, 0.0F);
    float outward = side * travel;
    hip.zRot = -this.sideDir * LEG_SIDE_SWING * WalkCycle.swing(sidePosition, 0.0F)
        * (outward < 0.0F ? LEG_SIDE_CLOSE : 1.0F) * this.sideShare * amount;
  }


  /**
   * Punch as phases over the elapsed ticks (LemurianEntity ATTACK_* constants): wind-up (fist pulled back, torso
   * turned away), strike (arm driven forward and straightened, torso turned into the punch), hold, recovery.
   * Every value is the walk/rest value blended to the pose by a weight that is 0 at the start and at the end.
   *
   * @param side +1 when the left arm punches, -1 when the right arm punches
   */
  private void getAttackAnim(float elapsed, float side) {
    float windupEnd = LemurianEntity.ATTACK_WINDUP_END;
    float impact = LemurianEntity.ATTACK_IMPACT_TICK;
    float holdEnd = LemurianEntity.ATTACK_HOLD_END;
    float end = LemurianEntity.ATTACK_DURATION;

    float windup = AttackAnimation.easeInOut(AttackAnimation.segment(elapsed, 0.0F, windupEnd));
    float recovery = AttackAnimation.easeInOut(AttackAnimation.segment(elapsed, holdEnd, end));
    // 0 = wind-up pose, 1 = full extension. Ease in: the punch builds speed and lands at impact.
    float strike = AttackAnimation.easeIn(AttackAnimation.segment(elapsed, windupEnd, impact));
    float weight = windup * (1.0F - recovery);

    ModelPart punchArm = side > 0 ? this.left_arm_axis : this.right_arm_axis;
    ModelPart punchForearm = side > 0 ? this.left_forearm_axis : this.right_forearm_axis;
    ModelPart guardArm = side > 0 ? this.right_arm_axis : this.left_arm_axis;
    ModelPart guardForearm = side > 0 ? this.right_forearm_axis : this.left_forearm_axis;

    punchArm.xRot = AttackAnimation.lerp(weight, punchArm.xRot, AttackAnimation.lerp(strike, WINDUP_ARM_X, STRIKE_ARM_X));
    punchForearm.xRot = AttackAnimation.lerp(weight, punchForearm.xRot, AttackAnimation.lerp(strike, WINDUP_FOREARM_X, STRIKE_FOREARM_X));
    // The walk swings both arms sideways; blend that out so the fist goes straight ahead, with the yaw
    // out in the wind-up and slightly inward at contact.
    float armYaw = side * AttackAnimation.lerp(strike, -WINDUP_ARM_YAW_OUT, STRIKE_ARM_YAW_IN);
    punchArm.yRot = AttackAnimation.lerp(weight, punchArm.yRot, armYaw);

    guardArm.xRot = AttackAnimation.lerp(weight, guardArm.xRot, AttackAnimation.lerp(strike, WINDUP_GUARD_ARM_X, STRIKE_GUARD_ARM_X));
    guardForearm.xRot = AttackAnimation.lerp(weight, guardForearm.xRot, AttackAnimation.lerp(strike, WINDUP_GUARD_FOREARM_X, STRIKE_GUARD_FOREARM_X));

    // Torso: turns away from the punching side in the wind-up and into it at contact.
    this.rib_cage_axis.yRot += weight * side * AttackAnimation.lerp(strike, -WINDUP_RIB_YAW, STRIKE_RIB_YAW);
    this.stomach_axis.yRot += weight * side * AttackAnimation.lerp(strike, -WINDUP_STOMACH_YAW, STRIKE_STOMACH_YAW);
    this.rib_cage_axis.xRot += weight * AttackAnimation.lerp(strike, WINDUP_RIB_X, STRIKE_RIB_X);
  }
}
