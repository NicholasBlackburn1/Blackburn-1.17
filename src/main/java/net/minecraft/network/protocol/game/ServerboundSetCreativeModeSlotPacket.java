package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;

public class ServerboundSetCreativeModeSlotPacket implements Packet<ServerGamePacketListener>
{
    private final int slotNum;
    private final ItemStack itemStack;

    public ServerboundSetCreativeModeSlotPacket(int p_134553_, ItemStack p_134554_)
    {
        this.slotNum = p_134553_;
        this.itemStack = p_134554_.copy();
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handleSetCreativeModeSlot(this);
    }

    public ServerboundSetCreativeModeSlotPacket(FriendlyByteBuf p_179760_)
    {
        this.slotNum = p_179760_.readShort();
        this.itemStack = p_179760_.readItem();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeShort(this.slotNum);
        pBuf.writeItem(this.itemStack);
    }

    public int getSlotNum()
    {
        return this.slotNum;
    }

    public ItemStack getItem()
    {
        return this.itemStack;
    }
}
