package net.optifine.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ModelPlayerItem extends Model
{
    public ModelPlayerItem(Function<ResourceLocation, RenderType> p_103110_)
    {
        super(p_103110_);
    }

    public void renderToBuffer(PoseStack pMatrixStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha)
    {
    }
}
