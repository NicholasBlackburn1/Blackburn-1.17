package net.optifine.reflect;

import com.mojang.math.Vector3f;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.optifine.Log;
import net.optifine.util.StrUtils;

public class ReflectorForge
{
    public static Object EVENT_RESULT_ALLOW = Reflector.getFieldValue(Reflector.Event_Result_ALLOW);
    public static Object EVENT_RESULT_DENY = Reflector.getFieldValue(Reflector.Event_Result_DENY);
    public static Object EVENT_RESULT_DEFAULT = Reflector.getFieldValue(Reflector.Event_Result_DEFAULT);
    public static final boolean FORGE_BLOCKSTATE_HAS_TILE_ENTITY = Reflector.IForgeBlockState_hasTileEntity.exists();
    public static final boolean FORGE_ENTITY_CAN_UPDATE = Reflector.IForgeEntity_canUpdate.exists();

    public static void putLaunchBlackboard(String key, Object value)
    {
        Map map = (Map)Reflector.getFieldValue(Reflector.Launch_blackboard);

        if (map != null)
        {
            map.put(key, value);
        }
    }

    public static InputStream getOptiFineResourceStream(String path)
    {
        if (!Reflector.OptiFineResourceLocator.exists())
        {
            return null;
        }
        else
        {
            path = StrUtils.removePrefix(path, "/");
            return (InputStream)Reflector.call(Reflector.OptiFineResourceLocator_getOptiFineResourceStream, path);
        }
    }

    public static ReflectorClass getReflectorClassOptiFineResourceLocator()
    {
        String s = "optifine.OptiFineResourceLocator";
        Object object = System.getProperties().get(s + ".class");

        if (object instanceof Class)
        {
            Class oclass = (Class)object;
            return new ReflectorClass(oclass);
        }
        else
        {
            return new ReflectorClass(s);
        }
    }

    public static boolean isBarVisible(ItemStack stack)
    {
        return Reflector.IForgeItem_showDurabilityBar.exists() ? Reflector.callBoolean(stack.getItem(), Reflector.IForgeItem_showDurabilityBar, stack) : stack.isBarVisible();
    }

    public static int getLightValue(BlockState stateIn, BlockAndTintGetter worldIn, BlockPos posIn)
    {
        return Reflector.IForgeBlockState_getLightValue2.exists() ? Reflector.callInt(stateIn, Reflector.IForgeBlockState_getLightValue2, worldIn, posIn) : stateIn.getLightEmission();
    }

    public static MapItemSavedData getMapData(ItemStack stack, Level world)
    {
        if (Reflector.ForgeHooksClient.exists())
        {
            MapItem mapitem = (MapItem)stack.getItem();
            return MapItem.getSavedData(stack, world);
        }
        else
        {
            return MapItem.getSavedData(stack, world);
        }
    }

    public static String[] getForgeModIds()
    {
        if (!Reflector.Loader.exists())
        {
            return new String[0];
        }
        else
        {
            Object object = Reflector.call(Reflector.Loader_instance);
            List list = (List)Reflector.call(object, Reflector.Loader_getActiveModList);

            if (list == null)
            {
                return new String[0];
            }
            else
            {
                List<String> list1 = new ArrayList<>();

                for (Object object1 : list)
                {
                    if (Reflector.ModContainer.isInstance(object1))
                    {
                        String s = Reflector.callString(object1, Reflector.ModContainer_getModId);

                        if (s != null)
                        {
                            list1.add(s);
                        }
                    }
                }

                String[] astring = list1.toArray(new String[list1.size()]);
                return astring;
            }
        }
    }

    public static boolean canDisableShield(ItemStack itemstack, ItemStack itemstack1, Player entityplayer, Mob entityLiving)
    {
        return Reflector.IForgeItemStack_canDisableShield.exists() ? Reflector.callBoolean(itemstack, Reflector.IForgeItemStack_canDisableShield, itemstack1, entityplayer, entityLiving) : itemstack.getItem() instanceof AxeItem;
    }

    public static boolean isShield(ItemStack itemstack, Player entityplayer)
    {
        if (Reflector.IForgeItemStack_isShield.exists())
        {
            return Reflector.callBoolean(itemstack, Reflector.IForgeItemStack_isShield, entityplayer);
        }
        else
        {
            return itemstack.getItem() == Items.SHIELD;
        }
    }

    public static Button makeButtonMods(TitleScreen guiMainMenu, int yIn, int rowHeightIn)
    {
        return !Reflector.ModListScreen_Constructor.exists() ? null : new Button(guiMainMenu.width / 2 - 100, yIn + rowHeightIn * 2, 98, 20, new TranslatableComponent("fml.menu.mods"), (button) ->
        {
            Screen screen = (Screen)Reflector.ModListScreen_Constructor.newInstance(guiMainMenu);
            Minecraft.getInstance().setScreen(screen);
        });
    }

