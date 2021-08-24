package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;

public class LakeLavaPlacementDecorator extends RepeatingDecorator<ChanceDecoratorConfiguration> {
   public LakeLavaPlacementDecorator(Codec<ChanceDecoratorConfiguration> p_70770_) {
      super(p_70770_);
   }

   protected int count(Random p_162252_, ChanceDecoratorConfiguration p_162253_, BlockPos p_162254_) {
      return p_162254_.getY() >= 63 && p_162252_.nextInt(10) != 0 ? 0 : 1;
   }
}