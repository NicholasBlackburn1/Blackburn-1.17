package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.BitSet;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;

public class ClientboundLevelChunkPacket implements Packet<ClientGamePacketListener>
{
    public static final int TWO_MEGABYTES = 2097152;
    private final int x;
    private final int z;
    private final BitSet availableSections;
    private final CompoundTag heightmaps;
    private final int[] biomes;
    private final byte[] buffer;
    private final List<CompoundTag> blockEntitiesTags;

    public ClientboundLevelChunkPacket(LevelChunk p_178898_)
    {
        ChunkPos chunkpos = p_178898_.getPos();
        this.x = chunkpos.x;
        this.z = chunkpos.z;
        this.heightmaps = new CompoundTag();

        for (Entry<Heightmap.Types, Heightmap> entry : p_178898_.getHeightmaps())
        {
            if (entry.getKey().sendToClient())
            {
                this.heightmaps.put(entry.getKey().getSerializationKey(), new LongArrayTag(entry.getValue().getRawData()));
            }
        }

        this.biomes = p_178898_.getBiomes().writeBiomes();
        this.buffer = new byte[this.calculateChunkSize(p_178898_)];
        this.availableSections = this.extractChunkData(new FriendlyByteBuf(this.getWriteBuffer()), p_178898_);
        this.blockEntitiesTags = Lists.newArrayList();

        for (Entry<BlockPos, BlockEntity> entry1 : p_178898_.getBlockEntities().entrySet())
        {
            BlockEntity blockentity = entry1.getValue();
            CompoundTag compoundtag = blockentity.getUpdateTag();
            this.blockEntitiesTags.add(compoundtag);
        }
    }

    public ClientboundLevelChunkPacket(FriendlyByteBuf p_178900_)
    {
        this.x = p_178900_.readInt();
        this.z = p_178900_.readInt();
        this.availableSections = p_178900_.readBitSet();
        this.heightmaps = p_178900_.readNbt();

        if (this.heightmaps == null)
        {
            throw new RuntimeException("Can't read heightmap in packet for [" + this.x + ", " + this.z + "]");
        }
        else
        {
            this.biomes = p_178900_.readVarIntArray(ChunkBiomeContainer.MAX_SIZE);
            int i = p_178900_.readVarInt();

            if (i > 2097152)
            {
                throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
            }
            else
            {
                this.buffer = new byte[i];
                p_178900_.readBytes(this.buffer);
                this.blockEntitiesTags = p_178900_.readList(FriendlyByteBuf::readNbt);
            }
        }
    }

    public void write(FriendlyByteBuf pBuf)
    {
        pBuf.writeInt(this.x);
        pBuf.writeInt(this.z);
        pBuf.writeBitSet(this.availableSections);
        pBuf.writeNbt(this.heightmaps);
        pBuf.m_130089_(this.biomes);
        pBuf.writeVarInt(this.buffer.length);
        pBuf.writeBytes(this.buffer);
        pBuf.writeCollection(this.blockEntitiesTags, FriendlyByteBuf::writeNbt);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleLevelChunk(this);
    }

    public FriendlyByteBuf getReadBuffer()
    {
        return new FriendlyByteBuf(Unpooled.wrappedBuffer(this.buffer));
    }

    private ByteBuf getWriteBuffer()
    {
        ByteBuf bytebuf = Unpooled.wrappedBuffer(this.buffer);
        bytebuf.writerIndex(0);
        return bytebuf;
    }

    public BitSet extractChunkData(FriendlyByteBuf p_178904_, LevelChunk p_178905_)
    {
        BitSet bitset = new BitSet();
        LevelChunkSection[] alevelchunksection = p_178905_.getSections();
        int i = 0;

        for (int j = alevelchunksection.length; i < j; ++i)
        {
            LevelChunkSection levelchunksection = alevelchunksection[i];

            if (levelchunksection != LevelChunk.EMPTY_SECTION && !levelchunksection.isEmpty())
            {
                bitset.set(i);
                levelchunksection.write(p_178904_);
            }
        }

        return bitset;
    }

    protected int calculateChunkSize(LevelChunk p_178902_)
    {
        int i = 0;
        LevelChunkSection[] alevelchunksection = p_178902_.getSections();
        int j = 0;

        for (int k = alevelchunksection.length; j < k; ++j)
        {
            LevelChunkSection levelchunksection = alevelchunksection[j];

            if (levelchunksection != LevelChunk.EMPTY_SECTION && !levelchunksection.isEmpty())
            {
                i += levelchunksection.getSerializedSize();
            }
        }

        return i;
    }

    public int getX()
    {
        return this.x;
    }

    public int getZ()
    {
        return this.z;
    }

    public BitSet getAvailableSections()
    {
        return this.availableSections;
    }

    public CompoundTag getHeightmaps()
    {
        return this.heightmaps;
    }

    public List<CompoundTag> getBlockEntitiesTags()
    {
        return this.blockEntitiesTags;
    }

    public int[] getBiomes()
    {
        return this.biomes;
    }
}
