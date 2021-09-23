package net.minecraft.client.gui.components;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.optifine.Config;
import net.optifine.SmartAnimations;
import net.optifine.TextureAnimations;
import net.optifine.reflect.Reflector;
import net.optifine.util.GuiPoint;
import net.optifine.util.GuiRect;
import net.optifine.util.GuiUtils;
import net.optifine.util.MemoryMonitor;
import net.optifine.util.NativeMemory;

public class DebugScreenOverlay extends GuiComponent
{
    private static final int COLOR_GREY = 14737632;
    private static final int MARGIN_RIGHT = 2;
    private static final int MARGIN_LEFT = 2;
    private static final int MARGIN_TOP = 2;
    private static final Map<Heightmap.Types, String> HEIGHTMAP_NAMES = Util.make(new EnumMap<>(Heightmap.Types.class), (p_94069_0_) ->
    {
        p_94069_0_.put(Heightmap.Types.WORLD_SURFACE_WG, "SW");
        p_94069_0_.put(Heightmap.Types.WORLD_SURFACE, "S");
        p_94069_0_.put(Heightmap.Types.OCEAN_FLOOR_WG, "OW");
        p_94069_0_.put(Heightmap.Types.OCEAN_FLOOR, "O");
        p_94069_0_.put(Heightmap.Types.MOTION_BLOCKING, "M");
        p_94069_0_.put(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, "ML");
    });
    private final Minecraft minecraft;
    private final Font font;
    private HitResult block;
    private HitResult liquid;
    @Nullable
    private ChunkPos lastPos;
    @Nullable
    private LevelChunk clientChunk;
    @Nullable
    private CompletableFuture<LevelChunk> serverChunk;
    private static final int RED = -65536;
    private static final int YELLOW = -256;
    private static final int GREEN = -16711936;
    private String debugOF = null;
    private List<String> debugInfoLeft = null;
    private List<String> debugInfoRight = null;
    private long updateInfoLeftTimeMs = 0L;
    private long updateInfoRightTimeMs = 0L;

    public DebugScreenOverlay(Minecraft p_94039_)
    {
        this.minecraft = p_94039_;
        this.font = p_94039_.font;
    }

    public void clearChunkCache()
    {
        this.serverChunk = null;
        this.clientChunk = null;
    }
    //* this is for rendering the debug screen for mc 
    public void render(PoseStack p_94057_)
    {
        this.minecraft.getProfiler().push("debug");
        Entity entity = this.minecraft.getCameraEntity();
       this.block = entity.pick(20.0D, 0.0F, false);
       this.liquid = entity.pick(20.0D, 0.0F, true);
        this.drawGameInformation(p_94057_);
        this.drawSystemInformation(p_94057_);

        if (this.minecraft.options.renderFpsChart)
        {
            int i = this.minecraft.getWindow().getGuiScaledWidth();
            this.drawChart(p_94057_, this.minecraft.getFrameTimer(), 0, i / 2, true);
            IntegratedServer integratedserver = this.minecraft.getSingleplayerServer();

            if (integratedserver != null)
            {
                this.drawChart(p_94057_, integratedserver.getFrameTimer(), i - Math.min(i / 2, 240), i / 2, false);
            }
        }

        this.minecraft.getProfiler().pop();
    }

    protected void drawGameInformation(PoseStack p_94077_)
    {
        List<String> list = this.debugInfoLeft;

        if (list == null || System.currentTimeMillis() > this.updateInfoLeftTimeMs)
        {
            list = this.getGameInformation();
            list.add("");
            boolean flag = this.minecraft.getSingleplayerServer() != null;
            list.add("Debug: Pie [shift]: " + (this.minecraft.options.renderDebugCharts ? "visible" : "hidden") + (flag ? " FPS + TPS" : " FPS") + " [alt]: " + (this.minecraft.options.renderFpsChart ? "visible" : "hidden"));
            list.add("For help: press F3 + Q");
            this.debugInfoLeft = list;
            this.updateInfoLeftTimeMs = System.currentTimeMillis() + 100L;
        }

        GuiPoint[] aguipoint = new GuiPoint[list.size()];
        GuiRect[] aguirect = new GuiRect[list.size()];

        for (int i = 0; i < list.size(); ++i)
        {
            String s = list.get(i);

            if (!Strings.isNullOrEmpty(s))
            {
                int j = 9;
                int k = this.font.width(s);
                int l = 2;
                int i1 = 2 + j * i;
                aguirect[i] = new GuiRect(1, i1 - 1, 2 + k + 1, i1 + j - 1);
                aguipoint[i] = new GuiPoint(2, i1);
            }
        }

        GuiUtils.fill(p_94077_.last().pose(), aguirect, -1873784752);
        this.font.renderStrings(list, aguipoint, 14737632, p_94077_.last().pose(), false, this.font.isBidirectional());
    }

