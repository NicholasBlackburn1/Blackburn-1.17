package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import javax.annotation.Nullable;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;

public class ArmorStandRenderer extends LivingEntityRenderer<ArmorStand, ArmorStandArmorModel>
{
    public static final ResourceLocation DEFAULT_SKIN_LOCATION = new ResourceLocation("textures/entity/armorstand/wood.png");

    public ArmorStandRenderer(EntityRendererProvider.Context p_173915_)
    {
        super(p_173915_, new ArmorStandModel(p_173915_.bakeLayer(ModelLayers.ARMOR_STAND)), 0.0F);
        this.addLayer(new HumanoidArmorLayer<>(this, new ArmorStandArmorModel(p_173915_.bakeLayer(ModelLayers.ARMOR_STAND_INNER_ARMOR)), new ArmorStandArmorModel(p_173915_.bakeLayer(ModelLayers.ARMOR_STAND_OUTER_ARMOR))));
        this.addLayer(new ItemInHandLayer<>(this));
        this.addLayer(new ElytraLayer<>(this, p_173915_.getModelSet()));
        this.addLayer(new CustomHeadLayer<>(this, p_173915_.getModelSet()));
    }

    public ResourceLocation getTextureLocation(ArmorStand pEntity)
    {
        return DEFAULT_SKIN_LOCATION;
    }

    protected void setupRotations(ArmorStand pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks)
    {
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - pRotationYaw));
        float f = (float)(pEntityLiving.level.getGameTime() - pEntityLiving.lastHit) + pPartialTicks;

        if (f < 5.0F)
        {
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(Mth.sin(f / 1.5F * (float)Math.PI) * 3.0F));
        }
    }

    protected boolean shouldShowName(ArmorStand pEntity)
    {
        double d0 = this.entityRenderDispatcher.distanceToSqr(pEntity);
        float f = pEntity.isCrouching() ? 32.0F : 64.0F;
        return d0 >= (double)(f * f) ? false : pEntity.isCustomNameVisible();
    }

    @Nullable
    protected RenderType getRenderType(ArmorStand p_113806_, boolean p_113807_, boolean p_113808_, boolean p_113809_)
    {
        if (!p_113806_.isMarker())
        {
            return super.getRenderType(p_113806_, p_113807_, p_113808_, p_113809_);
        }
        else
        {
            ResourceLocation resourcelocation = this.getTextureLocation(p_113806_);

            if (p_113808_)
            {
                return RenderType.entityTranslucent(resourcelocation, false);
            }
            else
            {
                return p_113807_ ? RenderType.entityCutoutNoCull(resourcelocation, false) : null;
            }
        }
    }
}
