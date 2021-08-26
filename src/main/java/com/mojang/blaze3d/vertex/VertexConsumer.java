package com.mojang.blaze3d.vertex;

import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.IForgeVertexConsumer;
import net.optifine.Config;
import net.optifine.IRandomEntity;
import net.optifine.RandomEntities;
import net.optifine.reflect.Reflector;
import net.optifine.render.RenderEnv;
import net.optifine.render.VertexPosition;
import net.optifine.shaders.Shaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface VertexConsumer extends IForgeVertexConsumer
{
    Logger LOGGER = LogManager.getLogger();
    ThreadLocal<RenderEnv> RENDER_ENV = ThreadLocal.withInitial(() ->
    {
        return new RenderEnv(Blocks.AIR.defaultBlockState(), new BlockPos(0, 0, 0));
    });
    boolean FORGE = Reflector.ForgeHooksClient.exists();

default RenderEnv getRenderEnv(BlockState blockState, BlockPos blockPos)
    {
        RenderEnv renderenv = RENDER_ENV.get();
        renderenv.reset(blockState, blockPos);
        return renderenv;
    }

    VertexConsumer vertex(double pX, double p_85946_, double pY);

    VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha);

    VertexConsumer uv(float pU, float pV);

    VertexConsumer overlayCoords(int pOverlayUV, int p_85972_);

    VertexConsumer uv2(int pLightmapUV, int p_86011_);

    VertexConsumer normal(float pX, float pY, float pZ);

    void endVertex();

default void vertex(float pX, float p_85956_, float pY, float p_85958_, float pZ, float p_85960_, float p_85961_, float p_85962_, float p_85963_, int p_85964_, int p_85965_, float p_85966_, float p_85967_, float p_85968_)
    {
        this.vertex((double)pX, (double)p_85956_, (double)pY);
        this.color(p_85958_, pZ, p_85960_, p_85961_);
        this.uv(p_85962_, p_85963_);
        this.overlayCoords(p_85964_);
        this.uv2(p_85965_);
        this.normal(p_85966_, p_85967_, p_85968_);
        this.endVertex();
    }

    void defaultColor(int p_166901_, int p_166902_, int p_166903_, int p_166904_);

    void unsetDefaultColor();

default VertexConsumer color(float pRed, float pGreen, float pBlue, float pAlpha)
    {
        return this.color((int)(pRed * 255.0F), (int)(pGreen * 255.0F), (int)(pBlue * 255.0F), (int)(pAlpha * 255.0F));
    }

default VertexConsumer uv2(int pLightmapUV)
    {
        return this.uv2(pLightmapUV & 65535, pLightmapUV >> 16 & 65535);
    }

default VertexConsumer overlayCoords(int pOverlayUV)
    {
        return this.overlayCoords(pOverlayUV & 65535, pOverlayUV >> 16 & 65535);
    }

default void putBulkData(PoseStack.Pose pMatrixEntry, BakedQuad pQuad, float pRed, float pGreen, float pBlue, int pCombinedLight, int pCombinedOverlay)
    {
        this.m_85995_(pMatrixEntry, pQuad, this.getTempFloat4(1.0F, 1.0F, 1.0F, 1.0F), pRed, pGreen, pBlue, this.getTempInt4(pCombinedLight, pCombinedLight, pCombinedLight, pCombinedLight), pCombinedOverlay, false);
    }

default void putBulkData(PoseStack.Pose matrixEntry, BakedQuad bakedQuad, float[] baseBrightness, float red, float green, float blue, float alpha, int[] lightmapCoords, int overlayCoords, boolean readExistingColor)
    {
        this.addQuad(matrixEntry, bakedQuad, baseBrightness, red, green, blue, alpha, lightmapCoords, overlayCoords, readExistingColor);
    }

default void m_85995_(PoseStack.Pose p_85996_, BakedQuad p_85997_, float[] p_85998_, float p_85999_, float p_86000_, float p_86001_, int[] p_86002_, int p_86003_, boolean p_86004_)
    {
        this.addQuad(p_85996_, p_85997_, p_85998_, p_85999_, p_86000_, p_86001_, 1.0F, p_86002_, p_86003_, p_86004_);
    }

