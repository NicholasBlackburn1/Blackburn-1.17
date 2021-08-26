package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import java.nio.IntBuffer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.optifine.reflect.ReflectorForge;
import net.optifine.render.GLConst;

public abstract class RenderTarget
{
    private static final int RED_CHANNEL = 0;
    private static final int GREEN_CHANNEL = 1;
    private static final int BLUE_CHANNEL = 2;
    private static final int ALPHA_CHANNEL = 3;
    public int width;
    public int height;
    public int viewWidth;
    public int viewHeight;
    public final boolean useDepth;
    public int frameBufferId;
    protected int colorTextureId;
    protected int depthBufferId;
    private final float[] clearChannels = Util.make(() ->
    {
        return new float[]{1.0F, 1.0F, 1.0F, 0.0F};
    });
    public int filterMode;
    private boolean stencilEnabled = false;

    public RenderTarget(boolean p_166199_)
    {
        this.useDepth = p_166199_;
        this.frameBufferId = -1;
        this.colorTextureId = -1;
        this.depthBufferId = -1;
    }

    public void resize(int pWidth, int pHeight, boolean pOnMac)
    {
        if (!RenderSystem.isOnRenderThread())
        {
            RenderSystem.recordRenderCall(() ->
            {
                this._resize(pWidth, pHeight, pOnMac);
            });
        }
        else
        {
            this._resize(pWidth, pHeight, pOnMac);
        }
    }

    private void _resize(int pWidth, int pHeight, boolean pOnMac)
    {
        if (!GLX.isUsingFBOs())
        {
            this.viewWidth = pWidth;
            this.viewHeight = pHeight;
        }
        else
        {
            RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
            GlStateManager._enableDepthTest();

            if (this.frameBufferId >= 0)
            {
                this.destroyBuffers();
            }

            this.createBuffers(pWidth, pHeight, pOnMac);
            GlStateManager._glBindFramebuffer(36160, 0);
        }
    }

    public void destroyBuffers()
    {
        if (GLX.isUsingFBOs())
        {
            RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
            this.unbindRead();
            this.unbindWrite();

            if (this.depthBufferId > -1)
            {
                TextureUtil.releaseTextureId(this.depthBufferId);
                this.depthBufferId = -1;
            }

            if (this.colorTextureId > -1)
            {
                TextureUtil.releaseTextureId(this.colorTextureId);
                this.colorTextureId = -1;
            }

            if (this.frameBufferId > -1)
            {
                GlStateManager._glBindFramebuffer(36160, 0);
                GlStateManager._glDeleteFramebuffers(this.frameBufferId);
                this.frameBufferId = -1;
            }
        }
    }

    public void copyDepthFrom(RenderTarget p_83946_)
    {
        if (GLX.isUsingFBOs())
        {
            RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
            GlStateManager._glBindFramebuffer(36008, p_83946_.frameBufferId);
            GlStateManager._glBindFramebuffer(36009, this.frameBufferId);
            GlStateManager._glBlitFrameBuffer(0, 0, p_83946_.width, p_83946_.height, 0, 0, this.width, this.height, 256, 9728);
            GlStateManager._glBindFramebuffer(36160, 0);
        }
    }

    public void createBuffers(int pWidth, int pHeight, boolean pOnMac)
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        int i = RenderSystem.maxSupportedTextureSize();

