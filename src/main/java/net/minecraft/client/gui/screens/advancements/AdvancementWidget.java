package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class AdvancementWidget extends GuiComponent
{
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/advancements/widgets.png");
    private static final int HEIGHT = 26;
    private static final int BOX_X = 0;
    private static final int BOX_WIDTH = 200;
    private static final int FRAME_WIDTH = 26;
    private static final int ICON_X = 8;
    private static final int ICON_Y = 5;
    private static final int ICON_WIDTH = 26;
    private static final int TITLE_PADDING_LEFT = 3;
    private static final int TITLE_PADDING_RIGHT = 5;
    private static final int TITLE_X = 32;
    private static final int TITLE_Y = 9;
    private static final int TITLE_MAX_WIDTH = 163;
    private static final int[] TEST_SPLIT_OFFSETS = new int[] {0, 10, -10, 25, -25};
    private final AdvancementTab tab;
    private final Advancement advancement;
    private final DisplayInfo display;
    private final FormattedCharSequence title;
    private final int width;
    private final List<FormattedCharSequence> description;
    private final Minecraft minecraft;
    private AdvancementWidget parent;
    private final List<AdvancementWidget> children = Lists.newArrayList();
    private AdvancementProgress progress;
    private final int x;
    private final int y;

    public AdvancementWidget(AdvancementTab p_97255_, Minecraft p_97256_, Advancement p_97257_, DisplayInfo p_97258_)
    {
        this.tab = p_97255_;
        this.advancement = p_97257_;
        this.display = p_97258_;
        this.minecraft = p_97256_;
        this.title = Language.getInstance().getVisualOrder(p_97256_.font.substrByWidth(p_97258_.getTitle(), 163));
        this.x = Mth.floor(p_97258_.getX() * 28.0F);
        this.y = Mth.floor(p_97258_.getY() * 27.0F);
        int i = p_97257_.getMaxCriteraRequired();
        int j = String.valueOf(i).length();
        int k = i > 1 ? p_97256_.font.width("  ") + p_97256_.font.width("0") * j * 2 + p_97256_.font.width("/") : 0;
        int l = 29 + p_97256_.font.width(this.title) + k;
        this.description = Language.getInstance().getVisualOrder(this.findOptimalLines(ComponentUtils.mergeStyles(p_97258_.getDescription().copy(), Style.EMPTY.withColor(p_97258_.getFrame().getChatColor())), l));

        for (FormattedCharSequence formattedcharsequence : this.description)
        {
            l = Math.max(l, p_97256_.font.width(formattedcharsequence));
        }

        this.width = l + 3 + 5;
    }

    private static float getMaxWidth(StringSplitter pManager, List<FormattedText> pText)
    {
        return (float)pText.stream().mapToDouble(pManager::stringWidth).max().orElse(0.0D);
    }

    private List<FormattedText> findOptimalLines(Component pComponent, int pMaxWidth)
    {
        StringSplitter stringsplitter = this.minecraft.font.getSplitter();
        List<FormattedText> list = null;
        float f = Float.MAX_VALUE;

        for (int i : TEST_SPLIT_OFFSETS)
        {
            List<FormattedText> list1 = stringsplitter.splitLines(pComponent, pMaxWidth - i, Style.EMPTY);
            float f1 = Math.abs(getMaxWidth(stringsplitter, list1) - (float)pMaxWidth);

            if (f1 <= 10.0F)
            {
                return list1;
            }

            if (f1 < f)
            {
                f = f1;
                list = list1;
            }
        }

        return list;
    }

    @Nullable
    private AdvancementWidget getFirstVisibleParent(Advancement pAdvancement)
    {
        do
        {
            pAdvancement = pAdvancement.getParent();
        }
        while (pAdvancement != null && pAdvancement.getDisplay() == null);

        return pAdvancement != null && pAdvancement.getDisplay() != null ? this.tab.getWidget(pAdvancement) : null;
    }

    public void drawConnectivity(PoseStack pMatrixStack, int pX, int pY, boolean pDropShadow)
    {
        if (this.parent != null)
        {
            int i = pX + this.parent.x + 13;
            int j = pX + this.parent.x + 26 + 4;
            int k = pY + this.parent.y + 13;
            int l = pX + this.x + 13;
            int i1 = pY + this.y + 13;
            int j1 = pDropShadow ? -16777216 : -1;

            if (pDropShadow)
            {
                this.hLine(pMatrixStack, j, i, k - 1, j1);
                this.hLine(pMatrixStack, j + 1, i, k, j1);
                this.hLine(pMatrixStack, j, i, k + 1, j1);
                this.hLine(pMatrixStack, l, j - 1, i1 - 1, j1);
                this.hLine(pMatrixStack, l, j - 1, i1, j1);
                this.hLine(pMatrixStack, l, j - 1, i1 + 1, j1);
                this.vLine(pMatrixStack, j - 1, i1, k, j1);
                this.vLine(pMatrixStack, j + 1, i1, k, j1);
            }
            else
            {
                this.hLine(pMatrixStack, j, i, k, j1);
                this.hLine(pMatrixStack, l, j, i1, j1);
                this.vLine(pMatrixStack, j, i1, k, j1);
            }
        }

        for (AdvancementWidget advancementwidget : this.children)
        {
            advancementwidget.drawConnectivity(pMatrixStack, pX, pY, pDropShadow);
        }
    }

    public void draw(PoseStack pMatrixStack, int pX, int pY)
    {
        if (!this.display.isHidden() || this.progress != null && this.progress.isDone())
        {
            float f = this.progress == null ? 0.0F : this.progress.getPercent();
            AdvancementWidgetType advancementwidgettype;

            if (f >= 1.0F)
            {
                advancementwidgettype = AdvancementWidgetType.OBTAINED;
            }
            else
            {
                advancementwidgettype = AdvancementWidgetType.UNOBTAINED;
            }

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
            this.blit(pMatrixStack, pX + this.x + 3, pY + this.y, this.display.getFrame().getTexture(), 128 + advancementwidgettype.getIndex() * 26, 26, 26);
            this.minecraft.getItemRenderer().renderAndDecorateFakeItem(this.display.getIcon(), pX + this.x + 8, pY + this.y + 5);
        }

        for (AdvancementWidget advancementwidget : this.children)
        {
            advancementwidget.draw(pMatrixStack, pX, pY);
        }
    }

    public int getWidth()
    {
        return this.width;
    }

    public void setProgress(AdvancementProgress pAdvancementProgress)
    {
        this.progress = pAdvancementProgress;
    }

    public void addChild(AdvancementWidget pGuiAdvancement)
    {
        this.children.add(pGuiAdvancement);
    }

    public void drawHover(PoseStack pMatrixStack, int pX, int pY, float pFade, int pWidth, int pHeight)
    {
        boolean flag = pWidth + pX + this.x + this.width + 26 >= this.tab.getScreen().width;
        String s = this.progress == null ? null : this.progress.getProgressText();
        int i = s == null ? 0 : this.minecraft.font.width(s);
        boolean flag1 = 113 - pY - this.y - 26 <= 6 + this.description.size() * 9;
        float f = this.progress == null ? 0.0F : this.progress.getPercent();
        int j = Mth.floor(f * (float)this.width);
        AdvancementWidgetType advancementwidgettype;
        AdvancementWidgetType advancementwidgettype1;
        AdvancementWidgetType advancementwidgettype2;

        if (f >= 1.0F)
        {
            j = this.width / 2;
            advancementwidgettype = AdvancementWidgetType.OBTAINED;
            advancementwidgettype1 = AdvancementWidgetType.OBTAINED;
            advancementwidgettype2 = AdvancementWidgetType.OBTAINED;
        }
        else if (j < 2)
        {
            j = this.width / 2;
            advancementwidgettype = AdvancementWidgetType.UNOBTAINED;
            advancementwidgettype1 = AdvancementWidgetType.UNOBTAINED;
            advancementwidgettype2 = AdvancementWidgetType.UNOBTAINED;
        }
        else if (j > this.width - 2)
        {
            j = this.width / 2;
            advancementwidgettype = AdvancementWidgetType.OBTAINED;
            advancementwidgettype1 = AdvancementWidgetType.OBTAINED;
            advancementwidgettype2 = AdvancementWidgetType.UNOBTAINED;
        }
        else
        {
            advancementwidgettype = AdvancementWidgetType.OBTAINED;
            advancementwidgettype1 = AdvancementWidgetType.UNOBTAINED;
            advancementwidgettype2 = AdvancementWidgetType.UNOBTAINED;
        }

        int k = this.width - j;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        int l = pY + this.y;
        int i1;

        if (flag)
        {
            i1 = pX + this.x - this.width + 26 + 6;
        }
        else
        {
            i1 = pX + this.x;
        }

        int j1 = 32 + this.description.size() * 9;

        if (!this.description.isEmpty())
        {
            if (flag1)
            {
                this.render9Sprite(pMatrixStack, i1, l + 26 - j1, this.width, j1, 10, 200, 26, 0, 52);
            }
            else
            {
                this.render9Sprite(pMatrixStack, i1, l, this.width, j1, 10, 200, 26, 0, 52);
            }
        }

        this.blit(pMatrixStack, i1, l, 0, advancementwidgettype.getIndex() * 26, j, 26);
        this.blit(pMatrixStack, i1 + j, l, 200 - k, advancementwidgettype1.getIndex() * 26, k, 26);
        this.blit(pMatrixStack, pX + this.x + 3, pY + this.y, this.display.getFrame().getTexture(), 128 + advancementwidgettype2.getIndex() * 26, 26, 26);

        if (flag)
        {
            this.minecraft.font.drawShadow(pMatrixStack, this.title, (float)(i1 + 5), (float)(pY + this.y + 9), -1);

            if (s != null)
            {
                this.minecraft.font.drawShadow(pMatrixStack, s, (float)(pX + this.x - i), (float)(pY + this.y + 9), -1);
            }
        }
        else
        {
            this.minecraft.font.drawShadow(pMatrixStack, this.title, (float)(pX + this.x + 32), (float)(pY + this.y + 9), -1);

            if (s != null)
            {
                this.minecraft.font.drawShadow(pMatrixStack, s, (float)(pX + this.x + this.width - i - 5), (float)(pY + this.y + 9), -1);
            }
        }

        if (flag1)
        {
            for (int k1 = 0; k1 < this.description.size(); ++k1)
            {
                this.minecraft.font.draw(pMatrixStack, this.description.get(k1), (float)(i1 + 5), (float)(l + 26 - j1 + 7 + k1 * 9), -5592406);
            }
        }
        else
        {
            for (int l1 = 0; l1 < this.description.size(); ++l1)
            {
                this.minecraft.font.draw(pMatrixStack, this.description.get(l1), (float)(i1 + 5), (float)(pY + this.y + 9 + 17 + l1 * 9), -5592406);
            }
        }

        this.minecraft.getItemRenderer().renderAndDecorateFakeItem(this.display.getIcon(), pX + this.x + 8, pY + this.y + 5);
    }

    protected void render9Sprite(PoseStack pMatrixStack, int pX, int pY, int pWidth, int pHeight, int pPadding, int pUWidth, int pVHeight, int pUOffset, int pVOffset)
    {
        this.blit(pMatrixStack, pX, pY, pUOffset, pVOffset, pPadding, pPadding);
        this.renderRepeating(pMatrixStack, pX + pPadding, pY, pWidth - pPadding - pPadding, pPadding, pUOffset + pPadding, pVOffset, pUWidth - pPadding - pPadding, pVHeight);
        this.blit(pMatrixStack, pX + pWidth - pPadding, pY, pUOffset + pUWidth - pPadding, pVOffset, pPadding, pPadding);
        this.blit(pMatrixStack, pX, pY + pHeight - pPadding, pUOffset, pVOffset + pVHeight - pPadding, pPadding, pPadding);
        this.renderRepeating(pMatrixStack, pX + pPadding, pY + pHeight - pPadding, pWidth - pPadding - pPadding, pPadding, pUOffset + pPadding, pVOffset + pVHeight - pPadding, pUWidth - pPadding - pPadding, pVHeight);
        this.blit(pMatrixStack, pX + pWidth - pPadding, pY + pHeight - pPadding, pUOffset + pUWidth - pPadding, pVOffset + pVHeight - pPadding, pPadding, pPadding);
        this.renderRepeating(pMatrixStack, pX, pY + pPadding, pPadding, pHeight - pPadding - pPadding, pUOffset, pVOffset + pPadding, pUWidth, pVHeight - pPadding - pPadding);
        this.renderRepeating(pMatrixStack, pX + pPadding, pY + pPadding, pWidth - pPadding - pPadding, pHeight - pPadding - pPadding, pUOffset + pPadding, pVOffset + pPadding, pUWidth - pPadding - pPadding, pVHeight - pPadding - pPadding);
        this.renderRepeating(pMatrixStack, pX + pWidth - pPadding, pY + pPadding, pPadding, pHeight - pPadding - pPadding, pUOffset + pUWidth - pPadding, pVOffset + pPadding, pUWidth, pVHeight - pPadding - pPadding);
    }

    protected void renderRepeating(PoseStack pMatrixStack, int pX, int pY, int pBorderToU, int pBorderToV, int pUOffset, int pVOffset, int pUWidth, int pVHeight)
    {
        for (int i = 0; i < pBorderToU; i += pUWidth)
        {
            int j = pX + i;
            int k = Math.min(pUWidth, pBorderToU - i);

            for (int l = 0; l < pBorderToV; l += pVHeight)
            {
                int i1 = pY + l;
                int j1 = Math.min(pVHeight, pBorderToV - l);
                this.blit(pMatrixStack, j, i1, pUOffset, pVOffset, k, j1);
            }
        }
    }

    public boolean isMouseOver(int pX, int pY, int pMouseX, int pMouseY)
    {
        if (!this.display.isHidden() || this.progress != null && this.progress.isDone())
        {
            int i = pX + this.x;
            int j = i + 26;
            int k = pY + this.y;
            int l = k + 26;
            return pMouseX >= i && pMouseX <= j && pMouseY >= k && pMouseY <= l;
        }
        else
        {
            return false;
        }
    }

    public void attachToParent()
    {
        if (this.parent == null && this.advancement.getParent() != null)
        {
            this.parent = this.getFirstVisibleParent(this.advancement);

            if (this.parent != null)
            {
                this.parent.addChild(this);
            }
        }
    }

    public int getY()
    {
        return this.y;
    }

    public int getX()
    {
        return this.x;
    }
}
