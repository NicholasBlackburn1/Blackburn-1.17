package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.optifine.Config;
import net.optifine.render.MultiTextureData;
import net.optifine.render.MultiTextureRenderer;
import net.optifine.shaders.SVertexBuilder;
import net.optifine.shaders.Shaders;

public class BufferUploader
{
    private static int lastVertexArrayObject;
    private static int lastVertexBufferObject;
    private static int lastIndexBufferObject;
    @Nullable
    private static VertexFormat lastFormat;

    public static void reset()
    {
        if (lastFormat != null)
        {
            lastFormat.clearBufferState();
            lastFormat = null;
        }

        GlStateManager._glBindBuffer(34963, 0);
        lastIndexBufferObject = 0;
        GlStateManager._glBindBuffer(34962, 0);
        lastVertexBufferObject = 0;
        GlStateManager._glBindVertexArray(0);
        lastVertexArrayObject = 0;
    }

    public static void invalidateElementArrayBufferBinding()
    {
        GlStateManager._glBindBuffer(34963, 0);
        lastIndexBufferObject = 0;
    }

    public static void end(BufferBuilder pBufferBuilder)
    {
        if (!RenderSystem.isOnRenderThreadOrInit())
        {
            RenderSystem.recordRenderCall(() ->
            {
                Pair<BufferBuilder.DrawState, ByteBuffer> pair1 = pBufferBuilder.popNextBuffer();
                BufferBuilder.DrawState bufferbuilder$drawstate1 = pair1.getFirst();
                draw(pair1.getSecond(), bufferbuilder$drawstate1.mode(), bufferbuilder$drawstate1.format(), bufferbuilder$drawstate1.vertexCount(), bufferbuilder$drawstate1.indexType(), bufferbuilder$drawstate1.indexCount(), bufferbuilder$drawstate1.sequentialIndex(), bufferbuilder$drawstate1.getMultiTextureData());
            });
        }
        else
        {
            Pair<BufferBuilder.DrawState, ByteBuffer> pair = pBufferBuilder.popNextBuffer();
            BufferBuilder.DrawState bufferbuilder$drawstate = pair.getFirst();
            draw(pair.getSecond(), bufferbuilder$drawstate.mode(), bufferbuilder$drawstate.format(), bufferbuilder$drawstate.vertexCount(), bufferbuilder$drawstate.indexType(), bufferbuilder$drawstate.indexCount(), bufferbuilder$drawstate.sequentialIndex(), bufferbuilder$drawstate.getMultiTextureData());
        }
    }

    private static void _end(ByteBuffer p_166839_, VertexFormat.Mode p_166840_, VertexFormat p_166841_, int p_166842_, VertexFormat.IndexType p_166843_, int p_166844_, boolean p_166845_)
    {
        draw(p_166839_, p_166840_, p_166841_, p_166842_, p_166843_, p_166844_, p_166845_, (MultiTextureData)null);
    }

    private static void draw(ByteBuffer byteBufferIn, VertexFormat.Mode modeIn, VertexFormat formatIn, int vertexCountIn, VertexFormat.IndexType indexTypeIn, int indexCountIn, boolean sequentialIndexIn, MultiTextureData multiTextureData)
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        ((Buffer)byteBufferIn).clear();

