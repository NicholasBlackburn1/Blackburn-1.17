package net.minecraft.world.level.chunk.storage;

import com.mojang.datafixers.DataFixer;
import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.LegacyStructureDataHandler;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class ChunkStorage implements AutoCloseable {
   private final IOWorker worker;
   protected final DataFixer fixerUpper;
   @Nullable
   private LegacyStructureDataHandler legacyStructureHandler;

   public ChunkStorage(File p_63499_, DataFixer p_63500_, boolean p_63501_) {
      this.fixerUpper = p_63500_;
      this.worker = new IOWorker(p_63499_, p_63501_, "chunk");
   }

   public CompoundTag upgradeChunkTag(ResourceKey<Level> p_63508_, Supplier<DimensionDataStorage> p_63509_, CompoundTag p_63510_) {
      int i = getVersion(p_63510_);
      int j = 1493;
      if (i < 1493) {
         p_63510_ = NbtUtils.update(this.fixerUpper, DataFixTypes.CHUNK, p_63510_, i, 1493);
         if (p_63510_.getCompound("Level").getBoolean("hasLegacyStructureData")) {
            if (this.legacyStructureHandler == null) {
               this.legacyStructureHandler = LegacyStructureDataHandler.getLegacyStructureHandler(p_63508_, p_63509_.get());
            }

            p_63510_ = this.legacyStructureHandler.updateFromLegacy(p_63510_);
         }
      }

      p_63510_ = NbtUtils.update(this.fixerUpper, DataFixTypes.CHUNK, p_63510_, Math.max(1493, i));
      if (i < SharedConstants.getCurrentVersion().getWorldVersion()) {
         p_63510_.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
      }

      return p_63510_;
   }

   public static int getVersion(CompoundTag p_63506_) {
      return p_63506_.contains("DataVersion", 99) ? p_63506_.getInt("DataVersion") : -1;
   }

   @Nullable
   public CompoundTag read(ChunkPos p_63513_) throws IOException {
      return this.worker.load(p_63513_);
   }

   public void write(ChunkPos p_63503_, CompoundTag p_63504_) {
      this.worker.store(p_63503_, p_63504_);
      if (this.legacyStructureHandler != null) {
         this.legacyStructureHandler.removeIndex(p_63503_.toLong());
      }

   }

   public void flushWorker() {
      this.worker.synchronize(true).join();
   }

   public void close() throws IOException {
      this.worker.close();
   }
}