default void addQuad(PoseStack.Pose matrixEntryIn, BakedQuad quadIn, float[] colorMuls, float redIn, float greenIn, float blueIn, float alphaIn, int[] combinedLightsIn, int combinedOverlayIn, boolean mulColor)
    {
        float[] afloat = colorMuls;
        int[] aint = combinedLightsIn;
        int[] aint1 = this.isMultiTexture() ? quadIn.getVertexDataSingle() : quadIn.getVertices();
        this.putSprite(quadIn.getSprite());
        boolean flag = ModelBlockRenderer.isSeparateAoLightValue();
        Vec3i vec3i = quadIn.getDirection().getNormal();
        float f = (float)vec3i.getX();
        float f1 = (float)vec3i.getY();
        float f2 = (float)vec3i.getZ();
        Matrix4f matrix4f = matrixEntryIn.pose();
        Matrix3f matrix3f = matrixEntryIn.normal();
        float f3 = matrix3f.getTransformX(f, f1, f2);
        float f4 = matrix3f.getTransformY(f, f1, f2);
        float f5 = matrix3f.getTransformZ(f, f1, f2);
        int i = 8;
        int j = DefaultVertexFormat.BLOCK.getIntegerSize();
        int k = aint1.length / j;
        boolean flag1 = Config.isShaders() && Shaders.useVelocityAttrib && Config.isMinecraftThread();

        if (flag1)
        {
            IRandomEntity irandomentity = RandomEntities.getRandomEntityRendered();

            if (irandomentity != null)
            {
                VertexPosition[] avertexposition = quadIn.getVertexPositions(irandomentity.getId());
                this.setQuadVertexPositions(avertexposition);
            }
        }

        for (int i1 = 0; i1 < k; ++i1)
        {
            int j1 = i1 * j;
            float f6 = Float.intBitsToFloat(aint1[j1 + 0]);
            float f7 = Float.intBitsToFloat(aint1[j1 + 1]);
            float f8 = Float.intBitsToFloat(aint1[j1 + 2]);
            float f12 = 1.0F;
            float f13 = flag ? 1.0F : afloat[i1];
            float f9;
            float f10;
            float f11;

            if (mulColor)
            {
                int l = aint1[j1 + 3];
                float f14 = (float)(l & 255) / 255.0F;
                float f15 = (float)(l >> 8 & 255) / 255.0F;
                float f16 = (float)(l >> 16 & 255) / 255.0F;
                f9 = f14 * f13 * redIn;
                f10 = f15 * f13 * greenIn;
                f11 = f16 * f13 * blueIn;

                if (FORGE)
                {
                    float f17 = (float)(l >> 24 & 255) / 255.0F;
                    f12 = f17 * alphaIn;
                }
            }
            else
            {
                f9 = f13 * redIn;
                f10 = f13 * greenIn;
                f11 = f13 * blueIn;

                if (FORGE)
                {
                    f12 = alphaIn;
                }
            }

            int k1 = aint[i1];

            if (FORGE)
            {
                k1 = this.applyBakedLighting(aint[i1], aint1, j1);
            }

            float f19 = Float.intBitsToFloat(aint1[j1 + 4]);
            float f20 = Float.intBitsToFloat(aint1[j1 + 5]);
            float f21 = matrix4f.getTransformX(f6, f7, f8, 1.0F);
            float f22 = matrix4f.getTransformY(f6, f7, f8, 1.0F);
            float f18 = matrix4f.getTransformZ(f6, f7, f8, 1.0F);

            if (FORGE)
            {
                Vector3f vector3f = this.applyBakedNormals(aint1, j1, matrixEntryIn.normal());

                if (vector3f != null)
                {
                    f3 = vector3f.x();
                    f4 = vector3f.y();
                    f5 = vector3f.z();
                }
            }

            if (flag)
            {
                f12 = afloat[i1];
            }

            this.vertex(f21, f22, f18, f9, f10, f11, f12, f19, f20, combinedOverlayIn, k1, f3, f4, f5);
        }
    }

default VertexConsumer vertex(Matrix4f pX, float p_85984_, float pY, float p_85986_)
    {
        float f = pX.getTransformX(p_85984_, pY, p_85986_, 1.0F);
        float f1 = pX.getTransformY(p_85984_, pY, p_85986_, 1.0F);
        float f2 = pX.getTransformZ(p_85984_, pY, p_85986_, 1.0F);
        return this.vertex((double)f, (double)f1, (double)f2);
    }

default VertexConsumer normal(Matrix3f pX, float pY, float pZ, float p_85981_)
    {
        float f = pX.getTransformX(pY, pZ, p_85981_);
        float f1 = pX.getTransformY(pY, pZ, p_85981_);
        float f2 = pX.getTransformZ(pY, pZ, p_85981_);
        return this.normal(f, f1, f2);
    }

default void putSprite(TextureAtlasSprite sprite)
    {
    }

default void setSprite(TextureAtlasSprite sprite)
    {
    }

default boolean isMultiTexture()
    {
        return false;
    }

default void setRenderType(RenderType layer)
    {
    }

default RenderType getRenderType()
    {
        return null;
    }

default void setRenderBlocks(boolean renderBlocks)
    {
    }

default Vector3f getTempVec3f(Vector3f vec)
    {
        return vec.copy();
    }

default Vector3f getTempVec3f(float x, float y, float z)
    {
        return new Vector3f(x, y, z);
    }

default float[] getTempFloat4(float f1, float f2, float f3, float f4)
    {
        return new float[] {f1, f2, f3, f4};
    }

default int[] getTempInt4(int i1, int i2, int i3, int i4)
    {
        return new int[] {i1, i2, i3, i4};
    }

default MultiBufferSource.BufferSource getRenderTypeBuffer()
    {
        return null;
    }

default void setQuadVertexPositions(VertexPosition[] vps)
    {
    }

default void setMidBlock(float mbx, float mby, float mbz)
    {
    }

default VertexConsumer getSecondaryBuilder()
    {
        return null;
    }

default int applyBakedLighting(int lightmapCoord, int[] data, int pos)
    {
        int i = getLightOffset(0);
        int j = LightTexture.block(data[pos + i]);
        int k = LightTexture.sky(data[pos + i]);

        if (j == 0 && k == 0)
        {
            return lightmapCoord;
        }
        else
        {
            int l = LightTexture.block(lightmapCoord);
            int i1 = LightTexture.sky(lightmapCoord);
            l = Math.max(l, j);
            i1 = Math.max(i1, k);
            return LightTexture.pack(l, i1);
        }
    }

    static int getLightOffset(int v)
    {
        return v * 8 + 6;
    }

default Vector3f applyBakedNormals(int[] data, int pos, Matrix3f normalTransform)
    {
        int i = 7;
        int j = data[pos + i];
        byte b0 = (byte)(j >> 0 & 255);
        byte b1 = (byte)(j >> 8 & 255);
        byte b2 = (byte)(j >> 16 & 255);

        if (b0 == 0 && b1 == 0 && b2 == 0)
        {
            return null;
        }
        else
        {
            Vector3f vector3f = this.getTempVec3f((float)b0 / 127.0F, (float)b1 / 127.0F, (float)b2 / 127.0F);
            vector3f.transform(normalTransform);
            return vector3f;
        }
    }
}
