package net.minecraft.world.entity.npc;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

public class ClientSideMerchant implements Merchant
{
    private final Player source;
    private MerchantOffers offers = new MerchantOffers();
    private int xp;

    public ClientSideMerchant(Player p_35344_)
    {
        this.source = p_35344_;
    }

    public Player getTradingPlayer()
    {
        return this.source;
    }

    public void setTradingPlayer(@Nullable Player pPlayer)
    {
    }

    public MerchantOffers getOffers()
    {
        return this.offers;
    }

    public void overrideOffers(MerchantOffers pOffers)
    {
        this.offers = pOffers;
    }

    public void notifyTrade(MerchantOffer pOffer)
    {
        pOffer.increaseUses();
    }

    public void notifyTradeUpdated(ItemStack pStack)
    {
    }

    public Level getLevel()
    {
        return this.source.level;
    }

    public int getVillagerXp()
    {
        return this.xp;
    }

    public void overrideXp(int pXp)
    {
        this.xp = pXp;
    }

    public boolean showProgressBar()
    {
        return true;
    }

    public SoundEvent getNotifyTradeSound()
    {
        return SoundEvents.VILLAGER_YES;
    }
}
