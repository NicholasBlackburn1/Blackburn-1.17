package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class AlertScreen extends Screen
{
    private final Runnable callback;
    protected final Component text;
    private MultiLineLabel message = MultiLineLabel.EMPTY;
    protected final Component okButton;

    public AlertScreen(Runnable p_95519_, Component p_95520_, Component p_95521_)
    {
        this(p_95519_, p_95520_, p_95521_, CommonComponents.GUI_BACK);
    }

    public AlertScreen(Runnable p_95523_, Component p_95524_, Component p_95525_, Component p_95526_)
    {
        super(p_95524_);
        this.callback = p_95523_;
        this.text = p_95525_;
        this.okButton = p_95526_;
    }

    protected void init()
    {
        super.init();
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 + 168, 200, 20, this.okButton, (p_95533_) ->
        {
            this.callback.run();
        }));
        this.message = MultiLineLabel.create(this.font, this.text, this.width - 50);
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, 70, 16777215);
        this.message.renderCentered(pMatrixStack, this.width / 2, 90);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }
}
