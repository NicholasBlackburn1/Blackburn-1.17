package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.level.block.state.BlockState;
import net.optifine.Config;
import net.optifine.shaders.Shaders;

public class TntMinecartRenderer extends MinecartRenderer<MinecartTNT>
{
    public TntMinecartRenderer(EntityRendererProvider.Context p_174424_)
    {
        super(p_174424_, ModelLayers.TNT_MINECART);
    }

    protected void renderMinecartContents(MinecartTNT pEntity, float pPartialTicks, BlockState pState, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight)
    {
        int i = pEntity.getFuse();

        if (i > -1 && (float)i - pPartialTicks + 1.0F < 10.0F)
        {
            float f = 1.0F - ((float)i - pPartialTicks + 1.0F) / 10.0F;
            f = Mth.clamp(f, 0.0F, 1.0F);
            f = f * f;
            f = f * f;
            float f1 = 1.0F + f * 0.3F;
            pMatrixStack.scale(f1, f1, f1);
        }

        renderWhiteSolidBlock(pState, pMatrixStack, pBuffer, pPackedLight, i > -1 && i / 5 % 2 == 0);
    }

    public static void renderWhiteSolidBlock(BlockState pBlockState, PoseStack pMatrixStack, MultiBufferSource pRenderTypeBuffer, int pCombinedLight, boolean pDoFullBright)
    {
        int i;

        if (pDoFullBright)
        {
            i = OverlayTexture.pack(OverlayTexture.u(1.0F), 10);
        }
        else
        {
            i = OverlayTexture.NO_OVERLAY;
        }

        if (Config.isShaders() && pDoFullBright)
        {
            Shaders.setEntityColor(1.0F, 1.0F, 1.0F, 0.5F);
        }

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(pBlockState, pMatrixStack, pRenderTypeBuffer, pCombinedLight, i);

        if (Config.isShaders())
        {
            Shaders.setEntityColor(0.0F, 0.0F, 0.0F, 0.0F);
        }
    }
}
