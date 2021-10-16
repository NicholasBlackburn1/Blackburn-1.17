package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Mth;
import net.minecraft.util.StringDecomposer;
import net.optifine.render.GlBlendState;
import net.optifine.util.GlyphAdvanceFixed;
import net.optifine.util.GuiPoint;

public class Font
{
    private static final float EFFECT_DEPTH = 0.01F;
    private static final Vector3f SHADOW_OFFSET = new Vector3f(0.0F, 0.0F, 0.03F);
    public final int lineHeight = 9;
    public final Random random = new Random();
    private final Function<ResourceLocation, FontSet> fonts;
    private final StringSplitter splitter;
    private boolean blend = false;
    private GlBlendState oldBlendState = new GlBlendState();
    private GlyphInfo glyphAdvanceSpace = new GlyphAdvanceFixed(4.0F);

    public Font(Function<ResourceLocation, FontSet> p_92717_)
    {
        this.fonts = p_92717_;
        this.splitter = new StringSplitter((charIn, styleIn) ->
        {
            return this.getFontSet(styleIn.getFont()).getGlyphInfo(charIn).getAdvance(styleIn.isBold());
        });
    }

    FontSet getFontSet(ResourceLocation pFontLocation)
    {
        return this.fonts.apply(pFontLocation);
    }

    public int drawShadow(PoseStack pMatrixStack, String pText, float pX, float pY, int pColor)
    {
        return this.drawInternal(pText, pX, pY, pColor, pMatrixStack.last().pose(), true, this.isBidirectional());
    }

    public int drawShadow(PoseStack pMatrixStack, String pText, float pX, float pY, int pColor, boolean p_92762_)
    {
        return this.drawInternal(pText, pX, pY, pColor, pMatrixStack.last().pose(), true, p_92762_);
    }

    public int draw(PoseStack pMatrixStack, String pText, float pX, float pY, int pColor)
    {
        return this.drawInternal(pText, pX, pY, pColor, pMatrixStack.last().pose(), false, this.isBidirectional());
    }

    public int drawShadow(PoseStack pMatrixStack, FormattedCharSequence pText, float pX, float pY, int pColor)
    {
        return this.drawInternal(pText, pX, pY, pColor, pMatrixStack.last().pose(), true);
    }

    public int drawShadow(PoseStack pMatrixStack, Component pText, float pX, float pY, int pColor)
    {
        return this.drawInternal(pText.getVisualOrderText(), pX, pY, pColor, pMatrixStack.last().pose(), true);
    }

    public int draw(PoseStack pMatrixStack, FormattedCharSequence pText, float pX, float pY, int pColor)
    {
        return this.drawInternal(pText, pX, pY, pColor, pMatrixStack.last().pose(), false);
    }

    public int draw(PoseStack pMatrixStack, Component pText, float pX, float pY, int pColor)
    {
        return this.drawInternal(pText.getVisualOrderText(), pX, pY, pColor, pMatrixStack.last().pose(), false);
    }

    public String bidirectionalShaping(String pText)
    {
        try
        {
            Bidi bidi = new Bidi((new ArabicShaping(8)).shape(pText), 127);
            bidi.setReorderingMode(0);
            return bidi.writeReordered(2);
        }
        catch (ArabicShapingException arabicshapingexception1)
        {
            return pText;
        }
    }

    private int drawInternal(String pText, float pX, float pY, int pColor, Matrix4f pMatrix, boolean pDropShadow, boolean pTransparency)
    {
        if (pText == null)
        {
            return 0;
        }
        else
        {
            MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            int i = this.drawInBatch(pText, pX, pY, pColor, pDropShadow, pMatrix, multibuffersource$buffersource, false, 0, 15728880, pTransparency);
            multibuffersource$buffersource.endBatch();
            return i;
        }
    }

