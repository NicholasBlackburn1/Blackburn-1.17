package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;

public class DiskConfiguration implements FeatureConfiguration {
   public static final Codec<DiskConfiguration> CODEC = RecordCodecBuilder.create((p_67632_) -> {
      return p_67632_.group(BlockState.CODEC.fieldOf("state").forGetter((p_160757_) -> {
         return p_160757_.state;
      }), IntProvider.codec(0, 8).fieldOf("radius").forGetter((p_160755_) -> {
         return p_160755_.radius;
      }), Codec.intRange(0, 4).fieldOf("half_height").forGetter((p_160753_) -> {
         return p_160753_.halfHeight;
      }), BlockState.CODEC.listOf().fieldOf("targets").forGetter((p_160751_) -> {
         return p_160751_.targets;
      })).apply(p_67632_, DiskConfiguration::new);
   });
   public final BlockState state;
   public final IntProvider radius;
   public final int halfHeight;
   public final List<BlockState> targets;

   public DiskConfiguration(BlockState p_160746_, IntProvider p_160747_, int p_160748_, List<BlockState> p_160749_) {
      this.state = p_160746_;
      this.radius = p_160747_;
      this.halfHeight = p_160748_;
      this.targets = p_160749_;
   }
}