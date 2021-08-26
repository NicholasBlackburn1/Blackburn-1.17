package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.optifine.Config;
import net.optifine.reflect.Reflector;
import net.optifine.shaders.Shaders;

public class ItemFrameRenderer<T extends ItemFrame> extends EntityRenderer<T>
{
    private static final ModelResourceLocation FRAME_LOCATION = new ModelResourceLocation("item_frame", "map=false");
    private static final ModelResourceLocation MAP_FRAME_LOCATION = new ModelResourceLocation("item_frame", "map=true");
    private static final ModelResourceLocation GLOW_FRAME_LOCATION = new ModelResourceLocation("glow_item_frame", "map=false");
    private static final ModelResourceLocation GLOW_MAP_FRAME_LOCATION = new ModelResourceLocation("glow_item_frame", "map=true");
    public static final int GLOW_FRAME_BRIGHTNESS = 5;
    public static final int BRIGHT_MAP_LIGHT_ADJUSTMENT = 30;
    private final Minecraft minecraft = Minecraft.getInstance();
    private final ItemRenderer itemRenderer;
    private static double itemRenderDistanceSq = 4096.0D;

    public ItemFrameRenderer(EntityRendererProvider.Context p_174204_)
    {
        super(p_174204_);
        this.itemRenderer = p_174204_.getItemRenderer();
    }

    protected int getBlockLightLevel(T p_174216_, BlockPos p_174217_)
    {
        return p_174216_.getType() == EntityType.GLOW_ITEM_FRAME ? Math.max(5, super.getBlockLightLevel(p_174216_, p_174217_)) : super.getBlockLightLevel(p_174216_, p_174217_);
    }

    public void render(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight)
    {
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
        pMatrixStack.pushPose();
        Direction direction = pEntity.getDirection();
        Vec3 vec3 = this.getRenderOffset(pEntity, pPartialTicks);
        pMatrixStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
        double d0 = 0.46875D;
        pMatrixStack.translate((double)direction.getStepX() * 0.46875D, (double)direction.getStepY() * 0.46875D, (double)direction.getStepZ() * 0.46875D);
        pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(pEntity.getXRot()));
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - pEntity.getYRot()));
        boolean flag = pEntity.isInvisible();
        ItemStack itemstack = pEntity.getItem();

        if (!flag)
        {
            BlockRenderDispatcher blockrenderdispatcher = this.minecraft.getBlockRenderer();
            ModelManager modelmanager = blockrenderdispatcher.getBlockModelShaper().getModelManager();
            ModelResourceLocation modelresourcelocation = this.getFrameModelResourceLoc(pEntity, itemstack);
            pMatrixStack.pushPose();
            pMatrixStack.translate(-0.5D, -0.5D, -0.5D);
            blockrenderdispatcher.getModelRenderer().renderModel(pMatrixStack.last(), pBuffer.getBuffer(Sheets.solidBlockSheet()), (BlockState)null, modelmanager.getModel(modelresourcelocation), 1.0F, 1.0F, 1.0F, pPackedLight, OverlayTexture.NO_OVERLAY);
            pMatrixStack.popPose();
        }

        if (!itemstack.isEmpty())
        {
            boolean flag1 = itemstack.getItem() instanceof MapItem;

            if (flag)
            {
                pMatrixStack.translate(0.0D, 0.0D, 0.5D);
            }
            else
            {
                pMatrixStack.translate(0.0D, 0.0D, 0.4375D);
            }

            int j = flag1 ? pEntity.getRotation() % 4 * 2 : pEntity.getRotation();
            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees((float)j * 360.0F / 8.0F));

            if (!Reflector.postForgeBusEvent(Reflector.RenderItemInFrameEvent_Constructor, pEntity, this, pMatrixStack, pBuffer, pPackedLight))
            {
                if (flag1)
                {
                    pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
                    float f = 0.0078125F;
                    pMatrixStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
                    pMatrixStack.translate(-64.0D, -64.0D, 0.0D);
                    Integer integer = MapItem.getMapId(itemstack);
                    MapItemSavedData mapitemsaveddata = MapItem.getSavedData(integer, pEntity.level);
                    pMatrixStack.translate(0.0D, 0.0D, -1.0D);

                    if (mapitemsaveddata != null)
                    {
                        int i = this.getLightVal(pEntity, 15728850, pPackedLight);
                        this.minecraft.gameRenderer.getMapRenderer().render(pMatrixStack, pBuffer, integer, mapitemsaveddata, true, i);
                    }
                }
                else
                {
                    int k = this.getLightVal(pEntity, 15728880, pPackedLight);
                    pMatrixStack.scale(0.5F, 0.5F, 0.5F);

                    if (this.isRenderItem(pEntity))
                    {
                        this.itemRenderer.renderStatic(itemstack, ItemTransforms.TransformType.FIXED, k, OverlayTexture.NO_OVERLAY, pMatrixStack, pBuffer, pEntity.getId());
                    }
                }
            }
        }

        pMatrixStack.popPose();
    }

    private int getLightVal(T p_174209_, int p_174210_, int p_174211_)
    {
        return p_174209_.getType() == EntityType.GLOW_ITEM_FRAME ? p_174210_ : p_174211_;
    }

    private ModelResourceLocation getFrameModelResourceLoc(T p_174213_, ItemStack p_174214_)
    {
        boolean flag = p_174213_.getType() == EntityType.GLOW_ITEM_FRAME;

        if (p_174214_.getItem() instanceof MapItem)
        {
            return flag ? GLOW_MAP_FRAME_LOCATION : MAP_FRAME_LOCATION;
        }
        else
        {
            return flag ? GLOW_FRAME_LOCATION : FRAME_LOCATION;
        }
    }

    public Vec3 getRenderOffset(T pEntity, float pPartialTicks)
    {
        return new Vec3((double)((float)pEntity.getDirection().getStepX() * 0.3F), -0.25D, (double)((float)pEntity.getDirection().getStepZ() * 0.3F));
    }

    public ResourceLocation getTextureLocation(T pEntity)
    {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    protected boolean shouldShowName(T pEntity)
    {
        if (Minecraft.renderNames() && !pEntity.getItem().isEmpty() && pEntity.getItem().hasCustomHoverName() && this.entityRenderDispatcher.crosshairPickEntity == pEntity)
        {
            double d0 = this.entityRenderDispatcher.distanceToSqr(pEntity);
            float f = pEntity.isDiscrete() ? 32.0F : 64.0F;
            return d0 < (double)(f * f);
        }
        else
        {
            return false;
        }
    }

    protected void renderNameTag(T pEntity, Component pDisplayName, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight)
    {
        super.renderNameTag(pEntity, pEntity.getItem().getHoverName(), pMatrixStack, pBuffer, pPackedLight);
    }

    private boolean isRenderItem(ItemFrame itemFrame)
    {
        if (Shaders.isShadowPass)
        {
            return false;
        }
        else
        {
            if (!Config.zoomMode)
            {
                Entity entity = this.minecraft.getCameraEntity();
                double d0 = itemFrame.distanceToSqr(entity.getX(), entity.getY(), entity.getZ());

                if (d0 > itemRenderDistanceSq)
                {
                    return false;
                }
            }

            return true;
        }
    }

    public static void updateItemRenderDistance()
    {
        Minecraft minecraft = Minecraft.getInstance();
        double d0 = Config.limit(minecraft.options.fov, 1.0D, 120.0D);
        double d1 = Math.max(6.0D * (double)minecraft.getWindow().getScreenHeight() / d0, 16.0D);
        itemRenderDistanceSq = d1 * d1;
    }
}