    public static void setForgeLightPipelineEnabled(boolean value)
    {
        if (Reflector.ForgeConfig_Client_forgeLightPipelineEnabled.exists())
        {
            setConfigClientBoolean(Reflector.ForgeConfig_Client_forgeLightPipelineEnabled, value);
        }
    }

    public static boolean getForgeUseCombinedDepthStencilAttachment()
    {
        return Reflector.ForgeConfig_Client_useCombinedDepthStencilAttachment.exists() ? getConfigClientBoolean(Reflector.ForgeConfig_Client_useCombinedDepthStencilAttachment, false) : false;
    }

    public static boolean getConfigClientBoolean(ReflectorField configField, boolean def)
    {
        if (!configField.exists())
        {
            return def;
        }
        else
        {
            Object object = Reflector.ForgeConfig_CLIENT.getValue();

            if (object == null)
            {
                return def;
            }
            else
            {
                Object object1 = Reflector.getFieldValue(object, configField);
                return object1 == null ? def : Reflector.callBoolean(object1, Reflector.ForgeConfigSpec_ConfigValue_get);
            }
        }
    }

    private static void setConfigClientBoolean(ReflectorField clientField, final boolean value)
    {
        if (clientField.exists())
        {
            Object object = Reflector.ForgeConfig_CLIENT.getValue();

            if (object != null)
            {
                Object object1 = Reflector.getFieldValue(object, clientField);

                if (object1 != null)
                {
                    Supplier<Boolean> supplier = new Supplier<Boolean>()
                    {
                        public Boolean get()
                        {
                            return value;
                        }
                    };
                    Reflector.setFieldValue(object1, Reflector.ForgeConfigSpec_ConfigValue_defaultSupplier, supplier);
                    Object object2 = Reflector.getFieldValue(object1, Reflector.ForgeConfigSpec_ConfigValue_spec);

                    if (object2 != null)
                    {
                        Reflector.setFieldValue(object2, Reflector.ForgeConfigSpec_childConfig, (Object)null);
                    }

                    Log.dbg("Set ForgeConfig.CLIENT." + clientField.getTargetField().getName() + "=" + value);
                }
            }
        }
    }

    public static boolean canUpdate(Entity entity)
    {
        return FORGE_ENTITY_CAN_UPDATE ? Reflector.callBoolean(entity, Reflector.IForgeEntity_canUpdate) : true;
    }

    public static boolean isDamageable(Item item, ItemStack stack)
    {
        return Reflector.IForgeItem_isDamageable1.exists() ? Reflector.callBoolean(item, Reflector.IForgeItem_isDamageable1, stack) : item.canBeDepleted();
    }

    public static void fillNormal(int[] faceData, Direction facing)
    {
        Vector3f vector3f = getVertexPos(faceData, 3);
        Vector3f vector3f1 = getVertexPos(faceData, 1);
        Vector3f vector3f2 = getVertexPos(faceData, 2);
        Vector3f vector3f3 = getVertexPos(faceData, 0);
        vector3f.sub(vector3f1);
        vector3f2.sub(vector3f3);
        vector3f2.cross(vector3f);
        vector3f2.normalize();
        int i = (byte)Math.round(vector3f2.x() * 127.0F) & 255;
        int j = (byte)Math.round(vector3f2.y() * 127.0F) & 255;
        int k = (byte)Math.round(vector3f2.z() * 127.0F) & 255;
        int l = i | j << 8 | k << 16;
        int i1 = faceData.length / 4;

        for (int j1 = 0; j1 < 4; ++j1)
        {
            faceData[j1 * i1 + 7] = l;
        }
    }

    private static Vector3f getVertexPos(int[] data, int vertex)
    {
        int i = data.length / 4;
        int j = vertex * i;
        float f = Float.intBitsToFloat(data[j]);
        float f1 = Float.intBitsToFloat(data[j + 1]);
        float f2 = Float.intBitsToFloat(data[j + 2]);
        return new Vector3f(f, f1, f2);
    }

    public static void postModLoaderEvent(ReflectorConstructor constr, Object... params)
    {
        Object object = Reflector.newInstance(constr, params);

        if (object != null)
        {
            postModLoaderEvent(object);
        }
    }

    public static void postModLoaderEvent(Object event)
    {
        if (event != null)
        {
            Object object = Reflector.ModLoader_get.call();

            if (object != null)
            {
                Reflector.callVoid(object, Reflector.ModLoader_postEvent, event);
            }
        }
    }
}
