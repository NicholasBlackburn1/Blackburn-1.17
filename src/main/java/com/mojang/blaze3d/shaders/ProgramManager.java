package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProgramManager
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static void glUseProgram(int p_85579_)
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager._glUseProgram(p_85579_);
    }

    public static void releaseProgram(Shader p_166622_)
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        p_166622_.getFragmentProgram().close();
        p_166622_.getVertexProgram().close();
        GlStateManager.glDeleteProgram(p_166622_.getId());
    }

    public static int createProgram() throws IOException
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        int i = GlStateManager.glCreateProgram();

        if (i <= 0)
        {
            throw new IOException("Could not create shader program (returned program ID " + i + ")");
        }
        else
        {
            return i;
        }
    }

    public static void linkShader(Shader p_166624_)
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        p_166624_.attachToProgram();
        GlStateManager.glLinkProgram(p_166624_.getId());
        int i = GlStateManager.glGetProgrami(p_166624_.getId(), 35714);

        if (i == 0)
        {
            LOGGER.warn("Error encountered when linking program containing VS {} and FS {}. Log output:", p_166624_.getVertexProgram().getName(), p_166624_.getFragmentProgram().getName());
            LOGGER.warn(GlStateManager.glGetProgramInfoLog(p_166624_.getId(), 32768));
        }
    }
}
