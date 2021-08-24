package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class RandomPatchConfiguration implements FeatureConfiguration {
   public static final Codec<RandomPatchConfiguration> CODEC = RecordCodecBuilder.create((p_67955_) -> {
      return p_67955_.group(BlockStateProvider.CODEC.fieldOf("state_provider").forGetter((p_161075_) -> {
         return p_161075_.stateProvider;
      }), BlockPlacer.CODEC.fieldOf("block_placer").forGetter((p_161073_) -> {
         return p_161073_.blockPlacer;
      }), BlockState.CODEC.listOf().fieldOf("whitelist").forGetter((p_161071_) -> {
         return p_161071_.whitelist.stream().map(Block::defaultBlockState).collect(Collectors.toList());
      }), BlockState.CODEC.listOf().fieldOf("blacklist").forGetter((p_161069_) -> {
         return ImmutableList.copyOf(p_161069_.blacklist);
      }), ExtraCodecs.POSITIVE_INT.fieldOf("tries").orElse(128).forGetter((p_161067_) -> {
         return p_161067_.tries;
      }), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("xspread").orElse(7).forGetter((p_161065_) -> {
         return p_161065_.xspread;
      }), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("yspread").orElse(3).forGetter((p_161063_) -> {
         return p_161063_.yspread;
      }), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("zspread").orElse(7).forGetter((p_161061_) -> {
         return p_161061_.zspread;
      }), Codec.BOOL.fieldOf("can_replace").orElse(false).forGetter((p_161059_) -> {
         return p_161059_.canReplace;
      }), Codec.BOOL.fieldOf("project").orElse(true).forGetter((p_161057_) -> {
         return p_161057_.project;
      }), Codec.BOOL.fieldOf("need_water").orElse(false).forGetter((p_161055_) -> {
         return p_161055_.needWater;
      })).apply(p_67955_, RandomPatchConfiguration::new);
   });
   public final BlockStateProvider stateProvider;
   public final BlockPlacer blockPlacer;
   public final Set<Block> whitelist;
   public final Set<BlockState> blacklist;
   public final int tries;
   public final int xspread;
   public final int yspread;
   public final int zspread;
   public final boolean canReplace;
   public final boolean project;
   public final boolean needWater;

   private RandomPatchConfiguration(BlockStateProvider p_67916_, BlockPlacer p_67917_, List<BlockState> p_67918_, List<BlockState> p_67919_, int p_67920_, int p_67921_, int p_67922_, int p_67923_, boolean p_67924_, boolean p_67925_, boolean p_67926_) {
      this(p_67916_, p_67917_, p_67918_.stream().map(BlockBehaviour.BlockStateBase::getBlock).collect(Collectors.toSet()), ImmutableSet.copyOf(p_67919_), p_67920_, p_67921_, p_67922_, p_67923_, p_67924_, p_67925_, p_67926_);
   }

   RandomPatchConfiguration(BlockStateProvider p_67928_, BlockPlacer p_67929_, Set<Block> p_67930_, Set<BlockState> p_67931_, int p_67932_, int p_67933_, int p_67934_, int p_67935_, boolean p_67936_, boolean p_67937_, boolean p_67938_) {
      this.stateProvider = p_67928_;
      this.blockPlacer = p_67929_;
      this.whitelist = p_67930_;
      this.blacklist = p_67931_;
      this.tries = p_67932_;
      this.xspread = p_67933_;
      this.yspread = p_67934_;
      this.zspread = p_67935_;
      this.canReplace = p_67936_;
      this.project = p_67937_;
      this.needWater = p_67938_;
   }

   public static class GrassConfigurationBuilder {
      private final BlockStateProvider stateProvider;
      private final BlockPlacer blockPlacer;
      private Set<Block> whitelist = ImmutableSet.of();
      private Set<BlockState> blacklist = ImmutableSet.of();
      private int tries = 64;
      private int xspread = 7;
      private int yspread = 3;
      private int zspread = 7;
      private boolean canReplace;
      private boolean project = true;
      private boolean needWater;

      public GrassConfigurationBuilder(BlockStateProvider p_67988_, BlockPlacer p_67989_) {
         this.stateProvider = p_67988_;
         this.blockPlacer = p_67989_;
      }

      public RandomPatchConfiguration.GrassConfigurationBuilder whitelist(Set<Block> p_67994_) {
         this.whitelist = p_67994_;
         return this;
      }

      public RandomPatchConfiguration.GrassConfigurationBuilder blacklist(Set<BlockState> p_67999_) {
         this.blacklist = p_67999_;
         return this;
      }

      public RandomPatchConfiguration.GrassConfigurationBuilder tries(int p_67992_) {
         this.tries = p_67992_;
         return this;
      }

      public RandomPatchConfiguration.GrassConfigurationBuilder xspread(int p_67997_) {
         this.xspread = p_67997_;
         return this;
      }

      public RandomPatchConfiguration.GrassConfigurationBuilder yspread(int p_68002_) {
         this.yspread = p_68002_;
         return this;
      }

      public RandomPatchConfiguration.GrassConfigurationBuilder zspread(int p_68005_) {
         this.zspread = p_68005_;
         return this;
      }

      public RandomPatchConfiguration.GrassConfigurationBuilder canReplace() {
         this.canReplace = true;
         return this;
      }

      public RandomPatchConfiguration.GrassConfigurationBuilder noProjection() {
         this.project = false;
         return this;
      }

      public RandomPatchConfiguration.GrassConfigurationBuilder needWater() {
         this.needWater = true;
         return this;
      }

      public RandomPatchConfiguration build() {
         return new RandomPatchConfiguration(this.stateProvider, this.blockPlacer, this.whitelist, this.blacklist, this.tries, this.xspread, this.yspread, this.zspread, this.canReplace, this.project, this.needWater);
      }
   }
}