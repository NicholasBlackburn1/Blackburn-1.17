package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.network.chat.TranslatableComponent;

public class WorldCoordinate
{
    private static final char PREFIX_RELATIVE = '~';
    public static final SimpleCommandExceptionType ERROR_EXPECTED_DOUBLE = new SimpleCommandExceptionType(new TranslatableComponent("argument.pos.missing.double"));
    public static final SimpleCommandExceptionType ERROR_EXPECTED_INT = new SimpleCommandExceptionType(new TranslatableComponent("argument.pos.missing.int"));
    private final boolean relative;
    private final double value;

    public WorldCoordinate(boolean p_120864_, double p_120865_)
    {
        this.relative = p_120864_;
        this.value = p_120865_;
    }

    public double get(double pCoord)
    {
        return this.relative ? this.value + pCoord : this.value;
    }

    public static WorldCoordinate parseDouble(StringReader pReader, boolean pCenterIntegers) throws CommandSyntaxException
    {
        if (pReader.canRead() && pReader.peek() == '^')
        {
            throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext(pReader);
        }
        else if (!pReader.canRead())
        {
            throw ERROR_EXPECTED_DOUBLE.createWithContext(pReader);
        }
        else
        {
            boolean flag = isRelative(pReader);
            int i = pReader.getCursor();
            double d0 = pReader.canRead() && pReader.peek() != ' ' ? pReader.readDouble() : 0.0D;
            String s = pReader.getString().substring(i, pReader.getCursor());

            if (flag && s.isEmpty())
            {
                return new WorldCoordinate(true, 0.0D);
            }
            else
            {
                if (!s.contains(".") && !flag && pCenterIntegers)
                {
                    d0 += 0.5D;
                }

                return new WorldCoordinate(flag, d0);
            }
        }
    }

    public static WorldCoordinate parseInt(StringReader pReader) throws CommandSyntaxException
    {
        if (pReader.canRead() && pReader.peek() == '^')
        {
            throw Vec3Argument.ERROR_MIXED_TYPE.createWithContext(pReader);
        }
        else if (!pReader.canRead())
        {
            throw ERROR_EXPECTED_INT.createWithContext(pReader);
        }
        else
        {
            boolean flag = isRelative(pReader);
            double d0;

            if (pReader.canRead() && pReader.peek() != ' ')
            {
                d0 = flag ? pReader.readDouble() : (double)pReader.readInt();
            }
            else
            {
                d0 = 0.0D;
            }

            return new WorldCoordinate(flag, d0);
        }
    }

    public static boolean isRelative(StringReader pReader)
    {
        boolean flag;

        if (pReader.peek() == '~')
        {
            flag = true;
            pReader.skip();
        }
        else
        {
            flag = false;
        }

        return flag;
    }

    public boolean equals(Object p_120877_)
    {
        if (this == p_120877_)
        {
            return true;
        }
        else if (!(p_120877_ instanceof WorldCoordinate))
        {
            return false;
        }
        else
        {
            WorldCoordinate worldcoordinate = (WorldCoordinate)p_120877_;

            if (this.relative != worldcoordinate.relative)
            {
                return false;
            }
            else
            {
                return Double.compare(worldcoordinate.value, this.value) == 0;
            }
        }
    }

    public int hashCode()
    {
        int i = this.relative ? 1 : 0;
        long j = Double.doubleToLongBits(this.value);
        return 31 * i + (int)(j ^ j >>> 32);
    }

    public boolean isRelative()
    {
        return this.relative;
    }
}
