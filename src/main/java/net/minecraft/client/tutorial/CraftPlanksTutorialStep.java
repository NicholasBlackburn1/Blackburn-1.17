package net.minecraft.client.tutorial;

import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CraftPlanksTutorialStep implements TutorialStepInstance
{
    private static final int HINT_DELAY = 1200;
    private static final Component CRAFT_TITLE = new TranslatableComponent("tutorial.craft_planks.title");
    private static final Component CRAFT_DESCRIPTION = new TranslatableComponent("tutorial.craft_planks.description");
    private final Tutorial tutorial;
    private TutorialToast toast;
    private int timeWaiting;

    public CraftPlanksTutorialStep(Tutorial p_120467_)
    {
        this.tutorial = p_120467_;
    }

    public void tick()
    {
        ++this.timeWaiting;

        if (!this.tutorial.isSurvival())
        {
            this.tutorial.setStep(TutorialSteps.NONE);
        }
        else
        {
            if (this.timeWaiting == 1)
            {
                LocalPlayer localplayer = this.tutorial.getMinecraft().player;

                if (localplayer != null)
                {
                    if (localplayer.getInventory().contains(ItemTags.PLANKS))
                    {
                        this.tutorial.setStep(TutorialSteps.NONE);
                        return;
                    }

                    if (hasCraftedPlanksPreviously(localplayer, ItemTags.PLANKS))
                    {
                        this.tutorial.setStep(TutorialSteps.NONE);
                        return;
                    }
                }
            }

            if (this.timeWaiting >= 1200 && this.toast == null)
            {
                this.toast = new TutorialToast(TutorialToast.Icons.WOODEN_PLANKS, CRAFT_TITLE, CRAFT_DESCRIPTION, false);
                this.tutorial.getMinecraft().getToasts().addToast(this.toast);
            }
        }
    }

    public void clear()
    {
        if (this.toast != null)
        {
            this.toast.hide();
            this.toast = null;
        }
    }

    public void onGetItem(ItemStack pStack)
    {
        if (pStack.is(ItemTags.PLANKS))
        {
            this.tutorial.setStep(TutorialSteps.NONE);
        }
    }

    public static boolean hasCraftedPlanksPreviously(LocalPlayer pPlayer, Tag<Item> pItems)
    {
        for (Item item : pItems.getValues())
        {
            if (pPlayer.getStats().getValue(Stats.ITEM_CRAFTED.get(item)) > 0)
            {
                return true;
            }
        }

        return false;
    }
}
