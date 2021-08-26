package net.minecraft.client;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.optifine.reflect.Reflector;

public class Camera
{
    private boolean initialized;
    private BlockGetter level;
    private Entity entity;
    private Vec3 position = Vec3.ZERO;
    private final BlockPos.MutableBlockPos blockPosition = new BlockPos.MutableBlockPos();
    private final Vector3f forwards = new Vector3f(0.0F, 0.0F, 1.0F);
    private final Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
    private final Vector3f left = new Vector3f(1.0F, 0.0F, 0.0F);
    private float xRot;
    private float yRot;
    private final Quaternion rotation = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
    private boolean detached;
    private float eyeHeight;
    private float eyeHeightOld;
    public static final float FOG_DISTANCE_SCALE = 0.083333336F;

    public void setup(BlockGetter pLevel, Entity pRenderViewEntity, boolean pThirdPerson, boolean pThirdPersonReverse, float pPartialTicks)
    {
        this.initialized = true;
        this.level = pLevel;
        this.entity = pRenderViewEntity;
        this.detached = pThirdPerson;
        this.setRotation(pRenderViewEntity.getViewYRot(pPartialTicks), pRenderViewEntity.getViewXRot(pPartialTicks));
        this.setPosition(Mth.lerp((double)pPartialTicks, pRenderViewEntity.xo, pRenderViewEntity.getX()), Mth.lerp((double)pPartialTicks, pRenderViewEntity.yo, pRenderViewEntity.getY()) + (double)Mth.lerp(pPartialTicks, this.eyeHeightOld, this.eyeHeight), Mth.lerp((double)pPartialTicks, pRenderViewEntity.zo, pRenderViewEntity.getZ()));

        if (pThirdPerson)
        {
            if (pThirdPersonReverse)
            {
                this.setRotation(this.yRot + 180.0F, -this.xRot);
            }

            this.move(-this.getMaxZoom(4.0D), 0.0D, 0.0D);
        }
        else if (pRenderViewEntity instanceof LivingEntity && ((LivingEntity)pRenderViewEntity).isSleeping())
        {
            Direction direction = ((LivingEntity)pRenderViewEntity).getBedOrientation();
            this.setRotation(direction != null ? direction.toYRot() - 180.0F : 0.0F, 0.0F);
            this.move(0.0D, 0.3D, 0.0D);
        }
    }

    public void tick()
    {
        if (this.entity != null)
        {
            this.eyeHeightOld = this.eyeHeight;
            this.eyeHeight += (this.entity.getEyeHeight() - this.eyeHeight) * 0.5F;
        }
    }

    private double getMaxZoom(double pStartingDistance)
    {
        for (int i = 0; i < 8; ++i)
        {
            float f = (float)((i & 1) * 2 - 1);
            float f1 = (float)((i >> 1 & 1) * 2 - 1);
            float f2 = (float)((i >> 2 & 1) * 2 - 1);
            f = f * 0.1F;
            f1 = f1 * 0.1F;
            f2 = f2 * 0.1F;
            Vec3 vec3 = this.position.add((double)f, (double)f1, (double)f2);
            Vec3 vec31 = new Vec3(this.position.x - (double)this.forwards.x() * pStartingDistance + (double)f + (double)f2, this.position.y - (double)this.forwards.y() * pStartingDistance + (double)f1, this.position.z - (double)this.forwards.z() * pStartingDistance + (double)f2);
            HitResult hitresult = this.level.clip(new ClipContext(vec3, vec31, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, this.entity));

            if (hitresult.getType() != HitResult.Type.MISS)
            {
                double d0 = hitresult.getLocation().distanceTo(this.position);

                if (d0 < pStartingDistance)
                {
                    pStartingDistance = d0;
                }
            }
        }

        return pStartingDistance;
    }

    protected void move(double pDistanceOffset, double p_90570_, double pVerticalOffset)
    {
        double d0 = (double)this.forwards.x() * pDistanceOffset + (double)this.up.x() * p_90570_ + (double)this.left.x() * pVerticalOffset;
        double d1 = (double)this.forwards.y() * pDistanceOffset + (double)this.up.y() * p_90570_ + (double)this.left.y() * pVerticalOffset;
        double d2 = (double)this.forwards.z() * pDistanceOffset + (double)this.up.z() * p_90570_ + (double)this.left.z() * pVerticalOffset;
        this.setPosition(new Vec3(this.position.x + d0, this.position.y + d1, this.position.z + d2));
    }

    protected void setRotation(float pPitch, float pYaw)
    {
        this.xRot = pYaw;
        this.yRot = pPitch;
        this.rotation.set(0.0F, 0.0F, 0.0F, 1.0F);
        this.rotation.mul(Vector3f.YP.rotationDegrees(-pPitch));
        this.rotation.mul(Vector3f.XP.rotationDegrees(pYaw));
        this.forwards.set(0.0F, 0.0F, 1.0F);
        this.forwards.transform(this.rotation);
        this.up.set(0.0F, 1.0F, 0.0F);
        this.up.transform(this.rotation);
        this.left.set(1.0F, 0.0F, 0.0F);
        this.left.transform(this.rotation);
    }

