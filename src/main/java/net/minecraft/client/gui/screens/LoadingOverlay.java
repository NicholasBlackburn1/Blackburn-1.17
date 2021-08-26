package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.optifine.Config;
import net.optifine.reflect.Reflector;
import net.optifine.render.GlBlendState;
import net.optifine.shaders.config.ShaderPackParser;
import net.optifine.util.PropertiesOrdered;

import net.blackburn.Const;

public class LoadingOverlay extends Overlay
{
    static final ResourceLocation MOJANG_STUDIOS_LOGO_LOCATION = new ResourceLocation("textures/gui/title/logo.png");
    private static final int LOGO_BACKGROUND_COLOR = FastColor.ARGB32.color(255, 239, 50, 61);
    private static final int LOGO_BACKGROUND_COLOR_DARK = FastColor.ARGB32.color(255, 0, 0, 0);
    private static final IntSupplier BRAND_BACKGROUND = () ->
    {
        return Minecraft.getInstance().options.darkMojangStudiosBackground ? LOGO_BACKGROUND_COLOR_DARK : LOGO_BACKGROUND_COLOR;
    };
    private static final int LOGO_SCALE = 240;
    private static final float LOGO_QUARTER_FLOAT = 60.0F;
    private static final int LOGO_QUARTER = 60;
    private static final int LOGO_HALF = 120;
    private static final float LOGO_OVERLAP = 0.0625F;
    private static final float SMOOTHING = 0.95F;
    public static final long FADE_OUT_TIME = 1000L;
    public static final long FADE_IN_TIME = 500L;
    private final Minecraft minecraft;
    private final ReloadInstance reload;
    private final Consumer<Optional<Throwable>> onFinish;
    private final boolean fadeIn;
    private float currentProgress;
    private long fadeOutStart = -1L;
    private long fadeInStart = -1L;
    private int colorBackground = BRAND_BACKGROUND.getAsInt();
    private int colorBar = BRAND_BACKGROUND.getAsInt();
    private int colorOutline = 16777215;
    private int colorProgress = 16777215;
    private GlBlendState blendState = null;
    private boolean fadeOut = false;

    public LoadingOverlay(Minecraft p_96172_, ReloadInstance p_96173_, Consumer<Optional<Throwable>> p_96174_, boolean p_96175_)
    {
        this.minecraft = p_96172_;
        this.reload = p_96173_;
        this.onFinish = p_96174_;
        this.fadeIn = false;
    }

    public static void registerTextures(Minecraft pMc)
    {
        pMc.getTextureManager().register(MOJANG_STUDIOS_LOGO_LOCATION, new LoadingOverlay.LogoTexture());
    }

