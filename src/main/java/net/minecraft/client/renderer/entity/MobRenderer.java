package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.optifine.Config;
import net.optifine.shaders.Shaders;

public abstract class MobRenderer<T extends Mob, M extends EntityModel<T>> extends LivingEntityRenderer<T, M>
{
    public static final int LEASH_RENDER_STEPS = 24;

    public MobRenderer(EntityRendererProvider.Context p_174304_, M p_174305_, float p_174306_)
    {
        super(p_174304_, p_174305_, p_174306_);
    }

    protected boolean shouldShowName(T pEntity)
    {
        return super.shouldShowName(pEntity) && (pEntity.shouldShowName() || pEntity.hasCustomName() && pEntity == this.entityRenderDispatcher.crosshairPickEntity);
    }

    public boolean shouldRender(T pLivingEntity, Frustum pCamera, double pCamX, double p_115471_, double pCamY)
    {
        if (super.shouldRender(pLivingEntity, pCamera, pCamX, p_115471_, pCamY))
        {
            return true;
        }
        else
        {
            Entity entity = pLivingEntity.getLeashHolder();
            return entity != null ? pCamera.isVisible(entity.getBoundingBoxForCulling()) : false;
        }
    }

    public void render(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight)
    {
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
        Entity entity = pEntity.getLeashHolder();

        if (entity != null)
        {
            this.renderLeash(pEntity, pPartialTicks, pMatrixStack, pBuffer, entity);
        }
    }

    private <E extends Entity> void renderLeash(T pEntityLiving, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, E pLeashHolder)
    {
        if (!Config.isShaders() || !Shaders.isShadowPass)
        {
            pMatrixStack.pushPose();
            Vec3 vec3 = pLeashHolder.getRopeHoldPosition(pPartialTicks);
            double d0 = (double)(Mth.lerp(pPartialTicks, pEntityLiving.yBodyRot, pEntityLiving.yBodyRotO) * ((float)Math.PI / 180F)) + (Math.PI / 2D);
            Vec3 vec31 = pEntityLiving.getLeashOffset();
            double d1 = Math.cos(d0) * vec31.z + Math.sin(d0) * vec31.x;
            double d2 = Math.sin(d0) * vec31.z - Math.cos(d0) * vec31.x;
            double d3 = Mth.lerp((double)pPartialTicks, pEntityLiving.xo, pEntityLiving.getX()) + d1;
            double d4 = Mth.lerp((double)pPartialTicks, pEntityLiving.yo, pEntityLiving.getY()) + vec31.y;
            double d5 = Mth.lerp((double)pPartialTicks, pEntityLiving.zo, pEntityLiving.getZ()) + d2;
            pMatrixStack.translate(d1, vec31.y, d2);
            float f = (float)(vec3.x - d3);
            float f1 = (float)(vec3.y - d4);
            float f2 = (float)(vec3.z - d5);
            float f3 = 0.025F;
            VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.leash());
            Matrix4f matrix4f = pMatrixStack.last().pose();
            float f4 = Mth.fastInvSqrt(f * f + f2 * f2) * 0.025F / 2.0F;
            float f5 = f2 * f4;
            float f6 = f * f4;
            BlockPos blockpos = new BlockPos(pEntityLiving.getEyePosition(pPartialTicks));
            BlockPos blockpos1 = new BlockPos(pLeashHolder.getEyePosition(pPartialTicks));
            int i = this.getBlockLightLevel(pEntityLiving, blockpos);
            int j = this.entityRenderDispatcher.getRenderer(pLeashHolder).getBlockLightLevel(pLeashHolder, blockpos1);
            int k = pEntityLiving.level.getBrightness(LightLayer.SKY, blockpos);
            int l = pEntityLiving.level.getBrightness(LightLayer.SKY, blockpos1);

            if (Config.isShaders())
            {
                Shaders.beginLeash();
            }

            for (int i1 = 0; i1 <= 24; ++i1)
            {
                addVertexPair(vertexconsumer, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.025F, f5, f6, i1, false);
            }

            for (int j1 = 24; j1 >= 0; --j1)
            {
                addVertexPair(vertexconsumer, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.0F, f5, f6, j1, true);
            }

            if (Config.isShaders())
            {
                Shaders.endLeash();
            }

            pMatrixStack.popPose();
        }
    }

    private static void addVertexPair(VertexConsumer p_174308_, Matrix4f p_174309_, float p_174310_, float p_174311_, float p_174312_, int p_174313_, int p_174314_, int p_174315_, int p_174316_, float p_174317_, float p_174318_, float p_174319_, float p_174320_, int p_174321_, boolean p_174322_)
    {
        float f = (float)p_174321_ / 24.0F;
        int i = (int)Mth.lerp(f, (float)p_174313_, (float)p_174314_);
        int j = (int)Mth.lerp(f, (float)p_174315_, (float)p_174316_);
        int k = LightTexture.pack(i, j);
        float f1 = p_174321_ % 2 == (p_174322_ ? 1 : 0) ? 0.7F : 1.0F;
        float f2 = 0.5F * f1;
        float f3 = 0.4F * f1;
        float f4 = 0.3F * f1;
        float f5 = p_174310_ * f;
        float f6 = p_174311_ > 0.0F ? p_174311_ * f * f : p_174311_ - p_174311_ * (1.0F - f) * (1.0F - f);
        float f7 = p_174312_ * f;
        p_174308_.vertex(p_174309_, f5 - p_174319_, f6 + p_174318_, f7 + p_174320_).color(f2, f3, f4, 1.0F).uv2(k).endVertex();
        p_174308_.vertex(p_174309_, f5 + p_174319_, f6 + p_174317_ - p_174318_, f7 - p_174320_).color(f2, f3, f4, 1.0F).uv2(k).endVertex();
    }
}
