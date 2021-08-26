package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.AbstractFurnaceRecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

public abstract class AbstractFurnaceScreen<T extends AbstractFurnaceMenu> extends AbstractContainerScreen<T> implements RecipeUpdateListener
{
    private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");
    public final AbstractFurnaceRecipeBookComponent recipeBookComponent;
    private boolean widthTooNarrow;
    private final ResourceLocation texture;

    public AbstractFurnaceScreen(T p_97825_, AbstractFurnaceRecipeBookComponent p_97826_, Inventory p_97827_, Component p_97828_, ResourceLocation p_97829_)
    {
        super(p_97825_, p_97827_, p_97828_);
        this.recipeBookComponent = p_97826_;
        this.texture = p_97829_;
    }

    public void init()
    {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        this.addRenderableWidget(new ImageButton(this.leftPos + 20, this.height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, (p_97863_) ->
        {
            this.recipeBookComponent.toggleVisibility();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            ((ImageButton)p_97863_).setPosition(this.leftPos + 20, this.height / 2 - 49);
        }));
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
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
        RenderSystem.setShaderTexture(0, this.texture);
        int i = this.leftPos;
        int j = this.topPos;
        this.blit(pMatrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);

        if (this.menu.isLit())
        {
            int k = this.menu.getLitProgress();
            this.blit(pMatrixStack, i + 56, j + 36 + 12 - k, 176, 12 - k, 14, k + 1);
        }

        int l = this.menu.getBurnProgress();
        this.blit(pMatrixStack, i + 79, j + 34, 176, 14, l + 1, 16);
    }

    public boolean mouseClicked(double pMouseX, double p_97835_, int pMouseY)
    {
        if (this.recipeBookComponent.mouseClicked(pMouseX, p_97835_, pMouseY))
        {
            return true;
        }
        else
        {
            return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? true : super.mouseClicked(pMouseX, p_97835_, pMouseY);
        }
    }

    protected void slotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType)
    {
        super.slotClicked(pSlot, pSlotId, pMouseButton, pType);
        this.recipeBookComponent.slotClicked(pSlot);
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        return this.recipeBookComponent.keyPressed(pKeyCode, pScanCode, pModifiers) ? false : super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    protected boolean hasClickedOutside(double pMouseX, double p_97839_, int pMouseY, int p_97841_, int pGuiLeft)
    {
        boolean flag = pMouseX < (double)pMouseY || p_97839_ < (double)p_97841_ || pMouseX >= (double)(pMouseY + this.imageWidth) || p_97839_ >= (double)(p_97841_ + this.imageHeight);
        return this.recipeBookComponent.hasClickedOutside(pMouseX, p_97839_, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, pGuiLeft) && flag;
    }

    public boolean charTyped(char pCodePoint, int pModifiers)
    {
        return this.recipeBookComponent.charTyped(pCodePoint, pModifiers) ? true : super.charTyped(pCodePoint, pModifiers);
    }

    public void recipesUpdated()
    {
        this.recipeBookComponent.recipesUpdated();
    }

    public RecipeBookComponent getRecipeBookComponent()
    {
        return this.recipeBookComponent;
    }

    public void removed()
    {
        this.recipeBookComponent.removed();
        super.removed();
    }
}
