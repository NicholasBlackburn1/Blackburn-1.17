package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;

public class SurfaceBuilderBaseConfiguration implements SurfaceBuilderConfiguration {
   public static final Codec<SurfaceBuilderBaseConfiguration> CODEC = RecordCodecBuilder.create((p_75252_) -> {
      return p_75252_.group(BlockState.CODEC.fieldOf("top_material").forGetter((p_164231_) -> {
         return p_164231_.topMaterial;
      }), BlockState.CODEC.fieldOf("under_material").forGetter((p_164229_) -> {
         return p_164229_.underMaterial;
      }), BlockState.CODEC.fieldOf("underwater_material").forGetter((p_164227_) -> {
         return p_164227_.underwaterMaterial;
      })).apply(p_75252_, SurfaceBuilderBaseConfiguration::new);
   });
   private final BlockState topMaterial;
   private final BlockState underMaterial;
   private final BlockState underwaterMaterial;

   public SurfaceBuilderBaseConfiguration(BlockState p_75247_, BlockState p_75248_, BlockState p_75249_) {
      this.topMaterial = p_75247_;
      this.underMaterial = p_75248_;
      this.underwaterMaterial = p_75249_;
   }

   public BlockState getTopMaterial() {
      return this.topMaterial;
   }

   public BlockState getUnderMaterial() {
      return this.underMaterial;
   }

   public BlockState getUnderwaterMaterial() {
      return this.underwaterMaterial;
   }
}