package net.minecraft.world.item.enchantment;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public class ProtectionEnchantment extends Enchantment
{
    public final ProtectionEnchantment.Type type;

    public ProtectionEnchantment(Enchantment.Rarity p_45126_, ProtectionEnchantment.Type p_45127_, EquipmentSlot... p_45128_)
    {
        super(p_45126_, p_45127_ == ProtectionEnchantment.Type.FALL ? EnchantmentCategory.ARMOR_FEET : EnchantmentCategory.ARMOR, p_45128_);
        this.type = p_45127_;
    }

    public int getMinCost(int pEnchantmentLevel)
    {
        return this.type.getMinCost() + (pEnchantmentLevel - 1) * this.type.getLevelCost();
    }

    public int getMaxCost(int pEnchantmentLevel)
    {
        return this.getMinCost(pEnchantmentLevel) + this.type.getLevelCost();
    }

    public int getMaxLevel()
    {
        return 4;
    }

    public int getDamageProtection(int pLevel, DamageSource pSource)
    {
        if (pSource.isBypassInvul())
        {
            return 0;
        }
        else if (this.type == ProtectionEnchantment.Type.ALL)
        {
            return pLevel;
        }
        else if (this.type == ProtectionEnchantment.Type.FIRE && pSource.isFire())
        {
            return pLevel * 2;
        }
        else if (this.type == ProtectionEnchantment.Type.FALL && pSource.isFall())
        {
            return pLevel * 3;
        }
        else if (this.type == ProtectionEnchantment.Type.EXPLOSION && pSource.isExplosion())
        {
            return pLevel * 2;
        }
        else
        {
            return this.type == ProtectionEnchantment.Type.PROJECTILE && pSource.isProjectile() ? pLevel * 2 : 0;
        }
    }

    public boolean checkCompatibility(Enchantment pEnch)
    {
        if (pEnch instanceof ProtectionEnchantment)
        {
            ProtectionEnchantment protectionenchantment = (ProtectionEnchantment)pEnch;

            if (this.type == protectionenchantment.type)
            {
                return false;
            }
            else
            {
                return this.type == ProtectionEnchantment.Type.FALL || protectionenchantment.type == ProtectionEnchantment.Type.FALL;
            }
        }
        else
        {
            return super.checkCompatibility(pEnch);
        }
    }

    public static int getFireAfterDampener(LivingEntity pLivingEntity, int pLevel)
    {
        int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_PROTECTION, pLivingEntity);

        if (i > 0)
        {
            pLevel -= Mth.floor((float)pLevel * (float)i * 0.15F);
        }

        return pLevel;
    }

    public static double getExplosionKnockbackAfterDampener(LivingEntity pLivingEntity, double pDamage)
    {
        int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.BLAST_PROTECTION, pLivingEntity);

        if (i > 0)
        {
            pDamage -= (double)Mth.floor(pDamage * (double)((float)i * 0.15F));
        }

        return pDamage;
    }

    public static enum Type
    {
        ALL(1, 11),
        FIRE(10, 8),
        FALL(5, 6),
        EXPLOSION(5, 8),
        PROJECTILE(3, 6);

        private final int minCost;
        private final int levelCost;

        private Type(int p_151299_, int p_151300_)
        {
            this.minCost = p_151299_;
            this.levelCost = p_151300_;
        }

        public int getMinCost()
        {
            return this.minCost;
        }

        public int getLevelCost()
        {
            return this.levelCost;
        }
    }
}
