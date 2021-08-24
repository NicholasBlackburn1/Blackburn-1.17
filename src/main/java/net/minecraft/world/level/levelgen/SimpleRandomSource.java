package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.util.Pair;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.util.DebugBuffer;
import net.minecraft.util.Mth;
import net.minecraft.util.ThreadingDetector;

public class SimpleRandomSource implements RandomSource {
   private static final int MODULUS_BITS = 48;
   private static final long MODULUS_MASK = 281474976710655L;
   private static final long MULTIPLIER = 25214903917L;
   private static final long INCREMENT = 11L;
   private static final float FLOAT_MULTIPLIER = 5.9604645E-8F;
   private static final double DOUBLE_MULTIPLIER = (double)1.110223E-16F;
   private final AtomicLong seed = new AtomicLong();
   private double nextNextGaussian;
   private boolean haveNextNextGaussian;

   public SimpleRandomSource(long p_158890_) {
      this.setSeed(p_158890_);
   }

   public void setSeed(long p_158902_) {
      if (!this.seed.compareAndSet(this.seed.get(), (p_158902_ ^ 25214903917L) & 281474976710655L)) {
         throw ThreadingDetector.makeThreadingException("SimpleRandomSource", (DebugBuffer<Pair<Thread, StackTraceElement[]>>)null);
      }
   }

   private int next(int p_158892_) {
      long i = this.seed.get();
      long j = i * 25214903917L + 11L & 281474976710655L;
      if (!this.seed.compareAndSet(i, j)) {
         throw ThreadingDetector.makeThreadingException("SimpleRandomSource", (DebugBuffer<Pair<Thread, StackTraceElement[]>>)null);
      } else {
         return (int)(j >> 48 - p_158892_);
      }
   }

   public int nextInt() {
      return this.next(32);
   }

   public int nextInt(int p_158899_) {
      if (p_158899_ <= 0) {
         throw new IllegalArgumentException("Bound must be positive");
      } else if ((p_158899_ & p_158899_ - 1) == 0) {
         return (int)((long)p_158899_ * (long)this.next(31) >> 31);
      } else {
         int i;
         int j;
         do {
            i = this.next(31);
            j = i % p_158899_;
         } while(i - j + (p_158899_ - 1) < 0);

         return j;
      }
   }

   public long nextLong() {
      int i = this.next(32);
      int j = this.next(32);
      long k = (long)i << 32;
      return k + (long)j;
   }

   public boolean nextBoolean() {
      return this.next(1) != 0;
   }

   public float nextFloat() {
      return (float)this.next(24) * 5.9604645E-8F;
   }

   public double nextDouble() {
      int i = this.next(26);
      int j = this.next(27);
      long k = ((long)i << 27) + (long)j;
      return (double)k * (double)1.110223E-16F;
   }

   public double nextGaussian() {
      if (this.haveNextNextGaussian) {
         this.haveNextNextGaussian = false;
         return this.nextNextGaussian;
      } else {
         while(true) {
            double d0 = 2.0D * this.nextDouble() - 1.0D;
            double d1 = 2.0D * this.nextDouble() - 1.0D;
            double d2 = Mth.square(d0) + Mth.square(d1);
            if (!(d2 >= 1.0D)) {
               if (d2 != 0.0D) {
                  double d3 = Math.sqrt(-2.0D * Math.log(d2) / d2);
                  this.nextNextGaussian = d1 * d3;
                  this.haveNextNextGaussian = true;
                  return d0 * d3;
               }
            }
         }
      }
   }
}