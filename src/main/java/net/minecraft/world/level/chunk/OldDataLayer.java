package net.minecraft.world.level.chunk;

public class OldDataLayer
{
    public final byte[] data;
    private final int depthBits;
    private final int depthBitsPlusFour;

    public OldDataLayer(byte[] p_63054_, int p_63055_)
    {
        this.data = p_63054_;
        this.depthBits = p_63055_;
        this.depthBitsPlusFour = p_63055_ + 4;
    }

    public int get(int pX, int pY, int pZ)
    {
        int i = pX << this.depthBitsPlusFour | pZ << this.depthBits | pY;
        int j = i >> 1;
        int k = i & 1;
        return k == 0 ? this.data[j] & 15 : this.data[j] >> 4 & 15;
    }
}
