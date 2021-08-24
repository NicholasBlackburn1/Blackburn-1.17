/**
 * This class is for rendering stuff on the loading screen kinda like forge but my way of imp
 */
package net.blackburn.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.stb.STBEasyFont;
import org.lwjgl.system.MemoryUtil;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.nio.ByteBuffer;

public class LoadingScreen {

    private final Minecraft minecraft;
    private final Window window;
    private boolean handledElsewhere;

    public LoadingScreen(final Minecraft minecraft) {
        this.minecraft = minecraft;
        this.window = minecraft.getWindow();
    }
    // Allows render of bakgorund
    private void renderBackground() {
        GL11.glBegin(GL11.GL_QUADS);
        boolean isDarkBackground = minecraft.options.darkMojangStudiosBackground;
        GL11.glColor4f(isDarkBackground ? 0 : (239F / 255F), isDarkBackground ? 0 : (50F / 255F), isDarkBackground ? 0 : (61F / 255F), 1); //Color from LoadingOverlay
        GL11.glVertex3f(0, 0, -10);
        GL11.glVertex3f(0, window.getGuiScaledHeight(), -10);
        GL11.glVertex3f(window.getGuiScaledWidth(), window.getGuiScaledHeight(), -10);
        GL11.glVertex3f(window.getGuiScaledWidth(), 0, -10);
        GL11.glEnd();
    }


    private static final float[] memorycolour = new float[] { 0.0f, 0.0f, 0.0f};

    // Renders MeM info
    private void renderMemoryInfo() {
        final MemoryUsage heapusage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        final MemoryUsage offheapusage = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
        final float pctmemory = (float) heapusage.getUsed() / heapusage.getMax();
        String memory = String.format("Memory Heap: %d / %d MB (%.1f%%)  OffHeap: %d MB", heapusage.getUsed() >> 20, heapusage.getMax() >> 20, pctmemory * 100.0, offheapusage.getUsed() >> 20);

        final int i = Mth.hsvToRgb((1.0f - (float)Math.pow(pctmemory, 1.5f)) / 3f, 1.0f, 0.5f);
        memorycolour[2] = ((i) & 0xFF) / 255.0f;
        memorycolour[1] = ((i >> 8 ) & 0xFF) / 255.0f;
        memorycolour[0] = ((i >> 16 ) & 0xFF) / 255.0f;
        renderMessage(memory, memorycolour, 1, 1.0f);
    }

    // allows me to render stuff on loading screen
    @SuppressWarnings("deprecation")
    public void renderMessage(final String message, final float[] colour, int line, float alpha) {
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        ByteBuffer charBuffer = MemoryUtil.memAlloc(message.length() * 270);
        int quads = STBEasyFont.stb_easy_font_print(0, 0, message, null, charBuffer);
        GL14.glVertexPointer(2, GL11.GL_FLOAT, 16, charBuffer);
        //
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        // // STBEasyFont's quads are in reverse order or what OGGL expects, so it gets culled for facing the wrong way.
        // // So Disable culling https://github.com/MinecraftForge/MinecraftForge/pull/6824
        RenderSystem.disableCull();
         GL14.glBlendColor(0,0,0, alpha);
         RenderSystem.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
         GL11.glColor3f(colour[0],colour[1],colour[2]);
         GL11.glPushMatrix();
         GL11.glTranslatef(10, line * 10, 0);
         GL11.glScalef(1, 1, 0);
         GL11.glDrawArrays(GL11.GL_QUADS, 0, quads * 4);
         GL11.glPopMatrix();
        
         RenderSystem.enableCull();
         GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
         MemoryUtil.memFree(charBuffer);
    }

    // This allows me to add stuff to th loading screen of mc
    public void writeStuffToLoading(){
        int i = 200;
        memorycolour[2] = ((i) & 0xFF) / 255.0f;
        memorycolour[1] = ((i >> 8 ) & 0xFF) / 255.0f;
        memorycolour[0] = ((i >> 16 ) & 0xFF) / 255.0f;

        renderMessage("Hello Hacked mc 1.17", memorycolour,  1, 1.0f);
    }
}
