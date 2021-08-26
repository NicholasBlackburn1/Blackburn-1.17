package net.minecraft.client.multiplayer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintCache;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagContainer;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.EmptyTickList;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.Scoreboard;
import net.optifine.Config;
import net.optifine.CustomGuis;
import net.optifine.DynamicLights;
import net.optifine.RandomEntities;
import net.optifine.override.PlayerControllerOF;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorForge;
import net.optifine.shaders.Shaders;

public class ClientLevel extends Level
{
    private static final double FLUID_PARTICLE_SPAWN_OFFSET = 0.05D;
    final EntityTickList tickingEntities = new EntityTickList();
    private final TransientEntitySectionManager<Entity> entityStorage = new TransientEntitySectionManager<>(Entity.class, new ClientLevel.EntityCallbacks());
    private final ClientPacketListener connection;
    private final LevelRenderer levelRenderer;
    private final ClientLevel.ClientLevelData clientLevelData;
    private final DimensionSpecialEffects effects;
    private final Minecraft minecraft = Minecraft.getInstance();
    final List<AbstractClientPlayer> players = Lists.newArrayList();
    private Scoreboard scoreboard = new Scoreboard();
    private final Map<String, MapItemSavedData> mapData = Maps.newHashMap();
    private static final long CLOUD_COLOR = 16777215L;
    private int skyFlashTime;
    private final Object2ObjectArrayMap<ColorResolver, BlockTintCache> tintCaches = Util.make(new Object2ObjectArrayMap<>(3), (mapIn) ->
    {
        mapIn.put(BiomeColors.GRASS_COLOR_RESOLVER, new BlockTintCache());
        mapIn.put(BiomeColors.FOLIAGE_COLOR_RESOLVER, new BlockTintCache());
        mapIn.put(BiomeColors.WATER_COLOR_RESOLVER, new BlockTintCache());
    });
    private final ClientChunkCache chunkSource;
    private boolean playerUpdate = false;

    public ClientLevel(ClientPacketListener p_104568_, ClientLevel.ClientLevelData p_104569_, ResourceKey<Level> p_104570_, DimensionType p_104571_, int p_104572_, Supplier<ProfilerFiller> p_104573_, LevelRenderer p_104574_, boolean p_104575_, long p_104576_)
    {
        super(p_104569_, p_104570_, p_104571_, p_104573_, true, p_104575_, p_104576_);
        this.connection = p_104568_;
        this.chunkSource = new ClientChunkCache(this, p_104572_);
        this.clientLevelData = p_104569_;
        this.levelRenderer = p_104574_;
        this.effects = DimensionSpecialEffects.forType(p_104571_);
        this.setDefaultSpawnPos(new BlockPos(8, 64, 8), 0.0F);
        this.updateSkyBrightness();
        this.prepareWeather();

        if (Reflector.CapabilityProvider_gatherCapabilities.exists())
        {
            Reflector.call(this, Reflector.CapabilityProvider_gatherCapabilities);
        }

        Reflector.postForgeBusEvent(Reflector.WorldEvent_Load_Constructor, this);

        if (this.minecraft.gameMode != null && this.minecraft.gameMode.getClass() == MultiPlayerGameMode.class)
        {
            this.minecraft.gameMode = new PlayerControllerOF(this.minecraft, this.connection);
            CustomGuis.setPlayerControllerOF((PlayerControllerOF)this.minecraft.gameMode);
        }
    }

    public DimensionSpecialEffects effects()
    {
        return this.effects;
    }

    public void tick(BooleanSupplier pHasTimeLeft)
    {
        this.getWorldBorder().tick();
        this.tickTime();
        this.getProfiler().push("blocks");
        this.chunkSource.tick(pHasTimeLeft);
        this.getProfiler().pop();
    }

