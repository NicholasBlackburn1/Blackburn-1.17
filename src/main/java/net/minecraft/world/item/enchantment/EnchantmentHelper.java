package net.minecraft.world.item.enchantment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;

public class EnchantmentHelper
{
    private static final String f_182430_ = "id";
    private static final String f_182431_ = "lvl";

    public static CompoundTag m_182443_(@Nullable ResourceLocation p_182444_, int p_182445_)
    {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putString("id", String.valueOf((Object)p_182444_));
        compoundtag.putShort("lvl", (short)p_182445_);
        return compoundtag;
    }

    public static void m_182440_(CompoundTag p_182441_, int p_182442_)
    {
        p_182441_.putShort("lvl", (short)p_182442_);
    }

    public static int m_182438_(CompoundTag p_182439_)
    {
        return Mth.clamp(p_182439_.getInt("lvl"), 0, 255);
    }

    @Nullable
    public static ResourceLocation m_182446_(CompoundTag p_182447_)
    {
        return ResourceLocation.tryParse(p_182447_.getString("id"));
    }

    @Nullable
    public static ResourceLocation m_182432_(Enchantment p_182433_)
    {
        return Registry.ENCHANTMENT.getKey(p_182433_);
    }

    public static int getItemEnchantmentLevel(Enchantment pEnchID, ItemStack pStack)
    {
        if (pStack.isEmpty())
        {
            return 0;
        }
        else
        {
            ResourceLocation resourcelocation = m_182432_(pEnchID);
            ListTag listtag = pStack.getEnchantmentTags();

            for (int i = 0; i < listtag.size(); ++i)
            {
                CompoundTag compoundtag = listtag.getCompound(i);
                ResourceLocation resourcelocation1 = m_182446_(compoundtag);

                if (resourcelocation1 != null && resourcelocation1.equals(resourcelocation))
                {
                    return m_182438_(compoundtag);
                }
            }

            return 0;
        }
    }

    public static Map<Enchantment, Integer> getEnchantments(ItemStack pStack)
    {
        ListTag listtag = pStack.is(Items.ENCHANTED_BOOK) ? EnchantedBookItem.getEnchantments(pStack) : pStack.getEnchantmentTags();
        return deserializeEnchantments(listtag);
    }

    public static Map<Enchantment, Integer> deserializeEnchantments(ListTag pSerialized)
    {
        Map<Enchantment, Integer> map = Maps.newLinkedHashMap();

        for (int i = 0; i < pSerialized.size(); ++i)
        {
            CompoundTag compoundtag = pSerialized.getCompound(i);
            Registry.ENCHANTMENT.getOptional(m_182446_(compoundtag)).ifPresent((p_44871_) ->
            {
                map.put(p_44871_, m_182438_(compoundtag));
            });
        }

        return map;
    }

    public static void setEnchantments(Map<Enchantment, Integer> pEnchMap, ItemStack pStack)
    {
        ListTag listtag = new ListTag();

        for (Entry<Enchantment, Integer> entry : pEnchMap.entrySet())
        {
            Enchantment enchantment = entry.getKey();

            if (enchantment != null)
            {
                int i = entry.getValue();
                listtag.add(m_182443_(m_182432_(enchantment), i));

                if (pStack.is(Items.ENCHANTED_BOOK))
                {
                    EnchantedBookItem.addEnchantment(pStack, new EnchantmentInstance(enchantment, i));
                }
            }
        }

        if (listtag.isEmpty())
        {
            pStack.removeTagKey("Enchantments");
        }
        else if (!pStack.is(Items.ENCHANTED_BOOK))
        {
            pStack.addTagElement("Enchantments", listtag);
        }
    }

    private static void runIterationOnItem(EnchantmentHelper.EnchantmentVisitor pModifier, ItemStack pStack)
    {
        if (!pStack.isEmpty())
        {
            ListTag listtag = pStack.getEnchantmentTags();

            for (int i = 0; i < listtag.size(); ++i)
            {
                CompoundTag compoundtag = listtag.getCompound(i);
                Registry.ENCHANTMENT.getOptional(m_182446_(compoundtag)).ifPresent((p_182437_) ->
                {
                    pModifier.accept(p_182437_, m_182438_(compoundtag));
                });
            }
        }
    }

