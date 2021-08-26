package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.Direction;

public class SliceShape extends VoxelShape
{
    private final VoxelShape delegate;
    private final Direction.Axis axis;
    private static final DoubleList SLICE_COORDS = new CubePointRange(1);

    public SliceShape(VoxelShape p_83173_, Direction.Axis p_83174_, int p_83175_)
    {
        super(makeSlice(p_83173_.shape, p_83174_, p_83175_));
        this.delegate = p_83173_;
        this.axis = p_83174_;
    }

    private static DiscreteVoxelShape makeSlice(DiscreteVoxelShape pShapePart, Direction.Axis pAxis, int p_83179_)
    {
        return new SubShape(pShapePart, pAxis.choose(p_83179_, 0, 0), pAxis.choose(0, p_83179_, 0), pAxis.choose(0, 0, p_83179_), pAxis.choose(p_83179_ + 1, pShapePart.xSize, pShapePart.xSize), pAxis.choose(pShapePart.ySize, p_83179_ + 1, pShapePart.ySize), pAxis.choose(pShapePart.zSize, pShapePart.zSize, p_83179_ + 1));
    }

    protected DoubleList getCoords(Direction.Axis pAxis)
    {
        return pAxis == this.axis ? SLICE_COORDS : this.delegate.getCoords(pAxis);
    }
}
