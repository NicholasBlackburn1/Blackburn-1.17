package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetContainerLootTable extends LootItemConditionalFunction {
   final ResourceLocation name;
   final long seed;

   SetContainerLootTable(LootItemCondition[] p_80958_, ResourceLocation p_80959_, long p_80960_) {
      super(p_80958_);
      this.name = p_80959_;
      this.seed = p_80960_;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_LOOT_TABLE;
   }

   public ItemStack run(ItemStack p_80967_, LootContext p_80968_) {
      if (p_80967_.isEmpty()) {
         return p_80967_;
      } else {
         CompoundTag compoundtag = new CompoundTag();
         compoundtag.putString("LootTable", this.name.toString());
         if (this.seed != 0L) {
            compoundtag.putLong("LootTableSeed", this.seed);
         }

         p_80967_.getOrCreateTag().put("BlockEntityTag", compoundtag);
         return p_80967_;
      }
   }

   public void validate(ValidationContext p_80970_) {
      if (p_80970_.hasVisitedTable(this.name)) {
         p_80970_.reportProblem("Table " + this.name + " is recursively called");
      } else {
         super.validate(p_80970_);
         LootTable loottable = p_80970_.resolveLootTable(this.name);
         if (loottable == null) {
            p_80970_.reportProblem("Unknown loot table called " + this.name);
         } else {
            loottable.validate(p_80970_.enterTable("->{" + this.name + "}", this.name));
         }

      }
   }

   public static LootItemConditionalFunction.Builder<?> withLootTable(ResourceLocation p_165323_) {
      return simpleBuilder((p_165333_) -> {
         return new SetContainerLootTable(p_165333_, p_165323_, 0L);
      });
   }

   public static LootItemConditionalFunction.Builder<?> withLootTable(ResourceLocation p_165325_, long p_165326_) {
      return simpleBuilder((p_165330_) -> {
         return new SetContainerLootTable(p_165330_, p_165325_, p_165326_);
      });
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetContainerLootTable> {
      public void serialize(JsonObject p_80986_, SetContainerLootTable p_80987_, JsonSerializationContext p_80988_) {
         super.serialize(p_80986_, p_80987_, p_80988_);
         p_80986_.addProperty("name", p_80987_.name.toString());
         if (p_80987_.seed != 0L) {
            p_80986_.addProperty("seed", p_80987_.seed);
         }

      }

      public SetContainerLootTable deserialize(JsonObject p_80978_, JsonDeserializationContext p_80979_, LootItemCondition[] p_80980_) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(p_80978_, "name"));
         long i = GsonHelper.getAsLong(p_80978_, "seed", 0L);
         return new SetContainerLootTable(p_80980_, resourcelocation, i);
      }
   }
}