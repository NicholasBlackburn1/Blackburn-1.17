package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.optifine.Config;
import net.optifine.render.MultiTextureData;
import net.optifine.render.MultiTextureRenderer;
import net.optifine.render.VboRange;
import net.optifine.render.VboRegion;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersRender;

public class VertexBuffer implements AutoCloseable
{
    private int vertextBufferId;
    private int indexBufferId;
    private VertexFormat.IndexType indexType;
    private int arrayObjectId;
    private int indexCount;
    private VertexFormat.Mode mode;
    private boolean sequentialIndices;
    private VertexFormat format;
    private VboRegion vboRegion;
    private VboRange vboRange;
    private MultiTextureData multiTextureData;

    public VertexBuffer()
    {
        RenderSystem.glGenBuffers((idIn) ->
        {
            this.vertextBufferId = idIn;
        });
        RenderSystem.glGenVertexArrays((idIn) ->
        {
            this.arrayObjectId = idIn;
        });
        RenderSystem.glGenBuffers((idIn) ->
        {
            this.indexBufferId = idIn;
        });
    }

    public void bind()
    {
        GlStateManager._glBindBuffer(34962, this.vertextBufferId);

        if (this.sequentialIndices)
        {
            RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = RenderSystem.getSequentialBuffer(this.mode, this.indexCount);
            this.indexType = rendersystem$autostorageindexbuffer.type();
            GlStateManager._glBindBuffer(34963, rendersystem$autostorageindexbuffer.name());
        }
        else
        {
            GlStateManager._glBindBuffer(34963, this.indexBufferId);
        }
    }

    public void upload(BufferBuilder pBuffer)
    {
        if (!RenderSystem.isOnRenderThread())
        {
            RenderSystem.recordRenderCall(() ->
            {
                this.upload_(pBuffer);
            });
        }
        else
        {
            this.upload_(pBuffer);
        }
    }

    public CompletableFuture<Void> uploadLater(BufferBuilder pBuffer)
    {
        if (!RenderSystem.isOnRenderThread())
        {
            return CompletableFuture.runAsync(() ->
            {
                this.upload_(pBuffer);
            }, (runnableIn) ->
            {
                RenderSystem.recordRenderCall(runnableIn::run);
            });
        }
        else
        {
            this.upload_(pBuffer);
            return CompletableFuture.completedFuture((Void)null);
        }
    }

    private void upload_(BufferBuilder pBuffer)
    {
        Pair<BufferBuilder.DrawState, ByteBuffer> pair = pBuffer.popNextBuffer();
        BufferBuilder.DrawState bufferbuilder$drawstate = pair.getFirst();

        if (this.vboRegion != null)
        {
            ByteBuffer bytebuffer1 = pair.getSecond();
            ((Buffer)bytebuffer1).position(0);
            ((Buffer)bytebuffer1).limit(bufferbuilder$drawstate.vertexBufferSize());
            this.vboRegion.bufferData(bytebuffer1, this.vboRange);
            ((Buffer)bytebuffer1).position(0);
            ((Buffer)bytebuffer1).limit(bufferbuilder$drawstate.bufferSize());
        }
        else
        {
            this.multiTextureData = bufferbuilder$drawstate.getMultiTextureData();

            if (this.vertextBufferId != 0)
            {
                BufferUploader.reset();
                BufferBuilder.DrawState bufferbuilder$drawstate1 = pair.getFirst();
                ByteBuffer bytebuffer = pair.getSecond();
                int i = bufferbuilder$drawstate1.vertexBufferSize();
                this.indexCount = bufferbuilder$drawstate1.indexCount();
                this.indexType = bufferbuilder$drawstate1.indexType();
                this.format = bufferbuilder$drawstate1.format();
                this.mode = bufferbuilder$drawstate1.mode();
                this.sequentialIndices = bufferbuilder$drawstate1.sequentialIndex();
                this.bindVertexArray();
                this.bind();

                if (!bufferbuilder$drawstate1.indexOnly())
                {
                    ((Buffer)bytebuffer).limit(i);
                    RenderSystem.glBufferData(34962, bytebuffer, 35044);
                    ((Buffer)bytebuffer).position(i);
                }

                if (!this.sequentialIndices)
                {
                    ((Buffer)bytebuffer).limit(bufferbuilder$drawstate1.bufferSize());
                    RenderSystem.glBufferData(34963, bytebuffer, 35044);
                    ((Buffer)bytebuffer).position(0);
                }
                else
                {
                    ((Buffer)bytebuffer).limit(bufferbuilder$drawstate1.bufferSize());
                    ((Buffer)bytebuffer).position(0);
                }

                unbind();
                unbindVertexArray();
            }
        }
    }

    private void bindVertexArray()
    {
        GlStateManager._glBindVertexArray(this.arrayObjectId);
    }

    public static void unbindVertexArray()
    {
        GlStateManager._glBindVertexArray(0);
    }

