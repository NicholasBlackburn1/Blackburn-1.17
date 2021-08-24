package net.minecraft.world.entity.ai.goal;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GoalSelector {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final WrappedGoal NO_GOAL = new WrappedGoal(Integer.MAX_VALUE, new Goal() {
      public boolean canUse() {
         return false;
      }
   }) {
      public boolean isRunning() {
         return false;
      }
   };
   private final Map<Goal.Flag, WrappedGoal> lockedFlags = new EnumMap<>(Goal.Flag.class);
   private final Set<WrappedGoal> availableGoals = Sets.newLinkedHashSet();
   private final Supplier<ProfilerFiller> profiler;
   private final EnumSet<Goal.Flag> disabledFlags = EnumSet.noneOf(Goal.Flag.class);
   private int tickCount;
   private int newGoalRate = 3;

   public GoalSelector(Supplier<ProfilerFiller> p_25351_) {
      this.profiler = p_25351_;
   }

   public void addGoal(int p_25353_, Goal p_25354_) {
      this.availableGoals.add(new WrappedGoal(p_25353_, p_25354_));
   }

   @VisibleForTesting
   public void removeAllGoals() {
      this.availableGoals.clear();
   }

   public void removeGoal(Goal p_25364_) {
      this.availableGoals.stream().filter((p_25378_) -> {
         return p_25378_.getGoal() == p_25364_;
      }).filter(WrappedGoal::isRunning).forEach(WrappedGoal::stop);
      this.availableGoals.removeIf((p_25367_) -> {
         return p_25367_.getGoal() == p_25364_;
      });
   }

   public void tick() {
      ProfilerFiller profilerfiller = this.profiler.get();
      profilerfiller.push("goalCleanup");
      this.getRunningGoals().filter((p_25390_) -> {
         return !p_25390_.isRunning() || p_25390_.getFlags().stream().anyMatch(this.disabledFlags::contains) || !p_25390_.canContinueToUse();
      }).forEach(Goal::stop);
      this.lockedFlags.forEach((p_25358_, p_25359_) -> {
         if (!p_25359_.isRunning()) {
            this.lockedFlags.remove(p_25358_);
         }

      });
      profilerfiller.pop();
      profilerfiller.push("goalUpdate");
      this.availableGoals.stream().filter((p_25388_) -> {
         return !p_25388_.isRunning();
      }).filter((p_25385_) -> {
         return p_25385_.getFlags().stream().noneMatch(this.disabledFlags::contains);
      }).filter((p_25380_) -> {
         return p_25380_.getFlags().stream().allMatch((p_148104_) -> {
            return this.lockedFlags.getOrDefault(p_148104_, NO_GOAL).canBeReplacedBy(p_25380_);
         });
      }).filter(WrappedGoal::canUse).forEach((p_25369_) -> {
         p_25369_.getFlags().forEach((p_148101_) -> {
            WrappedGoal wrappedgoal = this.lockedFlags.getOrDefault(p_148101_, NO_GOAL);
            wrappedgoal.stop();
            this.lockedFlags.put(p_148101_, p_25369_);
         });
         p_25369_.start();
      });
      profilerfiller.pop();
      profilerfiller.push("goalTick");
      this.getRunningGoals().forEach(WrappedGoal::tick);
      profilerfiller.pop();
   }

   public Set<WrappedGoal> getAvailableGoals() {
      return this.availableGoals;
   }

   public Stream<WrappedGoal> getRunningGoals() {
      return this.availableGoals.stream().filter(WrappedGoal::isRunning);
   }

   public void setNewGoalRate(int p_148098_) {
      this.newGoalRate = p_148098_;
   }

   public void disableControlFlag(Goal.Flag p_25356_) {
      this.disabledFlags.add(p_25356_);
   }

   public void enableControlFlag(Goal.Flag p_25375_) {
      this.disabledFlags.remove(p_25375_);
   }

   public void setControlFlag(Goal.Flag p_25361_, boolean p_25362_) {
      if (p_25362_) {
         this.enableControlFlag(p_25361_);
      } else {
         this.disableControlFlag(p_25361_);
      }

   }
}