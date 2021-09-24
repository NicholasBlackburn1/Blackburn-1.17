package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.optifine.Config;

public class CapeLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>
{
    public CapeLayer(PlayerRenderer playerRenderer)
    {
        super(playerRenderer);
    }

    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, AbstractClientPlayer pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
    {
        if (pLivingEntity.isCapeLoaded() && !pLivingEntity.isInvisible() && pLivingEntity.isModelPartShown(PlayerModelPart.CAPE) && pLivingEntity.getCloakTextureLocation() != null)
        {
            ItemStack itemstack = pLivingEntity.getItemBySlot(EquipmentSlot.CHEST);

            if (!itemstack.is(Items.ELYTRA))
            {
                pMatrixStack.pushPose();
                pMatrixStack.translate(0.0D, 0.0D, 0.125D);
                double d0 = Mth.lerp((double)pPartialTicks, pLivingEntity.xCloakO, pLivingEntity.xCloak) - Mth.lerp((double)pPartialTicks, pLivingEntity.xo, pLivingEntity.getX());
                double d1 = Mth.lerp((double)pPartialTicks, pLivingEntity.yCloakO, pLivingEntity.yCloak) - Mth.lerp((double)pPartialTicks, pLivingEntity.yo, pLivingEntity.getY());
                double d2 = Mth.lerp((double)pPartialTicks, pLivingEntity.zCloakO, pLivingEntity.zCloak) - Mth.lerp((double)pPartialTicks, pLivingEntity.zo, pLivingEntity.getZ());
                float f = pLivingEntity.yBodyRotO + (pLivingEntity.yBodyRot - pLivingEntity.yBodyRotO);
                double d3 = (double)Mth.sin(f * ((float)Math.PI / 180F));
                double d4 = (double)(-Mth.cos(f * ((float)Math.PI / 180F)));
                float f1 = (float)d1 * 10.0F;
                f1 = Mth.clamp(f1, -6.0F, 32.0F);
                float f2 = (float)(d0 * d3 + d2 * d4) * 100.0F;
                f2 = Mth.clamp(f2, 0.0F, 150.0F);
                float f3 = (float)(d0 * d4 - d2 * d3) * 100.0F;
                f3 = Mth.clamp(f3, -20.0F, 20.0F);

                if (f2 < 0.0F)
                {
                    f2 = 0.0F;
                }

                if (f2 > 165.0F)
                {
                    f2 = 165.0F;
                }

                if (f1 < -5.0F)
                {
                    f1 = -5.0F;
                }

                float f4 = Mth.lerp(pPartialTicks, pLivingEntity.oBob, pLivingEntity.bob);
                f1 = f1 + Mth.sin(Mth.lerp(pPartialTicks, pLivingEntity.walkDistO, pLivingEntity.walkDist) * 6.0F) * 32.0F * f4;

                if (pLivingEntity.isCrouching())
                {
                    f1 += 25.0F;
                }

                float f5 = Config.getAverageFrameTimeSec() * 20.0F;
                f5 = Config.limit(f5, 0.02F, 1.0F);
                pLivingEntity.capeRotateX = Mth.lerp(f5, pLivingEntity.capeRotateX, 6.0F + f2 / 2.0F + f1);
                pLivingEntity.capeRotateZ = Mth.lerp(f5, pLivingEntity.capeRotateZ, f3 / 2.0F);
                pLivingEntity.capeRotateY = Mth.lerp(f5, pLivingEntity.capeRotateY, 180.0F - f3 / 2.0F);
                pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(pLivingEntity.capeRotateX));
                pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(pLivingEntity.capeRotateZ));
                pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(pLivingEntity.capeRotateY));
                VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entitySolid(pLivingEntity.getCloakTextureLocation()));
                this.getParentModel().renderCloak(pMatrixStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY);
                pMatrixStack.popPose();
            }
        }
    }
}
