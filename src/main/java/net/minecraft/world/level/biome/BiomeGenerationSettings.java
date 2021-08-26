package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BiomeGenerationSettings
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final BiomeGenerationSettings EMPTY = new BiomeGenerationSettings(() ->
    {
        return SurfaceBuilders.NOPE;
    }, ImmutableMap.of(), ImmutableList.of(), ImmutableList.of());
    public static final MapCodec<BiomeGenerationSettings> CODEC = RecordCodecBuilder.mapCodec((p_47814_) ->
    {
        return p_47814_.group(ConfiguredSurfaceBuilder.CODEC.fieldOf("surface_builder").flatXmap(ExtraCodecs.nonNullSupplierCheck(), ExtraCodecs.nonNullSupplierCheck()).forGetter((p_151749_) -> {
            return p_151749_.surfaceBuilder;
        }), Codec.simpleMap(GenerationStep.Carving.CODEC, ConfiguredWorldCarver.LIST_CODEC.promotePartial(Util.prefix("Carver: ", LOGGER::error)).flatXmap(ExtraCodecs.nonNullSupplierListCheck(), ExtraCodecs.nonNullSupplierListCheck()), StringRepresentable.m_14357_(GenerationStep.Carving.values())).fieldOf("carvers").forGetter((p_151747_) -> {
            return p_151747_.carvers;
        }), ConfiguredFeature.LIST_CODEC.promotePartial(Util.prefix("Feature: ", LOGGER::error)).flatXmap(ExtraCodecs.nonNullSupplierListCheck(), ExtraCodecs.nonNullSupplierListCheck()).listOf().fieldOf("features").forGetter((p_151745_) -> {
            return p_151745_.features;
        }), ConfiguredStructureFeature.LIST_CODEC.promotePartial(Util.prefix("Structure start: ", LOGGER::error)).fieldOf("starts").flatXmap(ExtraCodecs.nonNullSupplierListCheck(), ExtraCodecs.nonNullSupplierListCheck()).forGetter((p_151743_) -> {
            return p_151743_.structureStarts;
        })).apply(p_47814_, BiomeGenerationSettings::new);
    });
    private final Supplier < ConfiguredSurfaceBuilder<? >> surfaceBuilder;
    private final Map < GenerationStep.Carving, List < Supplier < ConfiguredWorldCarver<? >>> > carvers;
    private final List < List < Supplier < ConfiguredFeature <? , ? >>> > features;
    private final List < Supplier < ConfiguredStructureFeature <? , ? >>> structureStarts;
    private final List < ConfiguredFeature <? , ? >> flowerFeatures;

    BiomeGenerationSettings(Supplier < ConfiguredSurfaceBuilder<? >> p_47786_, Map < GenerationStep.Carving, List < Supplier < ConfiguredWorldCarver<? >>> > p_47787_, List < List < Supplier < ConfiguredFeature <? , ? >>> > p_47788_, List < Supplier < ConfiguredStructureFeature <? , ? >>> p_47789_)
    {
        this.surfaceBuilder = p_47786_;
        this.carvers = p_47787_;
        this.features = p_47788_;
        this.structureStarts = p_47789_;
        this.flowerFeatures = p_47788_.stream().flatMap(Collection::stream).map(Supplier::get).flatMap(ConfiguredFeature::getFeatures).filter((p_47802_) ->
        {
            return p_47802_.feature == Feature.FLOWER;
        }).collect(ImmutableList.toImmutableList());
    }

    public List < Supplier < ConfiguredWorldCarver<? >>> getCarvers(GenerationStep.Carving pCarvingType)
    {
        return this.carvers.getOrDefault(pCarvingType, ImmutableList.of());
    }

    public boolean isValidStart(StructureFeature<?> pStructure)
    {
        return this.structureStarts.stream().anyMatch((p_47812_) ->
        {
            return (p_47812_.get()).feature == pStructure;
        });
    }

    public Collection < Supplier < ConfiguredStructureFeature <? , ? >>> structures()
    {
        return this.structureStarts;
    }

    public ConfiguredStructureFeature <? , ? > withBiomeConfig(ConfiguredStructureFeature <? , ? > pStructure)
    {
        return DataFixUtils.orElse(this.structureStarts.stream().map(Supplier::get).filter((p_47807_) ->
        {
            return p_47807_.feature == pStructure.feature;
        }).findAny(), pStructure);
    }

    public List < ConfiguredFeature <? , ? >> getFlowerFeatures()
    {
        return this.flowerFeatures;
    }

    public List < List < Supplier < ConfiguredFeature <? , ? >>> > features()
    {
        return this.features;
    }

    public Supplier < ConfiguredSurfaceBuilder<? >> getSurfaceBuilder()
    {
        return this.surfaceBuilder;
    }

    public SurfaceBuilderConfiguration getSurfaceBuilderConfig()
    {
        return this.surfaceBuilder.get().config();
    }

    public static class Builder
    {
        private Optional < Supplier < ConfiguredSurfaceBuilder<? >>> surfaceBuilder = Optional.empty();
        private final Map < GenerationStep.Carving, List < Supplier < ConfiguredWorldCarver<? >>> > carvers = Maps.newLinkedHashMap();
        private final List < List < Supplier < ConfiguredFeature <? , ? >>> > features = Lists.newArrayList();
        private final List < Supplier < ConfiguredStructureFeature <? , ? >>> structureStarts = Lists.newArrayList();

        public BiomeGenerationSettings.Builder surfaceBuilder(ConfiguredSurfaceBuilder<?> pConfiguredSurfaceBuilderSupplier)
        {
            return this.surfaceBuilder(() ->
            {
                return pConfiguredSurfaceBuilderSupplier;
            });
        }

        public BiomeGenerationSettings.Builder surfaceBuilder(Supplier < ConfiguredSurfaceBuilder<? >> pConfiguredSurfaceBuilderSupplier)
        {
            this.surfaceBuilder = Optional.of(pConfiguredSurfaceBuilderSupplier);
            return this;
        }

        public BiomeGenerationSettings.Builder addFeature(GenerationStep.Decoration pStage, ConfiguredFeature <? , ? > pFeatures)
        {
            return this.addFeature(pStage.ordinal(), () ->
            {
                return pFeatures;
            });
        }

        public BiomeGenerationSettings.Builder addFeature(int pStage, Supplier < ConfiguredFeature <? , ? >> pFeatures)
        {
            this.addFeatureStepsUpTo(pStage);
            this.features.get(pStage).add(pFeatures);
            return this;
        }

        public <C extends CarverConfiguration> BiomeGenerationSettings.Builder addCarver(GenerationStep.Carving pCarvingStage, ConfiguredWorldCarver<C> pCarver)
        {
            this.carvers.computeIfAbsent(pCarvingStage, (p_47838_) ->
            {
                return Lists.newArrayList();
            }).add(() ->
            {
                return pCarver;
            });
            return this;
        }

        public BiomeGenerationSettings.Builder addStructureStart(ConfiguredStructureFeature <? , ? > pStructure)
        {
            this.structureStarts.add(() ->
            {
                return pStructure;
            });
            return this;
        }

        private void addFeatureStepsUpTo(int pStage)
        {
            while (this.features.size() <= pStage)
            {
                this.features.add(Lists.newArrayList());
            }
        }

        public BiomeGenerationSettings build()
        {
            return new BiomeGenerationSettings(this.surfaceBuilder.orElseThrow(() ->
            {
                return new IllegalStateException("Missing surface builder");
            }), this.carvers.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, (p_47854_) ->
            {
                return ImmutableList.copyOf(p_47854_.getValue());
            })), this.features.stream().map(ImmutableList::copyOf).collect(ImmutableList.toImmutableList()), ImmutableList.copyOf(this.structureStarts));
        }
    }
}
