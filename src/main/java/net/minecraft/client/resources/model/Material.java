package net.minecraft.client.resources.model;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.optifine.EmissiveTextures;
import net.optifine.render.RenderUtils;

public class Material
{
    private final ResourceLocation atlasLocation;
    private final ResourceLocation texture;
    @Nullable
    private RenderType renderType;

    public Material(ResourceLocation p_119191_, ResourceLocation p_119192_)
    {
        this.atlasLocation = p_119191_;
        this.texture = p_119192_;
    }

    public ResourceLocation atlasLocation()
    {
        return this.atlasLocation;
    }

    public ResourceLocation texture()
    {
        return this.texture;
    }

    public TextureAtlasSprite sprite()
    {
        TextureAtlasSprite textureatlassprite = Minecraft.getInstance().getTextureAtlas(this.atlasLocation()).apply(this.texture());

        if (EmissiveTextures.isActive())
        {
            textureatlassprite = EmissiveTextures.getEmissiveSprite(textureatlassprite);
        }

        return textureatlassprite;
    }

    public RenderType renderType(Function<ResourceLocation, RenderType> pRenderTypeGetter)
    {
        if (this.renderType == null)
        {
            this.renderType = pRenderTypeGetter.apply(this.atlasLocation);
        }

        return this.renderType;
    }

    public VertexConsumer buffer(MultiBufferSource pBuffer, Function<ResourceLocation, RenderType> pRenderTypeGetter)
    {
        TextureAtlasSprite textureatlassprite = this.sprite();
        RenderType rendertype = this.renderType(pRenderTypeGetter);

        if (textureatlassprite.isSpriteEmissive && rendertype.isEntitySolid())
        {
            RenderUtils.flushRenderBuffers();
            rendertype = RenderType.entityCutout(this.atlasLocation);
        }

        return textureatlassprite.wrap(pBuffer.getBuffer(rendertype));
    }

    public VertexConsumer buffer(MultiBufferSource pBuffer, Function<ResourceLocation, RenderType> pRenderTypeGetter, boolean p_119200_)
    {
        return this.sprite().wrap(ItemRenderer.getFoilBufferDirect(pBuffer, this.renderType(pRenderTypeGetter), true, p_119200_));
    }

    public boolean equals(Object p_119206_)
    {
        if (this == p_119206_)
        {
            return true;
        }
        else if (p_119206_ != null && this.getClass() == p_119206_.getClass())
        {
            Material material = (Material)p_119206_;
            return this.atlasLocation.equals(material.atlasLocation) && this.texture.equals(material.texture);
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return Objects.hash(this.atlasLocation, this.texture);
    }

    public String toString()
    {
        return "Material{atlasLocation=" + this.atlasLocation + ", texture=" + this.texture + "}";
    }
}
