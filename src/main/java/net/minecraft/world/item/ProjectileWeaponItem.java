package net.minecraft.world.item;

import java.util.function.Predicate;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

public abstract class ProjectileWeaponItem extends Item
{
    public static final Predicate<ItemStack> ARROW_ONLY = (p_43017_) ->
    {
        return p_43017_.is(ItemTags.ARROWS);
    };
    public static final Predicate<ItemStack> ARROW_OR_FIREWORK = ARROW_ONLY.or((p_43015_) ->
    {
        return p_43015_.is(Items.FIREWORK_ROCKET);
    });

    public ProjectileWeaponItem(Item.Properties p_43009_)
    {
        super(p_43009_);
    }

    public Predicate<ItemStack> getSupportedHeldProjectiles()
    {
        return this.getAllSupportedProjectiles();
    }

    public abstract Predicate<ItemStack> getAllSupportedProjectiles();

    public static ItemStack getHeldProjectile(LivingEntity pLiving, Predicate<ItemStack> pIsAmmo)
    {
        if (pIsAmmo.test(pLiving.getItemInHand(InteractionHand.OFF_HAND)))
        {
            return pLiving.getItemInHand(InteractionHand.OFF_HAND);
        }
        else
        {
            return pIsAmmo.test(pLiving.getItemInHand(InteractionHand.MAIN_HAND)) ? pLiving.getItemInHand(InteractionHand.MAIN_HAND) : ItemStack.EMPTY;
        }
    }

    public int getEnchantmentValue()
    {
        return 1;
    }

    public abstract int getDefaultProjectileRange();
}
