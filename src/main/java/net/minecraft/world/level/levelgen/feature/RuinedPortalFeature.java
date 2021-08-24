package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.RuinedPortalPiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class RuinedPortalFeature extends StructureFeature<RuinedPortalConfiguration> {
   static final String[] STRUCTURE_LOCATION_PORTALS = new String[]{"ruined_portal/portal_1", "ruined_portal/portal_2", "ruined_portal/portal_3", "ruined_portal/portal_4", "ruined_portal/portal_5", "ruined_portal/portal_6", "ruined_portal/portal_7", "ruined_portal/portal_8", "ruined_portal/portal_9", "ruined_portal/portal_10"};
   static final String[] STRUCTURE_LOCATION_GIANT_PORTALS = new String[]{"ruined_portal/giant_portal_1", "ruined_portal/giant_portal_2", "ruined_portal/giant_portal_3"};
   private static final float PROBABILITY_OF_GIANT_PORTAL = 0.05F;
   private static final float PROBABILITY_OF_AIR_POCKET = 0.5F;
   private static final float PROBABILITY_OF_UNDERGROUND = 0.5F;
   private static final float UNDERWATER_MOSSINESS = 0.8F;
   private static final float JUNGLE_MOSSINESS = 0.8F;
   private static final float SWAMP_MOSSINESS = 0.5F;
   private static final int MIN_Y = 15;

   public RuinedPortalFeature(Codec<RuinedPortalConfiguration> p_66668_) {
      super(p_66668_);
   }

   public StructureFeature.StructureStartFactory<RuinedPortalConfiguration> getStartFactory() {
      return RuinedPortalFeature.FeatureStart::new;
   }

   static boolean isCold(BlockPos p_66689_, Biome p_66690_) {
      return p_66690_.getTemperature(p_66689_) < 0.15F;
   }

   static int findSuitableY(Random p_160272_, ChunkGenerator p_160273_, RuinedPortalPiece.VerticalPlacement p_160274_, boolean p_160275_, int p_160276_, int p_160277_, BoundingBox p_160278_, LevelHeightAccessor p_160279_) {
      int i;
      if (p_160274_ == RuinedPortalPiece.VerticalPlacement.IN_NETHER) {
         if (p_160275_) {
            i = Mth.randomBetweenInclusive(p_160272_, 32, 100);
         } else if (p_160272_.nextFloat() < 0.5F) {
            i = Mth.randomBetweenInclusive(p_160272_, 27, 29);
         } else {
            i = Mth.randomBetweenInclusive(p_160272_, 29, 100);
         }
      } else if (p_160274_ == RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN) {
         int j = p_160276_ - p_160277_;
         i = getRandomWithinInterval(p_160272_, 70, j);
      } else if (p_160274_ == RuinedPortalPiece.VerticalPlacement.UNDERGROUND) {
         int i1 = p_160276_ - p_160277_;
         i = getRandomWithinInterval(p_160272_, 15, i1);
      } else if (p_160274_ == RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED) {
         i = p_160276_ - p_160277_ + Mth.randomBetweenInclusive(p_160272_, 2, 8);
      } else {
         i = p_160276_;
      }

      List<BlockPos> list1 = ImmutableList.of(new BlockPos(p_160278_.minX(), 0, p_160278_.minZ()), new BlockPos(p_160278_.maxX(), 0, p_160278_.minZ()), new BlockPos(p_160278_.minX(), 0, p_160278_.maxZ()), new BlockPos(p_160278_.maxX(), 0, p_160278_.maxZ()));
      List<NoiseColumn> list = list1.stream().map((p_160270_) -> {
         return p_160273_.getBaseColumn(p_160270_.getX(), p_160270_.getZ(), p_160279_);
      }).collect(Collectors.toList());
      Heightmap.Types heightmap$types = p_160274_ == RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR ? Heightmap.Types.OCEAN_FLOOR_WG : Heightmap.Types.WORLD_SURFACE_WG;
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      int k;
      for(k = i; k > 15; --k) {
         int l = 0;
         blockpos$mutableblockpos.set(0, k, 0);

         for(NoiseColumn noisecolumn : list) {
            BlockState blockstate = noisecolumn.getBlockState(blockpos$mutableblockpos);
            if (heightmap$types.isOpaque().test(blockstate)) {
               ++l;
               if (l == 3) {
                  return k;
               }
            }
         }
      }

      return k;
   }

   private static int getRandomWithinInterval(Random p_66692_, int p_66693_, int p_66694_) {
      return p_66693_ < p_66694_ ? Mth.randomBetweenInclusive(p_66692_, p_66693_, p_66694_) : p_66694_;
   }

   public static class FeatureStart extends StructureStart<RuinedPortalConfiguration> {
      protected FeatureStart(StructureFeature<RuinedPortalConfiguration> p_160281_, ChunkPos p_160282_, int p_160283_, long p_160284_) {
         super(p_160281_, p_160282_, p_160283_, p_160284_);
      }

      public void generatePieces(RegistryAccess p_160294_, ChunkGenerator p_160295_, StructureManager p_160296_, ChunkPos p_160297_, Biome p_160298_, RuinedPortalConfiguration p_160299_, LevelHeightAccessor p_160300_) {
         RuinedPortalPiece.Properties ruinedportalpiece$properties = new RuinedPortalPiece.Properties();
         RuinedPortalPiece.VerticalPlacement ruinedportalpiece$verticalplacement;
         if (p_160299_.portalType == RuinedPortalFeature.Type.DESERT) {
            ruinedportalpiece$verticalplacement = RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED;
            ruinedportalpiece$properties.airPocket = false;
            ruinedportalpiece$properties.mossiness = 0.0F;
         } else if (p_160299_.portalType == RuinedPortalFeature.Type.JUNGLE) {
            ruinedportalpiece$verticalplacement = RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE;
            ruinedportalpiece$properties.airPocket = this.random.nextFloat() < 0.5F;
            ruinedportalpiece$properties.mossiness = 0.8F;
            ruinedportalpiece$properties.overgrown = true;
            ruinedportalpiece$properties.vines = true;
         } else if (p_160299_.portalType == RuinedPortalFeature.Type.SWAMP) {
            ruinedportalpiece$verticalplacement = RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR;
            ruinedportalpiece$properties.airPocket = false;
            ruinedportalpiece$properties.mossiness = 0.5F;
            ruinedportalpiece$properties.vines = true;
         } else if (p_160299_.portalType == RuinedPortalFeature.Type.MOUNTAIN) {
            boolean flag = this.random.nextFloat() < 0.5F;
            ruinedportalpiece$verticalplacement = flag ? RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN : RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE;
            ruinedportalpiece$properties.airPocket = flag || this.random.nextFloat() < 0.5F;
         } else if (p_160299_.portalType == RuinedPortalFeature.Type.OCEAN) {
            ruinedportalpiece$verticalplacement = RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR;
            ruinedportalpiece$properties.airPocket = false;
            ruinedportalpiece$properties.mossiness = 0.8F;
         } else if (p_160299_.portalType == RuinedPortalFeature.Type.NETHER) {
            ruinedportalpiece$verticalplacement = RuinedPortalPiece.VerticalPlacement.IN_NETHER;
            ruinedportalpiece$properties.airPocket = this.random.nextFloat() < 0.5F;
            ruinedportalpiece$properties.mossiness = 0.0F;
            ruinedportalpiece$properties.replaceWithBlackstone = true;
         } else {
            boolean flag1 = this.random.nextFloat() < 0.5F;
            ruinedportalpiece$verticalplacement = flag1 ? RuinedPortalPiece.VerticalPlacement.UNDERGROUND : RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE;
            ruinedportalpiece$properties.airPocket = flag1 || this.random.nextFloat() < 0.5F;
         }

         ResourceLocation resourcelocation;
         if (this.random.nextFloat() < 0.05F) {
            resourcelocation = new ResourceLocation(RuinedPortalFeature.STRUCTURE_LOCATION_GIANT_PORTALS[this.random.nextInt(RuinedPortalFeature.STRUCTURE_LOCATION_GIANT_PORTALS.length)]);
         } else {
            resourcelocation = new ResourceLocation(RuinedPortalFeature.STRUCTURE_LOCATION_PORTALS[this.random.nextInt(RuinedPortalFeature.STRUCTURE_LOCATION_PORTALS.length)]);
         }

         StructureTemplate structuretemplate = p_160296_.getOrCreate(resourcelocation);
         Rotation rotation = Util.getRandom(Rotation.values(), this.random);
         Mirror mirror = this.random.nextFloat() < 0.5F ? Mirror.NONE : Mirror.FRONT_BACK;
         BlockPos blockpos = new BlockPos(structuretemplate.getSize().getX() / 2, 0, structuretemplate.getSize().getZ() / 2);
         BlockPos blockpos1 = p_160297_.getWorldPosition();
         BoundingBox boundingbox = structuretemplate.getBoundingBox(blockpos1, rotation, blockpos, mirror);
         BlockPos blockpos2 = boundingbox.getCenter();
         int i = blockpos2.getX();
         int j = blockpos2.getZ();
         int k = p_160295_.getBaseHeight(i, j, RuinedPortalPiece.getHeightMapType(ruinedportalpiece$verticalplacement), p_160300_) - 1;
         int l = RuinedPortalFeature.findSuitableY(this.random, p_160295_, ruinedportalpiece$verticalplacement, ruinedportalpiece$properties.airPocket, k, boundingbox.getYSpan(), boundingbox, p_160300_);
         BlockPos blockpos3 = new BlockPos(blockpos1.getX(), l, blockpos1.getZ());
         if (p_160299_.portalType == RuinedPortalFeature.Type.MOUNTAIN || p_160299_.portalType == RuinedPortalFeature.Type.OCEAN || p_160299_.portalType == RuinedPortalFeature.Type.STANDARD) {
            ruinedportalpiece$properties.cold = RuinedPortalFeature.isCold(blockpos3, p_160298_);
         }

         this.addPiece(new RuinedPortalPiece(p_160296_, blockpos3, ruinedportalpiece$verticalplacement, ruinedportalpiece$properties, resourcelocation, structuretemplate, rotation, mirror, blockpos));
      }
   }

   public static enum Type implements StringRepresentable {
      STANDARD("standard"),
      DESERT("desert"),
      JUNGLE("jungle"),
      SWAMP("swamp"),
      MOUNTAIN("mountain"),
      OCEAN("ocean"),
      NETHER("nether");

      public static final Codec<RuinedPortalFeature.Type> CODEC = StringRepresentable.fromEnum(RuinedPortalFeature.Type::values, RuinedPortalFeature.Type::byName);
      private static final Map<String, RuinedPortalFeature.Type> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(RuinedPortalFeature.Type::getName, (p_66746_) -> {
         return p_66746_;
      }));
      private final String name;

      private Type(String p_66743_) {
         this.name = p_66743_;
      }

      public String getName() {
         return this.name;
      }

      public static RuinedPortalFeature.Type byName(String p_66748_) {
         return BY_NAME.get(p_66748_);
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}