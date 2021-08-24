package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientboundBlockBreakAckPacket implements Packet<ClientGamePacketListener> {
   private static final Logger LOGGER = LogManager.getLogger();
   private final BlockPos pos;
   private final BlockState state;
   private final ServerboundPlayerActionPacket.Action action;
   private final boolean allGood;

   public ClientboundBlockBreakAckPacket(BlockPos p_131654_, BlockState p_131655_, ServerboundPlayerActionPacket.Action p_131656_, boolean p_131657_, String p_131658_) {
      this.pos = p_131654_.immutable();
      this.state = p_131655_;
      this.action = p_131656_;
      this.allGood = p_131657_;
   }

   public ClientboundBlockBreakAckPacket(FriendlyByteBuf p_178604_) {
      this.pos = p_178604_.readBlockPos();
      this.state = Block.BLOCK_STATE_REGISTRY.byId(p_178604_.readVarInt());
      this.action = p_178604_.readEnum(ServerboundPlayerActionPacket.Action.class);
      this.allGood = p_178604_.readBoolean();
   }

   public void write(FriendlyByteBuf p_131667_) {
      p_131667_.writeBlockPos(this.pos);
      p_131667_.writeVarInt(Block.getId(this.state));
      p_131667_.writeEnum(this.action);
      p_131667_.writeBoolean(this.allGood);
   }

   public void handle(ClientGamePacketListener p_131664_) {
      p_131664_.handleBlockBreakAck(this);
   }

   public BlockState getState() {
      return this.state;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public boolean allGood() {
      return this.allGood;
   }

   public ServerboundPlayerActionPacket.Action action() {
      return this.action;
   }
}