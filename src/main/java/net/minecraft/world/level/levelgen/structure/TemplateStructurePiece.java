package net.minecraft.world.level.levelgen.structure;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class TemplateStructurePiece extends StructurePiece {
   private static final Logger LOGGER = LogManager.getLogger();
   protected final String templateName;
   protected StructureTemplate template;
   protected StructurePlaceSettings placeSettings;
   protected BlockPos templatePosition;

   public TemplateStructurePiece(StructurePieceType p_163660_, int p_163661_, StructureManager p_163662_, ResourceLocation p_163663_, String p_163664_, StructurePlaceSettings p_163665_, BlockPos p_163666_) {
      super(p_163660_, p_163661_, p_163662_.getOrCreate(p_163663_).getBoundingBox(p_163665_, p_163666_));
      this.setOrientation(Direction.NORTH);
      this.templateName = p_163664_;
      this.templatePosition = p_163666_;
      this.template = p_163662_.getOrCreate(p_163663_);
      this.placeSettings = p_163665_;
   }

   public TemplateStructurePiece(StructurePieceType p_163668_, CompoundTag p_163669_, ServerLevel p_163670_, Function<ResourceLocation, StructurePlaceSettings> p_163671_) {
      super(p_163668_, p_163669_);
      this.setOrientation(Direction.NORTH);
      this.templateName = p_163669_.getString("Template");
      this.templatePosition = new BlockPos(p_163669_.getInt("TPX"), p_163669_.getInt("TPY"), p_163669_.getInt("TPZ"));
      ResourceLocation resourcelocation = this.makeTemplateLocation();
      this.template = p_163670_.getStructureManager().getOrCreate(resourcelocation);
      this.placeSettings = p_163671_.apply(resourcelocation);
      this.boundingBox = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
   }

   protected ResourceLocation makeTemplateLocation() {
      return new ResourceLocation(this.templateName);
   }

   protected void addAdditionalSaveData(ServerLevel p_163673_, CompoundTag p_163674_) {
      p_163674_.putInt("TPX", this.templatePosition.getX());
      p_163674_.putInt("TPY", this.templatePosition.getY());
      p_163674_.putInt("TPZ", this.templatePosition.getZ());
      p_163674_.putString("Template", this.templateName);
   }

   public boolean postProcess(WorldGenLevel p_73672_, StructureFeatureManager p_73673_, ChunkGenerator p_73674_, Random p_73675_, BoundingBox p_73676_, ChunkPos p_73677_, BlockPos p_73678_) {
      this.placeSettings.setBoundingBox(p_73676_);
      this.boundingBox = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
      if (this.template.placeInWorld(p_73672_, this.templatePosition, p_73678_, this.placeSettings, p_73675_, 2)) {
         for(StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo : this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.STRUCTURE_BLOCK)) {
            if (structuretemplate$structureblockinfo.nbt != null) {
               StructureMode structuremode = StructureMode.valueOf(structuretemplate$structureblockinfo.nbt.getString("mode"));
               if (structuremode == StructureMode.DATA) {
                  this.handleDataMarker(structuretemplate$structureblockinfo.nbt.getString("metadata"), structuretemplate$structureblockinfo.pos, p_73672_, p_73675_, p_73676_);
               }
            }
         }

         for(StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo1 : this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.JIGSAW)) {
            if (structuretemplate$structureblockinfo1.nbt != null) {
               String s = structuretemplate$structureblockinfo1.nbt.getString("final_state");
               BlockStateParser blockstateparser = new BlockStateParser(new StringReader(s), false);
               BlockState blockstate = Blocks.AIR.defaultBlockState();

               try {
                  blockstateparser.parse(true);
                  BlockState blockstate1 = blockstateparser.getState();
                  if (blockstate1 != null) {
                     blockstate = blockstate1;
                  } else {
                     LOGGER.error("Error while parsing blockstate {} in jigsaw block @ {}", s, structuretemplate$structureblockinfo1.pos);
                  }
               } catch (CommandSyntaxException commandsyntaxexception) {
                  LOGGER.error("Error while parsing blockstate {} in jigsaw block @ {}", s, structuretemplate$structureblockinfo1.pos);
               }

               p_73672_.setBlock(structuretemplate$structureblockinfo1.pos, blockstate, 3);
            }
         }
      }

      return true;
   }

   protected abstract void handleDataMarker(String p_73683_, BlockPos p_73684_, ServerLevelAccessor p_73685_, Random p_73686_, BoundingBox p_73687_);

   public void move(int p_73668_, int p_73669_, int p_73670_) {
      super.move(p_73668_, p_73669_, p_73670_);
      this.templatePosition = this.templatePosition.offset(p_73668_, p_73669_, p_73670_);
   }

   public Rotation getRotation() {
      return this.placeSettings.getRotation();
   }
}