package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.LockIconButton;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.Difficulty;

public class OptionsScreen extends Screen
{
    private static final Option[] OPTION_SCREEN_OPTIONS = new Option[] {Option.FOV};
    private final Screen lastScreen;
    private final Options options;
    private CycleButton<Difficulty> difficultyButton;
    private LockIconButton lockButton;

    public OptionsScreen(Screen p_96242_, Options p_96243_)
    {
        super(new TranslatableComponent("options.title"));
        this.lastScreen = p_96242_;
        this.options = p_96243_;
    }

    protected void init()
    {
        int i = 0;

        for (Option option : OPTION_SCREEN_OPTIONS)
        {
            int j = this.width / 2 - 155 + i % 2 * 160;
            int k = this.height / 6 - 12 + 24 * (i >> 1);
            this.addRenderableWidget(option.createButton(this.minecraft.options, j, k, 150));
            ++i;
        }

        if (this.minecraft.level != null)
        {
            this.difficultyButton = this.addRenderableWidget(CycleButton.builder(Difficulty::getDisplayName).m_168961_(Difficulty.values()).withInitialValue(this.minecraft.level.getDifficulty()).create(this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + 24 * (i >> 1), 150, 20, new TranslatableComponent("options.difficulty"), (p_169330_, p_169331_) ->
            {
                this.minecraft.getConnection().send(new ServerboundChangeDifficultyPacket(p_169331_));
            }));

            if (this.minecraft.hasSingleplayerServer() && !this.minecraft.level.getLevelData().isHardcore())
            {
                this.difficultyButton.setWidth(this.difficultyButton.getWidth() - 20);
                this.lockButton = this.addRenderableWidget(new LockIconButton(this.difficultyButton.x + this.difficultyButton.getWidth(), this.difficultyButton.y, (p_96278_) ->
                {
                    this.minecraft.setScreen(new ConfirmScreen(this::lockCallback, new TranslatableComponent("difficulty.lock.title"), new TranslatableComponent("difficulty.lock.question", this.minecraft.level.getLevelData().getDifficulty().getDisplayName())));
                }));
                this.lockButton.setLocked(this.minecraft.level.getLevelData().isDifficultyLocked());
                this.lockButton.active = !this.lockButton.isLocked();
                this.difficultyButton.active = !this.lockButton.isLocked();
            }
            else
            {
                this.difficultyButton.active = false;
            }
        }
        else
        {
            this.addRenderableWidget(Option.REALMS_NOTIFICATIONS.createButton(this.options, this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + 24 * (i >> 1), 150));
        }

        this.addRenderableWidget(new Button(this.width / 2 - 155, this.height / 6 + 48 - 6, 150, 20, new TranslatableComponent("options.skinCustomisation"), (p_96276_) ->
        {
            this.minecraft.setScreen(new SkinCustomizationScreen(this, this.options));
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 5, this.height / 6 + 48 - 6, 150, 20, new TranslatableComponent("options.sounds"), (p_96274_) ->
        {
            this.minecraft.setScreen(new SoundOptionsScreen(this, this.options));
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 155, this.height / 6 + 72 - 6, 150, 20, new TranslatableComponent("options.video"), (p_96272_) ->
        {
            this.minecraft.setScreen(new VideoSettingsScreen(this, this.options));
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 5, this.height / 6 + 72 - 6, 150, 20, new TranslatableComponent("options.controls"), (p_96270_) ->
        {
            this.minecraft.setScreen(new ControlsScreen(this, this.options));
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 155, this.height / 6 + 96 - 6, 150, 20, new TranslatableComponent("options.language"), (p_96268_) ->
        {
            this.minecraft.setScreen(new LanguageSelectScreen(this, this.options, this.minecraft.getLanguageManager()));
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 5, this.height / 6 + 96 - 6, 150, 20, new TranslatableComponent("options.chat.title"), (p_96266_) ->
        {
            this.minecraft.setScreen(new ChatOptionsScreen(this, this.options));
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 155, this.height / 6 + 120 - 6, 150, 20, new TranslatableComponent("options.resourcepack"), (p_96263_) ->
        {
            this.minecraft.setScreen(new PackSelectionScreen(this, this.minecraft.getResourcePackRepository(), this::updatePackList, this.minecraft.getResourcePackDirectory(), new TranslatableComponent("resourcePack.title")));
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 5, this.height / 6 + 120 - 6, 150, 20, new TranslatableComponent("options.accessibility.title"), (p_96259_) ->
        {
            this.minecraft.setScreen(new AccessibilityOptionsScreen(this, this.options));
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 + 168, 200, 20, CommonComponents.GUI_DONE, (p_96257_) ->
        {
            this.minecraft.setScreen(this.lastScreen);
        }));
    }

    private void updatePackList(PackRepository p_96245_)
    {
        List<String> list = ImmutableList.copyOf(this.options.resourcePacks);
        this.options.resourcePacks.clear();
        this.options.incompatibleResourcePacks.clear();

        for (Pack pack : p_96245_.getSelectedPacks())
        {
            if (!pack.isFixedPosition())
            {
                this.options.resourcePacks.add(pack.getId());

                if (!pack.getCompatibility().isCompatible())
                {
                    this.options.incompatibleResourcePacks.add(pack.getId());
                }
            }
        }

        this.options.save();
        List<String> list1 = ImmutableList.copyOf(this.options.resourcePacks);

        if (!list1.equals(list))
        {
            this.minecraft.reloadResourcePacks();
        }
    }

    private void lockCallback(boolean pValue)
    {
        this.minecraft.setScreen(this);

        if (pValue && this.minecraft.level != null)
        {
            this.minecraft.getConnection().send(new ServerboundLockDifficultyPacket(true));
            this.lockButton.setLocked(true);
            this.lockButton.active = false;
            this.difficultyButton.active = false;
        }
    }

    public void removed()
    {
        this.options.save();
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, 15, 16777215);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }
}
