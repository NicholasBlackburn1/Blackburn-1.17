package net.blackburn.command;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;

public class CritAllThetime implements CommandBase{

    @Override
    public void runCommand(List<String> command, Minecraft mc) {
        
        if(command.contains(".crit")){
            command.clear();
              
            TextComponent crite = new TextComponent(" §5"+I18n.m_118938_("blackburn.command.crit.useage"));
            mc.gui.getChat().addMessage(crite);
        }

        if(command.contains(".crit enable")){
            command.clear();
              
            TextComponent crite = new TextComponent(" §5"+I18n.m_118938_("blackburn.command.crit.enable"));
            mc.gui.getChat().addMessage(crite);

            
        }
        
        
    }

    @Override
    public String getCommandName() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
    
