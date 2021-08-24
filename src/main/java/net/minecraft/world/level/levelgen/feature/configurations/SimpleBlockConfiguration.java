package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class SimpleBlockConfiguration implements FeatureConfiguration {
   public static final Codec<SimpleBlockConfiguration> CODEC = RecordCodecBuilder.create((p_68082_) -> {
      return p_68082_.group(BlockStateProvider.CODEC.fieldOf("to_place").forGetter((p_161168_) -> {
         return p_161168_.toPlace;
      }), BlockState.CODEC.listOf().fieldOf("place_on").orElse(ImmutableList.of()).forGetter((p_161166_) -> {
         return p_161166_.placeOn;
      }), BlockState.CODEC.listOf().fieldOf("place_in").orElse(ImmutableList.of()).forGetter((p_161164_) -> {
         return p_161164_.placeIn;
      }), BlockState.CODEC.listOf().fieldOf("place_under").orElse(ImmutableList.of()).forGetter((p_161162_) -> {
         return p_161162_.placeUnder;
      })).apply(p_68082_, SimpleBlockConfiguration::new);
   });
   public final BlockStateProvider toPlace;
   public final List<BlockState> placeOn;
   public final List<BlockState> placeIn;
   public final List<BlockState> placeUnder;

   public SimpleBlockConfiguration(BlockStateProvider p_161157_, List<BlockState> p_161158_, List<BlockState> p_161159_, List<BlockState> p_161160_) {
      this.toPlace = p_161157_;
      this.placeOn = p_161158_;
      this.placeIn = p_161159_;
      this.placeUnder = p_161160_;
   }

   public SimpleBlockConfiguration(BlockStateProvider p_161155_) {
      this(p_161155_, ImmutableList.of(), ImmutableList.of(), ImmutableList.of());
   }
}