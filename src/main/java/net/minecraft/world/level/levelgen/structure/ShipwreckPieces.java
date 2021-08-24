package net.minecraft.world.level.levelgen.structure;

import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class ShipwreckPieces {
   static final BlockPos PIVOT = new BlockPos(4, 0, 15);
   private static final ResourceLocation[] STRUCTURE_LOCATION_BEACHED = new ResourceLocation[]{new ResourceLocation("shipwreck/with_mast"), new ResourceLocation("shipwreck/sideways_full"), new ResourceLocation("shipwreck/sideways_fronthalf"), new ResourceLocation("shipwreck/sideways_backhalf"), new ResourceLocation("shipwreck/rightsideup_full"), new ResourceLocation("shipwreck/rightsideup_fronthalf"), new ResourceLocation("shipwreck/rightsideup_backhalf"), new ResourceLocation("shipwreck/with_mast_degraded"), new ResourceLocation("shipwreck/rightsideup_full_degraded"), new ResourceLocation("shipwreck/rightsideup_fronthalf_degraded"), new ResourceLocation("shipwreck/rightsideup_backhalf_degraded")};
   private static final ResourceLocation[] STRUCTURE_LOCATION_OCEAN = new ResourceLocation[]{new ResourceLocation("shipwreck/with_mast"), new ResourceLocation("shipwreck/upsidedown_full"), new ResourceLocation("shipwreck/upsidedown_fronthalf"), new ResourceLocation("shipwreck/upsidedown_backhalf"), new ResourceLocation("shipwreck/sideways_full"), new ResourceLocation("shipwreck/sideways_fronthalf"), new ResourceLocation("shipwreck/sideways_backhalf"), new ResourceLocation("shipwreck/rightsideup_full"), new ResourceLocation("shipwreck/rightsideup_fronthalf"), new ResourceLocation("shipwreck/rightsideup_backhalf"), new ResourceLocation("shipwreck/with_mast_degraded"), new ResourceLocation("shipwreck/upsidedown_full_degraded"), new ResourceLocation("shipwreck/upsidedown_fronthalf_degraded"), new ResourceLocation("shipwreck/upsidedown_backhalf_degraded"), new ResourceLocation("shipwreck/sideways_full_degraded"), new ResourceLocation("shipwreck/sideways_fronthalf_degraded"), new ResourceLocation("shipwreck/sideways_backhalf_degraded"), new ResourceLocation("shipwreck/rightsideup_full_degraded"), new ResourceLocation("shipwreck/rightsideup_fronthalf_degraded"), new ResourceLocation("shipwreck/rightsideup_backhalf_degraded")};

   public static void addPieces(StructureManager p_163201_, BlockPos p_163202_, Rotation p_163203_, StructurePieceAccessor p_163204_, Random p_163205_, ShipwreckConfiguration p_163206_) {
      ResourceLocation resourcelocation = Util.getRandom(p_163206_.isBeached ? STRUCTURE_LOCATION_BEACHED : STRUCTURE_LOCATION_OCEAN, p_163205_);
      p_163204_.addPiece(new ShipwreckPieces.ShipwreckPiece(p_163201_, resourcelocation, p_163202_, p_163203_, p_163206_.isBeached));
   }

   public static class ShipwreckPiece extends TemplateStructurePiece {
      private final boolean isBeached;

      public ShipwreckPiece(StructureManager p_72828_, ResourceLocation p_72829_, BlockPos p_72830_, Rotation p_72831_, boolean p_72832_) {
         super(StructurePieceType.SHIPWRECK_PIECE, 0, p_72828_, p_72829_, p_72829_.toString(), makeSettings(p_72831_), p_72830_);
         this.isBeached = p_72832_;
      }

      public ShipwreckPiece(ServerLevel p_163208_, CompoundTag p_163209_) {
         super(StructurePieceType.SHIPWRECK_PIECE, p_163209_, p_163208_, (p_163217_) -> {
            return makeSettings(Rotation.valueOf(p_163209_.getString("Rot")));
         });
         this.isBeached = p_163209_.getBoolean("isBeached");
      }

      protected void addAdditionalSaveData(ServerLevel p_163211_, CompoundTag p_163212_) {
         super.addAdditionalSaveData(p_163211_, p_163212_);
         p_163212_.putBoolean("isBeached", this.isBeached);
         p_163212_.putString("Rot", this.placeSettings.getRotation().name());
      }

      private static StructurePlaceSettings makeSettings(Rotation p_163214_) {
         return (new StructurePlaceSettings()).setRotation(p_163214_).setMirror(Mirror.NONE).setRotationPivot(ShipwreckPieces.PIVOT).addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
      }

      protected void handleDataMarker(String p_72844_, BlockPos p_72845_, ServerLevelAccessor p_72846_, Random p_72847_, BoundingBox p_72848_) {
         if ("map_chest".equals(p_72844_)) {
            RandomizableContainerBlockEntity.setLootTable(p_72846_, p_72847_, p_72845_.below(), BuiltInLootTables.SHIPWRECK_MAP);
         } else if ("treasure_chest".equals(p_72844_)) {
            RandomizableContainerBlockEntity.setLootTable(p_72846_, p_72847_, p_72845_.below(), BuiltInLootTables.SHIPWRECK_TREASURE);
         } else if ("supply_chest".equals(p_72844_)) {
            RandomizableContainerBlockEntity.setLootTable(p_72846_, p_72847_, p_72845_.below(), BuiltInLootTables.SHIPWRECK_SUPPLY);
         }

      }

      public boolean postProcess(WorldGenLevel p_72834_, StructureFeatureManager p_72835_, ChunkGenerator p_72836_, Random p_72837_, BoundingBox p_72838_, ChunkPos p_72839_, BlockPos p_72840_) {
         int i = p_72834_.getMaxBuildHeight();
         int j = 0;
         Vec3i vec3i = this.template.getSize();
         Heightmap.Types heightmap$types = this.isBeached ? Heightmap.Types.WORLD_SURFACE_WG : Heightmap.Types.OCEAN_FLOOR_WG;
         int k = vec3i.getX() * vec3i.getZ();
         if (k == 0) {
            j = p_72834_.getHeight(heightmap$types, this.templatePosition.getX(), this.templatePosition.getZ());
         } else {
            BlockPos blockpos = this.templatePosition.offset(vec3i.getX() - 1, 0, vec3i.getZ() - 1);

            for(BlockPos blockpos1 : BlockPos.betweenClosed(this.templatePosition, blockpos)) {
               int l = p_72834_.getHeight(heightmap$types, blockpos1.getX(), blockpos1.getZ());
               j += l;
               i = Math.min(i, l);
            }

            j = j / k;
         }

         int i1 = this.isBeached ? i - vec3i.getY() / 2 - p_72837_.nextInt(3) : j;
         this.templatePosition = new BlockPos(this.templatePosition.getX(), i1, this.templatePosition.getZ());
         return super.postProcess(p_72834_, p_72835_, p_72836_, p_72837_, p_72838_, p_72839_, p_72840_);
      }
   }
}