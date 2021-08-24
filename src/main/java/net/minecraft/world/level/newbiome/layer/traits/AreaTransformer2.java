package net.minecraft.world.level.newbiome.layer.traits;

import net.minecraft.world.level.newbiome.area.Area;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.context.BigContext;
import net.minecraft.world.level.newbiome.context.Context;

public interface AreaTransformer2 extends DimensionTransformer {
   default <R extends Area> AreaFactory<R> run(BigContext<R> p_77021_, AreaFactory<R> p_77022_, AreaFactory<R> p_77023_) {
      return () -> {
         R r = p_77022_.make();
         R r1 = p_77023_.make();
         return p_77021_.createResult((p_164653_, p_164654_) -> {
            p_77021_.initRandom((long)p_164653_, (long)p_164654_);
            return this.applyPixel(p_77021_, r, r1, p_164653_, p_164654_);
         }, r, r1);
      };
   }

   int applyPixel(Context p_77024_, Area p_77025_, Area p_77026_, int p_77027_, int p_77028_);
}