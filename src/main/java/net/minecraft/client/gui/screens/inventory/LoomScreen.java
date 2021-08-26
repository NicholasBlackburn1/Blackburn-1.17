package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;

public class LoomScreen extends AbstractContainerScreen<LoomMenu>
{
    private static final ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/loom.png");
    private static final int BASE_PATTERN_INDEX = 1;
    private static final int PATTERN_COLUMNS = 4;
    private static final int PATTERN_ROWS = 4;
    private static final int TOTAL_PATTERN_ROWS = (BannerPattern.COUNT - BannerPattern.PATTERN_ITEM_COUNT - 1 + 4 - 1) / 4;
    private static final int SCROLLER_WIDTH = 12;
    private static final int SCROLLER_HEIGHT = 15;
    private static final int PATTERN_IMAGE_SIZE = 14;
    private static final int SCROLLER_FULL_HEIGHT = 56;
    private static final int PATTERNS_X = 60;
    private static final int PATTERNS_Y = 13;
    private ModelPart flag;
    @Nullable
    private List<Pair<BannerPattern, DyeColor>> resultBannerPatterns;
    private ItemStack bannerStack = ItemStack.EMPTY;
    private ItemStack dyeStack = ItemStack.EMPTY;
    private ItemStack patternStack = ItemStack.EMPTY;
    private boolean displayPatterns;
    private boolean displaySpecialPattern;
    private boolean hasMaxPatterns;
    private float scrollOffs;
    private boolean scrolling;
    private int startIndex = 1;

    public LoomScreen(LoomMenu p_99075_, Inventory p_99076_, Component p_99077_)
    {
        super(p_99075_, p_99076_, p_99077_);
        p_99075_.registerUpdateListener(this::containerChanged);
        this.titleLabelY -= 2;
    }

    protected void init()
    {
        super.init();
        this.flag = this.minecraft.getEntityModels().bakeLayer(ModelLayers.BANNER).getChilds("flag");
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        this.renderTooltip(pMatrixStack, pMouseX, pMouseY);
    }

    protected void renderBg(PoseStack pMatrixStack, float pPartialTicks, int pX, int pY)
    {
        this.renderBackground(pMatrixStack);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BG_LOCATION);
        int i = this.leftPos;
        int j = this.topPos;
        this.blit(pMatrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
        Slot slot = this.menu.getBannerSlot();
        Slot slot1 = this.menu.getDyeSlot();
        Slot slot2 = this.menu.getPatternSlot();
        Slot slot3 = this.menu.getResultSlot();

        if (!slot.hasItem())
        {
            this.blit(pMatrixStack, i + slot.x, j + slot.y, this.imageWidth, 0, 16, 16);
        }

        if (!slot1.hasItem())
        {
            this.blit(pMatrixStack, i + slot1.x, j + slot1.y, this.imageWidth + 16, 0, 16, 16);
        }

        if (!slot2.hasItem())
        {
            this.blit(pMatrixStack, i + slot2.x, j + slot2.y, this.imageWidth + 32, 0, 16, 16);
        }

        int k = (int)(41.0F * this.scrollOffs);
        this.blit(pMatrixStack, i + 119, j + 13 + k, 232 + (this.displayPatterns ? 0 : 12), 0, 12, 15);
        Lighting.setupForFlatItems();

        if (this.resultBannerPatterns != null && !this.hasMaxPatterns)
        {
            MultiBufferSource.BufferSource multibuffersource$buffersource = this.minecraft.renderBuffers().bufferSource();
            pMatrixStack.pushPose();
            pMatrixStack.translate((double)(i + 139), (double)(j + 52), 0.0D);
            pMatrixStack.scale(24.0F, -24.0F, 1.0F);
            pMatrixStack.translate(0.5D, 0.5D, 0.5D);
            float f = 0.6666667F;
            pMatrixStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
            this.flag.xRot = 0.0F;
            this.flag.y = -32.0F;
            BannerRenderer.renderPatterns(pMatrixStack, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, this.flag, ModelBakery.BANNER_BASE, true, this.resultBannerPatterns);
            pMatrixStack.popPose();
            multibuffersource$buffersource.endBatch();
        }
        else if (this.hasMaxPatterns)
        {
            this.blit(pMatrixStack, i + slot3.x - 2, j + slot3.y - 2, this.imageWidth, 17, 17, 16);
        }

        if (this.displayPatterns)
        {
            int j2 = i + 60;
            int l2 = j + 13;
            int l = this.startIndex + 16;

            for (int i1 = this.startIndex; i1 < l && i1 < BannerPattern.COUNT - BannerPattern.PATTERN_ITEM_COUNT; ++i1)
            {
                int j1 = i1 - this.startIndex;
                int k1 = j2 + j1 % 4 * 14;
                int l1 = l2 + j1 / 4 * 14;
                RenderSystem.setShaderTexture(0, BG_LOCATION);
                int i2 = this.imageHeight;

                if (i1 == this.menu.getSelectedBannerPatternIndex())
                {
                    i2 += 14;
                }
                else if (pX >= k1 && pY >= l1 && pX < k1 + 14 && pY < l1 + 14)
                {
                    i2 += 28;
                }

                this.blit(pMatrixStack, k1, l1, 0, i2, 14, 14);
                this.renderPattern(i1, k1, l1);
            }
        }
        else if (this.displaySpecialPattern)
        {
            int k2 = i + 60;
            int i3 = j + 13;
            RenderSystem.setShaderTexture(0, BG_LOCATION);
            this.blit(pMatrixStack, k2, i3, 0, this.imageHeight, 14, 14);
            int j3 = this.menu.getSelectedBannerPatternIndex();
            this.renderPattern(j3, k2, i3);
        }

        Lighting.setupFor3DItems();
    }

