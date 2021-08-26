package net.minecraft.client.renderer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.optifine.Config;
import net.optifine.CustomItems;
import net.optifine.reflect.Reflector;
import net.optifine.shaders.Shaders;

public class ItemInHandRenderer
{
    private static final RenderType MAP_BACKGROUND = RenderType.text(new ResourceLocation("textures/map/map_background.png"));
    private static final RenderType MAP_BACKGROUND_CHECKERBOARD = RenderType.text(new ResourceLocation("textures/map/map_background_checkerboard.png"));
    private static final float ITEM_SWING_X_POS_SCALE = -0.4F;
    private static final float ITEM_SWING_Y_POS_SCALE = 0.2F;
    private static final float ITEM_SWING_Z_POS_SCALE = -0.2F;
    private static final float ITEM_HEIGHT_SCALE = -0.6F;
    private static final float ITEM_POS_X = 0.56F;
    private static final float ITEM_POS_Y = -0.52F;
    private static final float ITEM_POS_Z = -0.72F;
    private static final float ITEM_PRESWING_ROT_Y = 45.0F;
    private static final float ITEM_SWING_X_ROT_AMOUNT = -80.0F;
    private static final float ITEM_SWING_Y_ROT_AMOUNT = -20.0F;
    private static final float ITEM_SWING_Z_ROT_AMOUNT = -20.0F;
    private static final float EAT_JIGGLE_X_ROT_AMOUNT = 10.0F;
    private static final float EAT_JIGGLE_Y_ROT_AMOUNT = 90.0F;
    private static final float EAT_JIGGLE_Z_ROT_AMOUNT = 30.0F;
    private static final float EAT_JIGGLE_X_POS_SCALE = 0.6F;
    private static final float EAT_JIGGLE_Y_POS_SCALE = -0.5F;
    private static final float EAT_JIGGLE_Z_POS_SCALE = 0.0F;
    private static final double EAT_JIGGLE_EXPONENT = 27.0D;
    private static final float EAT_EXTRA_JIGGLE_CUTOFF = 0.8F;
    private static final float EAT_EXTRA_JIGGLE_SCALE = 0.1F;
    private static final float ARM_SWING_X_POS_SCALE = -0.3F;
    private static final float ARM_SWING_Y_POS_SCALE = 0.4F;
    private static final float ARM_SWING_Z_POS_SCALE = -0.4F;
    private static final float ARM_SWING_Y_ROT_AMOUNT = 70.0F;
    private static final float ARM_SWING_Z_ROT_AMOUNT = -20.0F;
    private static final float ARM_HEIGHT_SCALE = -0.6F;
    private static final float ARM_POS_SCALE = 0.8F;
    private static final float ARM_POS_X = 0.8F;
    private static final float ARM_POS_Y = -0.75F;
    private static final float ARM_POS_Z = -0.9F;
    private static final float ARM_PRESWING_ROT_Y = 45.0F;
    private static final float ARM_PREROTATION_X_OFFSET = -1.0F;
    private static final float ARM_PREROTATION_Y_OFFSET = 3.6F;
    private static final float ARM_PREROTATION_Z_OFFSET = 3.5F;
    private static final float ARM_POSTROTATION_X_OFFSET = 5.6F;
    private static final int ARM_ROT_X = 200;
    private static final int ARM_ROT_Y = -135;
    private static final int ARM_ROT_Z = 120;
    private static final float MAP_SWING_X_POS_SCALE = -0.4F;
    private static final float MAP_SWING_Z_POS_SCALE = -0.2F;
    private static final float MAP_HANDS_POS_X = 0.0F;
    private static final float MAP_HANDS_POS_Y = 0.04F;
    private static final float MAP_HANDS_POS_Z = -0.72F;
    private static final float MAP_HANDS_HEIGHT_SCALE = -1.2F;
    private static final float MAP_HANDS_TILT_SCALE = -0.5F;
    private static final float MAP_PLAYER_PITCH_SCALE = 45.0F;
    private static final float MAP_HANDS_Z_ROT_AMOUNT = -85.0F;
    private static final float MAPHAND_X_ROT_AMOUNT = 45.0F;
    private static final float MAPHAND_Y_ROT_AMOUNT = 92.0F;
    private static final float MAPHAND_Z_ROT_AMOUNT = -41.0F;
    private static final float MAP_HAND_X_POS = 0.3F;
    private static final float MAP_HAND_Y_POS = -1.1F;
    private static final float MAP_HAND_Z_POS = 0.45F;
    private static final float MAP_SWING_X_ROT_AMOUNT = 20.0F;
    private static final float MAP_PRE_ROT_SCALE = 0.38F;
    private static final float MAP_GLOBAL_X_POS = -0.5F;
    private static final float MAP_GLOBAL_Y_POS = -0.5F;
    private static final float MAP_GLOBAL_Z_POS = 0.0F;
    private static final float MAP_FINAL_SCALE = 0.0078125F;
    private static final int MAP_BORDER = 7;
    private static final int MAP_HEIGHT = 128;
    private static final int MAP_WIDTH = 128;
    private static final float BOW_CHARGE_X_POS_SCALE = 0.0F;
    private static final float BOW_CHARGE_Y_POS_SCALE = 0.0F;
    private static final float BOW_CHARGE_Z_POS_SCALE = 0.04F;
    private static final float BOW_CHARGE_SHAKE_X_SCALE = 0.0F;
    private static final float BOW_CHARGE_SHAKE_Y_SCALE = 0.004F;
    private static final float BOW_CHARGE_SHAKE_Z_SCALE = 0.0F;
    private static final float BOW_CHARGE_Z_SCALE = 0.2F;
    private static final float BOW_MIN_SHAKE_CHARGE = 0.1F;
    private final Minecraft minecraft;
    private ItemStack mainHandItem = ItemStack.EMPTY;
    private ItemStack offHandItem = ItemStack.EMPTY;
    private float mainHandHeight;
    private float oMainHandHeight;
    private float offHandHeight;
    private float oOffHandHeight;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final ItemRenderer itemRenderer;
    private static boolean renderItemHand = false;

