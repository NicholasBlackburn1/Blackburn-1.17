package net.minecraft.world.level;

import com.mojang.datafixers.DataFixUtils;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.FeatureAccess;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class StructureFeatureManager
{
    private final LevelAccessor level;
    private final WorldGenSettings worldGenSettings;

    public StructureFeatureManager(LevelAccessor p_47269_, WorldGenSettings p_47270_)
    {
        this.level = p_47269_;
        this.worldGenSettings = p_47270_;
    }

    public StructureFeatureManager forWorldGenRegion(WorldGenRegion pRegion)
    {
        if (pRegion.getLevel() != this.level)
        {
            throw new IllegalStateException("Using invalid feature manager (source level: " + pRegion.getLevel() + ", region: " + pRegion);
        }
        else
        {
            return new StructureFeatureManager(pRegion, this.worldGenSettings);
        }
    }

    public Stream <? extends StructureStart<? >> startsForFeature(SectionPos p_47290_, StructureFeature<?> p_47291_)
    {
        return this.level.getChunk(p_47290_.x(), p_47290_.z(), ChunkStatus.STRUCTURE_REFERENCES).getReferencesForFeature(p_47291_).stream().map((p_47307_) ->
        {
            return SectionPos.of(new ChunkPos(p_47307_), this.level.getMinSection());
        }).map((p_47276_) ->
        {
            return this.getStartForFeature(p_47276_, p_47291_, this.level.getChunk(p_47276_.x(), p_47276_.z(), ChunkStatus.STRUCTURE_STARTS));
        }).filter((p_47278_) ->
        {
            return p_47278_ != null && p_47278_.isValid();
        });
    }

    @Nullable
    public StructureStart<?> getStartForFeature(SectionPos pSectionPos, StructureFeature<?> pStructure, FeatureAccess pReader)
    {
        return pReader.getStartForFeature(pStructure);
    }

    public void setStartForFeature(SectionPos pSectionPos, StructureFeature<?> pStructure, StructureStart<?> pStart, FeatureAccess pReader)
    {
        pReader.setStartForFeature(pStructure, pStart);
    }

    public void addReferenceForFeature(SectionPos pSectionPos, StructureFeature<?> pStructure, long p_47295_, FeatureAccess p_47296_)
    {
        p_47296_.addReferenceForFeature(pStructure, p_47295_);
    }

    public boolean shouldGenerateFeatures()
    {
        return this.worldGenSettings.generateFeatures();
    }

    public StructureStart<?> getStructureAt(BlockPos pPos, boolean p_47287_, StructureFeature<?> pStructure)
    {
        return DataFixUtils.orElse(this.startsForFeature(SectionPos.of(pPos), pStructure).filter((p_151637_) ->
        {
            return p_47287_ ? p_151637_.getPieces().stream().anyMatch((p_151633_) -> {
                return p_151633_.getBoundingBox().isInside(pPos);
            }) : p_151637_.getBoundingBox().isInside(pPos);
        }).findFirst(), StructureStart.INVALID_START);
    }
}
