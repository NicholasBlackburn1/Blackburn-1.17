package net.minecraft.client.resources.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleBakedModel implements BakedModel
{
    protected final List<BakedQuad> unculledFaces;
    protected final Map<Direction, List<BakedQuad>> culledFaces;
    protected final boolean hasAmbientOcclusion;
    protected final boolean isGui3d;
    protected final boolean usesBlockLight;
    protected final TextureAtlasSprite particleIcon;
    protected final ItemTransforms transforms;
    protected final ItemOverrides overrides;

    public SimpleBakedModel(List<BakedQuad> p_119489_, Map<Direction, List<BakedQuad>> p_119490_, boolean p_119491_, boolean p_119492_, boolean p_119493_, TextureAtlasSprite p_119494_, ItemTransforms p_119495_, ItemOverrides p_119496_)
    {
        this.unculledFaces = p_119489_;
        this.culledFaces = p_119490_;
        this.hasAmbientOcclusion = p_119491_;
        this.isGui3d = p_119493_;
        this.usesBlockLight = p_119492_;
        this.particleIcon = p_119494_;
        this.transforms = p_119495_;
        this.overrides = p_119496_;
    }

    public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pSide, Random pRand)
    {
        return pSide == null ? this.unculledFaces : this.culledFaces.get(pSide);
    }

    public boolean useAmbientOcclusion()
    {
        return this.hasAmbientOcclusion;
    }

    public boolean isGui3d()
    {
        return this.isGui3d;
    }

    public boolean usesBlockLight()
    {
        return this.usesBlockLight;
    }

    public boolean isCustomRenderer()
    {
        return false;
    }

    public TextureAtlasSprite getParticleIcon()
    {
        return this.particleIcon;
    }

    public ItemTransforms getTransforms()
    {
        return this.transforms;
    }

    public ItemOverrides getOverrides()
    {
        return this.overrides;
    }

    public static class Builder
    {
        private final List<BakedQuad> unculledFaces = Lists.newArrayList();
        private final Map<Direction, List<BakedQuad>> culledFaces = Maps.newEnumMap(Direction.class);
        private final ItemOverrides overrides;
        private final boolean hasAmbientOcclusion;
        private TextureAtlasSprite particleIcon;
        private final boolean usesBlockLight;
        private final boolean isGui3d;
        private final ItemTransforms transforms;

        public Builder(BlockModel p_119517_, ItemOverrides p_119518_, boolean p_119519_)
        {
            this(p_119517_.hasAmbientOcclusion(), p_119517_.getGuiLight().lightLikeBlock(), p_119519_, p_119517_.getTransforms(), p_119518_);
        }

        private Builder(boolean p_119521_, boolean p_119522_, boolean p_119523_, ItemTransforms p_119524_, ItemOverrides p_119525_)
        {
            for (Direction direction : Direction.values())
            {
                this.culledFaces.put(direction, Lists.newArrayList());
            }

            this.overrides = p_119525_;
            this.hasAmbientOcclusion = p_119521_;
            this.usesBlockLight = p_119522_;
            this.isGui3d = p_119523_;
            this.transforms = p_119524_;
        }

        public SimpleBakedModel.Builder addCulledFace(Direction pFacing, BakedQuad pQuad)
        {
            this.culledFaces.get(pFacing).add(pQuad);
            return this;
        }

        public SimpleBakedModel.Builder addUnculledFace(BakedQuad pQuad)
        {
            this.unculledFaces.add(pQuad);
            return this;
        }

        public SimpleBakedModel.Builder particle(TextureAtlasSprite pTexture)
        {
            this.particleIcon = pTexture;
            return this;
        }

        public SimpleBakedModel.Builder item()
        {
            return this;
        }

        public BakedModel build()
        {
            if (this.particleIcon == null)
            {
                throw new RuntimeException("Missing particle!");
            }
            else
            {
                return new SimpleBakedModel(this.unculledFaces, this.culledFaces, this.hasAmbientOcclusion, this.usesBlockLight, this.isGui3d, this.particleIcon, this.transforms, this.overrides);
            }
        }
    }
}
