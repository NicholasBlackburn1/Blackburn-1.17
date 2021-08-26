package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPlayerActionPacket implements Packet<ServerGamePacketListener>
{
    private final BlockPos pos;
    private final Direction direction;
    private final ServerboundPlayerActionPacket.Action action;

    public ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action p_134272_, BlockPos p_134273_, Direction p_134274_)
    {
        this.action = p_134272_;
        this.pos = p_134273_.immutable();
        this.direction = p_134274_;
    }

    public ServerboundPlayerActionPacket(FriendlyByteBuf p_179711_)
    {
        this.action = p_179711_.readEnum(ServerboundPlayerActionPacket.Action.class);
        this.pos = p_179711_.readBlockPos();
        this.direction = Direction.from3DDataValue(p_179711_.readUnsignedByte());
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeEnum(this.action);
        pBuf.writeBlockPos(this.pos);
        pBuf.writeByte(this.direction.get3DDataValue());
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handlePlayerAction(this);
    }

    public BlockPos getPos()
    {
        return this.pos;
    }

    public Direction getDirection()
    {
        return this.direction;
    }

    public ServerboundPlayerActionPacket.Action getAction()
    {
        return this.action;
    }

    public static enum Action
    {
        START_DESTROY_BLOCK,
        ABORT_DESTROY_BLOCK,
        STOP_DESTROY_BLOCK,
        DROP_ALL_ITEMS,
        DROP_ITEM,
        RELEASE_USE_ITEM,
        SWAP_ITEM_WITH_OFFHAND;
    }
}
