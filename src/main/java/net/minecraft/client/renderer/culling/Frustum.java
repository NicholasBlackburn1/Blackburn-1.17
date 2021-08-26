package net.minecraft.client.renderer.culling;

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import net.minecraft.world.phys.AABB;
import net.optifine.render.ICamera;

public class Frustum implements ICamera
{
    private final Vector4f[] frustumData = new Vector4f[6];
    private double camX;
    private double camY;
    private double camZ;
    public boolean disabled = false;

    public Frustum(Matrix4f p_113000_, Matrix4f p_113001_)
    {
        this.calculateFrustum(p_113000_, p_113001_);
    }

    public void prepare(double pCamX, double p_113004_, double pCamY)
    {
        this.camX = pCamX;
        this.camY = p_113004_;
        this.camZ = pCamY;
    }

    private void calculateFrustum(Matrix4f pProjection, Matrix4f pFrustrumMatrix)
    {
        Matrix4f matrix4f = pFrustrumMatrix.copy();
        matrix4f.multiply(pProjection);
        matrix4f.transpose();
        this.getPlane(matrix4f, -1, 0, 0, 0);
        this.getPlane(matrix4f, 1, 0, 0, 1);
        this.getPlane(matrix4f, 0, -1, 0, 2);
        this.getPlane(matrix4f, 0, 1, 0, 3);
        this.getPlane(matrix4f, 0, 0, -1, 4);
        this.getPlane(matrix4f, 0, 0, 1, 5);
    }

    private void getPlane(Matrix4f pFrustrumMatrix, int pX, int pY, int pZ, int pId)
    {
        Vector4f vector4f = new Vector4f((float)pX, (float)pY, (float)pZ, 1.0F);
        vector4f.transform(pFrustrumMatrix);
        vector4f.normalize();
        this.frustumData[pId] = vector4f;
    }

    public boolean isVisible(AABB pAabb)
    {
        return this.cubeInFrustum(pAabb.minX, pAabb.minY, pAabb.minZ, pAabb.maxX, pAabb.maxY, pAabb.maxZ);
    }

    private boolean cubeInFrustum(double pMinX, double p_113008_, double pMinY, double p_113010_, double pMinZ, double p_113012_)
    {
        if (this.disabled)
        {
            return true;
        }
        else
        {
            float f = (float)(pMinX - this.camX);
            float f1 = (float)(p_113008_ - this.camY);
            float f2 = (float)(pMinY - this.camZ);
            float f3 = (float)(p_113010_ - this.camX);
            float f4 = (float)(pMinZ - this.camY);
            float f5 = (float)(p_113012_ - this.camZ);
            return this.cubeInFrustum(f, f1, f2, f3, f4, f5);
        }
    }

    private boolean cubeInFrustum(float pMinX, float p_113015_, float pMinY, float p_113017_, float pMinZ, float p_113019_)
    {
        for (int i = 0; i < 6; ++i)
        {
            Vector4f vector4f = this.frustumData[i];
            float f = vector4f.x();
            float f1 = vector4f.y();
            float f2 = vector4f.z();
            float f3 = vector4f.w();

            if (f * pMinX + f1 * p_113015_ + f2 * pMinY + f3 <= 0.0F && f * p_113017_ + f1 * p_113015_ + f2 * pMinY + f3 <= 0.0F && f * pMinX + f1 * pMinZ + f2 * pMinY + f3 <= 0.0F && f * p_113017_ + f1 * pMinZ + f2 * pMinY + f3 <= 0.0F && f * pMinX + f1 * p_113015_ + f2 * p_113019_ + f3 <= 0.0F && f * p_113017_ + f1 * p_113015_ + f2 * p_113019_ + f3 <= 0.0F && f * pMinX + f1 * pMinZ + f2 * p_113019_ + f3 <= 0.0F && f * p_113017_ + f1 * pMinZ + f2 * p_113019_ + f3 <= 0.0F)
            {
                return false;
            }
        }

        return true;
    }

    public boolean isBoxInFrustumFully(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
    {
        if (this.disabled)
        {
            return true;
        }
        else
        {
            float f = (float)minX;
            float f1 = (float)minY;
            float f2 = (float)minZ;
            float f3 = (float)maxX;
            float f4 = (float)maxY;
            float f5 = (float)maxZ;

            for (int i = 0; i < 6; ++i)
            {
                Vector4f vector4f = this.frustumData[i];
                float f6 = vector4f.x();
                float f7 = vector4f.y();
                float f8 = vector4f.z();
                float f9 = vector4f.w();

                if (i < 4)
                {
                    if (f6 * f + f7 * f1 + f8 * f2 + f9 <= 0.0F || f6 * f3 + f7 * f1 + f8 * f2 + f9 <= 0.0F || f6 * f + f7 * f4 + f8 * f2 + f9 <= 0.0F || f6 * f3 + f7 * f4 + f8 * f2 + f9 <= 0.0F || f6 * f + f7 * f1 + f8 * f5 + f9 <= 0.0F || f6 * f3 + f7 * f1 + f8 * f5 + f9 <= 0.0F || f6 * f + f7 * f4 + f8 * f5 + f9 <= 0.0F || f6 * f3 + f7 * f4 + f8 * f5 + f9 <= 0.0F)
                    {
                        return false;
                    }
                }
                else if (f6 * f + f7 * f1 + f8 * f2 + f9 <= 0.0F && f6 * f3 + f7 * f1 + f8 * f2 + f9 <= 0.0F && f6 * f + f7 * f4 + f8 * f2 + f9 <= 0.0F && f6 * f3 + f7 * f4 + f8 * f2 + f9 <= 0.0F && f6 * f + f7 * f1 + f8 * f5 + f9 <= 0.0F && f6 * f3 + f7 * f1 + f8 * f5 + f9 <= 0.0F && f6 * f + f7 * f4 + f8 * f5 + f9 <= 0.0F && f6 * f3 + f7 * f4 + f8 * f5 + f9 <= 0.0F)
                {
                    return false;
                }
            }

            return true;
        }
    }

    public Vector4f[] getFrustum()
    {
        return this.frustumData;
    }

	@Override
	public void setCameraPosition(double var1, double var3, double var5) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isBoundingBoxInFrustum(AABB var1) {
		// TODO Auto-generated method stub
		return false;
	}
}
