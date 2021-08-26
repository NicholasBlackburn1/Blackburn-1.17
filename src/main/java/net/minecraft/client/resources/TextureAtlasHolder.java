package net.minecraft.client.resources;

import java.util.stream.Stream;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public abstract class TextureAtlasHolder extends SimplePreparableReloadListener<TextureAtlas.Preparations> implements AutoCloseable
{
    private final TextureAtlas textureAtlas;
    private final String prefix;

    public TextureAtlasHolder(TextureManager p_118887_, ResourceLocation p_118888_, String p_118889_)
    {
        this.prefix = p_118889_;
        this.textureAtlas = new TextureAtlas(p_118888_);
        p_118887_.register(this.textureAtlas.location(), this.textureAtlas);
    }

    protected abstract Stream<ResourceLocation> getResourcesToLoad();

    protected TextureAtlasSprite getSprite(ResourceLocation pLocation)
    {
        return this.textureAtlas.getSprite(this.resolveLocation(pLocation));
    }

    private ResourceLocation resolveLocation(ResourceLocation pLocation)
    {
        return new ResourceLocation(pLocation.getNamespace(), this.prefix + "/" + pLocation.getPath());
    }

    protected TextureAtlas.Preparations prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler)
    {
        pProfiler.startTick();
        pProfiler.push("stitching");
        TextureAtlas.Preparations textureatlas$preparations = this.textureAtlas.prepareToStitch(pResourceManager, this.getResourcesToLoad().map(this::resolveLocation), pProfiler, 0);
        pProfiler.pop();
        pProfiler.endTick();
        return textureatlas$preparations;
    }

    protected void apply(TextureAtlas.Preparations pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler)
    {
        pProfiler.startTick();
        pProfiler.push("upload");
        this.textureAtlas.reload(pObject);
        pProfiler.pop();
        pProfiler.endTick();
    }

    public void close()
    {
        this.textureAtlas.clearTextureData();
    }
}
