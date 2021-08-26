package net.minecraft.core;

public enum AxisCycle
{
    NONE {
        public int cycle(int pX, int pY, int pZ, Direction.Axis pAxis)
        {
            return pAxis.choose(pX, pY, pZ);
        }

        public double cycle(double pX, double pY, double pZ, Direction.Axis pAxis)
        {
            return pAxis.choose(pX, pY, pZ);
        }

        public Direction.Axis cycle(Direction.Axis pX)
        {
            return pX;
        }

        public AxisCycle inverse()
        {
            return this;
        }
    },
    FORWARD {
        public int cycle(int pX, int pY, int pZ, Direction.Axis pAxis)
        {
            return pAxis.choose(pZ, pX, pY);
        }

        public double cycle(double pX, double pY, double pZ, Direction.Axis pAxis)
        {
            return pAxis.choose(pZ, pX, pY);
        }

        public Direction.Axis cycle(Direction.Axis pX)
        {
            return AXIS_VALUES[Math.floorMod(pX.ordinal() + 1, 3)];
        }

        public AxisCycle inverse()
        {
            return BACKWARD;
        }
    },
    BACKWARD {
        public int cycle(int pX, int pY, int pZ, Direction.Axis pAxis)
        {
            return pAxis.choose(pY, pZ, pX);
        }

        public double cycle(double pX, double pY, double pZ, Direction.Axis pAxis)
        {
            return pAxis.choose(pY, pZ, pX);
        }

        public Direction.Axis cycle(Direction.Axis pX)
        {
            return AXIS_VALUES[Math.floorMod(pX.ordinal() - 1, 3)];
        }

        public AxisCycle inverse()
        {
            return FORWARD;
        }
    };

    public static final Direction.Axis[] AXIS_VALUES = Direction.Axis.values();
    public static final AxisCycle[] VALUES = values();

    public abstract int cycle(int pX, int pY, int pZ, Direction.Axis pAxis);

    public abstract double cycle(double pX, double pY, double pZ, Direction.Axis pAxis);

    public abstract Direction.Axis cycle(Direction.Axis pX);

    public abstract AxisCycle inverse();

    public static AxisCycle between(Direction.Axis pAxis1, Direction.Axis pAxis2)
    {
        return VALUES[Math.floorMod(pAxis2.ordinal() - pAxis1.ordinal(), 3)];
    }
}
