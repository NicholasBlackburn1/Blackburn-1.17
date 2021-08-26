package net.minecraft.world.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SuspiciousStewItem extends Item
{
    public static final String EFFECTS_TAG = "Effects";
    public static final String EFFECT_ID_TAG = "EffectId";
    public static final String EFFECT_DURATION_TAG = "EffectDuration";

    public SuspiciousStewItem(Item.Properties p_43257_)
    {
        super(p_43257_);
    }

    public static void saveMobEffect(ItemStack pItemStack, MobEffect pEffect, int pEffectDuration)
    {
        CompoundTag compoundtag = pItemStack.getOrCreateTag();
        ListTag listtag = compoundtag.getList("Effects", 9);
        CompoundTag compoundtag1 = new CompoundTag();
        compoundtag1.putByte("EffectId", (byte)MobEffect.getId(pEffect));
        compoundtag1.putInt("EffectDuration", pEffectDuration);
        listtag.add(compoundtag1);
        compoundtag.put("Effects", listtag);
    }

    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving)
    {
        ItemStack itemstack = super.finishUsingItem(pStack, pLevel, pEntityLiving);
        CompoundTag compoundtag = pStack.getTag();

        if (compoundtag != null && compoundtag.contains("Effects", 9))
        {
            ListTag listtag = compoundtag.getList("Effects", 10);

            for (int i = 0; i < listtag.size(); ++i)
            {
                int j = 160;
                CompoundTag compoundtag1 = listtag.getCompound(i);

                if (compoundtag1.contains("EffectDuration", 3))
                {
                    j = compoundtag1.getInt("EffectDuration");
                }

                MobEffect mobeffect = MobEffect.byId(compoundtag1.getByte("EffectId"));

                if (mobeffect != null)
                {
                    pEntityLiving.addEffect(new MobEffectInstance(mobeffect, j));
                }
            }
        }

        return pEntityLiving instanceof Player && ((Player)pEntityLiving).getAbilities().instabuild ? itemstack : new ItemStack(Items.BOWL);
    }
}
