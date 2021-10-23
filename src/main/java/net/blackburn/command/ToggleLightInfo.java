package net.blackburn.command;

import java.util.List;

import com.mojang.brigadier.Command;

import net.blackburn.Const;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

public class ToggleLightInfo implements CommandBase{
    
  
    @Override
     // allows me to create a custom command etc for client
     public void runCommand(List<String> command,Minecraft mc){
        
        

        if(!command.isEmpty()){

            if (command.contains(".lightlevel")){
                
                command.clear();
              
                TextComponent lightlevelenable = new TextComponent(" §5"+I18n.m_118938_("blackburn.command.lightlevel.useage"));
                mc.gui.getChat().addMessage(lightlevelenable);
            
                
                command.clear();
            
            
                
            }

            if (command.contains(".lightlevel enable")){
                command.clear();
                Const.enableLightInfo = true;
                
                TextComponent lightlevelenable = new TextComponent("Light Level Info"+" "+I18n.m_118938_("blackburn.command.enabled"));
                mc.gui.getChat().addMessage(lightlevelenable);
            
                
                command.clear();
            
            
                
            }

            if (command.contains(".lightlevel disable")){
                command.clear();
            
                
                Const.enableLightInfo = false;
                TextComponent lightlevelenable = new TextComponent("Light Level Info"+" "+I18n.m_118938_("blackburn.command.disabled"));
                mc.gui.getChat().addMessage(lightlevelenable);
            
                
                command.clear();
            
            
                
            }
          
        }
        
    }
    @Override
    public String getCommandName(){
        return "blackburn.command.lightlevel";
    }
}
