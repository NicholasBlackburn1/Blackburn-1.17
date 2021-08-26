package net.minecraft.world.level.block;

import com.google.common.collect.Lists;
import com.mojang.math.OctahedralGroup;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.Direction;

public enum Rotation
{
    NONE(OctahedralGroup.IDENTITY),
    CLOCKWISE_90(OctahedralGroup.ROT_90_Y_NEG),
    CLOCKWISE_180(OctahedralGroup.ROT_180_FACE_XZ),
    COUNTERCLOCKWISE_90(OctahedralGroup.ROT_90_Y_POS);

    private final OctahedralGroup rotation;

    private Rotation(OctahedralGroup p_55947_)
    {
        this.rotation = p_55947_;
    }

    public Rotation getRotated(Rotation pRotation)
    {
        switch (pRotation)
        {
            case CLOCKWISE_180:
                switch (this)
                {
                    case NONE:
                        return CLOCKWISE_180;

                    case CLOCKWISE_90:
                        return COUNTERCLOCKWISE_90;

                    case CLOCKWISE_180:
                        return NONE;

                    case COUNTERCLOCKWISE_90:
                        return CLOCKWISE_90;
                }

            case COUNTERCLOCKWISE_90:
                switch (this)
                {
                    case NONE:
                        return COUNTERCLOCKWISE_90;

                    case CLOCKWISE_90:
                        return NONE;

                    case CLOCKWISE_180:
                        return CLOCKWISE_90;

                    case COUNTERCLOCKWISE_90:
                        return CLOCKWISE_180;
                }

            case CLOCKWISE_90:
                switch (this)
                {
                    case NONE:
                        return CLOCKWISE_90;

                    case CLOCKWISE_90:
                        return CLOCKWISE_180;

                    case CLOCKWISE_180:
                        return COUNTERCLOCKWISE_90;

                    case COUNTERCLOCKWISE_90:
                        return NONE;
                }

            default:
                return this;
        }
    }

    public OctahedralGroup rotation()
    {
        return this.rotation;
    }

    public Direction rotate(Direction pRotation)
    {
        if (pRotation.getAxis() == Direction.Axis.Y)
        {
            return pRotation;
        }
        else
        {
            switch (this)
            {
                case CLOCKWISE_90:
                    return pRotation.getClockWise();

                case CLOCKWISE_180:
                    return pRotation.getOpposite();

                case COUNTERCLOCKWISE_90:
                    return pRotation.getCounterClockWise();

                default:
                    return pRotation;
            }
        }
    }

    public int rotate(int pRotation, int pPositionCount)
    {
        switch (this)
        {
            case CLOCKWISE_90:
                return (pRotation + pPositionCount / 4) % pPositionCount;

            case CLOCKWISE_180:
                return (pRotation + pPositionCount / 2) % pPositionCount;

            case COUNTERCLOCKWISE_90:
                return (pRotation + pPositionCount * 3 / 4) % pPositionCount;

            default:
                return pRotation;
        }
    }

    public static Rotation getRandom(Random pRand)
    {
        return Util.m_137545_(values(), pRand);
    }

    public static List<Rotation> getShuffled(Random pRand)
    {
        List<Rotation> list = Lists.newArrayList(values());
        Collections.shuffle(list, pRand);
        return list;
    }
}
