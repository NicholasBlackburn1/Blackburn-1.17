package net.minecraft.client.renderer.entity;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforgeop.client.RenderProperties;
import net.minecraftforgeop.resource.IResourceType;
import net.minecraftforgeop.resource.VanillaResourceType;
import net.optifine.Config;
import net.optifine.CustomColors;
import net.optifine.CustomItems;
import net.optifine.EmissiveTextures;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorForge;
import net.optifine.render.VertexBuilderWrapper;
import net.optifine.shaders.Shaders;

public class ItemRenderer implements ResourceManagerReloadListener
{
    public static final ResourceLocation ENCHANT_GLINT_LOCATION = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    private static final Set<Item> IGNORED = Sets.newHashSet(Items.AIR);
    private static final int GUI_SLOT_CENTER_X = 8;
    private static final int GUI_SLOT_CENTER_Y = 8;
    public static final int ITEM_COUNT_BLIT_OFFSET = 200;
    public static final float COMPASS_FOIL_UI_SCALE = 0.5F;
    public static final float COMPASS_FOIL_FIRST_PERSON_SCALE = 0.75F;
    public float blitOffset;
    private final ItemModelShaper itemModelShaper;
    private final TextureManager textureManager;
    private final ItemColors itemColors;
    private final BlockEntityWithoutLevelRenderer blockEntityRenderer;
    public ModelManager modelManager = null;
    private static boolean renderItemGui = false;

    public ItemRenderer(TextureManager p_174225_, ModelManager p_174226_, ItemColors p_174227_, BlockEntityWithoutLevelRenderer p_174228_)
    {
        this.textureManager = p_174225_;
        this.modelManager = p_174226_;

        if (Reflector.ItemModelMesherForge_Constructor.exists())
        {
            this.itemModelShaper = (ItemModelShaper)Reflector.newInstance(Reflector.ItemModelMesherForge_Constructor, this.modelManager);
        }
        else
        {
            this.itemModelShaper = new ItemModelShaper(p_174226_);
        }

        this.blockEntityRenderer = p_174228_;

        for (Item item : Registry.ITEM)
        {
            if (!IGNORED.contains(item))
            {
                this.itemModelShaper.register(item, new ModelResourceLocation(Registry.ITEM.getKey(item), "inventory"));
            }
        }

        this.itemColors = p_174227_;
    }

    public ItemModelShaper getItemModelShaper()
    {
        return this.itemModelShaper;
    }

    public void renderModelLists(BakedModel pModel, ItemStack pStack, int pCombinedLight, int pCombinedOverlay, PoseStack pMatrixStack, VertexConsumer pBuffer)
    {
        if (Config.isMultiTexture())
        {
            pBuffer.setRenderBlocks(true);
        }

        Random random = new Random();
        long i = 42L;

        for (Direction direction : Direction.VALUES)
        {
            random.setSeed(42L);
            this.renderQuadList(pMatrixStack, pBuffer, pModel.getQuads((BlockState)null, direction, random), pStack, pCombinedLight, pCombinedOverlay);
        }

        random.setSeed(42L);
        this.renderQuadList(pMatrixStack, pBuffer, pModel.getQuads((BlockState)null, (Direction)null, random), pStack, pCombinedLight, pCombinedOverlay);
    }

