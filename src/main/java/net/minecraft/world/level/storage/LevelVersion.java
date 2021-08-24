package net.minecraft.world.level.storage;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import net.minecraft.SharedConstants;

public class LevelVersion {
   private final int levelDataVersion;
   private final long lastPlayed;
   private final String minecraftVersionName;
   private final int minecraftVersion;
   private final boolean snapshot;

   public LevelVersion(int p_78384_, long p_78385_, String p_78386_, int p_78387_, boolean p_78388_) {
      this.levelDataVersion = p_78384_;
      this.lastPlayed = p_78385_;
      this.minecraftVersionName = p_78386_;
      this.minecraftVersion = p_78387_;
      this.snapshot = p_78388_;
   }

   public static LevelVersion parse(Dynamic<?> p_78391_) {
      int i = p_78391_.get("version").asInt(0);
      long j = p_78391_.get("LastPlayed").asLong(0L);
      OptionalDynamic<?> optionaldynamic = p_78391_.get("Version");
      return optionaldynamic.result().isPresent() ? new LevelVersion(i, j, optionaldynamic.get("Name").asString(SharedConstants.getCurrentVersion().getName()), optionaldynamic.get("Id").asInt(SharedConstants.getCurrentVersion().getWorldVersion()), optionaldynamic.get("Snapshot").asBoolean(!SharedConstants.getCurrentVersion().isStable())) : new LevelVersion(i, j, "", 0, false);
   }

   public int levelDataVersion() {
      return this.levelDataVersion;
   }

   public long lastPlayed() {
      return this.lastPlayed;
   }

   public String minecraftVersionName() {
      return this.minecraftVersionName;
   }

   public int minecraftVersion() {
      return this.minecraftVersion;
   }

   public boolean snapshot() {
      return this.snapshot;
   }
}