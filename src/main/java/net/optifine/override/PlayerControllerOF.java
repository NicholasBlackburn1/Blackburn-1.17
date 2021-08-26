package net.optifine.override;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class PlayerControllerOF extends MultiPlayerGameMode
{
    private boolean acting = false;
    private BlockPos lastClickBlockPos = null;
    private Entity lastClickEntity = null;

    public PlayerControllerOF(Minecraft p_105203_, ClientPacketListener p_105204_)
    {
        super(p_105203_, p_105204_);
    }

    public boolean startDestroyBlock(BlockPos pLoc, Direction pFace)
    {
        this.acting = true;
        this.lastClickBlockPos = pLoc;
        boolean flag = super.startDestroyBlock(pLoc, pFace);
        this.acting = false;
        return flag;
    }

    public boolean continueDestroyBlock(BlockPos pPosBlock, Direction pDirectionFacing)
    {
        this.acting = true;
        this.lastClickBlockPos = pPosBlock;
        boolean flag = super.continueDestroyBlock(pPosBlock, pDirectionFacing);
        this.acting = false;
        return flag;
    }

    public InteractionResult useItem(Player pPlayer, Level pLevel, InteractionHand pHand)
    {
        this.acting = true;
        InteractionResult interactionresult = super.useItem(pPlayer, pLevel, pHand);
        this.acting = false;
        return interactionresult;
    }

    public InteractionResult useItemOn(LocalPlayer p_105263_, ClientLevel p_105264_, InteractionHand p_105265_, BlockHitResult p_105266_)
    {
        this.acting = true;
        this.lastClickBlockPos = p_105266_.getBlockPos();
        InteractionResult interactionresult = super.useItemOn(p_105263_, p_105264_, p_105265_, p_105266_);
        this.acting = false;
        return interactionresult;
    }

    public InteractionResult interact(Player pPlayer, Entity pTarget, InteractionHand pHand)
    {
        this.lastClickEntity = pTarget;
        return super.interact(pPlayer, pTarget, pHand);
    }

    public InteractionResult interactAt(Player pPlayer, Entity pTarget, EntityHitResult pRay, InteractionHand pHand)
    {
        this.lastClickEntity = pTarget;
        return super.interactAt(pPlayer, pTarget, pRay, pHand);
    }

    public boolean isActing()
    {
        return this.acting;
    }

    public BlockPos getLastClickBlockPos()
    {
        return this.lastClickBlockPos;
    }

    public Entity getLastClickEntity()
    {
        return this.lastClickEntity;
    }
}
