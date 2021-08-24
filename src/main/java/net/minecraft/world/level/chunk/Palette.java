package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

public interface Palette<T> {
   int idFor(T p_63061_);

   boolean maybeHas(Predicate<T> p_63062_);

   @Nullable
   T valueFor(int p_63060_);

   void read(FriendlyByteBuf p_63064_);

   void write(FriendlyByteBuf p_63065_);

   int getSerializedSize();

   int getSize();

   void read(ListTag p_63063_);
}