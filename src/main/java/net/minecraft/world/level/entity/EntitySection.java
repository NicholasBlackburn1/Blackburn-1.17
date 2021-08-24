package net.minecraft.world.level.entity;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.util.VisibleForDebug;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntitySection<T> {
   protected static final Logger LOGGER = LogManager.getLogger();
   private final ClassInstanceMultiMap<T> storage;
   private Visibility chunkStatus;

   public EntitySection(Class<T> p_156831_, Visibility p_156832_) {
      this.chunkStatus = p_156832_;
      this.storage = new ClassInstanceMultiMap<>(p_156831_);
   }

   public void add(T p_156841_) {
      this.storage.add(p_156841_);
   }

   public boolean remove(T p_156847_) {
      return this.storage.remove(p_156847_);
   }

   public void getEntities(Predicate<? super T> p_156843_, Consumer<T> p_156844_) {
      for(T t : this.storage) {
         if (p_156843_.test(t)) {
            p_156844_.accept(t);
         }
      }

   }

   public <U extends T> void getEntities(EntityTypeTest<T, U> p_156835_, Predicate<? super U> p_156836_, Consumer<? super U> p_156837_) {
      for(T t : this.storage.find(p_156835_.getBaseClass())) {
         U u = (U)p_156835_.tryCast(t);
         if (u != null && p_156836_.test(u)) {
            p_156837_.accept(u);
         }
      }

   }

   public boolean isEmpty() {
      return this.storage.isEmpty();
   }

   public Stream<T> getEntities() {
      return this.storage.stream();
   }

   public Visibility getStatus() {
      return this.chunkStatus;
   }

   public Visibility updateChunkStatus(Visibility p_156839_) {
      Visibility visibility = this.chunkStatus;
      this.chunkStatus = p_156839_;
      return visibility;
   }

   @VisibleForDebug
   public int size() {
      return this.storage.size();
   }
}
