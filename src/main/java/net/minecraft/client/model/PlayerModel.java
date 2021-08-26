package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Random;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class PlayerModel<T extends LivingEntity> extends HumanoidModel<T>
{
    private static final String EAR = "ear";
    private static final String CLOAK = "cloak";
    private static final String LEFT_SLEEVE = "left_sleeve";
    private static final String RIGHT_SLEEVE = "right_sleeve";
    private static final String LEFT_PANTS = "left_pants";
    private static final String RIGHT_PANTS = "right_pants";
    private final List<ModelPart> parts;
    public final ModelPart leftSleeve;
    public final ModelPart rightSleeve;
    public final ModelPart leftPants;
    public final ModelPart rightPants;
    public final ModelPart jacket;
    private final ModelPart cloak;
    private final ModelPart ear;
    private final boolean slim;

    public PlayerModel(ModelPart p_170821_, boolean p_170822_)
    {
        super(p_170821_, RenderType::entityTranslucent);
        this.slim = p_170822_;
        this.ear = p_170821_.getChilds("ear");
        this.cloak = p_170821_.getChilds("cloak");
        this.leftSleeve = p_170821_.getChilds("left_sleeve");
        this.rightSleeve = p_170821_.getChilds("right_sleeve");
        this.leftPants = p_170821_.getChilds("left_pants");
        this.rightPants = p_170821_.getChilds("right_pants");
        this.jacket = p_170821_.getChilds("jacket");
        this.parts = p_170821_.getAllParts().filter((p_170824_) ->
        {
            return !p_170824_.isEmpty();
        }).collect(ImmutableList.toImmutableList());
    }

    public static MeshDefinition createMesh(CubeDeformation p_170826_, boolean p_170827_)
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(p_170826_, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("ear", CubeListBuilder.create().texOffs(24, 0).addBox(-3.0F, -6.0F, -1.0F, 6.0F, 6.0F, 1.0F, p_170826_), PartPose.ZERO);
        partdefinition.addOrReplaceChild("cloak", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F, p_170826_, 1.0F, 0.5F), PartPose.offset(0.0F, 0.0F, 0.0F));
        float f = 0.25F;

        if (p_170827_)
        {
            partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, p_170826_), PartPose.offset(5.0F, 2.5F, 0.0F));
            partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, p_170826_), PartPose.offset(-5.0F, 2.5F, 0.0F));
            partdefinition.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, p_170826_.extend(0.25F)), PartPose.offset(5.0F, 2.5F, 0.0F));
            partdefinition.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, p_170826_.extend(0.25F)), PartPose.offset(-5.0F, 2.5F, 0.0F));
        }
        else
        {
            partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170826_), PartPose.offset(5.0F, 2.0F, 0.0F));
            partdefinition.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170826_.extend(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));
            partdefinition.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170826_.extend(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));
        }

        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170826_), PartPose.offset(1.9F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_pants", CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170826_.extend(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild("right_pants", CubeListBuilder.create().texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_170826_.extend(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, p_170826_.extend(0.25F)), PartPose.ZERO);
        return meshdefinition;
    }

    protected Iterable<ModelPart> bodyParts()
    {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(this.leftPants, this.rightPants, this.leftSleeve, this.rightSleeve, this.jacket));
    }

    public void renderEars(PoseStack pMatrixStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay)
    {
        this.ear.copyFrom(this.head);
        this.ear.x = 0.0F;
        this.ear.y = 0.0F;
        this.ear.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay);
    }

    public void renderCloak(PoseStack pMatrixStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay)
    {
        this.cloak.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay);
    }

    public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
    {
        super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.jacket.copyFrom(this.body);

        if (pEntity.getItemBySlot(EquipmentSlot.CHEST).isEmpty())
        {
            if (pEntity.isCrouching())
            {
                this.cloak.z = 1.4F;
                this.cloak.y = 1.85F;
            }
            else
            {
                this.cloak.z = 0.0F;
                this.cloak.y = 0.0F;
            }
        }
        else if (pEntity.isCrouching())
        {
            this.cloak.z = 0.3F;
            this.cloak.y = 0.8F;
        }
        else
        {
            this.cloak.z = -1.1F;
            this.cloak.y = -0.85F;
        }
    }

    public void setAllVisible(boolean pVisible)
    {
        super.setAllVisible(pVisible);
        this.leftSleeve.visible = pVisible;
        this.rightSleeve.visible = pVisible;
        this.leftPants.visible = pVisible;
        this.rightPants.visible = pVisible;
        this.jacket.visible = pVisible;
        this.cloak.visible = pVisible;
        this.ear.visible = pVisible;
    }

    public void translateToHand(HumanoidArm pSide, PoseStack pMatrixStack)
    {
        ModelPart modelpart = this.getArm(pSide);

        if (this.slim)
        {
            float f = 0.5F * (float)(pSide == HumanoidArm.RIGHT ? 1 : -1);
            modelpart.x += f;
            modelpart.translateAndRotate(pMatrixStack);
            modelpart.x -= f;
        }
        else
        {
            modelpart.translateAndRotate(pMatrixStack);
        }
    }

    public ModelPart getRandomModelPart(Random pRandom)
    {
        return this.parts.get(pRandom.nextInt(this.parts.size()));
    }
}
