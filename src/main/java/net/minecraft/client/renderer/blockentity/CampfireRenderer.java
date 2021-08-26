package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;

public class CampfireRenderer implements BlockEntityRenderer<CampfireBlockEntity>
{
    private static final float SIZE = 0.375F;

    public CampfireRenderer(BlockEntityRendererProvider.Context p_173602_)
    {
    }

    public void render(CampfireBlockEntity pBlockEntity, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay)
    {
        Direction direction = pBlockEntity.getBlockState().getValue(CampfireBlock.FACING);
        NonNullList<ItemStack> nonnulllist = pBlockEntity.getItems();
        int i = (int)pBlockEntity.getBlockPos().asLong();

        for (int j = 0; j < nonnulllist.size(); ++j)
        {
            ItemStack itemstack = nonnulllist.get(j);

            if (itemstack != ItemStack.EMPTY)
            {
                pMatrixStack.pushPose();
                pMatrixStack.translate(0.5D, 0.44921875D, 0.5D);
                Direction direction1 = Direction.from2DDataValue((j + direction.get2DDataValue()) % 4);
                float f = -direction1.toYRot();
                pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f));
                pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
                pMatrixStack.translate(-0.3125D, -0.3125D, 0.0D);
                pMatrixStack.scale(0.375F, 0.375F, 0.375F);
                Minecraft.getInstance().getItemRenderer().renderStatic(itemstack, ItemTransforms.TransformType.FIXED, pCombinedLight, pCombinedOverlay, pMatrixStack, pBuffer, i + j);
                pMatrixStack.popPose();
            }
        }
    }
}
