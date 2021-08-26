package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;

public class ConduitRenderer implements BlockEntityRenderer<ConduitBlockEntity>
{
    public static final Material SHELL_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/conduit/base"));
    public static final Material ACTIVE_SHELL_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/conduit/cage"));
    public static final Material WIND_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/conduit/wind"));
    public static final Material VERTICAL_WIND_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/conduit/wind_vertical"));
    public static final Material OPEN_EYE_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/conduit/open_eye"));
    public static final Material CLOSED_EYE_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/conduit/closed_eye"));
    private final ModelPart eye;
    private final ModelPart wind;
    private final ModelPart shell;
    private final ModelPart cage;
    private final BlockEntityRenderDispatcher renderer;

    public ConduitRenderer(BlockEntityRendererProvider.Context p_173613_)
    {
        this.renderer = p_173613_.getBlockEntityRenderDispatcher();
        this.eye = p_173613_.bakeLayer(ModelLayers.CONDUIT_EYE);
        this.wind = p_173613_.bakeLayer(ModelLayers.CONDUIT_WIND);
        this.shell = p_173613_.bakeLayer(ModelLayers.CONDUIT_SHELL);
        this.cage = p_173613_.bakeLayer(ModelLayers.CONDUIT_CAGE);
    }

    public static LayerDefinition createEyeLayer()
    {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    public static LayerDefinition createWindLayer()
    {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("wind", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    public static LayerDefinition createShellLayer()
    {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("shell", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 32, 16);
    }

    public static LayerDefinition createCageLayer()
    {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("shell", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 32, 16);
    }

    public void render(ConduitBlockEntity pBlockEntity, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay)
    {
        float f = (float)pBlockEntity.tickCount + pPartialTicks;

        if (!pBlockEntity.isActive())
        {
            float f5 = pBlockEntity.getActiveRotation(0.0F);
            VertexConsumer vertexconsumer1 = SHELL_TEXTURE.buffer(pBuffer, RenderType::entitySolid);
            pMatrixStack.pushPose();
            pMatrixStack.translate(0.5D, 0.5D, 0.5D);
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f5));
            this.shell.render(pMatrixStack, vertexconsumer1, pCombinedLight, pCombinedOverlay);
            pMatrixStack.popPose();
        }
        else
        {
            float f1 = pBlockEntity.getActiveRotation(pPartialTicks) * (180F / (float)Math.PI);
            float f2 = Mth.sin(f * 0.1F) / 2.0F + 0.5F;
            f2 = f2 * f2 + f2;
            pMatrixStack.pushPose();
            pMatrixStack.translate(0.5D, (double)(0.3F + f2 * 0.2F), 0.5D);
            Vector3f vector3f = new Vector3f(0.5F, 1.0F, 0.5F);
            vector3f.normalize();
            pMatrixStack.mulPose(vector3f.rotationDegrees(f1));
            this.cage.render(pMatrixStack, ACTIVE_SHELL_TEXTURE.buffer(pBuffer, RenderType::entityCutoutNoCull), pCombinedLight, pCombinedOverlay);
            pMatrixStack.popPose();
            int i = pBlockEntity.tickCount / 66 % 3;
            pMatrixStack.pushPose();
            pMatrixStack.translate(0.5D, 0.5D, 0.5D);

            if (i == 1)
            {
                pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            }
            else if (i == 2)
            {
                pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
            }

            VertexConsumer vertexconsumer = (i == 1 ? VERTICAL_WIND_TEXTURE : WIND_TEXTURE).buffer(pBuffer, RenderType::entityCutoutNoCull);
            this.wind.render(pMatrixStack, vertexconsumer, pCombinedLight, pCombinedOverlay);
            pMatrixStack.popPose();
            pMatrixStack.pushPose();
            pMatrixStack.translate(0.5D, 0.5D, 0.5D);
            pMatrixStack.scale(0.875F, 0.875F, 0.875F);
            pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
            this.wind.render(pMatrixStack, vertexconsumer, pCombinedLight, pCombinedOverlay);
            pMatrixStack.popPose();
            Camera camera = this.renderer.camera;
            pMatrixStack.pushPose();
            pMatrixStack.translate(0.5D, (double)(0.3F + f2 * 0.2F), 0.5D);
            pMatrixStack.scale(0.5F, 0.5F, 0.5F);
            float f3 = -camera.getYRot();
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f3));
            pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(camera.getXRot()));
            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
            float f4 = 1.3333334F;
            pMatrixStack.scale(1.3333334F, 1.3333334F, 1.3333334F);
            this.eye.render(pMatrixStack, (pBlockEntity.isHunting() ? OPEN_EYE_TEXTURE : CLOSED_EYE_TEXTURE).buffer(pBuffer, RenderType::entityCutoutNoCull), pCombinedLight, pCombinedOverlay);
            pMatrixStack.popPose();
        }
    }
}
