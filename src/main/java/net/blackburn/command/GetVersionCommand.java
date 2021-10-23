
/**
 * Should return version of client 
 */
package net.blackburn.command;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

import java.util.List;

import net.blackburn.Const;
import net.blackburn.util.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class GetVersionCommand implements CommandBase {



    @Override
// allows me to create a custom command etc for client
    public void runCommand(List<String> command,Minecraft mc){
       
       
        if(!command.isEmpty()){

            if (command.contains(".version")){
                command.clear();
                TextComponent startup = new TextComponent(I18n.m_118938_("blackburn.command.version")+" "+"\u00A7r"+ Const.VERSION);
                startup.setStyle(Style.EMPTY);
                mc.gui.getChat().addMessage(startup);
                command.clear();

                
               
               
                
            }
        
        }
        
      
    }
    @Override
    public String getCommandName(){
        return "blackburn.command.version";
    }
}