    private static int replaceAlpha(int p_169325_, int p_169326_)
    {
        return p_169325_ & 16777215 | p_169326_ << 24;
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        int i = this.minecraft.getWindow().getGuiScaledWidth();
        int j = this.minecraft.getWindow().getGuiScaledHeight();
        long k = Util.getMillis();

        if (this.fadeIn && this.fadeInStart == -1L)
        {
            this.fadeInStart = k;
        }

        float f = this.fadeOutStart > -1L ? (float)(k - this.fadeOutStart) / 1000.0F : -1.0F;
        float f1 = this.fadeInStart > -1L ? (float)(k - this.fadeInStart) / 500.0F : -1.0F;
        float f2;

        if (f >= 1.0F)
        {
            this.fadeOut = true;

            if (this.minecraft.screen != null)
            {
                this.minecraft.screen.render(pMatrixStack, 0, 0, pPartialTicks);
            }

            int l = Mth.ceil((1.0F - Mth.clamp(f - 1.0F, 0.0F, 1.0F)) * 255.0F);
            fill(pMatrixStack, 0, 0, i, j, replaceAlpha(this.colorBackground, l));
            f2 = 1.0F - Mth.clamp(f - 1.0F, 0.0F, 1.0F);
        }
        else if (this.fadeIn)
        {
            if (this.minecraft.screen != null && f1 < 1.0F)
            {
                this.minecraft.screen.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
            }

            int l1 = Mth.ceil(Mth.clamp((double)f1, 0.15D, 1.0D) * 255.0D);
            fill(pMatrixStack, 0, 0, i, j, replaceAlpha(this.colorBackground, l1));
            f2 = Mth.clamp(f1, 0.0F, 1.0F);
        }
        else
        {
            int i2 = this.colorBackground;
            float f3 = (float)(i2 >> 16 & 255) / 255.0F;
            float f4 = (float)(i2 >> 8 & 255) / 255.0F;
            float f5 = (float)(i2 & 255) / 255.0F;
            GlStateManager._clearColor(f3, f4, f5, 1.0F);
            GlStateManager._clear(16384, Minecraft.ON_OSX);
            f2 = 1.0F;
        }
     

        int j2 = (int)((double)this.minecraft.getWindow().getGuiScaledWidth() * 0.5D);
        int k2 = (int)((double)this.minecraft.getWindow().getGuiScaledHeight() * 0.5D);
        double d1 = Math.min((double)this.minecraft.getWindow().getGuiScaledWidth() * 0.75D, (double)this.minecraft.getWindow().getGuiScaledHeight()) * 0.25D;
        int i1 = (int)(d1 * 0.5D);
        double d0 = d1 * 4.0D;
        int j1 = (int)(d0 * 0.5D);
        RenderSystem.setShaderTexture(0, MOJANG_STUDIOS_LOGO_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.blendEquation(32774);
        RenderSystem.blendFunc(770, 1);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f2);

        // this allows me to add custom logo
        blit(pMatrixStack,Const.px, Const.py, Const.pUOffset, Const.pVOffset, Const.pWidth, Const.pHight,Const.pTextureWidth,Const.pTextureHeight);
      
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        int k1 = (int)((double)this.minecraft.getWindow().getGuiScaledHeight() * 0.8325D);
        float f6 = this.reload.getActualProgress();
        this.currentProgress = Mth.clamp(this.currentProgress * 0.95F + f6 * 0.050000012F, 0.0F, 1.0F);
        Reflector.ClientModLoader_renderProgressText.call();

        if (f < 1.0F)
        {
            this.drawProgressBar(pMatrixStack, i / 2 - j1, k1 - 5, i / 2 + j1, k1 + 5, 1.0F - Mth.clamp(f, 0.0F, 1.0F));
        }

        if (f >= 2.0F)
        {
            this.minecraft.setOverlay((Overlay)null);
        }

        if (this.fadeOutStart == -1L && this.reload.isDone() && (!this.fadeIn || f1 >= 2.0F))
        {
            this.fadeOutStart = Util.getMillis();

            try
            {
                this.reload.checkExceptions();
                this.onFinish.accept(Optional.empty());
            }
            catch (Throwable throwable)
            {
                this.onFinish.accept(Optional.of(throwable));
            }

            if (this.minecraft.screen != null)
            {
                this.minecraft.screen.init(this.minecraft, this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());
            }
        }
    }

    private void drawProgressBar(PoseStack p_96183_, int p_96184_, int p_96185_, int p_96186_, int p_96187_, float p_96188_)
    {
        int i = Mth.ceil((float)(p_96186_ - p_96184_ - 2) * this.currentProgress);
        int j = Math.round(p_96188_ * 255.0F);

        if (this.colorBar != this.colorBackground)
        {
            int k = this.colorBar >> 16 & 255;
            int l = this.colorBar >> 8 & 255;
            int i1 = this.colorBar & 255;
            int j1 = FastColor.ARGB32.color(j, k, l, i1);
            fill(p_96183_, p_96184_, p_96185_, p_96186_, p_96187_, j1);
        }

        int j2 = this.colorProgress >> 16 & 255;
        int k2 = this.colorProgress >> 8 & 255;
        int l2 = this.colorProgress & 255;
        int i3 = FastColor.ARGB32.color(j, j2, k2, l2);
        fill(p_96183_, p_96184_ + 2, p_96185_ + 2, p_96184_ + i, p_96187_ - 2, i3);
        int k1 = this.colorOutline >> 16 & 255;
        int l1 = this.colorOutline >> 8 & 255;
        int i2 = this.colorOutline & 255;
        i3 = FastColor.ARGB32.color(j, k1, l1, i2);
        fill(p_96183_, p_96184_ + 1, p_96185_, p_96186_ - 1, p_96185_ + 1, i3);
        fill(p_96183_, p_96184_ + 1, p_96187_, p_96186_ - 1, p_96187_ - 1, i3);
        fill(p_96183_, p_96184_, p_96185_, p_96184_ + 1, p_96187_, i3);
        fill(p_96183_, p_96186_, p_96185_, p_96186_ - 1, p_96187_, i3);
    }

