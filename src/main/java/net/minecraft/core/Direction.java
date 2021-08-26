package net.minecraft.core;

import com.google.common.collect.Iterators;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;

public enum Direction implements StringRepresentable
{
    DOWN(0, 1, -1, "down", Direction.AxisDirection.NEGATIVE, Direction.Axis.Y, new Vec3i(0, -1, 0)),
    UP(1, 0, -1, "up", Direction.AxisDirection.POSITIVE, Direction.Axis.Y, new Vec3i(0, 1, 0)),
    NORTH(2, 3, 2, "north", Direction.AxisDirection.NEGATIVE, Direction.Axis.Z, new Vec3i(0, 0, -1)),
    SOUTH(3, 2, 0, "south", Direction.AxisDirection.POSITIVE, Direction.Axis.Z, new Vec3i(0, 0, 1)),
    WEST(4, 5, 1, "west", Direction.AxisDirection.NEGATIVE, Direction.Axis.X, new Vec3i(-1, 0, 0)),
    EAST(5, 4, 3, "east", Direction.AxisDirection.POSITIVE, Direction.Axis.X, new Vec3i(1, 0, 0));

    public static final Codec<Direction> CODEC = StringRepresentable.fromEnum(Direction::values, Direction::byName);
    private final int data3d;
    private final int oppositeIndex;
    private final int data2d;
    private final String name;
    private final Direction.Axis axis;
    private final Direction.AxisDirection axisDirection;
    private final Vec3i normal;
    public static final Direction[] VALUES = values();
    private static final Map<String, Direction> BY_NAME = Arrays.stream(VALUES).collect(Collectors.toMap(Direction::getName, (p_122425_0_) -> {
        return p_122425_0_;
    }));
    public static final Direction[] BY_3D_DATA = Arrays.stream(VALUES).sorted(Comparator.comparingInt((p_122422_0_) -> {
        return p_122422_0_.data3d;
    })).toArray((p_122417_0_) -> {
        return new Direction[p_122417_0_];
    });
    private static final Direction[] BY_2D_DATA = Arrays.stream(VALUES).filter((p_122419_0_) -> {
        return p_122419_0_.getAxis().isHorizontal();
    }).sorted(Comparator.comparingInt((p_122414_0_) -> {
        return p_122414_0_.data2d;
    })).toArray((p_122412_0_) -> {
        return new Direction[p_122412_0_];
    });
    private static final Long2ObjectMap<Direction> BY_NORMAL = Arrays.stream(VALUES).collect(Collectors.toMap((p_122409_0_) -> {
        return (new BlockPos(p_122409_0_.getNormal())).asLong();
    }, (p_122393_0_) -> {
        return p_122393_0_;
    }, (p_122395_0_, p_122395_1_) -> {
        throw new IllegalArgumentException("Duplicate keys");
    }, Long2ObjectOpenHashMap::new));

    private Direction(int p_122356_, int p_122357_, int p_122358_, String p_122359_, Direction.AxisDirection p_122360_, Direction.Axis p_122361_, Vec3i p_122362_)
    {
        this.data3d = p_122356_;
        this.data2d = p_122358_;
        this.oppositeIndex = p_122357_;
        this.name = p_122359_;
        this.axis = p_122361_;
        this.axisDirection = p_122360_;
        this.normal = p_122362_;
    }

    public static Direction[] orderedByNearest(Entity pEntity)
    {
        float f = pEntity.getViewXRot(1.0F) * ((float)Math.PI / 180F);
        float f1 = -pEntity.getViewYRot(1.0F) * ((float)Math.PI / 180F);
        float f2 = Mth.sin(f);
        float f3 = Mth.cos(f);
        float f4 = Mth.sin(f1);
        float f5 = Mth.cos(f1);
        boolean flag = f4 > 0.0F;
        boolean flag1 = f2 < 0.0F;
        boolean flag2 = f5 > 0.0F;
        float f6 = flag ? f4 : -f4;
        float f7 = flag1 ? -f2 : f2;
        float f8 = flag2 ? f5 : -f5;
        float f9 = f6 * f3;
        float f10 = f8 * f3;
        Direction direction = flag ? EAST : WEST;
        Direction direction1 = flag1 ? UP : DOWN;
        Direction direction2 = flag2 ? SOUTH : NORTH;

        if (f6 > f8)
        {
            if (f7 > f9)
            {
                return makeDirectionArray(direction1, direction, direction2);
            }
            else
            {
                return f10 > f7 ? makeDirectionArray(direction, direction2, direction1) : makeDirectionArray(direction, direction1, direction2);
            }
        }
        else if (f7 > f10)
        {
            return makeDirectionArray(direction1, direction2, direction);
        }
        else
        {
            return f9 > f7 ? makeDirectionArray(direction2, direction, direction1) : makeDirectionArray(direction2, direction1, direction);
        }
    }

