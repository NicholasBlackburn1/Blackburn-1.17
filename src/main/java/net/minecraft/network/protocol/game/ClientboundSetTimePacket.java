package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetTimePacket implements Packet<ClientGamePacketListener>
{
    private final long gameTime;
    private final long dayTime;

    public ClientboundSetTimePacket(long p_133349_, long p_133350_, boolean p_133351_)
    {
        this.gameTime = p_133349_;
        long i = p_133350_;

        if (!p_133351_)
        {
            i = -p_133350_;

            if (i == 0L)
            {
                i = -1L;
            }
        }

        this.dayTime = i;
    }

    public ClientboundSetTimePacket(FriendlyByteBuf p_179387_)
    {
        this.gameTime = p_179387_.readLong();
        this.dayTime = p_179387_.readLong();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeLong(this.gameTime);
        pBuf.writeLong(this.dayTime);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleSetTime(this);
    }

    public long getGameTime()
    {
        return this.gameTime;
    }

    public long getDayTime()
    {
        return this.dayTime;
    }
}
