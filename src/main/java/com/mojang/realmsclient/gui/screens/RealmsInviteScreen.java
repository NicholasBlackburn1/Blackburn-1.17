package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import javax.annotation.Nullable;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsInviteScreen extends RealmsScreen
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Component NAME_LABEL = new TranslatableComponent("mco.configure.world.invite.profile.name");
    private static final Component NO_SUCH_PLAYER_ERROR_TEXT = new TranslatableComponent("mco.configure.world.players.error");
    private EditBox profileName;
    private final RealmsServer serverData;
    private final RealmsConfigureWorldScreen configureScreen;
    private final Screen lastScreen;
    @Nullable
    private Component errorMsg;

    public RealmsInviteScreen(RealmsConfigureWorldScreen p_88703_, Screen p_88704_, RealmsServer p_88705_)
    {
        super(NarratorChatListener.NO_TITLE);
        this.configureScreen = p_88703_;
        this.lastScreen = p_88704_;
        this.serverData = p_88705_;
    }

    public void tick()
    {
        this.profileName.tick();
    }

    public void init()
    {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.profileName = new EditBox(this.minecraft.font, this.width / 2 - 100, row(2), 200, 20, (EditBox)null, new TranslatableComponent("mco.configure.world.invite.profile.name"));
        this.addWidget(this.profileName);
        this.setInitialFocus(this.profileName);
        this.addRenderableWidget(new Button(this.width / 2 - 100, row(10), 200, 20, new TranslatableComponent("mco.configure.world.buttons.invite"), (p_88721_) ->
        {
            this.onInvite();
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 100, row(12), 200, 20, CommonComponents.GUI_CANCEL, (p_88716_) ->
        {
            this.minecraft.setScreen(this.lastScreen);
        }));
    }

    public void removed()
    {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    private void onInvite()
    {
        RealmsClient realmsclient = RealmsClient.create();

        if (this.profileName.getValue() != null && !this.profileName.getValue().isEmpty())
        {
            try
            {
                RealmsServer realmsserver = realmsclient.invite(this.serverData.id, this.profileName.getValue().trim());

                if (realmsserver != null)
                {
                    this.serverData.players = realmsserver.players;
                    this.minecraft.setScreen(new RealmsPlayerScreen(this.configureScreen, this.serverData));
                }
                else
                {
                    this.showError(NO_SUCH_PLAYER_ERROR_TEXT);
                }
            }
            catch (Exception exception)
            {
                LOGGER.error("Couldn't invite user");
                this.showError(NO_SUCH_PLAYER_ERROR_TEXT);
            }
        }
        else
        {
            this.showError(NO_SUCH_PLAYER_ERROR_TEXT);
        }
    }

    private void showError(Component p_88718_)
    {
        this.errorMsg = p_88718_;
        NarratorChatListener.INSTANCE.sayNow(p_88718_);
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

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        this.font.draw(pMatrixStack, NAME_LABEL, (float)(this.width / 2 - 100), (float)row(1), 10526880);

        if (this.errorMsg != null)
        {
            drawCenteredString(pMatrixStack, this.font, this.errorMsg, this.width / 2, row(5), 16711680);
        }

        this.profileName.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }
}
