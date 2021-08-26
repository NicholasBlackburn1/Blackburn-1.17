package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.GsonHelper;

public class WrappedMinMaxBounds
{
    public static final WrappedMinMaxBounds ANY = new WrappedMinMaxBounds((Float)null, (Float)null);
    public static final SimpleCommandExceptionType ERROR_INTS_ONLY = new SimpleCommandExceptionType(new TranslatableComponent("argument.range.ints"));
    private final Float min;
    private final Float max;

    public WrappedMinMaxBounds(@Nullable Float p_75356_, @Nullable Float p_75357_)
    {
        this.min = p_75356_;
        this.max = p_75357_;
    }

    public static WrappedMinMaxBounds exactly(float p_164403_)
    {
        return new WrappedMinMaxBounds(p_164403_, p_164403_);
    }

    public static WrappedMinMaxBounds between(float p_164405_, float p_164406_)
    {
        return new WrappedMinMaxBounds(p_164405_, p_164406_);
    }

    public static WrappedMinMaxBounds atLeast(float p_164415_)
    {
        return new WrappedMinMaxBounds(p_164415_, (Float)null);
    }

    public static WrappedMinMaxBounds atMost(float p_164418_)
    {
        return new WrappedMinMaxBounds((Float)null, p_164418_);
    }

    public boolean matches(float p_164420_)
    {
        if (this.min != null && this.max != null && this.min > this.max && this.min > p_164420_ && this.max < p_164420_)
        {
            return false;
        }
        else if (this.min != null && this.min > p_164420_)
        {
            return false;
        }
        else
        {
            return this.max == null || !(this.max < p_164420_);
        }
    }

    public boolean matchesSqr(double p_164401_)
    {
        if (this.min != null && this.max != null && this.min > this.max && (double)(this.min * this.min) > p_164401_ && (double)(this.max * this.max) < p_164401_)
        {
            return false;
        }
        else if (this.min != null && (double)(this.min * this.min) > p_164401_)
        {
            return false;
        }
        else
        {
            return this.max == null || !((double)(this.max * this.max) < p_164401_);
        }
    }

    @Nullable
    public Float getMin()
    {
        return this.min;
    }

    @Nullable
    public Float getMax()
    {
        return this.max;
    }

    public JsonElement serializeToJson()
    {
        if (this == ANY)
        {
            return JsonNull.INSTANCE;
        }
        else if (this.min != null && this.max != null && this.min.equals(this.max))
        {
            return new JsonPrimitive(this.min);
        }
        else
        {
            JsonObject jsonobject = new JsonObject();

            if (this.min != null)
            {
                jsonobject.addProperty("min", this.min);
            }

            if (this.max != null)
            {
                jsonobject.addProperty("max", this.min);
            }

            return jsonobject;
        }
    }

    public static WrappedMinMaxBounds fromJson(@Nullable JsonElement p_164408_)
    {
        if (p_164408_ != null && !p_164408_.isJsonNull())
        {
            if (GsonHelper.isNumberValue(p_164408_))
            {
                float f2 = GsonHelper.convertToFloat(p_164408_, "value");
                return new WrappedMinMaxBounds(f2, f2);
            }
            else
            {
                JsonObject jsonobject = GsonHelper.convertToJsonObject(p_164408_, "value");
                Float f = jsonobject.has("min") ? GsonHelper.getAsFloat(jsonobject, "min") : null;
                Float f1 = jsonobject.has("max") ? GsonHelper.getAsFloat(jsonobject, "max") : null;
                return new WrappedMinMaxBounds(f, f1);
            }
        }
        else
        {
            return ANY;
        }
    }

    public static WrappedMinMaxBounds fromReader(StringReader pReader, boolean pIsFloatingPoint) throws CommandSyntaxException
    {
        return fromReader(pReader, pIsFloatingPoint, (p_164413_) ->
        {
            return p_164413_;
        });
    }

    public static WrappedMinMaxBounds fromReader(StringReader pReader, boolean pIsFloatingPoint, Function<Float, Float> pValueFunction) throws CommandSyntaxException
    {
        if (!pReader.canRead())
        {
            throw MinMaxBounds.ERROR_EMPTY.createWithContext(pReader);
        }
        else
        {
            int i = pReader.getCursor();
            Float f = optionallyFormat(readNumber(pReader, pIsFloatingPoint), pValueFunction);
            Float f1;

            if (pReader.canRead(2) && pReader.peek() == '.' && pReader.peek(1) == '.')
            {
                pReader.skip();
                pReader.skip();
                f1 = optionallyFormat(readNumber(pReader, pIsFloatingPoint), pValueFunction);

                if (f == null && f1 == null)
                {
                    pReader.setCursor(i);
                    throw MinMaxBounds.ERROR_EMPTY.createWithContext(pReader);
                }
            }
            else
            {
                if (!pIsFloatingPoint && pReader.canRead() && pReader.peek() == '.')
                {
                    pReader.setCursor(i);
                    throw ERROR_INTS_ONLY.createWithContext(pReader);
                }

                f1 = f;
            }

            if (f == null && f1 == null)
            {
                pReader.setCursor(i);
                throw MinMaxBounds.ERROR_EMPTY.createWithContext(pReader);
            }
            else
            {
                return new WrappedMinMaxBounds(f, f1);
            }
        }
    }

    @Nullable
    private static Float readNumber(StringReader pReader, boolean pIsFloatingPoint) throws CommandSyntaxException
    {
        int i = pReader.getCursor();

        while (pReader.canRead() && isAllowedNumber(pReader, pIsFloatingPoint))
        {
            pReader.skip();
        }

        String s = pReader.getString().substring(i, pReader.getCursor());

        if (s.isEmpty())
        {
            return null;
        }
        else
        {
            try
            {
                return Float.parseFloat(s);
            }
            catch (NumberFormatException numberformatexception)
            {
                if (pIsFloatingPoint)
                {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidDouble().createWithContext(pReader, s);
                }
                else
                {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt().createWithContext(pReader, s);
                }
            }
        }
    }

    private static boolean isAllowedNumber(StringReader pReader, boolean pIsFloatingPoint)
    {
        char c0 = pReader.peek();

        if ((c0 < '0' || c0 > '9') && c0 != '-')
        {
            if (pIsFloatingPoint && c0 == '.')
            {
                return !pReader.canRead(2) || pReader.peek(1) != '.';
            }
            else
            {
                return false;
            }
        }
        else
        {
            return true;
        }
    }

    @Nullable
    private static Float optionallyFormat(@Nullable Float pValue, Function<Float, Float> pValueFunction)
    {
        return pValue == null ? null : pValueFunction.apply(pValue);
    }
}
