
/**
 * This is where a draw stuff to the players hud in game
 */
package net.blackburn.client.gui;

import java.util.LinkedList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;

public class Hud {


    // Draws Player pos on screem X:305.0F Y: 210.0F, 220.0F, 230.0F
    public void drawXYZPlayerPos(PoseStack matrixStackIn, Minecraft mc, float[] small, float[] full){
    {
        float[] pos = small;

        int color = 16755200;

        if(!mc.getWindow().isFullscreen()){
        int x = (int)mc.player.getX();
        int y = (int)mc.player.getY();
        int z = (int)mc.player.getZ();
    
        mc.font.drawShadow(matrixStackIn,"x:"+x, 10F,  100.3F, color);
        mc.font.drawShadow(matrixStackIn,"Y:"+y, pos[0],  pos[2], color);
        mc.font.drawShadow(matrixStackIn,"Z:"+z, pos[0],  pos[3], color);
        }

        else{
            //* window pos 335.0F, 240.0F,250.0F,260.0F

            float[] pos2 = full;
            int x = (int)mc.player.getX();
            int y = (int)mc.player.getY();
            int z = (int)mc.player.getZ();
        
            mc.font.drawShadow(matrixStackIn,"x:"+x,  pos2[0],  pos2[1], color);
            mc.font.drawShadow(matrixStackIn,"Y:"+y,  pos2[0],  pos2[2], color);
            mc.font.drawShadow(matrixStackIn,"Z:"+z,  pos2[0],  pos2[3], color);
        }}
        
    }

    // Shouls draw light
    public void drawLightLevel(PoseStack matrixStackIn, Minecraft mc, float[] large, float[] small){
        BlockPos blockpos = mc.getCameraEntity().blockPosition();

        int skybright= mc.level.getBrightness(LightLayer.SKY, blockpos);
        int blocklight = mc.level.getBrightness(LightLayer.BLOCK, blockpos);


        int color = 16755200;

        if(!mc.getWindow().isFullscreen()){
        
    
        mc.font.drawShadow(matrixStackIn,"Sky Light level:"+skybright, small[0],small[1], color);
        mc.font.drawShadow(matrixStackIn,"Block Light level:"+blocklight, small[0], small[2], color);
        //mc.font.drawShadow(matrixStackIn,"Y:"+y, 305.0F, 220.0F, color);
        //mc.font.drawShadow(matrixStackIn,"Z:"+z, 305.0F, 230.0F, color);
        }

        if(mc.getWindow().isFullscreen()){

        mc.font.drawShadow(matrixStackIn,"Sky Light level:"+skybright, large[0], large[1], color);
        mc.font.drawShadow(matrixStackIn,"Block Light level:"+blocklight, large[0], large[2], color);
        }

    
    }
}
