package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforgeop.client.extensions.IForgeRenderChunk;
import net.minecraftforgeop.client.model.ModelDataManager;
import net.minecraftforgeop.client.model.data.EmptyModelData;
import net.minecraftforgeop.client.model.data.IModelData;
import net.optifine.BlockPosM;
import net.optifine.Config;
import net.optifine.CustomBlockLayers;
import net.optifine.override.ChunkCacheOF;
import net.optifine.reflect.Reflector;
import net.optifine.render.AabbFrame;
import net.optifine.render.ChunkLayerMap;
import net.optifine.render.ChunkLayerSet;
import net.optifine.render.ICamera;
import net.optifine.render.RenderEnv;
import net.optifine.render.RenderTypes;
import net.optifine.shaders.SVertexBuilder;
import net.optifine.shaders.Shaders;
import net.optifine.util.ChunkUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkRenderDispatcher
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int MAX_WORKERS_32_BIT = 4;
    private static final VertexFormat VERTEX_FORMAT = DefaultVertexFormat.BLOCK;
    private final PriorityQueue<ChunkRenderDispatcher.RenderChunk.ChunkCompileTask> toBatch = Queues.newPriorityQueue();
    private final Queue<ChunkBufferBuilderPack> freeBuffers;
    private final Queue<Runnable> toUpload = Queues.newConcurrentLinkedQueue();
    private volatile int toBatchCount;
    private volatile int freeBufferCount;
    final ChunkBufferBuilderPack fixedBuffers;
    private final ProcessorMailbox<Runnable> mailbox;
    private final Executor executor;
    Level level;
    final LevelRenderer renderer;
    private Vec3 camera = Vec3.ZERO;
    private int countRenderBuilders;
    private List<ChunkBufferBuilderPack> listPausedBuilders = new ArrayList<>();
    public static final RenderType[] BLOCK_RENDER_LAYERS = RenderType.chunkBufferLayers().toArray(new RenderType[0]);
    private static final boolean FORGE = Reflector.ForgeHooksClient.exists();
    private static final boolean FORGE_CAN_RENDER_IN_LAYER_BS = Reflector.ForgeRenderTypeLookup_canRenderInLayerBs.exists();
    private static final boolean FORGE_CAN_RENDER_IN_LAYER_FS = Reflector.ForgeRenderTypeLookup_canRenderInLayerBs.exists();
    private static final boolean FORGE_SET_RENDER_LAYER = Reflector.ForgeHooksClient_setRenderLayer.exists();
    public static int renderChunksUpdated;

    public ChunkRenderDispatcher(Level p_112686_, LevelRenderer p_112687_, Executor p_112688_, boolean p_112689_, ChunkBufferBuilderPack p_112690_)
    {
        this(p_112686_, p_112687_, p_112688_, p_112689_, p_112690_, -1);
    }

    public ChunkRenderDispatcher(Level worldIn, LevelRenderer worldRendererIn, Executor executorIn, boolean java64bit, ChunkBufferBuilderPack fixedBuilderIn, int countRenderBuildersIn)
    {
        this.level = worldIn;
        this.renderer = worldRendererIn;
        int i = Math.max(1, (int)((double)Runtime.getRuntime().maxMemory() * 0.3D) / (RenderType.chunkBufferLayers().stream().mapToInt(RenderType::bufferSize).sum() * 4) - 1);
        int j = Runtime.getRuntime().availableProcessors();
        int k = java64bit ? j : Math.min(j, 4);
        int l = Math.max(1, Math.min(k, i));

        if (countRenderBuildersIn > 0)
        {
            l = countRenderBuildersIn;
        }

        this.fixedBuffers = fixedBuilderIn;
        List<ChunkBufferBuilderPack> list = Lists.newArrayListWithExpectedSize(l);

        try
        {
            for (int i1 = 0; i1 < l; ++i1)
            {
                list.add(new ChunkBufferBuilderPack());
            }
        }
        catch (OutOfMemoryError outofmemoryerror1)
        {
            LOGGER.warn("Allocated only {}/{} buffers", list.size(), l);
            int j1 = Math.min(list.size() * 2 / 3, list.size() - 1);

            for (int k1 = 0; k1 < j1; ++k1)
            {
                list.remove(list.size() - 1);
            }

            System.gc();
        }

        this.freeBuffers = Queues.newConcurrentLinkedQueue(list);
        this.freeBufferCount = this.freeBuffers.size();
        this.countRenderBuilders = this.freeBufferCount;
        this.executor = executorIn;
        this.mailbox = ProcessorMailbox.create(executorIn, "Chunk Renderer");
        this.mailbox.tell(this::runTask);
    }

    public void setLevel(Level pLevel)
    {
        this.level = pLevel;
    }

    private void runTask()
    {
        if (!this.freeBuffers.isEmpty())
        {
            ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher$renderchunk$chunkcompiletask = this.toBatch.poll();

            if (chunkrenderdispatcher$renderchunk$chunkcompiletask != null)
            {
                ChunkBufferBuilderPack chunkbufferbuilderpack = this.freeBuffers.poll();

                if (chunkbufferbuilderpack == null)
                {
                    this.toBatch.add(chunkrenderdispatcher$renderchunk$chunkcompiletask);
                    return;
                }

                this.toBatchCount = this.toBatch.size();
                this.freeBufferCount = this.freeBuffers.size();
                CompletableFuture.runAsync(() ->
                {
                }, this.executor).thenCompose((voidIn) ->
                {
                    return chunkrenderdispatcher$renderchunk$chunkcompiletask.doTask(chunkbufferbuilderpack);
                }).whenComplete((taskResultIn, throwableIn) ->
                {
                    if (throwableIn != null)
                    {
                        CrashReport crashreport = CrashReport.forThrowable(throwableIn, "Batching chunks");
                        Minecraft.getInstance().delayCrash(Minecraft.getInstance().fillReport(crashreport));
                    }
                    else {
                        this.mailbox.tell(() -> {
                            if (taskResultIn == ChunkRenderDispatcher.ChunkTaskResult.SUCCESSFUL)
                            {
                                chunkbufferbuilderpack.clearAll();
                            }
                            else {
                                chunkbufferbuilderpack.discardAll();
                            }

                            this.freeBuffers.add(chunkbufferbuilderpack);
                            this.freeBufferCount = this.freeBuffers.size();
                            this.runTask();
                        });
                    }
                });
            }
        }
    }

    public String getStats()
    {
        return String.format("pC: %03d, pU: %02d, aB: %02d", this.toBatchCount, this.toUpload.size(), this.freeBufferCount);
    }

    public int getToBatchCount()
    {
        return this.toBatchCount;
    }

    public int getToUpload()
    {
        return this.toUpload.size();
    }

    public int getFreeBufferCount()
    {
        return this.freeBufferCount;
    }

    public void setCamera(Vec3 pPos)
    {
        this.camera = pPos;
    }

    public Vec3 getCameraPosition()
    {
        return this.camera;
    }

    public boolean uploadAllPendingUploads()
    {
        boolean flag;
        Runnable runnable;

        for (flag = false; (runnable = this.toUpload.poll()) != null; flag = true)
        {
            runnable.run();
        }

        return flag;
    }

    public void rebuildChunkSync(ChunkRenderDispatcher.RenderChunk pChunkRender)
    {
        pChunkRender.compileSync();
    }

    public void blockUntilClear()
    {
        this.clearBatchQueue();
    }

    public void schedule(ChunkRenderDispatcher.RenderChunk.ChunkCompileTask pRenderTask)
    {
        this.mailbox.tell(() ->
        {
            this.toBatch.offer(pRenderTask);
            this.toBatchCount = this.toBatch.size();
            this.runTask();
        });
    }

    public CompletableFuture<Void> uploadChunkLayer(BufferBuilder pBufferBuilder, VertexBuffer pVertexBuffer)
    {
        return CompletableFuture.runAsync(() ->
        {
        }, this.toUpload::add).thenCompose((voidIn) ->
        {
            return this.doUploadChunkLayer(pBufferBuilder, pVertexBuffer);
        });
    }

    private CompletableFuture<Void> doUploadChunkLayer(BufferBuilder pBufferBuilder, VertexBuffer pVertexBuffer)
    {
        return pVertexBuffer.uploadLater(pBufferBuilder);
    }

    private void clearBatchQueue()
    {
        while (!this.toBatch.isEmpty())
        {
            ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher$renderchunk$chunkcompiletask = this.toBatch.poll();

            if (chunkrenderdispatcher$renderchunk$chunkcompiletask != null)
            {
                chunkrenderdispatcher$renderchunk$chunkcompiletask.cancel();
            }
        }

        this.toBatchCount = 0;
    }

    public boolean isQueueEmpty()
    {
        return this.toBatchCount == 0 && this.toUpload.isEmpty();
    }

    public void dispose()
    {
        this.clearBatchQueue();
        this.mailbox.close();
        this.freeBuffers.clear();
    }

    public void pauseChunkUpdates()
    {
        long i = System.currentTimeMillis();

        if (this.listPausedBuilders.size() <= 0)
        {
            while (this.listPausedBuilders.size() != this.countRenderBuilders)
            {
                this.uploadAllPendingUploads();
                ChunkBufferBuilderPack chunkbufferbuilderpack = this.freeBuffers.poll();

                if (chunkbufferbuilderpack != null)
                {
                    this.listPausedBuilders.add(chunkbufferbuilderpack);
                }

                if (System.currentTimeMillis() > i + 1000L)
                {
                    break;
                }
            }
        }
    }

    public void resumeChunkUpdates()
    {
        this.freeBuffers.addAll(this.listPausedBuilders);
        this.listPausedBuilders.clear();
    }

    public boolean updateChunkNow(ChunkRenderDispatcher.RenderChunk renderChunk)
    {
        this.rebuildChunkSync(renderChunk);
        return true;
    }

    public boolean updateChunkLater(ChunkRenderDispatcher.RenderChunk renderChunk)
    {
        if (this.freeBuffers.isEmpty())
        {
            return false;
        }
        else
        {
            renderChunk.rebuildChunkAsync(this);
            return true;
        }
    }

    public boolean updateTransparencyLater(ChunkRenderDispatcher.RenderChunk renderChunk)
    {
        return this.freeBuffers.isEmpty() ? false : renderChunk.resortTransparency(RenderTypes.TRANSLUCENT, this);
    }

    static enum ChunkTaskResult
    {
        SUCCESSFUL,
        CANCELLED;
    }

    public static class CompiledChunk
    {
        public static final ChunkRenderDispatcher.CompiledChunk UNCOMPILED = new ChunkRenderDispatcher.CompiledChunk()
        {
            public boolean facesCanSeeEachother(Direction pFacing, Direction pFacing2)
            {
                return false;
            }
            public void setAnimatedSprites(RenderType layer, BitSet animatedSprites)
            {
                throw new UnsupportedOperationException();
            }
        };
        final ChunkLayerSet hasBlocks = new ChunkLayerSet();
        final Set<RenderType> hasLayer = new ObjectArraySet<>();
        boolean isCompletelyEmpty = true;
        final List<BlockEntity> renderableBlockEntities = Lists.newArrayList();
        VisibilitySet visibilitySet = new VisibilitySet();
        @Nullable
        BufferBuilder.SortState transparencyState;
        private BitSet[] animatedSprites = new BitSet[RenderType.CHUNK_RENDER_TYPES.length];

        public boolean hasNoRenderableLayers()
        {
            return this.isCompletelyEmpty;
        }

        public boolean isEmpty(RenderType pRenderType)
        {
            return !this.hasBlocks.contains(pRenderType);
        }

        public List<BlockEntity> getRenderableBlockEntities()
        {
            return this.renderableBlockEntities;
        }

        public boolean facesCanSeeEachother(Direction pFacing, Direction pFacing2)
        {
            return this.visibilitySet.visibilityBetween(pFacing, pFacing2);
        }

        public BitSet getAnimatedSprites(RenderType layer)
        {
            return this.animatedSprites[layer.ordinal()];
        }

        public void setAnimatedSprites(RenderType layer, BitSet animatedSprites)
        {
            this.animatedSprites[layer.ordinal()] = animatedSprites;
        }

        public boolean isLayerStarted(RenderType renderTypeIn)
        {
            return this.hasLayer.contains(renderTypeIn);
        }

        public void setLayerStarted(RenderType renderTypeIn)
        {
            this.hasLayer.add(renderTypeIn);
        }

        public void setLayerUsed(RenderType renderTypeIn)
        {
            this.hasBlocks.add(renderTypeIn);
        }
    }

    public class RenderChunk implements IForgeRenderChunk
    {
        public static final int SIZE = 16;
        public final int index;
        public final AtomicReference<ChunkRenderDispatcher.CompiledChunk> compiled = new AtomicReference<>(ChunkRenderDispatcher.CompiledChunk.UNCOMPILED);
        @Nullable
        private ChunkRenderDispatcher.RenderChunk.RebuildTask lastRebuildTask;
        @Nullable
        private ChunkRenderDispatcher.RenderChunk.ResortTransparencyTask lastResortTransparencyTask;
        private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
        private final ChunkLayerMap<VertexBuffer> buffers = new ChunkLayerMap<>((renderType) ->
        {
            return new VertexBuffer();
        });
        public AABB bb;
        private int lastFrame = -1;
        private boolean dirty = true;
        final BlockPos.MutableBlockPos origin = new BlockPos.MutableBlockPos(-1, -1, -1);
        private final BlockPos.MutableBlockPos[] relativeOrigins = Util.make(new BlockPos.MutableBlockPos[6], (posArrIn) ->
        {
            for (int i = 0; i < posArrIn.length; ++i)
            {
                posArrIn[i] = new BlockPos.MutableBlockPos();
            }
        });
        private boolean playerChanged;
        private final boolean isMipmaps = Config.isMipmaps();
        private final boolean fixBlockLayer = !Reflector.BetterFoliageClient.exists();
        private boolean playerUpdate = false;
        private boolean renderRegions = Config.isRenderRegions();
        public int regionX;
        public int regionZ;
        public int regionDX;
        public int regionDY;
        public int regionDZ;
        private final ChunkRenderDispatcher.RenderChunk[] renderChunksOfset16 = new ChunkRenderDispatcher.RenderChunk[6];
        private boolean renderChunksOffset16Updated = false;
        private LevelChunk chunk;
        private ChunkRenderDispatcher.RenderChunk[] renderChunkNeighbours = new ChunkRenderDispatcher.RenderChunk[Direction.VALUES.length];
        private ChunkRenderDispatcher.RenderChunk[] renderChunkNeighboursValid = new ChunkRenderDispatcher.RenderChunk[Direction.VALUES.length];
        private boolean renderChunkNeighboursUpated = false;
        private LevelRenderer.RenderChunkInfo renderInfo = new LevelRenderer.RenderChunkInfo(this, (Direction)null, 0);
        public AabbFrame boundingBoxParent;

        public RenderChunk(int p_173720_)
        {
            this.index = p_173720_;
        }

        private boolean doesChunkExistAt(BlockPos pBlockPos)
        {
            return ChunkRenderDispatcher.this.level.getChunk(SectionPos.blockToSectionCoord(pBlockPos.getX()), SectionPos.blockToSectionCoord(pBlockPos.getZ()), ChunkStatus.FULL, false) != null;
        }

        public boolean hasAllNeighbors()
        {
            int i = 24;

            if (!(this.getDistToPlayerSqr() > 576.0D))
            {
                return true;
            }
            else
            {
                return this.doesChunkExistAt(this.relativeOrigins[Direction.WEST.ordinal()]) && this.doesChunkExistAt(this.relativeOrigins[Direction.NORTH.ordinal()]) && this.doesChunkExistAt(this.relativeOrigins[Direction.EAST.ordinal()]) && this.doesChunkExistAt(this.relativeOrigins[Direction.SOUTH.ordinal()]);
            }
        }

        public boolean setFrame(int pFrameIndex)
        {
            if (this.lastFrame == pFrameIndex)
            {
                return false;
            }
            else
            {
                this.lastFrame = pFrameIndex;
                return true;
            }
        }

        public VertexBuffer getBuffer(RenderType pRenderType)
        {
            return this.buffers.get(pRenderType);
        }

        public void setOrigin(int pX, int pY, int pZ)
        {
            if (pX != this.origin.getX() || pY != this.origin.getY() || pZ != this.origin.getZ())
            {
                this.reset();
                this.origin.set(pX, pY, pZ);

                if (this.renderRegions)
                {
                    int i = 8;
                    this.regionX = pX >> i << i;
                    this.regionZ = pZ >> i << i;
                    this.regionDX = pX - this.regionX;
                    this.regionDY = pY;
                    this.regionDZ = pZ - this.regionZ;
                }

                this.bb = new AABB((double)pX, (double)pY, (double)pZ, (double)(pX + 16), (double)(pY + 16), (double)(pZ + 16));

                for (Direction direction : Direction.VALUES)
                {
                    this.relativeOrigins[direction.ordinal()].set(this.origin).move(direction, 16);
                }

                this.renderChunksOffset16Updated = false;
                this.renderChunkNeighboursUpated = false;

                for (int j = 0; j < this.renderChunkNeighbours.length; ++j)
                {
                    ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = this.renderChunkNeighbours[j];

                    if (chunkrenderdispatcher$renderchunk != null)
                    {
                        chunkrenderdispatcher$renderchunk.renderChunkNeighboursUpated = false;
                    }
                }

                this.chunk = null;
                this.boundingBoxParent = null;
            }
        }

        protected double getDistToPlayerSqr()
        {
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            double d0 = this.bb.minX + 8.0D - camera.getPosition().x;
            double d1 = this.bb.minY + 8.0D - camera.getPosition().y;
            double d2 = this.bb.minZ + 8.0D - camera.getPosition().z;
            return d0 * d0 + d1 * d1 + d2 * d2;
        }

        void beginLayer(BufferBuilder pBufferBuilder)
        {
            pBufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        }

        public ChunkRenderDispatcher.CompiledChunk getCompiledChunk()
        {
            return this.compiled.get();
        }

        private void reset()
        {
            this.cancelTasks();
            this.compiled.set(ChunkRenderDispatcher.CompiledChunk.UNCOMPILED);
            this.dirty = true;
        }

        public void releaseBuffers()
        {
            this.reset();
            this.buffers.values().forEach(VertexBuffer::close);
        }

        public BlockPos getOrigin()
        {
            return this.origin;
        }

        public void setDirty(boolean pImmediate)
        {
            boolean flag = this.dirty;
            this.dirty = true;
            this.playerChanged = pImmediate | (flag && this.playerChanged);

            if (this.isWorldPlayerUpdate())
            {
                this.playerUpdate = true;
            }
        }

        public void setNotDirty()
        {
            this.dirty = false;
            this.playerChanged = false;
            this.playerUpdate = false;
        }

        public boolean isDirty()
        {
            return this.dirty;
        }

        public boolean isDirtyFromPlayer()
        {
            return this.dirty && this.playerChanged;
        }

        public BlockPos getRelativeOrigin(Direction pFacing)
        {
            return this.relativeOrigins[pFacing.ordinal()];
        }

        public boolean resortTransparency(RenderType pRenderType, ChunkRenderDispatcher pRenderDispatcher)
        {
            ChunkRenderDispatcher.CompiledChunk chunkrenderdispatcher$compiledchunk = this.getCompiledChunk();

            if (this.lastResortTransparencyTask != null)
            {
                this.lastResortTransparencyTask.cancel();
            }

            if (!chunkrenderdispatcher$compiledchunk.hasLayer.contains(pRenderType))
            {
                return false;
            }
            else
            {
                if (ChunkRenderDispatcher.FORGE)
                {
                    this.lastResortTransparencyTask = new ChunkRenderDispatcher.RenderChunk.ResortTransparencyTask(new ChunkPos(this.getOrigin()), this.getDistToPlayerSqr(), chunkrenderdispatcher$compiledchunk);
                }
                else
                {
                    this.lastResortTransparencyTask = new ChunkRenderDispatcher.RenderChunk.ResortTransparencyTask(this.getDistToPlayerSqr(), chunkrenderdispatcher$compiledchunk);
                }

                pRenderDispatcher.schedule(this.lastResortTransparencyTask);
                return true;
            }
        }

        protected void cancelTasks()
        {
            if (this.lastRebuildTask != null)
            {
                this.lastRebuildTask.cancel();
                this.lastRebuildTask = null;
            }

            if (this.lastResortTransparencyTask != null)
            {
                this.lastResortTransparencyTask.cancel();
                this.lastResortTransparencyTask = null;
            }
        }

        public ChunkRenderDispatcher.RenderChunk.ChunkCompileTask createCompileTask()
        {
            this.cancelTasks();
            BlockPos blockpos = this.origin.immutable();
            int i = 1;
            RenderChunkRegion renderchunkregion = null;

            if (ChunkRenderDispatcher.FORGE)
            {
                this.lastRebuildTask = new ChunkRenderDispatcher.RenderChunk.RebuildTask(new ChunkPos(this.getOrigin()), this.getDistToPlayerSqr(), renderchunkregion);
            }
            else
            {
                this.lastRebuildTask = new ChunkRenderDispatcher.RenderChunk.RebuildTask(this.getDistToPlayerSqr(), renderchunkregion);
            }

            return this.lastRebuildTask;
        }

        public void rebuildChunkAsync(ChunkRenderDispatcher pDispatcher)
        {
            ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher$renderchunk$chunkcompiletask = this.createCompileTask();
            pDispatcher.schedule(chunkrenderdispatcher$renderchunk$chunkcompiletask);
        }

        void updateGlobalBlockEntities(Set<BlockEntity> pGlobalEntities)
        {
            Set<BlockEntity> set = Sets.newHashSet(pGlobalEntities);
            Set<BlockEntity> set1 = Sets.newHashSet(this.globalBlockEntities);
            set.removeAll(this.globalBlockEntities);
            set1.removeAll(pGlobalEntities);
            this.globalBlockEntities.clear();
            this.globalBlockEntities.addAll(pGlobalEntities);
            ChunkRenderDispatcher.this.renderer.updateGlobalBlockEntities(set1, set);
        }

        public void compileSync()
        {
            ChunkRenderDispatcher.RenderChunk.ChunkCompileTask chunkrenderdispatcher$renderchunk$chunkcompiletask = this.createCompileTask();
            chunkrenderdispatcher$renderchunk$chunkcompiletask.doTask(ChunkRenderDispatcher.this.fixedBuffers);
        }

        private boolean isWorldPlayerUpdate()
        {
            if (ChunkRenderDispatcher.this.level instanceof ClientLevel)
            {
                ClientLevel clientlevel = (ClientLevel)ChunkRenderDispatcher.this.level;
                return clientlevel.isPlayerUpdate();
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

        private RenderType[] getFluidRenderLayers(FluidState fluidState, RenderType[] singleLayer)
        {
            if (ChunkRenderDispatcher.FORGE_CAN_RENDER_IN_LAYER_FS)
            {
                return ChunkRenderDispatcher.BLOCK_RENDER_LAYERS;
            }
            else
            {
                singleLayer[0] = ItemBlockRenderTypes.getRenderLayer(fluidState);
                return singleLayer;
            }
        }

        private RenderType[] getBlockRenderLayers(BlockState blockState, RenderType[] singleLayer)
        {
            if (ChunkRenderDispatcher.FORGE_CAN_RENDER_IN_LAYER_BS)
            {
                return ChunkRenderDispatcher.BLOCK_RENDER_LAYERS;
            }
            else
            {
                singleLayer[0] = ItemBlockRenderTypes.getChunkRenderType(blockState);
                return singleLayer;
            }
        }

        private RenderType fixBlockLayer(BlockGetter worldReader, BlockState blockState, BlockPos blockPos, RenderType layer)
        {
            if (CustomBlockLayers.isActive())
            {
                RenderType rendertype = CustomBlockLayers.getRenderLayer(worldReader, blockState, blockPos);

                if (rendertype != null)
                {
                    return rendertype;
                }
            }

            if (!this.fixBlockLayer)
            {
                return layer;
            }
            else
            {
                if (this.isMipmaps)
                {
                    if (layer == RenderTypes.CUTOUT)
                    {
                        Block block = blockState.getBlock();

                        if (block instanceof RedStoneWireBlock)
                        {
                            return layer;
                        }

                        if (block instanceof CactusBlock)
                        {
                            return layer;
                        }

                        return RenderTypes.CUTOUT_MIPPED;
                    }
                }
                else if (layer == RenderTypes.CUTOUT_MIPPED)
                {
                    return RenderTypes.CUTOUT;
                }

                return layer;
            }
        }

        private void postRenderOverlays(ChunkBufferBuilderPack regionRenderCacheBuilder, ChunkRenderDispatcher.CompiledChunk compiledChunk)
        {
            this.postRenderOverlay(RenderTypes.CUTOUT, regionRenderCacheBuilder, compiledChunk);
            this.postRenderOverlay(RenderTypes.CUTOUT_MIPPED, regionRenderCacheBuilder, compiledChunk);
            this.postRenderOverlay(RenderTypes.TRANSLUCENT, regionRenderCacheBuilder, compiledChunk);
        }

        private void postRenderOverlay(RenderType layer, ChunkBufferBuilderPack regionRenderCacheBuilder, ChunkRenderDispatcher.CompiledChunk compiledchunk)
        {
            BufferBuilder bufferbuilder = regionRenderCacheBuilder.builder(layer);

            if (bufferbuilder.building())
            {
                compiledchunk.setLayerStarted(layer);

                if (bufferbuilder.getVertexCount() > 0)
                {
                    compiledchunk.setLayerUsed(layer);
                }
            }
        }

        private ChunkCacheOF makeChunkCacheOF(BlockPos posIn)
        {
            BlockPos blockpos = posIn.offset(-1, -1, -1);
            BlockPos blockpos1 = posIn.offset(16, 16, 16);
            RenderChunkRegion renderchunkregion = this.createRegionRenderCache(ChunkRenderDispatcher.this.level, blockpos, blockpos1, 1);
            return new ChunkCacheOF(renderchunkregion, blockpos, blockpos1, 1);
        }

        public RenderChunkRegion createRegionRenderCache(Level world, BlockPos posFrom, BlockPos posTo, int i)
        {
            return RenderChunkRegion.generateCache(world, posFrom, posTo, i, false);
        }

        public ChunkRenderDispatcher.RenderChunk getRenderChunkOffset16(ViewArea viewFrustum, Direction facing)
        {
            if (!this.renderChunksOffset16Updated)
            {
                for (int i = 0; i < Direction.VALUES.length; ++i)
                {
                    Direction direction = Direction.VALUES[i];
                    BlockPos blockpos = this.getRelativeOrigin(direction);
                    this.renderChunksOfset16[i] = viewFrustum.getRenderChunkAt(blockpos);
                }

                this.renderChunksOffset16Updated = true;
            }

            return this.renderChunksOfset16[facing.ordinal()];
        }

        public LevelChunk getChunk()
        {
            return this.getChunk(this.origin);
        }

        private LevelChunk getChunk(BlockPos posIn)
        {
            LevelChunk levelchunk = this.chunk;

            if (levelchunk != null && ChunkUtils.isLoaded(levelchunk))
            {
                return levelchunk;
            }
            else
            {
                levelchunk = ChunkRenderDispatcher.this.level.getChunkAt(posIn);
                this.chunk = levelchunk;
                return levelchunk;
            }
        }

        public boolean isChunkRegionEmpty()
        {
            return this.isChunkRegionEmpty(this.origin);
        }

        private boolean isChunkRegionEmpty(BlockPos posIn)
        {
            int i = posIn.getY();
            int j = i + 15;
            return this.getChunk(posIn).isYSpaceEmpty(i, j);
        }

        public void setRenderChunkNeighbour(Direction facing, ChunkRenderDispatcher.RenderChunk neighbour)
        {
            this.renderChunkNeighbours[facing.ordinal()] = neighbour;
            this.renderChunkNeighboursValid[facing.ordinal()] = neighbour;
        }

        public ChunkRenderDispatcher.RenderChunk getRenderChunkNeighbour(Direction facing)
        {
            if (!this.renderChunkNeighboursUpated)
            {
                this.updateRenderChunkNeighboursValid();
            }

            return this.renderChunkNeighboursValid[facing.ordinal()];
        }

        public LevelRenderer.RenderChunkInfo getRenderInfo()
        {
            return this.renderInfo;
        }

        private void updateRenderChunkNeighboursValid()
        {
            int i = this.getOrigin().getX();
            int j = this.getOrigin().getZ();
            int k = Direction.NORTH.ordinal();
            int l = Direction.SOUTH.ordinal();
            int i1 = Direction.WEST.ordinal();
            int j1 = Direction.EAST.ordinal();
            this.renderChunkNeighboursValid[k] = this.renderChunkNeighbours[k].getOrigin().getZ() == j - 16 ? this.renderChunkNeighbours[k] : null;
            this.renderChunkNeighboursValid[l] = this.renderChunkNeighbours[l].getOrigin().getZ() == j + 16 ? this.renderChunkNeighbours[l] : null;
            this.renderChunkNeighboursValid[i1] = this.renderChunkNeighbours[i1].getOrigin().getX() == i - 16 ? this.renderChunkNeighbours[i1] : null;
            this.renderChunkNeighboursValid[j1] = this.renderChunkNeighbours[j1].getOrigin().getX() == i + 16 ? this.renderChunkNeighbours[j1] : null;
            this.renderChunkNeighboursUpated = true;
        }

        public boolean isBoundingBoxInFrustum(ICamera camera, int frameCount)
        {
            return this.getBoundingBoxParent().isBoundingBoxInFrustumFully(camera, frameCount) ? true : camera.isBoundingBoxInFrustum(this.bb);
        }

        public AabbFrame getBoundingBoxParent()
        {
            if (this.boundingBoxParent == null)
            {
                BlockPos blockpos = this.getOrigin();
                int i = blockpos.getX();
                int j = blockpos.getY();
                int k = blockpos.getZ();
                int l = 5;
                int i1 = i >> l << l;
                int j1 = j >> l << l;
                int k1 = k >> l << l;

                if (i1 != i || j1 != j || k1 != k)
                {
                    AabbFrame aabbframe = ChunkRenderDispatcher.this.renderer.getRenderChunk(new BlockPos(i1, j1, k1)).getBoundingBoxParent();

                    if (aabbframe != null && aabbframe.minX == (double)i1 && aabbframe.minY == (double)j1 && aabbframe.minZ == (double)k1)
                    {
                        this.boundingBoxParent = aabbframe;
                    }
                }

                if (this.boundingBoxParent == null)
                {
                    int l1 = 1 << l;
                    this.boundingBoxParent = new AabbFrame((double)i1, (double)j1, (double)k1, (double)(i1 + l1), (double)(j1 + l1), (double)(k1 + l1));
                }
            }

            return this.boundingBoxParent;
        }

        public Level getWorld()
        {
            return ChunkRenderDispatcher.this.level;
        }

        public String toString()
        {
            return "pos: " + this.getOrigin() + ", frameIndex: " + this.lastFrame;
        }

        abstract class ChunkCompileTask implements Comparable<ChunkRenderDispatcher.RenderChunk.ChunkCompileTask>
        {
            protected final double distAtCreation;
            protected final AtomicBoolean isCancelled = new AtomicBoolean(false);
            protected Map<BlockPos, IModelData> modelData;

            public ChunkCompileTask(double p_112852_)
            {
                this((ChunkPos)null, p_112852_);
            }

            public ChunkCompileTask(ChunkPos pos, double distanceSqIn)
            {
                this.distAtCreation = distanceSqIn;

                if (pos == null)
                {
                    this.modelData = Collections.emptyMap();
                }
                else
                {
                    this.modelData = ModelDataManager.getModelData(Minecraft.getInstance().level, pos);
                }
            }

            public abstract CompletableFuture<ChunkRenderDispatcher.ChunkTaskResult> doTask(ChunkBufferBuilderPack pBuilder);

            public abstract void cancel();

            public int compareTo(ChunkRenderDispatcher.RenderChunk.ChunkCompileTask p_112855_)
            {
                return Doubles.compare(this.distAtCreation, p_112855_.distAtCreation);
            }

            public IModelData getModelData(BlockPos pos)
            {
                return this.modelData.getOrDefault(pos, EmptyModelData.INSTANCE);
            }
        }

        class RebuildTask extends ChunkRenderDispatcher.RenderChunk.ChunkCompileTask
        {
            @Nullable
            protected RenderChunkRegion region;

            public RebuildTask(double p_112862_, RenderChunkRegion p_112863_)
            {
                this((ChunkPos)null, p_112862_, p_112863_);
            }

            public RebuildTask(ChunkPos pos, @Nullable double distanceSqIn, RenderChunkRegion renderCacheIn)
            {
                super(pos, distanceSqIn);
                this.region = renderCacheIn;
            }

            public CompletableFuture<ChunkRenderDispatcher.ChunkTaskResult> doTask(ChunkBufferBuilderPack pBuilder)
            {
                if (this.isCancelled.get())
                {
                    return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
                }
                else if (!RenderChunk.this.hasAllNeighbors())
                {
                    this.region = null;
                    RenderChunk.this.setDirty(false);
                    this.isCancelled.set(true);
                    return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
                }
                else if (this.isCancelled.get())
                {
                    return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
                }
                else
                {
                    Vec3 vec3 = ChunkRenderDispatcher.this.getCameraPosition();
                    float f = (float)vec3.x;
                    float f1 = (float)vec3.y;
                    float f2 = (float)vec3.z;
                    ChunkRenderDispatcher.CompiledChunk chunkrenderdispatcher$compiledchunk = new ChunkRenderDispatcher.CompiledChunk();
                    Set<BlockEntity> set = this.compile(f, f1, f2, chunkrenderdispatcher$compiledchunk, pBuilder);
                    RenderChunk.this.updateGlobalBlockEntities(set);

                    if (this.isCancelled.get())
                    {
                        return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
                    }
                    else
                    {
                        List<CompletableFuture<Void>> list = Lists.newArrayList();
                        chunkrenderdispatcher$compiledchunk.hasLayer.forEach((renderTypeIn) ->
                        {
                            list.add(ChunkRenderDispatcher.this.uploadChunkLayer(pBuilder.builder(renderTypeIn), RenderChunk.this.getBuffer(renderTypeIn)));
                        });
                        return Util.sequenceFailFast(list).handle((listIn, throwableIn) ->
                        {
                            if (throwableIn != null && !(throwableIn instanceof CancellationException) && !(throwableIn instanceof InterruptedException))
                            {
                                Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwableIn, "Rendering chunk"));
                            }

                            if (this.isCancelled.get())
                            {
                                return ChunkRenderDispatcher.ChunkTaskResult.CANCELLED;
                            }
                            else {
                                RenderChunk.this.compiled.set(chunkrenderdispatcher$compiledchunk);
                                return ChunkRenderDispatcher.ChunkTaskResult.SUCCESSFUL;
                            }
                        });
                    }
                }
            }

            private Set<BlockEntity> compile(float pX, float pY, float pZ, ChunkRenderDispatcher.CompiledChunk pCompiledChunk, ChunkBufferBuilderPack pBuilder)
            {
                int i = 1;
                BlockPos blockpos = RenderChunk.this.origin.immutable();
                BlockPos blockpos1 = blockpos.offset(15, 15, 15);
                VisGraph visgraph = new VisGraph();
                Set<BlockEntity> set = Sets.newHashSet();
                this.region = null;
                PoseStack posestack = new PoseStack();

                if (!RenderChunk.this.isChunkRegionEmpty(blockpos))
                {
                    ++ChunkRenderDispatcher.renderChunksUpdated;
                    ChunkCacheOF chunkcacheof = RenderChunk.this.makeChunkCacheOF(blockpos);
                    chunkcacheof.renderStart();
                    RenderType[] arendertype = new RenderType[1];
                    boolean flag = Config.isShaders();
                    boolean flag1 = flag && Shaders.useMidBlockAttrib;
                    ModelBlockRenderer.enableCaching();
                    Random random = new Random();
                    BlockRenderDispatcher blockrenderdispatcher = Minecraft.getInstance().getBlockRenderer();

                    for (BlockPosM blockposm : (Iterable<BlockPosM>)BlockPosM.getAllInBoxMutable(blockpos, blockpos1))
                    {
                        BlockState blockstate = chunkcacheof.getBlockState(blockposm);

                        if (!blockstate.isAir())
                        {
                            if (blockstate.isSolidRender(chunkcacheof, blockposm))
                            {
                                visgraph.setOpaque(blockposm);
                            }

                            if (blockstate.hasBlockEntity())
                            {
                                BlockEntity blockentity = chunkcacheof.getTileEntity(blockposm, LevelChunk.EntityCreationType.CHECK);

                                if (blockentity != null)
                                {
                                    this.handleBlockEntity(pCompiledChunk, set, blockentity);
                                }
                            }

                            FluidState fluidstate = blockstate.getFluidState();
                            IModelData imodeldata = ChunkRenderDispatcher.FORGE ? this.getModelData(blockposm) : null;

                            if (!fluidstate.isEmpty())
                            {
                                RenderType[] arendertype1 = RenderChunk.this.getFluidRenderLayers(fluidstate, arendertype);

                                for (int j = 0; j < arendertype1.length; ++j)
                                {
                                    RenderType rendertype = arendertype1[j];

                                    if (!ChunkRenderDispatcher.FORGE_CAN_RENDER_IN_LAYER_FS || Reflector.callBoolean(Reflector.ForgeRenderTypeLookup_canRenderInLayerFs, fluidstate, rendertype))
                                    {
                                        if (ChunkRenderDispatcher.FORGE_SET_RENDER_LAYER)
                                        {
                                            Reflector.callVoid(Reflector.ForgeHooksClient_setRenderLayer, rendertype);
                                        }

                                        BufferBuilder bufferbuilder = pBuilder.builder(rendertype);
                                        bufferbuilder.setBlockLayer(rendertype);
                                        RenderEnv renderenv = bufferbuilder.getRenderEnv(blockstate, blockposm);
                                        renderenv.setRegionRenderCacheBuilder(pBuilder);
                                        chunkcacheof.setRenderEnv(renderenv);

                                        if (pCompiledChunk.hasLayer.add(rendertype))
                                        {
                                            RenderChunk.this.beginLayer(bufferbuilder);
                                        }

                                        if (blockrenderdispatcher.renderLiquid(blockposm, chunkcacheof, bufferbuilder, fluidstate))
                                        {
                                            pCompiledChunk.isCompletelyEmpty = false;
                                            pCompiledChunk.hasBlocks.add(rendertype);
                                        }
                                    }
                                }
                            }

                            if (blockstate.getRenderShape() != RenderShape.INVISIBLE)
                            {
                                RenderType[] arendertype2 = RenderChunk.this.getBlockRenderLayers(blockstate, arendertype);

                                for (int k = 0; k < arendertype2.length; ++k)
                                {
                                    RenderType rendertype3 = arendertype2[k];

                                    if (!ChunkRenderDispatcher.FORGE_CAN_RENDER_IN_LAYER_BS || Reflector.callBoolean(Reflector.ForgeRenderTypeLookup_canRenderInLayerBs, blockstate, rendertype3))
                                    {
                                        if (ChunkRenderDispatcher.FORGE_SET_RENDER_LAYER)
                                        {
                                            Reflector.callVoid(Reflector.ForgeHooksClient_setRenderLayer, rendertype3);
                                        }

                                        rendertype3 = RenderChunk.this.fixBlockLayer(chunkcacheof, blockstate, blockposm, rendertype3);
                                        BufferBuilder bufferbuilder3 = pBuilder.builder(rendertype3);
                                        bufferbuilder3.setBlockLayer(rendertype3);
                                        RenderEnv renderenv1 = bufferbuilder3.getRenderEnv(blockstate, blockposm);
                                        renderenv1.setRegionRenderCacheBuilder(pBuilder);
                                        chunkcacheof.setRenderEnv(renderenv1);

                                        if (pCompiledChunk.hasLayer.add(rendertype3))
                                        {
                                            RenderChunk.this.beginLayer(bufferbuilder3);
                                        }

                                        posestack.pushPose();
                                        posestack.translate((double)RenderChunk.this.regionDX + (double)(blockposm.getX() & 15), (double)RenderChunk.this.regionDY + (double)(blockposm.getY() & 15), (double)RenderChunk.this.regionDZ + (double)(blockposm.getZ() & 15));

                                        if (flag1)
                                        {
                                            bufferbuilder3.setMidBlock(0.5F + (float)RenderChunk.this.regionDX + (float)(blockposm.getX() & 15), 0.5F + (float)RenderChunk.this.regionDY + (float)(blockposm.getY() & 15), 0.5F + (float)RenderChunk.this.regionDZ + (float)(blockposm.getZ() & 15));
                                        }

                                        if (blockrenderdispatcher.renderBatched(blockstate, blockposm, chunkcacheof, posestack, bufferbuilder3, true, random, imodeldata))
                                        {
                                            pCompiledChunk.isCompletelyEmpty = false;
                                            pCompiledChunk.hasBlocks.add(rendertype3);

                                            if (renderenv1.isOverlaysRendered())
                                            {
                                                RenderChunk.this.postRenderOverlays(pBuilder, pCompiledChunk);
                                                renderenv1.setOverlaysRendered(false);
                                            }
                                        }

                                        posestack.popPose();
                                    }
                                }
                            }

                            if (ChunkRenderDispatcher.FORGE_SET_RENDER_LAYER)
                            {
                                Reflector.callVoid(Reflector.ForgeHooksClient_setRenderLayer, (Object)null);
                            }
                        }
                    }

                    if (pCompiledChunk.hasBlocks.contains(RenderType.translucent()))
                    {
                        BufferBuilder bufferbuilder1 = pBuilder.builder(RenderType.translucent());
                        bufferbuilder1.setQuadSortOrigin((float)RenderChunk.this.regionDX + pX - (float)blockpos.getX(), (float)RenderChunk.this.regionDY + pY - (float)blockpos.getY(), (float)RenderChunk.this.regionDZ + pZ - (float)blockpos.getZ());
                        pCompiledChunk.transparencyState = bufferbuilder1.getSortState();
                    }

                    pCompiledChunk.hasLayer.stream().map(pBuilder::builder).forEach(BufferBuilder::end);

                    for (RenderType rendertype2 : ChunkRenderDispatcher.BLOCK_RENDER_LAYERS)
                    {
                        pCompiledChunk.setAnimatedSprites(rendertype2, (BitSet)null);
                    }

                    for (RenderType rendertype1 : pCompiledChunk.hasLayer)
                    {
                        if (Config.isShaders())
                        {
                            SVertexBuilder.calcNormalChunkLayer(pBuilder.builder(rendertype1));
                        }

                        BufferBuilder bufferbuilder2 = pBuilder.builder(rendertype1);

                        if (bufferbuilder2.animatedSprites != null && !bufferbuilder2.animatedSprites.isEmpty())
                        {
                            pCompiledChunk.setAnimatedSprites(rendertype1, (BitSet)bufferbuilder2.animatedSprites.clone());
                        }
                    }

                    chunkcacheof.renderFinish();
                    ModelBlockRenderer.clearCache();
                }

                pCompiledChunk.visibilitySet = visgraph.resolve();
                return set;
            }

            private <E extends BlockEntity> void handleBlockEntity(ChunkRenderDispatcher.CompiledChunk pCompiledChunk, Set<BlockEntity> pBlockEntities, E pBlockEntity)
            {
                BlockEntityRenderer<E> blockentityrenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(pBlockEntity);

                if (blockentityrenderer != null)
                {
                    if (blockentityrenderer.shouldRenderOffScreen(pBlockEntity))
                    {
                        pBlockEntities.add(pBlockEntity);
                    }
                    else
                    {
                        pCompiledChunk.renderableBlockEntities.add(pBlockEntity);
                    }
                }
            }

            public void cancel()
            {
                this.region = null;

                if (this.isCancelled.compareAndSet(false, true))
                {
                    RenderChunk.this.setDirty(false);
                }
            }
        }

        class ResortTransparencyTask extends ChunkRenderDispatcher.RenderChunk.ChunkCompileTask
        {
            private final ChunkRenderDispatcher.CompiledChunk compiledChunk;

            public ResortTransparencyTask(double p_112889_, ChunkRenderDispatcher.CompiledChunk p_112890_)
            {
                this((ChunkPos)null, p_112889_, p_112890_);
            }

            public ResortTransparencyTask(ChunkPos pos, double distanceSqIn, ChunkRenderDispatcher.CompiledChunk compiledChunkIn)
            {
                super(pos, distanceSqIn);
                this.compiledChunk = compiledChunkIn;
            }

            public CompletableFuture<ChunkRenderDispatcher.ChunkTaskResult> doTask(ChunkBufferBuilderPack pBuilder)
            {
                if (this.isCancelled.get())
                {
                    return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
                }
                else if (!RenderChunk.this.hasAllNeighbors())
                {
                    this.isCancelled.set(true);
                    return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
                }
                else if (this.isCancelled.get())
                {
                    return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
                }
                else
                {
                    Vec3 vec3 = ChunkRenderDispatcher.this.getCameraPosition();
                    float f = (float)vec3.x;
                    float f1 = (float)vec3.y;
                    float f2 = (float)vec3.z;
                    BufferBuilder.SortState bufferbuilder$sortstate = this.compiledChunk.transparencyState;

                    if (bufferbuilder$sortstate != null && this.compiledChunk.hasBlocks.contains(RenderType.translucent()))
                    {
                        BufferBuilder bufferbuilder = pBuilder.builder(RenderType.translucent());
                        bufferbuilder.setBlockLayer(RenderType.translucent());
                        RenderChunk.this.beginLayer(bufferbuilder);
                        bufferbuilder.restoreSortState(bufferbuilder$sortstate);
                        bufferbuilder.setQuadSortOrigin((float)RenderChunk.this.regionDX + f - (float)RenderChunk.this.origin.getX(), (float)RenderChunk.this.regionDY + f1 - (float)RenderChunk.this.origin.getY(), (float)RenderChunk.this.regionDZ + f2 - (float)RenderChunk.this.origin.getZ());
                        this.compiledChunk.transparencyState = bufferbuilder.getSortState();
                        bufferbuilder.end();

                        if (this.isCancelled.get())
                        {
                            return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
                        }
                        else
                        {
                            CompletableFuture<ChunkRenderDispatcher.ChunkTaskResult> completablefuture = ChunkRenderDispatcher.this.uploadChunkLayer(pBuilder.builder(RenderType.translucent()), RenderChunk.this.getBuffer(RenderType.translucent())).thenApply((voidIn) ->
                            {
                                return ChunkRenderDispatcher.ChunkTaskResult.CANCELLED;
                            });
                            return completablefuture.handle((taskResultIn, throwableIn) ->
                            {
                                if (throwableIn != null && !(throwableIn instanceof CancellationException) && !(throwableIn instanceof InterruptedException))
                                {
                                    Minecraft.getInstance().delayCrash(CrashReport.forThrowable(throwableIn, "Rendering chunk"));
                                }

                                return this.isCancelled.get() ? ChunkRenderDispatcher.ChunkTaskResult.CANCELLED : ChunkRenderDispatcher.ChunkTaskResult.SUCCESSFUL;
                            });
                        }
                    }
                    else
                    {
                        return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
                    }
                }
            }

            public void cancel()
            {
                this.isCancelled.set(true);
            }
        }
    }
}
