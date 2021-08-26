package net.minecraft.client.gui.chat;

import java.util.UUID;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;

public interface ChatListener
{
    void handle(ChatType pChatType, Component pMessage, UUID pSender);
}
