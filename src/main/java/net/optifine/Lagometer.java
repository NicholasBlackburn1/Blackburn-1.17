package net.optifine;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.optifine.util.MemoryMonitor;

public class Lagometer
{
    private static Minecraft mc;
    private static Options gameSettings;
    private static ProfilerFiller profiler;
    public static boolean active = false;
    public static Lagometer.TimerNano timerTick = new Lagometer.TimerNano();
    public static Lagometer.TimerNano timerScheduledExecutables = new Lagometer.TimerNano();
    public static Lagometer.TimerNano timerChunkUpload = new Lagometer.TimerNano();
    public static Lagometer.TimerNano timerChunkUpdate = new Lagometer.TimerNano();
    public static Lagometer.TimerNano timerVisibility = new Lagometer.TimerNano();
    public static Lagometer.TimerNano timerTerrain = new Lagometer.TimerNano();
    public static Lagometer.TimerNano timerServer = new Lagometer.TimerNano();
    private static long[] timesFrame = new long[512];
    private static long[] timesTick = new long[512];
    private static long[] timesScheduledExecutables = new long[512];
    private static long[] timesChunkUpload = new long[512];
    private static long[] timesChunkUpdate = new long[512];
    private static long[] timesVisibility = new long[512];
    private static long[] timesTerrain = new long[512];
    private static long[] timesServer = new long[512];
    private static boolean[] gcs = new boolean[512];
    private static int numRecordedFrameTimes = 0;
    private static long prevFrameTimeNano = -1L;
    private static long renderTimeNano = 0L;

    public static void updateLagometer()
    {
        if (mc == null)
        {
            mc = Minecraft.getInstance();
            gameSettings = mc.options;
            profiler = mc.getProfiler();
        }

        if (gameSettings.renderDebug && (gameSettings.ofLagometer || gameSettings.renderFpsChart))
        {
            active = true;
            long timeNowNano = System.nanoTime();

            if (prevFrameTimeNano == -1L)
            {
                prevFrameTimeNano = timeNowNano;
            }
            else
            {
                int j = numRecordedFrameTimes & timesFrame.length - 1;
                ++numRecordedFrameTimes;
                boolean flag = MemoryMonitor.isGcEvent();
                timesFrame[j] = timeNowNano - prevFrameTimeNano - renderTimeNano;
                timesTick[j] = timerTick.timeNano;
                timesScheduledExecutables[j] = timerScheduledExecutables.timeNano;
                timesChunkUpload[j] = timerChunkUpload.timeNano;
                timesChunkUpdate[j] = timerChunkUpdate.timeNano;
                timesVisibility[j] = timerVisibility.timeNano;
                timesTerrain[j] = timerTerrain.timeNano;
                timesServer[j] = timerServer.timeNano;
                gcs[j] = flag;
                timerTick.reset();
                timerScheduledExecutables.reset();
                timerVisibility.reset();
                timerChunkUpdate.reset();
                timerChunkUpload.reset();
                timerTerrain.reset();
                timerServer.reset();
                prevFrameTimeNano = System.nanoTime();
            }
        }
        else
        {
            active = false;
            prevFrameTimeNano = -1L;
        }
    }