    protected void drawSystemInformation(PoseStack p_94080_)
    {
        List<String> list = this.debugInfoRight;

        if (list == null || System.currentTimeMillis() > this.updateInfoRightTimeMs)
        {
            list = this.getSystemInformation();
            this.debugInfoRight = list;
            this.updateInfoRightTimeMs = System.currentTimeMillis() + 100L;
        }

        GuiPoint[] aguipoint = new GuiPoint[list.size()];
        GuiRect[] aguirect = new GuiRect[list.size()];

        for (int i = 0; i < list.size(); ++i)
        {
            String s = list.get(i);

            if (!Strings.isNullOrEmpty(s))
            {
                int j = 9;
                int k = this.font.width(s);
                int l = this.minecraft.getWindow().getGuiScaledWidth() - 2 - k;
                int i1 = 2 + j * i;
                aguirect[i] = new GuiRect(l - 1, i1 - 1, l + k + 1, i1 + j - 1);
                aguipoint[i] = new GuiPoint(l, i1);
            }
        }

        GuiUtils.fill(p_94080_.last().pose(), aguirect, -1873784752);
        this.font.renderStrings(list, aguipoint, 14737632, p_94080_.last().pose(), false, this.font.isBidirectional());
    }

    protected List<String> getGameInformation()
    {
        if (this.minecraft.fpsString != this.debugOF)
        {
            StringBuffer stringbuffer = new StringBuffer(this.minecraft.fpsString);
            int i = Config.getChunkUpdates();
            int j = this.minecraft.fpsString.indexOf("T: ");

            if (j >= 0)
            {
                stringbuffer.insert(j, "(" + i + " chunk updates) ");
            }

            int k = Config.getFpsMin();
            int l = this.minecraft.fpsString.indexOf(" fps ");

            if (l >= 0)
            {
                stringbuffer.replace(0, l + 4, Config.getFpsString());
            }

            if (Config.isSmoothFps())
            {
                stringbuffer.append(" sf");
            }

            if (Config.isFastRender())
            {
                stringbuffer.append(" fr");
            }

            if (Config.isAnisotropicFiltering())
            {
                stringbuffer.append(" af");
            }

            if (Config.isAntialiasing())
            {
                stringbuffer.append(" aa");
            }

            if (Config.isRenderRegions())
            {
                stringbuffer.append(" rr");
            }

            if (Config.isShaders())
            {
                stringbuffer.append(" sh");
            }

            this.minecraft.fpsString = stringbuffer.toString();
            this.debugOF = this.minecraft.fpsString;
        }

        List<String> list = this.getInfoLeft();
        StringBuilder stringbuilder = new StringBuilder();
        TextureAtlas textureatlas = Config.getTextureMap();
        stringbuilder.append(", A: ");

        if (SmartAnimations.isActive())
        {
            stringbuilder.append(textureatlas.getCountAnimationsActive() + TextureAnimations.getCountAnimationsActive());
            stringbuilder.append("/");
        }

        stringbuilder.append(textureatlas.getCountAnimations() + TextureAnimations.getCountAnimations());
        String s1 = stringbuilder.toString();

        for (int i1 = 0; i1 < list.size(); ++i1)
        {
            String s = list.get(i1);

            if (s != null && s.startsWith("P: "))
            {
                s = s + s1;
                list.set(i1, s);
                break;
            }
        }

        return list;
    }

