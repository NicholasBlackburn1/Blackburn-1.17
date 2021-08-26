package net.minecraft.world.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;

public class ArrowItem extends Item
{
    public ArrowItem(Item.Properties p_40512_)
    {
        super(p_40512_);
    }

    public AbstractArrow createArrow(Level pLevel, ItemStack pStack, LivingEntity pShooter)
    {
        Arrow arrow = new Arrow(pLevel, pShooter);
        arrow.setEffectsFromItem(pStack);
        return arrow;
    }
}
