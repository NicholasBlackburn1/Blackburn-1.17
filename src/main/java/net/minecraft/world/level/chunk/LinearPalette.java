package net.minecraft.world.level.chunk;

import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

public class LinearPalette<T> implements Palette<T> {
   private final IdMapper<T> registry;
   private final T[] values;
   private final PaletteResize<T> resizeHandler;
   private final Function<CompoundTag, T> reader;
   private final int bits;
   private int size;

   public LinearPalette(IdMapper<T> p_63032_, int p_63033_, PaletteResize<T> p_63034_, Function<CompoundTag, T> p_63035_) {
      this.registry = p_63032_;
      this.values = (T[])(new Object[1 << p_63033_]);
      this.bits = p_63033_;
      this.resizeHandler = p_63034_;
      this.reader = p_63035_;
   }

   public int idFor(T p_63040_) {
      for(int i = 0; i < this.size; ++i) {
         if (this.values[i] == p_63040_) {
            return i;
         }
      }

      int j = this.size;
      if (j < this.values.length) {
         this.values[j] = p_63040_;
         ++this.size;
         return j;
      } else {
         return this.resizeHandler.onResize(this.bits + 1, p_63040_);
      }
   }

   public boolean maybeHas(Predicate<T> p_63042_) {
      for(int i = 0; i < this.size; ++i) {
         if (p_63042_.test(this.values[i])) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public T valueFor(int p_63038_) {
      return (T)(p_63038_ >= 0 && p_63038_ < this.size ? this.values[p_63038_] : null);
   }

   public void read(FriendlyByteBuf p_63046_) {
      this.size = p_63046_.readVarInt();

      for(int i = 0; i < this.size; ++i) {
         this.values[i] = this.registry.byId(p_63046_.readVarInt());
      }

   }

   public void write(FriendlyByteBuf p_63049_) {
      p_63049_.writeVarInt(this.size);

      for(int i = 0; i < this.size; ++i) {
         p_63049_.writeVarInt(this.registry.getId(this.values[i]));
      }

   }

   public int getSerializedSize() {
      int i = FriendlyByteBuf.getVarIntSize(this.getSize());

      for(int j = 0; j < this.getSize(); ++j) {
         i += FriendlyByteBuf.getVarIntSize(this.registry.getId(this.values[j]));
      }

      return i;
   }

   public int getSize() {
      return this.size;
   }

   public void read(ListTag p_63044_) {
      for(int i = 0; i < p_63044_.size(); ++i) {
         this.values[i] = this.reader.apply(p_63044_.getCompound(i));
      }

      this.size = p_63044_.size();
   }
}