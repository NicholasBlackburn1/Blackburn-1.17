package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class StructureStart<C extends FeatureConfiguration> implements StructurePieceAccessor {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final String INVALID_START_ID = "INVALID";
   public static final StructureStart<?> INVALID_START = new StructureStart<MineshaftConfiguration>((StructureFeature)null, new ChunkPos(0, 0), 0, 0L) {
      public void generatePieces(RegistryAccess p_163644_, ChunkGenerator p_163645_, StructureManager p_163646_, ChunkPos p_163647_, Biome p_163648_, MineshaftConfiguration p_163649_, LevelHeightAccessor p_163650_) {
      }

      public boolean isValid() {
         return false;
      }
   };
   private final StructureFeature<C> feature;
   protected final List<StructurePiece> pieces = Lists.newArrayList();
   private final ChunkPos chunkPos;
   private int references;
   protected final WorldgenRandom random;
   @Nullable
   private BoundingBox cachedBoundingBox;

   public StructureStart(StructureFeature<C> p_163595_, ChunkPos p_163596_, int p_163597_, long p_163598_) {
      this.feature = p_163595_;
      this.chunkPos = p_163596_;
      this.references = p_163597_;
      this.random = new WorldgenRandom();
      this.random.setLargeFeatureSeed(p_163598_, p_163596_.x, p_163596_.z);
   }

   public abstract void generatePieces(RegistryAccess p_163615_, ChunkGenerator p_163616_, StructureManager p_163617_, ChunkPos p_163618_, Biome p_163619_, C p_163620_, LevelHeightAccessor p_163621_);

   public final BoundingBox getBoundingBox() {
      if (this.cachedBoundingBox == null) {
         this.cachedBoundingBox = this.createBoundingBox();
      }

      return this.cachedBoundingBox;
   }

   protected BoundingBox createBoundingBox() {
      synchronized(this.pieces) {
         return BoundingBox.encapsulatingBoxes(this.pieces.stream().map(StructurePiece::getBoundingBox)::iterator).orElseThrow(() -> {
            return new IllegalStateException("Unable to calculate boundingbox without pieces");
         });
      }
   }

   public List<StructurePiece> getPieces() {
      return this.pieces;
   }

   public void placeInChunk(WorldGenLevel p_73584_, StructureFeatureManager p_73585_, ChunkGenerator p_73586_, Random p_73587_, BoundingBox p_73588_, ChunkPos p_73589_) {
      synchronized(this.pieces) {
         if (!this.pieces.isEmpty()) {
            BoundingBox boundingbox = (this.pieces.get(0)).boundingBox;
            BlockPos blockpos = boundingbox.getCenter();
            BlockPos blockpos1 = new BlockPos(blockpos.getX(), boundingbox.minY(), blockpos.getZ());
            Iterator<StructurePiece> iterator = this.pieces.iterator();

            while(iterator.hasNext()) {
               StructurePiece structurepiece = iterator.next();
               if (structurepiece.getBoundingBox().intersects(p_73588_) && !structurepiece.postProcess(p_73584_, p_73585_, p_73586_, p_73587_, p_73588_, p_73589_, blockpos1)) {
                  iterator.remove();
               }
            }

         }
      }
   }

   public CompoundTag createTag(ServerLevel p_163607_, ChunkPos p_163608_) {
      CompoundTag compoundtag = new CompoundTag();
      if (this.isValid()) {
         compoundtag.putString("id", Registry.STRUCTURE_FEATURE.getKey(this.getFeature()).toString());
         compoundtag.putInt("ChunkX", p_163608_.x);
         compoundtag.putInt("ChunkZ", p_163608_.z);
         compoundtag.putInt("references", this.references);
         ListTag listtag = new ListTag();
         synchronized(this.pieces) {
            for(StructurePiece structurepiece : this.pieces) {
               listtag.add(structurepiece.createTag(p_163607_));
            }
         }

         compoundtag.put("Children", listtag);
         return compoundtag;
      } else {
         compoundtag.putString("id", "INVALID");
         return compoundtag;
      }
   }

   protected void moveBelowSeaLevel(int p_163602_, int p_163603_, Random p_163604_, int p_163605_) {
      int i = p_163602_ - p_163605_;
      BoundingBox boundingbox = this.getBoundingBox();
      int j = boundingbox.getYSpan() + p_163603_ + 1;
      if (j < i) {
         j += p_163604_.nextInt(i - j);
      }

      int k = j - boundingbox.maxY();
      this.offsetPiecesVertically(k);
   }

   protected void moveInsideHeights(Random p_73598_, int p_73599_, int p_73600_) {
      BoundingBox boundingbox = this.getBoundingBox();
      int i = p_73600_ - p_73599_ + 1 - boundingbox.getYSpan();
      int j;
      if (i > 1) {
         j = p_73599_ + p_73598_.nextInt(i);
      } else {
         j = p_73599_;
      }

      int k = j - boundingbox.minY();
      this.offsetPiecesVertically(k);
   }

   protected void offsetPiecesVertically(int p_163600_) {
      for(StructurePiece structurepiece : this.pieces) {
         structurepiece.move(0, p_163600_, 0);
      }

      this.invalidateCache();
   }

   private void invalidateCache() {
      this.cachedBoundingBox = null;
   }

   public boolean isValid() {
      return !this.pieces.isEmpty();
   }

   public ChunkPos getChunkPos() {
      return this.chunkPos;
   }

   public BlockPos getLocatePos() {
      return new BlockPos(this.chunkPos.getMinBlockX(), 0, this.chunkPos.getMinBlockZ());
   }

   public boolean canBeReferenced() {
      return this.references < this.getMaxReferences();
   }

   public void addReference() {
      ++this.references;
   }

   public int getReferences() {
      return this.references;
   }

   protected int getMaxReferences() {
      return 1;
   }

   public StructureFeature<?> getFeature() {
      return this.feature;
   }

   public void addPiece(StructurePiece p_163612_) {
      this.pieces.add(p_163612_);
      this.invalidateCache();
   }

   @Nullable
   public StructurePiece findCollisionPiece(BoundingBox p_163610_) {
      return findCollisionPiece(this.pieces, p_163610_);
   }

   public void clearPieces() {
      this.pieces.clear();
      this.invalidateCache();
   }

   public boolean hasNoPieces() {
      return this.pieces.isEmpty();
   }

   @Nullable
   public static StructurePiece findCollisionPiece(List<StructurePiece> p_163623_, BoundingBox p_163624_) {
      for(StructurePiece structurepiece : p_163623_) {
         if (structurepiece.getBoundingBox().intersects(p_163624_)) {
            return structurepiece;
         }
      }

      return null;
   }

   protected boolean isInsidePiece(BlockPos p_163614_) {
      for(StructurePiece structurepiece : this.pieces) {
         if (structurepiece.getBoundingBox().isInside(p_163614_)) {
            return true;
         }
      }

      return false;
   }
}