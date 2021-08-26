package net.minecraft.client.gui.screens;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.gui.screens.inventory.BlastFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.client.gui.screens.inventory.CartographyTableScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.DispenserScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.gui.screens.inventory.GrindstoneScreen;
import net.minecraft.client.gui.screens.inventory.HopperScreen;
import net.minecraft.client.gui.screens.inventory.LecternScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.client.gui.screens.inventory.SmokerScreen;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MenuScreens
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map < MenuType<?>, MenuScreens.ScreenConstructor <? , ? >> SCREENS = Maps.newHashMap();

    public static <T extends AbstractContainerMenu> void create(@Nullable MenuType<T> pType, Minecraft pMc, int pWindowId, Component pTitle)
    {
        if (pType == null)
        {
            LOGGER.warn("Trying to open invalid screen with name: {}", (Object)pTitle.getString());
        }
        else
        {
            MenuScreens.ScreenConstructor < T, ? > screenconstructor = getConstructor(pType);

            if (screenconstructor == null)
            {
                LOGGER.warn("Failed to create screen for menu type: {}", (Object)Registry.MENU.getKey(pType));
            }
            else
            {
                screenconstructor.fromPacket(pTitle, pType, pMc, pWindowId);
            }
        }
    }

    @Nullable
    private static <T extends AbstractContainerMenu> MenuScreens.ScreenConstructor < T, ? > getConstructor(MenuType<T> pType)
    {
        return (MenuScreens.ScreenConstructor < T, ? >)SCREENS.get(pType);
    }

    private static <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType <? extends M > pType, MenuScreens.ScreenConstructor<M, U> pFactory)
    {
        MenuScreens.ScreenConstructor <? , ? > screenconstructor = SCREENS.put(pType, pFactory);

        if (screenconstructor != null)
        {
            throw new IllegalStateException("Duplicate registration for " + Registry.MENU.getKey(pType));
        }
    }

    public static boolean selfTest()
    {
        boolean flag = false;

        for (MenuType<?> menutype : Registry.MENU)
        {
            if (!SCREENS.containsKey(menutype))
            {
                LOGGER.debug("Menu {} has no matching screen", (Object)Registry.MENU.getKey(menutype));
                flag = true;
            }
        }

        return flag;
    }

    static
    {
        register(MenuType.GENERIC_9x1, ContainerScreen::new);
        register(MenuType.GENERIC_9x2, ContainerScreen::new);
        register(MenuType.GENERIC_9x3, ContainerScreen::new);
        register(MenuType.GENERIC_9x4, ContainerScreen::new);
        register(MenuType.GENERIC_9x5, ContainerScreen::new);
        register(MenuType.GENERIC_9x6, ContainerScreen::new);
        register(MenuType.GENERIC_3x3, DispenserScreen::new);
        register(MenuType.ANVIL, AnvilScreen::new);
        register(MenuType.BEACON, BeaconScreen::new);
        register(MenuType.BLAST_FURNACE, BlastFurnaceScreen::new);
        register(MenuType.BREWING_STAND, BrewingStandScreen::new);
        register(MenuType.CRAFTING, CraftingScreen::new);
        register(MenuType.ENCHANTMENT, EnchantmentScreen::new);
        register(MenuType.FURNACE, FurnaceScreen::new);
        register(MenuType.GRINDSTONE, GrindstoneScreen::new);
        register(MenuType.HOPPER, HopperScreen::new);
        register(MenuType.LECTERN, LecternScreen::new);
        register(MenuType.LOOM, LoomScreen::new);
        register(MenuType.MERCHANT, MerchantScreen::new);
        register(MenuType.SHULKER_BOX, ShulkerBoxScreen::new);
        register(MenuType.SMITHING, SmithingScreen::new);
        register(MenuType.SMOKER, SmokerScreen::new);
        register(MenuType.CARTOGRAPHY_TABLE, CartographyTableScreen::new);
        register(MenuType.STONECUTTER, StonecutterScreen::new);
    }

    interface ScreenConstructor<T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>>
    {
    default void fromPacket(Component pTitle, MenuType<T> pType, Minecraft pMc, int pWindowId)
        {
            U u = this.create(pType.create(pWindowId, pMc.player.getInventory()), pMc.player.getInventory(), pTitle);
            pMc.player.containerMenu = u.getMenu();
            pMc.setScreen(u);
        }

        U create(T p_96215_, Inventory p_96216_, Component p_96217_);
    }
}
