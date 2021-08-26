package net.minecraft.network.chat;

import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;

public interface MutableComponent extends Component
{
    MutableComponent setStyle(Style pStyle);

default MutableComponent append(String pString)
    {
        return this.append(new TextComponent(pString));
    }

    MutableComponent append(Component pString);

default MutableComponent withStyle(UnaryOperator<Style> pModifyFunc)
    {
        this.setStyle(pModifyFunc.apply(this.getStyle()));
        return this;
    }

default MutableComponent withStyle(Style pModifyFunc)
    {
        this.setStyle(pModifyFunc.applyTo(this.getStyle()));
        return this;
    }

default MutableComponent m_130944_(ChatFormatting... p_130945_)
    {
        this.setStyle(this.getStyle().m_131152_(p_130945_));
        return this;
    }

default MutableComponent withStyle(ChatFormatting pModifyFunc)
    {
        this.setStyle(this.getStyle().applyFormat(pModifyFunc));
        return this;
    }
}
