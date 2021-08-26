package net.minecraft.server.level;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldGenRegion implements WorldGenLevel
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final List<ChunkAccess> cache;
    private final ChunkPos center;
    private final int size;
    private final ServerLevel level;
    private final long seed;
    private final LevelData levelData;
    private final Random random;
    private final DimensionType dimensionType;
    private final TickList<Block> blockTicks = new WorldGenTickList<>((p_9597_) ->
    {
        return this.getChunk(p_9597_).getBlockTicks();
    });
    private final TickList<Fluid> liquidTicks = new WorldGenTickList<>((p_9595_) ->
    {
        return this.getChunk(p_9595_).getLiquidTicks();
    });
    private final BiomeManager biomeManager;
    private final ChunkPos firstPos;
    private final ChunkPos lastPos;
    private final StructureFeatureManager structureFeatureManager;
    private final ChunkStatus generatingStatus;
    private final int writeRadiusCutoff;
    @Nullable
    private Supplier<String> currentlyGenerating;

    public WorldGenRegion(ServerLevel p_143484_, List<ChunkAccess> p_143485_, ChunkStatus p_143486_, int p_143487_)
    {
        this.generatingStatus = p_143486_;
        this.writeRadiusCutoff = p_143487_;
        int i = Mth.floor(Math.sqrt((double)p_143485_.size()));

        if (i * i != p_143485_.size())
        {
            throw(IllegalStateException)Util.pauseInIde(new IllegalStateException("Cache size is not a square."));
        }
        else
        {
            ChunkPos chunkpos = p_143485_.get(p_143485_.size() / 2).getPos();
            this.cache = p_143485_;
            this.center = chunkpos;
            this.size = i;
            this.level = p_143484_;
            this.seed = p_143484_.getSeed();
            this.levelData = p_143484_.getLevelData();
            this.random = p_143484_.getRandom();
            this.dimensionType = p_143484_.dimensionType();
            this.biomeManager = new BiomeManager(this, BiomeManager.obfuscateSeed(this.seed), p_143484_.dimensionType().getBiomeZoomer());
            this.firstPos = p_143485_.get(0).getPos();
            this.lastPos = p_143485_.get(p_143485_.size() - 1).getPos();
            this.structureFeatureManager = p_143484_.structureFeatureManager().forWorldGenRegion(this);
        }
    }

    public ChunkPos getCenter()
    {
        return this.center;
    }

    public void setCurrentlyGenerating(@Nullable Supplier<String> p_143498_)
    {
        this.currentlyGenerating = p_143498_;
    }

    public ChunkAccess getChunk(int pChunkX, int pChunkZ)
    {
        return this.getChunk(pChunkX, pChunkZ, ChunkStatus.EMPTY);
    }

    @Nullable
    public ChunkAccess getChunk(int pChunkX, int pChunkZ, ChunkStatus p_9516_, boolean p_9517_)
    {
        ChunkAccess chunkaccess;

        if (this.hasChunk(pChunkX, pChunkZ))
        {
            int i = pChunkX - this.firstPos.x;
            int j = pChunkZ - this.firstPos.z;
            chunkaccess = this.cache.get(i + j * this.size);

            if (chunkaccess.getStatus().isOrAfter(p_9516_))
            {
                return chunkaccess;
            }
        }
        else
        {
            chunkaccess = null;
        }

        if (!p_9517_)
        {
            return null;
        }
        else
        {
            LOGGER.error("Requested chunk : {} {}", pChunkX, pChunkZ);
            LOGGER.error("Region bounds : {} {} | {} {}", this.firstPos.x, this.firstPos.z, this.lastPos.x, this.lastPos.z);

            if (chunkaccess != null)
            {
                throw(RuntimeException)Util.pauseInIde(new RuntimeException(String.format("Chunk is not of correct status. Expecting %s, got %s | %s %s", p_9516_, chunkaccess.getStatus(), pChunkX, pChunkZ)));
            }
            else
            {
                throw(RuntimeException)Util.pauseInIde(new RuntimeException(String.format("We are asking a region for a chunk out of bound | %s %s", pChunkX, pChunkZ)));
            }
        }
    }

    public boolean hasChunk(int pChunkX, int pChunkZ)
    {
        return pChunkX >= this.firstPos.x && pChunkX <= this.lastPos.x && pChunkZ >= this.firstPos.z && pChunkZ <= this.lastPos.z;
    }

    public BlockState getBlockState(BlockPos pPos)
    {
        return this.getChunk(SectionPos.blockToSectionCoord(pPos.getX()), SectionPos.blockToSectionCoord(pPos.getZ())).getBlockState(pPos);
    }

    public FluidState getFluidState(BlockPos pPos)
    {
        return this.getChunk(pPos).getFluidState(pPos);
    }

    @Nullable
    public Player getNearestPlayer(double pX, double p_9502_, double pY, double p_9504_, Predicate<Entity> pZ)
    {
        return null;
    }

    public int getSkyDarken()
    {
        return 0;
    }

    public BiomeManager getBiomeManager()
    {
        return this.biomeManager;
    }

    public Biome getUncachedNoiseBiome(int pX, int pY, int pZ)
    {
        return this.level.getUncachedNoiseBiome(pX, pY, pZ);
    }

    public float getShade(Direction p_9555_, boolean p_9556_)
    {
        return 1.0F;
    }

    public LevelLightEngine getLightEngine()
    {
        return this.level.getLightEngine();
    }

    public boolean destroyBlock(BlockPos pPos, boolean pDropBlock, @Nullable Entity pEntity, int pRecursionLeft)
    {
        BlockState blockstate = this.getBlockState(pPos);

        if (blockstate.isAir())
        {
            return false;
        }
        else
        {
            if (pDropBlock)
            {
                BlockEntity blockentity = blockstate.hasBlockEntity() ? this.getBlockEntity(pPos) : null;
                Block.dropResources(blockstate, this.level, pPos, blockentity, pEntity, ItemStack.EMPTY);
            }

            return this.setBlock(pPos, Blocks.AIR.defaultBlockState(), 3, pRecursionLeft);
        }
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos pPos)
    {
        ChunkAccess chunkaccess = this.getChunk(pPos);
        BlockEntity blockentity = chunkaccess.getBlockEntity(pPos);

        if (blockentity != null)
        {
            return blockentity;
        }
        else
        {
            CompoundTag compoundtag = chunkaccess.getBlockEntityNbt(pPos);
            BlockState blockstate = chunkaccess.getBlockState(pPos);

            if (compoundtag != null)
            {
                if ("DUMMY".equals(compoundtag.getString("id")))
                {
                    if (!blockstate.hasBlockEntity())
                    {
                        return null;
                    }

                    blockentity = ((EntityBlock)blockstate.getBlock()).newBlockEntity(pPos, blockstate);
                }
                else
                {
                    blockentity = BlockEntity.loadStatic(pPos, blockstate, compoundtag);
                }

                if (blockentity != null)
                {
                    chunkaccess.setBlockEntity(blockentity);
                    return blockentity;
                }
            }

            if (blockstate.hasBlockEntity())
            {
                LOGGER.warn("Tried to access a block entity before it was created. {}", (Object)pPos);
            }

            return null;
        }
    }

    public boolean ensureCanWrite(BlockPos p_181031_)
    {
        int i = SectionPos.blockToSectionCoord(p_181031_.getX());
        int j = SectionPos.blockToSectionCoord(p_181031_.getZ());
        int k = Math.abs(this.center.x - i);
        int l = Math.abs(this.center.z - j);

        if (k <= this.writeRadiusCutoff && l <= this.writeRadiusCutoff)
        {
            return true;
        }
        else
        {
            Util.logAndPauseIfInIde("Detected setBlock in a far chunk [" + i + ", " + j + "], pos: " + p_181031_ + ", status: " + this.generatingStatus + (this.currentlyGenerating == null ? "" : ", currently generating: " + (String)this.currentlyGenerating.get()));
            return false;
        }
    }

    public boolean setBlock(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft)
    {
        if (!this.ensureCanWrite(pPos))
        {
            return false;
        }
        else
        {
            ChunkAccess chunkaccess = this.getChunk(pPos);
            BlockState blockstate = chunkaccess.setBlockState(pPos, pState, false);

            if (blockstate != null)
            {
                this.level.onBlockStateChange(pPos, blockstate, pState);
            }

            if (pState.hasBlockEntity())
            {
                if (chunkaccess.getStatus().getChunkType() == ChunkStatus.ChunkType.LEVELCHUNK)
                {
                    BlockEntity blockentity = ((EntityBlock)pState.getBlock()).newBlockEntity(pPos, pState);

                    if (blockentity != null)
                    {
                        chunkaccess.setBlockEntity(blockentity);
                    }
                    else
                    {
                        chunkaccess.removeBlockEntity(pPos);
                    }
                }
                else
                {
                    CompoundTag compoundtag = new CompoundTag();
                    compoundtag.putInt("x", pPos.getX());
                    compoundtag.putInt("y", pPos.getY());
                    compoundtag.putInt("z", pPos.getZ());
                    compoundtag.putString("id", "DUMMY");
                    chunkaccess.setBlockEntityNbt(compoundtag);
                }
            }
            else if (blockstate != null && blockstate.hasBlockEntity())
            {
                chunkaccess.removeBlockEntity(pPos);
            }

            if (pState.hasPostProcess(this, pPos))
            {
                this.markPosForPostprocessing(pPos);
            }

            return true;
        }
    }

    private void markPosForPostprocessing(BlockPos pPos)
    {
        this.getChunk(pPos).markPosForPostprocessing(pPos);
    }

    public boolean addFreshEntity(Entity pEntity)
    {
        int i = SectionPos.blockToSectionCoord(pEntity.getBlockX());
        int j = SectionPos.blockToSectionCoord(pEntity.getBlockZ());
        this.getChunk(i, j).addEntity(pEntity);
        return true;
    }

    public boolean removeBlock(BlockPos pPos, boolean pIsMoving)
    {
        return this.setBlock(pPos, Blocks.AIR.defaultBlockState(), 3);
    }

    public WorldBorder getWorldBorder()
    {
        return this.level.getWorldBorder();
    }

    public boolean isClientSide()
    {
        return false;
    }

    @Deprecated
    public ServerLevel getLevel()
    {
        return this.level;
    }

    public RegistryAccess registryAccess()
    {
        return this.level.registryAccess();
    }

    public LevelData getLevelData()
    {
        return this.levelData;
    }

    public DifficultyInstance getCurrentDifficultyAt(BlockPos pPos)
    {
        if (!this.hasChunk(SectionPos.blockToSectionCoord(pPos.getX()), SectionPos.blockToSectionCoord(pPos.getZ())))
        {
            throw new RuntimeException("We are asking a region for a chunk out of bound");
        }
        else
        {
            return new DifficultyInstance(this.level.getDifficulty(), this.level.getDayTime(), 0L, this.level.getMoonBrightness());
        }
    }

    @Nullable
    public MinecraftServer getServer()
    {
        return this.level.getServer();
    }

    public ChunkSource getChunkSource()
    {
        return this.level.getChunkSource();
    }

    public long getSeed()
    {
        return this.seed;
    }

    public TickList<Block> getBlockTicks()
    {
        return this.blockTicks;
    }

    public TickList<Fluid> getLiquidTicks()
    {
        return this.liquidTicks;
    }

    public int getSeaLevel()
    {
        return this.level.getSeaLevel();
    }

    public Random getRandom()
    {
        return this.random;
    }

    public int getHeight(Heightmap.Types pHeightmapType, int pX, int pZ)
    {
        return this.getChunk(SectionPos.blockToSectionCoord(pX), SectionPos.blockToSectionCoord(pZ)).getHeight(pHeightmapType, pX & 15, pZ & 15) + 1;
    }

    public void playSound(@Nullable Player pPlayer, BlockPos pPos, SoundEvent pSound, SoundSource pCategory, float pVolume, float pPitch)
    {
    }

    public void addParticle(ParticleOptions pParticleData, double pX, double p_9563_, double pY, double p_9565_, double pZ, double p_9567_)
    {
    }

    public void levelEvent(@Nullable Player pPlayer, int pType, BlockPos pPos, int pData)
    {
    }

    public void gameEvent(@Nullable Entity p_143490_, GameEvent p_143491_, BlockPos p_143492_)
    {
    }

    public DimensionType dimensionType()
    {
        return this.dimensionType;
    }

    public boolean isStateAtPosition(BlockPos pPos, Predicate<BlockState> pState)
    {
        return pState.test(this.getBlockState(pPos));
    }

    public boolean isFluidAtPosition(BlockPos p_143500_, Predicate<FluidState> p_143501_)
    {
        return p_143501_.test(this.getFluidState(p_143500_));
    }

    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> pEntity, AABB pBoundingBox, Predicate <? super T > pPredicate)
    {
        return Collections.emptyList();
    }

    public List<Entity> getEntities(@Nullable Entity pEntity, AABB pBoundingBox, @Nullable Predicate <? super Entity > pPredicate)
    {
        return Collections.emptyList();
    }

    public List<Player> players()
    {
        return Collections.emptyList();
    }

    public Stream <? extends StructureStart<? >> startsForFeature(SectionPos p_9558_, StructureFeature<?> p_9559_)
    {
        return this.structureFeatureManager.startsForFeature(p_9558_, p_9559_);
    }

    public int getMinBuildHeight()
    {
        return this.level.getMinBuildHeight();
    }

    public int getHeight()
    {
        return this.level.getHeight();
    }
}
