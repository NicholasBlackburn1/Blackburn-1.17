package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.BitSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class ClientboundLightUpdatePacket implements Packet<ClientGamePacketListener> {
   private final int x;
   private final int z;
   private final BitSet skyYMask;
   private final BitSet blockYMask;
   private final BitSet emptySkyYMask;
   private final BitSet emptyBlockYMask;
   private final List<byte[]> skyUpdates;
   private final List<byte[]> blockUpdates;
   private final boolean trustEdges;

   public ClientboundLightUpdatePacket(ChunkPos p_178912_, LevelLightEngine p_178913_, @Nullable BitSet p_178914_, @Nullable BitSet p_178915_, boolean p_178916_) {
      this.x = p_178912_.x;
      this.z = p_178912_.z;
      this.trustEdges = p_178916_;
      this.skyYMask = new BitSet();
      this.blockYMask = new BitSet();
      this.emptySkyYMask = new BitSet();
      this.emptyBlockYMask = new BitSet();
      this.skyUpdates = Lists.newArrayList();
      this.blockUpdates = Lists.newArrayList();

      for(int i = 0; i < p_178913_.getLightSectionCount(); ++i) {
         if (p_178914_ == null || p_178914_.get(i)) {
            prepareSectionData(p_178912_, p_178913_, LightLayer.SKY, i, this.skyYMask, this.emptySkyYMask, this.skyUpdates);
         }

         if (p_178915_ == null || p_178915_.get(i)) {
            prepareSectionData(p_178912_, p_178913_, LightLayer.BLOCK, i, this.blockYMask, this.emptyBlockYMask, this.blockUpdates);
         }
      }

   }

   private static void prepareSectionData(ChunkPos p_178920_, LevelLightEngine p_178921_, LightLayer p_178922_, int p_178923_, BitSet p_178924_, BitSet p_178925_, List<byte[]> p_178926_) {
      DataLayer datalayer = p_178921_.getLayerListener(p_178922_).getDataLayerData(SectionPos.of(p_178920_, p_178921_.getMinLightSection() + p_178923_));
      if (datalayer != null) {
         if (datalayer.isEmpty()) {
            p_178925_.set(p_178923_);
         } else {
            p_178924_.set(p_178923_);
            p_178926_.add((byte[])datalayer.getData().clone());
         }
      }

   }

   public ClientboundLightUpdatePacket(FriendlyByteBuf p_178918_) {
      this.x = p_178918_.readVarInt();
      this.z = p_178918_.readVarInt();
      this.trustEdges = p_178918_.readBoolean();
      this.skyYMask = p_178918_.readBitSet();
      this.blockYMask = p_178918_.readBitSet();
      this.emptySkyYMask = p_178918_.readBitSet();
      this.emptyBlockYMask = p_178918_.readBitSet();
      this.skyUpdates = p_178918_.readList((p_178930_) -> {
         return p_178930_.readByteArray(2048);
      });
      this.blockUpdates = p_178918_.readList((p_178928_) -> {
         return p_178928_.readByteArray(2048);
      });
   }

   public void write(FriendlyByteBuf p_132351_) {
      p_132351_.writeVarInt(this.x);
      p_132351_.writeVarInt(this.z);
      p_132351_.writeBoolean(this.trustEdges);
      p_132351_.writeBitSet(this.skyYMask);
      p_132351_.writeBitSet(this.blockYMask);
      p_132351_.writeBitSet(this.emptySkyYMask);
      p_132351_.writeBitSet(this.emptyBlockYMask);
      p_132351_.writeCollection(this.skyUpdates, FriendlyByteBuf::writeByteArray);
      p_132351_.writeCollection(this.blockUpdates, FriendlyByteBuf::writeByteArray);
   }

   public void handle(ClientGamePacketListener p_132348_) {
      p_132348_.handleLightUpdatePacked(this);
   }

   public int getX() {
      return this.x;
   }

   public int getZ() {
      return this.z;
   }

   public BitSet getSkyYMask() {
      return this.skyYMask;
   }

   public BitSet getEmptySkyYMask() {
      return this.emptySkyYMask;
   }

   public List<byte[]> getSkyUpdates() {
      return this.skyUpdates;
   }

   public BitSet getBlockYMask() {
      return this.blockYMask;
   }

   public BitSet getEmptyBlockYMask() {
      return this.emptyBlockYMask;
   }

   public List<byte[]> getBlockUpdates() {
      return this.blockUpdates;
   }

   public boolean getTrustEdges() {
      return this.trustEdges;
   }
}