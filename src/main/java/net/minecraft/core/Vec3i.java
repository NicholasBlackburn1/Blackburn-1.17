package net.minecraft.core;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import java.util.stream.IntStream;
import javax.annotation.concurrent.Immutable;
import net.minecraft.Util;
import net.minecraft.util.Mth;

@Immutable
public class Vec3i implements Comparable<Vec3i>
{
    public static final Codec<Vec3i> CODEC = Codec.INT_STREAM.comapFlatMap((p_123318_) ->
    {
        return Util.fixedSize(p_123318_, 3).map((p_175586_) -> {
            return new Vec3i(p_175586_[0], p_175586_[1], p_175586_[2]);
        });
    }, (p_123313_) ->
    {
        return IntStream.of(p_123313_.getX(), p_123313_.getY(), p_123313_.getZ());
    });
    public static final Vec3i ZERO = new Vec3i(0, 0, 0);
    private int x;
    private int y;
    private int z;

    public Vec3i(int p_123296_, int p_123297_, int p_123298_)
    {
        this.x = p_123296_;
        this.y = p_123297_;
        this.z = p_123298_;
    }

    public Vec3i(double p_123292_, double p_123293_, double p_123294_)
    {
        this(Mth.floor(p_123292_), Mth.floor(p_123293_), Mth.floor(p_123294_));
    }

    public boolean equals(Object p_123327_)
    {
        if (this == p_123327_)
        {
            return true;
        }
        else if (!(p_123327_ instanceof Vec3i))
        {
            return false;
        }
        else
        {
            Vec3i vec3i = (Vec3i)p_123327_;

            if (this.getX() != vec3i.getX())
            {
                return false;
            }
            else if (this.getY() != vec3i.getY())
            {
                return false;
            }
            else
            {
                return this.getZ() == vec3i.getZ();
            }
        }
    }

    public int hashCode()
    {
        return (this.getY() + this.getZ() * 31) * 31 + this.getX();
    }

    public int compareTo(Vec3i p_123330_)
    {
        if (this.getY() == p_123330_.getY())
        {
            return this.getZ() == p_123330_.getZ() ? this.getX() - p_123330_.getX() : this.getZ() - p_123330_.getZ();
        }
        else
        {
            return this.getY() - p_123330_.getY();
        }
    }

    public int getX()
    {
        return this.x;
    }

    public int getY()
    {
        return this.y;
    }

    public int getZ()
    {
        return this.z;
    }

    protected Vec3i setX(int p_175605_)
    {
        this.x = p_175605_;
        return this;
    }

    protected Vec3i setY(int p_175604_)
    {
        this.y = p_175604_;
        return this;
    }

    protected Vec3i setZ(int p_175603_)
    {
        this.z = p_175603_;
        return this;
    }

    public Vec3i offset(double p_175587_, double p_175588_, double p_175589_)
    {
        return p_175587_ == 0.0D && p_175588_ == 0.0D && p_175589_ == 0.0D ? this : new Vec3i((double)this.getX() + p_175587_, (double)this.getY() + p_175588_, (double)this.getZ() + p_175589_);
    }

    public Vec3i offset(int p_175593_, int p_175594_, int p_175595_)
    {
        return p_175593_ == 0 && p_175594_ == 0 && p_175595_ == 0 ? this : new Vec3i(this.getX() + p_175593_, this.getY() + p_175594_, this.getZ() + p_175595_);
    }

    public Vec3i offset(Vec3i p_175597_)
    {
        return this.offset(p_175597_.getX(), p_175597_.getY(), p_175597_.getZ());
    }

    public Vec3i subtract(Vec3i p_175596_)
    {
        return this.offset(-p_175596_.getX(), -p_175596_.getY(), -p_175596_.getZ());
    }

    public Vec3i multiply(int p_175602_)
    {
        if (p_175602_ == 1)
        {
            return this;
        }
        else
        {
            return p_175602_ == 0 ? ZERO : new Vec3i(this.getX() * p_175602_, this.getY() * p_175602_, this.getZ() * p_175602_);
        }
    }

    public Vec3i above()
    {
        return this.above(1);
    }

