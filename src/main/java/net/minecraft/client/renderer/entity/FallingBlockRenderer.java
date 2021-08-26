package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class FallingBlockRenderer extends EntityRenderer<FallingBlockEntity>
{
    public FallingBlockRenderer(EntityRendererProvider.Context p_174112_)
    {
        super(p_174112_);
        this.shadowRadius = 0.5F;
    }

    public void render(FallingBlockEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight)
    {
        BlockState blockstate = pEntity.getBlockState();

        if (blockstate.getRenderShape() == RenderShape.MODEL)
        {
            Level level = pEntity.getLevel();

            if (blockstate != level.getBlockState(pEntity.blockPosition()) && blockstate.getRenderShape() != RenderShape.INVISIBLE)
            {
                pMatrixStack.pushPose();
                BlockPos blockpos = new BlockPos(pEntity.getX(), pEntity.getBoundingBox().maxY, pEntity.getZ());
                pMatrixStack.translate(-0.5D, 0.0D, -0.5D);
                BlockRenderDispatcher blockrenderdispatcher = Minecraft.getInstance().getBlockRenderer();
                blockrenderdispatcher.getModelRenderer().tesselateBlock(level, blockrenderdispatcher.getBlockModel(blockstate), blockstate, blockpos, pMatrixStack, pBuffer.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(blockstate)), false, new Random(), blockstate.getSeed(pEntity.getStartPos()), OverlayTexture.NO_OVERLAY);
                pMatrixStack.popPose();
                super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
            }
        }
    }

    public ResourceLocation getTextureLocation(FallingBlockEntity pEntity)
    {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
