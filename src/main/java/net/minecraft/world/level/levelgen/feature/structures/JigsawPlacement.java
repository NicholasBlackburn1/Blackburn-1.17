package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JigsawPlacement {
   static final Logger LOGGER = LogManager.getLogger();

   public static void addPieces(RegistryAccess p_161613_, JigsawConfiguration p_161614_, JigsawPlacement.PieceFactory p_161615_, ChunkGenerator p_161616_, StructureManager p_161617_, BlockPos p_161618_, StructurePieceAccessor p_161619_, Random p_161620_, boolean p_161621_, boolean p_161622_, LevelHeightAccessor p_161623_) {
      StructureFeature.bootstrap();
      List<PoolElementStructurePiece> list = Lists.newArrayList();
      Registry<StructureTemplatePool> registry = p_161613_.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
      Rotation rotation = Rotation.getRandom(p_161620_);
      StructureTemplatePool structuretemplatepool = p_161614_.startPool().get();
      StructurePoolElement structurepoolelement = structuretemplatepool.getRandomTemplate(p_161620_);
      if (structurepoolelement != EmptyPoolElement.INSTANCE) {
         PoolElementStructurePiece poolelementstructurepiece = p_161615_.create(p_161617_, structurepoolelement, p_161618_, structurepoolelement.getGroundLevelDelta(), rotation, structurepoolelement.getBoundingBox(p_161617_, p_161618_, rotation));
         BoundingBox boundingbox = poolelementstructurepiece.getBoundingBox();
         int i = (boundingbox.maxX() + boundingbox.minX()) / 2;
         int j = (boundingbox.maxZ() + boundingbox.minZ()) / 2;
         int k;
         if (p_161622_) {
            k = p_161618_.getY() + p_161616_.getFirstFreeHeight(i, j, Heightmap.Types.WORLD_SURFACE_WG, p_161623_);
         } else {
            k = p_161618_.getY();
         }

         int l = boundingbox.minY() + poolelementstructurepiece.getGroundLevelDelta();
         poolelementstructurepiece.move(0, k - l, 0);
         list.add(poolelementstructurepiece);
         if (p_161614_.maxDepth() > 0) {
            int i1 = 80;
            AABB aabb = new AABB((double)(i - 80), (double)(k - 80), (double)(j - 80), (double)(i + 80 + 1), (double)(k + 80 + 1), (double)(j + 80 + 1));
            JigsawPlacement.Placer jigsawplacement$placer = new JigsawPlacement.Placer(registry, p_161614_.maxDepth(), p_161615_, p_161616_, p_161617_, list, p_161620_);
            jigsawplacement$placer.placing.addLast(new JigsawPlacement.PieceState(poolelementstructurepiece, new MutableObject<>(Shapes.join(Shapes.create(aabb), Shapes.create(AABB.of(boundingbox)), BooleanOp.ONLY_FIRST)), k + 80, 0));

            while(!jigsawplacement$placer.placing.isEmpty()) {
               JigsawPlacement.PieceState jigsawplacement$piecestate = jigsawplacement$placer.placing.removeFirst();
               jigsawplacement$placer.tryPlacingChildren(jigsawplacement$piecestate.piece, jigsawplacement$piecestate.free, jigsawplacement$piecestate.boundsTop, jigsawplacement$piecestate.depth, p_161621_, p_161623_);
            }

            list.forEach(p_161619_::addPiece);
         }
      }
   }

   public static void addPieces(RegistryAccess p_161625_, PoolElementStructurePiece p_161626_, int p_161627_, JigsawPlacement.PieceFactory p_161628_, ChunkGenerator p_161629_, StructureManager p_161630_, List<? super PoolElementStructurePiece> p_161631_, Random p_161632_, LevelHeightAccessor p_161633_) {
      Registry<StructureTemplatePool> registry = p_161625_.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
      JigsawPlacement.Placer jigsawplacement$placer = new JigsawPlacement.Placer(registry, p_161627_, p_161628_, p_161629_, p_161630_, p_161631_, p_161632_);
      jigsawplacement$placer.placing.addLast(new JigsawPlacement.PieceState(p_161626_, new MutableObject<>(Shapes.INFINITY), 0, 0));

      while(!jigsawplacement$placer.placing.isEmpty()) {
         JigsawPlacement.PieceState jigsawplacement$piecestate = jigsawplacement$placer.placing.removeFirst();
         jigsawplacement$placer.tryPlacingChildren(jigsawplacement$piecestate.piece, jigsawplacement$piecestate.free, jigsawplacement$piecestate.boundsTop, jigsawplacement$piecestate.depth, false, p_161633_);
      }

   }

   public interface PieceFactory {
      PoolElementStructurePiece create(StructureManager p_68965_, StructurePoolElement p_68966_, BlockPos p_68967_, int p_68968_, Rotation p_68969_, BoundingBox p_68970_);
   }

   static final class PieceState {
      final PoolElementStructurePiece piece;
      final MutableObject<VoxelShape> free;
      final int boundsTop;
      final int depth;

      PieceState(PoolElementStructurePiece p_68976_, MutableObject<VoxelShape> p_68977_, int p_68978_, int p_68979_) {
         this.piece = p_68976_;
         this.free = p_68977_;
         this.boundsTop = p_68978_;
         this.depth = p_68979_;
      }
   }

   static final class Placer {
      private final Registry<StructureTemplatePool> pools;
      private final int maxDepth;
      private final JigsawPlacement.PieceFactory factory;
      private final ChunkGenerator chunkGenerator;
      private final StructureManager structureManager;
      private final List<? super PoolElementStructurePiece> pieces;
      private final Random random;
      final Deque<JigsawPlacement.PieceState> placing = Queues.newArrayDeque();

      Placer(Registry<StructureTemplatePool> p_69003_, int p_69004_, JigsawPlacement.PieceFactory p_69005_, ChunkGenerator p_69006_, StructureManager p_69007_, List<? super PoolElementStructurePiece> p_69008_, Random p_69009_) {
         this.pools = p_69003_;
         this.maxDepth = p_69004_;
         this.factory = p_69005_;
         this.chunkGenerator = p_69006_;
         this.structureManager = p_69007_;
         this.pieces = p_69008_;
         this.random = p_69009_;
      }

      void tryPlacingChildren(PoolElementStructurePiece p_161637_, MutableObject<VoxelShape> p_161638_, int p_161639_, int p_161640_, boolean p_161641_, LevelHeightAccessor p_161642_) {
         StructurePoolElement structurepoolelement = p_161637_.getElement();
         BlockPos blockpos = p_161637_.getPosition();
         Rotation rotation = p_161637_.getRotation();
         StructureTemplatePool.Projection structuretemplatepool$projection = structurepoolelement.getProjection();
         boolean flag = structuretemplatepool$projection == StructureTemplatePool.Projection.RIGID;
         MutableObject<VoxelShape> mutableobject = new MutableObject<>();
         BoundingBox boundingbox = p_161637_.getBoundingBox();
         int i = boundingbox.minY();

         label139:
         for(StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo : structurepoolelement.getShuffledJigsawBlocks(this.structureManager, blockpos, rotation, this.random)) {
            Direction direction = JigsawBlock.getFrontFacing(structuretemplate$structureblockinfo.state);
            BlockPos blockpos1 = structuretemplate$structureblockinfo.pos;
            BlockPos blockpos2 = blockpos1.relative(direction);
            int j = blockpos1.getY() - i;
            int k = -1;
            ResourceLocation resourcelocation = new ResourceLocation(structuretemplate$structureblockinfo.nbt.getString("pool"));
            Optional<StructureTemplatePool> optional = this.pools.getOptional(resourcelocation);
            if (optional.isPresent() && (optional.get().size() != 0 || Objects.equals(resourcelocation, Pools.EMPTY.location()))) {
               ResourceLocation resourcelocation1 = optional.get().getFallback();
               Optional<StructureTemplatePool> optional1 = this.pools.getOptional(resourcelocation1);
               if (optional1.isPresent() && (optional1.get().size() != 0 || Objects.equals(resourcelocation1, Pools.EMPTY.location()))) {
                  boolean flag1 = boundingbox.isInside(blockpos2);
                  MutableObject<VoxelShape> mutableobject1;
                  int l;
                  if (flag1) {
                     mutableobject1 = mutableobject;
                     l = i;
                     if (mutableobject.getValue() == null) {
                        mutableobject.setValue(Shapes.create(AABB.of(boundingbox)));
                     }
                  } else {
                     mutableobject1 = p_161638_;
                     l = p_161639_;
                  }

                  List<StructurePoolElement> list = Lists.newArrayList();
                  if (p_161640_ != this.maxDepth) {
                     list.addAll(optional.get().getShuffledTemplates(this.random));
                  }

                  list.addAll(optional1.get().getShuffledTemplates(this.random));

                  for(StructurePoolElement structurepoolelement1 : list) {
                     if (structurepoolelement1 == EmptyPoolElement.INSTANCE) {
                        break;
                     }

                     for(Rotation rotation1 : Rotation.getShuffled(this.random)) {
                        List<StructureTemplate.StructureBlockInfo> list1 = structurepoolelement1.getShuffledJigsawBlocks(this.structureManager, BlockPos.ZERO, rotation1, this.random);
                        BoundingBox boundingbox1 = structurepoolelement1.getBoundingBox(this.structureManager, BlockPos.ZERO, rotation1);
                        int i1;
                        if (p_161641_ && boundingbox1.getYSpan() <= 16) {
                           i1 = list1.stream().mapToInt((p_69032_) -> {
                              if (!boundingbox1.isInside(p_69032_.pos.relative(JigsawBlock.getFrontFacing(p_69032_.state)))) {
                                 return 0;
                              } else {
                                 ResourceLocation resourcelocation2 = new ResourceLocation(p_69032_.nbt.getString("pool"));
                                 Optional<StructureTemplatePool> optional2 = this.pools.getOptional(resourcelocation2);
                                 Optional<StructureTemplatePool> optional3 = optional2.flatMap((p_161646_) -> {
                                    return this.pools.getOptional(p_161646_.getFallback());
                                 });
                                 int k3 = optional2.map((p_161644_) -> {
                                    return p_161644_.getMaxSize(this.structureManager);
                                 }).orElse(0);
                                 int l3 = optional3.map((p_161635_) -> {
                                    return p_161635_.getMaxSize(this.structureManager);
                                 }).orElse(0);
                                 return Math.max(k3, l3);
                              }
                           }).max().orElse(0);
                        } else {
                           i1 = 0;
                        }

                        for(StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo1 : list1) {
                           if (JigsawBlock.canAttach(structuretemplate$structureblockinfo, structuretemplate$structureblockinfo1)) {
                              BlockPos blockpos3 = structuretemplate$structureblockinfo1.pos;
                              BlockPos blockpos4 = blockpos2.subtract(blockpos3);
                              BoundingBox boundingbox2 = structurepoolelement1.getBoundingBox(this.structureManager, blockpos4, rotation1);
                              int j1 = boundingbox2.minY();
                              StructureTemplatePool.Projection structuretemplatepool$projection1 = structurepoolelement1.getProjection();
                              boolean flag2 = structuretemplatepool$projection1 == StructureTemplatePool.Projection.RIGID;
                              int k1 = blockpos3.getY();
                              int l1 = j - k1 + JigsawBlock.getFrontFacing(structuretemplate$structureblockinfo.state).getStepY();
                              int i2;
                              if (flag && flag2) {
                                 i2 = i + l1;
                              } else {
                                 if (k == -1) {
                                    k = this.chunkGenerator.getFirstFreeHeight(blockpos1.getX(), blockpos1.getZ(), Heightmap.Types.WORLD_SURFACE_WG, p_161642_);
                                 }

                                 i2 = k - k1;
                              }

                              int j2 = i2 - j1;
                              BoundingBox boundingbox3 = boundingbox2.moved(0, j2, 0);
                              BlockPos blockpos5 = blockpos4.offset(0, j2, 0);
                              if (i1 > 0) {
                                 int k2 = Math.max(i1 + 1, boundingbox3.maxY() - boundingbox3.minY());
                                 boundingbox3.encapsulate(new BlockPos(boundingbox3.minX(), boundingbox3.minY() + k2, boundingbox3.minZ()));
                              }

                              if (!Shapes.joinIsNotEmpty(mutableobject1.getValue(), Shapes.create(AABB.of(boundingbox3).deflate(0.25D)), BooleanOp.ONLY_SECOND)) {
                                 mutableobject1.setValue(Shapes.joinUnoptimized(mutableobject1.getValue(), Shapes.create(AABB.of(boundingbox3)), BooleanOp.ONLY_FIRST));
                                 int j3 = p_161637_.getGroundLevelDelta();
                                 int l2;
                                 if (flag2) {
                                    l2 = j3 - l1;
                                 } else {
                                    l2 = structurepoolelement1.getGroundLevelDelta();
                                 }

                                 PoolElementStructurePiece poolelementstructurepiece = this.factory.create(this.structureManager, structurepoolelement1, blockpos5, l2, rotation1, boundingbox3);
                                 int i3;
                                 if (flag) {
                                    i3 = i + j;
                                 } else if (flag2) {
                                    i3 = i2 + k1;
                                 } else {
                                    if (k == -1) {
                                       k = this.chunkGenerator.getFirstFreeHeight(blockpos1.getX(), blockpos1.getZ(), Heightmap.Types.WORLD_SURFACE_WG, p_161642_);
                                    }

                                    i3 = k + l1 / 2;
                                 }

                                 p_161637_.addJunction(new JigsawJunction(blockpos2.getX(), i3 - j + j3, blockpos2.getZ(), l1, structuretemplatepool$projection1));
                                 poolelementstructurepiece.addJunction(new JigsawJunction(blockpos1.getX(), i3 - k1 + l2, blockpos1.getZ(), -l1, structuretemplatepool$projection));
                                 this.pieces.add(poolelementstructurepiece);
                                 if (p_161640_ + 1 <= this.maxDepth) {
                                    this.placing.addLast(new JigsawPlacement.PieceState(poolelementstructurepiece, mutableobject1, l, p_161640_ + 1));
                                 }
                                 continue label139;
                              }
                           }
                        }
                     }
                  }
               } else {
                  JigsawPlacement.LOGGER.warn("Empty or non-existent fallback pool: {}", (Object)resourcelocation1);
               }
            } else {
               JigsawPlacement.LOGGER.warn("Empty or non-existent pool: {}", (Object)resourcelocation);
            }
         }

      }
   }
}