package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ShulkerBoxRenderer implements BlockEntityRenderer<ShulkerBoxBlockEntity>
{
    private final ShulkerModel<?> model;

    public ShulkerBoxRenderer(BlockEntityRendererProvider.Context p_173626_)
    {
        this.model = new ShulkerModel(p_173626_.bakeLayer(ModelLayers.SHULKER));
    }

    public void render(ShulkerBoxBlockEntity pBlockEntity, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay)
    {
        Direction direction = Direction.UP;

        if (pBlockEntity.hasLevel())
        {
            BlockState blockstate = pBlockEntity.getLevel().getBlockState(pBlockEntity.getBlockPos());

            if (blockstate.getBlock() instanceof ShulkerBoxBlock)
            {
                direction = blockstate.getValue(ShulkerBoxBlock.FACING);
            }
        }

        DyeColor dyecolor = pBlockEntity.getColor();
        Material material;

        if (dyecolor == null)
        {
            material = Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION;
        }
        else
        {
            material = Sheets.SHULKER_TEXTURE_LOCATION.get(dyecolor.getId());
        }

        pMatrixStack.pushPose();
        pMatrixStack.translate(0.5D, 0.5D, 0.5D);
        float f = 0.9995F;
        pMatrixStack.scale(0.9995F, 0.9995F, 0.9995F);
        pMatrixStack.mulPose(direction.getRotation());
        pMatrixStack.scale(1.0F, -1.0F, -1.0F);
        pMatrixStack.translate(0.0D, -1.0D, 0.0D);
        ModelPart modelpart = this.model.getLid();
        modelpart.setPos(0.0F, 24.0F - pBlockEntity.getProgress(pPartialTicks) * 0.5F * 16.0F, 0.0F);
        modelpart.yRot = 270.0F * pBlockEntity.getProgress(pPartialTicks) * ((float)Math.PI / 180F);
        VertexConsumer vertexconsumer = material.buffer(pBuffer, RenderType::entityCutoutNoCull);
        this.model.renderToBuffer(pMatrixStack, vertexconsumer, pCombinedLight, pCombinedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        pMatrixStack.popPose();
    }
}
