package net.optifine.config;

import net.minecraft.client.Option;
import net.minecraft.client.ProgressOption;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.optifine.Config;
import net.optifine.Lang;

public class FloatOptions
{
    public static Component getTextComponent(Option option, double val)
    {
        if (option == Option.RENDER_DISTANCE)
        {
            return option.genericValueLabel(new TranslatableComponent("options.chunks", (int)val));
        }
        else if (option == Option.MIPMAP_LEVELS)
        {
            if (val >= 4.0D)
            {
                return option.genericValueLabel(new TranslatableComponent("of.general.max"));
            }
            else
            {
                return (Component)(val == 0.0D ? CommonComponents.optionStatus(option.getCaption(), false) : option.genericValueLabel((int)val));
            }
        }
        else if (option == Option.BIOME_BLEND_RADIUS)
        {
            int i = (int)val * 2 + 1;
            return option.genericValueLabel(new TranslatableComponent("options.biomeBlendRadius." + i));
        }
        else
        {
            String s = getText(option, val);
            return s != null ? new TextComponent(s) : null;
        }
    }

    public static String getText(Option option, double val)
    {
        String s = I18n.m_118938_(option.getResourceKey()) + ": ";

        if (option == Option.AO_LEVEL)
        {
            return val == 0.0D ? s + I18n.m_118938_("options.off") : s + (int)(val * 100.0D) + "%";
        }
        else if (option == Option.MIPMAP_TYPE)
        {
            int k = (int)val;

            switch (k)
            {
                case 0:
                    return s + Lang.get("of.options.mipmap.nearest");

                case 1:
                    return s + Lang.get("of.options.mipmap.linear");

                case 2:
                    return s + Lang.get("of.options.mipmap.bilinear");

                case 3:
                    return s + Lang.get("of.options.mipmap.trilinear");

                default:
                    return s + "of.options.mipmap.nearest";
            }
        }
        else if (option == Option.AA_LEVEL)
        {
            int j = (int)val;
            String s1 = "";

            if (j != Config.getAntialiasingLevel())
            {
                s1 = " (" + Lang.get("of.general.restart") + ")";
            }

            return j == 0 ? s + Lang.getOff() + s1 : s + j + s1;
        }
        else if (option == Option.AF_LEVEL)
        {
            int i = (int)val;
            return i == 1 ? s + Lang.getOff() : s + i;
        }
        else
        {
            return null;
        }
    }

    public static boolean supportAdjusting(ProgressOption option)
    {
        Component component = getTextComponent(option, 0.0D);
        return component != null;
    }
}
