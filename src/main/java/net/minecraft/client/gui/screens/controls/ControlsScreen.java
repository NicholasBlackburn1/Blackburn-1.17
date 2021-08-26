package net.minecraft.client.gui.screens.controls;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.MouseSettingsScreen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;

public class ControlsScreen extends OptionsSubScreen
{
    public KeyMapping selectedKey;
    public long lastKeySelection;
    private ControlList controlList;
    private Button resetButton;

    public ControlsScreen(Screen p_97519_, Options p_97520_)
    {
        super(p_97519_, p_97520_, new TranslatableComponent("controls.title"));
    }

    protected void init()
    {
        this.addRenderableWidget(new Button(this.width / 2 - 155, 18, 150, 20, new TranslatableComponent("options.mouse_settings"), (p_97540_) ->
        {
            this.minecraft.setScreen(new MouseSettingsScreen(this, this.options));
        }));
        this.addRenderableWidget(Option.AUTO_JUMP.createButton(this.options, this.width / 2 - 155 + 160, 18, 150));
        this.controlList = new ControlList(this, this.minecraft);
        this.addWidget(this.controlList);
        this.resetButton = this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 29, 150, 20, new TranslatableComponent("controls.resetAll"), (p_97538_) ->
        {
            for (KeyMapping keymapping : this.options.keyMappings)
            {
                keymapping.setKey(keymapping.getDefaultKey());
            }

            KeyMapping.resetMapping();
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 155 + 160, this.height - 29, 150, 20, CommonComponents.GUI_DONE, (p_97535_) ->
        {
            this.minecraft.setScreen(this.lastScreen);
        }));
    }

    public boolean mouseClicked(double pMouseX, double p_97523_, int pMouseY)
    {
        if (this.selectedKey != null)
        {
            this.options.setKey(this.selectedKey, InputConstants.Type.MOUSE.getOrCreate(pMouseY));
            this.selectedKey = null;
            KeyMapping.resetMapping();
            return true;
        }
        else
        {
            return super.mouseClicked(pMouseX, p_97523_, pMouseY);
        }
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        if (this.selectedKey != null)
        {
            if (pKeyCode == 256)
            {
                this.options.setKey(this.selectedKey, InputConstants.UNKNOWN);
            }
            else
            {
                this.options.setKey(this.selectedKey, InputConstants.getKey(pKeyCode, pScanCode));
            }

            this.selectedKey = null;
            this.lastKeySelection = Util.getMillis();
            KeyMapping.resetMapping();
            return true;
        }
        else
        {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        this.controlList.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, 8, 16777215);
        boolean flag = false;

        for (KeyMapping keymapping : this.options.keyMappings)
        {
            if (!keymapping.isDefault())
            {
                flag = true;
                break;
            }
        }

        this.resetButton.active = flag;
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }
}
