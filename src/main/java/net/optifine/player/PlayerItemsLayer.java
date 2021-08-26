package net.optifine.player;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.optifine.Config;

public class PlayerItemsLayer extends RenderLayer
{
    private PlayerRenderer renderPlayer = null;

    public PlayerItemsLayer(PlayerRenderer renderPlayer)
    {
        super(renderPlayer);
        this.renderPlayer = renderPlayer;
    }

    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, Entity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
    {
        this.renderEquippedItems(pLivingEntity, pMatrixStack, pBuffer, pPackedLight, OverlayTexture.NO_OVERLAY);
    }

    protected void renderEquippedItems(Entity entityLiving, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, int packedOverlayIn)
    {
        if (Config.isShowCapes())
        {
            if (!entityLiving.isInvisible())
            {
                if (entityLiving instanceof AbstractClientPlayer)
                {
                    AbstractClientPlayer abstractclientplayer = (AbstractClientPlayer)entityLiving;
                    HumanoidModel humanoidmodel = this.renderPlayer.getModel();
                    PlayerConfigurations.renderPlayerItems(humanoidmodel, abstractclientplayer, matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                }
            }
        }
    }
}
