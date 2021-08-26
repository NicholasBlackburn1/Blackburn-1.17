package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundSetBeaconPacket implements Packet<ServerGamePacketListener>
{
    private final int primary;
    private final int secondary;

    public ServerboundSetBeaconPacket(int p_134476_, int p_134477_)
    {
        this.primary = p_134476_;
        this.secondary = p_134477_;
    }

    public ServerboundSetBeaconPacket(FriendlyByteBuf p_179749_)
    {
        this.primary = p_179749_.readVarInt();
        this.secondary = p_179749_.readVarInt();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeVarInt(this.primary);
        pBuf.writeVarInt(this.secondary);
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handleSetBeaconPacket(this);
    }

    public int getPrimary()
    {
        return this.primary;
    }

    public int getSecondary()
    {
        return this.secondary;
    }
}
