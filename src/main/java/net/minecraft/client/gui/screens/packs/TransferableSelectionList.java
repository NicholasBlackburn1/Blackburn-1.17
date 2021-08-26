package net.minecraft.client.gui.screens.packs;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.util.FormattedCharSequence;

public class TransferableSelectionList extends ObjectSelectionList<TransferableSelectionList.PackEntry>
{
    static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/resource_packs.png");
    static final Component INCOMPATIBLE_TITLE = new TranslatableComponent("pack.incompatible");
    static final Component INCOMPATIBLE_CONFIRM_TITLE = new TranslatableComponent("pack.incompatible.confirm.title");
    private final Component title;

    public TransferableSelectionList(Minecraft p_100058_, int p_100059_, int p_100060_, Component p_100061_)
    {
        super(p_100058_, p_100059_, p_100060_, 32, p_100060_ - 55 + 4, 36);
        this.title = p_100061_;
        this.centerListVertically = false;
        this.setRenderHeader(true, (int)(9.0F * 1.5F));
    }

    protected void renderHeader(PoseStack pMatrixStack, int pX, int pY, Tesselator pTessellator)
    {
        Component component = (new TextComponent("")).append(this.title).m_130944_(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
        this.minecraft.font.draw(pMatrixStack, component, (float)(pX + this.width / 2 - this.minecraft.font.width(component) / 2), (float)Math.min(this.y0 + 3, pY), 16777215);
    }

    public int getRowWidth()
    {
        return this.width;
    }

    protected int getScrollbarPosition()
    {
        return this.x1 - 6;
    }

    public static class PackEntry extends ObjectSelectionList.Entry<TransferableSelectionList.PackEntry>
    {
        private static final int ICON_OVERLAY_X_MOVE_RIGHT = 0;
        private static final int ICON_OVERLAY_X_MOVE_LEFT = 32;
        private static final int ICON_OVERLAY_X_MOVE_DOWN = 64;
        private static final int ICON_OVERLAY_X_MOVE_UP = 96;
        private static final int ICON_OVERLAY_Y_UNSELECTED = 0;
        private static final int ICON_OVERLAY_Y_SELECTED = 32;
        private static final int MAX_DESCRIPTION_WIDTH_PIXELS = 157;
        private static final int MAX_NAME_WIDTH_PIXELS = 157;
        private static final String TOO_LONG_NAME_SUFFIX = "...";
        private final TransferableSelectionList parent;
        protected final Minecraft minecraft;
        protected final Screen screen;
        private final PackSelectionModel.Entry pack;
        private final FormattedCharSequence nameDisplayCache;
        private final MultiLineLabel descriptionDisplayCache;
        private final FormattedCharSequence incompatibleNameDisplayCache;
        private final MultiLineLabel incompatibleDescriptionDisplayCache;

        public PackEntry(Minecraft p_100084_, TransferableSelectionList p_100085_, Screen p_100086_, PackSelectionModel.Entry p_100087_)
        {
            this.minecraft = p_100084_;
            this.screen = p_100086_;
            this.pack = p_100087_;
            this.parent = p_100085_;
            this.nameDisplayCache = cacheName(p_100084_, p_100087_.getTitle());
            this.descriptionDisplayCache = cacheDescription(p_100084_, p_100087_.getExtendedDescription());
            this.incompatibleNameDisplayCache = cacheName(p_100084_, TransferableSelectionList.INCOMPATIBLE_TITLE);
            this.incompatibleDescriptionDisplayCache = cacheDescription(p_100084_, p_100087_.getCompatibility().getDescription());
        }

        private static FormattedCharSequence cacheName(Minecraft p_100105_, Component p_100106_)
        {
            int i = p_100105_.font.width(p_100106_);

            if (i > 157)
            {
                FormattedText formattedtext = FormattedText.m_130773_(p_100105_.font.substrByWidth(p_100106_, 157 - p_100105_.font.width("...")), FormattedText.of("..."));
                return Language.getInstance().getVisualOrder(formattedtext);
            }
            else
            {
                return p_100106_.getVisualOrderText();
            }
        }

        private static MultiLineLabel cacheDescription(Minecraft p_100110_, Component p_100111_)
        {
            return MultiLineLabel.create(p_100110_.font, p_100111_, 157, 2);
        }

        public Component getNarration()
        {
            return new TranslatableComponent("narrator.select", this.pack.getTitle());
        }

        public void render(PoseStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks)
        {
            PackCompatibility packcompatibility = this.pack.getCompatibility();

            if (!packcompatibility.isCompatible())
            {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                GuiComponent.fill(pMatrixStack, pLeft - 1, pTop - 1, pLeft + pWidth - 9, pTop + pHeight + 1, -8978432);
            }

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, this.pack.getIconTexture());
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            GuiComponent.blit(pMatrixStack, pLeft, pTop, 0.0F, 0.0F, 32, 32, 32, 32);
            FormattedCharSequence formattedcharsequence = this.nameDisplayCache;
            MultiLineLabel multilinelabel = this.descriptionDisplayCache;

            if (this.showHoverOverlay() && (this.minecraft.options.touchscreen || pIsMouseOver))
            {
                RenderSystem.setShaderTexture(0, TransferableSelectionList.ICON_OVERLAY_LOCATION);
                GuiComponent.fill(pMatrixStack, pLeft, pTop, pLeft + 32, pTop + 32, -1601138544);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                int i = pMouseX - pLeft;
                int j = pMouseY - pTop;

                if (!this.pack.getCompatibility().isCompatible())
                {
                    formattedcharsequence = this.incompatibleNameDisplayCache;
                    multilinelabel = this.incompatibleDescriptionDisplayCache;
                }

                if (this.pack.canSelect())
                {
                    if (i < 32)
                    {
                        GuiComponent.blit(pMatrixStack, pLeft, pTop, 0.0F, 32.0F, 32, 32, 256, 256);
                    }
                    else
                    {
                        GuiComponent.blit(pMatrixStack, pLeft, pTop, 0.0F, 0.0F, 32, 32, 256, 256);
                    }
                }
                else
                {
                    if (this.pack.canUnselect())
                    {
                        if (i < 16)
                        {
                            GuiComponent.blit(pMatrixStack, pLeft, pTop, 32.0F, 32.0F, 32, 32, 256, 256);
                        }
                        else
                        {
                            GuiComponent.blit(pMatrixStack, pLeft, pTop, 32.0F, 0.0F, 32, 32, 256, 256);
                        }
                    }

                    if (this.pack.canMoveUp())
                    {
                        if (i < 32 && i > 16 && j < 16)
                        {
                            GuiComponent.blit(pMatrixStack, pLeft, pTop, 96.0F, 32.0F, 32, 32, 256, 256);
                        }
                        else
                        {
                            GuiComponent.blit(pMatrixStack, pLeft, pTop, 96.0F, 0.0F, 32, 32, 256, 256);
                        }
                    }

                    if (this.pack.canMoveDown())
                    {
                        if (i < 32 && i > 16 && j > 16)
                        {
                            GuiComponent.blit(pMatrixStack, pLeft, pTop, 64.0F, 32.0F, 32, 32, 256, 256);
                        }
                        else
                        {
                            GuiComponent.blit(pMatrixStack, pLeft, pTop, 64.0F, 0.0F, 32, 32, 256, 256);
                        }
                    }
                }
            }

            this.minecraft.font.drawShadow(pMatrixStack, formattedcharsequence, (float)(pLeft + 32 + 2), (float)(pTop + 1), 16777215);
            multilinelabel.renderLeftAligned(pMatrixStack, pLeft + 32 + 2, pTop + 12, 10, 8421504);
        }

        private boolean showHoverOverlay()
        {
            return !this.pack.isFixedPosition() || !this.pack.isRequired();
        }

        public boolean mouseClicked(double pMouseX, double p_100091_, int pMouseY)
        {
            double d0 = pMouseX - (double)this.parent.getRowLeft();
            double d1 = p_100091_ - (double)this.parent.getRowTop(this.parent.children().indexOf(this));

            if (this.showHoverOverlay() && d0 <= 32.0D)
            {
                if (this.pack.canSelect())
                {
                    PackCompatibility packcompatibility = this.pack.getCompatibility();

                    if (packcompatibility.isCompatible())
                    {
                        this.pack.select();
                    }
                    else
                    {
                        Component component = packcompatibility.getConfirmation();
                        this.minecraft.setScreen(new ConfirmScreen((p_100108_) ->
                        {
                            this.minecraft.setScreen(this.screen);

                            if (p_100108_)
                            {
                                this.pack.select();
                            }
                        }, TransferableSelectionList.INCOMPATIBLE_CONFIRM_TITLE, component));
                    }

                    return true;
                }

                if (d0 < 16.0D && this.pack.canUnselect())
                {
                    this.pack.unselect();
                    return true;
                }

                if (d0 > 16.0D && d1 < 16.0D && this.pack.canMoveUp())
                {
                    this.pack.moveUp();
                    return true;
                }

                if (d0 > 16.0D && d1 > 16.0D && this.pack.canMoveDown())
                {
                    this.pack.moveDown();
                    return true;
                }
            }

            return false;
        }
    }
}
