package net.minecraft.world.level.levelgen.structure;

import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

public class NetherFossilPieces {
   private static final ResourceLocation[] FOSSILS = new ResourceLocation[]{new ResourceLocation("nether_fossils/fossil_1"), new ResourceLocation("nether_fossils/fossil_2"), new ResourceLocation("nether_fossils/fossil_3"), new ResourceLocation("nether_fossils/fossil_4"), new ResourceLocation("nether_fossils/fossil_5"), new ResourceLocation("nether_fossils/fossil_6"), new ResourceLocation("nether_fossils/fossil_7"), new ResourceLocation("nether_fossils/fossil_8"), new ResourceLocation("nether_fossils/fossil_9"), new ResourceLocation("nether_fossils/fossil_10"), new ResourceLocation("nether_fossils/fossil_11"), new ResourceLocation("nether_fossils/fossil_12"), new ResourceLocation("nether_fossils/fossil_13"), new ResourceLocation("nether_fossils/fossil_14")};

   public static void addPieces(StructureManager p_162966_, StructurePieceAccessor p_162967_, Random p_162968_, BlockPos p_162969_) {
      Rotation rotation = Rotation.getRandom(p_162968_);
      p_162967_.addPiece(new NetherFossilPieces.NetherFossilPiece(p_162966_, Util.getRandom(FOSSILS, p_162968_), p_162969_, rotation));
   }

   public static class NetherFossilPiece extends TemplateStructurePiece {
      public NetherFossilPiece(StructureManager p_72069_, ResourceLocation p_72070_, BlockPos p_72071_, Rotation p_72072_) {
         super(StructurePieceType.NETHER_FOSSIL, 0, p_72069_, p_72070_, p_72070_.toString(), makeSettings(p_72072_), p_72071_);
      }

      public NetherFossilPiece(ServerLevel p_162971_, CompoundTag p_162972_) {
         super(StructurePieceType.NETHER_FOSSIL, p_162972_, p_162971_, (p_162980_) -> {
            return makeSettings(Rotation.valueOf(p_162972_.getString("Rot")));
         });
      }

      private static StructurePlaceSettings makeSettings(Rotation p_162977_) {
         return (new StructurePlaceSettings()).setRotation(p_162977_).setMirror(Mirror.NONE).addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
      }

      protected void addAdditionalSaveData(ServerLevel p_162974_, CompoundTag p_162975_) {
         super.addAdditionalSaveData(p_162974_, p_162975_);
         p_162975_.putString("Rot", this.placeSettings.getRotation().name());
      }

      protected void handleDataMarker(String p_72084_, BlockPos p_72085_, ServerLevelAccessor p_72086_, Random p_72087_, BoundingBox p_72088_) {
      }

      public boolean postProcess(WorldGenLevel p_72074_, StructureFeatureManager p_72075_, ChunkGenerator p_72076_, Random p_72077_, BoundingBox p_72078_, ChunkPos p_72079_, BlockPos p_72080_) {
         p_72078_.encapsulate(this.template.getBoundingBox(this.placeSettings, this.templatePosition));
         return super.postProcess(p_72074_, p_72075_, p_72076_, p_72077_, p_72078_, p_72079_, p_72080_);
      }
   }
}