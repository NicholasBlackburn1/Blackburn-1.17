package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;

public class CountWithExtraChanceDecorator extends RepeatingDecorator<FrequencyWithExtraChanceDecoratorConfiguration> {
   public CountWithExtraChanceDecorator(Codec<FrequencyWithExtraChanceDecoratorConfiguration> p_70519_) {
      super(p_70519_);
   }

   protected int count(Random p_162159_, FrequencyWithExtraChanceDecoratorConfiguration p_162160_, BlockPos p_162161_) {
      return p_162160_.count + (p_162159_.nextFloat() < p_162160_.extraChance ? p_162160_.extraCount : 0);
   }
}