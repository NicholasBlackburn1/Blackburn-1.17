package net.minecraft.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

public interface ParticleRenderType
{
    ParticleRenderType TERRAIN_SHEET = new ParticleRenderType()
    {
        public void begin(BufferBuilder pBufferBuilder, TextureManager pTextureManager)
        {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
            pBufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }
        public void end(Tesselator pTesselator)
        {
            pTesselator.end();
        }
        public String toString()
        {
            return "TERRAIN_SHEET";
        }
    };
    ParticleRenderType PARTICLE_SHEET_OPAQUE = new ParticleRenderType()
    {
        public void begin(BufferBuilder pBufferBuilder, TextureManager pTextureManager)
        {
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            pBufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }
        public void end(Tesselator pTesselator)
        {
            pTesselator.end();
        }
        public String toString()
        {
            return "PARTICLE_SHEET_OPAQUE";
        }
    };
    ParticleRenderType PARTICLE_SHEET_TRANSLUCENT = new ParticleRenderType()
    {
        public void begin(BufferBuilder pBufferBuilder, TextureManager pTextureManager)
        {
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            pBufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }
        public void end(Tesselator pTesselator)
        {
            pTesselator.end();
        }
        public String toString()
        {
            return "PARTICLE_SHEET_TRANSLUCENT";
        }
    };
    ParticleRenderType PARTICLE_SHEET_LIT = new ParticleRenderType()
    {
        public void begin(BufferBuilder pBufferBuilder, TextureManager pTextureManager)
        {
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            pBufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }
        public void end(Tesselator pTesselator)
        {
            pTesselator.end();
        }
        public String toString()
        {
            return "PARTICLE_SHEET_LIT";
        }
    };
    ParticleRenderType CUSTOM = new ParticleRenderType()
    {
        public void begin(BufferBuilder pBufferBuilder, TextureManager pTextureManager)
        {
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
        }
        public void end(Tesselator pTesselator)
        {
        }
        public String toString()
        {
            return "CUSTOM";
        }
    };
    ParticleRenderType NO_RENDER = new ParticleRenderType()
    {
        public void begin(BufferBuilder pBufferBuilder, TextureManager pTextureManager)
        {
        }
        public void end(Tesselator pTesselator)
        {
        }
        public String toString()
        {
            return "NO_RENDER";
        }
    };

    void begin(BufferBuilder pBufferBuilder, TextureManager pTextureManager);

    void end(Tesselator pTesselator);
}