    public void render(ItemStack pItemStack, ItemTransforms.TransformType pTransformType, boolean pLeftHand, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay, BakedModel pModel)
    {
        if (!pItemStack.isEmpty())
        {
            pMatrixStack.pushPose();
            boolean flag = pTransformType == ItemTransforms.TransformType.GUI || pTransformType == ItemTransforms.TransformType.GROUND || pTransformType == ItemTransforms.TransformType.FIXED;

            if (flag)
            {
                if (pItemStack.is(Items.TRIDENT))
                {
                    pModel = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:trident#inventory"));
                }
                else if (pItemStack.is(Items.SPYGLASS))
                {
                    pModel = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:spyglass#inventory"));
                }
            }

            if (Reflector.ForgeHooksClient_handleCameraTransforms.exists())
            {
                pModel = (BakedModel)Reflector.ForgeHooksClient_handleCameraTransforms.call(pMatrixStack, pModel, pTransformType, pLeftHand);
            }
            else
            {
                pModel.getTransforms().getTransform(pTransformType).apply(pLeftHand, pMatrixStack);
            }

            pMatrixStack.translate(-0.5D, -0.5D, -0.5D);

            if (!pModel.isCustomRenderer() && (!pItemStack.is(Items.TRIDENT) || flag))
            {
                boolean flag1;

                if (pTransformType != ItemTransforms.TransformType.GUI && !pTransformType.firstPerson() && pItemStack.getItem() instanceof BlockItem)
                {
                    Block block = ((BlockItem)pItemStack.getItem()).getBlock();
                    flag1 = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
                }
                else
                {
                    flag1 = true;
                }

                if (pModel.isLayered())
                {
                    Reflector.ForgeHooksClient_drawItemLayered.call(this, pModel, pItemStack, pMatrixStack, pBuffer, pCombinedLight, pCombinedOverlay, flag1);
                }
                else
                {
                    RenderType rendertype = ItemBlockRenderTypes.getRenderType(pItemStack, flag1);
                    VertexConsumer vertexconsumer;

                    if (pItemStack.is(Items.COMPASS) && pItemStack.hasFoil())
                    {
                        pMatrixStack.pushPose();
                        PoseStack.Pose posestack$pose = pMatrixStack.last();

                        if (pTransformType == ItemTransforms.TransformType.GUI)
                        {
                            posestack$pose.pose().multiply(0.5F);
                        }
                        else if (pTransformType.firstPerson())
                        {
                            posestack$pose.pose().multiply(0.75F);
                        }

                        if (flag1)
                        {
                            vertexconsumer = getCompassFoilBufferDirect(pBuffer, rendertype, posestack$pose);
                        }
                        else
                        {
                            vertexconsumer = getCompassFoilBuffer(pBuffer, rendertype, posestack$pose);
                        }

                        pMatrixStack.popPose();
                    }
                    else if (flag1)
                    {
                        vertexconsumer = getFoilBufferDirect(pBuffer, rendertype, true, pItemStack.hasFoil());
                    }
                    else
                    {
                        vertexconsumer = getFoilBuffer(pBuffer, rendertype, true, pItemStack.hasFoil());
                    }

                    if (Config.isCustomItems())
                    {
                        pModel = CustomItems.getCustomItemModel(pItemStack, pModel, ItemOverrides.lastModelLocation, false);
                        ItemOverrides.lastModelLocation = null;
                    }

                    if (EmissiveTextures.isActive())
                    {
                        EmissiveTextures.beginRender();
                    }

                    this.renderModelLists(pModel, pItemStack, pCombinedLight, pCombinedOverlay, pMatrixStack, vertexconsumer);

                    if (EmissiveTextures.isActive())
                    {
                        if (EmissiveTextures.hasEmissive())
                        {
                            EmissiveTextures.beginRenderEmissive();
                            VertexConsumer vertexconsumer1 = vertexconsumer instanceof VertexBuilderWrapper ? ((VertexBuilderWrapper)vertexconsumer).getVertexBuilder() : vertexconsumer;
                            this.renderModelLists(pModel, pItemStack, LightTexture.MAX_BRIGHTNESS, pCombinedOverlay, pMatrixStack, vertexconsumer1);
                            EmissiveTextures.endRenderEmissive();
                        }

                        EmissiveTextures.endRender();
                    }
                }
            }
            else if (Reflector.MinecraftForgeClient.exists())
            {
                RenderProperties.get(pItemStack).getItemStackRenderer().renderByItem(pItemStack, pTransformType, pMatrixStack, pBuffer, pCombinedLight, pCombinedOverlay);
            }
            else
            {
                this.blockEntityRenderer.renderByItem(pItemStack, pTransformType, pMatrixStack, pBuffer, pCombinedLight, pCombinedOverlay);
            }

            pMatrixStack.popPose();
        }
    }

