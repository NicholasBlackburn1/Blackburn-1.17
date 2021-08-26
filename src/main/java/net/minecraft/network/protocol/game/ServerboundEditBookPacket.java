package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundEditBookPacket implements Packet<ServerGamePacketListener>
{
    public static final int f_182742_ = 4;
    private static final int f_182743_ = 128;
    private static final int f_182744_ = 8192;
    private static final int f_182745_ = 200;
    private final int slot;
    private final List<String> f_182746_;
    private final Optional<String> f_182747_;

    public ServerboundEditBookPacket(int p_182749_, List<String> p_182750_, Optional<String> p_182751_)
    {
        this.slot = p_182749_;
        this.f_182746_ = ImmutableList.copyOf(p_182750_);
        this.f_182747_ = p_182751_;
    }

    public ServerboundEditBookPacket(FriendlyByteBuf p_179592_)
    {
        this.slot = p_179592_.readVarInt();
        this.f_182746_ = p_179592_.readCollection(FriendlyByteBuf.m_182695_(Lists::newArrayListWithCapacity, 200), (p_182763_) ->
        {
            return p_182763_.readUtf(8192);
        });
        this.f_182747_ = p_179592_.m_182698_((p_182757_) ->
        {
            return p_182757_.readUtf(128);
        });
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeVarInt(this.slot);
        pBuf.writeCollection(this.f_182746_, (p_182759_, p_182760_) ->
        {
            p_182759_.writeUtf(p_182760_, 8192);
        });
        pBuf.m_182687_(this.f_182747_, (p_182753_, p_182754_) ->
        {
            p_182753_.writeUtf(p_182754_, 128);
        });
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handleEditBook(this);
    }

    public List<String> m_182755_()
    {
        return this.f_182746_;
    }

    public Optional<String> m_182761_()
    {
        return this.f_182747_;
    }

    public int getSlot()
    {
        return this.slot;
    }
}
