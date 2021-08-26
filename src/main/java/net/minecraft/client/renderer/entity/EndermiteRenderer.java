package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.EndermiteModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Endermite;

public class EndermiteRenderer extends MobRenderer<Endermite, EndermiteModel<Endermite>>
{
    private static final ResourceLocation ENDERMITE_LOCATION = new ResourceLocation("textures/entity/endermite.png");

    public EndermiteRenderer(EntityRendererProvider.Context p_173994_)
    {
        super(p_173994_, new EndermiteModel<>(p_173994_.bakeLayer(ModelLayers.ENDERMITE)), 0.3F);
    }

    protected float getFlipDegrees(Endermite pLivingEntity)
    {
        return 180.0F;
    }

    public ResourceLocation getTextureLocation(Endermite pEntity)
    {
        return ENDERMITE_LOCATION;
    }
}
