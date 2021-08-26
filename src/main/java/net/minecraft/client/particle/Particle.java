package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.util.Mth;
import net.minecraft.util.RewindableStream;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.optifine.BlockPosM;

public abstract class Particle
{
    private static final AABB INITIAL_AABB = new AABB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    protected final ClientLevel level;
    protected double xo;
    protected double yo;
    protected double zo;
    protected double x;
    protected double y;
    protected double z;
    protected double xd;
    protected double yd;
    protected double zd;
    private AABB bb = INITIAL_AABB;
    protected boolean onGround;
    protected boolean hasPhysics = true;
    private boolean stoppedByCollision;
    protected boolean removed;
    protected float bbWidth = 0.6F;
    protected float bbHeight = 1.8F;
    protected final Random random = new Random();
    protected int age;
    protected int lifetime;
    protected float gravity;
    protected float rCol = 1.0F;
    protected float gCol = 1.0F;
    protected float bCol = 1.0F;
    protected float alpha = 1.0F;
    protected float roll;
    protected float oRoll;
    protected float friction = 0.98F;
    protected boolean speedUpWhenYMotionIsBlocked = false;
    private BlockPosM blockPosM = new BlockPosM();

    protected Particle(ClientLevel p_107234_, double p_107235_, double p_107236_, double p_107237_)
    {
        this.level = p_107234_;
        this.setSize(0.2F, 0.2F);
        this.setPos(p_107235_, p_107236_, p_107237_);
        this.xo = p_107235_;
        this.yo = p_107236_;
        this.zo = p_107237_;
        this.lifetime = (int)(4.0F / (this.random.nextFloat() * 0.9F + 0.1F));
    }

    public Particle(ClientLevel p_107239_, double p_107240_, double p_107241_, double p_107242_, double p_107243_, double p_107244_, double p_107245_)
    {
        this(p_107239_, p_107240_, p_107241_, p_107242_);
        this.xd = p_107243_ + (Math.random() * 2.0D - 1.0D) * (double)0.4F;
        this.yd = p_107244_ + (Math.random() * 2.0D - 1.0D) * (double)0.4F;
        this.zd = p_107245_ + (Math.random() * 2.0D - 1.0D) * (double)0.4F;
        double d0 = (Math.random() + Math.random() + 1.0D) * (double)0.15F;
        double d1 = Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
        this.xd = this.xd / d1 * d0 * (double)0.4F;
        this.yd = this.yd / d1 * d0 * (double)0.4F + (double)0.1F;
        this.zd = this.zd / d1 * d0 * (double)0.4F;
    }

    public Particle setPower(float pMultiplier)
    {
        this.xd *= (double)pMultiplier;
        this.yd = (this.yd - (double)0.1F) * (double)pMultiplier + (double)0.1F;
        this.zd *= (double)pMultiplier;
        return this;
    }

    public void setParticleSpeed(double p_172261_, double p_172262_, double p_172263_)
    {
        this.xd = p_172261_;
        this.yd = p_172262_;
        this.zd = p_172263_;
    }

    public Particle scale(float pScale)
    {
        this.setSize(0.2F * pScale, 0.2F * pScale);
        return this;
    }

    public void setColor(float pParticleRed, float pParticleGreen, float pParticleBlue)
    {
        this.rCol = pParticleRed;
        this.gCol = pParticleGreen;
        this.bCol = pParticleBlue;
    }

    protected void setAlpha(float pAlpha)
    {
        this.alpha = pAlpha;
    }

    public void setLifetime(int pParticleLifeTime)
    {
        this.lifetime = pParticleLifeTime;
    }

    public int getLifetime()
    {
        return this.lifetime;
    }

