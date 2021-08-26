package net.optifine.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;

public class GuiButtonOF extends Button
{
    public final int id;

    public GuiButtonOF(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, Button.OnPress pressable)
    {
        super(x, y, widthIn, heightIn, new TextComponent(buttonText), pressable);
        this.id = buttonId;
    }

    public GuiButtonOF(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
    {
        this(buttonId, x, y, widthIn, heightIn, buttonText, (btn) ->
        {
        });
    }

    public GuiButtonOF(int buttonId, int x, int y, String buttonText)
    {
        this(buttonId, x, y, 200, 20, buttonText, (btn) ->
        {
        });
    }

    public void setMessage(String messageIn)
    {
        super.setMessage(new TextComponent(messageIn));
    }
}
