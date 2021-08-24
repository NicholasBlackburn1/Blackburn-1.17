package net.minecraft.world.level;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class ServerTickList<T> implements TickList<T> {
   public static final int MAX_TICK_BLOCKS_PER_TICK = 65536;
   protected final Predicate<T> ignore;
   private final Function<T, ResourceLocation> toId;
   private final Set<TickNextTickData<T>> tickNextTickSet = Sets.newHashSet();
   private final Set<TickNextTickData<T>> tickNextTickList = Sets.newTreeSet(TickNextTickData.createTimeComparator());
   private final ServerLevel level;
   private final Queue<TickNextTickData<T>> currentlyTicking = Queues.newArrayDeque();
   private final List<TickNextTickData<T>> alreadyTicked = Lists.newArrayList();
   private final Consumer<TickNextTickData<T>> ticker;

   public ServerTickList(ServerLevel p_47216_, Predicate<T> p_47217_, Function<T, ResourceLocation> p_47218_, Consumer<TickNextTickData<T>> p_47219_) {
      this.ignore = p_47217_;
      this.toId = p_47218_;
      this.level = p_47216_;
      this.ticker = p_47219_;
   }

   public void tick() {
      int i = this.tickNextTickList.size();
      if (i != this.tickNextTickSet.size()) {
         throw new IllegalStateException("TickNextTick list out of synch");
      } else {
         if (i > 65536) {
            i = 65536;
         }

         Iterator<TickNextTickData<T>> iterator = this.tickNextTickList.iterator();
         this.level.getProfiler().push("cleaning");

         while(i > 0 && iterator.hasNext()) {
            TickNextTickData<T> ticknexttickdata = iterator.next();
            if (ticknexttickdata.triggerTick > this.level.getGameTime()) {
               break;
            }

            if (this.level.isPositionTickingWithEntitiesLoaded(ticknexttickdata.pos)) {
               iterator.remove();
               this.tickNextTickSet.remove(ticknexttickdata);
               this.currentlyTicking.add(ticknexttickdata);
               --i;
            }
         }

         this.level.getProfiler().popPush("ticking");

         TickNextTickData<T> ticknexttickdata1;
         while((ticknexttickdata1 = this.currentlyTicking.poll()) != null) {
            if (this.level.isPositionTickingWithEntitiesLoaded(ticknexttickdata1.pos)) {
               try {
                  this.alreadyTicked.add(ticknexttickdata1);
                  this.ticker.accept(ticknexttickdata1);
               } catch (Throwable throwable) {
                  CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception while ticking");
                  CrashReportCategory crashreportcategory = crashreport.addCategory("Block being ticked");
                  CrashReportCategory.populateBlockDetails(crashreportcategory, this.level, ticknexttickdata1.pos, (BlockState)null);
                  throw new ReportedException(crashreport);
               }
            } else {
               this.scheduleTick(ticknexttickdata1.pos, ticknexttickdata1.getType(), 0);
            }
         }

         this.level.getProfiler().pop();
         this.alreadyTicked.clear();
         this.currentlyTicking.clear();
      }
   }

   public boolean willTickThisTick(BlockPos p_47255_, T p_47256_) {
      return this.currentlyTicking.contains(new TickNextTickData(p_47255_, p_47256_));
   }

   public List<TickNextTickData<T>> fetchTicksInChunk(ChunkPos p_47224_, boolean p_47225_, boolean p_47226_) {
      int i = p_47224_.getMinBlockX() - 2;
      int j = i + 16 + 2;
      int k = p_47224_.getMinBlockZ() - 2;
      int l = k + 16 + 2;
      return this.fetchTicksInArea(new BoundingBox(i, this.level.getMinBuildHeight(), k, j, this.level.getMaxBuildHeight(), l), p_47225_, p_47226_);
   }

   public List<TickNextTickData<T>> fetchTicksInArea(BoundingBox p_47233_, boolean p_47234_, boolean p_47235_) {
      List<TickNextTickData<T>> list = this.fetchTicksInArea((List<TickNextTickData<T>>)null, this.tickNextTickList, p_47233_, p_47234_);
      if (p_47234_ && list != null) {
         this.tickNextTickSet.removeAll(list);
      }

      list = this.fetchTicksInArea(list, this.currentlyTicking, p_47233_, p_47234_);
      if (!p_47235_) {
         list = this.fetchTicksInArea(list, this.alreadyTicked, p_47233_, p_47234_);
      }

      return list == null ? Collections.emptyList() : list;
   }

   @Nullable
   private List<TickNextTickData<T>> fetchTicksInArea(@Nullable List<TickNextTickData<T>> p_47245_, Collection<TickNextTickData<T>> p_47246_, BoundingBox p_47247_, boolean p_47248_) {
      Iterator<TickNextTickData<T>> iterator = p_47246_.iterator();

      while(iterator.hasNext()) {
         TickNextTickData<T> ticknexttickdata = iterator.next();
         BlockPos blockpos = ticknexttickdata.pos;
         if (blockpos.getX() >= p_47247_.minX() && blockpos.getX() < p_47247_.maxX() && blockpos.getZ() >= p_47247_.minZ() && blockpos.getZ() < p_47247_.maxZ()) {
            if (p_47248_) {
               iterator.remove();
            }

            if (p_47245_ == null) {
               p_47245_ = Lists.newArrayList();
            }

            p_47245_.add(ticknexttickdata);
         }
      }

      return p_47245_;
   }

   public void copy(BoundingBox p_47230_, BlockPos p_47231_) {
      for(TickNextTickData<T> ticknexttickdata : this.fetchTicksInArea(p_47230_, false, false)) {
         if (p_47230_.isInside(ticknexttickdata.pos)) {
            BlockPos blockpos = ticknexttickdata.pos.offset(p_47231_);
            T t = ticknexttickdata.getType();
            this.addTickData(new TickNextTickData<>(blockpos, t, ticknexttickdata.triggerTick, ticknexttickdata.priority));
         }
      }

   }

   public ListTag save(ChunkPos p_47222_) {
      List<TickNextTickData<T>> list = this.fetchTicksInChunk(p_47222_, false, true);
      return saveTickList(this.toId, list, this.level.getGameTime());
   }

   private static <T> ListTag saveTickList(Function<T, ResourceLocation> p_47250_, Iterable<TickNextTickData<T>> p_47251_, long p_47252_) {
      ListTag listtag = new ListTag();

      for(TickNextTickData<T> ticknexttickdata : p_47251_) {
         CompoundTag compoundtag = new CompoundTag();
         compoundtag.putString("i", p_47250_.apply(ticknexttickdata.getType()).toString());
         compoundtag.putInt("x", ticknexttickdata.pos.getX());
         compoundtag.putInt("y", ticknexttickdata.pos.getY());
         compoundtag.putInt("z", ticknexttickdata.pos.getZ());
         compoundtag.putInt("t", (int)(ticknexttickdata.triggerTick - p_47252_));
         compoundtag.putInt("p", ticknexttickdata.priority.getValue());
         listtag.add(compoundtag);
      }

      return listtag;
   }

   public boolean hasScheduledTick(BlockPos p_47237_, T p_47238_) {
      return this.tickNextTickSet.contains(new TickNextTickData(p_47237_, p_47238_));
   }

   public void scheduleTick(BlockPos p_47240_, T p_47241_, int p_47242_, TickPriority p_47243_) {
      if (!this.ignore.test(p_47241_)) {
         this.addTickData(new TickNextTickData<>(p_47240_, p_47241_, (long)p_47242_ + this.level.getGameTime(), p_47243_));
      }

   }

   private void addTickData(TickNextTickData<T> p_47228_) {
      if (!this.tickNextTickSet.contains(p_47228_)) {
         this.tickNextTickSet.add(p_47228_);
         this.tickNextTickList.add(p_47228_);
      }

   }

   public int size() {
      return this.tickNextTickSet.size();
   }
}