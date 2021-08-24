package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;

public class ConfiguredWorldCarver<WC extends CarverConfiguration> {
   public static final Codec<ConfiguredWorldCarver<?>> DIRECT_CODEC = Registry.CARVER.dispatch((p_64867_) -> {
      return p_64867_.worldCarver;
   }, WorldCarver::configuredCodec);
   public static final Codec<Supplier<ConfiguredWorldCarver<?>>> CODEC = RegistryFileCodec.create(Registry.CONFIGURED_CARVER_REGISTRY, DIRECT_CODEC);
   public static final Codec<List<Supplier<ConfiguredWorldCarver<?>>>> LIST_CODEC = RegistryFileCodec.homogeneousList(Registry.CONFIGURED_CARVER_REGISTRY, DIRECT_CODEC);
   private final WorldCarver<WC> worldCarver;
   private final WC config;

   public ConfiguredWorldCarver(WorldCarver<WC> p_64853_, WC p_64854_) {
      this.worldCarver = p_64853_;
      this.config = p_64854_;
   }

   public WC config() {
      return this.config;
   }

   public boolean isStartChunk(Random p_159274_) {
      return this.worldCarver.isStartChunk(this.config, p_159274_);
   }

   public boolean carve(CarvingContext p_159266_, ChunkAccess p_159267_, Function<BlockPos, Biome> p_159268_, Random p_159269_, Aquifer p_159270_, ChunkPos p_159271_, BitSet p_159272_) {
      return this.worldCarver.carve(p_159266_, this.config, p_159267_, p_159268_, p_159269_, p_159270_, p_159271_, p_159272_);
   }
}