package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.RegistryWriteOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.structures.JigsawJunction;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PoolElementStructurePiece extends StructurePiece {
   private static final Logger LOGGER = LogManager.getLogger();
   protected final StructurePoolElement element;
   protected BlockPos position;
   private final int groundLevelDelta;
   protected final Rotation rotation;
   private final List<JigsawJunction> junctions = Lists.newArrayList();
   private final StructureManager structureManager;

   public PoolElementStructurePiece(StructureManager p_72606_, StructurePoolElement p_72607_, BlockPos p_72608_, int p_72609_, Rotation p_72610_, BoundingBox p_72611_) {
      super(StructurePieceType.JIGSAW, 0, p_72611_);
      this.structureManager = p_72606_;
      this.element = p_72607_;
      this.position = p_72608_;
      this.groundLevelDelta = p_72609_;
      this.rotation = p_72610_;
   }

   public PoolElementStructurePiece(ServerLevel p_163118_, CompoundTag p_163119_) {
      super(StructurePieceType.JIGSAW, p_163119_);
      this.structureManager = p_163118_.getStructureManager();
      this.position = new BlockPos(p_163119_.getInt("PosX"), p_163119_.getInt("PosY"), p_163119_.getInt("PosZ"));
      this.groundLevelDelta = p_163119_.getInt("ground_level_delta");
      RegistryReadOps<Tag> registryreadops = RegistryReadOps.create(NbtOps.INSTANCE, p_163118_.getServer().getResourceManager(), p_163118_.getServer().registryAccess());
      this.element = StructurePoolElement.CODEC.parse(registryreadops, p_163119_.getCompound("pool_element")).resultOrPartial(LOGGER::error).orElseThrow(() -> {
         return new IllegalStateException("Invalid pool element found");
      });
      this.rotation = Rotation.valueOf(p_163119_.getString("rotation"));
      this.boundingBox = this.element.getBoundingBox(this.structureManager, this.position, this.rotation);
      ListTag listtag = p_163119_.getList("junctions", 10);
      this.junctions.clear();
      listtag.forEach((p_163128_) -> {
         this.junctions.add(JigsawJunction.deserialize(new Dynamic<>(registryreadops, p_163128_)));
      });
   }

   protected void addAdditionalSaveData(ServerLevel p_163121_, CompoundTag p_163122_) {
      p_163122_.putInt("PosX", this.position.getX());
      p_163122_.putInt("PosY", this.position.getY());
      p_163122_.putInt("PosZ", this.position.getZ());
      p_163122_.putInt("ground_level_delta", this.groundLevelDelta);
      RegistryWriteOps<Tag> registrywriteops = RegistryWriteOps.create(NbtOps.INSTANCE, p_163121_.getServer().registryAccess());
      StructurePoolElement.CODEC.encodeStart(registrywriteops, this.element).resultOrPartial(LOGGER::error).ifPresent((p_163125_) -> {
         p_163122_.put("pool_element", p_163125_);
      });
      p_163122_.putString("rotation", this.rotation.name());
      ListTag listtag = new ListTag();

      for(JigsawJunction jigsawjunction : this.junctions) {
         listtag.add(jigsawjunction.serialize(registrywriteops).getValue());
      }

      p_163122_.put("junctions", listtag);
   }

   public boolean postProcess(WorldGenLevel p_72620_, StructureFeatureManager p_72621_, ChunkGenerator p_72622_, Random p_72623_, BoundingBox p_72624_, ChunkPos p_72625_, BlockPos p_72626_) {
      return this.place(p_72620_, p_72621_, p_72622_, p_72623_, p_72624_, p_72626_, false);
   }

   public boolean place(WorldGenLevel p_72628_, StructureFeatureManager p_72629_, ChunkGenerator p_72630_, Random p_72631_, BoundingBox p_72632_, BlockPos p_72633_, boolean p_72634_) {
      return this.element.place(this.structureManager, p_72628_, p_72629_, p_72630_, this.position, p_72633_, this.rotation, p_72632_, p_72631_, p_72634_);
   }

   public void move(int p_72616_, int p_72617_, int p_72618_) {
      super.move(p_72616_, p_72617_, p_72618_);
      this.position = this.position.offset(p_72616_, p_72617_, p_72618_);
   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public String toString() {
      return String.format("<%s | %s | %s | %s>", this.getClass().getSimpleName(), this.position, this.rotation, this.element);
   }

   public StructurePoolElement getElement() {
      return this.element;
   }

   public BlockPos getPosition() {
      return this.position;
   }

   public int getGroundLevelDelta() {
      return this.groundLevelDelta;
   }

   public void addJunction(JigsawJunction p_72636_) {
      this.junctions.add(p_72636_);
   }

   public List<JigsawJunction> getJunctions() {
      return this.junctions;
   }
}