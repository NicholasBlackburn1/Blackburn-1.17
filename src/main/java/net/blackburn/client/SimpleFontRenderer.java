/**
 * this class is for rendiering custom font on loadig screen 
 * UwU
 */

package net.blackburn.client;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.optifine.render.RenderUtils;

public class SimpleFontRenderer {


    private static final ResourceLocation DEFAULT_FONT = new ResourceLocation("textures/font/ascii.png");
    private static final Map<Character, Integer> CHARACTER_X_OFFSET = new HashMap<Character, Integer>();
    private static final Map<Character, Integer> CHARACTER_HEIGHT_OFFSET = new HashMap<Character, Integer>();

    public static void init() {

        CHARACTER_X_OFFSET.clear();

        CHARACTER_X_OFFSET.put(" ".charAt(0), -3);
        CHARACTER_X_OFFSET.put("!".charAt(0), -4);
        CHARACTER_X_OFFSET.put(".".charAt(0), -4);
        CHARACTER_X_OFFSET.put(",".charAt(0), -4);
        CHARACTER_X_OFFSET.put(":".charAt(0), -4);
        CHARACTER_X_OFFSET.put(";".charAt(0), -4);
        CHARACTER_X_OFFSET.put("*".charAt(0), -3);
        CHARACTER_X_OFFSET.put("'".charAt(0), -4);
        CHARACTER_X_OFFSET.put("´".charAt(0), -4);
        CHARACTER_X_OFFSET.put("`".charAt(0), -4);
        CHARACTER_X_OFFSET.put("/".charAt(0), -3);
        CHARACTER_X_OFFSET.put("\\".charAt(0), -3);
        CHARACTER_X_OFFSET.put("}".charAt(0), -3);
        CHARACTER_X_OFFSET.put("{".charAt(0), -3);
        CHARACTER_X_OFFSET.put(")".charAt(0), -3);
        CHARACTER_X_OFFSET.put("(".charAt(0), -3);
        CHARACTER_X_OFFSET.put("]".charAt(0), -3);
        CHARACTER_X_OFFSET.put("[".charAt(0), -3);
        CHARACTER_X_OFFSET.put("1".charAt(0), -3);
        CHARACTER_X_OFFSET.put("i".charAt(0), -4);
        CHARACTER_X_OFFSET.put("I".charAt(0), -2);
        CHARACTER_X_OFFSET.put("l".charAt(0), -3);
        CHARACTER_X_OFFSET.put("t".charAt(0), -2);
        CHARACTER_X_OFFSET.put("k".charAt(0), -1);

        CHARACTER_HEIGHT_OFFSET.clear();

        CHARACTER_HEIGHT_OFFSET.put("p".charAt(0), 1);
        CHARACTER_HEIGHT_OFFSET.put("q".charAt(0), 1);
        CHARACTER_HEIGHT_OFFSET.put("y".charAt(0), 1);
        CHARACTER_HEIGHT_OFFSET.put("j".charAt(0), 1);
        CHARACTER_HEIGHT_OFFSET.put("g".charAt(0), 1);
        CHARACTER_HEIGHT_OFFSET.put("@".charAt(0), 1);

    }
    // Draw string on loading screemn 
    public static void drawString(PoseStack matrix, String text, int x, int y, float[] color, float alpha, float scale) {


        int xOffset = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c > 0xFF) {
                continue;
            }
            int charX = (c & 0x0F) * 8;
            int charY = (c >> 4 & 0x0F) * 8;
            int heightOffset = 0;
            if (CHARACTER_HEIGHT_OFFSET.containsKey(c)) {
                heightOffset = CHARACTER_HEIGHT_OFFSET.get(c);
            }
            de.keksuccino.konkrete.rendering.RenderUtils.bindTexture(DEFAULT_FONT);
            matrix.pushPose();
            RenderSystem.setShaderColor(color[0], color[1], color[2], alpha);
            matrix.translate((x + ((i * 6) * scale)) + (xOffset * scale), y, 0);
            matrix.scale(scale, scale, 0);
            GuiComponent.blit(matrix, 0, 0, charX, charY, 6, 7 + heightOffset, 128, 128); //charX  charY  6  7  128  128
            //Apply char offset for next char
            if (CHARACTER_X_OFFSET.containsKey(c)) {
                xOffset += CHARACTER_X_OFFSET.get(c);
            }
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            matrix.popPose();
        }

    }

    // Draaes a string with the shadow
    public static void drawStringWithShadow(PoseStack matrix, String text, int x, int y, float[] rgbColor, float alpha, float scale) {
        //draw shadow
        drawString(matrix, text, x + Math.max((int)(1 * scale), 1), y + Math.max((int)(1 * scale), 1), rgbColor, alpha / 2.0F, scale);
        //draw normal text
        drawString(matrix, text, x, y, rgbColor, alpha, scale);
    }

    public static int getStringWidth(String text) {
        int length = 0;
        for (char c : text.toCharArray()) {
            int i = 6;
            if (CHARACTER_X_OFFSET.containsKey(c)) {
                i += CHARACTER_X_OFFSET.get(c);
            }
            length += i;
        }
        return length;
    }

    public static int getStringHeight() {
        return 7;
    }

    protected static float[] getColor(int rgb) {
        float[] color = new float[] { 0.0f, 0.0f, 0.0f};
        color[2] = ((rgb) & 0xFF) / 255.0f;
        color[1] = ((rgb >> 8 ) & 0xFF) / 255.0f;
        color[0] = ((rgb >> 16 ) & 0xFF) / 255.0f;
        return color;
    }

    // Renders MeM info on loading up
    public static void renderMeMInfo(PoseStack pose){
        float[] color = {0.0F,200F,0F};

        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean() ;
        MemoryUsage heap = memBean.getHeapMemoryUsage();
        MemoryUsage nonHeap = memBean.getNonHeapMemoryUsage();

        drawString(pose,String.format("(Heap: Init: %d, Used: %d, Committed: %d, Max: %d)", heap.getInit()/100000, heap.getUsed()/100000, heap.getCommitted()/100000, heap.getMax()/100000),0,0,color,2,1);
    }

}

