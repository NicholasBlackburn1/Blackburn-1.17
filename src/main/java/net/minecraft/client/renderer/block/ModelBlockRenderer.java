package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforgeop.client.model.data.EmptyModelData;
import net.minecraftforgeop.client.model.data.IModelData;
import net.optifine.BetterSnow;
import net.optifine.BlockPosM;
import net.optifine.Config;
import net.optifine.CustomColors;
import net.optifine.EmissiveTextures;
import net.optifine.model.BlockModelCustomizer;
import net.optifine.model.ListQuadsOverlay;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorForge;
import net.optifine.render.LightCacheOF;
import net.optifine.render.RenderEnv;
import net.optifine.render.RenderTypes;
import net.optifine.shaders.SVertexBuilder;
import net.optifine.shaders.Shaders;
import net.optifine.util.BlockUtils;

public class ModelBlockRenderer
{
    private static final int FACE_CUBIC = 0;
    private static final int FACE_PARTIAL = 1;
    static final Direction[] DIRECTIONS = Direction.values();
    private final BlockColors blockColors;
    private static final int CACHE_SIZE = 100;
    static final ThreadLocal<ModelBlockRenderer.Cache> CACHE = ThreadLocal.withInitial(() ->
    {
        return new ModelBlockRenderer.Cache();
    });
    private static float aoLightValueOpaque = 0.2F;
    private static boolean separateAoLightValue = false;
    private static final LightCacheOF LIGHT_CACHE_OF = new LightCacheOF();
    private static final RenderType[] OVERLAY_LAYERS = new RenderType[] {RenderTypes.CUTOUT, RenderTypes.CUTOUT_MIPPED, RenderTypes.TRANSLUCENT};
    private boolean forgeModelData = Reflector.ForgeHooksClient.exists();

    public ModelBlockRenderer(BlockColors p_110999_)
    {
        this.blockColors = p_110999_;
    }

    public boolean tesselateBlock(BlockAndTintGetter pLevel, BakedModel pModel, BlockState pState, BlockPos pPos, PoseStack pMatrix, VertexConsumer pBuffer, boolean pCheckSides, Random pRandom, long pRand, int p_111057_)
    {
        return this.tesselateBlock(pLevel, pModel, pState, pPos, pMatrix, pBuffer, pCheckSides, pRandom, pRand, p_111057_, EmptyModelData.INSTANCE);
    }

    public boolean tesselateBlock(BlockAndTintGetter worldIn, BakedModel modelIn, BlockState stateIn, BlockPos posIn, PoseStack matrixIn, VertexConsumer buffer, boolean checkSides, Random randomIn, long rand, int combinedOverlayIn, IModelData modelData)
    {
        boolean flag = Minecraft.useAmbientOcclusion() && ReflectorForge.getLightValue(stateIn, worldIn, posIn) == 0 && modelIn.useAmbientOcclusion();

        if (this.forgeModelData)
        {
            modelData = modelIn.getModelData(worldIn, posIn, stateIn, modelData);
        }

        Vec3 vec3 = stateIn.getOffset(worldIn, posIn);
        matrixIn.translate(vec3.x, vec3.y, vec3.z);

        try
        {
            if (Config.isShaders())
            {
                SVertexBuilder.pushEntity(stateIn, buffer);
            }

            if (!Config.isAlternateBlocks())
            {
                rand = 0L;
            }

            RenderEnv renderenv = buffer.getRenderEnv(stateIn, posIn);
            modelIn = BlockModelCustomizer.getRenderModel(modelIn, stateIn, renderenv);
            boolean flag1 = flag ? this.renderModelSmooth(worldIn, modelIn, stateIn, posIn, matrixIn, buffer, checkSides, randomIn, rand, combinedOverlayIn, modelData) : this.renderModelFlat(worldIn, modelIn, stateIn, posIn, matrixIn, buffer, checkSides, randomIn, rand, combinedOverlayIn, modelData);

            if (flag1)
            {
                this.renderOverlayModels(worldIn, modelIn, stateIn, posIn, matrixIn, buffer, combinedOverlayIn, checkSides, randomIn, rand, renderenv, flag, vec3);
            }

            if (Config.isShaders())
            {
                SVertexBuilder.popEntity(buffer);
            }

            return flag1;
        }
        catch (Throwable throwable1)
        {
            CrashReport crashreport = CrashReport.forThrowable(throwable1, "Tesselating block model");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Block model being tesselated");
            CrashReportCategory.populateBlockDetails(crashreportcategory, worldIn, posIn, stateIn);
            crashreportcategory.setDetail("Using AO", flag);
            throw new ReportedException(crashreport);
        }
    }

    public boolean tesselateWithAO(BlockAndTintGetter pLevel, BakedModel pModel, BlockState pState, BlockPos pPos, PoseStack pMatrixStack, VertexConsumer pBuffer, boolean pCheckSides, Random pRandom, long pRand, int p_111088_)
    {
        return this.renderModelSmooth(pLevel, pModel, pState, pPos, pMatrixStack, pBuffer, pCheckSides, pRandom, pRand, p_111088_, EmptyModelData.INSTANCE);
    }

