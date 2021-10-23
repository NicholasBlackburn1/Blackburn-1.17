package net.blackburn.command;

import java.util.List;

import net.minecraft.client.Minecraft;

public interface CommandBase {


    public void runCommand(List<String> command,Minecraft mc);

    public String getCommandName();
}