    private static void runIterationOnInventory(EnchantmentHelper.EnchantmentVisitor pModifier, Iterable<ItemStack> pStacks)
    {
        for (ItemStack itemstack : pStacks)
        {
            runIterationOnItem(pModifier, itemstack);
        }
    }

    public static int getDamageProtection(Iterable<ItemStack> pStacks, DamageSource pSource)
    {
        MutableInt mutableint = new MutableInt();
        runIterationOnInventory((p_44892_, p_44893_) ->
        {
            mutableint.add(p_44892_.getDamageProtection(p_44893_, pSource));
        }, pStacks);
        return mutableint.intValue();
    }

    public static float getDamageBonus(ItemStack pStack, MobType pCreatureAttribute)
    {
        MutableFloat mutablefloat = new MutableFloat();
        runIterationOnItem((p_44887_, p_44888_) ->
        {
            mutablefloat.add(p_44887_.getDamageBonus(p_44888_, pCreatureAttribute));
        }, pStack);
        return mutablefloat.floatValue();
    }

    public static float getSweepingDamageRatio(LivingEntity pEntity)
    {
        int i = getEnchantmentLevel(Enchantments.SWEEPING_EDGE, pEntity);
        return i > 0 ? SweepingEdgeEnchantment.getSweepingDamageRatio(i) : 0.0F;
    }

    public static void doPostHurtEffects(LivingEntity pUser, Entity pAttacker)
    {
        EnchantmentHelper.EnchantmentVisitor enchantmenthelper$enchantmentvisitor = (p_44902_, p_44903_) ->
        {
            p_44902_.doPostHurt(pUser, pAttacker, p_44903_);
        };

        if (pUser != null)
        {
            runIterationOnInventory(enchantmenthelper$enchantmentvisitor, pUser.getAllSlots());
        }

        if (pAttacker instanceof Player)
        {
            runIterationOnItem(enchantmenthelper$enchantmentvisitor, pUser.getMainHandItem());
        }
    }

    public static void doPostDamageEffects(LivingEntity pUser, Entity pTarget)
    {
        EnchantmentHelper.EnchantmentVisitor enchantmenthelper$enchantmentvisitor = (p_44829_, p_44830_) ->
        {
            p_44829_.doPostAttack(pUser, pTarget, p_44830_);
        };

        if (pUser != null)
        {
            runIterationOnInventory(enchantmenthelper$enchantmentvisitor, pUser.getAllSlots());
        }

        if (pUser instanceof Player)
        {
            runIterationOnItem(enchantmenthelper$enchantmentvisitor, pUser.getMainHandItem());
        }
    }

    public static int getEnchantmentLevel(Enchantment pEnchantment, LivingEntity pEntity)
    {
        Iterable<ItemStack> iterable = pEnchantment.getSlotItems(pEntity).values();

        if (iterable == null)
        {
            return 0;
        }
        else
        {
            int i = 0;

            for (ItemStack itemstack : iterable)
            {
                int j = getItemEnchantmentLevel(pEnchantment, itemstack);

                if (j > i)
                {
                    i = j;
                }
            }

            return i;
        }
    }

    public static int getKnockbackBonus(LivingEntity pPlayer)
    {
        return getEnchantmentLevel(Enchantments.KNOCKBACK, pPlayer);
    }

    public static int getFireAspect(LivingEntity pPlayer)
    {
        return getEnchantmentLevel(Enchantments.FIRE_ASPECT, pPlayer);
    }

    public static int getRespiration(LivingEntity pEntity)
    {
        return getEnchantmentLevel(Enchantments.RESPIRATION, pEntity);
    }

    public static int getDepthStrider(LivingEntity pEntity)
    {
        return getEnchantmentLevel(Enchantments.DEPTH_STRIDER, pEntity);
    }