    private void tickTime()
    {
        this.setGameTime(this.levelData.getGameTime() + 1L);

        if (this.levelData.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT))
        {
            this.setDayTime(this.levelData.getDayTime() + 1L);
        }
    }

    public void setGameTime(long pTime)
    {
        this.clientLevelData.setGameTime(pTime);
    }

    public void setDayTime(long pTime)
    {
        if (pTime < 0L)
        {
            pTime = -pTime;
            this.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, (MinecraftServer)null);
        }
        else
        {
            this.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(true, (MinecraftServer)null);
        }

        this.clientLevelData.setDayTime(pTime);
    }

    public Iterable<Entity> entitiesForRendering()
    {
        return this.getEntities().getAll();
    }

    public void tickEntities()
    {
        ProfilerFiller profilerfiller = this.getProfiler();
        profilerfiller.push("entities");
        this.tickingEntities.forEach((entityIn) ->
        {
            if (!entityIn.isRemoved() && !entityIn.isPassenger())
            {
                this.guardEntityTick(this::tickNonPassenger, entityIn);
            }
        });
        profilerfiller.pop();
        this.tickBlockEntities();
    }

    public void tickNonPassenger(Entity pEntity)
    {
        pEntity.setOldPosAndRot();
        ++pEntity.tickCount;
        this.getProfiler().push(() ->
        {
            return Registry.ENTITY_TYPE.getKey(pEntity.getType()).toString();
        });

        if (ReflectorForge.canUpdate(pEntity))
        {
            pEntity.tick();
        }

        if (pEntity.isRemoved())
        {
            this.onEntityRemoved(pEntity);
        }

        this.getProfiler().pop();

        for (Entity entity : pEntity.getPassengers())
        {
            this.tickPassenger(pEntity, entity);
        }
    }

    private void tickPassenger(Entity pMount, Entity pRider)
    {
        if (!pRider.isRemoved() && pRider.getVehicle() == pMount)
        {
            if (pRider instanceof Player || this.tickingEntities.contains(pRider))
            {
                pRider.setOldPosAndRot();
                ++pRider.tickCount;
                pRider.rideTick();

                for (Entity entity : pRider.getPassengers())
                {
                    this.tickPassenger(pRider, entity);
                }
            }
        }
        else
        {
            pRider.stopRiding();
        }
    }

    public void unload(LevelChunk pChunk)
    {
        pChunk.invalidateAllBlockEntities();
        this.chunkSource.getLightEngine().enableLightSources(pChunk.getPos(), false);
        this.entityStorage.stopTicking(pChunk.getPos());
    }

    public void onChunkLoaded(ChunkPos p_171650_)
    {
        this.tintCaches.forEach((resolverIn, tintCacheIn) ->
        {
            tintCacheIn.invalidateForChunk(p_171650_.x, p_171650_.z);
        });
        this.entityStorage.startTicking(p_171650_);
    }

    public void clearTintCaches()
    {
        this.tintCaches.forEach((resolverIn, tintCacheIn) ->
        {
            tintCacheIn.invalidateAll();
        });
    }

    public boolean hasChunk(int pChunkX, int pChunkZ)
    {
        return true;
    }

    public int getEntityCount()
    {
        return this.entityStorage.count();
    }

    public void addPlayer(int pPlayerId, AbstractClientPlayer pPlayerEntity)
    {
        this.addEntity(pPlayerId, pPlayerEntity);
    }

    public void putNonPlayerEntity(int pEntityId, Entity pEntityToSpawn)
    {
        this.addEntity(pEntityId, pEntityToSpawn);
    }

    private void addEntity(int pEntityId, Entity pEntityToSpawn)
    {
        if (!Reflector.EntityJoinWorldEvent_Constructor.exists() || !Reflector.postForgeBusEvent(Reflector.EntityJoinWorldEvent_Constructor, pEntityToSpawn, this))
        {
            this.removeEntity(pEntityId, Entity.RemovalReason.DISCARDED);
            this.entityStorage.addEntity(pEntityToSpawn);

            if (Reflector.IForgeEntity_onAddedToWorld.exists())
            {
                Reflector.call(pEntityToSpawn, Reflector.IForgeEntity_onAddedToWorld);
            }

            this.onEntityAdded(pEntityToSpawn);
        }
    }

    public void removeEntity(int p_171643_, Entity.RemovalReason p_171644_)
    {
        Entity entity = this.getEntities().get(p_171643_);

        if (entity != null)
        {
            entity.setRemoved(p_171644_);
            entity.onClientRemoval();
        }
    }

    @Nullable
    public Entity getEntity(int pId)
    {
        return this.getEntities().get(pId);
    }

    public void setKnownState(BlockPos pPos, BlockState pState)
    {
        this.setBlock(pPos, pState, 19);
    }

    public void disconnect()
    {
        this.connection.getConnection().disconnect(new TranslatableComponent("multiplayer.status.quitting"));
    }

    public void animateTick(int pPosX, int pPosY, int pPosZ)
    {
        int i = 32;
        Random random = new Random();
        ClientLevel.MarkerParticleStatus clientlevel$markerparticlestatus = this.getMarkerParticleStatus();
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int j = 0; j < 667; ++j)
        {
            this.doAnimateTick(pPosX, pPosY, pPosZ, 16, random, clientlevel$markerparticlestatus, blockpos$mutableblockpos);
            this.doAnimateTick(pPosX, pPosY, pPosZ, 32, random, clientlevel$markerparticlestatus, blockpos$mutableblockpos);
        }
    }

    @Nullable
    private ClientLevel.MarkerParticleStatus getMarkerParticleStatus()
    {
        if (this.minecraft.gameMode.getPlayerMode() == GameType.CREATIVE)
        {
            ItemStack itemstack = this.minecraft.player.getMainHandItem();

            if (itemstack.getItem() == Items.BARRIER)
            {
                return ClientLevel.MarkerParticleStatus.BARRIER;
            }

            if (itemstack.getItem() == Items.LIGHT)
            {
                return ClientLevel.MarkerParticleStatus.LIGHT;
            }
        }

        return null;
    }

    public void doAnimateTick(int p_171635_, int p_171636_, int p_171637_, int p_171638_, Random p_171639_, @Nullable ClientLevel.MarkerParticleStatus p_171640_, BlockPos.MutableBlockPos p_171641_)
    {
        int i = p_171635_ + this.random.nextInt(p_171638_) - this.random.nextInt(p_171638_);
        int j = p_171636_ + this.random.nextInt(p_171638_) - this.random.nextInt(p_171638_);
        int k = p_171637_ + this.random.nextInt(p_171638_) - this.random.nextInt(p_171638_);
        p_171641_.set(i, j, k);
        BlockState blockstate = this.getBlockState(p_171641_);
        blockstate.getBlock().animateTick(blockstate, this, p_171641_, p_171639_);
        FluidState fluidstate = this.getFluidState(p_171641_);

        if (!fluidstate.isEmpty())
        {
            fluidstate.animateTick(this, p_171641_, p_171639_);
            ParticleOptions particleoptions = fluidstate.getDripParticle();

            if (particleoptions != null && this.random.nextInt(10) == 0)
            {
                boolean flag = blockstate.isFaceSturdy(this, p_171641_, Direction.DOWN);
                BlockPos blockpos = p_171641_.below();
                this.trySpawnDripParticles(blockpos, this.getBlockState(blockpos), particleoptions, flag);
            }
        }

        if (p_171640_ != null && blockstate.getBlock() == p_171640_.block)
        {
            this.addParticle(p_171640_.particle, (double)i + 0.5D, (double)j + 0.5D, (double)k + 0.5D, 0.0D, 0.0D, 0.0D);
        }

        if (!blockstate.isCollisionShapeFullBlock(this, p_171641_))
        {
            this.getBiome(p_171641_).getAmbientParticle().ifPresent((settingsIn) ->
            {
                if (settingsIn.canSpawn(this.random))
                {
                    this.addParticle(settingsIn.getOptions(), (double)p_171641_.getX() + this.random.nextDouble(), (double)p_171641_.getY() + this.random.nextDouble(), (double)p_171641_.getZ() + this.random.nextDouble(), 0.0D, 0.0D, 0.0D);
                }
            });
        }
    }

    private void trySpawnDripParticles(BlockPos pBlockPos, BlockState pBlockState, ParticleOptions pParticleData, boolean pShapeDownSolid)
    {
        if (pBlockState.getFluidState().isEmpty())
        {
            VoxelShape voxelshape = pBlockState.getCollisionShape(this, pBlockPos);
            double d0 = voxelshape.max(Direction.Axis.Y);

            if (d0 < 1.0D)
            {
                if (pShapeDownSolid)
                {
                    this.spawnFluidParticle((double)pBlockPos.getX(), (double)(pBlockPos.getX() + 1), (double)pBlockPos.getZ(), (double)(pBlockPos.getZ() + 1), (double)(pBlockPos.getY() + 1) - 0.05D, pParticleData);
                }
            }
            else if (!pBlockState.is(BlockTags.IMPERMEABLE))
            {
                double d1 = voxelshape.min(Direction.Axis.Y);

                if (d1 > 0.0D)
                {
                    this.spawnParticle(pBlockPos, pParticleData, voxelshape, (double)pBlockPos.getY() + d1 - 0.05D);
                }
                else
                {
                    BlockPos blockpos = pBlockPos.below();
                    BlockState blockstate = this.getBlockState(blockpos);
                    VoxelShape voxelshape1 = blockstate.getCollisionShape(this, blockpos);
                    double d2 = voxelshape1.max(Direction.Axis.Y);

                    if (d2 < 1.0D && blockstate.getFluidState().isEmpty())
                    {
                        this.spawnParticle(pBlockPos, pParticleData, voxelshape, (double)pBlockPos.getY() - 0.05D);
                    }
                }
            }
        }
    }

    private void spawnParticle(BlockPos pPos, ParticleOptions pParticleData, VoxelShape pVoxelShape, double pY)
    {
        this.spawnFluidParticle((double)pPos.getX() + pVoxelShape.min(Direction.Axis.X), (double)pPos.getX() + pVoxelShape.max(Direction.Axis.X), (double)pPos.getZ() + pVoxelShape.min(Direction.Axis.Z), (double)pPos.getZ() + pVoxelShape.max(Direction.Axis.Z), pY, pParticleData);
    }

    private void spawnFluidParticle(double pXStart, double p_104594_, double pXEnd, double p_104596_, double pZStart, ParticleOptions p_104598_)
    {
        this.addParticle(p_104598_, Mth.lerp(this.random.nextDouble(), pXStart, p_104594_), pZStart, Mth.lerp(this.random.nextDouble(), pXEnd, p_104596_), 0.0D, 0.0D, 0.0D);
    }

    public CrashReportCategory fillReportDetails(CrashReport pReport)
    {
        CrashReportCategory crashreportcategory = super.fillReportDetails(pReport);
        crashreportcategory.setDetail("Server brand", () ->
        {
            return this.minecraft.player.getServerBrand();
        });
        crashreportcategory.setDetail("Server type", () ->
        {
            return this.minecraft.getSingleplayerServer() == null ? "Non-integrated multiplayer server" : "Integrated singleplayer server";
        });
        return crashreportcategory;
    }

    public void playSound(@Nullable Player pPlayer, double pX, double p_104647_, double pY, SoundEvent p_104649_, SoundSource pZ, float p_104651_, float pSound)
    {
        if (Reflector.ForgeEventFactory_onPlaySoundAtEntity.exists())
        {
            Object object = Reflector.ForgeEventFactory_onPlaySoundAtEntity.call(pPlayer, p_104649_, pZ, p_104651_, pSound);

            if (Reflector.callBoolean(object, Reflector.Event_isCanceled) || Reflector.call(object, Reflector.PlaySoundAtEntityEvent_getSound) == null)
            {
                return;
            }

            p_104649_ = (SoundEvent)Reflector.call(object, Reflector.PlaySoundAtEntityEvent_getSound);
            pZ = (SoundSource)Reflector.call(object, Reflector.PlaySoundAtEntityEvent_getCategory);
            p_104651_ = Reflector.callFloat(object, Reflector.PlaySoundAtEntityEvent_getVolume);
        }

        if (pPlayer == this.minecraft.player)
        {
            this.playLocalSound(pX, p_104647_, pY, p_104649_, pZ, p_104651_, pSound, false);
        }
    }

    public void playSound(@Nullable Player pPlayer, Entity pX, SoundEvent p_104661_, SoundSource pY, float p_104663_, float pZ)
    {
        if (Reflector.ForgeEventFactory_onPlaySoundAtEntity.exists())
        {
            Object object = Reflector.ForgeEventFactory_onPlaySoundAtEntity.call(pPlayer, p_104661_, pY, p_104663_, pZ);

            if (Reflector.callBoolean(object, Reflector.Event_isCanceled) || Reflector.call(object, Reflector.PlaySoundAtEntityEvent_getSound) == null)
            {
                return;
            }

            p_104661_ = (SoundEvent)Reflector.call(object, Reflector.PlaySoundAtEntityEvent_getSound);
            pY = (SoundSource)Reflector.call(object, Reflector.PlaySoundAtEntityEvent_getCategory);
            p_104663_ = Reflector.callFloat(object, Reflector.PlaySoundAtEntityEvent_getVolume);
        }

        if (pPlayer == this.minecraft.player)
        {
            this.minecraft.getSoundManager().play(new EntityBoundSoundInstance(p_104661_, pY, p_104663_, pZ, pX));
        }
    }

    public void playLocalSound(BlockPos pX, SoundEvent p_104679_, SoundSource pY, float p_104681_, float pZ, boolean p_104683_)
    {
        this.playLocalSound((double)pX.getX() + 0.5D, (double)pX.getY() + 0.5D, (double)pX.getZ() + 0.5D, p_104679_, pY, p_104681_, pZ, p_104683_);
    }

    public void playLocalSound(double pX, double p_104601_, double pY, SoundEvent p_104603_, SoundSource pZ, float p_104605_, float pSound, boolean pCategory)
    {
        double d0 = this.minecraft.gameRenderer.getMainCamera().getPosition().distanceToSqr(pX, p_104601_, pY);
        SimpleSoundInstance simplesoundinstance = new SimpleSoundInstance(p_104603_, pZ, p_104605_, pSound, pX, p_104601_, pY);

        if (pCategory && d0 > 100.0D)
        {
            double d1 = Math.sqrt(d0) / 40.0D;
            this.minecraft.getSoundManager().playDelayed(simplesoundinstance, (int)(d1 * 20.0D));
        }
        else
        {
            this.minecraft.getSoundManager().play(simplesoundinstance);
        }
    }

    public void createFireworks(double pX, double p_104586_, double pY, double p_104588_, double pZ, double p_104590_, @Nullable CompoundTag pMotionX)
    {
        this.minecraft.particleEngine.add(new FireworkParticles.Starter(this, pX, p_104586_, pY, p_104588_, pZ, p_104590_, this.minecraft.particleEngine, pMotionX));
    }

    public void sendPacketToServer(Packet<?> pPacket)
    {
        this.connection.send(pPacket);
    }

    public RecipeManager getRecipeManager()
    {
        return this.connection.getRecipeManager();
    }

    public void setScoreboard(Scoreboard pScoreboard)
    {
        this.scoreboard = pScoreboard;
    }

    public TickList<Block> getBlockTicks()
    {
        return EmptyTickList.empty();
    }

    public TickList<Fluid> getLiquidTicks()
    {
        return EmptyTickList.empty();
    }

    public ClientChunkCache getChunkSource()
    {
        return this.chunkSource;
    }

    public boolean setBlock(BlockPos pPos, BlockState pNewState, int pFlags)
    {
        this.playerUpdate = this.isPlayerActing();
        boolean flag = super.setBlock(pPos, pNewState, pFlags);
        this.playerUpdate = false;
        return flag;
    }

    private boolean isPlayerActing()
    {
        if (this.minecraft.gameMode instanceof PlayerControllerOF)
        {
            PlayerControllerOF playercontrollerof = (PlayerControllerOF)this.minecraft.gameMode;
            return playercontrollerof.isActing();
        }
        else
        {
            return false;
        }
    }

    public boolean isPlayerUpdate()
    {
        return this.playerUpdate;
    }

    public void onEntityAdded(Entity entityIn)
    {
        RandomEntities.entityLoaded(entityIn, this);

        if (Config.isDynamicLights())
        {
            DynamicLights.entityAdded(entityIn, Config.getRenderGlobal());
        }
    }

    public void onEntityRemoved(Entity entityIn)
    {
        RandomEntities.entityUnloaded(entityIn, this);

        if (Config.isDynamicLights())
        {
            DynamicLights.entityRemoved(entityIn, Config.getRenderGlobal());
        }
    }

    @Nullable
    public MapItemSavedData getMapData(String pMapName)
    {
        return this.mapData.get(pMapName);
    }

    public void setMapData(String p_171670_, MapItemSavedData p_171671_)
    {
        this.mapData.put(p_171670_, p_171671_);
    }

    public int getFreeMapId()
    {
        return 0;
    }

    public Scoreboard getScoreboard()
    {
        return this.scoreboard;
    }

    public TagContainer getTagManager()
    {
        return this.connection.getTags();
    }

    public RegistryAccess registryAccess()
    {
        return this.connection.registryAccess();
    }

    public void sendBlockUpdated(BlockPos pPos, BlockState pOldState, BlockState pNewState, int pFlags)
    {
        this.levelRenderer.blockChanged(this, pPos, pOldState, pNewState, pFlags);
    }

    public void setBlocksDirty(BlockPos pBlockPos, BlockState pOldState, BlockState pNewState)
    {
        this.levelRenderer.setBlockDirty(pBlockPos, pOldState, pNewState);
    }

    public void setSectionDirtyWithNeighbors(int pSectionX, int pSectionY, int pSectionZ)
    {
        this.levelRenderer.setSectionDirtyWithNeighbors(pSectionX, pSectionY, pSectionZ);
    }

    public void destroyBlockProgress(int pBreakerId, BlockPos pPos, int pProgress)
    {
        this.levelRenderer.destroyBlockProgress(pBreakerId, pPos, pProgress);
    }

    public void globalLevelEvent(int pId, BlockPos pPos, int pData)
    {
        this.levelRenderer.globalLevelEvent(pId, pPos, pData);
    }

    public void levelEvent(@Nullable Player pPlayer, int pType, BlockPos pPos, int pData)
    {
        try
        {
            this.levelRenderer.levelEvent(pPlayer, pType, pPos, pData);
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Playing level event");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Level event being played");
            crashreportcategory.setDetail("Block coordinates", CrashReportCategory.formatLocation(this, pPos));
            crashreportcategory.setDetail("Event source", pPlayer);
            crashreportcategory.setDetail("Event type", pType);
            crashreportcategory.setDetail("Event data", pData);
            throw new ReportedException(crashreport);
        }
    }

    public void addParticle(ParticleOptions pParticleData, double pX, double p_104708_, double pY, double p_104710_, double pZ, double p_104712_)
    {
        this.levelRenderer.addParticle(pParticleData, pParticleData.getType().getOverrideLimiter(), pX, p_104708_, pY, p_104710_, pZ, p_104712_);
    }

    public void addParticle(ParticleOptions pParticleData, boolean pX, double p_104716_, double pY, double p_104718_, double pZ, double p_104720_, double pXSpeed)
    {
        this.levelRenderer.addParticle(pParticleData, pParticleData.getType().getOverrideLimiter() || pX, p_104716_, pY, p_104718_, pZ, p_104720_, pXSpeed);
    }

    public void addAlwaysVisibleParticle(ParticleOptions pParticleData, double pX, double p_104768_, double pY, double p_104770_, double pZ, double p_104772_)
    {
        this.levelRenderer.addParticle(pParticleData, false, true, pX, p_104768_, pY, p_104770_, pZ, p_104772_);
    }

    public void addAlwaysVisibleParticle(ParticleOptions pParticleData, boolean pX, double p_104776_, double pY, double p_104778_, double pZ, double p_104780_, double pXSpeed)
    {
        this.levelRenderer.addParticle(pParticleData, pParticleData.getType().getOverrideLimiter() || pX, true, p_104776_, pY, p_104778_, pZ, p_104780_, pXSpeed);
    }

    public List<AbstractClientPlayer> players()
    {
        return this.players;
    }

    public Biome getUncachedNoiseBiome(int pX, int pY, int pZ)
    {
        return this.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getOrThrow(Biomes.PLAINS);
    }

    public float getSkyDarken(float pPartialTicks)
    {
        float f = this.getTimeOfDay(pPartialTicks);
        float f1 = 1.0F - (Mth.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.2F);
        f1 = Mth.clamp(f1, 0.0F, 1.0F);
        f1 = 1.0F - f1;
        f1 = (float)((double)f1 * (1.0D - (double)(this.getRainLevel(pPartialTicks) * 5.0F) / 16.0D));
        f1 = (float)((double)f1 * (1.0D - (double)(this.getThunderLevel(pPartialTicks) * 5.0F) / 16.0D));
        return f1 * 0.8F + 0.2F;
    }

    public Vec3 getSkyColor(Vec3 p_171661_, float p_171662_)
    {
        float f = this.getTimeOfDay(p_171662_);
        Vec3 vec3 = p_171661_.subtract(2.0D, 2.0D, 2.0D).scale(0.25D);
        BiomeManager biomemanager = this.getBiomeManager();
        Vec3 vec31 = CubicSampler.gaussianSampleVec3(vec3, (xIn, yIn, zIn) ->
        {
            return Vec3.fromRGB24(biomemanager.getNoiseBiomeAtQuart(xIn, yIn, zIn).getSkyColor());
        });
        float f1 = Mth.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
        f1 = Mth.clamp(f1, 0.0F, 1.0F);
        float f2 = (float)vec31.x * f1;
        float f3 = (float)vec31.y * f1;
        float f4 = (float)vec31.z * f1;
        float f5 = this.getRainLevel(p_171662_);

        if (f5 > 0.0F)
        {
            float f6 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.6F;
            float f7 = 1.0F - f5 * 0.75F;
            f2 = f2 * f7 + f6 * (1.0F - f7);
            f3 = f3 * f7 + f6 * (1.0F - f7);
            f4 = f4 * f7 + f6 * (1.0F - f7);
        }

        float f9 = this.getThunderLevel(p_171662_);

        if (f9 > 0.0F)
        {
            float f10 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.2F;
            float f8 = 1.0F - f9 * 0.75F;
            f2 = f2 * f8 + f10 * (1.0F - f8);
            f3 = f3 * f8 + f10 * (1.0F - f8);
            f4 = f4 * f8 + f10 * (1.0F - f8);
        }

        if (this.skyFlashTime > 0)
        {
            float f11 = (float)this.skyFlashTime - p_171662_;

            if (f11 > 1.0F)
            {
                f11 = 1.0F;
            }

            f11 = f11 * 0.45F;
            f2 = f2 * (1.0F - f11) + 0.8F * f11;
            f3 = f3 * (1.0F - f11) + 0.8F * f11;
            f4 = f4 * (1.0F - f11) + 1.0F * f11;
        }

        return new Vec3((double)f2, (double)f3, (double)f4);
    }

    public Vec3 getCloudColor(float pPartialTicks)
    {
        float f = this.getTimeOfDay(pPartialTicks);
        float f1 = Mth.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.5F;
        f1 = Mth.clamp(f1, 0.0F, 1.0F);
        float f2 = 1.0F;
        float f3 = 1.0F;
        float f4 = 1.0F;
        float f5 = this.getRainLevel(pPartialTicks);

        if (f5 > 0.0F)
        {
            float f6 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.6F;
            float f7 = 1.0F - f5 * 0.95F;
            f2 = f2 * f7 + f6 * (1.0F - f7);
            f3 = f3 * f7 + f6 * (1.0F - f7);
            f4 = f4 * f7 + f6 * (1.0F - f7);
        }

        f2 = f2 * (f1 * 0.9F + 0.1F);
        f3 = f3 * (f1 * 0.9F + 0.1F);
        f4 = f4 * (f1 * 0.85F + 0.15F);
        float f9 = this.getThunderLevel(pPartialTicks);

        if (f9 > 0.0F)
        {
            float f10 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.2F;
            float f8 = 1.0F - f9 * 0.95F;
            f2 = f2 * f8 + f10 * (1.0F - f8);
            f3 = f3 * f8 + f10 * (1.0F - f8);
            f4 = f4 * f8 + f10 * (1.0F - f8);
        }

        return new Vec3((double)f2, (double)f3, (double)f4);
    }

    public float getStarBrightness(float pPartialTicks)
    {
        float f = this.getTimeOfDay(pPartialTicks);
        float f1 = 1.0F - (Mth.cos(f * ((float)Math.PI * 2F)) * 2.0F + 0.25F);
        f1 = Mth.clamp(f1, 0.0F, 1.0F);
        return f1 * f1 * 0.5F;
    }

    public int getSkyFlashTime()
    {
        return this.skyFlashTime;
    }

    public void setSkyFlashTime(int pTimeFlash)
    {
        this.skyFlashTime = pTimeFlash;
    }

    public float getShade(Direction p_104703_, boolean p_104704_)
    {
        boolean flag = this.effects().constantAmbientLight();
        boolean flag1 = Config.isShaders();

        if (!p_104704_)
        {
            return flag ? 0.9F : 1.0F;
        }
        else
        {
            switch (p_104703_)
            {
                case DOWN:
                    return flag ? 0.9F : (flag1 ? Shaders.blockLightLevel05 : 0.5F);

                case UP:
                    return flag ? 0.9F : 1.0F;

                case NORTH:
                case SOUTH:
                    if (Config.isShaders())
                    {
                        return Shaders.blockLightLevel08;
                    }

                    return 0.8F;

                case WEST:
                case EAST:
                    if (Config.isShaders())
                    {
                        return Shaders.blockLightLevel06;
                    }

                    return 0.6F;

                default:
                    return 1.0F;
            }
        }
    }

    public int getBlockTint(BlockPos pBlockPos, ColorResolver pColorResolver)
    {
        BlockTintCache blocktintcache = this.tintCaches.get(pColorResolver);
        return blocktintcache.getColor(pBlockPos, () ->
        {
            return this.calculateBlockTint(pBlockPos, pColorResolver);
        });
    }

    public int calculateBlockTint(BlockPos pBlockPos, ColorResolver pColorResolver)
    {
        int i = Minecraft.getInstance().options.biomeBlendRadius;

        if (i == 0)
        {
            return pColorResolver.getColor(this.getBiome(pBlockPos), (double)pBlockPos.getX(), (double)pBlockPos.getZ());
        }
        else
        {
            int j = (i * 2 + 1) * (i * 2 + 1);
            int k = 0;
            int l = 0;
            int i1 = 0;
            Cursor3D cursor3d = new Cursor3D(pBlockPos.getX() - i, pBlockPos.getY(), pBlockPos.getZ() - i, pBlockPos.getX() + i, pBlockPos.getY(), pBlockPos.getZ() + i);
            int j1;

            for (BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(); cursor3d.advance(); i1 += j1 & 255)
            {
                blockpos$mutableblockpos.set(cursor3d.nextX(), cursor3d.nextY(), cursor3d.nextZ());
                j1 = pColorResolver.getColor(this.getBiome(blockpos$mutableblockpos), (double)blockpos$mutableblockpos.getX(), (double)blockpos$mutableblockpos.getZ());
                k += (j1 & 16711680) >> 16;
                l += (j1 & 65280) >> 8;
            }

            return (k / j & 255) << 16 | (l / j & 255) << 8 | i1 / j & 255;
        }
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

    public void setDefaultSpawnPos(BlockPos pSpawnPos, float p_104754_)
    {
        this.levelData.setSpawn(pSpawnPos, p_104754_);
    }

    public String toString()
    {
        return "ClientLevel";
    }

    public ClientLevel.ClientLevelData getLevelData()
    {
        return this.clientLevelData;
    }

    public void gameEvent(@Nullable Entity p_171646_, GameEvent p_171647_, BlockPos p_171648_)
    {
    }

    protected Map<String, MapItemSavedData> getAllMapData()
    {
        return ImmutableMap.copyOf(this.mapData);
    }

    protected void addMapData(Map<String, MapItemSavedData> p_171673_)
    {
        this.mapData.putAll(p_171673_);
    }

    protected LevelEntityGetter<Entity> getEntities()
    {
        return this.entityStorage.getEntityGetter();
    }

    public String gatherChunkSourceStats()
    {
        return "Chunks[C] W: " + this.chunkSource.gatherStats() + " E: " + this.entityStorage.gatherStats();
    }

    public void addDestroyBlockEffect(BlockPos p_171667_, BlockState p_171668_)
    {
        this.minecraft.particleEngine.destroy(p_171667_, p_171668_);
    }

    public TransientEntitySectionManager getEntityStorage()
    {
        return this.entityStorage;
    }

    public static class ClientLevelData implements WritableLevelData
    {
        private final boolean hardcore;
        private final GameRules gameRules;
        private final boolean isFlat;
        private int xSpawn;
        private int ySpawn;
        private int zSpawn;
        private float spawnAngle;
        private long gameTime;
        private long dayTime;
        private boolean raining;
        private Difficulty difficulty;
        private boolean difficultyLocked;

        public ClientLevelData(Difficulty p_104843_, boolean p_104844_, boolean p_104845_)
        {
            this.difficulty = p_104843_;
            this.hardcore = p_104844_;
            this.isFlat = p_104845_;
            this.gameRules = new GameRules();
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

        public boolean isThundering()
        {
            return false;
        }

        public boolean isRaining()
        {
            return this.raining;
        }

        public void setRaining(boolean pIsRaining)
        {
            this.raining = pIsRaining;
        }

        public boolean isHardcore()
        {
            return this.hardcore;
        }

        public GameRules getGameRules()
        {
            return this.gameRules;
        }

        public Difficulty getDifficulty()
        {
            return this.difficulty;
        }

        public boolean isDifficultyLocked()
        {
            return this.difficultyLocked;
        }

        public void fillCrashReportCategory(CrashReportCategory p_171690_, LevelHeightAccessor p_171691_)
        {
            WritableLevelData.super.fillCrashReportCategory(p_171690_, p_171691_);
        }

        public void setDifficulty(Difficulty pDifficulty)
        {
            Reflector.ForgeHooks_onDifficultyChange.callVoid(pDifficulty, this.difficulty);
            this.difficulty = pDifficulty;
        }

        public void setDifficultyLocked(boolean pDifficultyLocked)
        {
            this.difficultyLocked = pDifficultyLocked;
        }

        public double getHorizonHeight(LevelHeightAccessor p_171688_)
        {
            return this.isFlat ? (double)p_171688_.getMinBuildHeight() : 63.0D;
        }

        public double getClearColorScale()
        {
            return this.isFlat ? 1.0D : 0.03125D;
        }
    }

    final class EntityCallbacks implements LevelCallback<Entity>
    {
        public void onCreated(Entity p_171696_)
        {
        }

        public void onDestroyed(Entity p_171700_)
        {
        }

        public void onTickingStart(Entity p_171704_)
        {
            ClientLevel.this.tickingEntities.add(p_171704_);
        }

        public void onTickingEnd(Entity p_171708_)
        {
            ClientLevel.this.tickingEntities.remove(p_171708_);
        }

        public void onTrackingStart(Entity p_171712_)
        {
            if (p_171712_ instanceof AbstractClientPlayer)
            {
                ClientLevel.this.players.add((AbstractClientPlayer)p_171712_);
            }
        }

        public void onTrackingEnd(Entity p_171716_)
        {
            p_171716_.unRide();
            ClientLevel.this.players.remove(p_171716_);

            if (Reflector.IForgeEntity_onRemovedFromWorld.exists())
            {
                Reflector.call(p_171716_, Reflector.IForgeEntity_onRemovedFromWorld);
            }

            if (Reflector.EntityLeaveWorldEvent_Constructor.exists())
            {
                Reflector.postForgeBusEvent(Reflector.EntityLeaveWorldEvent_Constructor, p_171716_, ClientLevel.this);
            }

            ClientLevel.this.onEntityRemoved(p_171716_);
        }
    }

    static enum MarkerParticleStatus
    {
        BARRIER(Blocks.BARRIER, ParticleTypes.BARRIER),
        LIGHT(Blocks.LIGHT, ParticleTypes.LIGHT);

        final Block block;
        final ParticleOptions particle;

        private MarkerParticleStatus(Block p_171728_, ParticleOptions p_171729_)
        {
            this.block = p_171728_;
            this.particle = p_171729_;
        }
    }
}