    protected void setPosition(double pX, double p_90586_, double pY)
    {
        this.setPosition(new Vec3(pX, p_90586_, pY));
    }

    protected void setPosition(Vec3 pX)
    {
        this.position = pX;
        this.blockPosition.set(pX.x, pX.y, pX.z);
    }

    public Vec3 getPosition()
    {
        return this.position;
    }

    public BlockPos getBlockPosition()
    {
        return this.blockPosition;
    }

    public float getXRot()
    {
        return this.xRot;
    }

    public float getYRot()
    {
        return this.yRot;
    }

    public Quaternion rotation()
    {
        return this.rotation;
    }

    public Entity getEntity()
    {
        return this.entity;
    }

    public boolean isInitialized()
    {
        return this.initialized;
    }

    public boolean isDetached()
    {
        return this.detached;
    }

    public Camera.NearPlane getNearPlane()
    {
        Minecraft minecraft = Minecraft.getInstance();
        double d0 = (double)minecraft.getWindow().getWidth() / (double)minecraft.getWindow().getHeight();
        double d1 = Math.tan(minecraft.options.fov * (double)((float)Math.PI / 180F) / 2.0D) * (double)0.05F;
        double d2 = d1 * d0;
        Vec3 vec3 = (new Vec3(this.forwards)).scale((double)0.05F);
        Vec3 vec31 = (new Vec3(this.left)).scale(d2);
        Vec3 vec32 = (new Vec3(this.up)).scale(d1);
        return new Camera.NearPlane(vec3, vec31, vec32);
    }

    public FogType getFluidInCamera()
    {
        if (!this.initialized)
        {
            return FogType.NONE;
        }
        else
        {
            FluidState fluidstate = this.level.getFluidState(this.blockPosition);

            if (fluidstate.is(FluidTags.WATER) && this.position.y < (double)((float)this.blockPosition.getY() + fluidstate.getHeight(this.level, this.blockPosition)))
            {
                return FogType.WATER;
            }
            else
            {
                Camera.NearPlane camera$nearplane = this.getNearPlane();

                for (Vec3 vec3 : Arrays.asList(camera$nearplane.forward, camera$nearplane.getTopLeft(), camera$nearplane.getTopRight(), camera$nearplane.getBottomLeft(), camera$nearplane.getBottomRight()))
                {
                    Vec3 vec31 = this.position.add(vec3);
                    BlockPos blockpos = new BlockPos(vec31);
                    FluidState fluidstate1 = this.level.getFluidState(blockpos);

                    if (fluidstate1.is(FluidTags.LAVA))
                    {
                        if (vec31.y <= (double)(fluidstate1.getHeight(this.level, blockpos) + (float)blockpos.getY()))
                        {
                            return FogType.LAVA;
                        }
                    }
                    else
                    {
                        BlockState blockstate = this.level.getBlockState(blockpos);

                        if (blockstate.is(Blocks.POWDER_SNOW))
                        {
                            return FogType.POWDER_SNOW;
                        }
                    }
                }

                return FogType.NONE;
            }
        }
    }

    public BlockState getBlockState()
    {
        return !this.initialized ? Blocks.AIR.defaultBlockState() : this.level.getBlockState(this.blockPosition);
    }

    public void setAnglesInternal(float yaw, float pitch)
    {
        this.yRot = yaw;
        this.xRot = pitch;
    }

    public BlockState getBlockAtCamera()
    {
        if (!this.initialized)
        {
            return Blocks.AIR.defaultBlockState();
        }
        else
        {
            BlockState blockstate = this.level.getBlockState(this.blockPosition);

            if (Reflector.IForgeBlockState_getStateAtViewpoint.exists())
            {
                blockstate = (BlockState)Reflector.call(blockstate, Reflector.IForgeBlockState_getStateAtViewpoint, this.level, this.blockPosition, this.position);
            }

            return blockstate;
        }
    }

    public final Vector3f getLookVector()
    {
        return this.forwards;
    }

    public final Vector3f getUpVector()
    {
        return this.up;
    }

    public final Vector3f getLeftVector()
    {
        return this.left;
    }

    public void reset()
    {
        this.level = null;
        this.entity = null;
        this.initialized = false;
    }

    public static class NearPlane
    {
        final Vec3 forward;
        private final Vec3 left;
        private final Vec3 up;

        NearPlane(Vec3 pForward, Vec3 pLeft, Vec3 pUp)
        {
            this.forward = pForward;
            this.left = pLeft;
            this.up = pUp;
        }

        public Vec3 getTopLeft()
        {
            return this.forward.add(this.up).add(this.left);
        }

        public Vec3 getTopRight()
        {
            return this.forward.add(this.up).subtract(this.left);
        }

        public Vec3 getBottomLeft()
        {
            return this.forward.subtract(this.up).add(this.left);
        }

        public Vec3 getBottomRight()
        {
            return this.forward.subtract(this.up).subtract(this.left);
        }

        public Vec3 getPointOnPlane(float p_167696_, float p_167697_)
        {
            return this.forward.add(this.up.scale((double)p_167697_)).subtract(this.left.scale((double)p_167696_));
        }
    }
}
