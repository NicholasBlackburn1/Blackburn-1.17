package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class AdvancementTab extends GuiComponent
{
    private final Minecraft minecraft;
    private final AdvancementsScreen screen;
    private final AdvancementTabType type;
    private final int index;
    private final Advancement advancement;
    private final DisplayInfo display;
    private final ItemStack icon;
    private final Component title;
    private final AdvancementWidget root;
    private final Map<Advancement, AdvancementWidget> widgets = Maps.newLinkedHashMap();
    private double scrollX;
    private double scrollY;
    private int minX = Integer.MAX_VALUE;
    private int minY = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int maxY = Integer.MIN_VALUE;
    private float fade;
    private boolean centered;

    public AdvancementTab(Minecraft p_97145_, AdvancementsScreen p_97146_, AdvancementTabType p_97147_, int p_97148_, Advancement p_97149_, DisplayInfo p_97150_)
    {
        this.minecraft = p_97145_;
        this.screen = p_97146_;
        this.type = p_97147_;
        this.index = p_97148_;
        this.advancement = p_97149_;
        this.display = p_97150_;
        this.icon = p_97150_.getIcon();
        this.title = p_97150_.getTitle();
        this.root = new AdvancementWidget(this, p_97145_, p_97149_, p_97150_);
        this.addWidget(this.root, p_97149_);
    }

    public AdvancementTabType getType()
    {
        return this.type;
    }

    public int getIndex()
    {
        return this.index;
    }

    public Advancement getAdvancement()
    {
        return this.advancement;
    }

    public Component getTitle()
    {
        return this.title;
    }

    public DisplayInfo getDisplay()
    {
        return this.display;
    }

    public void drawTab(PoseStack pMatrixStack, int pOffsetX, int pOffsetY, boolean pIsSelected)
    {
        this.type.draw(pMatrixStack, this, pOffsetX, pOffsetY, pIsSelected, this.index);
    }

    public void drawIcon(int pOffsetX, int pOffsetY, ItemRenderer pRenderer)
    {
        this.type.drawIcon(pOffsetX, pOffsetY, this.index, pRenderer, this.icon);
    }

    public void drawContents(PoseStack pMatrixStack)
    {
        if (!this.centered)
        {
            this.scrollX = (double)(117 - (this.maxX + this.minX) / 2);
            this.scrollY = (double)(56 - (this.maxY + this.minY) / 2);
            this.centered = true;
        }

        pMatrixStack.pushPose();
        pMatrixStack.translate(0.0D, 0.0D, 950.0D);
        RenderSystem.enableDepthTest();
        RenderSystem.colorMask(false, false, false, false);
        fill(pMatrixStack, 4680, 2260, -4680, -2260, -16777216);
        RenderSystem.colorMask(true, true, true, true);
        pMatrixStack.translate(0.0D, 0.0D, -950.0D);
        RenderSystem.depthFunc(518);
        fill(pMatrixStack, 234, 113, 0, 0, -16777216);
        RenderSystem.depthFunc(515);
        ResourceLocation resourcelocation = this.display.getBackground();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        if (resourcelocation != null)
        {
            RenderSystem.setShaderTexture(0, resourcelocation);
        }
        else
        {
            RenderSystem.setShaderTexture(0, TextureManager.INTENTIONAL_MISSING_TEXTURE);
        }

        int i = Mth.floor(this.scrollX);
        int j = Mth.floor(this.scrollY);
        int k = i % 16;
        int l = j % 16;

        for (int i1 = -1; i1 <= 15; ++i1)
        {
            for (int j1 = -1; j1 <= 8; ++j1)
            {
                blit(pMatrixStack, k + 16 * i1, l + 16 * j1, 0.0F, 0.0F, 16, 16, 16, 16);
            }
        }

        this.root.drawConnectivity(pMatrixStack, i, j, true);
        this.root.drawConnectivity(pMatrixStack, i, j, false);
        this.root.draw(pMatrixStack, i, j);
        RenderSystem.depthFunc(518);
        pMatrixStack.translate(0.0D, 0.0D, -950.0D);
        RenderSystem.colorMask(false, false, false, false);
        fill(pMatrixStack, 4680, 2260, -4680, -2260, -16777216);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthFunc(515);
        pMatrixStack.popPose();
    }

    public void drawTooltips(PoseStack pMatrixStack, int pMouseX, int pMouseY, int pWidth, int pHeight)
    {
        pMatrixStack.pushPose();
        pMatrixStack.translate(0.0D, 0.0D, -200.0D);
        fill(pMatrixStack, 0, 0, 234, 113, Mth.floor(this.fade * 255.0F) << 24);
        boolean flag = false;
        int i = Mth.floor(this.scrollX);
        int j = Mth.floor(this.scrollY);

        if (pMouseX > 0 && pMouseX < 234 && pMouseY > 0 && pMouseY < 113)
        {
            for (AdvancementWidget advancementwidget : this.widgets.values())
            {
                if (advancementwidget.isMouseOver(i, j, pMouseX, pMouseY))
                {
                    flag = true;
                    advancementwidget.drawHover(pMatrixStack, i, j, this.fade, pWidth, pHeight);
                    break;
                }
            }
        }

        pMatrixStack.popPose();

        if (flag)
        {
            this.fade = Mth.clamp(this.fade + 0.02F, 0.0F, 0.3F);
        }
        else
        {
            this.fade = Mth.clamp(this.fade - 0.04F, 0.0F, 1.0F);
        }
    }

    public boolean isMouseOver(int pOffsetX, int pOffsetY, double pMouseX, double p_97158_)
    {
        return this.type.isMouseOver(pOffsetX, pOffsetY, this.index, pMouseX, p_97158_);
    }

    @Nullable
    public static AdvancementTab create(Minecraft pMinecraft, AdvancementsScreen pScreen, int pTabIndex, Advancement pAdvancement)
    {
        if (pAdvancement.getDisplay() == null)
        {
            return null;
        }
        else
        {
            for (AdvancementTabType advancementtabtype : AdvancementTabType.values())
            {
                if (pTabIndex < advancementtabtype.getMax())
                {
                    return new AdvancementTab(pMinecraft, pScreen, advancementtabtype, pTabIndex, pAdvancement, pAdvancement.getDisplay());
                }

                pTabIndex -= advancementtabtype.getMax();
            }

            return null;
        }
    }

    public void scroll(double pDragX, double p_97153_)
    {
        if (this.maxX - this.minX > 234)
        {
            this.scrollX = Mth.clamp(this.scrollX + pDragX, (double)(-(this.maxX - 234)), 0.0D);
        }

        if (this.maxY - this.minY > 113)
        {
            this.scrollY = Mth.clamp(this.scrollY + p_97153_, (double)(-(this.maxY - 113)), 0.0D);
        }
    }

    public void addAdvancement(Advancement pAdvancement)
    {
        if (pAdvancement.getDisplay() != null)
        {
            AdvancementWidget advancementwidget = new AdvancementWidget(this, this.minecraft, pAdvancement, pAdvancement.getDisplay());
            this.addWidget(advancementwidget, pAdvancement);
        }
    }

    private void addWidget(AdvancementWidget pGui, Advancement pAdvancement)
    {
        this.widgets.put(pAdvancement, pGui);
        int i = pGui.getX();
        int j = i + 28;
        int k = pGui.getY();
        int l = k + 27;
        this.minX = Math.min(this.minX, i);
        this.maxX = Math.max(this.maxX, j);
        this.minY = Math.min(this.minY, k);
        this.maxY = Math.max(this.maxY, l);

        for (AdvancementWidget advancementwidget : this.widgets.values())
        {
            advancementwidget.attachToParent();
        }
    }

    @Nullable
    public AdvancementWidget getWidget(Advancement pAdvancement)
    {
        return this.widgets.get(pAdvancement);
    }

    public AdvancementsScreen getScreen()
    {
        return this.screen;
    }
}
