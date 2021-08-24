package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.TickPriority;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;

public class ProtoTickList<T> implements TickList<T> {
   protected final Predicate<T> ignore;
   private final ChunkPos chunkPos;
   private final ShortList[] toBeTicked;
   private LevelHeightAccessor levelHeightAccessor;

   public ProtoTickList(Predicate<T> p_156495_, ChunkPos p_156496_, LevelHeightAccessor p_156497_) {
      this(p_156495_, p_156496_, new ListTag(), p_156497_);
   }

   public ProtoTickList(Predicate<T> p_156499_, ChunkPos p_156500_, ListTag p_156501_, LevelHeightAccessor p_156502_) {
      this.ignore = p_156499_;
      this.chunkPos = p_156500_;
      this.levelHeightAccessor = p_156502_;
      this.toBeTicked = new ShortList[p_156502_.getSectionsCount()];

      for(int i = 0; i < p_156501_.size(); ++i) {
         ListTag listtag = p_156501_.getList(i);

         for(int j = 0; j < listtag.size(); ++j) {
            ChunkAccess.getOrCreateOffsetList(this.toBeTicked, i).add(listtag.getShort(j));
         }
      }

   }

   public ListTag save() {
      return ChunkSerializer.packOffsets(this.toBeTicked);
   }

   public void copyOut(TickList<T> p_63306_, Function<BlockPos, T> p_63307_) {
      for(int i = 0; i < this.toBeTicked.length; ++i) {
         if (this.toBeTicked[i] != null) {
            for(Short oshort : this.toBeTicked[i]) {
               BlockPos blockpos = ProtoChunk.unpackOffsetCoordinates(oshort, this.levelHeightAccessor.getSectionYFromSectionIndex(i), this.chunkPos);
               p_63306_.scheduleTick(blockpos, p_63307_.apply(blockpos), 0);
            }

            this.toBeTicked[i].clear();
         }
      }

   }

   public boolean hasScheduledTick(BlockPos p_63309_, T p_63310_) {
      return false;
   }

   public void scheduleTick(BlockPos p_63312_, T p_63313_, int p_63314_, TickPriority p_63315_) {
      int i = this.levelHeightAccessor.getSectionIndex(p_63312_.getY());
      if (i >= 0 && i < this.levelHeightAccessor.getSectionsCount()) {
         ChunkAccess.getOrCreateOffsetList(this.toBeTicked, i).add(ProtoChunk.packOffsetCoordinates(p_63312_));
      }
   }

   public boolean willTickThisTick(BlockPos p_63318_, T p_63319_) {
      return false;
   }

   public int size() {
      return Stream.of(this.toBeTicked).filter(Objects::nonNull).mapToInt(List::size).sum();
   }
}