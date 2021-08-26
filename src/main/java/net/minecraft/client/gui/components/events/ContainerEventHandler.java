package net.minecraft.client.gui.components.events;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public interface ContainerEventHandler extends GuiEventListener
{
    List <? extends GuiEventListener > children();

default Optional<GuiEventListener> getChildAt(double pMouseX, double p_94731_)
    {
        for (GuiEventListener guieventlistener : this.children())
        {
            if (guieventlistener.isMouseOver(pMouseX, p_94731_))
            {
                return Optional.of(guieventlistener);
            }
        }

        return Optional.empty();
    }

default boolean mouseClicked(double pMouseX, double p_94696_, int pMouseY)
    {
        for (GuiEventListener guieventlistener : this.children())
        {
            if (guieventlistener.mouseClicked(pMouseX, p_94696_, pMouseY))
            {
                this.setFocused(guieventlistener);

                if (pMouseY == 0)
                {
                    this.setDragging(true);
                }

                return true;
            }
        }

        return false;
    }

default boolean mouseReleased(double pMouseX, double p_94723_, int pMouseY)
    {
        this.setDragging(false);
        return this.getChildAt(pMouseX, p_94723_).filter((p_94708_) ->
        {
            return p_94708_.mouseReleased(pMouseX, p_94723_, pMouseY);
        }).isPresent();
    }

default boolean mouseDragged(double pMouseX, double p_94700_, int pMouseY, double p_94702_, double pButton)
    {
        return this.getFocused() != null && this.isDragging() && pMouseY == 0 ? this.getFocused().mouseDragged(pMouseX, p_94700_, pMouseY, p_94702_, pButton) : false;
    }

    boolean isDragging();

    void setDragging(boolean pDragging);

default boolean mouseScrolled(double pMouseX, double p_94687_, double pMouseY)
    {
        return this.getChildAt(pMouseX, p_94687_).filter((p_94693_) ->
        {
            return p_94693_.mouseScrolled(pMouseX, p_94687_, pMouseY);
        }).isPresent();
    }

default boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        return this.getFocused() != null && this.getFocused().keyPressed(pKeyCode, pScanCode, pModifiers);
    }

default boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers)
    {
        return this.getFocused() != null && this.getFocused().keyReleased(pKeyCode, pScanCode, pModifiers);
    }

default boolean charTyped(char pCodePoint, int pModifiers)
    {
        return this.getFocused() != null && this.getFocused().charTyped(pCodePoint, pModifiers);
    }

    @Nullable
    GuiEventListener getFocused();

    void setFocused(@Nullable GuiEventListener pListener);

default void setInitialFocus(@Nullable GuiEventListener pEventListener)
    {
        this.setFocused(pEventListener);
        pEventListener.changeFocus(true);
    }

default void magicalSpecialHackyFocus(@Nullable GuiEventListener pEventListener)
    {
        this.setFocused(pEventListener);
    }

default boolean changeFocus(boolean pFocus)
    {
        GuiEventListener guieventlistener = this.getFocused();
        boolean flag = guieventlistener != null;

        if (flag && guieventlistener.changeFocus(pFocus))
        {
            return true;
        }
        else
        {
            List <? extends GuiEventListener > list = this.children();
            int j = list.indexOf(guieventlistener);
            int i;

            if (flag && j >= 0)
            {
                i = j + (pFocus ? 1 : 0);
            }
            else if (pFocus)
            {
                i = 0;
            }
            else
            {
                i = list.size();
            }

            ListIterator <? extends GuiEventListener > listiterator = list.listIterator(i);
            BooleanSupplier booleansupplier = pFocus ? listiterator::hasNext : listiterator::hasPrevious;
            Supplier <? extends GuiEventListener > supplier = pFocus ? listiterator::next : listiterator::previous;

            while (booleansupplier.getAsBoolean())
            {
                GuiEventListener guieventlistener1 = supplier.get();

                if (guieventlistener1.changeFocus(pFocus))
                {
                    this.setFocused(guieventlistener1);
                    return true;
                }
            }

            this.setFocused((GuiEventListener)null);
            return false;
        }
    }
}
