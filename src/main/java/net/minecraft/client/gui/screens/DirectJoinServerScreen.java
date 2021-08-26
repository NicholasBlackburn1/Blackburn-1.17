package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class DirectJoinServerScreen extends Screen
{
    private static final Component ENTER_IP_LABEL = new TranslatableComponent("addServer.enterIp");
    private Button selectButton;
    private final ServerData serverData;
    private EditBox ipEdit;
    private final BooleanConsumer callback;
    private final Screen lastScreen;

    public DirectJoinServerScreen(Screen p_95960_, BooleanConsumer p_95961_, ServerData p_95962_)
    {
        super(new TranslatableComponent("selectServer.direct"));
        this.lastScreen = p_95960_;
        this.serverData = p_95962_;
        this.callback = p_95961_;
    }

    public void tick()
    {
        this.ipEdit.tick();
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        if (!this.selectButton.active || this.getFocused() != this.ipEdit || pKeyCode != 257 && pKeyCode != 335)
        {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
        else
        {
            this.onSelect();
            return true;
        }
    }

    protected void init()
    {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.selectButton = this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 96 + 12, 200, 20, new TranslatableComponent("selectServer.select"), (p_95981_) ->
        {
            this.onSelect();
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20, CommonComponents.GUI_CANCEL, (p_95977_) ->
        {
            this.callback.accept(false);
        }));
        this.ipEdit = new EditBox(this.font, this.width / 2 - 100, 116, 200, 20, new TranslatableComponent("addServer.enterIp"));
        this.ipEdit.setMaxLength(128);
        this.ipEdit.setFocus(true);
        this.ipEdit.setValue(this.minecraft.options.lastMpIp);
        this.ipEdit.setResponder((p_95983_) ->
        {
            this.updateSelectButtonStatus();
        });
        this.addWidget(this.ipEdit);
        this.setInitialFocus(this.ipEdit);
        this.updateSelectButtonStatus();
    }

    public void resize(Minecraft pMinecraft, int pWidth, int pHeight)
    {
        String s = this.ipEdit.getValue();
        this.init(pMinecraft, pWidth, pHeight);
        this.ipEdit.setValue(s);
    }

    private void onSelect()
    {
        this.serverData.ip = this.ipEdit.getValue();
        this.callback.accept(true);
    }

    public void onClose()
    {
        this.minecraft.setScreen(this.lastScreen);
    }

    public void removed()
    {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        this.minecraft.options.lastMpIp = this.ipEdit.getValue();
        this.minecraft.options.save();
    }

    private void updateSelectButtonStatus()
    {
        this.selectButton.active = ServerAddress.isValidAddress(this.ipEdit.getValue());
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, 20, 16777215);
        drawString(pMatrixStack, this.font, ENTER_IP_LABEL, this.width / 2 - 100, 100, 10526880);
        this.ipEdit.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }
}
