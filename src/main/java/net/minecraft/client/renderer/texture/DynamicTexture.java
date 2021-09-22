package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import java.awt.image.BufferedImage;

import javax.annotation.Nullable;
import net.minecraft.server.packs.resources.ResourceManager;
import net.optifine.Config;
import net.optifine.shaders.ShadersTex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DynamicTexture extends AbstractTexture
{
    private static final Logger LOGGER = LogManager.getLogger();
    @Nullable
    private NativeImage pixels;
    private BufferedImage pixels2;

    public DynamicTexture(NativeImage bufferedImage)
    {
        this.pixels = bufferedImage;

        if (!RenderSystem.isOnRenderThread())
        {
            RenderSystem.recordRenderCall(() ->
            {
                TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
                this.upload();

                if (Config.isShaders())
                {
                    ShadersTex.initDynamicTextureNS(this);
                }
            });
        }
        else
        {
            TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());
            this.upload();

            if (Config.isShaders())
            {
                ShadersTex.initDynamicTextureNS(this);
            }
        }
    }

    public DynamicTexture(BufferedImage bufferedImage)
    {
        this.pixels2 = bufferedImage;

        if (!RenderSystem.isOnRenderThread())
        {
            RenderSystem.recordRenderCall(() ->
            {
                TextureUtil.prepareImage(this.getId(), this.pixels2.getWidth(), this.pixels2.getHeight());
                this.upload();

                if (Config.isShaders())
                {
                    ShadersTex.initDynamicTextureNS(this);
                }
            });
        }
        else
        {
            TextureUtil.prepareImage(this.getId(), this.pixels2.getWidth(), this.pixels2.getHeight());
            this.upload();

            if (Config.isShaders())
            {
                ShadersTex.initDynamicTextureNS(this);
            }
        }
    }

    public DynamicTexture(int p_117980_, int p_117981_, boolean p_117982_)
    {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        this.pixels = new NativeImage(p_117980_, p_117981_, p_117982_);
        TextureUtil.prepareImage(this.getId(), this.pixels.getWidth(), this.pixels.getHeight());

        if (Config.isShaders())
        {
            ShadersTex.initDynamicTextureNS(this);
        }
    }

    public void load(ResourceManager pManager)
    {
    }

    public void upload()
    {
        if (this.pixels != null)
        {
            this.bind();
            this.pixels.upload(0, 0, 0, false);
        }
        else
        {
            LOGGER.warn("Trying to upload disposed texture {}", (int)this.getId());
        }
    }

    @Nullable
    public NativeImage getPixels()
    {
        return this.pixels;
    }

    public void setPixels(NativeImage pNativeImage)
    {
        if (this.pixels != null)
        {
            this.pixels.close();
        }

        this.pixels = pNativeImage;
    }

    public void close()
    {
        if (this.pixels != null)
        {
            this.pixels.close();
            this.releaseId();
            this.pixels = null;
        }
    }
}
