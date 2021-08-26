package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.phys.Vec3;

public class IllusionerRenderer extends IllagerRenderer<Illusioner>
{
    private static final ResourceLocation ILLUSIONER = new ResourceLocation("textures/entity/illager/illusioner.png");

    public IllusionerRenderer(EntityRendererProvider.Context p_174186_)
    {
        super(p_174186_, new IllagerModel<>(p_174186_.bakeLayer(ModelLayers.ILLUSIONER)), 0.5F);
        this.addLayer(new ItemInHandLayer<Illusioner, IllagerModel<Illusioner>>(this)
        {
            public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, Illusioner pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
            {
                if (pLivingEntity.isCastingSpell() || pLivingEntity.isAggressive())
                {
                    super.render(pMatrixStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pPartialTicks, pAgeInTicks, pNetHeadYaw, pHeadPitch);
                }
            }
        });
        this.model.getHat().visible = true;
    }

    public ResourceLocation getTextureLocation(Illusioner pEntity)
    {
        return ILLUSIONER;
    }

    public void render(Illusioner pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight)
    {
        if (pEntity.isInvisible())
        {
            Vec3[] avec3 = pEntity.getIllusionOffsets(pPartialTicks);
            float f = this.getBob(pEntity, pPartialTicks);

            for (int i = 0; i < avec3.length; ++i)
            {
                pMatrixStack.pushPose();
                pMatrixStack.translate(avec3[i].x + (double)Mth.cos((float)i + f * 0.5F) * 0.025D, avec3[i].y + (double)Mth.cos((float)i + f * 0.75F) * 0.0125D, avec3[i].z + (double)Mth.cos((float)i + f * 0.7F) * 0.025D);
                super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
                pMatrixStack.popPose();
            }
        }
        else
        {
            super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
        }
    }

    protected boolean isBodyVisible(Illusioner pLivingEntity)
    {
        return true;
    }
}