    public static VertexConsumer getArmorFoilBuffer(MultiBufferSource pBuffer, RenderType pRenderType, boolean pNoEntity, boolean pWithGlint)
    {
        if (Shaders.isShadowPass)
        {
            pWithGlint = false;
        }

        if (EmissiveTextures.isRenderEmissive())
        {
            pWithGlint = false;
        }

        return pWithGlint ? VertexMultiConsumer.create(pBuffer.getBuffer(pNoEntity ? RenderType.armorGlint() : RenderType.armorEntityGlint()), pBuffer.getBuffer(pRenderType)) : pBuffer.getBuffer(pRenderType);
    }

    public static VertexConsumer getCompassFoilBuffer(MultiBufferSource pBuffer, RenderType pRenderType, PoseStack.Pose pMatrixEntry)
    {
        return VertexMultiConsumer.create(new SheetedDecalTextureGenerator(pBuffer.getBuffer(RenderType.glint()), pMatrixEntry.pose(), pMatrixEntry.normal()), pBuffer.getBuffer(pRenderType));
    }

    public static VertexConsumer getCompassFoilBufferDirect(MultiBufferSource pBuffer, RenderType pRenderType, PoseStack.Pose pMatrixEntry)
    {
        return VertexMultiConsumer.create(new SheetedDecalTextureGenerator(pBuffer.getBuffer(RenderType.glintDirect()), pMatrixEntry.pose(), pMatrixEntry.normal()), pBuffer.getBuffer(pRenderType));
    }

    public static VertexConsumer getFoilBuffer(MultiBufferSource pBuffer, RenderType pRenderType, boolean pIsItem, boolean pGlint)
    {
        if (Shaders.isShadowPass)
        {
            pGlint = false;
        }

        if (EmissiveTextures.isRenderEmissive())
        {
            pGlint = false;
        }

        if (!pGlint)
        {
            return pBuffer.getBuffer(pRenderType);
        }
        else
        {
            return Minecraft.useShaderTransparency() && pRenderType == Sheets.translucentItemSheet() ? VertexMultiConsumer.create(pBuffer.getBuffer(RenderType.glintTranslucent()), pBuffer.getBuffer(pRenderType)) : VertexMultiConsumer.create(pBuffer.getBuffer(pIsItem ? RenderType.glint() : RenderType.entityGlint()), pBuffer.getBuffer(pRenderType));
        }
    }

    public static VertexConsumer getFoilBufferDirect(MultiBufferSource pBuffer, RenderType pRenderType, boolean pNoEntity, boolean pWithGlint)
    {
        if (Shaders.isShadowPass)
        {
            pWithGlint = false;
        }

        if (EmissiveTextures.isRenderEmissive())
        {
            pWithGlint = false;
        }

        return pWithGlint ? VertexMultiConsumer.create(pBuffer.getBuffer(pNoEntity ? RenderType.glintDirect() : RenderType.entityGlintDirect()), pBuffer.getBuffer(pRenderType)) : pBuffer.getBuffer(pRenderType);
    }

