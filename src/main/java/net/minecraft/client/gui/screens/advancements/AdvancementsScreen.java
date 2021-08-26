package net.minecraft.client.gui.screens.advancements;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;

public class AdvancementsScreen extends Screen implements ClientAdvancements.Listener
{
    private static final ResourceLocation WINDOW_LOCATION = new ResourceLocation("textures/gui/advancements/window.png");
    private static final ResourceLocation TABS_LOCATION = new ResourceLocation("textures/gui/advancements/tabs.png");
    public static final int WINDOW_WIDTH = 252;
    public static final int WINDOW_HEIGHT = 140;
    private static final int WINDOW_INSIDE_X = 9;
    private static final int WINDOW_INSIDE_Y = 18;
    public static final int WINDOW_INSIDE_WIDTH = 234;
    public static final int WINDOW_INSIDE_HEIGHT = 113;
    private static final int WINDOW_TITLE_X = 8;
    private static final int WINDOW_TITLE_Y = 6;
    public static final int BACKGROUND_TILE_WIDTH = 16;
    public static final int BACKGROUND_TILE_HEIGHT = 16;
    public static final int BACKGROUND_TILE_COUNT_X = 14;
    public static final int BACKGROUND_TILE_COUNT_Y = 7;
    private static final Component VERY_SAD_LABEL = new TranslatableComponent("advancements.sad_label");
    private static final Component NO_ADVANCEMENTS_LABEL = new TranslatableComponent("advancements.empty");
    private static final Component TITLE = new TranslatableComponent("gui.advancements");
    private final ClientAdvancements advancements;
    private final Map<Advancement, AdvancementTab> tabs = Maps.newLinkedHashMap();
    private AdvancementTab selectedTab;
    private boolean isScrolling;

    public AdvancementsScreen(ClientAdvancements p_97340_)
    {
        super(NarratorChatListener.NO_TITLE);
        this.advancements = p_97340_;
    }

    protected void init()
    {
        this.tabs.clear();
        this.selectedTab = null;
        this.advancements.setListener(this);

        if (this.selectedTab == null && !this.tabs.isEmpty())
        {
            this.advancements.setSelectedTab(this.tabs.values().iterator().next().getAdvancement(), true);
        }
        else
        {
            this.advancements.setSelectedTab(this.selectedTab == null ? null : this.selectedTab.getAdvancement(), true);
        }
    }

    public void removed()
    {
        this.advancements.setListener((ClientAdvancements.Listener)null);
        ClientPacketListener clientpacketlistener = this.minecraft.getConnection();

        if (clientpacketlistener != null)
        {
            clientpacketlistener.send(ServerboundSeenAdvancementsPacket.closedScreen());
        }
    }

    public boolean mouseClicked(double pMouseX, double p_97344_, int pMouseY)
    {
        if (pMouseY == 0)
        {
            int i = (this.width - 252) / 2;
            int j = (this.height - 140) / 2;

            for (AdvancementTab advancementtab : this.tabs.values())
            {
                if (advancementtab.isMouseOver(i, j, pMouseX, p_97344_))
                {
                    this.advancements.setSelectedTab(advancementtab.getAdvancement(), true);
                    break;
                }
            }
        }

        return super.mouseClicked(pMouseX, p_97344_, pMouseY);
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        if (this.minecraft.options.keyAdvancements.matches(pKeyCode, pScanCode))
        {
            this.minecraft.setScreen((Screen)null);
            this.minecraft.mouseHandler.grabMouse();
            return true;
        }
        else
        {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        int i = (this.width - 252) / 2;
        int j = (this.height - 140) / 2;
        this.renderBackground(pMatrixStack);
        this.renderInside(pMatrixStack, pMouseX, pMouseY, i, j);
        this.renderWindow(pMatrixStack, i, j);
        this.renderTooltips(pMatrixStack, pMouseX, pMouseY, i, j);
    }

    public boolean mouseDragged(double pMouseX, double p_97348_, int pMouseY, double p_97350_, double pButton)
    {
        if (pMouseY != 0)
        {
            this.isScrolling = false;
            return false;
        }
        else
        {
            if (!this.isScrolling)
            {
                this.isScrolling = true;
            }
            else if (this.selectedTab != null)
            {
                this.selectedTab.scroll(p_97350_, pButton);
            }

            return true;
        }
    }

    private void renderInside(PoseStack pMatrixStack, int pMouseX, int pMouseY, int pOffsetX, int pOffsetY)
    {
        AdvancementTab advancementtab = this.selectedTab;

        if (advancementtab == null)
        {
            fill(pMatrixStack, pOffsetX + 9, pOffsetY + 18, pOffsetX + 9 + 234, pOffsetY + 18 + 113, -16777216);
            int i = pOffsetX + 9 + 117;
            drawCenteredString(pMatrixStack, this.font, NO_ADVANCEMENTS_LABEL, i, pOffsetY + 18 + 56 - 9 / 2, -1);
            drawCenteredString(pMatrixStack, this.font, VERY_SAD_LABEL, i, pOffsetY + 18 + 113 - 9, -1);
        }
        else
        {
            PoseStack posestack = RenderSystem.getModelViewStack();
            posestack.pushPose();
            posestack.translate((double)(pOffsetX + 9), (double)(pOffsetY + 18), 0.0D);
            RenderSystem.applyModelViewMatrix();
            advancementtab.drawContents(pMatrixStack);
            posestack.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.depthFunc(515);
            RenderSystem.disableDepthTest();
        }
    }

    public void renderWindow(PoseStack pMatrixStack, int pOffsetX, int pOffsetY)
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WINDOW_LOCATION);
        this.blit(pMatrixStack, pOffsetX, pOffsetY, 0, 0, 252, 140);

