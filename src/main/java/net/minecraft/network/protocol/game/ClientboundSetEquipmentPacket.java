package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ClientboundSetEquipmentPacket implements Packet<ClientGamePacketListener>
{
    private static final byte CONTINUE_MASK = -128;
    private final int entity;
    private final List<Pair<EquipmentSlot, ItemStack>> slots;

    public ClientboundSetEquipmentPacket(int p_133202_, List<Pair<EquipmentSlot, ItemStack>> p_133203_)
    {
        this.entity = p_133202_;
        this.slots = p_133203_;
    }

    public ClientboundSetEquipmentPacket(FriendlyByteBuf p_179297_)
    {
        this.entity = p_179297_.readVarInt();
        EquipmentSlot[] aequipmentslot = EquipmentSlot.values();
        this.slots = Lists.newArrayList();
        int i;

        do
        {
            i = p_179297_.readByte();
            EquipmentSlot equipmentslot = aequipmentslot[i & 127];
            ItemStack itemstack = p_179297_.readItem();
            this.slots.add(Pair.of(equipmentslot, itemstack));
        }
        while ((i & -128) != 0);
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeVarInt(this.entity);
        int i = this.slots.size();

        for (int j = 0; j < i; ++j)
        {
            Pair<EquipmentSlot, ItemStack> pair = this.slots.get(j);
            EquipmentSlot equipmentslot = pair.getFirst();
            boolean flag = j != i - 1;
            int k = equipmentslot.ordinal();
            pBuf.writeByte(flag ? k | -128 : k);
            pBuf.writeItem(pair.getSecond());
        }
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleSetEquipment(this);
    }

    public int getEntity()
    {
        return this.entity;
    }

    public List<Pair<EquipmentSlot, ItemStack>> getSlots()
    {
        return this.slots;
    }
}
