package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.BitSet;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class ImposterProtoChunk extends ProtoChunk
{
    private final LevelChunk wrapped;

    public ImposterProtoChunk(LevelChunk p_62687_)
    {
        super(p_62687_.getPos(), UpgradeData.EMPTY, p_62687_);
        this.wrapped = p_62687_;
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos pPos)
    {
        return this.wrapped.getBlockEntity(pPos);
    }

    @Nullable
    public BlockState getBlockState(BlockPos pPos)
    {
        return this.wrapped.getBlockState(pPos);
    }

    public FluidState getFluidState(BlockPos pPos)
    {
        return this.wrapped.getFluidState(pPos);
    }

    public int getMaxLightLevel()
    {
        return this.wrapped.getMaxLightLevel();
    }

    @Nullable
    public BlockState setBlockState(BlockPos pPos, BlockState pState, boolean pIsMoving)
    {
        return null;
    }

    public void setBlockEntity(BlockEntity p_156358_)
    {
    }

    public void addEntity(Entity pEntity)
    {
    }

    public void setStatus(ChunkStatus pStatus)
    {
    }

    public LevelChunkSection[] getSections()
    {
        return this.wrapped.getSections();
    }

    public void m_6511_(Heightmap.Types p_62706_, long[] p_62707_)
    {
    }

    private Heightmap.Types fixType(Heightmap.Types p_62742_)
    {
        if (p_62742_ == Heightmap.Types.WORLD_SURFACE_WG)
        {
            return Heightmap.Types.WORLD_SURFACE;
        }
        else
        {
            return p_62742_ == Heightmap.Types.OCEAN_FLOOR_WG ? Heightmap.Types.OCEAN_FLOOR : p_62742_;
        }
    }

    public int getHeight(Heightmap.Types pHeightmapType, int pX, int pZ)
    {
        return this.wrapped.getHeight(this.fixType(pHeightmapType), pX, pZ);
    }

    public BlockPos getHeighestPosition(Heightmap.Types p_156360_)
    {
        return this.wrapped.getHeighestPosition(this.fixType(p_156360_));
    }

    public ChunkPos getPos()
    {
        return this.wrapped.getPos();
    }

    @Nullable
    public StructureStart<?> getStartForFeature(StructureFeature<?> p_62709_)
    {
        return this.wrapped.getStartForFeature(p_62709_);
    }

    public void setStartForFeature(StructureFeature<?> p_62714_, StructureStart<?> p_62715_)
    {
    }

    public Map < StructureFeature<?>, StructureStart<? >> getAllStarts()
    {
        return this.wrapped.getAllStarts();
    }

    public void setAllStarts(Map < StructureFeature<?>, StructureStart<? >> pStructureStarts)
    {
    }

    public LongSet getReferencesForFeature(StructureFeature<?> p_62734_)
    {
        return this.wrapped.getReferencesForFeature(p_62734_);
    }

    public void addReferenceForFeature(StructureFeature<?> p_62711_, long p_62712_)
    {
    }

    public Map < StructureFeature<?>, LongSet > getAllReferences()
    {
        return this.wrapped.getAllReferences();
    }

    public void setAllReferences(Map < StructureFeature<?>, LongSet > pStructureReferences)
    {
    }

    public ChunkBiomeContainer getBiomes()
    {
        return this.wrapped.getBiomes();
    }

    public void setUnsaved(boolean pModified)
    {
    }

    public boolean isUnsaved()
    {
        return false;
    }

    public ChunkStatus getStatus()
    {
        return this.wrapped.getStatus();
    }

    public void removeBlockEntity(BlockPos pPos)
    {
    }

    public void markPosForPostprocessing(BlockPos pPos)
    {
    }

    public void setBlockEntityNbt(CompoundTag pNbt)
    {
    }

    @Nullable
    public CompoundTag getBlockEntityNbt(BlockPos pPos)
    {
        return this.wrapped.getBlockEntityNbt(pPos);
    }

    @Nullable
    public CompoundTag getBlockEntityNbtForSaving(BlockPos pPos)
    {
        return this.wrapped.getBlockEntityNbtForSaving(pPos);
    }

    public void setBiomes(ChunkBiomeContainer pBiomes)
    {
    }

    public Stream<BlockPos> getLights()
    {
        return this.wrapped.getLights();
    }

    public ProtoTickList<Block> getBlockTicks()
    {
        return new ProtoTickList<>((p_62694_) ->
        {
            return p_62694_.defaultBlockState().isAir();
        }, this.getPos(), this);
    }

    public ProtoTickList<Fluid> getLiquidTicks()
    {
        return new ProtoTickList<>((p_62717_) ->
        {
            return p_62717_ == Fluids.EMPTY;
        }, this.getPos(), this);
    }

    public BitSet getCarvingMask(GenerationStep.Carving pType)
    {
        throw(UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Meaningless in this context"));
    }

    public BitSet getOrCreateCarvingMask(GenerationStep.Carving pType)
    {
        throw(UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Meaningless in this context"));
    }

    public LevelChunk getWrapped()
    {
        return this.wrapped;
    }

    public boolean isLightCorrect()
    {
        return this.wrapped.isLightCorrect();
    }

    public void setLightCorrect(boolean pLightCorrect)
    {
        this.wrapped.setLightCorrect(pLightCorrect);
    }
}
