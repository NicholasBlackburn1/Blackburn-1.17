package net.minecraft.world.entity.ai.control;

import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class LookControl implements Control
{
    protected final Mob mob;
    protected float yMaxRotSpeed;
    protected float xMaxRotAngle;
    protected boolean hasWanted;
    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;

    public LookControl(Mob p_24945_)
    {
        this.mob = p_24945_;
    }

    public void setLookAt(Vec3 pX)
    {
        this.setLookAt(pX.x, pX.y, pX.z);
    }

    public void setLookAt(Entity pX)
    {
        this.setLookAt(pX.getX(), getWantedY(pX), pX.getZ());
    }

    public void setLookAt(Entity pX, float p_24962_, float pY)
    {
        this.setLookAt(pX.getX(), getWantedY(pX), pX.getZ(), p_24962_, pY);
    }

    public void setLookAt(double pX, double p_24948_, double pY)
    {
        this.setLookAt(pX, p_24948_, pY, (float)this.mob.getHeadRotSpeed(), (float)this.mob.getMaxHeadXRot());
    }

    public void setLookAt(double pX, double p_24952_, double pY, float p_24954_, float pZ)
    {
        this.wantedX = pX;
        this.wantedY = p_24952_;
        this.wantedZ = pY;
        this.yMaxRotSpeed = p_24954_;
        this.xMaxRotAngle = pZ;
        this.hasWanted = true;
    }

    public void tick()
    {
        if (this.resetXRotOnTick())
        {
            this.mob.setXRot(0.0F);
        }

        if (this.hasWanted)
        {
            this.hasWanted = false;
            this.getYRotD().ifPresent((p_181130_) ->
            {
                this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, p_181130_, this.yMaxRotSpeed);
            });
            this.getXRotD().ifPresent((p_181128_) ->
            {
                this.mob.setXRot(this.rotateTowards(this.mob.getXRot(), p_181128_, this.xMaxRotAngle));
            });
        }
        else
        {
            this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, this.mob.yBodyRot, 10.0F);
        }

        this.clampHeadRotationToBody();
    }

    protected void clampHeadRotationToBody()
    {
        if (!this.mob.getNavigation().isDone())
        {
            this.mob.yHeadRot = Mth.rotateIfNecessary(this.mob.yHeadRot, this.mob.yBodyRot, (float)this.mob.getMaxHeadYRot());
        }
    }

    protected boolean resetXRotOnTick()
    {
        return true;
    }

    public boolean isHasWanted()
    {
        return this.hasWanted;
    }

    public double getWantedX()
    {
        return this.wantedX;
    }

    public double getWantedY()
    {
        return this.wantedY;
    }

    public double getWantedZ()
    {
        return this.wantedZ;
    }

    protected Optional<Float> getXRotD()
    {
        double d0 = this.wantedX - this.mob.getX();
        double d1 = this.wantedY - this.mob.getEyeY();
        double d2 = this.wantedZ - this.mob.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        return !(Math.abs(d1) > (double)1.0E-5F) && !(Math.abs(d3) > (double)1.0E-5F) ? Optional.empty() : Optional.of((float)(-(Mth.atan2(d1, d3) * (double)(180F / (float)Math.PI))));
    }

    protected Optional<Float> getYRotD()
    {
        double d0 = this.wantedX - this.mob.getX();
        double d1 = this.wantedZ - this.mob.getZ();
        return !(Math.abs(d1) > (double)1.0E-5F) && !(Math.abs(d0) > (double)1.0E-5F) ? Optional.empty() : Optional.of((float)(Mth.atan2(d1, d0) * (double)(180F / (float)Math.PI)) - 90.0F);
    }

    protected float rotateTowards(float pFrom, float pTo, float pMaxDelta)
    {
        float f = Mth.degreesDifference(pFrom, pTo);
        float f1 = Mth.clamp(f, -pMaxDelta, pMaxDelta);
        return pFrom + f1;
    }

    private static double getWantedY(Entity pEntity)
    {
        return pEntity instanceof LivingEntity ? pEntity.getEyeY() : (pEntity.getBoundingBox().minY + pEntity.getBoundingBox().maxY) / 2.0D;
    }
}
