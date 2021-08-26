package net.minecraft.core;

import com.google.common.collect.AbstractIterator;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.concurrent.Immutable;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Immutable
public class BlockPos extends Vec3i
{
    public static final Codec<BlockPos> CODEC = Codec.INT_STREAM.comapFlatMap((p_121967_) ->
    {
        return Util.fixedSize(p_121967_, 3).map((p_175270_) -> {
            return new BlockPos(p_175270_[0], p_175270_[1], p_175270_[2]);
        });
    }, (p_121924_) ->
    {
        return IntStream.of(p_121924_.getX(), p_121924_.getY(), p_121924_.getZ());
    }).stable();
    private static final Logger LOGGER = LogManager.getLogger();
    public static final BlockPos ZERO = new BlockPos(0, 0, 0);
    private static final int PACKED_X_LENGTH = 1 + Mth.log2(Mth.smallestEncompassingPowerOfTwo(30000000));
    private static final int PACKED_Z_LENGTH = PACKED_X_LENGTH;
    public static final int PACKED_Y_LENGTH = 64 - PACKED_X_LENGTH - PACKED_Z_LENGTH;
    private static final long PACKED_X_MASK = (1L << PACKED_X_LENGTH) - 1L;
    private static final long PACKED_Y_MASK = (1L << PACKED_Y_LENGTH) - 1L;
    private static final long PACKED_Z_MASK = (1L << PACKED_Z_LENGTH) - 1L;
    private static final int Y_OFFSET = 0;
    private static final int Z_OFFSET = PACKED_Y_LENGTH;
    private static final int X_OFFSET = PACKED_Y_LENGTH + PACKED_Z_LENGTH;

    public BlockPos(int pX, int pY, int pZ)
    {
        super(pX, pY, pZ);
    }

    public BlockPos(double pX, double pY, double pZ)
    {
        super(pX, pY, pZ);
    }

    public BlockPos(Vec3 pX)
    {
        this(pX.x, pX.y, pX.z);
    }

    public BlockPos(Position pX)
    {
        this(pX.x(), pX.y(), pX.z());
    }

    public BlockPos(Vec3i pX)
    {
        this(pX.getX(), pX.getY(), pX.getZ());
    }

    public static long offset(long pPos, Direction p_121917_)
    {
        return offset(pPos, p_121917_.getStepX(), p_121917_.getStepY(), p_121917_.getStepZ());
    }

    public static long offset(long pPos, int p_121912_, int pDx, int pDy)
    {
        return asLong(getX(pPos) + p_121912_, getY(pPos) + pDx, getZ(pPos) + pDy);
    }

    public static int getX(long pPackedPos)
    {
        return (int)(pPackedPos << 64 - X_OFFSET - PACKED_X_LENGTH >> 64 - PACKED_X_LENGTH);
    }

    public static int getY(long pPackedPos)
    {
        return (int)(pPackedPos << 64 - PACKED_Y_LENGTH >> 64 - PACKED_Y_LENGTH);
    }

    public static int getZ(long pPackedPos)
    {
        return (int)(pPackedPos << 64 - Z_OFFSET - PACKED_Z_LENGTH >> 64 - PACKED_Z_LENGTH);
    }

    public static BlockPos of(long pPackedPos)
    {
        return new BlockPos(getX(pPackedPos), getY(pPackedPos), getZ(pPackedPos));
    }

    public long asLong()
    {
        return asLong(this.getX(), this.getY(), this.getZ());
    }

    public static long asLong(int pX, int pY, int pZ)
    {
        long i = 0L;
        i = i | ((long)pX & PACKED_X_MASK) << X_OFFSET;
        i = i | ((long)pY & PACKED_Y_MASK) << 0;
        return i | ((long)pZ & PACKED_Z_MASK) << Z_OFFSET;
    }

    public static long getFlatIndex(long pPackedPos)
    {
        return pPackedPos & -16L;
    }

    public BlockPos offset(double p_121879_, double pDx, double pDy)
    {
        return p_121879_ == 0.0D && pDx == 0.0D && pDy == 0.0D ? this : new BlockPos((double)this.getX() + p_121879_, (double)this.getY() + pDx, (double)this.getZ() + pDy);
    }

