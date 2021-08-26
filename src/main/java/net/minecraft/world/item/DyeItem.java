package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;

public class DyeItem extends Item
{
    private static final Map<DyeColor, DyeItem> ITEM_BY_COLOR = Maps.newEnumMap(DyeColor.class);
    private final DyeColor dyeColor;

    public DyeItem(DyeColor p_41080_, Item.Properties p_41081_)
    {
        super(p_41081_);
        this.dyeColor = p_41080_;
        ITEM_BY_COLOR.put(p_41080_, this);
    }

    public InteractionResult interactLivingEntity(ItemStack pStack, Player pPlayer, LivingEntity pTarget, InteractionHand pHand)
    {
        if (pTarget instanceof Sheep)
        {
            Sheep sheep = (Sheep)pTarget;

            if (sheep.isAlive() && !sheep.isSheared() && sheep.getColor() != this.dyeColor)
            {
                sheep.level.playSound(pPlayer, sheep, SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

                if (!pPlayer.level.isClientSide)
                {
                    sheep.setColor(this.dyeColor);
                    pStack.shrink(1);
                }

                return InteractionResult.sidedSuccess(pPlayer.level.isClientSide);
            }
        }

        return InteractionResult.PASS;
    }

    public DyeColor getDyeColor()
    {
        return this.dyeColor;
    }

    public static DyeItem byColor(DyeColor pColor)
    {
        return ITEM_BY_COLOR.get(pColor);
    }
}
