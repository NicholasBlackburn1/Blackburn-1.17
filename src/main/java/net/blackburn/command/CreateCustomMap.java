package net.blackburn.command;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;

public class CreateCustomMap implements CommandBase{

    @Override
    public void runCommand(List<String> command, Minecraft mc) {
       
        TextComponent startup;

        if(!command.isEmpty()){

            if (command.contains(".custommap")){
                
            }
        }
    }

    @Override
    public String getCommandName() {
        
        return "blackburn.command.createmap";
    }
    
}
