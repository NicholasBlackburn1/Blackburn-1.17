package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;

public class RealmsConfirmScreen extends RealmsScreen
{
    protected BooleanConsumer callback;
    private final Component title1;
    private final Component title2;

    public RealmsConfirmScreen(BooleanConsumer p_88550_, Component p_88551_, Component p_88552_)
    {
        super(NarratorChatListener.NO_TITLE);
        this.callback = p_88550_;
        this.title1 = p_88551_;
        this.title2 = p_88552_;
    }

    public void init()
    {
        this.addRenderableWidget(new Button(this.width / 2 - 105, row(9), 100, 20, CommonComponents.GUI_YES, (p_88562_) ->
        {
            this.callback.accept(true);
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 5, row(9), 100, 20, CommonComponents.GUI_NO, (p_88559_) ->
        {
            this.callback.accept(false);
        }));
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        drawCenteredString(pMatrixStack, this.font, this.title1, this.width / 2, row(3), 16777215);
        drawCenteredString(pMatrixStack, this.font, this.title2, this.width / 2, row(5), 16777215);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }
}
