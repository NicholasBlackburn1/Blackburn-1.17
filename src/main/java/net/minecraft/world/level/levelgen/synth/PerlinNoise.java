package net.minecraft.world.level.levelgen.synth;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.List;
import java.util.function.LongFunction;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class PerlinNoise implements SurfaceNoise {
   private static final int ROUND_OFF = 33554432;
   private final ImprovedNoise[] noiseLevels;
   private final DoubleList amplitudes;
   private final double lowestFreqValueFactor;
   private final double lowestFreqInputFactor;

   public PerlinNoise(RandomSource p_164377_, IntStream p_164378_) {
      this(p_164377_, p_164378_.boxed().collect(ImmutableList.toImmutableList()));
   }

   public PerlinNoise(RandomSource p_164374_, List<Integer> p_164375_) {
      this(p_164374_, new IntRBTreeSet(p_164375_));
   }

   public static PerlinNoise create(RandomSource p_164386_, int p_164387_, double... p_164388_) {
      return create(p_164386_, p_164387_, new DoubleArrayList(p_164388_));
   }

   public static PerlinNoise create(RandomSource p_164382_, int p_164383_, DoubleList p_164384_) {
      return new PerlinNoise(p_164382_, Pair.of(p_164383_, p_164384_));
   }

   private static Pair<Integer, DoubleList> makeAmplitudes(IntSortedSet p_75431_) {
      if (p_75431_.isEmpty()) {
         throw new IllegalArgumentException("Need some octaves!");
      } else {
         int i = -p_75431_.firstInt();
         int j = p_75431_.lastInt();
         int k = i + j + 1;
         if (k < 1) {
            throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
         } else {
            DoubleList doublelist = new DoubleArrayList(new double[k]);
            IntBidirectionalIterator intbidirectionaliterator = p_75431_.iterator();

            while(intbidirectionaliterator.hasNext()) {
               int l = intbidirectionaliterator.nextInt();
               doublelist.set(l + i, 1.0D);
            }

            return Pair.of(-i, doublelist);
         }
      }
   }

   private PerlinNoise(RandomSource p_164367_, IntSortedSet p_164368_) {
      this(p_164367_, p_164368_, WorldgenRandom::new);
   }

   private PerlinNoise(RandomSource p_164370_, IntSortedSet p_164371_, LongFunction<RandomSource> p_164372_) {
      this(p_164370_, makeAmplitudes(p_164371_), p_164372_);
   }

   protected PerlinNoise(RandomSource p_164360_, Pair<Integer, DoubleList> p_164361_) {
      this(p_164360_, p_164361_, WorldgenRandom::new);
   }

   protected PerlinNoise(RandomSource p_164363_, Pair<Integer, DoubleList> p_164364_, LongFunction<RandomSource> p_164365_) {
      int i = p_164364_.getFirst();
      this.amplitudes = p_164364_.getSecond();
      ImprovedNoise improvednoise = new ImprovedNoise(p_164363_);
      int j = this.amplitudes.size();
      int k = -i;
      this.noiseLevels = new ImprovedNoise[j];
      if (k >= 0 && k < j) {
         double d0 = this.amplitudes.getDouble(k);
         if (d0 != 0.0D) {
            this.noiseLevels[k] = improvednoise;
         }
      }

      for(int l = k - 1; l >= 0; --l) {
         if (l < j) {
            double d1 = this.amplitudes.getDouble(l);
            if (d1 != 0.0D) {
               this.noiseLevels[l] = new ImprovedNoise(p_164363_);
            } else {
               skipOctave(p_164363_);
            }
         } else {
            skipOctave(p_164363_);
         }
      }

      if (k < j - 1) {
         throw new IllegalArgumentException("Positive octaves are temporarily disabled");
      } else {
         this.lowestFreqInputFactor = Math.pow(2.0D, (double)(-k));
         this.lowestFreqValueFactor = Math.pow(2.0D, (double)(j - 1)) / (Math.pow(2.0D, (double)j) - 1.0D);
      }
   }

   private static void skipOctave(RandomSource p_164380_) {
      p_164380_.consumeCount(262);
   }

   public double getValue(double p_75409_, double p_75410_, double p_75411_) {
      return this.getValue(p_75409_, p_75410_, p_75411_, 0.0D, 0.0D, false);
   }

   @Deprecated
   public double getValue(double p_75418_, double p_75419_, double p_75420_, double p_75421_, double p_75422_, boolean p_75423_) {
      double d0 = 0.0D;
      double d1 = this.lowestFreqInputFactor;
      double d2 = this.lowestFreqValueFactor;

      for(int i = 0; i < this.noiseLevels.length; ++i) {
         ImprovedNoise improvednoise = this.noiseLevels[i];
         if (improvednoise != null) {
            double d3 = improvednoise.noise(wrap(p_75418_ * d1), p_75423_ ? -improvednoise.yo : wrap(p_75419_ * d1), wrap(p_75420_ * d1), p_75421_ * d1, p_75422_ * d1);
            d0 += this.amplitudes.getDouble(i) * d3 * d2;
         }

         d1 *= 2.0D;
         d2 /= 2.0D;
      }

      return d0;
   }

   @Nullable
   public ImprovedNoise getOctaveNoise(int p_75425_) {
      return this.noiseLevels[this.noiseLevels.length - 1 - p_75425_];
   }

   public static double wrap(double p_75407_) {
      return p_75407_ - (double)Mth.lfloor(p_75407_ / 3.3554432E7D + 0.5D) * 3.3554432E7D;
   }

   public double getSurfaceNoiseValue(double p_75413_, double p_75414_, double p_75415_, double p_75416_) {
      return this.getValue(p_75413_, p_75414_, 0.0D, p_75415_, p_75416_, false);
   }
}