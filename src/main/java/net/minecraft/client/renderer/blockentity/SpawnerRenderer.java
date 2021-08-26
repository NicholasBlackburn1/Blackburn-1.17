package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

public class SpawnerRenderer implements BlockEntityRenderer<SpawnerBlockEntity>
{
    public SpawnerRenderer(BlockEntityRendererProvider.Context p_173673_)
    {
    }

    public void render(SpawnerBlockEntity pBlockEntity, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay)
    {
        pMatrixStack.pushPose();
        pMatrixStack.translate(0.5D, 0.0D, 0.5D);
        BaseSpawner basespawner = pBlockEntity.getSpawner();
        Entity entity = basespawner.getOrCreateDisplayEntity(pBlockEntity.getLevel());

        if (entity != null)
        {
            float f = 0.53125F;
            float f1 = Math.max(entity.getBbWidth(), entity.getBbHeight());

            if ((double)f1 > 1.0D)
            {
                f /= f1;
            }

            pMatrixStack.translate(0.0D, (double)0.4F, 0.0D);
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees((float)Mth.lerp((double)pPartialTicks, basespawner.getoSpin(), basespawner.getSpin()) * 10.0F));
            pMatrixStack.translate(0.0D, (double) - 0.2F, 0.0D);
            pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(-30.0F));
            pMatrixStack.scale(f, f, f);
            Minecraft.getInstance().getEntityRenderDispatcher().render(entity, 0.0D, 0.0D, 0.0D, 0.0F, pPartialTicks, pMatrixStack, pBuffer, pCombinedLight);
        }

        pMatrixStack.popPose();
    }
}
