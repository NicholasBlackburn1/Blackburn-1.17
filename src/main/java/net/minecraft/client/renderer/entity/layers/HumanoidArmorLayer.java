package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.optifine.Config;
import net.optifine.CustomItems;
import net.optifine.reflect.Reflector;

public class HumanoidArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M>
{
    private static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = Maps.newHashMap();
    private final A innerModel;
    private final A outerModel;

    public HumanoidArmorLayer(RenderLayerParent<T, M> p_117075_, A p_117076_, A p_117077_)
    {
        super(p_117075_);
        this.innerModel = p_117076_;
        this.outerModel = p_117077_;
    }

    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
    {
        this.renderArmorPiece(pMatrixStack, pBuffer, pLivingEntity, EquipmentSlot.CHEST, pPackedLight, this.getArmorModel(EquipmentSlot.CHEST));
        this.renderArmorPiece(pMatrixStack, pBuffer, pLivingEntity, EquipmentSlot.LEGS, pPackedLight, this.getArmorModel(EquipmentSlot.LEGS));
        this.renderArmorPiece(pMatrixStack, pBuffer, pLivingEntity, EquipmentSlot.FEET, pPackedLight, this.getArmorModel(EquipmentSlot.FEET));
        this.renderArmorPiece(pMatrixStack, pBuffer, pLivingEntity, EquipmentSlot.HEAD, pPackedLight, this.getArmorModel(EquipmentSlot.HEAD));
    }

    private void renderArmorPiece(PoseStack p_117119_, MultiBufferSource p_117120_, T p_117121_, EquipmentSlot p_117122_, int p_117123_, A p_117124_)
    {
        ItemStack itemstack = p_117121_.getItemBySlot(p_117122_);

        if (itemstack.getItem() instanceof ArmorItem)
        {
            ArmorItem armoritem = (ArmorItem)itemstack.getItem();

            if (armoritem.getSlot() == p_117122_)
            {
                if (Reflector.ForgeHooksClient.exists())
                {
                    p_117124_ = this.getArmorModelHook(p_117121_, itemstack, p_117122_, p_117124_);
                }

                this.getParentModel().copyPropertiesTo(p_117124_);
                this.setPartVisibility(p_117124_, p_117122_);
                this.usesInnerModel(p_117122_);
                boolean flag = itemstack.hasFoil();

                if (armoritem instanceof DyeableLeatherItem)
                {
                    int i = ((DyeableLeatherItem)armoritem).getColor(itemstack);
                    float f = (float)(i >> 16 & 255) / 255.0F;
                    float f1 = (float)(i >> 8 & 255) / 255.0F;
                    float f2 = (float)(i & 255) / 255.0F;
                    this.renderModel(p_117119_, p_117120_, p_117123_, flag, p_117124_, f, f1, f2, this.getArmorResource(p_117121_, itemstack, p_117122_, (String)null));
                    this.renderModel(p_117119_, p_117120_, p_117123_, flag, p_117124_, 1.0F, 1.0F, 1.0F, this.getArmorResource(p_117121_, itemstack, p_117122_, "overlay"));
                }
                else
                {
                    this.renderModel(p_117119_, p_117120_, p_117123_, flag, p_117124_, 1.0F, 1.0F, 1.0F, this.getArmorResource(p_117121_, itemstack, p_117122_, (String)null));
                }
            }
        }
    }

    protected void setPartVisibility(A pModel, EquipmentSlot pSlot)
    {
        pModel.setAllVisible(false);

        switch (pSlot)
        {
            case HEAD:
                pModel.head.visible = true;
                pModel.hat.visible = true;
                break;

            case CHEST:
                pModel.body.visible = true;
                pModel.rightArm.visible = true;
                pModel.leftArm.visible = true;
                break;

            case LEGS:
                pModel.body.visible = true;
                pModel.rightLeg.visible = true;
                pModel.leftLeg.visible = true;
                break;

            case FEET:
                pModel.rightLeg.visible = true;
                pModel.leftLeg.visible = true;
        }
    }

    private void renderModel(PoseStack p_117107_, MultiBufferSource p_117108_, int p_117109_, ArmorItem p_117110_, boolean p_117111_, A p_117112_, boolean p_117113_, float p_117114_, float p_117115_, float p_117116_, @Nullable String p_117117_)
    {
        this.renderModel(p_117107_, p_117108_, p_117109_, p_117111_, p_117112_, p_117114_, p_117115_, p_117116_, this.getArmorLocation(p_117110_, p_117113_, p_117117_));
    }

    private void renderModel(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, boolean hasEffect, A bipedModelIn, float red, float green, float blue, ResourceLocation armorResource)
    {
        VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(bufferIn, RenderType.armorCutoutNoCull(armorResource), false, hasEffect);
        bipedModelIn.renderToBuffer(matrixStackIn, vertexconsumer, packedLightIn, OverlayTexture.NO_OVERLAY, red, green, blue, 1.0F);
    }

    private A getArmorModel(EquipmentSlot p_117079_)
    {
        return (A)(this.usesInnerModel(p_117079_) ? this.innerModel : this.outerModel);
    }

    private boolean usesInnerModel(EquipmentSlot pSlot)
    {
        return pSlot == EquipmentSlot.LEGS;
    }

    private ResourceLocation getArmorLocation(ArmorItem p_117081_, boolean p_117082_, @Nullable String p_117083_)
    {
        String s = "textures/models/armor/" + p_117081_.getMaterial().getName() + "_layer_" + (p_117082_ ? 2 : 1) + (p_117083_ == null ? "" : "_" + p_117083_) + ".png";
        return ARMOR_LOCATION_CACHE.computeIfAbsent(s, ResourceLocation::new);
    }

    protected A getArmorModelHook(T entity, ItemStack itemStack, EquipmentSlot slot, A model)
    {
        return (A)(Reflector.ForgeHooksClient_getArmorModel.exists() ? Reflector.ForgeHooksClient_getArmorModel.call(entity, itemStack, slot, model) : model);
    }

    public ResourceLocation getArmorResource(Entity entity, ItemStack stack, EquipmentSlot slot, String type)
    {
        ArmorItem armoritem = (ArmorItem)stack.getItem();
        String s = armoritem.getMaterial().getName();
        String s1 = "minecraft";
        int i = s.indexOf(58);

        if (i != -1)
        {
            s1 = s.substring(0, i);
            s = s.substring(i + 1);
        }

        String s2 = String.format("%s:textures/models/armor/%s_layer_%d%s.png", s1, s, this.usesInnerModel(slot) ? 2 : 1, type == null ? "" : String.format("_%s", type));

        if (Reflector.ForgeHooksClient_getArmorTexture.exists())
        {
            s2 = Reflector.callString(Reflector.ForgeHooksClient_getArmorTexture, entity, stack, s2, slot, type);
        }

        ResourceLocation resourcelocation = ARMOR_LOCATION_CACHE.get(s2);

        if (resourcelocation == null)
        {
            resourcelocation = new ResourceLocation(s2);
            ARMOR_LOCATION_CACHE.put(s2, resourcelocation);
        }

        if (Config.isCustomItems())
        {
            resourcelocation = CustomItems.getCustomArmorTexture(stack, slot, type, resourcelocation);
        }

        return resourcelocation;
    }
}