    private void renderPattern(int p_99109_, int p_99110_, int p_99111_)
    {
        ItemStack itemstack = new ItemStack(Items.GRAY_BANNER);
        CompoundTag compoundtag = itemstack.getOrCreateTagElement("BlockEntityTag");
        ListTag listtag = (new BannerPattern.Builder()).addPattern(BannerPattern.BASE, DyeColor.GRAY).addPattern(BannerPattern.values()[p_99109_], DyeColor.WHITE).toListTag();
        compoundtag.put("Patterns", listtag);
        PoseStack posestack = new PoseStack();
        posestack.pushPose();
        posestack.translate((double)((float)p_99110_ + 0.5F), (double)(p_99111_ + 16), 0.0D);
        posestack.scale(6.0F, -6.0F, 1.0F);
        posestack.translate(0.5D, 0.5D, 0.0D);
        posestack.translate(0.5D, 0.5D, 0.5D);
        float f = 0.6666667F;
        posestack.scale(0.6666667F, -0.6666667F, -0.6666667F);
        MultiBufferSource.BufferSource multibuffersource$buffersource = this.minecraft.renderBuffers().bufferSource();
        this.flag.xRot = 0.0F;
        this.flag.y = -32.0F;
        List<Pair<BannerPattern, DyeColor>> list = BannerBlockEntity.createPatterns(DyeColor.GRAY, BannerBlockEntity.getItemPatterns(itemstack));
        BannerRenderer.renderPatterns(posestack, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, this.flag, ModelBakery.BANNER_BASE, true, list);
        posestack.popPose();
        multibuffersource$buffersource.endBatch();
    }

