package net.minecraft.client.renderer.texture;

import java.util.Collection;

public class StitcherException extends RuntimeException
{
    private final Collection<TextureAtlasSprite.Info> allSprites;

    public StitcherException(TextureAtlasSprite.Info p_118256_, Collection<TextureAtlasSprite.Info> p_118257_)
    {
        super(String.format("Unable to fit: %s - size: %dx%d - Maybe try a lower resolution resourcepack?", p_118256_.name(), p_118256_.width(), p_118256_.height()));
        this.allSprites = p_118257_;
    }

    public Collection<TextureAtlasSprite.Info> getAllSprites()
    {
        return this.allSprites;
    }

    public StitcherException(TextureAtlasSprite.Info spriteInfoIn, Collection<TextureAtlasSprite.Info> spriteInfosIn, int atlasWidth, int atlasHeight, int maxWidth, int maxHeight)
    {
        super(String.format("Unable to fit: %s, size: %dx%d, atlas: %dx%d, atlasMax: %dx%d - Maybe try a lower resolution resourcepack?", "" + spriteInfoIn.name(), spriteInfoIn.width(), spriteInfoIn.height(), atlasWidth, atlasHeight, maxWidth, maxHeight));
        this.allSprites = spriteInfosIn;
    }
}
