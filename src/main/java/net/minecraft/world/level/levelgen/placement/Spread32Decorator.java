package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class Spread32Decorator extends VerticalDecorator<NoneDecoratorConfiguration> {
   public Spread32Decorator(Codec<NoneDecoratorConfiguration> p_70854_) {
      super(p_70854_);
   }

   protected int y(DecorationContext p_162308_, Random p_162309_, NoneDecoratorConfiguration p_162310_, int p_162311_) {
      return p_162309_.nextInt(Math.max(p_162311_, 0) + 32);
   }
}