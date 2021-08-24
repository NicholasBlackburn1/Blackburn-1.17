package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public class Sensing {
   private final Mob mob;
   private final List<Entity> seen = Lists.newArrayList();
   private final List<Entity> unseen = Lists.newArrayList();

   public Sensing(Mob p_26788_) {
      this.mob = p_26788_;
   }

   public void tick() {
      this.seen.clear();
      this.unseen.clear();
   }

   public boolean hasLineOfSight(Entity p_148307_) {
      if (this.seen.contains(p_148307_)) {
         return true;
      } else if (this.unseen.contains(p_148307_)) {
         return false;
      } else {
         this.mob.level.getProfiler().push("hasLineOfSight");
         boolean flag = this.mob.hasLineOfSight(p_148307_);
         this.mob.level.getProfiler().pop();
         if (flag) {
            this.seen.add(p_148307_);
         } else {
            this.unseen.add(p_148307_);
         }

         return flag;
      }
   }
}