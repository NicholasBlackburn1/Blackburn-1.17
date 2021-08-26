package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import net.optifine.Config;
import net.optifine.CustomColors;
import net.optifine.shaders.Shaders;

public class SignRenderer implements BlockEntityRenderer<SignBlockEntity>
{
    public static final int MAX_LINE_WIDTH = 90;
    private static final int LINE_HEIGHT = 10;
    private static final String STICK = "stick";
    private static final int BLACK_TEXT_OUTLINE_COLOR = -988212;
    private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);
    private final Map<WoodType, SignRenderer.SignModel> signModels;
    private final Font font;
    private static double textRenderDistanceSq = 4096.0D;

    public SignRenderer(BlockEntityRendererProvider.Context p_173636_)
    {
        this.signModels = WoodType.values().collect(ImmutableMap.toImmutableMap((woodTypeIn) ->
        {
            return woodTypeIn;
        }, (woodTypeIn) ->
        {
            return new SignRenderer.SignModel(p_173636_.bakeLayer(ModelLayers.createSignModelName(woodTypeIn)));
        }));
        this.font = p_173636_.getFont();
    }

    public void render(SignBlockEntity pBlockEntity, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay)
    {
        BlockState blockstate = pBlockEntity.getBlockState();
        pMatrixStack.pushPose();
        float f = 0.6666667F;
        WoodType woodtype = getWoodType(blockstate.getBlock());
        SignRenderer.SignModel signrenderer$signmodel = this.signModels.get(woodtype);

        if (blockstate.getBlock() instanceof StandingSignBlock)
        {
            pMatrixStack.translate(0.5D, 0.5D, 0.5D);
            float f1 = -((float)(blockstate.getValue(StandingSignBlock.ROTATION) * 360) / 16.0F);
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f1));
            signrenderer$signmodel.stick.visible = true;
        }
        else
        {
            pMatrixStack.translate(0.5D, 0.5D, 0.5D);
            float f4 = -blockstate.getValue(WallSignBlock.FACING).toYRot();
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f4));
            pMatrixStack.translate(0.0D, -0.3125D, -0.4375D);
            signrenderer$signmodel.stick.visible = false;
        }

        pMatrixStack.pushPose();
        pMatrixStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
        Material material = Sheets.getSignMaterial(woodtype);
        VertexConsumer vertexconsumer = material.buffer(pBuffer, signrenderer$signmodel::renderType);
        signrenderer$signmodel.root.render(pMatrixStack, vertexconsumer, pCombinedLight, pCombinedOverlay);
        pMatrixStack.popPose();

        if (isRenderText(pBlockEntity))
        {
            float f2 = 0.010416667F;
            pMatrixStack.translate(0.0D, (double)0.33333334F, (double)0.046666667F);
            pMatrixStack.scale(0.010416667F, -0.010416667F, 0.010416667F);
            int i = getDarkColor(pBlockEntity);
            int j = 20;
            FormattedCharSequence[] aformattedcharsequence = pBlockEntity.getRenderMessages(Minecraft.getInstance().isTextFilteringEnabled(), (componentIn) ->
            {
                List<FormattedCharSequence> list = this.font.split(componentIn, 90);
                return list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
            });
            int k;
            boolean flag;
            int l;

            if (pBlockEntity.hasGlowingText())
            {
                k = pBlockEntity.getColor().getTextColor();

                if (Config.isCustomColors())
                {
                    k = CustomColors.getSignTextColor(k);
                }

                flag = isOutlineVisible(pBlockEntity, k);
                l = 15728880;
            }
            else
            {
                k = i;
                flag = false;
                l = pCombinedLight;
            }

            for (int i1 = 0; i1 < 4; ++i1)
            {
                FormattedCharSequence formattedcharsequence = aformattedcharsequence[i1];
                float f3 = (float)(-this.font.width(formattedcharsequence) / 2);

                if (flag)
                {
                    this.font.drawInBatch8xOutline(formattedcharsequence, f3, (float)(i1 * 10 - 20), k, i, pMatrixStack.last().pose(), pBuffer, l);
                }
                else
                {
                    this.font.drawInBatch(formattedcharsequence, f3, (float)(i1 * 10 - 20), k, false, pMatrixStack.last().pose(), pBuffer, false, 0, l);
                }
            }
        }

        pMatrixStack.popPose();
    }

    private static boolean isOutlineVisible(SignBlockEntity p_173642_, int p_173643_)
    {
        if (p_173643_ == DyeColor.BLACK.getTextColor())
        {
            return true;
        }
        else
        {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer localplayer = minecraft.player;

            if (localplayer != null && minecraft.options.getCameraType().isFirstPerson() && localplayer.isScoping())
            {
                return true;
            }
            else
            {
                Entity entity = minecraft.getCameraEntity();
                return entity != null && entity.distanceToSqr(Vec3.atCenterOf(p_173642_.getBlockPos())) < (double)OUTLINE_RENDER_DISTANCE;
            }
        }
    }

    private static int getDarkColor(SignBlockEntity p_173640_)
    {
        int i = p_173640_.getColor().getTextColor();

        if (Config.isCustomColors())
        {
            i = CustomColors.getSignTextColor(i);
        }

        double d0 = 0.4D;
        int j = (int)((double)NativeImage.getR(i) * 0.4D);
        int k = (int)((double)NativeImage.getG(i) * 0.4D);
        int l = (int)((double)NativeImage.getB(i) * 0.4D);
        return i == DyeColor.BLACK.getTextColor() && p_173640_.hasGlowingText() ? -988212 : NativeImage.combine(0, l, k, j);
    }

    public static WoodType getWoodType(Block p_173638_)
    {
        WoodType woodtype;

        if (p_173638_ instanceof SignBlock)
        {
            woodtype = ((SignBlock)p_173638_).type();
        }
        else
        {
            woodtype = WoodType.OAK;
        }

        return woodtype;
    }

    public static SignRenderer.SignModel createSignModel(EntityModelSet p_173647_, WoodType p_173648_)
    {
        return new SignRenderer.SignModel(p_173647_.bakeLayer(ModelLayers.createSignModelName(p_173648_)));
    }

    public static LayerDefinition createSignLayer()
    {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("sign", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -14.0F, -1.0F, 24.0F, 12.0F, 2.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("stick", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 14.0F, 2.0F), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    private static boolean isRenderText(SignBlockEntity tileEntity)
    {
        if (Shaders.isShadowPass)
        {
            return false;
        }
        else
        {
            if (!Config.zoomMode)
            {
                BlockPos blockpos = tileEntity.getBlockPos();
                Entity entity = Minecraft.getInstance().getCameraEntity();
                double d0 = entity.distanceToSqr((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());

                if (d0 > textRenderDistanceSq)
                {
                    return false;
                }
            }

            return true;
        }
    }

    public static void updateTextRenderDistance()
    {
        Minecraft minecraft = Minecraft.getInstance();
        double d0 = Config.limit(minecraft.options.fov, 1.0D, 120.0D);
        double d1 = Math.max(1.5D * (double)minecraft.getWindow().getScreenHeight() / d0, 16.0D);
        textRenderDistanceSq = d1 * d1;
    }

    public static final class SignModel extends Model
    {
        public final ModelPart root;
        public final ModelPart stick;

        public SignModel(ModelPart p_173657_)
        {
            super(RenderType::entityCutoutNoCull);
            this.root = p_173657_;
            this.stick = p_173657_.getChilds("stick");
        }

        public void renderToBuffer(PoseStack pMatrixStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha)
        {
            this.root.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
        }
    }
}
