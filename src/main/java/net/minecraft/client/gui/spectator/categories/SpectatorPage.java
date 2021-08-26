package net.minecraft.client.gui.spectator.categories;

import com.google.common.base.MoreObjects;
import java.util.List;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;

public class SpectatorPage
{
    public static final int NO_SELECTION = -1;
    private final List<SpectatorMenuItem> items;
    private final int selection;

    public SpectatorPage(List<SpectatorMenuItem> p_170331_, int p_170332_)
    {
        this.items = p_170331_;
        this.selection = p_170332_;
    }

    public SpectatorMenuItem getItem(int pIndex)
    {
        return pIndex >= 0 && pIndex < this.items.size() ? MoreObjects.firstNonNull(this.items.get(pIndex), SpectatorMenu.EMPTY_SLOT) : SpectatorMenu.EMPTY_SLOT;
    }

    public int getSelectedSlot()
    {
        return this.selection;
    }
}
