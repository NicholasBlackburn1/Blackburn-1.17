package net.minecraft.world.level.chunk;

import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.VisibleForDebug;

public final class DataLayer
{
    public static final int f_182480_ = 16;
    public static final int LAYER_SIZE = 128;
    public static final int SIZE = 2048;
    private static final int NIBBLE_SIZE = 4;
    @Nullable
    protected byte[] data;

    public DataLayer()
    {
    }

    public DataLayer(byte[] p_62556_)
    {
        this.data = p_62556_;

        if (p_62556_.length != 2048)
        {
            throw(IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("DataLayer should be 2048 bytes not: " + p_62556_.length));
        }
    }

    protected DataLayer(int p_62554_)
    {
        this.data = new byte[p_62554_];
    }

    public int get(int pIndex, int p_62562_, int p_62563_)
    {
        return this.get(getIndex(pIndex, p_62562_, p_62563_));
    }

    public void set(int pIndex, int pValue, int p_62567_, int p_62568_)
    {
        this.set(getIndex(pIndex, pValue, p_62567_), p_62568_);
    }

    private static int getIndex(int pX, int pY, int pZ)
    {
        return pY << 8 | pZ << 4 | pX;
    }

    private int get(int pIndex)
    {
        if (this.data == null)
        {
            return 0;
        }
        else
        {
            int i = getPosition(pIndex);
            int j = m_182481_(pIndex);
            return this.data[i] >> 4 * j & 15;
        }
    }

    private void set(int pIndex, int pValue)
    {
        if (this.data == null)
        {
            this.data = new byte[2048];
        }

        int i = getPosition(pIndex);
        int j = m_182481_(pIndex);
        int k = ~(15 << 4 * j);
        int l = (pValue & 15) << 4 * j;
        this.data[i] = (byte)(this.data[i] & k | l);
    }

    private static int m_182481_(int p_182482_)
    {
        return p_182482_ & 1;
    }

    private static int getPosition(int pIndex)
    {
        return pIndex >> 1;
    }

    public byte[] getData()
    {
        if (this.data == null)
        {
            this.data = new byte[2048];
        }

        return this.data;
    }

    public DataLayer copy()
    {
        return this.data == null ? new DataLayer() : new DataLayer((byte[])this.data.clone());
    }

    public String toString()
    {
        StringBuilder stringbuilder = new StringBuilder();

        for (int i = 0; i < 4096; ++i)
        {
            stringbuilder.append(Integer.toHexString(this.get(i)));

            if ((i & 15) == 15)
            {
                stringbuilder.append("\n");
            }

            if ((i & 255) == 255)
            {
                stringbuilder.append("\n");
            }
        }

        return stringbuilder.toString();
    }

    @VisibleForDebug
    public String layerToString(int p_156342_)
    {
        StringBuilder stringbuilder = new StringBuilder();

        for (int i = 0; i < 256; ++i)
        {
            stringbuilder.append(Integer.toHexString(this.get(i)));

            if ((i & 15) == 15)
            {
                stringbuilder.append("\n");
            }
        }

        return stringbuilder.toString();
    }

    public boolean isEmpty()
    {
        return this.data == null;
    }
}