    public void renderStrings(List<String> texts, GuiPoint[] points, int color, Matrix4f matrix, boolean dropShadow, boolean bidiIn)
    {
        MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        for (int i = 0; i < texts.size(); ++i)
        {
            String s = texts.get(i);

            if (s != null && !s.isEmpty())
            {
                GuiPoint guipoint = points[i];

                if (guipoint != null)
                {
                    float f = (float)guipoint.getX();
                    float f1 = (float)guipoint.getY();
                    this.drawInBatch(s, f, f1, color, dropShadow, matrix, multibuffersource$buffersource, false, 0, 15728880, bidiIn);
                }
            }
        }

        multibuffersource$buffersource.endBatch();
    }

    private int drawInternal(FormattedCharSequence pText, float pX, float pY, int pColor, Matrix4f pMatrix, boolean pDropShadow)
    {
        MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        int i = this.drawInBatch(pText, pX, pY, pColor, pDropShadow, pMatrix, multibuffersource$buffersource, false, 0, 15728880);
        multibuffersource$buffersource.endBatch();
        return i;
    }

    public int drawInBatch(String pText, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix, MultiBufferSource pBuffer, boolean pTransparent, int pColorBackground, int pPackedLight)
    {
        return this.drawInBatch(pText, pX, pY, pColor, pDropShadow, pMatrix, pBuffer, pTransparent, pColorBackground, pPackedLight, this.isBidirectional());
    }

    public int drawInBatch(String pText, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix, MultiBufferSource pBuffer, boolean pTransparent, int pColorBackground, int pPackedLight, boolean p_92833_)
    {
        return this.drawInternal(pText, pX, pY, pColor, pDropShadow, pMatrix, pBuffer, pTransparent, pColorBackground, pPackedLight, p_92833_);
    }

    public int drawInBatch(Component pText, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix, MultiBufferSource pBuffer, boolean pTransparent, int pColorBackground, int pPackedLight)
    {
        return this.drawInBatch(pText.getVisualOrderText(), pX, pY, pColor, pDropShadow, pMatrix, pBuffer, pTransparent, pColorBackground, pPackedLight);
    }

    public int drawInBatch(FormattedCharSequence pText, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix, MultiBufferSource pBuffer, boolean pTransparent, int pColorBackground, int pPackedLight)
    {
        return this.drawInternal(pText, pX, pY, pColor, pDropShadow, pMatrix, pBuffer, pTransparent, pColorBackground, pPackedLight);
    }

    public void drawInBatch8xOutline(FormattedCharSequence p_168646_, float p_168647_, float p_168648_, int p_168649_, int p_168650_, Matrix4f p_168651_, MultiBufferSource p_168652_, int p_168653_)
    {
        int i = adjustColor(p_168650_);
        Font.StringRenderOutput font$stringrenderoutput = new Font.StringRenderOutput(p_168652_, 0.0F, 0.0F, i, false, p_168651_, Font.DisplayMode.NORMAL, p_168653_);

        for (int j = -1; j <= 1; ++j)
        {
            for (int k = -1; k <= 1; ++k)
            {
                if (j != 0 || k != 0)
                {
                    float[] afloat = new float[] {p_168647_};
                    int l = j;
                    int i1 = k;
                    p_168646_.accept((p_314032_7_, p_314032_8_, p_314032_9_) ->
                    {
                        boolean flag = p_314032_8_.isBold();
                        FontSet fontset = this.getFontSet(p_314032_8_.getFont());
                        GlyphInfo glyphinfo = fontset.getGlyphInfo(p_314032_9_);
                        font$stringrenderoutput.x = afloat[0] + (float)l * glyphinfo.getShadowOffset();
                        font$stringrenderoutput.y = p_168648_ + (float)i1 * glyphinfo.getShadowOffset();
                        afloat[0] += glyphinfo.getAdvance(flag);
                        return font$stringrenderoutput.accept(p_314032_7_, p_314032_8_.withColor(i), p_314032_9_);
                    });
                }
            }
        }

        Font.StringRenderOutput font$stringrenderoutput1 = new Font.StringRenderOutput(p_168652_, p_168647_, p_168648_, adjustColor(p_168649_), false, p_168651_, Font.DisplayMode.POLYGON_OFFSET, p_168653_);
        p_168646_.accept(font$stringrenderoutput1);
        font$stringrenderoutput1.finish(0, p_168647_);
    }

