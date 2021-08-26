package net.minecraft.client.gui.font;

import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.RawGlyph;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.glyphs.MissingGlyph;

public class AllMissingGlyphProvider implements GlyphProvider
{
    @Nullable
    public RawGlyph getGlyph(int pCharacter)
    {
        return MissingGlyph.INSTANCE;
    }

    public IntSet getSupportedGlyphs()
    {
        return IntSets.EMPTY_SET;
    }
}
