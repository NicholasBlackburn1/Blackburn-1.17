package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class LevelChunkSection {
   public static final int SECTION_WIDTH = 16;
   public static final int SECTION_HEIGHT = 16;
   public static final int SECTION_SIZE = 4096;
   private static final Palette<BlockState> GLOBAL_BLOCKSTATE_PALETTE = new GlobalPalette<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState());
   private final int bottomBlockY;
   private short nonEmptyBlockCount;
   private short tickingBlockCount;
   private short tickingFluidCount;
   private final PalettedContainer<BlockState> states;

   public LevelChunkSection(int p_62975_) {
      this(p_62975_, (short)0, (short)0, (short)0);
   }

   public LevelChunkSection(int p_62977_, short p_62978_, short p_62979_, short p_62980_) {
      this.bottomBlockY = getBottomBlockY(p_62977_);
      this.nonEmptyBlockCount = p_62978_;
      this.tickingBlockCount = p_62979_;
      this.tickingFluidCount = p_62980_;
      this.states = new PalettedContainer<>(GLOBAL_BLOCKSTATE_PALETTE, Block.BLOCK_STATE_REGISTRY, NbtUtils::readBlockState, NbtUtils::writeBlockState, Blocks.AIR.defaultBlockState());
   }

   public static int getBottomBlockY(int p_156459_) {
      return p_156459_ << 4;
   }

   public BlockState getBlockState(int p_62983_, int p_62984_, int p_62985_) {
      return this.states.get(p_62983_, p_62984_, p_62985_);
   }

   public FluidState getFluidState(int p_63008_, int p_63009_, int p_63010_) {
      return this.states.get(p_63008_, p_63009_, p_63010_).getFluidState();
   }

   public void acquire() {
      this.states.acquire();
   }

   public void release() {
      this.states.release();
   }

   public BlockState setBlockState(int p_62987_, int p_62988_, int p_62989_, BlockState p_62990_) {
      return this.setBlockState(p_62987_, p_62988_, p_62989_, p_62990_, true);
   }

   public BlockState setBlockState(int p_62992_, int p_62993_, int p_62994_, BlockState p_62995_, boolean p_62996_) {
      BlockState blockstate;
      if (p_62996_) {
         blockstate = this.states.getAndSet(p_62992_, p_62993_, p_62994_, p_62995_);
      } else {
         blockstate = this.states.getAndSetUnchecked(p_62992_, p_62993_, p_62994_, p_62995_);
      }

      FluidState fluidstate = blockstate.getFluidState();
      FluidState fluidstate1 = p_62995_.getFluidState();
      if (!blockstate.isAir()) {
         --this.nonEmptyBlockCount;
         if (blockstate.isRandomlyTicking()) {
            --this.tickingBlockCount;
         }
      }

      if (!fluidstate.isEmpty()) {
         --this.tickingFluidCount;
      }

      if (!p_62995_.isAir()) {
         ++this.nonEmptyBlockCount;
         if (p_62995_.isRandomlyTicking()) {
            ++this.tickingBlockCount;
         }
      }

      if (!fluidstate1.isEmpty()) {
         ++this.tickingFluidCount;
      }

      return blockstate;
   }

   public boolean isEmpty() {
      return this.nonEmptyBlockCount == 0;
   }

   public static boolean isEmpty(@Nullable LevelChunkSection p_63001_) {
      return p_63001_ == LevelChunk.EMPTY_SECTION || p_63001_.isEmpty();
   }

   public boolean isRandomlyTicking() {
      return this.isRandomlyTickingBlocks() || this.isRandomlyTickingFluids();
   }

   public boolean isRandomlyTickingBlocks() {
      return this.tickingBlockCount > 0;
   }

   public boolean isRandomlyTickingFluids() {
      return this.tickingFluidCount > 0;
   }

   public int bottomBlockY() {
      return this.bottomBlockY;
   }

   public void recalcBlockCounts() {
      this.nonEmptyBlockCount = 0;
      this.tickingBlockCount = 0;
      this.tickingFluidCount = 0;
      this.states.count((p_62998_, p_62999_) -> {
         FluidState fluidstate = p_62998_.getFluidState();
         if (!p_62998_.isAir()) {
            this.nonEmptyBlockCount = (short)(this.nonEmptyBlockCount + p_62999_);
            if (p_62998_.isRandomlyTicking()) {
               this.tickingBlockCount = (short)(this.tickingBlockCount + p_62999_);
            }
         }

         if (!fluidstate.isEmpty()) {
            this.nonEmptyBlockCount = (short)(this.nonEmptyBlockCount + p_62999_);
            if (fluidstate.isRandomlyTicking()) {
               this.tickingFluidCount = (short)(this.tickingFluidCount + p_62999_);
            }
         }

      });
   }

   public PalettedContainer<BlockState> getStates() {
      return this.states;
   }

   public void read(FriendlyByteBuf p_63005_) {
      this.nonEmptyBlockCount = p_63005_.readShort();
      this.states.read(p_63005_);
   }

   public void write(FriendlyByteBuf p_63012_) {
      p_63012_.writeShort(this.nonEmptyBlockCount);
      this.states.write(p_63012_);
   }

   public int getSerializedSize() {
      return 2 + this.states.getSerializedSize();
   }

   public boolean maybeHas(Predicate<BlockState> p_63003_) {
      return this.states.maybeHas(p_63003_);
   }
}