    private void renderQuadList(PoseStack pMatrixStack, VertexConsumer pBuffer, List<BakedQuad> pQuads, ItemStack pItemStack, int pCombinedLight, int pCombinedOverlay)
    {
        boolean flag = !pItemStack.isEmpty();
        PoseStack.Pose posestack$pose = pMatrixStack.last();
        boolean flag1 = EmissiveTextures.isActive();
        int i = pQuads.size();

        for (int j = 0; j < i; ++j)
        {
            BakedQuad bakedquad = pQuads.get(j);

            if (flag1)
            {
                bakedquad = EmissiveTextures.getEmissiveQuad(bakedquad);

                if (bakedquad == null)
                {
                    continue;
                }
            }

            int k = -1;

            if (flag && bakedquad.isTinted())
            {
                k = this.itemColors.getColor(pItemStack, bakedquad.getTintIndex());

                if (Config.isCustomColors())
                {
                    k = CustomColors.getColorFromItemStack(pItemStack, bakedquad.getTintIndex(), k);
                }
            }

            float f = (float)(k >> 16 & 255) / 255.0F;
            float f1 = (float)(k >> 8 & 255) / 255.0F;
            float f2 = (float)(k & 255) / 255.0F;

            if (Reflector.ForgeHooksClient.exists())
            {
                pBuffer.putBulkData(posestack$pose, bakedquad, f, f1, f2, pCombinedLight, pCombinedOverlay, true);
            }
            else
            {
                pBuffer.putBulkData(posestack$pose, bakedquad, f, f1, f2, pCombinedLight, pCombinedOverlay);
            }
        }
    }

    public BakedModel getModel(ItemStack p_174265_, @Nullable Level p_174266_, @Nullable LivingEntity p_174267_, int p_174268_)
    {
        BakedModel bakedmodel;

        if (p_174265_.is(Items.TRIDENT))
        {
            bakedmodel = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:trident_in_hand#inventory"));
        }
        else if (p_174265_.is(Items.SPYGLASS))
        {
            bakedmodel = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation("minecraft:spyglass_in_hand#inventory"));
        }
        else
        {
            bakedmodel = this.itemModelShaper.getItemModel(p_174265_);
        }

        ClientLevel clientlevel = p_174266_ instanceof ClientLevel ? (ClientLevel)p_174266_ : null;
        ItemOverrides.lastModelLocation = null;
        BakedModel bakedmodel1 = bakedmodel.getOverrides().resolve(bakedmodel, p_174265_, clientlevel, p_174267_, p_174268_);

        if (Config.isCustomItems())
        {
            bakedmodel1 = CustomItems.getCustomItemModel(p_174265_, bakedmodel1, ItemOverrides.lastModelLocation, true);
        }

