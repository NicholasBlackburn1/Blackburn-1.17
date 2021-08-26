package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Item implements ItemLike
{
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Map<Block, Item> BY_BLOCK = Maps.newHashMap();
    protected static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    protected static final UUID BASE_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    public static final int MAX_STACK_SIZE = 64;
    public static final int EAT_DURATION = 32;
    public static final int MAX_BAR_WIDTH = 13;
    protected final CreativeModeTab category;
    private final Rarity rarity;
    private final int maxStackSize;
    private final int maxDamage;
    private final boolean isFireResistant;
    private final Item craftingRemainingItem;
    @Nullable
    private String descriptionId;
    @Nullable
    private final FoodProperties foodProperties;

    public static int getId(Item pItem)
    {
        return pItem == null ? 0 : Registry.ITEM.getId(pItem);
    }

    public static Item byId(int pId)
    {
        return Registry.ITEM.byId(pId);
    }

    @Deprecated
    public static Item byBlock(Block pBlock)
    {
        return BY_BLOCK.getOrDefault(pBlock, Items.AIR);
    }

    public Item(Item.Properties p_41383_)
    {
        this.category = p_41383_.category;
        this.rarity = p_41383_.rarity;
        this.craftingRemainingItem = p_41383_.craftingRemainingItem;
        this.maxDamage = p_41383_.maxDamage;
        this.maxStackSize = p_41383_.maxStackSize;
        this.foodProperties = p_41383_.foodProperties;
        this.isFireResistant = p_41383_.isFireResistant;

        if (SharedConstants.IS_RUNNING_IN_IDE)
        {
            String s = this.getClass().getSimpleName();

            if (!s.endsWith("Item"))
            {
                LOGGER.error("Item classes should end with Item and {} doesn't.", (Object)s);
            }
        }
    }

    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pCount)
    {
    }

    public void onDestroyed(ItemEntity p_150887_)
    {
    }

    public void verifyTagAfterLoad(CompoundTag p_150898_)
    {
    }

    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer)
    {
        return true;
    }

    public Item asItem()
    {
        return this;
    }

    public InteractionResult useOn(UseOnContext pContext)
    {
        return InteractionResult.PASS;
    }

    public float getDestroySpeed(ItemStack pStack, BlockState pState)
    {
        return 1.0F;
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand)
    {
        if (this.isEdible())
        {
            ItemStack itemstack = pPlayer.getItemInHand(pHand);

            if (pPlayer.canEat(this.getFoodProperties().canAlwaysEat()))
            {
                pPlayer.startUsingItem(pHand);
                return InteractionResultHolder.consume(itemstack);
            }
            else
            {
                return InteractionResultHolder.fail(itemstack);
            }
        }
        else
        {
            return InteractionResultHolder.pass(pPlayer.getItemInHand(pHand));
        }
    }

    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving)
    {
        return this.isEdible() ? pEntityLiving.eat(pLevel, pStack) : pStack;
    }

    public final int getMaxStackSize()
    {
        return this.maxStackSize;
    }

    public final int getMaxDamage()
    {
        return this.maxDamage;
    }

    public boolean canBeDepleted()
    {
        return this.maxDamage > 0;
    }

    public boolean isBarVisible(ItemStack p_150899_)
    {
        return p_150899_.isDamaged();
    }

    public int getBarWidth(ItemStack p_150900_)
    {
        return Math.round(13.0F - (float)p_150900_.getDamageValue() * 13.0F / (float)this.maxDamage);
    }

    public int getBarColor(ItemStack p_150901_)
    {
        float f = Math.max(0.0F, ((float)this.maxDamage - (float)p_150901_.getDamageValue()) / (float)this.maxDamage);
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    public boolean overrideStackedOnOther(ItemStack p_150888_, Slot p_150889_, ClickAction p_150890_, Player p_150891_)
    {
        return false;
    }

    public boolean overrideOtherStackedOnMe(ItemStack p_150892_, ItemStack p_150893_, Slot p_150894_, ClickAction p_150895_, Player p_150896_, SlotAccess p_150897_)
    {
        return false;
    }

    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker)
    {
        return false;
    }

    public boolean mineBlock(ItemStack pStack, Level pLevel, BlockState pState, BlockPos pPos, LivingEntity pEntityLiving)
    {
        return false;
    }

    public boolean isCorrectToolForDrops(BlockState pBlock)
    {
        return false;
    }

    public InteractionResult interactLivingEntity(ItemStack pStack, Player pPlayer, LivingEntity pTarget, InteractionHand pHand)
    {
        return InteractionResult.PASS;
    }

    public Component getDescription()
    {
        return new TranslatableComponent(this.getDescriptionId());
    }

    public String toString()
    {
        return Registry.ITEM.getKey(this).getPath();
    }

    protected String getOrCreateDescriptionId()
    {
        if (this.descriptionId == null)
        {
            this.descriptionId = Util.makeDescriptionId("item", Registry.ITEM.getKey(this));
        }

        return this.descriptionId;
    }

    public String getDescriptionId()
    {
        return this.getOrCreateDescriptionId();
    }

    public String getDescriptionId(ItemStack p_41455_)
    {
        return this.getDescriptionId();
    }

    public boolean shouldOverrideMultiplayerNbt()
    {
        return true;
    }

    @Nullable
    public final Item getCraftingRemainingItem()
    {
        return this.craftingRemainingItem;
    }

    public boolean hasCraftingRemainingItem()
    {
        return this.craftingRemainingItem != null;
    }

    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pItemSlot, boolean pIsSelected)
    {
    }

    public void onCraftedBy(ItemStack pStack, Level pLevel, Player pPlayer)
    {
    }

    public boolean isComplex()
    {
        return false;
    }

    public UseAnim getUseAnimation(ItemStack pStack)
    {
        return pStack.getItem().isEdible() ? UseAnim.EAT : UseAnim.NONE;
    }

    public int getUseDuration(ItemStack pStack)
    {
        if (pStack.getItem().isEdible())
        {
            return this.getFoodProperties().isFastFood() ? 16 : 32;
        }
        else
        {
            return 0;
        }
    }

    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft)
    {
    }

    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag)
    {
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack p_150902_)
    {
        return Optional.empty();
    }

    public Component getName(ItemStack pStack)
    {
        return new TranslatableComponent(this.getDescriptionId(pStack));
    }

    public boolean isFoil(ItemStack pStack)
    {
        return pStack.isEnchanted();
    }

    public Rarity getRarity(ItemStack pStack)
    {
        if (!pStack.isEnchanted())
        {
            return this.rarity;
        }
        else
        {
            switch (this.rarity)
            {
                case COMMON:
                case UNCOMMON:
                    return Rarity.RARE;

                case RARE:
                    return Rarity.EPIC;

                case EPIC:
                default:
                    return this.rarity;
            }
        }
    }

    public boolean isEnchantable(ItemStack pStack)
    {
        return this.getMaxStackSize() == 1 && this.canBeDepleted();
    }

    protected static BlockHitResult getPlayerPOVHitResult(Level pLevel, Player pPlayer, ClipContext.Fluid pFluidMode)
    {
        float f = pPlayer.getXRot();
        float f1 = pPlayer.getYRot();
        Vec3 vec3 = pPlayer.getEyePosition();
        float f2 = Mth.cos(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f3 = Mth.sin(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f4 = -Mth.cos(-f * ((float)Math.PI / 180F));
        float f5 = Mth.sin(-f * ((float)Math.PI / 180F));
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double d0 = 5.0D;
        Vec3 vec31 = vec3.add((double)f6 * 5.0D, (double)f5 * 5.0D, (double)f7 * 5.0D);
        return pLevel.clip(new ClipContext(vec3, vec31, ClipContext.Block.OUTLINE, pFluidMode, pPlayer));
    }

    public int getEnchantmentValue()
    {
        return 0;
    }

    public void fillItemCategory(CreativeModeTab pGroup, NonNullList<ItemStack> pItems)
    {
        if (this.allowdedIn(pGroup))
        {
            pItems.add(new ItemStack(this));
        }
    }

    protected boolean allowdedIn(CreativeModeTab pGroup)
    {
        CreativeModeTab creativemodetab = this.getItemCategory();
        return creativemodetab != null && (pGroup == CreativeModeTab.TAB_SEARCH || pGroup == creativemodetab);
    }

    @Nullable
    public final CreativeModeTab getItemCategory()
    {
        return this.category;
    }

    public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair)
    {
        return false;
    }

    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot pEquipmentSlot)
    {
        return ImmutableMultimap.of();
    }

    public boolean useOnRelease(ItemStack pStack)
    {
        return false;
    }

    public ItemStack getDefaultInstance()
    {
        return new ItemStack(this);
    }

    public boolean isEdible()
    {
        return this.foodProperties != null;
    }

    @Nullable
    public FoodProperties getFoodProperties()
    {
        return this.foodProperties;
    }

    public SoundEvent getDrinkingSound()
    {
        return SoundEvents.GENERIC_DRINK;
    }

    public SoundEvent getEatingSound()
    {
        return SoundEvents.GENERIC_EAT;
    }

    public boolean isFireResistant()
    {
        return this.isFireResistant;
    }

    public boolean canBeHurtBy(DamageSource pDamageSource)
    {
        return !this.isFireResistant || !pDamageSource.isFire();
    }

    @Nullable
    public SoundEvent getEquipSound()
    {
        return null;
    }

    public boolean canFitInsideContainerItems()
    {
        return true;
    }

    public static class Properties
    {
        int maxStackSize = 64;
        int maxDamage;
        Item craftingRemainingItem;
        CreativeModeTab category;
        Rarity rarity = Rarity.COMMON;
        FoodProperties foodProperties;
        boolean isFireResistant;

        public Item.Properties food(FoodProperties pFood)
        {
            this.foodProperties = pFood;
            return this;
        }

        public Item.Properties stacksTo(int pMaxStackSize)
        {
            if (this.maxDamage > 0)
            {
                throw new RuntimeException("Unable to have damage AND stack.");
            }
            else
            {
                this.maxStackSize = pMaxStackSize;
                return this;
            }
        }

        public Item.Properties defaultDurability(int pMaxDamage)
        {
            return this.maxDamage == 0 ? this.durability(pMaxDamage) : this;
        }

        public Item.Properties durability(int pMaxDamage)
        {
            this.maxDamage = pMaxDamage;
            this.maxStackSize = 1;
            return this;
        }

        public Item.Properties craftRemainder(Item pContainerItem)
        {
            this.craftingRemainingItem = pContainerItem;
            return this;
        }

        public Item.Properties tab(CreativeModeTab pGroup)
        {
            this.category = pGroup;
            return this;
        }

        public Item.Properties rarity(Rarity pRarity)
        {
            this.rarity = pRarity;
            return this;
        }

        public Item.Properties fireResistant()
        {
            this.isFireResistant = true;
            return this;
        }
    }
}
