package net.minecraft.client.gui.components;

import java.util.List;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.ProgressOption;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.optifine.config.FloatOptions;
import net.optifine.gui.IOptionControl;

public class SliderButton extends AbstractOptionSliderButton implements TooltipAccessor, IOptionControl
{
    private final ProgressOption option;
    private final List<FormattedCharSequence> tooltip;
    private boolean supportAdjusting;
    private boolean adjusting;

    public SliderButton(Options p_169060_, int p_169061_, int p_169062_, int p_169063_, int p_169064_, ProgressOption p_169065_, List<FormattedCharSequence> p_169066_)
    {
        super(p_169060_, p_169061_, p_169062_, p_169063_, p_169064_, (double)((float)p_169065_.toPct(p_169065_.get(p_169060_))));
        this.option = p_169065_;
        this.tooltip = p_169066_;
        this.updateMessage();
        this.supportAdjusting = FloatOptions.supportAdjusting(this.option);
        this.adjusting = false;
    }

    protected void applyValue()
    {
        if (!this.adjusting)
        {
            double d0 = this.option.get(this.options);
            double d1 = this.option.toValue(this.value);

            if (d1 != d0)
            {
                this.option.set(this.options, this.option.toValue(this.value));
                this.options.save();
            }
        }
    }

    protected void updateMessage()
    {
        if (this.adjusting)
        {
            double d0 = this.option.toValue(this.value);
            Component component = FloatOptions.getTextComponent(this.option, d0);

            if (component != null)
            {
                this.setMessage(component);
            }
        }
        else
        {
            this.setMessage(this.option.getMessage(this.options));
        }
    }

    public List<FormattedCharSequence> getTooltip()
    {
        return this.tooltip;
    }

    public void onClick(double pMouseX, double p_93589_)
    {
        if (this.supportAdjusting)
        {
            this.adjusting = true;
        }

        super.onClick(pMouseX, p_93589_);
    }

    protected void onDrag(double pMouseX, double p_93592_, double pMouseY, double p_93594_)
    {
        if (this.supportAdjusting)
        {
            this.adjusting = true;
        }

        super.onDrag(pMouseX, p_93592_, pMouseY, p_93594_);
    }

    public void onRelease(double pMouseX, double p_93610_)
    {
        if (this.adjusting)
        {
            this.adjusting = false;
            this.applyValue();
            this.updateMessage();
        }

        super.onRelease(pMouseX, p_93610_);
    }

    public static int getWidth(AbstractWidget btn)
    {
        return btn.width;
    }

    public static int getHeight(AbstractWidget btn)
    {
        return btn.height;
    }

    public Option getControlOption()
    {
        return this.option;
    }
}
