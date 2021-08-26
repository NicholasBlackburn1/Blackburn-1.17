package net.minecraft.client.gui.components.events;

public interface GuiEventListener
{
default void mouseMoved(double pMouseX, double p_94759_)
    {
    }

default boolean mouseClicked(double pMouseX, double p_94738_, int pMouseY)
    {
        return false;
    }

default boolean mouseReleased(double pMouseX, double p_94754_, int pMouseY)
    {
        return false;
    }

default boolean mouseDragged(double pMouseX, double p_94741_, int pMouseY, double p_94743_, double pButton)
    {
        return false;
    }

default boolean mouseScrolled(double pMouseX, double p_94735_, double pMouseY)
    {
        return false;
    }

default boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        return false;
    }

default boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers)
    {
        return false;
    }

default boolean charTyped(char pCodePoint, int pModifiers)
    {
        return false;
    }

default boolean changeFocus(boolean pFocus)
    {
        return false;
    }

default boolean isMouseOver(double pMouseX, double p_94749_)
    {
        return false;
    }
}
