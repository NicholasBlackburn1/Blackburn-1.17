package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;

public class TreeConfiguration implements FeatureConfiguration {
   public static final Codec<TreeConfiguration> CODEC = RecordCodecBuilder.create((p_161228_) -> {
      return p_161228_.group(BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter((p_161248_) -> {
         return p_161248_.trunkProvider;
      }), TrunkPlacer.CODEC.fieldOf("trunk_placer").forGetter((p_161246_) -> {
         return p_161246_.trunkPlacer;
      }), BlockStateProvider.CODEC.fieldOf("foliage_provider").forGetter((p_161244_) -> {
         return p_161244_.foliageProvider;
      }), BlockStateProvider.CODEC.fieldOf("sapling_provider").forGetter((p_161242_) -> {
         return p_161242_.saplingProvider;
      }), FoliagePlacer.CODEC.fieldOf("foliage_placer").forGetter((p_161240_) -> {
         return p_161240_.foliagePlacer;
      }), BlockStateProvider.CODEC.fieldOf("dirt_provider").forGetter((p_161238_) -> {
         return p_161238_.dirtProvider;
      }), FeatureSize.CODEC.fieldOf("minimum_size").forGetter((p_161236_) -> {
         return p_161236_.minimumSize;
      }), TreeDecorator.CODEC.listOf().fieldOf("decorators").forGetter((p_161234_) -> {
         return p_161234_.decorators;
      }), Codec.BOOL.fieldOf("ignore_vines").orElse(false).forGetter((p_161232_) -> {
         return p_161232_.ignoreVines;
      }), Codec.BOOL.fieldOf("force_dirt").orElse(false).forGetter((p_161230_) -> {
         return p_161230_.forceDirt;
      })).apply(p_161228_, TreeConfiguration::new);
   });
   public final BlockStateProvider trunkProvider;
   public final BlockStateProvider dirtProvider;
   public final TrunkPlacer trunkPlacer;
   public final BlockStateProvider foliageProvider;
   public final BlockStateProvider saplingProvider;
   public final FoliagePlacer foliagePlacer;
   public final FeatureSize minimumSize;
   public final List<TreeDecorator> decorators;
   public final boolean ignoreVines;
   public final boolean forceDirt;

   protected TreeConfiguration(BlockStateProvider p_161217_, TrunkPlacer p_161218_, BlockStateProvider p_161219_, BlockStateProvider p_161220_, FoliagePlacer p_161221_, BlockStateProvider p_161222_, FeatureSize p_161223_, List<TreeDecorator> p_161224_, boolean p_161225_, boolean p_161226_) {
      this.trunkProvider = p_161217_;
      this.trunkPlacer = p_161218_;
      this.foliageProvider = p_161219_;
      this.foliagePlacer = p_161221_;
      this.dirtProvider = p_161222_;
      this.saplingProvider = p_161220_;
      this.minimumSize = p_161223_;
      this.decorators = p_161224_;
      this.ignoreVines = p_161225_;
      this.forceDirt = p_161226_;
   }

   public TreeConfiguration withDecorators(List<TreeDecorator> p_68211_) {
      return new TreeConfiguration(this.trunkProvider, this.trunkPlacer, this.foliageProvider, this.saplingProvider, this.foliagePlacer, this.dirtProvider, this.minimumSize, p_68211_, this.ignoreVines, this.forceDirt);
   }

   public static class TreeConfigurationBuilder {
      public final BlockStateProvider trunkProvider;
      private final TrunkPlacer trunkPlacer;
      public final BlockStateProvider foliageProvider;
      public final BlockStateProvider saplingProvider;
      private final FoliagePlacer foliagePlacer;
      private BlockStateProvider dirtProvider;
      private final FeatureSize minimumSize;
      private List<TreeDecorator> decorators = ImmutableList.of();
      private boolean ignoreVines;
      private boolean forceDirt;

      public TreeConfigurationBuilder(BlockStateProvider p_161254_, TrunkPlacer p_161255_, BlockStateProvider p_161256_, BlockStateProvider p_161257_, FoliagePlacer p_161258_, FeatureSize p_161259_) {
         this.trunkProvider = p_161254_;
         this.trunkPlacer = p_161255_;
         this.foliageProvider = p_161256_;
         this.saplingProvider = p_161257_;
         this.dirtProvider = new SimpleStateProvider(Blocks.DIRT.defaultBlockState());
         this.foliagePlacer = p_161258_;
         this.minimumSize = p_161259_;
      }

      public TreeConfiguration.TreeConfigurationBuilder dirt(BlockStateProvider p_161261_) {
         this.dirtProvider = p_161261_;
         return this;
      }

      public TreeConfiguration.TreeConfigurationBuilder decorators(List<TreeDecorator> p_68250_) {
         this.decorators = p_68250_;
         return this;
      }

      public TreeConfiguration.TreeConfigurationBuilder ignoreVines() {
         this.ignoreVines = true;
         return this;
      }

      public TreeConfiguration.TreeConfigurationBuilder forceDirt() {
         this.forceDirt = true;
         return this;
      }

      public TreeConfiguration build() {
         return new TreeConfiguration(this.trunkProvider, this.trunkPlacer, this.foliageProvider, this.saplingProvider, this.foliagePlacer, this.dirtProvider, this.minimumSize, this.decorators, this.ignoreVines, this.forceDirt);
      }
   }
}