package net.minecraft.core;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;

public class IdMapper<T> implements IdMap<T> {
   public static final int DEFAULT = -1;
   private int nextId;
   private final IdentityHashMap<T, Integer> tToId;
   private final List<T> idToT;

   public IdMapper() {
      this(512);
   }

   public IdMapper(int p_122658_) {
      this.idToT = Lists.newArrayListWithExpectedSize(p_122658_);
      this.tToId = new IdentityHashMap<>(p_122658_);
   }

   public void addMapping(T p_122665_, int p_122666_) {
      this.tToId.put(p_122665_, p_122666_);

      while(this.idToT.size() <= p_122666_) {
         this.idToT.add((T)null);
      }

      this.idToT.set(p_122666_, p_122665_);
      if (this.nextId <= p_122666_) {
         this.nextId = p_122666_ + 1;
      }

   }

   public void add(T p_122668_) {
      this.addMapping(p_122668_, this.nextId);
   }

   public int getId(T p_122663_) {
      Integer integer = this.tToId.get(p_122663_);
      return integer == null ? -1 : integer;
   }

   @Nullable
   public final T byId(int p_122661_) {
      return (T)(p_122661_ >= 0 && p_122661_ < this.idToT.size() ? this.idToT.get(p_122661_) : null);
   }

   public Iterator<T> iterator() {
      return Iterators.filter(this.idToT.iterator(), Predicates.notNull());
   }

   public boolean contains(int p_175381_) {
      return this.byId(p_175381_) != null;
   }

   public int size() {
      return this.tToId.size();
   }
}