package net.minecraft.core;

import javax.annotation.Nullable;

public interface IdMap<T> extends Iterable<T> {
   int getId(T p_122652_);

   @Nullable
   T byId(int p_122651_);
}