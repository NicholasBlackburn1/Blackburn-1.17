package net.minecraft.client.renderer.entity;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.scores.Team;
import net.optifine.Config;
import net.optifine.entity.model.CustomEntityModels;
import net.optifine.reflect.Reflector;
import net.optifine.shaders.Shaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class LivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M>
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final float EYE_BED_OFFSET = 0.1F;
    public M model;
    protected final List<RenderLayer> layers = Lists.newArrayList();
    public float renderLimbSwing;
    public float renderLimbSwingAmount;
    public float renderAgeInTicks;
    public float renderHeadYaw;
    public float renderHeadPitch;
    public float renderPartialTicks;
    public static final boolean animateModelLiving = Boolean.getBoolean("animate.model.living");

    public LivingEntityRenderer(EntityRendererProvider.Context p_174289_, M p_174290_, float p_174291_)
    {
        super(p_174289_);
        this.model = p_174290_;
        this.shadowRadius = p_174291_;
    }

    public final boolean addLayer(RenderLayer<T, M> pLayer)
    {
        return this.layers.add(pLayer);
    }

    public M getModel()
    {
        return this.model;
    }

    public void render(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight)
    {
        if (!Reflector.RenderLivingEvent_Pre_Constructor.exists() || !Reflector.postForgeBusEvent(Reflector.RenderLivingEvent_Pre_Constructor, pEntity, this, pPartialTicks, pMatrixStack, pBuffer, pPackedLight))
        {
            if (animateModelLiving)
            {
                pEntity.animationSpeed = 1.0F;
            }

            pMatrixStack.pushPose();
            this.model.attackTime = this.getAttackAnim(pEntity, pPartialTicks);
            this.model.riding = pEntity.isPassenger();

            if (Reflector.IForgeEntity_shouldRiderSit.exists())
            {
                this.model.riding = pEntity.isPassenger() && pEntity.getVehicle() != null && Reflector.callBoolean(pEntity.getVehicle(), Reflector.IForgeEntity_shouldRiderSit);
            }

            this.model.young = pEntity.isBaby();
            float f = Mth.rotLerp(pPartialTicks, pEntity.yBodyRotO, pEntity.yBodyRot);
            float f1 = Mth.rotLerp(pPartialTicks, pEntity.yHeadRotO, pEntity.yHeadRot);
            float f2 = f1 - f;

            if (this.model.riding && pEntity.getVehicle() instanceof LivingEntity)
            {
                LivingEntity livingentity = (LivingEntity)pEntity.getVehicle();
                f = Mth.rotLerp(pPartialTicks, livingentity.yBodyRotO, livingentity.yBodyRot);
                f2 = f1 - f;
                float f3 = Mth.wrapDegrees(f2);

                if (f3 < -85.0F)
                {
                    f3 = -85.0F;
                }

                if (f3 >= 85.0F)
                {
                    f3 = 85.0F;
                }

                f = f1 - f3;

                if (f3 * f3 > 2500.0F)
                {
                    f += f3 * 0.2F;
                }

                f2 = f1 - f;
            }

            float f7 = Mth.lerp(pPartialTicks, pEntity.xRotO, pEntity.getXRot());

            if (pEntity.getPose() == Pose.SLEEPING)
            {
                Direction direction = pEntity.getBedOrientation();

                if (direction != null)
                {
                    float f4 = pEntity.getEyeHeight(Pose.STANDING) - 0.1F;
                    pMatrixStack.translate((double)((float)(-direction.getStepX()) * f4), 0.0D, (double)((float)(-direction.getStepZ()) * f4));
                }
            }

            float f8 = this.getBob(pEntity, pPartialTicks);
            this.setupRotations(pEntity, pMatrixStack, f8, f, pPartialTicks);
            pMatrixStack.scale(-1.0F, -1.0F, 1.0F);
            this.scale(pEntity, pMatrixStack, pPartialTicks);
            pMatrixStack.translate(0.0D, (double) - 1.501F, 0.0D);
            float f9 = 0.0F;
            float f5 = 0.0F;

            if (!pEntity.isPassenger() && pEntity.isAlive())
            {
                f9 = Mth.lerp(pPartialTicks, pEntity.animationSpeedOld, pEntity.animationSpeed);
                f5 = pEntity.animationPosition - pEntity.animationSpeed * (1.0F - pPartialTicks);

                if (pEntity.isBaby())
                {
                    f5 *= 3.0F;
                }

                if (f9 > 1.0F)
                {
                    f9 = 1.0F;
                }
            }

            this.model.prepareMobModel(pEntity, f5, f9, pPartialTicks);
            this.model.setupAnim(pEntity, f5, f9, f8, f2, f7);

            if (CustomEntityModels.isActive())
            {
                this.renderLimbSwing = f5;
                this.renderLimbSwingAmount = f9;
                this.renderAgeInTicks = f8;
                this.renderHeadYaw = f2;
                this.renderHeadPitch = f7;
                this.renderPartialTicks = pPartialTicks;
            }

            boolean flag = Config.isShaders();
            Minecraft minecraft = Minecraft.getInstance();
            boolean flag1 = this.isBodyVisible(pEntity);
            boolean flag2 = !flag1 && !pEntity.isInvisibleTo(minecraft.player);
            boolean flag3 = minecraft.shouldEntityAppearGlowing(pEntity);
            RenderType rendertype = this.getRenderType(pEntity, flag1, flag2, flag3);

            if (rendertype != null)
            {
                VertexConsumer vertexconsumer = pBuffer.getBuffer(rendertype);
                float f6 = this.getWhiteOverlayProgress(pEntity, pPartialTicks);

                if (flag)
                {
                    if (pEntity.hurtTime > 0 || pEntity.deathTime > 0)
                    {
                        Shaders.setEntityColor(1.0F, 0.0F, 0.0F, 0.3F);
                    }

                    if (f6 > 0.0F)
                    {
                        Shaders.setEntityColor(f6, f6, f6, 0.5F);
                    }
                }

                int i = getOverlayCoords(pEntity, f6);
                this.model.renderToBuffer(pMatrixStack, vertexconsumer, pPackedLight, i, 1.0F, 1.0F, 1.0F, flag2 ? 0.15F : 1.0F);
            }

            if (!pEntity.isSpectator())
            {
                for (RenderLayer<T, M> renderlayer : this.layers)
                {
                    renderlayer.render(pMatrixStack, pBuffer, pPackedLight, pEntity, f5, f9, pPartialTicks, f8, f2, f7);
                }
            }

            if (Config.isShaders())
            {
                Shaders.setEntityColor(0.0F, 0.0F, 0.0F, 0.0F);
            }

            pMatrixStack.popPose();
            super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);

            if (Reflector.RenderLivingEvent_Post_Constructor.exists())
            {
                Reflector.postForgeBusEvent(Reflector.RenderLivingEvent_Post_Constructor, pEntity, this, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
            }
        }
    }

    @Nullable
    protected RenderType getRenderType(T p_115322_, boolean p_115323_, boolean p_115324_, boolean p_115325_)
    {
        ResourceLocation resourcelocation = this.getTextureLocation(p_115322_);

        if (this.getLocationTextureCustom() != null)
        {
            resourcelocation = this.getLocationTextureCustom();
        }

        if (p_115324_)
        {
            return RenderType.itemEntityTranslucentCull(resourcelocation);
        }
        else if (p_115323_)
        {
            return this.model.renderType(resourcelocation);
        }
        else if (p_115322_.hasGlowingTag() && !Config.getMinecraft().levelRenderer.shouldShowEntityOutlines())
        {
            return this.model.renderType(resourcelocation);
        }
        else
        {
            return p_115325_ ? RenderType.outline(resourcelocation) : null;
        }
    }

    public static int getOverlayCoords(LivingEntity pLivingEntity, float pU)
    {
        return OverlayTexture.pack(OverlayTexture.u(pU), OverlayTexture.v(pLivingEntity.hurtTime > 0 || pLivingEntity.deathTime > 0));
    }

    protected boolean isBodyVisible(T pLivingEntity)
    {
        return !pLivingEntity.isInvisible();
    }

    private static float sleepDirectionToRotation(Direction pFacing)
    {
        switch (pFacing)
        {
            case SOUTH:
                return 90.0F;

            case WEST:
                return 0.0F;

            case NORTH:
                return 270.0F;

            case EAST:
                return 180.0F;

            default:
                return 0.0F;
        }
    }

    protected boolean isShaking(T p_115304_)
    {
        return p_115304_.isFullyFrozen();
    }

    protected void setupRotations(T pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks)
    {
        if (this.isShaking(pEntityLiving))
        {
            pRotationYaw += (float)(Math.cos((double)pEntityLiving.tickCount * 3.25D) * Math.PI * (double)0.4F);
        }

        Pose pose = pEntityLiving.getPose();

        if (pose != Pose.SLEEPING)
        {
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - pRotationYaw));
        }

        if (pEntityLiving.deathTime > 0)
        {
            float f = ((float)pEntityLiving.deathTime + pPartialTicks - 1.0F) / 20.0F * 1.6F;
            f = Mth.sqrt(f);

            if (f > 1.0F)
            {
                f = 1.0F;
            }

            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * this.getFlipDegrees(pEntityLiving)));
        }
        else if (pEntityLiving.isAutoSpinAttack())
        {
            pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F - pEntityLiving.getXRot()));
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(((float)pEntityLiving.tickCount + pPartialTicks) * -75.0F));
        }
        else if (pose == Pose.SLEEPING)
        {
            Direction direction = pEntityLiving.getBedOrientation();
            float f1 = direction != null ? sleepDirectionToRotation(direction) : pRotationYaw;
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f1));
            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(this.getFlipDegrees(pEntityLiving)));
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(270.0F));
        }
        else if (pEntityLiving.hasCustomName() || pEntityLiving instanceof Player)
        {
            String s = ChatFormatting.stripFormatting(pEntityLiving.getName().getString());

            if (("Dinnerbone".equals(s) || "Grumm".equals(s)) && (!(pEntityLiving instanceof Player) || ((Player)pEntityLiving).isModelPartShown(PlayerModelPart.CAPE)))
            {
                pMatrixStack.translate(0.0D, (double)(pEntityLiving.getBbHeight() + 0.1F), 0.0D);
                pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
            }
        }
    }

    protected float getAttackAnim(T pLivingBase, float pPartialTickTime)
    {
        return pLivingBase.getAttackAnim(pPartialTickTime);
    }

    protected float getBob(T pLivingBase, float pPartialTicks)
    {
        return (float)pLivingBase.tickCount + pPartialTicks;
    }

    protected float getFlipDegrees(T pLivingEntity)
    {
        return 90.0F;
    }

    protected float getWhiteOverlayProgress(T pLivingEntity, float pPartialTicks)
    {
        return 0.0F;
    }

    protected void scale(T pLivingEntity, PoseStack pMatrixStack, float pPartialTickTime)
    {
    }

    protected boolean shouldShowName(T pEntity)
    {
        double d0 = this.entityRenderDispatcher.distanceToSqr(pEntity);
        float f = pEntity.isDiscrete() ? 32.0F : 64.0F;

        if (d0 >= (double)(f * f))
        {
            return false;
        }
        else
        {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer localplayer = minecraft.player;
            boolean flag = !pEntity.isInvisibleTo(localplayer);

            if (pEntity != localplayer)
            {
                Team team = pEntity.getTeam();
                Team team1 = localplayer.getTeam();

                if (team != null)
                {
                    Team.Visibility team$visibility = team.getNameTagVisibility();

                    switch (team$visibility)
                    {
                        case ALWAYS:
                            return flag;

                        case NEVER:
                            return false;

                        case HIDE_FOR_OTHER_TEAMS:
                            return team1 == null ? flag : team.isAlliedTo(team1) && (team.canSeeFriendlyInvisibles() || flag);

                        case HIDE_FOR_OWN_TEAM:
                            return team1 == null ? flag : !team.isAlliedTo(team1) && flag;

                        default:
                            return true;
                    }
                }
            }

            return Minecraft.renderNames() && pEntity != minecraft.getCameraEntity() && flag && !pEntity.isVehicle();
        }
    }

    public List<RenderLayer> getLayerRenderers()
    {
        return this.layers;
    }
}
