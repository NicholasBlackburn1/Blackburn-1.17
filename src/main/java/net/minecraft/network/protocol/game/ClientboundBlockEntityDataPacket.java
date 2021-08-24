package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundBlockEntityDataPacket implements Packet<ClientGamePacketListener> {
   public static final int TYPE_MOB_SPAWNER = 1;
   public static final int TYPE_ADV_COMMAND = 2;
   public static final int TYPE_BEACON = 3;
   public static final int TYPE_SKULL = 4;
   public static final int TYPE_CONDUIT = 5;
   public static final int TYPE_BANNER = 6;
   public static final int TYPE_STRUCT_COMMAND = 7;
   public static final int TYPE_END_GATEWAY = 8;
   public static final int TYPE_SIGN = 9;
   public static final int TYPE_BED = 11;
   public static final int TYPE_JIGSAW = 12;
   public static final int TYPE_CAMPFIRE = 13;
   public static final int TYPE_BEEHIVE = 14;
   private final BlockPos pos;
   private final int type;
   private final CompoundTag tag;

   public ClientboundBlockEntityDataPacket(BlockPos p_131695_, int p_131696_, CompoundTag p_131697_) {
      this.pos = p_131695_;
      this.type = p_131696_;
      this.tag = p_131697_;
   }

   public ClientboundBlockEntityDataPacket(FriendlyByteBuf p_178621_) {
      this.pos = p_178621_.readBlockPos();
      this.type = p_178621_.readUnsignedByte();
      this.tag = p_178621_.readNbt();
   }

   public void write(FriendlyByteBuf p_131706_) {
      p_131706_.writeBlockPos(this.pos);
      p_131706_.writeByte((byte)this.type);
      p_131706_.writeNbt(this.tag);
   }

   public void handle(ClientGamePacketListener p_131703_) {
      p_131703_.handleBlockEntityData(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public int getType() {
      return this.type;
   }

   public CompoundTag getTag() {
      return this.tag;
   }
}