    public BlockPos offset(int p_121973_, int pDx, int pDy)
    {
        return p_121973_ == 0 && pDx == 0 && pDy == 0 ? this : new BlockPos(this.getX() + p_121973_, this.getY() + pDx, this.getZ() + pDy);
    }

    public BlockPos offset(Vec3i p_121956_)
    {
        return this.offset(p_121956_.getX(), p_121956_.getY(), p_121956_.getZ());
    }

    public BlockPos subtract(Vec3i p_121997_)
    {
        return this.offset(-p_121997_.getX(), -p_121997_.getY(), -p_121997_.getZ());
    }

    public BlockPos multiply(int p_175263_)
    {
        if (p_175263_ == 1)
        {
            return this;
        }
        else
        {
            return p_175263_ == 0 ? ZERO : new BlockPos(this.getX() * p_175263_, this.getY() * p_175263_, this.getZ() * p_175263_);
        }
    }

    public BlockPos above()
    {
        return this.relative(Direction.UP);
    }

    public BlockPos above(int p_121972_)
    {
        return this.relative(Direction.UP, p_121972_);
    }

    public BlockPos below()
    {
        return this.relative(Direction.DOWN);
    }

    public BlockPos below(int p_122000_)
    {
        return this.relative(Direction.DOWN, p_122000_);
    }

    public BlockPos north()
    {
        return this.relative(Direction.NORTH);
    }

    public BlockPos north(int p_122014_)
    {
        return this.relative(Direction.NORTH, p_122014_);
    }

    public BlockPos south()
    {
        return this.relative(Direction.SOUTH);
    }

    public BlockPos south(int p_122021_)
    {
        return this.relative(Direction.SOUTH, p_122021_);
    }

    public BlockPos west()
    {
        return this.relative(Direction.WEST);
    }

    public BlockPos west(int p_122026_)
    {
        return this.relative(Direction.WEST, p_122026_);
    }

    public BlockPos east()
    {
        return this.relative(Direction.EAST);
    }

    public BlockPos east(int p_122031_)
    {
        return this.relative(Direction.EAST, p_122031_);
    }

    public BlockPos relative(Direction pFacing)
    {
        return new BlockPos(this.getX() + pFacing.getStepX(), this.getY() + pFacing.getStepY(), this.getZ() + pFacing.getStepZ());
    }

    public BlockPos relative(Direction pFacing, int pN)
    {
        return pN == 0 ? this : new BlockPos(this.getX() + pFacing.getStepX() * pN, this.getY() + pFacing.getStepY() * pN, this.getZ() + pFacing.getStepZ() * pN);
    }

    public BlockPos relative(Direction.Axis pFacing, int pN)
    {
        if (pN == 0)
        {
            return this;
        }
        else
        {
            int i = pFacing == Direction.Axis.X ? pN : 0;
            int j = pFacing == Direction.Axis.Y ? pN : 0;
            int k = pFacing == Direction.Axis.Z ? pN : 0;
            return new BlockPos(this.getX() + i, this.getY() + j, this.getZ() + k);
        }
    }

    public BlockPos rotate(Rotation pRotation)
    {
        switch (pRotation)
        {
            case NONE:
            default:
                return this;

            case CLOCKWISE_90:
                return new BlockPos(-this.getZ(), this.getY(), this.getX());

            case CLOCKWISE_180:
                return new BlockPos(-this.getX(), this.getY(), -this.getZ());

            case COUNTERCLOCKWISE_90:
                return new BlockPos(this.getZ(), this.getY(), -this.getX());
        }
    }

    public BlockPos cross(Vec3i pVec)
    {
        return new BlockPos(this.getY() * pVec.getZ() - this.getZ() * pVec.getY(), this.getZ() * pVec.getX() - this.getX() * pVec.getZ(), this.getX() * pVec.getY() - this.getY() * pVec.getX());
    }

    public BlockPos atY(int p_175289_)
    {
        return new BlockPos(this.getX(), p_175289_, this.getZ());
    }

    public BlockPos immutable()
    {
        return this;
    }

    public BlockPos.MutableBlockPos mutable()
    {
        return new BlockPos.MutableBlockPos(this.getX(), this.getY(), this.getZ());
    }

    public static Iterable<BlockPos> randomInCube(Random p_175265_, int p_175266_, BlockPos p_175267_, int p_175268_)
    {
        return randomBetweenClosed(p_175265_, p_175266_, p_175267_.getX() - p_175268_, p_175267_.getY() - p_175268_, p_175267_.getZ() - p_175268_, p_175267_.getX() + p_175268_, p_175267_.getY() + p_175268_, p_175267_.getZ() + p_175268_);
    }

