package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;

public class RangeDecorator extends VerticalDecorator<RangeDecoratorConfiguration> {
   public RangeDecorator(Codec<RangeDecoratorConfiguration> p_70834_) {
      super(p_70834_);
   }

   protected int y(DecorationContext p_162285_, Random p_162286_, RangeDecoratorConfiguration p_162287_, int p_162288_) {
      return p_162287_.height.sample(p_162286_, p_162285_);
   }
}