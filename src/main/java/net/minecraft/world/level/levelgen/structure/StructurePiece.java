package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableSet;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.NoiseEffect;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.material.FluidState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class StructurePiece
{
    private static final Logger LOGGER = LogManager.getLogger();
    protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
    protected BoundingBox boundingBox;
    @Nullable
    private Direction orientation;
    private Mirror mirror;
    private Rotation rotation;
    protected int genDepth;
    private final StructurePieceType type;
    private static final Set<Block> SHAPE_CHECK_BLOCKS = ImmutableSet.<Block>builder().add(Blocks.NETHER_BRICK_FENCE).add(Blocks.TORCH).add(Blocks.WALL_TORCH).add(Blocks.OAK_FENCE).add(Blocks.SPRUCE_FENCE).add(Blocks.DARK_OAK_FENCE).add(Blocks.ACACIA_FENCE).add(Blocks.BIRCH_FENCE).add(Blocks.JUNGLE_FENCE).add(Blocks.LADDER).add(Blocks.IRON_BARS).build();

    protected StructurePiece(StructurePieceType p_163538_, int p_163539_, BoundingBox p_163540_)
    {
        this.type = p_163538_;
        this.genDepth = p_163539_;
        this.boundingBox = p_163540_;
    }

    public StructurePiece(StructurePieceType p_73390_, CompoundTag p_73391_)
    {
        this(p_73390_, p_73391_.getInt("GD"), BoundingBox.CODEC.parse(NbtOps.INSTANCE, p_73391_.get("BB")).resultOrPartial(LOGGER::error).orElseThrow(() ->
        {
            return new IllegalArgumentException("Invalid boundingbox");
        }));
        int i = p_73391_.getInt("O");
        this.setOrientation(i == -1 ? null : Direction.from2DDataValue(i));
    }

    protected static BoundingBox makeBoundingBox(int p_163542_, int p_163543_, int p_163544_, Direction p_163545_, int p_163546_, int p_163547_, int p_163548_)
    {
        return p_163545_.getAxis() == Direction.Axis.Z ? new BoundingBox(p_163542_, p_163543_, p_163544_, p_163542_ + p_163546_ - 1, p_163543_ + p_163547_ - 1, p_163544_ + p_163548_ - 1) : new BoundingBox(p_163542_, p_163543_, p_163544_, p_163542_ + p_163548_ - 1, p_163543_ + p_163547_ - 1, p_163544_ + p_163546_ - 1);
    }

    protected static Direction getRandomHorizontalDirection(Random p_163581_)
    {
        return Direction.Plane.HORIZONTAL.getRandomDirection(p_163581_);
    }

    public final CompoundTag createTag(ServerLevel p_163550_)
    {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putString("id", Registry.STRUCTURE_PIECE.getKey(this.getType()).toString());
        BoundingBox.CODEC.encodeStart(NbtOps.INSTANCE, this.boundingBox).resultOrPartial(LOGGER::error).ifPresent((p_163579_) ->
        {
            compoundtag.put("BB", p_163579_);
        });
        Direction direction = this.getOrientation();
        compoundtag.putInt("O", direction == null ? -1 : direction.get2DDataValue());
        compoundtag.putInt("GD", this.genDepth);
        this.addAdditionalSaveData(p_163550_, compoundtag);
        return compoundtag;
    }

    protected abstract void addAdditionalSaveData(ServerLevel p_163551_, CompoundTag p_163552_);

    public NoiseEffect getNoiseEffect()
    {
        return NoiseEffect.BEARD;
    }

    public void addChildren(StructurePiece p_163574_, StructurePieceAccessor p_163575_, Random p_163576_)
    {
    }

    public abstract boolean postProcess(WorldGenLevel p_73427_, StructureFeatureManager p_73428_, ChunkGenerator p_73429_, Random p_73430_, BoundingBox p_73431_, ChunkPos p_73432_, BlockPos p_73433_);

    public BoundingBox getBoundingBox()
    {
        return this.boundingBox;
    }

    public int getGenDepth()
    {
        return this.genDepth;
    }

    public boolean isCloseToChunk(ChunkPos p_73412_, int p_73413_)
    {
        int i = p_73412_.getMinBlockX();
        int j = p_73412_.getMinBlockZ();
        return this.boundingBox.intersects(i - p_73413_, j - p_73413_, i + 15 + p_73413_, j + 15 + p_73413_);
    }

    public BlockPos getLocatorPosition()
    {
        return new BlockPos(this.boundingBox.getCenter());
    }

    protected BlockPos.MutableBlockPos getWorldPos(int p_163583_, int p_163584_, int p_163585_)
    {
        return new BlockPos.MutableBlockPos(this.getWorldX(p_163583_, p_163585_), this.getWorldY(p_163584_), this.getWorldZ(p_163583_, p_163585_));
    }

    protected int getWorldX(int pX, int pZ)
    {
        Direction direction = this.getOrientation();

        if (direction == null)
        {
            return pX;
        }
        else
        {
            switch (direction)
            {
                case NORTH:
                case SOUTH:
                    return this.boundingBox.minX() + pX;

                case WEST:
                    return this.boundingBox.maxX() - pZ;

                case EAST:
                    return this.boundingBox.minX() + pZ;

                default:
                    return pX;
            }
        }
    }

    protected int getWorldY(int pY)
    {
        return this.getOrientation() == null ? pY : pY + this.boundingBox.minY();
    }

    protected int getWorldZ(int pX, int pZ)
    {
        Direction direction = this.getOrientation();

        if (direction == null)
        {
            return pZ;
        }
        else
        {
            switch (direction)
            {
                case NORTH:
                    return this.boundingBox.maxZ() - pZ;

                case SOUTH:
                    return this.boundingBox.minZ() + pZ;

                case WEST:
                case EAST:
                    return this.boundingBox.minZ() + pX;

                default:
                    return pZ;
            }
        }
    }

    protected void placeBlock(WorldGenLevel pLevel, BlockState pBlockstate, int pX, int pY, int pZ, BoundingBox pBoundingbox)
    {
        BlockPos blockpos = this.getWorldPos(pX, pY, pZ);

        if (pBoundingbox.isInside(blockpos))
        {
            if (this.canBeReplaced(pLevel, pX, pY, pZ, pBoundingbox))
            {
                if (this.mirror != Mirror.NONE)
                {
                    pBlockstate = pBlockstate.mirror(this.mirror);
                }

                if (this.rotation != Rotation.NONE)
                {
                    pBlockstate = pBlockstate.rotate(this.rotation);
                }

                pLevel.setBlock(blockpos, pBlockstate, 2);
                FluidState fluidstate = pLevel.getFluidState(blockpos);

                if (!fluidstate.isEmpty())
                {
                    pLevel.getLiquidTicks().scheduleTick(blockpos, fluidstate.getType(), 0);
                }

                if (SHAPE_CHECK_BLOCKS.contains(pBlockstate.getBlock()))
                {
                    pLevel.getChunk(blockpos).markPosForPostprocessing(blockpos);
                }
            }
        }
    }

    protected boolean canBeReplaced(LevelReader p_163553_, int p_163554_, int p_163555_, int p_163556_, BoundingBox p_163557_)
    {
        return true;
    }

    protected BlockState getBlock(BlockGetter pLevel, int pX, int pY, int pZ, BoundingBox pBoundingbox)
    {
        BlockPos blockpos = this.getWorldPos(pX, pY, pZ);
        return !pBoundingbox.isInside(blockpos) ? Blocks.AIR.defaultBlockState() : pLevel.getBlockState(blockpos);
    }

    protected boolean isInterior(LevelReader pLevel, int pX, int pY, int pZ, BoundingBox pBoundingbox)
    {
        BlockPos blockpos = this.getWorldPos(pX, pY + 1, pZ);

        if (!pBoundingbox.isInside(blockpos))
        {
            return false;
        }
        else
        {
            return blockpos.getY() < pLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, blockpos.getX(), blockpos.getZ());
        }
    }

    protected void generateAirBox(WorldGenLevel pLevel, BoundingBox pStructurebb, int pMinX, int pMinY, int pMinZ, int pMaxX, int pMaxY, int pMaxZ)
    {
        for (int i = pMinY; i <= pMaxY; ++i)
        {
            for (int j = pMinX; j <= pMaxX; ++j)
            {
                for (int k = pMinZ; k <= pMaxZ; ++k)
                {
                    this.placeBlock(pLevel, Blocks.AIR.defaultBlockState(), j, i, k, pStructurebb);
                }
            }
        }
    }

    protected void generateBox(WorldGenLevel pLevel, BoundingBox pBoundingbox, int pXMin, int pYMin, int pZMin, int pXMax, int pYMax, int pZMax, BlockState pBoundaryBlockState, BlockState pInsideBlockState, boolean pExistingOnly)
    {
        for (int i = pYMin; i <= pYMax; ++i)
        {
            for (int j = pXMin; j <= pXMax; ++j)
            {
                for (int k = pZMin; k <= pZMax; ++k)
                {
                    if (!pExistingOnly || !this.getBlock(pLevel, j, i, k, pBoundingbox).isAir())
                    {
                        if (i != pYMin && i != pYMax && j != pXMin && j != pXMax && k != pZMin && k != pZMax)
                        {
                            this.placeBlock(pLevel, pInsideBlockState, j, i, k, pBoundingbox);
                        }
                        else
                        {
                            this.placeBlock(pLevel, pBoundaryBlockState, j, i, k, pBoundingbox);
                        }
                    }
                }
            }
        }
    }

    protected void generateBox(WorldGenLevel pLevel, BoundingBox pBoundingbox, BoundingBox pXMin, BlockState pYMin, BlockState pZMin, boolean pXMax)
    {
        this.generateBox(pLevel, pBoundingbox, pXMin.minX(), pXMin.minY(), pXMin.minZ(), pXMin.maxX(), pXMin.maxY(), pXMin.maxZ(), pYMin, pZMin, pXMax);
    }

    protected void generateBox(WorldGenLevel pLevel, BoundingBox pBoundingbox, int pXMin, int pYMin, int pZMin, int pXMax, int pYMax, int pZMax, boolean pBoundaryBlockState, Random pInsideBlockState, StructurePiece.BlockSelector pExistingOnly)
    {
        for (int i = pYMin; i <= pYMax; ++i)
        {
            for (int j = pXMin; j <= pXMax; ++j)
            {
                for (int k = pZMin; k <= pZMax; ++k)
                {
                    if (!pBoundaryBlockState || !this.getBlock(pLevel, j, i, k, pBoundingbox).isAir())
                    {
                        pExistingOnly.next(pInsideBlockState, j, i, k, i == pYMin || i == pYMax || j == pXMin || j == pXMax || k == pZMin || k == pZMax);
                        this.placeBlock(pLevel, pExistingOnly.getNext(), j, i, k, pBoundingbox);
                    }
                }
            }
        }
    }

    protected void generateBox(WorldGenLevel pLevel, BoundingBox pBoundingbox, BoundingBox pXMin, boolean pYMin, Random pZMin, StructurePiece.BlockSelector pXMax)
    {
        this.generateBox(pLevel, pBoundingbox, pXMin.minX(), pXMin.minY(), pXMin.minZ(), pXMin.maxX(), pXMin.maxY(), pXMin.maxZ(), pYMin, pZMin, pXMax);
    }

    protected void generateMaybeBox(WorldGenLevel pLevel, BoundingBox pSbb, Random pRand, float pChance, int pX1, int pY1, int pZ1, int pX2, int pY2, int pZ2, BlockState pEdgeState, BlockState pState, boolean pRequireNonAir, boolean pRequiredSkylight)
    {
        for (int i = pY1; i <= pY2; ++i)
        {
            for (int j = pX1; j <= pX2; ++j)
            {
                for (int k = pZ1; k <= pZ2; ++k)
                {
                    if (!(pRand.nextFloat() > pChance) && (!pRequireNonAir || !this.getBlock(pLevel, j, i, k, pSbb).isAir()) && (!pRequiredSkylight || this.isInterior(pLevel, j, i, k, pSbb)))
                    {
                        if (i != pY1 && i != pY2 && j != pX1 && j != pX2 && k != pZ1 && k != pZ2)
                        {
                            this.placeBlock(pLevel, pState, j, i, k, pSbb);
                        }
                        else
                        {
                            this.placeBlock(pLevel, pEdgeState, j, i, k, pSbb);
                        }
                    }
                }
            }
        }
    }

    protected void maybeGenerateBlock(WorldGenLevel pLevel, BoundingBox pBoundingbox, Random pRand, float pChance, int pX, int pY, int pZ, BlockState pBlockstate)
    {
        if (pRand.nextFloat() < pChance)
        {
            this.placeBlock(pLevel, pBlockstate, pX, pY, pZ, pBoundingbox);
        }
    }

    protected void generateUpperHalfSphere(WorldGenLevel pLevel, BoundingBox pBoundingbox, int pMinX, int pMinY, int pMinZ, int pMaxX, int pMaxY, int pMaxZ, BlockState pBlockstate, boolean pExcludeAir)
    {
        float f = (float)(pMaxX - pMinX + 1);
        float f1 = (float)(pMaxY - pMinY + 1);
        float f2 = (float)(pMaxZ - pMinZ + 1);
        float f3 = (float)pMinX + f / 2.0F;
        float f4 = (float)pMinZ + f2 / 2.0F;

        for (int i = pMinY; i <= pMaxY; ++i)
        {
            float f5 = (float)(i - pMinY) / f1;

            for (int j = pMinX; j <= pMaxX; ++j)
            {
                float f6 = ((float)j - f3) / (f * 0.5F);

                for (int k = pMinZ; k <= pMaxZ; ++k)
                {
                    float f7 = ((float)k - f4) / (f2 * 0.5F);

                    if (!pExcludeAir || !this.getBlock(pLevel, j, i, k, pBoundingbox).isAir())
                    {
                        float f8 = f6 * f6 + f5 * f5 + f7 * f7;

                        if (f8 <= 1.05F)
                        {
                            this.placeBlock(pLevel, pBlockstate, j, i, k, pBoundingbox);
                        }
                    }
                }
            }
        }
    }

    protected void fillColumnDown(WorldGenLevel pLevel, BlockState pBlockstate, int pX, int pY, int pZ, BoundingBox pBoundingbox)
    {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = this.getWorldPos(pX, pY, pZ);

        if (pBoundingbox.isInside(blockpos$mutableblockpos))
        {
            while (this.isReplaceableByStructures(pLevel.getBlockState(blockpos$mutableblockpos)) && blockpos$mutableblockpos.getY() > pLevel.getMinBuildHeight() + 1)
            {
                pLevel.setBlock(blockpos$mutableblockpos, pBlockstate, 2);
                blockpos$mutableblockpos.move(Direction.DOWN);
            }
        }
    }

    protected boolean isReplaceableByStructures(BlockState p_163573_)
    {
        return p_163573_.isAir() || p_163573_.getMaterial().isLiquid() || p_163573_.is(Blocks.GLOW_LICHEN) || p_163573_.is(Blocks.SEAGRASS) || p_163573_.is(Blocks.TALL_SEAGRASS);
    }

    protected boolean createChest(WorldGenLevel pLevel, BoundingBox pBounds, Random pRand, int pPos, int pResourceLocation, int p_73514_, ResourceLocation p_73515_)
    {
        return this.createChest(pLevel, pBounds, pRand, this.getWorldPos(pPos, pResourceLocation, p_73514_), p_73515_, (BlockState)null);
    }

    public static BlockState reorient(BlockGetter pLevel, BlockPos pPos, BlockState pBlockState)
    {
        Direction direction = null;

        for (Direction direction1 : Direction.Plane.HORIZONTAL)
        {
            BlockPos blockpos = pPos.relative(direction1);
            BlockState blockstate = pLevel.getBlockState(blockpos);

            if (blockstate.is(Blocks.CHEST))
            {
                return pBlockState;
            }

            if (blockstate.isSolidRender(pLevel, blockpos))
            {
                if (direction != null)
                {
                    direction = null;
                    break;
                }

                direction = direction1;
            }
        }

        if (direction != null)
        {
            return pBlockState.setValue(HorizontalDirectionalBlock.FACING, direction.getOpposite());
        }
        else
        {
            Direction direction2 = pBlockState.getValue(HorizontalDirectionalBlock.FACING);
            BlockPos blockpos1 = pPos.relative(direction2);

            if (pLevel.getBlockState(blockpos1).isSolidRender(pLevel, blockpos1))
            {
                direction2 = direction2.getOpposite();
                blockpos1 = pPos.relative(direction2);
            }

            if (pLevel.getBlockState(blockpos1).isSolidRender(pLevel, blockpos1))
            {
                direction2 = direction2.getClockWise();
                blockpos1 = pPos.relative(direction2);
            }

            if (pLevel.getBlockState(blockpos1).isSolidRender(pLevel, blockpos1))
            {
                direction2 = direction2.getOpposite();
                pPos.relative(direction2);
            }

            return pBlockState.setValue(HorizontalDirectionalBlock.FACING, direction2);
        }
    }

    protected boolean createChest(ServerLevelAccessor pLevel, BoundingBox pBounds, Random pRand, BlockPos pPos, ResourceLocation pResourceLocation, @Nullable BlockState p_73426_)
    {
        if (pBounds.isInside(pPos) && !pLevel.getBlockState(pPos).is(Blocks.CHEST))
        {
            if (p_73426_ == null)
            {
                p_73426_ = reorient(pLevel, pPos, Blocks.CHEST.defaultBlockState());
            }

            pLevel.setBlock(pPos, p_73426_, 2);
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);

            if (blockentity instanceof ChestBlockEntity)
            {
                ((ChestBlockEntity)blockentity).setLootTable(pResourceLocation, pRand.nextLong());
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    protected boolean createDispenser(WorldGenLevel pLevel, BoundingBox pSbb, Random pRand, int pX, int pY, int pZ, Direction pFacing, ResourceLocation pLootTable)
    {
        BlockPos blockpos = this.getWorldPos(pX, pY, pZ);

        if (pSbb.isInside(blockpos) && !pLevel.getBlockState(blockpos).is(Blocks.DISPENSER))
        {
            this.placeBlock(pLevel, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, pFacing), pX, pY, pZ, pSbb);
            BlockEntity blockentity = pLevel.getBlockEntity(blockpos);

            if (blockentity instanceof DispenserBlockEntity)
            {
                ((DispenserBlockEntity)blockentity).setLootTable(pLootTable, pRand.nextLong());
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    public void move(int pX, int pY, int pZ)
    {
        this.boundingBox.move(pX, pY, pZ);
    }

    @Nullable
    public Direction getOrientation()
    {
        return this.orientation;
    }

    public void setOrientation(@Nullable Direction pFacing)
    {
        this.orientation = pFacing;

        if (pFacing == null)
        {
            this.rotation = Rotation.NONE;
            this.mirror = Mirror.NONE;
        }
        else
        {
            switch (pFacing)
            {
                case SOUTH:
                    this.mirror = Mirror.LEFT_RIGHT;
                    this.rotation = Rotation.NONE;
                    break;

                case WEST:
                    this.mirror = Mirror.LEFT_RIGHT;
                    this.rotation = Rotation.CLOCKWISE_90;
                    break;

                case EAST:
                    this.mirror = Mirror.NONE;
                    this.rotation = Rotation.CLOCKWISE_90;
                    break;

                default:
                    this.mirror = Mirror.NONE;
                    this.rotation = Rotation.NONE;
            }
        }
    }

    public Rotation getRotation()
    {
        return this.rotation;
    }

    public Mirror getMirror()
    {
        return this.mirror;
    }

    public StructurePieceType getType()
    {
        return this.type;
    }

    protected abstract static class BlockSelector
    {
        protected BlockState next = Blocks.AIR.defaultBlockState();

        public abstract void next(Random pRand, int pX, int pY, int pZ, boolean pWall);

        public BlockState getNext()
        {
            return this.next;
        }
    }
}
