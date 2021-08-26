package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.optifine.Config;
import net.optifine.CustomColors;
import net.optifine.reflect.Reflector;
import net.optifine.render.RenderEnv;
import net.optifine.shaders.SVertexBuilder;
import net.optifine.shaders.Shaders;

public class LiquidBlockRenderer
{
    private static final float MAX_FLUID_HEIGHT = 0.8888889F;
    private final TextureAtlasSprite[] lavaIcons = new TextureAtlasSprite[2];
    private final TextureAtlasSprite[] waterIcons = new TextureAtlasSprite[2];
    private TextureAtlasSprite waterOverlay;

    protected void setupSprites()
    {
        this.lavaIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.LAVA.defaultBlockState()).getParticleIcon();
        this.lavaIcons[1] = ModelBakery.LAVA_FLOW.sprite();
        this.waterIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).getParticleIcon();
        this.waterIcons[1] = ModelBakery.WATER_FLOW.sprite();
        this.waterOverlay = ModelBakery.WATER_OVERLAY.sprite();
    }

    private static boolean isNeighborSameFluid(BlockGetter pLevel, BlockPos pPos, Direction pSide, FluidState pState)
    {
        BlockPos blockpos = pPos.relative(pSide);
        FluidState fluidstate = pLevel.getFluidState(blockpos);
        return fluidstate.getType().isSame(pState.getType());
    }

    private static boolean isFaceOccludedByState(BlockGetter p_110979_, Direction p_110980_, float p_110981_, BlockPos p_110982_, BlockState p_110983_)
    {
        if (p_110983_.canOcclude())
        {
            VoxelShape voxelshape = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, (double)p_110981_, 1.0D);
            VoxelShape voxelshape1 = p_110983_.getOcclusionShape(p_110979_, p_110982_);
            return Shapes.blockOccudes(voxelshape, voxelshape1, p_110980_);
        }
        else
        {
            return false;
        }
    }

    private static boolean isFaceOccludedByNeighbor(BlockGetter p_110969_, BlockPos p_110970_, Direction p_110971_, float p_110972_)
    {
        BlockPos blockpos = p_110970_.relative(p_110971_);
        BlockState blockstate = p_110969_.getBlockState(blockpos);
        return isFaceOccludedByState(p_110969_, p_110971_, p_110972_, blockpos, blockstate);
    }

    private static boolean isFaceOccludedBySelf(BlockGetter p_110960_, BlockPos p_110961_, BlockState p_110962_, Direction p_110963_)
    {
        return isFaceOccludedByState(p_110960_, p_110963_.getOpposite(), 1.0F, p_110961_, p_110962_);
    }

    public static boolean shouldRenderFace(BlockAndTintGetter p_110949_, BlockPos p_110950_, FluidState p_110951_, BlockState p_110952_, Direction p_110953_)
    {
        return !isFaceOccludedBySelf(p_110949_, p_110950_, p_110952_, p_110953_) && !isNeighborSameFluid(p_110949_, p_110950_, p_110953_, p_110951_);
    }

    public boolean tesselate(BlockAndTintGetter pLightReader, BlockPos pPos, VertexConsumer pVertexBuilder, FluidState pFluidState)
    {
        BlockState blockstate = pFluidState.createLegacyBlock();
        boolean flg, flag7 = false;

        try
        {
            if (Config.isShaders())
            {
                SVertexBuilder.pushEntity(blockstate, pVertexBuilder);
            }

            boolean flag = pFluidState.is(FluidTags.LAVA);
            TextureAtlasSprite[] atextureatlassprite = flag ? this.lavaIcons : this.waterIcons;
            BlockState blockstate1 = pLightReader.getBlockState(pPos);

            if (Reflector.ForgeHooksClient_getFluidSprites.exists())
            {
                TextureAtlasSprite[] atextureatlassprite1 = (TextureAtlasSprite[])Reflector.call(Reflector.ForgeHooksClient_getFluidSprites, pLightReader, pPos, pFluidState);

                if (atextureatlassprite1 != null)
                {
                    atextureatlassprite = atextureatlassprite1;
                }
            }

            RenderEnv renderenv = pVertexBuilder.getRenderEnv(blockstate, pPos);
            int i = -1;
            float f = 1.0F;

            if (Reflector.IForgeFluid_getAttributes.exists())
            {
                Object object = Reflector.call(pFluidState.getType(), Reflector.IForgeFluid_getAttributes);

                if (object != null && Reflector.FluidAttributes_getColor.exists())
                {
                    i = Reflector.callInt(object, Reflector.FluidAttributes_getColor, pLightReader, pPos);
                    f = (float)(i >> 24 & 255) / 255.0F;
                }
            }

            boolean flag10 = !isNeighborSameFluid(pLightReader, pPos, Direction.UP, pFluidState);
            boolean flag1 = shouldRenderFace(pLightReader, pPos, pFluidState, blockstate1, Direction.DOWN) && !isFaceOccludedByNeighbor(pLightReader, pPos, Direction.DOWN, 0.8888889F);
            boolean flag2 = shouldRenderFace(pLightReader, pPos, pFluidState, blockstate1, Direction.NORTH);
            boolean flag3 = shouldRenderFace(pLightReader, pPos, pFluidState, blockstate1, Direction.SOUTH);
            boolean flag4 = shouldRenderFace(pLightReader, pPos, pFluidState, blockstate1, Direction.WEST);
            boolean flag5 = shouldRenderFace(pLightReader, pPos, pFluidState, blockstate1, Direction.EAST);

            if (flag10 || flag1 || flag5 || flag4 || flag2 || flag3)
            {
                if (i < 0)
                {
                    i = CustomColors.getFluidColor(pLightReader, blockstate, pPos, renderenv);
                }

                float f28 = (float)(i >> 16 & 255) / 255.0F;
                float f1 = (float)(i >> 8 & 255) / 255.0F;
                float f2 = (float)(i & 255) / 255.0F;
                float f3 = pLightReader.getShade(Direction.DOWN, true);
                float f4 = pLightReader.getShade(Direction.UP, true);
                float f5 = pLightReader.getShade(Direction.NORTH, true);
                float f6 = pLightReader.getShade(Direction.WEST, true);
                float f7 = this.getWaterHeight(pLightReader, pPos, pFluidState.getType());
                float f8 = this.getWaterHeight(pLightReader, pPos.south(), pFluidState.getType());
                float f9 = this.getWaterHeight(pLightReader, pPos.east().south(), pFluidState.getType());
                float f10 = this.getWaterHeight(pLightReader, pPos.east(), pFluidState.getType());
                double d0 = (double)(pPos.getX() & 15);
                double d1 = (double)(pPos.getY() & 15);
                double d2 = (double)(pPos.getZ() & 15);

                if (Config.isRenderRegions())
                {
                    int j = pPos.getX() >> 4 << 4;
                    int k = pPos.getY() >> 4 << 4;
                    int l = pPos.getZ() >> 4 << 4;
                    int i1 = 8;
                    int j1 = j >> i1 << i1;
                    int k1 = l >> i1 << i1;
                    int l1 = j - j1;
                    int i2 = l - k1;
                    d0 += (double)l1;
                    d1 += (double)k;
                    d2 += (double)i2;
                }

                if (Config.isShaders() && Shaders.useMidBlockAttrib)
                {
                    pVertexBuilder.setMidBlock((float)(d0 + 0.5D), (float)(d1 + 0.5D), (float)(d2 + 0.5D));
                }

                float f29 = 0.001F;
                float f30 = flag1 ? 0.001F : 0.0F;

                if (flag10 && !isFaceOccludedByNeighbor(pLightReader, pPos, Direction.UP, Math.min(Math.min(f7, f8), Math.min(f9, f10))))
                {
                    flag7 = true;
                    f7 -= 0.001F;
                    f8 -= 0.001F;
                    f9 -= 0.001F;
                    f10 -= 0.001F;
                    Vec3 vec3 = pFluidState.getFlow(pLightReader, pPos);
                    float f11;
                    float f12;
                    float f13;
                    float f32;
                    float f34;
                    float f37;
                    float f40;
                    float f42;

                    if (vec3.x == 0.0D && vec3.z == 0.0D)
                    {
                        TextureAtlasSprite textureatlassprite1 = atextureatlassprite[0];
                        pVertexBuilder.setSprite(textureatlassprite1);
                        f32 = textureatlassprite1.getU(0.0D);
                        f11 = textureatlassprite1.getV(0.0D);
                        f34 = f32;
                        f42 = textureatlassprite1.getV(16.0D);
                        f37 = textureatlassprite1.getU(16.0D);
                        f12 = f42;
                        f40 = f37;
                        f13 = f11;
                    }
                    else
                    {
                        TextureAtlasSprite textureatlassprite = atextureatlassprite[1];
                        pVertexBuilder.setSprite(textureatlassprite);
                        float f14 = (float)Mth.atan2(vec3.z, vec3.x) - ((float)Math.PI / 2F);
                        float f15 = Mth.sin(f14) * 0.25F;
                        float f16 = Mth.cos(f14) * 0.25F;
                        float f17 = 8.0F;
                        f32 = textureatlassprite.getU((double)(8.0F + (-f16 - f15) * 16.0F));
                        f11 = textureatlassprite.getV((double)(8.0F + (-f16 + f15) * 16.0F));
                        f34 = textureatlassprite.getU((double)(8.0F + (-f16 + f15) * 16.0F));
                        f42 = textureatlassprite.getV((double)(8.0F + (f16 + f15) * 16.0F));
                        f37 = textureatlassprite.getU((double)(8.0F + (f16 + f15) * 16.0F));
                        f12 = textureatlassprite.getV((double)(8.0F + (f16 - f15) * 16.0F));
                        f40 = textureatlassprite.getU((double)(8.0F + (f16 - f15) * 16.0F));
                        f13 = textureatlassprite.getV((double)(8.0F + (-f16 - f15) * 16.0F));
                    }

                    float f46 = (f32 + f34 + f37 + f40) / 4.0F;
                    float f47 = (f11 + f42 + f12 + f13) / 4.0F;
                    float f48 = (float)atextureatlassprite[0].getWidth() / (atextureatlassprite[0].getU1() - atextureatlassprite[0].getU0());
                    float f49 = (float)atextureatlassprite[0].getHeight() / (atextureatlassprite[0].getV1() - atextureatlassprite[0].getV0());
                    float f50 = 4.0F / Math.max(f49, f48);
                    f32 = Mth.lerp(f50, f32, f46);
                    f34 = Mth.lerp(f50, f34, f46);
                    f37 = Mth.lerp(f50, f37, f46);
                    f40 = Mth.lerp(f50, f40, f46);
                    f11 = Mth.lerp(f50, f11, f47);
                    f42 = Mth.lerp(f50, f42, f47);
                    f12 = Mth.lerp(f50, f12, f47);
                    f13 = Mth.lerp(f50, f13, f47);
                    int j2 = this.getLightColor(pLightReader, pPos);
                    float f18 = f4 * f28;
                    float f19 = f4 * f1;
                    float f20 = f4 * f2;
                    this.vertexVanilla(pVertexBuilder, d0 + 0.0D, d1 + (double)f7, d2 + 0.0D, f18, f19, f20, f, f32, f11, j2);
                    this.vertexVanilla(pVertexBuilder, d0 + 0.0D, d1 + (double)f8, d2 + 1.0D, f18, f19, f20, f, f34, f42, j2);
                    this.vertexVanilla(pVertexBuilder, d0 + 1.0D, d1 + (double)f9, d2 + 1.0D, f18, f19, f20, f, f37, f12, j2);
                    this.vertexVanilla(pVertexBuilder, d0 + 1.0D, d1 + (double)f10, d2 + 0.0D, f18, f19, f20, f, f40, f13, j2);

                    if (pFluidState.shouldRenderBackwardUpFace(pLightReader, pPos.above()))
                    {
                        this.vertexVanilla(pVertexBuilder, d0 + 0.0D, d1 + (double)f7, d2 + 0.0D, f18, f19, f20, f, f32, f11, j2);
                        this.vertexVanilla(pVertexBuilder, d0 + 1.0D, d1 + (double)f10, d2 + 0.0D, f18, f19, f20, f, f40, f13, j2);
                        this.vertexVanilla(pVertexBuilder, d0 + 1.0D, d1 + (double)f9, d2 + 1.0D, f18, f19, f20, f, f37, f12, j2);
                        this.vertexVanilla(pVertexBuilder, d0 + 0.0D, d1 + (double)f8, d2 + 1.0D, f18, f19, f20, f, f34, f42, j2);
                    }
                }

                if (flag1)
                {
                    pVertexBuilder.setSprite(atextureatlassprite[0]);
                    float f31 = atextureatlassprite[0].getU0();
                    float f33 = atextureatlassprite[0].getU1();
                    float f35 = atextureatlassprite[0].getV0();
                    float f38 = atextureatlassprite[0].getV1();
                    int i3 = this.getLightColor(pLightReader, pPos.below());
                    float f41 = pLightReader.getShade(Direction.DOWN, true);
                    float f43 = f41 * f28;
                    float f44 = f41 * f1;
                    float f45 = f41 * f2;
                    this.vertexVanilla(pVertexBuilder, d0, d1 + (double)f30, d2 + 1.0D, f43, f44, f45, f, f31, f38, i3);
                    this.vertexVanilla(pVertexBuilder, d0, d1 + (double)f30, d2, f43, f44, f45, f, f31, f35, i3);
                    this.vertexVanilla(pVertexBuilder, d0 + 1.0D, d1 + (double)f30, d2, f43, f44, f45, f, f33, f35, i3);
                    this.vertexVanilla(pVertexBuilder, d0 + 1.0D, d1 + (double)f30, d2 + 1.0D, f43, f44, f45, f, f33, f38, i3);
                    flag7 = true;
                }

                int k2 = this.getLightColor(pLightReader, pPos);

                for (int l2 = 0; l2 < 4; ++l2)
                {
                    float f36;
                    float f39;
                    double d3;
                    double d4;
                    double d5;
                    double d6;
                    Direction direction;
                    boolean flag11;

                    if (l2 == 0)
                    {
                        f36 = f7;
                        f39 = f10;
                        d3 = d0;
                        d5 = d0 + 1.0D;
                        d4 = d2 + (double)0.001F;
                        d6 = d2 + (double)0.001F;
                        direction = Direction.NORTH;
                        flag11 = flag2;
                    }
                    else if (l2 == 1)
                    {
                        f36 = f9;
                        f39 = f8;
                        d3 = d0 + 1.0D;
                        d5 = d0;
                        d4 = d2 + 1.0D - (double)0.001F;
                        d6 = d2 + 1.0D - (double)0.001F;
                        direction = Direction.SOUTH;
                        flag11 = flag3;
                    }
                    else if (l2 == 2)
                    {
                        f36 = f8;
                        f39 = f7;
                        d3 = d0 + (double)0.001F;
                        d5 = d0 + (double)0.001F;
                        d4 = d2 + 1.0D;
                        d6 = d2;
                        direction = Direction.WEST;
                        flag11 = flag4;
                    }
                    else
                    {
                        f36 = f10;
                        f39 = f9;
                        d3 = d0 + 1.0D - (double)0.001F;
                        d5 = d0 + 1.0D - (double)0.001F;
                        d4 = d2;
                        d6 = d2 + 1.0D;
                        direction = Direction.EAST;
                        flag11 = flag5;
                    }

                    if (flag11 && !isFaceOccludedByNeighbor(pLightReader, pPos, direction, Math.max(f36, f39)))
                    {
                        flag7 = true;
                        BlockPos blockpos = pPos.relative(direction);
                        TextureAtlasSprite textureatlassprite2 = atextureatlassprite[1];
                        float f51 = 0.0F;
                        float f52 = 0.0F;
                        boolean flag8 = !flag;

                        if (Reflector.IForgeBlockState_shouldDisplayFluidOverlay.exists())
                        {
                            flag8 = atextureatlassprite[2] != null;
                        }

                        if (flag8)
                        {
                            BlockState blockstate2 = pLightReader.getBlockState(blockpos);
                            Block block = blockstate2.getBlock();
                            boolean flag9 = false;

                            if (Reflector.IForgeBlockState_shouldDisplayFluidOverlay.exists())
                            {
                                flag9 = Reflector.callBoolean(blockstate2, Reflector.IForgeBlockState_shouldDisplayFluidOverlay, pLightReader, blockpos, pFluidState);
                            }

                            if (flag9 || block instanceof HalfTransparentBlock || block instanceof LeavesBlock || block == Blocks.BEACON)
                            {
                                textureatlassprite2 = this.waterOverlay;
                            }

                            if (block == Blocks.FARMLAND || block == Blocks.DIRT_PATH)
                            {
                                f51 = 0.9375F;
                                f52 = 0.9375F;
                            }

                            if (block instanceof SlabBlock)
                            {
                                SlabBlock slabblock = (SlabBlock)block;

                                if (blockstate2.getValue(SlabBlock.TYPE) == SlabType.BOTTOM)
                                {
                                    f51 = 0.5F;
                                    f52 = 0.5F;
                                }
                            }
                        }

                        pVertexBuilder.setSprite(textureatlassprite2);

                        if (!(f36 <= f51) || !(f39 <= f52))
                        {
                            f51 = Math.min(f51, f36);
                            f52 = Math.min(f52, f39);

                            if (f51 > f29)
                            {
                                f51 -= f29;
                            }

                            if (f52 > f29)
                            {
                                f52 -= f29;
                            }

                            float f53 = textureatlassprite2.getV((double)((1.0F - f51) * 16.0F * 0.5F));
                            float f54 = textureatlassprite2.getV((double)((1.0F - f52) * 16.0F * 0.5F));
                            float f55 = textureatlassprite2.getU(0.0D);
                            float f56 = textureatlassprite2.getU(8.0D);
                            float f21 = textureatlassprite2.getV((double)((1.0F - f36) * 16.0F * 0.5F));
                            float f22 = textureatlassprite2.getV((double)((1.0F - f39) * 16.0F * 0.5F));
                            float f23 = textureatlassprite2.getV(8.0D);
                            float f24 = l2 < 2 ? pLightReader.getShade(Direction.NORTH, true) : pLightReader.getShade(Direction.WEST, true);
                            float f25 = f4 * f24 * f28;
                            float f26 = f4 * f24 * f1;
                            float f27 = f4 * f24 * f2;
                            this.vertexVanilla(pVertexBuilder, d3, d1 + (double)f36, d4, f25, f26, f27, f, f55, f21, k2);
                            this.vertexVanilla(pVertexBuilder, d5, d1 + (double)f39, d6, f25, f26, f27, f, f56, f22, k2);
                            this.vertexVanilla(pVertexBuilder, d5, d1 + (double)f30, d6, f25, f26, f27, f, f56, f54, k2);
                            this.vertexVanilla(pVertexBuilder, d3, d1 + (double)f30, d4, f25, f26, f27, f, f55, f53, k2);

                            if (textureatlassprite2 != this.waterOverlay)
                            {
                                this.vertexVanilla(pVertexBuilder, d3, d1 + (double)f30, d4, f25, f26, f27, f, f55, f53, k2);
                                this.vertexVanilla(pVertexBuilder, d5, d1 + (double)f30, d6, f25, f26, f27, f, f56, f54, k2);
                                this.vertexVanilla(pVertexBuilder, d5, d1 + (double)f39, d6, f25, f26, f27, f, f56, f22, k2);
                                this.vertexVanilla(pVertexBuilder, d3, d1 + (double)f36, d4, f25, f26, f27, f, f55, f21, k2);
                            }
                        }
                    }
                }

                pVertexBuilder.setSprite((TextureAtlasSprite)null);
                return flag7;
            }

            flg = flag7;
        }
        finally
        {
            if (Config.isShaders())
            {
                SVertexBuilder.popEntity(pVertexBuilder);
            }
        }

        return flg;
    }

    private void vertex(VertexConsumer pVertexBuilder, double pX, double p_110987_, double pY, float p_110989_, float pZ, float p_110991_, float pRed, float pGreen, int pBlue)
    {
        pVertexBuilder.vertex(pX, p_110987_, pY).color(p_110989_, pZ, p_110991_, 1.0F).uv(pRed, pGreen).uv2(pBlue).normal(0.0F, 1.0F, 0.0F).endVertex();
    }

    private void vertexVanilla(VertexConsumer buffer, double x, double y, double z, float red, float green, float blue, float alpha, float u, float v, int combinedLight)
    {
        buffer.vertex(x, y, z).color(red, green, blue, alpha).uv(u, v).uv2(combinedLight).normal(0.0F, 1.0F, 0.0F).endVertex();
    }

    private int getLightColor(BlockAndTintGetter pLightReader, BlockPos pPos)
    {
        int i = LevelRenderer.getLightColor(pLightReader, pPos);
        int j = LevelRenderer.getLightColor(pLightReader, pPos.above());
        int k = i & 255;
        int l = j & 255;
        int i1 = i >> 16 & 255;
        int j1 = j >> 16 & 255;
        return (k > l ? k : l) | (i1 > j1 ? i1 : j1) << 16;
    }

    private float getWaterHeight(BlockGetter pReader, BlockPos pPos, Fluid pFluid)
    {
        int i = 0;
        float f = 0.0F;

        for (int j = 0; j < 4; ++j)
        {
            BlockPos blockpos = pPos.offset(-(j & 1), 0, -(j >> 1 & 1));

            if (pReader.getFluidState(blockpos.above()).getType().isSame(pFluid))
            {
                return 1.0F;
            }

            FluidState fluidstate = pReader.getFluidState(blockpos);

            if (fluidstate.getType().isSame(pFluid))
            {
                float f1 = fluidstate.getHeight(pReader, blockpos);

                if (f1 >= 0.8F)
                {
                    f += f1 * 10.0F;
                    i += 10;
                }
                else
                {
                    f += f1;
                    ++i;
                }
            }
            else if (!pReader.getBlockState(blockpos).getMaterial().isSolid())
            {
                ++i;
            }
        }

        return f / (float)i;
    }
}
