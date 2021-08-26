package net.minecraft.client.renderer.block;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockModelShaper
{
    private final Map<BlockState, BakedModel> modelByStateCache = Maps.newIdentityHashMap();
    private final ModelManager modelManager;

    public BlockModelShaper(ModelManager p_110880_)
    {
        this.modelManager = p_110880_;
    }

    public TextureAtlasSprite getParticleIcon(BlockState pState)
    {
        return this.getBlockModel(pState).getParticleIcon();
    }

    public BakedModel getBlockModel(BlockState pState)
    {
        BakedModel bakedmodel = this.modelByStateCache.get(pState);

        if (bakedmodel == null)
        {
            bakedmodel = this.modelManager.getMissingModel();
        }

        return bakedmodel;
    }

    public ModelManager getModelManager()
    {
        return this.modelManager;
    }

    public void rebuildCache()
    {
        this.modelByStateCache.clear();

        for (Block block : Registry.BLOCK)
        {
            block.getStateDefinition().getPossibleStates().forEach((p_110898_) ->
            {
                this.modelByStateCache.put(p_110898_, this.modelManager.getModel(stateToModelLocation(p_110898_)));
            });
        }
    }

    public static ModelResourceLocation stateToModelLocation(BlockState pLocation)
    {
        return stateToModelLocation(Registry.BLOCK.getKey(pLocation.getBlock()), pLocation);
    }

    public static ModelResourceLocation stateToModelLocation(ResourceLocation pLocation, BlockState pState)
    {
        return new ModelResourceLocation(pLocation, statePropertiesToString(pState.getValues()));
    }

    public static String statePropertiesToString(Map < Property<?>, Comparable<? >> pPropertyValues)
    {
        StringBuilder stringbuilder = new StringBuilder();

        for (Entry < Property<?>, Comparable<? >> entry : pPropertyValues.entrySet())
        {
            if (stringbuilder.length() != 0)
            {
                stringbuilder.append(',');
            }

            Property<?> property = entry.getKey();
            stringbuilder.append(property.getName());
            stringbuilder.append('=');
            stringbuilder.append(getValue(property, entry.getValue()));
        }

        return stringbuilder.toString();
    }

    private static <T extends Comparable<T>> String getValue(Property<T> pProperty, Comparable<?> pValue)
    {
        return pProperty.getName((T)pValue);
    }
}
