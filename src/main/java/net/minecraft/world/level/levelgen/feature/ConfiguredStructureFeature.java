package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class ConfiguredStructureFeature<FC extends FeatureConfiguration, F extends StructureFeature<FC>> {
   public static final Codec<ConfiguredStructureFeature<?, ?>> DIRECT_CODEC = Registry.STRUCTURE_FEATURE.dispatch((p_65410_) -> {
      return p_65410_.feature;
   }, StructureFeature::configuredStructureCodec);
   public static final Codec<Supplier<ConfiguredStructureFeature<?, ?>>> CODEC = RegistryFileCodec.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, DIRECT_CODEC);
   public static final Codec<List<Supplier<ConfiguredStructureFeature<?, ?>>>> LIST_CODEC = RegistryFileCodec.homogeneousList(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, DIRECT_CODEC);
   public final F feature;
   public final FC config;

   public ConfiguredStructureFeature(F p_65407_, FC p_65408_) {
      this.feature = p_65407_;
      this.config = p_65408_;
   }

   public StructureStart<?> generate(RegistryAccess p_159525_, ChunkGenerator p_159526_, BiomeSource p_159527_, StructureManager p_159528_, long p_159529_, ChunkPos p_159530_, Biome p_159531_, int p_159532_, StructureFeatureConfiguration p_159533_, LevelHeightAccessor p_159534_) {
      return this.feature.generate(p_159525_, p_159526_, p_159527_, p_159528_, p_159529_, p_159530_, p_159531_, p_159532_, new WorldgenRandom(), p_159533_, this.config, p_159534_);
   }
}