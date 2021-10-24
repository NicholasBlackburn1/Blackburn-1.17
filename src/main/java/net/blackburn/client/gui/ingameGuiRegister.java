//*This wear you reguister components of the gui for the inagame screen

package net.blackburn.client.gui;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import org.lwjgl.system.Platform;

import net.blackburn.Const;
import net.minecraft.client.Minecraft;

public class ingameGuiRegister {
    
    Hud hud = new Hud();
    Const constant = new Const();

    public void RegisterComponents(PoseStack matrixStackIn, Minecraft mc){

        //* linux Display platform
        if( Platform.get() == Platform.LINUX){

            //* Enables player pos seen
            if(constant.enablePosInfo){

                float[] small = {305.0F, 210.0F, 220.0F, 230.0F};
                float[] large = { 335.0F, 240.0F,250.0F,260.0F};

                hud.drawXYZPlayerPos(matrixStackIn, mc,small,large);
            }

            //* enable light info
            if(constant.enableLightInfo){
                
                float[] large = {400.0F, 210.0F, 220.0F};
                float[] small = {400.0F, 210.0F, 220.0F};

                hud.drawLightLevel(matrixStackIn, mc,large,small);
        }



        //* Windows Display platform
        if( Platform.get() == Platform.WINDOWS){

            //* Enables player pos seen
            if(constant.enablePosInfo){

                float[] small = {305.0F, 210.0F, 220.0F, 230.0F};
                float[] large = { 335.0F, 240.0F,250.0F,260.0F};

                hud.drawXYZPlayerPos(matrixStackIn, mc,small,large);

                Const.dbg("Enabled command");
            }

            //* enable light info
            if(constant.enableLightInfo){
                
                float[] large = {400.0F, 210.0F, 220.0F};
                float[] small = {400.0F, 210.0F, 220.0F};

                hud.drawLightLevel(matrixStackIn, mc,large,small);
        }


    }
    }
}}
