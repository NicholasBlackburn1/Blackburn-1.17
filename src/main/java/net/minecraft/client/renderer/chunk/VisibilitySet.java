package net.minecraft.client.renderer.chunk;

import java.util.Set;
import net.minecraft.core.Direction;

public class VisibilitySet
{
    private static final int FACINGS = Direction.values().length;
    private long bits;

    public void add(Set<Direction> pFacing)
    {
        for (Direction direction : pFacing)
        {
            for (Direction direction1 : pFacing)
            {
                this.set(direction, direction1, true);
            }
        }
    }

    public void set(Direction pFacing, Direction pFacing2, boolean pValue)
    {
        this.setBit(pFacing.ordinal() + pFacing2.ordinal() * FACINGS, pValue);
        this.setBit(pFacing2.ordinal() + pFacing.ordinal() * FACINGS, pValue);
    }

    public void setAll(boolean pVisible)
    {
        if (pVisible)
        {
            this.bits = -1L;
        }
        else
        {
            this.bits = 0L;
        }
    }

    public boolean visibilityBetween(Direction pFacing, Direction pFacing2)
    {
        return this.getBit(pFacing.ordinal() + pFacing2.ordinal() * FACINGS);
    }

    public String toString()
    {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append(' ');

        for (Direction direction : Direction.values())
        {
            stringbuilder.append(' ').append(direction.toString().toUpperCase().charAt(0));
        }

        stringbuilder.append('\n');

        for (Direction direction2 : Direction.values())
        {
            stringbuilder.append(direction2.toString().toUpperCase().charAt(0));

            for (Direction direction1 : Direction.values())
            {
                if (direction2 == direction1)
                {
                    stringbuilder.append("  ");
                }
                else
                {
                    boolean flag = this.visibilityBetween(direction2, direction1);
                    stringbuilder.append(' ').append((char)(flag ? 'Y' : 'n'));
                }
            }

            stringbuilder.append('\n');
        }

        return stringbuilder.toString();
    }

    private boolean getBit(int i)
    {
        return (this.bits & (long)(1 << i)) != 0L;
    }

    private void setBit(int i, boolean on)
    {
        if (on)
        {
            this.setBit(i);
        }
        else
        {
            this.clearBit(i);
        }
    }

    private void setBit(int i)
    {
        this.bits |= (long)(1 << i);
    }

    private void clearBit(int i)
    {
        this.bits &= (long)(~(1 << i));
    }
}
