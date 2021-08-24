package net.minecraft.world.level.levelgen;

import java.util.Random;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class OreVeinifier {
   private static final float RARITY = 1.0F;
   private static final float RIDGE_NOISE_FREQUENCY = 4.0F;
   private static final float THICKNESS = 0.08F;
   private static final float VEININESS_THRESHOLD = 0.5F;
   private static final double VEININESS_FREQUENCY = 1.5D;
   private static final int EDGE_ROUNDOFF_BEGIN = 20;
   private static final double MAX_EDGE_ROUNDOFF = 0.2D;
   private static final float VEIN_SOLIDNESS = 0.7F;
   private static final float MIN_RICHNESS = 0.1F;
   private static final float MAX_RICHNESS = 0.3F;
   private static final float MAX_RICHNESS_THRESHOLD = 0.6F;
   private static final float CHANCE_OF_RAW_ORE_BLOCK = 0.02F;
   private static final float SKIP_ORE_IF_GAP_NOISE_IS_BELOW = -0.3F;
   private final int veinMaxY;
   private final int veinMinY;
   private final BlockState normalBlock;
   private final NormalNoise veininessNoiseSource;
   private final NormalNoise veinANoiseSource;
   private final NormalNoise veinBNoiseSource;
   private final NormalNoise gapNoise;
   private final int cellWidth;
   private final int cellHeight;

   public OreVeinifier(long p_158806_, BlockState p_158807_, int p_158808_, int p_158809_, int p_158810_) {
      Random random = new Random(p_158806_);
      this.normalBlock = p_158807_;
      this.veininessNoiseSource = NormalNoise.create(new SimpleRandomSource(random.nextLong()), -8, 1.0D);
      this.veinANoiseSource = NormalNoise.create(new SimpleRandomSource(random.nextLong()), -7, 1.0D);
      this.veinBNoiseSource = NormalNoise.create(new SimpleRandomSource(random.nextLong()), -7, 1.0D);
      this.gapNoise = NormalNoise.create(new SimpleRandomSource(0L), -5, 1.0D);
      this.cellWidth = p_158808_;
      this.cellHeight = p_158809_;
      this.veinMaxY = Stream.of(OreVeinifier.VeinType.values()).mapToInt((p_158842_) -> {
         return p_158842_.maxY;
      }).max().orElse(p_158810_);
      this.veinMinY = Stream.of(OreVeinifier.VeinType.values()).mapToInt((p_158818_) -> {
         return p_158818_.minY;
      }).min().orElse(p_158810_);
   }

   public void fillVeininessNoiseColumn(double[] p_158828_, int p_158829_, int p_158830_, int p_158831_, int p_158832_) {
      this.fillNoiseColumn(p_158828_, p_158829_, p_158830_, this.veininessNoiseSource, 1.5D, p_158831_, p_158832_);
   }

   public void fillNoiseColumnA(double[] p_158844_, int p_158845_, int p_158846_, int p_158847_, int p_158848_) {
      this.fillNoiseColumn(p_158844_, p_158845_, p_158846_, this.veinANoiseSource, 4.0D, p_158847_, p_158848_);
   }

   public void fillNoiseColumnB(double[] p_158850_, int p_158851_, int p_158852_, int p_158853_, int p_158854_) {
      this.fillNoiseColumn(p_158850_, p_158851_, p_158852_, this.veinBNoiseSource, 4.0D, p_158853_, p_158854_);
   }

   public void fillNoiseColumn(double[] p_158834_, int p_158835_, int p_158836_, NormalNoise p_158837_, double p_158838_, int p_158839_, int p_158840_) {
      for(int i = 0; i < p_158840_; ++i) {
         int j = i + p_158839_;
         int k = p_158835_ * this.cellWidth;
         int l = j * this.cellHeight;
         int i1 = p_158836_ * this.cellWidth;
         double d0;
         if (l >= this.veinMinY && l <= this.veinMaxY) {
            d0 = p_158837_.getValue((double)k * p_158838_, (double)l * p_158838_, (double)i1 * p_158838_);
         } else {
            d0 = 0.0D;
         }

         p_158834_[i] = d0;
      }

   }

   public BlockState oreVeinify(RandomSource p_158820_, int p_158821_, int p_158822_, int p_158823_, double p_158824_, double p_158825_, double p_158826_) {
      BlockState blockstate = this.normalBlock;
      OreVeinifier.VeinType oreveinifier$veintype = this.getVeinType(p_158824_, p_158822_);
      if (oreveinifier$veintype == null) {
         return blockstate;
      } else if (p_158820_.nextFloat() > 0.7F) {
         return blockstate;
      } else if (this.isVein(p_158825_, p_158826_)) {
         double d0 = Mth.clampedMap(Math.abs(p_158824_), 0.5D, (double)0.6F, (double)0.1F, (double)0.3F);
         if ((double)p_158820_.nextFloat() < d0 && this.gapNoise.getValue((double)p_158821_, (double)p_158822_, (double)p_158823_) > (double)-0.3F) {
            return p_158820_.nextFloat() < 0.02F ? oreveinifier$veintype.rawOreBlock : oreveinifier$veintype.ore;
         } else {
            return oreveinifier$veintype.filler;
         }
      } else {
         return blockstate;
      }
   }

   private boolean isVein(double p_158812_, double p_158813_) {
      double d0 = Math.abs(1.0D * p_158812_) - (double)0.08F;
      double d1 = Math.abs(1.0D * p_158813_) - (double)0.08F;
      return Math.max(d0, d1) < 0.0D;
   }

   @Nullable
   private OreVeinifier.VeinType getVeinType(double p_158815_, int p_158816_) {
      OreVeinifier.VeinType oreveinifier$veintype = p_158815_ > 0.0D ? OreVeinifier.VeinType.COPPER : OreVeinifier.VeinType.IRON;
      int i = oreveinifier$veintype.maxY - p_158816_;
      int j = p_158816_ - oreveinifier$veintype.minY;
      if (j >= 0 && i >= 0) {
         int k = Math.min(i, j);
         double d0 = Mth.clampedMap((double)k, 0.0D, 20.0D, -0.2D, 0.0D);
         return Math.abs(p_158815_) + d0 < 0.5D ? null : oreveinifier$veintype;
      } else {
         return null;
      }
   }

   static enum VeinType {
      COPPER(Blocks.COPPER_ORE.defaultBlockState(), Blocks.RAW_COPPER_BLOCK.defaultBlockState(), Blocks.GRANITE.defaultBlockState(), 0, 50),
      IRON(Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(), Blocks.RAW_IRON_BLOCK.defaultBlockState(), Blocks.TUFF.defaultBlockState(), -60, -8);

      final BlockState ore;
      final BlockState rawOreBlock;
      final BlockState filler;
      final int minY;
      final int maxY;

      private VeinType(BlockState p_158867_, BlockState p_158868_, BlockState p_158869_, int p_158870_, int p_158871_) {
         this.ore = p_158867_;
         this.rawOreBlock = p_158868_;
         this.filler = p_158869_;
         this.minY = p_158870_;
         this.maxY = p_158871_;
      }
   }
}