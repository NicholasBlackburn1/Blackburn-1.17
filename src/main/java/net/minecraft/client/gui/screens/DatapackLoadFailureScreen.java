package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.TranslatableComponent;

public class DatapackLoadFailureScreen extends Screen
{
    private MultiLineLabel message = MultiLineLabel.EMPTY;
    private final Runnable callback;

    public DatapackLoadFailureScreen(Runnable p_95894_)
    {
        super(new TranslatableComponent("datapackFailure.title"));
        this.callback = p_95894_;
    }

    protected void init()
    {
        super.init();
        this.message = MultiLineLabel.create(this.font, this.getTitle(), this.width - 50);
        this.addRenderableWidget(new Button(this.width / 2 - 155, this.height / 6 + 96, 150, 20, new TranslatableComponent("datapackFailure.safeMode"), (p_95905_) ->
        {
            this.callback.run();
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 155 + 160, this.height / 6 + 96, 150, 20, new TranslatableComponent("gui.toTitle"), (p_95901_) ->
        {
            this.minecraft.setScreen((Screen)null);
        }));
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        this.message.renderCentered(pMatrixStack, this.width / 2, 70);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }

    public boolean shouldCloseOnEsc()
    {
        return false;
    }
}
