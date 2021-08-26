package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.Closeable;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.optifine.Config;
import net.optifine.EmissiveTextures;
import net.optifine.shaders.ShadersTex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleTexture extends AbstractTexture
{
    static final Logger LOGGER = LogManager.getLogger();
    protected final ResourceLocation location;
    private ResourceManager resourceManager;
    public ResourceLocation locationEmissive;
    public boolean isEmissive;

    public SimpleTexture(ResourceLocation p_118133_)
    {
        this.location = p_118133_;
    }

    public void load(ResourceManager pManager) throws IOException
    {
        this.resourceManager = pManager;
        SimpleTexture.TextureImage simpletexture$textureimage = this.getTextureImage(pManager);
        simpletexture$textureimage.throwIfError();
        TextureMetadataSection texturemetadatasection = simpletexture$textureimage.getTextureMetadata();
        boolean flag;
        boolean flag1;

        if (texturemetadatasection != null)
        {
            flag = texturemetadatasection.isBlur();
            flag1 = texturemetadatasection.isClamp();
        }
        else
        {
            flag = false;
            flag1 = false;
        }

        NativeImage nativeimage = simpletexture$textureimage.getImage();

        if (!RenderSystem.isOnRenderThreadOrInit())
        {
            RenderSystem.recordRenderCall(() ->
            {
                this.doLoad(nativeimage, flag, flag1);
            });
        }
        else
        {
            this.doLoad(nativeimage, flag, flag1);
        }
    }

    private void doLoad(NativeImage pImage, boolean pBlur, boolean pClamp)
    {
        TextureUtil.prepareImage(this.getId(), 0, pImage.getWidth(), pImage.getHeight());
        pImage.upload(0, 0, 0, 0, 0, pImage.getWidth(), pImage.getHeight(), pBlur, pClamp, false, true);

        if (Config.isShaders())
        {
            ShadersTex.loadSimpleTextureNS(this.getId(), pImage, pBlur, pClamp, this.resourceManager, this.location, this.getMultiTexID());
        }

        if (EmissiveTextures.isActive())
        {
            EmissiveTextures.loadTexture(this.location, this);
        }
    }

    protected SimpleTexture.TextureImage getTextureImage(ResourceManager pResourceManager)
    {
        return SimpleTexture.TextureImage.load(pResourceManager, this.location);
    }

    protected static class TextureImage implements Closeable
    {
        @Nullable
        private final TextureMetadataSection metadata;
        @Nullable
        private final NativeImage image;
        @Nullable
        private final IOException exception;

        public TextureImage(IOException p_118153_)
        {
            this.exception = p_118153_;
            this.metadata = null;
            this.image = null;
        }

        public TextureImage(@Nullable TextureMetadataSection p_118150_, NativeImage p_118151_)
        {
            this.exception = null;
            this.metadata = p_118150_;
            this.image = p_118151_;
        }

        public static SimpleTexture.TextureImage load(ResourceManager pResourceManager, ResourceLocation pLocation)
        {
            try
            {
                Resource resource = pResourceManager.getResource(pLocation);
                SimpleTexture.TextureImage simpletexture$textureimage;

                try
                {
                    NativeImage nativeimage = NativeImage.read(resource.getInputStream());
                    TextureMetadataSection texturemetadatasection = null;

                    try
                    {
                        texturemetadatasection = resource.getMetadata(TextureMetadataSection.SERIALIZER);
                    }
                    catch (RuntimeException runtimeexception)
                    {
                        SimpleTexture.LOGGER.warn("Failed reading metadata of: {}", pLocation, runtimeexception);
                    }

                    simpletexture$textureimage = new SimpleTexture.TextureImage(texturemetadatasection, nativeimage);
                }
                catch (Throwable throwable11)
                {
                    if (resource != null)
                    {
                        try
                        {
                            resource.close();
                        }
                        catch (Throwable throwable1)
                        {
                            throwable11.addSuppressed(throwable1);
                        }
                    }

                    throw throwable11;
                }

                if (resource != null)
                {
                    resource.close();
                }

                return simpletexture$textureimage;
            }
            catch (IOException ioexception1)
            {
                return new SimpleTexture.TextureImage(ioexception1);
            }
        }

        @Nullable
        public TextureMetadataSection getTextureMetadata()
        {
            return this.metadata;
        }

        public NativeImage getImage() throws IOException
        {
            if (this.exception != null)
            {
                throw this.exception;
            }
            else
            {
                return this.image;
            }
        }

        public void close()
        {
            if (this.image != null)
            {
                this.image.close();
            }
        }

        public void throwIfError() throws IOException
        {
            if (this.exception != null)
            {
                throw this.exception;
            }
        }
    }
}
