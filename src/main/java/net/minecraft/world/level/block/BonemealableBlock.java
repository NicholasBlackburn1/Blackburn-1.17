package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface BonemealableBlock
{
    boolean isValidBonemealTarget(BlockGetter pLevel, BlockPos pPos, BlockState pState, boolean pIsClient);

    boolean isBonemealSuccess(Level pLevel, Random pRand, BlockPos pPos, BlockState pState);

    void performBonemeal(ServerLevel pLevel, Random pRand, BlockPos pPos, BlockState pState);
}
