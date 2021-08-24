package net.minecraft.world.level.chunk;

import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;

public class HashMapPalette<T> implements Palette<T> {
   private final IdMapper<T> registry;
   private final CrudeIncrementalIntIdentityHashBiMap<T> values;
   private final PaletteResize<T> resizeHandler;
   private final Function<CompoundTag, T> reader;
   private final Function<T, CompoundTag> writer;
   private final int bits;

   public HashMapPalette(IdMapper<T> p_62664_, int p_62665_, PaletteResize<T> p_62666_, Function<CompoundTag, T> p_62667_, Function<T, CompoundTag> p_62668_) {
      this.registry = p_62664_;
      this.bits = p_62665_;
      this.resizeHandler = p_62666_;
      this.reader = p_62667_;
      this.writer = p_62668_;
      this.values = new CrudeIncrementalIntIdentityHashBiMap<>(1 << p_62665_);
   }

   public int idFor(T p_62673_) {
      int i = this.values.getId(p_62673_);
      if (i == -1) {
         i = this.values.add(p_62673_);
         if (i >= 1 << this.bits) {
            i = this.resizeHandler.onResize(this.bits + 1, p_62673_);
         }
      }

      return i;
   }

   public boolean maybeHas(Predicate<T> p_62675_) {
      for(int i = 0; i < this.getSize(); ++i) {
         if (p_62675_.test(this.values.byId(i))) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public T valueFor(int p_62671_) {
      return this.values.byId(p_62671_);
   }

   public void read(FriendlyByteBuf p_62679_) {
      this.values.clear();
      int i = p_62679_.readVarInt();

      for(int j = 0; j < i; ++j) {
         this.values.add(this.registry.byId(p_62679_.readVarInt()));
      }

   }

   public void write(FriendlyByteBuf p_62684_) {
      int i = this.getSize();
      p_62684_.writeVarInt(i);

      for(int j = 0; j < i; ++j) {
         p_62684_.writeVarInt(this.registry.getId(this.values.byId(j)));
      }

   }

   public int getSerializedSize() {
      int i = FriendlyByteBuf.getVarIntSize(this.getSize());

      for(int j = 0; j < this.getSize(); ++j) {
         i += FriendlyByteBuf.getVarIntSize(this.registry.getId(this.values.byId(j)));
      }

      return i;
   }

   public int getSize() {
      return this.values.size();
   }

   public void read(ListTag p_62677_) {
      this.values.clear();

      for(int i = 0; i < p_62677_.size(); ++i) {
         this.values.add(this.reader.apply(p_62677_.getCompound(i)));
      }

   }

   public void write(ListTag p_62682_) {
      for(int i = 0; i < this.getSize(); ++i) {
         p_62682_.add(this.writer.apply(this.values.byId(i)));
      }

   }
}