    public static Iterable<BlockPos> randomBetweenClosed(Random pRand, int pAmount, int pMinX, int pMinY, int pMinZ, int pMaxX, int pMaxY, int pMaxZ)
    {
        int i = pMaxX - pMinX + 1;
        int j = pMaxY - pMinY + 1;
        int k = pMaxZ - pMinZ + 1;
        return () ->
        {
            return new AbstractIterator<BlockPos>()
            {
                final BlockPos.MutableBlockPos nextPos = new BlockPos.MutableBlockPos();
                int counter = pAmount;
                protected BlockPos computeNext()
                {
                    if (this.counter <= 0)
                    {
                        return this.endOfData();
                    }
                    else
                    {
                        BlockPos blockpos = this.nextPos.set(pMinX + pRand.nextInt(i), pMinY + pRand.nextInt(j), pMinZ + pRand.nextInt(k));
                        --this.counter;
                        return blockpos;
                    }
                }
            };
        };
    }

    public static Iterable<BlockPos> withinManhattan(BlockPos pPos, int pXWidth, int pYHeight, int pZWidth)
    {
        int i = pXWidth + pYHeight + pZWidth;
        int j = pPos.getX();
        int k = pPos.getY();
        int l = pPos.getZ();
        return () ->
        {
            return new AbstractIterator<BlockPos>()
            {
                private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
                private int currentDepth;
                private int maxX;
                private int maxY;
                private int x;
                private int y;
                private boolean zMirror;
                protected BlockPos computeNext()
                {
                    if (this.zMirror)
                    {
                        this.zMirror = false;
                        this.cursor.setZ(l - (this.cursor.getZ() - l));
                        return this.cursor;
                    }
                    else
                    {
                        BlockPos blockpos;

                        for (blockpos = null; blockpos == null; ++this.y)
                        {
                            if (this.y > this.maxY)
                            {
                                ++this.x;

                                if (this.x > this.maxX)
                                {
                                    ++this.currentDepth;

                                    if (this.currentDepth > i)
                                    {
                                        return this.endOfData();
                                    }

                                    this.maxX = Math.min(pXWidth, this.currentDepth);
                                    this.x = -this.maxX;
                                }

                                this.maxY = Math.min(pYHeight, this.currentDepth - Math.abs(this.x));
                                this.y = -this.maxY;
                            }

                            int i1 = this.x;
                            int j1 = this.y;
                            int k1 = this.currentDepth - Math.abs(i1) - Math.abs(j1);

                            if (k1 <= pZWidth)
                            {
                                this.zMirror = k1 != 0;
                                blockpos = this.cursor.set(j + i1, k + j1, l + k1);
                            }
                        }

                        return blockpos;
                    }
                }
            };
        };
    }

    public static Optional<BlockPos> findClosestMatch(BlockPos pPos, int pWidth, int pHeight, Predicate<BlockPos> pPosFilter)
    {
        return withinManhattanStream(pPos, pWidth, pHeight, pWidth).filter(pPosFilter).findFirst();
    }

    public static Stream<BlockPos> withinManhattanStream(BlockPos pPos, int pXWidth, int pYHeight, int pZWidth)
    {
        return StreamSupport.stream(withinManhattan(pPos, pXWidth, pYHeight, pZWidth).spliterator(), false);
    }

    public static Iterable<BlockPos> betweenClosed(BlockPos pX1, BlockPos pY1)
    {
        return betweenClosed(Math.min(pX1.getX(), pY1.getX()), Math.min(pX1.getY(), pY1.getY()), Math.min(pX1.getZ(), pY1.getZ()), Math.max(pX1.getX(), pY1.getX()), Math.max(pX1.getY(), pY1.getY()), Math.max(pX1.getZ(), pY1.getZ()));
    }

    public static Stream<BlockPos> betweenClosedStream(BlockPos pMinX, BlockPos pMinY)
    {
        return StreamSupport.stream(betweenClosed(pMinX, pMinY).spliterator(), false);
    }

