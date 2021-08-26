package net.optifine.entity.model.anim;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.optifine.expr.IExpressionFloat;

public enum RenderEntityParameterFloat implements IExpressionFloat
{
    LIMB_SWING("limb_swing"),
    LIMB_SWING_SPEED("limb_speed"),
    AGE("age"),
    HEAD_YAW("head_yaw"),
    HEAD_PITCH("head_pitch"),
    HEALTH("health"),
    HURT_TIME("hurt_time"),
    IDLE_TIME("idle_time"),
    MAX_HEALTH("max_health"),
    MOVE_FORWARD("move_forward"),
    MOVE_STRAFING("move_strafing"),
    PARTIAL_TICKS("partial_ticks"),
    POS_X("pos_x", true),
    POS_Y("pos_y", true),
    POS_Z("pos_z", true),
    REVENGE_TIME("revenge_time"),
    SWING_PROGRESS("swing_progress");

    private String name;
    private boolean blockEntity;
    private EntityRenderDispatcher renderManager;
    private static final RenderEntityParameterFloat[] VALUES = values();

    private RenderEntityParameterFloat(String name)
    {
        this(name, false);
    }

    private RenderEntityParameterFloat(String name, boolean blockEntity)
    {
        this.name = name;
        this.blockEntity = blockEntity;
        this.renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
    }

    public String getName()
    {
        return this.name;
    }

    public boolean isBlockEntity()
    {
        return this.blockEntity;
    }

    public float eval()
    {
        BlockEntity blockentity = BlockEntityRenderDispatcher.tileEntityRendered;

        if (blockentity != null)
        {
            switch (this)
            {
                case POS_X:
                    return (float)blockentity.getBlockPos().getX();

                case POS_Y:
                    return (float)blockentity.getBlockPos().getY();

                case POS_Z:
                    return (float)blockentity.getBlockPos().getZ();
            }
        }

        EntityRenderer entityrenderer = this.renderManager.renderRender;

        if (entityrenderer == null)
        {
            return 0.0F;
        }
        else
        {
            Entity entity = this.renderManager.renderEntity;

            if (entity == null)
            {
                return 0.0F;
            }
            else
            {
                if (entityrenderer instanceof LivingEntityRenderer)
                {
                    LivingEntityRenderer livingentityrenderer = (LivingEntityRenderer)entityrenderer;

                    switch (this)
                    {
                        case LIMB_SWING:
                            return livingentityrenderer.renderLimbSwing;

                        case LIMB_SWING_SPEED:
                            return livingentityrenderer.renderLimbSwingAmount;

                        case AGE:
                            return livingentityrenderer.renderAgeInTicks;

                        case HEAD_YAW:
                            return livingentityrenderer.renderHeadYaw;

                        case HEAD_PITCH:
                            return livingentityrenderer.renderHeadPitch;

                        default:
                            if (entity instanceof LivingEntity)
                            {
                                LivingEntity livingentity = (LivingEntity)entity;

                                switch (this)
                                {
                                    case HEALTH:
                                        return livingentity.getHealth();

                                    case HURT_TIME:
                                        return (float)livingentity.hurtTime;

                                    case IDLE_TIME:
                                        return (float)livingentity.getNoActionTime();

                                    case MAX_HEALTH:
                                        return livingentity.getMaxHealth();

                                    case MOVE_FORWARD:
                                        return livingentity.zza;

                                    case MOVE_STRAFING:
                                        return livingentity.xxa;

                                    case REVENGE_TIME:
                                        return (float)livingentity.getLastHurtByMobTimestamp();

                                    case SWING_PROGRESS:
                                        return livingentity.getAttackAnim(livingentityrenderer.renderPartialTicks);
                                }
                            }
                    }
                }

                switch (this)
                {
                    case POS_X:
                        return (float)entity.getX();

                    case POS_Y:
                        return (float)entity.getY();

                    case POS_Z:
                        return (float)entity.getZ();

                    default:
                        return 0.0F;
                }
            }
        }
    }

    public static RenderEntityParameterFloat parse(String str)
    {
        if (str == null)
        {
            return null;
        }
        else
        {
            for (int i = 0; i < VALUES.length; ++i)
            {
                RenderEntityParameterFloat renderentityparameterfloat = VALUES[i];

                if (renderentityparameterfloat.getName().equals(str))
                {
                    return renderentityparameterfloat;
                }
            }

            return null;
        }
    }
}
