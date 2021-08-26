package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundRemoveMobEffectPacket implements Packet<ClientGamePacketListener>
{
    private final int entityId;
    private final MobEffect effect;

    public ClientboundRemoveMobEffectPacket(int p_132899_, MobEffect p_132900_)
    {
        this.entityId = p_132899_;
        this.effect = p_132900_;
    }

    public ClientboundRemoveMobEffectPacket(FriendlyByteBuf p_179177_)
    {
        this.entityId = p_179177_.readVarInt();
        this.effect = MobEffect.byId(p_179177_.readUnsignedByte());
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeVarInt(this.entityId);
        pBuf.writeByte(MobEffect.getId(this.effect));
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleRemoveMobEffect(this);
    }

    @Nullable
    public Entity getEntity(Level pLevel)
    {
        return pLevel.getEntity(this.entityId);
    }

    @Nullable
    public MobEffect getEffect()
    {
        return this.effect;
    }
}
