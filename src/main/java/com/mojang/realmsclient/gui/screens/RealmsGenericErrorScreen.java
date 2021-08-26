package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.exception.RealmsServiceException;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsScreen;

public class RealmsGenericErrorScreen extends RealmsScreen
{
    private final Screen nextScreen;
    private Component line1;
    private Component line2;

    public RealmsGenericErrorScreen(RealmsServiceException p_88669_, Screen p_88670_)
    {
        super(NarratorChatListener.NO_TITLE);
        this.nextScreen = p_88670_;
        this.errorMessage(p_88669_);
    }

    public RealmsGenericErrorScreen(Component p_88672_, Screen p_88673_)
    {
        super(NarratorChatListener.NO_TITLE);
        this.nextScreen = p_88673_;
        this.errorMessage(p_88672_);
    }

    public RealmsGenericErrorScreen(Component p_88675_, Component p_88676_, Screen p_88677_)
    {
        super(NarratorChatListener.NO_TITLE);
        this.nextScreen = p_88677_;
        this.errorMessage(p_88675_, p_88676_);
    }

    private void errorMessage(RealmsServiceException p_88684_)
    {
        if (p_88684_.errorCode == -1)
        {
            this.line1 = new TextComponent("An error occurred (" + p_88684_.httpResultCode + "):");
            this.line2 = new TextComponent(p_88684_.httpResponseContent);
        }
        else
        {
            this.line1 = new TextComponent("Realms (" + p_88684_.errorCode + "):");
            String s = "mco.errorMessage." + p_88684_.errorCode;
            this.line2 = (Component)(I18n.exists(s) ? new TranslatableComponent(s) : Component.nullToEmpty(p_88684_.errorMsg));
        }
    }

    private void errorMessage(Component p_88688_)
    {
        this.line1 = new TextComponent("An error occurred: ");
        this.line2 = p_88688_;
    }

    private void errorMessage(Component p_88690_, Component p_88691_)
    {
        this.line1 = p_88690_;
        this.line2 = p_88691_;
    }

    public void init()
    {
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 52, 200, 20, new TextComponent("Ok"), (p_88686_) ->
        {
            this.minecraft.setScreen(this.nextScreen);
        }));
    }

    public Component getNarrationMessage()
    {
        return (new TextComponent("")).append(this.line1).append(": ").append(this.line2);
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        drawCenteredString(pMatrixStack, this.font, this.line1, this.width / 2, 80, 16777215);
        drawCenteredString(pMatrixStack, this.font, this.line2, this.width / 2, 100, 16711680);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }
}
