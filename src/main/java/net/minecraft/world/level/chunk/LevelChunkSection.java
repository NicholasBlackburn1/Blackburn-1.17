package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class LevelChunkSection
{
    public static final int SECTION_WIDTH = 16;
    public static final int SECTION_HEIGHT = 16;
    public static final int SECTION_SIZE = 4096;
    private static final Palette<BlockState> GLOBAL_BLOCKSTATE_PALETTE = new GlobalPalette<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState());
    private final int bottomBlockY;
    private short nonEmptyBlockCount;
    private short tickingBlockCount;
    private short tickingFluidCount;
    private final PalettedContainer<BlockState> states;

    public LevelChunkSection(int p_62975_)
    {
        this(p_62975_, (short)0, (short)0, (short)0);
    }

    public LevelChunkSection(int p_62977_, short p_62978_, short p_62979_, short p_62980_)
    {
        this.bottomBlockY = getBottomBlockY(p_62977_);
        this.nonEmptyBlockCount = p_62978_;
        this.tickingBlockCount = p_62979_;
        this.tickingFluidCount = p_62980_;
        this.states = new PalettedContainer<>(GLOBAL_BLOCKSTATE_PALETTE, Block.BLOCK_STATE_REGISTRY, NbtUtils::readBlockState, NbtUtils::writeBlockState, Blocks.AIR.defaultBlockState());
    }

    public static int getBottomBlockY(int p_156459_)
    {
        return p_156459_ << 4;
    }

    public BlockState getBlockState(int pX, int pY, int pZ)
    {
        return this.states.get(pX, pY, pZ);
    }

    public FluidState getFluidState(int pX, int pY, int pZ)
    {
        return this.states.get(pX, pY, pZ).getFluidState();
    }

    public void acquire()
    {
        this.states.acquire();
    }

    public void release()
    {
        this.states.release();
    }

    public BlockState setBlockState(int pX, int pY, int pZ, BlockState pBlockState)
    {
        return this.setBlockState(pX, pY, pZ, pBlockState, true);
    }

    public BlockState setBlockState(int pX, int pY, int pZ, BlockState pBlockState, boolean p_62996_)
    {
        BlockState blockstate;

        if (p_62996_)
        {
            blockstate = this.states.getAndSet(pX, pY, pZ, pBlockState);
        }
        else
        {
            blockstate = this.states.getAndSetUnchecked(pX, pY, pZ, pBlockState);
        }

        FluidState fluidstate = blockstate.getFluidState();
        FluidState fluidstate1 = pBlockState.getFluidState();

        if (!blockstate.isAir())
        {
            --this.nonEmptyBlockCount;

            if (blockstate.isRandomlyTicking())
            {
                --this.tickingBlockCount;
            }
        }

        if (!fluidstate.isEmpty())
        {
            --this.tickingFluidCount;
        }

        if (!pBlockState.isAir())
        {
            ++this.nonEmptyBlockCount;

            if (pBlockState.isRandomlyTicking())
            {
                ++this.tickingBlockCount;
            }
        }

        if (!fluidstate1.isEmpty())
        {
            ++this.tickingFluidCount;
        }

        return blockstate;
    }

    public boolean isEmpty()
    {
        return this.nonEmptyBlockCount == 0;
    }

    public static boolean isEmpty(@Nullable LevelChunkSection p_63001_)
    {
        return p_63001_ == LevelChunk.EMPTY_SECTION || p_63001_.isEmpty();
    }

    public boolean isRandomlyTicking()
    {
        return this.isRandomlyTickingBlocks() || this.isRandomlyTickingFluids();
    }

    public boolean isRandomlyTickingBlocks()
    {
        return this.tickingBlockCount > 0;
    }

    public boolean isRandomlyTickingFluids()
    {
        return this.tickingFluidCount > 0;
    }

    public int bottomBlockY()
    {
        return this.bottomBlockY;
    }

    public void recalcBlockCounts()
    {
        this.nonEmptyBlockCount = 0;
        this.tickingBlockCount = 0;
        this.tickingFluidCount = 0;
        this.states.count((p_62997_1_, p_62997_2_) ->
        {
            FluidState fluidstate = p_62997_1_.getFluidState();

            if (!p_62997_1_.isAir())
            {
                this.nonEmptyBlockCount = (short)(this.nonEmptyBlockCount + p_62997_2_);

                if (p_62997_1_.isRandomlyTicking())
                {
                    this.tickingBlockCount = (short)(this.tickingBlockCount + p_62997_2_);
                }
            }

            if (!fluidstate.isEmpty())
            {
                this.nonEmptyBlockCount = (short)(this.nonEmptyBlockCount + p_62997_2_);

                if (fluidstate.isRandomlyTicking())
                {
                    this.tickingFluidCount = (short)(this.tickingFluidCount + p_62997_2_);
                }
            }
        });
    }

    public PalettedContainer<BlockState> getStates()
    {
        return this.states;
    }

    public void read(FriendlyByteBuf pPacketBuffer)
    {
        this.nonEmptyBlockCount = pPacketBuffer.readShort();
        this.states.read(pPacketBuffer);
    }

    public void write(FriendlyByteBuf pPacketBuffer)
    {
        pPacketBuffer.writeShort(this.nonEmptyBlockCount);
        this.states.write(pPacketBuffer);
    }

    public int getSerializedSize()
    {
        return 2 + this.states.getSerializedSize();
    }

    public boolean maybeHas(Predicate<BlockState> pPredicate)
    {
        return this.states.maybeHas(pPredicate);
    }

    public short getBlockRefCount()
    {
        return this.nonEmptyBlockCount;
    }
}
