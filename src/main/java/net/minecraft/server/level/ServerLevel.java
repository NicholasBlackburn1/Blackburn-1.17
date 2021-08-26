package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddVibrationSignalPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagContainer;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.Mth;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raids;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.BlockEventData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerTickList;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.TickNextTickData;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListenerRegistrar;
import net.minecraft.world.level.gameevent.vibrations.VibrationPath;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.portal.PortalForcer;
import net.minecraft.world.level.saveddata.maps.MapIndex;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerLevel extends Level implements WorldGenLevel
{
    public static final BlockPos END_SPAWN_POINT = new BlockPos(100, 50, 0);
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int EMPTY_TIME_NO_TICK = 300;
    final List<ServerPlayer> players = Lists.newArrayList();
    private final ServerChunkCache chunkSource;
    private final MinecraftServer server;
    private final ServerLevelData serverLevelData;
    final EntityTickList entityTickList = new EntityTickList();
    private final PersistentEntitySectionManager<Entity> entityManager;
    public boolean noSave;
    private final SleepStatus sleepStatus;
    private int emptyTime;
    private final PortalForcer portalForcer;
    private final ServerTickList<Block> blockTicks = new ServerTickList<>(this, (p_8711_) ->
    {
        return p_8711_ == null || p_8711_.defaultBlockState().isAir();
    }, Registry.BLOCK::getKey, this::tickBlock);
    private final ServerTickList<Fluid> liquidTicks = new ServerTickList<>(this, (p_8728_) ->
    {
        return p_8728_ == null || p_8728_ == Fluids.EMPTY;
    }, Registry.FLUID::getKey, this::tickLiquid);
    final Set<Mob> navigatingMobs = new ObjectOpenHashSet<>();
    protected final Raids raids;
    private final ObjectLinkedOpenHashSet<BlockEventData> blockEvents = new ObjectLinkedOpenHashSet<>();
    private boolean handlingTick;
    private final List<CustomSpawner> customSpawners;
    @Nullable
    private final EndDragonFight dragonFight;
    final Int2ObjectMap<EnderDragonPart> dragonParts = new Int2ObjectOpenHashMap<>();
    private final StructureFeatureManager structureFeatureManager;
    private final boolean tickTime;

    public ServerLevel(MinecraftServer p_8571_, Executor p_8572_, LevelStorageSource.LevelStorageAccess p_8573_, ServerLevelData p_8574_, ResourceKey<Level> p_8575_, DimensionType p_8576_, ChunkProgressListener p_8577_, ChunkGenerator p_8578_, boolean p_8579_, long p_8580_, List<CustomSpawner> p_8581_, boolean p_8582_)
    {
        super(p_8574_, p_8575_, p_8576_, p_8571_::getProfiler, false, p_8579_, p_8580_);
        this.tickTime = p_8582_;
        this.server = p_8571_;
        this.customSpawners = p_8581_;
        this.serverLevelData = p_8574_;
        boolean flag = p_8571_.forceSynchronousWrites();
        DataFixer datafixer = p_8571_.getFixerUpper();
        EntityPersistentStorage<Entity> entitypersistentstorage = new EntityStorage(this, new File(p_8573_.getDimensionPath(p_8575_), "entities"), datafixer, flag, p_8571_);
        this.entityManager = new PersistentEntitySectionManager<>(Entity.class, new ServerLevel.EntityCallbacks(), entitypersistentstorage);
        this.chunkSource = new ServerChunkCache(this, p_8573_, datafixer, p_8571_.getStructureManager(), p_8572_, p_8578_, p_8571_.getPlayerList().getViewDistance(), flag, p_8577_, this.entityManager::updateChunkStatus, () ->
        {
            return p_8571_.overworld().getDataStorage();
        });
        this.portalForcer = new PortalForcer(this);
        this.updateSkyBrightness();
        this.prepareWeather();
        this.getWorldBorder().setAbsoluteMaxSize(p_8571_.getAbsoluteMaxWorldSize());
        this.raids = this.getDataStorage().computeIfAbsent((p_143314_) ->
        {
            return Raids.load(this, p_143314_);
        }, () ->
        {
            return new Raids(this);
        }, Raids.getFileId(this.dimensionType()));

        if (!p_8571_.isSingleplayer())
        {
            p_8574_.setGameType(p_8571_.getDefaultGameType());
        }

        this.structureFeatureManager = new StructureFeatureManager(this, p_8571_.getWorldData().worldGenSettings());

        if (this.dimensionType().createDragonFight())
        {
            this.dragonFight = new EndDragonFight(this, p_8571_.getWorldData().worldGenSettings().seed(), p_8571_.getWorldData().endDragonFightData());
        }
        else
        {
            this.dragonFight = null;
        }

        this.sleepStatus = new SleepStatus();
    }

    public void setWeatherParameters(int pClearTime, int pWeatherTime, boolean pIsRaining, boolean pIsThundering)
    {
        this.serverLevelData.setClearWeatherTime(pClearTime);
        this.serverLevelData.setRainTime(pWeatherTime);
        this.serverLevelData.setThunderTime(pWeatherTime);
        this.serverLevelData.setRaining(pIsRaining);
        this.serverLevelData.setThundering(pIsThundering);
    }

    public Biome getUncachedNoiseBiome(int pX, int pY, int pZ)
    {
        return this.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(pX, pY, pZ);
    }

    public StructureFeatureManager structureFeatureManager()
    {
        return this.structureFeatureManager;
    }

    public void tick(BooleanSupplier pHasTimeLeft)
    {
        ProfilerFiller profilerfiller = this.getProfiler();
        this.handlingTick = true;
        profilerfiller.push("world border");
        this.getWorldBorder().tick();
        profilerfiller.popPush("weather");
        boolean flag = this.isRaining();

        if (this.dimensionType().hasSkyLight())
        {
            if (this.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE))
            {
                int i = this.serverLevelData.getClearWeatherTime();
                int j = this.serverLevelData.getThunderTime();
                int k = this.serverLevelData.getRainTime();
                boolean flag1 = this.levelData.isThundering();
                boolean flag2 = this.levelData.isRaining();

                if (i > 0)
                {
                    --i;
                    j = flag1 ? 0 : 1;
                    k = flag2 ? 0 : 1;
                    flag1 = false;
                    flag2 = false;
                }
                else
                {
                    if (j > 0)
                    {
                        --j;

                        if (j == 0)
                        {
                            flag1 = !flag1;
                        }
                    }
                    else if (flag1)
                    {
                        j = this.random.nextInt(12000) + 3600;
                    }
                    else
                    {
                        j = this.random.nextInt(168000) + 12000;
                    }

                    if (k > 0)
                    {
                        --k;

                        if (k == 0)
                        {
                            flag2 = !flag2;
                        }
                    }
                    else if (flag2)
                    {
                        k = this.random.nextInt(12000) + 12000;
                    }
                    else
                    {
                        k = this.random.nextInt(168000) + 12000;
                    }
                }

                this.serverLevelData.setThunderTime(j);
                this.serverLevelData.setRainTime(k);
                this.serverLevelData.setClearWeatherTime(i);
                this.serverLevelData.setThundering(flag1);
                this.serverLevelData.setRaining(flag2);
            }

            this.oThunderLevel = this.thunderLevel;

            if (this.levelData.isThundering())
            {
                this.thunderLevel = (float)((double)this.thunderLevel + 0.01D);
            }
            else
            {
                this.thunderLevel = (float)((double)this.thunderLevel - 0.01D);
            }

            this.thunderLevel = Mth.clamp(this.thunderLevel, 0.0F, 1.0F);
            this.oRainLevel = this.rainLevel;

            if (this.levelData.isRaining())
            {
                this.rainLevel = (float)((double)this.rainLevel + 0.01D);
            }
            else
            {
                this.rainLevel = (float)((double)this.rainLevel - 0.01D);
            }

            this.rainLevel = Mth.clamp(this.rainLevel, 0.0F, 1.0F);
        }

        if (this.oRainLevel != this.rainLevel)
        {
            this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, this.rainLevel), this.dimension());
        }

        if (this.oThunderLevel != this.thunderLevel)
        {
            this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, this.thunderLevel), this.dimension());
        }

        if (flag != this.isRaining())
        {
            if (flag)
            {
                this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.STOP_RAINING, 0.0F));
            }
            else
            {
                this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F));
            }

            this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, this.rainLevel));
            this.server.getPlayerList().broadcastAll(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, this.thunderLevel));
        }

        int l = this.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);

        if (this.sleepStatus.areEnoughSleeping(l) && this.sleepStatus.areEnoughDeepSleeping(l, this.players))
        {
            if (this.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT))
            {
                long i1 = this.levelData.getDayTime() + 24000L;
                this.setDayTime(i1 - i1 % 24000L);
            }

            this.wakeUpAllPlayers();

            if (this.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE))
            {
                this.stopWeather();
            }
        }

        this.updateSkyBrightness();
        this.tickTime();
        profilerfiller.popPush("tickPending");

        if (!this.isDebug())
        {
            this.blockTicks.tick();
            this.liquidTicks.tick();
        }

        profilerfiller.popPush("raid");
        this.raids.tick();
        profilerfiller.popPush("chunkSource");
        this.getChunkSource().tick(pHasTimeLeft);
        profilerfiller.popPush("blockEvents");
        this.runBlockEvents();
        this.handlingTick = false;
        profilerfiller.pop();
        boolean flag3 = !this.players.isEmpty() || !this.getForcedChunks().isEmpty();

        if (flag3)
        {
            this.resetEmptyTime();
        }

        if (flag3 || this.emptyTime++ < 300)
        {
            profilerfiller.push("entities");

            if (this.dragonFight != null)
            {
                profilerfiller.push("dragonFight");
                this.dragonFight.tick();
                profilerfiller.pop();
            }

            this.entityTickList.forEach((p_143266_) ->
            {
                if (!p_143266_.isRemoved())
                {
                    if (this.shouldDiscardEntity(p_143266_))
                    {
                        p_143266_.discard();
                    }
                    else
                    {
                        profilerfiller.push("checkDespawn");
                        p_143266_.checkDespawn();
                        profilerfiller.pop();
                        Entity entity = p_143266_.getVehicle();

                        if (entity != null)
                        {
                            if (!entity.isRemoved() && entity.hasPassenger(p_143266_))
                            {
                                return;
                            }

                            p_143266_.stopRiding();
                        }

                        profilerfiller.push("tick");
                        this.guardEntityTick(this::tickNonPassenger, p_143266_);
                        profilerfiller.pop();
                    }
                }
            });
            profilerfiller.pop();
            this.tickBlockEntities();
        }

        profilerfiller.push("entityManagement");
        this.entityManager.tick();
        profilerfiller.pop();
    }

    protected void tickTime()
    {
        if (this.tickTime)
        {
            long i = this.levelData.getGameTime() + 1L;
            this.serverLevelData.setGameTime(i);
            this.serverLevelData.getScheduledEvents().tick(this.server, i);

            if (this.levelData.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT))
            {
                this.setDayTime(this.levelData.getDayTime() + 1L);
            }
        }
    }

    public void setDayTime(long pTime)
    {
        this.serverLevelData.setDayTime(pTime);
    }

    public void tickCustomSpawners(boolean pSpawnHostiles, boolean pSpawnPassives)
    {
        for (CustomSpawner customspawner : this.customSpawners)
        {
            customspawner.tick(this, pSpawnHostiles, pSpawnPassives);
        }
    }

    private boolean shouldDiscardEntity(Entity p_143343_)
    {
        if (this.server.isSpawningAnimals() || !(p_143343_ instanceof Animal) && !(p_143343_ instanceof WaterAnimal))
        {
            return !this.server.areNpcsEnabled() && p_143343_ instanceof Npc;
        }
        else
        {
            return true;
        }
    }

    private void wakeUpAllPlayers()
    {
        this.sleepStatus.removeAllSleepers();
        this.players.stream().filter(LivingEntity::isSleeping).collect(Collectors.toList()).forEach((p_143339_) ->
        {
            p_143339_.stopSleepInBed(false, false);
        });
    }

    public void tickChunk(LevelChunk pChunk, int pRandomTickSpeed)
    {
        ChunkPos chunkpos = pChunk.getPos();
        boolean flag = this.isRaining();
        int i = chunkpos.getMinBlockX();
        int j = chunkpos.getMinBlockZ();
        ProfilerFiller profilerfiller = this.getProfiler();
        profilerfiller.push("thunder");

        if (flag && this.isThundering() && this.random.nextInt(100000) == 0)
        {
            BlockPos blockpos = this.findLightningTargetAround(this.getBlockRandomPos(i, 0, j, 15));

            if (this.isRainingAt(blockpos))
            {
                DifficultyInstance difficultyinstance = this.getCurrentDifficultyAt(blockpos);
                boolean flag1 = this.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) && this.random.nextDouble() < (double)difficultyinstance.getEffectiveDifficulty() * 0.01D && !this.getBlockState(blockpos.below()).is(Blocks.LIGHTNING_ROD);

                if (flag1)
                {
                    SkeletonHorse skeletonhorse = EntityType.SKELETON_HORSE.create(this);
                    skeletonhorse.setTrap(true);
                    skeletonhorse.setAge(0);
                    skeletonhorse.setPos((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
                    this.addFreshEntity(skeletonhorse);
                }

                LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(this);
                lightningbolt.moveTo(Vec3.atBottomCenterOf(blockpos));
                lightningbolt.setVisualOnly(flag1);
                this.addFreshEntity(lightningbolt);
            }
        }

        profilerfiller.popPush("iceandsnow");

        if (this.random.nextInt(16) == 0)
        {
            BlockPos blockpos2 = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, this.getBlockRandomPos(i, 0, j, 15));
            BlockPos blockpos3 = blockpos2.below();
            Biome biome = this.getBiome(blockpos2);

            if (biome.shouldFreeze(this, blockpos3))
            {
                this.setBlockAndUpdate(blockpos3, Blocks.ICE.defaultBlockState());
            }

            if (flag)
            {
                if (biome.shouldSnow(this, blockpos2))
                {
                    this.setBlockAndUpdate(blockpos2, Blocks.SNOW.defaultBlockState());
                }

                BlockState blockstate1 = this.getBlockState(blockpos3);
                Biome.Precipitation biome$precipitation = this.getBiome(blockpos2).getPrecipitation();

                if (biome$precipitation == Biome.Precipitation.RAIN && biome.isColdEnoughToSnow(blockpos3))
                {
                    biome$precipitation = Biome.Precipitation.SNOW;
                }

                blockstate1.getBlock().handlePrecipitation(blockstate1, this, blockpos3, biome$precipitation);
            }
        }

        profilerfiller.popPush("tickBlocks");

        if (pRandomTickSpeed > 0)
        {
            for (LevelChunkSection levelchunksection : pChunk.getSections())
            {
                if (levelchunksection != LevelChunk.EMPTY_SECTION && levelchunksection.isRandomlyTicking())
                {
                    int l = levelchunksection.bottomBlockY();

                    for (int k = 0; k < pRandomTickSpeed; ++k)
                    {
                        BlockPos blockpos1 = this.getBlockRandomPos(i, l, j, 15);
                        profilerfiller.push("randomTick");
                        BlockState blockstate = levelchunksection.getBlockState(blockpos1.getX() - i, blockpos1.getY() - l, blockpos1.getZ() - j);

                        if (blockstate.isRandomlyTicking())
                        {
                            blockstate.randomTick(this, blockpos1, this.random);
                        }

                        FluidState fluidstate = blockstate.getFluidState();

                        if (fluidstate.isRandomlyTicking())
                        {
                            fluidstate.randomTick(this, blockpos1, this.random);
                        }

                        profilerfiller.pop();
                    }
                }
            }
        }

        profilerfiller.pop();
    }

    private Optional<BlockPos> findLightningRod(BlockPos p_143249_)
    {
        Optional<BlockPos> optional = this.getPoiManager().findClosest((p_143274_) ->
        {
            return p_143274_ == PoiType.LIGHTNING_ROD;
        }, (p_143255_) ->
        {
            return p_143255_.getY() == this.getLevel().getHeight(Heightmap.Types.WORLD_SURFACE, p_143255_.getX(), p_143255_.getZ()) - 1;
        }, p_143249_, 128, PoiManager.Occupancy.ANY);
        return optional.map((p_143253_) ->
        {
            return p_143253_.above(1);
        });
    }

    protected BlockPos findLightningTargetAround(BlockPos p_143289_)
    {
        BlockPos blockpos = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, p_143289_);
        Optional<BlockPos> optional = this.findLightningRod(blockpos);

        if (optional.isPresent())
        {
            return optional.get();
        }
        else
        {
            AABB aabb = (new AABB(blockpos, new BlockPos(blockpos.getX(), this.getMaxBuildHeight(), blockpos.getZ()))).inflate(3.0D);
            List<LivingEntity> list = this.getEntitiesOfClass(LivingEntity.class, aabb, (p_143272_) ->
            {
                return p_143272_ != null && p_143272_.isAlive() && this.canSeeSky(p_143272_.blockPosition());
            });

            if (!list.isEmpty())
            {
                return list.get(this.random.nextInt(list.size())).blockPosition();
            }
            else
            {
                if (blockpos.getY() == this.getMinBuildHeight() - 1)
                {
                    blockpos = blockpos.above(2);
                }

                return blockpos;
            }
        }
    }

    public boolean isHandlingTick()
    {
        return this.handlingTick;
    }

    public boolean canSleepThroughNights()
    {
        return this.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE) <= 100;
    }

    private void announceSleepStatus()
    {
        if (this.canSleepThroughNights())
        {
            if (!this.getServer().isSingleplayer() || this.getServer().isPublished())
            {
                int i = this.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);
                Component component;

                if (this.sleepStatus.areEnoughSleeping(i))
                {
                    component = new TranslatableComponent("sleep.skipping_night");
                }
                else
                {
                    component = new TranslatableComponent("sleep.players_sleeping", this.sleepStatus.amountSleeping(), this.sleepStatus.sleepersNeeded(i));
                }

                for (ServerPlayer serverplayer : this.players)
                {
                    serverplayer.displayClientMessage(component, true);
                }
            }
        }
    }

    public void updateSleepingPlayerList()
    {
        if (!this.players.isEmpty() && this.sleepStatus.update(this.players))
        {
            this.announceSleepStatus();
        }
    }

    public ServerScoreboard getScoreboard()
    {
        return this.server.getScoreboard();
    }

    private void stopWeather()
    {
        this.serverLevelData.setRainTime(0);
        this.serverLevelData.setRaining(false);
        this.serverLevelData.setThunderTime(0);
        this.serverLevelData.setThundering(false);
    }

    public void resetEmptyTime()
    {
        this.emptyTime = 0;
    }

    private void tickLiquid(TickNextTickData<Fluid> pFluidTickEntry)
    {
        FluidState fluidstate = this.getFluidState(pFluidTickEntry.pos);

        if (fluidstate.getType() == pFluidTickEntry.getType())
        {
            fluidstate.tick(this, pFluidTickEntry.pos);
        }
    }

    private void tickBlock(TickNextTickData<Block> pBlockTickEntry)
    {
        BlockState blockstate = this.getBlockState(pBlockTickEntry.pos);

        if (blockstate.is(pBlockTickEntry.getType()))
        {
            blockstate.tick(this, pBlockTickEntry.pos, this.random);
        }
    }

    public void tickNonPassenger(Entity pEntity)
    {
        pEntity.setOldPosAndRot();
        ProfilerFiller profilerfiller = this.getProfiler();
        ++pEntity.tickCount;
        this.getProfiler().push(() ->
        {
            return Registry.ENTITY_TYPE.getKey(pEntity.getType()).toString();
        });
        profilerfiller.incrementCounter("tickNonPassenger");
        pEntity.tick();
        this.getProfiler().pop();

        for (Entity entity : pEntity.getPassengers())
        {
            this.tickPassenger(pEntity, entity);
        }
    }

    private void tickPassenger(Entity pRidingEntity, Entity pPassengerEntity)
    {
        if (!pPassengerEntity.isRemoved() && pPassengerEntity.getVehicle() == pRidingEntity)
        {
            if (pPassengerEntity instanceof Player || this.entityTickList.contains(pPassengerEntity))
            {
                pPassengerEntity.setOldPosAndRot();
                ++pPassengerEntity.tickCount;
                ProfilerFiller profilerfiller = this.getProfiler();
                profilerfiller.push(() ->
                {
                    return Registry.ENTITY_TYPE.getKey(pPassengerEntity.getType()).toString();
                });
                profilerfiller.incrementCounter("tickPassenger");
                pPassengerEntity.rideTick();
                profilerfiller.pop();

                for (Entity entity : pPassengerEntity.getPassengers())
                {
                    this.tickPassenger(pPassengerEntity, entity);
                }
            }
        }
        else
        {
            pPassengerEntity.stopRiding();
        }
    }

    public boolean mayInteract(Player pPlayer, BlockPos pPos)
    {
        return !this.server.isUnderSpawnProtection(this, pPos, pPlayer) && this.getWorldBorder().isWithinBounds(pPos);
    }

    public void save(@Nullable ProgressListener pProgress, boolean pFlush, boolean pSkipSave)
    {
        ServerChunkCache serverchunkcache = this.getChunkSource();

        if (!pSkipSave)
        {
            if (pProgress != null)
            {
                pProgress.progressStartNoAbort(new TranslatableComponent("menu.savingLevel"));
            }

            this.saveLevelData();

            if (pProgress != null)
            {
                pProgress.progressStage(new TranslatableComponent("menu.savingChunks"));
            }

            serverchunkcache.save(pFlush);

            if (pFlush)
            {
                this.entityManager.saveAll();
            }
            else
            {
                this.entityManager.autoSave();
            }
        }
    }

    private void saveLevelData()
    {
        if (this.dragonFight != null)
        {
            this.server.getWorldData().setEndDragonFightData(this.dragonFight.saveData());
        }

        this.getChunkSource().getDataStorage().save();
    }

    public <T extends Entity> List <? extends T > getEntities(EntityTypeTest<Entity, T> p_143281_, Predicate <? super T > p_143282_)
    {
        List<T> list = Lists.newArrayList();
        this.getEntities().get(p_143281_, (p_143310_) ->
        {
            if (p_143282_.test(p_143310_))
            {
                list.add(p_143310_);
            }
        });
        return list;
    }

    public List <? extends EnderDragon > getDragons()
    {
        return this.getEntities(EntityType.ENDER_DRAGON, LivingEntity::isAlive);
    }

    public List<ServerPlayer> getPlayers(Predicate <? super ServerPlayer > pPredicate)
    {
        List<ServerPlayer> list = Lists.newArrayList();

        for (ServerPlayer serverplayer : this.players)
        {
            if (pPredicate.test(serverplayer))
            {
                list.add(serverplayer);
            }
        }

        return list;
    }

    @Nullable
    public ServerPlayer getRandomPlayer()
    {
        List<ServerPlayer> list = this.getPlayers(LivingEntity::isAlive);
        return list.isEmpty() ? null : list.get(this.random.nextInt(list.size()));
    }

    public boolean addFreshEntity(Entity pEntity)
    {
        return this.addEntity(pEntity);
    }

    public boolean addWithUUID(Entity pEntity)
    {
        return this.addEntity(pEntity);
    }

    public void addDuringTeleport(Entity p_143335_)
    {
        this.addEntity(p_143335_);
    }

    public void addDuringCommandTeleport(ServerPlayer pPlayer)
    {
        this.addPlayer(pPlayer);
    }

    public void addDuringPortalTeleport(ServerPlayer pPlayer)
    {
        this.addPlayer(pPlayer);
    }

    public void addNewPlayer(ServerPlayer pPlayer)
    {
        this.addPlayer(pPlayer);
    }

    public void addRespawnedPlayer(ServerPlayer pPlayer)
    {
        this.addPlayer(pPlayer);
    }

    private void addPlayer(ServerPlayer pPlayer)
    {
        Entity entity = this.getEntities().get(pPlayer.getUUID());

        if (entity != null)
        {
            LOGGER.warn("Force-added player with duplicate UUID {}", (Object)pPlayer.getUUID().toString());
            entity.unRide();
            this.removePlayerImmediately((ServerPlayer)entity, Entity.RemovalReason.DISCARDED);
        }

        this.entityManager.addNewEntity(pPlayer);
    }

    private boolean addEntity(Entity pEntity)
    {
        if (pEntity.isRemoved())
        {
            LOGGER.warn("Tried to add entity {} but it was marked as removed already", (Object)EntityType.getKey(pEntity.getType()));
            return false;
        }
        else
        {
            return this.entityManager.addNewEntity(pEntity);
        }
    }

    public boolean tryAddFreshEntityWithPassengers(Entity pEntity)
    {
        if (pEntity.getSelfAndPassengers().map(Entity::getUUID).anyMatch(this.entityManager::isLoaded))
        {
            return false;
        }
        else
        {
            this.addFreshEntityWithPassengers(pEntity);
            return true;
        }
    }

    public void unload(LevelChunk pChunk)
    {
        pChunk.invalidateAllBlockEntities();
    }

    public void removePlayerImmediately(ServerPlayer p_143262_, Entity.RemovalReason p_143263_)
    {
        p_143262_.remove(p_143263_);
    }

    public void destroyBlockProgress(int pBreakerId, BlockPos pPos, int pProgress)
    {
        for (ServerPlayer serverplayer : this.server.getPlayerList().getPlayers())
        {
            if (serverplayer != null && serverplayer.level == this && serverplayer.getId() != pBreakerId)
            {
                double d0 = (double)pPos.getX() - serverplayer.getX();
                double d1 = (double)pPos.getY() - serverplayer.getY();
                double d2 = (double)pPos.getZ() - serverplayer.getZ();

                if (d0 * d0 + d1 * d1 + d2 * d2 < 1024.0D)
                {
                    serverplayer.connection.send(new ClientboundBlockDestructionPacket(pBreakerId, pPos, pProgress));
                }
            }
        }
    }

    public void playSound(@Nullable Player pPlayer, double pX, double p_8677_, double pY, SoundEvent p_8679_, SoundSource pZ, float p_8681_, float pSound)
    {
        this.server.getPlayerList().broadcast(pPlayer, pX, p_8677_, pY, p_8681_ > 1.0F ? (double)(16.0F * p_8681_) : 16.0D, this.dimension(), new ClientboundSoundPacket(p_8679_, pZ, pX, p_8677_, pY, p_8681_, pSound));
    }

    public void playSound(@Nullable Player pPlayer, Entity pX, SoundEvent p_8691_, SoundSource pY, float p_8693_, float pZ)
    {
        this.server.getPlayerList().broadcast(pPlayer, pX.getX(), pX.getY(), pX.getZ(), p_8693_ > 1.0F ? (double)(16.0F * p_8693_) : 16.0D, this.dimension(), new ClientboundSoundEntityPacket(p_8691_, pY, pX, p_8693_, pZ));
    }

    public void globalLevelEvent(int pId, BlockPos pPos, int pData)
    {
        this.server.getPlayerList().broadcastAll(new ClientboundLevelEventPacket(pId, pPos, pData, true));
    }

    public void levelEvent(@Nullable Player pPlayer, int pType, BlockPos pPos, int pData)
    {
        this.server.getPlayerList().broadcast(pPlayer, (double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ(), 64.0D, this.dimension(), new ClientboundLevelEventPacket(pType, pPos, pData, false));
    }

    public int getLogicalHeight()
    {
        return this.dimensionType().logicalHeight();
    }

    public void gameEvent(@Nullable Entity p_143268_, GameEvent p_143269_, BlockPos p_143270_)
    {
        this.postGameEventInRadius(p_143268_, p_143269_, p_143270_, p_143269_.getNotificationRadius());
    }

    public void sendBlockUpdated(BlockPos pPos, BlockState pOldState, BlockState pNewState, int pFlags)
    {
        this.getChunkSource().blockChanged(pPos);
        VoxelShape voxelshape = pOldState.getCollisionShape(this, pPos);
        VoxelShape voxelshape1 = pNewState.getCollisionShape(this, pPos);

        if (Shapes.joinIsNotEmpty(voxelshape, voxelshape1, BooleanOp.NOT_SAME))
        {
            for (Mob mob : this.navigatingMobs)
            {
                PathNavigation pathnavigation = mob.getNavigation();

                if (!pathnavigation.hasDelayedRecomputation())
                {
                    pathnavigation.recomputePath(pPos);
                }
            }
        }
    }

    public void broadcastEntityEvent(Entity pEntity, byte pState)
    {
        this.getChunkSource().broadcastAndSend(pEntity, new ClientboundEntityEventPacket(pEntity, pState));
    }

    public ServerChunkCache getChunkSource()
    {
        return this.chunkSource;
    }

    public Explosion explode(@Nullable Entity pExploder, @Nullable DamageSource pDamageSource, @Nullable ExplosionDamageCalculator pContext, double pX, double p_8657_, double pY, float p_8659_, boolean pZ, Explosion.BlockInteraction p_8661_)
    {
        Explosion explosion = new Explosion(this, pExploder, pDamageSource, pContext, pX, p_8657_, pY, p_8659_, pZ, p_8661_);
        explosion.explode();
        explosion.finalizeExplosion(false);

        if (p_8661_ == Explosion.BlockInteraction.NONE)
        {
            explosion.clearToBlow();
        }

        for (ServerPlayer serverplayer : this.players)
        {
            if (serverplayer.distanceToSqr(pX, p_8657_, pY) < 4096.0D)
            {
                serverplayer.connection.send(new ClientboundExplodePacket(pX, p_8657_, pY, p_8659_, explosion.getToBlow(), explosion.getHitPlayers().get(serverplayer)));
            }
        }

        return explosion;
    }

    public void blockEvent(BlockPos pPos, Block pBlock, int pEventID, int pEventParam)
    {
        this.blockEvents.add(new BlockEventData(pPos, pBlock, pEventID, pEventParam));
    }

    private void runBlockEvents()
    {
        while (!this.blockEvents.isEmpty())
        {
            BlockEventData blockeventdata = this.blockEvents.removeFirst();

            if (this.doBlockEvent(blockeventdata))
            {
                this.server.getPlayerList().broadcast((Player)null, (double)blockeventdata.getPos().getX(), (double)blockeventdata.getPos().getY(), (double)blockeventdata.getPos().getZ(), 64.0D, this.dimension(), new ClientboundBlockEventPacket(blockeventdata.getPos(), blockeventdata.getBlock(), blockeventdata.getParamA(), blockeventdata.getParamB()));
            }
        }
    }

    private boolean doBlockEvent(BlockEventData pEvent)
    {
        BlockState blockstate = this.getBlockState(pEvent.getPos());
        return blockstate.is(pEvent.getBlock()) ? blockstate.triggerEvent(this, pEvent.getPos(), pEvent.getParamA(), pEvent.getParamB()) : false;
    }

    public ServerTickList<Block> getBlockTicks()
    {
        return this.blockTicks;
    }

    public ServerTickList<Fluid> getLiquidTicks()
    {
        return this.liquidTicks;
    }

    @Nonnull
    public MinecraftServer getServer()
    {
        return this.server;
    }

    public PortalForcer getPortalForcer()
    {
        return this.portalForcer;
    }

    public StructureManager getStructureManager()
    {
        return this.server.getStructureManager();
    }

    public void sendVibrationParticle(VibrationPath p_143284_)
    {
        BlockPos blockpos = p_143284_.getOrigin();
        ClientboundAddVibrationSignalPacket clientboundaddvibrationsignalpacket = new ClientboundAddVibrationSignalPacket(p_143284_);
        this.players.forEach((p_143296_) ->
        {
            this.sendParticles(p_143296_, false, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), clientboundaddvibrationsignalpacket);
        });
    }

    public <T extends ParticleOptions> int sendParticles(T pType, double pPosX, double p_8770_, double pPosY, int p_8772_, double pPosZ, double p_8774_, double pParticleCount, double pXOffset)
    {
        ClientboundLevelParticlesPacket clientboundlevelparticlespacket = new ClientboundLevelParticlesPacket(pType, false, pPosX, p_8770_, pPosY, (float)pPosZ, (float)p_8774_, (float)pParticleCount, (float)pXOffset, p_8772_);
        int i = 0;

        for (int j = 0; j < this.players.size(); ++j)
        {
            ServerPlayer serverplayer = this.players.get(j);

            if (this.sendParticles(serverplayer, false, pPosX, p_8770_, pPosY, clientboundlevelparticlespacket))
            {
                ++i;
            }
        }

        return i;
    }

    public <T extends ParticleOptions> boolean sendParticles(ServerPlayer pType, T pPosX, boolean p_8627_, double pPosY, double p_8629_, double pPosZ, int p_8631_, double pParticleCount, double pXOffset, double p_8634_, double pYOffset)
    {
        Packet<?> packet = new ClientboundLevelParticlesPacket(pPosX, p_8627_, pPosY, p_8629_, pPosZ, (float)pParticleCount, (float)pXOffset, (float)p_8634_, (float)pYOffset, p_8631_);
        return this.sendParticles(pType, p_8627_, pPosY, p_8629_, pPosZ, packet);
    }

    private boolean sendParticles(ServerPlayer pType, boolean pPosX, double p_8639_, double pPosY, double p_8641_, Packet<?> pPosZ)
    {
        if (pType.getLevel() != this)
        {
            return false;
        }
        else
        {
            BlockPos blockpos = pType.blockPosition();

            if (blockpos.closerThan(new Vec3(p_8639_, pPosY, p_8641_), pPosX ? 512.0D : 32.0D))
            {
                pType.connection.send(pPosZ);
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    @Nullable
    public Entity getEntity(int pId)
    {
        return this.getEntities().get(pId);
    }

    @Deprecated
    @Nullable
    public Entity getEntityOrPart(int p_143318_)
    {
        Entity entity = this.getEntities().get(p_143318_);
        return entity != null ? entity : this.dragonParts.get(p_143318_);
    }

    @Nullable
    public Entity getEntity(UUID pId)
    {
        return this.getEntities().get(pId);
    }

    @Nullable
    public BlockPos findNearestMapFeature(StructureFeature<?> pStructure, BlockPos pPos, int pRadius, boolean pSkipExistingChunks)
    {
        return !this.server.getWorldData().worldGenSettings().generateFeatures() ? null : this.getChunkSource().getGenerator().findNearestMapFeature(this, pStructure, pPos, pRadius, pSkipExistingChunks);
    }

    @Nullable
    public BlockPos findNearestBiome(Biome pBiome, BlockPos pPos, int pRadius, int pIncrement)
    {
        return this.getChunkSource().getGenerator().getBiomeSource().findBiomeHorizontal(pPos.getX(), pPos.getY(), pPos.getZ(), pRadius, pIncrement, (p_143279_) ->
        {
            return p_143279_ == pBiome;
        }, this.random, true);
    }

    public RecipeManager getRecipeManager()
    {
        return this.server.getRecipeManager();
    }

    public TagContainer getTagManager()
    {
        return this.server.getTags();
    }

    public boolean noSave()
    {
        return this.noSave;
    }

    public RegistryAccess registryAccess()
    {
        return this.server.registryAccess();
    }

    public DimensionDataStorage getDataStorage()
    {
        return this.getChunkSource().getDataStorage();
    }

    @Nullable
    public MapItemSavedData getMapData(String pMapName)
    {
        return this.getServer().overworld().getDataStorage().get(MapItemSavedData::load, pMapName);
    }

    public void setMapData(String p_143305_, MapItemSavedData p_143306_)
    {
        this.getServer().overworld().getDataStorage().set(p_143305_, p_143306_);
    }

    public int getFreeMapId()
    {
        return this.getServer().overworld().getDataStorage().computeIfAbsent(MapIndex::load, MapIndex::new, "idcounts").getFreeAuxValueForMap();
    }

    public void setDefaultSpawnPos(BlockPos pPos, float pAngle)
    {
        ChunkPos chunkpos = new ChunkPos(new BlockPos(this.levelData.getXSpawn(), 0, this.levelData.getZSpawn()));
        this.levelData.setSpawn(pPos, pAngle);
        this.getChunkSource().removeRegionTicket(TicketType.START, chunkpos, 11, Unit.INSTANCE);
        this.getChunkSource().addRegionTicket(TicketType.START, new ChunkPos(pPos), 11, Unit.INSTANCE);
        this.getServer().getPlayerList().broadcastAll(new ClientboundSetDefaultSpawnPositionPacket(pPos, pAngle));
    }

    public BlockPos getSharedSpawnPos()
    {
        BlockPos blockpos = new BlockPos(this.levelData.getXSpawn(), this.levelData.getYSpawn(), this.levelData.getZSpawn());

        if (!this.getWorldBorder().isWithinBounds(blockpos))
        {
            blockpos = this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(this.getWorldBorder().getCenterX(), 0.0D, this.getWorldBorder().getCenterZ()));
        }

        return blockpos;
    }

    public float getSharedSpawnAngle()
    {
        return this.levelData.getSpawnAngle();
    }

    public LongSet getForcedChunks()
    {
        ForcedChunksSavedData forcedchunkssaveddata = this.getDataStorage().get(ForcedChunksSavedData::load, "chunks");
        return (LongSet)(forcedchunkssaveddata != null ? LongSets.unmodifiable(forcedchunkssaveddata.getChunks()) : LongSets.EMPTY_SET);
    }

    public boolean setChunkForced(int pChunkX, int pChunkZ, boolean pAdd)
    {
        ForcedChunksSavedData forcedchunkssaveddata = this.getDataStorage().computeIfAbsent(ForcedChunksSavedData::load, ForcedChunksSavedData::new, "chunks");
        ChunkPos chunkpos = new ChunkPos(pChunkX, pChunkZ);
        long i = chunkpos.toLong();
        boolean flag;

        if (pAdd)
        {
            flag = forcedchunkssaveddata.getChunks().add(i);

            if (flag)
            {
                this.getChunk(pChunkX, pChunkZ);
            }
        }
        else
        {
            flag = forcedchunkssaveddata.getChunks().remove(i);
        }

        forcedchunkssaveddata.setDirty(flag);

        if (flag)
        {
            this.getChunkSource().updateChunkForced(chunkpos, pAdd);
        }

        return flag;
    }

    public List<ServerPlayer> players()
    {
        return this.players;
    }

    public void onBlockStateChange(BlockPos pPos, BlockState pBlockState, BlockState pNewState)
    {
        Optional<PoiType> optional = PoiType.forState(pBlockState);
        Optional<PoiType> optional1 = PoiType.forState(pNewState);

        if (!Objects.equals(optional, optional1))
        {
            BlockPos blockpos = pPos.immutable();
            optional.ifPresent((p_143331_) ->
            {
                this.getServer().execute(() -> {
                    this.getPoiManager().remove(blockpos);
                    DebugPackets.sendPoiRemovedPacket(this, blockpos);
                });
            });
            optional1.ifPresent((p_143292_) ->
            {
                this.getServer().execute(() -> {
                    this.getPoiManager().add(blockpos, p_143292_);
                    DebugPackets.sendPoiAddedPacket(this, blockpos);
                });
            });
        }
    }

    public PoiManager getPoiManager()
    {
        return this.getChunkSource().getPoiManager();
    }

    public boolean isVillage(BlockPos pPos)
    {
        return this.isCloseToVillage(pPos, 1);
    }

    public boolean isVillage(SectionPos pPos)
    {
        return this.isVillage(pPos.center());
    }

    public boolean isCloseToVillage(BlockPos pPos, int pSections)
    {
        if (pSections > 6)
        {
            return false;
        }
        else
        {
            return this.sectionsToVillage(SectionPos.of(pPos)) <= pSections;
        }
    }

    public int sectionsToVillage(SectionPos pPos)
    {
        return this.getPoiManager().sectionsToVillage(pPos);
    }

    public Raids getRaids()
    {
        return this.raids;
    }

    @Nullable
    public Raid getRaidAt(BlockPos pPos)
    {
        return this.raids.getNearbyRaid(pPos, 9216);
    }

    public boolean isRaided(BlockPos pPos)
    {
        return this.getRaidAt(pPos) != null;
    }

    public void onReputationEvent(ReputationEventType pType, Entity pTarget, ReputationEventHandler pHost)
    {
        pHost.onReputationEventFrom(pType, pTarget);
    }

    public void saveDebugReport(Path pPath) throws IOException
    {
        ChunkMap chunkmap = this.getChunkSource().chunkMap;
        Writer writer = Files.newBufferedWriter(pPath.resolve("stats.txt"));

        try
        {
            writer.write(String.format("spawning_chunks: %d\n", chunkmap.getDistanceManager().getNaturalSpawnChunkCount()));
            NaturalSpawner.SpawnState naturalspawner$spawnstate = this.getChunkSource().getLastSpawnState();

            if (naturalspawner$spawnstate != null)
            {
                for (Entry<MobCategory> entry : naturalspawner$spawnstate.getMobCategoryCounts().object2IntEntrySet())
                {
                    writer.write(String.format("spawn_count.%s: %d\n", entry.getKey().getName(), entry.getIntValue()));
                }
            }

            writer.write(String.format("entities: %s\n", this.entityManager.gatherStats()));
            writer.write(String.format("block_entity_tickers: %d\n", this.blockEntityTickers.size()));
            writer.write(String.format("block_ticks: %d\n", this.getBlockTicks().size()));
            writer.write(String.format("fluid_ticks: %d\n", this.getLiquidTicks().size()));
            writer.write("distance_manager: " + chunkmap.getDistanceManager().getDebugStatus() + "\n");
            writer.write(String.format("pending_tasks: %d\n", this.getChunkSource().getPendingTasksCount()));
        }
        catch (Throwable throwable11)
        {
            if (writer != null)
            {
                try
                {
                    writer.close();
                }
                catch (Throwable throwable5)
                {
                    throwable11.addSuppressed(throwable5);
                }
            }

            throw throwable11;
        }

        if (writer != null)
        {
            writer.close();
        }

        CrashReport crashreport = new CrashReport("Level dump", new Exception("dummy"));
        this.fillReportDetails(crashreport);
        Writer writer3 = Files.newBufferedWriter(pPath.resolve("example_crash.txt"));

        try
        {
            writer3.write(crashreport.getFriendlyReport());
        }
        catch (Throwable throwable10)
        {
            if (writer3 != null)
            {
                try
                {
                    writer3.close();
                }
                catch (Throwable throwable4)
                {
                    throwable10.addSuppressed(throwable4);
                }
            }

            throw throwable10;
        }

        if (writer3 != null)
        {
            writer3.close();
        }

        Path path = pPath.resolve("chunks.csv");
        Writer writer4 = Files.newBufferedWriter(path);

        try
        {
            chunkmap.dumpChunks(writer4);
        }
        catch (Throwable throwable9)
        {
            if (writer4 != null)
            {
                try
                {
                    writer4.close();
                }
                catch (Throwable throwable3)
                {
                    throwable9.addSuppressed(throwable3);
                }
            }

            throw throwable9;
        }

        if (writer4 != null)
        {
            writer4.close();
        }

        Path path1 = pPath.resolve("entity_chunks.csv");
        Writer writer5 = Files.newBufferedWriter(path1);

        try
        {
            this.entityManager.dumpSections(writer5);
        }
        catch (Throwable throwable8)
        {
            if (writer5 != null)
            {
                try
                {
                    writer5.close();
                }
                catch (Throwable throwable2)
                {
                    throwable8.addSuppressed(throwable2);
                }
            }

            throw throwable8;
        }

        if (writer5 != null)
        {
            writer5.close();
        }

        Path path2 = pPath.resolve("entities.csv");
        Writer writer1 = Files.newBufferedWriter(path2);

        try
        {
            dumpEntities(writer1, this.getEntities().getAll());
        }
        catch (Throwable throwable7)
        {
            if (writer1 != null)
            {
                try
                {
                    writer1.close();
                }
                catch (Throwable throwable1)
                {
                    throwable7.addSuppressed(throwable1);
                }
            }

            throw throwable7;
        }

        if (writer1 != null)
        {
            writer1.close();
        }

        Path path3 = pPath.resolve("block_entities.csv");
        Writer writer2 = Files.newBufferedWriter(path3);

        try
        {
            this.dumpBlockEntityTickers(writer2);
        }
        catch (Throwable throwable6)
        {
            if (writer2 != null)
            {
                try
                {
                    writer2.close();
                }
                catch (Throwable throwable)
                {
                    throwable6.addSuppressed(throwable);
                }
            }

            throw throwable6;
        }

        if (writer2 != null)
        {
            writer2.close();
        }
    }

    private static void dumpEntities(Writer pWriter, Iterable<Entity> pEntities) throws IOException
    {
        CsvOutput csvoutput = CsvOutput.builder().addColumn("x").addColumn("y").addColumn("z").addColumn("uuid").addColumn("type").addColumn("alive").addColumn("display_name").addColumn("custom_name").build(pWriter);

        for (Entity entity : pEntities)
        {
            Component component = entity.getCustomName();
            Component component1 = entity.getDisplayName();
            csvoutput.m_13624_(entity.getX(), entity.getY(), entity.getZ(), entity.getUUID(), Registry.ENTITY_TYPE.getKey(entity.getType()), entity.isAlive(), component1.getString(), component != null ? component.getString() : null);
        }
    }

    private void dumpBlockEntityTickers(Writer p_143300_) throws IOException
    {
        CsvOutput csvoutput = CsvOutput.builder().addColumn("x").addColumn("y").addColumn("z").addColumn("type").build(p_143300_);

        for (TickingBlockEntity tickingblockentity : this.blockEntityTickers)
        {
            BlockPos blockpos = tickingblockentity.getPos();
            csvoutput.m_13624_(blockpos.getX(), blockpos.getY(), blockpos.getZ(), tickingblockentity.getType());
        }
    }

    @VisibleForTesting
    public void clearBlockEvents(BoundingBox pBoundingBox)
    {
        this.blockEvents.removeIf((p_143287_) ->
        {
            return pBoundingBox.isInside(p_143287_.getPos());
        });
    }

    public void blockUpdated(BlockPos pPos, Block pBlock)
    {
        if (!this.isDebug())
        {
            this.updateNeighborsAt(pPos, pBlock);
        }
    }

    public float getShade(Direction p_8760_, boolean p_8761_)
    {
        return 1.0F;
    }

    public Iterable<Entity> getAllEntities()
    {
        return this.getEntities().getAll();
    }

    public String toString()
    {
        return "ServerLevel[" + this.serverLevelData.getLevelName() + "]";
    }

    public boolean isFlat()
    {
        return this.server.getWorldData().worldGenSettings().isFlatWorld();
    }

    public long getSeed()
    {
        return this.server.getWorldData().worldGenSettings().seed();
    }

    @Nullable
    public EndDragonFight dragonFight()
    {
        return this.dragonFight;
    }

    public Stream <? extends StructureStart<? >> startsForFeature(SectionPos p_8765_, StructureFeature<?> p_8766_)
    {
        return this.structureFeatureManager().startsForFeature(p_8765_, p_8766_);
    }

    public ServerLevel getLevel()
    {
        return this;
    }

    @VisibleForTesting
    public String getWatchdogStats()
    {
        return String.format("players: %s, entities: %s [%s], block_entities: %d [%s], block_ticks: %d, fluid_ticks: %d, chunk_source: %s", this.players.size(), this.entityManager.gatherStats(), getTypeCount(this.entityManager.getEntityGetter().getAll(), (p_143346_) ->
        {
            return Registry.ENTITY_TYPE.getKey(p_143346_.getType()).toString();
        }), this.blockEntityTickers.size(), getTypeCount(this.blockEntityTickers, TickingBlockEntity::getType), this.getBlockTicks().size(), this.getLiquidTicks().size(), this.gatherChunkSourceStats());
    }

    private static <T> String getTypeCount(Iterable<T> p_143302_, Function<T, String> p_143303_)
    {
        try
        {
            Object2IntOpenHashMap<String> object2intopenhashmap = new Object2IntOpenHashMap<>();

            for (T t : p_143302_)
            {
                String s = p_143303_.apply(t);
                object2intopenhashmap.addTo(s, 1);
            }

            return object2intopenhashmap.object2IntEntrySet().stream().sorted(Comparator.comparing(Entry<String>::getIntValue).reversed()).limit(5L).map((p_143298_) ->
            {
                return (String)p_143298_.getKey() + ":" + p_143298_.getIntValue();
            }).collect(Collectors.joining(","));
        }
        catch (Exception exception)
        {
            return "";
        }
    }

    public static void makeObsidianPlatform(ServerLevel pServerLevel)
    {
        BlockPos blockpos = END_SPAWN_POINT;
        int i = blockpos.getX();
        int j = blockpos.getY() - 2;
        int k = blockpos.getZ();
        BlockPos.betweenClosed(i - 2, j + 1, k - 2, i + 2, j + 3, k + 2).forEach((p_143323_) ->
        {
            pServerLevel.setBlockAndUpdate(p_143323_, Blocks.AIR.defaultBlockState());
        });
        BlockPos.betweenClosed(i - 2, j, k - 2, i + 2, j, k + 2).forEach((p_143260_) ->
        {
            pServerLevel.setBlockAndUpdate(p_143260_, Blocks.OBSIDIAN.defaultBlockState());
        });
    }

    protected LevelEntityGetter<Entity> getEntities()
    {
        return this.entityManager.getEntityGetter();
    }

    public void addLegacyChunkEntities(Stream<Entity> p_143312_)
    {
        this.entityManager.addLegacyChunkEntities(p_143312_);
    }

    public void addWorldGenChunkEntities(Stream<Entity> p_143328_)
    {
        this.entityManager.addWorldGenChunkEntities(p_143328_);
    }

    public void close() throws IOException
    {
        super.close();
        this.entityManager.close();
    }

    public String gatherChunkSourceStats()
    {
        return "Chunks[S] W: " + this.chunkSource.gatherStats() + " E: " + this.entityManager.gatherStats();
    }

    public boolean areEntitiesLoaded(long p_143320_)
    {
        return this.entityManager.areEntitiesLoaded(p_143320_);
    }

    public boolean isPositionTickingWithEntitiesLoaded(BlockPos p_143337_)
    {
        long i = ChunkPos.asLong(p_143337_);
        return this.chunkSource.isPositionTicking(i) && this.areEntitiesLoaded(i);
    }

    public boolean isPositionEntityTicking(BlockPos p_143341_)
    {
        return this.entityManager.isPositionTicking(p_143341_);
    }

    public boolean isPositionEntityTicking(ChunkPos p_143276_)
    {
        return this.entityManager.isPositionTicking(p_143276_);
    }

    final class EntityCallbacks implements LevelCallback<Entity>
    {
        public void onCreated(Entity p_143355_)
        {
        }

        public void onDestroyed(Entity p_143359_)
        {
            ServerLevel.this.getScoreboard().entityRemoved(p_143359_);
        }

        public void onTickingStart(Entity p_143363_)
        {
            ServerLevel.this.entityTickList.add(p_143363_);
        }

        public void onTickingEnd(Entity p_143367_)
        {
            ServerLevel.this.entityTickList.remove(p_143367_);
        }

        public void onTrackingStart(Entity p_143371_)
        {
            ServerLevel.this.getChunkSource().addEntity(p_143371_);

            if (p_143371_ instanceof ServerPlayer)
            {
                ServerLevel.this.players.add((ServerPlayer)p_143371_);
                ServerLevel.this.updateSleepingPlayerList();
            }

            if (p_143371_ instanceof Mob)
            {
                ServerLevel.this.navigatingMobs.add((Mob)p_143371_);
            }

            if (p_143371_ instanceof EnderDragon)
            {
                for (EnderDragonPart enderdragonpart : ((EnderDragon)p_143371_).getSubEntities())
                {
                    ServerLevel.this.dragonParts.put(enderdragonpart.getId(), enderdragonpart);
                }
            }
        }

        public void onTrackingEnd(Entity p_143375_)
        {
            ServerLevel.this.getChunkSource().removeEntity(p_143375_);

            if (p_143375_ instanceof ServerPlayer)
            {
                ServerPlayer serverplayer = (ServerPlayer)p_143375_;
                ServerLevel.this.players.remove(serverplayer);
                ServerLevel.this.updateSleepingPlayerList();
            }

            if (p_143375_ instanceof Mob)
            {
                ServerLevel.this.navigatingMobs.remove(p_143375_);
            }

            if (p_143375_ instanceof EnderDragon)
            {
                for (EnderDragonPart enderdragonpart : ((EnderDragon)p_143375_).getSubEntities())
                {
                    ServerLevel.this.dragonParts.remove(enderdragonpart.getId());
                }
            }

            GameEventListenerRegistrar gameeventlistenerregistrar = p_143375_.getGameEventListenerRegistrar();

            if (gameeventlistenerregistrar != null)
            {
                gameeventlistenerregistrar.onListenerRemoved(p_143375_.level);
            }
        }
    }
}
