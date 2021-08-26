package net.minecraft.util;

import java.util.Random;
import java.util.UUID;
import java.util.function.IntPredicate;
import net.minecraft.Util;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.optifine.util.MathUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class Mth
{
    private static final int BIG_ENOUGH_INT = 1024;
    private static final float BIG_ENOUGH_FLOAT = 1024.0F;
    private static final long UUID_VERSION = 61440L;
    private static final long UUID_VERSION_TYPE_4 = 16384L;
    private static final long UUID_VARIANT = -4611686018427387904L;
    private static final long UUID_VARIANT_2 = Long.MIN_VALUE;
    public static final float PI = (float)Math.PI;
    public static final float HALF_PI = ((float)Math.PI / 2F);
    public static final float TWO_PI = ((float)Math.PI * 2F);
    public static final float DEG_TO_RAD = ((float)Math.PI / 180F);
    public static final float RAD_TO_DEG = (180F / (float)Math.PI);
    public static final float EPSILON = 1.0E-5F;
    public static final float SQRT_OF_TWO = sqrt(2.0F);
    private static final float SIN_SCALE = 10430.378F;
    private static final float[] SIN = Util.make(new float[65536], (p_14076_0_) ->
    {
        for (int i = 0; i < p_14076_0_.length; ++i)
        {
            p_14076_0_[i] = (float)Math.sin((double)i * Math.PI * 2.0D / 65536.0D);
        }
    });
    private static final Random RANDOM = new Random();
    private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = new int[] {0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};
    private static final double ONE_SIXTH = 0.16666666666666666D;
    private static final int FRAC_EXP = 8;
    private static final int LUT_SIZE = 257;
    private static final double FRAC_BIAS = Double.longBitsToDouble(4805340802404319232L);
    private static final double[] ASIN_TAB = new double[257];
    private static final double[] COS_TAB = new double[257];
    private static final int SIN_BITS = 12;
    private static final int SIN_MASK = 4095;
    private static final int SIN_COUNT = 4096;
    private static final int SIN_COUNT_D4 = 1024;
    public static final float PI2 = MathUtils.roundToFloat((Math.PI * 2D));
    public static final float PId2 = MathUtils.roundToFloat((Math.PI / 2D));
    private static final float radToIndex = MathUtils.roundToFloat(651.8986469044033D);
    public static final float deg2Rad = MathUtils.roundToFloat((Math.PI / 180D));
    private static final float[] SIN_TABLE_FAST = new float[4096];
    public static boolean fastMath = false;

    public static float sin(float pValue)
    {
        return fastMath ? SIN_TABLE_FAST[(int)(pValue * radToIndex) & 4095] : SIN[(int)(pValue * 10430.378F) & 65535];
    }

    public static float cos(float pValue)
    {
        return fastMath ? SIN_TABLE_FAST[(int)(pValue * radToIndex + 1024.0F) & 4095] : SIN[(int)(pValue * 10430.378F + 16384.0F) & 65535];
    }

    public static float sqrt(float pValue)
    {
        return (float)Math.sqrt((double)pValue);
    }

    public static int floor(float pValue)
    {
        int i = (int)pValue;
        return pValue < (float)i ? i - 1 : i;
    }

    public static int fastFloor(double pValue)
    {
        return (int)(pValue + 1024.0D) - 1024;
    }

    public static int floor(double pValue)
    {
        int i = (int)pValue;
        return pValue < (double)i ? i - 1 : i;
    }

    public static long lfloor(double pValue)
    {
        long i = (long)pValue;
        return pValue < (double)i ? i - 1L : i;
    }

    public static int absFloor(double p_144940_)
    {
        return (int)(p_144940_ >= 0.0D ? p_144940_ : -p_144940_ + 1.0D);
    }

    public static float abs(float pValue)
    {
        return Math.abs(pValue);
    }

    public static int abs(int pValue)
    {
        return Math.abs(pValue);
    }

    public static int ceil(float pValue)
    {
        int i = (int)pValue;
        return pValue > (float)i ? i + 1 : i;
    }

    public static int ceil(double pValue)
    {
        int i = (int)pValue;
        return pValue > (double)i ? i + 1 : i;
    }

    public static byte clamp(byte pNum, byte p_144849_, byte pMin)
    {
        if (pNum < p_144849_)
        {
            return p_144849_;
        }
        else
        {
            return pNum > pMin ? pMin : pNum;
        }
    }

    public static int clamp(int pNum, int p_14047_, int pMin)
    {
        if (pNum < p_14047_)
        {
            return p_14047_;
        }
        else
        {
            return pNum > pMin ? pMin : pNum;
        }
    }

    public static long clamp(long pNum, long p_14055_, long pMin)
    {
        if (pNum < p_14055_)
        {
            return p_14055_;
        }
        else
        {
            return pNum > pMin ? pMin : pNum;
        }
    }

    public static float clamp(float pNum, float p_14038_, float pMin)
    {
        if (pNum < p_14038_)
        {
            return p_14038_;
        }
        else
        {
            return pNum > pMin ? pMin : pNum;
        }
    }

    public static double clamp(double pNum, double p_14010_, double pMin)
    {
        if (pNum < p_14010_)
        {
            return p_14010_;
        }
        else
        {
            return pNum > pMin ? pMin : pNum;
        }
    }

    public static double clampedLerp(double pLowerBnd, double p_14087_, double pUpperBnd)
    {
        if (pUpperBnd < 0.0D)
        {
            return pLowerBnd;
        }
        else
        {
            return pUpperBnd > 1.0D ? p_14087_ : lerp(pUpperBnd, pLowerBnd, p_14087_);
        }
    }

    public static float clampedLerp(float pLowerBnd, float p_144922_, float pUpperBnd)
    {
        if (pUpperBnd < 0.0F)
        {
            return pLowerBnd;
        }
        else
        {
            return pUpperBnd > 1.0F ? p_144922_ : lerp(pUpperBnd, pLowerBnd, p_144922_);
        }
    }

    public static double absMax(double pX, double p_14007_)
    {
        if (pX < 0.0D)
        {
            pX = -pX;
        }

        if (p_14007_ < 0.0D)
        {
            p_14007_ = -p_14007_;
        }

        return pX > p_14007_ ? pX : p_14007_;
    }

    public static int intFloorDiv(int pX, int pY)
    {
        return Math.floorDiv(pX, pY);
    }

    public static int nextInt(Random pRandom, int pMinimum, int pMaximum)
    {
        return pMinimum >= pMaximum ? pMinimum : pRandom.nextInt(pMaximum - pMinimum + 1) + pMinimum;
    }

    public static float nextFloat(Random pRandom, float pMinimum, float pMaximum)
    {
        return pMinimum >= pMaximum ? pMinimum : pRandom.nextFloat() * (pMaximum - pMinimum) + pMinimum;
    }

    public static double nextDouble(Random pRandom, double pMinimum, double p_14067_)
    {
        return pMinimum >= p_14067_ ? pMinimum : pRandom.nextDouble() * (p_14067_ - pMinimum) + pMinimum;
    }

    public static double m_14078_(long[] p_14079_)
    {
        long i = 0L;

        for (long j : p_14079_)
        {
            i += j;
        }

        return (double)i / (double)p_14079_.length;
    }

    public static boolean equal(float pX, float p_14035_)
    {
        return Math.abs(p_14035_ - pX) < 1.0E-5F;
    }

    public static boolean equal(double pX, double p_14084_)
    {
        return Math.abs(p_14084_ - pX) < (double)1.0E-5F;
    }

    public static int positiveModulo(int pNumerator, int p_14102_)
    {
        return Math.floorMod(pNumerator, p_14102_);
    }

    public static float positiveModulo(float pNumerator, float p_14093_)
    {
        return (pNumerator % p_14093_ + p_14093_) % p_14093_;
    }

    public static double positiveModulo(double pNumerator, double p_14111_)
    {
        return (pNumerator % p_14111_ + p_14111_) % p_14111_;
    }

    public static int wrapDegrees(int pValue)
    {
        int i = pValue % 360;

        if (i >= 180)
        {
            i -= 360;
        }

        if (i < -180)
        {
            i += 360;
        }

        return i;
    }

    public static float wrapDegrees(float pValue)
    {
        float f = pValue % 360.0F;

        if (f >= 180.0F)
        {
            f -= 360.0F;
        }

        if (f < -180.0F)
        {
            f += 360.0F;
        }

        return f;
    }

    public static double wrapDegrees(double pValue)
    {
        double d0 = pValue % 360.0D;

        if (d0 >= 180.0D)
        {
            d0 -= 360.0D;
        }

        if (d0 < -180.0D)
        {
            d0 += 360.0D;
        }

        return d0;
    }

    public static float degreesDifference(float p_14119_, float p_14120_)
    {
        return wrapDegrees(p_14120_ - p_14119_);
    }

    public static float degreesDifferenceAbs(float p_14146_, float p_14147_)
    {
        return abs(degreesDifference(p_14146_, p_14147_));
    }

    public static float rotateIfNecessary(float p_14095_, float p_14096_, float p_14097_)
    {
        float f = degreesDifference(p_14095_, p_14096_);
        float f1 = clamp(f, -p_14097_, p_14097_);
        return p_14096_ - f1;
    }

    public static float approach(float p_14122_, float p_14123_, float p_14124_)
    {
        p_14124_ = abs(p_14124_);
        return p_14122_ < p_14123_ ? clamp(p_14122_ + p_14124_, p_14122_, p_14123_) : clamp(p_14122_ - p_14124_, p_14123_, p_14122_);
    }

    public static float approachDegrees(float p_14149_, float p_14150_, float p_14151_)
    {
        float f = degreesDifference(p_14149_, p_14150_);
        return approach(p_14149_, p_14149_ + f, p_14151_);
    }

    public static int getInt(String pValue, int pDefaultValue)
    {
        return NumberUtils.toInt(pValue, pDefaultValue);
    }

    public static int getInt(String pValue, int pDefaultValue, int p_144908_)
    {
        return Math.max(p_144908_, getInt(pValue, pDefaultValue));
    }

    public static double getDouble(String pValue, double pDefaultValue)
    {
        try
        {
            return Double.parseDouble(pValue);
        }
        catch (Throwable throwable)
        {
            return pDefaultValue;
        }
    }

    public static double getDouble(String pValue, double pDefaultValue, double p_144904_)
    {
        return Math.max(p_144904_, getDouble(pValue, pDefaultValue));
    }

    public static int smallestEncompassingPowerOfTwo(int pValue)
    {
        int i = pValue - 1;
        i = i | i >> 1;
        i = i | i >> 2;
        i = i | i >> 4;
        i = i | i >> 8;
        i = i | i >> 16;
        return i + 1;
    }

    public static boolean isPowerOfTwo(int pValue)
    {
        return pValue != 0 && (pValue & pValue - 1) == 0;
    }

    public static int ceillog2(int pValue)
    {
        pValue = isPowerOfTwo(pValue) ? pValue : smallestEncompassingPowerOfTwo(pValue);
        return MULTIPLY_DE_BRUIJN_BIT_POSITION[(int)((long)pValue * 125613361L >> 27) & 31];
    }

    public static int log2(int pValue)
    {
        return ceillog2(pValue) - (isPowerOfTwo(pValue) ? 0 : 1);
    }

    public static int color(float pR, float pG, float pB)
    {
        return color(floor(pR * 255.0F), floor(pG * 255.0F), floor(pB * 255.0F));
    }

    public static int color(int pR, int pG, int pB)
    {
        int i = (pR << 8) + pG;
        return (i << 8) + pB;
    }

    public static int colorMultiply(int p_144933_, int p_144934_)
    {
        int i = (p_144933_ & 16711680) >> 16;
        int j = (p_144934_ & 16711680) >> 16;
        int k = (p_144933_ & 65280) >> 8;
        int l = (p_144934_ & 65280) >> 8;
        int i1 = (p_144933_ & 255) >> 0;
        int j1 = (p_144934_ & 255) >> 0;
        int k1 = (int)((float)i * (float)j / 255.0F);
        int l1 = (int)((float)k * (float)l / 255.0F);
        int i2 = (int)((float)i1 * (float)j1 / 255.0F);
        return p_144933_ & -16777216 | k1 << 16 | l1 << 8 | i2;
    }

    public static int colorMultiply(int p_144882_, float p_144883_, float p_144884_, float p_144885_)
    {
        int i = (p_144882_ & 16711680) >> 16;
        int j = (p_144882_ & 65280) >> 8;
        int k = (p_144882_ & 255) >> 0;
        int l = (int)((float)i * p_144883_);
        int i1 = (int)((float)j * p_144884_);
        int j1 = (int)((float)k * p_144885_);
        return p_144882_ & -16777216 | l << 16 | i1 << 8 | j1;
    }

    public static float frac(float pNumber)
    {
        return pNumber - (float)floor(pNumber);
    }

    public static double frac(double pNumber)
    {
        return pNumber - (double)lfloor(pNumber);
    }

    public static Vec3 catmullRomSplinePos(Vec3 p_144893_, Vec3 p_144894_, Vec3 p_144895_, Vec3 p_144896_, double p_144897_)
    {
        double d0 = ((-p_144897_ + 2.0D) * p_144897_ - 1.0D) * p_144897_ * 0.5D;
        double d1 = ((3.0D * p_144897_ - 5.0D) * p_144897_ * p_144897_ + 2.0D) * 0.5D;
        double d2 = ((-3.0D * p_144897_ + 4.0D) * p_144897_ + 1.0D) * p_144897_ * 0.5D;
        double d3 = (p_144897_ - 1.0D) * p_144897_ * p_144897_ * 0.5D;
        return new Vec3(p_144893_.x * d0 + p_144894_.x * d1 + p_144895_.x * d2 + p_144896_.x * d3, p_144893_.y * d0 + p_144894_.y * d1 + p_144895_.y * d2 + p_144896_.y * d3, p_144893_.z * d0 + p_144894_.z * d1 + p_144895_.z * d2 + p_144896_.z * d3);
    }

    public static long getSeed(Vec3i pX)
    {
        return getSeed(pX.getX(), pX.getY(), pX.getZ());
    }

    public static long getSeed(int pX, int pY, int pZ)
    {
        long i = (long)(pX * 3129871) ^ (long)pZ * 116129781L ^ (long)pY;
        i = i * i * 42317861L + i * 11L;
        return i >> 16;
    }

    public static UUID createInsecureUUID(Random p_14063_)
    {
        long i = p_14063_.nextLong() & -61441L | 16384L;
        long j = p_14063_.nextLong() & 4611686018427387903L | Long.MIN_VALUE;
        return new UUID(i, j);
    }

    public static UUID createInsecureUUID()
    {
        return createInsecureUUID(RANDOM);
    }

    public static double inverseLerp(double p_14113_, double p_14114_, double p_14115_)
    {
        return (p_14113_ - p_14114_) / (p_14115_ - p_14114_);
    }

    public static boolean rayIntersectsAABB(Vec3 p_144889_, Vec3 p_144890_, AABB p_144891_)
    {
        double d0 = (p_144891_.minX + p_144891_.maxX) * 0.5D;
        double d1 = (p_144891_.maxX - p_144891_.minX) * 0.5D;
        double d2 = p_144889_.x - d0;

        if (Math.abs(d2) > d1 && d2 * p_144890_.x >= 0.0D)
        {
            return false;
        }
        else
        {
            double d3 = (p_144891_.minY + p_144891_.maxY) * 0.5D;
            double d4 = (p_144891_.maxY - p_144891_.minY) * 0.5D;
            double d5 = p_144889_.y - d3;

            if (Math.abs(d5) > d4 && d5 * p_144890_.y >= 0.0D)
            {
                return false;
            }
            else
            {
                double d6 = (p_144891_.minZ + p_144891_.maxZ) * 0.5D;
                double d7 = (p_144891_.maxZ - p_144891_.minZ) * 0.5D;
                double d8 = p_144889_.z - d6;

                if (Math.abs(d8) > d7 && d8 * p_144890_.z >= 0.0D)
                {
                    return false;
                }
                else
                {
                    double d9 = Math.abs(p_144890_.x);
                    double d10 = Math.abs(p_144890_.y);
                    double d11 = Math.abs(p_144890_.z);
                    double d12 = p_144890_.y * d8 - p_144890_.z * d5;

                    if (Math.abs(d12) > d4 * d11 + d7 * d10)
                    {
                        return false;
                    }
                    else
                    {
                        d12 = p_144890_.z * d2 - p_144890_.x * d8;

                        if (Math.abs(d12) > d1 * d11 + d7 * d9)
                        {
                            return false;
                        }
                        else
                        {
                            d12 = p_144890_.x * d5 - p_144890_.y * d2;
                            return Math.abs(d12) < d1 * d10 + d4 * d9;
                        }
                    }
                }
            }
        }
    }

    public static double atan2(double p_14137_, double p_14138_)
    {
        double d0 = p_14138_ * p_14138_ + p_14137_ * p_14137_;

        if (Double.isNaN(d0))
        {
            return Double.NaN;
        }
        else
        {
            boolean flag = p_14137_ < 0.0D;

            if (flag)
            {
                p_14137_ = -p_14137_;
            }

            boolean flag1 = p_14138_ < 0.0D;

            if (flag1)
            {
                p_14138_ = -p_14138_;
            }

            boolean flag2 = p_14137_ > p_14138_;

            if (flag2)
            {
                double d1 = p_14138_;
                p_14138_ = p_14137_;
                p_14137_ = d1;
            }

            double d9 = fastInvSqrt(d0);
            p_14138_ = p_14138_ * d9;
            p_14137_ = p_14137_ * d9;
            double d2 = FRAC_BIAS + p_14137_;
            int i = (int)Double.doubleToRawLongBits(d2);
            double d3 = ASIN_TAB[i];
            double d4 = COS_TAB[i];
            double d5 = d2 - FRAC_BIAS;
            double d6 = p_14137_ * d4 - p_14138_ * d5;
            double d7 = (6.0D + d6 * d6) * d6 * 0.16666666666666666D;
            double d8 = d3 + d7;

            if (flag2)
            {
                d8 = (Math.PI / 2D) - d8;
            }

            if (flag1)
            {
                d8 = Math.PI - d8;
            }

            if (flag)
            {
                d8 = -d8;
            }

            return d8;
        }
    }

    public static float fastInvSqrt(float pNumber)
    {
        float f = 0.5F * pNumber;
        int i = Float.floatToIntBits(pNumber);
        i = 1597463007 - (i >> 1);
        pNumber = Float.intBitsToFloat(i);
        return pNumber * (1.5F - f * pNumber * pNumber);
    }

    public static double fastInvSqrt(double pNumber)
    {
        double d0 = 0.5D * pNumber;
        long i = Double.doubleToRawLongBits(pNumber);
        i = 6910469410427058090L - (i >> 1);
        pNumber = Double.longBitsToDouble(i);
        return pNumber * (1.5D - d0 * pNumber * pNumber);
    }

    public static float fastInvCubeRoot(float pNumber)
    {
        int i = Float.floatToIntBits(pNumber);
        i = 1419967116 - i / 3;
        float f = Float.intBitsToFloat(i);
        f = 0.6666667F * f + 1.0F / (3.0F * f * f * pNumber);
        return 0.6666667F * f + 1.0F / (3.0F * f * f * pNumber);
    }

    public static int hsvToRgb(float pHue, float pSaturation, float pValue)
    {
        int i = (int)(pHue * 6.0F) % 6;
        float f = pHue * 6.0F - (float)i;
        float f1 = pValue * (1.0F - pSaturation);
        float f2 = pValue * (1.0F - f * pSaturation);
        float f3 = pValue * (1.0F - (1.0F - f) * pSaturation);
        float f4;
        float f5;
        float f6;

        switch (i)
        {
            case 0:
                f4 = pValue;
                f5 = f3;
                f6 = f1;
                break;

            case 1:
                f4 = f2;
                f5 = pValue;
                f6 = f1;
                break;

            case 2:
                f4 = f1;
                f5 = pValue;
                f6 = f3;
                break;

            case 3:
                f4 = f1;
                f5 = f2;
                f6 = pValue;
                break;

            case 4:
                f4 = f3;
                f5 = f1;
                f6 = pValue;
                break;

            case 5:
                f4 = pValue;
                f5 = f1;
                f6 = f2;
                break;

            default:
                throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + pHue + ", " + pSaturation + ", " + pValue);
        }

        int j = clamp((int)(f4 * 255.0F), 0, 255);
        int k = clamp((int)(f5 * 255.0F), 0, 255);
        int l = clamp((int)(f6 * 255.0F), 0, 255);
        return j << 16 | k << 8 | l;
    }

    public static int murmurHash3Mixer(int p_14184_)
    {
        p_14184_ = p_14184_ ^ p_14184_ >>> 16;
        p_14184_ = p_14184_ * -2048144789;
        p_14184_ = p_14184_ ^ p_14184_ >>> 13;
        p_14184_ = p_14184_ * -1028477387;
        return p_14184_ ^ p_14184_ >>> 16;
    }

    public static long murmurHash3Mixer(long p_144887_)
    {
        p_144887_ = p_144887_ ^ p_144887_ >>> 33;
        p_144887_ = p_144887_ * -49064778989728563L;
        p_144887_ = p_144887_ ^ p_144887_ >>> 33;
        p_144887_ = p_144887_ * -4265267296055464877L;
        return p_144887_ ^ p_144887_ >>> 33;
    }

    public static double[] m_144912_(double... p_144913_)
    {
        float f = 0.0F;

        for (double d0 : p_144913_)
        {
            f = (float)((double)f + d0);
        }

        for (int i = 0; i < p_144913_.length; ++i)
        {
            p_144913_[i] /= (double)f;
        }

        for (int j = 0; j < p_144913_.length; ++j)
        {
            p_144913_[j] += j == 0 ? 0.0D : p_144913_[j - 1];
        }

        return p_144913_;
    }

    public static int m_144909_(Random p_144910_, double[] p_144911_)
    {
        double d0 = p_144910_.nextDouble();

        for (int i = 0; i < p_144911_.length; ++i)
        {
            if (d0 < p_144911_[i])
            {
                return i;
            }
        }

        return p_144911_.length;
    }

    public static double[] binNormalDistribution(double p_144867_, double p_144868_, double p_144869_, int p_144870_, int p_144871_)
    {
        double[] adouble = new double[p_144871_ - p_144870_ + 1];
        int i = 0;

        for (int j = p_144870_; j <= p_144871_; ++j)
        {
            adouble[i] = Math.max(0.0D, p_144867_ * StrictMath.exp(-((double)j - p_144869_) * ((double)j - p_144869_) / (2.0D * p_144868_ * p_144868_)));
            ++i;
        }

        return adouble;
    }

    public static double[] binBiModalNormalDistribution(double p_144858_, double p_144859_, double p_144860_, double p_144861_, double p_144862_, double p_144863_, int p_144864_, int p_144865_)
    {
        double[] adouble = new double[p_144865_ - p_144864_ + 1];
        int i = 0;

        for (int j = p_144864_; j <= p_144865_; ++j)
        {
            adouble[i] = Math.max(0.0D, p_144858_ * StrictMath.exp(-((double)j - p_144860_) * ((double)j - p_144860_) / (2.0D * p_144859_ * p_144859_)) + p_144861_ * StrictMath.exp(-((double)j - p_144863_) * ((double)j - p_144863_) / (2.0D * p_144862_ * p_144862_)));
            ++i;
        }

        return adouble;
    }

    public static double[] binLogDistribution(double p_144873_, double p_144874_, int p_144875_, int p_144876_)
    {
        double[] adouble = new double[p_144876_ - p_144875_ + 1];
        int i = 0;

        for (int j = p_144875_; j <= p_144876_; ++j)
        {
            adouble[i] = Math.max(p_144873_ * StrictMath.log((double)j) + p_144874_, 0.0D);
            ++i;
        }

        return adouble;
    }

    public static int binarySearch(int pMin, int pMax, IntPredicate pIsTargetBeforeOrAt)
    {
        int i = pMax - pMin;

        while (i > 0)
        {
            int j = i / 2;
            int k = pMin + j;

            if (pIsTargetBeforeOrAt.test(k))
            {
                i = j;
            }
            else
            {
                pMin = k + 1;
                i -= j + 1;
            }
        }

        return pMin;
    }

    public static float lerp(float pPct, float p_14181_, float pStart)
    {
        return p_14181_ + pPct * (pStart - p_14181_);
    }

    public static double lerp(double pPct, double p_14141_, double pStart)
    {
        return p_14141_ + pPct * (pStart - p_14141_);
    }

    public static double lerp2(double p_14013_, double p_14014_, double p_14015_, double p_14016_, double p_14017_, double p_14018_)
    {
        return lerp(p_14014_, lerp(p_14013_, p_14015_, p_14016_), lerp(p_14013_, p_14017_, p_14018_));
    }

    public static double lerp3(double p_14020_, double p_14021_, double p_14022_, double p_14023_, double p_14024_, double p_14025_, double p_14026_, double p_14027_, double p_14028_, double p_14029_, double p_14030_)
    {
        return lerp(p_14022_, lerp2(p_14020_, p_14021_, p_14023_, p_14024_, p_14025_, p_14026_), lerp2(p_14020_, p_14021_, p_14027_, p_14028_, p_14029_, p_14030_));
    }

    public static double smoothstep(double p_14198_)
    {
        return p_14198_ * p_14198_ * p_14198_ * (p_14198_ * (p_14198_ * 6.0D - 15.0D) + 10.0D);
    }

    public static double smoothstepDerivative(double p_144947_)
    {
        return 30.0D * p_144947_ * p_144947_ * (p_144947_ - 1.0D) * (p_144947_ - 1.0D);
    }

    public static int sign(double pX)
    {
        if (pX == 0.0D)
        {
            return 0;
        }
        else
        {
            return pX > 0.0D ? 1 : -1;
        }
    }

    public static float rotLerp(float p_14190_, float p_14191_, float p_14192_)
    {
        return p_14191_ + p_14190_ * wrapDegrees(p_14192_ - p_14191_);
    }

    public static float diffuseLight(float p_144949_, float p_144950_, float p_144951_)
    {
        return Math.min(p_144949_ * p_144949_ * 0.6F + p_144950_ * p_144950_ * ((3.0F + p_144950_) / 4.0F) + p_144951_ * p_144951_ * 0.8F, 1.0F);
    }

    @Deprecated
    public static float rotlerp(float p_14202_, float p_14203_, float p_14204_)
    {
        float f;

        for (f = p_14203_ - p_14202_; f < -180.0F; f += 360.0F)
        {
        }

        while (f >= 180.0F)
        {
            f -= 360.0F;
        }

        return p_14202_ + p_14204_ * f;
    }

    @Deprecated
    public static float rotWrap(double p_14210_)
    {
        while (p_14210_ >= 180.0D)
        {
            p_14210_ -= 360.0D;
        }

        while (p_14210_ < -180.0D)
        {
            p_14210_ += 360.0D;
        }

        return (float)p_14210_;
    }

    public static float triangleWave(float p_14157_, float p_14158_)
    {
        return (Math.abs(p_14157_ % p_14158_ - p_14158_ * 0.5F) - p_14158_ * 0.25F) / (p_14158_ * 0.25F);
    }

    public static float square(float pValue)
    {
        return pValue * pValue;
    }

    public static double square(double pValue)
    {
        return pValue * pValue;
    }

    public static int square(int pValue)
    {
        return pValue * pValue;
    }

    public static double clampedMap(double p_144852_, double p_144853_, double p_144854_, double p_144855_, double p_144856_)
    {
        return clampedLerp(p_144855_, p_144856_, inverseLerp(p_144852_, p_144853_, p_144854_));
    }

    public static double map(double p_144915_, double p_144916_, double p_144917_, double p_144918_, double p_144919_)
    {
        return lerp(inverseLerp(p_144915_, p_144916_, p_144917_), p_144918_, p_144919_);
    }

    public static double wobble(double p_144955_)
    {
        return p_144955_ + (2.0D * (new Random((long)floor(p_144955_ * 3000.0D))).nextDouble() - 1.0D) * 1.0E-7D / 2.0D;
    }

    public static int roundToward(int p_144942_, int p_144943_)
    {
        return (p_144942_ + p_144943_ - 1) / p_144943_ * p_144943_;
    }

    public static int randomBetweenInclusive(Random p_144929_, int p_144930_, int p_144931_)
    {
        return p_144929_.nextInt(p_144931_ - p_144930_ + 1) + p_144930_;
    }

    public static float randomBetween(Random p_144925_, float p_144926_, float p_144927_)
    {
        return p_144925_.nextFloat() * (p_144927_ - p_144926_) + p_144926_;
    }

    public static float normal(Random p_144936_, float p_144937_, float p_144938_)
    {
        return p_144937_ + (float)p_144936_.nextGaussian() * p_144938_;
    }

    public static double length(int p_144878_, double p_144879_, int p_144880_)
    {
        return Math.sqrt((double)(p_144878_ * p_144878_) + p_144879_ * p_144879_ + (double)(p_144880_ * p_144880_));
    }

    static
    {
        for (int i = 0; i < 257; ++i)
        {
            double d0 = (double)i / 256.0D;
            double d1 = Math.asin(d0);
            COS_TAB[i] = Math.cos(d1);
            ASIN_TAB[i] = d1;
        }

        for (int j = 0; j < SIN_TABLE_FAST.length; ++j)
        {
            SIN_TABLE_FAST[j] = MathUtils.roundToFloat(Math.sin((double)j * Math.PI * 2.0D / 4096.0D));
        }
    }
}
