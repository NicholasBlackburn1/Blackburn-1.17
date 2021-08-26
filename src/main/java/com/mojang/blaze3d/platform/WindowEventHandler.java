package com.mojang.blaze3d.platform;

public interface WindowEventHandler
{
    void setWindowActive(boolean pFocused);

    void resizeDisplay();

    void cursorEntered();
}
