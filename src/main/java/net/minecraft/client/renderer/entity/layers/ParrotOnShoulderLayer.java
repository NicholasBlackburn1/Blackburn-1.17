package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.optifine.Config;
import net.optifine.shaders.Shaders;

public class ParrotOnShoulderLayer<T extends Player> extends RenderLayer<T, PlayerModel<T>>
{
    private final ParrotModel model;

    public ParrotOnShoulderLayer(RenderLayerParent<T, PlayerModel<T>> p_174511_, EntityModelSet p_174512_)
    {
        super(p_174511_);
        this.model = new ParrotModel(p_174512_.bakeLayer(ModelLayers.PARROT));
    }

    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
    {
        this.render(pMatrixStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pNetHeadYaw, pHeadPitch, true);
        this.render(pMatrixStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pNetHeadYaw, pHeadPitch, false);
    }

    private void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, boolean pNetHeadYaw)
    {
        CompoundTag compoundtag = pNetHeadYaw ? pLivingEntity.getShoulderEntityLeft() : pLivingEntity.getShoulderEntityRight();
        EntityType.byString(compoundtag.getString("id")).filter((entityTypeIn) ->
        {
            return entityTypeIn == EntityType.PARROT;
        }).ifPresent((entityTypeIn) ->
        {
            Entity entity = Config.getRenderGlobal().renderedEntity;

            if (pLivingEntity instanceof AbstractClientPlayer)
            {
                AbstractClientPlayer abstractclientplayer = (AbstractClientPlayer)pLivingEntity;
                Entity entity1 = pNetHeadYaw ? abstractclientplayer.entityShoulderLeft : abstractclientplayer.entityShoulderRight;

                if (entity1 != null)
                {
                    Config.getRenderGlobal().renderedEntity = entity1;

                    if (Config.isShaders())
                    {
                        Shaders.nextEntity(entity1);
                    }
                }
            }

            pMatrixStack.pushPose();
            pMatrixStack.translate(pNetHeadYaw ? (double)0.4F : (double) - 0.4F, pLivingEntity.isCrouching() ? (double) - 1.3F : -1.5D, 0.0D);
            VertexConsumer vertexconsumer = pBuffer.getBuffer(this.model.renderType(ParrotRenderer.PARROT_LOCATIONS[compoundtag.getInt("Variant")]));
            this.model.renderOnShoulder(pMatrixStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, pLimbSwing, pLimbSwingAmount, pPartialTicks, pAgeInTicks, pLivingEntity.tickCount);
            pMatrixStack.popPose();
            Config.getRenderGlobal().renderedEntity = entity;

            if (Config.isShaders())
            {
                Shaders.nextEntity(entity);
            }
        });
    }
}
