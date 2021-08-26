package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.SliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.optifine.Config;

public class ProgressOption extends Option
{
    protected final float steps;
    protected final double minValue;
    protected double maxValue;
    private final Function<Minecraft, List<FormattedCharSequence>> tooltipSupplier;
    protected Function<Options, Double> getter;
    protected BiConsumer<Options, Double> setter;
    protected BiFunction<Options, ProgressOption, Component> toString;
    protected double[] stepValues;

    public ProgressOption(String p_168540_, double p_168541_, double p_168542_, float p_168543_, Function<Options, Double> p_168544_, BiConsumer<Options, Double> p_168545_, BiFunction<Options, ProgressOption, Component> p_168546_, Function<Minecraft, List<FormattedCharSequence>> p_168547_)
    {
        super(p_168540_);
        this.minValue = p_168541_;
        this.maxValue = p_168542_;
        this.steps = p_168543_;
        this.getter = p_168544_;
        this.setter = p_168545_;
        this.toString = p_168546_;
        this.tooltipSupplier = p_168547_;
    }

    public ProgressOption(String p_92211_, double p_92212_, double p_92213_, float p_92214_, Function<Options, Double> p_92215_, BiConsumer<Options, Double> p_92216_, BiFunction<Options, ProgressOption, Component> p_92217_)
    {
        this(p_92211_, p_92212_, p_92213_, p_92214_, p_92215_, p_92216_, p_92217_, (p_194076_0_) ->
        {
            return ImmutableList.of();
        });
    }

    public ProgressOption(String translationKey, double minValueIn, double maxValueIn, double[] stepValues, Function<Options, Double> getter, BiConsumer<Options, Double> setter, BiFunction<Options, ProgressOption, Component> getDisplayString)
    {
        super(translationKey);
        this.minValue = minValueIn;
        this.maxValue = maxValueIn;
        this.steps = 0.0F;
        this.getter = getter;
        this.setter = setter;
        this.toString = getDisplayString;
        this.tooltipSupplier = (x) ->
        {
            return new ArrayList<>();
        };
        this.stepValues = stepValues;

        if (stepValues != null)
        {
            stepValues = (double[])stepValues.clone();
            Arrays.sort(stepValues);
        }
    }

    public AbstractWidget createButton(Options pOptions, int pX, int pY, int pWidth)
    {
        List<FormattedCharSequence> list = this.tooltipSupplier.apply(Minecraft.getInstance());
        return new SliderButton(pOptions, pX, pY, pWidth, 20, this, list);
    }

    public double toPct(double pValue)
    {
        return Mth.clamp((this.clamp(pValue) - this.minValue) / (this.maxValue - this.minValue), 0.0D, 1.0D);
    }

    public double toValue(double pValue)
    {
        return this.clamp(Mth.lerp(Mth.clamp(pValue, 0.0D, 1.0D), this.minValue, this.maxValue));
    }

    private double clamp(double pValue)
    {
        if (this.steps > 0.0F)
        {
            pValue = (double)(this.steps * (float)Math.round(pValue / (double)this.steps));
        }

        if (this.stepValues != null)
        {
            for (int i = 0; i < this.stepValues.length; ++i)
            {
                double d0 = i <= 0 ? -Double.MAX_VALUE : (this.stepValues[i - 1] + this.stepValues[i]) / 2.0D;
                double d1 = i >= this.stepValues.length - 1 ? Double.MAX_VALUE : (this.stepValues[i] + this.stepValues[i + 1]) / 2.0D;

                if (Config.between(pValue, d0, d1))
                {
                    pValue = this.stepValues[i];
                    break;
                }
            }
        }

        return Mth.clamp(pValue, this.minValue, this.maxValue);
    }

    public double getMinValue()
    {
        return this.minValue;
    }

    public double getMaxValue()
    {
        return this.maxValue;
    }

    public void setMaxValue(float pValue)
    {
        this.maxValue = (double)pValue;
    }

    public void set(Options pOptions, double pValue)
    {
        this.setter.accept(pOptions, pValue);
    }

    public double get(Options pOptions)
    {
        return this.getter.apply(pOptions);
    }

    public Component getMessage(Options p_92234_)
    {
        return this.toString.apply(p_92234_, this);
    }
}
