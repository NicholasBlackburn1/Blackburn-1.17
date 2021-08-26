package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMonitorCallback;
import org.lwjgl.glfw.GLFWMonitorCallbackI;

public class ScreenManager
{
    private final Long2ObjectMap<Monitor> monitors = new Long2ObjectOpenHashMap<>();
    private final MonitorCreator monitorCreator;

    public ScreenManager(MonitorCreator p_85265_)
    {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        this.monitorCreator = p_85265_;
        GLFW.glfwSetMonitorCallback(this::onMonitorChange);
        PointerBuffer pointerbuffer = GLFW.glfwGetMonitors();

        if (pointerbuffer != null)
        {
            for (int i = 0; i < pointerbuffer.limit(); ++i)
            {
                long j = pointerbuffer.get(i);
                this.monitors.put(j, p_85265_.createMonitor(j));
            }
        }
    }

    private void onMonitorChange(long pMonitorID, int p_85275_)
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);

        if (p_85275_ == 262145)
        {
            this.monitors.put(pMonitorID, this.monitorCreator.createMonitor(pMonitorID));
        }
        else if (p_85275_ == 262146)
        {
            this.monitors.remove(pMonitorID);
        }
    }

    @Nullable
    public Monitor getMonitor(long pMonitorID)
    {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        return this.monitors.get(pMonitorID);
    }

    @Nullable
    public Monitor findBestMonitor(Window pWindow)
    {
        long i = GLFW.glfwGetWindowMonitor(pWindow.getWindow());

        if (i != 0L)
        {
            return this.getMonitor(i);
        }
        else
        {
            int j = pWindow.getX();
            int k = j + pWindow.getScreenWidth();
            int l = pWindow.getY();
            int i1 = l + pWindow.getScreenHeight();
            int j1 = -1;
            Monitor monitor = null;

            for (Monitor monitor1 : this.monitors.values())
            {
                int k1 = monitor1.getX();
                int l1 = k1 + monitor1.getCurrentMode().getWidth();
                int i2 = monitor1.getY();
                int j2 = i2 + monitor1.getCurrentMode().getHeight();
                int k2 = clamp(j, k1, l1);
                int l2 = clamp(k, k1, l1);
                int i3 = clamp(l, i2, j2);
                int j3 = clamp(i1, i2, j2);
                int k3 = Math.max(0, l2 - k2);
                int l3 = Math.max(0, j3 - i3);
                int i4 = k3 * l3;

                if (i4 > j1)
                {
                    monitor = monitor1;
                    j1 = i4;
                }
            }

            return monitor;
        }
    }

    public static int clamp(int pMinValue, int pValue, int pMaxValue)
    {
        if (pMinValue < pValue)
        {
            return pValue;
        }
        else
        {
            return pMinValue > pMaxValue ? pMaxValue : pMinValue;
        }
    }

    public void shutdown()
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GLFWMonitorCallback glfwmonitorcallback = GLFW.glfwSetMonitorCallback((GLFWMonitorCallbackI)null);

        if (glfwmonitorcallback != null)
        {
            glfwmonitorcallback.free();
        }
    }
}
