package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.newbiome.layer.Layer;
import net.minecraft.world.level.newbiome.layer.Layers;

public class OverworldBiomeSource extends BiomeSource
{
    public static final Codec<OverworldBiomeSource> CODEC = RecordCodecBuilder.create((p_48600_) ->
    {
        return p_48600_.group(Codec.LONG.fieldOf("seed").stable().forGetter((p_151883_) -> {
            return p_151883_.seed;
        }), Codec.BOOL.optionalFieldOf("legacy_biome_init_layer", Boolean.valueOf(false), Lifecycle.stable()).forGetter((p_151881_) -> {
            return p_151881_.legacyBiomeInitLayer;
        }), Codec.BOOL.fieldOf("large_biomes").orElse(false).stable().forGetter((p_151876_) -> {
            return p_151876_.largeBiomes;
        }), RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter((p_151874_) -> {
            return p_151874_.biomes;
        })).apply(p_48600_, p_48600_.stable(OverworldBiomeSource::new));
    });
    private final Layer noiseBiomeLayer;
    private static final List<ResourceKey<Biome>> POSSIBLE_BIOMES = ImmutableList.of(Biomes.OCEAN, Biomes.PLAINS, Biomes.DESERT, Biomes.MOUNTAINS, Biomes.FOREST, Biomes.TAIGA, Biomes.SWAMP, Biomes.RIVER, Biomes.FROZEN_OCEAN, Biomes.FROZEN_RIVER, Biomes.SNOWY_TUNDRA, Biomes.SNOWY_MOUNTAINS, Biomes.MUSHROOM_FIELDS, Biomes.MUSHROOM_FIELD_SHORE, Biomes.BEACH, Biomes.DESERT_HILLS, Biomes.WOODED_HILLS, Biomes.TAIGA_HILLS, Biomes.MOUNTAIN_EDGE, Biomes.JUNGLE, Biomes.JUNGLE_HILLS, Biomes.JUNGLE_EDGE, Biomes.DEEP_OCEAN, Biomes.STONE_SHORE, Biomes.SNOWY_BEACH, Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST_HILLS, Biomes.DARK_FOREST, Biomes.SNOWY_TAIGA, Biomes.SNOWY_TAIGA_HILLS, Biomes.GIANT_TREE_TAIGA, Biomes.GIANT_TREE_TAIGA_HILLS, Biomes.WOODED_MOUNTAINS, Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.BADLANDS, Biomes.WOODED_BADLANDS_PLATEAU, Biomes.BADLANDS_PLATEAU, Biomes.WARM_OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.COLD_OCEAN, Biomes.DEEP_WARM_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.DEEP_COLD_OCEAN, Biomes.DEEP_FROZEN_OCEAN, Biomes.SUNFLOWER_PLAINS, Biomes.DESERT_LAKES, Biomes.GRAVELLY_MOUNTAINS, Biomes.FLOWER_FOREST, Biomes.TAIGA_MOUNTAINS, Biomes.SWAMP_HILLS, Biomes.ICE_SPIKES, Biomes.MODIFIED_JUNGLE, Biomes.MODIFIED_JUNGLE_EDGE, Biomes.TALL_BIRCH_FOREST, Biomes.TALL_BIRCH_HILLS, Biomes.DARK_FOREST_HILLS, Biomes.SNOWY_TAIGA_MOUNTAINS, Biomes.GIANT_SPRUCE_TAIGA, Biomes.GIANT_SPRUCE_TAIGA_HILLS, Biomes.MODIFIED_GRAVELLY_MOUNTAINS, Biomes.SHATTERED_SAVANNA, Biomes.SHATTERED_SAVANNA_PLATEAU, Biomes.ERODED_BADLANDS, Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU, Biomes.MODIFIED_BADLANDS_PLATEAU);
    private final long seed;
    private final boolean legacyBiomeInitLayer;
    private final boolean largeBiomes;
    private final Registry<Biome> biomes;

    public OverworldBiomeSource(long p_48590_, boolean p_48591_, boolean p_48592_, Registry<Biome> p_48593_)
    {
        super(POSSIBLE_BIOMES.stream().map((p_48603_) ->
        {
            return () -> {
                return p_48593_.getOrThrow(p_48603_);
            };
        }));
        this.seed = p_48590_;
        this.legacyBiomeInitLayer = p_48591_;
        this.largeBiomes = p_48592_;
        this.biomes = p_48593_;
        this.noiseBiomeLayer = Layers.getDefaultLayer(p_48590_, p_48591_, p_48592_ ? 6 : 4, 4);
    }

    protected Codec <? extends BiomeSource > codec()
    {
        return CODEC;
    }

    public BiomeSource withSeed(long pSeed)
    {
        return new OverworldBiomeSource(pSeed, this.legacyBiomeInitLayer, this.largeBiomes, this.biomes);
    }

    public Biome getNoiseBiome(int pX, int pY, int pZ)
    {
        return this.noiseBiomeLayer.get(this.biomes, pX, pZ);
    }
}
