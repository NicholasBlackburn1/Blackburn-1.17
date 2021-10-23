package net.minecraftforgeop.client.extensions;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public interface IForgeTextureAtlasSprite
{
default boolean hasCustomLoader(ResourceManager manager, ResourceLocation location)
    {
        return false;
    }

default boolean load(ResourceManager manager, ResourceLocation location, Function<ResourceLocation, TextureAtlasSprite> textureGetter)
    {
        return true;
    }

default Collection<ResourceLocation> getDependencies()
    {
        return ImmutableList.of();
    }
}
