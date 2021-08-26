package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ErrorScreen extends Screen
{
    private final Component message;

    public ErrorScreen(Component p_96049_, Component p_96050_)
    {
        super(p_96049_);
        this.message = p_96050_;
    }

    protected void init()
    {
        super.init();
        this.addRenderableWidget(new Button(this.width / 2 - 100, 140, 200, 20, CommonComponents.GUI_CANCEL, (p_96057_) ->
        {
            this.minecraft.setScreen((Screen)null);
        }));
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.fillGradient(pMatrixStack, 0, 0, this.width, this.height, -12574688, -11530224);
        drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, 90, 16777215);
        drawCenteredString(pMatrixStack, this.font, this.message, this.width / 2, 110, 16777215);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }

    public boolean shouldCloseOnEsc()
    {
        return false;
    }
}
