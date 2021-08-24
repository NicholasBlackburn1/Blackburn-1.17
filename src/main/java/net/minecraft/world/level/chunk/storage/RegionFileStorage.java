package net.minecraft.world.level.chunk.storage;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.ExceptionCollector;
import net.minecraft.world.level.ChunkPos;

public final class RegionFileStorage implements AutoCloseable {
   public static final String ANVIL_EXTENSION = ".mca";
   private static final int MAX_CACHE_SIZE = 256;
   private final Long2ObjectLinkedOpenHashMap<RegionFile> regionCache = new Long2ObjectLinkedOpenHashMap<>();
   private final File folder;
   private final boolean sync;

   RegionFileStorage(File p_63703_, boolean p_63704_) {
      this.folder = p_63703_;
      this.sync = p_63704_;
   }

   private RegionFile getRegionFile(ChunkPos p_63712_) throws IOException {
      long i = ChunkPos.asLong(p_63712_.getRegionX(), p_63712_.getRegionZ());
      RegionFile regionfile = this.regionCache.getAndMoveToFirst(i);
      if (regionfile != null) {
         return regionfile;
      } else {
         if (this.regionCache.size() >= 256) {
            this.regionCache.removeLast().close();
         }

         if (!this.folder.exists()) {
            this.folder.mkdirs();
         }

         File file1 = new File(this.folder, "r." + p_63712_.getRegionX() + "." + p_63712_.getRegionZ() + ".mca");
         RegionFile regionfile1 = new RegionFile(file1, this.folder, this.sync);
         this.regionCache.putAndMoveToFirst(i, regionfile1);
         return regionfile1;
      }
   }

   @Nullable
   public CompoundTag read(ChunkPos p_63707_) throws IOException {
      RegionFile regionfile = this.getRegionFile(p_63707_);
      DataInputStream datainputstream = regionfile.getChunkDataInputStream(p_63707_);

      CompoundTag compoundtag;
      label43: {
         try {
            if (datainputstream == null) {
               compoundtag = null;
               break label43;
            }

            compoundtag = NbtIo.read(datainputstream);
         } catch (Throwable throwable1) {
            if (datainputstream != null) {
               try {
                  datainputstream.close();
               } catch (Throwable throwable) {
                  throwable1.addSuppressed(throwable);
               }
            }

            throw throwable1;
         }

         if (datainputstream != null) {
            datainputstream.close();
         }

         return compoundtag;
      }

      if (datainputstream != null) {
         datainputstream.close();
      }

      return compoundtag;
   }

   protected void write(ChunkPos p_63709_, @Nullable CompoundTag p_63710_) throws IOException {
      RegionFile regionfile = this.getRegionFile(p_63709_);
      if (p_63710_ == null) {
         regionfile.clear(p_63709_);
      } else {
         DataOutputStream dataoutputstream = regionfile.getChunkDataOutputStream(p_63709_);

         try {
            NbtIo.write(p_63710_, dataoutputstream);
         } catch (Throwable throwable1) {
            if (dataoutputstream != null) {
               try {
                  dataoutputstream.close();
               } catch (Throwable throwable) {
                  throwable1.addSuppressed(throwable);
               }
            }

            throw throwable1;
         }

         if (dataoutputstream != null) {
            dataoutputstream.close();
         }
      }

   }

   public void close() throws IOException {
      ExceptionCollector<IOException> exceptioncollector = new ExceptionCollector<>();

      for(RegionFile regionfile : this.regionCache.values()) {
         try {
            regionfile.close();
         } catch (IOException ioexception) {
            exceptioncollector.add(ioexception);
         }
      }

      exceptioncollector.throwIfPresent();
   }

   public void flush() throws IOException {
      for(RegionFile regionfile : this.regionCache.values()) {
         regionfile.flush();
      }

   }
}