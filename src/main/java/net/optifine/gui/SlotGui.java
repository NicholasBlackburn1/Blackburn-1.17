package net.optifine.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.optifine.util.TextureUtils;

public abstract class SlotGui extends AbstractContainerEventHandler implements Widget, NarratableEntry
{
    public static final ResourceLocation WHITE_TEXTURE_LOCATION = TextureUtils.WHITE_TEXTURE_LOCATION;
    protected static final int NO_DRAG = -1;
    protected static final int DRAG_OUTSIDE = -2;
    protected final Minecraft minecraft;
    protected int width;
    protected int height;
    protected int y0;
    protected int y1;
    protected int x1;
    protected int x0;
    protected final int itemHeight;
    protected boolean centerListVertically = true;
    protected int yDrag = -2;
    protected double yo;
    protected boolean visible = true;
    protected boolean renderSelection = true;
    protected boolean renderHeader;
    protected int headerHeight;
    private boolean scrolling;

    public SlotGui(Minecraft mcIn, int width, int height, int topIn, int bottomIn, int slotHeightIn)
    {
        this.minecraft = mcIn;
        this.width = width;
        this.height = height;
        this.y0 = topIn;
        this.y1 = bottomIn;
        this.itemHeight = slotHeightIn;
        this.x0 = 0;
        this.x1 = width;
    }

    public void updateSize(int p_updateSize_1_, int p_updateSize_2_, int p_updateSize_3_, int p_updateSize_4_)
    {
        this.width = p_updateSize_1_;
        this.height = p_updateSize_2_;
        this.y0 = p_updateSize_3_;
        this.y1 = p_updateSize_4_;
        this.x0 = 0;
        this.x1 = p_updateSize_1_;
    }

    public void setRenderSelection(boolean p_setRenderSelection_1_)
    {
        this.renderSelection = p_setRenderSelection_1_;
    }

    protected void setRenderHeader(boolean p_setRenderHeader_1_, int p_setRenderHeader_2_)
    {
        this.renderHeader = p_setRenderHeader_1_;
        this.headerHeight = p_setRenderHeader_2_;

        if (!p_setRenderHeader_1_)
        {
            this.headerHeight = 0;
        }
    }

    public void setVisible(boolean p_setVisible_1_)
    {
        this.visible = p_setVisible_1_;
    }

    public boolean isVisible()
    {
        return this.visible;
    }

    protected abstract int getItemCount();

    public List <? extends GuiEventListener > children()
    {
        return Collections.emptyList();
    }

    protected boolean selectItem(int p_selectItem_1_, int p_selectItem_2_, double p_selectItem_3_, double p_selectItem_5_)
    {
        return true;
    }

    protected abstract boolean isSelectedItem(int var1);

    protected int getMaxPosition()
    {
        return this.getItemCount() * this.itemHeight + this.headerHeight;
    }

    protected abstract void renderBackground();

    protected void updateItemPosition(int p_updateItemPosition_1_, int p_updateItemPosition_2_, int p_updateItemPosition_3_, float p_updateItemPosition_4_)
    {
    }

    protected abstract void renderItem(PoseStack var1, int var2, int var3, int var4, int var5, int var6, int var7, float var8);

    protected void renderHeader(int p_renderHeader_1_, int p_renderHeader_2_, Tesselator p_renderHeader_3_)
    {
    }

    protected void clickedHeader(int p_clickedHeader_1_, int p_clickedHeader_2_)
    {
    }

    protected void renderDecorations(int p_renderDecorations_1_, int p_renderDecorations_2_)
    {
    }

    public int getItemAtPosition(double p_getItemAtPosition_1_, double p_getItemAtPosition_3_)
    {
        int i = this.x0 + this.width / 2 - this.getRowWidth() / 2;
        int j = this.x0 + this.width / 2 + this.getRowWidth() / 2;
        int k = Mth.floor(p_getItemAtPosition_3_ - (double)this.y0) - this.headerHeight + (int)this.yo - 4;
        int l = k / this.itemHeight;
        return p_getItemAtPosition_1_ < (double)this.getScrollbarPosition() && p_getItemAtPosition_1_ >= (double)i && p_getItemAtPosition_1_ <= (double)j && l >= 0 && k >= 0 && l < this.getItemCount() ? l : -1;
    }