    public boolean renderModelSmooth(BlockAndTintGetter worldIn, BakedModel modelIn, BlockState stateIn, BlockPos posIn, PoseStack matrixStackIn, VertexConsumer buffer, boolean checkSides, Random randomIn, long rand, int combinedOverlayIn, IModelData modelData)
    {
        boolean flag = false;
        RenderEnv renderenv = buffer.getRenderEnv(stateIn, posIn);
        RenderType rendertype = buffer.getRenderType();

        for (Direction direction : DIRECTIONS)
        {
            if (!checkSides || BlockUtils.shouldSideBeRendered(stateIn, worldIn, posIn, direction, renderenv))
            {
                randomIn.setSeed(rand);
                List<BakedQuad> list = this.forgeModelData ? modelIn.getQuads(stateIn, direction, randomIn, modelData) : modelIn.getQuads(stateIn, direction, randomIn);
                list = BlockModelCustomizer.getRenderQuads(list, worldIn, stateIn, posIn, direction, rendertype, rand, renderenv);

                if (!list.isEmpty())
                {
                    this.renderQuadsSmooth(worldIn, stateIn, posIn, matrixStackIn, buffer, list, combinedOverlayIn, renderenv);
                    flag = true;
                }
            }
        }

        randomIn.setSeed(rand);
        List<BakedQuad> list1 = this.forgeModelData ? modelIn.getQuads(stateIn, (Direction)null, randomIn, modelData) : modelIn.getQuads(stateIn, (Direction)null, randomIn);

        if (!list1.isEmpty())
        {
            list1 = BlockModelCustomizer.getRenderQuads(list1, worldIn, stateIn, posIn, (Direction)null, rendertype, rand, renderenv);
            this.renderQuadsSmooth(worldIn, stateIn, posIn, matrixStackIn, buffer, list1, combinedOverlayIn, renderenv);
            flag = true;
        }

        return flag;
    }

    public boolean tesselateWithoutAO(BlockAndTintGetter pLevel, BakedModel pModel, BlockState pState, BlockPos pPos, PoseStack pMatrixStack, VertexConsumer pBuffer, boolean pCheckSides, Random pRandom, long pRand, int p_111100_)
    {
        return this.renderModelFlat(pLevel, pModel, pState, pPos, pMatrixStack, pBuffer, pCheckSides, pRandom, pRand, p_111100_, EmptyModelData.INSTANCE);
    }

    public boolean renderModelFlat(BlockAndTintGetter worldIn, BakedModel modelIn, BlockState stateIn, BlockPos posIn, PoseStack matrixStackIn, VertexConsumer buffer, boolean checkSides, Random randomIn, long rand, int combinedOverlayIn, IModelData modelData)
    {
        boolean flag = false;
        RenderEnv renderenv = buffer.getRenderEnv(stateIn, posIn);
        RenderType rendertype = buffer.getRenderType();

        for (Direction direction : DIRECTIONS)
        {
            if (!checkSides || BlockUtils.shouldSideBeRendered(stateIn, worldIn, posIn, direction, renderenv))
            {
                randomIn.setSeed(rand);
                List<BakedQuad> list = this.forgeModelData ? modelIn.getQuads(stateIn, direction, randomIn, modelData) : modelIn.getQuads(stateIn, direction, randomIn);
                list = BlockModelCustomizer.getRenderQuads(list, worldIn, stateIn, posIn, direction, rendertype, rand, renderenv);

                if (!list.isEmpty())
                {
                    BlockPos.MutableBlockPos blockpos$mutableblockpos = renderenv.getRenderMutableBlockPos();
                    blockpos$mutableblockpos.setWithOffset(posIn, direction);
                    int i = LevelRenderer.getLightColor(worldIn, stateIn, blockpos$mutableblockpos);
                    this.renderQuadsFlat(worldIn, stateIn, posIn, i, combinedOverlayIn, false, matrixStackIn, buffer, list, renderenv);
                    flag = true;
                }
            }
        }

        randomIn.setSeed(rand);
        List<BakedQuad> list1 = this.forgeModelData ? modelIn.getQuads(stateIn, (Direction)null, randomIn, modelData) : modelIn.getQuads(stateIn, (Direction)null, randomIn);

        if (!list1.isEmpty())
        {
            list1 = BlockModelCustomizer.getRenderQuads(list1, worldIn, stateIn, posIn, (Direction)null, rendertype, rand, renderenv);
            this.renderQuadsFlat(worldIn, stateIn, posIn, -1, combinedOverlayIn, true, matrixStackIn, buffer, list1, renderenv);
            flag = true;
        }

        return flag;
    }

    private void renderQuadsSmooth(BlockAndTintGetter blockAccessIn, BlockState stateIn, BlockPos posIn, PoseStack matrixStackIn, VertexConsumer buffer, List<BakedQuad> list, int combinedOverlayIn, RenderEnv renderEnv)
    {
        float[] afloat = renderEnv.getQuadBounds();
        BitSet bitset = renderEnv.getBoundsFlags();
        ModelBlockRenderer.AmbientOcclusionFace modelblockrenderer$ambientocclusionface = renderEnv.getAoFace();
        int i = list.size();

        for (int j = 0; j < i; ++j)
        {
            BakedQuad bakedquad = list.get(j);
            this.m_111039_(blockAccessIn, stateIn, posIn, bakedquad.getVertices(), bakedquad.getDirection(), afloat, bitset);
            modelblockrenderer$ambientocclusionface.m_111167_(blockAccessIn, stateIn, posIn, bakedquad.getDirection(), afloat, bitset, bakedquad.isShade());

            if (bakedquad.getSprite().isSpriteEmissive)
            {
                modelblockrenderer$ambientocclusionface.setMaxBlockLight();
            }

            this.renderQuadSmooth(blockAccessIn, stateIn, posIn, buffer, matrixStackIn.last(), bakedquad, modelblockrenderer$ambientocclusionface.brightness[0], modelblockrenderer$ambientocclusionface.brightness[1], modelblockrenderer$ambientocclusionface.brightness[2], modelblockrenderer$ambientocclusionface.brightness[3], modelblockrenderer$ambientocclusionface.lightmap[0], modelblockrenderer$ambientocclusionface.lightmap[1], modelblockrenderer$ambientocclusionface.lightmap[2], modelblockrenderer$ambientocclusionface.lightmap[3], combinedOverlayIn, renderEnv);
        }
    }

