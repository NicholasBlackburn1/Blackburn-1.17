package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GlowLichenConfiguration implements FeatureConfiguration {
   public static final Codec<GlowLichenConfiguration> CODEC = RecordCodecBuilder.create((p_160891_) -> {
      return p_160891_.group(Codec.intRange(1, 64).fieldOf("search_range").orElse(10).forGetter((p_160903_) -> {
         return p_160903_.searchRange;
      }), Codec.BOOL.fieldOf("can_place_on_floor").orElse(false).forGetter((p_160901_) -> {
         return p_160901_.canPlaceOnFloor;
      }), Codec.BOOL.fieldOf("can_place_on_ceiling").orElse(false).forGetter((p_160899_) -> {
         return p_160899_.canPlaceOnCeiling;
      }), Codec.BOOL.fieldOf("can_place_on_wall").orElse(false).forGetter((p_160897_) -> {
         return p_160897_.canPlaceOnWall;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_spreading").orElse(0.5F).forGetter((p_160895_) -> {
         return p_160895_.chanceOfSpreading;
      }), BlockState.CODEC.listOf().fieldOf("can_be_placed_on").forGetter((p_160893_) -> {
         return new ArrayList<>(p_160893_.canBePlacedOn);
      })).apply(p_160891_, GlowLichenConfiguration::new);
   });
   public final int searchRange;
   public final boolean canPlaceOnFloor;
   public final boolean canPlaceOnCeiling;
   public final boolean canPlaceOnWall;
   public final float chanceOfSpreading;
   public final List<BlockState> canBePlacedOn;
   public final List<Direction> validDirections;

   public GlowLichenConfiguration(int p_160879_, boolean p_160880_, boolean p_160881_, boolean p_160882_, float p_160883_, List<BlockState> p_160884_) {
      this.searchRange = p_160879_;
      this.canPlaceOnFloor = p_160880_;
      this.canPlaceOnCeiling = p_160881_;
      this.canPlaceOnWall = p_160882_;
      this.chanceOfSpreading = p_160883_;
      this.canBePlacedOn = p_160884_;
      List<Direction> list = Lists.newArrayList();
      if (p_160881_) {
         list.add(Direction.UP);
      }

      if (p_160880_) {
         list.add(Direction.DOWN);
      }

      if (p_160882_) {
         Direction.Plane.HORIZONTAL.forEach(list::add);
      }

      this.validDirections = Collections.unmodifiableList(list);
   }

   public boolean canBePlacedOn(Block p_160886_) {
      return this.canBePlacedOn.stream().anyMatch((p_160889_) -> {
         return p_160889_.is(p_160886_);
      });
   }
}