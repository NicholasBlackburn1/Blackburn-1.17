package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class ElytraItem extends Item implements Wearable
{
    public ElytraItem(Item.Properties p_41132_)
    {
        super(p_41132_);
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
    }

    public static boolean isFlyEnabled(ItemStack pStack)
    {
        return pStack.getDamageValue() < pStack.getMaxDamage() - 1;
    }

    public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair)
    {
        return pRepair.is(Items.PHANTOM_MEMBRANE);
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand)
    {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(itemstack);
        ItemStack itemstack1 = pPlayer.getItemBySlot(equipmentslot);

        if (itemstack1.isEmpty())
        {
            pPlayer.setItemSlot(equipmentslot, itemstack.copy());

            if (!pLevel.isClientSide())
            {
                pPlayer.awardStat(Stats.ITEM_USED.get(this));
            }

            itemstack.setCount(0);
            return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
        }
        else
        {
            return InteractionResultHolder.fail(itemstack);
        }
    }

    @Nullable
    public SoundEvent getEquipSound()
    {
        return SoundEvents.ARMOR_EQUIP_ELYTRA;
    }
}
