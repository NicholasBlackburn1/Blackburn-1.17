package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;
import net.optifine.Config;
import net.optifine.CustomColors;

public class WolfCollarLayer extends RenderLayer<Wolf, WolfModel<Wolf>>
{
    private static final ResourceLocation WOLF_COLLAR_LOCATION = new ResourceLocation("textures/entity/wolf/wolf_collar.png");

    public WolfCollarLayer(RenderLayerParent<Wolf, WolfModel<Wolf>> p_117707_)
    {
        super(p_117707_);
    }

    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, Wolf pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
    {
        if (pLivingEntity.isTame() && !pLivingEntity.isInvisible())
        {
            float[] afloat = pLivingEntity.getCollarColor().getTextureDiffuseColors();

            if (Config.isCustomColors())
            {
                afloat = CustomColors.getWolfCollarColors(pLivingEntity.getCollarColor(), afloat);
            }

            renderColoredCutoutModel(this.getParentModel(), WOLF_COLLAR_LOCATION, pMatrixStack, pBuffer, pPackedLight, pLivingEntity, afloat[0], afloat[1], afloat[2]);
        }
    }
}
