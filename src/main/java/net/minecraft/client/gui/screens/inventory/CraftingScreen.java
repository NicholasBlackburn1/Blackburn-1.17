package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;

public class CraftingScreen extends AbstractContainerScreen<CraftingMenu> implements RecipeUpdateListener
{
    private static final ResourceLocation CRAFTING_TABLE_LOCATION = new ResourceLocation("textures/gui/container/crafting_table.png");
    private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");
    private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
    private boolean widthTooNarrow;

    public CraftingScreen(CraftingMenu p_98448_, Inventory p_98449_, Component p_98450_)
    {
        super(p_98448_, p_98449_, p_98450_);
    }

    protected void init()
    {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        this.addRenderableWidget(new ImageButton(this.leftPos + 5, this.height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, (p_98484_) ->
        {
            this.recipeBookComponent.toggleVisibility();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            ((ImageButton)p_98484_).setPosition(this.leftPos + 5, this.height / 2 - 49);
        }));
        this.addWidget(this.recipeBookComponent);
        this.setInitialFocus(this.recipeBookComponent);
        this.titleLabelX = 29;
    }

    public void m_181908_()
    {
        super.m_181908_();
        this.recipeBookComponent.tick();
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);

        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow)
        {
            this.renderBg(pMatrixStack, pPartialTicks, pMouseX, pMouseY);
            this.recipeBookComponent.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        }
        else
        {
            this.recipeBookComponent.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
            super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
            this.recipeBookComponent.renderGhostRecipe(pMatrixStack, this.leftPos, this.topPos, true, pPartialTicks);
        }

        this.renderTooltip(pMatrixStack, pMouseX, pMouseY);
        this.recipeBookComponent.renderTooltip(pMatrixStack, this.leftPos, this.topPos, pMouseX, pMouseY);
    }

    protected void renderBg(PoseStack pMatrixStack, float pPartialTicks, int pX, int pY)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CRAFTING_TABLE_LOCATION);
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(pMatrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double p_98467_)
    {
        return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(pX, pY, pWidth, pHeight, pMouseX, p_98467_);
    }

    public boolean mouseClicked(double pMouseX, double p_98453_, int pMouseY)
    {
        if (this.recipeBookComponent.mouseClicked(pMouseX, p_98453_, pMouseY))
        {
            this.setFocused(this.recipeBookComponent);
            return true;
        }
        else
        {
            return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? true : super.mouseClicked(pMouseX, p_98453_, pMouseY);
        }
    }

    protected boolean hasClickedOutside(double pMouseX, double p_98457_, int pMouseY, int p_98459_, int pGuiLeft)
    {
        boolean flag = pMouseX < (double)pMouseY || p_98457_ < (double)p_98459_ || pMouseX >= (double)(pMouseY + this.imageWidth) || p_98457_ >= (double)(p_98459_ + this.imageHeight);
        return this.recipeBookComponent.hasClickedOutside(pMouseX, p_98457_, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, pGuiLeft) && flag;
    }

    protected void slotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType)
    {
        super.slotClicked(pSlot, pSlotId, pMouseButton, pType);
        this.recipeBookComponent.slotClicked(pSlot);
    }

    public void recipesUpdated()
    {
        this.recipeBookComponent.recipesUpdated();
    }

    public void removed()
    {
        this.recipeBookComponent.removed();
        super.removed();
    }

    public RecipeBookComponent getRecipeBookComponent()
    {
        return this.recipeBookComponent;
    }
}
