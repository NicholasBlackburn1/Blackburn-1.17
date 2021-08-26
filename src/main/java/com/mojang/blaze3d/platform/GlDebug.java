package com.mojang.blaze3d.platform;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.optifine.Config;
import net.optifine.GlErrors;
import net.optifine.util.ArrayUtils;
import net.optifine.util.StrUtils;
import net.optifine.util.TimedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLDebugMessageARBCallback;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.opengl.KHRDebug;

public class GlDebug
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int CIRCULAR_LOG_SIZE = 10;
    private static final Queue<GlDebug.LogEntry> MESSAGE_BUFFER = EvictingQueue.create(10);
    @Nullable
    private static volatile GlDebug.LogEntry lastEntry;
    private static final List<Integer> DEBUG_LEVELS = ImmutableList.of(37190, 37191, 37192, 33387);
    private static final List<Integer> DEBUG_LEVELS_ARB = ImmutableList.of(37190, 37191, 37192);
    private static boolean debugEnabled;
    private static int[] ignoredErrors = makeIgnoredErrors();

    private static int[] makeIgnoredErrors()
    {
        String s = System.getProperty("gl.ignore.errors");

        if (s == null)
        {
            return new int[0];
        }
        else
        {
            String[] astring = Config.tokenize(s, ",");
            int[] aint = new int[0];

            for (int i = 0; i < astring.length; ++i)
            {
                String s1 = astring[i].trim();
                int j = s1.startsWith("0x") ? Config.parseHexInt(s1, -1) : Config.parseInt(s1, -1);

                if (j < 0)
                {
                    Config.warn("Invalid error id: " + s1);
                }
                else
                {
                    Config.log("Ignore OpenGL error: " + j);
                    aint = ArrayUtils.addIntToArray(aint, j);
                }
            }

            return aint;
        }
    }

    private static String printUnknownToken(int p_84037_)
    {
        return "Unknown (0x" + Integer.toHexString(p_84037_).toUpperCase() + ")";
    }

    public static String sourceToString(int p_84056_)
    {
        switch (p_84056_)
        {
            case 33350:
                return "API";

            case 33351:
                return "WINDOW SYSTEM";

            case 33352:
                return "SHADER COMPILER";

            case 33353:
                return "THIRD PARTY";

            case 33354:
                return "APPLICATION";

            case 33355:
                return "OTHER";

            default:
                return printUnknownToken(p_84056_);
        }
    }

    public static String typeToString(int p_84058_)
    {
        switch (p_84058_)
        {
            case 33356:
                return "ERROR";

            case 33357:
                return "DEPRECATED BEHAVIOR";

            case 33358:
                return "UNDEFINED BEHAVIOR";

            case 33359:
                return "PORTABILITY";

            case 33360:
                return "PERFORMANCE";

            case 33361:
                return "OTHER";

            case 33384:
                return "MARKER";

            default:
                return printUnknownToken(p_84058_);
        }
    }

    public static String severityToString(int p_84060_)
    {
        switch (p_84060_)
        {
            case 33387:
                return "NOTIFICATION";

            case 37190:
                return "HIGH";

            case 37191:
                return "MEDIUM";

            case 37192:
                return "LOW";

            default:
                return printUnknownToken(p_84060_);
        }
    }

    private static void printDebugLog(int pSource, int pType, int pId, int pSeverity, int pMessageLength, long pMessage, long p_84045_)
    {
        if (pType != 33385 && pType != 33386)
        {
            if (!ArrayUtils.contains(ignoredErrors, pId))
            {
                if (!Config.isShaders() || pSource != 33352)
                {
                    Minecraft minecraft = Minecraft.getInstance();

                    if (minecraft == null || minecraft.getWindow() == null || !minecraft.getWindow().isClosed())
                    {
                        if (GlErrors.isEnabled(pId))
                        {
                            String s = sourceToString(pSource);
                            String s1 = typeToString(pType);
                            String s2 = severityToString(pSeverity);
                            String s3 = GLDebugMessageCallback.getMessage(pMessageLength, pMessage);
                            s3 = StrUtils.trim(s3, " \n\r\t");
                            String s4 = String.format("OpenGL %s %s: %s (%s)", s, s1, pId, s3);
                            Exception exception = new Exception("Stack trace");
                            StackTraceElement[] astacktraceelement = exception.getStackTrace();
                            StackTraceElement[] astacktraceelement1 = astacktraceelement.length > 2 ? Arrays.copyOfRange(astacktraceelement, 2, astacktraceelement.length) : astacktraceelement;
                            exception.setStackTrace(astacktraceelement1);

                            if (pType == 33356)
                            {
                                LOGGER.error(s4, (Throwable)exception);
                            }
                            else
                            {
                                LOGGER.info(s4, (Throwable)exception);
                            }

                            if (Config.isShowGlErrors() && TimedEvent.isActive("ShowGlErrorDebug", 10000L))
                            {
                                String s5 = Config.getGlErrorString(pId);

                                if (pId == 0 || Config.equals(s5, "Unknown"))
                                {
                                    s5 = s3;
                                }

                                String s6 = I18n.m_118938_("of.message.openglError", pId, s5);
                                Minecraft.getInstance().gui.getChat().addMessage(new TextComponent(s6));
                            }

                            String s7 = GLDebugMessageCallback.getMessage(pMessageLength, pMessage);

                            synchronized (MESSAGE_BUFFER)
                            {
                                GlDebug.LogEntry gldebug$logentry = lastEntry;

                                if (gldebug$logentry != null && gldebug$logentry.isSame(pSource, pType, pId, pSeverity, s7))
                                {
                                    ++gldebug$logentry.count;
                                }
                                else
                                {
                                    gldebug$logentry = new GlDebug.LogEntry(pSource, pType, pId, pSeverity, s7);
                                    MESSAGE_BUFFER.add(gldebug$logentry);
                                    lastEntry = gldebug$logentry;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static List<String> getLastOpenGlDebugMessages()
    {
        synchronized (MESSAGE_BUFFER)
        {
            List<String> list = Lists.newArrayListWithCapacity(MESSAGE_BUFFER.size());

            for (GlDebug.LogEntry gldebug$logentry : MESSAGE_BUFFER)
            {
                list.add(gldebug$logentry + " x " + gldebug$logentry.count);
            }

            return list;
        }
    }

    public static boolean isDebugEnabled()
    {
        return debugEnabled;
    }

    public static void enableDebugCallback(int pDebugVerbosity, boolean pSynchronous)
    {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);

        if (pDebugVerbosity > 0)
        {
            GLCapabilities glcapabilities = GL.getCapabilities();

            if (glcapabilities.GL_KHR_debug)
            {
                debugEnabled = true;
                GL11.glEnable(37600);

                if (pSynchronous)
                {
                    GL11.glEnable(33346);
                }

                for (int i = 0; i < DEBUG_LEVELS.size(); ++i)
                {
                    boolean flag = i < pDebugVerbosity;
                    KHRDebug.glDebugMessageControl(4352, 4352, DEBUG_LEVELS.get(i), (int[])null, flag);
                }

                KHRDebug.glDebugMessageCallback(GLX.make(GLDebugMessageCallback.create(GlDebug::printDebugLog), DebugMemoryUntracker::untrack), 0L);
            }
            else if (glcapabilities.GL_ARB_debug_output)
            {
                debugEnabled = true;

                if (pSynchronous)
                {
                    GL11.glEnable(33346);
                }

                for (int j = 0; j < DEBUG_LEVELS_ARB.size(); ++j)
                {
                    boolean flag1 = j < pDebugVerbosity;
                    ARBDebugOutput.glDebugMessageControlARB(4352, 4352, DEBUG_LEVELS_ARB.get(j), (int[])null, flag1);
                }

                ARBDebugOutput.glDebugMessageCallbackARB(GLX.make(GLDebugMessageARBCallback.create(GlDebug::printDebugLog), DebugMemoryUntracker::untrack), 0L);
            }
        }
    }

    static class LogEntry
    {
        private final int id;
        private final int source;
        private final int type;
        private final int severity;
        private final String message;
        int count = 1;

        LogEntry(int p_166234_, int p_166235_, int p_166236_, int p_166237_, String p_166238_)
        {
            this.id = p_166236_;
            this.source = p_166234_;
            this.type = p_166235_;
            this.severity = p_166237_;
            this.message = p_166238_;
        }

        boolean isSame(int p_166240_, int p_166241_, int p_166242_, int p_166243_, String p_166244_)
        {
            return p_166241_ == this.type && p_166240_ == this.source && p_166242_ == this.id && p_166243_ == this.severity && p_166244_.equals(this.message);
        }

        public String toString()
        {
            return "id=" + this.id + ", source=" + GlDebug.sourceToString(this.source) + ", type=" + GlDebug.typeToString(this.type) + ", severity=" + GlDebug.severityToString(this.severity) + ", message='" + this.message + "'";
        }
    }
}
