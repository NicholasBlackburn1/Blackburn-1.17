package net.minecraft.world.item.trading;

import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface Merchant
{
    void setTradingPlayer(@Nullable Player pPlayer);

    @Nullable
    Player getTradingPlayer();

    MerchantOffers getOffers();

    void overrideOffers(MerchantOffers pOffers);

    void notifyTrade(MerchantOffer pOffer);

    void notifyTradeUpdated(ItemStack pStack);

    Level getLevel();

    int getVillagerXp();

    void overrideXp(int pXp);

    boolean showProgressBar();

    SoundEvent getNotifyTradeSound();

default boolean canRestock()
    {
        return false;
    }

default void openTradingScreen(Player pPlayer, Component pDisplayName, int pLevel)
    {
        OptionalInt optionalint = pPlayer.openMenu(new SimpleMenuProvider((p_45298_, p_45299_, p_45300_) ->
        {
            return new MerchantMenu(p_45298_, p_45299_, this);
        }, pDisplayName));

        if (optionalint.isPresent())
        {
            MerchantOffers merchantoffers = this.getOffers();

            if (!merchantoffers.isEmpty())
            {
                pPlayer.sendMerchantOffers(optionalint.getAsInt(), merchantoffers, pLevel, this.getVillagerXp(), this.showProgressBar(), this.canRestock());
            }
        }
    }
}
