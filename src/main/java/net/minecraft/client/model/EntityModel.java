package net.minecraft.client.model;

import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public abstract class EntityModel<T extends Entity> extends Model
{
    public float attackTime;
    public boolean riding;
    public boolean young = true;

    protected EntityModel()
    {
        this(RenderType::entityCutoutNoCull);
    }

    protected EntityModel(Function<ResourceLocation, RenderType> p_102613_)
    {
        super(p_102613_);
    }

    public abstract void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch);

    public void prepareMobModel(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick)
    {
    }

    public void copyPropertiesTo(EntityModel<T> p_102625_)
    {
        p_102625_.attackTime = this.attackTime;
        p_102625_.riding = this.riding;
        p_102625_.young = this.young;
    }
}
