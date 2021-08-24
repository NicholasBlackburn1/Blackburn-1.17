package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.BaseStoneSource;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.SingleBaseStoneSource;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StrongholdConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public abstract class ChunkGenerator {
   public static final Codec<ChunkGenerator> CODEC = Registry.CHUNK_GENERATOR.dispatchStable(ChunkGenerator::codec, Function.identity());
   protected final BiomeSource biomeSource;
   protected final BiomeSource runtimeBiomeSource;
   private final StructureSettings settings;
   private final long strongholdSeed;
   private final List<ChunkPos> strongholdPositions = Lists.newArrayList();
   private final BaseStoneSource defaultBaseStoneSource;

   public ChunkGenerator(BiomeSource p_62149_, StructureSettings p_62150_) {
      this(p_62149_, p_62149_, p_62150_, 0L);
   }

   public ChunkGenerator(BiomeSource p_62144_, BiomeSource p_62145_, StructureSettings p_62146_, long p_62147_) {
      this.biomeSource = p_62144_;
      this.runtimeBiomeSource = p_62145_;
      this.settings = p_62146_;
      this.strongholdSeed = p_62147_;
      this.defaultBaseStoneSource = new SingleBaseStoneSource(Blocks.STONE.defaultBlockState());
   }

   private void generateStrongholds() {
      if (this.strongholdPositions.isEmpty()) {
         StrongholdConfiguration strongholdconfiguration = this.settings.stronghold();
         if (strongholdconfiguration != null && strongholdconfiguration.count() != 0) {
            List<Biome> list = Lists.newArrayList();

            for(Biome biome : this.biomeSource.possibleBiomes()) {
               if (biome.getGenerationSettings().isValidStart(StructureFeature.STRONGHOLD)) {
                  list.add(biome);
               }
            }

            int k1 = strongholdconfiguration.distance();
            int l1 = strongholdconfiguration.count();
            int i = strongholdconfiguration.spread();
            Random random = new Random();
            random.setSeed(this.strongholdSeed);
            double d0 = random.nextDouble() * Math.PI * 2.0D;
            int j = 0;
            int k = 0;

            for(int l = 0; l < l1; ++l) {
               double d1 = (double)(4 * k1 + k1 * k * 6) + (random.nextDouble() - 0.5D) * (double)k1 * 2.5D;
               int i1 = (int)Math.round(Math.cos(d0) * d1);
               int j1 = (int)Math.round(Math.sin(d0) * d1);
               BlockPos blockpos = this.biomeSource.findBiomeHorizontal(SectionPos.sectionToBlockCoord(i1, 8), 0, SectionPos.sectionToBlockCoord(j1, 8), 112, list::contains, random);
               if (blockpos != null) {
                  i1 = SectionPos.blockToSectionCoord(blockpos.getX());
                  j1 = SectionPos.blockToSectionCoord(blockpos.getZ());
               }

               this.strongholdPositions.add(new ChunkPos(i1, j1));
               d0 += (Math.PI * 2D) / (double)i;
               ++j;
               if (j == i) {
                  ++k;
                  j = 0;
                  i = i + 2 * i / (k + 1);
                  i = Math.min(i, l1 - l);
                  d0 += random.nextDouble() * Math.PI * 2.0D;
               }
            }

         }
      }
   }

   protected abstract Codec<? extends ChunkGenerator> codec();

   public abstract ChunkGenerator withSeed(long p_62156_);

   public void createBiomes(Registry<Biome> p_62197_, ChunkAccess p_62198_) {
      ChunkPos chunkpos = p_62198_.getPos();
      ((ProtoChunk)p_62198_).setBiomes(new ChunkBiomeContainer(p_62197_, p_62198_, chunkpos, this.runtimeBiomeSource));
   }

   public void applyCarvers(long p_62157_, BiomeManager p_62158_, ChunkAccess p_62159_, GenerationStep.Carving p_62160_) {
      BiomeManager biomemanager = p_62158_.withDifferentSource(this.biomeSource);
      WorldgenRandom worldgenrandom = new WorldgenRandom();
      int i = 8;
      ChunkPos chunkpos = p_62159_.getPos();
      CarvingContext carvingcontext = new CarvingContext(this, p_62159_);
      Aquifer aquifer = this.createAquifer(p_62159_);
      BitSet bitset = ((ProtoChunk)p_62159_).getOrCreateCarvingMask(p_62160_);

      for(int j = -8; j <= 8; ++j) {
         for(int k = -8; k <= 8; ++k) {
            ChunkPos chunkpos1 = new ChunkPos(chunkpos.x + j, chunkpos.z + k);
            BiomeGenerationSettings biomegenerationsettings = this.biomeSource.getNoiseBiome(QuartPos.fromBlock(chunkpos1.getMinBlockX()), 0, QuartPos.fromBlock(chunkpos1.getMinBlockZ())).getGenerationSettings();
            List<Supplier<ConfiguredWorldCarver<?>>> list = biomegenerationsettings.getCarvers(p_62160_);
            ListIterator<Supplier<ConfiguredWorldCarver<?>>> listiterator = list.listIterator();

            while(listiterator.hasNext()) {
               int l = listiterator.nextIndex();
               ConfiguredWorldCarver<?> configuredworldcarver = listiterator.next().get();
               worldgenrandom.setLargeFeatureSeed(p_62157_ + (long)l, chunkpos1.x, chunkpos1.z);
               if (configuredworldcarver.isStartChunk(worldgenrandom)) {
                  configuredworldcarver.carve(carvingcontext, p_62159_, biomemanager::getBiome, worldgenrandom, aquifer, chunkpos1, bitset);
               }
            }
         }
      }

   }

   protected Aquifer createAquifer(ChunkAccess p_156162_) {
      return Aquifer.createDisabled(this.getSeaLevel(), Blocks.WATER.defaultBlockState());
   }

   @Nullable
   public BlockPos findNearestMapFeature(ServerLevel p_62162_, StructureFeature<?> p_62163_, BlockPos p_62164_, int p_62165_, boolean p_62166_) {
      if (!this.biomeSource.canGenerateStructure(p_62163_)) {
         return null;
      } else if (p_62163_ == StructureFeature.STRONGHOLD) {
         this.generateStrongholds();
         BlockPos blockpos = null;
         double d0 = Double.MAX_VALUE;
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

         for(ChunkPos chunkpos : this.strongholdPositions) {
            blockpos$mutableblockpos.set(SectionPos.sectionToBlockCoord(chunkpos.x, 8), 32, SectionPos.sectionToBlockCoord(chunkpos.z, 8));
            double d1 = blockpos$mutableblockpos.distSqr(p_62164_);
            if (blockpos == null) {
               blockpos = new BlockPos(blockpos$mutableblockpos);
               d0 = d1;
            } else if (d1 < d0) {
               blockpos = new BlockPos(blockpos$mutableblockpos);
               d0 = d1;
            }
         }

         return blockpos;
      } else {
         StructureFeatureConfiguration structurefeatureconfiguration = this.settings.getConfig(p_62163_);
         return structurefeatureconfiguration == null ? null : p_62163_.getNearestGeneratedFeature(p_62162_, p_62162_.structureFeatureManager(), p_62164_, p_62165_, p_62166_, p_62162_.getSeed(), structurefeatureconfiguration);
      }
   }

   public void applyBiomeDecoration(WorldGenRegion p_62168_, StructureFeatureManager p_62169_) {
      ChunkPos chunkpos = p_62168_.getCenter();
      int i = chunkpos.getMinBlockX();
      int j = chunkpos.getMinBlockZ();
      BlockPos blockpos = new BlockPos(i, p_62168_.getMinBuildHeight(), j);
      Biome biome = this.biomeSource.getPrimaryBiome(chunkpos);
      WorldgenRandom worldgenrandom = new WorldgenRandom();
      long k = worldgenrandom.setDecorationSeed(p_62168_.getSeed(), i, j);

      try {
         biome.generate(p_62169_, this, p_62168_, k, worldgenrandom, blockpos);
      } catch (Exception exception) {
         CrashReport crashreport = CrashReport.forThrowable(exception, "Biome decoration");
         crashreport.addCategory("Generation").setDetail("CenterX", chunkpos.x).setDetail("CenterZ", chunkpos.z).setDetail("Seed", k).setDetail("Biome", biome);
         throw new ReportedException(crashreport);
      }
   }

   public abstract void buildSurfaceAndBedrock(WorldGenRegion p_62170_, ChunkAccess p_62171_);

   public void spawnOriginalMobs(WorldGenRegion p_62167_) {
   }

   public StructureSettings getSettings() {
      return this.settings;
   }

   public int getSpawnHeight(LevelHeightAccessor p_156157_) {
      return 64;
   }

   public BiomeSource getBiomeSource() {
      return this.runtimeBiomeSource;
   }

   public int getGenDepth() {
      return 256;
   }

   public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Biome p_156158_, StructureFeatureManager p_156159_, MobCategory p_156160_, BlockPos p_156161_) {
      return p_156158_.getMobSettings().getMobs(p_156160_);
   }

   public void createStructures(RegistryAccess p_62200_, StructureFeatureManager p_62201_, ChunkAccess p_62202_, StructureManager p_62203_, long p_62204_) {
      Biome biome = this.biomeSource.getPrimaryBiome(p_62202_.getPos());
      this.createStructure(StructureFeatures.STRONGHOLD, p_62200_, p_62201_, p_62202_, p_62203_, p_62204_, biome);

      for(Supplier<ConfiguredStructureFeature<?, ?>> supplier : biome.getGenerationSettings().structures()) {
         this.createStructure(supplier.get(), p_62200_, p_62201_, p_62202_, p_62203_, p_62204_, biome);
      }

   }

   private void createStructure(ConfiguredStructureFeature<?, ?> p_156164_, RegistryAccess p_156165_, StructureFeatureManager p_156166_, ChunkAccess p_156167_, StructureManager p_156168_, long p_156169_, Biome p_156170_) {
      ChunkPos chunkpos = p_156167_.getPos();
      SectionPos sectionpos = SectionPos.bottomOf(p_156167_);
      StructureStart<?> structurestart = p_156166_.getStartForFeature(sectionpos, p_156164_.feature, p_156167_);
      int i = structurestart != null ? structurestart.getReferences() : 0;
      StructureFeatureConfiguration structurefeatureconfiguration = this.settings.getConfig(p_156164_.feature);
      if (structurefeatureconfiguration != null) {
         StructureStart<?> structurestart1 = p_156164_.generate(p_156165_, this, this.biomeSource, p_156168_, p_156169_, chunkpos, p_156170_, i, structurefeatureconfiguration, p_156167_);
         p_156166_.setStartForFeature(sectionpos, p_156164_.feature, structurestart1, p_156167_);
      }

   }

   public void createReferences(WorldGenLevel p_62178_, StructureFeatureManager p_62179_, ChunkAccess p_62180_) {
      int i = 8;
      ChunkPos chunkpos = p_62180_.getPos();
      int j = chunkpos.x;
      int k = chunkpos.z;
      int l = chunkpos.getMinBlockX();
      int i1 = chunkpos.getMinBlockZ();
      SectionPos sectionpos = SectionPos.bottomOf(p_62180_);

      for(int j1 = j - 8; j1 <= j + 8; ++j1) {
         for(int k1 = k - 8; k1 <= k + 8; ++k1) {
            long l1 = ChunkPos.asLong(j1, k1);

            for(StructureStart<?> structurestart : p_62178_.getChunk(j1, k1).getAllStarts().values()) {
               try {
                  if (structurestart.isValid() && structurestart.getBoundingBox().intersects(l, i1, l + 15, i1 + 15)) {
                     p_62179_.addReferenceForFeature(sectionpos, structurestart.getFeature(), l1, p_62180_);
                     DebugPackets.sendStructurePacket(p_62178_, structurestart);
                  }
               } catch (Exception exception) {
                  CrashReport crashreport = CrashReport.forThrowable(exception, "Generating structure reference");
                  CrashReportCategory crashreportcategory = crashreport.addCategory("Structure");
                  crashreportcategory.setDetail("Id", () -> {
                     return Registry.STRUCTURE_FEATURE.getKey(structurestart.getFeature()).toString();
                  });
                  crashreportcategory.setDetail("Name", () -> {
                     return structurestart.getFeature().getFeatureName();
                  });
                  crashreportcategory.setDetail("Class", () -> {
                     return structurestart.getFeature().getClass().getCanonicalName();
                  });
                  throw new ReportedException(crashreport);
               }
            }
         }
      }

   }

   public abstract CompletableFuture<ChunkAccess> fillFromNoise(Executor p_156171_, StructureFeatureManager p_156172_, ChunkAccess p_156173_);

   public int getSeaLevel() {
      return 63;
   }

   public int getMinY() {
      return 0;
   }

   public abstract int getBaseHeight(int p_156153_, int p_156154_, Heightmap.Types p_156155_, LevelHeightAccessor p_156156_);

   public abstract NoiseColumn getBaseColumn(int p_156150_, int p_156151_, LevelHeightAccessor p_156152_);

   public int getFirstFreeHeight(int p_156175_, int p_156176_, Heightmap.Types p_156177_, LevelHeightAccessor p_156178_) {
      return this.getBaseHeight(p_156175_, p_156176_, p_156177_, p_156178_);
   }

   public int getFirstOccupiedHeight(int p_156180_, int p_156181_, Heightmap.Types p_156182_, LevelHeightAccessor p_156183_) {
      return this.getBaseHeight(p_156180_, p_156181_, p_156182_, p_156183_) - 1;
   }

   public boolean hasStronghold(ChunkPos p_62173_) {
      this.generateStrongholds();
      return this.strongholdPositions.contains(p_62173_);
   }

   public BaseStoneSource getBaseStoneSource() {
      return this.defaultBaseStoneSource;
   }

   static {
      Registry.register(Registry.CHUNK_GENERATOR, "noise", NoiseBasedChunkGenerator.CODEC);
      Registry.register(Registry.CHUNK_GENERATOR, "flat", FlatLevelSource.CODEC);
      Registry.register(Registry.CHUNK_GENERATOR, "debug", DebugLevelSource.CODEC);
   }
}