    public static Stream<BlockPos> betweenClosedStream(BoundingBox pMinX)
    {
        return betweenClosedStream(Math.min(pMinX.minX(), pMinX.maxX()), Math.min(pMinX.minY(), pMinX.maxY()), Math.min(pMinX.minZ(), pMinX.maxZ()), Math.max(pMinX.minX(), pMinX.maxX()), Math.max(pMinX.minY(), pMinX.maxY()), Math.max(pMinX.minZ(), pMinX.maxZ()));
    }

    public static Stream<BlockPos> betweenClosedStream(AABB pMinX)
    {
        return betweenClosedStream(Mth.floor(pMinX.minX), Mth.floor(pMinX.minY), Mth.floor(pMinX.minZ), Mth.floor(pMinX.maxX), Mth.floor(pMinX.maxY), Mth.floor(pMinX.maxZ));
    }

    public static Stream<BlockPos> betweenClosedStream(int pMinX, int pMinY, int pMinZ, int pMaxX, int pMaxY, int pMaxZ)
    {
        return StreamSupport.stream(betweenClosed(pMinX, pMinY, pMinZ, pMaxX, pMaxY, pMaxZ).spliterator(), false);
    }

    public static Iterable<BlockPos> betweenClosed(int pX1, int pY1, int pZ1, int pX2, int pY2, int pZ2)
    {
        int i = pX2 - pX1 + 1;
        int j = pY2 - pY1 + 1;
        int k = pZ2 - pZ1 + 1;
        int l = i * j * k;
        return () ->
        {
            return new AbstractIterator<BlockPos>()
            {
                private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
                private int index;
                protected BlockPos computeNext()
                {
                    if (this.index == l)
                    {
                        return this.endOfData();
                    }
                    else
                    {
                        int i1 = this.index % i;
                        int j1 = this.index / i;
                        int k1 = j1 % j;
                        int l1 = j1 / j;
                        ++this.index;
                        return this.cursor.set(pX1 + i1, pY1 + k1, pZ1 + l1);
                    }
                }
            };
        };
    }

    public static Iterable<BlockPos.MutableBlockPos> spiralAround(BlockPos p_121936_, int p_121937_, Direction p_121938_, Direction p_121939_)
    {
        Validate.validState(p_121938_.getAxis() != p_121939_.getAxis(), "The two directions cannot be on the same axis");
        return () ->
        {
            return new AbstractIterator<BlockPos.MutableBlockPos>()
            {
                private final Direction[] directions = new Direction[] {p_121938_, p_121939_, p_121938_.getOpposite(), p_121939_.getOpposite()};
                private final BlockPos.MutableBlockPos cursor = p_121936_.mutable().move(p_121939_);
                private final int legs = 4 * p_121937_;
                private int leg = -1;
                private int legSize;
                private int legIndex;
                private int lastX = this.cursor.getX();
                private int lastY = this.cursor.getY();
                private int lastZ = this.cursor.getZ();
                protected BlockPos.MutableBlockPos computeNext()
                {
                    this.cursor.set(this.lastX, this.lastY, this.lastZ).move(this.directions[(this.leg + 4) % 4]);
                    this.lastX = this.cursor.getX();
                    this.lastY = this.cursor.getY();
                    this.lastZ = this.cursor.getZ();

                    if (this.legIndex >= this.legSize)
                    {
                        if (this.leg >= this.legs)
                        {
                            return this.endOfData();
                        }

                        ++this.leg;
                        this.legIndex = 0;
                        this.legSize = this.leg / 2 + 1;
                    }

                    ++this.legIndex;
                    return this.cursor;
                }
            };
        };
    }

    public static class MutableBlockPos extends BlockPos
    {
        public MutableBlockPos()
        {
            this(0, 0, 0);
        }

        public MutableBlockPos(int p_122130_, int p_122131_, int p_122132_)
        {
            super(p_122130_, p_122131_, p_122132_);
        }

        public MutableBlockPos(double p_122126_, double p_122127_, double p_122128_)
        {
            this(Mth.floor(p_122126_), Mth.floor(p_122127_), Mth.floor(p_122128_));
        }

        public BlockPos offset(double p_122134_, double p_122135_, double p_122136_)
        {
            return super.offset(p_122134_, p_122135_, p_122136_).immutable();
        }

        public BlockPos offset(int p_122163_, int p_122164_, int p_122165_)
        {
            return super.offset(p_122163_, p_122164_, p_122165_).immutable();
        }

