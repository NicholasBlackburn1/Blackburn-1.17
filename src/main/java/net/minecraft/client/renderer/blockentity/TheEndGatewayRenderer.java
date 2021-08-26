package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;

public class TheEndGatewayRenderer extends TheEndPortalRenderer<TheEndGatewayBlockEntity>
{
    private static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/end_gateway_beam.png");

    public TheEndGatewayRenderer(BlockEntityRendererProvider.Context p_173683_)
    {
        super(p_173683_);
    }

    public void render(TheEndGatewayBlockEntity pBlockEntity, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay)
    {
        if (pBlockEntity.isSpawning() || pBlockEntity.isCoolingDown())
        {
            float f = pBlockEntity.isSpawning() ? pBlockEntity.getSpawnPercent(pPartialTicks) : pBlockEntity.getCooldownPercent(pPartialTicks);
            double d0 = pBlockEntity.isSpawning() ? (double)pBlockEntity.getLevel().getMaxBuildHeight() : 50.0D;
            f = Mth.sin(f * (float)Math.PI);
            int i = Mth.floor((double)f * d0);
            float[] afloat = pBlockEntity.isSpawning() ? DyeColor.MAGENTA.getTextureDiffuseColors() : DyeColor.PURPLE.getTextureDiffuseColors();
            long j = pBlockEntity.getLevel().getGameTime();
            BeaconRenderer.m_112184_(pMatrixStack, pBuffer, BEAM_LOCATION, pPartialTicks, f, j, -i, i * 2, afloat, 0.15F, 0.175F);
        }

        super.render(pBlockEntity, pPartialTicks, pMatrixStack, pBuffer, pCombinedLight, pCombinedOverlay);
    }

    protected float getOffsetUp()
    {
        return 1.0F;
    }

    protected float getOffsetDown()
    {
        return 0.0F;
    }

    protected RenderType renderType()
    {
        return RenderType.endGateway();
    }

    public int getViewDistance()
    {
        return 256;
    }
}