        if (vertexCountIn > 0)
        {
            int i = vertexCountIn * formatIn.getVertexSize();
            updateVertexSetup(formatIn);
            ((Buffer)byteBufferIn).position(0);
            ((Buffer)byteBufferIn).limit(i);
            GlStateManager._glBufferData(34962, byteBufferIn, 35048);
            int j;

            if (sequentialIndexIn)
            {
                RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = RenderSystem.getSequentialBuffer(modeIn, indexCountIn);
                int k = rendersystem$autostorageindexbuffer.name();

                if (k != lastIndexBufferObject)
                {
                    GlStateManager._glBindBuffer(34963, k);
                    lastIndexBufferObject = k;
                }

                j = rendersystem$autostorageindexbuffer.type().asGLType;
            }
            else
            {
                int i1 = formatIn.getOrCreateIndexBufferObject();

                if (i1 != lastIndexBufferObject)
                {
                    GlStateManager._glBindBuffer(34963, i1);
                    lastIndexBufferObject = i1;
                }

                ((Buffer)byteBufferIn).position(i);
                ((Buffer)byteBufferIn).limit(i + indexCountIn * indexTypeIn.bytes);
                GlStateManager._glBufferData(34963, byteBufferIn, 35048);
                j = indexTypeIn.asGLType;
            }

            ShaderInstance shaderinstance = RenderSystem.getShader();

            for (int j1 = 0; j1 < 8; ++j1)
            {
                int l = RenderSystem.getShaderTexture(j1);
                shaderinstance.setSampler(j1, l);
            }

            if (shaderinstance.MODEL_VIEW_MATRIX != null)
            {
                shaderinstance.MODEL_VIEW_MATRIX.set(RenderSystem.getModelViewMatrix());
            }

            if (shaderinstance.PROJECTION_MATRIX != null)
            {
                shaderinstance.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
            }

            if (shaderinstance.COLOR_MODULATOR != null)
            {
                shaderinstance.COLOR_MODULATOR.m_5941_(RenderSystem.getShaderColor());
            }

            if (shaderinstance.FOG_START != null)
            {
                shaderinstance.FOG_START.set(RenderSystem.getShaderFogStart());
            }

            if (shaderinstance.FOG_END != null)
            {
                shaderinstance.FOG_END.set(RenderSystem.getShaderFogEnd());
            }

            if (shaderinstance.FOG_COLOR != null)
            {
                shaderinstance.FOG_COLOR.m_5941_(RenderSystem.getShaderFogColor());
            }

            if (shaderinstance.TEXTURE_MATRIX != null)
            {
                shaderinstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
            }

            if (shaderinstance.GAME_TIME != null)
            {
                shaderinstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
            }

            if (shaderinstance.SCREEN_SIZE != null)
            {
                Window window = Minecraft.getInstance().getWindow();
                shaderinstance.SCREEN_SIZE.set((float)window.getWidth(), (float)window.getHeight());
            }

            if (shaderinstance.LINE_WIDTH != null && (modeIn == VertexFormat.Mode.LINES || modeIn == VertexFormat.Mode.LINE_STRIP))
            {
                shaderinstance.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
            }

            RenderSystem.setupShaderLights(shaderinstance);
            shaderinstance.apply();
            boolean flag = Config.isShaders() && Shaders.isRenderingWorld;
            boolean flag1 = flag && SVertexBuilder.preDrawArrays(formatIn);

            if (flag)
            {
                Shaders.setModelViewMatrix(RenderSystem.getModelViewMatrix());
                Shaders.setProjectionMatrix(RenderSystem.getProjectionMatrix());
                Shaders.setTextureMatrix(RenderSystem.getTextureMatrix());
                Shaders.setColorModulator(RenderSystem.getShaderColor());
            }

            if (multiTextureData != null)
            {
                MultiTextureRenderer.draw(modeIn, j, multiTextureData);
            }
            else
            {
                GlStateManager._drawElements(modeIn.asGLMode, indexCountIn, j, 0L);
            }

            if (flag1)
            {
                SVertexBuilder.postDrawArrays();
            }

            shaderinstance.clear();
            ((Buffer)byteBufferIn).position(0);
        }
    }

    public static void _endInternal(BufferBuilder p_166848_)
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        Pair<BufferBuilder.DrawState, ByteBuffer> pair = p_166848_.popNextBuffer();
        BufferBuilder.DrawState bufferbuilder$drawstate = pair.getFirst();
        ByteBuffer bytebuffer = pair.getSecond();
        VertexFormat vertexformat = bufferbuilder$drawstate.format();
        int i = bufferbuilder$drawstate.vertexCount();
        ((Buffer)bytebuffer).clear();

        if (i > 0)
        {
            int j = i * vertexformat.getVertexSize();
            updateVertexSetup(vertexformat);
            ((Buffer)bytebuffer).position(0);
            ((Buffer)bytebuffer).limit(j);
            GlStateManager._glBufferData(34962, bytebuffer, 35048);
            RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = RenderSystem.getSequentialBuffer(bufferbuilder$drawstate.mode(), bufferbuilder$drawstate.indexCount());
            int k = rendersystem$autostorageindexbuffer.name();

            if (k != lastIndexBufferObject)
            {
                GlStateManager._glBindBuffer(34963, k);
                lastIndexBufferObject = k;
            }

            int l = rendersystem$autostorageindexbuffer.type().asGLType;
            GlStateManager._drawElements(bufferbuilder$drawstate.mode().asGLMode, bufferbuilder$drawstate.indexCount(), l, 0L);
            ((Buffer)bytebuffer).position(0);
        }
    }

    private static void updateVertexSetup(VertexFormat p_166837_)
    {
        int i = p_166837_.getOrCreateVertexArrayObject();
        int j = p_166837_.getOrCreateVertexBufferObject();
        boolean flag = p_166837_ != lastFormat;

        if (flag)
        {
            reset();
        }

        if (i != lastVertexArrayObject)
        {
            GlStateManager._glBindVertexArray(i);
            lastVertexArrayObject = i;
        }

        if (j != lastVertexBufferObject)
        {
            GlStateManager._glBindBuffer(34962, j);
            lastVertexBufferObject = j;
        }

        if (flag)
        {
            p_166837_.setupBufferState();
            lastFormat = p_166837_;
        }
    }
}