    private static int adjustColor(int pColor)
    {
        return (pColor & -67108864) == 0 ? pColor | -16777216 : pColor;
    }

    private int drawInternal(String pText, float pX, float pY, int pColor, boolean pMatrix, Matrix4f pDropShadow, MultiBufferSource pTransparency, boolean p_92916_, int p_92917_, int p_92918_, boolean p_92919_)
    {
        if (p_92919_)
        {
            pText = this.bidirectionalShaping(pText);
        }

        pColor = adjustColor(pColor);
        Matrix4f matrix4f = pDropShadow.copy();

        if (pMatrix)
        {
            this.renderText(pText, pX, pY, pColor, true, pDropShadow, pTransparency, p_92916_, p_92917_, p_92918_);
            matrix4f.translate(SHADOW_OFFSET);
        }

        pX = this.renderText(pText, pX, pY, pColor, false, matrix4f, pTransparency, p_92916_, p_92917_, p_92918_);
        return (int)pX + (pMatrix ? 1 : 0);
    }

    private int drawInternal(FormattedCharSequence pText, float pX, float pY, int pColor, boolean pMatrix, Matrix4f pDropShadow, MultiBufferSource pTransparency, boolean p_92874_, int p_92875_, int p_92876_)
    {
        pColor = adjustColor(pColor);
        Matrix4f matrix4f = pDropShadow.copy();

        if (pMatrix)
        {
            this.renderText(pText, pX, pY, pColor, true, pDropShadow, pTransparency, p_92874_, p_92875_, p_92876_);
            matrix4f.translate(SHADOW_OFFSET);
        }

        pX = this.renderText(pText, pX, pY, pColor, false, matrix4f, pTransparency, p_92874_, p_92875_, p_92876_);
        return (int)pX + (pMatrix ? 1 : 0);
    }

    private float renderText(String pText, float pX, float pY, int pColor, boolean pIsShadow, Matrix4f pMatrix, MultiBufferSource pBuffer, boolean pIsTransparent, int pColorBackground, int pPackedLight)
    {
        Font.StringRenderOutput font$stringrenderoutput = new Font.StringRenderOutput(pBuffer, pX, pY, pColor, pIsShadow, pMatrix, pIsTransparent, pPackedLight);
        StringDecomposer.iterateFormatted(pText, Style.EMPTY, font$stringrenderoutput);
        return font$stringrenderoutput.finish(pColorBackground, pX);
    }

    private float renderText(FormattedCharSequence pText, float pX, float pY, int pColor, boolean pIsShadow, Matrix4f pMatrix, MultiBufferSource pBuffer, boolean pIsTransparent, int pColorBackground, int pPackedLight)
    {
        Font.StringRenderOutput font$stringrenderoutput = new Font.StringRenderOutput(pBuffer, pX, pY, pColor, pIsShadow, pMatrix, pIsTransparent, pPackedLight);
        pText.accept(font$stringrenderoutput);
        return font$stringrenderoutput.finish(pColorBackground, pX);
    }

    void renderChar(BakedGlyph pGlyph, boolean pBold, boolean pItalic, float pBoldOffset, float pX, float pY, Matrix4f pMatrix, VertexConsumer pBuffer, float pRed, float pGreen, float pBlue, float pAlpha, int pPackedLight)
    {
        pGlyph.render(pItalic, pX, pY, pMatrix, pBuffer, pRed, pGreen, pBlue, pAlpha, pPackedLight);

        if (pBold)
        {
            pGlyph.render(pItalic, pX + pBoldOffset, pY, pMatrix, pBuffer, pRed, pGreen, pBlue, pAlpha, pPackedLight);
        }
    }

    public int width(String pText)
    {
        return Mth.ceil(this.splitter.stringWidth(pText));
    }

    public int width(FormattedText pText)
    {
        return Mth.ceil(this.splitter.stringWidth(pText));
    }

