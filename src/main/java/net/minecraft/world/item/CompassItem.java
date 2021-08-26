package net.minecraft.world.item;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CompassItem extends Item implements Vanishable
{
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String TAG_LODESTONE_POS = "LodestonePos";
    public static final String TAG_LODESTONE_DIMENSION = "LodestoneDimension";
    public static final String TAG_LODESTONE_TRACKED = "LodestoneTracked";

    public CompassItem(Item.Properties p_40718_)
    {
        super(p_40718_);
    }

    public static boolean isLodestoneCompass(ItemStack pStack)
    {
        CompoundTag compoundtag = pStack.getTag();
        return compoundtag != null && (compoundtag.contains("LodestoneDimension") || compoundtag.contains("LodestonePos"));
    }

    public boolean isFoil(ItemStack pStack)
    {
        return isLodestoneCompass(pStack) || super.isFoil(pStack);
    }

    public static Optional<ResourceKey<Level>> getLodestoneDimension(CompoundTag pNbt)
    {
        return Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, pNbt.get("LodestoneDimension")).result();
    }

    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pItemSlot, boolean pIsSelected)
    {
        if (!pLevel.isClientSide)
        {
            if (isLodestoneCompass(pStack))
            {
                CompoundTag compoundtag = pStack.getOrCreateTag();

                if (compoundtag.contains("LodestoneTracked") && !compoundtag.getBoolean("LodestoneTracked"))
                {
                    return;
                }

                Optional<ResourceKey<Level>> optional = getLodestoneDimension(compoundtag);

                if (optional.isPresent() && optional.get() == pLevel.dimension() && compoundtag.contains("LodestonePos"))
                {
                    BlockPos blockpos = NbtUtils.readBlockPos(compoundtag.getCompound("LodestonePos"));

                    if (!pLevel.isInWorldBounds(blockpos) || !((ServerLevel)pLevel).getPoiManager().existsAtPosition(PoiType.LODESTONE, blockpos))
                    {
                        compoundtag.remove("LodestonePos");
                    }
                }
            }
        }
    }

    public InteractionResult useOn(UseOnContext pContext)
    {
        BlockPos blockpos = pContext.getClickedPos();
        Level level = pContext.getLevel();

        if (!level.getBlockState(blockpos).is(Blocks.LODESTONE))
        {
            return super.useOn(pContext);
        }
        else
        {
            level.playSound((Player)null, blockpos, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
            Player player = pContext.getPlayer();
            ItemStack itemstack = pContext.getItemInHand();
            boolean flag = !player.getAbilities().instabuild && itemstack.getCount() == 1;

            if (flag)
            {
                this.addLodestoneTags(level.dimension(), blockpos, itemstack.getOrCreateTag());
            }
            else
            {
                ItemStack itemstack1 = new ItemStack(Items.COMPASS, 1);
                CompoundTag compoundtag = itemstack.hasTag() ? itemstack.getTag().copy() : new CompoundTag();
                itemstack1.setTag(compoundtag);

                if (!player.getAbilities().instabuild)
                {
                    itemstack.shrink(1);
                }

                this.addLodestoneTags(level.dimension(), blockpos, compoundtag);

                if (!player.getInventory().add(itemstack1))
                {
                    player.drop(itemstack1, false);
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    private void addLodestoneTags(ResourceKey<Level> pLodestoneDimension, BlockPos pLodestonePos, CompoundTag pNbt)
    {
        pNbt.put("LodestonePos", NbtUtils.writeBlockPos(pLodestonePos));
        Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, pLodestoneDimension).resultOrPartial(LOGGER::error).ifPresent((p_40731_) ->
        {
            pNbt.put("LodestoneDimension", p_40731_);
        });
        pNbt.putBoolean("LodestoneTracked", true);
    }

    public String getDescriptionId(ItemStack pStack)
    {
        return isLodestoneCompass(pStack) ? "item.minecraft.lodestone_compass" : super.getDescriptionId(pStack);
    }
}
