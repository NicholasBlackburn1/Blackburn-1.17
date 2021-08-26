package net.minecraft.client.renderer.block.model;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockMath;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.optifine.Config;
import net.optifine.model.BlockModelUtils;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorForge;

public class FaceBakery
{
    public static final int VERTEX_INT_SIZE = 8;
    private static final float RESCALE_22_5 = 1.0F / (float)Math.cos((double)((float)Math.PI / 8F)) - 1.0F;
    private static final float RESCALE_45 = 1.0F / (float)Math.cos((double)((float)Math.PI / 4F)) - 1.0F;
    public static final int VERTEX_COUNT = 4;
    private static final int COLOR_INDEX = 3;
    public static final int UV_INDEX = 4;

    public BakedQuad bakeQuad(Vector3f pPosFrom, Vector3f pPosTo, BlockElementFace pFace, TextureAtlasSprite pSprite, Direction pFacing, ModelState pTransform, @Nullable BlockElementRotation pPartRotation, boolean pShade, ResourceLocation pModelLocation)
    {
        BlockFaceUV blockfaceuv = pFace.uv;

        if (pTransform.isUvLocked())
        {
            blockfaceuv = recomputeUVs(pFace.uv, pFacing, pTransform.getRotation(), pModelLocation);
        }

        float[] afloat = new float[blockfaceuv.uvs.length];
        System.arraycopy(blockfaceuv.uvs, 0, afloat, 0, afloat.length);
        float f = pSprite.uvShrinkRatio();
        float f1 = (blockfaceuv.uvs[0] + blockfaceuv.uvs[0] + blockfaceuv.uvs[2] + blockfaceuv.uvs[2]) / 4.0F;
        float f2 = (blockfaceuv.uvs[1] + blockfaceuv.uvs[1] + blockfaceuv.uvs[3] + blockfaceuv.uvs[3]) / 4.0F;
        blockfaceuv.uvs[0] = Mth.lerp(f, blockfaceuv.uvs[0], f1);
        blockfaceuv.uvs[2] = Mth.lerp(f, blockfaceuv.uvs[2], f1);
        blockfaceuv.uvs[1] = Mth.lerp(f, blockfaceuv.uvs[1], f2);
        blockfaceuv.uvs[3] = Mth.lerp(f, blockfaceuv.uvs[3], f2);
        boolean flag = Reflector.ForgeHooksClient_fillNormal.exists() ? false : pShade;
        int[] aint = this.m_111573_(blockfaceuv, pSprite, pFacing, this.setupShape(pPosFrom, pPosTo), pTransform.getRotation(), pPartRotation, flag);
        Direction direction = m_111612_(aint);
        System.arraycopy(afloat, 0, blockfaceuv.uvs, 0, afloat.length);

        if (pPartRotation == null)
        {
            this.m_111630_(aint, direction);
        }

        if (Reflector.ForgeHooksClient_fillNormal.exists())
        {
            ReflectorForge.fillNormal(aint, direction);
            return new BakedQuad(aint, pFace.tintIndex, direction, pSprite, pShade);
        }
        else
        {
            return new BakedQuad(aint, pFace.tintIndex, direction, pSprite, pShade);
        }
    }