    public int width(FormattedCharSequence pText)
    {
        return Mth.ceil(this.splitter.stringWidth(pText));
    }

    public String plainSubstrByWidth(String pText, int pMaxLength, boolean p_92840_)
    {
        return p_92840_ ? this.splitter.plainTailByWidth(pText, pMaxLength, Style.EMPTY) : this.splitter.plainHeadByWidth(pText, pMaxLength, Style.EMPTY);
    }

    public String plainSubstrByWidth(String pText, int pMaxLength)
    {
        return this.splitter.plainHeadByWidth(pText, pMaxLength, Style.EMPTY);
    }

    public FormattedText substrByWidth(FormattedText p_92855_, int p_92856_)
    {
        return this.splitter.headByWidth(p_92855_, p_92856_, Style.EMPTY);
    }

    public void drawWordWrap(FormattedText p_92858_, int p_92859_, int p_92860_, int p_92861_, int p_92862_)
    {
        Matrix4f matrix4f = Transformation.identity().getMatrix();

        for (FormattedCharSequence formattedcharsequence : this.split(p_92858_, p_92861_))
        {
            this.drawInternal(formattedcharsequence, (float)p_92859_, (float)p_92860_, p_92862_, matrix4f, false);
            p_92860_ += 9;
        }
    }

    public int wordWrapHeight(String pStr, int pMaxLength)
    {
        return 9 * this.splitter.splitLines(pStr, pMaxLength, Style.EMPTY).size();
    }

    public List<FormattedCharSequence> split(FormattedText p_92924_, int p_92925_)
    {
        return Language.getInstance().getVisualOrder(this.splitter.splitLines(p_92924_, p_92925_, Style.EMPTY));
    }

    public boolean isBidirectional()
    {
        return Language.getInstance().isDefaultRightToLeft();
    }

    public StringSplitter getSplitter()
    {
        return this.splitter;
    }

    public static enum DisplayMode
    {
        NORMAL,
        SEE_THROUGH,
        POLYGON_OFFSET;
    }

    class StringRenderOutput implements FormattedCharSink
    {
        final MultiBufferSource bufferSource;
        private final boolean dropShadow;
        private final float dimFactor;
        private final float r;
        private final float g;
        private final float b;
        private final float a;
        private final Matrix4f pose;
        private final Font.DisplayMode mode;
        private final int packedLightCoords;
        float x;
        float y;
        @Nullable
        private List<BakedGlyph.Effect> effects;
        private Style lastStyle;
        private FontSet lastStyleFont;

        private void addEffect(BakedGlyph.Effect pEffect)
        {
            if (this.effects == null)
            {
                this.effects = Lists.newArrayList();
            }

            this.effects.add(pEffect);
        }

        public StringRenderOutput(MultiBufferSource p_92953_, float p_92954_, float p_92955_, int p_92956_, boolean p_92957_, Matrix4f p_92958_, boolean p_92959_, int p_92960_)
        {
            this(p_92953_, p_92954_, p_92955_, p_92956_, p_92957_, p_92958_, p_92959_ ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, p_92960_);
        }

        public StringRenderOutput(MultiBufferSource p_181365_, float p_181366_, float p_181367_, int p_181368_, boolean p_181369_, Matrix4f p_181370_, Font.DisplayMode p_181371_, int p_181372_)
        {
            this.bufferSource = p_181365_;
            this.x = p_181366_;
            this.y = p_181367_;
            this.dropShadow = p_181369_;
            this.dimFactor = p_181369_ ? 0.25F : 1.0F;
            this.r = (float)(p_181368_ >> 16 & 255) / 255.0F * this.dimFactor;
            this.g = (float)(p_181368_ >> 8 & 255) / 255.0F * this.dimFactor;
            this.b = (float)(p_181368_ & 255) / 255.0F * this.dimFactor;
            this.a = (float)(p_181368_ >> 24 & 255) / 255.0F;
            this.pose = p_181370_.isIdentity() ? BakedGlyph.MATRIX_IDENTITY : p_181370_;
            this.mode = p_181371_;
            this.packedLightCoords = p_181372_;
        }

