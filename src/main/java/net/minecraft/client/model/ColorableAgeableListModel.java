package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.entity.Entity;

public abstract class ColorableAgeableListModel<E extends Entity> extends AgeableListModel<E>
{
    private float r = 1.0F;
    private float g = 1.0F;
    private float b = 1.0F;

    public void setColor(float p_102420_, float p_102421_, float p_102422_)
    {
        this.r = p_102420_;
        this.g = p_102421_;
        this.b = p_102422_;
    }

    public void renderToBuffer(PoseStack pMatrixStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha)
    {
        super.renderToBuffer(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, this.r * pRed, this.g * pGreen, this.b * pBlue, pAlpha);
    }
}
