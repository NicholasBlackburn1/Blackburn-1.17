package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.SilentInitException;
import net.minecraftforgeop.fml.loading.progress.EarlyProgressVisualization;
import net.optifine.Config;
import net.optifine.reflect.Reflector;
import net.optifine.util.TextureUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWImage.Buffer;
import org.lwjgl.opengl.GL;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public final class Window implements AutoCloseable
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final GLFWErrorCallback defaultErrorCallback = GLFWErrorCallback.create(this::defaultErrorCallback);
    private final WindowEventHandler eventHandler;
    private final ScreenManager screenManager;
    private final long window;
    private int windowedX;
    private int windowedY;
    private int windowedWidth;
    private int windowedHeight;
    private Optional<VideoMode> preferredFullscreenVideoMode;
    private boolean fullscreen;
    private boolean actuallyFullscreen;
    private int x;
    private int y;
    private int width;
    private int height;
    private int framebufferWidth;
    private int framebufferHeight;
    private int guiScaledWidth;
    private int guiScaledHeight;
    private double guiScale;
    private String errorSection = "";
    private boolean dirty;
    private int framerateLimit;
    private boolean vsync;
    private boolean closed;

    public Window(WindowEventHandler p_85372_, ScreenManager p_85373_, DisplayData p_85374_, @Nullable String p_85375_, String p_85376_)
    {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        this.screenManager = p_85373_;
        this.setBootErrorCallback();
        this.setErrorSection("Pre startup");
        this.eventHandler = p_85372_;
        Optional<VideoMode> optional = VideoMode.read(p_85375_);

        if (optional.isPresent())
        {
            this.preferredFullscreenVideoMode = optional;
        }
        else if (p_85374_.fullscreenWidth.isPresent() && p_85374_.fullscreenHeight.isPresent())
        {
            this.preferredFullscreenVideoMode = Optional.of(new VideoMode(p_85374_.fullscreenWidth.getAsInt(), p_85374_.fullscreenHeight.getAsInt(), 8, 8, 8, 60));
        }
        else
        {
            this.preferredFullscreenVideoMode = Optional.empty();
        }

        this.actuallyFullscreen = this.fullscreen = p_85374_.isFullscreen;
        Monitor monitor = p_85373_.getMonitor(GLFW.glfwGetPrimaryMonitor());
        this.windowedWidth = this.width = p_85374_.width > 0 ? p_85374_.width : 1;
        this.windowedHeight = this.height = p_85374_.height > 0 ? p_85374_.height : 1;
        GLFW.glfwDefaultWindowHints();

        if (Config.isAntialiasing())
        {
            GLFW.glfwWindowHint(135181, Config.getAntialiasingLevel());
        }

        GLFW.glfwWindowHint(139265, 196609);
        GLFW.glfwWindowHint(139275, 221185);
        GLFW.glfwWindowHint(139266, 3);
        GLFW.glfwWindowHint(139267, 2);
        GLFW.glfwWindowHint(139272, 204801);
        GLFW.glfwWindowHint(139270, 1);
        long i = 0L;

        if (Reflector.EarlyProgressVisualization_handOffWindow.exists())
        {
            Object object = Reflector.getFieldValue(Reflector.EarlyProgressVisualization_INSTANCE);
            i = Reflector.callLong(object, Reflector.EarlyProgressVisualization_handOffWindow, (IntSupplier)() ->
            {
                return this.width;
            }, (IntSupplier)() ->
            {
                return this.height;
            }, (Supplier<String>)() ->
            {
                return p_85376_;
            }, (LongSupplier)() ->
            {
                return this.fullscreen && monitor != null ? monitor.getMonitor() : 0L;
            });

            if (Config.isAntialiasing())
            {
                GLFW.glfwDestroyWindow(i);
                i = 0L;
            }
        }

        if (i != 0L)
        {
            this.window = i;
        }
        else
        {
            this.window = GLFW.glfwCreateWindow(this.width, this.height, p_85376_, this.fullscreen && monitor != null ? monitor.getMonitor() : 0L, 0L);
        }

        if (monitor != null)
        {
            VideoMode videomode = monitor.getPreferredVidMode(this.fullscreen ? this.preferredFullscreenVideoMode : Optional.empty());
            this.windowedX = this.x = monitor.getX() + videomode.getWidth() / 2 - this.width / 2;
            this.windowedY = this.y = monitor.getY() + videomode.getHeight() / 2 - this.height / 2;
        }
        else
        {
            int[] aint1 = new int[1];
            int[] aint = new int[1];
            GLFW.glfwGetWindowPos(this.window, aint1, aint);
            this.windowedX = this.x = aint1[0];
            this.windowedY = this.y = aint[0];
        }

        GLFW.glfwMakeContextCurrent(this.window);
        GL.createCapabilities();
        this.setMode();
        this.refreshFramebufferSize();
        GLFW.glfwSetFramebufferSizeCallback(this.window, this::onFramebufferResize);
        GLFW.glfwSetWindowPosCallback(this.window, this::onMove);
        GLFW.glfwSetWindowSizeCallback(this.window, this::onResize);
        GLFW.glfwSetWindowFocusCallback(this.window, this::onFocus);
        GLFW.glfwSetCursorEnterCallback(this.window, this::onEnter);
    }

    public int getRefreshRate()
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GLX._getRefreshRate(this);
    }

    public boolean shouldClose()
    {
        return GLX._shouldClose(this);
    }

    public static void checkGlfwError(BiConsumer<Integer, String> pGlfwErrorConsumer)
    {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        MemoryStack memorystack = MemoryStack.stackPush();

        try
        {
            PointerBuffer pointerbuffer = memorystack.mallocPointer(1);
            int i = GLFW.glfwGetError(pointerbuffer);

            if (i != 0)
            {
                long j = pointerbuffer.get();
                String s = j == 0L ? "" : MemoryUtil.memUTF8(j);
                pGlfwErrorConsumer.accept(i, s);
            }
        }
        catch (Throwable throwable11)
        {
            if (memorystack != null)
            {
                try
                {
                    memorystack.close();
                }
                catch (Throwable throwable1)
                {
                    throwable11.addSuppressed(throwable1);
                }
            }

            throw throwable11;
        }

        if (memorystack != null)
        {
            memorystack.close();
        }
    }

    public void setIcon(InputStream pIconStream16X, InputStream pIconStream32X)
    {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);

        try
        {
            MemoryStack memorystack = MemoryStack.stackPush();

            try
            {
                if (pIconStream16X == null)
                {
                    throw new FileNotFoundException("icons/icon_16x16.png");
                }

                if (pIconStream32X == null)
                {
                    throw new FileNotFoundException("icons/icon_32x32.png");
                }

                IntBuffer intbuffer = memorystack.mallocInt(1);
                IntBuffer intbuffer1 = memorystack.mallocInt(1);
                IntBuffer intbuffer2 = memorystack.mallocInt(1);
                Buffer buffer = GLFWImage.mallocStack(2, memorystack);
                ByteBuffer bytebuffer = this.readIconPixels(pIconStream16X, intbuffer, intbuffer1, intbuffer2);

                if (bytebuffer == null)
                {
                    throw new IllegalStateException("Could not load icon: " + STBImage.stbi_failure_reason());
                }

                buffer.position(0);
                buffer.width(intbuffer.get(0));
                buffer.height(intbuffer1.get(0));
                buffer.pixels(bytebuffer);
                ByteBuffer bytebuffer1 = this.readIconPixels(pIconStream32X, intbuffer, intbuffer1, intbuffer2);

                if (bytebuffer1 == null)
                {
                    throw new IllegalStateException("Could not load icon: " + STBImage.stbi_failure_reason());
                }

                buffer.position(1);
                buffer.width(intbuffer.get(0));
                buffer.height(intbuffer1.get(0));
                buffer.pixels(bytebuffer1);
                buffer.position(0);
                GLFW.glfwSetWindowIcon(this.window, buffer);
                STBImage.stbi_image_free(bytebuffer);
                STBImage.stbi_image_free(bytebuffer1);
            }
            catch (Throwable throwable11)
            {
                if (memorystack != null)
                {
                    try
                    {
                        memorystack.close();
                    }
                    catch (Throwable throwable1)
                    {
                        throwable11.addSuppressed(throwable1);
                    }
                }

                throw throwable11;
            }

            if (memorystack != null)
            {
                memorystack.close();
            }
        }
        catch (IOException ioexception1)
        {
            LOGGER.error("Couldn't set icon", (Throwable)ioexception1);
        }
    }

    @Nullable
    private ByteBuffer readIconPixels(InputStream pTextureStream, IntBuffer pX, IntBuffer pY, IntBuffer pChannelInFile) throws IOException
    {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        ByteBuffer bytebuffer = null;
        ByteBuffer bytebuffer1;

        try
        {
            bytebuffer = TextureUtil.readResource(pTextureStream);
            ((java.nio.Buffer)bytebuffer).rewind();
            bytebuffer1 = STBImage.stbi_load_from_memory(bytebuffer, pX, pY, pChannelInFile, 0);
        }
        finally
        {
            if (bytebuffer != null)
            {
                MemoryUtil.memFree(bytebuffer);
            }
        }

        return bytebuffer1;
    }

    public void setErrorSection(String pRenderPhase)
    {
        this.errorSection = pRenderPhase;

        if (pRenderPhase.equals("Startup"))
        {
            TextureUtils.registerTickableTextures();
        }
    }

    private void setBootErrorCallback()
    {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        GLFW.glfwSetErrorCallback(Window::bootCrash);
    }

    private static void bootCrash(int pError, long pDescription)
    {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        String s = "GLFW error " + pError + ": " + MemoryUtil.memUTF8(pDescription);
        TinyFileDialogs.tinyfd_messageBox("Minecraft", s + ".\n\nPlease make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions).", "ok", "error", false);
        throw new Window.WindowInitFailed(s);
    }

    public void defaultErrorCallback(int pError, long pDescription)
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        String s = MemoryUtil.memUTF8(pDescription);
        LOGGER.error("########## GL ERROR ##########");
        LOGGER.error("@ {}", (Object)this.errorSection);
        LOGGER.error("{}: {}", pError, s);
    }

    public void setDefaultErrorCallback()
    {
        GLFWErrorCallback glfwerrorcallback = GLFW.glfwSetErrorCallback(this.defaultErrorCallback);

        if (glfwerrorcallback != null)
        {
            glfwerrorcallback.free();
        }

        TextureUtils.registerResourceListener();
    }

    public void updateVsync(boolean pVsyncEnabled)
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        this.vsync = pVsyncEnabled;
        GLFW.glfwSwapInterval(pVsyncEnabled ? 1 : 0);
    }

    public void close()
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        this.closed = true;
        Callbacks.glfwFreeCallbacks(this.window);
        this.defaultErrorCallback.close();
        GLFW.glfwDestroyWindow(this.window);
        GLFW.glfwTerminate();
    }

    private void onMove(long pWindowPointer, int p_85390_, int pWindowX)
    {
        this.x = p_85390_;
        this.y = pWindowX;
    }

    private void onFramebufferResize(long pWindowPointer, int p_85417_, int pFramebufferWidth)
    {
        if (pWindowPointer == this.window)
        {
            int i = this.getWidth();
            int j = this.getHeight();

            if (p_85417_ != 0 && pFramebufferWidth != 0)
            {
                this.framebufferWidth = p_85417_;
                this.framebufferHeight = pFramebufferWidth;

                if (this.getWidth() != i || this.getHeight() != j)
                {
                    this.eventHandler.resizeDisplay();
                }
            }
        }
    }

    private void refreshFramebufferSize()
    {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        int[] aint = new int[1];
        int[] aint1 = new int[1];
        GLFW.glfwGetFramebufferSize(this.window, aint, aint1);
        this.framebufferWidth = aint[0] > 0 ? aint[0] : 1;
        this.framebufferHeight = aint1[0] > 0 ? aint1[0] : 1;

        if (this.framebufferHeight == 0 || this.framebufferWidth == 0)
        {
            EarlyProgressVisualization.INSTANCE.updateFBSize((w) ->
            {
                this.framebufferWidth = w;
            }, (h) ->
            {
                this.framebufferHeight = h;
            });
        }
    }

    private void onResize(long pWindowPointer, int p_85429_, int pWindowWidth)
    {
        this.width = p_85429_;
        this.height = pWindowWidth;
    }

    private void onFocus(long pWindowPointer, boolean p_85394_)
    {
        if (pWindowPointer == this.window)
        {
            this.eventHandler.setWindowActive(p_85394_);
        }
    }

    private void onEnter(long pWindow, boolean p_85421_)
    {
        if (p_85421_)
        {
            this.eventHandler.cursorEntered();
        }
    }

    public void setFramerateLimit(int pLimit)
    {
        this.framerateLimit = pLimit;
    }

    public int getFramerateLimit()
    {
        return this.framerateLimit;
    }

    public void updateDisplay()
    {
        RenderSystem.flipFrame(this.window);

        if (this.fullscreen != this.actuallyFullscreen)
        {
            this.actuallyFullscreen = this.fullscreen;
            this.updateFullscreen(this.vsync);
        }
    }

    public Optional<VideoMode> getPreferredFullscreenVideoMode()
    {
        return this.preferredFullscreenVideoMode;
    }

    public void setPreferredFullscreenVideoMode(Optional<VideoMode> pFullscreenMode)
    {
        boolean flag = !pFullscreenMode.equals(this.preferredFullscreenVideoMode);
        this.preferredFullscreenVideoMode = pFullscreenMode;

        if (flag)
        {
            this.dirty = true;
        }
    }

    public void changeFullscreenVideoMode()
    {
        if (this.fullscreen && this.dirty)
        {
            this.dirty = false;
            this.setMode();
            this.eventHandler.resizeDisplay();
        }
    }

    private void setMode()
    {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        boolean flag = GLFW.glfwGetWindowMonitor(this.window) != 0L;

        if (this.fullscreen)
        {
            Monitor monitor = this.screenManager.findBestMonitor(this);

            if (monitor == null)
            {
                LOGGER.warn("Failed to find suitable monitor for fullscreen mode");
                this.fullscreen = false;
            }
            else
            {
                if (Minecraft.ON_OSX)
                {
                    MacosUtil.m_182517_(this.window);
                }

                VideoMode videomode = monitor.getPreferredVidMode(this.preferredFullscreenVideoMode);

                if (!flag)
                {
                    this.windowedX = this.x;
                    this.windowedY = this.y;
                    this.windowedWidth = this.width;
                    this.windowedHeight = this.height;
                }

                this.x = 0;
                this.y = 0;
                this.width = videomode.getWidth();
                this.height = videomode.getHeight();
                GLFW.glfwSetWindowMonitor(this.window, monitor.getMonitor(), this.x, this.y, this.width, this.height, videomode.getRefreshRate());
            }
        }
        else
        {
            this.x = this.windowedX;
            this.y = this.windowedY;
            this.width = this.windowedWidth;
            this.height = this.windowedHeight;
            GLFW.glfwSetWindowMonitor(this.window, 0L, this.x, this.y, this.width, this.height, -1);
        }
    }

    public void toggleFullScreen()
    {
        this.fullscreen = !this.fullscreen;
    }

    public void setWindowed(int p_166448_, int p_166449_)
    {
        this.windowedWidth = p_166448_;
        this.windowedHeight = p_166449_;
        this.fullscreen = false;
        this.setMode();
    }

    private void updateFullscreen(boolean pVsyncEnabled)
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);

        try
        {
            this.setMode();
            this.eventHandler.resizeDisplay();
            this.updateVsync(pVsyncEnabled);
            this.updateDisplay();
        }
        catch (Exception exception)
        {
            LOGGER.error("Couldn't toggle fullscreen", (Throwable)exception);
        }
    }

    public int calculateScale(int pGuiScale, boolean pForceUnicode)
    {
        int i;

        for (i = 1; i != pGuiScale && i < this.framebufferWidth && i < this.framebufferHeight && this.framebufferWidth / (i + 1) >= 320 && this.framebufferHeight / (i + 1) >= 240; ++i)
        {
        }

        if (pForceUnicode && i % 2 != 0)
        {
            ++i;
        }

        return i;
    }

    public void setGuiScale(double pScaleFactor)
    {
        this.guiScale = pScaleFactor;
        int i = (int)((double)this.framebufferWidth / pScaleFactor);
        this.guiScaledWidth = (double)this.framebufferWidth / pScaleFactor > (double)i ? i + 1 : i;
        int j = (int)((double)this.framebufferHeight / pScaleFactor);
        this.guiScaledHeight = (double)this.framebufferHeight / pScaleFactor > (double)j ? j + 1 : j;
    }

    public void setTitle(String pTitle)
    {
        GLFW.glfwSetWindowTitle(this.window, pTitle);
    }

    public long getWindow()
    {
        return this.window;
    }

    public boolean isFullscreen()
    {
        return this.fullscreen;
    }

    public int getWidth()
    {
        return this.framebufferWidth;
    }

    public int getHeight()
    {
        return this.framebufferHeight;
    }

    public void setWidth(int p_166451_)
    {
        this.framebufferWidth = p_166451_;
    }

    public void setHeight(int p_166453_)
    {
        this.framebufferHeight = p_166453_;
    }

    public int getScreenWidth()
    {
        return this.width;
    }

    public int getScreenHeight()
    {
        return this.height;
    }

    public int getGuiScaledWidth()
    {
        return this.guiScaledWidth;
    }

    public int getGuiScaledHeight()
    {
        return this.guiScaledHeight;
    }

    public int getX()
    {
        return this.x;
    }

    public int getY()
    {
        return this.y;
    }

    public double getGuiScale()
    {
        return this.guiScale;
    }

    @Nullable
    public Monitor findBestMonitor()
    {
        return this.screenManager.findBestMonitor(this);
    }

    public void updateRawMouseInput(boolean pValue)
    {
        InputConstants.updateRawMouseInput(this.window, pValue);
    }

    public void resizeFramebuffer(int width, int height)
    {
        this.onFramebufferResize(this.window, width, height);
    }

    public boolean isClosed()
    {
        return this.closed;
    }

    public static class WindowInitFailed extends SilentInitException
    {
        WindowInitFailed(String p_85455_)
        {
            super(p_85455_);
        }
    }
}
