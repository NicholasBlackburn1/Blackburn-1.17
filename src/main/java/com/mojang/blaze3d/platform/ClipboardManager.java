package com.mojang.blaze3d.platform;

import com.google.common.base.Charsets;
import java.nio.ByteBuffer;
import net.minecraft.util.StringDecomposer;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.system.MemoryUtil;

public class ClipboardManager
{
    public static final int FORMAT_UNAVAILABLE = 65545;
    private final ByteBuffer clipboardScratchBuffer = BufferUtils.createByteBuffer(8192);

    public String getClipboard(long pWindow, GLFWErrorCallbackI p_83997_)
    {
        GLFWErrorCallback glfwerrorcallback = GLFW.glfwSetErrorCallback(p_83997_);
        String s = GLFW.glfwGetClipboardString(pWindow);
        s = s != null ? StringDecomposer.filterBrokenSurrogates(s) : "";
        GLFWErrorCallback glfwerrorcallback1 = GLFW.glfwSetErrorCallback(glfwerrorcallback);

        if (glfwerrorcallback1 != null)
        {
            glfwerrorcallback1.free();
        }

        return s;
    }

    private static void m_83991_(long p_83992_, ByteBuffer p_83993_, byte[] p_83994_)
    {
        p_83993_.clear();
        p_83993_.put(p_83994_);
        p_83993_.put((byte)0);
        p_83993_.flip();
        GLFW.glfwSetClipboardString(p_83992_, p_83993_);
    }

    public void setClipboard(long pWindow, String p_83990_)
    {
        byte[] abyte = p_83990_.getBytes(Charsets.UTF_8);
        int i = abyte.length + 1;

        if (i < this.clipboardScratchBuffer.capacity())
        {
            m_83991_(pWindow, this.clipboardScratchBuffer, abyte);
        }
        else
        {
            ByteBuffer bytebuffer = MemoryUtil.memAlloc(i);

            try
            {
                m_83991_(pWindow, bytebuffer, abyte);
            }
            finally
            {
                MemoryUtil.memFree(bytebuffer);
            }
        }
    }
}
