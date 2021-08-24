package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.layer.LayerBiomes;

public interface DimensionTransformer extends LayerBiomes {
   int getParentX(int p_77073_);

   int getParentY(int p_77074_);
}