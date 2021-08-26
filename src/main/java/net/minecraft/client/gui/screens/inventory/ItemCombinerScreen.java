package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;

public class ItemCombinerScreen<T extends ItemCombinerMenu> extends AbstractContainerScreen<T> implements ContainerListener
{
    private final ResourceLocation menuResource;

    public ItemCombinerScreen(T p_98901_, Inventory p_98902_, Component p_98903_, ResourceLocation p_98904_)
    {
        super(p_98901_, p_98902_, p_98903_);
        this.menuResource = p_98904_;
    }

    protected void subInit()
    {
    }

    protected void init()
    {
        super.init();
        this.subInit();
        this.menu.addSlotListener(this);
    }

    public void removed()
    {
        super.removed();
        this.menu.removeSlotListener(this);
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        RenderSystem.disableBlend();
        this.renderFg(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        this.renderTooltip(pMatrixStack, pMouseX, pMouseY);
    }

    protected void renderFg(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
    }

    protected void renderBg(PoseStack pMatrixStack, float pPartialTicks, int pX, int pY)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.menuResource);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(pMatrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
        this.blit(pMatrixStack, i + 59, j + 20, 0, this.imageHeight + (this.menu.getSlot(0).hasItem() ? 0 : 16), 110, 16);

        if ((this.menu.getSlot(0).hasItem() || this.menu.getSlot(1).hasItem()) && !this.menu.getSlot(2).hasItem())
        {
            this.blit(pMatrixStack, i + 99, j + 45, this.imageWidth, 0, 28, 21);
        }
    }

    public void dataChanged(AbstractContainerMenu p_169759_, int p_169760_, int p_169761_)
    {
    }

    public void slotChanged(AbstractContainerMenu pContainerToSend, int pSlotInd, ItemStack pStack)
    {
    }
}
