package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsScreen;

public class RealmsLongConfirmationScreen extends RealmsScreen
{
    private final RealmsLongConfirmationScreen.Type type;
    private final Component line2;
    private final Component line3;
    protected final BooleanConsumer callback;
    private final boolean yesNoQuestion;

    public RealmsLongConfirmationScreen(BooleanConsumer p_88731_, RealmsLongConfirmationScreen.Type p_88732_, Component p_88733_, Component p_88734_, boolean p_88735_)
    {
        super(NarratorChatListener.NO_TITLE);
        this.callback = p_88731_;
        this.type = p_88732_;
        this.line2 = p_88733_;
        this.line3 = p_88734_;
        this.yesNoQuestion = p_88735_;
    }

    public void init()
    {
        if (this.yesNoQuestion)
        {
            this.addRenderableWidget(new Button(this.width / 2 - 105, row(8), 100, 20, CommonComponents.GUI_YES, (p_88751_) ->
            {
                this.callback.accept(true);
            }));
            this.addRenderableWidget(new Button(this.width / 2 + 5, row(8), 100, 20, CommonComponents.GUI_NO, (p_88749_) ->
            {
                this.callback.accept(false);
            }));
        }
        else
        {
            this.addRenderableWidget(new Button(this.width / 2 - 50, row(8), 100, 20, new TranslatableComponent("mco.gui.ok"), (p_88746_) ->
            {
                this.callback.accept(true);
            }));
        }
    }

    public Component getNarrationMessage()
    {
        return CommonComponents.m_178396_(this.type.text, this.line2, this.line3);
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        if (pKeyCode == 256)
        {
            this.callback.accept(false);
            return true;
        }
        else
        {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        drawCenteredString(pMatrixStack, this.font, this.type.text, this.width / 2, row(2), this.type.colorCode);
        drawCenteredString(pMatrixStack, this.font, this.line2, this.width / 2, row(4), 16777215);
        drawCenteredString(pMatrixStack, this.font, this.line3, this.width / 2, row(6), 16777215);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }

    public static enum Type
    {
        Warning("Warning!", 16711680),
        Info("Info!", 8226750);

        public final int colorCode;
        public final Component text;

        private Type(String p_88761_, int p_88762_)
        {
            this.text = new TextComponent(p_88761_);
            this.colorCode = p_88762_;
        }
    }
}
