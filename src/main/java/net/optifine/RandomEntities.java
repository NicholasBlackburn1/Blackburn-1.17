package net.optifine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.optifine.reflect.ReflectorRaw;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.ResUtils;
import net.optifine.util.StrUtils;

public class RandomEntities
{
    private static Map<String, RandomEntityProperties> mapProperties = new HashMap<>();
    private static boolean active = false;
    private static LevelRenderer renderGlobal;
    private static RandomEntity randomEntity = new RandomEntity();
    private static BlockEntityRenderDispatcher tileEntityRendererDispatcher;
    private static RandomTileEntity randomTileEntity = new RandomTileEntity();
    private static boolean working = false;
    public static final String SUFFIX_PNG = ".png";
    public static final String SUFFIX_PROPERTIES = ".properties";
    public static final String PREFIX_TEXTURES_ENTITY = "textures/entity/";
    public static final String PREFIX_TEXTURES_PAINTING = "textures/painting/";
    public static final String PREFIX_TEXTURES = "textures/";
    public static final String PREFIX_OPTIFINE_RANDOM = "optifine/random/";
    public static final String PREFIX_OPTIFINE_MOB = "optifine/mob/";
    private static final String[] DEPENDANT_SUFFIXES = new String[] {"_armor", "_eyes", "_exploding", "_shooting", "_fur", "_eyes", "_invulnerable", "_angry", "_tame", "_collar"};
    private static final String PREFIX_DYNAMIC_TEXTURE_HORSE = "horse/";
    private static final String[] HORSE_TEXTURES = (String[])ReflectorRaw.getFieldValue((Object)null, Horse.class, String[].class, 0);
    private static final String[] HORSE_TEXTURES_ABBR = (String[])ReflectorRaw.getFieldValue((Object)null, Horse.class, String[].class, 1);

    public static void entityLoaded(Entity entity, Level world)
    {
        if (world != null)
        {
            SynchedEntityData synchedentitydata = entity.getEntityData();
            synchedentitydata.spawnPosition = entity.blockPosition();
            synchedentitydata.spawnBiome = world.getBiome(synchedentitydata.spawnPosition);

            if (entity instanceof ShoulderRidingEntity)
            {
                ShoulderRidingEntity shoulderridingentity = (ShoulderRidingEntity)entity;
                checkEntityShoulder(shoulderridingentity, false);
            }
        }
    }

    public static void entityUnloaded(Entity entity, Level world)
    {
        if (entity instanceof ShoulderRidingEntity)
        {
            ShoulderRidingEntity shoulderridingentity = (ShoulderRidingEntity)entity;
            checkEntityShoulder(shoulderridingentity, true);
        }
    }

    private static void checkEntityShoulder(ShoulderRidingEntity entity, boolean attach)
    {
        LivingEntity livingentity = entity.getOwner();

        if (livingentity == null)
        {
            livingentity = Config.getMinecraft().player;
        }

        if (livingentity instanceof AbstractClientPlayer)
        {
            AbstractClientPlayer abstractclientplayer = (AbstractClientPlayer)livingentity;
            UUID uuid = entity.getUUID();

            if (attach)
            {
                CompoundTag compoundtag = abstractclientplayer.getShoulderEntityLeft();

                if (compoundtag != null && compoundtag.contains("UUID") && Config.equals(compoundtag.getUUID("UUID"), uuid))
                {
                    abstractclientplayer.entityShoulderLeft = entity;
                }

                CompoundTag compoundtag1 = abstractclientplayer.getShoulderEntityRight();

                if (compoundtag1 != null && compoundtag1.contains("UUID") && Config.equals(compoundtag1.getUUID("UUID"), uuid))
                {
                    abstractclientplayer.entityShoulderRight = entity;
                }
            }
            else
            {
                SynchedEntityData synchedentitydata = entity.getEntityData();

                if (abstractclientplayer.entityShoulderLeft != null && Config.equals(abstractclientplayer.entityShoulderLeft.getUUID(), uuid))
                {
                    SynchedEntityData synchedentitydata1 = abstractclientplayer.entityShoulderLeft.getEntityData();
                    synchedentitydata.spawnPosition = synchedentitydata1.spawnPosition;
                    synchedentitydata.spawnBiome = synchedentitydata1.spawnBiome;
                    abstractclientplayer.entityShoulderLeft = null;
                }

                if (abstractclientplayer.entityShoulderRight != null && Config.equals(abstractclientplayer.entityShoulderRight.getUUID(), uuid))
                {
                    SynchedEntityData synchedentitydata2 = abstractclientplayer.entityShoulderRight.getEntityData();
                    synchedentitydata.spawnPosition = synchedentitydata2.spawnPosition;
                    synchedentitydata.spawnBiome = synchedentitydata2.spawnBiome;
                    abstractclientplayer.entityShoulderRight = null;
                }
            }
        }
    }

