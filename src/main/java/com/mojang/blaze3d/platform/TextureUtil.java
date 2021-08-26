package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.system.MemoryUtil;

@DontObfuscate
public class TextureUtil
{
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int MIN_MIPMAP_LEVEL = 0;
    private static final int DEFAULT_IMAGE_BUFFER_SIZE = 8192;

    public static int generateTextureId()
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);

        if (SharedConstants.IS_RUNNING_IN_IDE)
        {
            int[] aint = new int[ThreadLocalRandom.current().nextInt(15) + 1];
            GlStateManager.m_84305_(aint);
            int i = GlStateManager._genTexture();
            GlStateManager.m_84365_(aint);
            return i;
        }
        else
        {
            return GlStateManager._genTexture();
        }
    }

    public static void releaseTextureId(int pTextureId)
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GlStateManager._deleteTexture(pTextureId);
    }

    public static void prepareImage(int pTextureId, int pWidth, int pHeight)
    {
        prepareImage(NativeImage.InternalGlFormat.RGBA, pTextureId, 0, pWidth, pHeight);
    }

    public static void prepareImage(NativeImage.InternalGlFormat pTextureId, int pWidth, int pHeight, int p_85296_)
    {
        prepareImage(pTextureId, pWidth, 0, pHeight, p_85296_);
    }

    public static void prepareImage(int pTextureId, int pWidth, int pHeight, int p_85291_)
    {
        prepareImage(NativeImage.InternalGlFormat.RGBA, pTextureId, pWidth, pHeight, p_85291_);
    }

    public static void prepareImage(NativeImage.InternalGlFormat pTextureId, int pWidth, int pHeight, int p_85301_, int p_85302_)
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        bind(pWidth);

        if (pHeight >= 0)
        {
            GlStateManager._texParameter(3553, 33085, pHeight);
            GlStateManager._texParameter(3553, 33082, 0);
            GlStateManager._texParameter(3553, 33083, pHeight);
            GlStateManager._texParameter(3553, 34049, 0.0F);
        }

        for (int i = 0; i <= pHeight; ++i)
        {
            GlStateManager._texImage2D(3553, i, pTextureId.glFormat(), p_85301_ >> i, p_85302_ >> i, 0, 6408, 5121, (IntBuffer)null);
        }
    }

    private static void bind(int pTextureId)
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GlStateManager._bindTexture(pTextureId);
    }

    public static ByteBuffer readResource(InputStream pInputStream) throws IOException
    {
        ByteBuffer bytebuffer;

        if (pInputStream instanceof FileInputStream)
        {
            FileInputStream fileinputstream = (FileInputStream)pInputStream;
            FileChannel filechannel = fileinputstream.getChannel();
            bytebuffer = MemoryUtil.memAlloc((int)filechannel.size() + 1);

            while (filechannel.read(bytebuffer) != -1)
            {
            }
        }
        else
        {
            bytebuffer = MemoryUtil.memAlloc(8192);
            ReadableByteChannel readablebytechannel = Channels.newChannel(pInputStream);

            while (readablebytechannel.read(bytebuffer) != -1)
            {
                if (bytebuffer.remaining() == 0)
                {
                    bytebuffer = MemoryUtil.memRealloc(bytebuffer, bytebuffer.capacity() * 2);
                }
            }
        }

        return bytebuffer;
    }

    @Nullable
    public static String readResourceAsString(InputStream pInputStream)
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        ByteBuffer bytebuffer = null;

        try
        {
            bytebuffer = readResource(pInputStream);
            int i = bytebuffer.position();
            bytebuffer.rewind();
            return MemoryUtil.memASCII(bytebuffer, i);
        }
        catch (IOException ioexception)
        {
        }
        finally
        {
            if (bytebuffer != null)
            {
                MemoryUtil.memFree(bytebuffer);
            }
        }

        return null;
    }

    public static void writeAsPNG(String p_157135_, int p_157136_, int p_157137_, int p_157138_, int p_157139_)
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        bind(p_157136_);

        for (int i = 0; i <= p_157137_; ++i)
        {
            String s = p_157135_ + "_" + i + ".png";
            int j = p_157138_ >> i;
            int k = p_157139_ >> i;

            try
            {
                NativeImage nativeimage = new NativeImage(j, k, false);

                try
                {
                    nativeimage.downloadTexture(i, false);
                    nativeimage.writeToFile(s);
                    LOGGER.debug("Exported png to: {}", (Object)(new File(s)).getAbsolutePath());
                }
                catch (Throwable throwable1)
                {
                    try
                    {
                        nativeimage.close();
                    }
                    catch (Throwable throwable)
                    {
                        throwable1.addSuppressed(throwable);
                    }

                    throw throwable1;
                }

                nativeimage.close();
            }
            catch (IOException ioexception)
            {
                LOGGER.debug("Unable to write: ", (Throwable)ioexception);
            }
        }
    }

    public static void initTexture(IntBuffer pBuffer, int pWidth, int pHeight)
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glPixelStorei(GL11.GL_UNPACK_SWAP_BYTES, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_LSB_FIRST, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, pWidth, pHeight, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pBuffer);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
    }
}
