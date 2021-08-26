package com.mojang.blaze3d.vertex;

import com.google.common.collect.Queues;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.Util;
import net.minecraft.util.Mth;

public class PoseStack
{
    Deque<PoseStack.Pose> freeEntries = new ArrayDeque<>();
    private final Deque<PoseStack.Pose> poseStack = Util.make(Queues.newArrayDeque(), (dequeIn) ->
    {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setIdentity();
        Matrix3f matrix3f = new Matrix3f();
        matrix3f.setIdentity();
        dequeIn.add(new PoseStack.Pose(matrix4f, matrix3f));
    });

    public void translate(double pX, double p_85839_, double pY)
    {
        PoseStack.Pose posestack$pose = this.poseStack.getLast();
        posestack$pose.pose.multiplyWithTranslation((float)pX, (float)p_85839_, (float)pY);
    }

    public void scale(float pX, float pY, float pZ)
    {
        PoseStack.Pose posestack$pose = this.poseStack.getLast();
        posestack$pose.pose.mulScale(pX, pY, pZ);

        if (pX == pY && pY == pZ)
        {
            if (pX > 0.0F)
            {
                return;
            }

            posestack$pose.normal.mul(-1.0F);
        }

        float f = 1.0F / pX;
        float f1 = 1.0F / pY;
        float f2 = 1.0F / pZ;
        float f3 = Mth.fastInvCubeRoot(f * f1 * f2);
        posestack$pose.normal.mul(Matrix3f.createScaleMatrix(f3 * f, f3 * f1, f3 * f2));
    }

    public void mulPose(Quaternion pQuaternion)
    {
        PoseStack.Pose posestack$pose = this.poseStack.getLast();
        posestack$pose.pose.multiply(pQuaternion);
        posestack$pose.normal.mul(pQuaternion);
    }

    public void pushPose()
    {
        PoseStack.Pose posestack$pose = this.poseStack.getLast();
        PoseStack.Pose posestack$pose1 = this.freeEntries.pollLast();

        if (posestack$pose1 == null)
        {
            posestack$pose1 = new PoseStack.Pose(posestack$pose.pose.copy(), posestack$pose.normal.copy());
        }
        else
        {
            posestack$pose1.pose.set(posestack$pose.pose);
            posestack$pose1.normal.load(posestack$pose.normal);
        }

        this.poseStack.addLast(posestack$pose1);
    }

    public void popPose()
    {
        PoseStack.Pose posestack$pose = this.poseStack.removeLast();

        if (posestack$pose != null)
        {
            this.freeEntries.add(posestack$pose);
        }
    }

    public PoseStack.Pose last()
    {
        return this.poseStack.getLast();
    }

    public boolean clear()
    {
        return this.poseStack.size() == 1;
    }

    public void rotateDegXp(float angle)
    {
        this.mulPose(Vector3f.XP.rotationDegrees(angle));
    }

    public void rotateDegXn(float angle)
    {
        this.mulPose(Vector3f.XN.rotationDegrees(angle));
    }

    public void rotateDegYp(float angle)
    {
        this.mulPose(Vector3f.YP.rotationDegrees(angle));
    }

    public void rotateDegYn(float angle)
    {
        this.mulPose(Vector3f.YN.rotationDegrees(angle));
    }

    public void rotateDegZp(float angle)
    {
        this.mulPose(Vector3f.ZP.rotationDegrees(angle));
    }

    public void rotateDegZn(float angle)
    {
        this.mulPose(Vector3f.ZN.rotationDegrees(angle));
    }

    public void rotateDeg(float angle, float x, float y, float z)
    {
        Vector3f vector3f = new Vector3f(x, y, z);
        Quaternion quaternion = vector3f.rotationDegrees(angle);
        this.mulPose(quaternion);
    }

    public String toString()
    {
        return this.last().toString() + "Depth: " + this.poseStack.size();
    }

    public void setIdentity()
    {
        PoseStack.Pose posestack$pose = this.poseStack.getLast();
        posestack$pose.pose.setIdentity();
        posestack$pose.normal.setIdentity();
    }

    public void mulPoseMatrix(Matrix4f p_166855_)
    {
        (this.poseStack.getLast()).pose.multiply(p_166855_);
    }

    public static final class Pose
    {
        final Matrix4f pose;
        final Matrix3f normal;

        Pose(Matrix4f p_85855_, Matrix3f p_85856_)
        {
            this.pose = p_85855_;
            this.normal = p_85856_;
        }

        public Matrix4f pose()
        {
            return this.pose;
        }

        public Matrix3f normal()
        {
            return this.normal;
        }

        public String toString()
        {
            return this.pose.toString() + this.normal.toString();
        }
    }
}
