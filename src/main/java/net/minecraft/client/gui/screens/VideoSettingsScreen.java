package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.optifine.Config;
import net.optifine.Lang;
import net.optifine.gui.GuiAnimationSettingsOF;
import net.optifine.gui.GuiButtonOF;
import net.optifine.gui.GuiDetailSettingsOF;
import net.optifine.gui.GuiOtherSettingsOF;
import net.optifine.gui.GuiPerformanceSettingsOF;
import net.optifine.gui.GuiQualitySettingsOF;
import net.optifine.gui.GuiScreenButtonOF;
import net.optifine.gui.GuiScreenOF;
import net.optifine.gui.TooltipManager;
import net.optifine.gui.TooltipProviderOptions;
import net.optifine.shaders.gui.GuiShaders;
import net.optifine.util.GuiUtils;
import org.lwjgl.glfw.GLFW;

public class VideoSettingsScreen extends GuiScreenOF
{
    private Screen parentGuiScreen;
    private Options guiGameSettings;
    private static Option[] videoOptions = new Option[] {Option.GRAPHICS, Option.RENDER_DISTANCE, Option.AMBIENT_OCCLUSION, Option.FRAMERATE_LIMIT, Option.AO_LEVEL, Option.VIEW_BOBBING, Option.GUI_SCALE, Option.ENTITY_SHADOWS, Option.GAMMA, Option.ATTACK_INDICATOR, Option.DYNAMIC_LIGHTS, Option.DYNAMIC_FOV};
    private GpuWarnlistManager gpuWarnlistManager;
    private static final Component FABULOUS = (new TranslatableComponent("options.graphics.fabulous")).withStyle(ChatFormatting.ITALIC);
    private static final Component WARNING_MESSAGE = new TranslatableComponent("options.graphics.warning.message", FABULOUS, FABULOUS);
    private static final Component WARNING_TITLE = (new TranslatableComponent("options.graphics.warning.title")).withStyle(ChatFormatting.RED);
    private static final Component BUTTON_ACCEPT = new TranslatableComponent("options.graphics.warning.accept");
    private static final Component BUTTON_CANCEL = new TranslatableComponent("options.graphics.warning.cancel");
    private static final Component NEW_LINE = new TextComponent("\n");
    private TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderOptions());
    private List<AbstractWidget> buttonList = this.getButtonList();
    private AbstractWidget buttonGuiScale;

    public VideoSettingsScreen(Screen p_96806_, Options p_96807_)
    {
        super(new TranslatableComponent("options.videoTitle"));
        this.parentGuiScreen = p_96806_;
        this.guiGameSettings = p_96807_;
        this.gpuWarnlistManager = this.parentGuiScreen.minecraft.getGpuWarnlistManager();
        this.gpuWarnlistManager.resetWarnings();

        if (this.guiGameSettings.graphicsMode == GraphicsStatus.FABULOUS)
        {
            this.gpuWarnlistManager.dismissWarning();
        }
    }

    public void init()
    {
        this.buttonList.clear();

        for (int i = 0; i < videoOptions.length; ++i)
        {
            Option option = videoOptions[i];

            if (option != null)
            {
                int j = this.width / 2 - 155 + i % 2 * 160;
                int k = this.height / 6 + 21 * (i / 2) - 12;
                AbstractWidget abstractwidget = this.addRenderableWidget(option.createButton(this.minecraft.options, j, k, 150));

                if (option == Option.GUI_SCALE)
                {
                    this.buttonGuiScale = abstractwidget;
                }
            }
        }

        int l = this.height / 6 + 21 * (videoOptions.length / 2) - 12;
        int i1 = 0;
        i1 = this.width / 2 - 155 + 0;
        this.addRenderableWidget(new GuiScreenButtonOF(231, i1, l, Lang.get("of.options.shaders")));
        i1 = this.width / 2 - 155 + 160;
        this.addRenderableWidget(new GuiScreenButtonOF(202, i1, l, Lang.get("of.options.quality")));
        l = l + 21;
        i1 = this.width / 2 - 155 + 0;
        this.addRenderableWidget(new GuiScreenButtonOF(201, i1, l, Lang.get("of.options.details")));
        i1 = this.width / 2 - 155 + 160;
        this.addRenderableWidget(new GuiScreenButtonOF(212, i1, l, Lang.get("of.options.performance")));
        l = l + 21;
        i1 = this.width / 2 - 155 + 0;
        this.addRenderableWidget(new GuiScreenButtonOF(211, i1, l, Lang.get("of.options.animations")));
        i1 = this.width / 2 - 155 + 160;
        this.addRenderableWidget(new GuiScreenButtonOF(222, i1, l, Lang.get("of.options.other")));
        l = l + 21;
        this.addRenderableWidget(new GuiButtonOF(200, this.width / 2 - 100, this.height / 6 + 168 + 11, I18n.m_118938_("gui.done")));
    }

    protected void actionPerformed(AbstractWidget button)
    {
        if (button == this.buttonGuiScale)
        {
            this.updateGuiScale();
        }

        this.checkFabulousWarning();

        if (button instanceof GuiButtonOF)
        {
            GuiButtonOF guibuttonof = (GuiButtonOF)button;
            this.actionPerformed(guibuttonof, 1);
        }
    }

    private void checkFabulousWarning()
    {
        if (this.gpuWarnlistManager.isShowingWarning())
        {
            List<Component> list = Lists.newArrayList(WARNING_MESSAGE, NEW_LINE);
            String s = this.gpuWarnlistManager.getRendererWarnings();

            if (s != null)
            {
                list.add(NEW_LINE);
                list.add((new TranslatableComponent("options.graphics.warning.renderer", s)).withStyle(ChatFormatting.GRAY));
            }

            String s1 = this.gpuWarnlistManager.getVendorWarnings();

            if (s1 != null)
            {
                list.add(NEW_LINE);
                list.add((new TranslatableComponent("options.graphics.warning.vendor", s1)).withStyle(ChatFormatting.GRAY));
            }

            String s2 = this.gpuWarnlistManager.getVersionWarnings();

            if (s2 != null)
            {
                list.add(NEW_LINE);
                list.add((new TranslatableComponent("options.graphics.warning.version", s2)).withStyle(ChatFormatting.GRAY));
            }

            this.minecraft.setScreen(new PopupScreen(WARNING_TITLE, list, ImmutableList.of(new PopupScreen.ButtonOption(BUTTON_ACCEPT, (btn) ->
            {
                this.guiGameSettings.graphicsMode = GraphicsStatus.FABULOUS;
                Minecraft.getInstance().levelRenderer.allChanged();
                this.gpuWarnlistManager.dismissWarning();
                this.minecraft.setScreen(this);
            }), new PopupScreen.ButtonOption(BUTTON_CANCEL, (btn) ->
            {
                this.gpuWarnlistManager.dismissWarningAndSkipFabulous();
                this.minecraft.setScreen(this);
            }))));
        }
    }

    protected void actionPerformedRightClick(AbstractWidget button)
    {
        if (button == this.buttonGuiScale)
        {
            int i = this.guiGameSettings.guiScale - 1;

            if (i < 0)
            {
                i = Minecraft.getInstance().getWindow().calculateScale(0, Minecraft.getInstance().isEnforceUnicode());
            }

            Option.GUI_SCALE.setter.accept(this.guiGameSettings, Option.GUI_SCALE, i);
            this.updateGuiScale();
        }
    }

    private void updateGuiScale()
    {
        this.minecraft.resizeDisplay();
        Window window = this.minecraft.getWindow();
        int i = GuiUtils.getWidth(this.buttonGuiScale);
        int j = GuiUtils.getHeight(this.buttonGuiScale);
        int k = this.buttonGuiScale.x + (i - j);
        int l = this.buttonGuiScale.y + j / 2;
        GLFW.glfwSetCursorPos(window.getWindow(), (double)k * window.getGuiScale(), (double)l * window.getGuiScale());
    }

    private void actionPerformed(GuiButtonOF button, int val)
    {
        if (button.active)
        {
            if (button.id == 200)
            {
                this.minecraft.options.save();
                this.minecraft.setScreen(this.parentGuiScreen);
            }

            if (button.id == 201)
            {
                this.minecraft.options.save();
                GuiDetailSettingsOF guidetailsettingsof = new GuiDetailSettingsOF(this, this.guiGameSettings);
                this.minecraft.setScreen(guidetailsettingsof);
            }

            if (button.id == 202)
            {
                this.minecraft.options.save();
                GuiQualitySettingsOF guiqualitysettingsof = new GuiQualitySettingsOF(this, this.guiGameSettings);
                this.minecraft.setScreen(guiqualitysettingsof);
            }

            if (button.id == 211)
            {
                this.minecraft.options.save();
                GuiAnimationSettingsOF guianimationsettingsof = new GuiAnimationSettingsOF(this, this.guiGameSettings);
                this.minecraft.setScreen(guianimationsettingsof);
            }

            if (button.id == 212)
            {
                this.minecraft.options.save();
                GuiPerformanceSettingsOF guiperformancesettingsof = new GuiPerformanceSettingsOF(this, this.guiGameSettings);
                this.minecraft.setScreen(guiperformancesettingsof);
            }

            if (button.id == 222)
            {
                this.minecraft.options.save();
                GuiOtherSettingsOF guiothersettingsof = new GuiOtherSettingsOF(this, this.guiGameSettings);
                this.minecraft.setScreen(guiothersettingsof);
            }

            if (button.id == 231)
            {
                if (Config.isAntialiasing() || Config.isAntialiasingConfigured())
                {
                    Config.showGuiMessage(Lang.get("of.message.shaders.aa1"), Lang.get("of.message.shaders.aa2"));
                    return;
                }

                if (Config.isGraphicsFabulous())
                {
                    Config.showGuiMessage(Lang.get("of.message.shaders.gf1"), Lang.get("of.message.shaders.gf2"));
                    return;
                }

                this.minecraft.options.save();
                GuiShaders guishaders = new GuiShaders(this, this.guiGameSettings);
                this.minecraft.setScreen(guishaders);
            }
        }
    }

    public void removed()
    {
        this.minecraft.options.save();
        super.removed();
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        drawCenteredString(pMatrixStack, this.minecraft.font, this.title, this.width / 2, 15, 16777215);
        String s = Config.getVersion();
        String s1 = "HD_U";

        if (s1.equals("HD"))
        {
            s = "OptiFine HD G9_pre34";
        }

        if (s1.equals("HD_U"))
        {
            s = "OptiFine HD G9_pre34 Ultra";
        }

        if (s1.equals("L"))
        {
            s = "OptiFine G9_pre34 Light";
        }

        drawString(pMatrixStack, this.minecraft.font, s, 2, this.height - 10, 8421504);
        String s2 = "Minecraft 1.17.1";
        int i = this.minecraft.font.width(s2);
        drawString(pMatrixStack, this.minecraft.font, s2, this.width - i - 2, this.height - 10, 8421504);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        this.tooltipManager.drawTooltips(pMatrixStack, pMouseX, pMouseY, this.buttonList);
    }

    public static String getGuiChatText(ChatScreen guiChat)
    {
        return guiChat.input.getValue();
    }
}
