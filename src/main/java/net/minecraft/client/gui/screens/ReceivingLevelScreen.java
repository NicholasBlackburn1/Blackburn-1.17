package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.optifine.CustomLoadingScreen;
import net.optifine.CustomLoadingScreens;

public class ReceivingLevelScreen extends Screen
{
    private static final Component DOWNLOADING_TERRAIN_TEXT = new TranslatableComponent("multiplayer.downloadingTerrain");
    private CustomLoadingScreen customLoadingScreen = CustomLoadingScreens.getCustomLoadingScreen();

    public ReceivingLevelScreen()
    {
        super(NarratorChatListener.NO_TITLE);
    }

    public boolean shouldCloseOnEsc()
    {
        return false;
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        if (this.customLoadingScreen != null)
        {
            this.customLoadingScreen.drawBackground(this.width, this.height);
        }
        else
        {
            this.renderDirtBackground(0);
        }

        drawCenteredString(pMatrixStack, this.font, DOWNLOADING_TERRAIN_TEXT, this.width / 2, this.height / 2 - 50, 16777215);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }

    public boolean isPauseScreen()
    {
        return false;
    }
}