    private static Direction[] makeDirectionArray(Direction pFirst, Direction pSecond, Direction pThird)
    {
        return new Direction[] {pFirst, pSecond, pThird, pThird.getOpposite(), pSecond.getOpposite(), pFirst.getOpposite()};
    }

    public static Direction rotate(Matrix4f pMatrix, Direction pDirection)
    {
        Vec3i vec3i = pDirection.getNormal();
        Vector4f vector4f = new Vector4f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ(), 0.0F);
        vector4f.transform(pMatrix);
        return getNearest(vector4f.x(), vector4f.y(), vector4f.z());
    }

    public Quaternion getRotation()
    {
        Quaternion quaternion = Vector3f.XP.rotationDegrees(90.0F);

        switch (this)
        {
            case DOWN:
                return Vector3f.XP.rotationDegrees(180.0F);

            case UP:
                return Quaternion.ONE.copy();

            case NORTH:
                quaternion.mul(Vector3f.ZP.rotationDegrees(180.0F));
                return quaternion;

            case SOUTH:
                return quaternion;

            case WEST:
                quaternion.mul(Vector3f.ZP.rotationDegrees(90.0F));
                return quaternion;

            case EAST:
            default:
                quaternion.mul(Vector3f.ZP.rotationDegrees(-90.0F));
                return quaternion;
        }
    }

    public int get3DDataValue()
    {
        return this.data3d;
    }

    public int get2DDataValue()
    {
        return this.data2d;
    }

    public Direction.AxisDirection getAxisDirection()
    {
        return this.axisDirection;
    }

    public static Direction getFacingAxis(Entity p_175358_, Direction.Axis p_175359_)
    {
        switch (p_175359_)
        {
            case X:
                return EAST.isFacingAngle(p_175358_.getViewYRot(1.0F)) ? EAST : WEST;

            case Z:
                return SOUTH.isFacingAngle(p_175358_.getViewYRot(1.0F)) ? SOUTH : NORTH;

            case Y:
            default:
                return p_175358_.getViewXRot(1.0F) < 0.0F ? UP : DOWN;
        }
    }

    public Direction getOpposite()
    {
        return VALUES[this.oppositeIndex];
    }

    public Direction getClockWise(Direction.Axis p_175363_)
    {
        switch (p_175363_)
        {
            case X:
                if (this != WEST && this != EAST)
                {
                    return this.getClockWiseX();
                }

                return this;

            case Z:
                if (this != NORTH && this != SOUTH)
                {
                    return this.getClockWiseZ();
                }

                return this;

            case Y:
                if (this != UP && this != DOWN)
                {
                    return this.getClockWise();
                }

                return this;

            default:
                throw new IllegalStateException("Unable to get CW facing for axis " + p_175363_);
        }
    }

    public Direction getCounterClockWise(Direction.Axis p_175365_)
    {
        switch (p_175365_)
        {
            case X:
                if (this != WEST && this != EAST)
                {
                    return this.getCounterClockWiseX();
                }

                return this;

            case Z:
                if (this != NORTH && this != SOUTH)
                {
                    return this.getCounterClockWiseZ();
                }

                return this;

            case Y:
                if (this != UP && this != DOWN)
                {
                    return this.getCounterClockWise();
                }

                return this;

            default:
                throw new IllegalStateException("Unable to get CW facing for axis " + p_175365_);
        }
    }

    public Direction getClockWise()
    {
        switch (this)
        {
            case NORTH:
                return EAST;

            case SOUTH:
                return WEST;

            case WEST:
                return NORTH;

            case EAST:
                return SOUTH;

            default:
                throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
        }
    }

    private Direction getClockWiseX()
    {
        switch (this)
        {
            case DOWN:
                return SOUTH;

            case UP:
                return NORTH;

            case NORTH:
                return DOWN;

            case SOUTH:
                return UP;

            default:
                throw new IllegalStateException("Unable to get X-rotated facing of " + this);
        }
    }

    private Direction getCounterClockWiseX()
    {
        switch (this)
        {
            case DOWN:
                return NORTH;

            case UP:
                return SOUTH;

            case NORTH:
                return UP;

            case SOUTH:
                return DOWN;

            default:
                throw new IllegalStateException("Unable to get X-rotated facing of " + this);
        }
    }

    private Direction getClockWiseZ()
    {
        switch (this)
        {
            case DOWN:
                return WEST;

            case UP:
                return EAST;

            case NORTH:
            case SOUTH:
            default:
                throw new IllegalStateException("Unable to get Z-rotated facing of " + this);

            case WEST:
                return UP;

            case EAST:
                return DOWN;
        }
    }

    private Direction getCounterClockWiseZ()
    {
        switch (this)
        {
            case DOWN:
                return EAST;

            case UP:
                return WEST;

            case NORTH:
            case SOUTH:
            default:
                throw new IllegalStateException("Unable to get Z-rotated facing of " + this);

            case WEST:
                return DOWN;

            case EAST:
                return UP;
        }
    }

    public Direction getCounterClockWise()
    {
        switch (this)
        {
            case NORTH:
                return WEST;

            case SOUTH:
                return EAST;

            case WEST:
                return SOUTH;

            case EAST:
                return NORTH;

            default:
                throw new IllegalStateException("Unable to get CCW facing of " + this);
        }
    }

    public int getStepX()
    {
        return this.normal.getX();
    }

    public int getStepY()
    {
        return this.normal.getY();
    }

    public int getStepZ()
    {
        return this.normal.getZ();
    }

    public Vector3f step()
    {
        return new Vector3f((float)this.getStepX(), (float)this.getStepY(), (float)this.getStepZ());
    }

    public String getName()
    {
        return this.name;
    }

    public Direction.Axis getAxis()
    {
        return this.axis;
    }

    @Nullable
    public static Direction byName(@Nullable String pName)
    {
        return pName == null ? null : BY_NAME.get(pName.toLowerCase(Locale.ROOT));
    }

    public static Direction from3DDataValue(int pIndex)
    {
        return BY_3D_DATA[Mth.abs(pIndex % BY_3D_DATA.length)];
    }

    public static Direction from2DDataValue(int pHorizontalIndex)
    {
        return BY_2D_DATA[Mth.abs(pHorizontalIndex % BY_2D_DATA.length)];
    }

    @Nullable
    public static Direction fromNormal(BlockPos pX)
    {
        return BY_NORMAL.get(pX.asLong());
    }

    @Nullable
    public static Direction fromNormal(int pX, int pY, int pZ)
    {
        return BY_NORMAL.get(BlockPos.asLong(pX, pY, pZ));
    }

    public static Direction fromYRot(double pAngle)
    {
        return from2DDataValue(Mth.floor(pAngle / 90.0D + 0.5D) & 3);
    }

    public static Direction fromAxisAndDirection(Direction.Axis pAxis, Direction.AxisDirection pAxisDirection)
    {
        switch (pAxis)
        {
            case X:
                return pAxisDirection == Direction.AxisDirection.POSITIVE ? EAST : WEST;

            case Z:
            default:
                return pAxisDirection == Direction.AxisDirection.POSITIVE ? SOUTH : NORTH;

            case Y:
                return pAxisDirection == Direction.AxisDirection.POSITIVE ? UP : DOWN;
        }
    }

    public float toYRot()
    {
        return (float)((this.data2d & 3) * 90);
    }

    public static Direction getRandom(Random pRand)
    {
        return Util.m_137545_(VALUES, pRand);
    }

    public static Direction getNearest(double pX, double p_122368_, double pY)
    {
        return getNearest((float)pX, (float)p_122368_, (float)pY);
    }

    public static Direction getNearest(float pX, float p_122374_, float pY)
    {
        Direction direction = NORTH;
        float f = Float.MIN_VALUE;

        for (Direction direction1 : VALUES)
        {
            float f1 = pX * (float)direction1.normal.getX() + p_122374_ * (float)direction1.normal.getY() + pY * (float)direction1.normal.getZ();

            if (f1 > f)
            {
                f = f1;
                direction = direction1;
            }
        }

        return direction;
    }

    public String toString()
    {
        return this.name;
    }

    public String getSerializedName()
    {
        return this.name;
    }

    public static Direction get(Direction.AxisDirection pAxisDirection, Direction.Axis pAxis)
    {
        for (Direction direction : VALUES)
        {
            if (direction.getAxisDirection() == pAxisDirection && direction.getAxis() == pAxis)
            {
                return direction;
            }
        }

        throw new IllegalArgumentException("No such direction: " + pAxisDirection + " " + pAxis);
    }

    public Vec3i getNormal()
    {
        return this.normal;
    }

    public boolean isFacingAngle(float pDegrees)
    {
        float f = pDegrees * ((float)Math.PI / 180F);
        float f1 = -Mth.sin(f);
        float f2 = Mth.cos(f);
        return (float)this.normal.getX() * f1 + (float)this.normal.getZ() * f2 > 0.0F;
    }

    public static enum Axis implements StringRepresentable, Predicate<Direction> {
        X("x")
        {
            public int choose(int pX, int p_122497_, int pY)
            {
                return pX;
            }
            public double choose(double pX, double p_122493_, double pY)
            {
                return pX;
            }
        },
        Y("y")
        {
            public int choose(int pX, int p_122511_, int pY)
            {
                return p_122511_;
            }
            public double choose(double pX, double p_122507_, double pY)
            {
                return p_122507_;
            }
        },
        Z("z")
        {
            public int choose(int pX, int p_122525_, int pY)
            {
                return pY;
            }
            public double choose(double pX, double p_122521_, double pY)
            {
                return pY;
            }
        };

        public static final Direction.Axis[] VALUES = values();
        public static final Codec<Direction.Axis> CODEC = StringRepresentable.fromEnum(Direction.Axis::values, Direction.Axis::byName);
        private static final Map<String, Direction.Axis> BY_NAME = Arrays.stream(VALUES).collect(Collectors.toMap(Direction.Axis::getName, (p_122469_0_) -> {
            return p_122469_0_;
        }));
        private final String name;

        private Axis(String p_122456_)
        {
            this.name = p_122456_;
        }

        @Nullable
        public static Direction.Axis byName(String pName)
        {
            return BY_NAME.get(pName.toLowerCase(Locale.ROOT));
        }

        public String getName()
        {
            return this.name;
        }

        public boolean isVertical()
        {
            return this == Y;
        }

        public boolean isHorizontal()
        {
            return this == X || this == Z;
        }

        public String toString()
        {
            return this.name;
        }

        public static Direction.Axis getRandom(Random pRand)
        {
            return Util.m_137545_(VALUES, pRand);
        }

        public boolean test(@Nullable Direction p_122472_)
        {
            return p_122472_ != null && p_122472_.getAxis() == this;
        }

        public Direction.Plane getPlane()
        {
            switch (this)
            {
                case X:
                case Z:
                    return Direction.Plane.HORIZONTAL;

                case Y:
                    return Direction.Plane.VERTICAL;

                default:
                    throw new Error("Someone's been tampering with the universe!");
            }
        }

        public String getSerializedName()
        {
            return this.name;
        }

        public abstract int choose(int pX, int p_122467_, int pY);

        public abstract double choose(double pX, double p_122464_, double pY);
    }

    public static enum AxisDirection {
        POSITIVE(1, "Towards positive"),
        NEGATIVE(-1, "Towards negative");

        private final int step;
        private final String name;

        private AxisDirection(int p_122538_, String p_122539_)
        {
            this.step = p_122538_;
            this.name = p_122539_;
        }

        public int getStep()
        {
            return this.step;
        }

        public String getName()
        {
            return this.name;
        }

        public String toString()
        {
            return this.name;
        }

        public Direction.AxisDirection opposite()
        {
            return this == POSITIVE ? NEGATIVE : POSITIVE;
        }
    }

    public static enum Plane implements Iterable<Direction>, Predicate<Direction> {
        HORIZONTAL(new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}, new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}),
        VERTICAL(new Direction[]{Direction.UP, Direction.DOWN}, new Direction.Axis[]{Direction.Axis.Y});

        private final Direction[] faces;
        private final Direction.Axis[] axis;

        private Plane(Direction[] p_122555_, Direction.Axis[] p_122556_)
        {
            this.faces = p_122555_;
            this.axis = p_122556_;
        }

        public Direction getRandomDirection(Random pRand)
        {
            return Util.m_137545_(this.faces, pRand);
        }

        public Direction.Axis getRandomAxis(Random p_122563_)
        {
            return Util.m_137545_(this.axis, p_122563_);
        }

        public boolean test(@Nullable Direction p_122559_)
        {
            return p_122559_ != null && p_122559_.getAxis().getPlane() == this;
        }

        public Iterator<Direction> iterator()
        {
            return Iterators.forArray(this.faces);
        }

        public Stream<Direction> stream()
        {
            return Arrays.stream(this.faces);
        }
    }
}
