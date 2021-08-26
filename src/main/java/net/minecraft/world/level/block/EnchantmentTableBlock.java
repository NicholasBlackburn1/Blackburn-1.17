package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EnchantmentTableBlock extends BaseEntityBlock
{
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);

    protected EnchantmentTableBlock(BlockBehaviour.Properties p_52953_)
    {
        super(p_52953_);
    }

    public boolean useShapeForLightOcclusion(BlockState pState)
    {
        return true;
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        return SHAPE;
    }

    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRand)
    {
        super.animateTick(pState, pLevel, pPos, pRand);

        for (int i = -2; i <= 2; ++i)
        {
            for (int j = -2; j <= 2; ++j)
            {
                if (i > -2 && i < 2 && j == -1)
                {
                    j = 2;
                }

                if (pRand.nextInt(16) == 0)
                {
                    for (int k = 0; k <= 1; ++k)
                    {
                        BlockPos blockpos = pPos.offset(i, k, j);

                        if (pLevel.getBlockState(blockpos).is(Blocks.BOOKSHELF))
                        {
                            if (!pLevel.isEmptyBlock(pPos.offset(i / 2, 0, j / 2)))
                            {
                                break;
                            }

                            pLevel.addParticle(ParticleTypes.ENCHANT, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 2.0D, (double)pPos.getZ() + 0.5D, (double)((float)i + pRand.nextFloat()) - 0.5D, (double)((float)k - pRand.nextFloat() - 1.0F), (double)((float)j + pRand.nextFloat()) - 0.5D);
                        }
                    }
                }
            }
        }
    }

    public RenderShape getRenderShape(BlockState pState)
    {
        return RenderShape.MODEL;
    }

    public BlockEntity newBlockEntity(BlockPos p_153186_, BlockState p_153187_)
    {
        return new EnchantmentTableBlockEntity(p_153186_, p_153187_);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153182_, BlockState p_153183_, BlockEntityType<T> p_153184_)
    {
        return p_153182_.isClientSide ? createTickerHelper(p_153184_, BlockEntityType.ENCHANTING_TABLE, EnchantmentTableBlockEntity::bookAnimationTick) : null;
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit)
    {
        if (pLevel.isClientSide)
        {
            return InteractionResult.SUCCESS;
        }
        else
        {
            pPlayer.openMenu(pState.getMenuProvider(pLevel, pPos));
            return InteractionResult.CONSUME;
        }
    }

    @Nullable
    public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos)
    {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);

        if (blockentity instanceof EnchantmentTableBlockEntity)
        {
            Component component = ((Nameable)blockentity).getDisplayName();
            return new SimpleMenuProvider((p_52959_, p_52960_, p_52961_) ->
            {
                return new EnchantmentMenu(p_52959_, p_52960_, ContainerLevelAccess.create(pLevel, pPos));
            }, component);
        }
        else
        {
            return null;
        }
    }

    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack)
    {
        if (pStack.hasCustomHoverName())
        {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);

            if (blockentity instanceof EnchantmentTableBlockEntity)
            {
                ((EnchantmentTableBlockEntity)blockentity).setCustomName(pStack.getHoverName());
            }
        }
    }

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType)
    {
        return false;
    }
}
