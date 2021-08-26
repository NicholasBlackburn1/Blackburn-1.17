package net.minecraft.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.optifine.Config;
import net.optifine.reflect.Reflector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Screenshot
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    private int rowHeight;
    private final DataOutputStream outputStream;
    private final byte[] bytes;
    private final int width;
    private final int height;
    private File file;

    public static void grab(File pGameDirectory, RenderTarget pBuffer, Consumer<Component> pMessageConsumer)
    {
        grab(pGameDirectory, (String)null, pBuffer, pMessageConsumer);
    }

    public static void grab(File pGameDirectory, @Nullable String pWidth, RenderTarget pMessageConsumer, Consumer<Component> p_92301_)
    {
        if (!RenderSystem.isOnRenderThread())
        {
            RenderSystem.recordRenderCall(() ->
            {
                _grab(pGameDirectory, pWidth, pMessageConsumer, p_92301_);
            });
        }
        else
        {
            _grab(pGameDirectory, pWidth, pMessageConsumer, p_92301_);
        }
    }

    private static void _grab(File pGameDirectory, @Nullable String pScreenshotName, RenderTarget pBuffer, Consumer<Component> pMessageConsumer)
    {
        Minecraft minecraft = Config.getMinecraft();
        Window window = minecraft.getWindow();
        Options options = Config.getGameSettings();
        int i = window.getWidth();
        int j = window.getHeight();
        int k = options.guiScale;
        int l = window.calculateScale(minecraft.options.guiScale, minecraft.options.forceUnicodeFont);
        int i1 = Config.getScreenshotSize();
        boolean flag = GLX.isUsingFBOs() && i1 > 1;

        if (flag)
        {
            options.guiScale = l * i1;
            window.resizeFramebuffer(i * i1, j * i1);
            GlStateManager.clear(16640);
            minecraft.getMainRenderTarget().bindWrite(true);
            GlStateManager._enableTexture();
            RenderSystem.getModelViewStack().pushPose();
            minecraft.gameRenderer.render(minecraft.getFrameTime(), System.nanoTime(), true);
            RenderSystem.getModelViewStack().popPose();
            RenderSystem.applyModelViewMatrix();
        }

        NativeImage nativeimage = takeScreenshot(pBuffer);

        if (flag)
        {
            minecraft.getMainRenderTarget().unbindWrite();
            Config.getGameSettings().guiScale = k;
            window.resizeFramebuffer(i, j);
        }

        File file1 = new File(pGameDirectory, "screenshots");
        file1.mkdir();
        File file2;

        if (pScreenshotName == null)
        {
            file2 = getFile(file1);
        }
        else
        {
            file2 = new File(file1, pScreenshotName);
        }

        Object object = null;

        if (Reflector.ForgeHooksClient_onScreenshot.exists())
        {
            object = Reflector.call(Reflector.ForgeHooksClient_onScreenshot, nativeimage, file2);

            if (Reflector.callBoolean(object, Reflector.Event_isCanceled))
            {
                Component component = (Component)Reflector.call(object, Reflector.ScreenshotEvent_getCancelMessage);
                pMessageConsumer.accept(component);
                return;
            }

            file2 = (File)Reflector.call(object, Reflector.ScreenshotEvent_getScreenshotFile);
        }

        File file3 = file2;
        Object object1 = object;
        Util.ioPool().execute(() ->
        {
            try {
                nativeimage.writeToFile(file3);
                Component component1 = (new TextComponent(file3.getName())).withStyle(ChatFormatting.UNDERLINE).withStyle((p_313985_1_) -> {
                    return p_313985_1_.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file3.getAbsolutePath()));
                });

                if (object1 != null && Reflector.call(object1, Reflector.ScreenshotEvent_getResultMessage) != null)
                {
                    pMessageConsumer.accept((Component)Reflector.call(object1, Reflector.ScreenshotEvent_getResultMessage));
                }
                else {
                    pMessageConsumer.accept(new TranslatableComponent("screenshot.success", component1));
                }
            }
            catch (Exception exception1)
            {
                LOGGER.warn("Couldn't save screenshot", (Throwable)exception1);
                pMessageConsumer.accept(new TranslatableComponent("screenshot.failure", exception1.getMessage()));
            }
            finally {
                nativeimage.close();
            }
        });
    }

    public static NativeImage takeScreenshot(RenderTarget pFramebuffer)
    {
        if (!GLX.isUsingFBOs())
        {
            NativeImage nativeimage1 = new NativeImage(pFramebuffer.width, pFramebuffer.height, false);
            nativeimage1.downloadFromFramebuffer(true);
            nativeimage1.flipY();
            return nativeimage1;
        }
        else
        {
            int i = pFramebuffer.width;
            int j = pFramebuffer.height;
            NativeImage nativeimage = new NativeImage(i, j, false);
            RenderSystem.bindTexture(pFramebuffer.getColorTextureId());
            nativeimage.downloadTexture(0, true);
            nativeimage.flipY();
            return nativeimage;
        }
    }

    private static File getFile(File pGameDirectory)
    {
        String s = DATE_FORMAT.format(new Date());
        int i = 1;

        while (true)
        {
            File file1 = new File(pGameDirectory, s + (i == 1 ? "" : "_" + i) + ".png");

            if (!file1.exists())
            {
                return file1;
            }

            ++i;
        }
    }

    public Screenshot(File p_168601_, int p_168602_, int p_168603_, int p_168604_) throws IOException
    {
        this.width = p_168602_;
        this.height = p_168603_;
        this.rowHeight = p_168604_;
        File file1 = new File(p_168601_, "screenshots");
        file1.mkdir();
        DateFormat dateformat = DATE_FORMAT;
        String s = "huge_" + dateformat.format(new Date());

        for (int i = 1; (this.file = new File(file1, s + (i == 1 ? "" : "_" + i) + ".tga")).exists(); ++i)
        {
        }

        byte[] abyte = new byte[18];
        abyte[2] = 2;
        abyte[12] = (byte)(p_168602_ % 256);
        abyte[13] = (byte)(p_168602_ / 256);
        abyte[14] = (byte)(p_168603_ % 256);
        abyte[15] = (byte)(p_168603_ / 256);
        abyte[16] = 24;
        this.bytes = new byte[p_168602_ * p_168604_ * 3];
        this.outputStream = new DataOutputStream(new FileOutputStream(this.file));
        this.outputStream.write(abyte);
    }

    public void addRegion(ByteBuffer p_168610_, int p_168611_, int p_168612_, int p_168613_, int p_168614_)
    {
        int i = p_168613_;
        int j = p_168614_;

        if (p_168613_ > this.width - p_168611_)
        {
            i = this.width - p_168611_;
        }

        if (p_168614_ > this.height - p_168612_)
        {
            j = this.height - p_168612_;
        }

        this.rowHeight = j;

        for (int k = 0; k < j; ++k)
        {
            ((Buffer)p_168610_).position((p_168614_ - j) * p_168613_ * 3 + k * p_168613_ * 3);
            int l = (p_168611_ + k * this.width) * 3;
            p_168610_.get(this.bytes, l, i * 3);
        }
    }

    public void saveRow() throws IOException
    {
        this.outputStream.write(this.bytes, 0, this.width * 3 * this.rowHeight);
    }

    public File close() throws IOException
    {
        this.outputStream.close();
        return this.file;
    }
}
