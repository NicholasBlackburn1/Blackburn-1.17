package net.minecraft.world.level.biome;

import com.google.common.hash.Hashing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

public class BiomeManager {
   static final int CHUNK_CENTER_QUART = QuartPos.fromBlock(8);
   private final BiomeManager.NoiseBiomeSource noiseBiomeSource;
   private final long biomeZoomSeed;
   private final BiomeZoomer zoomer;

   public BiomeManager(BiomeManager.NoiseBiomeSource p_47866_, long p_47867_, BiomeZoomer p_47868_) {
      this.noiseBiomeSource = p_47866_;
      this.biomeZoomSeed = p_47867_;
      this.zoomer = p_47868_;
   }

   public static long obfuscateSeed(long p_47878_) {
      return Hashing.sha256().hashLong(p_47878_).asLong();
   }

   public BiomeManager withDifferentSource(BiomeSource p_47880_) {
      return new BiomeManager(p_47880_, this.biomeZoomSeed, this.zoomer);
   }

   public Biome getBiome(BlockPos p_47882_) {
      return this.zoomer.getBiome(this.biomeZoomSeed, p_47882_.getX(), p_47882_.getY(), p_47882_.getZ(), this.noiseBiomeSource);
   }

   public Biome getNoiseBiomeAtPosition(double p_47870_, double p_47871_, double p_47872_) {
      int i = QuartPos.fromBlock(Mth.floor(p_47870_));
      int j = QuartPos.fromBlock(Mth.floor(p_47871_));
      int k = QuartPos.fromBlock(Mth.floor(p_47872_));
      return this.getNoiseBiomeAtQuart(i, j, k);
   }

   public Biome getNoiseBiomeAtPosition(BlockPos p_47884_) {
      int i = QuartPos.fromBlock(p_47884_.getX());
      int j = QuartPos.fromBlock(p_47884_.getY());
      int k = QuartPos.fromBlock(p_47884_.getZ());
      return this.getNoiseBiomeAtQuart(i, j, k);
   }

   public Biome getNoiseBiomeAtQuart(int p_47874_, int p_47875_, int p_47876_) {
      return this.noiseBiomeSource.getNoiseBiome(p_47874_, p_47875_, p_47876_);
   }

   public Biome getPrimaryBiomeAtChunk(ChunkPos p_151753_) {
      return this.noiseBiomeSource.getPrimaryBiome(p_151753_);
   }

   public interface NoiseBiomeSource {
      Biome getNoiseBiome(int p_47885_, int p_47886_, int p_47887_);

      default Biome getPrimaryBiome(ChunkPos p_151755_) {
         return this.getNoiseBiome(QuartPos.fromSection(p_151755_.x) + BiomeManager.CHUNK_CENTER_QUART, 0, QuartPos.fromSection(p_151755_.z) + BiomeManager.CHUNK_CENTER_QUART);
      }
   }
}