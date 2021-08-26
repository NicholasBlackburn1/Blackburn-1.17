package net.minecraft.network.protocol.login;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.SerializableUUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundGameProfilePacket implements Packet<ClientLoginPacketListener>
{
    private final GameProfile gameProfile;

    public ClientboundGameProfilePacket(GameProfile p_134767_)
    {
        this.gameProfile = p_134767_;
    }

    public ClientboundGameProfilePacket(FriendlyByteBuf p_179814_)
    {
        int[] aint = new int[4];

        for (int i = 0; i < aint.length; ++i)
        {
            aint[i] = p_179814_.readInt();
        }

        UUID uuid = SerializableUUID.m_123281_(aint);
        String s = p_179814_.readUtf(16);
        this.gameProfile = new GameProfile(uuid, s);
    }

    public void write(FriendlyByteBuf pBuf)
    {
        for (int i : SerializableUUID.uuidToIntArray(this.gameProfile.getId()))
        {
            pBuf.writeInt(i);
        }

        pBuf.writeUtf(this.gameProfile.getName());
    }

    public void handle(ClientLoginPacketListener pHandler)
    {
        pHandler.handleGameProfile(this);
    }

    public GameProfile getGameProfile()
    {
        return this.gameProfile;
    }
}