        public boolean accept(int p_92967_, Style p_92968_, int p_92969_)
        {
            FontSet fontset = this.getFont(p_92968_);
            GlyphInfo glyphinfo = fontset.getGlyphInfo(p_92969_);
            BakedGlyph bakedglyph = p_92968_.isObfuscated() && p_92969_ != 32 ? fontset.getRandomGlyph(glyphinfo) : fontset.getGlyph(p_92969_);
            boolean flag = p_92968_.isBold();
            float f = this.a;
            TextColor textcolor = p_92968_.getColor();
            float f1;
            float f2;
            float f3;

            if (textcolor != null)
            {
                int i = textcolor.getValue();
                f1 = (float)(i >> 16 & 255) / 255.0F * this.dimFactor;
                f2 = (float)(i >> 8 & 255) / 255.0F * this.dimFactor;
                f3 = (float)(i & 255) / 255.0F * this.dimFactor;
            }
            else
            {
                f1 = this.r;
                f2 = this.g;
                f3 = this.b;
            }

            if (!(bakedglyph instanceof EmptyGlyph))
            {
                float f5 = flag ? glyphinfo.getBoldOffset() : 0.0F;
                float f4 = this.dropShadow ? glyphinfo.getShadowOffset() : 0.0F;
                VertexConsumer vertexconsumer = this.bufferSource.getBuffer(bakedglyph.renderType(this.mode));
                Font.this.renderChar(bakedglyph, flag, p_92968_.isItalic(), f5, this.x + f4, this.y + f4, this.pose, vertexconsumer, f1, f2, f3, f, this.packedLightCoords);
            }

            float f6 = glyphinfo.getAdvance(flag);
            float f7 = this.dropShadow ? 1.0F : 0.0F;

            if (p_92968_.isStrikethrough())
            {
                this.addEffect(new BakedGlyph.Effect(this.x + f7 - 1.0F, this.y + f7 + 4.5F, this.x + f7 + f6, this.y + f7 + 4.5F - 1.0F, 0.01F, f1, f2, f3, f));
            }

            if (p_92968_.isUnderlined())
            {
                this.addEffect(new BakedGlyph.Effect(this.x + f7 - 1.0F, this.y + f7 + 9.0F, this.x + f7 + f6, this.y + f7 + 9.0F - 1.0F, 0.01F, f1, f2, f3, f));
            }

            this.x += f6;
            return true;
        }

        public float finish(int p_92962_, float p_92963_)
        {
            if (p_92962_ != 0)
            {
                float f = (float)(p_92962_ >> 24 & 255) / 255.0F;
                float f1 = (float)(p_92962_ >> 16 & 255) / 255.0F;
                float f2 = (float)(p_92962_ >> 8 & 255) / 255.0F;
                float f3 = (float)(p_92962_ & 255) / 255.0F;
                this.addEffect(new BakedGlyph.Effect(p_92963_ - 1.0F, this.y + 9.0F, this.x + 1.0F, this.y - 1.0F, 0.01F, f1, f2, f3, f));
            }

            if (this.effects != null)
            {
                BakedGlyph bakedglyph = Font.this.getFontSet(Style.DEFAULT_FONT).whiteGlyph();
                VertexConsumer vertexconsumer = this.bufferSource.getBuffer(bakedglyph.renderType(this.mode));

                for (BakedGlyph.Effect bakedglyph$effect : this.effects)
                {
                    bakedglyph.renderEffect(bakedglyph$effect, this.pose, vertexconsumer, this.packedLightCoords);
                }
            }

            return this.x;
        }

        private FontSet getFont(Style styleIn)
        {
            if (styleIn == this.lastStyle)
            {
                return this.lastStyleFont;
            }
            else
            {
                this.lastStyle = styleIn;
                this.lastStyleFont = Font.this.getFontSet(styleIn.getFont());
                return this.lastStyleFont;
            }
        }
    }
}