    public static void worldChanged(Level oldWorld, Level newWorld)
    {
        if (newWorld instanceof ClientLevel)
        {
            ClientLevel clientlevel = (ClientLevel)newWorld;

            for (Entity entity : clientlevel.entitiesForRendering())
            {
                entityLoaded(entity, newWorld);
            }
        }

        randomEntity.setEntity((Entity)null);
        randomTileEntity.setTileEntity((BlockEntity)null);
    }

    public static ResourceLocation getTextureLocation(ResourceLocation loc)
    {
        if (!active)
        {
            return loc;
        }
        else if (working)
        {
            return loc;
        }
        else
        {
            ResourceLocation name;

            try
            {
                working = true;
                IRandomEntity irandomentity = getRandomEntityRendered();

                if (irandomentity != null)
                {
                    String s = loc.getPath();

                    if (s.startsWith("horse/"))
                    {
                        s = getHorseTexturePath(s, "horse/".length());
                    }

                    if (!s.startsWith("textures/entity/") && !s.startsWith("textures/painting/"))
                    {
                        return loc;
                    }

                    RandomEntityProperties randomentityproperties = mapProperties.get(s);

                    if (randomentityproperties == null)
                    {
                        return loc;
                    }

                    return randomentityproperties.getTextureLocation(loc, irandomentity);
                }

                name = loc;
            }
            finally
            {
                working = false;
            }

            return name;
        }
    }

    private static String getHorseTexturePath(String path, int pos)
    {
        if (HORSE_TEXTURES != null && HORSE_TEXTURES_ABBR != null)
        {
            for (int i = 0; i < HORSE_TEXTURES_ABBR.length; ++i)
            {
                String s = HORSE_TEXTURES_ABBR[i];

                if (path.startsWith(s, pos))
                {
                    return HORSE_TEXTURES[i];
                }
            }

            return path;
        }
        else
        {
            return path;
        }
    }

    public static IRandomEntity getRandomEntityRendered()
    {
        if (renderGlobal.renderedEntity != null)
        {
            randomEntity.setEntity(renderGlobal.renderedEntity);
            return randomEntity;
        }
        else
        {
            BlockEntityRenderDispatcher blockentityrenderdispatcher = tileEntityRendererDispatcher;

            if (BlockEntityRenderDispatcher.tileEntityRendered != null)
            {
                blockentityrenderdispatcher = tileEntityRendererDispatcher;
                BlockEntity blockentity = BlockEntityRenderDispatcher.tileEntityRendered;

                if (blockentity.getLevel() != null)
                {
                    randomTileEntity.setTileEntity(blockentity);
                    return randomTileEntity;
                }
            }

            return null;
        }
    }

    private static RandomEntityProperties makeProperties(ResourceLocation loc, boolean optifine)
    {
        String s = loc.getPath();
        ResourceLocation resourcelocation = getLocationProperties(loc, optifine);

        if (resourcelocation != null)
        {
            RandomEntityProperties randomentityproperties = parseProperties(resourcelocation, loc);

            if (randomentityproperties != null)
            {
                return randomentityproperties;
            }
        }

        ResourceLocation[] aresourcelocation = getLocationsVariants(loc, optifine);
        return aresourcelocation == null ? null : new RandomEntityProperties(s, aresourcelocation);
    }

    private static RandomEntityProperties parseProperties(ResourceLocation propLoc, ResourceLocation resLoc)
    {
        try
        {
            String s = propLoc.getPath();
            dbg(resLoc.getPath() + ", properties: " + s);
            InputStream inputstream = Config.getResourceStream(propLoc);

            if (inputstream == null)
            {
                warn("Properties not found: " + s);
                return null;
            }
            else
            {
                Properties properties = new PropertiesOrdered();
                properties.load(inputstream);
                inputstream.close();
                RandomEntityProperties randomentityproperties = new RandomEntityProperties(properties, s, resLoc);
                return !randomentityproperties.isValid(s) ? null : randomentityproperties;
            }
        }
        catch (FileNotFoundException filenotfoundexception)
        {
            warn("File not found: " + resLoc.getPath());
            return null;
        }
        catch (IOException ioexception)
        {
            ioexception.printStackTrace();
            return null;
        }
    }

    private static ResourceLocation getLocationProperties(ResourceLocation loc, boolean optifine)
    {
        ResourceLocation resourcelocation = getLocationRandom(loc, optifine);

        if (resourcelocation == null)
        {
            return null;
        }
        else
        {
            String s = resourcelocation.getNamespace();
            String s1 = resourcelocation.getPath();
            String s2 = StrUtils.removeSuffix(s1, ".png");
            String s3 = s2 + ".properties";
            ResourceLocation resourcelocation1 = new ResourceLocation(s, s3);

            if (Config.hasResource(resourcelocation1))
            {
                return resourcelocation1;
            }
            else
            {
                String s4 = getParentTexturePath(s2);

                if (s4 == null)
                {
                    return null;
                }
                else
                {
                    ResourceLocation resourcelocation2 = new ResourceLocation(s, s4 + ".properties");
                    return Config.hasResource(resourcelocation2) ? resourcelocation2 : null;
                }
            }
        }
    }

