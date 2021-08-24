package net.minecraft.data.loot;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class GiftLoot implements Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {
   public void accept(BiConsumer<ResourceLocation, LootTable.Builder> p_124402_) {
      p_124402_.accept(BuiltInLootTables.CAT_MORNING_GIFT, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.RABBIT_HIDE).setWeight(10)).add(LootItem.lootTableItem(Items.RABBIT_FOOT).setWeight(10)).add(LootItem.lootTableItem(Items.CHICKEN).setWeight(10)).add(LootItem.lootTableItem(Items.FEATHER).setWeight(10)).add(LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(10)).add(LootItem.lootTableItem(Items.STRING).setWeight(10)).add(LootItem.lootTableItem(Items.PHANTOM_MEMBRANE).setWeight(2))));
      p_124402_.accept(BuiltInLootTables.ARMORER_GIFT, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.CHAINMAIL_HELMET)).add(LootItem.lootTableItem(Items.CHAINMAIL_CHESTPLATE)).add(LootItem.lootTableItem(Items.CHAINMAIL_LEGGINGS)).add(LootItem.lootTableItem(Items.CHAINMAIL_BOOTS))));
      p_124402_.accept(BuiltInLootTables.BUTCHER_GIFT, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.COOKED_RABBIT)).add(LootItem.lootTableItem(Items.COOKED_CHICKEN)).add(LootItem.lootTableItem(Items.COOKED_PORKCHOP)).add(LootItem.lootTableItem(Items.COOKED_BEEF)).add(LootItem.lootTableItem(Items.COOKED_MUTTON))));
      p_124402_.accept(BuiltInLootTables.CARTOGRAPHER_GIFT, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.MAP)).add(LootItem.lootTableItem(Items.PAPER))));
      p_124402_.accept(BuiltInLootTables.CLERIC_GIFT, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.REDSTONE)).add(LootItem.lootTableItem(Items.LAPIS_LAZULI))));
      p_124402_.accept(BuiltInLootTables.FARMER_GIFT, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.BREAD)).add(LootItem.lootTableItem(Items.PUMPKIN_PIE)).add(LootItem.lootTableItem(Items.COOKIE))));
      p_124402_.accept(BuiltInLootTables.FISHERMAN_GIFT, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.COD)).add(LootItem.lootTableItem(Items.SALMON))));
      p_124402_.accept(BuiltInLootTables.FLETCHER_GIFT, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.ARROW).setWeight(26)).add(LootItem.lootTableItem(Items.TIPPED_ARROW).apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F))).apply(SetNbtFunction.setTag(Util.make(new CompoundTag(), (p_124430_) -> {
         p_124430_.putString("Potion", "minecraft:swiftness");
      })))).add(LootItem.lootTableItem(Items.TIPPED_ARROW).apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F))).apply(SetNbtFunction.setTag(Util.make(new CompoundTag(), (p_124428_) -> {
         p_124428_.putString("Potion", "minecraft:slowness");
      })))).add(LootItem.lootTableItem(Items.TIPPED_ARROW).apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F))).apply(SetNbtFunction.setTag(Util.make(new CompoundTag(), (p_124426_) -> {
         p_124426_.putString("Potion", "minecraft:strength");
      })))).add(LootItem.lootTableItem(Items.TIPPED_ARROW).apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F))).apply(SetNbtFunction.setTag(Util.make(new CompoundTag(), (p_124424_) -> {
         p_124424_.putString("Potion", "minecraft:healing");
      })))).add(LootItem.lootTableItem(Items.TIPPED_ARROW).apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F))).apply(SetNbtFunction.setTag(Util.make(new CompoundTag(), (p_124422_) -> {
         p_124422_.putString("Potion", "minecraft:harming");
      })))).add(LootItem.lootTableItem(Items.TIPPED_ARROW).apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F))).apply(SetNbtFunction.setTag(Util.make(new CompoundTag(), (p_124420_) -> {
         p_124420_.putString("Potion", "minecraft:leaping");
      })))).add(LootItem.lootTableItem(Items.TIPPED_ARROW).apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F))).apply(SetNbtFunction.setTag(Util.make(new CompoundTag(), (p_124418_) -> {
         p_124418_.putString("Potion", "minecraft:regeneration");
      })))).add(LootItem.lootTableItem(Items.TIPPED_ARROW).apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F))).apply(SetNbtFunction.setTag(Util.make(new CompoundTag(), (p_124416_) -> {
         p_124416_.putString("Potion", "minecraft:fire_resistance");
      })))).add(LootItem.lootTableItem(Items.TIPPED_ARROW).apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F))).apply(SetNbtFunction.setTag(Util.make(new CompoundTag(), (p_124414_) -> {
         p_124414_.putString("Potion", "minecraft:water_breathing");
      })))).add(LootItem.lootTableItem(Items.TIPPED_ARROW).apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F))).apply(SetNbtFunction.setTag(Util.make(new CompoundTag(), (p_124412_) -> {
         p_124412_.putString("Potion", "minecraft:invisibility");
      })))).add(LootItem.lootTableItem(Items.TIPPED_ARROW).apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F))).apply(SetNbtFunction.setTag(Util.make(new CompoundTag(), (p_124410_) -> {
         p_124410_.putString("Potion", "minecraft:night_vision");
      })))).add(LootItem.lootTableItem(Items.TIPPED_ARROW).apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F))).apply(SetNbtFunction.setTag(Util.make(new CompoundTag(), (p_124408_) -> {
         p_124408_.putString("Potion", "minecraft:weakness");
      })))).add(LootItem.lootTableItem(Items.TIPPED_ARROW).apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F))).apply(SetNbtFunction.setTag(Util.make(new CompoundTag(), (p_124404_) -> {
         p_124404_.putString("Potion", "minecraft:poison");
      }))))));
      p_124402_.accept(BuiltInLootTables.LEATHERWORKER_GIFT, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.LEATHER))));
      p_124402_.accept(BuiltInLootTables.LIBRARIAN_GIFT, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.BOOK))));
      p_124402_.accept(BuiltInLootTables.MASON_GIFT, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.CLAY))));
      p_124402_.accept(BuiltInLootTables.SHEPHERD_GIFT, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.WHITE_WOOL)).add(LootItem.lootTableItem(Items.ORANGE_WOOL)).add(LootItem.lootTableItem(Items.MAGENTA_WOOL)).add(LootItem.lootTableItem(Items.LIGHT_BLUE_WOOL)).add(LootItem.lootTableItem(Items.YELLOW_WOOL)).add(LootItem.lootTableItem(Items.LIME_WOOL)).add(LootItem.lootTableItem(Items.PINK_WOOL)).add(LootItem.lootTableItem(Items.GRAY_WOOL)).add(LootItem.lootTableItem(Items.LIGHT_GRAY_WOOL)).add(LootItem.lootTableItem(Items.CYAN_WOOL)).add(LootItem.lootTableItem(Items.PURPLE_WOOL)).add(LootItem.lootTableItem(Items.BLUE_WOOL)).add(LootItem.lootTableItem(Items.BROWN_WOOL)).add(LootItem.lootTableItem(Items.GREEN_WOOL)).add(LootItem.lootTableItem(Items.RED_WOOL)).add(LootItem.lootTableItem(Items.BLACK_WOOL))));
      p_124402_.accept(BuiltInLootTables.TOOLSMITH_GIFT, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.STONE_PICKAXE)).add(LootItem.lootTableItem(Items.STONE_AXE)).add(LootItem.lootTableItem(Items.STONE_HOE)).add(LootItem.lootTableItem(Items.STONE_SHOVEL))));
      p_124402_.accept(BuiltInLootTables.WEAPONSMITH_GIFT, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.STONE_AXE)).add(LootItem.lootTableItem(Items.GOLDEN_AXE)).add(LootItem.lootTableItem(Items.IRON_AXE))));
   }
}