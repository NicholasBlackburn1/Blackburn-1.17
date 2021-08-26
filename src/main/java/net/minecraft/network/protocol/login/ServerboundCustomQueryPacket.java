package net.minecraft.network.protocol.login;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundCustomQueryPacket implements Packet<ServerLoginPacketListener>
{
    private static final int MAX_PAYLOAD_SIZE = 1048576;
    private final int transactionId;
    private final FriendlyByteBuf data;

    public ServerboundCustomQueryPacket(int p_134829_, @Nullable FriendlyByteBuf p_134830_)
    {
        this.transactionId = p_134829_;
        this.data = p_134830_;
    }

    public ServerboundCustomQueryPacket(FriendlyByteBuf p_179823_)
    {
        this.transactionId = p_179823_.readVarInt();

        if (p_179823_.readBoolean())
        {
            int i = p_179823_.readableBytes();

            if (i < 0 || i > 1048576)
            {
                throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
            }

            this.data = new FriendlyByteBuf(p_179823_.readBytes(i));
        }
        else
        {
            this.data = null;
        }
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeVarInt(this.transactionId);

        if (this.data != null)
        {
            pBuf.writeBoolean(true);
            pBuf.writeBytes(this.data.copy());
        }
        else
        {
            pBuf.writeBoolean(false);
        }
    }

    public void handle(ServerLoginPacketListener pHandler)
    {
        pHandler.handleCustomQueryPacket(this);
    }

    public int getTransactionId()
    {
        return this.transactionId;
    }

    public FriendlyByteBuf getData()
    {
        return this.data;
    }
}
