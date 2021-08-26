package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RecordItem extends Item
{
    private static final Map<SoundEvent, RecordItem> BY_NAME = Maps.newHashMap();
    private final int analogOutput;
    private final SoundEvent sound;

    protected RecordItem(int p_43037_, SoundEvent p_43038_, Item.Properties p_43039_)
    {
        super(p_43039_);
        this.analogOutput = p_43037_;
        this.sound = p_43038_;
        BY_NAME.put(this.sound, this);
    }

    public InteractionResult useOn(UseOnContext pContext)
    {
        Level level = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);

        if (blockstate.is(Blocks.JUKEBOX) && !blockstate.getValue(JukeboxBlock.HAS_RECORD))
        {
            ItemStack itemstack = pContext.getItemInHand();

            if (!level.isClientSide)
            {
                ((JukeboxBlock)Blocks.JUKEBOX).setRecord(level, blockpos, blockstate, itemstack);
                level.levelEvent((Player)null, 1010, blockpos, Item.getId(this));
                itemstack.shrink(1);
                Player player = pContext.getPlayer();

                if (player != null)
                {
                    player.awardStat(Stats.PLAY_RECORD);
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        else
        {
            return InteractionResult.PASS;
        }
    }

    public int getAnalogOutput()
    {
        return this.analogOutput;
    }

    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag)
    {
        pTooltip.add(this.getDisplayName().withStyle(ChatFormatting.GRAY));
    }

    public MutableComponent getDisplayName()
    {
        return new TranslatableComponent(this.getDescriptionId() + ".desc");
    }

    @Nullable
    public static RecordItem getBySound(SoundEvent pSound)
    {
        return BY_NAME.get(pSound);
    }

    public SoundEvent getSound()
    {
        return this.sound;
    }
}
