package net.minecraft.world.level.chunk;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.BitStorage;
import net.minecraft.util.DebugBuffer;
import net.minecraft.util.Mth;
import net.minecraft.util.ThreadingDetector;

public class PalettedContainer<T> implements PaletteResize<T> {
   private static final int SIZE = 4096;
   public static final int GLOBAL_PALETTE_BITS = 9;
   public static final int MIN_PALETTE_SIZE = 4;
   private final Palette<T> globalPalette;
   private final PaletteResize<T> dummyPaletteResize = (p_63139_, p_63140_) -> {
      return 0;
   };
   private final IdMapper<T> registry;
   private final Function<CompoundTag, T> reader;
   private final Function<T, CompoundTag> writer;
   private final T defaultValue;
   protected BitStorage storage;
   private Palette<T> palette;
   private int bits;
   private final Semaphore lock = new Semaphore(1);
   @Nullable
   private final DebugBuffer<Pair<Thread, StackTraceElement[]>> traces = null;

   public void acquire() {
      if (this.traces != null) {
         Thread thread = Thread.currentThread();
         this.traces.push(Pair.of(thread, thread.getStackTrace()));
      }

      ThreadingDetector.checkAndLock(this.lock, this.traces, "PalettedContainer");
   }

   public void release() {
      this.lock.release();
   }

   public PalettedContainer(Palette<T> p_63079_, IdMapper<T> p_63080_, Function<CompoundTag, T> p_63081_, Function<T, CompoundTag> p_63082_, T p_63083_) {
      this.globalPalette = p_63079_;
      this.registry = p_63080_;
      this.reader = p_63081_;
      this.writer = p_63082_;
      this.defaultValue = p_63083_;
      this.setBits(4);
   }

   private static int getIndex(int p_63124_, int p_63125_, int p_63126_) {
      return p_63125_ << 8 | p_63126_ << 4 | p_63124_;
   }

   private void setBits(int p_63122_) {
      if (p_63122_ != this.bits) {
         this.bits = p_63122_;
         if (this.bits <= 4) {
            this.bits = 4;
            this.palette = new LinearPalette<>(this.registry, this.bits, this, this.reader);
         } else if (this.bits < 9) {
            this.palette = new HashMapPalette<>(this.registry, this.bits, this, this.reader, this.writer);
         } else {
            this.palette = this.globalPalette;
            this.bits = Mth.ceillog2(this.registry.size());
         }

         this.palette.idFor(this.defaultValue);
         this.storage = new BitStorage(this.bits, 4096);
      }
   }

   public int onResize(int p_63142_, T p_63143_) {
      BitStorage bitstorage = this.storage;
      Palette<T> palette = this.palette;
      this.setBits(p_63142_);

      for(int i = 0; i < bitstorage.getSize(); ++i) {
         T t = palette.valueFor(bitstorage.get(i));
         if (t != null) {
            this.set(i, t);
         }
      }

      return this.palette.idFor(p_63143_);
   }

   public T getAndSet(int p_63092_, int p_63093_, int p_63094_, T p_63095_) {
      Object object;
      try {
         this.acquire();
         T t = this.getAndSet(getIndex(p_63092_, p_63093_, p_63094_), p_63095_);
         object = t;
      } finally {
         this.release();
      }

      return (T)object;
   }

   public T getAndSetUnchecked(int p_63128_, int p_63129_, int p_63130_, T p_63131_) {
      return this.getAndSet(getIndex(p_63128_, p_63129_, p_63130_), p_63131_);
   }

   private T getAndSet(int p_63097_, T p_63098_) {
      int i = this.palette.idFor(p_63098_);
      int j = this.storage.getAndSet(p_63097_, i);
      T t = this.palette.valueFor(j);
      return (T)(t == null ? this.defaultValue : t);
   }

   public void set(int p_156471_, int p_156472_, int p_156473_, T p_156474_) {
      try {
         this.acquire();
         this.set(getIndex(p_156471_, p_156472_, p_156473_), p_156474_);
      } finally {
         this.release();
      }

   }

   private void set(int p_63133_, T p_63134_) {
      int i = this.palette.idFor(p_63134_);
      this.storage.set(p_63133_, i);
   }

