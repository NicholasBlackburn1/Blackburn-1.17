package net.minecraft.world.level.chunk;

import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.IdMap;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkBiomeContainer implements BiomeManager.NoiseBiomeSource {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int WIDTH_BITS = Mth.ceillog2(16) - 2;
   private static final int HORIZONTAL_MASK = (1 << WIDTH_BITS) - 1;
   public static final int MAX_SIZE = 1 << WIDTH_BITS + WIDTH_BITS + DimensionType.BITS_FOR_Y - 2;
   private final IdMap<Biome> biomeRegistry;
   private final Biome[] biomes;
   private final int quartMinY;
   private final int quartHeight;

   protected ChunkBiomeContainer(IdMap<Biome> p_156137_, LevelHeightAccessor p_156138_, Biome[] p_156139_) {
      this.biomeRegistry = p_156137_;
      this.biomes = p_156139_;
      this.quartMinY = QuartPos.fromBlock(p_156138_.getMinBuildHeight());
      this.quartHeight = QuartPos.fromBlock(p_156138_.getHeight()) - 1;
   }

   public ChunkBiomeContainer(IdMap<Biome> p_156133_, LevelHeightAccessor p_156134_, int[] p_156135_) {
      this(p_156133_, p_156134_, new Biome[p_156135_.length]);
      int i = -1;

      for(int j = 0; j < this.biomes.length; ++j) {
         int k = p_156135_[j];
         Biome biome = p_156133_.byId(k);
         if (biome == null) {
            if (i == -1) {
               i = j;
            }

            this.biomes[j] = p_156133_.byId(0);
         } else {
            this.biomes[j] = biome;
         }
      }

      if (i != -1) {
         LOGGER.warn("Invalid biome data received, starting from {}: {}", i, Arrays.toString(p_156135_));
      }

   }

   public ChunkBiomeContainer(IdMap<Biome> p_156122_, LevelHeightAccessor p_156123_, ChunkPos p_156124_, BiomeSource p_156125_) {
      this(p_156122_, p_156123_, p_156124_, p_156125_, (int[])null);
   }

   public ChunkBiomeContainer(IdMap<Biome> p_156127_, LevelHeightAccessor p_156128_, ChunkPos p_156129_, BiomeSource p_156130_, @Nullable int[] p_156131_) {
      this(p_156127_, p_156128_, new Biome[(1 << WIDTH_BITS + WIDTH_BITS) * ceilDiv(p_156128_.getHeight(), 4)]);
      int i = QuartPos.fromBlock(p_156129_.getMinBlockX());
      int j = this.quartMinY;
      int k = QuartPos.fromBlock(p_156129_.getMinBlockZ());

      for(int l = 0; l < this.biomes.length; ++l) {
         if (p_156131_ != null && l < p_156131_.length) {
            this.biomes[l] = p_156127_.byId(p_156131_[l]);
         }

         if (this.biomes[l] == null) {
            this.biomes[l] = generateBiomeForIndex(p_156130_, i, j, k, l);
         }
      }

   }

   private static int ceilDiv(int p_156141_, int p_156142_) {
      return (p_156141_ + p_156142_ - 1) / p_156142_;
   }

   private static Biome generateBiomeForIndex(BiomeSource p_156144_, int p_156145_, int p_156146_, int p_156147_, int p_156148_) {
      int i = p_156148_ & HORIZONTAL_MASK;
      int j = p_156148_ >> WIDTH_BITS + WIDTH_BITS;
      int k = p_156148_ >> WIDTH_BITS & HORIZONTAL_MASK;
      return p_156144_.getNoiseBiome(p_156145_ + i, p_156146_ + j, p_156147_ + k);
   }

   public int[] writeBiomes() {
      int[] aint = new int[this.biomes.length];

      for(int i = 0; i < this.biomes.length; ++i) {
         aint[i] = this.biomeRegistry.getId(this.biomes[i]);
      }

      return aint;
   }

   public Biome getNoiseBiome(int p_62133_, int p_62134_, int p_62135_) {
      int i = p_62133_ & HORIZONTAL_MASK;
      int j = Mth.clamp(p_62134_ - this.quartMinY, 0, this.quartHeight);
      int k = p_62135_ & HORIZONTAL_MASK;
      return this.biomes[j << WIDTH_BITS + WIDTH_BITS | k << WIDTH_BITS | i];
   }
}