
/***
 * This should be where i can get chat messages and set them up to be sent out from a diy interpeter
 */
package net.blackburn.util;

import net.blackburn.Const;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

public class ChatUtils {

    private Minecraft minecraft = Minecraft.getInstance();

    public  void getUserChat(){
       Const.error(minecraft.gui.getChat().getRecentChat().toString());
     
    }


    // This will allow me to display the success full run of command 
    public void CommandSuccess(){

        TextComponent success = new TextComponent(I18n.m_118938_("blackburn.command.success"));
        success.setStyle(Style.EMPTY);
        minecraft.gui.getChat().addMessage(success);
    }



    // This will allow me to display the fail full run of command 
    public void CommandFail(){

        TextComponent fail = new TextComponent(I18n.m_118938_("blackburn.command.fail"));
        fail.setStyle(Style.EMPTY);
        minecraft.gui.getChat().addMessage(fail);
    }

   
    
    
}
