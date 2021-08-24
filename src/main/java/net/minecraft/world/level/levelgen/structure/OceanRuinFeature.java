package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class OceanRuinFeature extends StructureFeature<OceanRuinConfiguration> {
   public OceanRuinFeature(Codec<OceanRuinConfiguration> p_72474_) {
      super(p_72474_);
   }

   public StructureFeature.StructureStartFactory<OceanRuinConfiguration> getStartFactory() {
      return OceanRuinFeature.OceanRuinStart::new;
   }

   public static class OceanRuinStart extends StructureStart<OceanRuinConfiguration> {
      public OceanRuinStart(StructureFeature<OceanRuinConfiguration> p_163056_, ChunkPos p_163057_, int p_163058_, long p_163059_) {
         super(p_163056_, p_163057_, p_163058_, p_163059_);
      }

      public void generatePieces(RegistryAccess p_163069_, ChunkGenerator p_163070_, StructureManager p_163071_, ChunkPos p_163072_, Biome p_163073_, OceanRuinConfiguration p_163074_, LevelHeightAccessor p_163075_) {
         BlockPos blockpos = new BlockPos(p_163072_.getMinBlockX(), 90, p_163072_.getMinBlockZ());
         Rotation rotation = Rotation.getRandom(this.random);
         OceanRuinPieces.addPieces(p_163071_, blockpos, rotation, this, this.random, p_163074_);
      }
   }

   public static enum Type implements StringRepresentable {
      WARM("warm"),
      COLD("cold");

      public static final Codec<OceanRuinFeature.Type> CODEC = StringRepresentable.fromEnum(OceanRuinFeature.Type::values, OceanRuinFeature.Type::byName);
      private static final Map<String, OceanRuinFeature.Type> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(OceanRuinFeature.Type::getName, (p_72512_) -> {
         return p_72512_;
      }));
      private final String name;

      private Type(String p_72509_) {
         this.name = p_72509_;
      }

      public String getName() {
         return this.name;
      }

      @Nullable
      public static OceanRuinFeature.Type byName(String p_72514_) {
         return BY_NAME.get(p_72514_);
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}