    private void renderQuadSmooth(BlockAndTintGetter blockAccessIn, BlockState stateIn, BlockPos posIn, VertexConsumer buffer, PoseStack.Pose matrixEntry, BakedQuad quadIn, float colorMul0, float colorMul1, float colorMul2, float colorMul3, int brightness0, int brightness1, int brightness2, int brightness3, int combinedOverlayIn, RenderEnv renderEnv)
    {
        int i = CustomColors.getColorMultiplier(quadIn, stateIn, blockAccessIn, posIn, renderEnv);
        float f;
        float f1;
        float f2;

        if (!quadIn.isTinted() && i == -1)
        {
            f = 1.0F;
            f1 = 1.0F;
            f2 = 1.0F;
        }
        else
        {
            int j = i != -1 ? i : this.blockColors.getColor(stateIn, blockAccessIn, posIn, quadIn.getTintIndex());
            f = (float)(j >> 16 & 255) / 255.0F;
            f1 = (float)(j >> 8 & 255) / 255.0F;
            f2 = (float)(j & 255) / 255.0F;
        }

        buffer.m_85995_(matrixEntry, quadIn, buffer.getTempFloat4(colorMul0, colorMul1, colorMul2, colorMul3), f, f1, f2, buffer.getTempInt4(brightness0, brightness1, brightness2, brightness3), combinedOverlayIn, true);
    }

    private void m_111039_(BlockAndTintGetter p_111040_, BlockState p_111041_, BlockPos p_111042_, int[] p_111043_, Direction p_111044_, @Nullable float[] p_111045_, BitSet p_111046_)
    {
        float f = 32.0F;
        float f1 = 32.0F;
        float f2 = 32.0F;
        float f3 = -32.0F;
        float f4 = -32.0F;
        float f5 = -32.0F;
        int i = p_111043_.length / 4;

        for (int j = 0; j < 4; ++j)
        {
            float f6 = Float.intBitsToFloat(p_111043_[j * i]);
            float f7 = Float.intBitsToFloat(p_111043_[j * i + 1]);
            float f8 = Float.intBitsToFloat(p_111043_[j * i + 2]);
            f = Math.min(f, f6);
            f1 = Math.min(f1, f7);
            f2 = Math.min(f2, f8);
            f3 = Math.max(f3, f6);
            f4 = Math.max(f4, f7);
            f5 = Math.max(f5, f8);
        }

        if (p_111045_ != null)
        {
            p_111045_[Direction.WEST.get3DDataValue()] = f;
            p_111045_[Direction.EAST.get3DDataValue()] = f3;
            p_111045_[Direction.DOWN.get3DDataValue()] = f1;
            p_111045_[Direction.UP.get3DDataValue()] = f4;
            p_111045_[Direction.NORTH.get3DDataValue()] = f2;
            p_111045_[Direction.SOUTH.get3DDataValue()] = f5;
            int k = DIRECTIONS.length;
            p_111045_[Direction.WEST.get3DDataValue() + k] = 1.0F - f;
            p_111045_[Direction.EAST.get3DDataValue() + k] = 1.0F - f3;
            p_111045_[Direction.DOWN.get3DDataValue() + k] = 1.0F - f1;
            p_111045_[Direction.UP.get3DDataValue() + k] = 1.0F - f4;
            p_111045_[Direction.NORTH.get3DDataValue() + k] = 1.0F - f2;
            p_111045_[Direction.SOUTH.get3DDataValue() + k] = 1.0F - f5;
        }

        float f9 = 1.0E-4F;
        float f10 = 0.9999F;

        switch (p_111044_)
        {
            case DOWN:
                p_111046_.set(1, f >= 1.0E-4F || f2 >= 1.0E-4F || f3 <= 0.9999F || f5 <= 0.9999F);
                p_111046_.set(0, f1 == f4 && (f1 < 1.0E-4F || p_111041_.isCollisionShapeFullBlock(p_111040_, p_111042_)));
                break;

            case UP:
                p_111046_.set(1, f >= 1.0E-4F || f2 >= 1.0E-4F || f3 <= 0.9999F || f5 <= 0.9999F);
                p_111046_.set(0, f1 == f4 && (f4 > 0.9999F || p_111041_.isCollisionShapeFullBlock(p_111040_, p_111042_)));
                break;

            case NORTH:
                p_111046_.set(1, f >= 1.0E-4F || f1 >= 1.0E-4F || f3 <= 0.9999F || f4 <= 0.9999F);
                p_111046_.set(0, f2 == f5 && (f2 < 1.0E-4F || p_111041_.isCollisionShapeFullBlock(p_111040_, p_111042_)));
                break;

            case SOUTH:
                p_111046_.set(1, f >= 1.0E-4F || f1 >= 1.0E-4F || f3 <= 0.9999F || f4 <= 0.9999F);
                p_111046_.set(0, f2 == f5 && (f5 > 0.9999F || p_111041_.isCollisionShapeFullBlock(p_111040_, p_111042_)));
                break;

            case WEST:
                p_111046_.set(1, f1 >= 1.0E-4F || f2 >= 1.0E-4F || f4 <= 0.9999F || f5 <= 0.9999F);
                p_111046_.set(0, f == f3 && (f < 1.0E-4F || p_111041_.isCollisionShapeFullBlock(p_111040_, p_111042_)));
                break;

            case EAST:
                p_111046_.set(1, f1 >= 1.0E-4F || f2 >= 1.0E-4F || f4 <= 0.9999F || f5 <= 0.9999F);
                p_111046_.set(0, f == f3 && (f3 > 0.9999F || p_111041_.isCollisionShapeFullBlock(p_111040_, p_111042_)));
        }
    }

