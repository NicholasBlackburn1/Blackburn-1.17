package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EndPortalBlock extends BaseEntityBlock
{
    protected static final VoxelShape SHAPE = Block.box(0.0D, 6.0D, 0.0D, 16.0D, 12.0D, 16.0D);

    protected EndPortalBlock(BlockBehaviour.Properties p_53017_)
    {
        super(p_53017_);
    }

    public BlockEntity newBlockEntity(BlockPos p_153196_, BlockState p_153197_)
    {
        return new TheEndPortalBlockEntity(p_153196_, p_153197_);
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        return SHAPE;
    }

    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity)
    {
        if (pLevel instanceof ServerLevel && !pEntity.isPassenger() && !pEntity.isVehicle() && pEntity.canChangeDimensions() && Shapes.joinIsNotEmpty(Shapes.create(pEntity.getBoundingBox().move((double)(-pPos.getX()), (double)(-pPos.getY()), (double)(-pPos.getZ()))), pState.getShape(pLevel, pPos), BooleanOp.AND))
        {
            ResourceKey<Level> resourcekey = pLevel.dimension() == Level.END ? Level.OVERWORLD : Level.END;
            ServerLevel serverlevel = ((ServerLevel)pLevel).getServer().getLevel(resourcekey);

            if (serverlevel == null)
            {
                return;
            }

            pEntity.changeDimension(serverlevel);
        }
    }

    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRand)
    {
        double d0 = (double)pPos.getX() + pRand.nextDouble();
        double d1 = (double)pPos.getY() + 0.8D;
        double d2 = (double)pPos.getZ() + pRand.nextDouble();
        pLevel.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
    }

    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState)
    {
        return ItemStack.EMPTY;
    }

    public boolean canBeReplaced(BlockState pState, Fluid pFluid)
    {
        return false;
    }
}
