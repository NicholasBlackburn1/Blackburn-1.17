package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class PacketDecoder extends ByteToMessageDecoder {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Marker MARKER = MarkerManager.getMarker("PACKET_RECEIVED", Connection.PACKET_MARKER);
   private final PacketFlow flow;

   public PacketDecoder(PacketFlow p_130533_) {
      this.flow = p_130533_;
   }

   protected void decode(ChannelHandlerContext p_130535_, ByteBuf p_130536_, List<Object> p_130537_) throws Exception {
      if (p_130536_.readableBytes() != 0) {
         FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(p_130536_);
         int i = friendlybytebuf.readVarInt();
         Packet<?> packet = p_130535_.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().createPacket(this.flow, i, friendlybytebuf);
         if (packet == null) {
            throw new IOException("Bad packet id " + i);
         } else if (friendlybytebuf.readableBytes() > 0) {
            throw new IOException("Packet " + p_130535_.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().getId() + "/" + i + " (" + packet.getClass().getSimpleName() + ") was larger than I expected, found " + friendlybytebuf.readableBytes() + " bytes extra whilst reading packet " + i);
         } else {
            p_130537_.add(packet);
            if (LOGGER.isDebugEnabled()) {
               LOGGER.debug(MARKER, " IN: [{}:{}] {}", p_130535_.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get(), i, packet.getClass().getName());
            }

         }
      }
   }
}