    public Vec3i above(int p_123336_)
    {
        return this.relative(Direction.UP, p_123336_);
    }

    public Vec3i below()
    {
        return this.below(1);
    }

    public Vec3i below(int p_123335_)
    {
        return this.relative(Direction.DOWN, p_123335_);
    }

    public Vec3i north()
    {
        return this.north(1);
    }

    public Vec3i north(int p_175601_)
    {
        return this.relative(Direction.NORTH, p_175601_);
    }

    public Vec3i south()
    {
        return this.south(1);
    }

    public Vec3i south(int p_175600_)
    {
        return this.relative(Direction.SOUTH, p_175600_);
    }

    public Vec3i west()
    {
        return this.west(1);
    }

    public Vec3i west(int p_175599_)
    {
        return this.relative(Direction.WEST, p_175599_);
    }

    public Vec3i east()
    {
        return this.east(1);
    }

    public Vec3i east(int p_175598_)
    {
        return this.relative(Direction.EAST, p_175598_);
    }

    public Vec3i relative(Direction pFacing)
    {
        return this.relative(pFacing, 1);
    }

    public Vec3i relative(Direction pFacing, int pN)
    {
        return pN == 0 ? this : new Vec3i(this.getX() + pFacing.getStepX() * pN, this.getY() + pFacing.getStepY() * pN, this.getZ() + pFacing.getStepZ() * pN);
    }

    public Vec3i relative(Direction.Axis pFacing, int pN)
    {
        if (pN == 0)
        {
            return this;
        }
        else
        {
            int i = pFacing == Direction.Axis.X ? pN : 0;
            int j = pFacing == Direction.Axis.Y ? pN : 0;
            int k = pFacing == Direction.Axis.Z ? pN : 0;
            return new Vec3i(this.getX() + i, this.getY() + j, this.getZ() + k);
        }
    }

    public Vec3i cross(Vec3i pVec)
    {
        return new Vec3i(this.getY() * pVec.getZ() - this.getZ() * pVec.getY(), this.getZ() * pVec.getX() - this.getX() * pVec.getZ(), this.getX() * pVec.getY() - this.getY() * pVec.getX());
    }

    public boolean closerThan(Vec3i pPosition, double pDistance)
    {
        return this.distSqr((double)pPosition.getX(), (double)pPosition.getY(), (double)pPosition.getZ(), false) < pDistance * pDistance;
    }

    public boolean closerThan(Position pPosition, double pDistance)
    {
        return this.distSqr(pPosition.x(), pPosition.y(), pPosition.z(), true) < pDistance * pDistance;
    }

    public double distSqr(Vec3i pX)
    {
        return this.distSqr((double)pX.getX(), (double)pX.getY(), (double)pX.getZ(), true);
    }

    public double distSqr(Position pX, boolean p_123311_)
    {
        return this.distSqr(pX.x(), pX.y(), pX.z(), p_123311_);
    }

    public double distSqr(Vec3i pX, boolean p_175584_)
    {
        return this.distSqr((double)pX.x, (double)pX.y, (double)pX.z, p_175584_);
    }

    public double distSqr(double pX, double p_123301_, double pY, boolean p_123303_)
    {
        double d0 = p_123303_ ? 0.5D : 0.0D;
        double d1 = (double)this.getX() + d0 - pX;
        double d2 = (double)this.getY() + d0 - p_123301_;
        double d3 = (double)this.getZ() + d0 - pY;
        return d1 * d1 + d2 * d2 + d3 * d3;
    }

    public int distManhattan(Vec3i pVector)
    {
        float f = (float)Math.abs(pVector.getX() - this.getX());
        float f1 = (float)Math.abs(pVector.getY() - this.getY());
        float f2 = (float)Math.abs(pVector.getZ() - this.getZ());
        return (int)(f + f1 + f2);
    }

    public int get(Direction.Axis p_123305_)
    {
        return p_123305_.choose(this.x, this.y, this.z);
    }

    public String toString()
    {
        return MoreObjects.toStringHelper(this).add("x", this.getX()).add("y", this.getY()).add("z", this.getZ()).toString();
    }

    public String toShortString()
    {
        return this.getX() + ", " + this.getY() + ", " + this.getZ();
    }
}
