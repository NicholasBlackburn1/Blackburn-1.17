package net.minecraft.client.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.properties.PropertyMap.Serializer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.List;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.CrashReport;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.User;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.blackburn.client.discordrpc.Discordrpc;

public class Main
{
    static final Logger LOGGER = LogManager.getLogger();

    @DontObfuscate
    public static void main(String[] p_129642_)
    {
        Discordrpc.setup();
        Discordrpc.StartingPresence();
        
        SharedConstants.tryDetectVersion();
        OptionParser optionparser = new OptionParser();
        optionparser.allowsUnrecognizedOptions();
        optionparser.accepts("demo");
        optionparser.accepts("disableMultiplayer");
        optionparser.accepts("disableChat");
        optionparser.accepts("fullscreen");
        optionparser.accepts("checkGlErrors");
        OptionSpec<String> optionspec = optionparser.accepts("server").withRequiredArg();
        OptionSpec<Integer> optionspec1 = optionparser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(25565);
        OptionSpec<File> optionspec2 = optionparser.accepts("gameDir").withRequiredArg().ofType(File.class).defaultsTo(new File("."));
        OptionSpec<File> optionspec3 = optionparser.accepts("assetsDir").withRequiredArg().ofType(File.class);
        OptionSpec<File> optionspec4 = optionparser.accepts("resourcePackDir").withRequiredArg().ofType(File.class);
        OptionSpec<String> optionspec5 = optionparser.accepts("proxyHost").withRequiredArg();
        OptionSpec<Integer> optionspec6 = optionparser.accepts("proxyPort").withRequiredArg().defaultsTo("8080").ofType(Integer.class);
        OptionSpec<String> optionspec7 = optionparser.accepts("proxyUser").withRequiredArg();
        OptionSpec<String> optionspec8 = optionparser.accepts("proxyPass").withRequiredArg();
        OptionSpec<String> optionspec9 = optionparser.accepts("username").withRequiredArg().defaultsTo("Player" + Util.getMillis() % 1000L);
        OptionSpec<String> optionspec10 = optionparser.accepts("uuid").withRequiredArg();
        OptionSpec<String> optionspec11 = optionparser.accepts("accessToken").withRequiredArg().required();
        OptionSpec<String> optionspec12 = optionparser.accepts("version").withRequiredArg().required();
        OptionSpec<Integer> optionspec13 = optionparser.accepts("width").withRequiredArg().ofType(Integer.class).defaultsTo(854);
        OptionSpec<Integer> optionspec14 = optionparser.accepts("height").withRequiredArg().ofType(Integer.class).defaultsTo(480);
        OptionSpec<Integer> optionspec15 = optionparser.accepts("fullscreenWidth").withRequiredArg().ofType(Integer.class);
        OptionSpec<Integer> optionspec16 = optionparser.accepts("fullscreenHeight").withRequiredArg().ofType(Integer.class);
        OptionSpec<String> optionspec17 = optionparser.accepts("userProperties").withRequiredArg().defaultsTo("{}");
        OptionSpec<String> optionspec18 = optionparser.accepts("profileProperties").withRequiredArg().defaultsTo("{}");
        OptionSpec<String> optionspec19 = optionparser.accepts("assetIndex").withRequiredArg();
        OptionSpec<String> optionspec20 = optionparser.accepts("userType").withRequiredArg().defaultsTo("legacy");
        OptionSpec<String> optionspec21 = optionparser.accepts("versionType").withRequiredArg().defaultsTo("release");
        OptionSpec<String> optionspec22 = optionparser.nonOptions();
        OptionSet optionset = optionparser.parse(p_129642_);
        List<String> list = optionset.valuesOf(optionspec22);

        if (!list.isEmpty())
        {
            System.out.println("Completely ignored arguments: " + list);
        }

        String s = parseArgument(optionset, optionspec5);
        Proxy proxy = Proxy.NO_PROXY;

        if (s != null)
        {
            try
            {
                proxy = new Proxy(Type.SOCKS, new InetSocketAddress(s, parseArgument(optionset, optionspec6)));
            }
            catch (Exception exception)
            {
            }
        }

        final String s1 = parseArgument(optionset, optionspec7);
        final String s2 = parseArgument(optionset, optionspec8);

        if (!proxy.equals(Proxy.NO_PROXY) && stringHasValue(s1) && stringHasValue(s2))
        {
            Authenticator.setDefault(new Authenticator()
            {
                protected PasswordAuthentication getPasswordAuthentication()
                {
                    return new PasswordAuthentication(s1, s2.toCharArray());
                }
            });
        }

        int i = parseArgument(optionset, optionspec13);
        int j = parseArgument(optionset, optionspec14);
        OptionalInt optionalint = ofNullable(parseArgument(optionset, optionspec15));
        OptionalInt optionalint1 = ofNullable(parseArgument(optionset, optionspec16));
        boolean flag = optionset.has("fullscreen");
        boolean flag1 = optionset.has("demo");
        boolean flag2 = optionset.has("disableMultiplayer");
        boolean flag3 = optionset.has("disableChat");
        String s3 = parseArgument(optionset, optionspec12);
        Gson gson = (new GsonBuilder()).registerTypeAdapter(PropertyMap.class, new Serializer()).create();
        PropertyMap propertymap = GsonHelper.fromJson(gson, parseArgument(optionset, optionspec17), PropertyMap.class);
        PropertyMap propertymap1 = GsonHelper.fromJson(gson, parseArgument(optionset, optionspec18), PropertyMap.class);
        String s4 = parseArgument(optionset, optionspec21);
        File file1 = parseArgument(optionset, optionspec2);
        File file2 = optionset.has(optionspec3) ? parseArgument(optionset, optionspec3) : new File(file1, "assets/");
        File file3 = optionset.has(optionspec4) ? parseArgument(optionset, optionspec4) : new File(file1, "resourcepacks/");
        String s5 = optionset.has(optionspec10) ? optionspec10.value(optionset) : Player.createPlayerUUID(optionspec9.value(optionset)).toString();
        String s6 = optionset.has(optionspec19) ? optionspec19.value(optionset) : null;
        String s7 = parseArgument(optionset, optionspec);
        Integer integer = parseArgument(optionset, optionspec1);
        CrashReport.preload();
        Bootstrap.bootStrap();
        Bootstrap.validate();
        Util.startTimerHackThread();
        User user = new User(optionspec9.value(optionset), s5, optionspec11.value(optionset), optionspec20.value(optionset));
        GameConfig gameconfig = new GameConfig(new GameConfig.UserData(user, propertymap, propertymap1, proxy), new DisplayData(i, j, optionalint, optionalint1, flag), new GameConfig.FolderData(file1, file3, file2, s6), new GameConfig.GameData(flag1, s3, s4, flag2, flag3), new GameConfig.ServerData(s7, integer));
        Thread thread = new Thread("Client Shutdown Thread")
        {
            public void run()
            {
                Minecraft minecraft1 = Minecraft.getInstance();

                if (minecraft1 != null)
                {
                    IntegratedServer integratedserver = minecraft1.getSingleplayerServer();

                    if (integratedserver != null)
                    {
                        integratedserver.halt(true);
                    }
                }
            }
        };
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        Runtime.getRuntime().addShutdownHook(thread);
        new RenderPipeline();
        final Minecraft minecraft;

        try
        {
            Thread.currentThread().setName("Render thread");
            RenderSystem.initRenderThread();
            RenderSystem.beginInitialization();
            minecraft = new Minecraft(gameconfig);
            RenderSystem.finishInitialization();
        }
        catch (SilentInitException silentinitexception)
        {
            LOGGER.warn("Failed to create window: ", (Throwable)silentinitexception);
            return;
        }
        catch (Throwable throwable1)
        {
            CrashReport crashreport = CrashReport.forThrowable(throwable1, "Initializing game");
            crashreport.addCategory("Initialization");
            Minecraft.fillReport((Minecraft)null, (LanguageManager)null, gameconfig.game.launchVersion, (Options)null, crashreport);
            Minecraft.crash(crashreport);
            return;
        }

        Thread thread1;

        if (minecraft.renderOnThread())
        {
            thread1 = new Thread("Game thread")
            {
                public void run()
                {
                    try
                    {
                        RenderSystem.initGameThread(true);
                        minecraft.run();
                    }
                    catch (Throwable throwable2)
                    {
                        Main.LOGGER.error("Exception in client thread", throwable2);
                    }
                }
            };
            thread1.start();

            while (minecraft.isRunning())
            {
            }
        }
        else
        {
            thread1 = null;

            try
            {
                RenderSystem.initGameThread(false);
                minecraft.run();
            }
            catch (Throwable throwable)
            {
                LOGGER.error("Unhandled game exception", throwable);
            }
        }

        BufferUploader.reset();

        try
        {
            minecraft.stop();

            if (thread1 != null)
            {
                thread1.join();
            }
        }
        catch (InterruptedException interruptedexception)
        {
            LOGGER.error("Exception during client thread shutdown", (Throwable)interruptedexception);
        }
        finally
        {
            minecraft.destroy();
        }
    }

    private static OptionalInt ofNullable(@Nullable Integer pValue)
    {
        return pValue != null ? OptionalInt.of(pValue) : OptionalInt.empty();
    }

    @Nullable
    private static <T> T parseArgument(OptionSet pSet, OptionSpec<T> pOption)
    {
        try
        {
            return pSet.valueOf(pOption);
        }
        catch (Throwable throwable)
        {
            if (pOption instanceof ArgumentAcceptingOptionSpec)
            {
                ArgumentAcceptingOptionSpec<T> argumentacceptingoptionspec = (ArgumentAcceptingOptionSpec)pOption;
                List<T> list = argumentacceptingoptionspec.defaultValues();

                if (!list.isEmpty())
                {
                    return list.get(0);
                }
            }

            throw throwable;
        }
    }

    private static boolean stringHasValue(@Nullable String pStr)
    {
        return pStr != null && !pStr.isEmpty();
    }

    static
    {
        System.setProperty("java.awt.headless", "true");
    }
}