    public static void showLagometer(PoseStack matrixStackIn, int scaleFactor)
    {
        if (gameSettings != null)
        {
            if (gameSettings.ofLagometer || gameSettings.renderFpsChart)
            {
                long i = System.nanoTime();
                GlStateManager.clear(256);
                RenderSystem.backupProjectionMatrix();
                int j = mc.getWindow().getWidth();
                int k = mc.getWindow().getHeight();
                Matrix4f matrix4f = Matrix4f.orthographic((float)j, (float)(-k), 1000.0F, 3000.0F);
                RenderSystem.setProjectionMatrix(matrix4f);
                matrixStackIn.pushPose();
                GlStateManager._disableTexture();
                GlStateManager._depthMask(false);
                GlStateManager._disableCull();
                RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
                RenderSystem.lineWidth(1.0F);
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder bufferbuilder = tesselator.getBuilder();
                bufferbuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);

                for (int l = 0; l < timesFrame.length; ++l)
                {
                    int i1 = (l - numRecordedFrameTimes & timesFrame.length - 1) * 100 / timesFrame.length;
                    i1 = i1 + 155;
                    float f = (float)k;
                    long j1 = 0L;

                    if (gcs[l])
                    {
                        renderTime(l, timesFrame[l], i1, i1 / 2, 0, f, bufferbuilder);
                    }
                    else
                    {
                        renderTime(l, timesFrame[l], i1, i1, i1, f, bufferbuilder);
                        f = f - (float)renderTime(l, timesServer[l], i1 / 2, i1 / 2, i1 / 2, f, bufferbuilder);
                        f = f - (float)renderTime(l, timesTerrain[l], 0, i1, 0, f, bufferbuilder);
                        f = f - (float)renderTime(l, timesVisibility[l], i1, i1, 0, f, bufferbuilder);
                        f = f - (float)renderTime(l, timesChunkUpdate[l], i1, 0, 0, f, bufferbuilder);
                        f = f - (float)renderTime(l, timesChunkUpload[l], i1, 0, i1, f, bufferbuilder);
                        f = f - (float)renderTime(l, timesScheduledExecutables[l], 0, 0, i1, f, bufferbuilder);
                        float f2 = f - (float)renderTime(l, timesTick[l], 0, i1, i1, f, bufferbuilder);
                    }
                }

                renderTimeDivider(0, timesFrame.length, 33333333L, 196, 196, 196, (float)k, bufferbuilder);
                renderTimeDivider(0, timesFrame.length, 16666666L, 196, 196, 196, (float)k, bufferbuilder);
                tesselator.end();
                GlStateManager._enableCull();
                GlStateManager._depthMask(true);
                GlStateManager._enableTexture();
                int i3 = k - 80;
                int j3 = k - 160;
                String s = Config.isShowFrameTime() ? "33" : "30";
                String s1 = Config.isShowFrameTime() ? "17" : "60";
                mc.font.draw(matrixStackIn, s, 2.0F, (float)(j3 + 1), -8947849);
                mc.font.draw(matrixStackIn, s, 1.0F, (float)j3, -3881788);
                mc.font.draw(matrixStackIn, s1, 2.0F, (float)(i3 + 1), -8947849);
                mc.font.draw(matrixStackIn, s1, 1.0F, (float)i3, -3881788);
                RenderSystem.restoreProjectionMatrix();
                matrixStackIn.popPose();
                float f1 = 1.0F - (float)((double)(System.currentTimeMillis() - MemoryMonitor.getStartTimeMs()) / 1000.0D);
                f1 = Config.limit(f1, 0.0F, 1.0F);
                int k1 = (int)Mth.lerp(f1, 180.0F, 255.0F);
                int l1 = (int)Mth.lerp(f1, 110.0F, 155.0F);
                int i2 = (int)Mth.lerp(f1, 15.0F, 20.0F);
                int j2 = k1 << 16 | l1 << 8 | i2;
                int k2 = 512 / scaleFactor + 2;
                int l2 = k / scaleFactor - 8;
                Gui gui = mc.gui;
                Gui.fill(matrixStackIn, k2 - 1, l2 - 1, k2 + 50, l2 + 10, -1605349296);
                mc.font.draw(matrixStackIn, " " + MemoryMonitor.getAllocationRateMb() + " MB/s", (float)k2, (float)l2, j2);
                renderTimeNano = System.nanoTime() - i;
            }
        }
    }

    private static long renderTime(int frameNum, long time, int r, int g, int b, float baseHeight, BufferBuilder tessellator)
    {
        long i = time / 200000L;

        if (i < 3L)
        {
            return 0L;
        }
        else
        {
            tessellator.vertex((double)((float)frameNum + 0.5F), (double)(baseHeight - (float)i + 0.5F), 0.0D).color(r, g, b, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
            tessellator.vertex((double)((float)frameNum + 0.5F), (double)(baseHeight + 0.5F), 0.0D).color(r, g, b, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
            return i;
        }
    }

    private static long renderTimeDivider(int frameStart, int frameEnd, long time, int r, int g, int b, float baseHeight, BufferBuilder tessellator)
    {
        long i = time / 200000L;

        if (i < 3L)
        {
            return 0L;
        }
        else
        {
            tessellator.vertex((double)((float)frameStart + 0.5F), (double)(baseHeight - (float)i + 0.5F), 0.0D).color(r, g, b, 255).normal(1.0F, 0.0F, 0.0F).endVertex();
            tessellator.vertex((double)((float)frameEnd + 0.5F), (double)(baseHeight - (float)i + 0.5F), 0.0D).color(r, g, b, 255).normal(1.0F, 0.0F, 0.0F).endVertex();
            return i;
        }
    }

    public static boolean isActive()
    {
        return active;
    }

    public static class TimerNano
    {
        public long timeStartNano = 0L;
        public long timeNano = 0L;

        public void start()
        {
            if (Lagometer.active)
            {
                if (this.timeStartNano == 0L)
                {
                    this.timeStartNano = System.nanoTime();
                }
            }
        }

        public void end()
        {
            if (Lagometer.active)
            {
                if (this.timeStartNano != 0L)
                {
                    this.timeNano += System.nanoTime() - this.timeStartNano;
                    this.timeStartNano = 0L;
                }
            }
        }

        private void reset()
        {
            this.timeNano = 0L;
            this.timeStartNano = 0L;
        }
    }
}
