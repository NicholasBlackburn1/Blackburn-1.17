package net.minecraft.world.level.levelgen;

import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.TrapezoidHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public interface Decoratable<R>
{
    R decorated(ConfiguredDecorator<?> pDecorator);

default R rarity(int p_158242_)
    {
        return this.decorated(FeatureDecorator.CHANCE.configured(new ChanceDecoratorConfiguration(p_158242_)));
    }

default R count(IntProvider pAmount)
    {
        return this.decorated(FeatureDecorator.COUNT.configured(new CountConfiguration(pAmount)));
    }

default R count(int pAmount)
    {
        return this.count(ConstantInt.of(pAmount));
    }

default R countRandom(int pMax)
    {
        return this.count(UniformInt.of(0, pMax));
    }

default R rangeUniform(VerticalAnchor p_158246_, VerticalAnchor p_158247_)
    {
        return this.range(new RangeDecoratorConfiguration(UniformHeight.of(p_158246_, p_158247_)));
    }

default R rangeTriangle(VerticalAnchor p_158251_, VerticalAnchor p_158252_)
    {
        return this.range(new RangeDecoratorConfiguration(TrapezoidHeight.of(p_158251_, p_158252_)));
    }

default R range(RangeDecoratorConfiguration p_158249_)
    {
        return this.decorated(FeatureDecorator.RANGE.configured(p_158249_));
    }

default R squared()
    {
        return this.decorated(FeatureDecorator.SQUARE.configured(NoneDecoratorConfiguration.INSTANCE));
    }
}
