package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AnvilScreen extends ItemCombinerScreen<AnvilMenu>
{
    private static final ResourceLocation ANVIL_LOCATION = new ResourceLocation("textures/gui/container/anvil.png");
    private static final Component TOO_EXPENSIVE_TEXT = new TranslatableComponent("container.repair.expensive");
    private EditBox name;
    private final Player player;

    public AnvilScreen(AnvilMenu p_97874_, Inventory p_97875_, Component p_97876_)
    {
        super(p_97874_, p_97875_, p_97876_, ANVIL_LOCATION);
        this.player = p_97875_.player;
        this.titleLabelX = 60;
    }

    public void m_181908_()
    {
        super.m_181908_();
        this.name.tick();
    }

    protected void subInit()
    {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.name = new EditBox(this.font, i + 62, j + 24, 103, 12, new TranslatableComponent("container.repair"));
        this.name.setCanLoseFocus(false);
        this.name.setTextColor(-1);
        this.name.setTextColorUneditable(-1);
        this.name.setBordered(false);
        this.name.setMaxLength(50);
        this.name.setResponder(this::onNameChanged);
        this.name.setValue("");
        this.addWidget(this.name);
        this.setInitialFocus(this.name);
        this.name.setEditable(false);
    }

    public void resize(Minecraft pMinecraft, int pWidth, int pHeight)
    {
        String s = this.name.getValue();
        this.init(pMinecraft, pWidth, pHeight);
        this.name.setValue(s);
    }

    public void removed()
    {
        super.removed();
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        if (pKeyCode == 256)
        {
            this.minecraft.player.closeContainer();
        }

        return !this.name.keyPressed(pKeyCode, pScanCode, pModifiers) && !this.name.canConsumeInput() ? super.keyPressed(pKeyCode, pScanCode, pModifiers) : true;
    }

    private void onNameChanged(String pName)
    {
        if (!pName.isEmpty())
        {
            String s = pName;
            Slot slot = this.menu.getSlot(0);

            if (slot != null && slot.hasItem() && !slot.getItem().hasCustomHoverName() && pName.equals(slot.getItem().getHoverName().getString()))
            {
                s = "";
            }

            this.menu.setItemName(s);
            this.minecraft.player.connection.send(new ServerboundRenameItemPacket(s));
        }
    }

    protected void renderLabels(PoseStack pMatrixStack, int pX, int pY)
    {
        RenderSystem.disableBlend();
        super.renderLabels(pMatrixStack, pX, pY);
        int i = this.menu.getCost();

        if (i > 0)
        {
            int j = 8453920;
            Component component;

            if (i >= 40 && !this.minecraft.player.getAbilities().instabuild)
            {
                component = TOO_EXPENSIVE_TEXT;
                j = 16736352;
            }
            else if (!this.menu.getSlot(2).hasItem())
            {
                component = null;
            }
            else
            {
                component = new TranslatableComponent("container.repair.cost", i);

                if (!this.menu.getSlot(2).mayPickup(this.player))
                {
                    j = 16736352;
                }
            }

            if (component != null)
            {
                int k = this.imageWidth - 8 - this.font.width(component) - 2;
                int l = 69;
                fill(pMatrixStack, k - 2, 67, this.imageWidth - 8, 79, 1325400064);
                this.font.drawShadow(pMatrixStack, component, (float)k, 69.0F, j);
            }
        }
    }

    public void renderFg(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.name.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }

    public void slotChanged(AbstractContainerMenu pContainerToSend, int pSlotInd, ItemStack pStack)
    {
        if (pSlotInd == 0)
        {
            this.name.setValue(pStack.isEmpty() ? "" : pStack.getHoverName().getString());
            this.name.setEditable(!pStack.isEmpty());
            this.setFocused(this.name);
        }
    }
}
