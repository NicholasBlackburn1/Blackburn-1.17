package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Vindicator;

public class VindicatorRenderer extends IllagerRenderer<Vindicator>
{
    private static final ResourceLocation VINDICATOR = new ResourceLocation("textures/entity/illager/vindicator.png");

    public VindicatorRenderer(EntityRendererProvider.Context p_174439_)
    {
        super(p_174439_, new IllagerModel<>(p_174439_.bakeLayer(ModelLayers.VINDICATOR)), 0.5F);
        this.addLayer(new ItemInHandLayer<Vindicator, IllagerModel<Vindicator>>(this)
        {
            public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, Vindicator pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
            {
                if (pLivingEntity.isAggressive())
                {
                    super.render(pMatrixStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pPartialTicks, pAgeInTicks, pNetHeadYaw, pHeadPitch);
                }
            }
        });
    }

    public ResourceLocation getTextureLocation(Vindicator pEntity)
    {
        return VINDICATOR;
    }
}