    protected void capYPosition()
    {
        this.yo = Mth.clamp(this.yo, 0.0D, (double)this.getMaxScroll());
    }

    public int getMaxScroll()
    {
        return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
    }

    public void centerScrollOn(int p_centerScrollOn_1_)
    {
        this.yo = (double)(p_centerScrollOn_1_ * this.itemHeight + this.itemHeight / 2 - (this.y1 - this.y0) / 2);
        this.capYPosition();
    }

    public int getScroll()
    {
        return (int)this.yo;
    }

    public boolean isMouseInList(double p_isMouseInList_1_, double p_isMouseInList_3_)
    {
        return p_isMouseInList_3_ >= (double)this.y0 && p_isMouseInList_3_ <= (double)this.y1 && p_isMouseInList_1_ >= (double)this.x0 && p_isMouseInList_1_ <= (double)this.x1;
    }

    public int getScrollBottom()
    {
        return (int)this.yo - this.height - this.headerHeight;
    }

    public void scroll(int p_scroll_1_)
    {
        this.yo += (double)p_scroll_1_;
        this.capYPosition();
        this.yDrag = -2;
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        if (this.visible)
        {
            this.renderBackground();
            int i = this.getScrollbarPosition();
            int j = i + 6;
            this.capYPosition();
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            float f = 32.0F;
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex((double)this.x0, (double)this.y1, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y1 + (int)this.yo) / 32.0F).color(32, 32, 32, 255).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)this.y1, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y1 + (int)this.yo) / 32.0F).color(32, 32, 32, 255).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)this.y0, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y0 + (int)this.yo) / 32.0F).color(32, 32, 32, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)this.y0, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y0 + (int)this.yo) / 32.0F).color(32, 32, 32, 255).endVertex();
            tesselator.end();
            int k = this.x0 + this.width / 2 - this.getRowWidth() / 2 + 2;
            int l = this.y0 + 4 - (int)this.yo;

            if (this.renderHeader)
            {
                this.renderHeader(k, l, tesselator);
            }

            this.renderList(pMatrixStack, k, l, pMouseX, pMouseY, pPartialTicks);
            RenderSystem.disableDepthTest();
            this.renderHoleBackground(0, this.y0, 255, 255);
            this.renderHoleBackground(this.y1, this.height, 255, 255);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, WHITE_TEXTURE_LOCATION);
            RenderSystem.disableTexture();
            int i1 = 4;
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex((double)this.x0, (double)(this.y0 + 4), 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 0).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)(this.y0 + 4), 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 0).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)this.y0, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)this.y0, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            tesselator.end();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex((double)this.x0, (double)this.y1, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)this.y1, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)(this.y1 - 4), 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 0).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)(this.y1 - 4), 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 0).endVertex();
            tesselator.end();
            int j1 = this.getMaxScroll();

            if (j1 > 0)
            {
                int k1 = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
                k1 = Mth.clamp(k1, 32, this.y1 - this.y0 - 8);
                int l1 = (int)this.yo * (this.y1 - this.y0 - k1) / j1 + this.y0;

                if (l1 < this.y0)
                {
                    l1 = this.y0;
                }

                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                bufferbuilder.vertex((double)i, (double)this.y1, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.vertex((double)j, (double)this.y1, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.vertex((double)j, (double)this.y0, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
                bufferbuilder.vertex((double)i, (double)this.y0, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
                tesselator.end();
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                bufferbuilder.vertex((double)i, (double)(l1 + k1), 0.0D).uv(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.vertex((double)j, (double)(l1 + k1), 0.0D).uv(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.vertex((double)j, (double)l1, 0.0D).uv(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
                bufferbuilder.vertex((double)i, (double)l1, 0.0D).uv(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
                tesselator.end();
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                bufferbuilder.vertex((double)i, (double)(l1 + k1 - 1), 0.0D).uv(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.vertex((double)(j - 1), (double)(l1 + k1 - 1), 0.0D).uv(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.vertex((double)(j - 1), (double)l1, 0.0D).uv(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
                bufferbuilder.vertex((double)i, (double)l1, 0.0D).uv(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
                tesselator.end();
            }

            this.renderDecorations(pMouseX, pMouseY);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }
    }

    protected void updateScrollingState(double p_updateScrollingState_1_, double p_updateScrollingState_3_, int p_updateScrollingState_5_)
    {
        this.scrolling = p_updateScrollingState_5_ == 0 && p_updateScrollingState_1_ >= (double)this.getScrollbarPosition() && p_updateScrollingState_1_ < (double)(this.getScrollbarPosition() + 6);
    }

    public boolean mouseClicked(double pMouseX, double p_94738_, int pMouseY)
    {
        this.updateScrollingState(pMouseX, p_94738_, pMouseY);

        if (this.isVisible() && this.isMouseInList(pMouseX, p_94738_))
        {
            int i = this.getItemAtPosition(pMouseX, p_94738_);

            if (i == -1 && pMouseY == 0)
            {
                this.clickedHeader((int)(pMouseX - (double)(this.x0 + this.width / 2 - this.getRowWidth() / 2)), (int)(p_94738_ - (double)this.y0) + (int)this.yo - 4);
                return true;
            }
            else if (i != -1 && this.selectItem(i, pMouseY, pMouseX, p_94738_))
            {
                if (this.children().size() > i)
                {
                    this.setFocused(this.children().get(i));
                }

                this.setDragging(true);
                return true;
            }
            else
            {
                return this.scrolling;
            }
        }
        else
        {
            return false;
        }
    }

    public boolean mouseReleased(double pMouseX, double p_94754_, int pMouseY)
    {
        if (this.getFocused() != null)
        {
            this.getFocused().mouseReleased(pMouseX, p_94754_, pMouseY);
        }

        return false;
    }

    public boolean mouseDragged(double pMouseX, double p_94741_, int pMouseY, double p_94743_, double pButton)
    {
        if (super.mouseDragged(pMouseX, p_94741_, pMouseY, p_94743_, pButton))
        {
            return true;
        }
        else if (this.isVisible() && pMouseY == 0 && this.scrolling)
        {
            if (p_94741_ < (double)this.y0)
            {
                this.yo = 0.0D;
            }
            else if (p_94741_ > (double)this.y1)
            {
                this.yo = (double)this.getMaxScroll();
            }
            else
            {
                double d0 = (double)this.getMaxScroll();

                if (d0 < 1.0D)
                {
                    d0 = 1.0D;
                }

                int i = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
                i = Mth.clamp(i, 32, this.y1 - this.y0 - 8);
                double d1 = d0 / (double)(this.y1 - this.y0 - i);

                if (d1 < 1.0D)
                {
                    d1 = 1.0D;
                }

                this.yo += pButton * d1;
                this.capYPosition();
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean mouseScrolled(double pMouseX, double p_94735_, double pMouseY)
    {
        if (!this.isVisible())
        {
            return false;
        }
        else
        {
            this.yo -= pMouseY * (double)this.itemHeight / 2.0D;
            return true;
        }
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        if (!this.isVisible())
        {
            return false;
        }
        else if (super.keyPressed(pKeyCode, pScanCode, pModifiers))
        {
            return true;
        }
        else if (pKeyCode == 264)
        {
            this.moveSelection(1);
            return true;
        }
        else if (pKeyCode == 265)
        {
            this.moveSelection(-1);
            return true;
        }
        else
        {
            return false;
        }
    }

    protected void moveSelection(int p_moveSelection_1_)
    {
    }

    public boolean charTyped(char pCodePoint, int pModifiers)
    {
        return !this.isVisible() ? false : super.charTyped(pCodePoint, pModifiers);
    }

    public boolean isMouseOver(double pMouseX, double p_94749_)
    {
        return this.isMouseInList(pMouseX, p_94749_);
    }

    public int getRowWidth()
    {
        return 220;
    }

    protected void renderList(PoseStack matrixStackIn, int p_renderList_1_, int p_renderList_2_, int p_renderList_3_, int p_renderList_4_, float p_renderList_5_)
    {
        int i = this.getItemCount();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        for (int j = 0; j < i; ++j)
        {
            int k = p_renderList_2_ + j * this.itemHeight + this.headerHeight;
            int l = this.itemHeight - 4;

            if (k > this.y1 || k + l < this.y0)
            {
                this.updateItemPosition(j, p_renderList_1_, k, p_renderList_5_);
            }

            if (this.renderSelection && this.isSelectedItem(j))
            {
                int i1 = this.x0 + this.width / 2 - this.getRowWidth() / 2;
                int j1 = this.x0 + this.width / 2 + this.getRowWidth() / 2;
                RenderSystem.disableTexture();
                RenderSystem.setShader(GameRenderer::getPositionShader);
                float f = this.isFocused() ? 1.0F : 0.5F;
                RenderSystem.setShaderColor(f, f, f, 1.0F);
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                bufferbuilder.vertex((double)i1, (double)(k + l + 2), 0.0D).endVertex();
                bufferbuilder.vertex((double)j1, (double)(k + l + 2), 0.0D).endVertex();
                bufferbuilder.vertex((double)j1, (double)(k - 2), 0.0D).endVertex();
                bufferbuilder.vertex((double)i1, (double)(k - 2), 0.0D).endVertex();
                tesselator.end();
                RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                bufferbuilder.vertex((double)(i1 + 1), (double)(k + l + 1), 0.0D).endVertex();
                bufferbuilder.vertex((double)(j1 - 1), (double)(k + l + 1), 0.0D).endVertex();
                bufferbuilder.vertex((double)(j1 - 1), (double)(k - 1), 0.0D).endVertex();
                bufferbuilder.vertex((double)(i1 + 1), (double)(k - 1), 0.0D).endVertex();
                tesselator.end();
                RenderSystem.enableTexture();
            }

            if (k + this.itemHeight >= this.y0 && k <= this.y1)
            {
                this.renderItem(matrixStackIn, j, p_renderList_1_, k, l, p_renderList_3_, p_renderList_4_, p_renderList_5_);
            }
        }
    }

    protected boolean isFocused()
    {
        return false;
    }

    protected int getScrollbarPosition()
    {
        return this.width / 2 + 124;
    }

    protected void renderHoleBackground(int p_renderHoleBackground_1_, int p_renderHoleBackground_2_, int p_renderHoleBackground_3_, int p_renderHoleBackground_4_)
    {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex((double)this.x0, (double)p_renderHoleBackground_2_, 0.0D).uv(0.0F, (float)p_renderHoleBackground_2_ / 32.0F).color(64, 64, 64, p_renderHoleBackground_4_).endVertex();
        bufferbuilder.vertex((double)(this.x0 + this.width), (double)p_renderHoleBackground_2_, 0.0D).uv((float)this.width / 32.0F, (float)p_renderHoleBackground_2_ / 32.0F).color(64, 64, 64, p_renderHoleBackground_4_).endVertex();
        bufferbuilder.vertex((double)(this.x0 + this.width), (double)p_renderHoleBackground_1_, 0.0D).uv((float)this.width / 32.0F, (float)p_renderHoleBackground_1_ / 32.0F).color(64, 64, 64, p_renderHoleBackground_3_).endVertex();
        bufferbuilder.vertex((double)this.x0, (double)p_renderHoleBackground_1_, 0.0D).uv(0.0F, (float)p_renderHoleBackground_1_ / 32.0F).color(64, 64, 64, p_renderHoleBackground_3_).endVertex();
        tesselator.end();
    }

    public void setLeftPos(int p_setLeftPos_1_)
    {
        this.x0 = p_setLeftPos_1_;
        this.x1 = p_setLeftPos_1_ + this.width;
    }

    public int getItemHeight()
    {
        return this.itemHeight;
    }

    public NarratableEntry.NarrationPriority narrationPriority()
    {
        return NarratableEntry.NarrationPriority.HOVERED;
    }

    public void updateNarration(NarrationElementOutput p_169152_)
    {
    }
}
