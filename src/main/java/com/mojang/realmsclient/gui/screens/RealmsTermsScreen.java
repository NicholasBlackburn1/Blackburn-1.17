package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.task.GetServerDetailsTask;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsTermsScreen extends RealmsScreen
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Component TITLE = new TranslatableComponent("mco.terms.title");
    private static final Component TERMS_STATIC_TEXT = new TranslatableComponent("mco.terms.sentence.1");
    private static final Component TERMS_LINK_TEXT = (new TextComponent(" ")).append((new TranslatableComponent("mco.terms.sentence.2")).withStyle(Style.EMPTY.withUnderlined(true)));
    private final Screen lastScreen;
    private final RealmsMainScreen mainScreen;
    private final RealmsServer realmsServer;
    private boolean onLink;
    private final String realmsToSUrl = "https://aka.ms/MinecraftRealmsTerms";

    public RealmsTermsScreen(Screen p_90033_, RealmsMainScreen p_90034_, RealmsServer p_90035_)
    {
        super(TITLE);
        this.lastScreen = p_90033_;
        this.mainScreen = p_90034_;
        this.realmsServer = p_90035_;
    }

    public void init()
    {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        int i = this.width / 4 - 2;
        this.addRenderableWidget(new Button(this.width / 4, row(12), i, 20, new TranslatableComponent("mco.terms.buttons.agree"), (p_90054_) ->
        {
            this.agreedToTos();
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 4, row(12), i, 20, new TranslatableComponent("mco.terms.buttons.disagree"), (p_90050_) ->
        {
            this.minecraft.setScreen(this.lastScreen);
        }));
    }

    public void removed()
    {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        if (pKeyCode == 256)
        {
            this.minecraft.setScreen(this.lastScreen);
            return true;
        }
        else
        {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }

    private void agreedToTos()
    {
        RealmsClient realmsclient = RealmsClient.create();

        try
        {
            realmsclient.agreeToTos();
            this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new GetServerDetailsTask(this.mainScreen, this.lastScreen, this.realmsServer, new ReentrantLock())));
        }
        catch (RealmsServiceException realmsserviceexception)
        {
            LOGGER.error("Couldn't agree to TOS");
        }
    }

    public boolean mouseClicked(double pMouseX, double p_90038_, int pMouseY)
    {
        if (this.onLink)
        {
            this.minecraft.keyboardHandler.setClipboard("https://aka.ms/MinecraftRealmsTerms");
            Util.getPlatform().openUri("https://aka.ms/MinecraftRealmsTerms");
            return true;
        }
        else
        {
            return super.mouseClicked(pMouseX, p_90038_, pMouseY);
        }
    }

    public Component getNarrationMessage()
    {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), TERMS_STATIC_TEXT).append(" ").append(TERMS_LINK_TEXT);
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, 17, 16777215);
        this.font.draw(pMatrixStack, TERMS_STATIC_TEXT, (float)(this.width / 2 - 120), (float)row(5), 16777215);
        int i = this.font.width(TERMS_STATIC_TEXT);
        int j = this.width / 2 - 121 + i;
        int k = row(5);
        int l = j + this.font.width(TERMS_LINK_TEXT) + 1;
        int i1 = k + 1 + 9;
        this.onLink = j <= pMouseX && pMouseX <= l && k <= pMouseY && pMouseY <= i1;
        this.font.draw(pMatrixStack, TERMS_LINK_TEXT, (float)(this.width / 2 - 120 + i), (float)row(5), this.onLink ? 7107012 : 3368635);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }
}