    public void tick()
    {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime)
        {
            this.remove();
        }
        else
        {
            this.yd -= 0.04D * (double)this.gravity;
            this.move(this.xd, this.yd, this.zd);

            if (this.speedUpWhenYMotionIsBlocked && this.y == this.yo)
            {
                this.xd *= 1.1D;
                this.zd *= 1.1D;
            }

            this.xd *= (double)this.friction;
            this.yd *= (double)this.friction;
            this.zd *= (double)this.friction;

            if (this.onGround)
            {
                this.xd *= (double)0.7F;
                this.zd *= (double)0.7F;
            }
        }
    }

    public abstract void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks);

    public abstract ParticleRenderType getRenderType();

    public String toString()
    {
        return this.getClass().getSimpleName() + ", Pos (" + this.x + "," + this.y + "," + this.z + "), RGBA (" + this.rCol + "," + this.gCol + "," + this.bCol + "," + this.alpha + "), Age " + this.age;
    }

    public void remove()
    {
        this.removed = true;
    }

    protected void setSize(float pParticleWidth, float pParticleHeight)
    {
        if (pParticleWidth != this.bbWidth || pParticleHeight != this.bbHeight)
        {
            this.bbWidth = pParticleWidth;
            this.bbHeight = pParticleHeight;
            AABB aabb = this.getBoundingBox();
            double d0 = (aabb.minX + aabb.maxX - (double)pParticleWidth) / 2.0D;
            double d1 = (aabb.minZ + aabb.maxZ - (double)pParticleWidth) / 2.0D;
            this.setBoundingBox(new AABB(d0, aabb.minY, d1, d0 + (double)this.bbWidth, aabb.minY + (double)this.bbHeight, d1 + (double)this.bbWidth));
        }
    }

    public void setPos(double pX, double p_107266_, double pY)
    {
        this.x = pX;
        this.y = p_107266_;
        this.z = pY;
        float f = this.bbWidth / 2.0F;
        float f1 = this.bbHeight;
        this.setBoundingBox(new AABB(pX - (double)f, p_107266_, pY - (double)f, pX + (double)f, p_107266_ + (double)f1, pY + (double)f));
    }

    public void move(double pX, double p_107247_, double pY)
    {
        if (!this.stoppedByCollision)
        {
            double d0 = pX;
            double d1 = p_107247_;
            double d2 = pY;

            if (this.hasPhysics && (pX != 0.0D || p_107247_ != 0.0D || pY != 0.0D) && this.hasNearBlocks(pX, p_107247_, pY))
            {
                Vec3 vec3 = Entity.collideBoundingBoxHeuristically((Entity)null, new Vec3(pX, p_107247_, pY), this.getBoundingBox(), this.level, CollisionContext.empty(), new RewindableStream<>(Stream.empty()));
                pX = vec3.x;
                p_107247_ = vec3.y;
                pY = vec3.z;
            }

            if (pX != 0.0D || p_107247_ != 0.0D || pY != 0.0D)
            {
                this.setBoundingBox(this.getBoundingBox().move(pX, p_107247_, pY));
                this.setLocationFromBoundingbox();
            }

            if (Math.abs(d1) >= (double)1.0E-5F && Math.abs(p_107247_) < (double)1.0E-5F)
            {
                this.stoppedByCollision = true;
            }

            this.onGround = d1 != p_107247_ && d1 < 0.0D;

            if (d0 != pX)
            {
                this.xd = 0.0D;
            }

            if (d2 != pY)
            {
                this.zd = 0.0D;
            }
        }
    }

    protected void setLocationFromBoundingbox()
    {
        AABB aabb = this.getBoundingBox();
        this.x = (aabb.minX + aabb.maxX) / 2.0D;
        this.y = aabb.minY;
        this.z = (aabb.minZ + aabb.maxZ) / 2.0D;
    }

    protected int getLightColor(float pPartialTick)
    {
        BlockPos blockpos = new BlockPos(this.x, this.y, this.z);
        return this.level.hasChunkAt(blockpos) ? LevelRenderer.getLightColor(this.level, blockpos) : 0;
    }

    public boolean isAlive()
    {
        return !this.removed;
    }

    public AABB getBoundingBox()
    {
        return this.bb;
    }

    public void setBoundingBox(AABB pBb)
    {
        this.bb = pBb;
    }

    public Optional<ParticleGroup> getParticleGroup()
    {
        return Optional.empty();
    }

    private boolean hasNearBlocks(double dx, double dy, double dz)
    {
        if (!(this.bbWidth > 1.0F) && !(this.bbHeight > 1.0F))
        {
            int i = Mth.floor(this.x);
            int j = Mth.floor(this.y);
            int k = Mth.floor(this.z);
            this.blockPosM.setXyz(i, j, k);
            BlockState blockstate = this.level.getBlockState(this.blockPosM);

            if (!blockstate.isAir())
            {
                return true;
            }
            else
            {
                double d0 = dx > 0.0D ? this.bb.maxX : (dx < 0.0D ? this.bb.minX : this.x);
                double d1 = dy > 0.0D ? this.bb.maxY : (dy < 0.0D ? this.bb.minY : this.y);
                double d2 = dz > 0.0D ? this.bb.maxZ : (dz < 0.0D ? this.bb.minZ : this.z);
                int l = Mth.floor(d0 + dx);
                int i1 = Mth.floor(d1 + dy);
                int j1 = Mth.floor(d2 + dz);

                if (l != i || i1 != j || j1 != k)
                {
                    this.blockPosM.setXyz(l, i1, j1);
                    BlockState blockstate1 = this.level.getBlockState(this.blockPosM);

                    if (!blockstate1.isAir())
                    {
                        return true;
                    }
                }

                return false;
            }
        }
        else
        {
            return true;
        }
    }

    public boolean shouldCull()
    {
        return true;
    }
}
