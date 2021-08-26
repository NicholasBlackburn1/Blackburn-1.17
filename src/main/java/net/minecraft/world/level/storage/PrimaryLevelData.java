package net.minecraft.world.level.storage;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SerializableUUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryWriteOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.timers.TimerCallbacks;
import net.minecraft.world.level.timers.TimerQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PrimaryLevelData implements ServerLevelData, WorldData
{
    private static final Logger LOGGER = LogManager.getLogger();
    protected static final String WORLD_GEN_SETTINGS = "WorldGenSettings";
    private LevelSettings settings;
    private final WorldGenSettings worldGenSettings;
    private final Lifecycle worldGenSettingsLifecycle;
    private int xSpawn;
    private int ySpawn;
    private int zSpawn;
    private float spawnAngle;
    private long gameTime;
    private long dayTime;
    @Nullable
    private final DataFixer fixerUpper;
    private final int playerDataVersion;
    private boolean upgradedPlayerTag;
    @Nullable
    private CompoundTag loadedPlayerTag;
    private final int version;
    private int clearWeatherTime;
    private boolean raining;
    private int rainTime;
    private boolean thundering;
    private int thunderTime;
    private boolean initialized;
    private boolean difficultyLocked;
    private WorldBorder.Settings worldBorder;
    private CompoundTag endDragonFightData;
    @Nullable
    private CompoundTag customBossEvents;
    private int wanderingTraderSpawnDelay;
    private int wanderingTraderSpawnChance;
    @Nullable
    private UUID wanderingTraderId;
    private final Set<String> knownServerBrands;
    private boolean wasModded;
    private final TimerQueue<MinecraftServer> scheduledEvents;

    private PrimaryLevelData(@Nullable DataFixer p_164942_, int p_164943_, @Nullable CompoundTag p_164944_, boolean p_164945_, int p_164946_, int p_164947_, int p_164948_, float p_164949_, long p_164950_, long p_164951_, int p_164952_, int p_164953_, int p_164954_, boolean p_164955_, int p_164956_, boolean p_164957_, boolean p_164958_, boolean p_164959_, WorldBorder.Settings p_164960_, int p_164961_, int p_164962_, @Nullable UUID p_164963_, Set<String> p_164964_, TimerQueue<MinecraftServer> p_164965_, @Nullable CompoundTag p_164966_, CompoundTag p_164967_, LevelSettings p_164968_, WorldGenSettings p_164969_, Lifecycle p_164970_)
    {
        this.fixerUpper = p_164942_;
        this.wasModded = p_164945_;
        this.xSpawn = p_164946_;
        this.ySpawn = p_164947_;
        this.zSpawn = p_164948_;
        this.spawnAngle = p_164949_;
        this.gameTime = p_164950_;
        this.dayTime = p_164951_;
        this.version = p_164952_;
        this.clearWeatherTime = p_164953_;
        this.rainTime = p_164954_;
        this.raining = p_164955_;
        this.thunderTime = p_164956_;
        this.thundering = p_164957_;
        this.initialized = p_164958_;
        this.difficultyLocked = p_164959_;
        this.worldBorder = p_164960_;
        this.wanderingTraderSpawnDelay = p_164961_;
        this.wanderingTraderSpawnChance = p_164962_;
        this.wanderingTraderId = p_164963_;
        this.knownServerBrands = p_164964_;
        this.loadedPlayerTag = p_164944_;
        this.playerDataVersion = p_164943_;
        this.scheduledEvents = p_164965_;
        this.customBossEvents = p_164966_;
        this.endDragonFightData = p_164967_;
        this.settings = p_164968_;
        this.worldGenSettings = p_164969_;
        this.worldGenSettingsLifecycle = p_164970_;
    }

    public PrimaryLevelData(LevelSettings p_78470_, WorldGenSettings p_78471_, Lifecycle p_78472_)
    {
        this((DataFixer)null, SharedConstants.getCurrentVersion().getWorldVersion(), (CompoundTag)null, false, 0, 0, 0, 0.0F, 0L, 0L, 19133, 0, 0, false, 0, false, false, false, WorldBorder.DEFAULT_SETTINGS, 0, 0, (UUID)null, Sets.newLinkedHashSet(), new TimerQueue<>(TimerCallbacks.SERVER_CALLBACKS), (CompoundTag)null, new CompoundTag(), p_78470_.copy(), p_78471_, p_78472_);
    }

    public static PrimaryLevelData parse(Dynamic<Tag> pDynamic, DataFixer pDataFixer, int pVersion, @Nullable CompoundTag pPlayerNBT, LevelSettings pLevelSettings, LevelVersion pVersionData, WorldGenSettings pGeneratorSettings, Lifecycle pLifecycle)
    {
        long i = pDynamic.get("Time").asLong(0L);
        CompoundTag compoundtag = (CompoundTag)pDynamic.get("DragonFight").result().map(Dynamic::getValue).orElseGet(() ->
        {
            return pDynamic.get("DimensionData").get("1").get("DragonFight").orElseEmptyMap().getValue();
        });
        return new PrimaryLevelData(pDataFixer, pVersion, pPlayerNBT, pDynamic.get("WasModded").asBoolean(false), pDynamic.get("SpawnX").asInt(0), pDynamic.get("SpawnY").asInt(0), pDynamic.get("SpawnZ").asInt(0), pDynamic.get("SpawnAngle").asFloat(0.0F), i, pDynamic.get("DayTime").asLong(i), pVersionData.levelDataVersion(), pDynamic.get("clearWeatherTime").asInt(0), pDynamic.get("rainTime").asInt(0), pDynamic.get("raining").asBoolean(false), pDynamic.get("thunderTime").asInt(0), pDynamic.get("thundering").asBoolean(false), pDynamic.get("initialized").asBoolean(true), pDynamic.get("DifficultyLocked").asBoolean(false), WorldBorder.Settings.read(pDynamic, WorldBorder.DEFAULT_SETTINGS), pDynamic.get("WanderingTraderSpawnDelay").asInt(0), pDynamic.get("WanderingTraderSpawnChance").asInt(0), pDynamic.get("WanderingTraderId").read(SerializableUUID.CODEC).result().orElse((UUID)null), pDynamic.get("ServerBrands").asStream().flatMap((p_78529_) ->
        {
            return Util.toStream(p_78529_.asString().result());
        }).collect(Collectors.toCollection(Sets::newLinkedHashSet)), new TimerQueue<>(TimerCallbacks.SERVER_CALLBACKS, pDynamic.get("ScheduledEvents").asStream()), (CompoundTag)pDynamic.get("CustomBossEvents").orElseEmptyMap().getValue(), compoundtag, pLevelSettings, pGeneratorSettings, pLifecycle);
    }

    public CompoundTag createTag(RegistryAccess pRegistries, @Nullable CompoundTag pHostPlayerNBT)
    {
        this.updatePlayerTag();

        if (pHostPlayerNBT == null)
        {
            pHostPlayerNBT = this.loadedPlayerTag;
        }

        CompoundTag compoundtag = new CompoundTag();
        this.setTagData(pRegistries, compoundtag, pHostPlayerNBT);
        return compoundtag;
    }

    private void setTagData(RegistryAccess pRegistry, CompoundTag pNbt, @Nullable CompoundTag pPlayerNBT)
    {
        ListTag listtag = new ListTag();
        this.knownServerBrands.stream().map(StringTag::valueOf).forEach(listtag::add);
        pNbt.put("ServerBrands", listtag);
        pNbt.putBoolean("WasModded", this.wasModded);
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putString("Name", SharedConstants.getCurrentVersion().getName());
        compoundtag.putInt("Id", SharedConstants.getCurrentVersion().getWorldVersion());
        compoundtag.putBoolean("Snapshot", !SharedConstants.getCurrentVersion().isStable());
        pNbt.put("Version", compoundtag);
        pNbt.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        RegistryWriteOps<Tag> registrywriteops = RegistryWriteOps.create(NbtOps.INSTANCE, pRegistry);
        WorldGenSettings.CODEC.encodeStart(registrywriteops, this.worldGenSettings).resultOrPartial(Util.prefix("WorldGenSettings: ", LOGGER::error)).ifPresent((p_78574_) ->
        {
            pNbt.put("WorldGenSettings", p_78574_);
        });
        pNbt.putInt("GameType", this.settings.gameType().getId());
        pNbt.putInt("SpawnX", this.xSpawn);
        pNbt.putInt("SpawnY", this.ySpawn);
        pNbt.putInt("SpawnZ", this.zSpawn);
        pNbt.putFloat("SpawnAngle", this.spawnAngle);
        pNbt.putLong("Time", this.gameTime);
        pNbt.putLong("DayTime", this.dayTime);
        pNbt.putLong("LastPlayed", Util.getEpochMillis());
        pNbt.putString("LevelName", this.settings.levelName());
        pNbt.putInt("version", 19133);
        pNbt.putInt("clearWeatherTime", this.clearWeatherTime);
        pNbt.putInt("rainTime", this.rainTime);
        pNbt.putBoolean("raining", this.raining);
        pNbt.putInt("thunderTime", this.thunderTime);
        pNbt.putBoolean("thundering", this.thundering);
        pNbt.putBoolean("hardcore", this.settings.hardcore());
        pNbt.putBoolean("allowCommands", this.settings.allowCommands());
        pNbt.putBoolean("initialized", this.initialized);
        this.worldBorder.write(pNbt);
        pNbt.putByte("Difficulty", (byte)this.settings.difficulty().getId());
        pNbt.putBoolean("DifficultyLocked", this.difficultyLocked);
        pNbt.put("GameRules", this.settings.gameRules().createTag());
        pNbt.put("DragonFight", this.endDragonFightData);

        if (pPlayerNBT != null)
        {
            pNbt.put("Player", pPlayerNBT);
        }

        DataPackConfig.CODEC.encodeStart(NbtOps.INSTANCE, this.settings.getDataPackConfig()).result().ifPresent((p_78560_) ->
        {
            pNbt.put("DataPacks", p_78560_);
        });

        if (this.customBossEvents != null)
        {
            pNbt.put("CustomBossEvents", this.customBossEvents);
        }

        pNbt.put("ScheduledEvents", this.scheduledEvents.store());
        pNbt.putInt("WanderingTraderSpawnDelay", this.wanderingTraderSpawnDelay);
        pNbt.putInt("WanderingTraderSpawnChance", this.wanderingTraderSpawnChance);

        if (this.wanderingTraderId != null)
        {
            pNbt.putUUID("WanderingTraderId", this.wanderingTraderId);
        }
    }

    public int getXSpawn()
    {
        return this.xSpawn;
    }

    public int getYSpawn()
    {
        return this.ySpawn;
    }

    public int getZSpawn()
    {
        return this.zSpawn;
    }

    public float getSpawnAngle()
    {
        return this.spawnAngle;
    }

    public long getGameTime()
    {
        return this.gameTime;
    }

    public long getDayTime()
    {
        return this.dayTime;
    }

    private void updatePlayerTag()
    {
        if (!this.upgradedPlayerTag && this.loadedPlayerTag != null)
        {
            if (this.playerDataVersion < SharedConstants.getCurrentVersion().getWorldVersion())
            {
                if (this.fixerUpper == null)
                {
                    throw(NullPointerException)Util.pauseInIde(new NullPointerException("Fixer Upper not set inside LevelData, and the player tag is not upgraded."));
                }

                this.loadedPlayerTag = NbtUtils.update(this.fixerUpper, DataFixTypes.PLAYER, this.loadedPlayerTag, this.playerDataVersion);
            }

            this.upgradedPlayerTag = true;
        }
    }

    public CompoundTag getLoadedPlayerTag()
    {
        this.updatePlayerTag();
        return this.loadedPlayerTag;
    }

    public void setXSpawn(int pX)
    {
        this.xSpawn = pX;
    }

    public void setYSpawn(int pY)
    {
        this.ySpawn = pY;
    }

    public void setZSpawn(int pZ)
    {
        this.zSpawn = pZ;
    }

    public void setSpawnAngle(float pAngle)
    {
        this.spawnAngle = pAngle;
    }

    public void setGameTime(long pTime)
    {
        this.gameTime = pTime;
    }

    public void setDayTime(long pTime)
    {
        this.dayTime = pTime;
    }

    public void setSpawn(BlockPos pSpawnPoint, float pAngle)
    {
        this.xSpawn = pSpawnPoint.getX();
        this.ySpawn = pSpawnPoint.getY();
        this.zSpawn = pSpawnPoint.getZ();
        this.spawnAngle = pAngle;
    }

    public String getLevelName()
    {
        return this.settings.levelName();
    }

    public int getVersion()
    {
        return this.version;
    }

    public int getClearWeatherTime()
    {
        return this.clearWeatherTime;
    }

    public void setClearWeatherTime(int pTime)
    {
        this.clearWeatherTime = pTime;
    }

    public boolean isThundering()
    {
        return this.thundering;
    }

    public void setThundering(boolean pThundering)
    {
        this.thundering = pThundering;
    }

    public int getThunderTime()
    {
        return this.thunderTime;
    }

    public void setThunderTime(int pTime)
    {
        this.thunderTime = pTime;
    }

    public boolean isRaining()
    {
        return this.raining;
    }

    public void setRaining(boolean pIsRaining)
    {
        this.raining = pIsRaining;
    }

    public int getRainTime()
    {
        return this.rainTime;
    }

    public void setRainTime(int pTime)
    {
        this.rainTime = pTime;
    }

    public GameType getGameType()
    {
        return this.settings.gameType();
    }

    public void setGameType(GameType pType)
    {
        this.settings = this.settings.withGameType(pType);
    }

    public boolean isHardcore()
    {
        return this.settings.hardcore();
    }

    public boolean getAllowCommands()
    {
        return this.settings.allowCommands();
    }

    public boolean isInitialized()
    {
        return this.initialized;
    }

    public void setInitialized(boolean pInitialized)
    {
        this.initialized = pInitialized;
    }

    public GameRules getGameRules()
    {
        return this.settings.gameRules();
    }

    public WorldBorder.Settings getWorldBorder()
    {
        return this.worldBorder;
    }

    public void setWorldBorder(WorldBorder.Settings pSerializer)
    {
        this.worldBorder = pSerializer;
    }

    public Difficulty getDifficulty()
    {
        return this.settings.difficulty();
    }

    public void setDifficulty(Difficulty pDifficulty)
    {
        this.settings = this.settings.withDifficulty(pDifficulty);
    }

    public boolean isDifficultyLocked()
    {
        return this.difficultyLocked;
    }

    public void setDifficultyLocked(boolean pLocked)
    {
        this.difficultyLocked = pLocked;
    }

    public TimerQueue<MinecraftServer> getScheduledEvents()
    {
        return this.scheduledEvents;
    }

    public void fillCrashReportCategory(CrashReportCategory p_164972_, LevelHeightAccessor p_164973_)
    {
        ServerLevelData.super.fillCrashReportCategory(p_164972_, p_164973_);
        WorldData.super.fillCrashReportCategory(p_164972_);
    }

    public WorldGenSettings worldGenSettings()
    {
        return this.worldGenSettings;
    }

    public Lifecycle worldGenSettingsLifecycle()
    {
        return this.worldGenSettingsLifecycle;
    }

    public CompoundTag endDragonFightData()
    {
        return this.endDragonFightData;
    }

    public void setEndDragonFightData(CompoundTag pNbt)
    {
        this.endDragonFightData = pNbt;
    }

    public DataPackConfig getDataPackConfig()
    {
        return this.settings.getDataPackConfig();
    }

    public void setDataPackConfig(DataPackConfig pCodec)
    {
        this.settings = this.settings.withDataPackConfig(pCodec);
    }

    @Nullable
    public CompoundTag getCustomBossEvents()
    {
        return this.customBossEvents;
    }

    public void setCustomBossEvents(@Nullable CompoundTag pNbt)
    {
        this.customBossEvents = pNbt;
    }

    public int getWanderingTraderSpawnDelay()
    {
        return this.wanderingTraderSpawnDelay;
    }

    public void setWanderingTraderSpawnDelay(int pDelay)
    {
        this.wanderingTraderSpawnDelay = pDelay;
    }

    public int getWanderingTraderSpawnChance()
    {
        return this.wanderingTraderSpawnChance;
    }

    public void setWanderingTraderSpawnChance(int pChance)
    {
        this.wanderingTraderSpawnChance = pChance;
    }

    @Nullable
    public UUID getWanderingTraderId()
    {
        return this.wanderingTraderId;
    }

    public void setWanderingTraderId(UUID pId)
    {
        this.wanderingTraderId = pId;
    }

    public void setModdedInfo(String pName, boolean pIsModded)
    {
        this.knownServerBrands.add(pName);
        this.wasModded |= pIsModded;
    }

    public boolean wasModded()
    {
        return this.wasModded;
    }

    public Set<String> getKnownServerBrands()
    {
        return ImmutableSet.copyOf(this.knownServerBrands);
    }

    public ServerLevelData overworldData()
    {
        return this;
    }

    public LevelSettings getLevelSettings()
    {
        return this.settings.copy();
    }
}
