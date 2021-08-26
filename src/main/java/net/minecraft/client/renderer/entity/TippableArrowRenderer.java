package net.minecraft.client.renderer.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.Arrow;

public class TippableArrowRenderer extends ArrowRenderer<Arrow>
{
    public static final ResourceLocation NORMAL_ARROW_LOCATION = new ResourceLocation("textures/entity/projectiles/arrow.png");
    public static final ResourceLocation TIPPED_ARROW_LOCATION = new ResourceLocation("textures/entity/projectiles/tipped_arrow.png");

    public TippableArrowRenderer(EntityRendererProvider.Context p_174422_)
    {
        super(p_174422_);
    }

    public ResourceLocation getTextureLocation(Arrow pEntity)
    {
        return pEntity.getColor() > 0 ? TIPPED_ARROW_LOCATION : NORMAL_ARROW_LOCATION;
    }
}
