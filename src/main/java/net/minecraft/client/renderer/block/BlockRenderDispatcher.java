package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Random;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforgeop.client.model.data.EmptyModelData;
import net.minecraftforgeop.client.model.data.IModelData;
import net.minecraftforgeop.resource.IResourceType;
import net.minecraftforgeop.resource.VanillaResourceType;
import net.optifine.reflect.Reflector;

public class BlockRenderDispatcher implements ResourceManagerReloadListener
{
    private final BlockModelShaper blockModelShaper;
    private final ModelBlockRenderer modelRenderer;
    private final BlockEntityWithoutLevelRenderer blockEntityRenderer;
    private final LiquidBlockRenderer liquidBlockRenderer;
    private final Random random = new Random();
    private final BlockColors blockColors;

    public BlockRenderDispatcher(BlockModelShaper p_173399_, BlockEntityWithoutLevelRenderer p_173400_, BlockColors p_173401_)
    {
        this.blockModelShaper = p_173399_;
        this.blockEntityRenderer = p_173400_;
        this.blockColors = p_173401_;

        if (Reflector.ForgeBlockModelRenderer_Constructor.exists())
        {
            this.modelRenderer = (ModelBlockRenderer)Reflector.newInstance(Reflector.ForgeBlockModelRenderer_Constructor, this.blockColors);
        }
        else
        {
            this.modelRenderer = new ModelBlockRenderer(this.blockColors);
        }

        this.liquidBlockRenderer = new LiquidBlockRenderer();
    }

    public BlockModelShaper getBlockModelShaper()
    {
        return this.blockModelShaper;
    }

    public void renderBreakingTexture(BlockState pBlockState, BlockPos pPos, BlockAndTintGetter pLightReader, PoseStack pMatrixStack, VertexConsumer pVertexBuilder)
    {
        this.renderBreakingTexture(pBlockState, pPos, pLightReader, pMatrixStack, pVertexBuilder, EmptyModelData.INSTANCE);
    }

    public void renderBreakingTexture(BlockState blockStateIn, BlockPos posIn, BlockAndTintGetter lightReaderIn, PoseStack matrixStackIn, VertexConsumer vertexBuilderIn, IModelData modelData)
    {
        if (blockStateIn.getRenderShape() == RenderShape.MODEL)
        {
            BakedModel bakedmodel = this.blockModelShaper.getBlockModel(blockStateIn);
            long i = blockStateIn.getSeed(posIn);
            this.modelRenderer.tesselateBlock(lightReaderIn, bakedmodel, blockStateIn, posIn, matrixStackIn, vertexBuilderIn, true, this.random, i, OverlayTexture.NO_OVERLAY, modelData);
        }
    }

    public boolean renderBatched(BlockState pBlockState, BlockPos pPos, BlockAndTintGetter pLightReader, PoseStack pMatrixStack, VertexConsumer pVertexBuilder, boolean pCheckSides, Random pRand)
    {
        return this.renderBatched(pBlockState, pPos, pLightReader, pMatrixStack, pVertexBuilder, pCheckSides, pRand, EmptyModelData.INSTANCE);
    }

    public boolean renderBatched(BlockState blockStateIn, BlockPos posIn, BlockAndTintGetter lightReaderIn, PoseStack matrixStackIn, VertexConsumer vertexBuilderIn, boolean checkSides, Random rand, IModelData modelData)
    {
        try
        {
            RenderShape rendershape = blockStateIn.getRenderShape();
            return rendershape != RenderShape.MODEL ? false : this.modelRenderer.tesselateBlock(lightReaderIn, this.getBlockModel(blockStateIn), blockStateIn, posIn, matrixStackIn, vertexBuilderIn, checkSides, rand, blockStateIn.getSeed(posIn), OverlayTexture.NO_OVERLAY, modelData);
        }
        catch (Throwable throwable1)
        {
            CrashReport crashreport = CrashReport.forThrowable(throwable1, "Tesselating block in world");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Block being tesselated");
            CrashReportCategory.populateBlockDetails(crashreportcategory, lightReaderIn, posIn, blockStateIn);
            throw new ReportedException(crashreport);
        }
    }

    public boolean renderLiquid(BlockPos pPos, BlockAndTintGetter pLightReader, VertexConsumer pVertexBuilder, FluidState pFluidState)
    {
        try
        {
            return this.liquidBlockRenderer.tesselate(pLightReader, pPos, pVertexBuilder, pFluidState);
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Tesselating liquid in world");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Block being tesselated");
            CrashReportCategory.populateBlockDetails(crashreportcategory, pLightReader, pPos, (BlockState)null);
            throw new ReportedException(crashreport);
        }
    }

    public ModelBlockRenderer getModelRenderer()
    {
        return this.modelRenderer;
    }

    public BakedModel getBlockModel(BlockState pState)
    {
        return this.blockModelShaper.getBlockModel(pState);
    }

    public void renderSingleBlock(BlockState pBlockState, PoseStack pMatrixStack, MultiBufferSource pBufferType, int pCombinedLight, int pCombinedOverlay)
    {
        this.renderSingleBlock(pBlockState, pMatrixStack, pBufferType, pCombinedLight, pCombinedOverlay, EmptyModelData.INSTANCE);
    }

    public void renderSingleBlock(BlockState blockStateIn, PoseStack matrixStackIn, MultiBufferSource bufferTypeIn, int combinedLightIn, int combinedOverlayIn, IModelData modelData)
    {
        RenderShape rendershape = blockStateIn.getRenderShape();

        if (rendershape != RenderShape.INVISIBLE)
        {
            switch (rendershape)
            {
                case MODEL:
                    BakedModel bakedmodel = this.getBlockModel(blockStateIn);
                    int i = this.blockColors.getColor(blockStateIn, (BlockAndTintGetter)null, (BlockPos)null, 0);
                    float f = (float)(i >> 16 & 255) / 255.0F;
                    float f1 = (float)(i >> 8 & 255) / 255.0F;
                    float f2 = (float)(i & 255) / 255.0F;
                    this.modelRenderer.renderModel(matrixStackIn.last(), bufferTypeIn.getBuffer(ItemBlockRenderTypes.getRenderType(blockStateIn, false)), blockStateIn, bakedmodel, f, f1, f2, combinedLightIn, combinedOverlayIn, modelData);
                    break;

                case ENTITYBLOCK_ANIMATED:
                    if (Reflector.IForgeItem_getItemStackTileEntityRenderer.exists())
                    {
                        ItemStack itemstack = new ItemStack(blockStateIn.getBlock());
                        BlockEntityWithoutLevelRenderer blockentitywithoutlevelrenderer = (BlockEntityWithoutLevelRenderer)Reflector.call(itemstack.getItem(), Reflector.IForgeItem_getItemStackTileEntityRenderer);
                        blockentitywithoutlevelrenderer.renderByItem(itemstack, ItemTransforms.TransformType.NONE, matrixStackIn, bufferTypeIn, combinedLightIn, combinedOverlayIn);
                    }
                    else
                    {
                        this.blockEntityRenderer.renderByItem(new ItemStack(blockStateIn.getBlock()), ItemTransforms.TransformType.NONE, matrixStackIn, bufferTypeIn, combinedLightIn, combinedOverlayIn);
                    }
            }
        }
    }

    public void onResourceManagerReload(ResourceManager pResourceManager)
    {
        this.liquidBlockRenderer.setupSprites();
    }

    public IResourceType getResourceType()
    {
        return VanillaResourceType.MODELS;
    }
}
