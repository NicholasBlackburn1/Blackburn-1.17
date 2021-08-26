package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Random;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;

public class PistonHeadRenderer implements BlockEntityRenderer<PistonMovingBlockEntity>
{
    private final BlockRenderDispatcher blockRenderer;

    public PistonHeadRenderer(BlockEntityRendererProvider.Context p_173623_)
    {
        this.blockRenderer = p_173623_.getBlockRenderDispatcher();
    }

    public void render(PistonMovingBlockEntity pBlockEntity, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay)
    {
        Level level = pBlockEntity.getLevel();

        if (level != null)
        {
            BlockPos blockpos = pBlockEntity.getBlockPos().relative(pBlockEntity.getMovementDirection().getOpposite());
            BlockState blockstate = pBlockEntity.getMovedState();

            if (!blockstate.isAir())
            {
                ModelBlockRenderer.enableCaching();
                pMatrixStack.pushPose();
                pMatrixStack.translate((double)pBlockEntity.getXOff(pPartialTicks), (double)pBlockEntity.getYOff(pPartialTicks), (double)pBlockEntity.getZOff(pPartialTicks));

                if (blockstate.is(Blocks.PISTON_HEAD) && pBlockEntity.getProgress(pPartialTicks) <= 4.0F)
                {
                    blockstate = blockstate.setValue(PistonHeadBlock.SHORT, Boolean.valueOf(pBlockEntity.getProgress(pPartialTicks) <= 0.5F));
                    this.renderBlock(blockpos, blockstate, pMatrixStack, pBuffer, level, false, pCombinedOverlay);
                }
                else if (pBlockEntity.isSourcePiston() && !pBlockEntity.isExtending())
                {
                    PistonType pistontype = blockstate.is(Blocks.STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT;
                    BlockState blockstate1 = Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.TYPE, pistontype).setValue(PistonHeadBlock.FACING, blockstate.getValue(PistonBaseBlock.FACING));
                    blockstate1 = blockstate1.setValue(PistonHeadBlock.SHORT, Boolean.valueOf(pBlockEntity.getProgress(pPartialTicks) >= 0.5F));
                    this.renderBlock(blockpos, blockstate1, pMatrixStack, pBuffer, level, false, pCombinedOverlay);
                    BlockPos blockpos1 = blockpos.relative(pBlockEntity.getMovementDirection());
                    pMatrixStack.popPose();
                    pMatrixStack.pushPose();
                    blockstate = blockstate.setValue(PistonBaseBlock.EXTENDED, Boolean.valueOf(true));
                    this.renderBlock(blockpos1, blockstate, pMatrixStack, pBuffer, level, true, pCombinedOverlay);
                }
                else
                {
                    this.renderBlock(blockpos, blockstate, pMatrixStack, pBuffer, level, false, pCombinedOverlay);
                }

                pMatrixStack.popPose();
                ModelBlockRenderer.clearCache();
            }
        }
    }

    private void renderBlock(BlockPos p_112459_, BlockState p_112460_, PoseStack p_112461_, MultiBufferSource p_112462_, Level p_112463_, boolean p_112464_, int p_112465_)
    {
        RenderType rendertype = ItemBlockRenderTypes.getMovingBlockRenderType(p_112460_);
        VertexConsumer vertexconsumer = p_112462_.getBuffer(rendertype);
        this.blockRenderer.getModelRenderer().tesselateBlock(p_112463_, this.blockRenderer.getBlockModel(p_112460_), p_112460_, p_112459_, p_112461_, vertexconsumer, p_112464_, new Random(), p_112460_.getSeed(p_112459_), p_112465_);
    }

    public int getViewDistance()
    {
        return 68;
    }
}
