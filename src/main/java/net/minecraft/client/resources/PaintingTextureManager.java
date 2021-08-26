package net.minecraft.client.resources;

import java.util.stream.Stream;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.Motive;

public class PaintingTextureManager extends TextureAtlasHolder
{
    private static final ResourceLocation BACK_SPRITE_LOCATION = new ResourceLocation("back");

    public PaintingTextureManager(TextureManager p_118802_)
    {
        super(p_118802_, new ResourceLocation("textures/atlas/paintings.png"), "painting");
    }

    protected Stream<ResourceLocation> getResourcesToLoad()
    {
        return Stream.concat(Registry.MOTIVE.keySet().stream(), Stream.of(BACK_SPRITE_LOCATION));
    }

    public TextureAtlasSprite get(Motive pPaintingType)
    {
        return this.getSprite(Registry.MOTIVE.getKey(pPaintingType));
    }

    public TextureAtlasSprite getBackSprite()
    {
        return this.getSprite(BACK_SPRITE_LOCATION);
    }
}
