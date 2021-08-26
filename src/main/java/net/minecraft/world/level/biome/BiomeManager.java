package net.minecraft.world.level.biome;

import com.google.common.hash.Hashing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

public class BiomeManager
{
    static final int CHUNK_CENTER_QUART = QuartPos.fromBlock(8);
    private final BiomeManager.NoiseBiomeSource noiseBiomeSource;
    private final long biomeZoomSeed;
    private final BiomeZoomer zoomer;

    public BiomeManager(BiomeManager.NoiseBiomeSource p_47866_, long p_47867_, BiomeZoomer p_47868_)
    {
        this.noiseBiomeSource = p_47866_;
        this.biomeZoomSeed = p_47867_;
        this.zoomer = p_47868_;
    }

    public static long obfuscateSeed(long pSeed)
    {
        return Hashing.sha256().hashLong(pSeed).asLong();
    }

    public BiomeManager withDifferentSource(BiomeSource pNewProvider)
    {
        return new BiomeManager(pNewProvider, this.biomeZoomSeed, this.zoomer);
    }

    public Biome getBiome(BlockPos pPos)
    {
        return this.zoomer.getBiome(this.biomeZoomSeed, pPos.getX(), pPos.getY(), pPos.getZ(), this.noiseBiomeSource);
    }

    public Biome getNoiseBiomeAtPosition(double pX, double p_47871_, double pY)
    {
        int i = QuartPos.fromBlock(Mth.floor(pX));
        int j = QuartPos.fromBlock(Mth.floor(p_47871_));
        int k = QuartPos.fromBlock(Mth.floor(pY));
        return this.getNoiseBiomeAtQuart(i, j, k);
    }

    public Biome getNoiseBiomeAtPosition(BlockPos pX)
    {
        int i = QuartPos.fromBlock(pX.getX());
        int j = QuartPos.fromBlock(pX.getY());
        int k = QuartPos.fromBlock(pX.getZ());
        return this.getNoiseBiomeAtQuart(i, j, k);
    }

    public Biome getNoiseBiomeAtQuart(int pX, int pY, int pZ)
    {
        return this.noiseBiomeSource.getNoiseBiome(pX, pY, pZ);
    }

    public Biome getPrimaryBiomeAtChunk(ChunkPos p_151753_)
    {
        return this.noiseBiomeSource.getPrimaryBiome(p_151753_);
    }

    public interface NoiseBiomeSource
    {
        Biome getNoiseBiome(int pX, int pY, int pZ);

    default Biome getPrimaryBiome(ChunkPos p_151755_)
        {
            return this.getNoiseBiome(QuartPos.fromSection(p_151755_.x) + BiomeManager.CHUNK_CENTER_QUART, 0, QuartPos.fromSection(p_151755_.z) + BiomeManager.CHUNK_CENTER_QUART);
        }
    }
}
