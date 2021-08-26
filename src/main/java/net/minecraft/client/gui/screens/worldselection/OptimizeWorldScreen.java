package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OptimizeWorldScreen extends Screen
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Object2IntMap<ResourceKey<Level>> DIMENSION_COLORS = Util.make(new Object2IntOpenCustomHashMap<>(Util.identityStrategy()), (p_101324_) ->
    {
        p_101324_.put(Level.OVERWORLD, -13408734);
        p_101324_.put(Level.NETHER, -10075085);
        p_101324_.put(Level.END, -8943531);
        p_101324_.defaultReturnValue(-2236963);
    });
    private final BooleanConsumer callback;
    private final WorldUpgrader upgrader;

    @Nullable
    public static OptimizeWorldScreen create(Minecraft p_101316_, BooleanConsumer p_101317_, DataFixer p_101318_, LevelStorageSource.LevelStorageAccess p_101319_, boolean p_101320_)
    {
        RegistryAccess.RegistryHolder registryaccess$registryholder = RegistryAccess.builtin();

        try
        {
            Minecraft.ServerStem minecraft$serverstem = p_101316_.makeServerStem(registryaccess$registryholder, Minecraft::loadDataPacks, Minecraft::loadWorldData, false, p_101319_);
            OptimizeWorldScreen optimizeworldscreen;

            try
            {
                WorldData worlddata = minecraft$serverstem.worldData();
                p_101319_.saveDataTag(registryaccess$registryholder, worlddata);
                ImmutableSet<ResourceKey<Level>> immutableset = worlddata.worldGenSettings().levels();
                optimizeworldscreen = new OptimizeWorldScreen(p_101317_, p_101318_, p_101319_, worlddata.getLevelSettings(), p_101320_, immutableset);
            }
            catch (Throwable throwable1)
            {
                if (minecraft$serverstem != null)
                {
                    try
                    {
                        minecraft$serverstem.close();
                    }
                    catch (Throwable throwable)
                    {
                        throwable1.addSuppressed(throwable);
                    }
                }

                throw throwable1;
            }

            if (minecraft$serverstem != null)
            {
                minecraft$serverstem.close();
            }

            return optimizeworldscreen;
        }
        catch (Exception exception)
        {
            LOGGER.warn("Failed to load datapacks, can't optimize world", (Throwable)exception);
            return null;
        }
    }

    private OptimizeWorldScreen(BooleanConsumer p_101304_, DataFixer p_101305_, LevelStorageSource.LevelStorageAccess p_101306_, LevelSettings p_101307_, boolean p_101308_, ImmutableSet<ResourceKey<Level>> p_101309_)
    {
        super(new TranslatableComponent("optimizeWorld.title", p_101307_.levelName()));
        this.callback = p_101304_;
        this.upgrader = new WorldUpgrader(p_101306_, p_101305_, p_101309_, p_101308_);
    }

    protected void init()
    {
        super.init();
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 150, 200, 20, CommonComponents.GUI_CANCEL, (p_101322_) ->
        {
            this.upgrader.cancel();
            this.callback.accept(false);
        }));
    }

    public void tick()
    {
        if (this.upgrader.isFinished())
        {
            this.callback.accept(true);
        }
    }

    public void onClose()
    {
        this.callback.accept(false);
    }

    public void removed()
    {
        this.upgrader.cancel();
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, 20, 16777215);
        int i = this.width / 2 - 150;
        int j = this.width / 2 + 150;
        int k = this.height / 4 + 100;
        int l = k + 10;
        drawCenteredString(pMatrixStack, this.font, this.upgrader.getStatus(), this.width / 2, k - 9 - 2, 10526880);

        if (this.upgrader.getTotalChunks() > 0)
        {
            fill(pMatrixStack, i - 1, k - 1, j + 1, l + 1, -16777216);
            drawString(pMatrixStack, this.font, new TranslatableComponent("optimizeWorld.info.converted", this.upgrader.getConverted()), i, 40, 10526880);
            drawString(pMatrixStack, this.font, new TranslatableComponent("optimizeWorld.info.skipped", this.upgrader.getSkipped()), i, 40 + 9 + 3, 10526880);
            drawString(pMatrixStack, this.font, new TranslatableComponent("optimizeWorld.info.total", this.upgrader.getTotalChunks()), i, 40 + (9 + 3) * 2, 10526880);
            int i1 = 0;

            for (ResourceKey<Level> resourcekey : this.upgrader.levels())
            {
                int j1 = Mth.floor(this.upgrader.dimensionProgress(resourcekey) * (float)(j - i));
                fill(pMatrixStack, i + i1, k, i + i1 + j1, l, DIMENSION_COLORS.getInt(resourcekey));
                i1 += j1;
            }

            int k1 = this.upgrader.getConverted() + this.upgrader.getSkipped();
            drawCenteredString(pMatrixStack, this.font, k1 + " / " + this.upgrader.getTotalChunks(), this.width / 2, k + 2 * 9 + 2, 10526880);
            drawCenteredString(pMatrixStack, this.font, Mth.floor(this.upgrader.getProgress() * 100.0F) + "%", this.width / 2, k + (l - k) / 2 - 9 / 2, 10526880);
        }

        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }
}
