package com.mojang.blaze3d.platform;

import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.MemoryUtil.MemoryAllocator;

public class MemoryTracker
{
    private static final MemoryAllocator f_182525_ = MemoryUtil.getAllocator(false);

    public static ByteBuffer m_182527_(int p_182528_)
    {
        long i = f_182525_.malloc((long)p_182528_);

        if (i == 0L)
        {
            throw new OutOfMemoryError("Failed to allocate " + p_182528_ + " bytes");
        }
        else
        {
            return MemoryUtil.memByteBuffer(i, p_182528_);
        }
    }

    public static ByteBuffer m_182529_(ByteBuffer p_182530_, int p_182531_)
    {
        long i = f_182525_.realloc(MemoryUtil.memAddress0(p_182530_), (long)p_182531_);

        if (i == 0L)
        {
            throw new OutOfMemoryError("Failed to resize buffer from " + p_182530_.capacity() + " bytes to " + p_182531_ + " bytes");
        }
        else
        {
            return MemoryUtil.memByteBuffer(i, p_182531_);
        }
    }
}
