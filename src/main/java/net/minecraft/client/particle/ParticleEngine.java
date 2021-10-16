package net.minecraft.client.particle;

import com.google.common.base.Charsets;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforgeop.client.RenderProperties;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ParticleEngine implements PreparableReloadListener
{
    private static final int MAX_PARTICLES_PER_LAYER = 16384;
    private static final List<ParticleRenderType> RENDER_ORDER = ImmutableList.of(ParticleRenderType.TERRAIN_SHEET, ParticleRenderType.PARTICLE_SHEET_OPAQUE, ParticleRenderType.PARTICLE_SHEET_LIT, ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT, ParticleRenderType.CUSTOM);
    protected ClientLevel level;
    private final Map<ParticleRenderType, Queue<Particle>> particles = Maps.newIdentityHashMap();
    private final Queue<TrackingEmitter> trackingEmitters = Queues.newArrayDeque();
    private final TextureManager textureManager;
    private final Random random = new Random();
    private final Map < ResourceLocation, ParticleProvider<? >> providers = new HashMap<>();
    private final Queue<Particle> particlesToAdd = Queues.newArrayDeque();
    private final Map<ResourceLocation, ParticleEngine.MutableSpriteSet> spriteSets = Maps.newHashMap();
    private final TextureAtlas textureAtlas;
    private final Object2IntOpenHashMap<ParticleGroup> trackedParticleCounts = new Object2IntOpenHashMap<>();

    public ParticleEngine(ClientLevel p_107299_, TextureManager p_107300_)
    {
        this.textureAtlas = new TextureAtlas(TextureAtlas.LOCATION_PARTICLES);
        p_107300_.register(this.textureAtlas.location(), this.textureAtlas);
        this.level = p_107299_;
        this.textureManager = p_107300_;
        this.registerProviders();
    }

    private void registerProviders()
    {
        this.register(ParticleTypes.AMBIENT_ENTITY_EFFECT, SpellParticle.AmbientMobProvider::new);
        this.register(ParticleTypes.ANGRY_VILLAGER, HeartParticle.AngryVillagerProvider::new);
        this.register(ParticleTypes.BARRIER, new StationaryItemParticle.BarrierProvider());
        this.register(ParticleTypes.LIGHT, new StationaryItemParticle.LightProvider());
        this.register(ParticleTypes.BLOCK, new TerrainParticle.Provider());
        this.register(ParticleTypes.BUBBLE, BubbleParticle.Provider::new);
        this.register(ParticleTypes.BUBBLE_COLUMN_UP, BubbleColumnUpParticle.Provider::new);
        this.register(ParticleTypes.BUBBLE_POP, BubblePopParticle.Provider::new);
        this.register(ParticleTypes.CAMPFIRE_COSY_SMOKE, CampfireSmokeParticle.CosyProvider::new);
        this.register(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, CampfireSmokeParticle.SignalProvider::new);
        this.register(ParticleTypes.CLOUD, PlayerCloudParticle.Provider::new);
        this.register(ParticleTypes.COMPOSTER, SuspendedTownParticle.ComposterFillProvider::new);
        this.register(ParticleTypes.CRIT, CritParticle.Provider::new);
        this.register(ParticleTypes.CURRENT_DOWN, WaterCurrentDownParticle.Provider::new);
        this.register(ParticleTypes.DAMAGE_INDICATOR, CritParticle.DamageIndicatorProvider::new);
        this.register(ParticleTypes.DRAGON_BREATH, DragonBreathParticle.Provider::new);
        this.register(ParticleTypes.DOLPHIN, SuspendedTownParticle.DolphinSpeedProvider::new);
        this.register(ParticleTypes.DRIPPING_LAVA, DripParticle.LavaHangProvider::new);
        this.register(ParticleTypes.FALLING_LAVA, DripParticle.LavaFallProvider::new);
        this.register(ParticleTypes.LANDING_LAVA, DripParticle.LavaLandProvider::new);
        this.register(ParticleTypes.DRIPPING_WATER, DripParticle.WaterHangProvider::new);
        this.register(ParticleTypes.FALLING_WATER, DripParticle.WaterFallProvider::new);
        this.register(ParticleTypes.DUST, DustParticle.Provider::new);
        this.register(ParticleTypes.DUST_COLOR_TRANSITION, DustColorTransitionParticle.Provider::new);
        this.register(ParticleTypes.EFFECT, SpellParticle.Provider::new);
        this.register(ParticleTypes.ELDER_GUARDIAN, new MobAppearanceParticle.Provider());
        this.register(ParticleTypes.ENCHANTED_HIT, CritParticle.MagicProvider::new);
        this.register(ParticleTypes.ENCHANT, EnchantmentTableParticle.Provider::new);
        this.register(ParticleTypes.END_ROD, EndRodParticle.Provider::new);
        this.register(ParticleTypes.ENTITY_EFFECT, SpellParticle.MobProvider::new);
        this.register(ParticleTypes.EXPLOSION_EMITTER, new HugeExplosionSeedParticle.Provider());
        this.register(ParticleTypes.EXPLOSION, HugeExplosionParticle.Provider::new);
        this.register(ParticleTypes.FALLING_DUST, FallingDustParticle.Provider::new);
        this.register(ParticleTypes.FIREWORK, FireworkParticles.SparkProvider::new);
        this.register(ParticleTypes.FISHING, WakeParticle.Provider::new);
        this.register(ParticleTypes.FLAME, FlameParticle.Provider::new);
        this.register(ParticleTypes.SOUL, SoulParticle.Provider::new);
        this.register(ParticleTypes.SOUL_FIRE_FLAME, FlameParticle.Provider::new);
        this.register(ParticleTypes.FLASH, FireworkParticles.FlashProvider::new);
        this.register(ParticleTypes.HAPPY_VILLAGER, SuspendedTownParticle.HappyVillagerProvider::new);
        this.register(ParticleTypes.HEART, HeartParticle.Provider::new);
        this.register(ParticleTypes.INSTANT_EFFECT, SpellParticle.InstantProvider::new);
        this.register(ParticleTypes.ITEM, new BreakingItemParticle.Provider());
        this.register(ParticleTypes.ITEM_SLIME, new BreakingItemParticle.SlimeProvider());
        this.register(ParticleTypes.ITEM_SNOWBALL, new BreakingItemParticle.SnowballProvider());
        this.register(ParticleTypes.LARGE_SMOKE, LargeSmokeParticle.Provider::new);
        this.register(ParticleTypes.LAVA, LavaParticle.Provider::new);
        this.register(ParticleTypes.MYCELIUM, SuspendedTownParticle.Provider::new);
        this.register(ParticleTypes.NAUTILUS, EnchantmentTableParticle.NautilusProvider::new);
        this.register(ParticleTypes.NOTE, NoteParticle.Provider::new);
        this.register(ParticleTypes.POOF, ExplodeParticle.Provider::new);
        this.register(ParticleTypes.PORTAL, PortalParticle.Provider::new);
        this.register(ParticleTypes.RAIN, WaterDropParticle.Provider::new);
        this.register(ParticleTypes.SMOKE, SmokeParticle.Provider::new);
        this.register(ParticleTypes.SNEEZE, PlayerCloudParticle.SneezeProvider::new);
        this.register(ParticleTypes.SNOWFLAKE, SnowflakeParticle.Provider::new);
        this.register(ParticleTypes.SPIT, SpitParticle.Provider::new);
        this.register(ParticleTypes.SWEEP_ATTACK, AttackSweepParticle.Provider::new);
        this.register(ParticleTypes.TOTEM_OF_UNDYING, TotemParticle.Provider::new);
        this.register(ParticleTypes.SQUID_INK, SquidInkParticle.Provider::new);
        this.register(ParticleTypes.UNDERWATER, SuspendedParticle.UnderwaterProvider::new);
        this.register(ParticleTypes.SPLASH, SplashParticle.Provider::new);
        this.register(ParticleTypes.WITCH, SpellParticle.WitchProvider::new);
        this.register(ParticleTypes.DRIPPING_HONEY, DripParticle.HoneyHangProvider::new);
        this.register(ParticleTypes.FALLING_HONEY, DripParticle.HoneyFallProvider::new);
        this.register(ParticleTypes.LANDING_HONEY, DripParticle.HoneyLandProvider::new);
        this.register(ParticleTypes.FALLING_NECTAR, DripParticle.NectarFallProvider::new);
        this.register(ParticleTypes.FALLING_SPORE_BLOSSOM, DripParticle.SporeBlossomFallProvider::new);
        this.register(ParticleTypes.SPORE_BLOSSOM_AIR, SuspendedParticle.SporeBlossomAirProvider::new);
        this.register(ParticleTypes.ASH, AshParticle.Provider::new);
        this.register(ParticleTypes.CRIMSON_SPORE, SuspendedParticle.CrimsonSporeProvider::new);
        this.register(ParticleTypes.WARPED_SPORE, SuspendedParticle.WarpedSporeProvider::new);
        this.register(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, DripParticle.ObsidianTearHangProvider::new);
        this.register(ParticleTypes.FALLING_OBSIDIAN_TEAR, DripParticle.ObsidianTearFallProvider::new);
        this.register(ParticleTypes.LANDING_OBSIDIAN_TEAR, DripParticle.ObsidianTearLandProvider::new);
        this.register(ParticleTypes.REVERSE_PORTAL, ReversePortalParticle.ReversePortalProvider::new);
        this.register(ParticleTypes.WHITE_ASH, WhiteAshParticle.Provider::new);
        this.register(ParticleTypes.SMALL_FLAME, FlameParticle.SmallFlameProvider::new);
        this.register(ParticleTypes.DRIPPING_DRIPSTONE_WATER, DripParticle.DripstoneWaterHangProvider::new);
        this.register(ParticleTypes.FALLING_DRIPSTONE_WATER, DripParticle.DripstoneWaterFallProvider::new);
        this.register(ParticleTypes.DRIPPING_DRIPSTONE_LAVA, DripParticle.DripstoneLavaHangProvider::new);
        this.register(ParticleTypes.FALLING_DRIPSTONE_LAVA, DripParticle.DripstoneLavaFallProvider::new);
        this.register(ParticleTypes.VIBRATION, VibrationSignalParticle.Provider::new);
        this.register(ParticleTypes.GLOW_SQUID_INK, SquidInkParticle.GlowInkProvider::new);
        this.register(ParticleTypes.GLOW, GlowParticle.GlowSquidProvider::new);
        this.register(ParticleTypes.WAX_ON, GlowParticle.WaxOnProvider::new);
        this.register(ParticleTypes.WAX_OFF, GlowParticle.WaxOffProvider::new);
        this.register(ParticleTypes.ELECTRIC_SPARK, GlowParticle.ElectricSparkProvider::new);
        this.register(ParticleTypes.SCRAPE, GlowParticle.ScrapeProvider::new);
    }

    private <T extends ParticleOptions> void register(ParticleType<T> pParticleType, ParticleProvider<T> pParticleMetaFactory)
    {
        this.providers.put(Registry.PARTICLE_TYPE.getKey(pParticleType), pParticleMetaFactory);
    }

    private <T extends ParticleOptions> void register(ParticleType<T> pParticleType, ParticleEngine.SpriteParticleRegistration<T> pParticleMetaFactory)
    {
        ParticleEngine.MutableSpriteSet particleengine$mutablespriteset = new ParticleEngine.MutableSpriteSet();
        this.spriteSets.put(Registry.PARTICLE_TYPE.getKey(pParticleType), particleengine$mutablespriteset);
        this.providers.put(Registry.PARTICLE_TYPE.getKey(pParticleType), pParticleMetaFactory.create(particleengine$mutablespriteset));
    }

    public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier pStage, ResourceManager pResourceManager, ProfilerFiller pPreparationsProfiler, ProfilerFiller pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor)
    {
        Map<ResourceLocation, List<ResourceLocation>> map = Maps.newConcurrentMap();
        CompletableFuture<?>[] completablefuture = Registry.PARTICLE_TYPE.keySet().stream().map((locationIn) ->
        {
            return CompletableFuture.runAsync(() -> {
                this.loadParticleDescription(pResourceManager, locationIn, map);
            }, pBackgroundExecutor);
        }).toArray((intIn) ->
        {
            return new CompletableFuture[intIn];
        });
        return CompletableFuture.allOf(completablefuture).thenApplyAsync((voidIn) ->
        {
            pPreparationsProfiler.startTick();
            pPreparationsProfiler.push("stitching");
            TextureAtlas.Preparations textureatlas$preparations = this.textureAtlas.prepareToStitch(pResourceManager, map.values().stream().flatMap(Collection::stream), pPreparationsProfiler, 0);
            pPreparationsProfiler.pop();
            pPreparationsProfiler.endTick();
            return textureatlas$preparations;
        }, pBackgroundExecutor).thenCompose(pStage::wait).thenAcceptAsync((preparationsIn) ->
        {
            this.particles.clear();
            pReloadProfiler.startTick();
            pReloadProfiler.push("upload");
            this.textureAtlas.reload(preparationsIn);
            pReloadProfiler.popPush("bindSpriteSets");
            TextureAtlasSprite textureatlassprite = this.textureAtlas.getSprite(MissingTextureAtlasSprite.getLocation());
            map.forEach((p_315767_2_, p_315767_3_) -> {
                ImmutableList<TextureAtlasSprite> immutablelist = p_315767_3_.isEmpty() ? ImmutableList.of(textureatlassprite) : p_315767_3_.stream().map(this.textureAtlas::getSprite).collect(ImmutableList.toImmutableList());
                this.spriteSets.get(p_315767_2_).rebind(immutablelist);
            });
            pReloadProfiler.pop();
            pReloadProfiler.endTick();
        }, pGameExecutor);
    }

    public void close()
    {
        this.textureAtlas.clearTextureData();
    }

    private void loadParticleDescription(ResourceManager pManager, ResourceLocation pParticleId, Map<ResourceLocation, List<ResourceLocation>> pTextures)
    {
        ResourceLocation resourcelocation = new ResourceLocation(pParticleId.getNamespace(), "particles/" + pParticleId.getPath() + ".json");

        try
        {
            Resource resource = pManager.getResource(resourcelocation);

            try
            {
                Reader reader = new InputStreamReader(resource.getInputStream(), Charsets.UTF_8);

                try
                {
                    ParticleDescription particledescription = ParticleDescription.fromJson(GsonHelper.parse(reader));
                    List<ResourceLocation> list = particledescription.getTextures();
                    boolean flag = this.spriteSets.containsKey(pParticleId);

                    if (list == null)
                    {
                        if (flag)
                        {
                            throw new IllegalStateException("Missing texture list for particle " + pParticleId);
                        }
                    }
                    else
                    {
                        if (!flag)
                        {
                            throw new IllegalStateException("Redundant texture list for particle " + pParticleId);
                        }

                        pTextures.put(pParticleId, list.stream().map((locationIn) ->
                        {
                            return new ResourceLocation(locationIn.getNamespace(), "particle/" + locationIn.getPath());
                        }).collect(Collectors.toList()));
                    }
                }
                catch (Throwable throwable2)
                {
                    try
                    {
                        reader.close();
                    }
                    catch (Throwable throwable11)
                    {
                        throwable2.addSuppressed(throwable11);
                    }

                    throw throwable2;
                }

                reader.close();
            }
            catch (Throwable throwable31)
            {
                if (resource != null)
                {
                    try
                    {
                        resource.close();
                    }
                    catch (Throwable throwable1)
                    {
                        throwable31.addSuppressed(throwable1);
                    }
                }

                throw throwable31;
            }

            if (resource != null)
            {
                resource.close();
            }
        }
        catch (IOException ioexception1)
        {
            throw new IllegalStateException("Failed to load description for particle " + pParticleId, ioexception1);
        }
    }

    public void createTrackingEmitter(Entity pEntity, ParticleOptions pParticleData)
    {
        this.trackingEmitters.add(new TrackingEmitter(this.level, pEntity, pParticleData));
    }

    public void createTrackingEmitter(Entity pEntity, ParticleOptions pParticleData, int p_107335_)
    {
        this.trackingEmitters.add(new TrackingEmitter(this.level, pEntity, pParticleData, p_107335_));
    }

    @Nullable
    public Particle createParticle(ParticleOptions pParticleData, double pX, double p_107373_, double pY, double p_107375_, double pZ, double p_107377_)
    {
        Particle particle = this.makeParticle(pParticleData, pX, p_107373_, pY, p_107375_, pZ, p_107377_);

        if (particle != null)
        {
            this.add(particle);
            return particle;
        }
        else
        {
            return null;
        }
    }

    @Nullable
    private <T extends ParticleOptions> Particle makeParticle(T pParticleData, double pX, double p_107398_, double pY, double p_107400_, double pZ, double p_107402_)
    {
        ParticleProvider<T> particleprovider = (ParticleProvider<T>) this.providers.get(Registry.PARTICLE_TYPE.getKey(pParticleData.getType()));
        return particleprovider == null ? null : particleprovider.createParticle(pParticleData, this.level, pX, p_107398_, pY, p_107400_, pZ, p_107402_);
    }

    public void add(Particle pEffect)
    {
        if (pEffect != null)
        {
            if (!(pEffect instanceof FireworkParticles.SparkParticle) || Config.isFireworkParticles())
            {
                Optional<ParticleGroup> optional = pEffect.getParticleGroup();

                if (optional.isPresent())
                {
                    if (this.hasSpaceInParticleLimit(optional.get()))
                    {
                        this.particlesToAdd.add(pEffect);
                        this.updateCount(optional.get(), 1);
                    }
                }
                else
                {
                    this.particlesToAdd.add(pEffect);
                }
            }
        }
    }

    public void tick()
    {
        this.particles.forEach((renderTypeIn, queueIn) ->
        {
            this.level.getProfiler().push(renderTypeIn.toString());
            this.tickParticleList(queueIn);
            this.level.getProfiler().pop();
        });

        if (!this.trackingEmitters.isEmpty())
        {
            List<TrackingEmitter> list = Lists.newArrayList();

            for (TrackingEmitter trackingemitter : this.trackingEmitters)
            {
                trackingemitter.tick();

                if (!trackingemitter.isAlive())
                {
                    list.add(trackingemitter);
                }
            }

            this.trackingEmitters.removeAll(list);
        }

        Particle particle;

        if (!this.particlesToAdd.isEmpty())
        {
            while ((particle = this.particlesToAdd.poll()) != null)
            {
                Queue<Particle> queue = this.particles.computeIfAbsent(particle.getRenderType(), (renderTypeIn) ->
                {
                    return EvictingQueue.create(16384);
                });
                queue.add(particle);
            }
        }
    }

    private void tickParticleList(Collection<Particle> pParticles)
    {
        if (!pParticles.isEmpty())
        {
            long i = System.currentTimeMillis();
            int j = pParticles.size();
            Iterator<Particle> iterator = pParticles.iterator();

            while (iterator.hasNext())
            {
                Particle particle = iterator.next();
                this.tickParticle(particle);

                if (!particle.isAlive())
                {
                    particle.getParticleGroup().ifPresent((groupIn) ->
                    {
                        this.updateCount(groupIn, -1);
                    });
                    iterator.remove();
                }

                --j;

                if (System.currentTimeMillis() > i + 20L)
                {
                    break;
                }
            }

            if (j > 0)
            {
                int k = j;

                for (Iterator iterator1 = pParticles.iterator(); iterator1.hasNext() && k > 0; --k)
                {
                    Particle particle1 = (Particle)iterator1.next();
                    particle1.remove();
                    iterator1.remove();
                }
            }
        }
    }

    private void updateCount(ParticleGroup p_172282_, int p_172283_)
    {
        this.trackedParticleCounts.addTo(p_172282_, p_172283_);
    }

    private void tickParticle(Particle pParticle)
    {
        try
        {
            pParticle.tick();
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Ticking Particle");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being ticked");
            crashreportcategory.setDetail("Particle", pParticle::toString);
            crashreportcategory.setDetail("Particle Type", pParticle.getRenderType()::toString);
            throw new ReportedException(crashreport);
        }
    }

    public void render(PoseStack pMatrixStack, MultiBufferSource.BufferSource pBuffer, LightTexture pLightTexture, Camera pActiveRenderInfo, float pPartialTicks)
    {
        this.render(pMatrixStack, pBuffer, pLightTexture, pActiveRenderInfo, pPartialTicks, (Frustum)null);
    }

    public void render(PoseStack matrixStackIn, MultiBufferSource.BufferSource bufferIn, LightTexture lightTextureIn, Camera activeRenderInfoIn, float partialTicks, Frustum clippingHelper)
    {
        lightTextureIn.turnOnLightLayer();
        RenderSystem.enableDepthTest();
        FogType fogtype = activeRenderInfoIn.getFluidInCamera();
        boolean flag = fogtype == FogType.WATER;
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.mulPoseMatrix(matrixStackIn.last().pose());
        RenderSystem.applyModelViewMatrix();
        Collection<ParticleRenderType> collection = RENDER_ORDER;

        if (Reflector.ForgeHooksClient.exists())
        {
            collection = this.particles.keySet();
        }

        for (ParticleRenderType particlerendertype : collection)
        {
            if (particlerendertype != ParticleRenderType.NO_RENDER)
            {
                Iterable<Particle> iterable = this.particles.get(particlerendertype);

                if (iterable != null)
                {
                    RenderSystem.setShader(GameRenderer::getParticleShader);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    Tesselator tesselator = Tesselator.getInstance();
                    BufferBuilder bufferbuilder = tesselator.getBuilder();
                    particlerendertype.begin(bufferbuilder, this.textureManager);

                    for (Particle particle : iterable)
                    {
                        if (clippingHelper == null || !particle.shouldCull() || clippingHelper.isVisible(particle.getBoundingBox()))
                        {
                            try
                            {
                                if (flag || !(particle instanceof SuspendedParticle) || particle.xd != 0.0D || particle.yd != 0.0D || particle.zd != 0.0D)
                                {
                                    particle.render(bufferbuilder, activeRenderInfoIn, partialTicks);
                                }
                            }
                            catch (Throwable throwable)
                            {
                                CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering Particle");
                                CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being rendered");
                                crashreportcategory.setDetail("Particle", particle::toString);
                                crashreportcategory.setDetail("Particle Type", particlerendertype::toString);
                                throw new ReportedException(crashreport);
                            }
                        }
                    }

                    particlerendertype.end(tesselator);
                }
            }
        }

        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        lightTextureIn.turnOffLightLayer();
        RenderSystem.enableDepthTest();
    }

    public void setLevel(@Nullable ClientLevel pLevel)
    {
        this.level = pLevel;
        this.particles.clear();
        this.trackingEmitters.clear();
        this.trackedParticleCounts.clear();
    }

    public void destroy(BlockPos pPos, BlockState pState)
    {
        boolean flag = RenderProperties.get(pState).addDestroyEffects(pState, this.level, pPos, this);

        if (!pState.isAir() && !flag)
        {
            VoxelShape voxelshape = pState.getShape(this.level, pPos);
            double d0 = 0.25D;
            voxelshape.forAllBoxes((p_204189_3_, p_204189_5_, p_204189_7_, p_204189_9_, p_204189_11_, p_204189_13_) ->
            {
                double d1 = Math.min(1.0D, p_204189_9_ - p_204189_3_);
                double d2 = Math.min(1.0D, p_204189_11_ - p_204189_5_);
                double d3 = Math.min(1.0D, p_204189_13_ - p_204189_7_);
                int i = Math.max(2, Mth.ceil(d1 / 0.25D));
                int j = Math.max(2, Mth.ceil(d2 / 0.25D));
                int k = Math.max(2, Mth.ceil(d3 / 0.25D));

                for (int l = 0; l < i; ++l)
                {
                    for (int i1 = 0; i1 < j; ++i1)
                    {
                        for (int j1 = 0; j1 < k; ++j1)
                        {
                            double d4 = ((double)l + 0.5D) / (double)i;
                            double d5 = ((double)i1 + 0.5D) / (double)j;
                            double d6 = ((double)j1 + 0.5D) / (double)k;
                            double d7 = d4 * d1 + p_204189_3_;
                            double d8 = d5 * d2 + p_204189_5_;
                            double d9 = d6 * d3 + p_204189_7_;
                            this.add(new TerrainParticle(this.level, (double)pPos.getX() + d7, (double)pPos.getY() + d8, (double)pPos.getZ() + d9, d4 - 0.5D, d5 - 0.5D, d6 - 0.5D, pState, pPos));
                        }
                    }
                }
            });
        }
    }

    public void crack(BlockPos pPos, Direction pSide)
    {
        BlockState blockstate = this.level.getBlockState(pPos);

        if (blockstate.getRenderShape() != RenderShape.INVISIBLE)
        {
            int i = pPos.getX();
            int j = pPos.getY();
            int k = pPos.getZ();
            float f = 0.1F;
            AABB aabb = blockstate.getShape(this.level, pPos).bounds();
            double d0 = (double)i + this.random.nextDouble() * (aabb.maxX - aabb.minX - (double)0.2F) + (double)0.1F + aabb.minX;
            double d1 = (double)j + this.random.nextDouble() * (aabb.maxY - aabb.minY - (double)0.2F) + (double)0.1F + aabb.minY;
            double d2 = (double)k + this.random.nextDouble() * (aabb.maxZ - aabb.minZ - (double)0.2F) + (double)0.1F + aabb.minZ;

            if (pSide == Direction.DOWN)
            {
                d1 = (double)j + aabb.minY - (double)0.1F;
            }

            if (pSide == Direction.UP)
            {
                d1 = (double)j + aabb.maxY + (double)0.1F;
            }

            if (pSide == Direction.NORTH)
            {
                d2 = (double)k + aabb.minZ - (double)0.1F;
            }

            if (pSide == Direction.SOUTH)
            {
                d2 = (double)k + aabb.maxZ + (double)0.1F;
            }

            if (pSide == Direction.WEST)
            {
                d0 = (double)i + aabb.minX - (double)0.1F;
            }

            if (pSide == Direction.EAST)
            {
                d0 = (double)i + aabb.maxX + (double)0.1F;
            }

            this.add((new TerrainParticle(this.level, d0, d1, d2, 0.0D, 0.0D, 0.0D, blockstate, pPos)).setPower(0.2F).scale(0.6F));
        }
    }

    public String countParticles()
    {
        return String.valueOf(this.particles.values().stream().mapToInt(Collection::size).sum());
    }

    private boolean hasSpaceInParticleLimit(ParticleGroup p_172280_)
    {
        return this.trackedParticleCounts.getInt(p_172280_) < p_172280_.getLimit();
    }

    private boolean reuseBarrierParticle(Particle var1, Queue<Particle> deque)
    {
    	Particle var4;
        for(Iterator it = deque.iterator(); it.hasNext(); var4 = (Particle)it.next()) {
           ;
        }

        return false;
    }

    public void addBlockHitEffects(BlockPos pos, BlockHitResult target)
    {
        BlockState blockstate = this.level.getBlockState(pos);

        if (!RenderProperties.get(blockstate).addHitEffects(blockstate, this.level, target, this))
        {
            this.crack(pos, target.getDirection());
        }
    }

    class MutableSpriteSet implements SpriteSet
    {
        private List<TextureAtlasSprite> sprites;

        public TextureAtlasSprite get(int pParticleAge, int pParticleMaxAge)
        {
            return this.sprites.get(pParticleAge * (this.sprites.size() - 1) / pParticleMaxAge);
        }

        public TextureAtlasSprite get(Random pParticleAge)
        {
            return this.sprites.get(pParticleAge.nextInt(this.sprites.size()));
        }

        public void rebind(List<TextureAtlasSprite> pSprites)
        {
            this.sprites = ImmutableList.copyOf(pSprites);
        }
    }

    @FunctionalInterface
    interface SpriteParticleRegistration<T extends ParticleOptions>
    {
        ParticleProvider<T> create(SpriteSet p_107420_);
    }
}
