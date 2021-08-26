package net.minecraft.client.renderer.texture;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

public class AtlasSet implements AutoCloseable
{
    private final Map<ResourceLocation, TextureAtlas> atlases;

    public AtlasSet(Collection<TextureAtlas> p_117970_)
    {
        this.atlases = p_117970_.stream().collect(Collectors.toMap(TextureAtlas::location, Function.identity()));
    }

    public TextureAtlas getAtlas(ResourceLocation pLocation)
    {
        return this.atlases.get(pLocation);
    }

    public TextureAtlasSprite getSprite(Material pMaterial)
    {
        return this.atlases.get(pMaterial.atlasLocation()).getSprite(pMaterial.texture());
    }

    public void close()
    {
        this.atlases.values().forEach(TextureAtlas::clearTextureData);
        this.atlases.clear();
    }
}
