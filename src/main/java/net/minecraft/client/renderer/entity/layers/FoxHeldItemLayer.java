package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.item.ItemStack;

public class FoxHeldItemLayer extends RenderLayer<Fox, FoxModel<Fox>>
{
    public FoxHeldItemLayer(RenderLayerParent<Fox, FoxModel<Fox>> p_116994_)
    {
        super(p_116994_);
    }

    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, Fox pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
    {
        boolean flag = pLivingEntity.isSleeping();
        boolean flag1 = pLivingEntity.isBaby();
        pMatrixStack.pushPose();

        if (flag1)
        {
            float f = 0.75F;
            pMatrixStack.scale(0.75F, 0.75F, 0.75F);
            pMatrixStack.translate(0.0D, 0.5D, (double)0.209375F);
        }

        pMatrixStack.translate((double)((this.getParentModel()).head.x / 16.0F), (double)((this.getParentModel()).head.y / 16.0F), (double)((this.getParentModel()).head.z / 16.0F));
        float f1 = pLivingEntity.getHeadRollAngle(pPartialTicks);
        pMatrixStack.mulPose(Vector3f.ZP.rotation(f1));
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(pNetHeadYaw));
        pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(pHeadPitch));

        if (pLivingEntity.isBaby())
        {
            if (flag)
            {
                pMatrixStack.translate((double)0.4F, (double)0.26F, (double)0.15F);
            }
            else
            {
                pMatrixStack.translate((double)0.06F, (double)0.26F, -0.5D);
            }
        }
        else if (flag)
        {
            pMatrixStack.translate((double)0.46F, (double)0.26F, (double)0.22F);
        }
        else
        {
            pMatrixStack.translate((double)0.06F, (double)0.27F, -0.5D);
        }

        pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));

        if (flag)
        {
            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
        }

        ItemStack itemstack = pLivingEntity.getItemBySlot(EquipmentSlot.MAINHAND);
        Minecraft.getInstance().getItemInHandRenderer().renderItem(pLivingEntity, itemstack, ItemTransforms.TransformType.GROUND, false, pMatrixStack, pBuffer, pPackedLight);
        pMatrixStack.popPose();
    }
}
