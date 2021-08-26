package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HorseInventoryMenu;

public class HorseInventoryScreen extends AbstractContainerScreen<HorseInventoryMenu>
{
    private static final ResourceLocation HORSE_INVENTORY_LOCATION = new ResourceLocation("textures/gui/container/horse.png");
    private final AbstractHorse horse;
    private float xMouse;
    private float yMouse;

    public HorseInventoryScreen(HorseInventoryMenu p_98817_, Inventory p_98818_, AbstractHorse p_98819_)
    {
        super(p_98817_, p_98818_, p_98819_.getDisplayName());
        this.horse = p_98819_;
        this.passEvents = false;
    }

    protected void renderBg(PoseStack pMatrixStack, float pPartialTicks, int pX, int pY)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, HORSE_INVENTORY_LOCATION);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(pMatrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);

        if (this.horse instanceof AbstractChestedHorse)
        {
            AbstractChestedHorse abstractchestedhorse = (AbstractChestedHorse)this.horse;

            if (abstractchestedhorse.hasChest())
            {
                this.blit(pMatrixStack, i + 79, j + 17, 0, this.imageHeight, abstractchestedhorse.getInventoryColumns() * 18, 54);
            }
        }

        if (this.horse.isSaddleable())
        {
            this.blit(pMatrixStack, i + 7, j + 35 - 18, 18, this.imageHeight + 54, 18, 18);
        }

        if (this.horse.canWearArmor())
        {
            if (this.horse instanceof Llama)
            {
                this.blit(pMatrixStack, i + 7, j + 35, 36, this.imageHeight + 54, 18, 18);
            }
            else
            {
                this.blit(pMatrixStack, i + 7, j + 35, 0, this.imageHeight + 54, 18, 18);
            }
        }

        InventoryScreen.renderEntityInInventory(i + 51, j + 60, 17, (float)(i + 51) - this.xMouse, (float)(j + 75 - 50) - this.yMouse, this.horse);
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        this.xMouse = (float)pMouseX;
        this.yMouse = (float)pMouseY;
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        this.renderTooltip(pMatrixStack, pMouseX, pMouseY);
    }
}
