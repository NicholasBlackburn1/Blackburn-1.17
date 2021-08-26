package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WitherRoseBlock extends FlowerBlock
{
    public WitherRoseBlock(MobEffect p_58235_, BlockBehaviour.Properties p_58236_)
    {
        super(p_58235_, 8, p_58236_);
    }

    protected boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos)
    {
        return super.mayPlaceOn(pState, pLevel, pPos) || pState.is(Blocks.NETHERRACK) || pState.is(Blocks.SOUL_SAND) || pState.is(Blocks.SOUL_SOIL);
    }

    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRand)
    {
        VoxelShape voxelshape = this.getShape(pState, pLevel, pPos, CollisionContext.empty());
        Vec3 vec3 = voxelshape.bounds().getCenter();
        double d0 = (double)pPos.getX() + vec3.x;
        double d1 = (double)pPos.getZ() + vec3.z;

        for (int i = 0; i < 3; ++i)
        {
            if (pRand.nextBoolean())
            {
                pLevel.addParticle(ParticleTypes.SMOKE, d0 + pRand.nextDouble() / 5.0D, (double)pPos.getY() + (0.5D - pRand.nextDouble()), d1 + pRand.nextDouble() / 5.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity)
    {
        if (!pLevel.isClientSide && pLevel.getDifficulty() != Difficulty.PEACEFUL)
        {
            if (pEntity instanceof LivingEntity)
            {
                LivingEntity livingentity = (LivingEntity)pEntity;

                if (!livingentity.isInvulnerableTo(DamageSource.WITHER))
                {
                    livingentity.addEffect(new MobEffectInstance(MobEffects.WITHER, 40));
                }
            }
        }
    }
}