        if (pWidth > 0 && pWidth <= i && pHeight > 0 && pHeight <= i)
        {
            this.viewWidth = pWidth;
            this.viewHeight = pHeight;
            this.width = pWidth;
            this.height = pHeight;

            if (!GLX.isUsingFBOs())
            {
                this.clear(pOnMac);
            }
            else
            {
                this.frameBufferId = GlStateManager.glGenFramebuffers();
                this.colorTextureId = TextureUtil.generateTextureId();

                if (this.useDepth)
                {
                    this.depthBufferId = TextureUtil.generateTextureId();
                    GlStateManager._bindTexture(this.depthBufferId);
                    GlStateManager._texParameter(3553, 10241, 9728);
                    GlStateManager._texParameter(3553, 10240, 9728);
                    GlStateManager._texParameter(3553, 34892, 0);
                    GlStateManager._texParameter(3553, 10242, 33071);
                    GlStateManager._texParameter(3553, 10243, 33071);

                    if (this.stencilEnabled)
                    {
                        GlStateManager._texImage2D(3553, 0, 36013, this.width, this.height, 0, 34041, 36269, (IntBuffer)null);
                    }
                    else
                    {
                        GlStateManager._texImage2D(3553, 0, 6402, this.width, this.height, 0, 6402, 5126, (IntBuffer)null);
                    }
                }

                this.setFilterMode(9728);
                GlStateManager._bindTexture(this.colorTextureId);
                GlStateManager._texParameter(3553, 10242, 33071);
                GlStateManager._texParameter(3553, 10243, 33071);
                GlStateManager._texImage2D(3553, 0, 32856, this.width, this.height, 0, 6408, 5121, (IntBuffer)null);
                GlStateManager._glBindFramebuffer(36160, this.frameBufferId);
                GlStateManager._glFramebufferTexture2D(36160, 36064, 3553, this.colorTextureId, 0);

                if (this.useDepth)
                {
                    if (this.stencilEnabled)
                    {
                        if (ReflectorForge.getForgeUseCombinedDepthStencilAttachment())
                        {
                            GlStateManager._glFramebufferTexture2D(GLConst.GL_FRAMEBUFFER, 33306, 3553, this.depthBufferId, 0);
                        }
                        else
                        {
                            GlStateManager._glFramebufferTexture2D(GLConst.GL_FRAMEBUFFER, 36096, 3553, this.depthBufferId, 0);
                            GlStateManager._glFramebufferTexture2D(GLConst.GL_FRAMEBUFFER, 36128, 3553, this.depthBufferId, 0);
                        }
                    }
                    else
                    {
                        GlStateManager._glFramebufferTexture2D(36160, 36096, 3553, this.depthBufferId, 0);
                    }
                }

                this.checkStatus();
                this.clear(pOnMac);
                this.unbindRead();
            }
        }
        else
        {
            throw new IllegalArgumentException("Window " + pWidth + "x" + pHeight + " size out of bounds (max. size: " + i + ")");
        }
    }

    public void setFilterMode(int pFramebufferFilter)
    {
        if (GLX.isUsingFBOs())
        {
            RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
            this.filterMode = pFramebufferFilter;
            GlStateManager._bindTexture(this.colorTextureId);
            GlStateManager._texParameter(3553, 10241, pFramebufferFilter);
            GlStateManager._texParameter(3553, 10240, pFramebufferFilter);
            GlStateManager._bindTexture(0);
        }
    }

    public void checkStatus()
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        int i = GlStateManager.glCheckFramebufferStatus(36160);

        if (i != 36053)
        {
            if (i == 36054)
            {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
            }
            else if (i == 36055)
            {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
            }
            else if (i == 36059)
            {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
            }
            else if (i == 36060)
            {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
            }
            else if (i == 36061)
            {
                throw new RuntimeException("GL_FRAMEBUFFER_UNSUPPORTED");
            }
            else if (i == 1285)
            {
                throw new RuntimeException("GL_OUT_OF_MEMORY");
            }
            else
            {
                throw new RuntimeException("glCheckFramebufferStatus returned unknown status:" + i);
            }
        }
    }

    public void bindRead()
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager._bindTexture(this.colorTextureId);
    }

    public void unbindRead()
    {
        if (GLX.isUsingFBOs())
        {
            RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
            GlStateManager._bindTexture(0);
        }
    }

    public void bindWrite(boolean pSetViewport)
    {
        if (!RenderSystem.isOnRenderThread())
        {
            RenderSystem.recordRenderCall(() ->
            {
                this._bindWrite(pSetViewport);
            });
        }
        else
        {
            this._bindWrite(pSetViewport);
        }
    }

    private void _bindWrite(boolean pSetViewport)
    {
        if (GLX.isUsingFBOs())
        {
            RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
            GlStateManager._glBindFramebuffer(36160, this.frameBufferId);

            if (pSetViewport)
            {
                GlStateManager._viewport(0, 0, this.viewWidth, this.viewHeight);
            }
        }
    }

    public void unbindWrite()
    {
        if (GLX.isUsingFBOs())
        {
            if (!RenderSystem.isOnRenderThread())
            {
                RenderSystem.recordRenderCall(() ->
                {
                    GlStateManager._glBindFramebuffer(36160, 0);
                });
            }
            else
            {
                GlStateManager._glBindFramebuffer(36160, 0);
            }
        }
    }

    public void setClearColor(float pRed, float pGreen, float pBlue, float pAlpha)
    {
        this.clearChannels[0] = pRed;
        this.clearChannels[1] = pGreen;
        this.clearChannels[2] = pBlue;
        this.clearChannels[3] = pAlpha;
    }

    public void blitToScreen(int pWidth, int pHeight)
    {
        this.blitToScreen(pWidth, pHeight, true);
    }

    public void blitToScreen(int pWidth, int pHeight, boolean p_83960_)
    {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);

        if (!RenderSystem.isInInitPhase())
        {
            RenderSystem.recordRenderCall(() ->
            {
                this._blitToScreen(pWidth, pHeight, p_83960_);
            });
        }
        else
        {
            this._blitToScreen(pWidth, pHeight, p_83960_);
        }
    }

    private void _blitToScreen(int pWidth, int pHeight, boolean p_83974_)
    {
        if (GLX.isUsingFBOs())
        {
            RenderSystem.assertThread(RenderSystem::isOnRenderThread);
            GlStateManager._colorMask(true, true, true, false);
            GlStateManager._disableDepthTest();
            GlStateManager._depthMask(false);
            GlStateManager._viewport(0, 0, pWidth, pHeight);

            if (p_83974_)
            {
                GlStateManager._disableBlend();
            }

            Minecraft minecraft = Minecraft.getInstance();
            ShaderInstance shaderinstance = minecraft.gameRenderer.blitShader;
            shaderinstance.setSampler("DiffuseSampler", this.colorTextureId);
            Matrix4f matrix4f = Matrix4f.orthographic((float)pWidth, (float)(-pHeight), 1000.0F, 3000.0F);
            RenderSystem.setProjectionMatrix(matrix4f);

            if (shaderinstance.MODEL_VIEW_MATRIX != null)
            {
                shaderinstance.MODEL_VIEW_MATRIX.set(Matrix4f.createTranslateMatrix(0.0F, 0.0F, -2000.0F));
            }

            if (shaderinstance.PROJECTION_MATRIX != null)
            {
                shaderinstance.PROJECTION_MATRIX.set(matrix4f);
            }

            shaderinstance.apply();
            float f = (float)pWidth;
            float f1 = (float)pHeight;
            float f2 = (float)this.viewWidth / (float)this.width;
            float f3 = (float)this.viewHeight / (float)this.height;
            Tesselator tesselator = RenderSystem.renderThreadTesselator();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex(0.0D, (double)f1, 0.0D).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
            bufferbuilder.vertex((double)f, (double)f1, 0.0D).uv(f2, 0.0F).color(255, 255, 255, 255).endVertex();
            bufferbuilder.vertex((double)f, 0.0D, 0.0D).uv(f2, f3).color(255, 255, 255, 255).endVertex();
            bufferbuilder.vertex(0.0D, 0.0D, 0.0D).uv(0.0F, f3).color(255, 255, 255, 255).endVertex();
            bufferbuilder.end();
            BufferUploader._endInternal(bufferbuilder);
            shaderinstance.clear();
            GlStateManager._depthMask(true);
            GlStateManager._colorMask(true, true, true, true);
        }
    }

    public void clear(boolean pOnMac)
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        this.bindWrite(true);
        GlStateManager._clearColor(this.clearChannels[0], this.clearChannels[1], this.clearChannels[2], this.clearChannels[3]);
        int i = 16384;

        if (this.useDepth)
        {
            GlStateManager._clearDepth(1.0D);
            i |= 256;
        }

        GlStateManager._clear(i, pOnMac);
        this.unbindWrite();
    }

    public int getColorTextureId()
    {
        return this.colorTextureId;
    }

    public int getDepthTextureId()
    {
        return this.depthBufferId;
    }

    public void enableStencil()
    {
        if (!this.stencilEnabled)
        {
            this.stencilEnabled = true;
            this.resize(this.viewWidth, this.viewHeight, Minecraft.ON_OSX);
        }
    }

    public boolean isStencilEnabled()
    {
        return this.stencilEnabled;
    }
}