    private void renderQuadsFlat(BlockAndTintGetter blockAccessIn, BlockState stateIn, BlockPos posIn, int brightnessIn, int combinedOverlayIn, boolean ownBrightness, PoseStack matrixStackIn, VertexConsumer buffer, List<BakedQuad> list, RenderEnv renderEnv)
    {
        BitSet bitset = renderEnv.getBoundsFlags();
        int i = list.size();

        for (int j = 0; j < i; ++j)
        {
            BakedQuad bakedquad = list.get(j);

            if (ownBrightness)
            {
                this.m_111039_(blockAccessIn, stateIn, posIn, bakedquad.getVertices(), bakedquad.getDirection(), (float[])null, bitset);
                BlockPos blockpos = bitset.get(0) ? posIn.relative(bakedquad.getDirection()) : posIn;
                brightnessIn = LevelRenderer.getLightColor(blockAccessIn, stateIn, blockpos);
            }

            if (bakedquad.getSprite().isSpriteEmissive)
            {
                brightnessIn = LightTexture.MAX_BRIGHTNESS;
            }

            float f = blockAccessIn.getShade(bakedquad.getDirection(), bakedquad.isShade());
            this.renderQuadSmooth(blockAccessIn, stateIn, posIn, buffer, matrixStackIn.last(), bakedquad, f, f, f, f, brightnessIn, brightnessIn, brightnessIn, brightnessIn, combinedOverlayIn, renderEnv);
        }
    }

    public void renderModel(PoseStack.Pose pMatrixEntry, VertexConsumer pBuffer, @Nullable BlockState pState, BakedModel pModel, float pRed, float pGreen, float pBlue, int pCombinedLight, int pCombinedOverlay)
    {
        this.renderModel(pMatrixEntry, pBuffer, pState, pModel, pRed, pGreen, pBlue, pCombinedLight, pCombinedOverlay, EmptyModelData.INSTANCE);
    }

    public void renderModel(PoseStack.Pose matrixEntry, VertexConsumer buffer, @Nullable BlockState state, BakedModel modelIn, float red, float green, float blue, int combinedLightIn, int combinedOverlayIn, IModelData modelData)
    {
        Random random = new Random();
        long i = 42L;

        for (Direction direction : DIRECTIONS)
        {
            random.setSeed(42L);

            if (this.forgeModelData)
            {
                renderQuadList(matrixEntry, buffer, red, green, blue, modelIn.getQuads(state, direction, random, modelData), combinedLightIn, combinedOverlayIn);
            }
            else
            {
                renderQuadList(matrixEntry, buffer, red, green, blue, modelIn.getQuads(state, direction, random), combinedLightIn, combinedOverlayIn);
            }
        }

        random.setSeed(42L);

        if (this.forgeModelData)
        {
            renderQuadList(matrixEntry, buffer, red, green, blue, modelIn.getQuads(state, (Direction)null, random, modelData), combinedLightIn, combinedOverlayIn);
        }
        else
        {
            renderQuadList(matrixEntry, buffer, red, green, blue, modelIn.getQuads(state, (Direction)null, random), combinedLightIn, combinedOverlayIn);
        }
    }

    private static void renderQuadList(PoseStack.Pose pMatrixEntry, VertexConsumer pBuffer, float pRed, float pGreen, float pBlue, List<BakedQuad> pListQuads, int pCombinedLight, int pCombinedOverlay)
    {
        boolean flag = EmissiveTextures.isActive();
        Iterator iterator = pListQuads.iterator();

        while (true)
        {
            BakedQuad bakedquad;

            do
            {
                if (!iterator.hasNext())
                {
                    return;
                }

                bakedquad = (BakedQuad)iterator.next();

                if (!flag)
                {
                    break;
                }

                bakedquad = EmissiveTextures.getEmissiveQuad(bakedquad);
            }
            while (bakedquad == null);

            float f;
            float f1;
            float f2;

            if (bakedquad.isTinted())
            {
                f = Mth.clamp(pRed, 0.0F, 1.0F);
                f1 = Mth.clamp(pGreen, 0.0F, 1.0F);
                f2 = Mth.clamp(pBlue, 0.0F, 1.0F);
            }
            else
            {
                f = 1.0F;
                f1 = 1.0F;
                f2 = 1.0F;
            }

            pBuffer.putBulkData(pMatrixEntry, bakedquad, f, f1, f2, pCombinedLight, pCombinedOverlay);
        }
    }

    public static void enableCaching()
    {
        CACHE.get().enable();
    }

    public static void clearCache()
    {
        CACHE.get().disable();
    }

    public static float fixAoLightValue(float val)
    {
        return val == 0.2F ? aoLightValueOpaque : val;
    }

    public static void updateAoLightValue()
    {
        aoLightValueOpaque = 1.0F - Config.getAmbientOcclusionLevel() * 0.8F;
        separateAoLightValue = Config.isShaders() && Shaders.isSeparateAo();
    }

    public static boolean isSeparateAoLightValue()
    {
        return separateAoLightValue;
    }