    public void draw()
    {
        if (this.vboRegion != null)
        {
            this.vboRegion.drawArrays(VertexFormat.Mode.QUADS, this.vboRange);
        }
        else if (this.multiTextureData != null)
        {
            MultiTextureRenderer.draw(this.mode, this.indexType.asGLType, this.multiTextureData);
        }
        else if (this.indexCount != 0)
        {
            RenderSystem.drawElements(this.mode.asGLMode, this.indexCount, this.indexType.asGLType);
        }
    }

    public void drawWithShader(Matrix4f p_166868_, Matrix4f p_166869_, ShaderInstance p_166870_)
    {
        if (!RenderSystem.isOnRenderThread())
        {
            RenderSystem.recordRenderCall(() ->
            {
                this._drawWithShader(p_166868_.copy(), p_166869_.copy(), p_166870_);
            });
        }
        else
        {
            this._drawWithShader(p_166868_, p_166869_, p_166870_);
        }
    }

    public void _drawWithShader(Matrix4f p_166877_, Matrix4f p_166878_, ShaderInstance p_166879_)
    {
        if (this.indexCount != 0)
        {
            RenderSystem.assertThread(RenderSystem::isOnRenderThread);
            BufferUploader.reset();

            for (int i = 0; i < 12; ++i)
            {
                int j = RenderSystem.getShaderTexture(i);
                p_166879_.setSampler(i, j);
            }

            if (p_166879_.MODEL_VIEW_MATRIX != null)
            {
                p_166879_.MODEL_VIEW_MATRIX.set(p_166877_);
            }

            if (p_166879_.PROJECTION_MATRIX != null)
            {
                p_166879_.PROJECTION_MATRIX.set(p_166878_);
            }

            if (p_166879_.COLOR_MODULATOR != null)
            {
                p_166879_.COLOR_MODULATOR.m_5941_(RenderSystem.getShaderColor());
            }

            if (p_166879_.FOG_START != null)
            {
                p_166879_.FOG_START.set(RenderSystem.getShaderFogStart());
            }

            if (p_166879_.FOG_END != null)
            {
                p_166879_.FOG_END.set(RenderSystem.getShaderFogEnd());
            }

            if (p_166879_.FOG_COLOR != null)
            {
                p_166879_.FOG_COLOR.m_5941_(RenderSystem.getShaderFogColor());
            }

            if (p_166879_.TEXTURE_MATRIX != null)
            {
                p_166879_.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
            }

            if (p_166879_.GAME_TIME != null)
            {
                p_166879_.GAME_TIME.set(RenderSystem.getShaderGameTime());
            }

            if (p_166879_.SCREEN_SIZE != null)
            {
                Window window = Minecraft.getInstance().getWindow();
                p_166879_.SCREEN_SIZE.set((float)window.getWidth(), (float)window.getHeight());
            }

            if (p_166879_.LINE_WIDTH != null && (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP))
            {
                p_166879_.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
            }

            if (Config.isShaders())
            {
                Shaders.setModelViewMatrix(p_166877_);
                Shaders.setProjectionMatrix(p_166878_);
                Shaders.setTextureMatrix(RenderSystem.getTextureMatrix());
                Shaders.setColorModulator(RenderSystem.getShaderColor());
            }

            RenderSystem.setupShaderLights(p_166879_);
            this.bindVertexArray();
            this.bind();
            this.getFormat().setupBufferState();
            p_166879_.apply();

            if (Config.isShaders())
            {
                ShadersRender.setupArrayPointersVbo();
            }

            this.draw();
            p_166879_.clear();
            this.getFormat().clearBufferState();
            unbind();
            unbindVertexArray();
        }
    }

    public void drawChunkLayer()
    {
        if (this.indexCount != 0)
        {
            RenderSystem.assertThread(RenderSystem::isOnRenderThread);
            this.bindVertexArray();
            this.bind();
            this.format.setupBufferState();

            if (Config.isShaders())
            {
                ShadersRender.setupArrayPointersVbo();
            }

            this.draw();
        }
    }

    public static void unbind()
    {
        GlStateManager._glBindBuffer(34962, 0);
        GlStateManager._glBindBuffer(34963, 0);
    }

    public void close()
    {
        if (this.indexBufferId >= 0)
        {
            RenderSystem.glDeleteBuffers(this.indexBufferId);
            this.indexBufferId = -1;
        }

        if (this.vertextBufferId > 0)
        {
            RenderSystem.glDeleteBuffers(this.vertextBufferId);
            this.vertextBufferId = 0;
        }

        if (this.arrayObjectId > 0)
        {
            RenderSystem.glDeleteVertexArrays(this.arrayObjectId);
            this.arrayObjectId = 0;
        }
    }

    public VertexFormat getFormat()
    {
        return this.format;
    }

    public void setVboRegion(VboRegion vboRegion)
    {
        if (vboRegion != null)
        {
            this.close();
            this.vboRegion = vboRegion;
            this.vboRange = new VboRange();
        }
    }

    public VboRegion getVboRegion()
    {
        return this.vboRegion;
    }
}
