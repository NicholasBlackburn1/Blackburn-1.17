
/**
 * this is the help command class this will return and display all avalable commands that ive created in mc 
 * @TODO: Need to acutally format help commands and decriptions ad  list in chat 
 */
package net.blackburn.command;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;

import java.util.LinkedList;
import java.util.List;

import javax.security.auth.callback.TextOutputCallback;

import org.apache.logging.log4j.core.tools.picocli.CommandLine.Help.Ansi.Text;

import net.blackburn.Const;
import net.minecraft.client.Minecraft;

public class HelpCommand implements CommandBase{

    @Override
    // allows me to create a custom command etc for client
    public void runCommand(List<String> command,Minecraft mc){
        

        TextComponent startup ;
        TextComponent commands;

        if(!command.isEmpty()){

            if (command.contains(".help")){
              
                startup = new TextComponent(I18n.m_118938_("blackburn.command.help.header"));
                startup.setStyle(Style.EMPTY);
                mc.gui.getChat().addMessage(startup);
                
                // added for loop to loop throw list of commands 
                for(int i=0; i<Const.commandList.size(); i++){
                    Const.warn("lopped about"+i+" "+"times");
                    commands = new TextComponent(I18n.m_118938_(Const.commandList.get(i))+" "+I18n.m_118938_(Const.commandList.get(i)+".desc"));
                    
                    mc.gui.getChat().addMessage(commands);

                }
                command.clear();
               
                   
            }
             
        }
        
        
        
            
            
     }
    
    @Override
    public String getCommandName(){
        return "blackburn.command.help";
    }
    
}
