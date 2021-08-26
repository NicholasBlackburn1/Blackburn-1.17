package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LecternRenderer implements BlockEntityRenderer<LecternBlockEntity>
{
    private final BookModel bookModel;

    public LecternRenderer(BlockEntityRendererProvider.Context p_173621_)
    {
        this.bookModel = new BookModel(p_173621_.bakeLayer(ModelLayers.BOOK));
    }

    public void render(LecternBlockEntity pBlockEntity, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay)
    {
        BlockState blockstate = pBlockEntity.getBlockState();

        if (blockstate.getValue(LecternBlock.HAS_BOOK))
        {
            pMatrixStack.pushPose();
            pMatrixStack.translate(0.5D, 1.0625D, 0.5D);
            float f = blockstate.getValue(LecternBlock.FACING).getClockWise().toYRot();
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(-f));
            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(67.5F));
            pMatrixStack.translate(0.0D, -0.125D, 0.0D);
            this.bookModel.setupAnim(0.0F, 0.1F, 0.9F, 1.2F);
            VertexConsumer vertexconsumer = EnchantTableRenderer.BOOK_LOCATION.buffer(pBuffer, RenderType::entitySolid);
            this.bookModel.render(pMatrixStack, vertexconsumer, pCombinedLight, pCombinedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
            pMatrixStack.popPose();
        }
    }
}
