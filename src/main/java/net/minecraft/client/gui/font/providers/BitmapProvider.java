package net.minecraft.client.gui.font.providers;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.RawGlyph;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.optifine.util.FontUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BitmapProvider implements GlyphProvider
{
    static final Logger LOGGER = LogManager.getLogger();
    private final NativeImage image;
    private final Int2ObjectMap<BitmapProvider.Glyph> glyphs;
    private boolean blend = false;
    private float widthSpace = -1.0F;

    BitmapProvider(NativeImage p_95333_, Int2ObjectMap<BitmapProvider.Glyph> p_95334_)
    {
        this.image = p_95333_;
        this.glyphs = p_95334_;
    }

    public void close()
    {
        this.image.close();
    }

    @Nullable
    public RawGlyph getGlyph(int pCharacter)
    {
        return this.glyphs.get(pCharacter);
    }

    public IntSet getSupportedGlyphs()
    {
        return IntSets.unmodifiable(this.glyphs.keySet());
    }

    public boolean isBlend()
    {
        return this.blend;
    }

    public float getWidthSpace()
    {
        return this.widthSpace;
    }

    public static class Builder implements GlyphProviderBuilder
    {
        private ResourceLocation texture;
        private final List<int[]> chars;
        private final int height;
        private final int ascent;

        public Builder(ResourceLocation p_95349_, int p_95350_, int p_95351_, List<int[]> p_95352_)
        {
            this.texture = new ResourceLocation(p_95349_.getNamespace(), "textures/" + p_95349_.getPath());
            this.texture = FontUtils.getHdFontLocation(this.texture);
            this.chars = p_95352_;
            this.height = p_95350_;
            this.ascent = p_95351_;
        }

        public static BitmapProvider.Builder fromJson(JsonObject pJson)
        {
            int i = GsonHelper.getAsInt(pJson, "height", 8);
            int j = GsonHelper.getAsInt(pJson, "ascent");

            if (j > i)
            {
                throw new JsonParseException("Ascent " + j + " higher than height " + i);
            }
            else
            {
                List<int[]> list = Lists.newArrayList();
                JsonArray jsonarray = GsonHelper.getAsJsonArray(pJson, "chars");

                for (int k = 0; k < jsonarray.size(); ++k)
                {
                    String s = GsonHelper.convertToString(jsonarray.get(k), "chars[" + k + "]");
                    int[] aint = s.codePoints().toArray();

                    if (k > 0)
                    {
                        int l = ((int[])((int[])list.get(0))).length;

                        if (aint.length != l)
                        {
                            throw new JsonParseException("Elements of chars have to be the same length (found: " + aint.length + ", expected: " + l + "), pad with space or \\u0000");
                        }
                    }

                    list.add(aint);
                }

                if (!list.isEmpty() && ((int[])((int[])list.get(0))).length != 0)
                {
                    return new BitmapProvider.Builder(new ResourceLocation(GsonHelper.getAsString(pJson, "file")), i, j, list);
                }
                else
                {
                    throw new JsonParseException("Expected to find data in chars, found none.");
                }
            }
        }

        @Nullable
        public GlyphProvider create(ResourceManager pResourceManager)
        {
            try
            {
                Resource resource = pResourceManager.getResource(this.texture);
                BitmapProvider bitmapprovider;

                try
                {
                    NativeImage nativeimage = NativeImage.read(NativeImage.Format.RGBA, resource.getInputStream());
                    int i = nativeimage.getWidth();
                    int j = nativeimage.getHeight();
                    int k = i / ((int[])((int[])this.chars.get(0))).length;
                    int l = j / this.chars.size();
                    float f = (float)this.height / (float)l;
                    Int2ObjectMap<BitmapProvider.Glyph> int2objectmap = new Int2ObjectOpenHashMap<>();
                    Properties properties = FontUtils.readFontProperties(this.texture);
                    Int2ObjectMap<Float> int2objectmap1 = FontUtils.readCustomCharWidths(properties);
                    Float f1 = int2objectmap1.get(32);
                    boolean flag = FontUtils.readBoolean(properties, "blend", false);
                    float f2 = FontUtils.readFloat(properties, "offsetBold", -1.0F);

                    if (f2 < 0.0F)
                    {
                        f2 = k > 8 ? 0.5F : 1.0F;
                    }

                    for (int i1 = 0; i1 < this.chars.size(); ++i1)
                    {
                        int j1 = 0;

                        for (int k1 : this.chars.get(i1))
                        {
                            int l1 = j1++;

                            if (k1 != 0 && k1 != 32)
                            {
                                float f3 = (float)this.getActualGlyphWidth(nativeimage, k, l, l1, i1);
                                Float f4 = int2objectmap1.get(k1);

                                if (f4 != null)
                                {
                                    f3 = f4 * ((float)k / 8.0F);
                                }

                                BitmapProvider.Glyph bitmapprovider$glyph = int2objectmap.put(k1, new BitmapProvider.Glyph(f, nativeimage, l1 * k, i1 * l, k, l, (int)(0.5D + (double)(f3 * f)) + 1, this.ascent));

                                if (bitmapprovider$glyph != null)
                                {
                                    BitmapProvider.LOGGER.warn("Codepoint '{}' declared multiple times in {}", Integer.toHexString(k1), this.texture);
                                }

                                BitmapProvider.Glyph bitmapprovider$glyph1 = int2objectmap.get(k1);
                                bitmapprovider$glyph1.offsetBold = f2;
                            }
                        }
                    }

                    bitmapprovider = new BitmapProvider(nativeimage, int2objectmap);
                    bitmapprovider.blend = flag;

                    if (f1 != null)
                    {
                        bitmapprovider.widthSpace = f1;
                    }
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

                return bitmapprovider;
            }
            catch (IOException ioexception1)
            {
                throw new RuntimeException(ioexception1.getMessage());
            }
        }

        private int getActualGlyphWidth(NativeImage pNativeImage, int pCharWidth, int pCharHeightInsp, int pColumn, int pRow)
        {
            int i;

            for (i = pCharWidth - 1; i >= 0; --i)
            {
                int j = pColumn * pCharWidth + i;

                for (int k = 0; k < pCharHeightInsp; ++k)
                {
                    int l = pRow * pCharHeightInsp + k;

                    if ((pNativeImage.getLuminanceOrAlpha(j, l) & 255) > 16)
                    {
                        return i + 1;
                    }
                }
            }

            return i + 1;
        }
    }

    static final class Glyph implements RawGlyph
    {
        private final float scale;
        private final NativeImage image;
        private final int offsetX;
        private final int offsetY;
        private final int width;
        private final int height;
        private final int advance;
        private final int ascent;
        private float offsetBold = 1.0F;

        Glyph(float p_95372_, NativeImage p_95373_, int p_95374_, int p_95375_, int p_95376_, int p_95377_, int p_95378_, int p_95379_)
        {
            this.scale = p_95372_;
            this.image = p_95373_;
            this.offsetX = p_95374_;
            this.offsetY = p_95375_;
            this.width = p_95376_;
            this.height = p_95377_;
            this.advance = p_95378_;
            this.ascent = p_95379_;
        }

        public float getOversample()
        {
            return 1.0F / this.scale;
        }

        public int getPixelWidth()
        {
            return this.width;
        }

        public int getPixelHeight()
        {
            return this.height;
        }

        public float getAdvance()
        {
            return (float)this.advance;
        }

        public float getBearingY()
        {
            return RawGlyph.super.getBearingY() + 7.0F - (float)this.ascent;
        }

        public void upload(int pXOffset, int pYOffset)
        {
            this.image.upload(0, pXOffset, pYOffset, this.offsetX, this.offsetY, this.width, this.height, false, false);
        }

        public boolean isColored()
        {
            return this.image.format().components() > 1;
        }

        public float getBoldOffset()
        {
            return this.offsetBold;
        }
    }
}
