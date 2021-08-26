package net.minecraft.world.item;

import com.google.common.collect.Lists;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CrossbowItem extends ProjectileWeaponItem implements Vanishable
{
    private static final String TAG_CHARGED = "Charged";
    private static final String TAG_CHARGED_PROJECTILES = "ChargedProjectiles";
    private static final int MAX_CHARGE_DURATION = 25;
    public static final int DEFAULT_RANGE = 8;
    private boolean startSoundPlayed = false;
    private boolean midLoadSoundPlayed = false;
    private static final float START_SOUND_PERCENT = 0.2F;
    private static final float MID_SOUND_PERCENT = 0.5F;
    private static final float ARROW_POWER = 3.15F;
    private static final float FIREWORK_POWER = 1.6F;

    public CrossbowItem(Item.Properties p_40850_)
    {
        super(p_40850_);
    }

    public Predicate<ItemStack> getSupportedHeldProjectiles()
    {
        return ARROW_OR_FIREWORK;
    }

    public Predicate<ItemStack> getAllSupportedProjectiles()
    {
        return ARROW_ONLY;
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand)
    {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);

        if (isCharged(itemstack))
        {
            performShooting(pLevel, pPlayer, pHand, itemstack, getShootingPower(itemstack), 1.0F);
            setCharged(itemstack, false);
            return InteractionResultHolder.consume(itemstack);
        }
        else if (!pPlayer.getProjectile(itemstack).isEmpty())
        {
            if (!isCharged(itemstack))
            {
                this.startSoundPlayed = false;
                this.midLoadSoundPlayed = false;
                pPlayer.startUsingItem(pHand);
            }

            return InteractionResultHolder.consume(itemstack);
        }
        else
        {
            return InteractionResultHolder.fail(itemstack);
        }
    }

    private static float getShootingPower(ItemStack p_40946_)
    {
        return containsChargedProjectile(p_40946_, Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
    }

    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft)
    {
        int i = this.getUseDuration(pStack) - pTimeLeft;
        float f = getPowerForTime(i, pStack);

        if (f >= 1.0F && !isCharged(pStack) && tryLoadProjectiles(pEntityLiving, pStack))
        {
            setCharged(pStack, true);
            SoundSource soundsource = pEntityLiving instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
            pLevel.playSound((Player)null, pEntityLiving.getX(), pEntityLiving.getY(), pEntityLiving.getZ(), SoundEvents.CROSSBOW_LOADING_END, soundsource, 1.0F, 1.0F / (pLevel.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F);
        }
    }

    private static boolean tryLoadProjectiles(LivingEntity pEntity, ItemStack pStack)
    {
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, pStack);
        int j = i == 0 ? 1 : 3;
        boolean flag = pEntity instanceof Player && ((Player)pEntity).getAbilities().instabuild;
        ItemStack itemstack = pEntity.getProjectile(pStack);
        ItemStack itemstack1 = itemstack.copy();

        for (int k = 0; k < j; ++k)
        {
            if (k > 0)
            {
                itemstack = itemstack1.copy();
            }

            if (itemstack.isEmpty() && flag)
            {
                itemstack = new ItemStack(Items.ARROW);
                itemstack1 = itemstack.copy();
            }

            if (!loadProjectile(pEntity, pStack, itemstack, k > 0, flag))
            {
                return false;
            }
        }

        return true;
    }

    private static boolean loadProjectile(LivingEntity p_40863_, ItemStack p_40864_, ItemStack p_40865_, boolean p_40866_, boolean p_40867_)
    {
        if (p_40865_.isEmpty())
        {
            return false;
        }
        else
        {
            boolean flag = p_40867_ && p_40865_.getItem() instanceof ArrowItem;
            ItemStack itemstack;

            if (!flag && !p_40867_ && !p_40866_)
            {
                itemstack = p_40865_.split(1);

                if (p_40865_.isEmpty() && p_40863_ instanceof Player)
                {
                    ((Player)p_40863_).getInventory().removeItem(p_40865_);
                }
            }
            else
            {
                itemstack = p_40865_.copy();
            }

            addChargedProjectile(p_40864_, itemstack);
            return true;
        }
    }

    public static boolean isCharged(ItemStack pStack)
    {
        CompoundTag compoundtag = pStack.getTag();
        return compoundtag != null && compoundtag.getBoolean("Charged");
    }

    public static void setCharged(ItemStack pStack, boolean pCharged)
    {
        CompoundTag compoundtag = pStack.getOrCreateTag();
        compoundtag.putBoolean("Charged", pCharged);
    }

    private static void addChargedProjectile(ItemStack pCrossbow, ItemStack pProjectile)
    {
        CompoundTag compoundtag = pCrossbow.getOrCreateTag();
        ListTag listtag;

        if (compoundtag.contains("ChargedProjectiles", 9))
        {
            listtag = compoundtag.getList("ChargedProjectiles", 10);
        }
        else
        {
            listtag = new ListTag();
        }

        CompoundTag compoundtag1 = new CompoundTag();
        pProjectile.save(compoundtag1);
        listtag.add(compoundtag1);
        compoundtag.put("ChargedProjectiles", listtag);
    }

    private static List<ItemStack> getChargedProjectiles(ItemStack pStack)
    {
        List<ItemStack> list = Lists.newArrayList();
        CompoundTag compoundtag = pStack.getTag();

        if (compoundtag != null && compoundtag.contains("ChargedProjectiles", 9))
        {
            ListTag listtag = compoundtag.getList("ChargedProjectiles", 10);

            if (listtag != null)
            {
                for (int i = 0; i < listtag.size(); ++i)
                {
                    CompoundTag compoundtag1 = listtag.getCompound(i);
                    list.add(ItemStack.of(compoundtag1));
                }
            }
        }

        return list;
    }

    private static void clearChargedProjectiles(ItemStack pStack)
    {
        CompoundTag compoundtag = pStack.getTag();

        if (compoundtag != null)
        {
            ListTag listtag = compoundtag.getList("ChargedProjectiles", 9);
            listtag.clear();
            compoundtag.put("ChargedProjectiles", listtag);
        }
    }

    public static boolean containsChargedProjectile(ItemStack pStack, Item pAmmoItem)
    {
        return getChargedProjectiles(pStack).stream().anyMatch((p_40870_) ->
        {
            return p_40870_.is(pAmmoItem);
        });
    }

    private static void shootProjectile(Level pLevel, LivingEntity pShooter, InteractionHand pHand, ItemStack pCrossbow, ItemStack pProjectile, float pSoundPitch, boolean pIsCreativeMode, float pVelocity, float pInaccuracy, float pProjectileAngle)
    {
        if (!pLevel.isClientSide)
        {
            boolean flag = pProjectile.is(Items.FIREWORK_ROCKET);
            Projectile projectile;

            if (flag)
            {
                projectile = new FireworkRocketEntity(pLevel, pProjectile, pShooter, pShooter.getX(), pShooter.getEyeY() - (double)0.15F, pShooter.getZ(), true);
            }
            else
            {
                projectile = getArrow(pLevel, pShooter, pCrossbow, pProjectile);

                if (pIsCreativeMode || pProjectileAngle != 0.0F)
                {
                    ((AbstractArrow)projectile).pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
            }

            if (pShooter instanceof CrossbowAttackMob)
            {
                CrossbowAttackMob crossbowattackmob = (CrossbowAttackMob)pShooter;
                crossbowattackmob.shootCrossbowProjectile(crossbowattackmob.getTarget(), pCrossbow, projectile, pProjectileAngle);
            }
            else
            {
                Vec3 vec31 = pShooter.getUpVector(1.0F);
                Quaternion quaternion = new Quaternion(new Vector3f(vec31), pProjectileAngle, true);
                Vec3 vec3 = pShooter.getViewVector(1.0F);
                Vector3f vector3f = new Vector3f(vec3);
                vector3f.transform(quaternion);
                projectile.shoot((double)vector3f.x(), (double)vector3f.y(), (double)vector3f.z(), pVelocity, pInaccuracy);
            }

            pCrossbow.hurtAndBreak(flag ? 3 : 1, pShooter, (p_40858_) ->
            {
                p_40858_.broadcastBreakEvent(pHand);
            });
            pLevel.addFreshEntity(projectile);
            pLevel.playSound((Player)null, pShooter.getX(), pShooter.getY(), pShooter.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, pSoundPitch);
        }
    }

    private static AbstractArrow getArrow(Level pLevel, LivingEntity pShooter, ItemStack pCrossbow, ItemStack pAmmo)
    {
        ArrowItem arrowitem = (ArrowItem)(pAmmo.getItem() instanceof ArrowItem ? pAmmo.getItem() : Items.ARROW);
        AbstractArrow abstractarrow = arrowitem.createArrow(pLevel, pAmmo, pShooter);

        if (pShooter instanceof Player)
        {
            abstractarrow.setCritArrow(true);
        }

        abstractarrow.setSoundEvent(SoundEvents.CROSSBOW_HIT);
        abstractarrow.setShotFromCrossbow(true);
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, pCrossbow);

        if (i > 0)
        {
            abstractarrow.setPierceLevel((byte)i);
        }

        return abstractarrow;
    }

    public static void performShooting(Level pLevel, LivingEntity pShooter, InteractionHand pHand, ItemStack pStack, float pVelocity, float pInaccuracy)
    {
        List<ItemStack> list = getChargedProjectiles(pStack);
        float[] afloat = getShotPitches(pShooter.getRandom());

        for (int i = 0; i < list.size(); ++i)
        {
            ItemStack itemstack = list.get(i);
            boolean flag = pShooter instanceof Player && ((Player)pShooter).getAbilities().instabuild;

            if (!itemstack.isEmpty())
            {
                if (i == 0)
                {
                    shootProjectile(pLevel, pShooter, pHand, pStack, itemstack, afloat[i], flag, pVelocity, pInaccuracy, 0.0F);
                }
                else if (i == 1)
                {
                    shootProjectile(pLevel, pShooter, pHand, pStack, itemstack, afloat[i], flag, pVelocity, pInaccuracy, -10.0F);
                }
                else if (i == 2)
                {
                    shootProjectile(pLevel, pShooter, pHand, pStack, itemstack, afloat[i], flag, pVelocity, pInaccuracy, 10.0F);
                }
            }
        }

        onCrossbowShot(pLevel, pShooter, pStack);
    }

    private static float[] getShotPitches(Random pRand)
    {
        boolean flag = pRand.nextBoolean();
        return new float[] {1.0F, getRandomShotPitch(flag, pRand), getRandomShotPitch(!flag, pRand)};
    }

    private static float getRandomShotPitch(boolean p_150798_, Random p_150799_)
    {
        float f = p_150798_ ? 0.63F : 0.43F;
        return 1.0F / (p_150799_.nextFloat() * 0.5F + 1.8F) + f;
    }

    private static void onCrossbowShot(Level pLevel, LivingEntity pShooter, ItemStack pStack)
    {
        if (pShooter instanceof ServerPlayer)
        {
            ServerPlayer serverplayer = (ServerPlayer)pShooter;

            if (!pLevel.isClientSide)
            {
                CriteriaTriggers.SHOT_CROSSBOW.trigger(serverplayer, pStack);
            }

            serverplayer.awardStat(Stats.ITEM_USED.get(pStack.getItem()));
        }

        clearChargedProjectiles(pStack);
    }

    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pCount)
    {
        if (!pLevel.isClientSide)
        {
            int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, pStack);
            SoundEvent soundevent = this.getStartSound(i);
            SoundEvent soundevent1 = i == 0 ? SoundEvents.CROSSBOW_LOADING_MIDDLE : null;
            float f = (float)(pStack.getUseDuration() - pCount) / (float)getChargeDuration(pStack);

            if (f < 0.2F)
            {
                this.startSoundPlayed = false;
                this.midLoadSoundPlayed = false;
            }

            if (f >= 0.2F && !this.startSoundPlayed)
            {
                this.startSoundPlayed = true;
                pLevel.playSound((Player)null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), soundevent, SoundSource.PLAYERS, 0.5F, 1.0F);
            }

            if (f >= 0.5F && soundevent1 != null && !this.midLoadSoundPlayed)
            {
                this.midLoadSoundPlayed = true;
                pLevel.playSound((Player)null, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), soundevent1, SoundSource.PLAYERS, 0.5F, 1.0F);
            }
        }
    }

    public int getUseDuration(ItemStack pStack)
    {
        return getChargeDuration(pStack) + 3;
    }

    public static int getChargeDuration(ItemStack pStack)
    {
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, pStack);
        return i == 0 ? 25 : 25 - 5 * i;
    }

    public UseAnim getUseAnimation(ItemStack pStack)
    {
        return UseAnim.CROSSBOW;
    }

    private SoundEvent getStartSound(int pEnchantmentLevel)
    {
        switch (pEnchantmentLevel)
        {
            case 1:
                return SoundEvents.CROSSBOW_QUICK_CHARGE_1;

            case 2:
                return SoundEvents.CROSSBOW_QUICK_CHARGE_2;

            case 3:
                return SoundEvents.CROSSBOW_QUICK_CHARGE_3;

            default:
                return SoundEvents.CROSSBOW_LOADING_START;
        }
    }

    private static float getPowerForTime(int pUseTime, ItemStack pStack)
    {
        float f = (float)pUseTime / (float)getChargeDuration(pStack);

        if (f > 1.0F)
        {
            f = 1.0F;
        }

        return f;
    }

    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag)
    {
        List<ItemStack> list = getChargedProjectiles(pStack);

        if (isCharged(pStack) && !list.isEmpty())
        {
            ItemStack itemstack = list.get(0);
            pTooltip.add((new TranslatableComponent("item.minecraft.crossbow.projectile")).append(" ").append(itemstack.getDisplayName()));

            if (pFlag.isAdvanced() && itemstack.is(Items.FIREWORK_ROCKET))
            {
                List<Component> list1 = Lists.newArrayList();
                Items.FIREWORK_ROCKET.appendHoverText(itemstack, pLevel, list1, pFlag);

                if (!list1.isEmpty())
                {
                    for (int i = 0; i < list1.size(); ++i)
                    {
                        list1.set(i, (new TextComponent("  ")).append(list1.get(i)).withStyle(ChatFormatting.GRAY));
                    }

                    pTooltip.addAll(list1);
                }
            }
        }
    }

    public boolean useOnRelease(ItemStack p_150801_)
    {
        return p_150801_.is(this);
    }

    public int getDefaultProjectileRange()
    {
        return 8;
    }
}
