package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.Wearable;
import net.minecraft.world.level.block.Block;

public enum EnchantmentCategory
{
    ARMOR {
        public boolean canEnchant(Item pItem)
        {
            return pItem instanceof ArmorItem;
        }
    },
    ARMOR_FEET {
        public boolean canEnchant(Item pItem)
        {
            return pItem instanceof ArmorItem && ((ArmorItem)pItem).getSlot() == EquipmentSlot.FEET;
        }
    },
    ARMOR_LEGS {
        public boolean canEnchant(Item pItem)
        {
            return pItem instanceof ArmorItem && ((ArmorItem)pItem).getSlot() == EquipmentSlot.LEGS;
        }
    },
    ARMOR_CHEST {
        public boolean canEnchant(Item pItem)
        {
            return pItem instanceof ArmorItem && ((ArmorItem)pItem).getSlot() == EquipmentSlot.CHEST;
        }
    },
    ARMOR_HEAD {
        public boolean canEnchant(Item pItem)
        {
            return pItem instanceof ArmorItem && ((ArmorItem)pItem).getSlot() == EquipmentSlot.HEAD;
        }
    },
    WEAPON {
        public boolean canEnchant(Item pItem)
        {
            return pItem instanceof SwordItem;
        }
    },
    DIGGER {
        public boolean canEnchant(Item pItem)
        {
            return pItem instanceof DiggerItem;
        }
    },
    FISHING_ROD {
        public boolean canEnchant(Item pItem)
        {
            return pItem instanceof FishingRodItem;
        }
    },
    TRIDENT {
        public boolean canEnchant(Item pItem)
        {
            return pItem instanceof TridentItem;
        }
    },
    BREAKABLE {
        public boolean canEnchant(Item pItem)
        {
            return pItem.canBeDepleted();
        }
    },
    BOW {
        public boolean canEnchant(Item pItem)
        {
            return pItem instanceof BowItem;
        }
    },
    WEARABLE {
        public boolean canEnchant(Item pItem)
        {
            return pItem instanceof Wearable || Block.byItem(pItem) instanceof Wearable;
        }
    },
    CROSSBOW {
        public boolean canEnchant(Item pItem)
        {
            return pItem instanceof CrossbowItem;
        }
    },
    VANISHABLE {
        public boolean canEnchant(Item pItem)
        {
            return pItem instanceof Vanishable || Block.byItem(pItem) instanceof Vanishable || BREAKABLE.canEnchant(pItem);
        }
    };

    public abstract boolean canEnchant(Item pItem);
}
