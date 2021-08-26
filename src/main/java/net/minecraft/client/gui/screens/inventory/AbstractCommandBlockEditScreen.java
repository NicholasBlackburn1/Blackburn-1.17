package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BaseCommandBlock;

public abstract class AbstractCommandBlockEditScreen extends Screen
{
    private static final Component SET_COMMAND_LABEL = new TranslatableComponent("advMode.setCommand");
    private static final Component COMMAND_LABEL = new TranslatableComponent("advMode.command");
    private static final Component PREVIOUS_OUTPUT_LABEL = new TranslatableComponent("advMode.previousOutput");
    protected EditBox commandEdit;
    protected EditBox previousEdit;
    protected Button doneButton;
    protected Button cancelButton;
    protected CycleButton<Boolean> outputButton;
    CommandSuggestions commandSuggestions;

    public AbstractCommandBlockEditScreen()
    {
        super(NarratorChatListener.NO_TITLE);
    }

    public void tick()
    {
        this.commandEdit.tick();
    }

    abstract BaseCommandBlock getCommandBlock();

    abstract int getPreviousY();

    protected void init()
    {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.doneButton = this.addRenderableWidget(new Button(this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20, CommonComponents.GUI_DONE, (p_97691_) ->
        {
            this.onDone();
        }));
        this.cancelButton = this.addRenderableWidget(new Button(this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20, CommonComponents.GUI_CANCEL, (p_97687_) ->
        {
            this.onClose();
        }));
        boolean flag = this.getCommandBlock().isTrackOutput();
        this.outputButton = this.addRenderableWidget(CycleButton.booleanBuilder(new TextComponent("O"), new TextComponent("X")).withInitialValue(flag).displayOnlyValue().create(this.width / 2 + 150 - 20, this.getPreviousY(), 20, 20, new TranslatableComponent("advMode.trackOutput"), (p_169596_, p_169597_) ->
        {
            BaseCommandBlock basecommandblock = this.getCommandBlock();
            basecommandblock.setTrackOutput(p_169597_);
            this.updatePreviousOutput(p_169597_);
        }));
        this.commandEdit = new EditBox(this.font, this.width / 2 - 150, 50, 300, 20, new TranslatableComponent("advMode.command"))
        {
            protected MutableComponent createNarrationMessage()
            {
                return super.createNarrationMessage().append(AbstractCommandBlockEditScreen.this.commandSuggestions.getNarrationMessage());
            }
        };
        this.commandEdit.setMaxLength(32500);
        this.commandEdit.setResponder(this::onEdited);
        this.addWidget(this.commandEdit);
        this.previousEdit = new EditBox(this.font, this.width / 2 - 150, this.getPreviousY(), 276, 20, new TranslatableComponent("advMode.previousOutput"));
        this.previousEdit.setMaxLength(32500);
        this.previousEdit.setEditable(false);
        this.previousEdit.setValue("-");
        this.addWidget(this.previousEdit);
        this.setInitialFocus(this.commandEdit);
        this.commandEdit.setFocus(true);
        this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.commandEdit, this.font, true, true, 0, 7, false, Integer.MIN_VALUE);
        this.commandSuggestions.setAllowSuggestions(true);
        this.commandSuggestions.updateCommandInfo();
        this.updatePreviousOutput(flag);
    }

    public void resize(Minecraft pMinecraft, int pWidth, int pHeight)
    {
        String s = this.commandEdit.getValue();
        this.init(pMinecraft, pWidth, pHeight);
        this.commandEdit.setValue(s);
        this.commandSuggestions.updateCommandInfo();
    }

    protected void updatePreviousOutput(boolean p_169599_)
    {
        this.previousEdit.setValue(p_169599_ ? this.getCommandBlock().getLastOutput().getString() : "-");
    }

    protected void onDone()
    {
        BaseCommandBlock basecommandblock = this.getCommandBlock();
        this.populateAndSendPacket(basecommandblock);

        if (!basecommandblock.isTrackOutput())
        {
            basecommandblock.setLastOutput((Component)null);
        }

        this.minecraft.setScreen((Screen)null);
    }

    public void removed()
    {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    protected abstract void populateAndSendPacket(BaseCommandBlock p_97670_);

    private void onEdited(String p_97689_)
    {
        this.commandSuggestions.updateCommandInfo();
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        if (this.commandSuggestions.keyPressed(pKeyCode, pScanCode, pModifiers))
        {
            return true;
        }
        else if (super.keyPressed(pKeyCode, pScanCode, pModifiers))
        {
            return true;
        }
        else if (pKeyCode != 257 && pKeyCode != 335)
        {
            return false;
        }
        else
        {
            this.onDone();
            return true;
        }
    }

    public boolean mouseScrolled(double pMouseX, double p_97660_, double pMouseY)
    {
        return this.commandSuggestions.mouseScrolled(pMouseY) ? true : super.mouseScrolled(pMouseX, p_97660_, pMouseY);
    }

    public boolean mouseClicked(double pMouseX, double p_97664_, int pMouseY)
    {
        return this.commandSuggestions.mouseClicked(pMouseX, p_97664_, pMouseY) ? true : super.mouseClicked(pMouseX, p_97664_, pMouseY);
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        drawCenteredString(pMatrixStack, this.font, SET_COMMAND_LABEL, this.width / 2, 20, 16777215);
        drawString(pMatrixStack, this.font, COMMAND_LABEL, this.width / 2 - 150, 40, 10526880);
        this.commandEdit.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        int i = 75;

        if (!this.previousEdit.getValue().isEmpty())
        {
            i = i + (5 * 9 + 1 + this.getPreviousY() - 135);
            drawString(pMatrixStack, this.font, PREVIOUS_OUTPUT_LABEL, this.width / 2 - 150, i + 4, 10526880);
            this.previousEdit.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        }

        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        this.commandSuggestions.render(pMatrixStack, pMouseX, pMouseY);
    }
}