    protected static ResourceLocation getLocationRandom(ResourceLocation loc, boolean optifine)
    {
        String s = loc.getNamespace();
        String s1 = loc.getPath();
        String s2 = "textures/";
        String s3 = "optifine/random/";

        if (optifine)
        {
            s2 = "textures/entity/";
            s3 = "optifine/mob/";
        }

        if (!s1.startsWith(s2))
        {
            return null;
        }
        else
        {
            String s4 = StrUtils.replacePrefix(s1, s2, s3);
            return new ResourceLocation(s, s4);
        }
    }

    private static String getPathBase(String pathRandom)
    {
        if (pathRandom.startsWith("optifine/random/"))
        {
            return StrUtils.replacePrefix(pathRandom, "optifine/random/", "textures/");
        }
        else
        {
            return pathRandom.startsWith("optifine/mob/") ? StrUtils.replacePrefix(pathRandom, "optifine/mob/", "textures/entity/") : null;
        }
    }

    protected static ResourceLocation getLocationIndexed(ResourceLocation loc, int index)
    {
        if (loc == null)
        {
            return null;
        }
        else
        {
            String s = loc.getPath();
            int i = s.lastIndexOf(46);

            if (i < 0)
            {
                return null;
            }
            else
            {
                String s1 = s.substring(0, i);
                String s2 = s.substring(i);
                String s3 = s1 + index + s2;
                return new ResourceLocation(loc.getNamespace(), s3);
            }
        }
    }

    private static String getParentTexturePath(String path)
    {
        for (int i = 0; i < DEPENDANT_SUFFIXES.length; ++i)
        {
            String s = DEPENDANT_SUFFIXES[i];

            if (path.endsWith(s))
            {
                return StrUtils.removeSuffix(path, s);
            }
        }

        return null;
    }

    private static ResourceLocation[] getLocationsVariants(ResourceLocation loc, boolean optifine)
    {
        List list = new ArrayList();
        list.add(loc);
        ResourceLocation resourcelocation = getLocationRandom(loc, optifine);

        if (resourcelocation == null)
        {
            return null;
        }
        else
        {
            for (int i = 1; i < list.size() + 10; ++i)
            {
                int j = i + 1;
                ResourceLocation resourcelocation1 = getLocationIndexed(resourcelocation, j);

                if (Config.hasResource(resourcelocation1))
                {
                    list.add(resourcelocation1);
                }
            }

            if (list.size() <= 1)
            {
                return null;
            }
            else
            {
                ResourceLocation[] aresourcelocation = (ResourceLocation[]) list.toArray(new ResourceLocation[list.size()]);
                dbg(loc.getPath() + ", variants: " + aresourcelocation.length);
                return aresourcelocation;
            }
        }
    }

    public static void update()
    {
        mapProperties.clear();
        active = false;

        if (Config.isRandomEntities())
        {
            initialize();
        }
    }

    private static void initialize()
    {
        renderGlobal = Config.getRenderGlobal();
        tileEntityRendererDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        String[] astring = new String[] {"optifine/random/", "optifine/mob/"};
        String[] astring1 = new String[] {".png", ".properties"};
        String[] astring2 = ResUtils.collectFiles(astring, astring1);
        Set set = new HashSet();

        for (int i = 0; i < astring2.length; ++i)
        {
            String s = astring2[i];
            s = StrUtils.removeSuffix(s, astring1);
            s = StrUtils.trimTrailing(s, "0123456789");
            s = s + ".png";
            String s1 = getPathBase(s);

            if (!set.contains(s1))
            {
                set.add(s1);
                ResourceLocation resourcelocation = new ResourceLocation(s1);

                if (Config.hasResource(resourcelocation))
                {
                    RandomEntityProperties randomentityproperties = mapProperties.get(s1);

                    if (randomentityproperties == null)
                    {
                        randomentityproperties = makeProperties(resourcelocation, false);

                        if (randomentityproperties == null)
                        {
                            randomentityproperties = makeProperties(resourcelocation, true);
                        }

                        if (randomentityproperties != null)
                        {
                            mapProperties.put(s1, randomentityproperties);
                        }
                    }
                }
            }
        }

        active = !mapProperties.isEmpty();
    }

    public static void dbg(String str)
    {
        Config.dbg("RandomEntities: " + str);
    }

    public static void warn(String str)
    {
        Config.warn("RandomEntities: " + str);
    }
}