    public static BlockFaceUV recomputeUVs(BlockFaceUV pBlockFaceUV, Direction pFacing, Transformation pModelRotation, ResourceLocation pModelLocation)
    {
        Matrix4f matrix4f = BlockMath.getUVLockTransform(pModelRotation, pFacing, () ->
        {
            return "Unable to resolve UVLock for model: " + pModelLocation;
        }).getMatrix();
        float f = pBlockFaceUV.getU(pBlockFaceUV.getReverseIndex(0));
        float f1 = pBlockFaceUV.getV(pBlockFaceUV.getReverseIndex(0));
        Vector4f vector4f = new Vector4f(f / 16.0F, f1 / 16.0F, 0.0F, 1.0F);
        vector4f.transform(matrix4f);
        float f2 = 16.0F * vector4f.x();
        float f3 = 16.0F * vector4f.y();
        float f4 = pBlockFaceUV.getU(pBlockFaceUV.getReverseIndex(2));
        float f5 = pBlockFaceUV.getV(pBlockFaceUV.getReverseIndex(2));
        Vector4f vector4f1 = new Vector4f(f4 / 16.0F, f5 / 16.0F, 0.0F, 1.0F);
        vector4f1.transform(matrix4f);
        float f6 = 16.0F * vector4f1.x();
        float f7 = 16.0F * vector4f1.y();
        float f8;
        float f9;

        if (Math.signum(f4 - f) == Math.signum(f6 - f2))
        {
            f8 = f2;
            f9 = f6;
        }
        else
        {
            f8 = f6;
            f9 = f2;
        }

        float f10;
        float f11;

        if (Math.signum(f5 - f1) == Math.signum(f7 - f3))
        {
            f10 = f3;
            f11 = f7;
        }
        else
        {
            f10 = f7;
            f11 = f3;
        }

        float f12 = (float)Math.toRadians((double)pBlockFaceUV.rotation);
        Vector3f vector3f = new Vector3f(Mth.cos(f12), Mth.sin(f12), 0.0F);
        Matrix3f matrix3f = new Matrix3f(matrix4f);
        vector3f.transform(matrix3f);
        int i = Math.floorMod(-((int)Math.round(Math.toDegrees(Math.atan2((double)vector3f.y(), (double)vector3f.x())) / 90.0D)) * 90, 360);
        return new BlockFaceUV(new float[] {f8, f10, f9, f11}, i);
    }

    private int[] m_111573_(BlockFaceUV p_111574_, TextureAtlasSprite p_111575_, Direction p_111576_, float[] p_111577_, Transformation p_111578_, @Nullable BlockElementRotation p_111579_, boolean p_111580_)
    {
        int i = Config.isShaders() ? DefaultVertexFormat.BLOCK_SHADERS_SIZE : DefaultVertexFormat.BLOCK_VANILLA_SIZE;
        int[] aint = new int[i];

        for (int j = 0; j < 4; ++j)
        {
            this.m_111620_(aint, j, p_111576_, p_111574_, p_111577_, p_111575_, p_111578_, p_111579_, p_111580_);
        }

        return aint;
    }

    private float[] setupShape(Vector3f pPos1, Vector3f pPos2)
    {
        float[] afloat = new float[Direction.values().length];
        afloat[FaceInfo.Constants.MIN_X] = pPos1.x() / 16.0F;
        afloat[FaceInfo.Constants.MIN_Y] = pPos1.y() / 16.0F;
        afloat[FaceInfo.Constants.MIN_Z] = pPos1.z() / 16.0F;
        afloat[FaceInfo.Constants.MAX_X] = pPos2.x() / 16.0F;
        afloat[FaceInfo.Constants.MAX_Y] = pPos2.y() / 16.0F;
        afloat[FaceInfo.Constants.MAX_Z] = pPos2.z() / 16.0F;
        return afloat;
    }

    private void m_111620_(int[] p_111621_, int p_111622_, Direction p_111623_, BlockFaceUV p_111624_, float[] p_111625_, TextureAtlasSprite p_111626_, Transformation p_111627_, @Nullable BlockElementRotation p_111628_, boolean p_111629_)
    {
        FaceInfo.VertexInfo faceinfo$vertexinfo = FaceInfo.fromFacing(p_111623_).getVertexInfo(p_111622_);
        Vector3f vector3f = new Vector3f(p_111625_[faceinfo$vertexinfo.xFace], p_111625_[faceinfo$vertexinfo.yFace], p_111625_[faceinfo$vertexinfo.zFace]);
        this.applyElementRotation(vector3f, p_111628_);
        this.applyModelRotation(vector3f, p_111627_);
        BlockModelUtils.snapVertexPosition(vector3f);
        this.m_111614_(p_111621_, p_111622_, vector3f, p_111626_, p_111624_);
    }

