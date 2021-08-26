package net.optifine.config;

import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.optifine.gui.GuiOptionButtonOF;

public class IteratableOptionOF extends Option
{
    public IteratableOptionOF(String pCaptionKey)
    {
        super(pCaptionKey);
    }

    public AbstractWidget createButton(Options pOptions, int pX, int pY, int pWidth)
    {
        return new GuiOptionButtonOF(pX, pY, pWidth, 20, this, this.getOptionText(pOptions), (btn) ->
        {
            this.onPress(btn, pOptions);
        });
    }

    public void onPress(Button button, Options options)
    {
        options.setOptionValueOF(this, 1);
        button.setMessage(options.getKeyComponentOF(this));
        options.save();
    }

    public Component getOptionText(Options options)
    {
        return options.getKeyComponentOF(this);
    }
}
