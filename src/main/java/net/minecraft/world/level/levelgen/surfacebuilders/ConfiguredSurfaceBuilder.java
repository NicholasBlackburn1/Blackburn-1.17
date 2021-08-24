package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class ConfiguredSurfaceBuilder<SC extends SurfaceBuilderConfiguration> {
   public static final Codec<ConfiguredSurfaceBuilder<?>> DIRECT_CODEC = Registry.SURFACE_BUILDER.dispatch((p_74774_) -> {
      return p_74774_.surfaceBuilder;
   }, SurfaceBuilder::configuredCodec);
   public static final Codec<Supplier<ConfiguredSurfaceBuilder<?>>> CODEC = RegistryFileCodec.create(Registry.CONFIGURED_SURFACE_BUILDER_REGISTRY, DIRECT_CODEC);
   public final SurfaceBuilder<SC> surfaceBuilder;
   public final SC config;

   public ConfiguredSurfaceBuilder(SurfaceBuilder<SC> p_74768_, SC p_74769_) {
      this.surfaceBuilder = p_74768_;
      this.config = p_74769_;
   }

   public void apply(Random p_163849_, ChunkAccess p_163850_, Biome p_163851_, int p_163852_, int p_163853_, int p_163854_, double p_163855_, BlockState p_163856_, BlockState p_163857_, int p_163858_, int p_163859_, long p_163860_) {
      this.surfaceBuilder.apply(p_163849_, p_163850_, p_163851_, p_163852_, p_163853_, p_163854_, p_163855_, p_163856_, p_163857_, p_163858_, p_163859_, p_163860_, this.config);
   }

   public void initNoise(long p_74772_) {
      this.surfaceBuilder.initNoise(p_74772_);
   }

   public SC config() {
      return this.config;
   }
}