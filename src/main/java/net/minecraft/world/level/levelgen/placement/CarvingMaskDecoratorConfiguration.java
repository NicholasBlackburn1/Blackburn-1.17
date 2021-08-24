package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class CarvingMaskDecoratorConfiguration implements DecoratorConfiguration {
   public static final Codec<CarvingMaskDecoratorConfiguration> CODEC = GenerationStep.Carving.CODEC.fieldOf("step").xmap(CarvingMaskDecoratorConfiguration::new, (p_162078_) -> {
      return p_162078_.step;
   }).codec();
   protected final GenerationStep.Carving step;

   public CarvingMaskDecoratorConfiguration(GenerationStep.Carving p_162076_) {
      this.step = p_162076_;
   }
}