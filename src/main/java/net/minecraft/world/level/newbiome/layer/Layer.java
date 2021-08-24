package net.minecraft.world.level.newbiome.layer;

import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.biome.Biomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.newbiome.area.AreaFactory;
import net.minecraft.world.level.newbiome.area.LazyArea;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Layer {
   private static final Logger LOGGER = LogManager.getLogger();
   private final LazyArea area;

   public Layer(AreaFactory<LazyArea> p_76714_) {
      this.area = p_76714_.make();
   }

   public Biome get(Registry<Biome> p_76716_, int p_76717_, int p_76718_) {
      int i = this.area.get(p_76717_, p_76718_);
      ResourceKey<Biome> resourcekey = Biomes.byId(i);
      if (resourcekey == null) {
         throw new IllegalStateException("Unknown biome id emitted by layers: " + i);
      } else {
         Biome biome = p_76716_.get(resourcekey);
         if (biome == null) {
            Util.logAndPauseIfInIde("Unknown biome id: " + i);
            return p_76716_.get(Biomes.byId(0));
         } else {
            return biome;
         }
      }
   }
}