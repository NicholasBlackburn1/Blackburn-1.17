package net.minecraft.client.renderer.blockentity;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.optifine.EmissiveTextures;

public class BrightnessCombiner<S extends BlockEntity> implements DoubleBlockCombiner.Combiner<S, Int2IntFunction>
{
    public Int2IntFunction acceptDouble(S p_112320_, S p_112321_)
    {
        return (valIn) ->
        {
            if (EmissiveTextures.isRenderEmissive())
            {
                return LightTexture.MAX_BRIGHTNESS;
            }
            else {
                int i = LevelRenderer.getLightColor(p_112320_.getLevel(), p_112320_.getBlockPos());
                int j = LevelRenderer.getLightColor(p_112321_.getLevel(), p_112321_.getBlockPos());
                int k = LightTexture.block(i);
                int l = LightTexture.block(j);
                int i1 = LightTexture.sky(i);
                int j1 = LightTexture.sky(j);
                return LightTexture.pack(Math.max(k, l), Math.max(i1, j1));
            }
        };
    }

    public Int2IntFunction acceptSingle(S p_112318_)
    {
        return (valIn) ->
        {
            return valIn;
        };
    }

    public Int2IntFunction acceptNone()
    {
        return (valIn) ->
        {
            return valIn;
        };
    }
}
