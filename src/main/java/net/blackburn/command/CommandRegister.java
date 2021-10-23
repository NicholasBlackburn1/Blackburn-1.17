/**
 * This class is for registering  all custom commands in mc
 */
package net.blackburn.command;

import java.io.Console;

import net.blackburn.Const;
import net.blackburn.util.ChatUtils;
import net.minecraft.client.Minecraft;
import net.blackburn.command.ToggleLightInfo;

public class CommandRegister {
    ChatUtils chatUtils = new ChatUtils();
    
    // This is were u add commands
    GetVersionCommand getVersionCommand = new GetVersionCommand();
    HelpCommand helpCommand = new HelpCommand();
    TestDiscordrpc testDiscordrpc = new TestDiscordrpc();
    TogglePlayerPosInfo playerPosInfo = new TogglePlayerPosInfo();
    ToggleLightInfo lightinfo = new ToggleLightInfo();

    
    // inits commands in mc
    public void initCommands(){
        Const.commandList.add(getVersionCommand.getCommandName());
        Const.commandList.add(helpCommand.getCommandName());
        Const.commandList.add(testDiscordrpc.getCommandName());
        Const.commandList.add(playerPosInfo.getCommandName());
        Const.commandList.add(lightinfo.getCommandName());

    }




    public void RegisterCommands(Minecraft mc){
        playerPosInfo.runCommand(mc.gui.getChat().getRecentChat(), mc);
        getVersionCommand.runCommand(mc.gui.getChat().getRecentChat(), mc);
        helpCommand.runCommand(mc.gui.getChat().getRecentChat(), mc);
        testDiscordrpc.runCommand(mc.gui.getChat().getRecentChat(), mc);
        lightinfo.runCommand(mc.gui.getChat().getRecentChat(),mc);
        
        
    }
}
