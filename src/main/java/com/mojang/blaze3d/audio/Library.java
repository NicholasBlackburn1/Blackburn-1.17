package com.mojang.blaze3d.audio;

import com.google.common.collect.Sets;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.system.MemoryStack;

@OnlyIn(Dist.CLIENT)
public class Library {
   private static final int NUM_OPEN_DEVICE_RETRIES = 3;
   static final Logger LOGGER = LogManager.getLogger();
   private static final int DEFAULT_CHANNEL_COUNT = 30;
   private long device;
   private long context;
   private static final Library.ChannelPool EMPTY = new Library.ChannelPool() {
      @Nullable
      public Channel acquire() {
         return null;
      }

      public boolean release(Channel p_83708_) {
         return false;
      }

      public void cleanup() {
      }

      public int getMaxCount() {
         return 0;
      }

      public int getUsedCount() {
         return 0;
      }
   };
   private Library.ChannelPool staticChannels = EMPTY;
   private Library.ChannelPool streamingChannels = EMPTY;
   private final Listener listener = new Listener();

   public void init() {
      this.device = tryOpenDevice();
      ALCCapabilities alccapabilities = ALC.createCapabilities(this.device);
      if (OpenAlUtil.checkALCError(this.device, "Get capabilities")) {
         throw new IllegalStateException("Failed to get OpenAL capabilities");
      } else if (!alccapabilities.OpenALC11) {
         throw new IllegalStateException("OpenAL 1.1 not supported");
      } else {
         this.context = ALC10.alcCreateContext(this.device, (IntBuffer)null);
         ALC10.alcMakeContextCurrent(this.context);
         int i = this.getChannelCount();
         int j = Mth.clamp((int)Mth.sqrt((float)i), 2, 8);
         int k = Mth.clamp(i - j, 8, 255);
         this.staticChannels = new Library.CountingChannelPool(k);
         this.streamingChannels = new Library.CountingChannelPool(j);
         ALCapabilities alcapabilities = AL.createCapabilities(alccapabilities);
         OpenAlUtil.checkALError("Initialization");
         if (!alcapabilities.AL_EXT_source_distance_model) {
            throw new IllegalStateException("AL_EXT_source_distance_model is not supported");
         } else {
            AL10.alEnable(512);
            if (!alcapabilities.AL_EXT_LINEAR_DISTANCE) {
               throw new IllegalStateException("AL_EXT_LINEAR_DISTANCE is not supported");
            } else {
               OpenAlUtil.checkALError("Enable per-source distance models");
               LOGGER.info("OpenAL initialized.");
            }
         }
      }
   }

   private int getChannelCount() {
      MemoryStack memorystack = MemoryStack.stackPush();

      int i1;
      label58: {
         try {
            int i = ALC10.alcGetInteger(this.device, 4098);
            if (OpenAlUtil.checkALCError(this.device, "Get attributes size")) {
               throw new IllegalStateException("Failed to get OpenAL attributes");
            }

            IntBuffer intbuffer = memorystack.mallocInt(i);
            ALC10.alcGetIntegerv(this.device, 4099, intbuffer);
            if (OpenAlUtil.checkALCError(this.device, "Get attributes")) {
               throw new IllegalStateException("Failed to get OpenAL attributes");
            }

            int j = 0;

            while(j < i) {
               int k = intbuffer.get(j++);
               if (k == 0) {
                  break;
               }

               int l = intbuffer.get(j++);
               if (k == 4112) {
                  i1 = l;
                  break label58;
               }
            }
         } catch (Throwable throwable1) {
            if (memorystack != null) {
               try {
                  memorystack.close();
               } catch (Throwable throwable) {
                  throwable1.addSuppressed(throwable);
               }
            }

            throw throwable1;
         }

         if (memorystack != null) {
            memorystack.close();
         }

         return 30;
      }

      if (memorystack != null) {
         memorystack.close();
      }

      return i1;
   }

   private static long tryOpenDevice() {
      for(int i = 0; i < 3; ++i) {
         long j = ALC10.alcOpenDevice((ByteBuffer)null);
         if (j != 0L && !OpenAlUtil.checkALCError(j, "Open device")) {
            return j;
         }
      }

      throw new IllegalStateException("Failed to open OpenAL device");
   }

   public void cleanup() {
      this.staticChannels.cleanup();
      this.streamingChannels.cleanup();
      ALC10.alcDestroyContext(this.context);
      if (this.device != 0L) {
         ALC10.alcCloseDevice(this.device);
      }

   }

   public Listener getListener() {
      return this.listener;
   }

   @Nullable
   public Channel acquireChannel(Library.Pool p_83698_) {
      return (p_83698_ == Library.Pool.STREAMING ? this.streamingChannels : this.staticChannels).acquire();
   }

   public void releaseChannel(Channel p_83696_) {
      if (!this.staticChannels.release(p_83696_) && !this.streamingChannels.release(p_83696_)) {
         throw new IllegalStateException("Tried to release unknown channel");
      }
   }

   public String getDebugString() {
      return String.format("Sounds: %d/%d + %d/%d", this.staticChannels.getUsedCount(), this.staticChannels.getMaxCount(), this.streamingChannels.getUsedCount(), this.streamingChannels.getMaxCount());
   }

   @OnlyIn(Dist.CLIENT)
   interface ChannelPool {
      @Nullable
      Channel acquire();

      boolean release(Channel p_83712_);

      void cleanup();

      int getMaxCount();

      int getUsedCount();
   }

   @OnlyIn(Dist.CLIENT)
   static class CountingChannelPool implements Library.ChannelPool {
      private final int limit;
      private final Set<Channel> activeChannels = Sets.newIdentityHashSet();

      public CountingChannelPool(int p_83716_) {
         this.limit = p_83716_;
      }

      @Nullable
      public Channel acquire() {
         if (this.activeChannels.size() >= this.limit) {
            Library.LOGGER.warn("Maximum sound pool size {} reached", (int)this.limit);
            return null;
         } else {
            Channel channel = Channel.create();
            if (channel != null) {
               this.activeChannels.add(channel);
            }

            return channel;
         }
      }

      public boolean release(Channel p_83719_) {
         if (!this.activeChannels.remove(p_83719_)) {
            return false;
         } else {
            p_83719_.destroy();
            return true;
         }
      }

      public void cleanup() {
         this.activeChannels.forEach(Channel::destroy);
         this.activeChannels.clear();
      }

      public int getMaxCount() {
         return this.limit;
      }

      public int getUsedCount() {
         return this.activeChannels.size();
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static enum Pool {
      STATIC,
      STREAMING;
   }
}