        if (this.tabs.size() > 1)
        {
            RenderSystem.setShaderTexture(0, TABS_LOCATION);

            for (AdvancementTab advancementtab : this.tabs.values())
            {
                advancementtab.drawTab(pMatrixStack, pOffsetX, pOffsetY, advancementtab == this.selectedTab);
            }

            RenderSystem.defaultBlendFunc();

            for (AdvancementTab advancementtab1 : this.tabs.values())
            {
                advancementtab1.drawIcon(pOffsetX, pOffsetY, this.itemRenderer);
            }

            RenderSystem.disableBlend();
        }

        this.font.draw(pMatrixStack, TITLE, (float)(pOffsetX + 8), (float)(pOffsetY + 6), 4210752);
    }

    private void renderTooltips(PoseStack pMatrixStack, int pMouseX, int pMouseY, int pOffsetX, int pOffsetY)
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (this.selectedTab != null)
        {
            PoseStack posestack = RenderSystem.getModelViewStack();
            posestack.pushPose();
            posestack.translate((double)(pOffsetX + 9), (double)(pOffsetY + 18), 400.0D);
            RenderSystem.applyModelViewMatrix();
            RenderSystem.enableDepthTest();
            this.selectedTab.drawTooltips(pMatrixStack, pMouseX - pOffsetX - 9, pMouseY - pOffsetY - 18, pOffsetX, pOffsetY);
            RenderSystem.disableDepthTest();
            posestack.popPose();
            RenderSystem.applyModelViewMatrix();
        }

        if (this.tabs.size() > 1)
        {
            for (AdvancementTab advancementtab : this.tabs.values())
            {
                if (advancementtab.isMouseOver(pOffsetX, pOffsetY, (double)pMouseX, (double)pMouseY))
                {
                    this.renderTooltip(pMatrixStack, advancementtab.getTitle(), pMouseX, pMouseY);
                }
            }
        }
    }

    public void onAddAdvancementRoot(Advancement pAdvancement)
    {
        AdvancementTab advancementtab = AdvancementTab.create(this.minecraft, this, this.tabs.size(), pAdvancement);

        if (advancementtab != null)
        {
            this.tabs.put(pAdvancement, advancementtab);
        }
    }

    public void onRemoveAdvancementRoot(Advancement pAdvancement)
    {
    }

    public void onAddAdvancementTask(Advancement pAdvancement)
    {
        AdvancementTab advancementtab = this.getTab(pAdvancement);

        if (advancementtab != null)
        {
            advancementtab.addAdvancement(pAdvancement);
        }
    }

    public void onRemoveAdvancementTask(Advancement pAdvancement)
    {
    }

    public void onUpdateAdvancementProgress(Advancement pAdvancement, AdvancementProgress pProgress)
    {
        AdvancementWidget advancementwidget = this.getAdvancementWidget(pAdvancement);

        if (advancementwidget != null)
        {
            advancementwidget.setProgress(pProgress);
        }
    }

    public void onSelectedTabChanged(@Nullable Advancement pAdvancement)
    {
        this.selectedTab = this.tabs.get(pAdvancement);
    }

    public void onAdvancementsCleared()
    {
        this.tabs.clear();
        this.selectedTab = null;
    }

    @Nullable
    public AdvancementWidget getAdvancementWidget(Advancement pAdvancement)
    {
        AdvancementTab advancementtab = this.getTab(pAdvancement);
        return advancementtab == null ? null : advancementtab.getWidget(pAdvancement);
    }

    @Nullable
    private AdvancementTab getTab(Advancement pAdvancement)
    {
        while (pAdvancement.getParent() != null)
        {
            pAdvancement = pAdvancement.getParent();
        }

        return this.tabs.get(pAdvancement);
    }
}
