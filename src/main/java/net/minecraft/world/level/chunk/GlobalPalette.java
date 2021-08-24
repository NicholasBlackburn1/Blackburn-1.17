package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

public class GlobalPalette<T> implements Palette<T> {
   private final IdMapper<T> registry;
   private final T defaultValue;

   public GlobalPalette(IdMapper<T> p_62642_, T p_62643_) {
      this.registry = p_62642_;
      this.defaultValue = p_62643_;
   }

   public int idFor(T p_62648_) {
      int i = this.registry.getId(p_62648_);
      return i == -1 ? 0 : i;
   }

   public boolean maybeHas(Predicate<T> p_62650_) {
      return true;
   }

   public T valueFor(int p_62646_) {
      T t = this.registry.byId(p_62646_);
      return (T)(t == null ? this.defaultValue : t);
   }

   public void read(FriendlyByteBuf p_62654_) {
   }

   public void write(FriendlyByteBuf p_62656_) {
   }

   public int getSerializedSize() {
      return FriendlyByteBuf.getVarIntSize(0);
   }

   public int getSize() {
      return this.registry.size();
   }

   public void read(ListTag p_62652_) {
   }
}