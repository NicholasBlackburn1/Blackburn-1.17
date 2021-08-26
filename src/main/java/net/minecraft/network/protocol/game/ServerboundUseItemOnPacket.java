package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

public class ServerboundUseItemOnPacket implements Packet<ServerGamePacketListener>
{
    private final BlockHitResult blockHit;
    private final InteractionHand hand;

    public ServerboundUseItemOnPacket(InteractionHand p_134695_, BlockHitResult p_134696_)
    {
        this.hand = p_134695_;
        this.blockHit = p_134696_;
    }

    public ServerboundUseItemOnPacket(FriendlyByteBuf p_179796_)
    {
        this.hand = p_179796_.readEnum(InteractionHand.class);
        this.blockHit = p_179796_.readBlockHitResult();
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeEnum(this.hand);
        pBuf.writeBlockHitResult(this.blockHit);
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handleUseItemOn(this);
    }

    public InteractionHand getHand()
    {
        return this.hand;
    }

    public BlockHitResult getHitResult()
    {
        return this.blockHit;
    }
}