    private void renderOverlayModels(BlockAndTintGetter worldIn, BakedModel modelIn, BlockState stateIn, BlockPos posIn, PoseStack matrixStackIn, VertexConsumer buffer, int combinedOverlayIn, boolean checkSides, Random random, long rand, RenderEnv renderEnv, boolean smooth, Vec3 renderOffset)
    {
        if (renderEnv.isOverlaysRendered())
        {
            for (int i = 0; i < OVERLAY_LAYERS.length; ++i)
            {
                RenderType rendertype = OVERLAY_LAYERS[i];
                ListQuadsOverlay listquadsoverlay = renderEnv.getListQuadsOverlay(rendertype);

                if (listquadsoverlay.size() > 0)
                {
                    ChunkBufferBuilderPack chunkbufferbuilderpack = renderEnv.getRegionRenderCacheBuilder();

                    if (chunkbufferbuilderpack != null)
                    {
                        BufferBuilder bufferbuilder = chunkbufferbuilderpack.builder(rendertype);

                        if (!bufferbuilder.building())
                        {
                            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
                        }

                        for (int j = 0; j < listquadsoverlay.size(); ++j)
                        {
                            BakedQuad bakedquad = listquadsoverlay.getQuad(j);
                            List<BakedQuad> list = listquadsoverlay.getListQuadsSingle(bakedquad);
                            BlockState blockstate = listquadsoverlay.getBlockState(j);

                            if (bakedquad.getQuadEmissive() != null)
                            {
                                listquadsoverlay.addQuad(bakedquad.getQuadEmissive(), blockstate);
                            }

                            renderEnv.reset(blockstate, posIn);

                            if (smooth)
                            {
                                this.renderQuadsSmooth(worldIn, blockstate, posIn, matrixStackIn, bufferbuilder, list, combinedOverlayIn, renderEnv);
                            }
                            else
                            {
                                int k = LevelRenderer.getLightColor(worldIn, blockstate, posIn.relative(bakedquad.getDirection()));
                                this.renderQuadsFlat(worldIn, blockstate, posIn, k, combinedOverlayIn, false, matrixStackIn, bufferbuilder, list, renderEnv);
                            }
                        }
                    }

                    listquadsoverlay.clear();
                }
            }
        }

        if (Config.isBetterSnow() && !renderEnv.isBreakingAnimation() && BetterSnow.shouldRender(worldIn, stateIn, posIn))
        {
            BakedModel bakedmodel = BetterSnow.getModelSnowLayer();
            BlockState blockstate1 = BetterSnow.getStateSnowLayer();
            matrixStackIn.translate(-renderOffset.x, -renderOffset.y, -renderOffset.z);
            this.tesselateBlock(worldIn, bakedmodel, blockstate1, posIn, matrixStackIn, buffer, checkSides, random, rand, combinedOverlayIn);
        }
    }