    public ItemInHandRenderer(Minecraft p_109310_)
    {
        this.minecraft = p_109310_;
        this.entityRenderDispatcher = p_109310_.getEntityRenderDispatcher();
        this.itemRenderer = p_109310_.getItemRenderer();
    }

    public void renderItem(LivingEntity pLivingEntity, ItemStack pItemStack, ItemTransforms.TransformType pTransformType, boolean pLeftHand, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight)
    {
        CustomItems.setRenderOffHand(pLeftHand);
        renderItemHand = true;

        if (!pItemStack.isEmpty())
        {
            this.itemRenderer.renderStatic(pLivingEntity, pItemStack, pTransformType, pLeftHand, pMatrixStack, pBuffer, pLivingEntity.level, pCombinedLight, OverlayTexture.NO_OVERLAY, pLivingEntity.getId() + pTransformType.ordinal());
        }

        renderItemHand = false;
        CustomItems.setRenderOffHand(false);
    }

    private float calculateMapTilt(float pPitch)
    {
        float f = 1.0F - pPitch / 45.0F + 0.1F;
        f = Mth.clamp(f, 0.0F, 1.0F);
        return -Mth.cos(f * (float)Math.PI) * 0.5F + 0.5F;
    }

    private void renderMapHand(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, HumanoidArm pSide)
    {
        RenderSystem.setShaderTexture(0, this.minecraft.player.getSkinTextureLocation());
        PlayerRenderer playerrenderer = (PlayerRenderer)this.entityRenderDispatcher.<AbstractClientPlayer>getRenderer(this.minecraft.player);
        pMatrixStack.pushPose();
        float f = pSide == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(92.0F));
        pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(45.0F));
        pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * -41.0F));
        pMatrixStack.translate((double)(f * 0.3F), (double) - 1.1F, (double)0.45F);

        if (pSide == HumanoidArm.RIGHT)
        {
            playerrenderer.renderRightHand(pMatrixStack, pBuffer, pCombinedLight, this.minecraft.player);
        }
        else
        {
            playerrenderer.renderLeftHand(pMatrixStack, pBuffer, pCombinedLight, this.minecraft.player);
        }

        pMatrixStack.popPose();
    }

    private void renderOneHandedMap(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, float pEquippedProgress, HumanoidArm pHand, float pSwingProgress, ItemStack pStack)
    {
        float f = pHand == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        pMatrixStack.translate((double)(f * 0.125F), -0.125D, 0.0D);

        if (!this.minecraft.player.isInvisible())
        {
            pMatrixStack.pushPose();
            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * 10.0F));
            this.renderPlayerArm(pMatrixStack, pBuffer, pCombinedLight, pEquippedProgress, pSwingProgress, pHand);
            pMatrixStack.popPose();
        }

        pMatrixStack.pushPose();
        pMatrixStack.translate((double)(f * 0.51F), (double)(-0.08F + pEquippedProgress * -1.2F), -0.75D);
        float f1 = Mth.sqrt(pSwingProgress);
        float f2 = Mth.sin(f1 * (float)Math.PI);
        float f3 = -0.5F * f2;
        float f4 = 0.4F * Mth.sin(f1 * ((float)Math.PI * 2F));
        float f5 = -0.3F * Mth.sin(pSwingProgress * (float)Math.PI);
        pMatrixStack.translate((double)(f * f3), (double)(f4 - 0.3F * f2), (double)f5);
        pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(f2 * -45.0F));
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f * f2 * -30.0F));
        this.renderMap(pMatrixStack, pBuffer, pCombinedLight, pStack);
        pMatrixStack.popPose();
    }

    private void renderTwoHandedMap(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, float pPitch, float pEquippedProgress, float pSwingProgress)
    {
        float f = Mth.sqrt(pSwingProgress);
        float f1 = -0.2F * Mth.sin(pSwingProgress * (float)Math.PI);
        float f2 = -0.4F * Mth.sin(f * (float)Math.PI);
        pMatrixStack.translate(0.0D, (double)(-f1 / 2.0F), (double)f2);
        float f3 = this.calculateMapTilt(pPitch);
        pMatrixStack.translate(0.0D, (double)(0.04F + pEquippedProgress * -1.2F + f3 * -0.5F), (double) - 0.72F);
        pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(f3 * -85.0F));

        if (!this.minecraft.player.isInvisible())
        {
            pMatrixStack.pushPose();
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
            this.renderMapHand(pMatrixStack, pBuffer, pCombinedLight, HumanoidArm.RIGHT);
            this.renderMapHand(pMatrixStack, pBuffer, pCombinedLight, HumanoidArm.LEFT);
            pMatrixStack.popPose();
        }

        float f4 = Mth.sin(f * (float)Math.PI);
        pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(f4 * 20.0F));
        pMatrixStack.scale(2.0F, 2.0F, 2.0F);
        this.renderMap(pMatrixStack, pBuffer, pCombinedLight, this.mainHandItem);
    }

    private void renderMap(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, ItemStack pStack)
    {
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        pMatrixStack.scale(0.38F, 0.38F, 0.38F);
        pMatrixStack.translate(-0.5D, -0.5D, 0.0D);
        pMatrixStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
        Integer integer = MapItem.getMapId(pStack);
        MapItemSavedData mapitemsaveddata = MapItem.getSavedData(integer, this.minecraft.level);
        VertexConsumer vertexconsumer = pBuffer.getBuffer(mapitemsaveddata == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD);
        Matrix4f matrix4f = pMatrixStack.last().pose();
        vertexconsumer.vertex(matrix4f, -7.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(pCombinedLight).endVertex();
        vertexconsumer.vertex(matrix4f, 135.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(pCombinedLight).endVertex();
        vertexconsumer.vertex(matrix4f, 135.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(pCombinedLight).endVertex();
        vertexconsumer.vertex(matrix4f, -7.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(pCombinedLight).endVertex();

        if (mapitemsaveddata != null)
        {
            this.minecraft.gameRenderer.getMapRenderer().render(pMatrixStack, pBuffer, integer, mapitemsaveddata, false, pCombinedLight);
        }
    }

    private void renderPlayerArm(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, float pEquippedProgress, float pSwingProgress, HumanoidArm pSide)
    {
        boolean flag = pSide != HumanoidArm.LEFT;
        float f = flag ? 1.0F : -1.0F;
        float f1 = Mth.sqrt(pSwingProgress);
        float f2 = -0.3F * Mth.sin(f1 * (float)Math.PI);
        float f3 = 0.4F * Mth.sin(f1 * ((float)Math.PI * 2F));
        float f4 = -0.4F * Mth.sin(pSwingProgress * (float)Math.PI);
        pMatrixStack.translate((double)(f * (f2 + 0.64000005F)), (double)(f3 + -0.6F + pEquippedProgress * -0.6F), (double)(f4 + -0.71999997F));
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f * 45.0F));
        float f5 = Mth.sin(pSwingProgress * pSwingProgress * (float)Math.PI);
        float f6 = Mth.sin(f1 * (float)Math.PI);
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f * f6 * 70.0F));
        pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * f5 * -20.0F));
        AbstractClientPlayer abstractclientplayer = this.minecraft.player;
        RenderSystem.setShaderTexture(0, abstractclientplayer.getSkinTextureLocation());
        pMatrixStack.translate((double)(f * -1.0F), (double)3.6F, 3.5D);
        pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * 120.0F));
        pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(200.0F));
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f * -135.0F));
        pMatrixStack.translate((double)(f * 5.6F), 0.0D, 0.0D);
        PlayerRenderer playerrenderer = (PlayerRenderer)this.entityRenderDispatcher.<AbstractClientPlayer>getRenderer(abstractclientplayer);

        if (flag)
        {
            playerrenderer.renderRightHand(pMatrixStack, pBuffer, pCombinedLight, abstractclientplayer);
        }
        else
        {
            playerrenderer.renderLeftHand(pMatrixStack, pBuffer, pCombinedLight, abstractclientplayer);
        }
    }

    private void applyEatTransform(PoseStack pMatrixStack, float pPartialTicks, HumanoidArm pHand, ItemStack pStack)
    {
        float f = (float)this.minecraft.player.getUseItemRemainingTicks() - pPartialTicks + 1.0F;
        float f1 = f / (float)pStack.getUseDuration();

        if (f1 < 0.8F)
        {
            float f2 = Mth.abs(Mth.cos(f / 4.0F * (float)Math.PI) * 0.1F);
            pMatrixStack.translate(0.0D, (double)f2, 0.0D);
        }

        float f3 = 1.0F - (float)Math.pow((double)f1, 27.0D);
        int i = pHand == HumanoidArm.RIGHT ? 1 : -1;
        pMatrixStack.translate((double)(f3 * 0.6F * (float)i), (double)(f3 * -0.5F), (double)(f3 * 0.0F));
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees((float)i * f3 * 90.0F));
        pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(f3 * 10.0F));
        pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees((float)i * f3 * 30.0F));
    }

    private void applyItemArmAttackTransform(PoseStack pMatrixStack, HumanoidArm pHand, float pSwingProgress)
    {
        int i = pHand == HumanoidArm.RIGHT ? 1 : -1;
        float f = Mth.sin(pSwingProgress * pSwingProgress * (float)Math.PI);
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees((float)i * (45.0F + f * -20.0F)));
        float f1 = Mth.sin(Mth.sqrt(pSwingProgress) * (float)Math.PI);
        pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees((float)i * f1 * -20.0F));
        pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(f1 * -80.0F));
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees((float)i * -45.0F));
    }

    private void applyItemArmTransform(PoseStack pMatrixStack, HumanoidArm pHand, float pEquippedProg)
    {
        int i = pHand == HumanoidArm.RIGHT ? 1 : -1;
        pMatrixStack.translate((double)((float)i * 0.56F), (double)(-0.52F + pEquippedProg * -0.6F), (double) - 0.72F);
    }

    public void renderHandsWithItems(float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource.BufferSource pBuffer, LocalPlayer pPlayerEntity, int pCombinedLight)
    {
        float f = pPlayerEntity.getAttackAnim(pPartialTicks);
        InteractionHand interactionhand = MoreObjects.firstNonNull(pPlayerEntity.swingingArm, InteractionHand.MAIN_HAND);
        float f1 = Mth.lerp(pPartialTicks, pPlayerEntity.xRotO, pPlayerEntity.getXRot());
        ItemInHandRenderer.HandRenderSelection iteminhandrenderer$handrenderselection = evaluateWhichHandsToRender(pPlayerEntity);
        float f2 = Mth.lerp(pPartialTicks, pPlayerEntity.xBobO, pPlayerEntity.xBob);
        float f3 = Mth.lerp(pPartialTicks, pPlayerEntity.yBobO, pPlayerEntity.yBob);
        pMatrixStack.mulPose(Vector3f.XP.rotationDegrees((pPlayerEntity.getViewXRot(pPartialTicks) - f2) * 0.1F));
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees((pPlayerEntity.getViewYRot(pPartialTicks) - f3) * 0.1F));

        if (iteminhandrenderer$handrenderselection.renderMainHand)
        {
            float f4 = interactionhand == InteractionHand.MAIN_HAND ? f : 0.0F;
            float f5 = 1.0F - Mth.lerp(pPartialTicks, this.oMainHandHeight, this.mainHandHeight);

            if (!Reflector.ForgeHooksClient_renderSpecificFirstPersonHand.exists() || !Reflector.callBoolean(Reflector.ForgeHooksClient_renderSpecificFirstPersonHand, InteractionHand.MAIN_HAND, pMatrixStack, pBuffer, pCombinedLight, pPartialTicks, f1, f5, f2, this.mainHandItem))
            {
                this.renderArmWithItem(pPlayerEntity, pPartialTicks, f1, InteractionHand.MAIN_HAND, f4, this.mainHandItem, f5, pMatrixStack, pBuffer, pCombinedLight);
            }
        }

        if (iteminhandrenderer$handrenderselection.renderOffHand)
        {
            float f6 = interactionhand == InteractionHand.OFF_HAND ? f : 0.0F;
            float f7 = 1.0F - Mth.lerp(pPartialTicks, this.oOffHandHeight, this.offHandHeight);

            if (!Reflector.ForgeHooksClient_renderSpecificFirstPersonHand.exists() || !Reflector.callBoolean(Reflector.ForgeHooksClient_renderSpecificFirstPersonHand, InteractionHand.OFF_HAND, pMatrixStack, pBuffer, pCombinedLight, pPartialTicks, f1, f6, f7, this.offHandItem))
            {
                this.renderArmWithItem(pPlayerEntity, pPartialTicks, f1, InteractionHand.OFF_HAND, f6, this.offHandItem, f7, pMatrixStack, pBuffer, pCombinedLight);
            }
        }

        pBuffer.endBatch();
    }

    @VisibleForTesting
    static ItemInHandRenderer.HandRenderSelection evaluateWhichHandsToRender(LocalPlayer p_172915_)
    {
        ItemStack itemstack = p_172915_.getMainHandItem();
        ItemStack itemstack1 = p_172915_.getOffhandItem();
        boolean flag = itemstack.is(Items.BOW) || itemstack1.is(Items.BOW);
        boolean flag1 = itemstack.is(Items.CROSSBOW) || itemstack1.is(Items.CROSSBOW);

        if (!flag && !flag1)
        {
            return ItemInHandRenderer.HandRenderSelection.RENDER_BOTH_HANDS;
        }
        else if (p_172915_.isUsingItem())
        {
            return selectionUsingItemWhileHoldingBowLike(p_172915_);
        }
        else
        {
            return isChargedCrossbow(itemstack) ? ItemInHandRenderer.HandRenderSelection.RENDER_MAIN_HAND_ONLY : ItemInHandRenderer.HandRenderSelection.RENDER_BOTH_HANDS;
        }
    }

    private static ItemInHandRenderer.HandRenderSelection selectionUsingItemWhileHoldingBowLike(LocalPlayer p_172917_)
    {
        ItemStack itemstack = p_172917_.getUseItem();
        InteractionHand interactionhand = p_172917_.getUsedItemHand();

        if (!itemstack.is(Items.BOW) && !itemstack.is(Items.CROSSBOW))
        {
            return interactionhand == InteractionHand.MAIN_HAND && isChargedCrossbow(p_172917_.getOffhandItem()) ? ItemInHandRenderer.HandRenderSelection.RENDER_MAIN_HAND_ONLY : ItemInHandRenderer.HandRenderSelection.RENDER_BOTH_HANDS;
        }
        else
        {
            return ItemInHandRenderer.HandRenderSelection.onlyForHand(interactionhand);
        }
    }

    private static boolean isChargedCrossbow(ItemStack p_172913_)
    {
        return p_172913_.is(Items.CROSSBOW) && CrossbowItem.isCharged(p_172913_);
    }

    private void renderArmWithItem(AbstractClientPlayer pPlayer, float pPartialTicks, float pPitch, InteractionHand pHand, float pSwingProgress, ItemStack pStack, float pEquippedProgress, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight)
    {
        if (!Config.isShaders() || !Shaders.isSkipRenderHand(pHand))
        {
            if (!pPlayer.isScoping())
            {
                boolean flag = pHand == InteractionHand.MAIN_HAND;
                HumanoidArm humanoidarm = flag ? pPlayer.getMainArm() : pPlayer.getMainArm().getOpposite();
                pMatrixStack.pushPose();

                if (pStack.isEmpty())
                {
                    if (flag && !pPlayer.isInvisible())
                    {
                        this.renderPlayerArm(pMatrixStack, pBuffer, pCombinedLight, pEquippedProgress, pSwingProgress, humanoidarm);
                    }
                }
                else if (pStack.getItem() instanceof MapItem)
                {
                    if (flag && this.offHandItem.isEmpty())
                    {
                        this.renderTwoHandedMap(pMatrixStack, pBuffer, pCombinedLight, pPitch, pEquippedProgress, pSwingProgress);
                    }
                    else
                    {
                        this.renderOneHandedMap(pMatrixStack, pBuffer, pCombinedLight, pEquippedProgress, humanoidarm, pSwingProgress, pStack);
                    }
                }
                else if (pStack.is(Items.CROSSBOW))
                {
                    boolean flag2 = CrossbowItem.isCharged(pStack);
                    boolean flag3 = humanoidarm == HumanoidArm.RIGHT;
                    int l = flag3 ? 1 : -1;

                    if (pPlayer.isUsingItem() && pPlayer.getUseItemRemainingTicks() > 0 && pPlayer.getUsedItemHand() == pHand)
                    {
                        this.applyItemArmTransform(pMatrixStack, humanoidarm, pEquippedProgress);
                        pMatrixStack.translate((double)((float)l * -0.4785682F), (double) - 0.094387F, (double)0.05731531F);
                        pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(-11.935F));
                        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees((float)l * 65.3F));
                        pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees((float)l * -9.785F));
                        float f10 = (float)pStack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - pPartialTicks + 1.0F);
                        float f14 = f10 / (float)CrossbowItem.getChargeDuration(pStack);

                        if (f14 > 1.0F)
                        {
                            f14 = 1.0F;
                        }

                        if (f14 > 0.1F)
                        {
                            float f17 = Mth.sin((f10 - 0.1F) * 1.3F);
                            float f19 = f14 - 0.1F;
                            float f20 = f17 * f19;
                            pMatrixStack.translate((double)(f20 * 0.0F), (double)(f20 * 0.004F), (double)(f20 * 0.0F));
                        }

                        pMatrixStack.translate((double)(f14 * 0.0F), (double)(f14 * 0.0F), (double)(f14 * 0.04F));
                        pMatrixStack.scale(1.0F, 1.0F, 1.0F + f14 * 0.2F);
                        pMatrixStack.mulPose(Vector3f.YN.rotationDegrees((float)l * 45.0F));
                    }
                    else
                    {
                        float f9 = -0.4F * Mth.sin(Mth.sqrt(pSwingProgress) * (float)Math.PI);
                        float f13 = 0.2F * Mth.sin(Mth.sqrt(pSwingProgress) * ((float)Math.PI * 2F));
                        float f16 = -0.2F * Mth.sin(pSwingProgress * (float)Math.PI);
                        pMatrixStack.translate((double)((float)l * f9), (double)f13, (double)f16);
                        this.applyItemArmTransform(pMatrixStack, humanoidarm, pEquippedProgress);
                        this.applyItemArmAttackTransform(pMatrixStack, humanoidarm, pSwingProgress);

                        if (flag2 && pSwingProgress < 0.001F && flag)
                        {
                            pMatrixStack.translate((double)((float)l * -0.641864F), 0.0D, 0.0D);
                            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees((float)l * 10.0F));
                        }
                    }

                    this.renderItem(pPlayer, pStack, flag3 ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !flag3, pMatrixStack, pBuffer, pCombinedLight);
                }
                else
                {
                    boolean flag1 = humanoidarm == HumanoidArm.RIGHT;

                    if (pPlayer.isUsingItem() && pPlayer.getUseItemRemainingTicks() > 0 && pPlayer.getUsedItemHand() == pHand)
                    {
                        int k = flag1 ? 1 : -1;

                        switch (pStack.getUseAnimation())
                        {
                            case NONE:
                                this.applyItemArmTransform(pMatrixStack, humanoidarm, pEquippedProgress);
                                break;

                            case EAT:
                            case DRINK:
                                this.applyEatTransform(pMatrixStack, pPartialTicks, humanoidarm, pStack);
                                this.applyItemArmTransform(pMatrixStack, humanoidarm, pEquippedProgress);
                                break;

                            case BLOCK:
                                this.applyItemArmTransform(pMatrixStack, humanoidarm, pEquippedProgress);
                                break;

                            case BOW:
                                this.applyItemArmTransform(pMatrixStack, humanoidarm, pEquippedProgress);
                                pMatrixStack.translate((double)((float)k * -0.2785682F), (double)0.18344387F, (double)0.15731531F);
                                pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(-13.935F));
                                pMatrixStack.mulPose(Vector3f.YP.rotationDegrees((float)k * 35.3F));
                                pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees((float)k * -9.785F));
                                float f7 = (float)pStack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - pPartialTicks + 1.0F);
                                float f8 = f7 / 20.0F;
                                f8 = (f8 * f8 + f8 * 2.0F) / 3.0F;

                                if (f8 > 1.0F)
                                {
                                    f8 = 1.0F;
                                }

                                if (f8 > 0.1F)
                                {
                                    float f12 = Mth.sin((f7 - 0.1F) * 1.3F);
                                    float f15 = f8 - 0.1F;
                                    float f18 = f12 * f15;
                                    pMatrixStack.translate((double)(f18 * 0.0F), (double)(f18 * 0.004F), (double)(f18 * 0.0F));
                                }

                                pMatrixStack.translate((double)(f8 * 0.0F), (double)(f8 * 0.0F), (double)(f8 * 0.04F));
                                pMatrixStack.scale(1.0F, 1.0F, 1.0F + f8 * 0.2F);
                                pMatrixStack.mulPose(Vector3f.YN.rotationDegrees((float)k * 45.0F));
                                break;

                            case SPEAR:
                                this.applyItemArmTransform(pMatrixStack, humanoidarm, pEquippedProgress);
                                pMatrixStack.translate((double)((float)k * -0.5F), (double)0.7F, (double)0.1F);
                                pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(-55.0F));
                                pMatrixStack.mulPose(Vector3f.YP.rotationDegrees((float)k * 35.3F));
                                pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees((float)k * -9.785F));
                                float f11 = (float)pStack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - pPartialTicks + 1.0F);
                                float f2 = f11 / 10.0F;

                                if (f2 > 1.0F)
                                {
                                    f2 = 1.0F;
                                }

                                if (f2 > 0.1F)
                                {
                                    float f3 = Mth.sin((f11 - 0.1F) * 1.3F);
                                    float f4 = f2 - 0.1F;
                                    float f5 = f3 * f4;
                                    pMatrixStack.translate((double)(f5 * 0.0F), (double)(f5 * 0.004F), (double)(f5 * 0.0F));
                                }

                                pMatrixStack.translate(0.0D, 0.0D, (double)(f2 * 0.2F));
                                pMatrixStack.scale(1.0F, 1.0F, 1.0F + f2 * 0.2F);
                                pMatrixStack.mulPose(Vector3f.YN.rotationDegrees((float)k * 45.0F));
                        }
                    }
                    else if (pPlayer.isAutoSpinAttack())
                    {
                        this.applyItemArmTransform(pMatrixStack, humanoidarm, pEquippedProgress);
                        int i = flag1 ? 1 : -1;
                        pMatrixStack.translate((double)((float)i * -0.4F), (double)0.8F, (double)0.3F);
                        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees((float)i * 65.0F));
                        pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees((float)i * -85.0F));
                    }
                    else
                    {
                        float f6 = -0.4F * Mth.sin(Mth.sqrt(pSwingProgress) * (float)Math.PI);
                        float f = 0.2F * Mth.sin(Mth.sqrt(pSwingProgress) * ((float)Math.PI * 2F));
                        float f1 = -0.2F * Mth.sin(pSwingProgress * (float)Math.PI);
                        int j = flag1 ? 1 : -1;
                        pMatrixStack.translate((double)((float)j * f6), (double)f, (double)f1);
                        this.applyItemArmTransform(pMatrixStack, humanoidarm, pEquippedProgress);
                        this.applyItemArmAttackTransform(pMatrixStack, humanoidarm, pSwingProgress);
                    }

                    this.renderItem(pPlayer, pStack, flag1 ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !flag1, pMatrixStack, pBuffer, pCombinedLight);
                }

                pMatrixStack.popPose();
            }
        }
    }

    public void tick()
    {
        this.oMainHandHeight = this.mainHandHeight;
        this.oOffHandHeight = this.offHandHeight;
        LocalPlayer localplayer = this.minecraft.player;
        ItemStack itemstack = localplayer.getMainHandItem();
        ItemStack itemstack1 = localplayer.getOffhandItem();

        if (ItemStack.matches(this.mainHandItem, itemstack))
        {
            this.mainHandItem = itemstack;
        }

        if (ItemStack.matches(this.offHandItem, itemstack1))
        {
            this.offHandItem = itemstack1;
        }

        if (localplayer.isHandsBusy())
        {
            this.mainHandHeight = Mth.clamp(this.mainHandHeight - 0.4F, 0.0F, 1.0F);
            this.offHandHeight = Mth.clamp(this.offHandHeight - 0.4F, 0.0F, 1.0F);
        }
        else
        {
            float f = localplayer.getAttackStrengthScale(1.0F);

            if (Reflector.ForgeHooksClient_shouldCauseReequipAnimation.exists())
            {
                boolean flag = Reflector.callBoolean(Reflector.ForgeHooksClient_shouldCauseReequipAnimation, this.mainHandItem, itemstack, localplayer.getInventory().selected);
                boolean flag1 = Reflector.callBoolean(Reflector.ForgeHooksClient_shouldCauseReequipAnimation, this.offHandItem, itemstack1, -1);

                if (!flag && !Objects.equals(this.mainHandItem, itemstack))
                {
                    this.mainHandItem = itemstack;
                }

                if (!flag1 && !Objects.equals(this.offHandItem, itemstack1))
                {
                    this.offHandItem = itemstack1;
                }
            }

            this.mainHandHeight += Mth.clamp((this.mainHandItem == itemstack ? f * f * f : 0.0F) - this.mainHandHeight, -0.4F, 0.4F);
            this.offHandHeight += Mth.clamp((float)(this.offHandItem == itemstack1 ? 1 : 0) - this.offHandHeight, -0.4F, 0.4F);
        }

        if (this.mainHandHeight < 0.1F)
        {
            this.mainHandItem = itemstack;

            if (Config.isShaders())
            {
                Shaders.setItemToRenderMain(this.mainHandItem);
            }
        }

        if (this.offHandHeight < 0.1F)
        {
            this.offHandItem = itemstack1;

            if (Config.isShaders())
            {
                Shaders.setItemToRenderOff(this.offHandItem);
            }
        }
    }

    public void itemUsed(InteractionHand pHand)
    {
        if (pHand == InteractionHand.MAIN_HAND)
        {
            this.mainHandHeight = 0.0F;
        }
        else
        {
            this.offHandHeight = 0.0F;
        }
    }

    public static boolean isRenderItemHand()
    {
        return renderItemHand;
    }

    @VisibleForTesting
    static enum HandRenderSelection
    {
        RENDER_BOTH_HANDS(true, true),
        RENDER_MAIN_HAND_ONLY(true, false),
        RENDER_OFF_HAND_ONLY(false, true);

        final boolean renderMainHand;
        final boolean renderOffHand;

        private HandRenderSelection(boolean p_172928_, boolean p_172929_)
        {
            this.renderMainHand = p_172928_;
            this.renderOffHand = p_172929_;
        }

        public static ItemInHandRenderer.HandRenderSelection onlyForHand(InteractionHand p_172932_)
        {
            return p_172932_ == InteractionHand.MAIN_HAND ? RENDER_MAIN_HAND_ONLY : RENDER_OFF_HAND_ONLY;
        }
    }
}