    public boolean isPauseScreen()
    {
        return true;
    }

    public void update()
    {
        this.colorBackground = BRAND_BACKGROUND.getAsInt();
        this.colorBar = BRAND_BACKGROUND.getAsInt();
        this.colorOutline = 16777215;
        this.colorProgress = 16777215;

        if (Config.isCustomColors())
        {
            try
            {
                String s = "optifine/color.properties";
                ResourceLocation resourcelocation = new ResourceLocation(s);

                if (!Config.hasResource(resourcelocation))
                {
                    return;
                }

                InputStream inputstream = Config.getResourceStream(resourcelocation);
                Config.dbg("Loading " + s);
                Properties properties = new PropertiesOrdered();
                properties.load(inputstream);
                inputstream.close();
                this.colorBackground = readColor(properties, "screen.loading", this.colorBackground);
                this.colorOutline = readColor(properties, "screen.loading.outline", this.colorOutline);
                this.colorBar = readColor(properties, "screen.loading.bar", this.colorBar);
                this.colorProgress = readColor(properties, "screen.loading.progress", this.colorProgress);
                this.blendState = ShaderPackParser.parseBlendState(properties.getProperty("screen.loading.blend"));
            }
            catch (Exception exception)
            {
                Config.warn("" + exception.getClass().getName() + ": " + exception.getMessage());
            }
        }
    }

    private static int readColor(Properties props, String name, int colDef)
    {
        String s = props.getProperty(name);

        if (s == null)
        {
            return colDef;
        }
        else
        {
            s = s.trim();
            int i = parseColor(s, colDef);

            if (i < 0)
            {
                Config.warn("Invalid color: " + name + " = " + s);
                return i;
            }
            else
            {
                Config.dbg(name + " = " + s);
                return i;
            }
        }
    }

    private static int parseColor(String str, int colDef)
    {
        if (str == null)
        {
            return colDef;
        }
        else
        {
            str = str.trim();

            try
            {
                return Integer.parseInt(str, 16) & 16777215;
            }
            catch (NumberFormatException numberformatexception)
            {
                return colDef;
            }
        }
    }

    public boolean isFadeOut()
    {
        return this.fadeOut;
    }

    static class LogoTexture extends SimpleTexture
    {
        public LogoTexture()
        {
            super(LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION);
        }

        protected SimpleTexture.TextureImage getTextureImage(ResourceManager pResourceManager)
        {
            Minecraft minecraft = Minecraft.getInstance();
            VanillaPackResources vanillapackresources = minecraft.getClientPackSource().getVanillaPack();

            try
            {
                InputStream inputstream = getLogoInputStream(pResourceManager, vanillapackresources);
                SimpleTexture.TextureImage simpletexture$textureimage;

                try
                {
                    simpletexture$textureimage = new SimpleTexture.TextureImage(new TextureMetadataSection(true, true), NativeImage.read(inputstream));
                }
                catch (Throwable throwable1)
                {
                    if (inputstream != null)
                    {
                        try
                        {
                            inputstream.close();
                        }
                        catch (Throwable throwable)
                        {
                            throwable1.addSuppressed(throwable);
                        }
                    }

                    throw throwable1;
                }

                if (inputstream != null)
                {
                    inputstream.close();
                }

                return simpletexture$textureimage;
            }
            catch (IOException ioexception1)
            {
                return new SimpleTexture.TextureImage(ioexception1);
            }
        }

        private static InputStream getLogoInputStream(ResourceManager resourceManager, VanillaPackResources vanillapack) throws IOException
        {
            return resourceManager.hasResource(LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION) ? resourceManager.getResource(LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION).getInputStream() : vanillapack.getResource(PackType.CLIENT_RESOURCES, LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION);
        }
    }
}
