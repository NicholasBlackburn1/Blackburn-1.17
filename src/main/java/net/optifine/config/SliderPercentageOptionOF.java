package net.optifine.config;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.client.Options;
import net.minecraft.client.ProgressOption;
import net.minecraft.network.chat.Component;

public class SliderPercentageOptionOF extends ProgressOption
{
    public SliderPercentageOptionOF(String pCaptionKey)
    {
        this(pCaptionKey, 0.0D, 1.0D, 0.0F);
    }

    public SliderPercentageOptionOF(String name, double valueMin, double valueMax, float step)
    {
        super(name, valueMin, valueMax, step, (Function<Options, Double>)null, (BiConsumer<Options, Double>)null, (BiFunction<Options, ProgressOption, Component>)null);
        super.getter = this::getOptionValue;
        super.setter = this::setOptionValue;
        super.toString = this::getOptionText;
    }

    public SliderPercentageOptionOF(String name, double valueMin, double valueMax, double[] stepValues)
    {
        super(name, valueMin, valueMax, stepValues, (Function<Options, Double>)null, (BiConsumer<Options, Double>)null, (BiFunction<Options, ProgressOption, Component>)null);
        super.getter = this::getOptionValue;
        super.setter = this::setOptionValue;
        super.toString = this::getOptionText;
    }

    private double getOptionValue(Options gameSettings)
    {
        return gameSettings.getOptionFloatValueOF(this);
    }

    private void setOptionValue(Options gameSettings, double value)
    {
        gameSettings.setOptionFloatValueOF(this, value);
    }

    private Component getOptionText(Options gameSettings, ProgressOption sliderPercentageOption)
    {
        return gameSettings.getKeyComponentOF(sliderPercentageOption);
    }
}
