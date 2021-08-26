package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.HotbarManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class CreativeModeInventoryScreen extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu>
{
    private static final ResourceLocation CREATIVE_TABS_LOCATION = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
    private static final String GUI_CREATIVE_TAB_PREFIX = "textures/gui/container/creative_inventory/tab_";
    private static final String CUSTOM_SLOT_LOCK = "CustomCreativeLock";
    private static final int NUM_ROWS = 5;
    private static final int NUM_COLS = 9;
    private static final int TAB_WIDTH = 28;
    private static final int TAB_HEIGHT = 32;
    private static final int SCROLLER_WIDTH = 12;
    private static final int SCROLLER_HEIGHT = 15;
    static final SimpleContainer CONTAINER = new SimpleContainer(45);
    private static final Component TRASH_SLOT_TOOLTIP = new TranslatableComponent("inventory.binSlot");
    private static final int TEXT_COLOR = 16777215;
    private static int selectedTab = CreativeModeTab.TAB_BUILDING_BLOCKS.getId();
    private float scrollOffs;
    private boolean scrolling;
    private EditBox searchBox;
    @Nullable
    private List<Slot> originalSlots;
    @Nullable
    private Slot destroyItemSlot;
    private CreativeInventoryListener listener;
    private boolean ignoreTextInput;
    private boolean hasClickedOutside;
    private final Map<ResourceLocation, Tag<Item>> visibleTags = Maps.newTreeMap();

    public CreativeModeInventoryScreen(Player p_98519_)
    {
        super(new CreativeModeInventoryScreen.ItemPickerMenu(p_98519_), p_98519_.getInventory(), TextComponent.EMPTY);
        p_98519_.containerMenu = this.menu;
        this.passEvents = true;
        this.imageHeight = 136;
        this.imageWidth = 195;
    }

    public void m_181908_()
    {
        super.m_181908_();

        if (!this.minecraft.gameMode.hasInfiniteItems())
        {
            this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
        }
        else if (this.searchBox != null)
        {
            this.searchBox.tick();
        }
    }

    protected void slotClicked(@Nullable Slot pSlot, int pSlotId, int pMouseButton, ClickType pType)
    {
        if (this.isCreativeSlot(pSlot))
        {
            this.searchBox.moveCursorToEnd();
            this.searchBox.setHighlightPos(0);
        }

        boolean flag = pType == ClickType.QUICK_MOVE;
        pType = pSlotId == -999 && pType == ClickType.PICKUP ? ClickType.THROW : pType;

        if (pSlot == null && selectedTab != CreativeModeTab.TAB_INVENTORY.getId() && pType != ClickType.QUICK_CRAFT)
        {
            if (!this.menu.getCarried().isEmpty() && this.hasClickedOutside)
            {
                if (pMouseButton == 0)
                {
                    this.minecraft.player.drop(this.menu.getCarried(), true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop(this.menu.getCarried());
                    this.menu.setCarried(ItemStack.EMPTY);
                }

                if (pMouseButton == 1)
                {
                    ItemStack itemstack5 = this.menu.getCarried().split(1);
                    this.minecraft.player.drop(itemstack5, true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop(itemstack5);
                }
            }
        }
        else
        {
            if (pSlot != null && !pSlot.mayPickup(this.minecraft.player))
            {
                return;
            }

            if (pSlot == this.destroyItemSlot && flag)
            {
                for (int j = 0; j < this.minecraft.player.inventoryMenu.getItems().size(); ++j)
                {
                    this.minecraft.gameMode.handleCreativeModeItemAdd(ItemStack.EMPTY, j);
                }
            }
            else if (selectedTab == CreativeModeTab.TAB_INVENTORY.getId())
            {
                if (pSlot == this.destroyItemSlot)
                {
                    this.menu.setCarried(ItemStack.EMPTY);
                }
                else if (pType == ClickType.THROW && pSlot != null && pSlot.hasItem())
                {
                    ItemStack itemstack = pSlot.remove(pMouseButton == 0 ? 1 : pSlot.getItem().getMaxStackSize());
                    ItemStack itemstack1 = pSlot.getItem();
                    this.minecraft.player.drop(itemstack, true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop(itemstack);
                    this.minecraft.gameMode.handleCreativeModeItemAdd(itemstack1, ((CreativeModeInventoryScreen.SlotWrapper)pSlot).target.index);
                }
                else if (pType == ClickType.THROW && !this.menu.getCarried().isEmpty())
                {
                    this.minecraft.player.drop(this.menu.getCarried(), true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop(this.menu.getCarried());
                    this.menu.setCarried(ItemStack.EMPTY);
                }
                else
                {
                    this.minecraft.player.inventoryMenu.clicked(pSlot == null ? pSlotId : ((CreativeModeInventoryScreen.SlotWrapper)pSlot).target.index, pMouseButton, pType, this.minecraft.player);
                    this.minecraft.player.inventoryMenu.broadcastChanges();
                }
            }
            else if (pType != ClickType.QUICK_CRAFT && pSlot.container == CONTAINER)
            {
                ItemStack itemstack4 = this.menu.getCarried();
                ItemStack itemstack7 = pSlot.getItem();

                if (pType == ClickType.SWAP)
                {
                    if (!itemstack7.isEmpty())
                    {
                        ItemStack itemstack10 = itemstack7.copy();
                        itemstack10.setCount(itemstack10.getMaxStackSize());
                        this.minecraft.player.getInventory().setItem(pMouseButton, itemstack10);
                        this.minecraft.player.inventoryMenu.broadcastChanges();
                    }

                    return;
                }

                if (pType == ClickType.CLONE)
                {
                    if (this.menu.getCarried().isEmpty() && pSlot.hasItem())
                    {
                        ItemStack itemstack9 = pSlot.getItem().copy();
                        itemstack9.setCount(itemstack9.getMaxStackSize());
                        this.menu.setCarried(itemstack9);
                    }

                    return;
                }

                if (pType == ClickType.THROW)
                {
                    if (!itemstack7.isEmpty())
                    {
                        ItemStack itemstack8 = itemstack7.copy();
                        itemstack8.setCount(pMouseButton == 0 ? 1 : itemstack8.getMaxStackSize());
                        this.minecraft.player.drop(itemstack8, true);
                        this.minecraft.gameMode.handleCreativeModeItemDrop(itemstack8);
                    }

                    return;
                }

                if (!itemstack4.isEmpty() && !itemstack7.isEmpty() && itemstack4.sameItem(itemstack7) && ItemStack.tagMatches(itemstack4, itemstack7))
                {
                    if (pMouseButton == 0)
                    {
                        if (flag)
                        {
                            itemstack4.setCount(itemstack4.getMaxStackSize());
                        }
                        else if (itemstack4.getCount() < itemstack4.getMaxStackSize())
                        {
                            itemstack4.grow(1);
                        }
                    }
                    else
                    {
                        itemstack4.shrink(1);
                    }
                }
                else if (!itemstack7.isEmpty() && itemstack4.isEmpty())
                {
                    this.menu.setCarried(itemstack7.copy());
                    itemstack4 = this.menu.getCarried();

                    if (flag)
                    {
                        itemstack4.setCount(itemstack4.getMaxStackSize());
                    }
                }
                else if (pMouseButton == 0)
                {
                    this.menu.setCarried(ItemStack.EMPTY);
                }
                else
                {
                    this.menu.getCarried().shrink(1);
                }
            }
            else if (this.menu != null)
            {
                ItemStack itemstack3 = pSlot == null ? ItemStack.EMPTY : this.menu.getSlot(pSlot.index).getItem();
                this.menu.clicked(pSlot == null ? pSlotId : pSlot.index, pMouseButton, pType, this.minecraft.player);

                if (AbstractContainerMenu.getQuickcraftHeader(pMouseButton) == 2)
                {
                    for (int k = 0; k < 9; ++k)
                    {
                        this.minecraft.gameMode.handleCreativeModeItemAdd(this.menu.getSlot(45 + k).getItem(), 36 + k);
                    }
                }
                else if (pSlot != null)
                {
                    ItemStack itemstack6 = this.menu.getSlot(pSlot.index).getItem();
                    this.minecraft.gameMode.handleCreativeModeItemAdd(itemstack6, pSlot.index - (this.menu).slots.size() + 9 + 36);
                    int i = 45 + pMouseButton;

                    if (pType == ClickType.SWAP)
                    {
                        this.minecraft.gameMode.handleCreativeModeItemAdd(itemstack3, i - (this.menu).slots.size() + 9 + 36);
                    }
                    else if (pType == ClickType.THROW && !itemstack3.isEmpty())
                    {
                        ItemStack itemstack2 = itemstack3.copy();
                        itemstack2.setCount(pMouseButton == 0 ? 1 : itemstack2.getMaxStackSize());
                        this.minecraft.player.drop(itemstack2, true);
                        this.minecraft.gameMode.handleCreativeModeItemDrop(itemstack2);
                    }

                    this.minecraft.player.inventoryMenu.broadcastChanges();
                }
            }
        }
    }

    private boolean isCreativeSlot(@Nullable Slot pSlot)
    {
        return pSlot != null && pSlot.container == CONTAINER;
    }

    protected void checkEffectRendering()
    {
        int i = this.leftPos;
        super.checkEffectRendering();

        if (this.searchBox != null && this.leftPos != i)
        {
            this.searchBox.setX(this.leftPos + 82);
        }
    }

    protected void init()
    {
        if (this.minecraft.gameMode.hasInfiniteItems())
        {
            super.init();
            this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
            this.searchBox = new EditBox(this.font, this.leftPos + 82, this.topPos + 6, 80, 9, new TranslatableComponent("itemGroup.search"));
            this.searchBox.setMaxLength(50);
            this.searchBox.setBordered(false);
            this.searchBox.setVisible(false);
            this.searchBox.setTextColor(16777215);
            this.addWidget(this.searchBox);
            int i = selectedTab;
            selectedTab = -1;
            this.selectTab(CreativeModeTab.TABS[i]);
            this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
            this.listener = new CreativeInventoryListener(this.minecraft);
            this.minecraft.player.inventoryMenu.addSlotListener(this.listener);
        }
        else
        {
            this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
        }
    }

    public void resize(Minecraft pMinecraft, int pWidth, int pHeight)
    {
        String s = this.searchBox.getValue();
        this.init(pMinecraft, pWidth, pHeight);
        this.searchBox.setValue(s);

        if (!this.searchBox.getValue().isEmpty())
        {
            this.refreshSearchResults();
        }
    }

    public void removed()
    {
        super.removed();

        if (this.minecraft.player != null && this.minecraft.player.getInventory() != null)
        {
            this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
        }

        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    public boolean charTyped(char pCodePoint, int pModifiers)
    {
        if (this.ignoreTextInput)
        {
            return false;
        }
        else if (selectedTab != CreativeModeTab.TAB_SEARCH.getId())
        {
            return false;
        }
        else
        {
            String s = this.searchBox.getValue();

            if (this.searchBox.charTyped(pCodePoint, pModifiers))
            {
                if (!Objects.equals(s, this.searchBox.getValue()))
                {
                    this.refreshSearchResults();
                }

                return true;
            }
            else
            {
                return false;
            }
        }
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        this.ignoreTextInput = false;

        if (selectedTab != CreativeModeTab.TAB_SEARCH.getId())
        {
            if (this.minecraft.options.keyChat.matches(pKeyCode, pScanCode))
            {
                this.ignoreTextInput = true;
                this.selectTab(CreativeModeTab.TAB_SEARCH);
                return true;
            }
            else
            {
                return super.keyPressed(pKeyCode, pScanCode, pModifiers);
            }
        }
        else
        {
            boolean flag = !this.isCreativeSlot(this.hoveredSlot) || this.hoveredSlot.hasItem();
            boolean flag1 = InputConstants.getKey(pKeyCode, pScanCode).getNumericKeyValue().isPresent();

            if (flag && flag1 && this.checkHotbarKeyPressed(pKeyCode, pScanCode))
            {
                this.ignoreTextInput = true;
                return true;
            }
            else
            {
                String s = this.searchBox.getValue();

                if (this.searchBox.keyPressed(pKeyCode, pScanCode, pModifiers))
                {
                    if (!Objects.equals(s, this.searchBox.getValue()))
                    {
                        this.refreshSearchResults();
                    }

                    return true;
                }
                else
                {
                    return this.searchBox.isFocused() && this.searchBox.isVisible() && pKeyCode != 256 ? true : super.keyPressed(pKeyCode, pScanCode, pModifiers);
                }
            }
        }
    }

    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers)
    {
        this.ignoreTextInput = false;
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    private void refreshSearchResults()
    {
        (this.menu).items.clear();
        this.visibleTags.clear();
        String s = this.searchBox.getValue();

        if (s.isEmpty())
        {
            for (Item item : Registry.ITEM)
            {
                item.fillItemCategory(CreativeModeTab.TAB_SEARCH, (this.menu).items);
            }
        }
        else
        {
            SearchTree<ItemStack> searchtree;

            if (s.startsWith("#"))
            {
                s = s.substring(1);
                searchtree = this.minecraft.getSearchTree(SearchRegistry.CREATIVE_TAGS);
                this.updateVisibleTags(s);
            }
            else
            {
                searchtree = this.minecraft.getSearchTree(SearchRegistry.CREATIVE_NAMES);
            }

            (this.menu).items.addAll(searchtree.search(s.toLowerCase(Locale.ROOT)));
        }

        this.scrollOffs = 0.0F;
        this.menu.scrollTo(0.0F);
    }

    private void updateVisibleTags(String pSearch)
    {
        int i = pSearch.indexOf(58);
        Predicate<ResourceLocation> predicate;

        if (i == -1)
        {
            predicate = (p_98609_) ->
            {
                return p_98609_.getPath().contains(pSearch);
            };
        }
        else
        {
            String s = pSearch.substring(0, i).trim();
            String s1 = pSearch.substring(i + 1).trim();
            predicate = (p_98606_) ->
            {
                return p_98606_.getNamespace().contains(s) && p_98606_.getPath().contains(s1);
            };
        }

        TagCollection<Item> tagcollection = ItemTags.getAllTags();
        tagcollection.getAvailableTags().stream().filter(predicate).forEach((p_98552_) ->
        {
            this.visibleTags.put(p_98552_, tagcollection.getTag(p_98552_));
        });
    }

    protected void renderLabels(PoseStack pMatrixStack, int pX, int pY)
    {
        CreativeModeTab creativemodetab = CreativeModeTab.TABS[selectedTab];

        if (creativemodetab.showTitle())
        {
            RenderSystem.disableBlend();
            this.font.draw(pMatrixStack, creativemodetab.getDisplayName(), 8.0F, 6.0F, 4210752);
        }
    }

    public boolean mouseClicked(double pMouseX, double p_98532_, int pMouseY)
    {
        if (pMouseY == 0)
        {
            double d0 = pMouseX - (double)this.leftPos;
            double d1 = p_98532_ - (double)this.topPos;

            for (CreativeModeTab creativemodetab : CreativeModeTab.TABS)
            {
                if (this.checkTabClicked(creativemodetab, d0, d1))
                {
                    return true;
                }
            }

            if (selectedTab != CreativeModeTab.TAB_INVENTORY.getId() && this.insideScrollbar(pMouseX, p_98532_))
            {
                this.scrolling = this.canScroll();
                return true;
            }
        }

        return super.mouseClicked(pMouseX, p_98532_, pMouseY);
    }

    public boolean mouseReleased(double pMouseX, double p_98623_, int pMouseY)
    {
        if (pMouseY == 0)
        {
            double d0 = pMouseX - (double)this.leftPos;
            double d1 = p_98623_ - (double)this.topPos;
            this.scrolling = false;

            for (CreativeModeTab creativemodetab : CreativeModeTab.TABS)
            {
                if (this.checkTabClicked(creativemodetab, d0, d1))
                {
                    this.selectTab(creativemodetab);
                    return true;
                }
            }
        }

        return super.mouseReleased(pMouseX, p_98623_, pMouseY);
    }

    private boolean canScroll()
    {
        return selectedTab != CreativeModeTab.TAB_INVENTORY.getId() && CreativeModeTab.TABS[selectedTab].canScroll() && this.menu.canScroll();
    }

    private void selectTab(CreativeModeTab pTab)
    {
        int i = selectedTab;
        selectedTab = pTab.getId();
        this.quickCraftSlots.clear();
        (this.menu).items.clear();

        if (pTab == CreativeModeTab.TAB_HOTBAR)
        {
            HotbarManager hotbarmanager = this.minecraft.getHotbarManager();

            for (int j = 0; j < 9; ++j)
            {
                Hotbar hotbar = hotbarmanager.get(j);

                if (hotbar.isEmpty())
                {
                    for (int k = 0; k < 9; ++k)
                    {
                        if (k == j)
                        {
                            ItemStack itemstack = new ItemStack(Items.PAPER);
                            itemstack.getOrCreateTagElement("CustomCreativeLock");
                            Component component = this.minecraft.options.keyHotbarSlots[j].getTranslatedKeyMessage();
                            Component component1 = this.minecraft.options.keySaveHotbarActivator.getTranslatedKeyMessage();
                            itemstack.setHoverName(new TranslatableComponent("inventory.hotbarInfo", component1, component));
                            (this.menu).items.add(itemstack);
                        }
                        else
                        {
                            (this.menu).items.add(ItemStack.EMPTY);
                        }
                    }
                }
                else
                {
                    (this.menu).items.addAll(hotbar);
                }
            }
        }
        else if (pTab != CreativeModeTab.TAB_SEARCH)
        {
            pTab.fillItemList((this.menu).items);
        }

        if (pTab == CreativeModeTab.TAB_INVENTORY)
        {
            AbstractContainerMenu abstractcontainermenu = this.minecraft.player.inventoryMenu;

            if (this.originalSlots == null)
            {
                this.originalSlots = ImmutableList.copyOf((this.menu).slots);
            }

            (this.menu).slots.clear();

            for (int l = 0; l < abstractcontainermenu.slots.size(); ++l)
            {
                int i1;
                int j1;

                if (l >= 5 && l < 9)
                {
                    int l1 = l - 5;
                    int j2 = l1 / 2;
                    int l2 = l1 % 2;
                    i1 = 54 + j2 * 54;
                    j1 = 6 + l2 * 27;
                }
                else if (l >= 0 && l < 5)
                {
                    i1 = -2000;
                    j1 = -2000;
                }
                else if (l == 45)
                {
                    i1 = 35;
                    j1 = 20;
                }
                else
                {
                    int k1 = l - 9;
                    int i2 = k1 % 9;
                    int k2 = k1 / 9;
                    i1 = 9 + i2 * 18;

                    if (l >= 36)
                    {
                        j1 = 112;
                    }
                    else
                    {
                        j1 = 54 + k2 * 18;
                    }
                }

                Slot slot = new CreativeModeInventoryScreen.SlotWrapper(abstractcontainermenu.slots.get(l), l, i1, j1);
                (this.menu).slots.add(slot);
            }

            this.destroyItemSlot = new Slot(CONTAINER, 0, 173, 112);
            (this.menu).slots.add(this.destroyItemSlot);
        }
        else if (i == CreativeModeTab.TAB_INVENTORY.getId())
        {
            (this.menu).slots.clear();
            (this.menu).slots.addAll(this.originalSlots);
            this.originalSlots = null;
        }

        if (this.searchBox != null)
        {
            if (pTab == CreativeModeTab.TAB_SEARCH)
            {
                this.searchBox.setVisible(true);
                this.searchBox.setCanLoseFocus(false);
                this.searchBox.setFocus(true);

                if (i != pTab.getId())
                {
                    this.searchBox.setValue("");
                }

                this.refreshSearchResults();
            }
            else
            {
                this.searchBox.setVisible(false);
                this.searchBox.setCanLoseFocus(true);
                this.searchBox.setFocus(false);
                this.searchBox.setValue("");
            }
        }

        this.scrollOffs = 0.0F;
        this.menu.scrollTo(0.0F);
    }

    public boolean mouseScrolled(double pMouseX, double p_98528_, double pMouseY)
    {
        if (!this.canScroll())
        {
            return false;
        }
        else
        {
            int i = ((this.menu).items.size() + 9 - 1) / 9 - 5;
            this.scrollOffs = (float)((double)this.scrollOffs - pMouseY / (double)i);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.menu.scrollTo(this.scrollOffs);
            return true;
        }
    }

    protected boolean hasClickedOutside(double pMouseX, double p_98542_, int pMouseY, int p_98544_, int pGuiLeft)
    {
        boolean flag = pMouseX < (double)pMouseY || p_98542_ < (double)p_98544_ || pMouseX >= (double)(pMouseY + this.imageWidth) || p_98542_ >= (double)(p_98544_ + this.imageHeight);
        this.hasClickedOutside = flag && !this.checkTabClicked(CreativeModeTab.TABS[selectedTab], pMouseX, p_98542_);
        return this.hasClickedOutside;
    }

    protected boolean insideScrollbar(double p_98524_, double p_98525_)
    {
        int i = this.leftPos;
        int j = this.topPos;
        int k = i + 175;
        int l = j + 18;
        int i1 = k + 14;
        int j1 = l + 112;
        return p_98524_ >= (double)k && p_98525_ >= (double)l && p_98524_ < (double)i1 && p_98525_ < (double)j1;
    }

    public boolean mouseDragged(double pMouseX, double p_98536_, int pMouseY, double p_98538_, double pButton)
    {
        if (this.scrolling)
        {
            int i = this.topPos + 18;
            int j = i + 112;
            this.scrollOffs = ((float)p_98536_ - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.menu.scrollTo(this.scrollOffs);
            return true;
        }
        else
        {
            return super.mouseDragged(pMouseX, p_98536_, pMouseY, p_98538_, pButton);
        }
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);

        for (CreativeModeTab creativemodetab : CreativeModeTab.TABS)
        {
            if (this.checkTabHovering(pMatrixStack, creativemodetab, pMouseX, pMouseY))
            {
                break;
            }
        }

        if (this.destroyItemSlot != null && selectedTab == CreativeModeTab.TAB_INVENTORY.getId() && this.isHovering(this.destroyItemSlot.x, this.destroyItemSlot.y, 16, 16, (double)pMouseX, (double)pMouseY))
        {
            this.renderTooltip(pMatrixStack, TRASH_SLOT_TOOLTIP, pMouseX, pMouseY);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderTooltip(pMatrixStack, pMouseX, pMouseY);
    }

    protected void renderTooltip(PoseStack pMatrixStack, ItemStack pItemStack, int pMouseX, int pMouseY)
    {
        if (selectedTab == CreativeModeTab.TAB_SEARCH.getId())
        {
            List<Component> list = pItemStack.getTooltipLines(this.minecraft.player, this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
            List<Component> list1 = Lists.newArrayList(list);
            Item item = pItemStack.getItem();
            CreativeModeTab creativemodetab = item.getItemCategory();

            if (creativemodetab == null && pItemStack.is(Items.ENCHANTED_BOOK))
            {
                Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(pItemStack);

                if (map.size() == 1)
                {
                    Enchantment enchantment = map.keySet().iterator().next();

                    for (CreativeModeTab creativemodetab1 : CreativeModeTab.TABS)
                    {
                        if (creativemodetab1.hasEnchantmentCategory(enchantment.category))
                        {
                            creativemodetab = creativemodetab1;
                            break;
                        }
                    }
                }
            }

            this.visibleTags.forEach((p_169747_, p_169748_) ->
            {
                if (pItemStack.is(p_169748_))
                {
                    list1.add(1, (new TextComponent("#" + p_169747_)).withStyle(ChatFormatting.DARK_PURPLE));
                }
            });

            if (creativemodetab != null)
            {
                list1.add(1, creativemodetab.getDisplayName().copy().withStyle(ChatFormatting.BLUE));
            }

            this.renderTooltip(pMatrixStack, list1, pItemStack.getTooltipImage(), pMouseX, pMouseY);
        }
        else
        {
            super.renderTooltip(pMatrixStack, pItemStack, pMouseX, pMouseY);
        }
    }

    protected void renderBg(PoseStack pMatrixStack, float pPartialTicks, int pX, int pY)
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        CreativeModeTab creativemodetab = CreativeModeTab.TABS[selectedTab];

        for (CreativeModeTab creativemodetab1 : CreativeModeTab.TABS)
        {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, CREATIVE_TABS_LOCATION);

            if (creativemodetab1.getId() != selectedTab)
            {
                this.renderTabButton(pMatrixStack, creativemodetab1);
            }
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, new ResourceLocation("textures/gui/container/creative_inventory/tab_" + creativemodetab.getBackgroundSuffix()));
        this.blit(pMatrixStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        this.searchBox.render(pMatrixStack, pX, pY, pPartialTicks);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.leftPos + 175;
        int j = this.topPos + 18;
        int k = j + 112;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, CREATIVE_TABS_LOCATION);

        if (creativemodetab.canScroll())
        {
            this.blit(pMatrixStack, i, j + (int)((float)(k - j - 17) * this.scrollOffs), 232 + (this.canScroll() ? 0 : 12), 0, 12, 15);
        }

        this.renderTabButton(pMatrixStack, creativemodetab);

        if (creativemodetab == CreativeModeTab.TAB_INVENTORY)
        {
            InventoryScreen.renderEntityInInventory(this.leftPos + 88, this.topPos + 45, 20, (float)(this.leftPos + 88 - pX), (float)(this.topPos + 45 - 30 - pY), this.minecraft.player);
        }
    }

    protected boolean checkTabClicked(CreativeModeTab p_98563_, double p_98564_, double p_98565_)
    {
        int i = p_98563_.getColumn();
        int j = 28 * i;
        int k = 0;

        if (p_98563_.isAlignedRight())
        {
            j = this.imageWidth - 28 * (6 - i) + 2;
        }
        else if (i > 0)
        {
            j += i;
        }

        if (p_98563_.isTopRow())
        {
            k = k - 32;
        }
        else
        {
            k = k + this.imageHeight;
        }

        return p_98564_ >= (double)j && p_98564_ <= (double)(j + 28) && p_98565_ >= (double)k && p_98565_ <= (double)(k + 32);
    }

    protected boolean checkTabHovering(PoseStack p_98585_, CreativeModeTab p_98586_, int p_98587_, int p_98588_)
    {
        int i = p_98586_.getColumn();
        int j = 28 * i;
        int k = 0;

        if (p_98586_.isAlignedRight())
        {
            j = this.imageWidth - 28 * (6 - i) + 2;
        }
        else if (i > 0)
        {
            j += i;
        }

        if (p_98586_.isTopRow())
        {
            k = k - 32;
        }
        else
        {
            k = k + this.imageHeight;
        }

        if (this.isHovering(j + 3, k + 3, 23, 27, (double)p_98587_, (double)p_98588_))
        {
            this.renderTooltip(p_98585_, p_98586_.getDisplayName(), p_98587_, p_98588_);
            return true;
        }
        else
        {
            return false;
        }
    }

    protected void renderTabButton(PoseStack p_98582_, CreativeModeTab p_98583_)
    {
        boolean flag = p_98583_.getId() == selectedTab;
        boolean flag1 = p_98583_.isTopRow();
        int i = p_98583_.getColumn();
        int j = i * 28;
        int k = 0;
        int l = this.leftPos + 28 * i;
        int i1 = this.topPos;
        int j1 = 32;

        if (flag)
        {
            k += 32;
        }

        if (p_98583_.isAlignedRight())
        {
            l = this.leftPos + this.imageWidth - 28 * (6 - i);
        }
        else if (i > 0)
        {
            l += i;
        }

        if (flag1)
        {
            i1 = i1 - 28;
        }
        else
        {
            k += 64;
            i1 = i1 + (this.imageHeight - 4);
        }

        this.blit(p_98582_, l, i1, j, k, 28, 32);
        this.itemRenderer.blitOffset = 100.0F;
        l = l + 6;
        i1 = i1 + 8 + (flag1 ? 1 : -1);
        ItemStack itemstack = p_98583_.getIconItem();
        this.itemRenderer.renderAndDecorateItem(itemstack, l, i1);
        this.itemRenderer.renderGuiItemDecorations(this.font, itemstack, l, i1);
        this.itemRenderer.blitOffset = 0.0F;
    }

    public int getSelectedTab()
    {
        return selectedTab;
    }

    public static void handleHotbarLoadOrSave(Minecraft pClient, int pIndex, boolean pLoad, boolean pSave)
    {
        LocalPlayer localplayer = pClient.player;
        HotbarManager hotbarmanager = pClient.getHotbarManager();
        Hotbar hotbar = hotbarmanager.get(pIndex);

        if (pLoad)
        {
            for (int i = 0; i < Inventory.getSelectionSize(); ++i)
            {
                ItemStack itemstack = hotbar.get(i).copy();
                localplayer.getInventory().setItem(i, itemstack);
                pClient.gameMode.handleCreativeModeItemAdd(itemstack, 36 + i);
            }

            localplayer.inventoryMenu.broadcastChanges();
        }
        else if (pSave)
        {
            for (int j = 0; j < Inventory.getSelectionSize(); ++j)
            {
                hotbar.set(j, localplayer.getInventory().getItem(j).copy());
            }

            Component component = pClient.options.keyHotbarSlots[pIndex].getTranslatedKeyMessage();
            Component component1 = pClient.options.keyLoadHotbarActivator.getTranslatedKeyMessage();
            pClient.gui.setOverlayMessage(new TranslatableComponent("inventory.hotbarSaved", component1, component), false);
            hotbarmanager.save();
        }
    }

    static class CustomCreativeSlot extends Slot
    {
        public CustomCreativeSlot(Container p_98633_, int p_98634_, int p_98635_, int p_98636_)
        {
            super(p_98633_, p_98634_, p_98635_, p_98636_);
        }

        public boolean mayPickup(Player pPlayer)
        {
            if (super.mayPickup(pPlayer) && this.hasItem())
            {
                return this.getItem().getTagElement("CustomCreativeLock") == null;
            }
            else
            {
                return !this.hasItem();
            }
        }
    }

    public static class ItemPickerMenu extends AbstractContainerMenu
    {
        public final NonNullList<ItemStack> items = NonNullList.create();
        private final AbstractContainerMenu inventoryMenu;

        public ItemPickerMenu(Player p_98641_)
        {
            super((MenuType<?>)null, 0);
            this.inventoryMenu = p_98641_.inventoryMenu;
            Inventory inventory = p_98641_.getInventory();

            for (int i = 0; i < 5; ++i)
            {
                for (int j = 0; j < 9; ++j)
                {
                    this.addSlot(new CreativeModeInventoryScreen.CustomCreativeSlot(CreativeModeInventoryScreen.CONTAINER, i * 9 + j, 9 + j * 18, 18 + i * 18));
                }
            }

            for (int k = 0; k < 9; ++k)
            {
                this.addSlot(new Slot(inventory, k, 9 + k * 18, 112));
            }

            this.scrollTo(0.0F);
        }

        public boolean stillValid(Player pPlayer)
        {
            return true;
        }

        public void scrollTo(float pPos)
        {
            int i = (this.items.size() + 9 - 1) / 9 - 5;
            int j = (int)((double)(pPos * (float)i) + 0.5D);

            if (j < 0)
            {
                j = 0;
            }

            for (int k = 0; k < 5; ++k)
            {
                for (int l = 0; l < 9; ++l)
                {
                    int i1 = l + (k + j) * 9;

                    if (i1 >= 0 && i1 < this.items.size())
                    {
                        CreativeModeInventoryScreen.CONTAINER.setItem(l + k * 9, this.items.get(i1));
                    }
                    else
                    {
                        CreativeModeInventoryScreen.CONTAINER.setItem(l + k * 9, ItemStack.EMPTY);
                    }
                }
            }
        }

        public boolean canScroll()
        {
            return this.items.size() > 45;
        }

        public ItemStack quickMoveStack(Player pPlayer, int pIndex)
        {
            if (pIndex >= this.slots.size() - 9 && pIndex < this.slots.size())
            {
                Slot slot = this.slots.get(pIndex);

                if (slot != null && slot.hasItem())
                {
                    slot.set(ItemStack.EMPTY);
                }
            }

            return ItemStack.EMPTY;
        }

        public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot)
        {
            return pSlot.container != CreativeModeInventoryScreen.CONTAINER;
        }

        public boolean canDragTo(Slot pSlot)
        {
            return pSlot.container != CreativeModeInventoryScreen.CONTAINER;
        }

        public ItemStack getCarried()
        {
            return this.inventoryMenu.getCarried();
        }

        public void setCarried(ItemStack p_169751_)
        {
            this.inventoryMenu.setCarried(p_169751_);
        }
    }

    static class SlotWrapper extends Slot
    {
        final Slot target;

        public SlotWrapper(Slot p_98657_, int p_98658_, int p_98659_, int p_98660_)
        {
            super(p_98657_.container, p_98658_, p_98659_, p_98660_);
            this.target = p_98657_;
        }

        public void onTake(Player p_169754_, ItemStack p_169755_)
        {
            this.target.onTake(p_169754_, p_169755_);
        }

        public boolean mayPlace(ItemStack pStack)
        {
            return this.target.mayPlace(pStack);
        }

        public ItemStack getItem()
        {
            return this.target.getItem();
        }

        public boolean hasItem()
        {
            return this.target.hasItem();
        }

        public void set(ItemStack pStack)
        {
            this.target.set(pStack);
        }

        public void setChanged()
        {
            this.target.setChanged();
        }

        public int getMaxStackSize()
        {
            return this.target.getMaxStackSize();
        }

        public int getMaxStackSize(ItemStack p_98675_)
        {
            return this.target.getMaxStackSize(p_98675_);
        }

        @Nullable
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon()
        {
            return this.target.getNoItemIcon();
        }

        public ItemStack remove(int pAmount)
        {
            return this.target.remove(pAmount);
        }

        public boolean isActive()
        {
            return this.target.isActive();
        }

        public boolean mayPickup(Player pPlayer)
        {
            return this.target.mayPickup(pPlayer);
        }
    }
}
