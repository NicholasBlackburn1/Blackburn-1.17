package net.optifine.render;

import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import java.util.Random;

public class TestMath
{
    static Random random = new Random();

    public static void main(String[] args)
    {
        int i = 1000000;
        dbg("Test math: " + i);

        for (int j = 0; j < 1000000; ++j)
        {
            testMatrix4f_mulTranslate();
            testMatrix4f_mulScale();
            testMatrix4f_mulQuaternion();
            testMatrix3f_mulQuaternion();
            testVector4f_transform();
            testVector3f_transform();
        }

        dbg("Done");
    }

    private static void testMatrix4f_mulTranslate()
    {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setRandom(random);
        Matrix4f matrix4f1 = matrix4f.copy();
        float f = random.nextFloat();
        float f1 = random.nextFloat();
        float f2 = random.nextFloat();
        matrix4f.multiply(Matrix4f.createTranslateMatrix(f, f1, f2));
        matrix4f1.multiplyWithTranslation(f, f1, f2);

        if (!matrix4f1.equals(matrix4f))
        {
            dbg("*** DIFFERENT ***");
            dbg(matrix4f.toString());
            dbg(matrix4f1.toString());
        }
    }

    private static void testMatrix4f_mulScale()
    {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setRandom(random);
        Matrix4f matrix4f1 = matrix4f.copy();
        float f = random.nextFloat();
        float f1 = random.nextFloat();
        float f2 = random.nextFloat();
        matrix4f.multiply(Matrix4f.createScaleMatrix(f, f1, f2));
        matrix4f1.mulScale(f, f1, f2);

        if (!matrix4f1.equals(matrix4f))
        {
            dbg("*** DIFFERENT ***");
            dbg(matrix4f.toString());
            dbg(matrix4f1.toString());
        }
    }

    private static void testMatrix4f_mulQuaternion()
    {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setRandom(random);
        Matrix4f matrix4f1 = matrix4f.copy();
        Quaternion quaternion = new Quaternion(random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat());
        matrix4f.multiply(new Matrix4f(quaternion));
        matrix4f1.multiply(quaternion);

        if (!matrix4f1.equals(matrix4f))
        {
            dbg("*** DIFFERENT ***");
            dbg(matrix4f.toString());
            dbg(matrix4f1.toString());
        }
    }

    private static void testMatrix3f_mulQuaternion()
    {
        Matrix3f matrix3f = new Matrix3f();
        matrix3f.setRandom(random);
        Matrix3f matrix3f1 = matrix3f.copy();
        Quaternion quaternion = new Quaternion(random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat());
        matrix3f.mul(new Matrix3f(quaternion));
        matrix3f1.mul(quaternion);

        if (!matrix3f1.equals(matrix3f))
        {
            dbg("*** DIFFERENT ***");
            dbg(matrix3f.toString());
            dbg(matrix3f1.toString());
        }
    }

    private static void testVector3f_transform()
    {
        Vector3f vector3f = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
        Vector3f vector3f1 = vector3f.copy();
        Matrix3f matrix3f = new Matrix3f();
        matrix3f.setRandom(random);
        vector3f.transform(matrix3f);
        float f = matrix3f.getTransformX(vector3f1.x(), vector3f1.y(), vector3f1.z());
        float f1 = matrix3f.getTransformY(vector3f1.x(), vector3f1.y(), vector3f1.z());
        float f2 = matrix3f.getTransformZ(vector3f1.x(), vector3f1.y(), vector3f1.z());
        vector3f1 = new Vector3f(f, f1, f2);

        if (!vector3f1.equals(vector3f))
        {
            dbg("*** DIFFERENT ***");
            dbg(vector3f.toString());
            dbg(vector3f1.toString());
        }
    }

    private static void testVector4f_transform()
    {
        Vector4f vector4f = new Vector4f(random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat());
        Vector4f vector4f1 = new Vector4f(vector4f.x(), vector4f.y(), vector4f.z(), vector4f.w());
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setRandom(random);
        vector4f.transform(matrix4f);
        float f = matrix4f.getTransformX(vector4f1.x(), vector4f1.y(), vector4f1.z(), vector4f1.w());
        float f1 = matrix4f.getTransformY(vector4f1.x(), vector4f1.y(), vector4f1.z(), vector4f1.w());
        float f2 = matrix4f.getTransformZ(vector4f1.x(), vector4f1.y(), vector4f1.z(), vector4f1.w());
        float f3 = matrix4f.getTransformW(vector4f1.x(), vector4f1.y(), vector4f1.z(), vector4f1.w());
        vector4f1 = new Vector4f(f, f1, f2, f3);

        if (!vector4f1.equals(vector4f))
        {
            dbg("*** DIFFERENT ***");
            dbg(vector4f.toString());
            dbg(vector4f1.toString());
        }
    }

    private static void dbg(String str)
    {
        System.out.println(str);
    }
}
