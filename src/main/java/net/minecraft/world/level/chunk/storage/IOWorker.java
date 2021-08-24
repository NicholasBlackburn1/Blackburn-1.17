package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.util.thread.StrictQueue;
import net.minecraft.world.level.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IOWorker implements AutoCloseable {
   private static final Logger LOGGER = LogManager.getLogger();
   private final AtomicBoolean shutdownRequested = new AtomicBoolean();
   private final ProcessorMailbox<StrictQueue.IntRunnable> mailbox;
   private final RegionFileStorage storage;
   private final Map<ChunkPos, IOWorker.PendingStore> pendingWrites = Maps.newLinkedHashMap();

   protected IOWorker(File p_63522_, boolean p_63523_, String p_63524_) {
      this.storage = new RegionFileStorage(p_63522_, p_63523_);
      this.mailbox = new ProcessorMailbox<>(new StrictQueue.FixedPriorityQueue(IOWorker.Priority.values().length), Util.ioPool(), "IOWorker-" + p_63524_);
   }

   public CompletableFuture<Void> store(ChunkPos p_63539_, @Nullable CompoundTag p_63540_) {
      return this.submitTask(() -> {
         IOWorker.PendingStore ioworker$pendingstore = this.pendingWrites.computeIfAbsent(p_63539_, (p_156584_) -> {
            return new IOWorker.PendingStore(p_63540_);
         });
         ioworker$pendingstore.data = p_63540_;
         return Either.left(ioworker$pendingstore.result);
      }).thenCompose(Function.identity());
   }

   @Nullable
   public CompoundTag load(ChunkPos p_63534_) throws IOException {
      CompletableFuture<CompoundTag> completablefuture = this.loadAsync(p_63534_);

      try {
         return completablefuture.join();
      } catch (CompletionException completionexception) {
         if (completionexception.getCause() instanceof IOException) {
            throw (IOException)completionexception.getCause();
         } else {
            throw completionexception;
         }
      }
   }

   protected CompletableFuture<CompoundTag> loadAsync(ChunkPos p_156588_) {
      return this.submitTask(() -> {
         IOWorker.PendingStore ioworker$pendingstore = this.pendingWrites.get(p_156588_);
         if (ioworker$pendingstore != null) {
            return Either.left(ioworker$pendingstore.data);
         } else {
            try {
               CompoundTag compoundtag = this.storage.read(p_156588_);
               return Either.left(compoundtag);
            } catch (Exception exception) {
               LOGGER.warn("Failed to read chunk {}", p_156588_, exception);
               return Either.right(exception);
            }
         }
      });
   }

   public CompletableFuture<Void> synchronize(boolean p_182499_) {
      CompletableFuture<Void> completablefuture = this.submitTask(() -> {
         return Either.left(CompletableFuture.allOf(this.pendingWrites.values().stream().map((p_156581_) -> {
            return p_156581_.result;
         }).toArray((p_156576_) -> {
            return new CompletableFuture[p_156576_];
         })));
      }).thenCompose(Function.identity());
      return p_182499_ ? completablefuture.thenCompose((p_63544_) -> {
         return this.submitTask(() -> {
            try {
               this.storage.flush();
               return Either.left((Void)null);
            } catch (Exception exception) {
               LOGGER.warn("Failed to synchronize chunks", (Throwable)exception);
               return Either.right(exception);
            }
         });
      }) : completablefuture.thenCompose((p_182494_) -> {
         return this.submitTask(() -> {
            return Either.left((Void)null);
         });
      });
   }

   private <T> CompletableFuture<T> submitTask(Supplier<Either<T, Exception>> p_63546_) {
      return this.mailbox.askEither((p_63549_) -> {
         return new StrictQueue.IntRunnable(IOWorker.Priority.FOREGROUND.ordinal(), () -> {
            if (!this.shutdownRequested.get()) {
               p_63549_.tell(p_63546_.get());
            }

            this.tellStorePending();
         });
      });
   }

   private void storePendingChunk() {
      if (!this.pendingWrites.isEmpty()) {
         Iterator<Entry<ChunkPos, IOWorker.PendingStore>> iterator = this.pendingWrites.entrySet().iterator();
         Entry<ChunkPos, IOWorker.PendingStore> entry = iterator.next();
         iterator.remove();
         this.runStore(entry.getKey(), entry.getValue());
         this.tellStorePending();
      }
   }

   private void tellStorePending() {
      this.mailbox.tell(new StrictQueue.IntRunnable(IOWorker.Priority.BACKGROUND.ordinal(), this::storePendingChunk));
   }

   private void runStore(ChunkPos p_63536_, IOWorker.PendingStore p_63537_) {
      try {
         this.storage.write(p_63536_, p_63537_.data);
         p_63537_.result.complete((Void)null);
      } catch (Exception exception) {
         LOGGER.error("Failed to store chunk {}", p_63536_, exception);
         p_63537_.result.completeExceptionally(exception);
      }

   }

   public void close() throws IOException {
      if (this.shutdownRequested.compareAndSet(false, true)) {
         this.mailbox.ask((p_63529_) -> {
            return new StrictQueue.IntRunnable(IOWorker.Priority.SHUTDOWN.ordinal(), () -> {
               p_63529_.tell(Unit.INSTANCE);
            });
         }).join();
         this.mailbox.close();

         try {
            this.storage.close();
         } catch (Exception exception) {
            LOGGER.error("Failed to close storage", (Throwable)exception);
         }

      }
   }

   static class PendingStore {
      @Nullable
      CompoundTag data;
      final CompletableFuture<Void> result = new CompletableFuture<>();

      public PendingStore(@Nullable CompoundTag p_63568_) {
         this.data = p_63568_;
      }
   }

   static enum Priority {
      FOREGROUND,
      BACKGROUND,
      SHUTDOWN;
   }
}