package net.optifine.shaders.gui;

import net.optifine.gui.GuiButtonOF;
import net.optifine.shaders.config.ShaderOption;
import net.optifine.shaders.config.ShaderOptionScreen;

public class GuiButtonShaderOption extends GuiButtonOF
{
    private ShaderOption shaderOption = null;

    public GuiButtonShaderOption(int buttonId, int x, int y, int widthIn, int heightIn, ShaderOption shaderOption, String text)
    {
        super(buttonId, x, y, widthIn, heightIn, text);
        this.shaderOption = shaderOption;
    }

    protected boolean isValidClickButton(int pButton)
    {
        if (this.shaderOption instanceof ShaderOptionScreen)
        {
            return pButton == 0;
        }
        else
        {
            return true;
        }
    }

    public ShaderOption getShaderOption()
    {
        return this.shaderOption;
    }

    public void valueChanged()
    {
    }

    public boolean isSwitchable()
    {
        return true;
    }
}