    protected List<String> getInfoLeft()
    {
        IntegratedServer integratedserver = this.minecraft.getSingleplayerServer();
        Connection connection = this.minecraft.getConnection().getConnection();
        float f = connection.getAverageSentPackets();
        float f1 = connection.getAverageReceivedPackets();
        String s;

        if (integratedserver != null)
        {
            s = String.format("Integrated server @ %.0f ms ticks, %.0f tx, %.0f rx", integratedserver.getAverageTickTime(), f, f1);
        }
        else
        {
            s = String.format("\"%s\" server, %.0f tx, %.0f rx", this.minecraft.player.getServerBrand(), f, f1);
        }

        BlockPos blockpos = this.minecraft.getCameraEntity().blockPosition();

        if (this.minecraft.showOnlyReducedInfo())
        {
            return Lists.newArrayList("Minecraft " + SharedConstants.getCurrentVersion().getName() + " (" + this.minecraft.getLaunchedVersion() + "/" + ClientBrandRetriever.getClientModName() + ")", this.minecraft.fpsString, s, this.minecraft.levelRenderer.getChunkStatistics(), this.minecraft.levelRenderer.getEntityStatistics(), "P: " + this.minecraft.particleEngine.countParticles() + ". T: " + this.minecraft.level.getEntityCount(), this.minecraft.level.gatherChunkSourceStats(), "", String.format("Chunk-relative: %d %d %d", blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15));
        }
        else
        {
            Entity entity = this.minecraft.getCameraEntity();
            Direction direction = entity.getDirection();
            String s1;

            switch (direction)
            {
                case NORTH:
                    s1 = "Towards negative Z";
                    break;

                case SOUTH:
                    s1 = "Towards positive Z";
                    break;

                case WEST:
                    s1 = "Towards negative X";
                    break;

                case EAST:
                    s1 = "Towards positive X";
                    break;

                default:
                    s1 = "Invalid";
            }

            ChunkPos chunkpos = new ChunkPos(blockpos);

            if (!Objects.equals(this.lastPos, chunkpos))
            {
                this.lastPos = chunkpos;
                this.clearChunkCache();
            }

            Level level = this.getLevel();
            LongSet longset = (LongSet)(level instanceof ServerLevel ? ((ServerLevel)level).getForcedChunks() : LongSets.EMPTY_SET);
            List<String> list = Lists.newArrayList("Minecraft " + SharedConstants.getCurrentVersion().getName() + " (" + this.minecraft.getLaunchedVersion() + "/" + ClientBrandRetriever.getClientModName() + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType()) + ")", this.minecraft.fpsString, s, this.minecraft.levelRenderer.getChunkStatistics(), this.minecraft.levelRenderer.getEntityStatistics(), "P: " + this.minecraft.particleEngine.countParticles() + ". T: " + this.minecraft.level.getEntityCount(), this.minecraft.level.gatherChunkSourceStats());
            String s2 = this.getServerChunkStats();

            if (s2 != null)
            {
                list.add(s2);
            }
            LevelChunk levelchunk = this.getClientChunk();

            if (levelchunk.isEmpty())
            {
                list.add("Waiting for chunk...");
            }
            else
            {
                int i = this.minecraft.level.getChunkSource().getLightEngine().getRawBrightness(blockpos, 0);
                int j = this.minecraft.level.getBrightness(LightLayer.SKY, blockpos);
                int k = this.minecraft.level.getBrightness(LightLayer.BLOCK, blockpos);
                //list.add("Client Light: " + i + " (" + j + " sky, " + k + " block)");
                LevelChunk levelchunk1 = this.getServerChunk();
                StringBuilder stringbuilder = new StringBuilder("CH");

                for (Heightmap.Types heightmap$types : Heightmap.Types.values())
                {
                    if (heightmap$types.sendToClient())
                    {
                        stringbuilder.append(" ").append(HEIGHTMAP_NAMES.get(heightmap$types)).append(": ").append(levelchunk.getHeight(heightmap$types, blockpos.getX(), blockpos.getZ()));
                    }
                }

                list.add(stringbuilder.toString());
                stringbuilder.setLength(0);
                stringbuilder.append("SH");

                for (Heightmap.Types heightmap$types1 : Heightmap.Types.values())
                {
                    if (heightmap$types1.keepAfterWorldgen())
                    {
                        stringbuilder.append(" ").append(HEIGHTMAP_NAMES.get(heightmap$types1)).append(": ");

                        if (levelchunk1 != null)
                        {
                            stringbuilder.append(levelchunk1.getHeight(heightmap$types1, blockpos.getX(), blockpos.getZ()));
                        }
                        else
                        {
                            stringbuilder.append("??");
                        }
                    }
                }

                list.add(stringbuilder.toString());

                if (blockpos.getY() >= this.minecraft.level.getMinBuildHeight() && blockpos.getY() < this.minecraft.level.getMaxBuildHeight())
                {
                    list.add("Biome: " + this.minecraft.level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getKey(this.minecraft.level.getBiome(blockpos)));
                    long i1 = 0L;
                    float f2 = 0.0F;

                    if (levelchunk1 != null)
                    {
                        f2 = level.getMoonBrightness();
                        i1 = levelchunk1.getInhabitedTime();
                    }

                    DifficultyInstance difficultyinstance = new DifficultyInstance(level.getDifficulty(), level.getDayTime(), i1, f2);
                   // list.add(String.format(Locale.ROOT, "Local Difficulty: %.2f // %.2f (Day %d)", difficultyinstance.getEffectiveDifficulty(), difficultyinstance.getSpecialMultiplier(), this.minecraft.level.getDayTime() / 24000L));
                }
            }

            ServerLevel serverlevel = this.getServerLevel();

            if (serverlevel != null)
            {
                NaturalSpawner.SpawnState naturalspawner$spawnstate = serverlevel.getChunkSource().getLastSpawnState();

                if (naturalspawner$spawnstate != null)
                {
                    Object2IntMap<MobCategory> object2intmap = naturalspawner$spawnstate.getMobCategoryCounts();
                    int l = naturalspawner$spawnstate.getSpawnableChunkCount();
                   /* list.add("SC: " + l + ", " + (String)Stream.of(MobCategory.values()).map((p_94066_1_) ->
                    {
                        return Character.toUpperCase(p_94066_1_.getName().charAt(0)) + ": " + object2intmap.getInt(p_94066_1_);
                    }).collect(Collectors.joining(", ")));*/
                }
                else
                {
                    list.add("SC: N/A");
                }
            }

            PostChain postchain = this.minecraft.gameRenderer.currentEffect();

            if (postchain != null)
            {
                list.add("Shader: " + postchain.getName());
            }

            list.add(this.minecraft.getSoundManager().getDebugString() + String.format(" (Mood %d%%)", Math.round(this.minecraft.player.getCurrentMood() * 100.0F)));
            return list;
        }
    }

    @Nullable
    private ServerLevel getServerLevel()
    {
        IntegratedServer integratedserver = this.minecraft.getSingleplayerServer();
        return integratedserver != null ? integratedserver.getLevel(this.minecraft.level.dimension()) : null;
    }

    @Nullable
    private String getServerChunkStats()
    {
        ServerLevel serverlevel = this.getServerLevel();
        return serverlevel != null ? serverlevel.gatherChunkSourceStats() : null;
    }

    private Level getLevel()
    {
        return DataFixUtils.orElse(Optional.ofNullable(this.minecraft.getSingleplayerServer()).flatMap((p_94064_1_) ->
        {
            return Optional.ofNullable(p_94064_1_.getLevel(this.minecraft.level.dimension()));
        }), this.minecraft.level);
    }

    @Nullable
    private LevelChunk getServerChunk()
    {
        if (this.serverChunk == null)
        {
            ServerLevel serverlevel = this.getServerLevel();

            if (serverlevel != null)
            {
                this.serverChunk = serverlevel.getChunkSource().getChunkFuture(this.lastPos.x, this.lastPos.z, ChunkStatus.FULL, false).thenApply((p_94054_0_) ->
                {
                    return p_94054_0_.map((p_314197_0_) -> {
                        return (LevelChunk)p_314197_0_;
                    }, (p_314195_0_) -> {
                        return null;
                    });
                });
            }

            if (this.serverChunk == null)
            {
                this.serverChunk = CompletableFuture.completedFuture(this.getClientChunk());
            }
        }

        return this.serverChunk.getNow((LevelChunk)null);
    }

    private LevelChunk getClientChunk()
    {
        if (this.clientChunk == null)
        {
            this.clientChunk = this.minecraft.level.getChunk(this.lastPos.x, this.lastPos.z);
        }

        return this.clientChunk;
    }

    protected List<String> getSystemInformation()
    {
        long i = Runtime.getRuntime().maxMemory();
        long j = Runtime.getRuntime().totalMemory();
        long k = Runtime.getRuntime().freeMemory();
        long l = j - k;
        List<String> list = Lists.newArrayList(String.format("Java: %s %dbit", System.getProperty("java.version"), this.minecraft.is64Bit() ? 64 : 32), String.format("Mem: % 2d%% %03d/%03dMB", l * 100L / i, bytesToMegabytes(l), bytesToMegabytes(i)), String.format("Allocated: % 2d%% %03dMB", j * 100L / i, bytesToMegabytes(j)), "", String.format("CPU: %s", GlUtil.getCpuInfo()), "", String.format("Display: %dx%d (%s)", Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), GlUtil.getVendor()), GlUtil.getRenderer(), GlUtil.getOpenGLVersion());
        long i1 = NativeMemory.getBufferAllocated();
        long j1 = NativeMemory.getBufferMaximum();
        long k1 = NativeMemory.getImageAllocated();
        String s = "Native: " + bytesToMegabytes(i1) + "/" + bytesToMegabytes(j1) + "+" + bytesToMegabytes(k1) + "MB";
        list.add(3, s);
        list.set(4, "GC: " + MemoryMonitor.getAllocationRateMb() + "MB/s");

        if (Reflector.BrandingControl_getBrandings.exists())
        {
            list.add("");

            for (String s1 : (Collection<String>)Reflector.call(Reflector.BrandingControl_getBrandings, true, false))
            {
                if (!s1.startsWith("Minecraft "))
                {
                    list.add(s1);
                }
            }
        }

        if (this.minecraft.showOnlyReducedInfo())
        {
            return list;
        }
        else
        {
            if (this.block.getType() == HitResult.Type.BLOCK)
            {
                BlockPos blockpos = ((BlockHitResult)this.block).getBlockPos();
                BlockState blockstate = this.minecraft.level.getBlockState(blockpos);
                list.add("");
                list.add(ChatFormatting.UNDERLINE + "Targeted Block: " + blockpos.getX() + ", " + blockpos.getY() + ", " + blockpos.getZ());
                list.add(String.valueOf((Object)Registry.BLOCK.getKey(blockstate.getBlock())));

                for (Entry < Property<?>, Comparable<? >> entry : blockstate.getValues().entrySet())
                {
                    list.add(this.getPropertyValueString(entry));
                }

                Collection<ResourceLocation> collection1;

                if (Reflector.IForgeBlock_getTags.exists())
                {
                    collection1 = (Collection)Reflector.call(blockstate.getBlock(), Reflector.IForgeBlock_getTags);
                }
                else
                {
                    collection1 = this.minecraft.getConnection().getTags().getOrEmpty(Registry.BLOCK_REGISTRY).getMatchingTags(blockstate.getBlock());
                }

                for (ResourceLocation resourcelocation : collection1)
                {
                    list.add("#" + resourcelocation);
                }
            }

            if (this.liquid.getType() == HitResult.Type.BLOCK)
            {
                BlockPos blockpos1 = ((BlockHitResult)this.liquid).getBlockPos();
                FluidState fluidstate = this.minecraft.level.getFluidState(blockpos1);
                list.add("");
                list.add(ChatFormatting.UNDERLINE + "Targeted Fluid: " + blockpos1.getX() + ", " + blockpos1.getY() + ", " + blockpos1.getZ());
                list.add(String.valueOf((Object)Registry.FLUID.getKey(fluidstate.getType())));

                for (Entry < Property<?>, Comparable<? >> entry1 : fluidstate.getValues().entrySet())
                {
                    list.add(this.getPropertyValueString(entry1));
                }

                Collection<ResourceLocation> collection2;

                if (Reflector.ForgeFluid_getTags.exists())
                {
                    collection2 = (Collection)Reflector.call(fluidstate.getType(), Reflector.ForgeFluid_getTags);
                }
                else
                {
                    collection2 = this.minecraft.getConnection().getTags().getOrEmpty(Registry.FLUID_REGISTRY).getMatchingTags(fluidstate.getType());
                }

                for (ResourceLocation resourcelocation1 : collection2)
                {
                    list.add("#" + resourcelocation1);
                }
            }

            Entity entity = this.minecraft.crosshairPickEntity;

            if (entity != null)
            {
                list.add("");
                list.add(ChatFormatting.UNDERLINE + "Targeted Entity");
                list.add(String.valueOf((Object)Registry.ENTITY_TYPE.getKey(entity.getType())));

                if (Reflector.ForgeEntityType_getTags.exists())
                {
                    Collection<ResourceLocation> collection = (Collection)Reflector.call(entity.getType(), Reflector.ForgeEntityType_getTags);
                    collection.forEach((t) ->
                    {
                        list.add("#" + t);
                    });
                }
            }

            return list;
        }
    }

    private String getPropertyValueString(Entry < Property<?>, Comparable<? >> pEntry)
    {
        Property<?> property = pEntry.getKey();
        Comparable<?> comparable = pEntry.getValue();
        String s = Util.getPropertyName(property, comparable);

        if (Boolean.TRUE.equals(comparable))
        {
            s = ChatFormatting.GREEN + s;
        }
        else if (Boolean.FALSE.equals(comparable))
        {
            s = ChatFormatting.RED + s;
        }

        return property.getName() + ": " + s;
    }

    private void drawChart(PoseStack p_94059_, FrameTimer p_94060_, int p_94061_, int p_94062_, boolean p_94063_)
    {
        if (!p_94063_)
        {
            int i = (int)(512.0D / this.minecraft.getWindow().getGuiScale());
            p_94061_ = Math.max(p_94061_, i);
            p_94062_ = this.minecraft.getWindow().getGuiScaledWidth() - p_94061_;
            RenderSystem.disableDepthTest();
            int j = p_94060_.getLogStart();
            int k = p_94060_.getLogEnd();
            long[] along = p_94060_.getLog();
            int l = p_94061_;
            int i1 = Math.max(0, along.length - p_94062_);
            int j1 = along.length - i1;
            int k1 = p_94060_.wrapIndex(j + i1);
            long l1 = 0L;
            int i2 = Integer.MAX_VALUE;
            int j2 = Integer.MIN_VALUE;

            for (int k2 = 0; k2 < j1; ++k2)
            {
                int l2 = (int)(along[p_94060_.wrapIndex(k1 + k2)] / 1000000L);
                i2 = Math.min(i2, l2);
                j2 = Math.max(j2, l2);
                l1 += (long)l2;
            }

            int l4 = this.minecraft.getWindow().getGuiScaledHeight();
            fill(p_94059_, p_94061_, l4 - 60, p_94061_ + j1, l4, -1873784752);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.defaultBlendFunc();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            for (Matrix4f matrix4f = Transformation.identity().getMatrix(); k1 != k; k1 = p_94060_.wrapIndex(k1 + 1))
            {
                int i3 = p_94060_.scaleSampleTo(along[k1], p_94063_ ? 30 : 60, p_94063_ ? 60 : 20);
                int j3 = p_94063_ ? 100 : 60;
                int k3 = this.getSampleColor(Mth.clamp(i3, 0, j3), 0, j3 / 2, j3);
                int l3 = k3 >> 24 & 255;
                int i4 = k3 >> 16 & 255;
                int j4 = k3 >> 8 & 255;
                int k4 = k3 & 255;
                bufferbuilder.vertex(matrix4f, (float)(l + 1), (float)l4, 0.0F).color(i4, j4, k4, l3).endVertex();
                bufferbuilder.vertex(matrix4f, (float)(l + 1), (float)(l4 - i3 + 1), 0.0F).color(i4, j4, k4, l3).endVertex();
                bufferbuilder.vertex(matrix4f, (float)l, (float)(l4 - i3 + 1), 0.0F).color(i4, j4, k4, l3).endVertex();
                bufferbuilder.vertex(matrix4f, (float)l, (float)l4, 0.0F).color(i4, j4, k4, l3).endVertex();
                ++l;
            }

            bufferbuilder.end();
            BufferUploader.end(bufferbuilder);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();

            if (p_94063_)
            {
                fill(p_94059_, p_94061_ + 1, l4 - 30 + 1, p_94061_ + 14, l4 - 30 + 10, -1873784752);
                this.font.draw(p_94059_, "60 FPS", (float)(p_94061_ + 2), (float)(l4 - 30 + 2), 14737632);
                this.hLine(p_94059_, p_94061_, p_94061_ + j1 - 1, l4 - 30, -1);
                fill(p_94059_, p_94061_ + 1, l4 - 60 + 1, p_94061_ + 14, l4 - 60 + 10, -1873784752);
                this.font.draw(p_94059_, "30 FPS", (float)(p_94061_ + 2), (float)(l4 - 60 + 2), 14737632);
                this.hLine(p_94059_, p_94061_, p_94061_ + j1 - 1, l4 - 60, -1);
            }
            else
            {
                fill(p_94059_, p_94061_ + 1, l4 - 60 + 1, p_94061_ + 14, l4 - 60 + 10, -1873784752);
                this.font.draw(p_94059_, "20 TPS", (float)(p_94061_ + 2), (float)(l4 - 60 + 2), 14737632);
                this.hLine(p_94059_, p_94061_, p_94061_ + j1 - 1, l4 - 60, -1);
            }

            this.hLine(p_94059_, p_94061_, p_94061_ + j1 - 1, l4 - 1, -1);
            this.vLine(p_94059_, p_94061_, l4 - 60, l4, -1);
            this.vLine(p_94059_, p_94061_ + j1 - 1, l4 - 60, l4, -1);

            if (p_94063_ && this.minecraft.options.framerateLimit > 0 && this.minecraft.options.framerateLimit <= 250)
            {
                this.hLine(p_94059_, p_94061_, p_94061_ + j1 - 1, l4 - 1 - (int)(1800.0D / (double)this.minecraft.options.framerateLimit), -16711681);
            }

            String s = i2 + " ms min";
            String s1 = l1 / (long)j1 + " ms avg";
            String s2 = j2 + " ms max";
            this.font.drawShadow(p_94059_, s, (float)(p_94061_ + 2), (float)(l4 - 60 - 9), 14737632);
            this.font.drawShadow(p_94059_, s1, (float)(p_94061_ + j1 / 2 - this.font.width(s1) / 2), (float)(l4 - 60 - 9), 14737632);
            this.font.drawShadow(p_94059_, s2, (float)(p_94061_ + j1 - this.font.width(s2)), (float)(l4 - 60 - 9), 14737632);
            RenderSystem.enableDepthTest();
        }
    }

    private int getSampleColor(int pHeight, int pHeightMin, int pHeightMid, int pHeightMax)
    {
        return pHeight < pHeightMid ? this.colorLerp(-16711936, -256, (float)pHeight / (float)pHeightMid) : this.colorLerp(-256, -65536, (float)(pHeight - pHeightMid) / (float)(pHeightMax - pHeightMid));
    }

    private int colorLerp(int pCol1, int pCol2, float pFactor)
    {
        int i = pCol1 >> 24 & 255;
        int j = pCol1 >> 16 & 255;
        int k = pCol1 >> 8 & 255;
        int l = pCol1 & 255;
        int i1 = pCol2 >> 24 & 255;
        int j1 = pCol2 >> 16 & 255;
        int k1 = pCol2 >> 8 & 255;
        int l1 = pCol2 & 255;
        int i2 = Mth.clamp((int)Mth.lerp(pFactor, (float)i, (float)i1), 0, 255);
        int j2 = Mth.clamp((int)Mth.lerp(pFactor, (float)j, (float)j1), 0, 255);
        int k2 = Mth.clamp((int)Mth.lerp(pFactor, (float)k, (float)k1), 0, 255);
        int l2 = Mth.clamp((int)Mth.lerp(pFactor, (float)l, (float)l1), 0, 255);
        return i2 << 24 | j2 << 16 | k2 << 8 | l2;
    }

    private static long bytesToMegabytes(long pBytes)
    {
        return pBytes / 1024L / 1024L;
    }
}
