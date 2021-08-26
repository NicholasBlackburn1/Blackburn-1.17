package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class DemoIntroScreen extends Screen
{
    private static final ResourceLocation DEMO_BACKGROUND_LOCATION = new ResourceLocation("textures/gui/demo_background.png");
    private MultiLineLabel movementMessage = MultiLineLabel.EMPTY;
    private MultiLineLabel durationMessage = MultiLineLabel.EMPTY;

    public DemoIntroScreen()
    {
        super(new TranslatableComponent("demo.help.title"));
    }

    protected void init()
    {
        int i = -16;
        this.addRenderableWidget(new Button(this.width / 2 - 116, this.height / 2 + 62 + -16, 114, 20, new TranslatableComponent("demo.help.buy"), (p_95951_) ->
        {
            p_95951_.active = false;
            Util.getPlatform().openUri("http://www.minecraft.net/store?source=demo");
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 2, this.height / 2 + 62 + -16, 114, 20, new TranslatableComponent("demo.help.later"), (p_95948_) ->
        {
            this.minecraft.setScreen((Screen)null);
            this.minecraft.mouseHandler.grabMouse();
        }));
        Options options = this.minecraft.options;
        this.movementMessage = MultiLineLabel.m_94350_(this.font, new TranslatableComponent("demo.help.movementShort", options.keyUp.getTranslatedKeyMessage(), options.keyLeft.getTranslatedKeyMessage(), options.keyDown.getTranslatedKeyMessage(), options.keyRight.getTranslatedKeyMessage()), new TranslatableComponent("demo.help.movementMouse"), new TranslatableComponent("demo.help.jump", options.keyJump.getTranslatedKeyMessage()), new TranslatableComponent("demo.help.inventory", options.keyInventory.getTranslatedKeyMessage()));
        this.durationMessage = MultiLineLabel.create(this.font, new TranslatableComponent("demo.help.fullWrapped"), 218);
    }

    public void renderBackground(PoseStack pMatrixStack)
    {
        super.renderBackground(pMatrixStack);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, DEMO_BACKGROUND_LOCATION);
        int i = (this.width - 248) / 2;
        int j = (this.height - 166) / 2;
        this.blit(pMatrixStack, i, j, 0, 0, 248, 166);
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        int i = (this.width - 248) / 2 + 10;
        int j = (this.height - 166) / 2 + 8;
        this.font.draw(pMatrixStack, this.title, (float)i, (float)j, 2039583);
        j = this.movementMessage.renderLeftAlignedNoShadow(pMatrixStack, i, j + 12, 12, 5197647);
        this.durationMessage.renderLeftAlignedNoShadow(pMatrixStack, i, j + 20, 9, 2039583);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }
}
