package net.minecraftforge.client.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Random;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import net.optifine.reflect.Reflector;

public interface IForgeBakedModel
{
default BakedModel getBakedModel()
    {
        return (BakedModel)this;
    }

default List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData)
    {
        return this.getBakedModel().getQuads(state, side, rand);
    }

default boolean isAmbientOcclusion(BlockState state)
    {
        return this.getBakedModel().useAmbientOcclusion();
    }

default BakedModel handlePerspective(ItemTransforms.TransformType cameraTransformType, PoseStack mat)
    {
        return (BakedModel)Reflector.ForgeHooksClient_handlePerspective.call(this.getBakedModel(), cameraTransformType, mat);
    }

default IModelData getModelData(BlockAndTintGetter world, BlockPos pos, BlockState state, IModelData tileData)
    {
        return tileData;
    }

default TextureAtlasSprite getParticleTexture(IModelData data)
    {
        return this.getBakedModel().getParticleIcon();
    }

default boolean isLayered()
    {
        return false;
    }
}
