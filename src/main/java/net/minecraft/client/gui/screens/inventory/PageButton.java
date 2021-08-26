package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;

public class PageButton extends Button
{
    private final boolean isForward;
    private final boolean playTurnSound;

    public PageButton(int p_99225_, int p_99226_, boolean p_99227_, Button.OnPress p_99228_, boolean p_99229_)
    {
        super(p_99225_, p_99226_, 23, 13, TextComponent.EMPTY, p_99228_);
        this.isForward = p_99227_;
        this.playTurnSound = p_99229_;
    }

    public void renderButton(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BookViewScreen.BOOK_LOCATION);
        int i = 0;
        int j = 192;

        if (this.isHovered())
        {
            i += 23;
        }

        if (!this.isForward)
        {
            j += 13;
        }

        this.blit(pMatrixStack, this.x, this.y, i, j, 23, 13);
    }

    public void playDownSound(SoundManager pHandler)
    {
        if (this.playTurnSound)
        {
            pHandler.play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }
    }
}
