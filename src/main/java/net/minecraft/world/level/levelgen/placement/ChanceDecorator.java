package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;

public class ChanceDecorator extends RepeatingDecorator<ChanceDecoratorConfiguration> {
   public ChanceDecorator(Codec<ChanceDecoratorConfiguration> p_70455_) {
      super(p_70455_);
   }

   protected int count(Random p_162135_, ChanceDecoratorConfiguration p_162136_, BlockPos p_162137_) {
      return p_162135_.nextFloat() < 1.0F / (float)p_162136_.chance ? 1 : 0;
   }
}