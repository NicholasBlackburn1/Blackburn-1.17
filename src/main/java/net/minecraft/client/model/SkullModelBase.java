package net.minecraft.client.model;

import net.minecraft.client.renderer.RenderType;

public abstract class SkullModelBase extends Model
{
    public SkullModelBase()
    {
        super(RenderType::entityTranslucent);
    }

    public abstract void setupAnim(float p_170950_, float p_170951_, float p_170952_);
}
