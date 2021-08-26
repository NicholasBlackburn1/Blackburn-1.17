package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.optifine.Config;
import net.optifine.CustomColors;

public class SheepFurLayer extends RenderLayer<Sheep, SheepModel<Sheep>>
{
    private static final ResourceLocation SHEEP_FUR_LOCATION = new ResourceLocation("textures/entity/sheep/sheep_fur.png");
    public SheepFurModel<Sheep> model;

    public SheepFurLayer(RenderLayerParent<Sheep, SheepModel<Sheep>> p_174533_, EntityModelSet p_174534_)
    {
        super(p_174533_);
        this.model = new SheepFurModel<>(p_174534_.bakeLayer(ModelLayers.SHEEP_FUR));
    }

    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, Sheep pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
    {
        if (!pLivingEntity.isSheared())
        {
            if (pLivingEntity.isInvisible())
            {
                Minecraft minecraft = Minecraft.getInstance();
                boolean flag = minecraft.shouldEntityAppearGlowing(pLivingEntity);

                if (flag)
                {
                    this.getParentModel().copyPropertiesTo(this.model);
                    this.model.prepareMobModel(pLivingEntity, pLimbSwing, pLimbSwingAmount, pPartialTicks);
                    this.model.setupAnim(pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
                    VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.outline(SHEEP_FUR_LOCATION));
                    this.model.renderToBuffer(pMatrixStack, vertexconsumer, pPackedLight, LivingEntityRenderer.getOverlayCoords(pLivingEntity, 0.0F), 0.0F, 0.0F, 0.0F, 1.0F);
                }
            }
            else
            {
                float f1;
                float f2;
                float f3;

                if (pLivingEntity.hasCustomName() && "jeb_".equals(pLivingEntity.getName().getContents()))
                {
                    int i1 = 25;
                    int i = pLivingEntity.tickCount / 25 + pLivingEntity.getId();
                    int j = DyeColor.values().length;
                    int k = i % j;
                    int l = (i + 1) % j;
                    float f = ((float)(pLivingEntity.tickCount % 25) + pPartialTicks) / 25.0F;
                    float[] afloat1 = Sheep.getColorArray(DyeColor.byId(k));
                    float[] afloat2 = Sheep.getColorArray(DyeColor.byId(l));

                    if (Config.isCustomColors())
                    {
                        afloat1 = CustomColors.getSheepColors(DyeColor.byId(k), afloat1);
                        afloat2 = CustomColors.getSheepColors(DyeColor.byId(l), afloat2);
                    }

                    f1 = afloat1[0] * (1.0F - f) + afloat2[0] * f;
                    f2 = afloat1[1] * (1.0F - f) + afloat2[1] * f;
                    f3 = afloat1[2] * (1.0F - f) + afloat2[2] * f;
                }
                else
                {
                    float[] afloat = Sheep.getColorArray(pLivingEntity.getColor());

                    if (Config.isCustomColors())
                    {
                        afloat = CustomColors.getSheepColors(pLivingEntity.getColor(), afloat);
                    }

                    f1 = afloat[0];
                    f2 = afloat[1];
                    f3 = afloat[2];
                }

                coloredCutoutModelCopyLayerRender(this.getParentModel(), this.model, SHEEP_FUR_LOCATION, pMatrixStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch, pPartialTicks, f1, f2, f3);
            }
        }
    }
}
