package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;

public class CarriedBlockLayer extends RenderLayer<EnderMan, EndermanModel<EnderMan>>
{
    public CarriedBlockLayer(RenderLayerParent<EnderMan, EndermanModel<EnderMan>> p_116626_)
    {
        super(p_116626_);
    }

    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, EnderMan pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
    {
        BlockState blockstate = pLivingEntity.getCarriedBlock();

        if (blockstate != null)
        {
            pMatrixStack.pushPose();
            pMatrixStack.translate(0.0D, 0.6875D, -0.75D);
            pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(20.0F));
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(45.0F));
            pMatrixStack.translate(0.25D, 0.1875D, 0.25D);
            float f = 0.5F;
            pMatrixStack.scale(-0.5F, -0.5F, 0.5F);
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockstate, pMatrixStack, pBuffer, pPackedLight, OverlayTexture.NO_OVERLAY);
            pMatrixStack.popPose();
        }
    }
}
