package com.mojang.blaze3d.platform;

import ca.weblite.objc.NSObject;
import com.sun.jna.Pointer;
import java.util.Optional;
import org.lwjgl.glfw.GLFWNativeCocoa;

public class MacosUtil
{
    private static final int f_182515_ = 16384;

    public static void m_182517_(long p_182518_)
    {
        m_182521_(p_182518_).filter(MacosUtil::m_182519_).ifPresent(MacosUtil::m_182523_);
    }

    private static Optional<NSObject> m_182521_(long p_182522_)
    {
        long i = GLFWNativeCocoa.glfwGetCocoaWindow(p_182522_);
        return i != 0L ? Optional.of(new NSObject(new Pointer(i))) : Optional.empty();
    }

    private static boolean m_182519_(NSObject p_182520_)
    {
        return (((Number)p_182520_.sendRaw("styleMask", new Object[0])).longValue() & 16384L) == 16384L;
    }

    private static void m_182523_(NSObject p_182524_)
    {
        p_182524_.send("toggleFullScreen:", new Object[0]);
    }
}