        public BlockPos multiply(int p_175305_)
        {
            return super.multiply(p_175305_).immutable();
        }

        public BlockPos relative(Direction pFacing, int pN)
        {
            return super.relative(pFacing, pN).immutable();
        }

        public BlockPos relative(Direction.Axis pFacing, int pN)
        {
            return super.relative(pFacing, pN).immutable();
        }

        public BlockPos rotate(Rotation pRotation)
        {
            return super.rotate(pRotation).immutable();
        }

        public BlockPos.MutableBlockPos set(int pX, int p_122180_, int pY)
        {
            this.setX(pX);
            this.setY(p_122180_);
            this.setZ(pY);
            return this;
        }

        public BlockPos.MutableBlockPos set(double pX, double p_122171_, double pY)
        {
            return this.set(Mth.floor(pX), Mth.floor(p_122171_), Mth.floor(pY));
        }

        public BlockPos.MutableBlockPos set(Vec3i pX)
        {
            return this.set(pX.getX(), pX.getY(), pX.getZ());
        }

        public BlockPos.MutableBlockPos set(long pX)
        {
            return this.set(getX(pX), getY(pX), getZ(pX));
        }

        public BlockPos.MutableBlockPos set(AxisCycle pX, int p_122141_, int pY, int p_122143_)
        {
            return this.set(pX.cycle(p_122141_, pY, p_122143_, Direction.Axis.X), pX.cycle(p_122141_, pY, p_122143_, Direction.Axis.Y), pX.cycle(p_122141_, pY, p_122143_, Direction.Axis.Z));
        }

        public BlockPos.MutableBlockPos setWithOffset(Vec3i pPos, Direction pOffsetX)
        {
            return this.set(pPos.getX() + pOffsetX.getStepX(), pPos.getY() + pOffsetX.getStepY(), pPos.getZ() + pOffsetX.getStepZ());
        }

        public BlockPos.MutableBlockPos setWithOffset(Vec3i pPos, int pOffsetX, int pOffsetY, int pOffsetZ)
        {
            return this.set(pPos.getX() + pOffsetX, pPos.getY() + pOffsetY, pPos.getZ() + pOffsetZ);
        }

        public BlockPos.MutableBlockPos setWithOffset(Vec3i pPos, Vec3i pOffsetX)
        {
            return this.set(pPos.getX() + pOffsetX.getX(), pPos.getY() + pOffsetX.getY(), pPos.getZ() + pOffsetX.getZ());
        }

        public BlockPos.MutableBlockPos move(Direction pX)
        {
            return this.move(pX, 1);
        }

        public BlockPos.MutableBlockPos move(Direction pX, int pY)
        {
            return this.set(this.getX() + pX.getStepX() * pY, this.getY() + pX.getStepY() * pY, this.getZ() + pX.getStepZ() * pY);
        }

        public BlockPos.MutableBlockPos move(int pX, int pY, int pZ)
        {
            return this.set(this.getX() + pX, this.getY() + pY, this.getZ() + pZ);
        }

        public BlockPos.MutableBlockPos move(Vec3i pX)
        {
            return this.set(this.getX() + pX.getX(), this.getY() + pX.getY(), this.getZ() + pX.getZ());
        }

        public BlockPos.MutableBlockPos clamp(Direction.Axis pAxis, int pMin, int pMax)
        {
            switch (pAxis)
            {
                case X:
                    return this.set(Mth.clamp(this.getX(), pMin, pMax), this.getY(), this.getZ());

                case Y:
                    return this.set(this.getX(), Mth.clamp(this.getY(), pMin, pMax), this.getZ());

                case Z:
                    return this.set(this.getX(), this.getY(), Mth.clamp(this.getZ(), pMin, pMax));

                default:
                    throw new IllegalStateException("Unable to clamp axis " + pAxis);
            }
        }

        public BlockPos.MutableBlockPos setX(int p_175341_)
        {
            super.setX(p_175341_);
            return this;
        }

        public BlockPos.MutableBlockPos setY(int p_175343_)
        {
            super.setY(p_175343_);
            return this;
        }

        public BlockPos.MutableBlockPos setZ(int p_175345_)
        {
            super.setZ(p_175345_);
            return this;
        }

        public BlockPos immutable()
        {
            return new BlockPos(this);
        }
    }
}
