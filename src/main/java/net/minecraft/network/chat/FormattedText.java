package net.minecraft.network.chat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Unit;

public interface FormattedText
{
    Optional<Unit> STOP_ITERATION = Optional.of(Unit.INSTANCE);
    FormattedText EMPTY = new FormattedText()
    {
        public <T> Optional<T> visit(FormattedText.ContentConsumer<T> pAcceptor)
        {
            return Optional.empty();
        }
        public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> pAcceptor, Style p_130782_)
        {
            return Optional.empty();
        }
    };

    <T> Optional<T> visit(FormattedText.ContentConsumer<T> pAcceptor);

    <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> pAcceptor, Style p_130772_);

    static FormattedText of(final String p_130776_)
    {
        return new FormattedText()
        {
            public <T> Optional<T> visit(FormattedText.ContentConsumer<T> pAcceptor)
            {
                return pAcceptor.accept(p_130776_);
            }
            public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> pAcceptor, Style p_130790_)
            {
                return pAcceptor.accept(p_130790_, p_130776_);
            }
        };
    }

    static FormattedText of(final String p_130763_, final Style p_130764_)
    {
        return new FormattedText()
        {
            public <T> Optional<T> visit(FormattedText.ContentConsumer<T> pAcceptor)
            {
                return pAcceptor.accept(p_130763_);
            }
            public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> pAcceptor, Style p_130800_)
            {
                return pAcceptor.accept(p_130764_.applyTo(p_130800_), p_130763_);
            }
        };
    }

    static FormattedText m_130773_(FormattedText... p_130774_)
    {
        return composite(ImmutableList.copyOf(p_130774_));
    }

    static FormattedText composite(final List <? extends FormattedText > p_130769_)
    {
        return new FormattedText()
        {
            public <T> Optional<T> visit(FormattedText.ContentConsumer<T> pAcceptor)
            {
                for (FormattedText formattedtext : p_130769_)
                {
                    Optional<T> optional = formattedtext.visit(pAcceptor);

                    if (optional.isPresent())
                    {
                        return optional;
                    }
                }

                return Optional.empty();
            }
            public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> pAcceptor, Style p_130808_)
            {
                for (FormattedText formattedtext : p_130769_)
                {
                    Optional<T> optional = formattedtext.visit(pAcceptor, p_130808_);

                    if (optional.isPresent())
                    {
                        return optional;
                    }
                }

                return Optional.empty();
            }
        };
    }

default String getString()
    {
        StringBuilder stringbuilder = new StringBuilder();
        this.visit((p_130767_) ->
        {
            stringbuilder.append(p_130767_);
            return Optional.empty();
        });
        return stringbuilder.toString();
    }

    public interface ContentConsumer<T>
    {
        Optional<T> accept(String p_130810_);
    }

    public interface StyledContentConsumer<T>
    {
        Optional<T> accept(Style p_130811_, String p_130812_);
    }
}
