package com.mojang.math;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class Vector3f
{
    public static final Codec<Vector3f> CODEC = Codec.FLOAT.listOf().comapFlatMap((p_176767_) ->
    {
        return Util.fixedSize(p_176767_, 3).map((p_176774_) -> {
            return new Vector3f(p_176774_.get(0), p_176774_.get(1), p_176774_.get(2));
        });
    }, (p_176776_) ->
    {
        return ImmutableList.of(p_176776_.x, p_176776_.y, p_176776_.z);
    });
    public static Vector3f XN = new Vector3f(-1.0F, 0.0F, 0.0F);
    public static Vector3f XP = new Vector3f(1.0F, 0.0F, 0.0F);
    public static Vector3f YN = new Vector3f(0.0F, -1.0F, 0.0F);
    public static Vector3f YP = new Vector3f(0.0F, 1.0F, 0.0F);
    public static Vector3f ZN = new Vector3f(0.0F, 0.0F, -1.0F);
    public static Vector3f ZP = new Vector3f(0.0F, 0.0F, 1.0F);
    public static Vector3f ZERO = new Vector3f(0.0F, 0.0F, 0.0F);
    private float x;
    private float y;
    private float z;

    public Vector3f()
    {
    }

    public Vector3f(float p_122234_, float p_122235_, float p_122236_)
    {
        this.x = p_122234_;
        this.y = p_122235_;
        this.z = p_122236_;
    }

    public Vector3f(Vector4f p_176765_)
    {
        this(p_176765_.x(), p_176765_.y(), p_176765_.z());
    }

    public Vector3f(Vec3 p_122238_)
    {
        this((float)p_122238_.x, (float)p_122238_.y, (float)p_122238_.z);
    }

    public boolean equals(Object p_122283_)
    {
        if (this == p_122283_)
        {
            return true;
        }
        else if (p_122283_ != null && this.getClass() == p_122283_.getClass())
        {
            Vector3f vector3f = (Vector3f)p_122283_;

            if (Float.compare(vector3f.x, this.x) != 0)
            {
                return false;
            }
            else if (Float.compare(vector3f.y, this.y) != 0)
            {
                return false;
            }
            else
            {
                return Float.compare(vector3f.z, this.z) == 0;
            }
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        int i = Float.floatToIntBits(this.x);
        i = 31 * i + Float.floatToIntBits(this.y);
        return 31 * i + Float.floatToIntBits(this.z);
    }

    public float x()
    {
        return this.x;
    }

    public float y()
    {
        return this.y;
    }

    public float z()
    {
        return this.z;
    }

    public void mul(float pMultiplier)
    {
        this.x *= pMultiplier;
        this.y *= pMultiplier;
        this.z *= pMultiplier;
    }

    public void mul(float pMultiplier, float p_122265_, float p_122266_)
    {
        this.x *= pMultiplier;
        this.y *= p_122265_;
        this.z *= p_122266_;
    }

    public void clamp(Vector3f pMin, Vector3f pMax)
    {
        this.x = Mth.clamp(this.x, pMin.x(), pMax.x());
        this.y = Mth.clamp(this.y, pMin.x(), pMax.y());
        this.z = Mth.clamp(this.z, pMin.z(), pMax.z());
    }

    public void clamp(float pMin, float pMax)
    {
        this.x = Mth.clamp(this.x, pMin, pMax);
        this.y = Mth.clamp(this.y, pMin, pMax);
        this.z = Mth.clamp(this.z, pMin, pMax);
    }

    public void set(float pX, float pY, float pZ)
    {
        this.x = pX;
        this.y = pY;
        this.z = pZ;
    }

    public void load(Vector3f p_176769_)
    {
        this.x = p_176769_.x;
        this.y = p_176769_.y;
        this.z = p_176769_.z;
    }

    public void add(float pX, float pY, float pZ)
    {
        this.x += pX;
        this.y += pY;
        this.z += pZ;
    }

    public void add(Vector3f pX)
    {
        this.x += pX.x;
        this.y += pX.y;
        this.z += pX.z;
    }

    public void sub(Vector3f pVec)
    {
        this.x -= pVec.x;
        this.y -= pVec.y;
        this.z -= pVec.z;
    }

    public float dot(Vector3f pVec)
    {
        return this.x * pVec.x + this.y * pVec.y + this.z * pVec.z;
    }

    public boolean normalize()
    {
        float f = this.x * this.x + this.y * this.y + this.z * this.z;

        if ((double)f < 1.0E-5D)
        {
            return false;
        }
        else
        {
            float f1 = Mth.fastInvSqrt(f);
            this.x *= f1;
            this.y *= f1;
            this.z *= f1;
            return true;
        }
    }

    public void cross(Vector3f pVec)
    {
        float f = this.x;
        float f1 = this.y;
        float f2 = this.z;
        float f3 = pVec.x();
        float f4 = pVec.y();
        float f5 = pVec.z();
        this.x = f1 * f5 - f2 * f4;
        this.y = f2 * f3 - f * f5;
        this.z = f * f4 - f1 * f3;
    }

    public void transform(Matrix3f pMatrix)
    {
        float f = this.x;
        float f1 = this.y;
        float f2 = this.z;
        this.x = pMatrix.m00 * f + pMatrix.m01 * f1 + pMatrix.m02 * f2;
        this.y = pMatrix.m10 * f + pMatrix.m11 * f1 + pMatrix.m12 * f2;
        this.z = pMatrix.m20 * f + pMatrix.m21 * f1 + pMatrix.m22 * f2;
    }

    public void transform(Quaternion pMatrix)
    {
        Quaternion quaternion = new Quaternion(pMatrix);
        quaternion.mul(new Quaternion(this.x(), this.y(), this.z(), 0.0F));
        Quaternion quaternion1 = new Quaternion(pMatrix);
        quaternion1.conj();
        quaternion.mul(quaternion1);
        this.set(quaternion.i(), quaternion.j(), quaternion.k());
    }

    public void lerp(Vector3f pVector, float pPct)
    {
        float f = 1.0F - pPct;
        this.x = this.x * f + pVector.x * pPct;
        this.y = this.y * f + pVector.y * pPct;
        this.z = this.z * f + pVector.z * pPct;
    }

    public Quaternion rotation(float pValue)
    {
        return new Quaternion(this, pValue, false);
    }

    public Quaternion rotationDegrees(float pValue)
    {
        return new Quaternion(this, pValue, true);
    }

    public Vector3f copy()
    {
        return new Vector3f(this.x, this.y, this.z);
    }

    public void map(Float2FloatFunction pFunction)
    {
        this.x = pFunction.get(this.x);
        this.y = pFunction.get(this.y);
        this.z = pFunction.get(this.z);
    }

    public String toString()
    {
        return "[" + this.x + ", " + this.y + ", " + this.z + "]";
    }
}