    private void m_111614_(int[] p_111615_, int p_111616_, Vector3f p_111617_, TextureAtlasSprite p_111618_, BlockFaceUV p_111619_)
    {
        int i = p_111615_.length / 4;
        int j = p_111616_ * i;
        p_111615_[j] = Float.floatToRawIntBits(p_111617_.x());
        p_111615_[j + 1] = Float.floatToRawIntBits(p_111617_.y());
        p_111615_[j + 2] = Float.floatToRawIntBits(p_111617_.z());
        p_111615_[j + 3] = -1;
        p_111615_[j + 4] = Float.floatToRawIntBits(p_111618_.getU((double)p_111619_.getU(p_111616_)));
        p_111615_[j + 4 + 1] = Float.floatToRawIntBits(p_111618_.getV((double)p_111619_.getV(p_111616_)));
    }

    private void applyElementRotation(Vector3f pVec, @Nullable BlockElementRotation pPartRotation)
    {
        if (pPartRotation != null)
        {
            Vector3f vector3f;
            Vector3f vector3f1;

            switch (pPartRotation.axis)
            {
                case X:
                    vector3f = Vector3f.XP;
                    vector3f1 = new Vector3f(0.0F, 1.0F, 1.0F);
                    break;

                case Y:
                    vector3f = Vector3f.YP;
                    vector3f1 = new Vector3f(1.0F, 0.0F, 1.0F);
                    break;

                case Z:
                    vector3f = Vector3f.ZP;
                    vector3f1 = new Vector3f(1.0F, 1.0F, 0.0F);
                    break;

                default:
                    throw new IllegalArgumentException("There are only 3 axes");
            }

            Quaternion quaternion = vector3f.rotationDegrees(pPartRotation.angle);

            if (pPartRotation.rescale)
            {
                if (Math.abs(pPartRotation.angle) == 22.5F)
                {
                    vector3f1.mul(RESCALE_22_5);
                }
                else
                {
                    vector3f1.mul(RESCALE_45);
                }

                vector3f1.add(1.0F, 1.0F, 1.0F);
            }
            else
            {
                vector3f1.set(1.0F, 1.0F, 1.0F);
            }

            this.rotateVertexBy(pVec, pPartRotation.origin.copy(), new Matrix4f(quaternion), vector3f1);
        }
    }

    public void applyModelRotation(Vector3f pPos, Transformation pTransform)
    {
        if (pTransform != Transformation.identity())
        {
            this.rotateVertexBy(pPos, new Vector3f(0.5F, 0.5F, 0.5F), pTransform.getMatrix(), new Vector3f(1.0F, 1.0F, 1.0F));
        }
    }

    private void rotateVertexBy(Vector3f pPos, Vector3f pOrigin, Matrix4f pTransform, Vector3f pScale)
    {
        Vector4f vector4f = new Vector4f(pPos.x() - pOrigin.x(), pPos.y() - pOrigin.y(), pPos.z() - pOrigin.z(), 1.0F);
        vector4f.transform(pTransform);
        vector4f.mul(pScale);
        pPos.set(vector4f.x() + pOrigin.x(), vector4f.y() + pOrigin.y(), vector4f.z() + pOrigin.z());
    }

    public static Direction m_111612_(int[] p_111613_)
    {
        int i = p_111613_.length / 4;
        int j = i * 2;
        Vector3f vector3f = new Vector3f(Float.intBitsToFloat(p_111613_[0]), Float.intBitsToFloat(p_111613_[1]), Float.intBitsToFloat(p_111613_[2]));
        Vector3f vector3f1 = new Vector3f(Float.intBitsToFloat(p_111613_[i]), Float.intBitsToFloat(p_111613_[i + 1]), Float.intBitsToFloat(p_111613_[i + 2]));
        Vector3f vector3f2 = new Vector3f(Float.intBitsToFloat(p_111613_[j]), Float.intBitsToFloat(p_111613_[j + 1]), Float.intBitsToFloat(p_111613_[j + 2]));
        Vector3f vector3f3 = vector3f.copy();
        vector3f3.sub(vector3f1);
        Vector3f vector3f4 = vector3f2.copy();
        vector3f4.sub(vector3f1);
        Vector3f vector3f5 = vector3f4.copy();
        vector3f5.cross(vector3f3);
        vector3f5.normalize();
        Direction direction = null;
        float f = 0.0F;

        for (Direction direction1 : Direction.values())
        {
            Vec3i vec3i = direction1.getNormal();
            Vector3f vector3f6 = new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
            float f1 = vector3f5.dot(vector3f6);

            if (f1 >= 0.0F && f1 > f)
            {
                f = f1;
                direction = direction1;
            }
        }

        return direction == null ? Direction.UP : direction;
    }

