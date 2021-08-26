package net.minecraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

public abstract class GuiComponent
{
    public static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation("textures/gui/options_background.png");
    public static final ResourceLocation STATS_ICON_LOCATION = new ResourceLocation("textures/gui/container/stats_icons.png");
    public static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    private int blitOffset;

    protected void hLine(PoseStack pMatrixStack, int pMinX, int pMaxX, int pY, int pColor)
    {
        if (pMaxX < pMinX)
        {
            int i = pMinX;
            pMinX = pMaxX;
            pMaxX = i;
        }

        fill(pMatrixStack, pMinX, pY, pMaxX + 1, pY + 1, pColor);
    }

    protected void vLine(PoseStack pMatrixStack, int pX, int pMinY, int pMaxY, int pColor)
    {
        if (pMaxY < pMinY)
        {
            int i = pMinY;
            pMinY = pMaxY;
            pMaxY = i;
        }

        fill(pMatrixStack, pX, pMinY + 1, pX + 1, pMaxY, pColor);
    }

    public static void fill(PoseStack pMatrixStack, int pMinX, int pMinY, int pMaxX, int pMaxY, int pColor)
    {
        innerFill(pMatrixStack.last().pose(), pMinX, pMinY, pMaxX, pMaxY, pColor);
    }

    private static void innerFill(Matrix4f pMatrix, int pMinX, int pMinY, int pMaxX, int pMaxY, int pColor)
    {
        if (pMinX < pMaxX)
        {
            int i = pMinX;
            pMinX = pMaxX;
            pMaxX = i;
        }

        if (pMinY < pMaxY)
        {
            int j = pMinY;
            pMinY = pMaxY;
            pMaxY = j;
        }

        float f3 = (float)(pColor >> 24 & 255) / 255.0F;
        float f = (float)(pColor >> 16 & 255) / 255.0F;
        float f1 = (float)(pColor >> 8 & 255) / 255.0F;
        float f2 = (float)(pColor & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(pMatrix, (float)pMinX, (float)pMaxY, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(pMatrix, (float)pMaxX, (float)pMaxY, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(pMatrix, (float)pMaxX, (float)pMinY, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(pMatrix, (float)pMinX, (float)pMinY, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    protected void fillGradient(PoseStack pMatrixStack, int pX1, int pY1, int pX2, int pY2, int pColorFrom, int pColorTo)
    {
        fillGradient(pMatrixStack, pX1, pY1, pX2, pY2, pColorFrom, pColorTo, this.blitOffset);
    }

    protected static void fillGradient(PoseStack p_168741_, int pMatrixStack, int pX1, int pY1, int pX2, int pY2, int pColorFrom, int pColorTo)
    {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        fillGradient(p_168741_.last().pose(), bufferbuilder, pMatrixStack, pX1, pY1, pX2, pColorTo, pY2, pColorFrom);
        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    protected static void fillGradient(Matrix4f p_93124_, BufferBuilder pMatrixStack, int pX1, int pY1, int pX2, int pY2, int pColorFrom, int pColorTo, int p_93132_)
    {
        float f = (float)(pColorTo >> 24 & 255) / 255.0F;
        float f1 = (float)(pColorTo >> 16 & 255) / 255.0F;
        float f2 = (float)(pColorTo >> 8 & 255) / 255.0F;
        float f3 = (float)(pColorTo & 255) / 255.0F;
        float f4 = (float)(p_93132_ >> 24 & 255) / 255.0F;
        float f5 = (float)(p_93132_ >> 16 & 255) / 255.0F;
        float f6 = (float)(p_93132_ >> 8 & 255) / 255.0F;
        float f7 = (float)(p_93132_ & 255) / 255.0F;
        pMatrixStack.vertex(p_93124_, (float)pX2, (float)pY1, (float)pColorFrom).color(f1, f2, f3, f).endVertex();
        pMatrixStack.vertex(p_93124_, (float)pX1, (float)pY1, (float)pColorFrom).color(f1, f2, f3, f).endVertex();
        pMatrixStack.vertex(p_93124_, (float)pX1, (float)pY2, (float)pColorFrom).color(f5, f6, f7, f4).endVertex();
        pMatrixStack.vertex(p_93124_, (float)pX2, (float)pY2, (float)pColorFrom).color(f5, f6, f7, f4).endVertex();
    }

    public static void drawCenteredString(PoseStack pMatrixStack, Font pFontRenderer, String pFont, int pText, int pX, int pY)
    {
        pFontRenderer.drawShadow(pMatrixStack, pFont, (float)(pText - pFontRenderer.width(pFont) / 2), (float)pX, pY);
    }

    public static void drawCenteredString(PoseStack pMatrixStack, Font pFontRenderer, Component pFont, int pText, int pX, int pY)
    {
        FormattedCharSequence formattedcharsequence = pFont.getVisualOrderText();
        pFontRenderer.drawShadow(pMatrixStack, formattedcharsequence, (float)(pText - pFontRenderer.width(formattedcharsequence) / 2), (float)pX, pY);
    }

    public static void drawCenteredString(PoseStack pMatrixStack, Font pFontRenderer, FormattedCharSequence pFont, int pText, int pX, int pY)
    {
        pFontRenderer.drawShadow(pMatrixStack, pFont, (float)(pText - pFontRenderer.width(pFont) / 2), (float)pX, pY);
    }

    public static void drawString(PoseStack pMatrixStack, Font pFontRenderer, String pFont, int pText, int pX, int pY)
    {
        pFontRenderer.drawShadow(pMatrixStack, pFont, (float)pText, (float)pX, pY);
    }

    public static void drawString(PoseStack pMatrixStack, Font pFontRenderer, FormattedCharSequence pFont, int pText, int pX, int pY)
    {
        pFontRenderer.drawShadow(pMatrixStack, pFont, (float)pText, (float)pX, pY);
    }

    public static void drawString(PoseStack pMatrixStack, Font pFontRenderer, Component pFont, int pText, int pX, int pY)
    {
        pFontRenderer.drawShadow(pMatrixStack, pFont, (float)pText, (float)pX, pY);
    }

    public void blitOutlineBlack(int pWidth, int pHeight, BiConsumer<Integer, Integer> pBoxXYConsumer)
    {
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        pBoxXYConsumer.accept(pWidth + 1, pHeight);
        pBoxXYConsumer.accept(pWidth - 1, pHeight);
        pBoxXYConsumer.accept(pWidth, pHeight + 1);
        pBoxXYConsumer.accept(pWidth, pHeight - 1);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        pBoxXYConsumer.accept(pWidth, pHeight);
    }

    public static void blit(PoseStack pMatrixStack, int pX, int pY, int pUOffset, int pVOffset, int pWidth, TextureAtlasSprite pHeight)
    {
        innerBlit(pMatrixStack.last().pose(), pX, pX + pVOffset, pY, pY + pWidth, pUOffset, pHeight.getU0(), pHeight.getU1(), pHeight.getV0(), pHeight.getV1());
    }

    public void blit(PoseStack pX, int pY, int pUOffset, int pVOffset, int pWidth, int pHeight, int pTextureWidth)
    {
        blit(pX, pY, pUOffset, this.blitOffset, (float)pVOffset, (float)pWidth, pHeight, pTextureWidth, 256, 256);
    }

    public static void blit(PoseStack pMatrixStack, int pX, int pY, int pUOffset, float pVOffset, float pWidth, int pHeight, int pTextureWidth, int pTextureHeight, int p_93153_)
    {
        innerBlit(pMatrixStack, pX, pX + pHeight, pY, pY + pTextureWidth, pUOffset, pHeight, pTextureWidth, pVOffset, pWidth, p_93153_, pTextureHeight);
    }

    public static void blit(PoseStack pMatrixStack, int pX, int pY, int pUOffset, int pVOffset, float pWidth, float pHeight, int pTextureWidth, int pTextureHeight, int p_93170_, int p_93171_)
    {
        innerBlit(pMatrixStack, pX, pX + pUOffset, pY, pY + pVOffset, 0, pTextureWidth, pTextureHeight, pWidth, pHeight, p_93170_, p_93171_);
    }

    public static void blit(PoseStack pMatrixStack, int pX, int pY, float pUOffset, float pVOffset, int pWidth, int pHeight, int pTextureWidth, int pTextureHeight)
    {
        blit(pMatrixStack, pX, pY, pWidth, pHeight, pUOffset, pVOffset, pWidth, pHeight, pTextureWidth, pTextureHeight);
    }

    private static void innerBlit(PoseStack pMatrixStack, int pX1, int pX2, int pY1, int pY2, int pBlitOffset, int pUWidth, int pVHeight, float pUOffset, float pVOffset, int pTextureWidth, int pTextureHeight)
    {
        innerBlit(pMatrixStack.last().pose(), pX1, pX2, pY1, pY2, pBlitOffset, (pUOffset + 0.0F) / (float)pTextureWidth, (pUOffset + (float)pUWidth) / (float)pTextureWidth, (pVOffset + 0.0F) / (float)pTextureHeight, (pVOffset + (float)pVHeight) / (float)pTextureHeight);
    }

    private static void innerBlit(Matrix4f pMatrixStack, int pX1, int pX2, int pY1, int pY2, int pBlitOffset, float pUWidth, float pVHeight, float pUOffset, float pVOffset)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(pMatrixStack, (float)pX1, (float)pY2, (float)pBlitOffset).uv(pUWidth, pVOffset).endVertex();
        bufferbuilder.vertex(pMatrixStack, (float)pX2, (float)pY2, (float)pBlitOffset).uv(pVHeight, pVOffset).endVertex();
        bufferbuilder.vertex(pMatrixStack, (float)pX2, (float)pY1, (float)pBlitOffset).uv(pVHeight, pUOffset).endVertex();
        bufferbuilder.vertex(pMatrixStack, (float)pX1, (float)pY1, (float)pBlitOffset).uv(pUWidth, pUOffset).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
    }

    public int getBlitOffset()
    {
        return this.blitOffset;
    }

    public void setBlitOffset(int pValue)
    {
        this.blitOffset = pValue;
    }
}