   public T get(int p_63088_, int p_63089_, int p_63090_) {
      return this.get(getIndex(p_63088_, p_63089_, p_63090_));
   }

   protected T get(int p_63086_) {
      T t = this.palette.valueFor(this.storage.get(p_63086_));
      return (T)(t == null ? this.defaultValue : t);
   }

   public void read(FriendlyByteBuf p_63119_) {
      try {
         this.acquire();
         int i = p_63119_.readByte();
         if (this.bits != i) {
            this.setBits(i);
         }

         this.palette.read(p_63119_);
         p_63119_.readLongArray(this.storage.getRaw());
      } finally {
         this.release();
      }

   }

   public void write(FriendlyByteBuf p_63136_) {
      try {
         this.acquire();
         p_63136_.writeByte(this.bits);
         this.palette.write(p_63136_);
         p_63136_.writeLongArray(this.storage.getRaw());
      } finally {
         this.release();
      }

   }

   public void read(ListTag p_63116_, long[] p_63117_) {
      try {
         this.acquire();
         int i = Math.max(4, Mth.ceillog2(p_63116_.size()));
         if (i != this.bits) {
            this.setBits(i);
         }

         this.palette.read(p_63116_);
         int j = p_63117_.length * 64 / 4096;
         if (this.palette == this.globalPalette) {
            Palette<T> palette = new HashMapPalette<>(this.registry, i, this.dummyPaletteResize, this.reader, this.writer);
            palette.read(p_63116_);
            BitStorage bitstorage = new BitStorage(i, 4096, p_63117_);

            for(int k = 0; k < 4096; ++k) {
               this.storage.set(k, this.globalPalette.idFor(palette.valueFor(bitstorage.get(k))));
            }
         } else if (j == this.bits) {
            System.arraycopy(p_63117_, 0, this.storage.getRaw(), 0, p_63117_.length);
         } else {
            BitStorage bitstorage1 = new BitStorage(j, 4096, p_63117_);

            for(int l = 0; l < 4096; ++l) {
               this.storage.set(l, bitstorage1.get(l));
            }
         }
      } finally {
         this.release();
      }

   }

   public void write(CompoundTag p_63112_, String p_63113_, String p_63114_) {
      try {
         this.acquire();
         HashMapPalette<T> hashmappalette = new HashMapPalette<>(this.registry, this.bits, this.dummyPaletteResize, this.reader, this.writer);
         T t = this.defaultValue;
         int i = hashmappalette.idFor(this.defaultValue);
         int[] aint = new int[4096];

         for(int j = 0; j < 4096; ++j) {
            T t1 = this.get(j);
            if (t1 != t) {
               t = t1;
               i = hashmappalette.idFor(t1);
            }

            aint[j] = i;
         }

         ListTag listtag = new ListTag();
         hashmappalette.write(listtag);
         p_63112_.put(p_63113_, listtag);
         int l = Math.max(4, Mth.ceillog2(listtag.size()));
         BitStorage bitstorage = new BitStorage(l, 4096);

         for(int k = 0; k < aint.length; ++k) {
            bitstorage.set(k, aint[k]);
         }

         p_63112_.putLongArray(p_63114_, bitstorage.getRaw());
      } finally {
         this.release();
      }

   }

   public int getSerializedSize() {
      return 1 + this.palette.getSerializedSize() + FriendlyByteBuf.getVarIntSize(this.storage.getSize()) + this.storage.getRaw().length * 8;
   }

   public boolean maybeHas(Predicate<T> p_63110_) {
      return this.palette.maybeHas(p_63110_);
   }

   public void count(PalettedContainer.CountConsumer<T> p_63100_) {
      Int2IntMap int2intmap = new Int2IntOpenHashMap();
      this.storage.getAll((p_156469_) -> {
         int2intmap.put(p_156469_, int2intmap.get(p_156469_) + 1);
      });
      int2intmap.int2IntEntrySet().forEach((p_156466_) -> {
         p_63100_.accept(this.palette.valueFor(p_156466_.getIntKey()), p_156466_.getIntValue());
      });
   }

   @FunctionalInterface
   public interface CountConsumer<T> {
      void accept(T p_63145_, int p_63146_);
   }
}