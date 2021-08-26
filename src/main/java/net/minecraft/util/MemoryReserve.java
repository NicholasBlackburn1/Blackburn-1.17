package net.minecraft.util;

import javax.annotation.Nullable;

public class MemoryReserve
{
    @Nullable
    private static byte[] f_182324_ = null;

    public static void m_182327_()
    {
        f_182324_ = new byte[10485760];
    }

    public static void m_182328_()
    {
        f_182324_ = new byte[0];
    }
}
