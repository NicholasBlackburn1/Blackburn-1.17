package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class EndGatewayPlacementDecorator extends VerticalDecorator<NoneDecoratorConfiguration> {
   public EndGatewayPlacementDecorator(Codec<NoneDecoratorConfiguration> p_70656_) {
      super(p_70656_);
   }

   protected int y(DecorationContext p_162175_, Random p_162176_, NoneDecoratorConfiguration p_162177_, int p_162178_) {
      return p_162178_ + 3 + p_162176_.nextInt(7);
   }
}