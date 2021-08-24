package net.minecraft.world.level;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry;

public class SpawnData extends WeightedEntry.IntrusiveBase {
   public static final int DEFAULT_WEIGHT = 1;
   public static final String DEFAULT_TYPE = "minecraft:pig";
   private final CompoundTag tag;

   public SpawnData() {
      super(1);
      this.tag = new CompoundTag();
      this.tag.putString("id", "minecraft:pig");
   }

   public SpawnData(CompoundTag p_47263_) {
      this(p_47263_.contains("Weight", 99) ? p_47263_.getInt("Weight") : 1, p_47263_.getCompound("Entity"));
   }

   public SpawnData(int p_47260_, CompoundTag p_47261_) {
      super(p_47260_);
      this.tag = p_47261_;
      ResourceLocation resourcelocation = ResourceLocation.tryParse(p_47261_.getString("id"));
      if (resourcelocation != null) {
         p_47261_.putString("id", resourcelocation.toString());
      } else {
         p_47261_.putString("id", "minecraft:pig");
      }

   }

   public CompoundTag save() {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.put("Entity", this.tag);
      compoundtag.putInt("Weight", this.getWeight().asInt());
      return compoundtag;
   }

   public CompoundTag getTag() {
      return this.tag;
   }
}