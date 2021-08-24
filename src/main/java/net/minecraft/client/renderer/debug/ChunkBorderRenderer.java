package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChunkBorderRenderer implements DebugRenderer.SimpleDebugRenderer {
   private final Minecraft minecraft;

   public ChunkBorderRenderer(Minecraft p_113356_) {
      this.minecraft = p_113356_;
   }

   public void render(PoseStack p_113358_, MultiBufferSource p_113359_, double p_113360_, double p_113361_, double p_113362_) {
      RenderSystem.enableDepthTest();
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      Entity entity = this.minecraft.gameRenderer.getMainCamera().getEntity();
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      double d0 = (double)this.minecraft.level.getMinBuildHeight() - p_113361_;
      double d1 = (double)this.minecraft.level.getMaxBuildHeight() - p_113361_;
      RenderSystem.disableTexture();
      RenderSystem.disableBlend();
      ChunkPos chunkpos = entity.chunkPosition();
      double d2 = (double)chunkpos.getMinBlockX() - p_113360_;
      double d3 = (double)chunkpos.getMinBlockZ() - p_113362_;
      RenderSystem.lineWidth(1.0F);
      bufferbuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

      for(int i = -16; i <= 32; i += 16) {
         for(int j = -16; j <= 32; j += 16) {
            bufferbuilder.vertex(d2 + (double)i, d0, d3 + (double)j).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
            bufferbuilder.vertex(d2 + (double)i, d0, d3 + (double)j).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
            bufferbuilder.vertex(d2 + (double)i, d1, d3 + (double)j).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
            bufferbuilder.vertex(d2 + (double)i, d1, d3 + (double)j).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
         }
      }

      for(int k = 2; k < 16; k += 2) {
         bufferbuilder.vertex(d2 + (double)k, d0, d3).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         bufferbuilder.vertex(d2 + (double)k, d0, d3).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         bufferbuilder.vertex(d2 + (double)k, d1, d3).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         bufferbuilder.vertex(d2 + (double)k, d1, d3).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         bufferbuilder.vertex(d2 + (double)k, d0, d3 + 16.0D).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         bufferbuilder.vertex(d2 + (double)k, d0, d3 + 16.0D).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         bufferbuilder.vertex(d2 + (double)k, d1, d3 + 16.0D).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         bufferbuilder.vertex(d2 + (double)k, d1, d3 + 16.0D).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
      }

      for(int l = 2; l < 16; l += 2) {
         bufferbuilder.vertex(d2, d0, d3 + (double)l).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         bufferbuilder.vertex(d2, d0, d3 + (double)l).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         bufferbuilder.vertex(d2, d1, d3 + (double)l).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         bufferbuilder.vertex(d2, d1, d3 + (double)l).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         bufferbuilder.vertex(d2 + 16.0D, d0, d3 + (double)l).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         bufferbuilder.vertex(d2 + 16.0D, d0, d3 + (double)l).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         bufferbuilder.vertex(d2 + 16.0D, d1, d3 + (double)l).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         bufferbuilder.vertex(d2 + 16.0D, d1, d3 + (double)l).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
      }

      for(int i1 = this.minecraft.level.getMinBuildHeight(); i1 <= this.minecraft.level.getMaxBuildHeight(); i1 += 2) {
         double d4 = (double)i1 - p_113361_;
         bufferbuilder.vertex(d2, d4, d3).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         bufferbuilder.vertex(d2, d4, d3).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         bufferbuilder.vertex(d2, d4, d3 + 16.0D).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         bufferbuilder.vertex(d2 + 16.0D, d4, d3 + 16.0D).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         bufferbuilder.vertex(d2 + 16.0D, d4, d3).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         bufferbuilder.vertex(d2, d4, d3).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         bufferbuilder.vertex(d2, d4, d3).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
      }

      tesselator.end();
      RenderSystem.lineWidth(2.0F);
      bufferbuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

      for(int j1 = 0; j1 <= 16; j1 += 16) {
         for(int l1 = 0; l1 <= 16; l1 += 16) {
            bufferbuilder.vertex(d2 + (double)j1, d0, d3 + (double)l1).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
            bufferbuilder.vertex(d2 + (double)j1, d0, d3 + (double)l1).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            bufferbuilder.vertex(d2 + (double)j1, d1, d3 + (double)l1).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            bufferbuilder.vertex(d2 + (double)j1, d1, d3 + (double)l1).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
         }
      }

      for(int k1 = this.minecraft.level.getMinBuildHeight(); k1 <= this.minecraft.level.getMaxBuildHeight(); k1 += 16) {
         double d5 = (double)k1 - p_113361_;
         bufferbuilder.vertex(d2, d5, d3).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
         bufferbuilder.vertex(d2, d5, d3).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
         bufferbuilder.vertex(d2, d5, d3 + 16.0D).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
         bufferbuilder.vertex(d2 + 16.0D, d5, d3 + 16.0D).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
         bufferbuilder.vertex(d2 + 16.0D, d5, d3).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
         bufferbuilder.vertex(d2, d5, d3).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
         bufferbuilder.vertex(d2, d5, d3).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
      }

      tesselator.end();
      RenderSystem.lineWidth(1.0F);
      RenderSystem.enableBlend();
      RenderSystem.enableTexture();
   }
}