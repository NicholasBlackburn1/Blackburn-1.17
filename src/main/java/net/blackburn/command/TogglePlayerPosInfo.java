package net.blackburn.command;

import java.util.List;

import com.mojang.brigadier.Command;

import net.blackburn.Const;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

public class TogglePlayerPosInfo implements CommandBase{
    
  
    @Override
     // allows me to create a custom command etc for client
     public void runCommand(List<String> command,Minecraft mc){
        
        

        if(!command.isEmpty()){

            if (command.contains(".playerpos")){
                
                command.clear();
              
                TextComponent playersposenable = new TextComponent(" §5"+I18n.m_118938_("blackburn.command.playerpos.useage"));
                mc.gui.getChat().addMessage(playersposenable);
            
                
                command.clear();
            
            
                
            }

            if (command.contains(".playerpos enable")){
                command.clear();
                Const.enablePosInfo = true;
                
                TextComponent playersposenable = new TextComponent("Player Position Info"+" "+I18n.m_118938_("blackburn.command.enabled"));
                mc.gui.getChat().addMessage(playersposenable);
            
                
                command.clear();
            
            
                
            }

            if (command.contains(".playerpos disable")){
                command.clear();
            
                
                Const.enablePosInfo = false;
                TextComponent playersposenable = new TextComponent("Player Position Info"+" "+I18n.m_118938_("blackburn.command.disabled"));
                mc.gui.getChat().addMessage(playersposenable);
            
                
                command.clear();
            
            
                
            }
          
        }
        
    }
    @Override
    public String getCommandName(){
        return "blackburn.command.playerpos";
    }
}
