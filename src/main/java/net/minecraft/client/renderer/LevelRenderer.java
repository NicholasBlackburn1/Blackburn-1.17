package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforgeop.client.ICloudRenderHandler;
import net.minecraftforgeop.client.ISkyRenderHandler;
import net.minecraftforgeop.client.IWeatherParticleRenderHandler;
import net.minecraftforgeop.client.IWeatherRenderHandler;
import net.minecraftforgeop.resource.IResourceType;
import net.minecraftforgeop.resource.VanillaResourceType;
import net.optifine.Config;
import net.optifine.CustomColors;
import net.optifine.CustomSky;
import net.optifine.DynamicLights;
import net.optifine.Lagometer;
import net.optifine.SmartAnimations;
import net.optifine.entity.model.CustomEntityModels;
import net.optifine.reflect.Reflector;
import net.optifine.render.ChunkVisibility;
import net.optifine.render.RenderEnv;
import net.optifine.render.RenderStateManager;
import net.optifine.render.VboRegion;
import net.optifine.shaders.RenderStage;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersRender;
import net.optifine.shaders.ShadowUtils;
import net.optifine.util.BiomeUtils;
import net.optifine.util.MathUtils;
import net.optifine.util.PairInt;
import net.optifine.util.RenderChunkUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

public class LevelRenderer implements ResourceManagerReloadListener, AutoCloseable
{
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int CHUNK_SIZE = 16;
    public static final int MAX_CHUNKS_WIDTH = 66;
    public static final int MAX_CHUNKS_AREA = 4356;
    private static final float SKY_DISC_RADIUS = 512.0F;
    private static final int MIN_FOG_DISTANCE = 32;
    private static final int RAIN_RADIUS = 10;
    private static final int RAIN_DIAMETER = 21;
    private static final int TRANSPARENT_SORT_COUNT = 15;
    private static final ResourceLocation MOON_LOCATION = new ResourceLocation("textures/environment/moon_phases.png");
    private static final ResourceLocation SUN_LOCATION = new ResourceLocation("textures/environment/sun.png");
    private static final ResourceLocation CLOUDS_LOCATION = new ResourceLocation("textures/environment/clouds.png");
    private static final ResourceLocation END_SKY_LOCATION = new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation FORCEFIELD_LOCATION = new ResourceLocation("textures/misc/forcefield.png");
    private static final ResourceLocation RAIN_LOCATION = new ResourceLocation("textures/environment/rain.png");
    private static final ResourceLocation SNOW_LOCATION = new ResourceLocation("textures/environment/snow.png");
    public static final Direction[] DIRECTIONS = Direction.values();
    private final Minecraft minecraft;
    private final TextureManager textureManager;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    private final RenderBuffers renderBuffers;
    private ClientLevel level;
    private Set<ChunkRenderDispatcher.RenderChunk> chunksToCompile = new ObjectLinkedOpenHashSet<>();
    private ObjectArrayList<LevelRenderer.RenderChunkInfo> renderChunks = new ObjectArrayList<>();
    private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
    private ViewArea viewArea;
    private LevelRenderer.RenderInfoMap renderInfoMap;
    @Nullable
    private VertexBuffer starBuffer;
    @Nullable
    private VertexBuffer skyBuffer;
    @Nullable
    private VertexBuffer darkBuffer;
    private boolean generateClouds = true;
    @Nullable
    private VertexBuffer cloudBuffer;
    private final RunningTrimmedMean frameTimes = new RunningTrimmedMean(100);
    private int ticks;
    private final Int2ObjectMap<BlockDestructionProgress> destroyingBlocks = new Int2ObjectOpenHashMap<>();
    private final Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress = new Long2ObjectOpenHashMap<>();
    private final Map<BlockPos, SoundInstance> playingRecords = Maps.newHashMap();
    @Nullable
    private RenderTarget entityTarget;
    @Nullable
    private PostChain entityEffect;
    @Nullable
    private RenderTarget translucentTarget;
    @Nullable
    private RenderTarget itemEntityTarget;
    @Nullable
    private RenderTarget particlesTarget;
    @Nullable
    private RenderTarget weatherTarget;
    @Nullable
    private RenderTarget cloudsTarget;
    @Nullable
    private PostChain transparencyChain;
    private double lastCameraX = Double.MIN_VALUE;
    private double lastCameraY = Double.MIN_VALUE;
    private double lastCameraZ = Double.MIN_VALUE;
    private int lastCameraChunkX = Integer.MIN_VALUE;
    private int lastCameraChunkY = Integer.MIN_VALUE;
    private int lastCameraChunkZ = Integer.MIN_VALUE;
    private double prevCamX = Double.MIN_VALUE;
    private double prevCamY = Double.MIN_VALUE;
    private double prevCamZ = Double.MIN_VALUE;
    private double prevCamRotX = Double.MIN_VALUE;
    private double prevCamRotY = Double.MIN_VALUE;
    private int prevCloudX = Integer.MIN_VALUE;
    private int prevCloudY = Integer.MIN_VALUE;
    private int prevCloudZ = Integer.MIN_VALUE;
    private Vec3 prevCloudColor = Vec3.ZERO;
    private CloudStatus prevCloudsType;
    private ChunkRenderDispatcher chunkRenderDispatcher;
    private int lastViewDistance = -1;
    private int renderedEntities;
    private int culledEntities;
    private Frustum cullingFrustum;
    private boolean captureFrustum;
    @Nullable
    private Frustum capturedFrustum;
    private final Vector4f[] frustumPoints = new Vector4f[8];
    private final Vector3d frustumPos = new Vector3d(0.0D, 0.0D, 0.0D);
    private double xTransparentOld;
    private double yTransparentOld;
    private double zTransparentOld;
    private boolean needsUpdate = true;
    private int frameId;
    private int rainSoundTime;
    private final float[] rainSizeX = new float[1024];
    private final float[] rainSizeZ = new float[1024];
    public Entity renderedEntity;
    public Set chunksToResortTransparency = new LinkedHashSet();
    public Set chunksToUpdateForced = new LinkedHashSet();
    private Set<ChunkRenderDispatcher.RenderChunk> chunksToUpdatePrev = new ObjectLinkedOpenHashSet<>();
    private Deque visibilityDeque = new ArrayDeque();
    private LongOpenHashSet renderInfosEntities = new LongOpenHashSet(1024);
    private List<LevelRenderer.RenderChunkInfo> renderInfosTileEntities = new ArrayList<>(1024);
    private ObjectArrayList renderInfosNormal = new ObjectArrayList(1024);
    private LongOpenHashSet renderInfosEntitiesNormal = new LongOpenHashSet(1024);
    private List renderInfosTileEntitiesNormal = new ArrayList(1024);
    private ObjectArrayList renderInfosShadow = new ObjectArrayList(1024);
    private LongOpenHashSet renderInfosEntitiesShadow = new LongOpenHashSet(1024);
    private List renderInfosTileEntitiesShadow = new ArrayList(1024);
    private int renderDistance = 0;
    private int renderDistanceSq = 0;
    private static final Set SET_ALL_FACINGS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(Direction.VALUES)));
    private int countTileEntitiesRendered;
    private int countLoadedChunksPrev = 0;
    private RenderEnv renderEnv = new RenderEnv(Blocks.AIR.defaultBlockState(), new BlockPos(0, 0, 0));
    public boolean renderOverlayDamaged = false;
    public boolean renderOverlayEyes = false;
    private boolean firstWorldLoad = false;
    private static int renderEntitiesCounter = 0;
    public int loadVisibleChunksCounter = -1;
    public static final int loadVisibleChunksMessageId = 201435902;
    private static boolean ambientOcclusion = false;
    private Map<String, List<Entity>> mapEntityLists = new HashMap<>();
    private Map<RenderType, Map> mapRegionLayers = new LinkedHashMap<>();

    public LevelRenderer(Minecraft p_109480_, RenderBuffers p_109481_)
    {
        this.minecraft = p_109480_;
        this.entityRenderDispatcher = p_109480_.getEntityRenderDispatcher();
        this.blockEntityRenderDispatcher = p_109480_.getBlockEntityRenderDispatcher();
        this.renderBuffers = p_109481_;
        this.textureManager = p_109480_.getTextureManager();

        for (int i = 0; i < 32; ++i)
        {
            for (int j = 0; j < 32; ++j)
            {
                float f = (float)(j - 16);
                float f1 = (float)(i - 16);
                float f2 = Mth.sqrt(f * f + f1 * f1);
                this.rainSizeX[i << 5 | j] = -f1 / f2;
                this.rainSizeZ[i << 5 | j] = f / f2;
            }
        }

        this.createStars();
        this.createLightSky();
        this.createDarkSky();
    }

    private void renderSnowAndRain(LightTexture pLightmap, float pPartialTicks, double pX, double p_109707_, double pY)
    {
        if (Reflector.ForgeDimensionRenderInfo_getWeatherRenderHandler.exists())
        {
            IWeatherRenderHandler iweatherrenderhandler = (IWeatherRenderHandler)Reflector.call(this.level.effects(), Reflector.ForgeDimensionRenderInfo_getWeatherRenderHandler);

            if (iweatherrenderhandler != null)
            {
                iweatherrenderhandler.render(this.ticks, pPartialTicks, this.level, this.minecraft, pLightmap, pX, p_109707_, pY);
                return;
            }
        }

        float f5 = this.minecraft.level.getRainLevel(pPartialTicks);

        if (!(f5 <= 0.0F))
        {
            if (Config.isRainOff())
            {
                return;
            }

            pLightmap.turnOnLightLayer();
            Level level = this.minecraft.level;
            int i = Mth.floor(pX);
            int j = Mth.floor(p_109707_);
            int k = Mth.floor(pY);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            int l = 5;

            if (Config.isRainFancy())
            {
                l = 10;
            }

            RenderSystem.depthMask(Minecraft.useShaderTransparency());
            int i1 = -1;
            float f = (float)this.ticks + pPartialTicks;
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (int j1 = k - l; j1 <= k + l; ++j1)
            {
                for (int k1 = i - l; k1 <= i + l; ++k1)
                {
                    int l1 = (j1 - k + 16) * 32 + k1 - i + 16;
                    double d0 = (double)this.rainSizeX[l1] * 0.5D;
                    double d1 = (double)this.rainSizeZ[l1] * 0.5D;
                    blockpos$mutableblockpos.set(k1, 0, j1);
                    Biome biome = level.getBiome(blockpos$mutableblockpos);

                    if (biome.getPrecipitation() != Biome.Precipitation.NONE)
                    {
                        int i2 = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockpos$mutableblockpos).getY();
                        int j2 = j - l;
                        int k2 = j + l;

                        if (j2 < i2)
                        {
                            j2 = i2;
                        }

                        if (k2 < i2)
                        {
                            k2 = i2;
                        }

                        int l2 = i2;

                        if (i2 < j)
                        {
                            l2 = j;
                        }

                        if (j2 != k2)
                        {
                            Random random = new Random((long)(k1 * k1 * 3121 + k1 * 45238971 ^ j1 * j1 * 418711 + j1 * 13761));
                            blockpos$mutableblockpos.set(k1, j2, j1);
                            float f1 = biome.getTemperature(blockpos$mutableblockpos);

                            if (f1 >= 0.15F)
                            {
                                if (i1 != 0)
                                {
                                    if (i1 >= 0)
                                    {
                                        tesselator.end();
                                    }

                                    i1 = 0;
                                    RenderSystem.setShaderTexture(0, RAIN_LOCATION);
                                    bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                                }

                                int i3 = this.ticks + k1 * k1 * 3121 + k1 * 45238971 + j1 * j1 * 418711 + j1 * 13761 & 31;
                                float f2 = -((float)i3 + pPartialTicks) / 32.0F * (3.0F + random.nextFloat());
                                double d2 = (double)k1 + 0.5D - pX;
                                double d4 = (double)j1 + 0.5D - pY;
                                float f3 = (float)Math.sqrt(d2 * d2 + d4 * d4) / (float)l;
                                float f4 = ((1.0F - f3 * f3) * 0.5F + 0.5F) * f5;
                                blockpos$mutableblockpos.set(k1, l2, j1);
                                int j3 = getLightColor(level, blockpos$mutableblockpos);
                                bufferbuilder.vertex((double)k1 - pX - d0 + 0.5D, (double)k2 - p_109707_, (double)j1 - pY - d1 + 0.5D).uv(0.0F, (float)j2 * 0.25F + f2).color(1.0F, 1.0F, 1.0F, f4).uv2(j3).endVertex();
                                bufferbuilder.vertex((double)k1 - pX + d0 + 0.5D, (double)k2 - p_109707_, (double)j1 - pY + d1 + 0.5D).uv(1.0F, (float)j2 * 0.25F + f2).color(1.0F, 1.0F, 1.0F, f4).uv2(j3).endVertex();
                                bufferbuilder.vertex((double)k1 - pX + d0 + 0.5D, (double)j2 - p_109707_, (double)j1 - pY + d1 + 0.5D).uv(1.0F, (float)k2 * 0.25F + f2).color(1.0F, 1.0F, 1.0F, f4).uv2(j3).endVertex();
                                bufferbuilder.vertex((double)k1 - pX - d0 + 0.5D, (double)j2 - p_109707_, (double)j1 - pY - d1 + 0.5D).uv(0.0F, (float)k2 * 0.25F + f2).color(1.0F, 1.0F, 1.0F, f4).uv2(j3).endVertex();
                            }
                            else
                            {
                                if (i1 != 1)
                                {
                                    if (i1 >= 0)
                                    {
                                        tesselator.end();
                                    }

                                    i1 = 1;
                                    RenderSystem.setShaderTexture(0, SNOW_LOCATION);
                                    bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                                }

                                float f6 = -((float)(this.ticks & 511) + pPartialTicks) / 512.0F;
                                float f7 = (float)(random.nextDouble() + (double)f * 0.01D * (double)((float)random.nextGaussian()));
                                float f8 = (float)(random.nextDouble() + (double)(f * (float)random.nextGaussian()) * 0.001D);
                                double d3 = (double)k1 + 0.5D - pX;
                                double d5 = (double)j1 + 0.5D - pY;
                                float f9 = (float)Math.sqrt(d3 * d3 + d5 * d5) / (float)l;
                                float f10 = ((1.0F - f9 * f9) * 0.3F + 0.5F) * f5;
                                blockpos$mutableblockpos.set(k1, l2, j1);
                                int k3 = getLightColor(level, blockpos$mutableblockpos);
                                int l3 = k3 >> 16 & 65535;
                                int i4 = k3 & 65535;
                                int j4 = (l3 * 3 + 240) / 4;
                                int k4 = (i4 * 3 + 240) / 4;
                                bufferbuilder.vertex((double)k1 - pX - d0 + 0.5D, (double)k2 - p_109707_, (double)j1 - pY - d1 + 0.5D).uv(0.0F + f7, (float)j2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, f10).uv2(k4, j4).endVertex();
                                bufferbuilder.vertex((double)k1 - pX + d0 + 0.5D, (double)k2 - p_109707_, (double)j1 - pY + d1 + 0.5D).uv(1.0F + f7, (float)j2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, f10).uv2(k4, j4).endVertex();
                                bufferbuilder.vertex((double)k1 - pX + d0 + 0.5D, (double)j2 - p_109707_, (double)j1 - pY + d1 + 0.5D).uv(1.0F + f7, (float)k2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, f10).uv2(k4, j4).endVertex();
                                bufferbuilder.vertex((double)k1 - pX - d0 + 0.5D, (double)j2 - p_109707_, (double)j1 - pY - d1 + 0.5D).uv(0.0F + f7, (float)k2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, f10).uv2(k4, j4).endVertex();
                            }
                        }
                    }
                }
            }

            if (i1 >= 0)
            {
                tesselator.end();
            }

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            pLightmap.turnOffLightLayer();
        }
    }

    public void tickRain(Camera pActiveRenderInfo)
    {
        if (Reflector.ForgeDimensionRenderInfo_getWeatherParticleRenderHandler.exists())
        {
            IWeatherParticleRenderHandler iweatherparticlerenderhandler = (IWeatherParticleRenderHandler)Reflector.call(this.level.effects(), Reflector.ForgeDimensionRenderInfo_getWeatherParticleRenderHandler);

            if (iweatherparticlerenderhandler != null)
            {
                iweatherparticlerenderhandler.render(this.ticks, this.level, this.minecraft, pActiveRenderInfo);
                return;
            }
        }

        float f = this.minecraft.level.getRainLevel(1.0F) / (Minecraft.useFancyGraphics() ? 1.0F : 2.0F);

        if (!Config.isRainFancy())
        {
            f /= 2.0F;
        }

        if (!(f <= 0.0F) && Config.isRainSplash())
        {
            Random random = new Random((long)this.ticks * 312987231L);
            LevelReader levelreader = this.minecraft.level;
            BlockPos blockpos = new BlockPos(pActiveRenderInfo.getPosition());
            BlockPos blockpos1 = null;
            int i = (int)(100.0F * f * f) / (this.minecraft.options.particles == ParticleStatus.DECREASED ? 2 : 1);

            for (int j = 0; j < i; ++j)
            {
                int k = random.nextInt(21) - 10;
                int l = random.nextInt(21) - 10;
                BlockPos blockpos2 = levelreader.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockpos.offset(k, 0, l)).below();
                Biome biome = levelreader.getBiome(blockpos2);

                if (blockpos2.getY() > levelreader.getMinBuildHeight() && blockpos2.getY() <= blockpos.getY() + 10 && blockpos2.getY() >= blockpos.getY() - 10 && biome.getPrecipitation() == Biome.Precipitation.RAIN && biome.getTemperature(blockpos2) >= 0.15F)
                {
                    blockpos1 = blockpos2;

                    if (this.minecraft.options.particles == ParticleStatus.MINIMAL)
                    {
                        break;
                    }

                    double d0 = random.nextDouble();
                    double d1 = random.nextDouble();
                    BlockState blockstate = levelreader.getBlockState(blockpos2);
                    FluidState fluidstate = levelreader.getFluidState(blockpos2);
                    VoxelShape voxelshape = blockstate.getCollisionShape(levelreader, blockpos2);
                    double d2 = voxelshape.max(Direction.Axis.Y, d0, d1);
                    double d3 = (double)fluidstate.getHeight(levelreader, blockpos2);
                    double d4 = Math.max(d2, d3);
                    ParticleOptions particleoptions = !fluidstate.is(FluidTags.LAVA) && !blockstate.is(Blocks.MAGMA_BLOCK) && !CampfireBlock.isLitCampfire(blockstate) ? ParticleTypes.RAIN : ParticleTypes.SMOKE;
                    this.minecraft.level.addParticle(particleoptions, (double)blockpos2.getX() + d0, (double)blockpos2.getY() + d4, (double)blockpos2.getZ() + d1, 0.0D, 0.0D, 0.0D);
                }
            }

            if (blockpos1 != null && random.nextInt(3) < this.rainSoundTime++)
            {
                this.rainSoundTime = 0;

                if (blockpos1.getY() > blockpos.getY() + 1 && levelreader.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockpos).getY() > Mth.floor((float)blockpos.getY()))
                {
                    this.minecraft.level.playLocalSound(blockpos1, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 0.1F, 0.5F, false);
                }
                else
                {
                    this.minecraft.level.playLocalSound(blockpos1, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.2F, 1.0F, false);
                }
            }
        }
    }

    public void close()
    {
        if (this.entityEffect != null)
        {
            this.entityEffect.close();
        }

        if (this.transparencyChain != null)
        {
            this.transparencyChain.close();
        }
    }

    public void onResourceManagerReload(ResourceManager pResourceManager)
    {
        this.initOutline();

        if (Minecraft.useShaderTransparency())
        {
            this.initTransparency();
        }
    }

    public void initOutline()
    {
        if (this.entityEffect != null)
        {
            this.entityEffect.close();
        }

        ResourceLocation resourcelocation = new ResourceLocation("shaders/post/entity_outline.json");

        try
        {
            this.entityEffect = new PostChain(this.minecraft.getTextureManager(), this.minecraft.getResourceManager(), this.minecraft.getMainRenderTarget(), resourcelocation);
            this.entityEffect.resize(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
            this.entityTarget = this.entityEffect.getTempTarget("final");
        }
        catch (IOException ioexception)
        {
            LOGGER.warn("Failed to load shader: {}", resourcelocation, ioexception);
            this.entityEffect = null;
            this.entityTarget = null;
        }
        catch (JsonSyntaxException jsonsyntaxexception)
        {
            LOGGER.warn("Failed to parse shader: {}", resourcelocation, jsonsyntaxexception);
            this.entityEffect = null;
            this.entityTarget = null;
        }
    }

    private void initTransparency()
    {
        this.deinitTransparency();
        ResourceLocation resourcelocation = new ResourceLocation("shaders/post/transparency.json");

        try
        {
            PostChain postchain = new PostChain(this.minecraft.getTextureManager(), this.minecraft.getResourceManager(), this.minecraft.getMainRenderTarget(), resourcelocation);
            postchain.resize(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
            RenderTarget rendertarget1 = postchain.getTempTarget("translucent");
            RenderTarget rendertarget2 = postchain.getTempTarget("itemEntity");
            RenderTarget rendertarget3 = postchain.getTempTarget("particles");
            RenderTarget rendertarget4 = postchain.getTempTarget("weather");
            RenderTarget rendertarget = postchain.getTempTarget("clouds");
            this.transparencyChain = postchain;
            this.translucentTarget = rendertarget1;
            this.itemEntityTarget = rendertarget2;
            this.particlesTarget = rendertarget3;
            this.weatherTarget = rendertarget4;
            this.cloudsTarget = rendertarget;
        }
        catch (Exception exception1)
        {
            String s = exception1 instanceof JsonSyntaxException ? "parse" : "load";
            String s1 = "Failed to " + s + " shader: " + resourcelocation;
            LevelRenderer.TransparencyShaderException levelrenderer$transparencyshaderexception = new LevelRenderer.TransparencyShaderException(s1, exception1);

            if (this.minecraft.getResourcePackRepository().getSelectedIds().size() > 1)
            {
                Component component;

                try
                {
                    component = new TextComponent(this.minecraft.getResourceManager().getResource(resourcelocation).getSourceName());
                }
                catch (IOException ioexception1)
                {
                    component = null;
                }

                this.minecraft.options.graphicsMode = GraphicsStatus.FANCY;
                this.minecraft.clearResourcePacksOnError(levelrenderer$transparencyshaderexception, component);
            }
            else
            {
                CrashReport crashreport = this.minecraft.fillReport(new CrashReport(s1, levelrenderer$transparencyshaderexception));
                this.minecraft.options.graphicsMode = GraphicsStatus.FANCY;
                this.minecraft.options.save();
                LOGGER.fatal(s1, (Throwable)levelrenderer$transparencyshaderexception);
                this.minecraft.emergencySave();
                Minecraft.crash(crashreport);
            }
        }
    }

    private void deinitTransparency()
    {
        if (this.transparencyChain != null)
        {
            this.transparencyChain.close();
            this.translucentTarget.destroyBuffers();
            this.itemEntityTarget.destroyBuffers();
            this.particlesTarget.destroyBuffers();
            this.weatherTarget.destroyBuffers();
            this.cloudsTarget.destroyBuffers();
            this.transparencyChain = null;
            this.translucentTarget = null;
            this.itemEntityTarget = null;
            this.particlesTarget = null;
            this.weatherTarget = null;
            this.cloudsTarget = null;
        }
    }

    public void doEntityOutline()
    {
        if (this.shouldShowEntityOutlines())
        {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            this.entityTarget.blitToScreen(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight(), false);
            RenderSystem.disableBlend();
        }
    }

    public boolean shouldShowEntityOutlines()
    {
        if (!Config.isShaders() && !Config.isAntialiasing())
        {
            return !this.minecraft.gameRenderer.isPanoramicMode() && this.entityTarget != null && this.entityEffect != null && this.minecraft.player != null;
        }
        else
        {
            return false;
        }
    }

    private void createDarkSky()
    {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        if (this.darkBuffer != null)
        {
            this.darkBuffer.close();
        }

        this.darkBuffer = new VertexBuffer();
        buildSkyDisc(bufferbuilder, -16.0F);
        this.darkBuffer.upload(bufferbuilder);
    }

    private void createLightSky()
    {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        if (this.skyBuffer != null)
        {
            this.skyBuffer.close();
        }

        this.skyBuffer = new VertexBuffer();
        buildSkyDisc(bufferbuilder, 16.0F);
        this.skyBuffer.upload(bufferbuilder);
    }

    private static void buildSkyDisc(BufferBuilder p_172948_, float p_172949_)
    {
        float f = Math.signum(p_172949_) * 512.0F;
        float f1 = 512.0F;
        RenderSystem.setShader(GameRenderer::getPositionShader);
        p_172948_.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
        p_172948_.vertex(0.0D, (double)p_172949_, 0.0D).endVertex();

        for (int i = -180; i <= 180; i += 45)
        {
            p_172948_.vertex((double)(f * Mth.cos((float)i * ((float)Math.PI / 180F))), (double)p_172949_, (double)(512.0F * Mth.sin((float)i * ((float)Math.PI / 180F)))).endVertex();
        }

        p_172948_.end();
    }

    private void createStars()
    {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);

        if (this.starBuffer != null)
        {
            this.starBuffer.close();
        }

        this.starBuffer = new VertexBuffer();
        this.drawStars(bufferbuilder);
        bufferbuilder.end();
        this.starBuffer.upload(bufferbuilder);
    }

    private void drawStars(BufferBuilder pBufferBuilder)
    {
        Random random = new Random(10842L);
        pBufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        for (int i = 0; i < 1500; ++i)
        {
            double d0 = (double)(random.nextFloat() * 2.0F - 1.0F);
            double d1 = (double)(random.nextFloat() * 2.0F - 1.0F);
            double d2 = (double)(random.nextFloat() * 2.0F - 1.0F);
            double d3 = (double)(0.15F + random.nextFloat() * 0.1F);
            double d4 = d0 * d0 + d1 * d1 + d2 * d2;

            if (d4 < 1.0D && d4 > 0.01D)
            {
                d4 = 1.0D / Math.sqrt(d4);
                d0 = d0 * d4;
                d1 = d1 * d4;
                d2 = d2 * d4;
                double d5 = d0 * 100.0D;
                double d6 = d1 * 100.0D;
                double d7 = d2 * 100.0D;
                double d8 = Math.atan2(d0, d2);
                double d9 = Math.sin(d8);
                double d10 = Math.cos(d8);
                double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
                double d12 = Math.sin(d11);
                double d13 = Math.cos(d11);
                double d14 = random.nextDouble() * Math.PI * 2.0D;
                double d15 = Math.sin(d14);
                double d16 = Math.cos(d14);

                for (int j = 0; j < 4; ++j)
                {
                    double d17 = 0.0D;
                    double d18 = (double)((j & 2) - 1) * d3;
                    double d19 = (double)((j + 1 & 2) - 1) * d3;
                    double d20 = 0.0D;
                    double d21 = d18 * d16 - d19 * d15;
                    double d22 = d19 * d16 + d18 * d15;
                    double d23 = d21 * d12 + 0.0D * d13;
                    double d24 = 0.0D * d12 - d21 * d13;
                    double d25 = d24 * d9 - d22 * d10;
                    double d26 = d22 * d9 + d24 * d10;
                    pBufferBuilder.vertex(d5 + d25, d6 + d23, d7 + d26).endVertex();
                }
            }
        }
    }

    public void setLevel(@Nullable ClientLevel pLevelClient)
    {
        this.lastCameraX = Double.MIN_VALUE;
        this.lastCameraY = Double.MIN_VALUE;
        this.lastCameraZ = Double.MIN_VALUE;
        this.lastCameraChunkX = Integer.MIN_VALUE;
        this.lastCameraChunkY = Integer.MIN_VALUE;
        this.lastCameraChunkZ = Integer.MIN_VALUE;
        this.entityRenderDispatcher.setLevel(pLevelClient);
        this.level = pLevelClient;

        if (Config.isDynamicLights())
        {
            DynamicLights.clear();
        }

        ChunkVisibility.reset();
        this.renderEnv.reset((BlockState)null, (BlockPos)null);
        BiomeUtils.onWorldChanged(this.level);
        Shaders.checkWorldChanged(this.level);

        if (pLevelClient != null)
        {
            this.renderChunks.ensureCapacity(4356 * pLevelClient.getSectionsCount());
            this.allChanged();
        }
        else
        {
            this.chunksToCompile.clear();
            this.chunksToUpdatePrev.clear();
            this.clearRenderInfos();

            if (this.viewArea != null)
            {
                this.viewArea.releaseAllBuffers();
                this.viewArea = null;
            }

            if (this.chunkRenderDispatcher != null)
            {
                this.chunkRenderDispatcher.dispose();
            }

            this.chunkRenderDispatcher = null;
            this.globalBlockEntities.clear();
        }
    }

    public void graphicsChanged()
    {
        if (Minecraft.useShaderTransparency())
        {
            this.initTransparency();
        }
        else
        {
            this.deinitTransparency();
        }
    }

    public void allChanged()
    {
        if (this.level != null)
        {
            this.graphicsChanged();
            this.level.clearTintCaches();

            if (this.chunkRenderDispatcher == null)
            {
                this.chunkRenderDispatcher = new ChunkRenderDispatcher(this.level, this, Util.backgroundExecutor(), this.minecraft.is64Bit(), this.renderBuffers.fixedBufferPack());
            }
            else
            {
                this.chunkRenderDispatcher.setLevel(this.level);
            }

            this.needsUpdate = true;
            this.generateClouds = true;
            ItemBlockRenderTypes.setFancy(Config.isTreesFancy());
            ModelBlockRenderer.updateAoLightValue();

            if (Config.isDynamicLights())
            {
                DynamicLights.clear();
            }

            SmartAnimations.update();
            ambientOcclusion = Minecraft.useAmbientOcclusion();
            this.lastViewDistance = this.minecraft.options.renderDistance;
            this.renderDistance = this.lastViewDistance * 16;
            this.renderDistanceSq = this.renderDistance * this.renderDistance;

            if (this.viewArea != null)
            {
                this.viewArea.releaseAllBuffers();
            }

            this.resetChunksToCompile();

            synchronized (this.globalBlockEntities)
            {
                this.globalBlockEntities.clear();
            }

            this.viewArea = new ViewArea(this.chunkRenderDispatcher, this.level, this.minecraft.options.renderDistance, this);
            this.renderInfoMap = new LevelRenderer.RenderInfoMap(this.viewArea.chunks.length);

            if (this.level != null)
            {
                Entity entity = this.minecraft.getCameraEntity();

                if (entity != null)
                {
                    this.viewArea.repositionCamera(entity.getX(), entity.getZ());
                }
            }
        }

        if (this.minecraft.player == null)
        {
            this.firstWorldLoad = true;
        }
    }

    protected void resetChunksToCompile()
    {
        this.chunksToCompile.clear();
        this.chunkRenderDispatcher.blockUntilClear();
    }

    public void resize(int pWidth, int pHeight)
    {
        this.needsUpdate();

        if (this.entityEffect != null)
        {
            this.entityEffect.resize(pWidth, pHeight);
        }

        if (this.transparencyChain != null)
        {
            this.transparencyChain.resize(pWidth, pHeight);
        }
    }

    public String getChunkStatistics()
    {
        int i = this.viewArea.chunks.length;
        int j = this.countRenderedChunks();
        return String.format("C: %d/%d %sD: %d, %s", j, i, this.minecraft.smartCull ? "(s) " : "", this.lastViewDistance, this.chunkRenderDispatcher == null ? "null" : this.chunkRenderDispatcher.getStats());
    }

    public ChunkRenderDispatcher getChunkRenderDispatcher()
    {
        return this.chunkRenderDispatcher;
    }

    public double getTotalChunks()
    {
        return (double)this.viewArea.chunks.length;
    }

    public double getLastViewDistance()
    {
        return (double)this.lastViewDistance;
    }

    public int countRenderedChunks()
    {
        int i = 0;

        for (LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo : this.renderChunks)
        {
            if (!levelrenderer$renderchunkinfo.chunk.getCompiledChunk().hasNoRenderableLayers())
            {
                ++i;
            }
        }

        return i;
    }

    public String getEntityStatistics()
    {
        return "E: " + this.renderedEntities + "/" + this.level.getEntityCount() + ", B: " + this.culledEntities + ", " + Config.getVersionDebug();
    }

    public void setupRender(Camera pActiveRenderInfo, Frustum pCamera, boolean pDebugCamera, int pFrameCount, boolean pPlayerSpectator)
    {
        Vec3 vec3 = pActiveRenderInfo.getPosition();

        if (this.minecraft.options.renderDistance != this.lastViewDistance)
        {
            this.allChanged();
        }

        this.level.getProfiler().push("camera");
        double d0 = this.minecraft.player.getX();
        double d1 = this.minecraft.player.getY();
        double d2 = this.minecraft.player.getZ();
        double d3 = d0 - this.lastCameraX;
        double d4 = d1 - this.lastCameraY;
        double d5 = d2 - this.lastCameraZ;
        int i = SectionPos.posToSectionCoord(d0);
        int j = SectionPos.posToSectionCoord(d1);
        int k = SectionPos.posToSectionCoord(d2);

        if (this.lastCameraChunkX != i || this.lastCameraChunkY != j || this.lastCameraChunkZ != k || d3 * d3 + d4 * d4 + d5 * d5 > 16.0D)
        {
            this.lastCameraX = d0;
            this.lastCameraY = d1;
            this.lastCameraZ = d2;
            this.lastCameraChunkX = i;
            this.lastCameraChunkY = j;
            this.lastCameraChunkZ = k;
            this.viewArea.repositionCamera(d0, d2);
        }

        if (Config.isDynamicLights())
        {
            DynamicLights.update(this);
        }

        this.chunkRenderDispatcher.setCamera(vec3);
        this.level.getProfiler().popPush("cull");
        this.minecraft.getProfiler().popPush("culling");
        BlockPos blockpos = pActiveRenderInfo.getBlockPosition();
        ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = this.viewArea.getRenderChunkAt(blockpos);
        int l = 16;
        BlockPos blockpos1 = new BlockPos(Mth.floor(vec3.x / 16.0D) * 16, Mth.floor(vec3.y / 16.0D) * 16, Mth.floor(vec3.z / 16.0D) * 16);
        float f = pActiveRenderInfo.getXRot();
        float f1 = pActiveRenderInfo.getYRot();
        this.needsUpdate = this.needsUpdate || !this.chunksToCompile.isEmpty() || vec3.x != this.prevCamX || vec3.y != this.prevCamY || vec3.z != this.prevCamZ || (double)f != this.prevCamRotX || (double)f1 != this.prevCamRotY;
        this.prevCamX = vec3.x;
        this.prevCamY = vec3.y;
        this.prevCamZ = vec3.z;
        this.prevCamRotX = (double)f;
        this.prevCamRotY = (double)f1;
        this.minecraft.getProfiler().popPush("update");
        Lagometer.timerVisibility.start();
        int i1 = this.getCountLoadedChunks();

        if (i1 != this.countLoadedChunksPrev)
        {
            this.countLoadedChunksPrev = i1;
            this.needsUpdate = true;
        }

        Entity entity = pActiveRenderInfo.getEntity();
        int j1 = this.level.getMaxBuildHeight();
        int k1 = j1;

        if (!ChunkVisibility.isFinished())
        {
            this.needsUpdate = true;
        }

        EntitySectionStorage entitysectionstorage = this.level.getEntityStorage().getSectionStorage();

        if (entitysectionstorage.resetUpdated())
        {
            this.needsUpdate = true;
        }

        if (!pDebugCamera && this.needsUpdate && Config.isIntegratedServerRunning() && !Shaders.isShadowPass)
        {
            k1 = ChunkVisibility.getMaxChunkY(this.level, entity, this.lastViewDistance);
        }

        ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk1 = this.viewArea.getRenderChunkAt(new BlockPos(entity.getX(), entity.getY(), entity.getZ()));

        if (Shaders.isShadowPass)
        {
            this.renderChunks = this.renderInfosShadow;
            this.renderInfosEntities = this.renderInfosEntitiesShadow;
            this.renderInfosTileEntities = this.renderInfosTileEntitiesShadow;

            if (!pDebugCamera && this.needsUpdate)
            {
                this.clearRenderInfos();

                if (chunkrenderdispatcher$renderchunk1 != null && chunkrenderdispatcher$renderchunk1.getOrigin().getY() > k1)
                {
                    this.addEntitySection(this.renderInfosEntities, entitysectionstorage, chunkrenderdispatcher$renderchunk1.getOrigin());
                }

                Iterator<ChunkRenderDispatcher.RenderChunk> iterator = ShadowUtils.makeShadowChunkIterator(this.level, 0.0D, entity, this.lastViewDistance, this.viewArea);

                while (iterator.hasNext())
                {
                    ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk2 = iterator.next();

                    if (chunkrenderdispatcher$renderchunk2 != null && chunkrenderdispatcher$renderchunk2.getOrigin().getY() <= k1)
                    {
                        LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo = chunkrenderdispatcher$renderchunk2.getRenderInfo();

                        if (!chunkrenderdispatcher$renderchunk2.compiled.get().hasNoRenderableLayers())
                        {
                            this.renderChunks.add(levelrenderer$renderchunkinfo);
                        }

                        this.addEntitySection(this.renderInfosEntities, entitysectionstorage, chunkrenderdispatcher$renderchunk2.getOrigin());

                        if (chunkrenderdispatcher$renderchunk2.getCompiledChunk().getRenderableBlockEntities().size() > 0)
                        {
                            this.renderInfosTileEntities.add(levelrenderer$renderchunkinfo);
                        }
                    }
                }
            }
        }
        else
        {
            this.renderChunks = this.renderInfosNormal;
            this.renderInfosEntities = this.renderInfosEntitiesNormal;
            this.renderInfosTileEntities = this.renderInfosTileEntitiesNormal;
        }

        if (!pDebugCamera && this.needsUpdate && !Shaders.isShadowPass)
        {
            this.needsUpdate = false;
            Vector3f vector3f = pActiveRenderInfo.getLookVector();
            this.updateRenderChunks(pCamera, pFrameCount, pPlayerSpectator, vec3, blockpos, chunkrenderdispatcher$renderchunk, 16, blockpos1, k1, j1, chunkrenderdispatcher$renderchunk1, vector3f);
        }

        Lagometer.timerVisibility.end();

        if (Shaders.isShadowPass)
        {
            Shaders.mcProfilerEndSection();
        }
        else
        {
            this.minecraft.getProfiler().popPush("rebuildNear");
            Set<ChunkRenderDispatcher.RenderChunk> set = this.chunksToCompile;
            this.chunksToCompile = this.chunksToUpdatePrev;
            this.chunksToUpdatePrev = set;
            this.chunksToCompile.clear();
            Lagometer.timerChunkUpdate.start();

            for (LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo1 : this.renderChunks)
            {
                ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk3 = levelrenderer$renderchunkinfo1.chunk;

                if (chunkrenderdispatcher$renderchunk3.isDirty() || set.contains(chunkrenderdispatcher$renderchunk3))
                {
                    this.needsUpdate = true;
                    BlockPos blockpos2 = chunkrenderdispatcher$renderchunk3.getOrigin();
                    boolean flag = (double)MathUtils.distanceSq(blockpos1, (float)(blockpos2.getX() + 8), (float)(blockpos2.getY() + 8), (float)(blockpos2.getZ() + 8)) < 768.0D;

                    if (!chunkrenderdispatcher$renderchunk3.isDirtyFromPlayer() && !flag)
                    {
                        this.chunksToCompile.add(chunkrenderdispatcher$renderchunk3);
                    }
                    else if (!chunkrenderdispatcher$renderchunk3.isPlayerUpdate())
                    {
                        this.chunksToUpdateForced.add(chunkrenderdispatcher$renderchunk3);
                    }
                    else
                    {
                        this.minecraft.getProfiler().push("build near");
                        this.chunkRenderDispatcher.rebuildChunkSync(chunkrenderdispatcher$renderchunk3);
                        chunkrenderdispatcher$renderchunk3.setNotDirty();
                        this.minecraft.getProfiler().pop();
                    }
                }
            }

            Lagometer.timerChunkUpdate.end();
            this.chunksToCompile.addAll(set);
            this.minecraft.getProfiler().pop();
        }
    }

    private void updateRenderChunks(Frustum camera, int frameCount, boolean playerSpectator, Vec3 viewPos, BlockPos viewBlockPos, ChunkRenderDispatcher.RenderChunk viewRenderChunk, int chunkSizeIn, BlockPos playerPosIn, int maxChunkY, int maxWorldY, ChunkRenderDispatcher.RenderChunk renderChunkPlayer, Vector3f viewVector)
    {
        this.clearRenderInfos();
        this.visibilityDeque.clear();
        Deque<LevelRenderer.RenderChunkInfo> deque = this.visibilityDeque;
        Entity.setViewScale(Mth.clamp((double)this.minecraft.options.renderDistance / 8.0D, 1.0D, 2.5D) * (double)this.minecraft.options.entityDistanceScaling);
        boolean flag = this.minecraft.smartCull;
        EntitySectionStorage entitysectionstorage = this.level.getEntityStorage().getSectionStorage();
        int i = viewBlockPos.getY();
        int j = i >> 4 << 4;

        if (j > maxChunkY)
        {
            maxChunkY += 16;

            if (j > maxChunkY && maxChunkY < maxWorldY)
            {
                if (renderChunkPlayer != null)
                {
                    this.addEntitySection(this.renderInfosEntities, entitysectionstorage, renderChunkPlayer.getOrigin());
                }

                Vec3 vec3 = new Vec3((double)viewBlockPos.getX(), (double)maxChunkY, (double)viewBlockPos.getZ());
                Vec3 vec31 = new Vec3(vec3.x(), vec3.y(), vec3.z());
                Vector3f vector3f = new Vector3f(viewVector.x(), 0.0F, viewVector.z());

                if (!vector3f.normalize())
                {
                    vector3f = new Vector3f(1.0F, 0.0F, 0.0F);
                }

                double d0 = (double)(vector3f.x() * 16.0F);
                double d1 = (double)(vector3f.z() * 16.0F);
                double d2 = (double)(this.lastViewDistance * 16);

                for (double d3 = d2 * d2; vec31.distanceToSqr(vec3) < d3; vec31 = vec31.add(d0, 0.0D, d1))
                {
                    ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = this.viewArea.getRenderChunkAt(new BlockPos(vec31));

                    if (chunkrenderdispatcher$renderchunk == null)
                    {
                        break;
                    }

                    if (camera.isVisible(chunkrenderdispatcher$renderchunk.bb))
                    {
                        chunkrenderdispatcher$renderchunk.setFrame(frameCount);
                        deque.add(new LevelRenderer.RenderChunkInfo(chunkrenderdispatcher$renderchunk, (Direction)null, 0));
                        break;
                    }
                }
            }
        }

        if (deque.isEmpty())
        {
            if (viewRenderChunk != null && viewRenderChunk.getOrigin().getY() <= maxChunkY)
            {
                if (playerSpectator && this.level.getBlockState(viewBlockPos).isSolidRender(this.level, viewBlockPos))
                {
                    flag = false;
                }

                viewRenderChunk.setFrame(frameCount);
                deque.add(new LevelRenderer.RenderChunkInfo(viewRenderChunk, (Direction)null, 0));
            }
            else
            {
                int i1 = playerPosIn.getY() > this.level.getMinBuildHeight() ? Math.min(maxChunkY, this.level.getMaxBuildHeight() - 8) : this.level.getMinBuildHeight() + 8;

                if (renderChunkPlayer != null)
                {
                    this.addEntitySection(this.renderInfosEntities, entitysectionstorage, renderChunkPlayer.getOrigin());
                }

                int k1 = Mth.floor(viewPos.x / (double)chunkSizeIn) * chunkSizeIn;
                int k = Mth.floor(viewPos.z / (double)chunkSizeIn) * chunkSizeIn;
                List<LevelRenderer.RenderChunkInfo> list = Lists.newArrayList();

                for (int l1 = -this.lastViewDistance; l1 <= this.lastViewDistance; ++l1)
                {
                    for (int l = -this.lastViewDistance; l <= this.lastViewDistance; ++l)
                    {
                        ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk2 = this.viewArea.getRenderChunkAt(new BlockPos(k1 + SectionPos.sectionToBlockCoord(l1, 8), i1, k + SectionPos.sectionToBlockCoord(l, 8)));

                        if (chunkrenderdispatcher$renderchunk2 != null && camera.isVisible(chunkrenderdispatcher$renderchunk2.bb))
                        {
                            chunkrenderdispatcher$renderchunk2.setFrame(frameCount);
                            LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo = chunkrenderdispatcher$renderchunk2.getRenderInfo();
                            levelrenderer$renderchunkinfo.initialize((Direction)null, 0, 0);
                            list.add(levelrenderer$renderchunkinfo);
                        }
                    }
                }

                list.sort(Comparator.comparingDouble((p_316154_1_) ->
                {
                    return viewBlockPos.distSqr(p_316154_1_.chunk.getOrigin().offset(8, 8, 8));
                }));
                deque.addAll(list);
            }
        }

        this.minecraft.getProfiler().push("iteration");
        int j1 = this.minecraft.options.renderDistance;
        this.renderInfoMap.clear();
        boolean flag2 = Config.isFogOn();

        while (!deque.isEmpty())
        {
            LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo1 = deque.poll();
            ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk1 = levelrenderer$renderchunkinfo1.chunk;
            ChunkRenderDispatcher.CompiledChunk chunkrenderdispatcher$compiledchunk = chunkrenderdispatcher$renderchunk1.compiled.get();

            if (!chunkrenderdispatcher$compiledchunk.hasNoRenderableLayers() || chunkrenderdispatcher$renderchunk1.isDirty())
            {
                this.renderChunks.add(levelrenderer$renderchunkinfo1);
            }

            this.addEntitySection(this.renderInfosEntities, entitysectionstorage, chunkrenderdispatcher$renderchunk1.getOrigin());

            if (chunkrenderdispatcher$compiledchunk.getRenderableBlockEntities().size() > 0)
            {
                this.renderInfosTileEntities.add(levelrenderer$renderchunkinfo1);
            }

            Direction[] adirection = flag ? ChunkVisibility.getFacingsNotOpposite(levelrenderer$renderchunkinfo1.directions) : Direction.VALUES;

            for (Direction direction : adirection)
            {
                if (!flag || !levelrenderer$renderchunkinfo1.hasDirection(direction.getOpposite()))
                {
                    if (flag && levelrenderer$renderchunkinfo1.hasSourceDirections())
                    {
                        ChunkRenderDispatcher.CompiledChunk chunkrenderdispatcher$compiledchunk1 = chunkrenderdispatcher$renderchunk1.getCompiledChunk();
                        boolean flag1 = false;

                        for (int i2 = 0; i2 < DIRECTIONS.length; ++i2)
                        {
                            if (levelrenderer$renderchunkinfo1.hasSourceDirection(i2) && chunkrenderdispatcher$compiledchunk1.facesCanSeeEachother(DIRECTIONS[i2].getOpposite(), direction))
                            {
                                flag1 = true;
                                break;
                            }
                        }

                        if (!flag1)
                        {
                            continue;
                        }
                    }

                    ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk3 = this.getRenderChunkOffset(playerPosIn, chunkrenderdispatcher$renderchunk1, direction, flag2, maxChunkY);

                    if (chunkrenderdispatcher$renderchunk3 != null && chunkrenderdispatcher$renderchunk3.hasAllNeighbors())
                    {
                        if (!chunkrenderdispatcher$renderchunk3.setFrame(frameCount))
                        {
                            LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo2 = this.renderInfoMap.get(chunkrenderdispatcher$renderchunk3);

                            if (levelrenderer$renderchunkinfo2 != null)
                            {
                                levelrenderer$renderchunkinfo2.addSourceDirection(direction);
                            }
                        }
                        else if (camera.isVisible(chunkrenderdispatcher$renderchunk3.bb))
                        {
                            LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo3 = chunkrenderdispatcher$renderchunk3.getRenderInfo();
                            int j2 = levelrenderer$renderchunkinfo1.directions | 1 << direction.ordinal();
                            levelrenderer$renderchunkinfo3.initialize(direction, j2, levelrenderer$renderchunkinfo1.step + 1);
                            deque.add(levelrenderer$renderchunkinfo3);
                            this.renderInfoMap.put(chunkrenderdispatcher$renderchunk3, levelrenderer$renderchunkinfo3);
                        }
                    }
                }
            }
        }

        this.minecraft.getProfiler().pop();
    }

    @Nullable
    private ChunkRenderDispatcher.RenderChunk getRenderChunkOffset(BlockPos playerPos, ChunkRenderDispatcher.RenderChunk renderChunkBase, Direction facing, boolean fog, int yMax)
    {
        ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = renderChunkBase.getRenderChunkNeighbour(facing);

        if (chunkrenderdispatcher$renderchunk == null)
        {
            return null;
        }
        else if (chunkrenderdispatcher$renderchunk.getOrigin().getY() > yMax)
        {
            return null;
        }
        else
        {
            if (fog)
            {
                BlockPos blockpos = chunkrenderdispatcher$renderchunk.getOrigin();
                int i = playerPos.getX() - blockpos.getX();
                int j = playerPos.getZ() - blockpos.getZ();
                int k = i * i + j * j;

                if (k > this.renderDistanceSq)
                {
                    return null;
                }
            }

            return chunkrenderdispatcher$renderchunk;
        }
    }

    private void captureFrustum(Matrix4f p_109526_, Matrix4f p_109527_, double p_109528_, double p_109529_, double p_109530_, Frustum p_109531_)
    {
        this.capturedFrustum = p_109531_;
        Matrix4f matrix4f = p_109527_.copy();
        matrix4f.multiply(p_109526_);
        matrix4f.invert();
        this.frustumPos.x = p_109528_;
        this.frustumPos.y = p_109529_;
        this.frustumPos.z = p_109530_;
        this.frustumPoints[0] = new Vector4f(-1.0F, -1.0F, -1.0F, 1.0F);
        this.frustumPoints[1] = new Vector4f(1.0F, -1.0F, -1.0F, 1.0F);
        this.frustumPoints[2] = new Vector4f(1.0F, 1.0F, -1.0F, 1.0F);
        this.frustumPoints[3] = new Vector4f(-1.0F, 1.0F, -1.0F, 1.0F);
        this.frustumPoints[4] = new Vector4f(-1.0F, -1.0F, 1.0F, 1.0F);
        this.frustumPoints[5] = new Vector4f(1.0F, -1.0F, 1.0F, 1.0F);
        this.frustumPoints[6] = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.frustumPoints[7] = new Vector4f(-1.0F, 1.0F, 1.0F, 1.0F);

        for (int i = 0; i < 8; ++i)
        {
            this.frustumPoints[i].transform(matrix4f);
            this.frustumPoints[i].perspectiveDivide();
        }
    }

    public void prepareCullFrustum(PoseStack p_172962_, Vec3 p_172963_, Matrix4f p_172964_)
    {
        Matrix4f matrix4f = p_172962_.last().pose();
        double d0 = p_172963_.x();
        double d1 = p_172963_.y();
        double d2 = p_172963_.z();
        this.cullingFrustum = new Frustum(matrix4f, p_172964_);
        this.cullingFrustum.prepare(d0, d1, d2);
    }

    public void renderLevel(PoseStack pMatrixStack, float pPartialTicks, long pFinishTimeNano, boolean p_109603_, Camera pDrawBlockOutline, GameRenderer pActiveRenderInfo, LightTexture pGameRenderer, Matrix4f pLightmap)
    {
        RenderSystem.setShaderGameTime(this.level.getGameTime(), pPartialTicks);
        this.blockEntityRenderDispatcher.prepare(this.level, pDrawBlockOutline, this.minecraft.hitResult);
        this.entityRenderDispatcher.prepare(this.level, pDrawBlockOutline, this.minecraft.crosshairPickEntity);
        ProfilerFiller profilerfiller = this.level.getProfiler();
        profilerfiller.popPush("light_updates");
        this.minecraft.level.getChunkSource().getLightEngine().runUpdates(Integer.MAX_VALUE, true, true);
        Vec3 vec3 = pDrawBlockOutline.getPosition();
        double d0 = vec3.x();
        double d1 = vec3.y();
        double d2 = vec3.z();
        Matrix4f matrix4f = pMatrixStack.last().pose();
        profilerfiller.popPush("culling");
        boolean flag = this.capturedFrustum != null;
        Frustum frustum;

        if (flag)
        {
            frustum = this.capturedFrustum;
            frustum.prepare(this.frustumPos.x, this.frustumPos.y, this.frustumPos.z);
        }
        else
        {
            frustum = this.cullingFrustum;
        }

        this.minecraft.getProfiler().popPush("captureFrustum");

        if (this.captureFrustum)
        {
            this.captureFrustum(matrix4f, pLightmap, vec3.x, vec3.y, vec3.z, flag ? new Frustum(matrix4f, pLightmap) : frustum);
            this.captureFrustum = false;
        }

        profilerfiller.popPush("clear");

        if (Config.isShaders())
        {
            Shaders.setViewport(0, 0, this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
        }
        else
        {
            RenderSystem.viewport(0, 0, this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
        }

        FogRenderer.setupColor(pDrawBlockOutline, pPartialTicks, this.minecraft.level, this.minecraft.options.renderDistance, pActiveRenderInfo.getDarkenWorldAmount(pPartialTicks));
        FogRenderer.levelFogColor();
        RenderSystem.clear(16640, Minecraft.ON_OSX);
        boolean flag1 = Config.isShaders();

        if (flag1)
        {
            Shaders.clearRenderBuffer();
            Shaders.setCamera(pMatrixStack, pDrawBlockOutline, pPartialTicks);
            Shaders.renderPrepare();
        }

        frustum.disabled = Config.isShaders() && !Shaders.isFrustumCulling();
        float f = pActiveRenderInfo.getRenderDistance();
        boolean flag2 = this.minecraft.level.effects().isFoggyAt(Mth.floor(d0), Mth.floor(d1)) || this.minecraft.gui.getBossOverlay().shouldCreateWorldFog();

        if ((Config.isSkyEnabled() || Config.isSunMoonEnabled() || Config.isStarsEnabled()) && !Shaders.isShadowPass)
        {
            profilerfiller.popPush("sky");

            if (flag1)
            {
                Shaders.beginSky();
            }

            RenderSystem.setShader(GameRenderer::getPositionShader);
            this.renderSky(pMatrixStack, pLightmap, pPartialTicks, () ->
            {
                FogRenderer.setupFog(pDrawBlockOutline, FogRenderer.FogMode.FOG_SKY, f, flag2, pPartialTicks);
            });

            if (flag1)
            {
                Shaders.endSky();
            }
        }
        else
        {
            GlStateManager._disableBlend();
        }

        profilerfiller.popPush("fog");
        FogRenderer.setupFog(pDrawBlockOutline, FogRenderer.FogMode.FOG_TERRAIN, Math.max(f - 16.0F, 32.0F), flag2, pPartialTicks);
        profilerfiller.popPush("terrain_setup");
        this.checkLoadVisibleChunks(pDrawBlockOutline, frustum, this.minecraft.player.isSpectator());
        this.setupRender(pDrawBlockOutline, frustum, flag, this.frameId++, this.minecraft.player.isSpectator());
        profilerfiller.popPush("updatechunks");
        int i = 30;
        int j = this.minecraft.options.framerateLimit;
        long k = 33333333L;
        long l;

        if ((double)j == Option.FRAMERATE_LIMIT.getMaxValue())
        {
            l = 0L;
        }
        else
        {
            l = (long)(1000000000 / j);
        }

        long i1 = Util.getNanos() - pFinishTimeNano;
        long j1 = this.frameTimes.registerValueAndGetMean(i1);
        long k1 = j1 * 3L / 2L;
        long l1 = Mth.clamp(k1, l, 33333333L);
        Lagometer.timerChunkUpload.start();
        this.compileChunksUntil(pFinishTimeNano + l1);
        Lagometer.timerChunkUpload.end();
        profilerfiller.popPush("terrain");
        Lagometer.timerTerrain.start();

        if (this.minecraft.options.ofSmoothFps)
        {
            this.minecraft.getProfiler().popPush("finish");
            GL11.glFinish();
            this.minecraft.getProfiler().popPush("terrain");
        }

        if (Config.isFogOff() && FogRenderer.fogStandard)
        {
            RenderSystem.setFogAllowed(false);
        }

        this.renderChunkLayer(RenderType.solid(), pMatrixStack, d0, d1, d2, pLightmap);
        this.minecraft.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).setBlurMipmap(false, this.minecraft.options.mipmapLevels > 0);
        this.renderChunkLayer(RenderType.cutoutMipped(), pMatrixStack, d0, d1, d2, pLightmap);
        this.minecraft.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).restoreLastBlurMipmap();
        this.renderChunkLayer(RenderType.cutout(), pMatrixStack, d0, d1, d2, pLightmap);

        if (flag1)
        {
            ShadersRender.endTerrain();
        }

        Lagometer.timerTerrain.end();

        if (this.level.effects().constantAmbientLight())
        {
            Lighting.setupNetherLevel(pMatrixStack.last().pose());
        }
        else
        {
            Lighting.setupLevel(pMatrixStack.last().pose());
        }

        if (flag1)
        {
            Shaders.beginEntities();
        }

        ItemFrameRenderer.updateItemRenderDistance();
        profilerfiller.popPush("entities");
        ++renderEntitiesCounter;
        this.renderedEntities = 0;
        this.culledEntities = 0;
        this.countTileEntitiesRendered = 0;

        if (this.itemEntityTarget != null)
        {
            this.itemEntityTarget.clear(Minecraft.ON_OSX);
            this.itemEntityTarget.copyDepthFrom(this.minecraft.getMainRenderTarget());
            this.minecraft.getMainRenderTarget().bindWrite(false);
        }

        if (this.weatherTarget != null)
        {
            this.weatherTarget.clear(Minecraft.ON_OSX);
        }

        if (this.shouldShowEntityOutlines())
        {
            this.entityTarget.clear(Minecraft.ON_OSX);
            this.minecraft.getMainRenderTarget().bindWrite(false);
        }

        boolean flag3 = false;
        MultiBufferSource.BufferSource multibuffersource$buffersource = this.renderBuffers.bufferSource();

        if (Config.isFastRender())
        {
            RenderStateManager.enableCache();
        }

        int i2 = this.level.getMinBuildHeight();
        int j2 = this.level.getMaxBuildHeight();

        for (Entity entity : this.level.entitiesForRendering())
        {
            BlockPos blockpos = entity.blockPosition();

            if (this.renderInfosEntities.contains(SectionPos.asLong(blockpos)) || blockpos.getY() <= i2 || blockpos.getY() >= j2)
            {
                boolean flag4 = entity == this.minecraft.player && !this.minecraft.player.isSpectator();

                if ((this.entityRenderDispatcher.shouldRender(entity, frustum, d0, d1, d2) || entity.hasIndirectPassenger(this.minecraft.player)) && (entity != pDrawBlockOutline.getEntity() || pDrawBlockOutline.isDetached() || pDrawBlockOutline.getEntity() instanceof LivingEntity && ((LivingEntity)pDrawBlockOutline.getEntity()).isSleeping()) && (!(entity instanceof LocalPlayer) || pDrawBlockOutline.getEntity() == entity || flag4))
                {
                    String s = entity.getClass().getName();
                    List<Entity> list = this.mapEntityLists.get(s);

                    if (list == null)
                    {
                        list = new ArrayList<>();
                        this.mapEntityLists.put(s, list);
                    }

                    list.add(entity);
                }
            }
        }

        for (List<Entity> list1 : this.mapEntityLists.values())
        {
            for (Entity entity1 : list1)
            {
                ++this.renderedEntities;

                if (entity1.tickCount == 0)
                {
                    entity1.xOld = entity1.getX();
                    entity1.yOld = entity1.getY();
                    entity1.zOld = entity1.getZ();
                }

                MultiBufferSource multibuffersource;

                if (this.shouldShowEntityOutlines() && this.minecraft.shouldEntityAppearGlowing(entity1))
                {
                    flag3 = true;
                    OutlineBufferSource outlinebuffersource = this.renderBuffers.outlineBufferSource();
                    multibuffersource = outlinebuffersource;
                    int k2 = entity1.getTeamColor();
                    int l2 = 255;
                    int i3 = k2 >> 16 & 255;
                    int j3 = k2 >> 8 & 255;
                    int k3 = k2 & 255;
                    outlinebuffersource.setColor(i3, j3, k3, 255);
                }
                else
                {
                    multibuffersource = multibuffersource$buffersource;
                }

                this.renderedEntity = entity1;

                if (flag1)
                {
                    Shaders.nextEntity(entity1);
                }

                this.renderEntity(entity1, d0, d1, d2, pPartialTicks, pMatrixStack, multibuffersource);
                this.renderedEntity = null;
            }

            list1.clear();
        }

        multibuffersource$buffersource.endLastBatch();
        this.checkPoseStack(pMatrixStack);
        multibuffersource$buffersource.endBatch(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
        multibuffersource$buffersource.endBatch(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
        multibuffersource$buffersource.endBatch(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        multibuffersource$buffersource.endBatch(RenderType.entitySmoothCutout(TextureAtlas.LOCATION_BLOCKS));

        if (flag1)
        {
            Shaders.endEntities();
            Shaders.beginBlockEntities();
        }

        profilerfiller.popPush("blockentities");
        SignRenderer.updateTextRenderDistance();
        boolean flag5 = Reflector.IForgeTileEntity_getRenderBoundingBox.exists();
        Frustum frustum1 = frustum;
        label344:

        for (LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo : this.renderInfosTileEntities)
        {
            List<BlockEntity> list2 = levelrenderer$renderchunkinfo.chunk.getCompiledChunk().getRenderableBlockEntities();

            if (!list2.isEmpty())
            {
                Iterator iterator1 = list2.iterator();

                while (true)
                {
                    BlockEntity blockentity1;
                    AABB aabb1;

                    do
                    {
                        if (!iterator1.hasNext())
                        {
                            continue label344;
                        }

                        blockentity1 = (BlockEntity)iterator1.next();

                        if (!flag5)
                        {
                            break;
                        }

                        aabb1 = (AABB)Reflector.call(blockentity1, Reflector.IForgeTileEntity_getRenderBoundingBox);
                    }
                    while (aabb1 != null && !frustum1.isVisible(aabb1));

                    if (flag1)
                    {
                        Shaders.nextBlockEntity(blockentity1);
                    }

                    BlockPos blockpos4 = blockentity1.getBlockPos();
                    MultiBufferSource multibuffersource1 = multibuffersource$buffersource;
                    pMatrixStack.pushPose();
                    pMatrixStack.translate((double)blockpos4.getX() - d0, (double)blockpos4.getY() - d1, (double)blockpos4.getZ() - d2);
                    SortedSet<BlockDestructionProgress> sortedset = this.destructionProgress.get(blockpos4.asLong());

                    if (sortedset != null && !sortedset.isEmpty())
                    {
                        int l3 = sortedset.last().getProgress();

                        if (l3 >= 0)
                        {
                            PoseStack.Pose posestack$pose = pMatrixStack.last();
                            VertexConsumer vertexconsumer = new SheetedDecalTextureGenerator(this.renderBuffers.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(l3)), posestack$pose.pose(), posestack$pose.normal());
                            multibuffersource1 = (renderTypeIn) ->
                            {
                                VertexConsumer vertexconsumer3 = multibuffersource$buffersource.getBuffer(renderTypeIn);
                                return renderTypeIn.affectsCrumbling() ? VertexMultiConsumer.create(vertexconsumer, vertexconsumer3) : vertexconsumer3;
                            };
                        }
                    }

                    this.blockEntityRenderDispatcher.render(blockentity1, pPartialTicks, pMatrixStack, multibuffersource1);
                    pMatrixStack.popPose();
                    ++this.countTileEntitiesRendered;
                }
            }
        }

        synchronized (this.globalBlockEntities)
        {
            Iterator iterator = this.globalBlockEntities.iterator();
            label319:

            while (true)
            {
                BlockEntity blockentity;
                AABB aabb;

                do
                {
                    if (!iterator.hasNext())
                    {
                        break label319;
                    }

                    blockentity = (BlockEntity)iterator.next();

                    if (!flag5)
                    {
                        break;
                    }

                    aabb = (AABB)Reflector.call(blockentity, Reflector.IForgeTileEntity_getRenderBoundingBox);
                }
                while (aabb != null && !frustum1.isVisible(aabb));

                if (flag1)
                {
                    Shaders.nextBlockEntity(blockentity);
                }

                BlockPos blockpos3 = blockentity.getBlockPos();
                pMatrixStack.pushPose();
                pMatrixStack.translate((double)blockpos3.getX() - d0, (double)blockpos3.getY() - d1, (double)blockpos3.getZ() - d2);
                this.blockEntityRenderDispatcher.render(blockentity, pPartialTicks, pMatrixStack, multibuffersource$buffersource);
                pMatrixStack.popPose();
                ++this.countTileEntitiesRendered;
            }
        }

        this.checkPoseStack(pMatrixStack);
        multibuffersource$buffersource.endBatch(RenderType.solid());
        multibuffersource$buffersource.endBatch(RenderType.endPortal());
        multibuffersource$buffersource.endBatch(RenderType.endGateway());
        multibuffersource$buffersource.endBatch(Sheets.solidBlockSheet());
        multibuffersource$buffersource.endBatch(Sheets.cutoutBlockSheet());
        multibuffersource$buffersource.endBatch(Sheets.bedSheet());
        multibuffersource$buffersource.endBatch(Sheets.shulkerBoxSheet());
        multibuffersource$buffersource.endBatch(Sheets.signSheet());
        multibuffersource$buffersource.endBatch(Sheets.chestSheet());
        multibuffersource$buffersource.endBatch(Sheets.bannerSheet());
        this.renderBuffers.outlineBufferSource().endOutlineBatch();

        if (Config.isFastRender())
        {
            RenderStateManager.disableCache();
        }

        if (flag3)
        {
            this.entityEffect.process(pPartialTicks);
            this.minecraft.getMainRenderTarget().bindWrite(false);
        }

        if (flag1)
        {
            Shaders.endBlockEntities();
        }

        this.renderOverlayDamaged = true;
        profilerfiller.popPush("destroyProgress");

        for (Entry<SortedSet<BlockDestructionProgress>> entry : this.destructionProgress.long2ObjectEntrySet())
        {
            BlockPos blockpos2 = BlockPos.of(entry.getLongKey());
            double d3 = (double)blockpos2.getX() - d0;
            double d4 = (double)blockpos2.getY() - d1;
            double d5 = (double)blockpos2.getZ() - d2;

            if (!(d3 * d3 + d4 * d4 + d5 * d5 > 1024.0D))
            {
                SortedSet<BlockDestructionProgress> sortedset1 = entry.getValue();

                if (sortedset1 != null && !sortedset1.isEmpty())
                {
                    int i4 = sortedset1.last().getProgress();
                    pMatrixStack.pushPose();
                    pMatrixStack.translate((double)blockpos2.getX() - d0, (double)blockpos2.getY() - d1, (double)blockpos2.getZ() - d2);
                    PoseStack.Pose posestack$pose1 = pMatrixStack.last();
                    VertexConsumer vertexconsumer1 = new SheetedDecalTextureGenerator(this.renderBuffers.crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(i4)), posestack$pose1.pose(), posestack$pose1.normal());
                    this.minecraft.getBlockRenderer().renderBreakingTexture(this.level.getBlockState(blockpos2), blockpos2, this.level, pMatrixStack, vertexconsumer1);
                    pMatrixStack.popPose();
                }
            }
        }

        this.renderOverlayDamaged = false;
        --renderEntitiesCounter;
        this.checkPoseStack(pMatrixStack);
        HitResult hitresult = this.minecraft.hitResult;

        if (p_109603_ && hitresult != null && hitresult.getType() == HitResult.Type.BLOCK)
        {
            profilerfiller.popPush("outline");
            BlockPos blockpos1 = ((BlockHitResult)hitresult).getBlockPos();
            BlockState blockstate = this.level.getBlockState(blockpos1);

            if (flag1)
            {
                ShadersRender.beginOutline();
            }

            if (!Reflector.callBoolean(Reflector.ForgeHooksClient_onDrawBlockHighlight, this, pDrawBlockOutline, hitresult, pPartialTicks, pMatrixStack, multibuffersource$buffersource) && !blockstate.isAir() && this.level.getWorldBorder().isWithinBounds(blockpos1))
            {
                VertexConsumer vertexconsumer2 = multibuffersource$buffersource.getBuffer(RenderType.lines());
                this.renderHitOutline(pMatrixStack, vertexconsumer2, pDrawBlockOutline.getEntity(), d0, d1, d2, blockpos1, blockstate);
            }

            if (flag1)
            {
                multibuffersource$buffersource.endBatch(RenderType.lines());
                ShadersRender.endOutline();
            }
        }
        else if (hitresult != null && hitresult.getType() == HitResult.Type.ENTITY)
        {
            Reflector.ForgeHooksClient_onDrawBlockHighlight.call(this, pDrawBlockOutline, hitresult, pPartialTicks, pMatrixStack, multibuffersource$buffersource);
        }

        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.mulPoseMatrix(pMatrixStack.last().pose());
        RenderSystem.applyModelViewMatrix();

        if (flag1)
        {
            ShadersRender.beginDebug();
        }

        this.minecraft.debugRenderer.render(pMatrixStack, multibuffersource$buffersource, d0, d1, d2);
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        multibuffersource$buffersource.endBatch(Sheets.translucentCullBlockSheet());
        multibuffersource$buffersource.endBatch(Sheets.bannerSheet());
        multibuffersource$buffersource.endBatch(Sheets.shieldSheet());
        multibuffersource$buffersource.endBatch(RenderType.armorGlint());
        multibuffersource$buffersource.endBatch(RenderType.armorEntityGlint());
        multibuffersource$buffersource.endBatch(RenderType.glint());
        multibuffersource$buffersource.endBatch(RenderType.glintDirect());
        multibuffersource$buffersource.endBatch(RenderType.glintTranslucent());
        multibuffersource$buffersource.endBatch(RenderType.entityGlint());
        multibuffersource$buffersource.endBatch(RenderType.entityGlintDirect());
        multibuffersource$buffersource.endBatch(RenderType.waterMask());
        this.renderBuffers.crumblingBufferSource().endBatch();

        if (flag1)
        {
            multibuffersource$buffersource.endBatch();
            ShadersRender.endDebug();
            Shaders.preRenderHand();
            Matrix4f matrix4f1 = RenderSystem.getProjectionMatrix().copy();
            ShadersRender.renderHand0(pActiveRenderInfo, pMatrixStack, pDrawBlockOutline, pPartialTicks);
            RenderSystem.setProjectionMatrix(matrix4f1);
            Shaders.preWater();
        }

        if (this.transparencyChain != null)
        {
            multibuffersource$buffersource.endBatch(RenderType.lines());
            multibuffersource$buffersource.endBatch();
            this.translucentTarget.clear(Minecraft.ON_OSX);
            this.translucentTarget.copyDepthFrom(this.minecraft.getMainRenderTarget());
            profilerfiller.popPush("translucent");
            this.renderChunkLayer(RenderType.translucent(), pMatrixStack, d0, d1, d2, pLightmap);
            profilerfiller.popPush("string");
            this.renderChunkLayer(RenderType.tripwire(), pMatrixStack, d0, d1, d2, pLightmap);
            this.particlesTarget.clear(Minecraft.ON_OSX);
            this.particlesTarget.copyDepthFrom(this.minecraft.getMainRenderTarget());
            RenderStateShard.PARTICLES_TARGET.setupRenderState();
            profilerfiller.popPush("particles");

            if (Reflector.ForgeHooksClient.exists())
            {
                this.minecraft.particleEngine.render(pMatrixStack, multibuffersource$buffersource, pGameRenderer, pDrawBlockOutline, pPartialTicks, frustum);
            }
            else
            {
                this.minecraft.particleEngine.render(pMatrixStack, multibuffersource$buffersource, pGameRenderer, pDrawBlockOutline, pPartialTicks);
            }

            RenderStateShard.PARTICLES_TARGET.clearRenderState();
        }
        else
        {
            profilerfiller.popPush("translucent");

            if (flag1)
            {
                Shaders.beginWater();
            }

            if (this.translucentTarget != null)
            {
                this.translucentTarget.clear(Minecraft.ON_OSX);
            }

            this.renderChunkLayer(RenderType.translucent(), pMatrixStack, d0, d1, d2, pLightmap);

            if (flag1)
            {
                Shaders.endWater();
            }

            multibuffersource$buffersource.endBatch(RenderType.lines());
            multibuffersource$buffersource.endBatch();
            profilerfiller.popPush("string");
            this.renderChunkLayer(RenderType.tripwire(), pMatrixStack, d0, d1, d2, pLightmap);
            profilerfiller.popPush("particles");

            if (flag1)
            {
                Shaders.beginParticles();
            }

            if (Reflector.ForgeHooksClient.exists())
            {
                this.minecraft.particleEngine.render(pMatrixStack, multibuffersource$buffersource, pGameRenderer, pDrawBlockOutline, pPartialTicks, frustum);
            }
            else
            {
                this.minecraft.particleEngine.render(pMatrixStack, multibuffersource$buffersource, pGameRenderer, pDrawBlockOutline, pPartialTicks);
            }

            if (flag1)
            {
                Shaders.endParticles();
            }
        }

        RenderSystem.setFogAllowed(true);
        posestack.pushPose();
        posestack.mulPoseMatrix(pMatrixStack.last().pose());
        RenderSystem.applyModelViewMatrix();

        if (this.minecraft.options.getCloudsType() != CloudStatus.OFF)
        {
            if (this.transparencyChain != null)
            {
                this.cloudsTarget.clear(Minecraft.ON_OSX);
                RenderStateShard.CLOUDS_TARGET.setupRenderState();
                profilerfiller.popPush("clouds");
                this.renderClouds(pMatrixStack, pLightmap, pPartialTicks, d0, d1, d2);
                RenderStateShard.CLOUDS_TARGET.clearRenderState();
            }
            else
            {
                profilerfiller.popPush("clouds");
                RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
                this.renderClouds(pMatrixStack, pLightmap, pPartialTicks, d0, d1, d2);
            }
        }

        if (this.transparencyChain != null)
        {
            RenderStateShard.WEATHER_TARGET.setupRenderState();
            profilerfiller.popPush("weather");
            this.renderSnowAndRain(pGameRenderer, pPartialTicks, d0, d1, d2);
            this.renderWorldBorder(pDrawBlockOutline);
            RenderStateShard.WEATHER_TARGET.clearRenderState();
            this.transparencyChain.process(pPartialTicks);
            this.minecraft.getMainRenderTarget().bindWrite(false);
        }
        else
        {
            RenderSystem.depthMask(false);

            if (Config.isShaders())
            {
                GlStateManager._depthMask(Shaders.isRainDepth());
            }

            profilerfiller.popPush("weather");

            if (flag1)
            {
                Shaders.beginWeather();
            }

            this.renderSnowAndRain(pGameRenderer, pPartialTicks, d0, d1, d2);

            if (flag1)
            {
                Shaders.endWeather();
            }

            this.renderWorldBorder(pDrawBlockOutline);
            RenderSystem.depthMask(true);
        }

        this.renderDebug(pDrawBlockOutline);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        FogRenderer.setupNoFog();
    }

    public void checkPoseStack(PoseStack pMatrixStack)
    {
        if (!pMatrixStack.clear())
        {
            throw new IllegalStateException("Pose stack not empty");
        }
    }

    public void renderEntity(Entity pEntity, double pCamX, double p_109520_, double pCamY, float p_109522_, PoseStack pCamZ, MultiBufferSource p_109524_)
    {
        double d0 = Mth.lerp((double)p_109522_, pEntity.xOld, pEntity.getX());
        double d1 = Mth.lerp((double)p_109522_, pEntity.yOld, pEntity.getY());
        double d2 = Mth.lerp((double)p_109522_, pEntity.zOld, pEntity.getZ());
        float f = Mth.lerp(p_109522_, pEntity.yRotO, pEntity.getYRot());
        this.entityRenderDispatcher.render(pEntity, d0 - pCamX, d1 - p_109520_, d2 - pCamY, f, p_109522_, pCamZ, p_109524_, this.entityRenderDispatcher.getPackedLightCoords(pEntity, p_109522_));
    }

    public void renderChunkLayer(RenderType p_172994_, PoseStack p_172995_, double p_172996_, double p_172997_, double p_172998_, Matrix4f p_172999_)
    {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        p_172994_.setupRenderState();
        boolean flag = Config.isShaders();

        if (p_172994_ == RenderType.translucent() && !Shaders.isShadowPass)
        {
            this.minecraft.getProfiler().push("translucent_sort");
            double d0 = p_172996_ - this.xTransparentOld;
            double d1 = p_172997_ - this.yTransparentOld;
            double d2 = p_172998_ - this.zTransparentOld;

            if (d0 * d0 + d1 * d1 + d2 * d2 > 1.0D)
            {
                this.xTransparentOld = p_172996_;
                this.yTransparentOld = p_172997_;
                this.zTransparentOld = p_172998_;
                int j = 0;
                this.chunksToResortTransparency.clear();

                for (LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo : this.renderChunks)
                {
                    if (j < 15 && levelrenderer$renderchunkinfo.chunk.getCompiledChunk().isLayerStarted(p_172994_))
                    {
                        this.chunksToResortTransparency.add(levelrenderer$renderchunkinfo.chunk);
                        ++j;
                    }
                }
            }

            this.minecraft.getProfiler().pop();
        }

        this.minecraft.getProfiler().push("filterempty");
        this.minecraft.getProfiler().popPush(() ->
        {
            return "render_" + p_172994_;
        });
        boolean flag1 = p_172994_ != RenderType.translucent();
        ObjectListIterator<LevelRenderer.RenderChunkInfo> objectlistiterator = this.renderChunks.listIterator(flag1 ? 0 : this.renderChunks.size());
        VertexFormat vertexformat = p_172994_.format();
        ShaderInstance shaderinstance = RenderSystem.getShader();
        BufferUploader.reset();

        for (int k = 0; k < 12; ++k)
        {
            int i = RenderSystem.getShaderTexture(k);
            shaderinstance.setSampler(k, i);
        }

        if (shaderinstance.MODEL_VIEW_MATRIX != null)
        {
            shaderinstance.MODEL_VIEW_MATRIX.set(p_172995_.last().pose());
        }

        if (shaderinstance.PROJECTION_MATRIX != null)
        {
            shaderinstance.PROJECTION_MATRIX.set(p_172999_);
        }

        if (shaderinstance.COLOR_MODULATOR != null)
        {
            shaderinstance.COLOR_MODULATOR.m_5941_(RenderSystem.getShaderColor());
        }

        if (shaderinstance.FOG_START != null)
        {
            shaderinstance.FOG_START.set(RenderSystem.getShaderFogStart());
        }

        if (shaderinstance.FOG_END != null)
        {
            shaderinstance.FOG_END.set(RenderSystem.getShaderFogEnd());
        }

        if (shaderinstance.FOG_COLOR != null)
        {
            shaderinstance.FOG_COLOR.m_5941_(RenderSystem.getShaderFogColor());
        }

        if (shaderinstance.TEXTURE_MATRIX != null)
        {
            shaderinstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
        }

        if (shaderinstance.GAME_TIME != null)
        {
            shaderinstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
        }

        RenderSystem.setupShaderLights(shaderinstance);
        shaderinstance.apply();
        Uniform uniform = shaderinstance.CHUNK_OFFSET;

        if (flag)
        {
            ShadersRender.preRenderChunkLayer(p_172994_);
            Shaders.setModelViewMatrix(p_172995_.last().pose());
            Shaders.setProjectionMatrix(p_172999_);
            Shaders.setTextureMatrix(RenderSystem.getTextureMatrix());
            Shaders.setColorModulator(RenderSystem.getShaderColor());
        }

        boolean flag2 = SmartAnimations.isActive();

        if (flag && Shaders.activeProgramID > 0)
        {
            uniform = null;
        }

        boolean flag3 = false;

        if (Config.isRenderRegions() && !p_172994_.isNeedsSorting())
        {
            int l = Integer.MIN_VALUE;
            int i1 = Integer.MIN_VALUE;
            VboRegion vboregion2 = null;
            Map<PairInt, Map<VboRegion, List<VertexBuffer>>> map = this.mapRegionLayers.computeIfAbsent(p_172994_, (kx) ->
            {
                return new LinkedHashMap(16);
            });
            Map<VboRegion, List<VertexBuffer>> map1 = null;
            List<VertexBuffer> list = null;

            while (true)
            {
                if (flag1)
                {
                    if (!objectlistiterator.hasNext())
                    {
                        break;
                    }
                }
                else if (!objectlistiterator.hasPrevious())
                {
                    break;
                }

                LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo1 = flag1 ? objectlistiterator.next() : objectlistiterator.previous();
                ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = levelrenderer$renderchunkinfo1.chunk;

                if (!chunkrenderdispatcher$renderchunk.getCompiledChunk().isEmpty(p_172994_))
                {
                    VertexBuffer vertexbuffer1 = chunkrenderdispatcher$renderchunk.getBuffer(p_172994_);
                    VboRegion vboregion = vertexbuffer1.getVboRegion();

                    if (chunkrenderdispatcher$renderchunk.regionX != l || chunkrenderdispatcher$renderchunk.regionZ != i1)
                    {
                        PairInt pairint = PairInt.of(chunkrenderdispatcher$renderchunk.regionX, chunkrenderdispatcher$renderchunk.regionZ);
                        map1 = map.computeIfAbsent(pairint, (kx) ->
                        {
                            return new LinkedHashMap(8);
                        });
                        l = chunkrenderdispatcher$renderchunk.regionX;
                        i1 = chunkrenderdispatcher$renderchunk.regionZ;
                        vboregion2 = null;
                    }

                    if (vboregion != vboregion2)
                    {
                        list = map1.computeIfAbsent(vboregion, (kx) ->
                        {
                            return new ArrayList();
                        });
                        vboregion2 = vboregion;
                    }

                    list.add(vertexbuffer1);

                    if (flag2)
                    {
                        BitSet bitset1 = chunkrenderdispatcher$renderchunk.getCompiledChunk().getAnimatedSprites(p_172994_);

                        if (bitset1 != null)
                        {
                            SmartAnimations.spritesRendered(bitset1);
                        }
                    }
                }
            }

            for (java.util.Map.Entry<PairInt, Map<VboRegion, List<VertexBuffer>>> entry : map.entrySet())
            {
                PairInt pairint1 = entry.getKey();
                Map<VboRegion, List<VertexBuffer>> map2 = entry.getValue();

                for (java.util.Map.Entry<VboRegion, List<VertexBuffer>> entry1 : map2.entrySet())
                {
                    VboRegion vboregion1 = entry1.getKey();
                    List<VertexBuffer> list1 = entry1.getValue();

                    if (!list1.isEmpty())
                    {
                        for (VertexBuffer vertexbuffer2 : list1)
                        {
                            vertexbuffer2.draw();
                        }

                        this.drawRegion(pairint1.getLeft(), 0, pairint1.getRight(), p_172996_, p_172997_, p_172998_, vboregion1, uniform, flag);
                        list1.clear();
                        flag3 = true;
                    }
                }
            }
        }
        else
        {
            while (true)
            {
                if (flag1)
                {
                    if (!objectlistiterator.hasNext())
                    {
                        break;
                    }
                }
                else if (!objectlistiterator.hasPrevious())
                {
                    break;
                }

                LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo2 = flag1 ? objectlistiterator.next() : objectlistiterator.previous();
                ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk1 = levelrenderer$renderchunkinfo2.chunk;

                if (!chunkrenderdispatcher$renderchunk1.getCompiledChunk().isEmpty(p_172994_))
                {
                    VertexBuffer vertexbuffer = chunkrenderdispatcher$renderchunk1.getBuffer(p_172994_);
                    BlockPos blockpos = chunkrenderdispatcher$renderchunk1.getOrigin();

                    if (uniform != null)
                    {
                        uniform.set((float)((double)blockpos.getX() - p_172996_ - (double)chunkrenderdispatcher$renderchunk1.regionDX), (float)((double)blockpos.getY() - p_172997_ - (double)chunkrenderdispatcher$renderchunk1.regionDY), (float)((double)blockpos.getZ() - p_172998_ - (double)chunkrenderdispatcher$renderchunk1.regionDZ));
                        uniform.upload();
                    }

                    if (flag)
                    {
                        Shaders.uniform_chunkOffset.setValue((float)((double)blockpos.getX() - p_172996_ - (double)chunkrenderdispatcher$renderchunk1.regionDX), (float)((double)blockpos.getY() - p_172997_ - (double)chunkrenderdispatcher$renderchunk1.regionDY), (float)((double)blockpos.getZ() - p_172998_ - (double)chunkrenderdispatcher$renderchunk1.regionDZ));
                    }

                    if (flag2)
                    {
                        BitSet bitset = chunkrenderdispatcher$renderchunk1.getCompiledChunk().getAnimatedSprites(p_172994_);

                        if (bitset != null)
                        {
                            SmartAnimations.spritesRendered(bitset);
                        }
                    }

                    vertexbuffer.drawChunkLayer();
                    flag3 = true;
                }
            }
        }

        if (Config.isMultiTexture())
        {
            this.minecraft.getTextureManager().bindForSetup(TextureAtlas.LOCATION_BLOCKS);
        }

        if (uniform != null)
        {
            uniform.set(Vector3f.ZERO);
        }

        if (flag)
        {
            Shaders.uniform_chunkOffset.setValue(0.0F, 0.0F, 0.0F);
        }

        shaderinstance.clear();

        if (flag3)
        {
            vertexformat.clearBufferState();
        }

        VertexBuffer.unbind();
        VertexBuffer.unbindVertexArray();
        this.minecraft.getProfiler().pop();

        if (flag)
        {
            ShadersRender.postRenderChunkLayer(p_172994_);
        }

        p_172994_.clearRenderState();
    }

    private void drawRegion(int regionX, int regionY, int regionZ, double xIn, double yIn, double zIn, VboRegion vboRegion, Uniform uniform, boolean isShaders)
    {
        if (uniform != null)
        {
            uniform.set((float)((double)regionX - xIn), (float)((double)regionY - yIn), (float)((double)regionZ - zIn));
            uniform.upload();
        }

        if (isShaders)
        {
            Shaders.uniform_chunkOffset.setValue((float)((double)regionX - xIn), (float)((double)regionY - yIn), (float)((double)regionZ - zIn));
        }

        vboRegion.finishDraw();
    }

    private void renderDebug(Camera pActiveRenderInfo)
    {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        if (this.minecraft.chunkPath || this.minecraft.chunkVisibility)
        {
            double d0 = pActiveRenderInfo.getPosition().x();
            double d1 = pActiveRenderInfo.getPosition().y();
            double d2 = pActiveRenderInfo.getPosition().z();
            RenderSystem.depthMask(true);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableTexture();

            for (LevelRenderer.RenderChunkInfo levelrenderer$renderchunkinfo : this.renderChunks)
            {
                ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = levelrenderer$renderchunkinfo.chunk;
                BlockPos blockpos = chunkrenderdispatcher$renderchunk.getOrigin();
                PoseStack posestack = RenderSystem.getModelViewStack();
                posestack.pushPose();
                posestack.translate((double)blockpos.getX() - d0, (double)blockpos.getY() - d1, (double)blockpos.getZ() - d2);
                RenderSystem.applyModelViewMatrix();

                if (this.minecraft.chunkPath)
                {
                    bufferbuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
                    RenderSystem.lineWidth(10.0F);
                    int i = levelrenderer$renderchunkinfo.step == 0 ? 0 : Mth.hsvToRgb((float)levelrenderer$renderchunkinfo.step / 50.0F, 0.9F, 0.9F);
                    int j = i >> 16 & 255;
                    int k = i >> 8 & 255;
                    int l = i & 255;

                    for (int i1 = 0; i1 < DIRECTIONS.length; ++i1)
                    {
                        if (levelrenderer$renderchunkinfo.hasSourceDirection(i1))
                        {
                            Direction direction = DIRECTIONS[i1];
                            bufferbuilder.vertex(8.0D, 8.0D, 8.0D).color(j, k, l, 255).endVertex();
                            bufferbuilder.vertex((double)(8 - 16 * direction.getStepX()), (double)(8 - 16 * direction.getStepY()), (double)(8 - 16 * direction.getStepZ())).color(j, k, l, 255).endVertex();
                        }
                    }

                    tesselator.end();
                    RenderSystem.lineWidth(1.0F);
                }

                if (this.minecraft.chunkVisibility && !chunkrenderdispatcher$renderchunk.getCompiledChunk().hasNoRenderableLayers())
                {
                    bufferbuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
                    RenderSystem.lineWidth(10.0F);
                    int j1 = 0;

                    for (Direction direction2 : DIRECTIONS)
                    {
                        for (Direction direction1 : DIRECTIONS)
                        {
                            boolean flag = chunkrenderdispatcher$renderchunk.getCompiledChunk().facesCanSeeEachother(direction2, direction1);

                            if (!flag)
                            {
                                ++j1;
                                bufferbuilder.vertex((double)(8 + 8 * direction2.getStepX()), (double)(8 + 8 * direction2.getStepY()), (double)(8 + 8 * direction2.getStepZ())).color(1, 0, 0, 1).endVertex();
                                bufferbuilder.vertex((double)(8 + 8 * direction1.getStepX()), (double)(8 + 8 * direction1.getStepY()), (double)(8 + 8 * direction1.getStepZ())).color(1, 0, 0, 1).endVertex();
                            }
                        }
                    }

                    tesselator.end();
                    RenderSystem.lineWidth(1.0F);

                    if (j1 > 0)
                    {
                        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                        float f = 0.5F;
                        float f1 = 0.2F;
                        bufferbuilder.vertex(0.5D, 15.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 15.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 15.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(0.5D, 15.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(0.5D, 0.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 0.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 0.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(0.5D, 0.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(0.5D, 15.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(0.5D, 15.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(0.5D, 0.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(0.5D, 0.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 0.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 0.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 15.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 15.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(0.5D, 0.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 0.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 15.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(0.5D, 15.5D, 0.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(0.5D, 15.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 15.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(15.5D, 0.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        bufferbuilder.vertex(0.5D, 0.5D, 15.5D).color(0.9F, 0.9F, 0.0F, 0.2F).endVertex();
                        tesselator.end();
                    }
                }

                posestack.popPose();
                RenderSystem.applyModelViewMatrix();
            }

            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableTexture();
        }

        if (this.capturedFrustum != null)
        {
            RenderSystem.disableCull();
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.lineWidth(10.0F);
            PoseStack posestack1 = RenderSystem.getModelViewStack();
            posestack1.pushPose();
            posestack1.translate((double)((float)(this.frustumPos.x - pActiveRenderInfo.getPosition().x)), (double)((float)(this.frustumPos.y - pActiveRenderInfo.getPosition().y)), (double)((float)(this.frustumPos.z - pActiveRenderInfo.getPosition().z)));
            RenderSystem.applyModelViewMatrix();
            RenderSystem.depthMask(true);
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            this.addFrustumQuad(bufferbuilder, 0, 1, 2, 3, 0, 1, 1);
            this.addFrustumQuad(bufferbuilder, 4, 5, 6, 7, 1, 0, 0);
            this.addFrustumQuad(bufferbuilder, 0, 1, 5, 4, 1, 1, 0);
            this.addFrustumQuad(bufferbuilder, 2, 3, 7, 6, 0, 0, 1);
            this.addFrustumQuad(bufferbuilder, 0, 4, 7, 3, 0, 1, 0);
            this.addFrustumQuad(bufferbuilder, 1, 5, 6, 2, 1, 0, 1);
            tesselator.end();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getPositionShader);
            bufferbuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            this.addFrustumVertex(bufferbuilder, 0);
            this.addFrustumVertex(bufferbuilder, 1);
            this.addFrustumVertex(bufferbuilder, 1);
            this.addFrustumVertex(bufferbuilder, 2);
            this.addFrustumVertex(bufferbuilder, 2);
            this.addFrustumVertex(bufferbuilder, 3);
            this.addFrustumVertex(bufferbuilder, 3);
            this.addFrustumVertex(bufferbuilder, 0);
            this.addFrustumVertex(bufferbuilder, 4);
            this.addFrustumVertex(bufferbuilder, 5);
            this.addFrustumVertex(bufferbuilder, 5);
            this.addFrustumVertex(bufferbuilder, 6);
            this.addFrustumVertex(bufferbuilder, 6);
            this.addFrustumVertex(bufferbuilder, 7);
            this.addFrustumVertex(bufferbuilder, 7);
            this.addFrustumVertex(bufferbuilder, 4);
            this.addFrustumVertex(bufferbuilder, 0);
            this.addFrustumVertex(bufferbuilder, 4);
            this.addFrustumVertex(bufferbuilder, 1);
            this.addFrustumVertex(bufferbuilder, 5);
            this.addFrustumVertex(bufferbuilder, 2);
            this.addFrustumVertex(bufferbuilder, 6);
            this.addFrustumVertex(bufferbuilder, 3);
            this.addFrustumVertex(bufferbuilder, 7);
            tesselator.end();
            posestack1.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableTexture();
            RenderSystem.lineWidth(1.0F);
        }
    }

    private void addFrustumVertex(VertexConsumer pBuffer, int pVertex)
    {
        pBuffer.vertex((double)this.frustumPoints[pVertex].x(), (double)this.frustumPoints[pVertex].y(), (double)this.frustumPoints[pVertex].z()).endVertex();
    }

    private void addFrustumQuad(VertexConsumer pBuffer, int pVertex1, int pVertex2, int pVertex3, int pVertex4, int pRed, int pGreen, int pBlue)
    {
        float f = 0.25F;
        pBuffer.vertex((double)this.frustumPoints[pVertex1].x(), (double)this.frustumPoints[pVertex1].y(), (double)this.frustumPoints[pVertex1].z()).color((float)pRed, (float)pGreen, (float)pBlue, 0.25F).endVertex();
        pBuffer.vertex((double)this.frustumPoints[pVertex2].x(), (double)this.frustumPoints[pVertex2].y(), (double)this.frustumPoints[pVertex2].z()).color((float)pRed, (float)pGreen, (float)pBlue, 0.25F).endVertex();
        pBuffer.vertex((double)this.frustumPoints[pVertex3].x(), (double)this.frustumPoints[pVertex3].y(), (double)this.frustumPoints[pVertex3].z()).color((float)pRed, (float)pGreen, (float)pBlue, 0.25F).endVertex();
        pBuffer.vertex((double)this.frustumPoints[pVertex4].x(), (double)this.frustumPoints[pVertex4].y(), (double)this.frustumPoints[pVertex4].z()).color((float)pRed, (float)pGreen, (float)pBlue, 0.25F).endVertex();
    }

    public void captureFrustum()
    {
        this.captureFrustum = true;
    }

    public void killFrustum()
    {
        this.capturedFrustum = null;
    }

    public void tick()
    {
        ++this.ticks;

        if (this.ticks % 20 == 0)
        {
            Iterator<BlockDestructionProgress> iterator = this.destroyingBlocks.values().iterator();

            while (iterator.hasNext())
            {
                BlockDestructionProgress blockdestructionprogress = iterator.next();
                int i = blockdestructionprogress.getUpdatedRenderTick();

                if (this.ticks - i > 400)
                {
                    iterator.remove();
                    this.removeProgress(blockdestructionprogress);
                }
            }
        }

        if (Config.isRenderRegions() && this.ticks % 20 == 0)
        {
            this.mapRegionLayers.clear();
        }
    }

    private void removeProgress(BlockDestructionProgress pProgress)
    {
        long i = pProgress.getPos().asLong();
        Set<BlockDestructionProgress> set = this.destructionProgress.get(i);
        set.remove(pProgress);

        if (set.isEmpty())
        {
            this.destructionProgress.remove(i);
        }
    }

    private void renderEndSky(PoseStack pMatrixStack)
    {
        if (Config.isSkyEnabled())
        {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, END_SKY_LOCATION);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();

            for (int i = 0; i < 6; ++i)
            {
                pMatrixStack.pushPose();

                if (i == 1)
                {
                    pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
                }

                if (i == 2)
                {
                    pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
                }

                if (i == 3)
                {
                    pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
                }

                if (i == 4)
                {
                    pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
                }

                if (i == 5)
                {
                    pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(-90.0F));
                }

                Matrix4f matrix4f = pMatrixStack.last().pose();
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                int j = 40;
                int k = 40;
                int l = 40;

                if (Config.isCustomColors())
                {
                    Vec3 vec3 = new Vec3((double)j / 255.0D, (double)k / 255.0D, (double)l / 255.0D);
                    vec3 = CustomColors.getWorldSkyColor(vec3, this.level, this.minecraft.getCameraEntity(), 0.0F);
                    j = (int)(vec3.x * 255.0D);
                    k = (int)(vec3.y * 255.0D);
                    l = (int)(vec3.z * 255.0D);
                }

                bufferbuilder.vertex(matrix4f, -100.0F, -100.0F, -100.0F).uv(0.0F, 0.0F).color(j, k, l, 255).endVertex();
                bufferbuilder.vertex(matrix4f, -100.0F, -100.0F, 100.0F).uv(0.0F, 16.0F).color(j, k, l, 255).endVertex();
                bufferbuilder.vertex(matrix4f, 100.0F, -100.0F, 100.0F).uv(16.0F, 16.0F).color(j, k, l, 255).endVertex();
                bufferbuilder.vertex(matrix4f, 100.0F, -100.0F, -100.0F).uv(16.0F, 0.0F).color(j, k, l, 255).endVertex();
                tesselator.end();
                pMatrixStack.popPose();
            }

            RenderSystem.depthMask(true);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
            CustomSky.renderSky(this.level, this.textureManager, pMatrixStack, 0.0F);
        }
    }

    public void renderSky(PoseStack p_181410_, Matrix4f p_181411_, float p_181412_, Runnable p_181413_)
    {
        p_181413_.run();

        if (Reflector.ForgeDimensionRenderInfo_getSkyRenderHandler.exists())
        {
            ISkyRenderHandler iskyrenderhandler = (ISkyRenderHandler)Reflector.call(this.level.effects(), Reflector.ForgeDimensionRenderInfo_getSkyRenderHandler);

            if (iskyrenderhandler != null)
            {
                iskyrenderhandler.render(this.ticks, p_181412_, p_181410_, this.level, this.minecraft);
                return;
            }
        }

        if (this.minecraft.level.effects().skyType() == DimensionSpecialEffects.SkyType.END)
        {
            this.renderEndSky(p_181410_);
        }
        else if (this.minecraft.level.effects().skyType() == DimensionSpecialEffects.SkyType.NORMAL)
        {
            RenderSystem.disableTexture();
            boolean flag = Config.isShaders();

            if (flag)
            {
                Shaders.disableTexture2D();
            }

            Vec3 vec3 = this.level.getSkyColor(this.minecraft.gameRenderer.getMainCamera().getPosition(), p_181412_);
            vec3 = CustomColors.getSkyColor(vec3, this.minecraft.level, this.minecraft.getCameraEntity().getX(), this.minecraft.getCameraEntity().getY() + 1.0D, this.minecraft.getCameraEntity().getZ());

            if (flag)
            {
                Shaders.setSkyColor(vec3);
                RenderSystem.setColorToAttribute(true);
            }

            float f = (float)vec3.x;
            float f1 = (float)vec3.y;
            float f2 = (float)vec3.z;
            FogRenderer.levelFogColor();
            BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
            RenderSystem.depthMask(false);

            if (flag)
            {
                Shaders.enableFog();
            }

            RenderSystem.setShaderColor(f, f1, f2, 1.0F);

            if (flag)
            {
                Shaders.preSkyList(p_181410_);
            }

            ShaderInstance shaderinstance = RenderSystem.getShader();

            if (Config.isSkyEnabled())
            {
                this.skyBuffer.drawWithShader(p_181410_.last().pose(), p_181411_, shaderinstance);
            }

            if (flag)
            {
                Shaders.disableFog();
            }

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            float[] afloat = this.level.effects().getSunriseColor(this.level.getTimeOfDay(p_181412_), p_181412_);

            if (afloat != null && Config.isSunMoonEnabled())
            {
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                RenderSystem.disableTexture();

                if (flag)
                {
                    Shaders.disableTexture2D();
                }

                if (flag)
                {
                    Shaders.setRenderStage(RenderStage.SUNSET);
                }

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                p_181410_.pushPose();
                p_181410_.mulPose(Vector3f.XP.rotationDegrees(90.0F));
                float f3 = Mth.sin(this.level.getSunAngle(p_181412_)) < 0.0F ? 180.0F : 0.0F;
                p_181410_.mulPose(Vector3f.ZP.rotationDegrees(f3));
                p_181410_.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
                float f4 = afloat[0];
                float f5 = afloat[1];
                float f6 = afloat[2];
                Matrix4f matrix4f = p_181410_.last().pose();
                bufferbuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
                bufferbuilder.vertex(matrix4f, 0.0F, 100.0F, 0.0F).color(f4, f5, f6, afloat[3]).endVertex();
                int i = 16;

                for (int j = 0; j <= 16; ++j)
                {
                    float f7 = (float)j * ((float)Math.PI * 2F) / 16.0F;
                    float f8 = Mth.sin(f7);
                    float f9 = Mth.cos(f7);
                    bufferbuilder.vertex(matrix4f, f8 * 120.0F, f9 * 120.0F, -f9 * 40.0F * afloat[3]).color(afloat[0], afloat[1], afloat[2], 0.0F).endVertex();
                }

                bufferbuilder.end();
                BufferUploader.end(bufferbuilder);
                p_181410_.popPose();
            }

            RenderSystem.enableTexture();

            if (flag)
            {
                Shaders.enableTexture2D();
            }

            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            p_181410_.pushPose();
            float f10 = 1.0F - this.level.getRainLevel(p_181412_);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f10);
            p_181410_.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
            CustomSky.renderSky(this.level, this.textureManager, p_181410_, p_181412_);

            if (flag)
            {
                Shaders.preCelestialRotate(p_181410_);
            }

            p_181410_.mulPose(Vector3f.XP.rotationDegrees(this.level.getTimeOfDay(p_181412_) * 360.0F));

            if (flag)
            {
                Shaders.postCelestialRotate(p_181410_);
            }

            Matrix4f matrix4f1 = p_181410_.last().pose();
            float f11 = 30.0F;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);

            if (Config.isSunTexture())
            {
                if (flag)
                {
                    Shaders.setRenderStage(RenderStage.SUN);
                }

                RenderSystem.setShaderTexture(0, SUN_LOCATION);
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                bufferbuilder.vertex(matrix4f1, -f11, 100.0F, -f11).uv(0.0F, 0.0F).endVertex();
                bufferbuilder.vertex(matrix4f1, f11, 100.0F, -f11).uv(1.0F, 0.0F).endVertex();
                bufferbuilder.vertex(matrix4f1, f11, 100.0F, f11).uv(1.0F, 1.0F).endVertex();
                bufferbuilder.vertex(matrix4f1, -f11, 100.0F, f11).uv(0.0F, 1.0F).endVertex();
                bufferbuilder.end();
                BufferUploader.end(bufferbuilder);
            }

            f11 = 20.0F;

            if (Config.isMoonTexture())
            {
                if (flag)
                {
                    Shaders.setRenderStage(RenderStage.MOON);
                }

                RenderSystem.setShaderTexture(0, MOON_LOCATION);
                int k = this.level.getMoonPhase();
                int l = k % 4;
                int i1 = k / 4 % 2;
                float f13 = (float)(l + 0) / 4.0F;
                float f14 = (float)(i1 + 0) / 2.0F;
                float f15 = (float)(l + 1) / 4.0F;
                float f16 = (float)(i1 + 1) / 2.0F;
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                bufferbuilder.vertex(matrix4f1, -f11, -100.0F, f11).uv(f15, f16).endVertex();
                bufferbuilder.vertex(matrix4f1, f11, -100.0F, f11).uv(f13, f16).endVertex();
                bufferbuilder.vertex(matrix4f1, f11, -100.0F, -f11).uv(f13, f14).endVertex();
                bufferbuilder.vertex(matrix4f1, -f11, -100.0F, -f11).uv(f15, f14).endVertex();
                bufferbuilder.end();
                BufferUploader.end(bufferbuilder);
            }

            RenderSystem.disableTexture();

            if (flag)
            {
                Shaders.disableTexture2D();
            }

            float f12 = this.level.getStarBrightness(p_181412_) * f10;

            if (f12 > 0.0F && Config.isStarsEnabled() && !CustomSky.hasSkyLayers(this.level))
            {
                if (flag)
                {
                    Shaders.setRenderStage(RenderStage.STARS);
                }

                RenderSystem.setShaderColor(f12, f12, f12, f12);
                FogRenderer.setupNoFog();
                this.starBuffer.drawWithShader(p_181410_.last().pose(), p_181411_, GameRenderer.getPositionShader());
                p_181413_.run();
            }

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();

            if (flag)
            {
                Shaders.enableFog();
            }

            p_181410_.popPose();
            RenderSystem.disableTexture();

            if (flag)
            {
                Shaders.disableTexture2D();
            }

            RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
            double d0 = this.minecraft.player.getEyePosition(p_181412_).y - this.level.getLevelData().getHorizonHeight(this.level);
            boolean flag1 = false;

            if (d0 < 0.0D)
            {
                if (flag)
                {
                    Shaders.setRenderStage(RenderStage.VOID);
                }

                p_181410_.pushPose();
                p_181410_.translate(0.0D, 12.0D, 0.0D);
                this.darkBuffer.drawWithShader(p_181410_.last().pose(), p_181411_, shaderinstance);
                p_181410_.popPose();
                flag1 = true;
            }

            if (this.level.effects().hasGround())
            {
                RenderSystem.setShaderColor(f * 0.2F + 0.04F, f1 * 0.2F + 0.04F, f2 * 0.6F + 0.1F, 1.0F);
            }
            else
            {
                RenderSystem.setShaderColor(f, f1, f2, 1.0F);
            }

            if (flag)
            {
                RenderSystem.setColorToAttribute(false);
            }

            RenderSystem.enableTexture();
            RenderSystem.depthMask(true);
        }
    }

    public void renderClouds(PoseStack p_172955_, Matrix4f p_172956_, float p_172957_, double p_172958_, double p_172959_, double p_172960_)
    {
        if (!Config.isCloudsOff())
        {
            if (Reflector.ForgeDimensionRenderInfo_getCloudRenderHandler.exists())
            {
                ICloudRenderHandler icloudrenderhandler = (ICloudRenderHandler)Reflector.call(this.level.effects(), Reflector.ForgeDimensionRenderInfo_getCloudRenderHandler);

                if (icloudrenderhandler != null)
                {
                    icloudrenderhandler.render(this.ticks, p_172957_, p_172955_, this.level, this.minecraft, p_172958_, p_172959_, p_172960_);
                    return;
                }
            }

            float f5 = this.level.effects().getCloudHeight();

            if (!Float.isNaN(f5))
            {
                if (Config.isShaders())
                {
                    Shaders.beginClouds();
                }

                RenderSystem.disableCull();
                RenderSystem.enableBlend();
                RenderSystem.enableDepthTest();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                RenderSystem.depthMask(true);
                float f = 12.0F;
                float f1 = 4.0F;
                double d0 = 2.0E-4D;
                double d1 = (double)(((float)this.ticks + p_172957_) * 0.03F);
                double d2 = (p_172958_ + d1) / 12.0D;
                double d3 = (double)(f5 - (float)p_172959_ + 0.33F);
                d3 = d3 + this.minecraft.options.ofCloudsHeight * 128.0D;
                double d4 = p_172960_ / 12.0D + (double)0.33F;
                d2 = d2 - (double)(Mth.floor(d2 / 2048.0D) * 2048);
                d4 = d4 - (double)(Mth.floor(d4 / 2048.0D) * 2048);
                float f2 = (float)(d2 - (double)Mth.floor(d2));
                float f3 = (float)(d3 / 4.0D - (double)Mth.floor(d3 / 4.0D)) * 4.0F;
                float f4 = (float)(d4 - (double)Mth.floor(d4));
                Vec3 vec3 = this.level.getCloudColor(p_172957_);
                int i = (int)Math.floor(d2);
                int j = (int)Math.floor(d3 / 4.0D);
                int k = (int)Math.floor(d4);

                if (i != this.prevCloudX || j != this.prevCloudY || k != this.prevCloudZ || this.minecraft.options.getCloudsType() != this.prevCloudsType || this.prevCloudColor.distanceToSqr(vec3) > 2.0E-4D)
                {
                    this.prevCloudX = i;
                    this.prevCloudY = j;
                    this.prevCloudZ = k;
                    this.prevCloudColor = vec3;
                    this.prevCloudsType = this.minecraft.options.getCloudsType();
                    this.generateClouds = true;
                }

                if (this.generateClouds)
                {
                    this.generateClouds = false;
                    BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();

                    if (this.cloudBuffer != null)
                    {
                        this.cloudBuffer.close();
                    }

                    this.cloudBuffer = new VertexBuffer();
                    this.buildClouds(bufferbuilder, d2, d3, d4, vec3);
                    bufferbuilder.end();
                    this.cloudBuffer.upload(bufferbuilder);
                }

                RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
                RenderSystem.setShaderTexture(0, CLOUDS_LOCATION);
                FogRenderer.levelFogColor();
                p_172955_.pushPose();
                p_172955_.scale(12.0F, 1.0F, 12.0F);
                p_172955_.translate((double)(-f2), (double)f3, (double)(-f4));

                if (this.cloudBuffer != null)
                {
                    int i1 = this.prevCloudsType == CloudStatus.FANCY ? 0 : 1;

                    for (int l = i1; l < 2; ++l)
                    {
                        if (l == 0)
                        {
                            RenderSystem.colorMask(false, false, false, false);
                        }
                        else
                        {
                            RenderSystem.colorMask(true, true, true, true);
                        }

                        ShaderInstance shaderinstance = RenderSystem.getShader();
                        this.cloudBuffer.drawWithShader(p_172955_.last().pose(), p_172956_, shaderinstance);
                    }
                }

                p_172955_.popPose();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.enableCull();
                RenderSystem.disableBlend();

                if (Config.isShaders())
                {
                    Shaders.endClouds();
                }
            }
        }
    }

    private void buildClouds(BufferBuilder pBuffer, double pCloudsX, double p_109581_, double pCloudsY, Vec3 p_109583_)
    {
        float f = 4.0F;
        float f1 = 0.00390625F;
        int i = 8;
        int j = 4;
        float f2 = 9.765625E-4F;
        float f3 = (float)Mth.floor(pCloudsX) * 0.00390625F;
        float f4 = (float)Mth.floor(pCloudsY) * 0.00390625F;
        float f5 = (float)p_109583_.x;
        float f6 = (float)p_109583_.y;
        float f7 = (float)p_109583_.z;
        float f8 = f5 * 0.9F;
        float f9 = f6 * 0.9F;
        float f10 = f7 * 0.9F;
        float f11 = f5 * 0.7F;
        float f12 = f6 * 0.7F;
        float f13 = f7 * 0.7F;
        float f14 = f5 * 0.8F;
        float f15 = f6 * 0.8F;
        float f16 = f7 * 0.8F;
        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        pBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        float f17 = (float)Math.floor(p_109581_ / 4.0D) * 4.0F;

        if (Config.isCloudsFancy())
        {
            for (int k = -3; k <= 4; ++k)
            {
                for (int l = -3; l <= 4; ++l)
                {
                    float f18 = (float)(k * 8);
                    float f19 = (float)(l * 8);

                    if (f17 > -5.0F)
                    {
                        pBuffer.vertex((double)(f18 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + 8.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f11, f12, f13, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                        pBuffer.vertex((double)(f18 + 8.0F), (double)(f17 + 0.0F), (double)(f19 + 8.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f11, f12, f13, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                        pBuffer.vertex((double)(f18 + 8.0F), (double)(f17 + 0.0F), (double)(f19 + 0.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f11, f12, f13, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                        pBuffer.vertex((double)(f18 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + 0.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f11, f12, f13, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                    }

                    if (f17 <= 5.0F)
                    {
                        pBuffer.vertex((double)(f18 + 0.0F), (double)(f17 + 4.0F - 9.765625E-4F), (double)(f19 + 8.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                        pBuffer.vertex((double)(f18 + 8.0F), (double)(f17 + 4.0F - 9.765625E-4F), (double)(f19 + 8.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                        pBuffer.vertex((double)(f18 + 8.0F), (double)(f17 + 4.0F - 9.765625E-4F), (double)(f19 + 0.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                        pBuffer.vertex((double)(f18 + 0.0F), (double)(f17 + 4.0F - 9.765625E-4F), (double)(f19 + 0.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                    }

                    if (k > -1)
                    {
                        for (int i1 = 0; i1 < 8; ++i1)
                        {
                            pBuffer.vertex((double)(f18 + (float)i1 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + 8.0F)).uv((f18 + (float)i1 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            pBuffer.vertex((double)(f18 + (float)i1 + 0.0F), (double)(f17 + 4.0F), (double)(f19 + 8.0F)).uv((f18 + (float)i1 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            pBuffer.vertex((double)(f18 + (float)i1 + 0.0F), (double)(f17 + 4.0F), (double)(f19 + 0.0F)).uv((f18 + (float)i1 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            pBuffer.vertex((double)(f18 + (float)i1 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + 0.0F)).uv((f18 + (float)i1 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                        }
                    }

                    if (k <= 1)
                    {
                        for (int j2 = 0; j2 < 8; ++j2)
                        {
                            pBuffer.vertex((double)(f18 + (float)j2 + 1.0F - 9.765625E-4F), (double)(f17 + 0.0F), (double)(f19 + 8.0F)).uv((f18 + (float)j2 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                            pBuffer.vertex((double)(f18 + (float)j2 + 1.0F - 9.765625E-4F), (double)(f17 + 4.0F), (double)(f19 + 8.0F)).uv((f18 + (float)j2 + 0.5F) * 0.00390625F + f3, (f19 + 8.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                            pBuffer.vertex((double)(f18 + (float)j2 + 1.0F - 9.765625E-4F), (double)(f17 + 4.0F), (double)(f19 + 0.0F)).uv((f18 + (float)j2 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                            pBuffer.vertex((double)(f18 + (float)j2 + 1.0F - 9.765625E-4F), (double)(f17 + 0.0F), (double)(f19 + 0.0F)).uv((f18 + (float)j2 + 0.5F) * 0.00390625F + f3, (f19 + 0.0F) * 0.00390625F + f4).color(f8, f9, f10, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                        }
                    }

                    if (l > -1)
                    {
                        for (int k2 = 0; k2 < 8; ++k2)
                        {
                            pBuffer.vertex((double)(f18 + 0.0F), (double)(f17 + 4.0F), (double)(f19 + (float)k2 + 0.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float)k2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                            pBuffer.vertex((double)(f18 + 8.0F), (double)(f17 + 4.0F), (double)(f19 + (float)k2 + 0.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float)k2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                            pBuffer.vertex((double)(f18 + 8.0F), (double)(f17 + 0.0F), (double)(f19 + (float)k2 + 0.0F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float)k2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                            pBuffer.vertex((double)(f18 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + (float)k2 + 0.0F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float)k2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                        }
                    }

                    if (l <= 1)
                    {
                        for (int l2 = 0; l2 < 8; ++l2)
                        {
                            pBuffer.vertex((double)(f18 + 0.0F), (double)(f17 + 4.0F), (double)(f19 + (float)l2 + 1.0F - 9.765625E-4F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float)l2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                            pBuffer.vertex((double)(f18 + 8.0F), (double)(f17 + 4.0F), (double)(f19 + (float)l2 + 1.0F - 9.765625E-4F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float)l2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                            pBuffer.vertex((double)(f18 + 8.0F), (double)(f17 + 0.0F), (double)(f19 + (float)l2 + 1.0F - 9.765625E-4F)).uv((f18 + 8.0F) * 0.00390625F + f3, (f19 + (float)l2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                            pBuffer.vertex((double)(f18 + 0.0F), (double)(f17 + 0.0F), (double)(f19 + (float)l2 + 1.0F - 9.765625E-4F)).uv((f18 + 0.0F) * 0.00390625F + f3, (f19 + (float)l2 + 0.5F) * 0.00390625F + f4).color(f14, f15, f16, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                        }
                    }
                }
            }
        }
        else
        {
            int j1 = 1;
            int k1 = 32;

            for (int l1 = -32; l1 < 32; l1 += 32)
            {
                for (int i2 = -32; i2 < 32; i2 += 32)
                {
                    pBuffer.vertex((double)(l1 + 0), (double)f17, (double)(i2 + 32)).uv((float)(l1 + 0) * 0.00390625F + f3, (float)(i2 + 32) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                    pBuffer.vertex((double)(l1 + 32), (double)f17, (double)(i2 + 32)).uv((float)(l1 + 32) * 0.00390625F + f3, (float)(i2 + 32) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                    pBuffer.vertex((double)(l1 + 32), (double)f17, (double)(i2 + 0)).uv((float)(l1 + 32) * 0.00390625F + f3, (float)(i2 + 0) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                    pBuffer.vertex((double)(l1 + 0), (double)f17, (double)(i2 + 0)).uv((float)(l1 + 0) * 0.00390625F + f3, (float)(i2 + 0) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                }
            }
        }
    }

    public void compileChunksUntil(long pFinishTimeNano)
    {
        pFinishTimeNano = (long)((double)pFinishTimeNano + 1.0E8D);
        this.needsUpdate |= this.chunkRenderDispatcher.uploadAllPendingUploads();
        long i = Util.getNanos();
        int j = 0;

        if (this.chunksToUpdateForced.size() > 0)
        {
            Iterator iterator = this.chunksToUpdateForced.iterator();

            while (iterator.hasNext())
            {
                ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = (ChunkRenderDispatcher.RenderChunk)iterator.next();

                if (!this.chunkRenderDispatcher.updateChunkLater(chunkrenderdispatcher$renderchunk))
                {
                    break;
                }

                chunkrenderdispatcher$renderchunk.setNotDirty();
                iterator.remove();
                this.chunksToCompile.remove(chunkrenderdispatcher$renderchunk);
                this.chunksToResortTransparency.remove(chunkrenderdispatcher$renderchunk);
            }
        }

        if (this.chunksToResortTransparency.size() > 0)
        {
            Iterator iterator2 = this.chunksToResortTransparency.iterator();

            if (iterator2.hasNext())
            {
                ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk2 = (ChunkRenderDispatcher.RenderChunk)iterator2.next();

                if (this.chunkRenderDispatcher.updateTransparencyLater(chunkrenderdispatcher$renderchunk2))
                {
                    iterator2.remove();
                }
            }
        }

        double d1 = 0.0D;
        int k = Config.getUpdatesPerFrame();

        if (!this.chunksToCompile.isEmpty())
        {
            Iterator<ChunkRenderDispatcher.RenderChunk> iterator1 = this.chunksToCompile.iterator();

            while (iterator1.hasNext())
            {
                ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk1 = iterator1.next();
                boolean flag1 = chunkrenderdispatcher$renderchunk1.isChunkRegionEmpty();
                boolean flag;

                if (!chunkrenderdispatcher$renderchunk1.isDirtyFromPlayer() && !flag1)
                {
                    flag = this.chunkRenderDispatcher.updateChunkLater(chunkrenderdispatcher$renderchunk1);
                }
                else
                {
                    flag = this.chunkRenderDispatcher.updateChunkNow(chunkrenderdispatcher$renderchunk1);
                }

                if (!flag)
                {
                    break;
                }

                chunkrenderdispatcher$renderchunk1.setNotDirty();
                iterator1.remove();

                if (!flag1)
                {
                    double d0 = 2.0D * RenderChunkUtils.getRelativeBufferSize(chunkrenderdispatcher$renderchunk1);
                    d1 += d0;

                    if (d1 > (double)k)
                    {
                        break;
                    }
                }
            }
        }
    }

    private void renderWorldBorder(Camera p_173013_)
    {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        WorldBorder worldborder = this.level.getWorldBorder();
        double d0 = (double)(this.minecraft.options.renderDistance * 16);

        if (!(p_173013_.getPosition().x < worldborder.getMaxX() - d0) || !(p_173013_.getPosition().x > worldborder.getMinX() + d0) || !(p_173013_.getPosition().z < worldborder.getMaxZ() - d0) || !(p_173013_.getPosition().z > worldborder.getMinZ() + d0))
        {
            if (Config.isShaders())
            {
                Shaders.pushProgram();
                Shaders.useProgram(Shaders.ProgramTexturedLit);
                Shaders.setRenderStage(RenderStage.WORLD_BORDER);
            }

            double d1 = 1.0D - worldborder.getDistanceToBorder(p_173013_.getPosition().x, p_173013_.getPosition().z) / d0;
            d1 = Math.pow(d1, 4.0D);
            d1 = Mth.clamp(d1, 0.0D, 1.0D);
            double d2 = p_173013_.getPosition().x;
            double d3 = p_173013_.getPosition().z;
            double d4 = (double)this.minecraft.gameRenderer.getDepthFar();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderSystem.setShaderTexture(0, FORCEFIELD_LOCATION);
            RenderSystem.depthMask(Minecraft.useShaderTransparency());
            PoseStack posestack = RenderSystem.getModelViewStack();
            posestack.pushPose();
            RenderSystem.applyModelViewMatrix();
            int i = worldborder.getStatus().getColor();
            float f = (float)(i >> 16 & 255) / 255.0F;
            float f1 = (float)(i >> 8 & 255) / 255.0F;
            float f2 = (float)(i & 255) / 255.0F;
            RenderSystem.setShaderColor(f, f1, f2, (float)d1);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.polygonOffset(-3.0F, -3.0F);
            RenderSystem.enablePolygonOffset();
            RenderSystem.disableCull();
            float f3 = (float)(Util.getMillis() % 3000L) / 3000.0F;
            float f4 = 0.0F;
            float f5 = 0.0F;
            float f6 = (float)(d4 - Mth.frac(p_173013_.getPosition().y));
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            double d5 = Math.max((double)Mth.floor(d3 - d0), worldborder.getMinZ());
            double d6 = Math.min((double)Mth.ceil(d3 + d0), worldborder.getMaxZ());

            if (d2 > worldborder.getMaxX() - d0)
            {
                float f7 = 0.0F;

                for (double d7 = d5; d7 < d6; f7 += 0.5F)
                {
                    double d8 = Math.min(1.0D, d6 - d7);
                    float f8 = (float)d8 * 0.5F;
                    bufferbuilder.vertex(worldborder.getMaxX() - d2, -d4, d7 - d3).uv(f3 - f7, f3 + f6).endVertex();
                    bufferbuilder.vertex(worldborder.getMaxX() - d2, -d4, d7 + d8 - d3).uv(f3 - (f8 + f7), f3 + f6).endVertex();
                    bufferbuilder.vertex(worldborder.getMaxX() - d2, d4, d7 + d8 - d3).uv(f3 - (f8 + f7), f3 + 0.0F).endVertex();
                    bufferbuilder.vertex(worldborder.getMaxX() - d2, d4, d7 - d3).uv(f3 - f7, f3 + 0.0F).endVertex();
                    ++d7;
                }
            }

            if (d2 < worldborder.getMinX() + d0)
            {
                float f9 = 0.0F;

                for (double d9 = d5; d9 < d6; f9 += 0.5F)
                {
                    double d12 = Math.min(1.0D, d6 - d9);
                    float f12 = (float)d12 * 0.5F;
                    bufferbuilder.vertex(worldborder.getMinX() - d2, -d4, d9 - d3).uv(f3 + f9, f3 + f6).endVertex();
                    bufferbuilder.vertex(worldborder.getMinX() - d2, -d4, d9 + d12 - d3).uv(f3 + f12 + f9, f3 + f6).endVertex();
                    bufferbuilder.vertex(worldborder.getMinX() - d2, d4, d9 + d12 - d3).uv(f3 + f12 + f9, f3 + 0.0F).endVertex();
                    bufferbuilder.vertex(worldborder.getMinX() - d2, d4, d9 - d3).uv(f3 + f9, f3 + 0.0F).endVertex();
                    ++d9;
                }
            }

            d5 = Math.max((double)Mth.floor(d2 - d0), worldborder.getMinX());
            d6 = Math.min((double)Mth.ceil(d2 + d0), worldborder.getMaxX());

            if (d3 > worldborder.getMaxZ() - d0)
            {
                float f10 = 0.0F;

                for (double d10 = d5; d10 < d6; f10 += 0.5F)
                {
                    double d13 = Math.min(1.0D, d6 - d10);
                    float f13 = (float)d13 * 0.5F;
                    bufferbuilder.vertex(d10 - d2, -d4, worldborder.getMaxZ() - d3).uv(f3 + f10, f3 + f6).endVertex();
                    bufferbuilder.vertex(d10 + d13 - d2, -d4, worldborder.getMaxZ() - d3).uv(f3 + f13 + f10, f3 + f6).endVertex();
                    bufferbuilder.vertex(d10 + d13 - d2, d4, worldborder.getMaxZ() - d3).uv(f3 + f13 + f10, f3 + 0.0F).endVertex();
                    bufferbuilder.vertex(d10 - d2, d4, worldborder.getMaxZ() - d3).uv(f3 + f10, f3 + 0.0F).endVertex();
                    ++d10;
                }
            }

            if (d3 < worldborder.getMinZ() + d0)
            {
                float f11 = 0.0F;

                for (double d11 = d5; d11 < d6; f11 += 0.5F)
                {
                    double d14 = Math.min(1.0D, d6 - d11);
                    float f14 = (float)d14 * 0.5F;
                    bufferbuilder.vertex(d11 - d2, -d4, worldborder.getMinZ() - d3).uv(f3 - f11, f3 + f6).endVertex();
                    bufferbuilder.vertex(d11 + d14 - d2, -d4, worldborder.getMinZ() - d3).uv(f3 - (f14 + f11), f3 + f6).endVertex();
                    bufferbuilder.vertex(d11 + d14 - d2, d4, worldborder.getMinZ() - d3).uv(f3 - (f14 + f11), f3 + 0.0F).endVertex();
                    bufferbuilder.vertex(d11 - d2, d4, worldborder.getMinZ() - d3).uv(f3 - f11, f3 + 0.0F).endVertex();
                    ++d11;
                }
            }

            bufferbuilder.end();
            BufferUploader.end(bufferbuilder);
            RenderSystem.enableCull();
            RenderSystem.polygonOffset(0.0F, 0.0F);
            RenderSystem.disablePolygonOffset();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderSystem.disableBlend();
            posestack.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.depthMask(true);

            if (Config.isShaders())
            {
                Shaders.popProgram();
                Shaders.setRenderStage(RenderStage.NONE);
            }
        }
    }

    private void renderHitOutline(PoseStack pMatrixStack, VertexConsumer pBuffer, Entity pEntity, double pX, double p_109642_, double pY, BlockPos p_109644_, BlockState pZ)
    {
        if (!Config.isCustomEntityModels() || !CustomEntityModels.isCustomModel(pZ))
        {
            renderShape(pMatrixStack, pBuffer, pZ.getShape(this.level, p_109644_, CollisionContext.of(pEntity)), (double)p_109644_.getX() - pX, (double)p_109644_.getY() - p_109642_, (double)p_109644_.getZ() - pY, 0.0F, 0.0F, 0.0F, 0.4F);
        }
    }

    public static void renderVoxelShape(PoseStack pMatrixStack, VertexConsumer pBuffer, VoxelShape pShape, double pX, double p_109659_, double pY, float p_109661_, float pZ, float p_109663_, float pRed)
    {
        List<AABB> list = pShape.toAabbs();
        int i = Mth.ceil((double)list.size() / 3.0D);

        for (int j = 0; j < list.size(); ++j)
        {
            AABB aabb = list.get(j);
            float f = ((float)j % (float)i + 1.0F) / (float)i;
            float f1 = (float)(j / i);
            float f2 = f * (float)(f1 == 0.0F ? 1 : 0);
            float f3 = f * (float)(f1 == 1.0F ? 1 : 0);
            float f4 = f * (float)(f1 == 2.0F ? 1 : 0);
            renderShape(pMatrixStack, pBuffer, Shapes.create(aabb.move(0.0D, 0.0D, 0.0D)), pX, p_109659_, pY, f2, f3, f4, 1.0F);
        }
    }

    private static void renderShape(PoseStack pMatrixStack, VertexConsumer pBuffer, VoxelShape pShape, double pX, double p_109787_, double pY, float p_109789_, float pZ, float p_109791_, float pRed)
    {
        PoseStack.Pose posestack$pose = pMatrixStack.last();
        pShape.forAllEdges((x1, y1, z1, x2, y2, z2) ->
        {
            float f = (float)(x2 - x1);
            float f1 = (float)(y2 - y1);
            float f2 = (float)(z2 - z1);
            float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
            f = f / f3;
            f1 = f1 / f3;
            f2 = f2 / f3;
            pBuffer.vertex(posestack$pose.pose(), (float)(x1 + pX), (float)(y1 + p_109787_), (float)(z1 + pY)).color(p_109789_, pZ, p_109791_, pRed).normal(posestack$pose.normal(), f, f1, f2).endVertex();
            pBuffer.vertex(posestack$pose.pose(), (float)(x2 + pX), (float)(y2 + p_109787_), (float)(z2 + pY)).color(p_109789_, pZ, p_109791_, pRed).normal(posestack$pose.normal(), f, f1, f2).endVertex();
        });
    }

    public static void renderLineBox(VertexConsumer pMatrixStack, double pBuffer, double pMinX, double p_172969_, double pMinY, double p_172971_, double pMinZ, float p_172973_, float pMaxX, float p_172975_, float pMaxY)
    {
        renderLineBox(new PoseStack(), pMatrixStack, pBuffer, pMinX, p_172969_, pMinY, p_172971_, pMinZ, p_172973_, pMaxX, p_172975_, pMaxY, p_172973_, pMaxX, p_172975_);
    }

    public static void renderLineBox(PoseStack pMatrixStack, VertexConsumer pBuffer, AABB pMinX, float p_109650_, float pMinY, float p_109652_, float pMinZ)
    {
        renderLineBox(pMatrixStack, pBuffer, pMinX.minX, pMinX.minY, pMinX.minZ, pMinX.maxX, pMinX.maxY, pMinX.maxZ, p_109650_, pMinY, p_109652_, pMinZ, p_109650_, pMinY, p_109652_);
    }

    public static void renderLineBox(PoseStack pMatrixStack, VertexConsumer pBuffer, double pMinX, double p_109612_, double pMinY, double p_109614_, double pMinZ, double p_109616_, float pMaxX, float p_109618_, float pMaxY, float p_109620_)
    {
        renderLineBox(pMatrixStack, pBuffer, pMinX, p_109612_, pMinY, p_109614_, pMinZ, p_109616_, pMaxX, p_109618_, pMaxY, p_109620_, pMaxX, p_109618_, pMaxY);
    }

    public static void renderLineBox(PoseStack pMatrixStack, VertexConsumer pBuffer, double pMinX, double p_109625_, double pMinY, double p_109627_, double pMinZ, double p_109629_, float pMaxX, float p_109631_, float pMaxY, float p_109633_, float pMaxZ, float p_109635_, float pRed)
    {
        Matrix4f matrix4f = pMatrixStack.last().pose();
        Matrix3f matrix3f = pMatrixStack.last().normal();
        float f = (float)pMinX;
        float f1 = (float)p_109625_;
        float f2 = (float)pMinY;
        float f3 = (float)p_109627_;
        float f4 = (float)pMinZ;
        float f5 = (float)p_109629_;
        pBuffer.vertex(matrix4f, f, f1, f2).color(pMaxX, p_109635_, pRed, p_109633_).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
        pBuffer.vertex(matrix4f, f3, f1, f2).color(pMaxX, p_109635_, pRed, p_109633_).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
        pBuffer.vertex(matrix4f, f, f1, f2).color(pMaxZ, p_109631_, pRed, p_109633_).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        pBuffer.vertex(matrix4f, f, f4, f2).color(pMaxZ, p_109631_, pRed, p_109633_).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        pBuffer.vertex(matrix4f, f, f1, f2).color(pMaxZ, p_109635_, pMaxY, p_109633_).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
        pBuffer.vertex(matrix4f, f, f1, f5).color(pMaxZ, p_109635_, pMaxY, p_109633_).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
        pBuffer.vertex(matrix4f, f3, f1, f2).color(pMaxX, p_109631_, pMaxY, p_109633_).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        pBuffer.vertex(matrix4f, f3, f4, f2).color(pMaxX, p_109631_, pMaxY, p_109633_).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        pBuffer.vertex(matrix4f, f3, f4, f2).color(pMaxX, p_109631_, pMaxY, p_109633_).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
        pBuffer.vertex(matrix4f, f, f4, f2).color(pMaxX, p_109631_, pMaxY, p_109633_).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
        pBuffer.vertex(matrix4f, f, f4, f2).color(pMaxX, p_109631_, pMaxY, p_109633_).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
        pBuffer.vertex(matrix4f, f, f4, f5).color(pMaxX, p_109631_, pMaxY, p_109633_).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
        pBuffer.vertex(matrix4f, f, f4, f5).color(pMaxX, p_109631_, pMaxY, p_109633_).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
        pBuffer.vertex(matrix4f, f, f1, f5).color(pMaxX, p_109631_, pMaxY, p_109633_).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
        pBuffer.vertex(matrix4f, f, f1, f5).color(pMaxX, p_109631_, pMaxY, p_109633_).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
        pBuffer.vertex(matrix4f, f3, f1, f5).color(pMaxX, p_109631_, pMaxY, p_109633_).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
        pBuffer.vertex(matrix4f, f3, f1, f5).color(pMaxX, p_109631_, pMaxY, p_109633_).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
        pBuffer.vertex(matrix4f, f3, f1, f2).color(pMaxX, p_109631_, pMaxY, p_109633_).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
        pBuffer.vertex(matrix4f, f, f4, f5).color(pMaxX, p_109631_, pMaxY, p_109633_).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
        pBuffer.vertex(matrix4f, f3, f4, f5).color(pMaxX, p_109631_, pMaxY, p_109633_).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
        pBuffer.vertex(matrix4f, f3, f1, f5).color(pMaxX, p_109631_, pMaxY, p_109633_).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        pBuffer.vertex(matrix4f, f3, f4, f5).color(pMaxX, p_109631_, pMaxY, p_109633_).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        pBuffer.vertex(matrix4f, f3, f4, f2).color(pMaxX, p_109631_, pMaxY, p_109633_).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
        pBuffer.vertex(matrix4f, f3, f4, f5).color(pMaxX, p_109631_, pMaxY, p_109633_).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
    }

    public static void addChainedFilledBoxVertices(BufferBuilder pBuilder, double pX1, double p_109559_, double pY1, double p_109561_, double pZ1, double p_109563_, float pX2, float p_109565_, float pY2, float p_109567_)
    {
        pBuilder.vertex(pX1, p_109559_, pY1).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(pX1, p_109559_, pY1).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(pX1, p_109559_, pY1).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(pX1, p_109559_, p_109563_).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(pX1, pZ1, pY1).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(pX1, pZ1, p_109563_).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(pX1, pZ1, p_109563_).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(pX1, p_109559_, p_109563_).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(p_109561_, pZ1, p_109563_).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(p_109561_, p_109559_, p_109563_).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(p_109561_, p_109559_, p_109563_).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(p_109561_, p_109559_, pY1).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(p_109561_, pZ1, p_109563_).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(p_109561_, pZ1, pY1).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(p_109561_, pZ1, pY1).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(p_109561_, p_109559_, pY1).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(pX1, pZ1, pY1).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(pX1, p_109559_, pY1).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(pX1, p_109559_, pY1).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(p_109561_, p_109559_, pY1).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(pX1, p_109559_, p_109563_).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(p_109561_, p_109559_, p_109563_).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(p_109561_, p_109559_, p_109563_).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(pX1, pZ1, pY1).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(pX1, pZ1, pY1).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(pX1, pZ1, p_109563_).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(p_109561_, pZ1, pY1).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(p_109561_, pZ1, p_109563_).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(p_109561_, pZ1, p_109563_).color(pX2, p_109565_, pY2, p_109567_).endVertex();
        pBuilder.vertex(p_109561_, pZ1, p_109563_).color(pX2, p_109565_, pY2, p_109567_).endVertex();
    }

    public void blockChanged(BlockGetter pLevel, BlockPos pPos, BlockState pOldState, BlockState pNewState, int pFlags)
    {
        this.setBlockDirty(pPos, (pFlags & 8) != 0);
    }

    private void setBlockDirty(BlockPos pBlockPos, boolean pOldState)
    {
        for (int i = pBlockPos.getZ() - 1; i <= pBlockPos.getZ() + 1; ++i)
        {
            for (int j = pBlockPos.getX() - 1; j <= pBlockPos.getX() + 1; ++j)
            {
                for (int k = pBlockPos.getY() - 1; k <= pBlockPos.getY() + 1; ++k)
                {
                    this.setSectionDirty(SectionPos.blockToSectionCoord(j), SectionPos.blockToSectionCoord(k), SectionPos.blockToSectionCoord(i), pOldState);
                }
            }
        }
    }

    public void setBlocksDirty(int pX1, int pY1, int pZ1, int pX2, int pY2, int pZ2)
    {
        for (int i = pZ1 - 1; i <= pZ2 + 1; ++i)
        {
            for (int j = pX1 - 1; j <= pX2 + 1; ++j)
            {
                for (int k = pY1 - 1; k <= pY2 + 1; ++k)
                {
                    this.setSectionDirty(SectionPos.blockToSectionCoord(j), SectionPos.blockToSectionCoord(k), SectionPos.blockToSectionCoord(i));
                }
            }
        }
    }

    public void setBlockDirty(BlockPos pBlockPos, BlockState pOldState, BlockState pNewState)
    {
        if (this.minecraft.getModelManager().requiresRender(pOldState, pNewState))
        {
            this.setBlocksDirty(pBlockPos.getX(), pBlockPos.getY(), pBlockPos.getZ(), pBlockPos.getX(), pBlockPos.getY(), pBlockPos.getZ());
        }
    }

    public void setSectionDirtyWithNeighbors(int pSectionX, int pSectionY, int pSectionZ)
    {
        for (int i = pSectionZ - 1; i <= pSectionZ + 1; ++i)
        {
            for (int j = pSectionX - 1; j <= pSectionX + 1; ++j)
            {
                for (int k = pSectionY - 1; k <= pSectionY + 1; ++k)
                {
                    this.setSectionDirty(j, k, i);
                }
            }
        }
    }

    public void setSectionDirty(int pSectionX, int pSectionY, int pSectionZ)
    {
        this.setSectionDirty(pSectionX, pSectionY, pSectionZ, false);
    }

    private void setSectionDirty(int pSectionX, int pSectionY, int pSectionZ, boolean p_109505_)
    {
        this.viewArea.setDirty(pSectionX, pSectionY, pSectionZ, p_109505_);
    }

    public void playStreamingMusic(@Nullable SoundEvent pSound, BlockPos pPos)
    {
        this.playStreamingMusic(pSound, pPos, pSound == null ? null : RecordItem.getBySound(pSound));
    }

    public void playStreamingMusic(@Nullable SoundEvent soundIn, BlockPos pos, @Nullable RecordItem musicDiscItem)
    {
        SoundInstance soundinstance = this.playingRecords.get(pos);

        if (soundinstance != null)
        {
            this.minecraft.getSoundManager().stop(soundinstance);
            this.playingRecords.remove(pos);
        }

        if (soundIn != null)
        {
            RecordItem recorditem = RecordItem.getBySound(soundIn);

            if (Reflector.MinecraftForgeClient.exists())
            {
                recorditem = musicDiscItem;
            }

            if (recorditem != null)
            {
                this.minecraft.gui.setNowPlaying(recorditem.getDisplayName());
            }

            SoundInstance soundinstance1 = SimpleSoundInstance.forRecord(soundIn, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
            this.playingRecords.put(pos, soundinstance1);
            this.minecraft.getSoundManager().play(soundinstance1);
        }

        this.notifyNearbyEntities(this.level, pos, soundIn != null);
    }

    private void notifyNearbyEntities(Level pLevel, BlockPos pPos, boolean pIsPartying)
    {
        for (LivingEntity livingentity : pLevel.getEntitiesOfClass(LivingEntity.class, (new AABB(pPos)).inflate(3.0D)))
        {
            livingentity.setRecordPlayingNearby(pPos, pIsPartying);
        }
    }

    public void addParticle(ParticleOptions pParticleData, boolean pX, double p_109746_, double pY, double p_109748_, double pZ, double p_109750_, double pXSpeed)
    {
        this.addParticle(pParticleData, pX, false, p_109746_, pY, p_109748_, pZ, p_109750_, pXSpeed);
    }

    public void addParticle(ParticleOptions pParticleData, boolean pX, boolean p_109755_, double pY, double p_109757_, double pZ, double p_109759_, double pXSpeed, double p_109761_)
    {
        try
        {
            this.addParticleInternal(pParticleData, pX, p_109755_, pY, p_109757_, pZ, p_109759_, pXSpeed, p_109761_);
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception while adding particle");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being added");
            crashreportcategory.setDetail("ID", Registry.PARTICLE_TYPE.getKey(pParticleData.getType()));
            crashreportcategory.setDetail("Parameters", pParticleData.writeToString());
            crashreportcategory.setDetail("Position", () ->
            {
                return CrashReportCategory.formatLocation(this.level, pY, p_109757_, pZ);
            });
            throw new ReportedException(crashreport);
        }
    }

    private <T extends ParticleOptions> void addParticle(T pParticleData, double pX, double p_109738_, double pY, double p_109740_, double pZ, double p_109742_)
    {
        this.addParticle(pParticleData, pParticleData.getType().getOverrideLimiter(), pX, p_109738_, pY, p_109740_, pZ, p_109742_);
    }

    @Nullable
    private Particle addParticleInternal(ParticleOptions pParticleData, boolean pAlwaysRender, double pX, double p_109799_, double pY, double p_109801_, double pZ, double p_109803_)
    {
        return this.addParticleInternal(pParticleData, pAlwaysRender, false, pX, p_109799_, pY, p_109801_, pZ, p_109803_);
    }

    @Nullable
    private Particle addParticleInternal(ParticleOptions pParticleData, boolean pAlwaysRender, boolean pX, double p_109808_, double pY, double p_109810_, double pZ, double p_109812_, double pXSpeed)
    {
        Camera camera = this.minecraft.gameRenderer.getMainCamera();

        if (this.minecraft != null && camera.isInitialized() && this.minecraft.particleEngine != null)
        {
            ParticleStatus particlestatus = this.calculateParticleLevel(pX);

            if (pParticleData == ParticleTypes.EXPLOSION_EMITTER && !Config.isAnimatedExplosion())
            {
                return null;
            }
            else if (pParticleData == ParticleTypes.EXPLOSION && !Config.isAnimatedExplosion())
            {
                return null;
            }
            else if (pParticleData == ParticleTypes.POOF && !Config.isAnimatedExplosion())
            {
                return null;
            }
            else if (pParticleData == ParticleTypes.UNDERWATER && !Config.isWaterParticles())
            {
                return null;
            }
            else if (pParticleData == ParticleTypes.SMOKE && !Config.isAnimatedSmoke())
            {
                return null;
            }
            else if (pParticleData == ParticleTypes.LARGE_SMOKE && !Config.isAnimatedSmoke())
            {
                return null;
            }
            else if (pParticleData == ParticleTypes.ENTITY_EFFECT && !Config.isPotionParticles())
            {
                return null;
            }
            else if (pParticleData == ParticleTypes.AMBIENT_ENTITY_EFFECT && !Config.isPotionParticles())
            {
                return null;
            }
            else if (pParticleData == ParticleTypes.EFFECT && !Config.isPotionParticles())
            {
                return null;
            }
            else if (pParticleData == ParticleTypes.INSTANT_EFFECT && !Config.isPotionParticles())
            {
                return null;
            }
            else if (pParticleData == ParticleTypes.WITCH && !Config.isPotionParticles())
            {
                return null;
            }
            else if (pParticleData == ParticleTypes.PORTAL && !Config.isPortalParticles())
            {
                return null;
            }
            else if (pParticleData == ParticleTypes.FLAME && !Config.isAnimatedFlame())
            {
                return null;
            }
            else if (pParticleData == ParticleTypes.SOUL_FIRE_FLAME && !Config.isAnimatedFlame())
            {
                return null;
            }
            else if (pParticleData == ParticleTypes.DUST && !Config.isAnimatedRedstone())
            {
                return null;
            }
            else if (pParticleData == ParticleTypes.DRIPPING_WATER && !Config.isDrippingWaterLava())
            {
                return null;
            }
            else if (pParticleData == ParticleTypes.DRIPPING_LAVA && !Config.isDrippingWaterLava())
            {
                return null;
            }
            else if (pParticleData == ParticleTypes.FIREWORK && !Config.isFireworkParticles())
            {
                return null;
            }
            else
            {
                if (!pAlwaysRender)
                {
                    double d0 = 1024.0D;

                    if (pParticleData == ParticleTypes.CRIT)
                    {
                        d0 = 38416.0D;
                    }

                    if (camera.getPosition().distanceToSqr(p_109808_, pY, p_109810_) > d0)
                    {
                        return null;
                    }

                    if (particlestatus == ParticleStatus.MINIMAL)
                    {
                        return null;
                    }
                }

                Particle particle = this.minecraft.particleEngine.createParticle(pParticleData, p_109808_, pY, p_109810_, pZ, p_109812_, pXSpeed);

                if (pParticleData == ParticleTypes.BUBBLE)
                {
                    CustomColors.updateWaterFX(particle, this.level, p_109808_, pY, p_109810_, this.renderEnv);
                }

                if (pParticleData == ParticleTypes.SPLASH)
                {
                    CustomColors.updateWaterFX(particle, this.level, p_109808_, pY, p_109810_, this.renderEnv);
                }

                if (pParticleData == ParticleTypes.RAIN)
                {
                    CustomColors.updateWaterFX(particle, this.level, p_109808_, pY, p_109810_, this.renderEnv);
                }

                if (pParticleData == ParticleTypes.MYCELIUM)
                {
                    CustomColors.updateMyceliumFX(particle);
                }

                if (pParticleData == ParticleTypes.PORTAL)
                {
                    CustomColors.updatePortalFX(particle);
                }

                if (pParticleData == ParticleTypes.DUST)
                {
                    CustomColors.updateReddustFX(particle, this.level, p_109808_, pY, p_109810_);
                }

                return particle;
            }
        }
        else
        {
            return null;
        }
    }

    private ParticleStatus calculateParticleLevel(boolean pMinimiseLevel)
    {
        ParticleStatus particlestatus = this.minecraft.options.particles;

        if (pMinimiseLevel && particlestatus == ParticleStatus.MINIMAL && this.level.random.nextInt(10) == 0)
        {
            particlestatus = ParticleStatus.DECREASED;
        }

        if (particlestatus == ParticleStatus.DECREASED && this.level.random.nextInt(3) == 0)
        {
            particlestatus = ParticleStatus.MINIMAL;
        }

        return particlestatus;
    }

    public void clear()
    {
    }

    public void globalLevelEvent(int pSoundID, BlockPos pPos, int pData)
    {
        switch (pSoundID)
        {
            case 1023:
            case 1028:
            case 1038:
                Camera camera = this.minecraft.gameRenderer.getMainCamera();

                if (camera.isInitialized())
                {
                    double d0 = (double)pPos.getX() - camera.getPosition().x;
                    double d1 = (double)pPos.getY() - camera.getPosition().y;
                    double d2 = (double)pPos.getZ() - camera.getPosition().z;
                    double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                    double d4 = camera.getPosition().x;
                    double d5 = camera.getPosition().y;
                    double d6 = camera.getPosition().z;

                    if (d3 > 0.0D)
                    {
                        d4 += d0 / d3 * 2.0D;
                        d5 += d1 / d3 * 2.0D;
                        d6 += d2 / d3 * 2.0D;
                    }

                    if (pSoundID == 1023)
                    {
                        this.level.playLocalSound(d4, d5, d6, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0F, 1.0F, false);
                    }
                    else if (pSoundID == 1038)
                    {
                        this.level.playLocalSound(d4, d5, d6, SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 1.0F, 1.0F, false);
                    }
                    else
                    {
                        this.level.playLocalSound(d4, d5, d6, SoundEvents.ENDER_DRAGON_DEATH, SoundSource.HOSTILE, 5.0F, 1.0F, false);
                    }
                }

            default:
        }
    }

    public void levelEvent(Player pPlayer, int pType, BlockPos pBlockPos, int pData)
    {
        Random random = this.level.random;

        switch (pType)
        {
            case 1000:
                this.level.playLocalSound(pBlockPos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;

            case 1001:
                this.level.playLocalSound(pBlockPos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 1.2F, false);
                break;

            case 1002:
                this.level.playLocalSound(pBlockPos, SoundEvents.DISPENSER_LAUNCH, SoundSource.BLOCKS, 1.0F, 1.2F, false);
                break;

            case 1003:
                this.level.playLocalSound(pBlockPos, SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 1.0F, 1.2F, false);
                break;

            case 1004:
                this.level.playLocalSound(pBlockPos, SoundEvents.FIREWORK_ROCKET_SHOOT, SoundSource.NEUTRAL, 1.0F, 1.2F, false);
                break;

            case 1005:
                this.level.playLocalSound(pBlockPos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1006:
                this.level.playLocalSound(pBlockPos, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1007:
                this.level.playLocalSound(pBlockPos, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1008:
                this.level.playLocalSound(pBlockPos, SoundEvents.FENCE_GATE_OPEN, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1009:
                if (pData == 0)
                {
                    this.level.playLocalSound(pBlockPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F, false);
                }
                else if (pData == 1)
                {
                    this.level.playLocalSound(pBlockPos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.7F, 1.6F + (random.nextFloat() - random.nextFloat()) * 0.4F, false);
                }

                break;

            case 1010:
                if (Item.byId(pData) instanceof RecordItem)
                {
                    if (Reflector.MinecraftForgeClient.exists())
                    {
                        this.playStreamingMusic(((RecordItem)Item.byId(pData)).getSound(), pBlockPos, (RecordItem)Item.byId(pData));
                    }
                    else
                    {
                        this.playStreamingMusic(((RecordItem)Item.byId(pData)).getSound(), pBlockPos);
                    }
                }
                else
                {
                    this.playStreamingMusic((SoundEvent)null, pBlockPos);
                }

                break;

            case 1011:
                this.level.playLocalSound(pBlockPos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1012:
                this.level.playLocalSound(pBlockPos, SoundEvents.WOODEN_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1013:
                this.level.playLocalSound(pBlockPos, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1014:
                this.level.playLocalSound(pBlockPos, SoundEvents.FENCE_GATE_CLOSE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1015:
                this.level.playLocalSound(pBlockPos, SoundEvents.GHAST_WARN, SoundSource.HOSTILE, 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1016:
                this.level.playLocalSound(pBlockPos, SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1017:
                this.level.playLocalSound(pBlockPos, SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.HOSTILE, 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1018:
                this.level.playLocalSound(pBlockPos, SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1019:
                this.level.playLocalSound(pBlockPos, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1020:
                this.level.playLocalSound(pBlockPos, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1021:
                this.level.playLocalSound(pBlockPos, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1022:
                this.level.playLocalSound(pBlockPos, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1024:
                this.level.playLocalSound(pBlockPos, SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1025:
                this.level.playLocalSound(pBlockPos, SoundEvents.BAT_TAKEOFF, SoundSource.NEUTRAL, 0.05F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1026:
                this.level.playLocalSound(pBlockPos, SoundEvents.ZOMBIE_INFECT, SoundSource.HOSTILE, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1027:
                this.level.playLocalSound(pBlockPos, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.NEUTRAL, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1029:
                this.level.playLocalSound(pBlockPos, SoundEvents.ANVIL_DESTROY, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1030:
                this.level.playLocalSound(pBlockPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1031:
                this.level.playLocalSound(pBlockPos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.3F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1032:
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forLocalAmbience(SoundEvents.PORTAL_TRAVEL, random.nextFloat() * 0.4F + 0.8F, 0.25F));
                break;

            case 1033:
                this.level.playLocalSound(pBlockPos, SoundEvents.CHORUS_FLOWER_GROW, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;

            case 1034:
                this.level.playLocalSound(pBlockPos, SoundEvents.CHORUS_FLOWER_DEATH, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;

            case 1035:
                this.level.playLocalSound(pBlockPos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;

            case 1036:
                this.level.playLocalSound(pBlockPos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1037:
                this.level.playLocalSound(pBlockPos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1039:
                this.level.playLocalSound(pBlockPos, SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 0.3F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1040:
                this.level.playLocalSound(pBlockPos, SoundEvents.ZOMBIE_CONVERTED_TO_DROWNED, SoundSource.NEUTRAL, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1041:
                this.level.playLocalSound(pBlockPos, SoundEvents.HUSK_CONVERTED_TO_ZOMBIE, SoundSource.NEUTRAL, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1042:
                this.level.playLocalSound(pBlockPos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1043:
                this.level.playLocalSound(pBlockPos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1044:
                this.level.playLocalSound(pBlockPos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1045:
                this.level.playLocalSound(pBlockPos, SoundEvents.POINTED_DRIPSTONE_LAND, SoundSource.BLOCKS, 2.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1046:
                this.level.playLocalSound(pBlockPos, SoundEvents.POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON, SoundSource.BLOCKS, 2.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1047:
                this.level.playLocalSound(pBlockPos, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundSource.BLOCKS, 2.0F, this.level.random.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1048:
                this.level.playLocalSound(pBlockPos, SoundEvents.SKELETON_CONVERTED_TO_STRAY, SoundSource.NEUTRAL, 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1500:
                ComposterBlock.handleFill(this.level, pBlockPos, pData > 0);
                break;

            case 1501:
                this.level.playLocalSound(pBlockPos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F, false);

                for (int l1 = 0; l1 < 8; ++l1)
                {
                    this.level.addParticle(ParticleTypes.LARGE_SMOKE, (double)pBlockPos.getX() + random.nextDouble(), (double)pBlockPos.getY() + 1.2D, (double)pBlockPos.getZ() + random.nextDouble(), 0.0D, 0.0D, 0.0D);
                }

                break;

            case 1502:
                this.level.playLocalSound(pBlockPos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F, false);

                for (int k1 = 0; k1 < 5; ++k1)
                {
                    double d14 = (double)pBlockPos.getX() + random.nextDouble() * 0.6D + 0.2D;
                    double d16 = (double)pBlockPos.getY() + random.nextDouble() * 0.6D + 0.2D;
                    double d17 = (double)pBlockPos.getZ() + random.nextDouble() * 0.6D + 0.2D;
                    this.level.addParticle(ParticleTypes.SMOKE, d14, d16, d17, 0.0D, 0.0D, 0.0D);
                }

                break;

            case 1503:
                this.level.playLocalSound(pBlockPos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0F, 1.0F, false);

                for (int j1 = 0; j1 < 16; ++j1)
                {
                    double d13 = (double)pBlockPos.getX() + (5.0D + random.nextDouble() * 6.0D) / 16.0D;
                    double d15 = (double)pBlockPos.getY() + 0.8125D;
                    double d1 = (double)pBlockPos.getZ() + (5.0D + random.nextDouble() * 6.0D) / 16.0D;
                    this.level.addParticle(ParticleTypes.SMOKE, d13, d15, d1, 0.0D, 0.0D, 0.0D);
                }

                break;

            case 1504:
                PointedDripstoneBlock.spawnDripParticle(this.level, pBlockPos, this.level.getBlockState(pBlockPos));
                break;

            case 1505:
                BoneMealItem.addGrowthParticles(this.level, pBlockPos, pData);
                this.level.playLocalSound(pBlockPos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;

            case 2000:
                Direction direction = Direction.from3DDataValue(pData);
                int i = direction.getStepX();
                int j = direction.getStepY();
                int k = direction.getStepZ();
                double d0 = (double)pBlockPos.getX() + (double)i * 0.6D + 0.5D;
                double d2 = (double)pBlockPos.getY() + (double)j * 0.6D + 0.5D;
                double d3 = (double)pBlockPos.getZ() + (double)k * 0.6D + 0.5D;

                for (int i2 = 0; i2 < 10; ++i2)
                {
                    double d18 = random.nextDouble() * 0.2D + 0.01D;
                    double d19 = d0 + (double)i * 0.01D + (random.nextDouble() - 0.5D) * (double)k * 0.5D;
                    double d20 = d2 + (double)j * 0.01D + (random.nextDouble() - 0.5D) * (double)j * 0.5D;
                    double d21 = d3 + (double)k * 0.01D + (random.nextDouble() - 0.5D) * (double)i * 0.5D;
                    double d22 = (double)i * d18 + random.nextGaussian() * 0.01D;
                    double d23 = (double)j * d18 + random.nextGaussian() * 0.01D;
                    double d27 = (double)k * d18 + random.nextGaussian() * 0.01D;
                    this.addParticle(ParticleTypes.SMOKE, d19, d20, d21, d22, d23, d27);
                }

                break;

            case 2001:
                BlockState blockstate = Block.stateById(pData);

                if (!blockstate.isAir())
                {
                    SoundType soundtype = blockstate.getSoundType();

                    if (Reflector.IForgeBlockState_getSoundType3.exists())
                    {
                        soundtype = (SoundType)Reflector.call(blockstate, Reflector.IForgeBlockState_getSoundType3, this.level, pBlockPos, null);
                    }

                    this.level.playLocalSound(pBlockPos, soundtype.getBreakSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F, false);
                }

                this.level.addDestroyBlockEffect(pBlockPos, blockstate);
                break;

            case 2002:
            case 2007:
                Vec3 vec3 = Vec3.atBottomCenterOf(pBlockPos);

                for (int l = 0; l < 8; ++l)
                {
                    this.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.SPLASH_POTION)), vec3.x, vec3.y, vec3.z, random.nextGaussian() * 0.15D, random.nextDouble() * 0.2D, random.nextGaussian() * 0.15D);
                }

                float f5 = (float)(pData >> 16 & 255) / 255.0F;
                float f = (float)(pData >> 8 & 255) / 255.0F;
                float f1 = (float)(pData >> 0 & 255) / 255.0F;
                ParticleOptions particleoptions = pType == 2007 ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.EFFECT;

                for (int j2 = 0; j2 < 100; ++j2)
                {
                    double d5 = random.nextDouble() * 4.0D;
                    double d7 = random.nextDouble() * Math.PI * 2.0D;
                    double d9 = Math.cos(d7) * d5;
                    double d26 = 0.01D + random.nextDouble() * 0.5D;
                    double d29 = Math.sin(d7) * d5;
                    Particle particle1 = this.addParticleInternal(particleoptions, particleoptions.getType().getOverrideLimiter(), vec3.x + d9 * 0.1D, vec3.y + 0.3D, vec3.z + d29 * 0.1D, d9, d26, d29);

                    if (particle1 != null)
                    {
                        float f4 = 0.75F + random.nextFloat() * 0.25F;
                        particle1.setColor(f5 * f4, f * f4, f1 * f4);
                        particle1.setPower((float)d5);
                    }
                }

                this.level.playLocalSound(pBlockPos, SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 2003:
                double d4 = (double)pBlockPos.getX() + 0.5D;
                double d6 = (double)pBlockPos.getY();
                double d8 = (double)pBlockPos.getZ() + 0.5D;

                for (int i3 = 0; i3 < 8; ++i3)
                {
                    this.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.ENDER_EYE)), d4, d6, d8, random.nextGaussian() * 0.15D, random.nextDouble() * 0.2D, random.nextGaussian() * 0.15D);
                }

                for (double d24 = 0.0D; d24 < (Math.PI * 2D); d24 += 0.15707963267948966D)
                {
                    this.addParticle(ParticleTypes.PORTAL, d4 + Math.cos(d24) * 5.0D, d6 - 0.4D, d8 + Math.sin(d24) * 5.0D, Math.cos(d24) * -5.0D, 0.0D, Math.sin(d24) * -5.0D);
                    this.addParticle(ParticleTypes.PORTAL, d4 + Math.cos(d24) * 5.0D, d6 - 0.4D, d8 + Math.sin(d24) * 5.0D, Math.cos(d24) * -7.0D, 0.0D, Math.sin(d24) * -7.0D);
                }

                break;

            case 2004:
                for (int l2 = 0; l2 < 20; ++l2)
                {
                    double d25 = (double)pBlockPos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 2.0D;
                    double d28 = (double)pBlockPos.getY() + 0.5D + (random.nextDouble() - 0.5D) * 2.0D;
                    double d30 = (double)pBlockPos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 2.0D;
                    this.level.addParticle(ParticleTypes.SMOKE, d25, d28, d30, 0.0D, 0.0D, 0.0D);
                    this.level.addParticle(ParticleTypes.FLAME, d25, d28, d30, 0.0D, 0.0D, 0.0D);
                }

                break;

            case 2005:
                BoneMealItem.addGrowthParticles(this.level, pBlockPos, pData);
                break;

            case 2006:
                for (int k2 = 0; k2 < 200; ++k2)
                {
                    float f2 = random.nextFloat() * 4.0F;
                    float f3 = random.nextFloat() * ((float)Math.PI * 2F);
                    double d10 = (double)(Mth.cos(f3) * f2);
                    double d11 = 0.01D + random.nextDouble() * 0.5D;
                    double d12 = (double)(Mth.sin(f3) * f2);
                    Particle particle = this.addParticleInternal(ParticleTypes.DRAGON_BREATH, false, (double)pBlockPos.getX() + d10 * 0.1D, (double)pBlockPos.getY() + 0.3D, (double)pBlockPos.getZ() + d12 * 0.1D, d10, d11, d12);

                    if (particle != null)
                    {
                        particle.setPower(f2);
                    }
                }

                if (pData == 1)
                {
                    this.level.playLocalSound(pBlockPos, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE, 1.0F, random.nextFloat() * 0.1F + 0.9F, false);
                }

                break;

            case 2008:
                this.level.addParticle(ParticleTypes.EXPLOSION, (double)pBlockPos.getX() + 0.5D, (double)pBlockPos.getY() + 0.5D, (double)pBlockPos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
                break;

            case 2009:
                for (int i1 = 0; i1 < 8; ++i1)
                {
                    this.level.addParticle(ParticleTypes.CLOUD, (double)pBlockPos.getX() + random.nextDouble(), (double)pBlockPos.getY() + 1.2D, (double)pBlockPos.getZ() + random.nextDouble(), 0.0D, 0.0D, 0.0D);
                }

                break;

            case 3000:
                this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, true, (double)pBlockPos.getX() + 0.5D, (double)pBlockPos.getY() + 0.5D, (double)pBlockPos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
                this.level.playLocalSound(pBlockPos, SoundEvents.END_GATEWAY_SPAWN, SoundSource.BLOCKS, 10.0F, (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F, false);
                break;

            case 3001:
                this.level.playLocalSound(pBlockPos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 64.0F, 0.8F + this.level.random.nextFloat() * 0.3F, false);
                break;

            case 3002:
                if (pData >= 0 && pData < Direction.Axis.VALUES.length)
                {
                    ParticleUtils.spawnParticlesAlongAxis(Direction.Axis.VALUES[pData], this.level, pBlockPos, 0.125D, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(10, 19));
                }
                else
                {
                    ParticleUtils.spawnParticlesOnBlockFaces(this.level, pBlockPos, ParticleTypes.ELECTRIC_SPARK, UniformInt.of(3, 5));
                }

                break;

            case 3003:
                ParticleUtils.spawnParticlesOnBlockFaces(this.level, pBlockPos, ParticleTypes.WAX_ON, UniformInt.of(3, 5));
                this.level.playLocalSound(pBlockPos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F, false);
                break;

            case 3004:
                ParticleUtils.spawnParticlesOnBlockFaces(this.level, pBlockPos, ParticleTypes.WAX_OFF, UniformInt.of(3, 5));
                break;

            case 3005:
                ParticleUtils.spawnParticlesOnBlockFaces(this.level, pBlockPos, ParticleTypes.SCRAPE, UniformInt.of(3, 5));
        }
    }

    public void destroyBlockProgress(int pBreakerId, BlockPos pPos, int pProgress)
    {
        if (pProgress >= 0 && pProgress < 10)
        {
            BlockDestructionProgress blockdestructionprogress1 = this.destroyingBlocks.get(pBreakerId);

            if (blockdestructionprogress1 != null)
            {
                this.removeProgress(blockdestructionprogress1);
            }

            if (blockdestructionprogress1 == null || blockdestructionprogress1.getPos().getX() != pPos.getX() || blockdestructionprogress1.getPos().getY() != pPos.getY() || blockdestructionprogress1.getPos().getZ() != pPos.getZ())
            {
                blockdestructionprogress1 = new BlockDestructionProgress(pBreakerId, pPos);
                this.destroyingBlocks.put(pBreakerId, blockdestructionprogress1);
            }

            blockdestructionprogress1.setProgress(pProgress);
            blockdestructionprogress1.updateTick(this.ticks);
            this.destructionProgress.computeIfAbsent(blockdestructionprogress1.getPos().asLong(), (longIn) ->
            {
                return Sets.newTreeSet();
            }).add(blockdestructionprogress1);
        }
        else
        {
            BlockDestructionProgress blockdestructionprogress = this.destroyingBlocks.remove(pBreakerId);

            if (blockdestructionprogress != null)
            {
                this.removeProgress(blockdestructionprogress);
            }
        }
    }

    public boolean hasRenderedAllChunks()
    {
        return this.chunksToCompile.isEmpty() && this.chunkRenderDispatcher.isQueueEmpty();
    }

    public void needsUpdate()
    {
        this.needsUpdate = true;
        this.generateClouds = true;
    }

    public int getCountRenderers()
    {
        return this.viewArea.chunks.length;
    }

    public int getCountActiveRenderers()
    {
        return this.renderChunks.size();
    }

    public int getCountEntitiesRendered()
    {
        return this.renderedEntities;
    }

    public int getCountTileEntitiesRendered()
    {
        return this.countTileEntitiesRendered;
    }

    public int getCountLoadedChunks()
    {
        if (this.level == null)
        {
            return 0;
        }
        else
        {
            ClientChunkCache clientchunkcache = this.level.getChunkSource();
            return clientchunkcache == null ? 0 : clientchunkcache.getLoadedChunksCount();
        }
    }

    public int getCountChunksToUpdate()
    {
        return this.chunksToCompile.size();
    }

    public ChunkRenderDispatcher.RenderChunk getRenderChunk(BlockPos pos)
    {
        return this.viewArea.getRenderChunkAt(pos);
    }

    public ClientLevel getWorld()
    {
        return this.level;
    }

    private void clearRenderInfos()
    {
        if (renderEntitiesCounter > 0)
        {
            this.renderChunks = new ObjectArrayList<>(this.renderChunks.size() + 16);
            this.renderInfosEntities = new LongOpenHashSet(this.renderInfosEntities.size() + 16);
            this.renderInfosTileEntities = new ArrayList<>(this.renderInfosTileEntities.size() + 16);
        }
        else
        {
            this.renderChunks.clear();
            this.renderInfosEntities.clear();
            this.renderInfosTileEntities.clear();
        }
    }

    public void onPlayerPositionSet()
    {
        if (this.firstWorldLoad)
        {
            this.allChanged();
            this.firstWorldLoad = false;
        }
    }

    public void pauseChunkUpdates()
    {
        if (this.chunkRenderDispatcher != null)
        {
            this.chunkRenderDispatcher.pauseChunkUpdates();
        }
    }

    public void resumeChunkUpdates()
    {
        if (this.chunkRenderDispatcher != null)
        {
            this.chunkRenderDispatcher.resumeChunkUpdates();
        }
    }

    public int getFrameCount()
    {
        return this.frameId;
    }

    public int getNextFrameCount()
    {
        return ++this.frameId;
    }

    public RenderBuffers getRenderTypeTextures()
    {
        return this.renderBuffers;
    }

    public LongOpenHashSet getRenderChunksEntities()
    {
        return this.renderInfosEntities;
    }

    private void addEntitySection(LongOpenHashSet set, EntitySectionStorage storage, BlockPos pos)
    {
        long i = SectionPos.asLong(pos);
        EntitySection entitysection = storage.getSection(i);

        if (entitysection != null)
        {
            set.add(i);
        }
    }

    public List<LevelRenderer.RenderChunkInfo> getRenderInfosTileEntities()
    {
        return this.renderInfosTileEntities;
    }

    private void checkLoadVisibleChunks(Camera activeRenderInfo, Frustum icamera, boolean spectator)
    {
        if (this.loadVisibleChunksCounter == 0)
        {
            this.loadAllVisibleChunks(activeRenderInfo, icamera, spectator);
            this.minecraft.gui.getChat().removeById(201435902);
        }

        if (this.loadVisibleChunksCounter > -1)
        {
            --this.loadVisibleChunksCounter;
        }
    }

    private void loadAllVisibleChunks(Camera activeRenderInfo, Frustum icamera, boolean spectator)
    {
        int i = this.minecraft.options.ofChunkUpdates;
        boolean flag = this.minecraft.options.ofLazyChunkLoading;

        try
        {
            this.minecraft.options.ofChunkUpdates = 1000;
            this.minecraft.options.ofLazyChunkLoading = false;
            LevelRenderer levelrenderer = Config.getRenderGlobal();
            int j = levelrenderer.getCountLoadedChunks();
            long k = System.currentTimeMillis();
            Config.dbg("Loading visible chunks");
            long l = System.currentTimeMillis() + 5000L;
            int i1 = 0;
            boolean flag1 = false;

            do
            {
                flag1 = false;

                for (int j1 = 0; j1 < 100; ++j1)
                {
                    levelrenderer.needsUpdate();
                    levelrenderer.setupRender(activeRenderInfo, icamera, false, this.frameId++, spectator);

                    if (!levelrenderer.hasRenderedAllChunks())
                    {
                        flag1 = true;
                    }

                    i1 = i1 + levelrenderer.getCountChunksToUpdate();

                    while (!levelrenderer.hasRenderedAllChunks())
                    {
                        levelrenderer.compileChunksUntil(System.nanoTime() + 1000000000L);
                    }

                    i1 = i1 - levelrenderer.getCountChunksToUpdate();

                    if (!flag1)
                    {
                        break;
                    }
                }

                if (levelrenderer.getCountLoadedChunks() != j)
                {
                    flag1 = true;
                    j = levelrenderer.getCountLoadedChunks();
                }

                if (System.currentTimeMillis() > l)
                {
                    Config.log("Chunks loaded: " + i1);
                    l = System.currentTimeMillis() + 5000L;
                }
            }
            while (flag1);

            Config.log("Chunks loaded: " + i1);
            Config.log("Finished loading visible chunks");
            ChunkRenderDispatcher.renderChunksUpdated = 0;
        }
        finally
        {
            this.minecraft.options.ofChunkUpdates = i;
            this.minecraft.options.ofLazyChunkLoading = flag;
        }
    }

    public IResourceType getResourceType()
    {
        return VanillaResourceType.MODELS;
    }

    public void updateGlobalBlockEntities(Collection<BlockEntity> pTileEntitiesToRemove, Collection<BlockEntity> pTileEntitiesToAdd)
    {
        synchronized (this.globalBlockEntities)
        {
            this.globalBlockEntities.removeAll(pTileEntitiesToRemove);
            this.globalBlockEntities.addAll(pTileEntitiesToAdd);
        }
    }

    public static int getLightColor(BlockAndTintGetter pLightReader, BlockPos pBlockPos)
    {
        return getLightColor(pLightReader, pLightReader.getBlockState(pBlockPos), pBlockPos);
    }

    public static int getLightColor(BlockAndTintGetter pLightReader, BlockState pBlockPos, BlockPos p_109540_)
    {
        if (pBlockPos.emissiveRendering(pLightReader, p_109540_))
        {
            return 15728880;
        }
        else
        {
            int i = pLightReader.getBrightness(LightLayer.SKY, p_109540_);
            int j = pLightReader.getBrightness(LightLayer.BLOCK, p_109540_);
            int k = pBlockPos.getLightValue(pLightReader, p_109540_);

            if (j < k)
            {
                j = k;
            }

            int l = i << 20 | j << 4;

            if (Config.isDynamicLights() && pLightReader instanceof BlockGetter && (!ambientOcclusion || !pBlockPos.isSolidRender(pLightReader, p_109540_)))
            {
                l = DynamicLights.getCombinedLight(p_109540_, l);
            }

            return l;
        }
    }

    @Nullable
    public RenderTarget entityTarget()
    {
        return this.entityTarget;
    }

    @Nullable
    public RenderTarget getTranslucentTarget()
    {
        return this.translucentTarget;
    }

    @Nullable
    public RenderTarget getItemEntityTarget()
    {
        return this.itemEntityTarget;
    }

    @Nullable
    public RenderTarget getParticlesTarget()
    {
        return this.particlesTarget;
    }

    @Nullable
    public RenderTarget getWeatherTarget()
    {
        return this.weatherTarget;
    }

    @Nullable
    public RenderTarget getCloudsTarget()
    {
        return this.cloudsTarget;
    }

    public static class RenderChunkInfo
    {
        public final ChunkRenderDispatcher.RenderChunk chunk;
        private int sourceDirections;
        int directions;
        int step;

        public RenderChunkInfo(ChunkRenderDispatcher.RenderChunk p_173022_, @Nullable Direction p_173023_, int p_173024_)
        {
            this.chunk = p_173022_;

            if (p_173023_ != null)
            {
                this.addSourceDirection(p_173023_);
            }

            this.step = p_173024_;
        }

        public void setDirection(int dir, Direction facingIn)
        {
            this.directions = this.directions | dir | 1 << facingIn.ordinal();
        }

        public boolean hasDirection(Direction pFacing)
        {
            return (this.directions & 1 << pFacing.ordinal()) > 0;
        }

        private void initialize(Direction facingIn, int setFacingIn, int counter)
        {
            this.sourceDirections = facingIn != null ? 1 << facingIn.ordinal() : 0;
            this.directions = setFacingIn;
            this.step = counter;
        }

        public void addSourceDirection(Direction p_173029_)
        {
            this.sourceDirections = (byte)(this.sourceDirections | this.sourceDirections | 1 << p_173029_.ordinal());
        }

        public boolean hasSourceDirection(int p_173027_)
        {
            return (this.sourceDirections & 1 << p_173027_) > 0;
        }

        public boolean hasSourceDirections()
        {
            return this.sourceDirections != 0;
        }
    }

    static class RenderInfoMap
    {
        private final LevelRenderer.RenderChunkInfo[] infos;
        private final LevelRenderer.RenderChunkInfo[] blank;

        RenderInfoMap(int p_173033_)
        {
            this.infos = new LevelRenderer.RenderChunkInfo[p_173033_];
            this.blank = new LevelRenderer.RenderChunkInfo[p_173033_];
        }

        void clear()
        {
            System.arraycopy(this.blank, 0, this.infos, 0, this.infos.length);
        }

        public void put(ChunkRenderDispatcher.RenderChunk p_173038_, LevelRenderer.RenderChunkInfo p_173039_)
        {
            this.infos[p_173038_.index] = p_173039_;
        }

        public LevelRenderer.RenderChunkInfo get(ChunkRenderDispatcher.RenderChunk p_173036_)
        {
            return this.infos[p_173036_.index];
        }
    }

    public static class TransparencyShaderException extends RuntimeException
    {
        public TransparencyShaderException(String p_109868_, Throwable p_109869_)
        {
            super(p_109868_, p_109869_);
        }
    }
}