    public boolean mouseClicked(double pMouseX, double p_99084_, int pMouseY)
    {
        this.scrolling = false;

        if (this.displayPatterns)
        {
            int i = this.leftPos + 60;
            int j = this.topPos + 13;
            int k = this.startIndex + 16;

            for (int l = this.startIndex; l < k; ++l)
            {
                int i1 = l - this.startIndex;
                double d0 = pMouseX - (double)(i + i1 % 4 * 14);
                double d1 = p_99084_ - (double)(j + i1 / 4 * 14);

                if (d0 >= 0.0D && d1 >= 0.0D && d0 < 14.0D && d1 < 14.0D && this.menu.clickMenuButton(this.minecraft.player, l))
                {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_LOOM_SELECT_PATTERN, 1.0F));
                    this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, l);
                    return true;
                }
            }

            i = this.leftPos + 119;
            j = this.topPos + 9;

            if (pMouseX >= (double)i && pMouseX < (double)(i + 12) && p_99084_ >= (double)j && p_99084_ < (double)(j + 56))
            {
                this.scrolling = true;
            }
        }

        return super.mouseClicked(pMouseX, p_99084_, pMouseY);
    }

    public boolean mouseDragged(double pMouseX, double p_99088_, int pMouseY, double p_99090_, double pButton)
    {
        if (this.scrolling && this.displayPatterns)
        {
            int i = this.topPos + 13;
            int j = i + 56;
            this.scrollOffs = ((float)p_99088_ - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            int k = TOTAL_PATTERN_ROWS - 4;
            int l = (int)((double)(this.scrollOffs * (float)k) + 0.5D);

            if (l < 0)
            {
                l = 0;
            }

            this.startIndex = 1 + l * 4;
            return true;
        }
        else
        {
            return super.mouseDragged(pMouseX, p_99088_, pMouseY, p_99090_, pButton);
        }
    }

    public boolean mouseScrolled(double pMouseX, double p_99080_, double pMouseY)
    {
        if (this.displayPatterns)
        {
            int i = TOTAL_PATTERN_ROWS - 4;
            this.scrollOffs = (float)((double)this.scrollOffs - pMouseY / (double)i);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = 1 + (int)((double)(this.scrollOffs * (float)i) + 0.5D) * 4;
        }

        return true;
    }

    protected boolean hasClickedOutside(double pMouseX, double p_99094_, int pMouseY, int p_99096_, int pGuiLeft)
    {
        return pMouseX < (double)pMouseY || p_99094_ < (double)p_99096_ || pMouseX >= (double)(pMouseY + this.imageWidth) || p_99094_ >= (double)(p_99096_ + this.imageHeight);
    }

    private void containerChanged()
    {
        ItemStack itemstack = this.menu.getResultSlot().getItem();

        if (itemstack.isEmpty())
        {
            this.resultBannerPatterns = null;
        }
        else
        {
            this.resultBannerPatterns = BannerBlockEntity.createPatterns(((BannerItem)itemstack.getItem()).getColor(), BannerBlockEntity.getItemPatterns(itemstack));
        }

        ItemStack itemstack1 = this.menu.getBannerSlot().getItem();
        ItemStack itemstack2 = this.menu.getDyeSlot().getItem();
        ItemStack itemstack3 = this.menu.getPatternSlot().getItem();
        CompoundTag compoundtag = itemstack1.getOrCreateTagElement("BlockEntityTag");
        this.hasMaxPatterns = compoundtag.contains("Patterns", 9) && !itemstack1.isEmpty() && compoundtag.getList("Patterns", 10).size() >= 6;

        if (this.hasMaxPatterns)
        {
            this.resultBannerPatterns = null;
        }

        if (!ItemStack.matches(itemstack1, this.bannerStack) || !ItemStack.matches(itemstack2, this.dyeStack) || !ItemStack.matches(itemstack3, this.patternStack))
        {
            this.displayPatterns = !itemstack1.isEmpty() && !itemstack2.isEmpty() && itemstack3.isEmpty() && !this.hasMaxPatterns;
            this.displaySpecialPattern = !this.hasMaxPatterns && !itemstack3.isEmpty() && !itemstack1.isEmpty() && !itemstack2.isEmpty();
        }

        this.bannerStack = itemstack1.copy();
        this.dyeStack = itemstack2.copy();
        this.patternStack = itemstack3.copy();
    }
}