        return bakedmodel1 == null ? this.itemModelShaper.getModelManager().getMissingModel() : bakedmodel1;
    }

    public void renderStatic(ItemStack p_174270_, ItemTransforms.TransformType p_174271_, int p_174272_, int p_174273_, PoseStack p_174274_, MultiBufferSource p_174275_, int p_174276_)
    {
        this.renderStatic((LivingEntity)null, p_174270_, p_174271_, false, p_174274_, p_174275_, (Level)null, p_174272_, p_174273_, p_174276_);
    }

    public void renderStatic(@Nullable LivingEntity p_174243_, ItemStack p_174244_, ItemTransforms.TransformType p_174245_, boolean p_174246_, PoseStack p_174247_, MultiBufferSource p_174248_, @Nullable Level p_174249_, int p_174250_, int p_174251_, int p_174252_)
    {
        if (!p_174244_.isEmpty())
        {
            BakedModel bakedmodel = this.getModel(p_174244_, p_174249_, p_174243_, p_174252_);
            this.render(p_174244_, p_174245_, p_174246_, p_174247_, p_174248_, p_174250_, p_174251_, bakedmodel);
        }
    }

    public void renderGuiItem(ItemStack pStack, int pX, int pY)
    {
        this.renderGuiItem(pStack, pX, pY, this.getModel(pStack, (Level)null, (LivingEntity)null, 0));
    }

    protected void renderGuiItem(ItemStack pStack, int pX, int pY, BakedModel p_115131_)
    {
        renderItemGui = true;
        this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate((double)pX, (double)pY, (double)(100.0F + this.blitOffset));
        posestack.translate(8.0D, 8.0D, 0.0D);
        posestack.scale(1.0F, -1.0F, 1.0F);
        posestack.scale(16.0F, 16.0F, 16.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack posestack1 = new PoseStack();
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean flag = !p_115131_.usesBlockLight();

        if (flag)
        {
            Lighting.setupForFlatItems();
        }

        this.render(pStack, ItemTransforms.TransformType.GUI, false, posestack1, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, p_115131_);
        multibuffersource$buffersource.endBatch();
        RenderSystem.enableDepthTest();

        if (flag)
        {
            Lighting.setupFor3DItems();
        }

        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        renderItemGui = false;
    }

    public void renderAndDecorateItem(ItemStack pStack, int pXPosition, int pYPosition)
    {
        this.tryRenderGuiItem(Minecraft.getInstance().player, pStack, pXPosition, pYPosition, 0);
    }

    public void renderAndDecorateItem(ItemStack pStack, int pXPosition, int pYPosition, int p_174257_)
    {
        this.tryRenderGuiItem(Minecraft.getInstance().player, pStack, pXPosition, pYPosition, p_174257_);
    }

    public void renderAndDecorateItem(ItemStack pStack, int pXPosition, int pYPosition, int p_174262_, int p_174263_)
    {
        this.tryRenderGuiItem(Minecraft.getInstance().player, pStack, pXPosition, pYPosition, p_174262_, p_174263_);
    }

    public void renderAndDecorateFakeItem(ItemStack pStack, int pX, int pY)
    {
        this.tryRenderGuiItem((LivingEntity)null, pStack, pX, pY, 0);
    }

    public void renderAndDecorateItem(LivingEntity pStack, ItemStack pXPosition, int pYPosition, int p_174233_, int p_174234_)
    {
        this.tryRenderGuiItem(pStack, pXPosition, pYPosition, p_174233_, p_174234_);
    }

    private void tryRenderGuiItem(@Nullable LivingEntity p_174278_, ItemStack p_174279_, int p_174280_, int p_174281_, int p_174282_)
    {
        this.tryRenderGuiItem(p_174278_, p_174279_, p_174280_, p_174281_, p_174282_, 0);
    }

    private void tryRenderGuiItem(@Nullable LivingEntity p_174236_, ItemStack p_174237_, int p_174238_, int p_174239_, int p_174240_, int p_174241_)
    {
        if (!p_174237_.isEmpty())
        {
            BakedModel bakedmodel = this.getModel(p_174237_, (Level)null, p_174236_, p_174240_);
            this.blitOffset = bakedmodel.isGui3d() ? this.blitOffset + 50.0F + (float)p_174241_ : this.blitOffset + 50.0F;

            try
            {
                this.renderGuiItem(p_174237_, p_174238_, p_174239_, bakedmodel);
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
                crashreportcategory.setDetail("Item Type", () ->
                {
                    return String.valueOf((Object)p_174237_.getItem());
                });
                crashreportcategory.setDetail("Registry Name", () ->
                {
                    return String.valueOf(Reflector.call(p_174237_.getItem(), Reflector.ForgeRegistryEntry_getRegistryName));
                });
                crashreportcategory.setDetail("Item Damage", () ->
                {
                    return String.valueOf(p_174237_.getDamageValue());
                });
                crashreportcategory.setDetail("Item NBT", () ->
                {
                    return String.valueOf((Object)p_174237_.getTag());
                });
                crashreportcategory.setDetail("Item Foil", () ->
                {
                    return String.valueOf(p_174237_.hasFoil());
                });
                throw new ReportedException(crashreport);
            }

            this.blitOffset = bakedmodel.isGui3d() ? this.blitOffset - 50.0F - (float)p_174241_ : this.blitOffset - 50.0F;
        }
    }

    public void renderGuiItemDecorations(Font pFr, ItemStack pStack, int pXPosition, int pYPosition)
    {
        this.renderGuiItemDecorations(pFr, pStack, pXPosition, pYPosition, (String)null);
    }

    public void renderGuiItemDecorations(Font pFr, ItemStack pStack, int pXPosition, int pYPosition, @Nullable String p_115179_)
    {
        if (!pStack.isEmpty())
        {
            PoseStack posestack = new PoseStack();

            if (pStack.getCount() != 1 || p_115179_ != null)
            {
                String s = p_115179_ == null ? String.valueOf(pStack.getCount()) : p_115179_;
                posestack.translate(0.0D, 0.0D, (double)(this.blitOffset + 200.0F));
                MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                pFr.drawInBatch(s, (float)(pXPosition + 19 - 2 - pFr.width(s)), (float)(pYPosition + 6 + 3), 16777215, true, posestack.last().pose(), multibuffersource$buffersource, false, 0, 15728880);
                multibuffersource$buffersource.endBatch();
            }

            if (ReflectorForge.isBarVisible(pStack))
            {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.disableBlend();
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder bufferbuilder = tesselator.getBuilder();
                int i = pStack.getBarWidth();
                int j = pStack.getBarColor();

                if (Reflector.IForgeItem_getDurabilityForDisplay.exists() && Reflector.IForgeItem_getRGBDurabilityForDisplay.exists())
                {
                    double d0 = Reflector.callDouble(pStack.getItem(), Reflector.IForgeItem_getDurabilityForDisplay, pStack);
                    int k = Reflector.callInt(pStack.getItem(), Reflector.IForgeItem_getRGBDurabilityForDisplay, pStack);
                    i = Math.round(13.0F - (float)d0 * 13.0F);
                    j = k;
                }

                if (Config.isCustomColors())
                {
                    float f2 = (float)pStack.getDamageValue();
                    float f = (float)pStack.getMaxDamage();
                    float f3 = Math.max(0.0F, (f - f2) / f);
                    j = CustomColors.getDurabilityColor(f3, j);
                }

                this.fillRect(bufferbuilder, pXPosition + 2, pYPosition + 13, 13, 2, 0, 0, 0, 255);
                this.fillRect(bufferbuilder, pXPosition + 2, pYPosition + 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
                RenderSystem.enableBlend();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

            LocalPlayer localplayer = Minecraft.getInstance().player;
            float f1 = localplayer == null ? 0.0F : localplayer.getCooldowns().getCooldownPercent(pStack.getItem(), Minecraft.getInstance().getFrameTime());

            if (f1 > 0.0F)
            {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                Tesselator tesselator1 = Tesselator.getInstance();
                BufferBuilder bufferbuilder1 = tesselator1.getBuilder();
                this.fillRect(bufferbuilder1, pXPosition, pYPosition + Mth.floor(16.0F * (1.0F - f1)), 16, Mth.ceil(16.0F * f1), 255, 255, 255, 127);
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }
        }
    }

    private void fillRect(BufferBuilder pRenderer, int pX, int pY, int pWidth, int pHeight, int pRed, int pGreen, int pBlue, int pAlpha)
    {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        pRenderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        pRenderer.vertex((double)(pX + 0), (double)(pY + 0), 0.0D).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pRenderer.vertex((double)(pX + 0), (double)(pY + pHeight), 0.0D).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pRenderer.vertex((double)(pX + pWidth), (double)(pY + pHeight), 0.0D).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pRenderer.vertex((double)(pX + pWidth), (double)(pY + 0), 0.0D).color(pRed, pGreen, pBlue, pAlpha).endVertex();
        pRenderer.end();
        BufferUploader.end(pRenderer);
    }

    public void onResourceManagerReload(ResourceManager pResourceManager)
    {
        this.itemModelShaper.rebuildCache();
    }

    public static boolean isRenderItemGui()
    {
        return renderItemGui;
    }

    public BlockEntityWithoutLevelRenderer getBlockEntityRenderer()
    {
        return this.blockEntityRenderer;
    }

    public IResourceType getResourceType()
    {
        return VanillaResourceType.MODELS;
    }
}
