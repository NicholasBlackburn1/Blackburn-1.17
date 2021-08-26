package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.Vec3;

public class BeaconRenderer implements BlockEntityRenderer<BeaconBlockEntity>
{
    public static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/beacon_beam.png");
    public static final int MAX_RENDER_Y = 1024;

    public BeaconRenderer(BlockEntityRendererProvider.Context p_173529_)
    {
    }

    public void render(BeaconBlockEntity pBlockEntity, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay)
    {
        long i = pBlockEntity.getLevel().getGameTime();
        List<BeaconBlockEntity.BeaconBeamSection> list = pBlockEntity.getBeamSections();
        int j = 0;

        for (int k = 0; k < list.size(); ++k)
        {
            BeaconBlockEntity.BeaconBeamSection beaconblockentity$beaconbeamsection = list.get(k);
            m_112176_(pMatrixStack, pBuffer, pPartialTicks, i, j, k == list.size() - 1 ? 1024 : beaconblockentity$beaconbeamsection.getHeight(), beaconblockentity$beaconbeamsection.getColor());
            j += beaconblockentity$beaconbeamsection.getHeight();
        }
    }

    private static void m_112176_(PoseStack p_112177_, MultiBufferSource p_112178_, float p_112179_, long p_112180_, int p_112181_, int p_112182_, float[] p_112183_)
    {
        m_112184_(p_112177_, p_112178_, BEAM_LOCATION, p_112179_, 1.0F, p_112180_, p_112181_, p_112182_, p_112183_, 0.2F, 0.25F);
    }

    public static void m_112184_(PoseStack p_112185_, MultiBufferSource p_112186_, ResourceLocation p_112187_, float p_112188_, float p_112189_, long p_112190_, int p_112191_, int p_112192_, float[] p_112193_, float p_112194_, float p_112195_)
    {
        int i = p_112191_ + p_112192_;
        p_112185_.pushPose();
        p_112185_.translate(0.5D, 0.0D, 0.5D);
        float f = (float)Math.floorMod(p_112190_, 40) + p_112188_;
        float f1 = p_112192_ < 0 ? f : -f;
        float f2 = Mth.frac(f1 * 0.2F - (float)Mth.floor(f1 * 0.1F));
        float f3 = p_112193_[0];
        float f4 = p_112193_[1];
        float f5 = p_112193_[2];
        p_112185_.pushPose();
        p_112185_.mulPose(Vector3f.YP.rotationDegrees(f * 2.25F - 45.0F));
        float f6 = 0.0F;
        float f8 = 0.0F;
        float f9 = -p_112194_;
        float f10 = 0.0F;
        float f11 = 0.0F;
        float f12 = -p_112194_;
        float f13 = 0.0F;
        float f14 = 1.0F;
        float f15 = -1.0F + f2;
        float f16 = (float)p_112192_ * p_112189_ * (0.5F / p_112194_) + f15;
        renderPart(p_112185_, p_112186_.getBuffer(RenderType.beaconBeam(p_112187_, false)), f3, f4, f5, 1.0F, p_112191_, i, 0.0F, p_112194_, p_112194_, 0.0F, f9, 0.0F, 0.0F, f12, 0.0F, 1.0F, f16, f15);
        p_112185_.popPose();
        f6 = -p_112195_;
        float f7 = -p_112195_;
        f8 = -p_112195_;
        f9 = -p_112195_;
        f13 = 0.0F;
        f14 = 1.0F;
        f15 = -1.0F + f2;
        f16 = (float)p_112192_ * p_112189_ + f15;
        renderPart(p_112185_, p_112186_.getBuffer(RenderType.beaconBeam(p_112187_, true)), f3, f4, f5, 0.125F, p_112191_, i, f6, f7, p_112195_, f8, f9, p_112195_, p_112195_, p_112195_, 0.0F, 1.0F, f16, f15);
        p_112185_.popPose();
    }

    private static void renderPart(PoseStack pMatrixStack, VertexConsumer pBuffer, float pRed, float pGreen, float pBlue, float pAlpha, int pYMin, int pYMax, float p_112164_, float p_112165_, float p_112166_, float p_112167_, float p_112168_, float p_112169_, float p_112170_, float p_112171_, float pU1, float pU2, float pV1, float pV2)
    {
        PoseStack.Pose posestack$pose = pMatrixStack.last();
        Matrix4f matrix4f = posestack$pose.pose();
        Matrix3f matrix3f = posestack$pose.normal();
        renderQuad(matrix4f, matrix3f, pBuffer, pRed, pGreen, pBlue, pAlpha, pYMin, pYMax, p_112164_, p_112165_, p_112166_, p_112167_, pU1, pU2, pV1, pV2);
        renderQuad(matrix4f, matrix3f, pBuffer, pRed, pGreen, pBlue, pAlpha, pYMin, pYMax, p_112170_, p_112171_, p_112168_, p_112169_, pU1, pU2, pV1, pV2);
        renderQuad(matrix4f, matrix3f, pBuffer, pRed, pGreen, pBlue, pAlpha, pYMin, pYMax, p_112166_, p_112167_, p_112170_, p_112171_, pU1, pU2, pV1, pV2);
        renderQuad(matrix4f, matrix3f, pBuffer, pRed, pGreen, pBlue, pAlpha, pYMin, pYMax, p_112168_, p_112169_, p_112164_, p_112165_, pU1, pU2, pV1, pV2);
    }

    private static void renderQuad(Matrix4f pMatrixPos, Matrix3f pMatrixNormal, VertexConsumer pBuffer, float pRed, float pGreen, float pBlue, float pAlpha, int pYMin, int pYMax, float pX1, float pZ1, float pX2, float pZ2, float pU1, float pU2, float pV1, float pV2)
    {
        addVertex(pMatrixPos, pMatrixNormal, pBuffer, pRed, pGreen, pBlue, pAlpha, pYMax, pX1, pZ1, pU2, pV1);
        addVertex(pMatrixPos, pMatrixNormal, pBuffer, pRed, pGreen, pBlue, pAlpha, pYMin, pX1, pZ1, pU2, pV2);
        addVertex(pMatrixPos, pMatrixNormal, pBuffer, pRed, pGreen, pBlue, pAlpha, pYMin, pX2, pZ2, pU1, pV2);
        addVertex(pMatrixPos, pMatrixNormal, pBuffer, pRed, pGreen, pBlue, pAlpha, pYMax, pX2, pZ2, pU1, pV1);
    }

    private static void addVertex(Matrix4f pMatrixPos, Matrix3f pMatrixNormal, VertexConsumer pBuffer, float pRed, float pGreen, float pBlue, float pAlpha, int pY, float pX, float pZ, float pTexU, float pTexV)
    {
        pBuffer.vertex(pMatrixPos, pX, (float)pY, pZ).color(pRed, pGreen, pBlue, pAlpha).uv(pTexU, pTexV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(pMatrixNormal, 0.0F, 1.0F, 0.0F).endVertex();
    }

    public boolean shouldRenderOffScreen(BeaconBlockEntity pTe)
    {
        return true;
    }

    public int getViewDistance()
    {
        return 256;
    }

    public boolean shouldRender(BeaconBlockEntity p_173531_, Vec3 p_173532_)
    {
        return Vec3.atCenterOf(p_173531_.getBlockPos()).multiply(1.0D, 0.0D, 1.0D).closerThan(p_173532_.multiply(1.0D, 0.0D, 1.0D), (double)this.getViewDistance());
    }
}
