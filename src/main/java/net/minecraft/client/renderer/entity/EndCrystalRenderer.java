package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;

public class EndCrystalRenderer extends EntityRenderer<EndCrystal>
{
    private static final ResourceLocation END_CRYSTAL_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(END_CRYSTAL_LOCATION);
    private static final float SIN_45 = (float)Math.sin((Math.PI / 4D));
    private static final String GLASS = "glass";
    private static final String BASE = "base";
    private final ModelPart cube;
    private final ModelPart glass;
    private final ModelPart base;

    public EndCrystalRenderer(EntityRendererProvider.Context p_173970_)
    {
        super(p_173970_);
        this.shadowRadius = 0.5F;
        ModelPart modelpart = p_173970_.bakeLayer(ModelLayers.END_CRYSTAL);
        this.glass = modelpart.getChilds("glass");
        this.cube = modelpart.getChilds("cube");
        this.base = modelpart.getChilds("base");
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("glass", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 16).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 4.0F, 12.0F), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    public void render(EndCrystal pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight)
    {
        pMatrixStack.pushPose();
        float f = getY(pEntity, pPartialTicks);
        float f1 = ((float)pEntity.time + pPartialTicks) * 3.0F;
        VertexConsumer vertexconsumer = pBuffer.getBuffer(RENDER_TYPE);
        pMatrixStack.pushPose();
        pMatrixStack.scale(2.0F, 2.0F, 2.0F);
        pMatrixStack.translate(0.0D, -0.5D, 0.0D);
        int i = OverlayTexture.NO_OVERLAY;

        if (pEntity.showsBottom())
        {
            this.base.render(pMatrixStack, vertexconsumer, pPackedLight, i);
        }

        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f1));
        pMatrixStack.translate(0.0D, (double)(1.5F + f / 2.0F), 0.0D);
        pMatrixStack.mulPose(new Quaternion(new Vector3f(SIN_45, 0.0F, SIN_45), 60.0F, true));
        this.glass.render(pMatrixStack, vertexconsumer, pPackedLight, i);
        float f2 = 0.875F;
        pMatrixStack.scale(0.875F, 0.875F, 0.875F);
        pMatrixStack.mulPose(new Quaternion(new Vector3f(SIN_45, 0.0F, SIN_45), 60.0F, true));
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f1));
        this.glass.render(pMatrixStack, vertexconsumer, pPackedLight, i);
        pMatrixStack.scale(0.875F, 0.875F, 0.875F);
        pMatrixStack.mulPose(new Quaternion(new Vector3f(SIN_45, 0.0F, SIN_45), 60.0F, true));
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f1));
        this.cube.render(pMatrixStack, vertexconsumer, pPackedLight, i);
        pMatrixStack.popPose();
        pMatrixStack.popPose();
        BlockPos blockpos = pEntity.getBeamTarget();

        if (blockpos != null)
        {
            float f3 = (float)blockpos.getX() + 0.5F;
            float f4 = (float)blockpos.getY() + 0.5F;
            float f5 = (float)blockpos.getZ() + 0.5F;
            float f6 = (float)((double)f3 - pEntity.getX());
            float f7 = (float)((double)f4 - pEntity.getY());
            float f8 = (float)((double)f5 - pEntity.getZ());
            pMatrixStack.translate((double)f6, (double)f7, (double)f8);
            EnderDragonRenderer.renderCrystalBeams(-f6, -f7 + f, -f8, pPartialTicks, pEntity.time, pMatrixStack, pBuffer, pPackedLight);
        }

        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

    public static float getY(EndCrystal p_114159_, float p_114160_)
    {
        float f = (float)p_114159_.time + p_114160_;
        float f1 = Mth.sin(f * 0.2F) / 2.0F + 0.5F;
        f1 = (f1 * f1 + f1) * 0.4F;
        return f1 - 1.4F;
    }

    public ResourceLocation getTextureLocation(EndCrystal pEntity)
    {
        return END_CRYSTAL_LOCATION;
    }

    public boolean shouldRender(EndCrystal pLivingEntity, Frustum pCamera, double pCamX, double p_114172_, double pCamY)
    {
        return super.shouldRender(pLivingEntity, pCamera, pCamX, p_114172_, pCamY) || pLivingEntity.getBeamTarget() != null;
    }
}
