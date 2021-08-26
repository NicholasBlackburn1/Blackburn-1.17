package net.minecraft.world.level.block;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.IdMapper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Block extends BlockBehaviour implements ItemLike
{
    protected static final Logger LOGGER = LogManager.getLogger();
    public static final IdMapper<BlockState> BLOCK_STATE_REGISTRY = new IdMapper<>();
    private static final LoadingCache<VoxelShape, Boolean> SHAPE_FULL_BLOCK_CACHE = CacheBuilder.newBuilder().maximumSize(512L).weakKeys().build(new CacheLoader<VoxelShape, Boolean>()
    {
        public Boolean load(VoxelShape p_49972_)
        {
            return !Shapes.joinIsNotEmpty(Shapes.block(), p_49972_, BooleanOp.NOT_SAME);
        }
    });
    public static final int UPDATE_NEIGHBORS = 1;
    public static final int UPDATE_CLIENTS = 2;
    public static final int UPDATE_INVISIBLE = 4;
    public static final int UPDATE_IMMEDIATE = 8;
    public static final int UPDATE_KNOWN_SHAPE = 16;
    public static final int UPDATE_SUPPRESS_DROPS = 32;
    public static final int UPDATE_MOVE_BY_PISTON = 64;
    public static final int UPDATE_SUPPRESS_LIGHT = 128;
    public static final int UPDATE_NONE = 4;
    public static final int UPDATE_ALL = 3;
    public static final int UPDATE_ALL_IMMEDIATE = 11;
    public static final float INDESTRUCTIBLE = -1.0F;
    public static final float INSTANT = 0.0F;
    public static final int UPDATE_LIMIT = 512;
    protected final StateDefinition<Block, BlockState> stateDefinition;
    private BlockState defaultBlockState;
    @Nullable
    private String descriptionId;
    @Nullable
    private Item item;
    private static final int CACHE_SIZE = 2048;
    private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() ->
    {
        Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2bytelinkedopenhashmap = new Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>(2048, 0.25F)
        {
            protected void rehash(int p_49979_)
            {
            }
        };
        object2bytelinkedopenhashmap.defaultReturnValue((byte)127);
        return object2bytelinkedopenhashmap;
    });

    public static int getId(@Nullable BlockState pState)
    {
        if (pState == null)
        {
            return 0;
        }
        else
        {
            int i = BLOCK_STATE_REGISTRY.getId(pState);
            return i == -1 ? 0 : i;
        }
    }

    public static BlockState stateById(int pId)
    {
        BlockState blockstate = BLOCK_STATE_REGISTRY.byId(pId);
        return blockstate == null ? Blocks.AIR.defaultBlockState() : blockstate;
    }

    public static Block byItem(@Nullable Item pItem)
    {
        return pItem instanceof BlockItem ? ((BlockItem)pItem).getBlock() : Blocks.AIR;
    }

    public static BlockState pushEntitiesUp(BlockState pOldState, BlockState pNewState, Level pLevel, BlockPos pPos)
    {
        VoxelShape voxelshape = Shapes.joinUnoptimized(pOldState.getCollisionShape(pLevel, pPos), pNewState.getCollisionShape(pLevel, pPos), BooleanOp.ONLY_SECOND).move((double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ());

        if (voxelshape.isEmpty())
        {
            return pNewState;
        }
        else
        {
            for (Entity entity : pLevel.getEntities((Entity)null, voxelshape.bounds()))
            {
                double d0 = Shapes.collide(Direction.Axis.Y, entity.getBoundingBox().move(0.0D, 1.0D, 0.0D), Stream.of(voxelshape), -1.0D);
                entity.teleportTo(entity.getX(), entity.getY() + 1.0D + d0, entity.getZ());
            }

            return pNewState;
        }
    }

    public static VoxelShape box(double pX1, double p_49798_, double pY1, double p_49800_, double pZ1, double p_49802_)
    {
        return Shapes.box(pX1 / 16.0D, p_49798_ / 16.0D, pY1 / 16.0D, p_49800_ / 16.0D, pZ1 / 16.0D, p_49802_ / 16.0D);
    }

    public static BlockState updateFromNeighbourShapes(BlockState pCurrentState, LevelAccessor pLevel, BlockPos pPos)
    {
        BlockState blockstate = pCurrentState;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (Direction direction : UPDATE_SHAPE_ORDER)
        {
            blockpos$mutableblockpos.setWithOffset(pPos, direction);
            blockstate = blockstate.updateShape(direction, pLevel.getBlockState(blockpos$mutableblockpos), pLevel, pPos, blockpos$mutableblockpos);
        }

        return blockstate;
    }

    public static void updateOrDestroy(BlockState pOldState, BlockState pNewState, LevelAccessor pLevel, BlockPos pPos, int pFlags)
    {
        updateOrDestroy(pOldState, pNewState, pLevel, pPos, pFlags, 512);
    }

    public static void updateOrDestroy(BlockState pOldState, BlockState pNewState, LevelAccessor pLevel, BlockPos pPos, int pFlags, int p_49914_)
    {
        if (pNewState != pOldState)
        {
            if (pNewState.isAir())
            {
                if (!pLevel.isClientSide())
                {
                    pLevel.destroyBlock(pPos, (pFlags & 32) == 0, (Entity)null, p_49914_);
                }
            }
            else
            {
                pLevel.setBlock(pPos, pNewState, pFlags & -33, p_49914_);
            }
        }
    }

    public Block(BlockBehaviour.Properties p_49795_)
    {
        super(p_49795_);
        StateDefinition.Builder<Block, BlockState> builder = new StateDefinition.Builder<>(this);
        this.createBlockStateDefinition(builder);
        this.stateDefinition = builder.create(Block::defaultBlockState, BlockState::new);
        this.registerDefaultState(this.stateDefinition.any());

        if (SharedConstants.IS_RUNNING_IN_IDE)
        {
            String s = this.getClass().getSimpleName();

            if (!s.endsWith("Block"))
            {
                LOGGER.error("Block classes should end with Block and {} doesn't.", (Object)s);
            }
        }
    }

    public static boolean isExceptionForConnection(BlockState p_152464_)
    {
        return p_152464_.getBlock() instanceof LeavesBlock || p_152464_.is(Blocks.BARRIER) || p_152464_.is(Blocks.CARVED_PUMPKIN) || p_152464_.is(Blocks.JACK_O_LANTERN) || p_152464_.is(Blocks.MELON) || p_152464_.is(Blocks.PUMPKIN) || p_152464_.is(BlockTags.SHULKER_BOXES);
    }

    public boolean isRandomlyTicking(BlockState pState)
    {
        return this.isRandomlyTicking;
    }

    public static boolean shouldRenderFace(BlockState p_152445_, BlockGetter p_152446_, BlockPos p_152447_, Direction p_152448_, BlockPos p_152449_)
    {
        BlockState blockstate = p_152446_.getBlockState(p_152449_);

        if (p_152445_.skipRendering(blockstate, p_152448_))
        {
            return false;
        }
        else if (blockstate.canOcclude())
        {
            Block.BlockStatePairKey block$blockstatepairkey = new Block.BlockStatePairKey(p_152445_, blockstate, p_152448_);
            Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2bytelinkedopenhashmap = OCCLUSION_CACHE.get();
            byte b0 = object2bytelinkedopenhashmap.getAndMoveToFirst(block$blockstatepairkey);

            if (b0 != 127)
            {
                return b0 != 0;
            }
            else
            {
                VoxelShape voxelshape = p_152445_.getFaceOcclusionShape(p_152446_, p_152447_, p_152448_);

                if (voxelshape.isEmpty())
                {
                    return true;
                }
                else
                {
                    VoxelShape voxelshape1 = blockstate.getFaceOcclusionShape(p_152446_, p_152449_, p_152448_.getOpposite());
                    boolean flag = Shapes.joinIsNotEmpty(voxelshape, voxelshape1, BooleanOp.ONLY_FIRST);

                    if (object2bytelinkedopenhashmap.size() == 2048)
                    {
                        object2bytelinkedopenhashmap.removeLastByte();
                    }

                    object2bytelinkedopenhashmap.putAndMoveToFirst(block$blockstatepairkey, (byte)(flag ? 1 : 0));
                    return flag;
                }
            }
        }
        else
        {
            return true;
        }
    }

    public static boolean canSupportRigidBlock(BlockGetter pLevel, BlockPos pPos)
    {
        return pLevel.getBlockState(pPos).isFaceSturdy(pLevel, pPos, Direction.UP, SupportType.RIGID);
    }

    public static boolean canSupportCenter(LevelReader pLevel, BlockPos pPos, Direction pDirection)
    {
        BlockState blockstate = pLevel.getBlockState(pPos);
        return pDirection == Direction.DOWN && blockstate.is(BlockTags.UNSTABLE_BOTTOM_CENTER) ? false : blockstate.isFaceSturdy(pLevel, pPos, pDirection, SupportType.CENTER);
    }

    public static boolean isFaceFull(VoxelShape pShape, Direction pSide)
    {
        VoxelShape voxelshape = pShape.getFaceShape(pSide);
        return isShapeFullBlock(voxelshape);
    }

    public static boolean isShapeFullBlock(VoxelShape pShape)
    {
        return SHAPE_FULL_BLOCK_CACHE.getUnchecked(pShape);
    }

    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pReader, BlockPos pPos)
    {
        return !isShapeFullBlock(pState.getShape(pReader, pPos)) && pState.getFluidState().isEmpty();
    }

    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRand)
    {
    }

    public void destroy(LevelAccessor pLevel, BlockPos pPos, BlockState pState)
    {
    }

    public static List<ItemStack> getDrops(BlockState pState, ServerLevel pLevel, BlockPos pPos, @Nullable BlockEntity pBlockEntity)
    {
        LootContext.Builder lootcontext$builder = (new LootContext.Builder(pLevel)).withRandom(pLevel.random).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pPos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, pBlockEntity);
        return pState.getDrops(lootcontext$builder);
    }

    public static List<ItemStack> getDrops(BlockState pState, ServerLevel pLevel, BlockPos pPos, @Nullable BlockEntity pBlockEntity, @Nullable Entity p_49879_, ItemStack p_49880_)
    {
        LootContext.Builder lootcontext$builder = (new LootContext.Builder(pLevel)).withRandom(pLevel.random).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pPos)).withParameter(LootContextParams.TOOL, p_49880_).withOptionalParameter(LootContextParams.THIS_ENTITY, p_49879_).withOptionalParameter(LootContextParams.BLOCK_ENTITY, pBlockEntity);
        return pState.getDrops(lootcontext$builder);
    }

    public static void dropResources(BlockState pState, LootContext.Builder pLevel)
    {
        ServerLevel serverlevel = pLevel.getLevel();
        BlockPos blockpos = new BlockPos(pLevel.getParameter(LootContextParams.ORIGIN));
        pState.getDrops(pLevel).forEach((p_152406_) ->
        {
            popResource(serverlevel, blockpos, p_152406_);
        });
        pState.spawnAfterBreak(serverlevel, blockpos, ItemStack.EMPTY);
    }

    public static void dropResources(BlockState pState, Level pLevel, BlockPos pPos)
    {
        if (pLevel instanceof ServerLevel)
        {
            getDrops(pState, (ServerLevel)pLevel, pPos, (BlockEntity)null).forEach((p_49944_) ->
            {
                popResource(pLevel, pPos, p_49944_);
            });
            pState.spawnAfterBreak((ServerLevel)pLevel, pPos, ItemStack.EMPTY);
        }
    }

    public static void dropResources(BlockState pState, LevelAccessor pLevel, BlockPos pPos, @Nullable BlockEntity p_49896_)
    {
        if (pLevel instanceof ServerLevel)
        {
            getDrops(pState, (ServerLevel)pLevel, pPos, p_49896_).forEach((p_49859_) ->
            {
                popResource((ServerLevel)pLevel, pPos, p_49859_);
            });
            pState.spawnAfterBreak((ServerLevel)pLevel, pPos, ItemStack.EMPTY);
        }
    }

    public static void dropResources(BlockState pState, Level pLevel, BlockPos pPos, @Nullable BlockEntity p_49885_, Entity p_49886_, ItemStack p_49887_)
    {
        if (pLevel instanceof ServerLevel)
        {
            getDrops(pState, (ServerLevel)pLevel, pPos, p_49885_, p_49886_, p_49887_).forEach((p_49925_) ->
            {
                popResource(pLevel, pPos, p_49925_);
            });
            pState.spawnAfterBreak((ServerLevel)pLevel, pPos, p_49887_);
        }
    }

    public static void popResource(Level pLevel, BlockPos pPos, ItemStack pStack)
    {
        float f = EntityType.ITEM.getHeight() / 2.0F;
        double d0 = (double)((float)pPos.getX() + 0.5F) + Mth.nextDouble(pLevel.random, -0.25D, 0.25D);
        double d1 = (double)((float)pPos.getY() + 0.5F) + Mth.nextDouble(pLevel.random, -0.25D, 0.25D) - (double)f;
        double d2 = (double)((float)pPos.getZ() + 0.5F) + Mth.nextDouble(pLevel.random, -0.25D, 0.25D);
        popResource(pLevel, () ->
        {
            return new ItemEntity(pLevel, d0, d1, d2, pStack);
        }, pStack);
    }

    public static void popResourceFromFace(Level p_152436_, BlockPos p_152437_, Direction p_152438_, ItemStack p_152439_)
    {
        int i = p_152438_.getStepX();
        int j = p_152438_.getStepY();
        int k = p_152438_.getStepZ();
        float f = EntityType.ITEM.getWidth() / 2.0F;
        float f1 = EntityType.ITEM.getHeight() / 2.0F;
        double d0 = (double)((float)p_152437_.getX() + 0.5F) + (i == 0 ? Mth.nextDouble(p_152436_.random, -0.25D, 0.25D) : (double)((float)i * (0.5F + f)));
        double d1 = (double)((float)p_152437_.getY() + 0.5F) + (j == 0 ? Mth.nextDouble(p_152436_.random, -0.25D, 0.25D) : (double)((float)j * (0.5F + f1))) - (double)f1;
        double d2 = (double)((float)p_152437_.getZ() + 0.5F) + (k == 0 ? Mth.nextDouble(p_152436_.random, -0.25D, 0.25D) : (double)((float)k * (0.5F + f)));
        double d3 = i == 0 ? Mth.nextDouble(p_152436_.random, -0.1D, 0.1D) : (double)i * 0.1D;
        double d4 = j == 0 ? Mth.nextDouble(p_152436_.random, 0.0D, 0.1D) : (double)j * 0.1D + 0.1D;
        double d5 = k == 0 ? Mth.nextDouble(p_152436_.random, -0.1D, 0.1D) : (double)k * 0.1D;
        popResource(p_152436_, () ->
        {
            return new ItemEntity(p_152436_, d0, d1, d2, p_152439_, d3, d4, d5);
        }, p_152439_);
    }

    private static void popResource(Level pLevel, Supplier<ItemEntity> pPos, ItemStack pStack)
    {
        if (!pLevel.isClientSide && !pStack.isEmpty() && pLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS))
        {
            ItemEntity itementity = pPos.get();
            itementity.setDefaultPickUpDelay();
            pLevel.addFreshEntity(itementity);
        }
    }

    protected void popExperience(ServerLevel pLevel, BlockPos pPos, int pAmount)
    {
        if (pLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS))
        {
            ExperienceOrb.award(pLevel, Vec3.atCenterOf(pPos), pAmount);
        }
    }

    public float getExplosionResistance()
    {
        return this.explosionResistance;
    }

    public void wasExploded(Level pLevel, BlockPos pPos, Explosion pExplosion)
    {
    }

    public void stepOn(Level p_152431_, BlockPos p_152432_, BlockState p_152433_, Entity p_152434_)
    {
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext)
    {
        return this.defaultBlockState();
    }

    public void playerDestroy(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState, @Nullable BlockEntity pTe, ItemStack pStack)
    {
        pPlayer.awardStat(Stats.BLOCK_MINED.get(this));
        pPlayer.causeFoodExhaustion(0.005F);
        dropResources(pState, pLevel, pPos, pTe, pPlayer, pStack);
    }

    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack)
    {
    }

    public boolean isPossibleToRespawnInThis()
    {
        return !this.material.isSolid() && !this.material.isLiquid();
    }

    public MutableComponent getName()
    {
        return new TranslatableComponent(this.getDescriptionId());
    }

    public String getDescriptionId()
    {
        if (this.descriptionId == null)
        {
            this.descriptionId = Util.makeDescriptionId("block", Registry.BLOCK.getKey(this));
        }

        return this.descriptionId;
    }

    public void fallOn(Level p_152426_, BlockState p_152427_, BlockPos p_152428_, Entity p_152429_, float p_152430_)
    {
        p_152429_.causeFallDamage(p_152430_, 1.0F, DamageSource.FALL);
    }

    public void updateEntityAfterFallOn(BlockGetter pLevel, Entity pEntity)
    {
        pEntity.setDeltaMovement(pEntity.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
    }

    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState)
    {
        return new ItemStack(this);
    }

    public void fillItemCategory(CreativeModeTab pGroup, NonNullList<ItemStack> pItems)
    {
        pItems.add(new ItemStack(this));
    }

    public float getFriction()
    {
        return this.friction;
    }

    public float getSpeedFactor()
    {
        return this.speedFactor;
    }

    public float getJumpFactor()
    {
        return this.jumpFactor;
    }

    protected void spawnDestroyParticles(Level p_152422_, Player p_152423_, BlockPos p_152424_, BlockState p_152425_)
    {
        p_152422_.levelEvent(p_152423_, 2001, p_152424_, getId(p_152425_));
    }

    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer)
    {
        this.spawnDestroyParticles(pLevel, pPlayer, pPos, pState);

        if (pState.is(BlockTags.GUARDED_BY_PIGLINS))
        {
            PiglinAi.angerNearbyPiglins(pPlayer, false);
        }

        pLevel.gameEvent(pPlayer, GameEvent.BLOCK_DESTROY, pPos);
    }

    public void handlePrecipitation(BlockState p_152450_, Level p_152451_, BlockPos p_152452_, Biome.Precipitation p_152453_)
    {
    }

    public boolean dropFromExplosion(Explosion pExplosion)
    {
        return true;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
    }

    public StateDefinition<Block, BlockState> getStateDefinition()
    {
        return this.stateDefinition;
    }

    protected final void registerDefaultState(BlockState pState)
    {
        this.defaultBlockState = pState;
    }

    public final BlockState defaultBlockState()
    {
        return this.defaultBlockState;
    }

    public final BlockState withPropertiesOf(BlockState p_152466_)
    {
        BlockState blockstate = this.defaultBlockState();

        for (Property<?> property : p_152466_.getBlock().getStateDefinition().getProperties())
        {
            if (blockstate.hasProperty(property))
            {
                blockstate = copyProperty(p_152466_, blockstate, property);
            }
        }

        return blockstate;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState p_152455_, BlockState p_152456_, Property<T> p_152457_)
    {
        return p_152456_.setValue(p_152457_, p_152455_.getValue(p_152457_));
    }

    public SoundType getSoundType(BlockState pState)
    {
        return this.soundType;
    }

    public Item asItem()
    {
        if (this.item == null)
        {
            this.item = Item.byBlock(this);
        }

        return this.item;
    }

    public boolean hasDynamicShape()
    {
        return this.dynamicShape;
    }

    public String toString()
    {
        return "Block{" + Registry.BLOCK.getKey(this) + "}";
    }

    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag)
    {
    }

    protected Block asBlock()
    {
        return this;
    }

    protected ImmutableMap<BlockState, VoxelShape> getShapeForEachState(Function<BlockState, VoxelShape> p_152459_)
    {
        return this.stateDefinition.getPossibleStates().stream().collect(ImmutableMap.toImmutableMap(Function.identity(), p_152459_));
    }

    public static final class BlockStatePairKey
    {
        private final BlockState first;
        private final BlockState second;
        private final Direction direction;

        public BlockStatePairKey(BlockState p_49984_, BlockState p_49985_, Direction p_49986_)
        {
            this.first = p_49984_;
            this.second = p_49985_;
            this.direction = p_49986_;
        }

        public boolean equals(Object p_49988_)
        {
            if (this == p_49988_)
            {
                return true;
            }
            else if (!(p_49988_ instanceof Block.BlockStatePairKey))
            {
                return false;
            }
            else
            {
                Block.BlockStatePairKey block$blockstatepairkey = (Block.BlockStatePairKey)p_49988_;
                return this.first == block$blockstatepairkey.first && this.second == block$blockstatepairkey.second && this.direction == block$blockstatepairkey.direction;
            }
        }

        public int hashCode()
        {
            int i = this.first.hashCode();
            i = 31 * i + this.second.hashCode();
            return 31 * i + this.direction.hashCode();
        }
    }
}
