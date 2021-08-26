package net.minecraft.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.PngInfo;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.extensions.IForgeTextureAtlasSprite;
import net.optifine.Config;
import net.optifine.SmartAnimations;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersTextureType;
import net.optifine.texture.IColorBlender;
import net.optifine.util.CounterInt;
import net.optifine.util.TextureUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TextureAtlasSprite implements AutoCloseable, IForgeTextureAtlasSprite
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final TextureAtlas atlas;
    private final ResourceLocation name;
    final int width;
    final int height;
    protected final NativeImage[] mainImage;
    @Nullable
    private final TextureAtlasSprite.AnimatedTexture animatedTexture;
    private final int x;
    private final int y;
    private final float u0;
    private final float u1;
    private final float v0;
    private final float v1;
    private int indexInMap = -1;
    public float baseU;
    public float baseV;
    public int sheetWidth;
    public int sheetHeight;
    public int glSpriteTextureId = -1;
    public TextureAtlasSprite spriteSingle = null;
    public boolean isSpriteSingle = false;
    public static final String SUFFIX_SPRITE_SINGLE = ".sprite_single";
    public int mipmapLevels = 0;
    public TextureAtlasSprite spriteNormal = null;
    public TextureAtlasSprite spriteSpecular = null;
    public ShadersTextureType spriteShadersType = null;
    public TextureAtlasSprite spriteEmissive = null;
    public boolean isSpriteEmissive = false;
    private int animationIndex = -1;
    private boolean animationActive = false;
    private boolean usesParentAnimationTime = false;
    private boolean terrain;
    private boolean shaders;
    private boolean multiTexture;
    private ResourceManager resourceManager;
    private final int imageWidth;
    private final int imageHeight;
    private final AnimationMetadataSection animationMetadata;

    public TextureAtlasSprite(ResourceLocation name)
    {
        this.atlas = null;
        this.name = name;
        this.width = 0;
        this.height = 0;
        this.animationMetadata = null;
        this.mainImage = null;
        this.x = 0;
        this.y = 0;
        this.u0 = 0.0F;
        this.u1 = 0.0F;
        this.v0 = 0.0F;
        this.v1 = 0.0F;
        this.animatedTexture = null;
        this.imageWidth = 0;
        this.imageHeight = 0;
    }

    private TextureAtlasSprite(TextureAtlasSprite parent)
    {
        this.atlas = parent.atlas;
        this.name = parent.getName();
        this.width = parent.getWidth();
        this.height = parent.getHeight();
        this.imageWidth = parent.imageWidth;
        this.imageHeight = parent.imageHeight;
        this.animationMetadata = parent.animationMetadata;
        this.usesParentAnimationTime = true;
        this.mainImage = parent.mainImage;
        TextureAtlasSprite.Info textureatlassprite$info = new TextureAtlasSprite.Info(this.name, this.width, this.height, this.animationMetadata);
        this.animatedTexture = this.createTicker(textureatlassprite$info, this.imageWidth, this.imageHeight, parent.mipmapLevels);

        if (this.animatedTexture != null && this.animatedTexture.interpolationData != null && parent.animatedTexture != null && parent.animatedTexture.interpolationData != null)
        {
            this.animatedTexture.interpolationData.activeFrame = parent.animatedTexture.interpolationData.activeFrame;
        }

        this.x = 0;
        this.y = 0;
        this.u0 = 0.0F;
        this.u1 = 1.0F;
        this.v0 = 0.0F;
        this.v1 = 1.0F;
        this.indexInMap = parent.indexInMap;
        this.baseU = parent.baseU;
        this.baseV = parent.baseV;
        this.sheetWidth = parent.sheetWidth;
        this.sheetHeight = parent.sheetHeight;
        this.isSpriteSingle = true;
        this.mipmapLevels = parent.mipmapLevels;
        this.animationIndex = parent.animationIndex;
        this.animationActive = parent.animationActive;
    }

    protected TextureAtlasSprite(TextureAtlas p_118358_, TextureAtlasSprite.Info p_118359_, int p_118360_, int p_118361_, int p_118362_, int p_118363_, int p_118364_, NativeImage p_118365_)
    {
        this(p_118358_, p_118359_, p_118360_, p_118361_, p_118362_, p_118363_, p_118364_, p_118365_, (ShadersTextureType)null);
    }

    protected TextureAtlasSprite(TextureAtlas atlasTextureIn, TextureAtlasSprite.Info spriteInfoIn, int mipmapLevelsIn, int atlasWidthIn, int atlasHeightIn, int xIn, int yIn, NativeImage imageIn, ShadersTextureType spriteShadersTypeIn)
    {
        this.atlas = atlasTextureIn;
        this.width = spriteInfoIn.width;
        this.height = spriteInfoIn.height;
        this.name = spriteInfoIn.name;
        this.imageWidth = imageIn.getWidth();
        this.imageHeight = imageIn.getHeight();
        this.animationMetadata = spriteInfoIn.getSpriteAnimationMetadata();
        this.x = xIn;
        this.y = yIn;
        this.u0 = (float)xIn / (float)atlasWidthIn;
        this.u1 = (float)(xIn + this.width) / (float)atlasWidthIn;
        this.v0 = (float)yIn / (float)atlasHeightIn;
        this.v1 = (float)(yIn + this.height) / (float)atlasHeightIn;

        if (spriteInfoIn.scaleFactor > 1.0D)
        {
            int i = (int)Math.round((double)imageIn.getWidth() * spriteInfoIn.scaleFactor);
            NativeImage nativeimage = TextureUtils.scaleImage(imageIn, i);

            if (nativeimage != imageIn)
            {
                imageIn.close();
                imageIn = nativeimage;
            }
        }

        this.spriteShadersType = spriteShadersTypeIn;
        IColorBlender icolorblender = this.atlas.getShadersColorBlender(this.spriteShadersType);

        if (this.spriteShadersType == null && !spriteInfoIn.name().getPath().endsWith("_leaves"))
        {
            this.fixTransparentColor(imageIn);
        }

        NativeImage nativeimage1 = imageIn;
        this.animatedTexture = this.createTicker(spriteInfoIn, imageIn.getWidth(), imageIn.getHeight(), mipmapLevelsIn);

        try
        {
            try
            {
                this.mainImage = MipmapGenerator.generateMipmaps(imageIn, mipmapLevelsIn, icolorblender);
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport1 = CrashReport.forThrowable(throwable, "Generating mipmaps for frame");
                CrashReportCategory crashreportcategory1 = crashreport1.addCategory("Frame being iterated");
                crashreportcategory1.setDetail("First frame", () ->
                {
                    StringBuilder stringbuilder = new StringBuilder();

                    if (stringbuilder.length() > 0)
                    {
                        stringbuilder.append(", ");
                    }

                    stringbuilder.append(nativeimage1.getWidth()).append("x").append(nativeimage1.getHeight());
                    return stringbuilder.toString();
                });
                throw new ReportedException(crashreport1);
            }
        }
        catch (Throwable throwable1)
        {
            CrashReport crashreport = CrashReport.forThrowable(throwable1, "Applying mipmap");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Sprite being mipmapped");
            crashreportcategory.setDetail("Sprite name", this.name::toString);
            crashreportcategory.setDetail("Sprite size", () ->
            {
                return this.width + " x " + this.height;
            });
            crashreportcategory.setDetail("Sprite frames", () ->
            {
                return this.getFrameCount() + " frames";
            });
            crashreportcategory.setDetail("Mipmap levels", mipmapLevelsIn);
            throw new ReportedException(crashreport);
        }

        this.mipmapLevels = mipmapLevelsIn;
        this.baseU = Math.min(this.u0, this.u1);
        this.baseV = Math.min(this.v0, this.v1);
        this.sheetWidth = atlasWidthIn;
        this.sheetHeight = atlasHeightIn;
    }

    private int getFrameCount()
    {
        return this.animatedTexture != null ? this.animatedTexture.frames.size() : 1;
    }

    @Nullable
    private TextureAtlasSprite.AnimatedTexture createTicker(TextureAtlasSprite.Info p_174730_, int p_174731_, int p_174732_, int p_174733_)
    {
        AnimationMetadataSection animationmetadatasection = p_174730_.metadata;
        int i = p_174731_ / animationmetadatasection.getFrameWidth(p_174730_.width);
        int j = p_174732_ / animationmetadatasection.getFrameHeight(p_174730_.height);
        int k = i * j;
        List<TextureAtlasSprite.FrameInfo> list = Lists.newArrayList();
        animationmetadatasection.forEachFrame((indexIn, timeIn) ->
        {
            list.add(new TextureAtlasSprite.FrameInfo(indexIn, timeIn));
        });

        if (list.isEmpty())
        {
            for (int l = 0; l < k; ++l)
            {
                list.add(new TextureAtlasSprite.FrameInfo(l, animationmetadatasection.getDefaultFrameTime()));
            }
        }
        else
        {
            int i1 = 0;
            IntSet intset = new IntOpenHashSet();

            for (Iterator<TextureAtlasSprite.FrameInfo> iterator = list.iterator(); iterator.hasNext(); ++i1)
            {
                TextureAtlasSprite.FrameInfo textureatlassprite$frameinfo = iterator.next();
                boolean flag = true;

                if (textureatlassprite$frameinfo.time <= 0)
                {
                    LOGGER.warn("Invalid frame duration on sprite {} frame {}: {}", this.name, i1, textureatlassprite$frameinfo.time);
                    flag = false;
                }

                if (textureatlassprite$frameinfo.index < 0 || textureatlassprite$frameinfo.index >= k)
                {
                    LOGGER.warn("Invalid frame index on sprite {} frame {}: {}", this.name, i1, textureatlassprite$frameinfo.index);
                    flag = false;
                }

                if (flag)
                {
                    intset.add(textureatlassprite$frameinfo.index);
                }
                else
                {
                    iterator.remove();
                }
            }

            int[] aint = IntStream.range(0, k).filter((intIn) ->
            {
                return !intset.contains(intIn);
            }).toArray();

            if (aint.length > 0)
            {
                LOGGER.warn("Unused frames in sprite {}: {}", this.name, Arrays.toString(aint));
            }
        }

        if (list.size() <= 1)
        {
            return null;
        }
        else
        {
            TextureAtlasSprite.InterpolationData textureatlassprite$interpolationdata = animationmetadatasection.isInterpolatedFrames() ? new TextureAtlasSprite.InterpolationData(p_174730_, p_174733_) : null;
            return new TextureAtlasSprite.AnimatedTexture(ImmutableList.copyOf(list), i, textureatlassprite$interpolationdata);
        }
    }

    void m_118375_(int p_118376_, int p_118377_, NativeImage[] p_118378_)
    {
        boolean flag = false;
        boolean flag1 = this.isSpriteSingle;

        for (int i = 0; i < p_118378_.length && this.getWidth() >> i > 0 && this.getHeight() >> i > 0; ++i)
        {
            p_118378_[i].upload(i, this.x >> i, this.y >> i, p_118376_ >> i, p_118377_ >> i, this.width >> i, this.height >> i, p_118378_.length > 1, false);
        }
    }

    public int getX()
    {
        return this.x;
    }

    public int getY()
    {
        return this.y;
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getHeight()
    {
        return this.height;
    }

    public float getU0()
    {
        return this.u0;
    }

    public float getU1()
    {
        return this.u1;
    }

    public float getU(double pU)
    {
        float f = this.u1 - this.u0;
        return this.u0 + f * (float)pU / 16.0F;
    }

    public float getUOffset(float p_174728_)
    {
        float f = this.u1 - this.u0;
        return (p_174728_ - this.u0) / f * 16.0F;
    }

    public float getV0()
    {
        return this.v0;
    }

    public float getV1()
    {
        return this.v1;
    }

    public float getV(double pV)
    {
        float f = this.v1 - this.v0;
        return this.v0 + f * (float)pV / 16.0F;
    }

    public float getVOffset(float p_174742_)
    {
        float f = this.v1 - this.v0;
        return (p_174742_ - this.v0) / f * 16.0F;
    }

    public ResourceLocation getName()
    {
        return this.name;
    }

    public TextureAtlas atlas()
    {
        return this.atlas;
    }

    public IntStream getUniqueFrames()
    {
        return this.animatedTexture != null ? this.animatedTexture.getUniqueFrames() : IntStream.of(1);
    }

    public void close()
    {
        for (NativeImage nativeimage : this.mainImage)
        {
            if (nativeimage != null)
            {
                nativeimage.close();
            }
        }

        if (this.animatedTexture != null)
        {
            this.animatedTexture.close();
        }

        if (this.spriteSingle != null)
        {
        }

        if (this.spriteNormal != null)
        {
            this.spriteNormal.close();
        }

        if (this.spriteSpecular != null)
        {
            this.spriteSpecular.close();
        }
    }

    public String toString()
    {
        return "TextureAtlasSprite{name='" + this.name + "', frameCount=" + this.getFrameCount() + ", x=" + this.x + ", y=" + this.y + ", height=" + this.height + ", width=" + this.width + ", u0=" + this.u0 + ", u1=" + this.u1 + ", v0=" + this.v0 + ", v1=" + this.v1 + "}";
    }

    public boolean isTransparent(int pFrameIndex, int pPixelX, int pPixelY)
    {
        int i = pPixelX;
        int j = pPixelY;

        if (this.animatedTexture != null)
        {
            i = pPixelX + this.animatedTexture.getFrameX(pFrameIndex) * this.width;
            j = pPixelY + this.animatedTexture.getFrameY(pFrameIndex) * this.height;
        }

        return (this.mainImage[0].getPixelRGBA(i, j) >> 24 & 255) == 0;
    }

    public void uploadFirstFrame()
    {
        if (this.animatedTexture != null)
        {
            this.animatedTexture.uploadFirstFrame();
        }
        else
        {
            this.m_118375_(0, 0, this.mainImage);
        }
    }

    private float atlasSize()
    {
        float f = (float)this.width / (this.u1 - this.u0);
        float f1 = (float)this.height / (this.v1 - this.v0);
        return Math.max(f1, f);
    }

    public float uvShrinkRatio()
    {
        return 4.0F / this.atlasSize();
    }

    @Nullable
    public Tickable getAnimationTicker()
    {
        return this.animatedTexture;
    }

    public VertexConsumer wrap(VertexConsumer pBuffer)
    {
        if (this.getName() == TextureUtils.LOCATION_SPRITE_EMPTY)
        {
            MultiBufferSource.BufferSource multibuffersource$buffersource = pBuffer.getRenderTypeBuffer();

            if (multibuffersource$buffersource != null)
            {
                return multibuffersource$buffersource.getDummyBuffer();
            }
        }

        return new SpriteCoordinateExpander(pBuffer, this);
    }

    public int getIndexInMap()
    {
        return this.indexInMap;
    }

    public void updateIndexInMap(CounterInt counterInt)
    {
        if (this.indexInMap < 0)
        {
            if (this.atlas != null)
            {
                TextureAtlasSprite textureatlassprite = this.atlas.getRegisteredSprite(this.getName());

                if (textureatlassprite != null)
                {
                    this.indexInMap = textureatlassprite.getIndexInMap();
                }
            }

            if (this.indexInMap < 0)
            {
                this.indexInMap = counterInt.nextValue();
            }
        }
    }

    public int getAnimationIndex()
    {
        return this.animationIndex;
    }

    public void setAnimationIndex(int animationIndex)
    {
        this.animationIndex = animationIndex;

        if (this.spriteSingle != null)
        {
            this.spriteSingle.setAnimationIndex(animationIndex);
        }

        if (this.spriteNormal != null)
        {
            this.spriteNormal.setAnimationIndex(animationIndex);
        }

        if (this.spriteSpecular != null)
        {
            this.spriteSpecular.setAnimationIndex(animationIndex);
        }
    }

    public boolean isAnimationActive()
    {
        return this.animationActive;
    }

    private void fixTransparentColor(NativeImage ni)
    {
        int[] aint = new int[ni.getWidth() * ni.getHeight()];
        ni.getBufferRGBA().get(aint);
        this.fixTransparentColor(aint);
        ni.getBufferRGBA().put(aint);
    }

    private void fixTransparentColor(int[] data)
    {
        if (data != null)
        {
            long i = 0L;
            long j = 0L;
            long k = 0L;
            long l = 0L;

            for (int i1 = 0; i1 < data.length; ++i1)
            {
                int j1 = data[i1];
                int k1 = j1 >> 24 & 255;

                if (k1 >= 16)
                {
                    int l1 = j1 >> 16 & 255;
                    int i2 = j1 >> 8 & 255;
                    int j2 = j1 & 255;
                    i += (long)l1;
                    j += (long)i2;
                    k += (long)j2;
                    ++l;
                }
            }

            if (l > 0L)
            {
                int l2 = (int)(i / l);
                int i3 = (int)(j / l);
                int j3 = (int)(k / l);
                int k3 = l2 << 16 | i3 << 8 | j3;

                for (int l3 = 0; l3 < data.length; ++l3)
                {
                    int i4 = data[l3];
                    int k2 = i4 >> 24 & 255;

                    if (k2 <= 16)
                    {
                        data[l3] = k3;
                    }
                }
            }
        }
    }

    public double getSpriteU16(float atlasU)
    {
        float f = this.u1 - this.u0;
        return (double)((atlasU - this.u0) / f * 16.0F);
    }

    public double getSpriteV16(float atlasV)
    {
        float f = this.v1 - this.v0;
        return (double)((atlasV - this.v0) / f * 16.0F);
    }

    public void bindSpriteTexture()
    {
        if (this.glSpriteTextureId < 0)
        {
            this.glSpriteTextureId = TextureUtil.generateTextureId();
            TextureUtil.prepareImage(this.glSpriteTextureId, this.mipmapLevels, this.getWidth(), this.getHeight());
            boolean flag = this.atlas.isTextureBlend(this.spriteShadersType);

            if (flag)
            {
                TextureUtils.applyAnisotropicLevel();
            }
            else
            {
                GlStateManager._texParameter(3553, 34046, 1.0F);
                int i = this.mipmapLevels > 0 ? 9984 : 9728;
                GlStateManager._texParameter(3553, 10241, i);
                GlStateManager._texParameter(3553, 10240, 9728);
            }
        }

        TextureUtils.bindTexture(this.glSpriteTextureId);
    }

    public void deleteSpriteTexture()
    {
        if (this.glSpriteTextureId >= 0)
        {
            TextureUtil.releaseTextureId(this.glSpriteTextureId);
            this.glSpriteTextureId = -1;
        }
    }

    public float toSingleU(float u)
    {
        u = u - this.baseU;
        float f = (float)this.sheetWidth / (float)this.getWidth();
        return u * f;
    }

    public float toSingleV(float v)
    {
        v = v - this.baseV;
        float f = (float)this.sheetHeight / (float)this.getHeight();
        return v * f;
    }

    public NativeImage[] getMipmapImages()
    {
        return this.mainImage;
    }

    public AnimationMetadataSection getAnimationMetadata()
    {
        return this.animationMetadata;
    }

    public int getOriginX()
    {
        return this.x;
    }

    public int getOriginY()
    {
        return this.y;
    }

    public float getUnInterpolatedU(float u)
    {
        float f = this.u1 - this.u0;
        return (u - this.u0) / f * 16.0F;
    }

    public float getUnInterpolatedV(float v)
    {
        float f = this.v1 - this.v0;
        return (v - this.v0) / f * 16.0F;
    }

    public TextureAtlasSprite makeSpriteSingle()
    {
        TextureAtlasSprite textureatlassprite = new TextureAtlasSprite(this);
        textureatlassprite.isSpriteSingle = true;
        return textureatlassprite;
    }

    public TextureAtlasSprite makeSpriteShaders(ShadersTextureType type, int colDef, AnimationMetadataSection parentAnimation)
    {
        String s = type.getSuffix();
        ResourceLocation resourcelocation = new ResourceLocation(this.getName().getNamespace(), this.getName().getPath() + s);
        ResourceLocation resourcelocation1 = this.atlas.getResourceLocation(resourcelocation);
        TextureAtlasSprite textureatlassprite = null;

        if (this.resourceManager.hasResource(resourcelocation1))
        {
            try (Resource resource = this.resourceManager.getResource(resourcelocation1))
            {
                Resource resource1 = this.resourceManager.getResource(resourcelocation1);
                PngInfo pnginfo = new PngInfo(resourcelocation1.toString(), resource1.getInputStream());
                AnimationMetadataSection animationmetadatasection = resource.getMetadata(AnimationMetadataSection.SERIALIZER);

                if (animationmetadatasection == null)
                {
                    animationmetadatasection = AnimationMetadataSection.EMPTY;
                }

                Pair<Integer, Integer> pair = animationmetadatasection.getFrameSize(pnginfo.width, pnginfo.height);
                TextureAtlasSprite.Info textureatlassprite$info = new TextureAtlasSprite.Info(resourcelocation, pair.getFirst(), pair.getSecond(), animationmetadatasection);
                NativeImage nativeimage = NativeImage.read(resource.getInputStream());

                if (nativeimage.getWidth() != this.getWidth())
                {
                    NativeImage nativeimage1 = TextureUtils.scaleImage(nativeimage, this.getWidth());

                    if (nativeimage1 != nativeimage)
                    {
                        double d0 = 1.0D * (double)this.getWidth() / (double)nativeimage.getWidth();
                        nativeimage.close();
                        nativeimage = nativeimage1;
                        textureatlassprite$info = new TextureAtlasSprite.Info(resourcelocation, (int)((double)pair.getFirst().intValue() * d0), (int)((double)pair.getSecond().intValue() * d0), animationmetadatasection);
                    }
                }

                textureatlassprite = new TextureAtlasSprite(this.atlas, textureatlassprite$info, this.mipmapLevels, this.sheetWidth, this.sheetHeight, this.x, this.y, nativeimage, type);
            }
            catch (IOException ioexception)
            {
            }
        }

        if (textureatlassprite == null)
        {
            NativeImage nativeimage2 = new NativeImage(this.getWidth(), this.getHeight(), false);
            int i = TextureUtils.toAbgr(colDef);
            nativeimage2.fillRect(0, 0, nativeimage2.getWidth(), nativeimage2.getHeight(), i);
            TextureAtlasSprite.Info textureatlassprite$info1 = new TextureAtlasSprite.Info(resourcelocation, this.getWidth(), this.getHeight(), AnimationMetadataSection.EMPTY);
            textureatlassprite = new TextureAtlasSprite(this.atlas, textureatlassprite$info1, this.mipmapLevels, this.sheetWidth, this.sheetHeight, this.x, this.y, nativeimage2, type);
        }

        if (this.terrain && this.multiTexture && !this.isSpriteSingle)
        {
            textureatlassprite.spriteSingle = textureatlassprite.makeSpriteSingle();
        }

        textureatlassprite.usesParentAnimationTime = matchesTiming(textureatlassprite.animationMetadata, parentAnimation);
        return textureatlassprite;
    }

    public boolean isTerrain()
    {
        return this.terrain;
    }

    private void setTerrain(boolean terrainIn)
    {
        this.terrain = terrainIn;
        this.multiTexture = false;
        this.shaders = false;

        if (this.spriteSingle != null)
        {
            this.deleteSpriteTexture();
            this.spriteSingle = null;
        }

        if (this.spriteNormal != null)
        {
            if (this.spriteNormal.spriteSingle != null)
            {
                this.spriteNormal.deleteSpriteTexture();
            }

            this.spriteNormal.close();
            this.spriteNormal = null;
        }

        if (this.spriteSpecular != null)
        {
            if (this.spriteSpecular.spriteSingle != null)
            {
                this.spriteSpecular.deleteSpriteTexture();
            }

            this.spriteSpecular.close();
            this.spriteSpecular = null;
        }

        this.multiTexture = Config.isMultiTexture();
        this.shaders = Config.isShaders();

        if (this.terrain && this.multiTexture && !this.isSpriteSingle)
        {
            this.spriteSingle = this.makeSpriteSingle();
        }

        if (this.shaders && !this.isSpriteSingle)
        {
            if (this.spriteNormal == null && Shaders.configNormalMap)
            {
                this.spriteNormal = this.makeSpriteShaders(ShadersTextureType.NORMAL, -8421377, this.animationMetadata);
            }

            if (this.spriteSpecular == null && Shaders.configSpecularMap)
            {
                this.spriteSpecular = this.makeSpriteShaders(ShadersTextureType.SPECULAR, 0, this.animationMetadata);
            }
        }
    }

    private static boolean matchesTiming(AnimationMetadataSection am1, AnimationMetadataSection am2)
    {
        if (am1 == am2)
        {
            return true;
        }
        else if (am1 != null && am2 != null)
        {
            if (am1.getDefaultFrameTime() != am2.getDefaultFrameTime())
            {
                return false;
            }
            else if (am1.isInterpolatedFrames() != am2.isInterpolatedFrames())
            {
                return false;
            }
            else if (am1.getFrameCount() != am2.getFrameCount())
            {
                return false;
            }
            else
            {
                for (int i = 0; i < am1.getFrameCount(); ++i)
                {
                }

                return true;
            }
        }
        else
        {
            return false;
        }
    }

    public void update(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
        this.updateIndexInMap(this.atlas.getCounterIndexInMap());
        this.setTerrain(this.atlas.isTerrain());
    }

    public void updateAnimation()
    {
        if (this.animatedTexture != null)
        {
            this.animatedTexture.tick();
        }
    }

    public int getPixelRGBA(int frameIndex, int x, int y)
    {
        if (this.animatedTexture != null)
        {
            x += this.animatedTexture.getFrameX(frameIndex) * this.width;
            y += this.animatedTexture.getFrameY(frameIndex) * this.height;
        }

        return this.mainImage[0].getPixelRGBA(x, y);
    }

    class AnimatedTexture implements Tickable, AutoCloseable
    {
        int frame;
        int subFrame;
        final List<TextureAtlasSprite.FrameInfo> frames;
        private final int frameRowSize;
        @Nullable
        private final TextureAtlasSprite.InterpolationData interpolationData;
        private TextureAtlasSprite sprite = TextureAtlasSprite.this;

        AnimatedTexture(@Nullable List<TextureAtlasSprite.FrameInfo> p_174755_, int p_174756_, TextureAtlasSprite.InterpolationData p_174757_)
        {
            this.frames = p_174755_;
            this.frameRowSize = p_174756_;
            this.interpolationData = p_174757_;
        }

        int getFrameX(int p_174760_)
        {
            return p_174760_ % this.frameRowSize;
        }

        int getFrameY(int p_174765_)
        {
            return p_174765_ / this.frameRowSize;
        }

        private void uploadFrame(int p_174768_)
        {
            int i = this.getFrameX(p_174768_) * TextureAtlasSprite.this.width;
            int j = this.getFrameY(p_174768_) * TextureAtlasSprite.this.height;
            TextureAtlasSprite.this.m_118375_(i, j, TextureAtlasSprite.this.mainImage);
        }

        public void close()
        {
            if (this.interpolationData != null)
            {
                this.interpolationData.close();
            }
        }

        public void tick()
        {
            TextureAtlasSprite.this.animationActive = SmartAnimations.isActive() ? SmartAnimations.isSpriteRendered(this.sprite) : true;

            if (this.frames.size() <= 1)
            {
                TextureAtlasSprite.this.animationActive = false;
            }

            if (TextureAtlasSprite.this.spriteSingle != null && TextureAtlasSprite.this.spriteSingle.usesParentAnimationTime && TextureAtlasSprite.this.spriteSingle.animatedTexture != null)
            {
                TextureAtlasSprite.this.spriteSingle.animatedTexture.frame = this.frame;
                TextureAtlasSprite.this.spriteSingle.animatedTexture.subFrame = this.subFrame;
            }

            if (TextureAtlasSprite.this.spriteNormal != null && TextureAtlasSprite.this.spriteNormal.usesParentAnimationTime && TextureAtlasSprite.this.spriteNormal.animatedTexture != null)
            {
                TextureAtlasSprite.this.spriteNormal.animatedTexture.frame = this.frame;
                TextureAtlasSprite.this.spriteNormal.animatedTexture.subFrame = this.subFrame;
            }

            if (TextureAtlasSprite.this.spriteSpecular != null && TextureAtlasSprite.this.spriteSpecular.usesParentAnimationTime && TextureAtlasSprite.this.spriteSpecular.animatedTexture != null)
            {
                TextureAtlasSprite.this.spriteSpecular.animatedTexture.frame = this.frame;
                TextureAtlasSprite.this.spriteSpecular.animatedTexture.subFrame = this.subFrame;
            }

            ++this.subFrame;
            TextureAtlasSprite.FrameInfo textureatlassprite$frameinfo = this.frames.get(this.frame);

            if (this.subFrame >= textureatlassprite$frameinfo.time)
            {
                int i = textureatlassprite$frameinfo.index;
                this.frame = (this.frame + 1) % this.frames.size();
                this.subFrame = 0;
                int j = (this.frames.get(this.frame)).index;

                if (!TextureAtlasSprite.this.animationActive)
                {
                    return;
                }

                if (i != j)
                {
                    this.uploadFrame(j);
                }
            }
            else if (this.interpolationData != null)
            {
                if (!TextureAtlasSprite.this.animationActive)
                {
                    return;
                }

                if (!RenderSystem.isOnRenderThread())
                {
                    RenderSystem.recordRenderCall(() ->
                    {
                        this.interpolationData.uploadInterpolatedFrame(this);
                    });
                }
                else
                {
                    this.interpolationData.uploadInterpolatedFrame(this);
                }
            }
        }

        public void uploadFirstFrame()
        {
            this.uploadFrame((this.frames.get(0)).index);
        }

        public IntStream getUniqueFrames()
        {
            return this.frames.stream().mapToInt((infoIn) ->
            {
                return infoIn.index;
            }).distinct();
        }

        public TextureAtlasSprite getSprite()
        {
            return this.sprite;
        }

        public String toString()
        {
            return "animation:" + TextureAtlasSprite.this.toString();
        }
    }

    static class FrameInfo
    {
        final int index;
        final int time;

        FrameInfo(int p_174774_, int p_174775_)
        {
            this.index = p_174774_;
            this.time = p_174775_;
        }
    }

    public static final class Info
    {
        final ResourceLocation name;
        int width;
        int height;
        final AnimationMetadataSection metadata;
        private double scaleFactor = 1.0D;

        public Info(ResourceLocation p_118427_, int p_118428_, int p_118429_, AnimationMetadataSection p_118430_)
        {
            this.name = p_118427_;
            this.width = p_118428_;
            this.height = p_118429_;
            this.metadata = p_118430_;
        }

        public ResourceLocation name()
        {
            return this.name;
        }

        public int width()
        {
            return this.width;
        }

        public int height()
        {
            return this.height;
        }

        public void setSpriteWidth(int spriteWidth)
        {
            this.width = spriteWidth;
        }

        public void setSpriteHeight(int spriteHeight)
        {
            this.height = spriteHeight;
        }

        public AnimationMetadataSection getSpriteAnimationMetadata()
        {
            return this.metadata;
        }

        public double getScaleFactor()
        {
            return this.scaleFactor;
        }

        public void setScaleFactor(double scaleFactor)
        {
            this.scaleFactor = scaleFactor;
        }

        public String toString()
        {
            return "" + this.name + ", width: " + this.width + ", height: " + this.height + ", frames: " + this.metadata.getFrameCount() + ", scale: " + this.scaleFactor;
        }
    }

    final class InterpolationData implements AutoCloseable
    {
        private NativeImage[] activeFrame;

        public String toString()
        {
            return "interpolation:" + TextureAtlasSprite.this.toString();
        }

        InterpolationData(TextureAtlasSprite.Info p_118446_, int p_118447_)
        {
            this.activeFrame = new NativeImage[p_118447_ + 1];

            for (int i = 0; i < this.activeFrame.length; ++i)
            {
                int j = p_118446_.width >> i;
                int k = p_118446_.height >> i;

                if (this.activeFrame[i] == null)
                {
                    this.activeFrame[i] = new NativeImage(j, k, false);
                }
            }
        }

        void uploadInterpolatedFrame(TextureAtlasSprite.AnimatedTexture p_174777_)
        {
            TextureAtlasSprite.FrameInfo textureatlassprite$frameinfo = p_174777_.frames.get(p_174777_.frame);
            double d0 = 1.0D - (double)p_174777_.subFrame / (double)textureatlassprite$frameinfo.time;
            int i = textureatlassprite$frameinfo.index;
            int j = (p_174777_.frames.get((p_174777_.frame + 1) % p_174777_.frames.size())).index;

            if (i != j)
            {
                if (!TextureAtlasSprite.this.isSpriteSingle)
                {
                    for (int k = 0; k < this.activeFrame.length; ++k)
                    {
                        int l = TextureAtlasSprite.this.width >> k;
                        int i1 = TextureAtlasSprite.this.height >> k;

                        for (int j1 = 0; j1 < i1; ++j1)
                        {
                            for (int k1 = 0; k1 < l; ++k1)
                            {
                                int l1 = this.getPixel(p_174777_, i, k, k1, j1);
                                int i2 = this.getPixel(p_174777_, j, k, k1, j1);
                                int j2 = this.mix(d0, l1 >> 16 & 255, i2 >> 16 & 255);
                                int k2 = this.mix(d0, l1 >> 8 & 255, i2 >> 8 & 255);
                                int l2 = this.mix(d0, l1 & 255, i2 & 255);
                                this.activeFrame[k].setPixelRGBA(k1, j1, l1 & -16777216 | j2 << 16 | k2 << 8 | l2);
                            }
                        }
                    }
                }

                TextureAtlasSprite.this.m_118375_(0, 0, this.activeFrame);
            }
        }

        private int getPixel(TextureAtlasSprite.AnimatedTexture p_174779_, int p_174780_, int p_174781_, int p_174782_, int p_174783_)
        {
            return TextureAtlasSprite.this.mainImage[p_174781_].getPixelRGBA(p_174782_ + (p_174779_.getFrameX(p_174780_) * TextureAtlasSprite.this.width >> p_174781_), p_174783_ + (p_174779_.getFrameY(p_174780_) * TextureAtlasSprite.this.height >> p_174781_));
        }

        private int mix(double pRatio, int p_118456_, int pVal1)
        {
            return (int)(pRatio * (double)p_118456_ + (1.0D - pRatio) * (double)pVal1);
        }

        public void close()
        {
            for (NativeImage nativeimage : this.activeFrame)
            {
                if (nativeimage != null)
                {
                    nativeimage.close();
                }
            }
        }
    }
}
