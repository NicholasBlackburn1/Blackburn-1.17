package net.minecraft.client.tutorial;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.Input;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

public interface TutorialStepInstance
{
default void clear()
    {
    }

default void tick()
    {
    }

default void onInput(Input pInput)
    {
    }

default void onMouse(double pVelocityX, double p_120615_)
    {
    }

default void onLookAt(ClientLevel pLevel, HitResult pResult)
    {
    }

default void onDestroyBlock(ClientLevel pLevel, BlockPos pPos, BlockState pState, float pDiggingStage)
    {
    }

default void onOpenInventory()
    {
    }

default void onGetItem(ItemStack pStack)
    {
    }
}
