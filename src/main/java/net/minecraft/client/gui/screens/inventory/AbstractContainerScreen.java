package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractContainerScreen<T extends AbstractContainerMenu> extends Screen implements MenuAccess<T>
{
    public static final ResourceLocation INVENTORY_LOCATION = new ResourceLocation("textures/gui/container/inventory.png");
    private static final float SNAPBACK_SPEED = 100.0F;
    private static final int QUICKDROP_DELAY = 500;
    private static final int DOUBLECLICK_SPEED = 250;
    public static final int SLOT_ITEM_BLIT_OFFSET = 100;
    private static final int HOVER_ITEM_BLIT_OFFSET = 200;
    protected int imageWidth = 176;
    protected int imageHeight = 166;
    protected int titleLabelX;
    protected int titleLabelY;
    protected int inventoryLabelX;
    protected int inventoryLabelY;
    protected final T menu;
    protected final Component playerInventoryTitle;
    @Nullable
    protected Slot hoveredSlot;
    @Nullable
    private Slot clickedSlot;
    @Nullable
    private Slot snapbackEnd;
    @Nullable
    private Slot quickdropSlot;
    @Nullable
    private Slot lastClickSlot;
    protected int leftPos;
    protected int topPos;
    private boolean isSplittingStack;
    private ItemStack draggingItem = ItemStack.EMPTY;
    private int snapbackStartX;
    private int snapbackStartY;
    private long snapbackTime;
    private ItemStack snapbackItem = ItemStack.EMPTY;
    private long quickdropTime;
    protected final Set<Slot> quickCraftSlots = Sets.newHashSet();
    protected boolean isQuickCrafting;
    private int quickCraftingType;
    private int quickCraftingButton;
    private boolean skipNextRelease;
    private int quickCraftingRemainder;
    private long lastClickTime;
    private int lastClickButton;
    private boolean doubleclick;
    private ItemStack lastQuickMoved = ItemStack.EMPTY;

    public AbstractContainerScreen(T p_97741_, Inventory p_97742_, Component p_97743_)
    {
        super(p_97743_);
        this.menu = p_97741_;
        this.playerInventoryTitle = p_97742_.getDisplayName();
        this.skipNextRelease = true;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    protected void init()
    {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        int i = this.leftPos;
        int j = this.topPos;
        this.renderBg(pMatrixStack, pPartialTicks, pMouseX, pMouseY);
        RenderSystem.disableDepthTest();
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate((double)i, (double)j, 0.0D);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.hoveredSlot = null;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        for (int k = 0; k < this.menu.slots.size(); ++k)
        {
            Slot slot = this.menu.slots.get(k);

            if (slot.isActive())
            {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                this.renderSlot(pMatrixStack, slot);
            }

            if (this.isHovering(slot, (double)pMouseX, (double)pMouseY) && slot.isActive())
            {
                this.hoveredSlot = slot;
                int l = slot.x;
                int i1 = slot.y;
                renderSlotHighlight(pMatrixStack, l, i1, this.getBlitOffset());
            }
        }

        this.renderLabels(pMatrixStack, pMouseX, pMouseY);
        ItemStack itemstack = this.draggingItem.isEmpty() ? this.menu.getCarried() : this.draggingItem;

        if (!itemstack.isEmpty())
        {
            int l1 = 8;
            int i2 = this.draggingItem.isEmpty() ? 8 : 16;
            String s = null;

            if (!this.draggingItem.isEmpty() && this.isSplittingStack)
            {
                itemstack = itemstack.copy();
                itemstack.setCount(Mth.ceil((float)itemstack.getCount() / 2.0F));
            }
            else if (this.isQuickCrafting && this.quickCraftSlots.size() > 1)
            {
                itemstack = itemstack.copy();
                itemstack.setCount(this.quickCraftingRemainder);

                if (itemstack.isEmpty())
                {
                    s = ChatFormatting.YELLOW + "0";
                }
            }

            this.renderFloatingItem(itemstack, pMouseX - i - 8, pMouseY - j - i2, s);
        }

        if (!this.snapbackItem.isEmpty())
        {
            float f = (float)(Util.getMillis() - this.snapbackTime) / 100.0F;

            if (f >= 1.0F)
            {
                f = 1.0F;
                this.snapbackItem = ItemStack.EMPTY;
            }

            int j2 = this.snapbackEnd.x - this.snapbackStartX;
            int k2 = this.snapbackEnd.y - this.snapbackStartY;
            int j1 = this.snapbackStartX + (int)((float)j2 * f);
            int k1 = this.snapbackStartY + (int)((float)k2 * f);
            this.renderFloatingItem(this.snapbackItem, j1, k1, (String)null);
        }

        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();
    }

    public static void renderSlotHighlight(PoseStack p_169607_, int p_169608_, int p_169609_, int p_169610_)
    {
        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        fillGradient(p_169607_, p_169608_, p_169609_, p_169608_ + 16, p_169609_ + 16, -2130706433, -2130706433, p_169610_);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();
    }

    protected void renderTooltip(PoseStack pMatrixStack, int pX, int pY)
    {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem())
        {
            this.renderTooltip(pMatrixStack, this.hoveredSlot.getItem(), pX, pY);
        }
    }

    private void renderFloatingItem(ItemStack pStack, int pX, int pY, String pAltText)
    {
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.translate(0.0D, 0.0D, 32.0D);
        RenderSystem.applyModelViewMatrix();
        this.setBlitOffset(200);
        this.itemRenderer.blitOffset = 200.0F;
        this.itemRenderer.renderAndDecorateItem(pStack, pX, pY);
        this.itemRenderer.renderGuiItemDecorations(this.font, pStack, pX, pY - (this.draggingItem.isEmpty() ? 0 : 8), pAltText);
        this.setBlitOffset(0);
        this.itemRenderer.blitOffset = 0.0F;
    }

    protected void renderLabels(PoseStack pMatrixStack, int pX, int pY)
    {
        this.font.draw(pMatrixStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        this.font.draw(pMatrixStack, this.playerInventoryTitle, (float)this.inventoryLabelX, (float)this.inventoryLabelY, 4210752);
    }

    protected abstract void renderBg(PoseStack pMatrixStack, float pPartialTicks, int pX, int pY);

    private void renderSlot(PoseStack pMatrixStack, Slot pSlot)
    {
        int i = pSlot.x;
        int j = pSlot.y;
        ItemStack itemstack = pSlot.getItem();
        boolean flag = false;
        boolean flag1 = pSlot == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
        ItemStack itemstack1 = this.menu.getCarried();
        String s = null;

        if (pSlot == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !itemstack.isEmpty())
        {
            itemstack = itemstack.copy();
            itemstack.setCount(itemstack.getCount() / 2);
        }
        else if (this.isQuickCrafting && this.quickCraftSlots.contains(pSlot) && !itemstack1.isEmpty())
        {
            if (this.quickCraftSlots.size() == 1)
            {
                return;
            }

            if (AbstractContainerMenu.canItemQuickReplace(pSlot, itemstack1, true) && this.menu.canDragTo(pSlot))
            {
                itemstack = itemstack1.copy();
                flag = true;
                AbstractContainerMenu.getQuickCraftSlotCount(this.quickCraftSlots, this.quickCraftingType, itemstack, pSlot.getItem().isEmpty() ? 0 : pSlot.getItem().getCount());
                int k = Math.min(itemstack.getMaxStackSize(), pSlot.getMaxStackSize(itemstack));

                if (itemstack.getCount() > k)
                {
                    s = ChatFormatting.YELLOW.toString() + k;
                    itemstack.setCount(k);
                }
            }
            else
            {
                this.quickCraftSlots.remove(pSlot);
                this.recalculateQuickCraftRemaining();
            }
        }

        this.setBlitOffset(100);
        this.itemRenderer.blitOffset = 100.0F;

        if (itemstack.isEmpty() && pSlot.isActive())
        {
            Pair<ResourceLocation, ResourceLocation> pair = pSlot.getNoItemIcon();

            if (pair != null)
            {
                TextureAtlasSprite textureatlassprite = this.minecraft.getTextureAtlas(pair.getFirst()).apply(pair.getSecond());
                RenderSystem.setShaderTexture(0, textureatlassprite.atlas().location());
                blit(pMatrixStack, i, j, this.getBlitOffset(), 16, 16, textureatlassprite);
                flag1 = true;
            }
        }

        if (!flag1)
        {
            if (flag)
            {
                fill(pMatrixStack, i, j, i + 16, j + 16, -2130706433);
            }

            RenderSystem.enableDepthTest();
            this.itemRenderer.renderAndDecorateItem(this.minecraft.player, itemstack, i, j, pSlot.x + pSlot.y * this.imageWidth);
            this.itemRenderer.renderGuiItemDecorations(this.font, itemstack, i, j, s);
        }

        this.itemRenderer.blitOffset = 0.0F;
        this.setBlitOffset(0);
    }

    private void recalculateQuickCraftRemaining()
    {
        ItemStack itemstack = this.menu.getCarried();

        if (!itemstack.isEmpty() && this.isQuickCrafting)
        {
            if (this.quickCraftingType == 2)
            {
                this.quickCraftingRemainder = itemstack.getMaxStackSize();
            }
            else
            {
                this.quickCraftingRemainder = itemstack.getCount();

                for (Slot slot : this.quickCraftSlots)
                {
                    ItemStack itemstack1 = itemstack.copy();
                    ItemStack itemstack2 = slot.getItem();
                    int i = itemstack2.isEmpty() ? 0 : itemstack2.getCount();
                    AbstractContainerMenu.getQuickCraftSlotCount(this.quickCraftSlots, this.quickCraftingType, itemstack1, i);
                    int j = Math.min(itemstack1.getMaxStackSize(), slot.getMaxStackSize(itemstack1));

                    if (itemstack1.getCount() > j)
                    {
                        itemstack1.setCount(j);
                    }

                    this.quickCraftingRemainder -= itemstack1.getCount() - i;
                }
            }
        }
    }

    @Nullable
    private Slot findSlot(double pMouseX, double p_97746_)
    {
        for (int i = 0; i < this.menu.slots.size(); ++i)
        {
            Slot slot = this.menu.slots.get(i);

            if (this.isHovering(slot, pMouseX, p_97746_) && slot.isActive())
            {
                return slot;
            }
        }

        return null;
    }

    public boolean mouseClicked(double pMouseX, double p_97749_, int pMouseY)
    {
        if (super.mouseClicked(pMouseX, p_97749_, pMouseY))
        {
            return true;
        }
        else
        {
            boolean flag = this.minecraft.options.keyPickItem.matchesMouse(pMouseY);
            Slot slot = this.findSlot(pMouseX, p_97749_);
            long i = Util.getMillis();
            this.doubleclick = this.lastClickSlot == slot && i - this.lastClickTime < 250L && this.lastClickButton == pMouseY;
            this.skipNextRelease = false;

            if (pMouseY != 0 && pMouseY != 1 && !flag)
            {
                this.checkHotbarMouseClicked(pMouseY);
            }
            else
            {
                int j = this.leftPos;
                int k = this.topPos;
                boolean flag1 = this.hasClickedOutside(pMouseX, p_97749_, j, k, pMouseY);
                int l = -1;

                if (slot != null)
                {
                    l = slot.index;
                }

                if (flag1)
                {
                    l = -999;
                }

                if (this.minecraft.options.touchscreen && flag1 && this.menu.getCarried().isEmpty())
                {
                    this.minecraft.setScreen((Screen)null);
                    return true;
                }

                if (l != -1)
                {
                    if (this.minecraft.options.touchscreen)
                    {
                        if (slot != null && slot.hasItem())
                        {
                            this.clickedSlot = slot;
                            this.draggingItem = ItemStack.EMPTY;
                            this.isSplittingStack = pMouseY == 1;
                        }
                        else
                        {
                            this.clickedSlot = null;
                        }
                    }
                    else if (!this.isQuickCrafting)
                    {
                        if (this.menu.getCarried().isEmpty())
                        {
                            if (this.minecraft.options.keyPickItem.matchesMouse(pMouseY))
                            {
                                this.slotClicked(slot, l, pMouseY, ClickType.CLONE);
                            }
                            else
                            {
                                boolean flag2 = l != -999 && (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344));
                                ClickType clicktype = ClickType.PICKUP;

                                if (flag2)
                                {
                                    this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
                                    clicktype = ClickType.QUICK_MOVE;
                                }
                                else if (l == -999)
                                {
                                    clicktype = ClickType.THROW;
                                }

                                this.slotClicked(slot, l, pMouseY, clicktype);
                            }

                            this.skipNextRelease = true;
                        }
                        else
                        {
                            this.isQuickCrafting = true;
                            this.quickCraftingButton = pMouseY;
                            this.quickCraftSlots.clear();

                            if (pMouseY == 0)
                            {
                                this.quickCraftingType = 0;
                            }
                            else if (pMouseY == 1)
                            {
                                this.quickCraftingType = 1;
                            }
                            else if (this.minecraft.options.keyPickItem.matchesMouse(pMouseY))
                            {
                                this.quickCraftingType = 2;
                            }
                        }
                    }
                }
            }

            this.lastClickSlot = slot;
            this.lastClickTime = i;
            this.lastClickButton = pMouseY;
            return true;
        }
    }

    private void checkHotbarMouseClicked(int pKeyCode)
    {
        if (this.hoveredSlot != null && this.menu.getCarried().isEmpty())
        {
            if (this.minecraft.options.keySwapOffhand.matchesMouse(pKeyCode))
            {
                this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 40, ClickType.SWAP);
                return;
            }

            for (int i = 0; i < 9; ++i)
            {
                if (this.minecraft.options.keyHotbarSlots[i].matchesMouse(pKeyCode))
                {
                    this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, i, ClickType.SWAP);
                }
            }
        }
    }

    protected boolean hasClickedOutside(double pMouseX, double p_97758_, int pMouseY, int p_97760_, int pGuiLeft)
    {
        return pMouseX < (double)pMouseY || p_97758_ < (double)p_97760_ || pMouseX >= (double)(pMouseY + this.imageWidth) || p_97758_ >= (double)(p_97760_ + this.imageHeight);
    }

    public boolean mouseDragged(double pMouseX, double p_97753_, int pMouseY, double p_97755_, double pButton)
    {
        Slot slot = this.findSlot(pMouseX, p_97753_);
        ItemStack itemstack = this.menu.getCarried();

        if (this.clickedSlot != null && this.minecraft.options.touchscreen)
        {
            if (pMouseY == 0 || pMouseY == 1)
            {
                if (this.draggingItem.isEmpty())
                {
                    if (slot != this.clickedSlot && !this.clickedSlot.getItem().isEmpty())
                    {
                        this.draggingItem = this.clickedSlot.getItem().copy();
                    }
                }
                else if (this.draggingItem.getCount() > 1 && slot != null && AbstractContainerMenu.canItemQuickReplace(slot, this.draggingItem, false))
                {
                    long i = Util.getMillis();

                    if (this.quickdropSlot == slot)
                    {
                        if (i - this.quickdropTime > 500L)
                        {
                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, 0, ClickType.PICKUP);
                            this.slotClicked(slot, slot.index, 1, ClickType.PICKUP);
                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, 0, ClickType.PICKUP);
                            this.quickdropTime = i + 750L;
                            this.draggingItem.shrink(1);
                        }
                    }
                    else
                    {
                        this.quickdropSlot = slot;
                        this.quickdropTime = i;
                    }
                }
            }
        }
        else if (this.isQuickCrafting && slot != null && !itemstack.isEmpty() && (itemstack.getCount() > this.quickCraftSlots.size() || this.quickCraftingType == 2) && AbstractContainerMenu.canItemQuickReplace(slot, itemstack, true) && slot.mayPlace(itemstack) && this.menu.canDragTo(slot))
        {
            this.quickCraftSlots.add(slot);
            this.recalculateQuickCraftRemaining();
        }

        return true;
    }

    public boolean mouseReleased(double pMouseX, double p_97813_, int pMouseY)
    {
        Slot slot = this.findSlot(pMouseX, p_97813_);
        int i = this.leftPos;
        int j = this.topPos;
        boolean flag = this.hasClickedOutside(pMouseX, p_97813_, i, j, pMouseY);
        int k = -1;

        if (slot != null)
        {
            k = slot.index;
        }

        if (flag)
        {
            k = -999;
        }

        if (this.doubleclick && slot != null && pMouseY == 0 && this.menu.canTakeItemForPickAll(ItemStack.EMPTY, slot))
        {
            if (hasShiftDown())
            {
                if (!this.lastQuickMoved.isEmpty())
                {
                    for (Slot slot2 : this.menu.slots)
                    {
                        if (slot2 != null && slot2.mayPickup(this.minecraft.player) && slot2.hasItem() && slot2.container == slot.container && AbstractContainerMenu.canItemQuickReplace(slot2, this.lastQuickMoved, true))
                        {
                            this.slotClicked(slot2, slot2.index, pMouseY, ClickType.QUICK_MOVE);
                        }
                    }
                }
            }
            else
            {
                this.slotClicked(slot, k, pMouseY, ClickType.PICKUP_ALL);
            }

            this.doubleclick = false;
            this.lastClickTime = 0L;
        }
        else
        {
            if (this.isQuickCrafting && this.quickCraftingButton != pMouseY)
            {
                this.isQuickCrafting = false;
                this.quickCraftSlots.clear();
                this.skipNextRelease = true;
                return true;
            }

            if (this.skipNextRelease)
            {
                this.skipNextRelease = false;
                return true;
            }

            if (this.clickedSlot != null && this.minecraft.options.touchscreen)
            {
                if (pMouseY == 0 || pMouseY == 1)
                {
                    if (this.draggingItem.isEmpty() && slot != this.clickedSlot)
                    {
                        this.draggingItem = this.clickedSlot.getItem();
                    }

                    boolean flag2 = AbstractContainerMenu.canItemQuickReplace(slot, this.draggingItem, false);

                    if (k != -1 && !this.draggingItem.isEmpty() && flag2)
                    {
                        this.slotClicked(this.clickedSlot, this.clickedSlot.index, pMouseY, ClickType.PICKUP);
                        this.slotClicked(slot, k, 0, ClickType.PICKUP);

                        if (this.menu.getCarried().isEmpty())
                        {
                            this.snapbackItem = ItemStack.EMPTY;
                        }
                        else
                        {
                            this.slotClicked(this.clickedSlot, this.clickedSlot.index, pMouseY, ClickType.PICKUP);
                            this.snapbackStartX = Mth.floor(pMouseX - (double)i);
                            this.snapbackStartY = Mth.floor(p_97813_ - (double)j);
                            this.snapbackEnd = this.clickedSlot;
                            this.snapbackItem = this.draggingItem;
                            this.snapbackTime = Util.getMillis();
                        }
                    }
                    else if (!this.draggingItem.isEmpty())
                    {
                        this.snapbackStartX = Mth.floor(pMouseX - (double)i);
                        this.snapbackStartY = Mth.floor(p_97813_ - (double)j);
                        this.snapbackEnd = this.clickedSlot;
                        this.snapbackItem = this.draggingItem;
                        this.snapbackTime = Util.getMillis();
                    }

                    this.draggingItem = ItemStack.EMPTY;
                    this.clickedSlot = null;
                }
            }
            else if (this.isQuickCrafting && !this.quickCraftSlots.isEmpty())
            {
                this.slotClicked((Slot)null, -999, AbstractContainerMenu.getQuickcraftMask(0, this.quickCraftingType), ClickType.QUICK_CRAFT);

                for (Slot slot1 : this.quickCraftSlots)
                {
                    this.slotClicked(slot1, slot1.index, AbstractContainerMenu.getQuickcraftMask(1, this.quickCraftingType), ClickType.QUICK_CRAFT);
                }

                this.slotClicked((Slot)null, -999, AbstractContainerMenu.getQuickcraftMask(2, this.quickCraftingType), ClickType.QUICK_CRAFT);
            }
            else if (!this.menu.getCarried().isEmpty())
            {
                if (this.minecraft.options.keyPickItem.matchesMouse(pMouseY))
                {
                    this.slotClicked(slot, k, pMouseY, ClickType.CLONE);
                }
                else
                {
                    boolean flag1 = k != -999 && (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344));

                    if (flag1)
                    {
                        this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
                    }

                    this.slotClicked(slot, k, pMouseY, flag1 ? ClickType.QUICK_MOVE : ClickType.PICKUP);
                }
            }
        }

        if (this.menu.getCarried().isEmpty())
        {
            this.lastClickTime = 0L;
        }

        this.isQuickCrafting = false;
        return true;
    }

    private boolean isHovering(Slot pX, double pY, double pWidth)
    {
        return this.isHovering(pX.x, pX.y, 16, 16, pY, pWidth);
    }

    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double p_97773_)
    {
        int i = this.leftPos;
        int j = this.topPos;
        pMouseX = pMouseX - (double)i;
        p_97773_ = p_97773_ - (double)j;
        return pMouseX >= (double)(pX - 1) && pMouseX < (double)(pX + pWidth + 1) && p_97773_ >= (double)(pY - 1) && p_97773_ < (double)(pY + pHeight + 1);
    }

    protected void slotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType)
    {
        if (pSlot != null)
        {
            pSlotId = pSlot.index;
        }

        this.minecraft.gameMode.handleInventoryMouseClick(this.menu.containerId, pSlotId, pMouseButton, pType, this.minecraft.player);
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        if (super.keyPressed(pKeyCode, pScanCode, pModifiers))
        {
            return true;
        }
        else if (this.minecraft.options.keyInventory.matches(pKeyCode, pScanCode))
        {
            this.onClose();
            return true;
        }
        else
        {
            this.checkHotbarKeyPressed(pKeyCode, pScanCode);

            if (this.hoveredSlot != null && this.hoveredSlot.hasItem())
            {
                if (this.minecraft.options.keyPickItem.matches(pKeyCode, pScanCode))
                {
                    this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 0, ClickType.CLONE);
                }
                else if (this.minecraft.options.keyDrop.matches(pKeyCode, pScanCode))
                {
                    this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, hasControlDown() ? 1 : 0, ClickType.THROW);
                }
            }

            return true;
        }
    }

    protected boolean checkHotbarKeyPressed(int pKeyCode, int pScanCode)
    {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null)
        {
            if (this.minecraft.options.keySwapOffhand.matches(pKeyCode, pScanCode))
            {
                this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 40, ClickType.SWAP);
                return true;
            }

            for (int i = 0; i < 9; ++i)
            {
                if (this.minecraft.options.keyHotbarSlots[i].matches(pKeyCode, pScanCode))
                {
                    this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, i, ClickType.SWAP);
                    return true;
                }
            }
        }

        return false;
    }

    public void removed()
    {
        if (this.minecraft.player != null)
        {
            this.menu.removed(this.minecraft.player);
        }
    }

    public boolean isPauseScreen()
    {
        return false;
    }

    public final void tick()
    {
        super.tick();

        if (this.minecraft.player.isAlive() && !this.minecraft.player.isRemoved())
        {
            this.m_181908_();
        }
        else
        {
            this.minecraft.player.closeContainer();
        }
    }

    protected void m_181908_()
    {
    }

    public T getMenu()
    {
        return this.menu;
    }

    public void onClose()
    {
        this.minecraft.player.closeContainer();
        super.onClose();
    }
}
