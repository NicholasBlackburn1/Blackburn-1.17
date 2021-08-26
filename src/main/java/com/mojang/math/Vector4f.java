package com.mojang.math;

import net.minecraft.util.Mth;

public class Vector4f
{
    private float x;
    private float y;
    private float z;
    private float w;

    public Vector4f()
    {
    }

    public Vector4f(float p_123595_, float p_123596_, float p_123597_, float p_123598_)
    {
        this.x = p_123595_;
        this.y = p_123596_;
        this.z = p_123597_;
        this.w = p_123598_;
    }

    public Vector4f(Vector3f p_123600_)
    {
        this(p_123600_.x(), p_123600_.y(), p_123600_.z(), 1.0F);
    }

    public boolean equals(Object p_123620_)
    {
        if (this == p_123620_)
        {
            return true;
        }
        else if (p_123620_ != null && this.getClass() == p_123620_.getClass())
        {
            Vector4f vector4f = (Vector4f)p_123620_;

            if (Float.compare(vector4f.x, this.x) != 0)
            {
                return false;
            }
            else if (Float.compare(vector4f.y, this.y) != 0)
            {
                return false;
            }
            else if (Float.compare(vector4f.z, this.z) != 0)
            {
                return false;
            }
            else
            {
                return Float.compare(vector4f.w, this.w) == 0;
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
        i = 31 * i + Float.floatToIntBits(this.z);
        return 31 * i + Float.floatToIntBits(this.w);
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

    public float w()
    {
        return this.w;
    }

    public void mul(float pVec)
    {
        this.x *= pVec;
        this.y *= pVec;
        this.z *= pVec;
        this.w *= pVec;
    }

    public void mul(Vector3f pVec)
    {
        this.x *= pVec.x();
        this.y *= pVec.y();
        this.z *= pVec.z();
    }

    public void set(float pX, float pY, float pZ, float pW)
    {
        this.x = pX;
        this.y = pY;
        this.z = pZ;
        this.w = pW;
    }

    public void add(float p_176876_, float p_176877_, float p_176878_, float p_176879_)
    {
        this.x += p_176876_;
        this.y += p_176877_;
        this.z += p_176878_;
        this.w += p_176879_;
    }

    public float dot(Vector4f pVector)
    {
        return this.x * pVector.x + this.y * pVector.y + this.z * pVector.z + this.w * pVector.w;
    }

    public boolean normalize()
    {
        float f = this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;

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
            this.w *= f1;
            return true;
        }
    }

    public void transform(Matrix4f pMatrix)
    {
        float f = this.x;
        float f1 = this.y;
        float f2 = this.z;
        float f3 = this.w;
        this.x = pMatrix.m00 * f + pMatrix.m01 * f1 + pMatrix.m02 * f2 + pMatrix.m03 * f3;
        this.y = pMatrix.m10 * f + pMatrix.m11 * f1 + pMatrix.m12 * f2 + pMatrix.m13 * f3;
        this.z = pMatrix.m20 * f + pMatrix.m21 * f1 + pMatrix.m22 * f2 + pMatrix.m23 * f3;
        this.w = pMatrix.m30 * f + pMatrix.m31 * f1 + pMatrix.m32 * f2 + pMatrix.m33 * f3;
    }

    public void transform(Quaternion pMatrix)
    {
        Quaternion quaternion = new Quaternion(pMatrix);
        quaternion.mul(new Quaternion(this.x(), this.y(), this.z(), 0.0F));
        Quaternion quaternion1 = new Quaternion(pMatrix);
        quaternion1.conj();
        quaternion.mul(quaternion1);
        this.set(quaternion.i(), quaternion.j(), quaternion.k(), this.w());
    }

    public void perspectiveDivide()
    {
        this.x /= this.w;
        this.y /= this.w;
        this.z /= this.w;
        this.w = 1.0F;
    }

    public void lerp(Vector4f p_176873_, float p_176874_)
    {
        float f = 1.0F - p_176874_;
        this.x = this.x * f + p_176873_.x * p_176874_;
        this.y = this.y * f + p_176873_.y * p_176874_;
        this.z = this.z * f + p_176873_.z * p_176874_;
        this.w = this.w * f + p_176873_.w * p_176874_;
    }

    public String toString()
    {
        return "[" + this.x + ", " + this.y + ", " + this.z + ", " + this.w + "]";
    }
}
