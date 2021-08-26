package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.SmithingMenu;

public class SmithingScreen extends ItemCombinerScreen<SmithingMenu>
{
    private static final ResourceLocation SMITHING_LOCATION = new ResourceLocation("textures/gui/container/smithing.png");

    public SmithingScreen(SmithingMenu p_99290_, Inventory p_99291_, Component p_99292_)
    {
        super(p_99290_, p_99291_, p_99292_, SMITHING_LOCATION);
        this.titleLabelX = 60;
        this.titleLabelY = 18;
    }

    protected void renderLabels(PoseStack pMatrixStack, int pX, int pY)
    {
        RenderSystem.disableBlend();
        super.renderLabels(pMatrixStack, pX, pY);
    }
}
