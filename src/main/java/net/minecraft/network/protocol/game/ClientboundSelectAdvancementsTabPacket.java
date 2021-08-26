package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundSelectAdvancementsTabPacket implements Packet<ClientGamePacketListener>
{
    @Nullable
    private final ResourceLocation tab;

    public ClientboundSelectAdvancementsTabPacket(@Nullable ResourceLocation p_133006_)
    {
        this.tab = p_133006_;
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleSelectAdvancementsTab(this);
    }

    public ClientboundSelectAdvancementsTabPacket(FriendlyByteBuf p_179198_)
    {
        if (p_179198_.readBoolean())
        {
            this.tab = p_179198_.readResourceLocation();
        }
        else
        {
            this.tab = null;
        }
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeBoolean(this.tab != null);

        if (this.tab != null)
        {
            pBuf.writeResourceLocation(this.tab);
        }
    }

    @Nullable
    public ResourceLocation getTab()
    {
        return this.tab;
    }
}
