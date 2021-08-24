package net.minecraft.world.level.levelgen.flat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.Features;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FlatLevelGeneratorSettings {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final Codec<FlatLevelGeneratorSettings> CODEC = RecordCodecBuilder.<FlatLevelGeneratorSettings>create((p_70373_) -> {
      return p_70373_.group(RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter((p_161916_) -> {
         return p_161916_.biomes;
      }), StructureSettings.CODEC.fieldOf("structures").forGetter(FlatLevelGeneratorSettings::structureSettings), FlatLayerInfo.CODEC.listOf().fieldOf("layers").forGetter(FlatLevelGeneratorSettings::getLayersInfo), Codec.BOOL.fieldOf("lakes").orElse(false).forGetter((p_161914_) -> {
         return p_161914_.addLakes;
      }), Codec.BOOL.fieldOf("features").orElse(false).forGetter((p_161912_) -> {
         return p_161912_.decoration;
      }), Biome.CODEC.optionalFieldOf("biome").orElseGet(Optional::empty).forGetter((p_161908_) -> {
         return Optional.of(p_161908_.biome);
      })).apply(p_70373_, FlatLevelGeneratorSettings::new);
   }).comapFlatMap(FlatLevelGeneratorSettings::validateHeight, Function.identity()).stable();
   private static final Map<StructureFeature<?>, ConfiguredStructureFeature<?, ?>> STRUCTURE_FEATURES = Util.make(Maps.newHashMap(), (p_70379_) -> {
      p_70379_.put(StructureFeature.MINESHAFT, StructureFeatures.MINESHAFT);
      p_70379_.put(StructureFeature.VILLAGE, StructureFeatures.VILLAGE_PLAINS);
      p_70379_.put(StructureFeature.STRONGHOLD, StructureFeatures.STRONGHOLD);
      p_70379_.put(StructureFeature.SWAMP_HUT, StructureFeatures.SWAMP_HUT);
      p_70379_.put(StructureFeature.DESERT_PYRAMID, StructureFeatures.DESERT_PYRAMID);
      p_70379_.put(StructureFeature.JUNGLE_TEMPLE, StructureFeatures.JUNGLE_TEMPLE);
      p_70379_.put(StructureFeature.IGLOO, StructureFeatures.IGLOO);
      p_70379_.put(StructureFeature.OCEAN_RUIN, StructureFeatures.OCEAN_RUIN_COLD);
      p_70379_.put(StructureFeature.SHIPWRECK, StructureFeatures.SHIPWRECK);
      p_70379_.put(StructureFeature.OCEAN_MONUMENT, StructureFeatures.OCEAN_MONUMENT);
      p_70379_.put(StructureFeature.END_CITY, StructureFeatures.END_CITY);
      p_70379_.put(StructureFeature.WOODLAND_MANSION, StructureFeatures.WOODLAND_MANSION);
      p_70379_.put(StructureFeature.NETHER_BRIDGE, StructureFeatures.NETHER_BRIDGE);
      p_70379_.put(StructureFeature.PILLAGER_OUTPOST, StructureFeatures.PILLAGER_OUTPOST);
      p_70379_.put(StructureFeature.RUINED_PORTAL, StructureFeatures.RUINED_PORTAL_STANDARD);
      p_70379_.put(StructureFeature.BASTION_REMNANT, StructureFeatures.BASTION_REMNANT);
   });
   private final Registry<Biome> biomes;
   private final StructureSettings structureSettings;
   private final List<FlatLayerInfo> layersInfo = Lists.newArrayList();
   private Supplier<Biome> biome;
   private final List<BlockState> layers;
   private boolean voidGen;
   private boolean decoration;
   private boolean addLakes;

   private static DataResult<FlatLevelGeneratorSettings> validateHeight(FlatLevelGeneratorSettings p_161906_) {
      int i = p_161906_.layersInfo.stream().mapToInt(FlatLayerInfo::getHeight).sum();
      return i > DimensionType.Y_SIZE ? DataResult.error("Sum of layer heights is > " + DimensionType.Y_SIZE, p_161906_) : DataResult.success(p_161906_);
   }

   private FlatLevelGeneratorSettings(Registry<Biome> p_70363_, StructureSettings p_70364_, List<FlatLayerInfo> p_70365_, boolean p_70366_, boolean p_70367_, Optional<Supplier<Biome>> p_70368_) {
      this(p_70364_, p_70363_);
      if (p_70366_) {
         this.setAddLakes();
      }

      if (p_70367_) {
         this.setDecoration();
      }

      this.layersInfo.addAll(p_70365_);
      this.updateLayers();
      if (!p_70368_.isPresent()) {
         LOGGER.error("Unknown biome, defaulting to plains");
         this.biome = () -> {
            return p_70363_.getOrThrow(Biomes.PLAINS);
         };
      } else {
         this.biome = p_70368_.get();
      }

   }

   public FlatLevelGeneratorSettings(StructureSettings p_70360_, Registry<Biome> p_70361_) {
      this.biomes = p_70361_;
      this.structureSettings = p_70360_;
      this.biome = () -> {
         return p_70361_.getOrThrow(Biomes.PLAINS);
      };
      this.layers = Lists.newArrayList();
   }

   public FlatLevelGeneratorSettings withStructureSettings(StructureSettings p_70371_) {
      return this.withLayers(this.layersInfo, p_70371_);
   }

   public FlatLevelGeneratorSettings withLayers(List<FlatLayerInfo> p_70381_, StructureSettings p_70382_) {
      FlatLevelGeneratorSettings flatlevelgeneratorsettings = new FlatLevelGeneratorSettings(p_70382_, this.biomes);

      for(FlatLayerInfo flatlayerinfo : p_70381_) {
         flatlevelgeneratorsettings.layersInfo.add(new FlatLayerInfo(flatlayerinfo.getHeight(), flatlayerinfo.getBlockState().getBlock()));
         flatlevelgeneratorsettings.updateLayers();
      }

      flatlevelgeneratorsettings.setBiome(this.biome);
      if (this.decoration) {
         flatlevelgeneratorsettings.setDecoration();
      }

      if (this.addLakes) {
         flatlevelgeneratorsettings.setAddLakes();
      }

      return flatlevelgeneratorsettings;
   }

   public void setDecoration() {
      this.decoration = true;
   }

   public void setAddLakes() {
      this.addLakes = true;
   }

   public Biome getBiomeFromSettings() {
      Biome biome = this.getBiome();
      BiomeGenerationSettings biomegenerationsettings = biome.getGenerationSettings();
      BiomeGenerationSettings.Builder biomegenerationsettings$builder = (new BiomeGenerationSettings.Builder()).surfaceBuilder(biomegenerationsettings.getSurfaceBuilder());
      if (this.addLakes) {
         biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_WATER);
         biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_LAVA);
      }

      for(Entry<StructureFeature<?>, StructureFeatureConfiguration> entry : this.structureSettings.structureConfig().entrySet()) {
         biomegenerationsettings$builder.addStructureStart(biomegenerationsettings.withBiomeConfig(STRUCTURE_FEATURES.get(entry.getKey())));
      }

      boolean flag = (!this.voidGen || this.biomes.getResourceKey(biome).equals(Optional.of(Biomes.THE_VOID))) && this.decoration;
      if (flag) {
         List<List<Supplier<ConfiguredFeature<?, ?>>>> list = biomegenerationsettings.features();

         for(int i = 0; i < list.size(); ++i) {
            if (i != GenerationStep.Decoration.UNDERGROUND_STRUCTURES.ordinal() && i != GenerationStep.Decoration.SURFACE_STRUCTURES.ordinal()) {
               for(Supplier<ConfiguredFeature<?, ?>> supplier : list.get(i)) {
                  biomegenerationsettings$builder.addFeature(i, supplier);
               }
            }
         }
      }

      List<BlockState> list1 = this.getLayers();

      for(int j = 0; j < list1.size(); ++j) {
         BlockState blockstate = list1.get(j);
         if (!Heightmap.Types.MOTION_BLOCKING.isOpaque().test(blockstate)) {
            list1.set(j, (BlockState)null);
            biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Feature.FILL_LAYER.configured(new LayerConfiguration(j, blockstate)));
         }
      }

      return (new Biome.BiomeBuilder()).precipitation(biome.getPrecipitation()).biomeCategory(biome.getBiomeCategory()).depth(biome.getDepth()).scale(biome.getScale()).temperature(biome.getBaseTemperature()).downfall(biome.getDownfall()).specialEffects(biome.getSpecialEffects()).generationSettings(biomegenerationsettings$builder.build()).mobSpawnSettings(biome.getMobSettings()).build();
   }

   public StructureSettings structureSettings() {
      return this.structureSettings;
   }

   public Biome getBiome() {
      return this.biome.get();
   }

   public void setBiome(Supplier<Biome> p_70384_) {
      this.biome = p_70384_;
   }

   public List<FlatLayerInfo> getLayersInfo() {
      return this.layersInfo;
   }

   public List<BlockState> getLayers() {
      return this.layers;
   }

   public void updateLayers() {
      this.layers.clear();

      for(FlatLayerInfo flatlayerinfo : this.layersInfo) {
         for(int i = 0; i < flatlayerinfo.getHeight(); ++i) {
            this.layers.add(flatlayerinfo.getBlockState());
         }
      }

      this.voidGen = this.layers.stream().allMatch((p_161904_) -> {
         return p_161904_.is(Blocks.AIR);
      });
   }

   public static FlatLevelGeneratorSettings getDefault(Registry<Biome> p_70377_) {
      StructureSettings structuresettings = new StructureSettings(Optional.of(StructureSettings.DEFAULT_STRONGHOLD), Maps.newHashMap(ImmutableMap.of(StructureFeature.VILLAGE, StructureSettings.DEFAULTS.get(StructureFeature.VILLAGE))));
      FlatLevelGeneratorSettings flatlevelgeneratorsettings = new FlatLevelGeneratorSettings(structuresettings, p_70377_);
      flatlevelgeneratorsettings.biome = () -> {
         return p_70377_.getOrThrow(Biomes.PLAINS);
      };
      flatlevelgeneratorsettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BEDROCK));
      flatlevelgeneratorsettings.getLayersInfo().add(new FlatLayerInfo(2, Blocks.DIRT));
      flatlevelgeneratorsettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.GRASS_BLOCK));
      flatlevelgeneratorsettings.updateLayers();
      return flatlevelgeneratorsettings;
   }
}