    public static int getBlockEfficiency(LivingEntity pEntity)
    {
        return getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, pEntity);
    }

    public static int getFishingLuckBonus(ItemStack pStack)
    {
        return getItemEnchantmentLevel(Enchantments.FISHING_LUCK, pStack);
    }

    public static int getFishingSpeedBonus(ItemStack pStack)
    {
        return getItemEnchantmentLevel(Enchantments.FISHING_SPEED, pStack);
    }

    public static int getMobLooting(LivingEntity pEntity)
    {
        return getEnchantmentLevel(Enchantments.MOB_LOOTING, pEntity);
    }

    public static boolean hasAquaAffinity(LivingEntity pEntity)
    {
        return getEnchantmentLevel(Enchantments.AQUA_AFFINITY, pEntity) > 0;
    }

    public static boolean hasFrostWalker(LivingEntity pPlayer)
    {
        return getEnchantmentLevel(Enchantments.FROST_WALKER, pPlayer) > 0;
    }

    public static boolean hasSoulSpeed(LivingEntity pEntity)
    {
        return getEnchantmentLevel(Enchantments.SOUL_SPEED, pEntity) > 0;
    }

    public static boolean hasBindingCurse(ItemStack pStack)
    {
        return getItemEnchantmentLevel(Enchantments.BINDING_CURSE, pStack) > 0;
    }

    public static boolean hasVanishingCurse(ItemStack pStack)
    {
        return getItemEnchantmentLevel(Enchantments.VANISHING_CURSE, pStack) > 0;
    }

    public static int getLoyalty(ItemStack pStack)
    {
        return getItemEnchantmentLevel(Enchantments.LOYALTY, pStack);
    }

    public static int getRiptide(ItemStack pStack)
    {
        return getItemEnchantmentLevel(Enchantments.RIPTIDE, pStack);
    }

    public static boolean hasChanneling(ItemStack pStack)
    {
        return getItemEnchantmentLevel(Enchantments.CHANNELING, pStack) > 0;
    }

    @Nullable
    public static Entry<EquipmentSlot, ItemStack> getRandomItemWith(Enchantment pTargetEnchantment, LivingEntity pEntity)
    {
        return getRandomItemWith(pTargetEnchantment, pEntity, (p_44941_) ->
        {
            return true;
        });
    }

    @Nullable
    public static Entry<EquipmentSlot, ItemStack> getRandomItemWith(Enchantment pTargetEnchantment, LivingEntity pEntity, Predicate<ItemStack> p_44842_)
    {
        Map<EquipmentSlot, ItemStack> map = pTargetEnchantment.getSlotItems(pEntity);

        if (map.isEmpty())
        {
            return null;
        }
        else
        {
            List<Entry<EquipmentSlot, ItemStack>> list = Lists.newArrayList();

            for (Entry<EquipmentSlot, ItemStack> entry : map.entrySet())
            {
                ItemStack itemstack = entry.getValue();

                if (!itemstack.isEmpty() && getItemEnchantmentLevel(pTargetEnchantment, itemstack) > 0 && p_44842_.test(itemstack))
                {
                    list.add(entry);
                }
            }

            return list.isEmpty() ? null : list.get(pEntity.getRandom().nextInt(list.size()));
        }
    }

    public static int getEnchantmentCost(Random pRand, int pEnchantNum, int pPower, ItemStack pStack)
    {
        Item item = pStack.getItem();
        int i = item.getEnchantmentValue();

        if (i <= 0)
        {
            return 0;
        }
        else
        {
            if (pPower > 15)
            {
                pPower = 15;
            }

            int j = pRand.nextInt(8) + 1 + (pPower >> 1) + pRand.nextInt(pPower + 1);

            if (pEnchantNum == 0)
            {
                return Math.max(j / 3, 1);
            }
            else
            {
                return pEnchantNum == 1 ? j * 2 / 3 + 1 : Math.max(j, pPower * 2);
            }
        }
    }

    public static ItemStack enchantItem(Random pRandom, ItemStack pStack, int pLevel, boolean pAllowTreasure)
    {
        List<EnchantmentInstance> list = selectEnchantment(pRandom, pStack, pLevel, pAllowTreasure);
        boolean flag = pStack.is(Items.BOOK);

        if (flag)
        {
            pStack = new ItemStack(Items.ENCHANTED_BOOK);
        }

        for (EnchantmentInstance enchantmentinstance : list)
        {
            if (flag)
            {
                EnchantedBookItem.addEnchantment(pStack, enchantmentinstance);
            }
            else
            {
                pStack.enchant(enchantmentinstance.enchantment, enchantmentinstance.level);
            }
        }

        return pStack;
    }

    public static List<EnchantmentInstance> selectEnchantment(Random pRandom, ItemStack pItemStack, int pLevel, boolean pAllowTreasure)
    {
        List<EnchantmentInstance> list = Lists.newArrayList();
        Item item = pItemStack.getItem();
        int i = item.getEnchantmentValue();

        if (i <= 0)
        {
            return list;
        }
        else
        {
            pLevel = pLevel + 1 + pRandom.nextInt(i / 4 + 1) + pRandom.nextInt(i / 4 + 1);
            float f = (pRandom.nextFloat() + pRandom.nextFloat() - 1.0F) * 0.15F;
            pLevel = Mth.clamp(Math.round((float)pLevel + (float)pLevel * f), 1, Integer.MAX_VALUE);
            List<EnchantmentInstance> list1 = getAvailableEnchantmentResults(pLevel, pItemStack, pAllowTreasure);

            if (!list1.isEmpty())
            {
                WeightedRandom.getRandomItem(pRandom, list1).ifPresent(list::add);

                while (pRandom.nextInt(50) <= pLevel)
                {
                    if (!list.isEmpty())
                    {
                        filterCompatibleEnchantments(list1, Util.lastOf(list));
                    }

                    if (list1.isEmpty())
                    {
                        break;
                    }

                    WeightedRandom.getRandomItem(pRandom, list1).ifPresent(list::add);
                    pLevel /= 2;
                }
            }

            return list;
        }
    }

    public static void filterCompatibleEnchantments(List<EnchantmentInstance> pDataList, EnchantmentInstance pData)
    {
        Iterator<EnchantmentInstance> iterator = pDataList.iterator();

        while (iterator.hasNext())
        {
            if (!pData.enchantment.isCompatibleWith((iterator.next()).enchantment))
            {
                iterator.remove();
            }
        }
    }

    public static boolean isEnchantmentCompatible(Collection<Enchantment> pEnchantments, Enchantment pEnchantment)
    {
        for (Enchantment enchantment : pEnchantments)
        {
            if (!enchantment.isCompatibleWith(pEnchantment))
            {
                return false;
            }
        }

        return true;
    }

    public static List<EnchantmentInstance> getAvailableEnchantmentResults(int pLevel, ItemStack pStack, boolean pAllowTreasure)
    {
        List<EnchantmentInstance> list = Lists.newArrayList();
        Item item = pStack.getItem();
        boolean flag = pStack.is(Items.BOOK);

        for (Enchantment enchantment : Registry.ENCHANTMENT)
        {
            if ((!enchantment.isTreasureOnly() || pAllowTreasure) && enchantment.isDiscoverable() && (enchantment.category.canEnchant(item) || flag))
            {
                for (int i = enchantment.getMaxLevel(); i > enchantment.getMinLevel() - 1; --i)
                {
                    if (pLevel >= enchantment.getMinCost(i) && pLevel <= enchantment.getMaxCost(i))
                    {
                        list.add(new EnchantmentInstance(enchantment, i));
                        break;
                    }
                }
            }
        }

        return list;
    }

    @FunctionalInterface
    interface EnchantmentVisitor
    {
        void accept(Enchantment p_44945_, int p_44946_);
    }
}
