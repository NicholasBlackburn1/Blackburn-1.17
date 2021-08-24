package net.minecraft.world.level.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.File;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetBiomeZoomer;
import net.minecraft.world.level.biome.FuzzyOffsetConstantColumnBiomeZoomer;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public class DimensionType {
   public static final int BITS_FOR_Y = BlockPos.PACKED_Y_LENGTH;
   public static final int MIN_HEIGHT = 16;
   public static final int Y_SIZE = (1 << BITS_FOR_Y) - 32;
   public static final int MAX_Y = (Y_SIZE >> 1) - 1;
   public static final int MIN_Y = MAX_Y - Y_SIZE + 1;
   public static final ResourceLocation OVERWORLD_EFFECTS = new ResourceLocation("overworld");
   public static final ResourceLocation NETHER_EFFECTS = new ResourceLocation("the_nether");
   public static final ResourceLocation END_EFFECTS = new ResourceLocation("the_end");
   public static final Codec<DimensionType> DIRECT_CODEC = RecordCodecBuilder.<DimensionType>create((p_63914_) -> {
      return p_63914_.group(Codec.LONG.optionalFieldOf("fixed_time").xmap((p_156696_) -> {
         return p_156696_.map(OptionalLong::of).orElseGet(OptionalLong::empty);
      }, (p_156698_) -> {
         return p_156698_.isPresent() ? Optional.of(p_156698_.getAsLong()) : Optional.empty();
      }).forGetter((p_156731_) -> {
         return p_156731_.fixedTime;
      }), Codec.BOOL.fieldOf("has_skylight").forGetter(DimensionType::hasSkyLight), Codec.BOOL.fieldOf("has_ceiling").forGetter(DimensionType::hasCeiling), Codec.BOOL.fieldOf("ultrawarm").forGetter(DimensionType::ultraWarm), Codec.BOOL.fieldOf("natural").forGetter(DimensionType::natural), Codec.doubleRange((double)1.0E-5F, 3.0E7D).fieldOf("coordinate_scale").forGetter(DimensionType::coordinateScale), Codec.BOOL.fieldOf("piglin_safe").forGetter(DimensionType::piglinSafe), Codec.BOOL.fieldOf("bed_works").forGetter(DimensionType::bedWorks), Codec.BOOL.fieldOf("respawn_anchor_works").forGetter(DimensionType::respawnAnchorWorks), Codec.BOOL.fieldOf("has_raids").forGetter(DimensionType::hasRaids), Codec.intRange(MIN_Y, MAX_Y).fieldOf("min_y").forGetter(DimensionType::minY), Codec.intRange(16, Y_SIZE).fieldOf("height").forGetter(DimensionType::height), Codec.intRange(0, Y_SIZE).fieldOf("logical_height").forGetter(DimensionType::logicalHeight), ResourceLocation.CODEC.fieldOf("infiniburn").forGetter((p_156729_) -> {
         return p_156729_.infiniburn;
      }), ResourceLocation.CODEC.fieldOf("effects").orElse(OVERWORLD_EFFECTS).forGetter((p_156725_) -> {
         return p_156725_.effectsLocation;
      }), Codec.FLOAT.fieldOf("ambient_light").forGetter((p_156721_) -> {
         return p_156721_.ambientLight;
      })).apply(p_63914_, DimensionType::new);
   }).comapFlatMap(DimensionType::guardY, Function.identity());
   private static final int MOON_PHASES = 8;
   public static final float[] MOON_BRIGHTNESS_PER_PHASE = new float[]{1.0F, 0.75F, 0.5F, 0.25F, 0.0F, 0.25F, 0.5F, 0.75F};
   public static final ResourceKey<DimensionType> OVERWORLD_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld"));
   public static final ResourceKey<DimensionType> NETHER_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_nether"));
   public static final ResourceKey<DimensionType> END_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("the_end"));
   protected static final DimensionType DEFAULT_OVERWORLD = create(OptionalLong.empty(), true, false, false, true, 1.0D, false, false, true, false, true, 0, 256, 256, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE, BlockTags.INFINIBURN_OVERWORLD.getName(), OVERWORLD_EFFECTS, 0.0F);
   protected static final DimensionType DEFAULT_NETHER = create(OptionalLong.of(18000L), false, true, true, false, 8.0D, false, true, false, true, false, 0, 256, 128, FuzzyOffsetBiomeZoomer.INSTANCE, BlockTags.INFINIBURN_NETHER.getName(), NETHER_EFFECTS, 0.1F);
   protected static final DimensionType DEFAULT_END = create(OptionalLong.of(6000L), false, false, false, false, 1.0D, true, false, false, false, true, 0, 256, 256, FuzzyOffsetBiomeZoomer.INSTANCE, BlockTags.INFINIBURN_END.getName(), END_EFFECTS, 0.0F);
   public static final ResourceKey<DimensionType> OVERWORLD_CAVES_LOCATION = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld_caves"));
   protected static final DimensionType DEFAULT_OVERWORLD_CAVES = create(OptionalLong.empty(), true, true, false, true, 1.0D, false, false, true, false, true, 0, 256, 256, FuzzyOffsetConstantColumnBiomeZoomer.INSTANCE, BlockTags.INFINIBURN_OVERWORLD.getName(), OVERWORLD_EFFECTS, 0.0F);
   public static final Codec<Supplier<DimensionType>> CODEC = RegistryFileCodec.create(Registry.DIMENSION_TYPE_REGISTRY, DIRECT_CODEC);
   private final OptionalLong fixedTime;
   private final boolean hasSkylight;
   private final boolean hasCeiling;
   private final boolean ultraWarm;
   private final boolean natural;
   private final double coordinateScale;
   private final boolean createDragonFight;
   private final boolean piglinSafe;
   private final boolean bedWorks;
   private final boolean respawnAnchorWorks;
   private final boolean hasRaids;
   private final int minY;
   private final int height;
   private final int logicalHeight;
   private final BiomeZoomer biomeZoomer;
   private final ResourceLocation infiniburn;
   private final ResourceLocation effectsLocation;
   private final float ambientLight;
   private final transient float[] brightnessRamp;

   private static DataResult<DimensionType> guardY(DimensionType p_156719_) {
      if (p_156719_.height() < 16) {
         return DataResult.error("height has to be at least 16");
      } else if (p_156719_.minY() + p_156719_.height() > MAX_Y + 1) {
         return DataResult.error("min_y + height cannot be higher than: " + (MAX_Y + 1));
      } else if (p_156719_.logicalHeight() > p_156719_.height()) {
         return DataResult.error("logical_height cannot be higher than height");
      } else if (p_156719_.height() % 16 != 0) {
         return DataResult.error("height has to be multiple of 16");
      } else {
         return p_156719_.minY() % 16 != 0 ? DataResult.error("min_y has to be a multiple of 16") : DataResult.success(p_156719_);
      }
   }

   private DimensionType(OptionalLong p_156656_, boolean p_156657_, boolean p_156658_, boolean p_156659_, boolean p_156660_, double p_156661_, boolean p_156662_, boolean p_156663_, boolean p_156664_, boolean p_156665_, int p_156666_, int p_156667_, int p_156668_, ResourceLocation p_156669_, ResourceLocation p_156670_, float p_156671_) {
      this(p_156656_, p_156657_, p_156658_, p_156659_, p_156660_, p_156661_, false, p_156662_, p_156663_, p_156664_, p_156665_, p_156666_, p_156667_, p_156668_, FuzzyOffsetBiomeZoomer.INSTANCE, p_156669_, p_156670_, p_156671_);
   }

   public static DimensionType create(OptionalLong p_156700_, boolean p_156701_, boolean p_156702_, boolean p_156703_, boolean p_156704_, double p_156705_, boolean p_156706_, boolean p_156707_, boolean p_156708_, boolean p_156709_, boolean p_156710_, int p_156711_, int p_156712_, int p_156713_, BiomeZoomer p_156714_, ResourceLocation p_156715_, ResourceLocation p_156716_, float p_156717_) {
      DimensionType dimensiontype = new DimensionType(p_156700_, p_156701_, p_156702_, p_156703_, p_156704_, p_156705_, p_156706_, p_156707_, p_156708_, p_156709_, p_156710_, p_156711_, p_156712_, p_156713_, p_156714_, p_156715_, p_156716_, p_156717_);
      guardY(dimensiontype).error().ifPresent((p_156692_) -> {
         throw new IllegalStateException(p_156692_.message());
      });
      return dimensiontype;
   }

   @Deprecated
   private DimensionType(OptionalLong p_156673_, boolean p_156674_, boolean p_156675_, boolean p_156676_, boolean p_156677_, double p_156678_, boolean p_156679_, boolean p_156680_, boolean p_156681_, boolean p_156682_, boolean p_156683_, int p_156684_, int p_156685_, int p_156686_, BiomeZoomer p_156687_, ResourceLocation p_156688_, ResourceLocation p_156689_, float p_156690_) {
      this.fixedTime = p_156673_;
      this.hasSkylight = p_156674_;
      this.hasCeiling = p_156675_;
      this.ultraWarm = p_156676_;
      this.natural = p_156677_;
      this.coordinateScale = p_156678_;
      this.createDragonFight = p_156679_;
      this.piglinSafe = p_156680_;
      this.bedWorks = p_156681_;
      this.respawnAnchorWorks = p_156682_;
      this.hasRaids = p_156683_;
      this.minY = p_156684_;
      this.height = p_156685_;
      this.logicalHeight = p_156686_;
      this.biomeZoomer = p_156687_;
      this.infiniburn = p_156688_;
      this.effectsLocation = p_156689_;
      this.ambientLight = p_156690_;
      this.brightnessRamp = fillBrightnessRamp(p_156690_);
   }

   private static float[] fillBrightnessRamp(float p_63901_) {
      float[] afloat = new float[16];

      for(int i = 0; i <= 15; ++i) {
         float f = (float)i / 15.0F;
         float f1 = f / (4.0F - 3.0F * f);
         afloat[i] = Mth.lerp(p_63901_, f1, 1.0F);
      }

      return afloat;
   }

   @Deprecated
   public static DataResult<ResourceKey<Level>> parseLegacy(Dynamic<?> p_63912_) {
      Optional<Number> optional = p_63912_.asNumber().result();
      if (optional.isPresent()) {
         int i = optional.get().intValue();
         if (i == -1) {
            return DataResult.success(Level.NETHER);
         }

         if (i == 0) {
            return DataResult.success(Level.OVERWORLD);
         }

         if (i == 1) {
            return DataResult.success(Level.END);
         }
      }

      return Level.RESOURCE_KEY_CODEC.parse(p_63912_);
   }

   public static RegistryAccess.RegistryHolder registerBuiltin(RegistryAccess.RegistryHolder p_63927_) {
      WritableRegistry<DimensionType> writableregistry = p_63927_.ownedRegistryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
      writableregistry.register(OVERWORLD_LOCATION, DEFAULT_OVERWORLD, Lifecycle.stable());
      writableregistry.register(OVERWORLD_CAVES_LOCATION, DEFAULT_OVERWORLD_CAVES, Lifecycle.stable());
      writableregistry.register(NETHER_LOCATION, DEFAULT_NETHER, Lifecycle.stable());
      writableregistry.register(END_LOCATION, DEFAULT_END, Lifecycle.stable());
      return p_63927_;
   }

   private static ChunkGenerator defaultEndGenerator(Registry<Biome> p_63918_, Registry<NoiseGeneratorSettings> p_63919_, long p_63920_) {
      return new NoiseBasedChunkGenerator(new TheEndBiomeSource(p_63918_, p_63920_), p_63920_, () -> {
         return p_63919_.getOrThrow(NoiseGeneratorSettings.END);
      });
   }

   private static ChunkGenerator defaultNetherGenerator(Registry<Biome> p_63943_, Registry<NoiseGeneratorSettings> p_63944_, long p_63945_) {
      return new NoiseBasedChunkGenerator(MultiNoiseBiomeSource.Preset.NETHER.biomeSource(p_63943_, p_63945_), p_63945_, () -> {
         return p_63944_.getOrThrow(NoiseGeneratorSettings.NETHER);
      });
   }

   public static MappedRegistry<LevelStem> defaultDimensions(Registry<DimensionType> p_63922_, Registry<Biome> p_63923_, Registry<NoiseGeneratorSettings> p_63924_, long p_63925_) {
      MappedRegistry<LevelStem> mappedregistry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
      mappedregistry.register(LevelStem.NETHER, new LevelStem(() -> {
         return p_63922_.getOrThrow(NETHER_LOCATION);
      }, defaultNetherGenerator(p_63923_, p_63924_, p_63925_)), Lifecycle.stable());
      mappedregistry.register(LevelStem.END, new LevelStem(() -> {
         return p_63922_.getOrThrow(END_LOCATION);
      }, defaultEndGenerator(p_63923_, p_63924_, p_63925_)), Lifecycle.stable());
      return mappedregistry;
   }

   public static double getTeleportationScale(DimensionType p_63909_, DimensionType p_63910_) {
      double d0 = p_63909_.coordinateScale();
      double d1 = p_63910_.coordinateScale();
      return d0 / d1;
   }

   @Deprecated
   public String getFileSuffix() {
      return this.equalTo(DEFAULT_END) ? "_end" : "";
   }

   public static File getStorageFolder(ResourceKey<Level> p_63933_, File p_63934_) {
      if (p_63933_ == Level.OVERWORLD) {
         return p_63934_;
      } else if (p_63933_ == Level.END) {
         return new File(p_63934_, "DIM1");
      } else {
         return p_63933_ == Level.NETHER ? new File(p_63934_, "DIM-1") : new File(p_63934_, "dimensions/" + p_63933_.location().getNamespace() + "/" + p_63933_.location().getPath());
      }
   }

   public boolean hasSkyLight() {
      return this.hasSkylight;
   }

   public boolean hasCeiling() {
      return this.hasCeiling;
   }

   public boolean ultraWarm() {
      return this.ultraWarm;
   }

   public boolean natural() {
      return this.natural;
   }

   public double coordinateScale() {
      return this.coordinateScale;
   }

   public boolean piglinSafe() {
      return this.piglinSafe;
   }

   public boolean bedWorks() {
      return this.bedWorks;
   }

   public boolean respawnAnchorWorks() {
      return this.respawnAnchorWorks;
   }

   public boolean hasRaids() {
      return this.hasRaids;
   }

   public int minY() {
      return this.minY;
   }

   public int height() {
      return this.height;
   }

   public int logicalHeight() {
      return this.logicalHeight;
   }

   public boolean createDragonFight() {
      return this.createDragonFight;
   }

   public BiomeZoomer getBiomeZoomer() {
      return this.biomeZoomer;
   }

   public boolean hasFixedTime() {
      return this.fixedTime.isPresent();
   }

   public float timeOfDay(long p_63905_) {
      double d0 = Mth.frac((double)this.fixedTime.orElse(p_63905_) / 24000.0D - 0.25D);
      double d1 = 0.5D - Math.cos(d0 * Math.PI) / 2.0D;
      return (float)(d0 * 2.0D + d1) / 3.0F;
   }

   public int moonPhase(long p_63937_) {
      return (int)(p_63937_ / 24000L % 8L + 8L) % 8;
   }

   public float brightness(int p_63903_) {
      return this.brightnessRamp[p_63903_];
   }

   public Tag<Block> infiniburn() {
      Tag<Block> tag = BlockTags.getAllTags().getTag(this.infiniburn);
      return (Tag<Block>)(tag != null ? tag : BlockTags.INFINIBURN_OVERWORLD);
   }

   public ResourceLocation effectsLocation() {
      return this.effectsLocation;
   }

   public boolean equalTo(DimensionType p_63907_) {
      if (this == p_63907_) {
         return true;
      } else {
         return this.hasSkylight == p_63907_.hasSkylight && this.hasCeiling == p_63907_.hasCeiling && this.ultraWarm == p_63907_.ultraWarm && this.natural == p_63907_.natural && this.coordinateScale == p_63907_.coordinateScale && this.createDragonFight == p_63907_.createDragonFight && this.piglinSafe == p_63907_.piglinSafe && this.bedWorks == p_63907_.bedWorks && this.respawnAnchorWorks == p_63907_.respawnAnchorWorks && this.hasRaids == p_63907_.hasRaids && this.minY == p_63907_.minY && this.height == p_63907_.height && this.logicalHeight == p_63907_.logicalHeight && Float.compare(p_63907_.ambientLight, this.ambientLight) == 0 && this.fixedTime.equals(p_63907_.fixedTime) && this.biomeZoomer.equals(p_63907_.biomeZoomer) && this.infiniburn.equals(p_63907_.infiniburn) && this.effectsLocation.equals(p_63907_.effectsLocation);
      }
   }
}
