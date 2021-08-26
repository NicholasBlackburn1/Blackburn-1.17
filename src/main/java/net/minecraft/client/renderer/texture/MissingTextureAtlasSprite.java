package net.minecraft.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.NativeImage;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.LazyLoadedValue;

public final class MissingTextureAtlasSprite extends TextureAtlasSprite
{
    private static final int MISSING_IMAGE_WIDTH = 16;
    private static final int MISSING_IMAGE_HEIGHT = 16;
    private static final String MISSING_TEXTURE_NAME = "missingno";
    private static final ResourceLocation MISSING_TEXTURE_LOCATION = new ResourceLocation("missingno");
    @Nullable
    private static DynamicTexture missingTexture;
    private static final LazyLoadedValue<NativeImage> MISSING_IMAGE_DATA = new LazyLoadedValue<>(() ->
    {
        NativeImage nativeimage = new NativeImage(16, 16, false);
        int i = -16777216;
        int j = -524040;

        for (int k = 0; k < 16; ++k)
        {
            for (int l = 0; l < 16; ++l)
            {
                if (k < 8 ^ l < 8)
                {
                    nativeimage.setPixelRGBA(l, k, -524040);
                }
                else
                {
                    nativeimage.setPixelRGBA(l, k, -16777216);
                }
            }
        }

        nativeimage.untrack();
        return nativeimage;
    });
    private static final TextureAtlasSprite.Info INFO = new TextureAtlasSprite.Info(MISSING_TEXTURE_LOCATION, 16, 16, new AnimationMetadataSection(ImmutableList.of(new AnimationFrame(0, -1)), 16, 16, 1, false));

    public MissingTextureAtlasSprite(TextureAtlas atlasTextureIn, TextureAtlasSprite.Info spriteInfoIn, int mipmapLevelIn, int atlasWidthIn, int atlasHeightIn, int xIn, int yIn)
    {
        super(atlasTextureIn, spriteInfoIn, mipmapLevelIn, atlasWidthIn, atlasHeightIn, xIn, yIn, makeMissingImage(spriteInfoIn.width(), spriteInfoIn.height()));
    }

    private MissingTextureAtlasSprite(TextureAtlas p_118065_, int p_118066_, int p_118067_, int p_118068_, int p_118069_, int p_118070_)
    {
        super(p_118065_, INFO, p_118066_, p_118067_, p_118068_, p_118069_, p_118070_, MISSING_IMAGE_DATA.get());
    }

    public static MissingTextureAtlasSprite newInstance(TextureAtlas pAtlasTexture, int pMipmapLevel, int pAtlasWidth, int pAtlasHeight, int pX, int pY)
    {
        return new MissingTextureAtlasSprite(pAtlasTexture, pMipmapLevel, pAtlasWidth, pAtlasHeight, pX, pY);
    }

    public static ResourceLocation getLocation()
    {
        return MISSING_TEXTURE_LOCATION;
    }

    public static TextureAtlasSprite.Info info()
    {
        return INFO;
    }

    public void close()
    {
        super.close();
    }

    public static DynamicTexture getTexture()
    {
        if (missingTexture == null)
        {
            missingTexture = new DynamicTexture(MISSING_IMAGE_DATA.get());
            Minecraft.getInstance().getTextureManager().register(MISSING_TEXTURE_LOCATION, missingTexture);
        }

        return missingTexture;
    }

    private static NativeImage makeMissingImage(int width, int height)
    {
        int i = width / 2;
        int j = height / 2;
        NativeImage nativeimage = new NativeImage(width, height, false);
        int k = -16777216;
        int l = -524040;

        for (int i1 = 0; i1 < height; ++i1)
        {
            for (int j1 = 0; j1 < width; ++j1)
            {
                if (i1 < j ^ j1 < i)
                {
                    nativeimage.setPixelRGBA(j1, i1, l);
                }
                else
                {
                    nativeimage.setPixelRGBA(j1, i1, k);
                }
            }
        }

        return nativeimage;
    }
}
