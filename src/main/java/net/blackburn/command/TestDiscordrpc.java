package net.blackburn.command;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.blackburn.Const;
import net.blackburn.client.discordrpc.Discordrpc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.blackburn.Const;

public class TestDiscordrpc  implements CommandBase {
    

@Override
// allows me to create a custom command etc for client
public void runCommand(List<String> command,Minecraft mc){
       
       
    if(!command.isEmpty()){

        if (command.contains(".testrpc")){
            
            TextComponent startup = new TextComponent(I18n.m_118938_("blackburn.command.testrpc"));
            startup.setStyle(Style.EMPTY);
            mc.gui.getChat().addMessage(startup);
            
            Const.rpc.ImageWithDescPersantes("Hehe~ Furries are amazing","Rawr Ran test command","1",0,100);
            command.clear();
        }
    
    }
    
  
}

@Override
public String getCommandName(){
    return "blackburn.command.testrpc";
}
}
