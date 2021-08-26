package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public abstract class SimpleOptionsSubScreen extends OptionsSubScreen
{
    private final Option[] smallOptions;
    @Nullable
    private AbstractWidget narratorButton;
    private OptionsList list;

    public SimpleOptionsSubScreen(Screen p_96670_, Options p_96671_, Component p_96672_, Option[] p_96673_)
    {
        super(p_96670_, p_96671_, p_96672_);
        this.smallOptions = p_96673_;
    }

    protected void init()
    {
        this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
        this.list.m_94476_(this.smallOptions);
        this.addWidget(this.list);
        this.createFooter();
        this.narratorButton = this.list.findOption(Option.NARRATOR);

        if (this.narratorButton != null)
        {
            this.narratorButton.active = NarratorChatListener.INSTANCE.isActive();
        }
    }

    protected void createFooter()
    {
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 27, 200, 20, CommonComponents.GUI_DONE, (p_96680_) ->
        {
            this.minecraft.setScreen(this.lastScreen);
        }));
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        this.list.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, 20, 16777215);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        List<FormattedCharSequence> list = tooltipAt(this.list, pMouseX, pMouseY);
        this.renderTooltip(pMatrixStack, list, pMouseX, pMouseY);
    }

    public void updateNarratorButton()
    {
        if (this.narratorButton instanceof CycleButton)
        {
            ((CycleButton)this.narratorButton).setValue(this.options.narratorStatus);
        }
    }
}
