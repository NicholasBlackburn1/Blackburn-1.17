
/**
 * this class will allow me to set up and handle registration of the discordrpc for the minecraft client 
 */
package net.blackburn.client.discordrpc;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.blackburn.Const;
public class Discordrpc {


    // Setup's the Discord rpc for use 
    public static void setup(){
        /*
        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().setReadyEventHandler((user) -> {
            System.out.println("Welcome " + user.username + "#" + user.discriminator + "!");
        }).build();

        DiscordRPC.discordInitialize("886991053121519657", handlers, true);
        */
    }


    // Allows the game to um create rich prents as it starts 
    public static void StartingPresence() {
        /*
        DiscordRichPresence rich = new DiscordRichPresence.Builder("Minecraft is Starting....").setDetails("Loading the UwU's....").build();
        DiscordRPC.discordUpdatePresence(rich);*/
      }


          // Allows the game to um create rich prents as it starts 
    public void LerkingPresence() {
        /*
        DiscordRichPresence rich = new DiscordRichPresence.Builder("Learking in the Main Menu~~").setDetails("Hehe~ i see you").build();
        DiscordRPC.discordUpdatePresence(rich);*/
      }

    public void CustomPresence(String title, String details){/*
        DiscordRichPresence rich = new DiscordRichPresence.Builder(title).setDetails(details).build();
        DiscordRPC.discordUpdatePresence(rich);*/
    }

    public void CustomPresenceWithImage(String title, String details,String image){
        /*
        DiscordRichPresence rich = new DiscordRichPresence.Builder(title,details,image).build();
        DiscordRPC.discordUpdatePresence(rich);*/
    }

    // Allows the game to um create rich prents as it starts 
    public void ImageWithoutDescPersantes(String status, String image, int currentPlayers, int maxPlayers) {
        /*
    DiscordRichPresence rich = new DiscordRichPresence.Builder(status,image,currentPlayers,maxPlayers).build();
    DiscordRPC.discordUpdatePresence(rich);*/
    }


    // Allows the game to um create rich prents as it starts 
    public void ImageWithDescPersantes(String status, String desc, String image, int currentPlayers, int maxPlayers) {
        /*
        Const.warn("Sending update to discord...");
        DiscordRichPresence rich = new DiscordRichPresence.Builder(status,desc,image,currentPlayers,maxPlayers).build();
        DiscordRPC.discordUpdatePresence(rich);
        Const.error("Sent Update to discord...");*/
        }

    
}
