package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StationaryItemParticle extends TextureSheetParticle {
   StationaryItemParticle(ClientLevel p_172356_, double p_172357_, double p_172358_, double p_172359_, ItemLike p_172360_) {
      super(p_172356_, p_172357_, p_172358_, p_172359_);
      this.setSprite(Minecraft.getInstance().getItemRenderer().getItemModelShaper().getParticleIcon(p_172360_));
      this.gravity = 0.0F;
      this.lifetime = 80;
      this.hasPhysics = false;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.TERRAIN_SHEET;
   }

   public float getQuadSize(float p_172363_) {
      return 0.5F;
   }

   @OnlyIn(Dist.CLIENT)
   public static class BarrierProvider implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(SimpleParticleType p_172375_, ClientLevel p_172376_, double p_172377_, double p_172378_, double p_172379_, double p_172380_, double p_172381_, double p_172382_) {
         return new StationaryItemParticle(p_172376_, p_172377_, p_172378_, p_172379_, Blocks.BARRIER.asItem());
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class LightProvider implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(SimpleParticleType p_172394_, ClientLevel p_172395_, double p_172396_, double p_172397_, double p_172398_, double p_172399_, double p_172400_, double p_172401_) {
         return new StationaryItemParticle(p_172395_, p_172396_, p_172397_, p_172398_, Items.LIGHT);
      }
   }
}