package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.optifine.Config;

public class MushroomCowMushroomLayer<T extends MushroomCow> extends RenderLayer<T, CowModel<T>>
{
    private ModelPart modelRendererMushroom = new ModelPart(new ArrayList<>(), new HashMap<>());
    private static final ResourceLocation LOCATION_MUSHROOM_RED = new ResourceLocation("textures/entity/cow/red_mushroom.png");
    private static final ResourceLocation LOCATION_MUSHROOM_BROWN = new ResourceLocation("textures/entity/cow/brown_mushroom.png");
    private static boolean hasTextureMushroomRed = false;
    private static boolean hasTextureMushroomBrown = false;

    public MushroomCowMushroomLayer(RenderLayerParent<T, CowModel<T>> p_117243_)
    {
        super(p_117243_);
        this.modelRendererMushroom.setTextureSize(16, 16);
        this.modelRendererMushroom.x = 8.0F;
        this.modelRendererMushroom.z = 8.0F;
        this.modelRendererMushroom.yRot = ((float)Math.PI / 4F);
        int[][] aint = new int[][] {null, null, {16, 16, 0, 0}, {16, 16, 0, 0}, null, null};
        this.modelRendererMushroom.addBox(aint, -10.0F, 0.0F, 0.0F, 20.0F, 16.0F, 0.0F, 0.0F);
        int[][] aint1 = new int[][] {null, null, null, null, {16, 16, 0, 0}, {16, 16, 0, 0}};
        this.modelRendererMushroom.addBox(aint1, 0.0F, 0.0F, -10.0F, 0.0F, 16.0F, 20.0F, 0.0F);
    }

    public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
    {
        if (!pLivingEntity.isBaby())
        {
            Minecraft minecraft = Minecraft.getInstance();
            boolean flag = minecraft.shouldEntityAppearGlowing(pLivingEntity) && pLivingEntity.isInvisible();

            if (!pLivingEntity.isInvisible() || flag)
            {
                BlockRenderDispatcher blockrenderdispatcher = minecraft.getBlockRenderer();
                BlockState blockstate = pLivingEntity.getMushroomType().getBlockState();
                ResourceLocation resourcelocation = this.getCustomMushroom(blockstate);
                VertexConsumer vertexconsumer = null;

                if (resourcelocation != null)
                {
                    vertexconsumer = pBuffer.getBuffer(RenderType.entityCutout(resourcelocation));
                }

                int i = LivingEntityRenderer.getOverlayCoords(pLivingEntity, 0.0F);
                BakedModel bakedmodel = blockrenderdispatcher.getBlockModel(blockstate);
                pMatrixStack.pushPose();
                pMatrixStack.translate((double)0.2F, (double) - 0.35F, 0.5D);
                pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(-48.0F));
                pMatrixStack.scale(-1.0F, -1.0F, 1.0F);
                pMatrixStack.translate(-0.5D, -0.5D, -0.5D);

                if (resourcelocation != null)
                {
                    this.modelRendererMushroom.render(pMatrixStack, vertexconsumer, pPackedLight, i);
                }
                else
                {
                    this.renderMushroomBlock(pMatrixStack, pBuffer, pPackedLight, flag, blockrenderdispatcher, blockstate, i, bakedmodel);
                }

                pMatrixStack.popPose();
                pMatrixStack.pushPose();
                pMatrixStack.translate((double)0.2F, (double) - 0.35F, 0.5D);
                pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(42.0F));
                pMatrixStack.translate((double)0.1F, 0.0D, (double) - 0.6F);
                pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(-48.0F));
                pMatrixStack.scale(-1.0F, -1.0F, 1.0F);
                pMatrixStack.translate(-0.5D, -0.5D, -0.5D);

                if (resourcelocation != null)
                {
                    this.modelRendererMushroom.render(pMatrixStack, vertexconsumer, pPackedLight, i);
                }
                else
                {
                    this.renderMushroomBlock(pMatrixStack, pBuffer, pPackedLight, flag, blockrenderdispatcher, blockstate, i, bakedmodel);
                }

                pMatrixStack.popPose();
                pMatrixStack.pushPose();
                this.getParentModel().getHead().translateAndRotate(pMatrixStack);
                pMatrixStack.translate(0.0D, (double) - 0.7F, (double) - 0.2F);
                pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(-78.0F));
                pMatrixStack.scale(-1.0F, -1.0F, 1.0F);
                pMatrixStack.translate(-0.5D, -0.5D, -0.5D);

                if (resourcelocation != null)
                {
                    this.modelRendererMushroom.render(pMatrixStack, vertexconsumer, pPackedLight, i);
                }
                else
                {
                    this.renderMushroomBlock(pMatrixStack, pBuffer, pPackedLight, flag, blockrenderdispatcher, blockstate, i, bakedmodel);
                }

                pMatrixStack.popPose();
            }
        }
    }

    private void renderMushroomBlock(PoseStack p_174502_, MultiBufferSource p_174503_, int p_174504_, boolean p_174505_, BlockRenderDispatcher p_174506_, BlockState p_174507_, int p_174508_, BakedModel p_174509_)
    {
        if (p_174505_)
        {
            p_174506_.getModelRenderer().renderModel(p_174502_.last(), p_174503_.getBuffer(RenderType.outline(TextureAtlas.LOCATION_BLOCKS)), p_174507_, p_174509_, 0.0F, 0.0F, 0.0F, p_174504_, p_174508_);
        }
        else
        {
            p_174506_.renderSingleBlock(p_174507_, p_174502_, p_174503_, p_174504_, p_174508_);
        }
    }

    private ResourceLocation getCustomMushroom(BlockState iblockstate)
    {
        Block block = iblockstate.getBlock();

        if (block == Blocks.RED_MUSHROOM && hasTextureMushroomRed)
        {
            return LOCATION_MUSHROOM_RED;
        }
        else
        {
            return block == Blocks.BROWN_MUSHROOM && hasTextureMushroomBrown ? LOCATION_MUSHROOM_BROWN : null;
        }
    }

    public static void update()
    {
        hasTextureMushroomRed = Config.hasResource(LOCATION_MUSHROOM_RED);
        hasTextureMushroomBrown = Config.hasResource(LOCATION_MUSHROOM_BROWN);
    }
}