    protected static enum AdjacencyInfo
    {
        DOWN(new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH}, 0.5F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.SOUTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.SOUTH}),
        UP(new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}, 1.0F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.SOUTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.SOUTH}),
        NORTH(new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST}, 0.8F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_WEST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_EAST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_EAST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_WEST}),
        SOUTH(new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP}, 0.8F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.WEST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_WEST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.WEST, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.WEST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.EAST}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_EAST, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.EAST, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.EAST}),
        WEST(new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH}, 0.6F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.SOUTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.SOUTH}),
        EAST(new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH}, 0.6F, true, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.SOUTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.FLIP_DOWN, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.DOWN, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.NORTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_NORTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.NORTH}, new ModelBlockRenderer.SizeInfo[]{ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.SOUTH, ModelBlockRenderer.SizeInfo.FLIP_UP, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.FLIP_SOUTH, ModelBlockRenderer.SizeInfo.UP, ModelBlockRenderer.SizeInfo.SOUTH});

        final Direction[] corners;
        final boolean doNonCubicWeight;
        final ModelBlockRenderer.SizeInfo[] vert0Weights;
        final ModelBlockRenderer.SizeInfo[] vert1Weights;
        final ModelBlockRenderer.SizeInfo[] vert2Weights;
        final ModelBlockRenderer.SizeInfo[] vert3Weights;
        private static final ModelBlockRenderer.AdjacencyInfo[] BY_FACING = Util.make(new ModelBlockRenderer.AdjacencyInfo[6], (infoIn) -> {
            infoIn[Direction.DOWN.get3DDataValue()] = DOWN;
            infoIn[Direction.UP.get3DDataValue()] = UP;
            infoIn[Direction.NORTH.get3DDataValue()] = NORTH;
            infoIn[Direction.SOUTH.get3DDataValue()] = SOUTH;
            infoIn[Direction.WEST.get3DDataValue()] = WEST;
            infoIn[Direction.EAST.get3DDataValue()] = EAST;
        });

        private AdjacencyInfo(Direction[] p_111122_, float p_111123_, boolean p_111124_, ModelBlockRenderer.SizeInfo[] p_111125_, ModelBlockRenderer.SizeInfo[] p_111126_, ModelBlockRenderer.SizeInfo[] p_111127_, ModelBlockRenderer.SizeInfo[] p_111128_)
        {
            this.corners = p_111122_;
            this.doNonCubicWeight = p_111124_;
            this.vert0Weights = p_111125_;
            this.vert1Weights = p_111126_;
            this.vert2Weights = p_111127_;
            this.vert3Weights = p_111128_;
        }

        public static ModelBlockRenderer.AdjacencyInfo fromFacing(Direction pFacing)
        {
            return BY_FACING[pFacing.get3DDataValue()];
        }
    }

    public static class AmbientOcclusionFace
    {
        final float[] brightness = new float[4];
        final int[] lightmap = new int[4];
        private BlockPosM blockPos = new BlockPosM();

        public AmbientOcclusionFace()
        {
            this((ModelBlockRenderer)null);
        }

        public AmbientOcclusionFace(ModelBlockRenderer p_111152_)
        {
        }

        public void setMaxBlockLight()
        {
            int i = LightTexture.MAX_BRIGHTNESS;
            this.lightmap[0] = i;
            this.lightmap[1] = i;
            this.lightmap[2] = i;
            this.lightmap[3] = i;
            this.brightness[0] = 1.0F;
            this.brightness[1] = 1.0F;
            this.brightness[2] = 1.0F;
            this.brightness[3] = 1.0F;
        }

        public void m_111167_(BlockAndTintGetter p_111168_, BlockState p_111169_, BlockPos p_111170_, Direction p_111171_, float[] p_111172_, BitSet p_111173_, boolean p_111174_)
        {
            BlockPos blockpos = p_111173_.get(0) ? p_111170_.relative(p_111171_) : p_111170_;
            ModelBlockRenderer.AdjacencyInfo modelblockrenderer$adjacencyinfo = ModelBlockRenderer.AdjacencyInfo.fromFacing(p_111171_);
            BlockPosM blockposm = this.blockPos;
            LightCacheOF lightcacheof = ModelBlockRenderer.LIGHT_CACHE_OF;
            blockposm.setPosOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[0]);
            BlockState blockstate = p_111168_.getBlockState(blockposm);
            int i = LightCacheOF.getPackedLight(blockstate, p_111168_, blockposm);
            float f = LightCacheOF.getBrightness(blockstate, p_111168_, blockposm);
            blockposm.setPosOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[1]);
            BlockState blockstate1 = p_111168_.getBlockState(blockposm);
            int j = LightCacheOF.getPackedLight(blockstate1, p_111168_, blockposm);
            float f1 = LightCacheOF.getBrightness(blockstate1, p_111168_, blockposm);
            blockposm.setPosOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[2]);
            BlockState blockstate2 = p_111168_.getBlockState(blockposm);
            int k = LightCacheOF.getPackedLight(blockstate2, p_111168_, blockposm);
            float f2 = LightCacheOF.getBrightness(blockstate2, p_111168_, blockposm);
            blockposm.setPosOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[3]);
            BlockState blockstate3 = p_111168_.getBlockState(blockposm);
            int l = LightCacheOF.getPackedLight(blockstate3, p_111168_, blockposm);
            float f3 = LightCacheOF.getBrightness(blockstate3, p_111168_, blockposm);
            BlockState blockstate4 = p_111168_.getBlockState(blockposm.setPosOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[0], p_111171_));
            boolean flag = !blockstate4.isViewBlocking(p_111168_, blockposm) || blockstate4.getLightBlock(p_111168_, blockposm) == 0;
            BlockState blockstate5 = p_111168_.getBlockState(blockposm.setPosOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[1], p_111171_));
            boolean flag1 = !blockstate5.isViewBlocking(p_111168_, blockposm) || blockstate5.getLightBlock(p_111168_, blockposm) == 0;
            BlockState blockstate6 = p_111168_.getBlockState(blockposm.setPosOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[2], p_111171_));
            boolean flag2 = !blockstate6.isViewBlocking(p_111168_, blockposm) || blockstate6.getLightBlock(p_111168_, blockposm) == 0;
            BlockState blockstate7 = p_111168_.getBlockState(blockposm.setPosOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[3], p_111171_));
            boolean flag3 = !blockstate7.isViewBlocking(p_111168_, blockposm) || blockstate7.getLightBlock(p_111168_, blockposm) == 0;
            float f4;
            int i1;

            if (!flag2 && !flag)
            {
                f4 = f;
                i1 = i;
            }
            else
            {
                blockposm.setPosOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[0], modelblockrenderer$adjacencyinfo.corners[2]);
                BlockState blockstate8 = p_111168_.getBlockState(blockposm);
                f4 = LightCacheOF.getBrightness(blockstate8, p_111168_, blockposm);
                i1 = LightCacheOF.getPackedLight(blockstate8, p_111168_, blockposm);
            }

            int j1;
            float f26;

            if (!flag3 && !flag)
            {
                f26 = f;
                j1 = i;
            }
            else
            {
                blockposm.setPosOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[0], modelblockrenderer$adjacencyinfo.corners[3]);
                BlockState blockstate9 = p_111168_.getBlockState(blockposm);
                f26 = LightCacheOF.getBrightness(blockstate9, p_111168_, blockposm);
                j1 = LightCacheOF.getPackedLight(blockstate9, p_111168_, blockposm);
            }

            int k1;
            float f27;

            if (!flag2 && !flag1)
            {
                f27 = f;
                k1 = i;
            }
            else
            {
                blockposm.setPosOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[1], modelblockrenderer$adjacencyinfo.corners[2]);
                BlockState blockstate10 = p_111168_.getBlockState(blockposm);
                f27 = LightCacheOF.getBrightness(blockstate10, p_111168_, blockposm);
                k1 = LightCacheOF.getPackedLight(blockstate10, p_111168_, blockposm);
            }

            int l1;
            float f28;

            if (!flag3 && !flag1)
            {
                f28 = f;
                l1 = i;
            }
            else
            {
                blockposm.setPosOffset(blockpos, modelblockrenderer$adjacencyinfo.corners[1], modelblockrenderer$adjacencyinfo.corners[3]);
                BlockState blockstate11 = p_111168_.getBlockState(blockposm);
                f28 = LightCacheOF.getBrightness(blockstate11, p_111168_, blockposm);
                l1 = LightCacheOF.getPackedLight(blockstate11, p_111168_, blockposm);
            }

            int i3 = LightCacheOF.getPackedLight(p_111169_, p_111168_, p_111170_);
            blockposm.setPosOffset(p_111170_, p_111171_);
            BlockState blockstate12 = p_111168_.getBlockState(blockposm);

            if (p_111173_.get(0) || !blockstate12.isSolidRender(p_111168_, blockposm))
            {
                i3 = LightCacheOF.getPackedLight(blockstate12, p_111168_, blockposm);
            }

            float f5 = p_111173_.get(0) ? LightCacheOF.getBrightness(p_111168_.getBlockState(blockpos), p_111168_, blockpos) : LightCacheOF.getBrightness(p_111168_.getBlockState(p_111170_), p_111168_, p_111170_);
            ModelBlockRenderer.AmbientVertexRemap modelblockrenderer$ambientvertexremap = ModelBlockRenderer.AmbientVertexRemap.fromFacing(p_111171_);

            if (p_111173_.get(1) && modelblockrenderer$adjacencyinfo.doNonCubicWeight)
            {
                float f29 = (f3 + f + f26 + f5) * 0.25F;
                float f31 = (f2 + f + f4 + f5) * 0.25F;
                float f32 = (f2 + f1 + f27 + f5) * 0.25F;
                float f33 = (f3 + f1 + f28 + f5) * 0.25F;
                float f10 = p_111172_[modelblockrenderer$adjacencyinfo.vert0Weights[0].shape] * p_111172_[modelblockrenderer$adjacencyinfo.vert0Weights[1].shape];
                float f11 = p_111172_[modelblockrenderer$adjacencyinfo.vert0Weights[2].shape] * p_111172_[modelblockrenderer$adjacencyinfo.vert0Weights[3].shape];
                float f12 = p_111172_[modelblockrenderer$adjacencyinfo.vert0Weights[4].shape] * p_111172_[modelblockrenderer$adjacencyinfo.vert0Weights[5].shape];
                float f13 = p_111172_[modelblockrenderer$adjacencyinfo.vert0Weights[6].shape] * p_111172_[modelblockrenderer$adjacencyinfo.vert0Weights[7].shape];
                float f14 = p_111172_[modelblockrenderer$adjacencyinfo.vert1Weights[0].shape] * p_111172_[modelblockrenderer$adjacencyinfo.vert1Weights[1].shape];
                float f15 = p_111172_[modelblockrenderer$adjacencyinfo.vert1Weights[2].shape] * p_111172_[modelblockrenderer$adjacencyinfo.vert1Weights[3].shape];
                float f16 = p_111172_[modelblockrenderer$adjacencyinfo.vert1Weights[4].shape] * p_111172_[modelblockrenderer$adjacencyinfo.vert1Weights[5].shape];
                float f17 = p_111172_[modelblockrenderer$adjacencyinfo.vert1Weights[6].shape] * p_111172_[modelblockrenderer$adjacencyinfo.vert1Weights[7].shape];
                float f18 = p_111172_[modelblockrenderer$adjacencyinfo.vert2Weights[0].shape] * p_111172_[modelblockrenderer$adjacencyinfo.vert2Weights[1].shape];
                float f19 = p_111172_[modelblockrenderer$adjacencyinfo.vert2Weights[2].shape] * p_111172_[modelblockrenderer$adjacencyinfo.vert2Weights[3].shape];
                float f20 = p_111172_[modelblockrenderer$adjacencyinfo.vert2Weights[4].shape] * p_111172_[modelblockrenderer$adjacencyinfo.vert2Weights[5].shape];
                float f21 = p_111172_[modelblockrenderer$adjacencyinfo.vert2Weights[6].shape] * p_111172_[modelblockrenderer$adjacencyinfo.vert2Weights[7].shape];
                float f22 = p_111172_[modelblockrenderer$adjacencyinfo.vert3Weights[0].shape] * p_111172_[modelblockrenderer$adjacencyinfo.vert3Weights[1].shape];
                float f23 = p_111172_[modelblockrenderer$adjacencyinfo.vert3Weights[2].shape] * p_111172_[modelblockrenderer$adjacencyinfo.vert3Weights[3].shape];
                float f24 = p_111172_[modelblockrenderer$adjacencyinfo.vert3Weights[4].shape] * p_111172_[modelblockrenderer$adjacencyinfo.vert3Weights[5].shape];
                float f25 = p_111172_[modelblockrenderer$adjacencyinfo.vert3Weights[6].shape] * p_111172_[modelblockrenderer$adjacencyinfo.vert3Weights[7].shape];
                this.brightness[modelblockrenderer$ambientvertexremap.vert0] = f29 * f10 + f31 * f11 + f32 * f12 + f33 * f13;
                this.brightness[modelblockrenderer$ambientvertexremap.vert1] = f29 * f14 + f31 * f15 + f32 * f16 + f33 * f17;
                this.brightness[modelblockrenderer$ambientvertexremap.vert2] = f29 * f18 + f31 * f19 + f32 * f20 + f33 * f21;
                this.brightness[modelblockrenderer$ambientvertexremap.vert3] = f29 * f22 + f31 * f23 + f32 * f24 + f33 * f25;
                int i2 = this.blend(l, i, j1, i3);
                int j2 = this.blend(k, i, i1, i3);
                int k2 = this.blend(k, j, k1, i3);
                int l2 = this.blend(l, j, l1, i3);
                this.lightmap[modelblockrenderer$ambientvertexremap.vert0] = this.blend(i2, j2, k2, l2, f10, f11, f12, f13);
                this.lightmap[modelblockrenderer$ambientvertexremap.vert1] = this.blend(i2, j2, k2, l2, f14, f15, f16, f17);
                this.lightmap[modelblockrenderer$ambientvertexremap.vert2] = this.blend(i2, j2, k2, l2, f18, f19, f20, f21);
                this.lightmap[modelblockrenderer$ambientvertexremap.vert3] = this.blend(i2, j2, k2, l2, f22, f23, f24, f25);
            }
            else
            {
                float f6 = (f3 + f + f26 + f5) * 0.25F;
                float f7 = (f2 + f + f4 + f5) * 0.25F;
                float f8 = (f2 + f1 + f27 + f5) * 0.25F;
                float f9 = (f3 + f1 + f28 + f5) * 0.25F;
                this.lightmap[modelblockrenderer$ambientvertexremap.vert0] = this.blend(l, i, j1, i3);
                this.lightmap[modelblockrenderer$ambientvertexremap.vert1] = this.blend(k, i, i1, i3);
                this.lightmap[modelblockrenderer$ambientvertexremap.vert2] = this.blend(k, j, k1, i3);
                this.lightmap[modelblockrenderer$ambientvertexremap.vert3] = this.blend(l, j, l1, i3);
                this.brightness[modelblockrenderer$ambientvertexremap.vert0] = f6;
                this.brightness[modelblockrenderer$ambientvertexremap.vert1] = f7;
                this.brightness[modelblockrenderer$ambientvertexremap.vert2] = f8;
                this.brightness[modelblockrenderer$ambientvertexremap.vert3] = f9;
            }

            float f30 = p_111168_.getShade(p_111171_, p_111174_);

            for (int j3 = 0; j3 < this.brightness.length; ++j3)
            {
                this.brightness[j3] *= f30;
            }
        }

        private int blend(int pBr1, int pBr2, int pBr3, int pBr4)
        {
            if (pBr1 == 0)
            {
                pBr1 = pBr4;
            }

            if (pBr2 == 0)
            {
                pBr2 = pBr4;
            }

            if (pBr3 == 0)
            {
                pBr3 = pBr4;
            }

            return pBr1 + pBr2 + pBr3 + pBr4 >> 2 & 16711935;
        }

        private int blend(int pBr1, int pBr2, int pBr3, int pBr4, float p_111163_, float p_111164_, float p_111165_, float p_111166_)
        {
            int i = (int)((float)(pBr1 >> 16 & 255) * p_111163_ + (float)(pBr2 >> 16 & 255) * p_111164_ + (float)(pBr3 >> 16 & 255) * p_111165_ + (float)(pBr4 >> 16 & 255) * p_111166_) & 255;
            int j = (int)((float)(pBr1 & 255) * p_111163_ + (float)(pBr2 & 255) * p_111164_ + (float)(pBr3 & 255) * p_111165_ + (float)(pBr4 & 255) * p_111166_) & 255;
            return i << 16 | j;
        }
    }

    static enum AmbientVertexRemap
    {
        DOWN(0, 1, 2, 3),
        UP(2, 3, 0, 1),
        NORTH(3, 0, 1, 2),
        SOUTH(0, 1, 2, 3),
        WEST(3, 0, 1, 2),
        EAST(1, 2, 3, 0);

        final int vert0;
        final int vert1;
        final int vert2;
        final int vert3;
        private static final ModelBlockRenderer.AmbientVertexRemap[] BY_FACING = Util.make(new ModelBlockRenderer.AmbientVertexRemap[6], (remapIn) -> {
            remapIn[Direction.DOWN.get3DDataValue()] = DOWN;
            remapIn[Direction.UP.get3DDataValue()] = UP;
            remapIn[Direction.NORTH.get3DDataValue()] = NORTH;
            remapIn[Direction.SOUTH.get3DDataValue()] = SOUTH;
            remapIn[Direction.WEST.get3DDataValue()] = WEST;
            remapIn[Direction.EAST.get3DDataValue()] = EAST;
        });

        private AmbientVertexRemap(int p_111195_, int p_111196_, int p_111197_, int p_111198_)
        {
            this.vert0 = p_111195_;
            this.vert1 = p_111196_;
            this.vert2 = p_111197_;
            this.vert3 = p_111198_;
        }

        public static ModelBlockRenderer.AmbientVertexRemap fromFacing(Direction pFacing)
        {
            return BY_FACING[pFacing.get3DDataValue()];
        }
    }

    static class Cache
    {
        private boolean enabled;
        private final Long2IntLinkedOpenHashMap colorCache = Util.make(() ->
        {
            Long2IntLinkedOpenHashMap long2intlinkedopenhashmap = new Long2IntLinkedOpenHashMap(100, 0.25F)
            {
                protected void rehash(int p_111238_)
                {
                }
            };
            long2intlinkedopenhashmap.defaultReturnValue(Integer.MAX_VALUE);
            return long2intlinkedopenhashmap;
        });
        private final Long2FloatLinkedOpenHashMap brightnessCache = Util.make(() ->
        {
            Long2FloatLinkedOpenHashMap long2floatlinkedopenhashmap = new Long2FloatLinkedOpenHashMap(100, 0.25F)
            {
                protected void rehash(int p_111245_)
                {
                }
            };
            long2floatlinkedopenhashmap.defaultReturnValue(Float.NaN);
            return long2floatlinkedopenhashmap;
        });

        private Cache()
        {
        }

        public void enable()
        {
            this.enabled = true;
        }

        public void disable()
        {
            this.enabled = false;
            this.colorCache.clear();
            this.brightnessCache.clear();
        }

        public int getLightColor(BlockState pBlockState, BlockAndTintGetter pLightReader, BlockPos pBlockPos)
        {
            long i = pBlockPos.asLong();

            if (this.enabled)
            {
                int j = this.colorCache.get(i);

                if (j != Integer.MAX_VALUE)
                {
                    return j;
                }
            }

            int k = LevelRenderer.getLightColor(pLightReader, pBlockState, pBlockPos);

            if (this.enabled)
            {
                if (this.colorCache.size() == 100)
                {
                    this.colorCache.removeFirstInt();
                }

                this.colorCache.put(i, k);
            }

            return k;
        }

        public float getShadeBrightness(BlockState pBlockState, BlockAndTintGetter pLightReader, BlockPos pBlockPos)
        {
            long i = pBlockPos.asLong();

            if (this.enabled)
            {
                float f = this.brightnessCache.get(i);

                if (!Float.isNaN(f))
                {
                    return f;
                }
            }

            float f1 = pBlockState.getShadeBrightness(pLightReader, pBlockPos);

            if (this.enabled)
            {
                if (this.brightnessCache.size() == 100)
                {
                    this.brightnessCache.removeFirstFloat();
                }

                this.brightnessCache.put(i, f1);
            }

            return f1;
        }
    }

    protected static enum SizeInfo
    {
        DOWN(Direction.DOWN, false),
        UP(Direction.UP, false),
        NORTH(Direction.NORTH, false),
        SOUTH(Direction.SOUTH, false),
        WEST(Direction.WEST, false),
        EAST(Direction.EAST, false),
        FLIP_DOWN(Direction.DOWN, true),
        FLIP_UP(Direction.UP, true),
        FLIP_NORTH(Direction.NORTH, true),
        FLIP_SOUTH(Direction.SOUTH, true),
        FLIP_WEST(Direction.WEST, true),
        FLIP_EAST(Direction.EAST, true);

        final int shape;

        private SizeInfo(Direction p_111264_, boolean p_111265_)
        {
            this.shape = p_111264_.get3DDataValue() + (p_111265_ ? ModelBlockRenderer.DIRECTIONS.length : 0);
        }
    }
}