    private void m_111630_(int[] p_111631_, Direction p_111632_)
    {
        int[] aint = new int[p_111631_.length];
        System.arraycopy(p_111631_, 0, aint, 0, p_111631_.length);
        float[] afloat = new float[Direction.values().length];
        afloat[FaceInfo.Constants.MIN_X] = 999.0F;
        afloat[FaceInfo.Constants.MIN_Y] = 999.0F;
        afloat[FaceInfo.Constants.MIN_Z] = 999.0F;
        afloat[FaceInfo.Constants.MAX_X] = -999.0F;
        afloat[FaceInfo.Constants.MAX_Y] = -999.0F;
        afloat[FaceInfo.Constants.MAX_Z] = -999.0F;
        int i = p_111631_.length / 4;

        for (int j = 0; j < 4; ++j)
        {
            int k = i * j;
            float f = Float.intBitsToFloat(aint[k]);
            float f1 = Float.intBitsToFloat(aint[k + 1]);
            float f2 = Float.intBitsToFloat(aint[k + 2]);

            if (f < afloat[FaceInfo.Constants.MIN_X])
            {
                afloat[FaceInfo.Constants.MIN_X] = f;
            }

            if (f1 < afloat[FaceInfo.Constants.MIN_Y])
            {
                afloat[FaceInfo.Constants.MIN_Y] = f1;
            }

            if (f2 < afloat[FaceInfo.Constants.MIN_Z])
            {
                afloat[FaceInfo.Constants.MIN_Z] = f2;
            }

            if (f > afloat[FaceInfo.Constants.MAX_X])
            {
                afloat[FaceInfo.Constants.MAX_X] = f;
            }

            if (f1 > afloat[FaceInfo.Constants.MAX_Y])
            {
                afloat[FaceInfo.Constants.MAX_Y] = f1;
            }

            if (f2 > afloat[FaceInfo.Constants.MAX_Z])
            {
                afloat[FaceInfo.Constants.MAX_Z] = f2;
            }
        }

        FaceInfo faceinfo = FaceInfo.fromFacing(p_111632_);

        for (int j1 = 0; j1 < 4; ++j1)
        {
            int k1 = i * j1;
            FaceInfo.VertexInfo faceinfo$vertexinfo = faceinfo.getVertexInfo(j1);
            float f8 = afloat[faceinfo$vertexinfo.xFace];
            float f3 = afloat[faceinfo$vertexinfo.yFace];
            float f4 = afloat[faceinfo$vertexinfo.zFace];
            p_111631_[k1] = Float.floatToRawIntBits(f8);
            p_111631_[k1 + 1] = Float.floatToRawIntBits(f3);
            p_111631_[k1 + 2] = Float.floatToRawIntBits(f4);

            for (int l = 0; l < 4; ++l)
            {
                int i1 = i * l;
                float f5 = Float.intBitsToFloat(aint[i1]);
                float f6 = Float.intBitsToFloat(aint[i1 + 1]);
                float f7 = Float.intBitsToFloat(aint[i1 + 2]);

                if (Mth.equal(f8, f5) && Mth.equal(f3, f6) && Mth.equal(f4, f7))
                {
                    p_111631_[k1 + 4] = aint[i1 + 4];
                    p_111631_[k1 + 4 + 1] = aint[i1 + 4 + 1];
                }
            }
        }
    }

    public static ResourceLocation getParentLocation(BlockModel blockModel)
    {
        return blockModel.parentLocation;
    }

    public static void setParentLocation(BlockModel blockModel, ResourceLocation location)
    {
        blockModel.parentLocation = location;
    }

    public static Map<String, Either<Material, String>> getTextures(BlockModel blockModel)
    {
        return blockModel.textureMap;
    }
}
