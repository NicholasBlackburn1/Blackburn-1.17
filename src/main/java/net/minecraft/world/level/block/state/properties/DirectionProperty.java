package net.minecraft.world.level.block.state.properties;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.Direction;

public class DirectionProperty extends EnumProperty<Direction>
{
    protected DirectionProperty(String p_61541_, Collection<Direction> p_61542_)
    {
        super(p_61541_, Direction.class, p_61542_);
    }

    public static DirectionProperty create(String pName)
    {
        return create(pName, Predicates.alwaysTrue());
    }

    public static DirectionProperty create(String pName, Predicate<Direction> pValues)
    {
        return create(pName, Arrays.stream(Direction.values()).filter(pValues).collect(Collectors.toList()));
    }

    public static DirectionProperty m_61549_(String p_61550_, Direction... p_61551_)
    {
        return create(p_61550_, Lists.newArrayList(p_61551_));
    }

    public static DirectionProperty create(String pName, Collection<Direction> pValues)
    {
        return new DirectionProperty(pName, pValues);
    }
}
