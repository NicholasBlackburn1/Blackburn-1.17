package net.minecraft.world.level.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BlockBehaviour
{
    protected static final Direction[] UPDATE_SHAPE_ORDER = new Direction[] {Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP};
    protected final Material material;
    protected final boolean hasCollision;
    protected final float explosionResistance;
    protected final boolean isRandomlyTicking;
    protected final SoundType soundType;
    protected final float friction;
    protected final float speedFactor;
    protected final float jumpFactor;
    protected final boolean dynamicShape;
    protected final BlockBehaviour.Properties properties;
    @Nullable
    protected ResourceLocation drops;

    public BlockBehaviour(BlockBehaviour.Properties p_60452_)
    {
        this.material = p_60452_.material;
        this.hasCollision = p_60452_.hasCollision;
        this.drops = p_60452_.drops;
        this.explosionResistance = p_60452_.explosionResistance;
        this.isRandomlyTicking = p_60452_.isRandomlyTicking;
        this.soundType = p_60452_.soundType;
        this.friction = p_60452_.friction;
        this.speedFactor = p_60452_.speedFactor;
        this.jumpFactor = p_60452_.jumpFactor;
        this.dynamicShape = p_60452_.dynamicShape;
        this.properties = p_60452_;
    }

    @Deprecated
    public void updateIndirectNeighbourShapes(BlockState pState, LevelAccessor pLevel, BlockPos pPos, int pFlags, int pRecursionLeft)
    {
    }

    @Deprecated
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType)
    {
        switch (pType)
        {
            case LAND:
                return !pState.isCollisionShapeFullBlock(pLevel, pPos);

            case WATER:
                return pLevel.getFluidState(pPos).is(FluidTags.WATER);

            case AIR:
                return !pState.isCollisionShapeFullBlock(pLevel, pPos);

            default:
                return false;
        }
    }

    @Deprecated
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos)
    {
        return pState;
    }

    @Deprecated
    public boolean skipRendering(BlockState pState, BlockState pAdjacentBlockState, Direction pSide)
    {
        return false;
    }

    @Deprecated
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving)
    {
        DebugPackets.sendNeighborsUpdatePacket(pLevel, pPos);
    }

    @Deprecated
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving)
    {
    }

    @Deprecated
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving)
    {
        if (pState.hasBlockEntity() && !pState.is(pNewState.getBlock()))
        {
            pLevel.removeBlockEntity(pPos);
        }
    }

    @Deprecated
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit)
    {
        return InteractionResult.PASS;
    }

    @Deprecated
    public boolean triggerEvent(BlockState pState, Level pLevel, BlockPos pPos, int pId, int pParam)
    {
        return false;
    }

    @Deprecated
    public RenderShape getRenderShape(BlockState pState)
    {
        return RenderShape.MODEL;
    }

    @Deprecated
    public boolean useShapeForLightOcclusion(BlockState pState)
    {
        return false;
    }

    @Deprecated
    public boolean isSignalSource(BlockState pState)
    {
        return false;
    }

    @Deprecated
    public PushReaction getPistonPushReaction(BlockState pState)
    {
        return this.material.getPushReaction();
    }

    @Deprecated
    public FluidState getFluidState(BlockState pState)
    {
        return Fluids.EMPTY.defaultFluidState();
    }

    @Deprecated
    public boolean hasAnalogOutputSignal(BlockState pState)
    {
        return false;
    }

    public BlockBehaviour.OffsetType getOffsetType()
    {
        return BlockBehaviour.OffsetType.NONE;
    }

    public float getMaxHorizontalOffset()
    {
        return 0.25F;
    }

    public float getMaxVerticalOffset()
    {
        return 0.2F;
    }

    @Deprecated
    public BlockState rotate(BlockState pState, Rotation pRot)
    {
        return pState;
    }

    @Deprecated
    public BlockState mirror(BlockState pState, Mirror pMirror)
    {
        return pState;
    }

    @Deprecated
    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext)
    {
        return this.material.isReplaceable() && (pUseContext.getItemInHand().isEmpty() || !pUseContext.getItemInHand().is(this.asItem()));
    }

    @Deprecated
    public boolean canBeReplaced(BlockState pState, Fluid pUseContext)
    {
        return this.material.isReplaceable() || !this.material.isSolid();
    }

    @Deprecated
    public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder)
    {
        ResourceLocation resourcelocation = this.getLootTable();

        if (resourcelocation == BuiltInLootTables.EMPTY)
        {
            return Collections.emptyList();
        }
        else
        {
            LootContext lootcontext = pBuilder.withParameter(LootContextParams.BLOCK_STATE, pState).create(LootContextParamSets.BLOCK);
            ServerLevel serverlevel = lootcontext.getLevel();
            LootTable loottable = serverlevel.getServer().getLootTables().get(resourcelocation);
            return loottable.getRandomItems(lootcontext);
        }
    }

    @Deprecated
    public long getSeed(BlockState pState, BlockPos pPos)
    {
        return Mth.getSeed(pPos);
    }

    @Deprecated
    public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos)
    {
        return pState.getShape(pLevel, pPos);
    }

    @Deprecated
    public VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pReader, BlockPos pPos)
    {
        return this.getCollisionShape(pState, pReader, pPos, CollisionContext.empty());
    }

    @Deprecated
    public VoxelShape getInteractionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos)
    {
        return Shapes.empty();
    }

    @Deprecated
    public int getLightBlock(BlockState pState, BlockGetter pLevel, BlockPos pPos)
    {
        if (pState.isSolidRender(pLevel, pPos))
        {
            return pLevel.getMaxLightLevel();
        }
        else
        {
            return pState.propagatesSkylightDown(pLevel, pPos) ? 0 : 1;
        }
    }

    @Nullable
    @Deprecated
    public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos)
    {
        return null;
    }

    @Deprecated
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos)
    {
        return true;
    }

    @Deprecated
    public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos)
    {
        return pState.isCollisionShapeFullBlock(pLevel, pPos) ? 0.2F : 1.0F;
    }

    @Deprecated
    public int getAnalogOutputSignal(BlockState pBlockState, Level pLevel, BlockPos pPos)
    {
        return 0;
    }

    @Deprecated
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        return Shapes.block();
    }

    @Deprecated
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        return this.hasCollision ? pState.getShape(pLevel, pPos) : Shapes.empty();
    }

    @Deprecated
    public boolean isCollisionShapeFullBlock(BlockState p_181242_, BlockGetter p_181243_, BlockPos p_181244_)
    {
        return Block.isShapeFullBlock(p_181242_.getCollisionShape(p_181243_, p_181244_));
    }

    @Deprecated
    public VoxelShape getVisualShape(BlockState pState, BlockGetter pReader, BlockPos pPos, CollisionContext pContext)
    {
        return this.getCollisionShape(pState, pReader, pPos, pContext);
    }

    @Deprecated
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom)
    {
        this.tick(pState, pLevel, pPos, pRandom);
    }

    @Deprecated
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRand)
    {
    }

    @Deprecated
    public float getDestroyProgress(BlockState pState, Player pPlayer, BlockGetter pLevel, BlockPos pPos)
    {
        float f = pState.getDestroySpeed(pLevel, pPos);

        if (f == -1.0F)
        {
            return 0.0F;
        }
        else
        {
            int i = pPlayer.hasCorrectToolForDrops(pState) ? 30 : 100;
            return pPlayer.getDestroySpeed(pState) / f / (float)i;
        }
    }

    @Deprecated
    public void spawnAfterBreak(BlockState pState, ServerLevel pLevel, BlockPos pPos, ItemStack pStack)
    {
    }

    @Deprecated
    public void attack(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer)
    {
    }

    @Deprecated
    public int getSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide)
    {
        return 0;
    }

    @Deprecated
    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity)
    {
    }

    @Deprecated
    public int getDirectSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide)
    {
        return 0;
    }

    public final ResourceLocation getLootTable()
    {
        if (this.drops == null)
        {
            ResourceLocation resourcelocation = Registry.BLOCK.getKey(this.asBlock());
            this.drops = new ResourceLocation(resourcelocation.getNamespace(), "blocks/" + resourcelocation.getPath());
        }

        return this.drops;
    }

    @Deprecated
    public void onProjectileHit(Level pLevel, BlockState pState, BlockHitResult pHit, Projectile pProjectile)
    {
    }

    public abstract Item asItem();

    protected abstract Block asBlock();

    public MaterialColor defaultMaterialColor()
    {
        return this.properties.materialColor.apply(this.asBlock().defaultBlockState());
    }

    public float defaultDestroyTime()
    {
        return this.properties.destroyTime;
    }

    public abstract static class BlockStateBase extends StateHolder<Block, BlockState>
    {
        private final int lightEmission;
        private final boolean useShapeForLightOcclusion;
        private final boolean isAir;
        private final Material material;
        private final MaterialColor materialColor;
        private final float destroySpeed;
        private final boolean requiresCorrectToolForDrops;
        private final boolean canOcclude;
        private final BlockBehaviour.StatePredicate isRedstoneConductor;
        private final BlockBehaviour.StatePredicate isSuffocating;
        private final BlockBehaviour.StatePredicate isViewBlocking;
        private final BlockBehaviour.StatePredicate hasPostProcess;
        private final BlockBehaviour.StatePredicate emissiveRendering;
        @Nullable
        protected BlockBehaviour.BlockStateBase.Cache cache;

        protected BlockStateBase(Block p_60608_, ImmutableMap < Property<?>, Comparable<? >> p_60609_, MapCodec<BlockState> p_60610_)
        {
            super(p_60608_, p_60609_, p_60610_);
            BlockBehaviour.Properties blockbehaviour$properties = p_60608_.properties;
            this.lightEmission = blockbehaviour$properties.lightEmission.applyAsInt(this.asState());
            this.useShapeForLightOcclusion = p_60608_.useShapeForLightOcclusion(this.asState());
            this.isAir = blockbehaviour$properties.isAir;
            this.material = blockbehaviour$properties.material;
            this.materialColor = blockbehaviour$properties.materialColor.apply(this.asState());
            this.destroySpeed = blockbehaviour$properties.destroyTime;
            this.requiresCorrectToolForDrops = blockbehaviour$properties.requiresCorrectToolForDrops;
            this.canOcclude = blockbehaviour$properties.canOcclude;
            this.isRedstoneConductor = blockbehaviour$properties.isRedstoneConductor;
            this.isSuffocating = blockbehaviour$properties.isSuffocating;
            this.isViewBlocking = blockbehaviour$properties.isViewBlocking;
            this.hasPostProcess = blockbehaviour$properties.hasPostProcess;
            this.emissiveRendering = blockbehaviour$properties.emissiveRendering;
        }

        public void initCache()
        {
            if (!this.getBlock().hasDynamicShape())
            {
                this.cache = new BlockBehaviour.BlockStateBase.Cache(this.asState());
            }
        }

        public Block getBlock()
        {
            return this.owner;
        }

        public Material getMaterial()
        {
            return this.material;
        }

        public boolean isValidSpawn(BlockGetter pLevel, BlockPos pPos, EntityType<?> pType)
        {
            return this.getBlock().properties.isValidSpawn.test(this.asState(), pLevel, pPos, pType);
        }

        public boolean propagatesSkylightDown(BlockGetter pLevel, BlockPos pPos)
        {
            return this.cache != null ? this.cache.propagatesSkylightDown : this.getBlock().propagatesSkylightDown(this.asState(), pLevel, pPos);
        }

        public int getLightBlock(BlockGetter pLevel, BlockPos pPos)
        {
            return this.cache != null ? this.cache.lightBlock : this.getBlock().getLightBlock(this.asState(), pLevel, pPos);
        }

        public VoxelShape getFaceOcclusionShape(BlockGetter pLevel, BlockPos pPos, Direction pDirection)
        {
            return this.cache != null && this.cache.occlusionShapes != null ? this.cache.occlusionShapes[pDirection.ordinal()] : Shapes.getFaceShape(this.getOcclusionShape(pLevel, pPos), pDirection);
        }

        public VoxelShape getOcclusionShape(BlockGetter pReader, BlockPos pPos)
        {
            return this.getBlock().getOcclusionShape(this.asState(), pReader, pPos);
        }

        public boolean hasLargeCollisionShape()
        {
            return this.cache == null || this.cache.largeCollisionShape;
        }

        public boolean useShapeForLightOcclusion()
        {
            return this.useShapeForLightOcclusion;
        }

        public int getLightEmission()
        {
            return this.lightEmission;
        }

        public boolean isAir()
        {
            return this.isAir;
        }

        public MaterialColor getMapColor(BlockGetter pLevel, BlockPos pPos)
        {
            return this.materialColor;
        }

        public BlockState rotate(Rotation pRot)
        {
            return this.getBlock().rotate(this.asState(), pRot);
        }

        public BlockState mirror(Mirror pMirror)
        {
            return this.getBlock().mirror(this.asState(), pMirror);
        }

        public RenderShape getRenderShape()
        {
            return this.getBlock().getRenderShape(this.asState());
        }

        public boolean emissiveRendering(BlockGetter pReader, BlockPos pPos)
        {
            return this.emissiveRendering.test(this.asState(), pReader, pPos);
        }

        public float getShadeBrightness(BlockGetter pReader, BlockPos pPos)
        {
            return this.getBlock().getShadeBrightness(this.asState(), pReader, pPos);
        }

        public boolean isRedstoneConductor(BlockGetter pReader, BlockPos pPos)
        {
            return this.isRedstoneConductor.test(this.asState(), pReader, pPos);
        }

        public boolean isSignalSource()
        {
            return this.getBlock().isSignalSource(this.asState());
        }

        public int getSignal(BlockGetter pBlockAccess, BlockPos pPos, Direction pSide)
        {
            return this.getBlock().getSignal(this.asState(), pBlockAccess, pPos, pSide);
        }

        public boolean hasAnalogOutputSignal()
        {
            return this.getBlock().hasAnalogOutputSignal(this.asState());
        }

        public int getAnalogOutputSignal(Level pLevel, BlockPos pPos)
        {
            return this.getBlock().getAnalogOutputSignal(this.asState(), pLevel, pPos);
        }

        public float getDestroySpeed(BlockGetter pLevel, BlockPos pPos)
        {
            return this.destroySpeed;
        }

        public float getDestroyProgress(Player pPlayer, BlockGetter pLevel, BlockPos pPos)
        {
            return this.getBlock().getDestroyProgress(this.asState(), pPlayer, pLevel, pPos);
        }

        public int getDirectSignal(BlockGetter pBlockAccess, BlockPos pPos, Direction pSide)
        {
            return this.getBlock().getDirectSignal(this.asState(), pBlockAccess, pPos, pSide);
        }

        public PushReaction getPistonPushReaction()
        {
            return this.getBlock().getPistonPushReaction(this.asState());
        }

        public boolean isSolidRender(BlockGetter pLevel, BlockPos pPos)
        {
            if (this.cache != null)
            {
                return this.cache.solidRender;
            }
            else
            {
                BlockState blockstate = this.asState();
                return blockstate.canOcclude() ? Block.isShapeFullBlock(blockstate.getOcclusionShape(pLevel, pPos)) : false;
            }
        }

        public boolean canOcclude()
        {
            return this.canOcclude;
        }

        public boolean skipRendering(BlockState pState, Direction pFace)
        {
            return this.getBlock().skipRendering(this.asState(), pState, pFace);
        }

        public VoxelShape getShape(BlockGetter pLevel, BlockPos pPos)
        {
            return this.getShape(pLevel, pPos, CollisionContext.empty());
        }

        public VoxelShape getShape(BlockGetter pLevel, BlockPos pPos, CollisionContext p_60654_)
        {
            return this.getBlock().getShape(this.asState(), pLevel, pPos, p_60654_);
        }

        public VoxelShape getCollisionShape(BlockGetter pLevel, BlockPos pPos)
        {
            return this.cache != null ? this.cache.collisionShape : this.getCollisionShape(pLevel, pPos, CollisionContext.empty());
        }

        public VoxelShape getCollisionShape(BlockGetter pLevel, BlockPos pPos, CollisionContext p_60745_)
        {
            return this.getBlock().getCollisionShape(this.asState(), pLevel, pPos, p_60745_);
        }

        public VoxelShape getBlockSupportShape(BlockGetter pLevel, BlockPos pPos)
        {
            return this.getBlock().getBlockSupportShape(this.asState(), pLevel, pPos);
        }

        public VoxelShape getVisualShape(BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
        {
            return this.getBlock().getVisualShape(this.asState(), pLevel, pPos, pContext);
        }

        public VoxelShape getInteractionShape(BlockGetter pReader, BlockPos pPos)
        {
            return this.getBlock().getInteractionShape(this.asState(), pReader, pPos);
        }

        public final boolean entityCanStandOn(BlockGetter pReader, BlockPos pPos, Entity pEntity)
        {
            return this.entityCanStandOnFace(pReader, pPos, pEntity, Direction.UP);
        }

        public final boolean entityCanStandOnFace(BlockGetter pReader, BlockPos pPos, Entity pEntity, Direction pDirection)
        {
            return Block.isFaceFull(this.getCollisionShape(pReader, pPos, CollisionContext.of(pEntity)), pDirection);
        }

        public Vec3 getOffset(BlockGetter pAccess, BlockPos pPos)
        {
            Block block = this.getBlock();
            BlockBehaviour.OffsetType blockbehaviour$offsettype = block.getOffsetType();

            if (blockbehaviour$offsettype == BlockBehaviour.OffsetType.NONE)
            {
                return Vec3.ZERO;
            }
            else
            {
                long i = Mth.getSeed(pPos.getX(), 0, pPos.getZ());
                float f = block.getMaxHorizontalOffset();
                double d0 = Mth.clamp(((double)((float)(i & 15L) / 15.0F) - 0.5D) * 0.5D, (double)(-f), (double)f);
                double d1 = blockbehaviour$offsettype == BlockBehaviour.OffsetType.XYZ ? ((double)((float)(i >> 4 & 15L) / 15.0F) - 1.0D) * (double)block.getMaxVerticalOffset() : 0.0D;
                double d2 = Mth.clamp(((double)((float)(i >> 8 & 15L) / 15.0F) - 0.5D) * 0.5D, (double)(-f), (double)f);
                return new Vec3(d0, d1, d2);
            }
        }

        public boolean triggerEvent(Level pLevel, BlockPos pPos, int pId, int pParam)
        {
            return this.getBlock().triggerEvent(this.asState(), pLevel, pPos, pId, pParam);
        }

        public void neighborChanged(Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving)
        {
            this.getBlock().neighborChanged(this.asState(), pLevel, pPos, pBlock, pFromPos, pIsMoving);
        }

        public final void updateNeighbourShapes(LevelAccessor pLevel, BlockPos pPos, int pFlag)
        {
            this.updateNeighbourShapes(pLevel, pPos, pFlag, 512);
        }

        public final void updateNeighbourShapes(LevelAccessor pLevel, BlockPos pPos, int pFlag, int p_60709_)
        {
            this.getBlock();
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (Direction direction : BlockBehaviour.UPDATE_SHAPE_ORDER)
            {
                blockpos$mutableblockpos.setWithOffset(pPos, direction);
                BlockState blockstate = pLevel.getBlockState(blockpos$mutableblockpos);
                BlockState blockstate1 = blockstate.updateShape(direction.getOpposite(), this.asState(), pLevel, blockpos$mutableblockpos, pPos);
                Block.updateOrDestroy(blockstate, blockstate1, pLevel, blockpos$mutableblockpos, pFlag, p_60709_);
            }
        }

        public final void updateIndirectNeighbourShapes(LevelAccessor pLevel, BlockPos pPos, int pFlags)
        {
            this.updateIndirectNeighbourShapes(pLevel, pPos, pFlags, 512);
        }

        public void updateIndirectNeighbourShapes(LevelAccessor pLevel, BlockPos pPos, int pFlags, int p_60766_)
        {
            this.getBlock().updateIndirectNeighbourShapes(this.asState(), pLevel, pPos, pFlags, p_60766_);
        }

        public void onPlace(Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving)
        {
            this.getBlock().onPlace(this.asState(), pLevel, pPos, pOldState, pIsMoving);
        }

        public void onRemove(Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving)
        {
            this.getBlock().onRemove(this.asState(), pLevel, pPos, pNewState, pIsMoving);
        }

        public void tick(ServerLevel pLevel, BlockPos pPos, Random pRandom)
        {
            this.getBlock().tick(this.asState(), pLevel, pPos, pRandom);
        }

        public void randomTick(ServerLevel pLevel, BlockPos pPos, Random pRandom)
        {
            this.getBlock().randomTick(this.asState(), pLevel, pPos, pRandom);
        }

        public void entityInside(Level pLevel, BlockPos pPos, Entity pEntity)
        {
            this.getBlock().entityInside(this.asState(), pLevel, pPos, pEntity);
        }

        public void spawnAfterBreak(ServerLevel pLevel, BlockPos pPos, ItemStack pStack)
        {
            this.getBlock().spawnAfterBreak(this.asState(), pLevel, pPos, pStack);
        }

        public List<ItemStack> getDrops(LootContext.Builder pBuilder)
        {
            return this.getBlock().getDrops(this.asState(), pBuilder);
        }

        public InteractionResult use(Level pLevel, Player pPlayer, InteractionHand pHand, BlockHitResult pResult)
        {
            return this.getBlock().use(this.asState(), pLevel, pResult.getBlockPos(), pPlayer, pHand, pResult);
        }

        public void attack(Level pLevel, BlockPos pPos, Player pPlayer)
        {
            this.getBlock().attack(this.asState(), pLevel, pPos, pPlayer);
        }

        public boolean isSuffocating(BlockGetter pBlockReader, BlockPos pBlockPos)
        {
            return this.isSuffocating.test(this.asState(), pBlockReader, pBlockPos);
        }

        public boolean isViewBlocking(BlockGetter pLevel, BlockPos pPos)
        {
            return this.isViewBlocking.test(this.asState(), pLevel, pPos);
        }

        public BlockState updateShape(Direction pFace, BlockState pQueried, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pOffsetPos)
        {
            return this.getBlock().updateShape(this.asState(), pFace, pQueried, pLevel, pCurrentPos, pOffsetPos);
        }

        public boolean isPathfindable(BlockGetter pLevel, BlockPos pPos, PathComputationType pType)
        {
            return this.getBlock().isPathfindable(this.asState(), pLevel, pPos, pType);
        }

        public boolean canBeReplaced(BlockPlaceContext pUseContext)
        {
            return this.getBlock().canBeReplaced(this.asState(), pUseContext);
        }

        public boolean canBeReplaced(Fluid pUseContext)
        {
            return this.getBlock().canBeReplaced(this.asState(), pUseContext);
        }

        public boolean canSurvive(LevelReader pLevel, BlockPos pPos)
        {
            return this.getBlock().canSurvive(this.asState(), pLevel, pPos);
        }

        public boolean hasPostProcess(BlockGetter pLevel, BlockPos pPos)
        {
            return this.hasPostProcess.test(this.asState(), pLevel, pPos);
        }

        @Nullable
        public MenuProvider getMenuProvider(Level pLevel, BlockPos pPos)
        {
            return this.getBlock().getMenuProvider(this.asState(), pLevel, pPos);
        }

        public boolean is(Tag<Block> pTag)
        {
            return pTag.contains(this.getBlock());
        }

        public boolean is(Tag<Block> pTag, Predicate<BlockBehaviour.BlockStateBase> p_60624_)
        {
            return this.is(pTag) && p_60624_.test(this);
        }

        public boolean hasBlockEntity()
        {
            return this.getBlock() instanceof EntityBlock;
        }

        @Nullable
        public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_155945_, BlockEntityType<T> p_155946_)
        {
            return this.getBlock() instanceof EntityBlock ? ((EntityBlock)this.getBlock()).getTicker(p_155945_, this.asState(), p_155946_) : null;
        }

        public boolean is(Block pTag)
        {
            return this.getBlock() == pTag;
        }

        public FluidState getFluidState()
        {
            return this.getBlock().getFluidState(this.asState());
        }

        public boolean isRandomlyTicking()
        {
            return this.getBlock().isRandomlyTicking(this.asState());
        }

        public long getSeed(BlockPos pPos)
        {
            return this.getBlock().getSeed(this.asState(), pPos);
        }

        public SoundType getSoundType()
        {
            return this.getBlock().getSoundType(this.asState());
        }

        public void onProjectileHit(Level pLevel, BlockState pState, BlockHitResult pHit, Projectile pProjectile)
        {
            this.getBlock().onProjectileHit(pLevel, pState, pHit, pProjectile);
        }

        public boolean isFaceSturdy(BlockGetter pBlockReader, BlockPos pBlockPos, Direction pDirection)
        {
            return this.isFaceSturdy(pBlockReader, pBlockPos, pDirection, SupportType.FULL);
        }

        public boolean isFaceSturdy(BlockGetter pBlockReader, BlockPos pBlockPos, Direction pDirection, SupportType p_60663_)
        {
            return this.cache != null ? this.cache.isFaceSturdy(pDirection, p_60663_) : p_60663_.isSupporting(this.asState(), pBlockReader, pBlockPos, pDirection);
        }

        public boolean isCollisionShapeFullBlock(BlockGetter pReader, BlockPos pPos)
        {
            return this.cache != null ? this.cache.isCollisionShapeFullBlock : this.getBlock().isCollisionShapeFullBlock(this.asState(), pReader, pPos);
        }

        protected abstract BlockState asState();

        public boolean requiresCorrectToolForDrops()
        {
            return this.requiresCorrectToolForDrops;
        }

        static final class Cache
        {
            private static final Direction[] DIRECTIONS = Direction.values();
            private static final int SUPPORT_TYPE_COUNT = SupportType.values().length;
            protected final boolean solidRender;
            final boolean propagatesSkylightDown;
            final int lightBlock;
            @Nullable
            final VoxelShape[] occlusionShapes;
            protected final VoxelShape collisionShape;
            protected final boolean largeCollisionShape;
            private final boolean[] faceSturdy;
            protected final boolean isCollisionShapeFullBlock;

            Cache(BlockState p_60853_)
            {
                Block block = p_60853_.getBlock();
                this.solidRender = p_60853_.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
                this.propagatesSkylightDown = block.propagatesSkylightDown(p_60853_, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
                this.lightBlock = block.getLightBlock(p_60853_, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);

                if (!p_60853_.canOcclude())
                {
                    this.occlusionShapes = null;
                }
                else
                {
                    this.occlusionShapes = new VoxelShape[DIRECTIONS.length];
                    VoxelShape voxelshape = block.getOcclusionShape(p_60853_, EmptyBlockGetter.INSTANCE, BlockPos.ZERO);

                    for (Direction direction : DIRECTIONS)
                    {
                        this.occlusionShapes[direction.ordinal()] = Shapes.getFaceShape(voxelshape, direction);
                    }
                }

                this.collisionShape = block.getCollisionShape(p_60853_, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, CollisionContext.empty());

                if (!this.collisionShape.isEmpty() && block.getOffsetType() != BlockBehaviour.OffsetType.NONE)
                {
                    throw new IllegalStateException(String.format("%s has a collision shape and an offset type, but is not marked as dynamicShape in its properties.", Registry.BLOCK.getKey(block)));
                }
                else
                {
                    this.largeCollisionShape = Arrays.stream(Direction.Axis.values()).anyMatch((p_60860_) ->
                    {
                        return this.collisionShape.min(p_60860_) < 0.0D || this.collisionShape.max(p_60860_) > 1.0D;
                    });
                    this.faceSturdy = new boolean[DIRECTIONS.length * SUPPORT_TYPE_COUNT];

                    for (Direction direction1 : DIRECTIONS)
                    {
                        for (SupportType supporttype : SupportType.values())
                        {
                            this.faceSturdy[getFaceSupportIndex(direction1, supporttype)] = supporttype.isSupporting(p_60853_, EmptyBlockGetter.INSTANCE, BlockPos.ZERO, direction1);
                        }
                    }

                    this.isCollisionShapeFullBlock = Block.isShapeFullBlock(p_60853_.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
                }
            }

            public boolean isFaceSturdy(Direction pDirection, SupportType pBlockVoxelShape)
            {
                return this.faceSturdy[getFaceSupportIndex(pDirection, pBlockVoxelShape)];
            }

            private static int getFaceSupportIndex(Direction pDirection, SupportType pBlockVoxelShape)
            {
                return pDirection.ordinal() * SUPPORT_TYPE_COUNT + pBlockVoxelShape.ordinal();
            }
        }
    }

    public static enum OffsetType
    {
        NONE,
        XZ,
        XYZ;
    }

    public static class Properties
    {
        Material material;
        Function<BlockState, MaterialColor> materialColor;
        boolean hasCollision = true;
        SoundType soundType = SoundType.STONE;
        ToIntFunction<BlockState> lightEmission = (p_60929_) ->
        {
            return 0;
        };
        float explosionResistance;
        float destroyTime;
        boolean requiresCorrectToolForDrops;
        boolean isRandomlyTicking;
        float friction = 0.6F;
        float speedFactor = 1.0F;
        float jumpFactor = 1.0F;
        ResourceLocation drops;
        boolean canOcclude = true;
        boolean isAir;
        BlockBehaviour.StateArgumentPredicate < EntityType<? >> isValidSpawn = (p_60935_, p_60936_, p_60937_, p_60938_) ->
        {
            return p_60935_.isFaceSturdy(p_60936_, p_60937_, Direction.UP) && p_60935_.getLightEmission() < 14;
        };
        BlockBehaviour.StatePredicate isRedstoneConductor = (p_60985_, p_60986_, p_60987_) ->
        {
            return p_60985_.getMaterial().isSolidBlocking() && p_60985_.isCollisionShapeFullBlock(p_60986_, p_60987_);
        };
        BlockBehaviour.StatePredicate isSuffocating = (p_60974_, p_60975_, p_60976_) ->
        {
            return this.material.blocksMotion() && p_60974_.isCollisionShapeFullBlock(p_60975_, p_60976_);
        };
        BlockBehaviour.StatePredicate isViewBlocking = this.isSuffocating;
        BlockBehaviour.StatePredicate hasPostProcess = (p_60963_, p_60964_, p_60965_) ->
        {
            return false;
        };
        BlockBehaviour.StatePredicate emissiveRendering = (p_60931_, p_60932_, p_60933_) ->
        {
            return false;
        };
        boolean dynamicShape;

        private Properties(Material p_60905_, MaterialColor p_60906_)
        {
            this(p_60905_, (p_60952_) ->
            {
                return p_60906_;
            });
        }

        private Properties(Material p_60908_, Function<BlockState, MaterialColor> p_60909_)
        {
            this.material = p_60908_;
            this.materialColor = p_60909_;
        }

        public static BlockBehaviour.Properties of(Material pMaterial)
        {
            return of(pMaterial, pMaterial.getColor());
        }

        public static BlockBehaviour.Properties of(Material pMaterial, DyeColor p_60943_)
        {
            return of(pMaterial, p_60943_.getMaterialColor());
        }

        public static BlockBehaviour.Properties of(Material pMaterial, MaterialColor p_60946_)
        {
            return new BlockBehaviour.Properties(pMaterial, p_60946_);
        }

        public static BlockBehaviour.Properties of(Material pMaterial, Function<BlockState, MaterialColor> p_60949_)
        {
            return new BlockBehaviour.Properties(pMaterial, p_60949_);
        }

        public static BlockBehaviour.Properties copy(BlockBehaviour pBlock)
        {
            BlockBehaviour.Properties blockbehaviour$properties = new BlockBehaviour.Properties(pBlock.material, pBlock.properties.materialColor);
            blockbehaviour$properties.material = pBlock.properties.material;
            blockbehaviour$properties.destroyTime = pBlock.properties.destroyTime;
            blockbehaviour$properties.explosionResistance = pBlock.properties.explosionResistance;
            blockbehaviour$properties.hasCollision = pBlock.properties.hasCollision;
            blockbehaviour$properties.isRandomlyTicking = pBlock.properties.isRandomlyTicking;
            blockbehaviour$properties.lightEmission = pBlock.properties.lightEmission;
            blockbehaviour$properties.materialColor = pBlock.properties.materialColor;
            blockbehaviour$properties.soundType = pBlock.properties.soundType;
            blockbehaviour$properties.friction = pBlock.properties.friction;
            blockbehaviour$properties.speedFactor = pBlock.properties.speedFactor;
            blockbehaviour$properties.dynamicShape = pBlock.properties.dynamicShape;
            blockbehaviour$properties.canOcclude = pBlock.properties.canOcclude;
            blockbehaviour$properties.isAir = pBlock.properties.isAir;
            blockbehaviour$properties.requiresCorrectToolForDrops = pBlock.properties.requiresCorrectToolForDrops;
            return blockbehaviour$properties;
        }

        public BlockBehaviour.Properties noCollission()
        {
            this.hasCollision = false;
            this.canOcclude = false;
            return this;
        }

        public BlockBehaviour.Properties noOcclusion()
        {
            this.canOcclude = false;
            return this;
        }

        public BlockBehaviour.Properties friction(float pSlipperiness)
        {
            this.friction = pSlipperiness;
            return this;
        }

        public BlockBehaviour.Properties speedFactor(float pFactor)
        {
            this.speedFactor = pFactor;
            return this;
        }

        public BlockBehaviour.Properties jumpFactor(float pFactor)
        {
            this.jumpFactor = pFactor;
            return this;
        }

        public BlockBehaviour.Properties sound(SoundType pSoundType)
        {
            this.soundType = pSoundType;
            return this;
        }

        public BlockBehaviour.Properties lightLevel(ToIntFunction<BlockState> pStateLightFunction)
        {
            this.lightEmission = pStateLightFunction;
            return this;
        }

        public BlockBehaviour.Properties strength(float pHardnessAndResistance, float p_60915_)
        {
            return this.destroyTime(pHardnessAndResistance).explosionResistance(p_60915_);
        }

        public BlockBehaviour.Properties instabreak()
        {
            return this.strength(0.0F);
        }

        public BlockBehaviour.Properties strength(float pHardnessAndResistance)
        {
            this.strength(pHardnessAndResistance, pHardnessAndResistance);
            return this;
        }

        public BlockBehaviour.Properties randomTicks()
        {
            this.isRandomlyTicking = true;
            return this;
        }

        public BlockBehaviour.Properties dynamicShape()
        {
            this.dynamicShape = true;
            return this;
        }

        public BlockBehaviour.Properties noDrops()
        {
            this.drops = BuiltInLootTables.EMPTY;
            return this;
        }

        public BlockBehaviour.Properties dropsLike(Block pBlock)
        {
            this.drops = pBlock.getLootTable();
            return this;
        }

        public BlockBehaviour.Properties air()
        {
            this.isAir = true;
            return this;
        }

        public BlockBehaviour.Properties isValidSpawn(BlockBehaviour.StateArgumentPredicate < EntityType<? >> pSpawnPredicate)
        {
            this.isValidSpawn = pSpawnPredicate;
            return this;
        }

        public BlockBehaviour.Properties isRedstoneConductor(BlockBehaviour.StatePredicate pOpaquePredicate)
        {
            this.isRedstoneConductor = pOpaquePredicate;
            return this;
        }

        public BlockBehaviour.Properties isSuffocating(BlockBehaviour.StatePredicate pSuffocatesPredicate)
        {
            this.isSuffocating = pSuffocatesPredicate;
            return this;
        }

        public BlockBehaviour.Properties isViewBlocking(BlockBehaviour.StatePredicate pBlocksVisionPredicate)
        {
            this.isViewBlocking = pBlocksVisionPredicate;
            return this;
        }

        public BlockBehaviour.Properties hasPostProcess(BlockBehaviour.StatePredicate pPostProcessingPredicate)
        {
            this.hasPostProcess = pPostProcessingPredicate;
            return this;
        }

        public BlockBehaviour.Properties emissiveRendering(BlockBehaviour.StatePredicate pEmmisiveRenderPredicate)
        {
            this.emissiveRendering = pEmmisiveRenderPredicate;
            return this;
        }

        public BlockBehaviour.Properties requiresCorrectToolForDrops()
        {
            this.requiresCorrectToolForDrops = true;
            return this;
        }

        public BlockBehaviour.Properties color(MaterialColor p_155950_)
        {
            this.materialColor = (p_155953_) ->
            {
                return p_155950_;
            };
            return this;
        }

        public BlockBehaviour.Properties destroyTime(float p_155955_)
        {
            this.destroyTime = p_155955_;
            return this;
        }

        public BlockBehaviour.Properties explosionResistance(float p_155957_)
        {
            this.explosionResistance = Math.max(0.0F, p_155957_);
            return this;
        }
    }

    public interface StateArgumentPredicate<A>
    {
        boolean test(BlockState p_61031_, BlockGetter p_61032_, BlockPos p_61033_, A p_61034_);
    }

    public interface StatePredicate
    {
        boolean test(BlockState p_61036_, BlockGetter p_61037_, BlockPos p_61038_);
    }
}
