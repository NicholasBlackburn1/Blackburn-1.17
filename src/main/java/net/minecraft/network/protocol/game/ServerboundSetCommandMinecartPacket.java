package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.Level;

public class ServerboundSetCommandMinecartPacket implements Packet<ServerGamePacketListener>
{
    private final int entity;
    private final String command;
    private final boolean trackOutput;

    public ServerboundSetCommandMinecartPacket(int p_134534_, String p_134535_, boolean p_134536_)
    {
        this.entity = p_134534_;
        this.command = p_134535_;
        this.trackOutput = p_134536_;
    }

    public ServerboundSetCommandMinecartPacket(FriendlyByteBuf p_179758_)
    {
        this.entity = p_179758_.readVarInt();
        this.command = p_179758_.readUtf();
        this.trackOutput = p_179758_.readBoolean();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeVarInt(this.entity);
        pBuf.writeUtf(this.command);
        pBuf.writeBoolean(this.trackOutput);
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handleSetCommandMinecart(this);
    }

    @Nullable
    public BaseCommandBlock getCommandBlock(Level pLevel)
    {
        Entity entity = pLevel.getEntity(this.entity);
        return entity instanceof MinecartCommandBlock ? ((MinecartCommandBlock)entity).getCommandBlock() : null;
    }

    public String getCommand()
    {
        return this.command;
    }

    public boolean isTrackOutput()
    {
        return this.trackOutput;
    }
}
