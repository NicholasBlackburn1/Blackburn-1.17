package net.minecraft.client;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.optifine.Config;
import net.optifine.CustomColors;
import net.optifine.CustomGuis;
import net.optifine.CustomSky;
import net.optifine.DynamicLights;
import net.optifine.Lang;
import net.optifine.NaturalTextures;
import net.optifine.RandomEntities;
import net.optifine.config.FloatOptions;
import net.optifine.reflect.Reflector;
import net.optifine.shaders.Shaders;
import net.optifine.util.FontUtils;
import net.optifine.util.KeyUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Options
{
    static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();
    private static final TypeToken<List<String>> RESOURCE_PACK_TYPE = new TypeToken<List<String>>()
    {
    };
    public static final int RENDER_DISTANCE_TINY = 2;
    public static final int RENDER_DISTANCE_SHORT = 4;
    public static final int RENDER_DISTANCE_NORMAL = 8;
    public static final int RENDER_DISTANCE_FAR = 12;
    public static final int RENDER_DISTANCE_REALLY_FAR = 16;
    public static final int RENDER_DISTANCE_EXTREME = 32;
    private static final Splitter OPTION_SPLITTER = Splitter.on(':').limit(2);
    private static final float DEFAULT_VOLUME = 1.0F;
    public boolean darkMojangStudiosBackground;
    public double sensitivity = 0.5D;
    public int renderDistance;
    public float entityDistanceScaling = 1.0F;
    public int framerateLimit = 120;
    public CloudStatus renderClouds = CloudStatus.FANCY;
    public GraphicsStatus graphicsMode = GraphicsStatus.FANCY;
    public AmbientOcclusionStatus ambientOcclusion = AmbientOcclusionStatus.MAX;
    public List<String> resourcePacks = Lists.newArrayList();
    public List<String> incompatibleResourcePacks = Lists.newArrayList();
    public ChatVisiblity chatVisibility = ChatVisiblity.FULL;
    public double chatOpacity = 1.0D;
    public double chatLineSpacing;
    public double textBackgroundOpacity = 0.5D;
    @Nullable
    public String fullscreenVideoModeString;
    public boolean hideServerAddress;
    public boolean advancedItemTooltips;
    public boolean pauseOnLostFocus = true;
    private final Set<PlayerModelPart> modelParts = EnumSet.allOf(PlayerModelPart.class);
    public HumanoidArm mainHand = HumanoidArm.RIGHT;
    public int overrideWidth;
    public int overrideHeight;
    public boolean heldItemTooltips = true;
    public double chatScale = 1.0D;
    public double chatWidth = 1.0D;
    public double chatHeightUnfocused = (double)0.44366196F;
    public double chatHeightFocused = 1.0D;
    public double chatDelay;
    public int mipmapLevels = 4;
    private final Object2FloatMap<SoundSource> sourceVolumes = Util.make(new Object2FloatOpenHashMap<>(), (mapIn) ->
    {
        mapIn.defaultReturnValue(1.0F);
    });
    public boolean useNativeTransport = true;
    public AttackIndicatorStatus attackIndicator = AttackIndicatorStatus.CROSSHAIR;
    public TutorialSteps tutorialStep = TutorialSteps.MOVEMENT;
    public boolean joinedFirstServer = false;
    public boolean hideBundleTutorial = false;
    public int biomeBlendRadius = 2;
    public double mouseWheelSensitivity = 1.0D;
    public boolean rawMouseInput = true;
    public int glDebugVerbosity = 1;
    public boolean autoJump = true;
    public boolean autoSuggestions = true;
    public boolean chatColors = true;
    public boolean chatLinks = true;
    public boolean chatLinksPrompt = true;
    public boolean enableVsync = true;
    public boolean entityShadows = true;
    public boolean forceUnicodeFont;
    public boolean invertYMouse;
    public boolean discreteMouseScroll;
    public boolean realmsNotifications = true;
    public boolean reducedDebugInfo;
    public boolean snooperEnabled = true;
    public boolean showSubtitles;
    public boolean backgroundForChatOnly = true;
    public boolean touchscreen;
    public boolean fullscreen;
    public boolean bobView = true;
    public boolean toggleCrouch;
    public boolean toggleSprint;
    public boolean skipMultiplayerWarning;
    public boolean hideMatchedNames = true;
    public final KeyMapping keyUp = new KeyMapping("key.forward", 87, "key.categories.movement");
    public final KeyMapping keyLeft = new KeyMapping("key.left", 65, "key.categories.movement");
    public final KeyMapping keyDown = new KeyMapping("key.back", 83, "key.categories.movement");
    public final KeyMapping keyRight = new KeyMapping("key.right", 68, "key.categories.movement");
    public final KeyMapping keyJump = new KeyMapping("key.jump", 32, "key.categories.movement");
    public final KeyMapping keyShift = new ToggleKeyMapping("key.sneak", 340, "key.categories.movement", () ->
    {
        return this.toggleCrouch;
    });
    public final KeyMapping keySprint = new ToggleKeyMapping("key.sprint", 341, "key.categories.movement", () ->
    {
        return this.toggleSprint;
    });
    public final KeyMapping keyInventory = new KeyMapping("key.inventory", 69, "key.categories.inventory");
    public final KeyMapping keySwapOffhand = new KeyMapping("key.swapOffhand", 70, "key.categories.inventory");
    public final KeyMapping keyDrop = new KeyMapping("key.drop", 81, "key.categories.inventory");
    public final KeyMapping keyUse = new KeyMapping("key.use", InputConstants.Type.MOUSE, 1, "key.categories.gameplay");
    public final KeyMapping keyAttack = new KeyMapping("key.attack", InputConstants.Type.MOUSE, 0, "key.categories.gameplay");
    public final KeyMapping keyPickItem = new KeyMapping("key.pickItem", InputConstants.Type.MOUSE, 2, "key.categories.gameplay");
    public final KeyMapping keyChat = new KeyMapping("key.chat", 84, "key.categories.multiplayer");
    public final KeyMapping keyPlayerList = new KeyMapping("key.playerlist", 258, "key.categories.multiplayer");
    public final KeyMapping keyCommand = new KeyMapping("key.command", 47, "key.categories.multiplayer");
    public final KeyMapping keySocialInteractions = new KeyMapping("key.socialInteractions", 80, "key.categories.multiplayer");
    public final KeyMapping keyScreenshot = new KeyMapping("key.screenshot", 291, "key.categories.misc");
    public final KeyMapping keyTogglePerspective = new KeyMapping("key.togglePerspective", 294, "key.categories.misc");
    public final KeyMapping keySmoothCamera = new KeyMapping("key.smoothCamera", InputConstants.UNKNOWN.getValue(), "key.categories.misc");
    public final KeyMapping keyFullscreen = new KeyMapping("key.fullscreen", 300, "key.categories.misc");
    public final KeyMapping keySpectatorOutlines = new KeyMapping("key.spectatorOutlines", InputConstants.UNKNOWN.getValue(), "key.categories.misc");
    public final KeyMapping keyAdvancements = new KeyMapping("key.advancements", 76, "key.categories.misc");
    public final KeyMapping[] keyHotbarSlots = new KeyMapping[] {new KeyMapping("key.hotbar.1", 49, "key.categories.inventory"), new KeyMapping("key.hotbar.2", 50, "key.categories.inventory"), new KeyMapping("key.hotbar.3", 51, "key.categories.inventory"), new KeyMapping("key.hotbar.4", 52, "key.categories.inventory"), new KeyMapping("key.hotbar.5", 53, "key.categories.inventory"), new KeyMapping("key.hotbar.6", 54, "key.categories.inventory"), new KeyMapping("key.hotbar.7", 55, "key.categories.inventory"), new KeyMapping("key.hotbar.8", 56, "key.categories.inventory"), new KeyMapping("key.hotbar.9", 57, "key.categories.inventory")};
    public final KeyMapping keySaveHotbarActivator = new KeyMapping("key.saveToolbarActivator", 67, "key.categories.creative");
    public final KeyMapping keyLoadHotbarActivator = new KeyMapping("key.loadToolbarActivator", 88, "key.categories.creative");
    public KeyMapping[] keyMappings = ArrayUtils.addAll(new KeyMapping[] {this.keyAttack, this.keyUse, this.keyUp, this.keyLeft, this.keyDown, this.keyRight, this.keyJump, this.keyShift, this.keySprint, this.keyDrop, this.keyInventory, this.keyChat, this.keyPlayerList, this.keyPickItem, this.keyCommand, this.keySocialInteractions, this.keyScreenshot, this.keyTogglePerspective, this.keySmoothCamera, this.keyFullscreen, this.keySpectatorOutlines, this.keySwapOffhand, this.keySaveHotbarActivator, this.keyLoadHotbarActivator, this.keyAdvancements}, this.keyHotbarSlots);
    protected Minecraft minecraft;
    private final File optionsFile;
    public Difficulty difficulty = Difficulty.NORMAL;
    public boolean hideGui;
    private CameraType cameraType = CameraType.FIRST_PERSON;
    public boolean renderDebug;
    public boolean renderDebugCharts;
    public boolean renderFpsChart;
    public String lastMpIp = "";
    public boolean smoothCamera;
    public double fov = 70.0D;
    public float screenEffectScale = 1.0F;
    public float fovEffectScale = 1.0F;
    public double gamma;
    public int guiScale;
    public ParticleStatus particles = ParticleStatus.ALL;
    public NarratorStatus narratorStatus = NarratorStatus.OFF;
    public String languageCode = "en_us";
    public boolean syncWrites;
    public int ofFogType = 1;
    public float ofFogStart = 0.8F;
    public int ofMipmapType = 0;
    public boolean ofOcclusionFancy = false;
    public boolean ofSmoothFps = false;
    public boolean ofSmoothWorld = Config.isSingleProcessor();
    public boolean ofLazyChunkLoading = Config.isSingleProcessor();
    public boolean ofRenderRegions = false;
    public boolean ofSmartAnimations = false;
    public double ofAoLevel = 1.0D;
    public int ofAaLevel = 0;
    public int ofAfLevel = 1;
    public int ofClouds = 0;
    public double ofCloudsHeight = 0.0D;
    public int ofTrees = 0;
    public int ofRain = 0;
    public int ofDroppedItems = 0;
    public int ofBetterGrass = 3;
    public int ofAutoSaveTicks = 4000;
    public boolean ofLagometer = false;
    public boolean ofProfiler = false;
    public boolean ofShowFps = false;
    public boolean ofWeather = true;
    public boolean ofSky = true;
    public boolean ofStars = true;
    public boolean ofSunMoon = true;
    public int ofVignette = 0;
    public int ofChunkUpdates = 1;
    public boolean ofChunkUpdatesDynamic = false;
    public int ofTime = 0;
    public boolean ofBetterSnow = false;
    public boolean ofSwampColors = true;
    public boolean ofRandomEntities = true;
    public boolean ofCustomFonts = true;
    public boolean ofCustomColors = true;
    public boolean ofCustomSky = true;
    public boolean ofShowCapes = true;
    public int ofConnectedTextures = 2;
    public boolean ofCustomItems = true;
    public boolean ofNaturalTextures = false;
    public boolean ofEmissiveTextures = true;
    public boolean ofFastMath = false;
    public boolean ofFastRender = false;
    public int ofTranslucentBlocks = 0;
    public boolean ofDynamicFov = true;
    public boolean ofAlternateBlocks = true;
    public int ofDynamicLights = 3;
    public boolean ofCustomEntityModels = true;
    public boolean ofCustomGuis = true;
    public boolean ofShowGlErrors = true;
    public int ofScreenshotSize = 1;
    public int ofChatBackground = 0;
    public boolean ofChatShadow = true;
    public int ofAnimatedWater = 0;
    public int ofAnimatedLava = 0;
    public boolean ofAnimatedFire = true;
    public boolean ofAnimatedPortal = true;
    public boolean ofAnimatedRedstone = true;
    public boolean ofAnimatedExplosion = true;
    public boolean ofAnimatedFlame = true;
    public boolean ofAnimatedSmoke = true;
    public boolean ofVoidParticles = true;
    public boolean ofWaterParticles = true;
    public boolean ofRainSplash = true;
    public boolean ofPortalParticles = true;
    public boolean ofPotionParticles = true;
    public boolean ofFireworkParticles = true;
    public boolean ofDrippingWaterLava = true;
    public boolean ofAnimatedTerrain = true;
    public boolean ofAnimatedTextures = true;
    public static final int DEFAULT = 0;
    public static final int FAST = 1;
    public static final int FANCY = 2;
    public static final int OFF = 3;
    public static final int SMART = 4;
    public static final int COMPACT = 5;
    public static final int ANIM_ON = 0;
    public static final int ANIM_GENERATED = 1;
    public static final int ANIM_OFF = 2;
    public static final String DEFAULT_STR = "Default";
    public static final double CHAT_WIDTH_SCALE = 4.0571431D;
    private static final int[] OF_TREES_VALUES = new int[] {0, 1, 4, 2};
    private static final int[] OF_DYNAMIC_LIGHTS = new int[] {3, 1, 2};
    private static final String[] KEYS_DYNAMIC_LIGHTS = new String[] {"options.off", "options.graphics.fast", "options.graphics.fancy"};
    public KeyMapping ofKeyBindZoom;
    private File optionsFileOF;
    private boolean loadOptions;
    private boolean saveOptions;

    public Options(Minecraft pMinecraft, File pGameDirectory)
    {
        this.setForgeKeybindProperties();
        this.minecraft = pMinecraft;
        this.optionsFile = new File(pGameDirectory, "options.txt");

        if (pMinecraft.is64Bit() && Runtime.getRuntime().maxMemory() >= 1000000000L)
        {
            Option.RENDER_DISTANCE.setMaxValue(32.0F);
            long i = 1000000L;

            if (Runtime.getRuntime().maxMemory() >= 1500L * i)
            {
                Option.RENDER_DISTANCE.setMaxValue(48.0F);
            }

            if (Runtime.getRuntime().maxMemory() >= 2500L * i)
            {
                Option.RENDER_DISTANCE.setMaxValue(64.0F);
            }
        }
        else
        {
            Option.RENDER_DISTANCE.setMaxValue(16.0F);
        }

        this.renderDistance = pMinecraft.is64Bit() ? 12 : 8;
        this.syncWrites = Util.getPlatform() == Util.OS.WINDOWS;
        this.optionsFileOF = new File(pGameDirectory, "optionsof.txt");
        this.framerateLimit = (int)Option.FRAMERATE_LIMIT.getMaxValue();
        this.ofKeyBindZoom = new KeyMapping("of.key.zoom", 67, "key.categories.misc");
        this.keyMappings = ArrayUtils.add(this.keyMappings, this.ofKeyBindZoom);
        KeyUtils.fixKeyConflicts(this.keyMappings, new KeyMapping[] {this.ofKeyBindZoom});
        this.renderDistance = 8;
        this.load();
        Config.initGameSettings(this);
    }

    public float getBackgroundOpacity(float pOpacity)
    {
        return this.backgroundForChatOnly ? pOpacity : (float)this.textBackgroundOpacity;
    }

    public int getBackgroundColor(float pOpacity)
    {
        return (int)(this.getBackgroundOpacity(pOpacity) * 255.0F) << 24 & -16777216;
    }

    public int getBackgroundColor(int pOpacity)
    {
        return this.backgroundForChatOnly ? pOpacity : (int)(this.textBackgroundOpacity * 255.0D) << 24 & -16777216;
    }

    public void setKey(KeyMapping pKeyBinding, InputConstants.Key pInput)
    {
        pKeyBinding.setKey(pInput);
        this.save();
    }

    private void processOptions(Options.FieldAccess p_168428_)
    {
        this.autoJump = p_168428_.process("autoJump", this.autoJump);
        this.autoSuggestions = p_168428_.process("autoSuggestions", this.autoSuggestions);
        this.chatColors = p_168428_.process("chatColors", this.chatColors);
        this.chatLinks = p_168428_.process("chatLinks", this.chatLinks);
        this.chatLinksPrompt = p_168428_.process("chatLinksPrompt", this.chatLinksPrompt);
        this.enableVsync = p_168428_.process("enableVsync", this.enableVsync);

        if (this.loadOptions)
        {
            if (this.enableVsync)
            {
                this.framerateLimit = (int)Option.FRAMERATE_LIMIT.getMaxValue();
            }

            this.updateVSync();
        }

        this.entityShadows = p_168428_.process("entityShadows", this.entityShadows);
        this.forceUnicodeFont = p_168428_.process("forceUnicodeFont", this.forceUnicodeFont);
        this.discreteMouseScroll = p_168428_.process("discrete_mouse_scroll", this.discreteMouseScroll);
        this.invertYMouse = p_168428_.process("invertYMouse", this.invertYMouse);
        this.realmsNotifications = p_168428_.process("realmsNotifications", this.realmsNotifications);
        this.reducedDebugInfo = p_168428_.process("reducedDebugInfo", this.reducedDebugInfo);
        this.snooperEnabled = p_168428_.process("snooperEnabled", this.snooperEnabled);
        this.showSubtitles = p_168428_.process("showSubtitles", this.showSubtitles);
        this.touchscreen = p_168428_.process("touchscreen", this.touchscreen);
        this.fullscreen = p_168428_.process("fullscreen", this.fullscreen);
        this.bobView = p_168428_.process("bobView", this.bobView);
        this.toggleCrouch = p_168428_.process("toggleCrouch", this.toggleCrouch);
        this.toggleSprint = p_168428_.process("toggleSprint", this.toggleSprint);
        this.darkMojangStudiosBackground = p_168428_.process("darkMojangStudiosBackground", this.darkMojangStudiosBackground);
        this.sensitivity = p_168428_.process("mouseSensitivity", this.sensitivity);
        this.fov = p_168428_.process("fov", (this.fov - 70.0D) / 40.0D) * 40.0D + 70.0D;
        this.screenEffectScale = p_168428_.process("screenEffectScale", this.screenEffectScale);
        this.fovEffectScale = p_168428_.process("fovEffectScale", this.fovEffectScale);
        this.gamma = p_168428_.process("gamma", this.gamma);
        this.renderDistance = p_168428_.process("renderDistance", this.renderDistance);
        this.entityDistanceScaling = p_168428_.process("entityDistanceScaling", this.entityDistanceScaling);
        this.guiScale = p_168428_.process("guiScale", this.guiScale);
        this.particles = p_168428_.process("particles", this.particles, ParticleStatus::byId, ParticleStatus::getId);
        this.framerateLimit = p_168428_.process("maxFps", this.framerateLimit);

        if (this.loadOptions)
        {
            if (this.enableVsync)
            {
                this.framerateLimit = (int)Option.FRAMERATE_LIMIT.getMaxValue();
            }

            if (this.framerateLimit <= 0)
            {
                this.framerateLimit = (int)Option.FRAMERATE_LIMIT.getMaxValue();
            }
        }

        this.difficulty = p_168428_.process("difficulty", this.difficulty, Difficulty::byId, Difficulty::getId);
        this.graphicsMode = p_168428_.process("graphicsMode", this.graphicsMode, GraphicsStatus::byId, GraphicsStatus::getId);

        if (this.loadOptions)
        {
            this.updateRenderClouds();
        }

        this.ambientOcclusion = p_168428_.process("ao", this.ambientOcclusion, Options::readAmbientOcclusion, (aoIn) ->
        {
            return Integer.toString(aoIn.getId());
        });
        this.biomeBlendRadius = p_168428_.process("biomeBlendRadius", this.biomeBlendRadius);
        this.renderClouds = p_168428_.process("renderClouds", this.renderClouds, Options::readCloudStatus, Options::writeCloudStatus);
        this.resourcePacks = p_168428_.process("resourcePacks", this.resourcePacks, Options::readPackList, GSON::toJson);
        this.incompatibleResourcePacks = p_168428_.process("incompatibleResourcePacks", this.incompatibleResourcePacks, Options::readPackList, GSON::toJson);
        this.lastMpIp = p_168428_.process("lastServer", this.lastMpIp);
        this.languageCode = p_168428_.process("lang", this.languageCode);
        this.chatVisibility = p_168428_.process("chatVisibility", this.chatVisibility, ChatVisiblity::byId, ChatVisiblity::getId);
        this.chatOpacity = p_168428_.process("chatOpacity", this.chatOpacity);
        this.chatLineSpacing = p_168428_.process("chatLineSpacing", this.chatLineSpacing);
        this.textBackgroundOpacity = p_168428_.process("textBackgroundOpacity", this.textBackgroundOpacity);
        this.backgroundForChatOnly = p_168428_.process("backgroundForChatOnly", this.backgroundForChatOnly);
        this.hideServerAddress = p_168428_.process("hideServerAddress", this.hideServerAddress);
        this.advancedItemTooltips = p_168428_.process("advancedItemTooltips", this.advancedItemTooltips);
        this.pauseOnLostFocus = p_168428_.process("pauseOnLostFocus", this.pauseOnLostFocus);
        this.overrideWidth = p_168428_.process("overrideWidth", this.overrideWidth);
        this.overrideHeight = p_168428_.process("overrideHeight", this.overrideHeight);
        this.heldItemTooltips = p_168428_.process("heldItemTooltips", this.heldItemTooltips);
        this.chatHeightFocused = p_168428_.process("chatHeightFocused", this.chatHeightFocused);
        this.chatDelay = p_168428_.process("chatDelay", this.chatDelay);
        this.chatHeightUnfocused = p_168428_.process("chatHeightUnfocused", this.chatHeightUnfocused);
        this.chatScale = p_168428_.process("chatScale", this.chatScale);
        this.chatWidth = p_168428_.process("chatWidth", this.chatWidth);
        this.mipmapLevels = p_168428_.process("mipmapLevels", this.mipmapLevels);
        this.useNativeTransport = p_168428_.process("useNativeTransport", this.useNativeTransport);
        this.mainHand = p_168428_.process("mainHand", this.mainHand, Options::readMainHand, Options::writeMainHand);
        this.attackIndicator = p_168428_.process("attackIndicator", this.attackIndicator, AttackIndicatorStatus::byId, AttackIndicatorStatus::getId);
        this.narratorStatus = p_168428_.process("narrator", this.narratorStatus, NarratorStatus::byId, NarratorStatus::getId);
        this.tutorialStep = p_168428_.process("tutorialStep", this.tutorialStep, TutorialSteps::getByName, TutorialSteps::getName);
        this.mouseWheelSensitivity = p_168428_.process("mouseWheelSensitivity", this.mouseWheelSensitivity);
        this.rawMouseInput = p_168428_.process("rawMouseInput", this.rawMouseInput);
        this.glDebugVerbosity = p_168428_.process("glDebugVerbosity", this.glDebugVerbosity);
        this.skipMultiplayerWarning = p_168428_.process("skipMultiplayerWarning", this.skipMultiplayerWarning);
        this.hideMatchedNames = p_168428_.process("hideMatchedNames", this.hideMatchedNames);
        this.joinedFirstServer = p_168428_.process("joinedFirstServer", this.joinedFirstServer);
        this.hideBundleTutorial = p_168428_.process("hideBundleTutorial", this.hideBundleTutorial);
        this.syncWrites = p_168428_.process("syncChunkWrites", this.syncWrites);

        for (KeyMapping keymapping : this.keyMappings)
        {
            String s = keymapping.saveString();
            String s1 = p_168428_.process("key_" + keymapping.getName(), s);

            if (Reflector.ForgeKeyBinding_getKeyModifier.exists())
            {
                Object object = Reflector.call(keymapping, Reflector.ForgeKeyBinding_getKeyModifier);
                Object object1 = Reflector.getFieldValue(Reflector.KeyModifier_NONE);
                s = keymapping.saveString() + (object != object1 ? ":" + object : "");
            }

            if (!s.equals(s1))
            {
                if (Reflector.KeyModifier_valueFromString.exists())
                {
                    if (s1.indexOf(58) != -1)
                    {
                        String[] astring = s1.split(":");
                        Object object3 = Reflector.call(Reflector.KeyModifier_valueFromString, astring[1]);
                        Reflector.call(keymapping, Reflector.ForgeKeyBinding_setKeyModifierAndCode, object3, InputConstants.getKey(astring[0]));
                    }
                    else
                    {
                        Object object2 = Reflector.getFieldValue(Reflector.KeyModifier_NONE);
                        Reflector.call(keymapping, Reflector.ForgeKeyBinding_setKeyModifierAndCode, object2, InputConstants.getKey(s1));
                    }
                }
                else
                {
                    keymapping.setKey(InputConstants.getKey(s1));
                }
            }
        }

        for (SoundSource soundsource : SoundSource.values())
        {
            this.sourceVolumes.computeFloat(soundsource, (sourceIn, levelIn) ->
            {
                return p_168428_.process("soundCategory_" + sourceIn.getName(), levelIn != null ? levelIn : 1.0F);
            });
        }

        for (PlayerModelPart playermodelpart : PlayerModelPart.values())
        {
            boolean flag = this.modelParts.contains(playermodelpart);
            boolean flag1 = p_168428_.process("modelPart_" + playermodelpart.getId(), flag);

            if (flag1 != flag)
            {
                this.setModelPart(playermodelpart, flag1);
            }
        }
    }

    public void load()
    {
        this.loadOptions = true;

        try
        {
            if (!this.optionsFile.exists())
            {
                return;
            }

            this.sourceVolumes.clear();
            CompoundTag compoundtag = new CompoundTag();
            BufferedReader bufferedreader = Files.newReader(this.optionsFile, Charsets.UTF_8);

            try
            {
                bufferedreader.lines().forEach((lineIn) ->
                {
                    try {
                        Iterator<String> iterator = OPTION_SPLITTER.split(lineIn).iterator();
                        compoundtag.putString(iterator.next(), iterator.next());
                    }
                    catch (Exception exception11)
                    {
                        LOGGER.warn("Skipping bad option: {}", (Object)lineIn);
                    }
                });
            }
            catch (Throwable throwable1)
            {
                if (bufferedreader != null)
                {
                    try
                    {
                        bufferedreader.close();
                    }
                    catch (Throwable throwable)
                    {
                        throwable1.addSuppressed(throwable);
                    }
                }

                throw throwable1;
            }

            if (bufferedreader != null)
            {
                bufferedreader.close();
            }

            final CompoundTag compoundtag1 = this.dataFix(compoundtag);

            if (!compoundtag1.contains("graphicsMode") && compoundtag1.contains("fancyGraphics"))
            {
                if (isTrue(compoundtag1.getString("fancyGraphics")))
                {
                    this.graphicsMode = GraphicsStatus.FANCY;
                }
                else
                {
                    this.graphicsMode = GraphicsStatus.FAST;
                }
            }

            this.processOptions(new Options.FieldAccess()
            {
                @Nullable
                private String getValueOrNull(String p_168459_)
                {
                    return compoundtag1.contains(p_168459_) ? compoundtag1.getString(p_168459_) : null;
                }
                public int process(String p_168467_, int p_168468_)
                {
                    String s = this.getValueOrNull(p_168467_);

                    if (s != null)
                    {
                        try
                        {
                            return Integer.parseInt(s);
                        }
                        catch (NumberFormatException numberformatexception)
                        {
                            Options.LOGGER.warn("Invalid integer value for option {} = {}", p_168467_, s, numberformatexception);
                        }
                    }

                    return p_168468_;
                }
                public boolean process(String p_168483_, boolean p_168484_)
                {
                    String s = this.getValueOrNull(p_168483_);
                    return s != null ? Options.isTrue(s) : p_168484_;
                }
                public String process(String p_168480_, String p_168481_)
                {
                    return MoreObjects.firstNonNull(this.getValueOrNull(p_168480_), p_168481_);
                }
                public double process(String p_168461_, double p_168462_)
                {
                    String s = this.getValueOrNull(p_168461_);

                    if (s != null)
                    {
                        if (Options.isTrue(s))
                        {
                            return 1.0D;
                        }

                        if (Options.isFalse(s))
                        {
                            return 0.0D;
                        }

                        try
                        {
                            return Double.parseDouble(s);
                        }
                        catch (NumberFormatException numberformatexception)
                        {
                            Options.LOGGER.warn("Invalid floating point value for option {} = {}", p_168461_, s, numberformatexception);
                        }
                    }

                    return p_168462_;
                }
                public float process(String p_168464_, float p_168465_)
                {
                    String s = this.getValueOrNull(p_168464_);

                    if (s != null)
                    {
                        if (Options.isTrue(s))
                        {
                            return 1.0F;
                        }

                        if (Options.isFalse(s))
                        {
                            return 0.0F;
                        }

                        try
                        {
                            return Float.parseFloat(s);
                        }
                        catch (NumberFormatException numberformatexception)
                        {
                            Options.LOGGER.warn("Invalid floating point value for option {} = {}", p_168464_, s, numberformatexception);
                        }
                    }

                    return p_168465_;
                }
                public <T> T process(String p_168470_, T p_168471_, Function<String, T> p_168472_, Function<T, String> p_168473_)
                {
                    String s = this.getValueOrNull(p_168470_);
                    return (T)(s == null ? p_168471_ : p_168472_.apply(s));
                }
                public <T> T process(String p_168475_, T p_168476_, IntFunction<T> p_168477_, ToIntFunction<T> p_168478_)
                {
                    String s = this.getValueOrNull(p_168475_);

                    if (s != null)
                    {
                        try
                        {
                            return p_168477_.apply(Integer.parseInt(s));
                        }
                        catch (Exception exception1)
                        {
                            Options.LOGGER.warn("Invalid integer value for option {} = {}", p_168475_, s, exception1);
                        }
                    }

                    return p_168476_;
                }
            });

            if (compoundtag1.contains("fullscreenResolution"))
            {
                this.fullscreenVideoModeString = compoundtag1.getString("fullscreenResolution");
            }

            if (this.minecraft.getWindow() != null)
            {
                this.minecraft.getWindow().setFramerateLimit(this.framerateLimit);
            }

            KeyMapping.resetMapping();
        }
        catch (Exception exception1)
        {
            LOGGER.error("Failed to load options", (Throwable)exception1);
        }

        this.loadOptions = false;
        this.loadOfOptions();
    }

    static boolean isTrue(String p_168436_)
    {
        return "true".equals(p_168436_);
    }

    static boolean isFalse(String p_168441_)
    {
        return "false".equals(p_168441_);
    }

    private CompoundTag dataFix(CompoundTag pNbt)
    {
        int i = 0;

        try
        {
            i = Integer.parseInt(pNbt.getString("version"));
        }
        catch (RuntimeException runtimeexception)
        {
        }

        return NbtUtils.update(this.minecraft.getFixerUpper(), DataFixTypes.OPTIONS, pNbt, i);
    }

    public void save()
    {
        this.saveOptions = true;

        if (!Reflector.ClientModLoader_isLoading.exists() || !Reflector.callBoolean(Reflector.ClientModLoader_isLoading))
        {
            try
            {
                final PrintWriter printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.optionsFile), StandardCharsets.UTF_8));

                try
                {
                    printwriter.println("version:" + SharedConstants.getCurrentVersion().getWorldVersion());
                    this.processOptions(new Options.FieldAccess()
                    {
                        public void writePrefix(String p_168491_)
                        {
                            printwriter.print(p_168491_);
                            printwriter.print(':');
                        }
                        public int process(String p_168499_, int p_168500_)
                        {
                            this.writePrefix(p_168499_);
                            printwriter.println(p_168500_);
                            return p_168500_;
                        }
                        public boolean process(String p_168515_, boolean p_168516_)
                        {
                            this.writePrefix(p_168515_);
                            printwriter.println(p_168516_);
                            return p_168516_;
                        }
                        public String process(String p_168512_, String p_168513_)
                        {
                            this.writePrefix(p_168512_);
                            printwriter.println(p_168513_);
                            return p_168513_;
                        }
                        public double process(String p_168493_, double p_168494_)
                        {
                            this.writePrefix(p_168493_);
                            printwriter.println(p_168494_);
                            return p_168494_;
                        }
                        public float process(String p_168496_, float p_168497_)
                        {
                            this.writePrefix(p_168496_);
                            printwriter.println(p_168497_);
                            return p_168497_;
                        }
                        public <T> T process(String p_168502_, T p_168503_, Function<String, T> p_168504_, Function<T, String> p_168505_)
                        {
                            this.writePrefix(p_168502_);
                            printwriter.println(p_168505_.apply(p_168503_));
                            return p_168503_;
                        }
                        public <T> T process(String p_168507_, T p_168508_, IntFunction<T> p_168509_, ToIntFunction<T> p_168510_)
                        {
                            this.writePrefix(p_168507_);
                            printwriter.println(p_168510_.applyAsInt(p_168508_));
                            return p_168508_;
                        }
                    });

                    if (this.minecraft.getWindow().getPreferredFullscreenVideoMode().isPresent())
                    {
                        printwriter.println("fullscreenResolution:" + this.minecraft.getWindow().getPreferredFullscreenVideoMode().get().write());
                    }
                }
                catch (Throwable throwable1)
                {
                    try
                    {
                        printwriter.close();
                    }
                    catch (Throwable throwable)
                    {
                        throwable1.addSuppressed(throwable);
                    }

                    throw throwable1;
                }

                printwriter.close();
            }
            catch (Exception exception1)
            {
                LOGGER.error("Failed to save options", (Throwable)exception1);
            }

            this.saveOptions = false;
            this.saveOfOptions();
            this.broadcastOptions();
        }
    }

    public float getSoundSourceVolume(SoundSource pCategory)
    {
        return this.sourceVolumes.getFloat(pCategory);
    }

    public void setSoundCategoryVolume(SoundSource pCategory, float pVolume)
    {
        this.sourceVolumes.put(pCategory, pVolume);
        this.minecraft.getSoundManager().updateSourceVolume(pCategory, pVolume);
    }

    public void broadcastOptions()
    {
        if (this.minecraft.player != null)
        {
            int i = 0;

            for (PlayerModelPart playermodelpart : this.modelParts)
            {
                i |= playermodelpart.getMask();
            }

            this.minecraft.player.connection.send(new ServerboundClientInformationPacket(this.languageCode, this.renderDistance, this.chatVisibility, this.chatColors, i, this.mainHand, this.minecraft.isTextFilteringEnabled()));
        }
    }

    private void setModelPart(PlayerModelPart pModelPart, boolean pEnable)
    {
        if (pEnable)
        {
            this.modelParts.add(pModelPart);
        }
        else
        {
            this.modelParts.remove(pModelPart);
        }
    }

    public boolean isModelPartEnabled(PlayerModelPart p_168417_)
    {
        return this.modelParts.contains(p_168417_);
    }

    public void toggleModelPart(PlayerModelPart p_168419_, boolean p_168420_)
    {
        this.setModelPart(p_168419_, p_168420_);
        this.broadcastOptions();
    }

    public CloudStatus getCloudsType()
    {
        return this.renderDistance >= 4 ? this.renderClouds : CloudStatus.OFF;
    }

    public boolean useNativeTransport()
    {
        return this.useNativeTransport;
    }

    public void setOptionFloatValueOF(Option option, double val)
    {
        if (option == Option.CLOUD_HEIGHT)
        {
            this.ofCloudsHeight = val;
        }

        if (option == Option.AO_LEVEL)
        {
            this.ofAoLevel = val;
            this.minecraft.levelRenderer.allChanged();
        }

        if (option == Option.AA_LEVEL)
        {
            int i = (int)val;

            if (i > 0 && Config.isShaders())
            {
                Config.showGuiMessage(Lang.get("of.message.aa.shaders1"), Lang.get("of.message.aa.shaders2"));
                return;
            }

            if (i > 0 && Config.isGraphicsFabulous())
            {
                Config.showGuiMessage(Lang.get("of.message.aa.gf1"), Lang.get("of.message.aa.gf2"));
                return;
            }

            this.ofAaLevel = i;
            this.ofAaLevel = Config.limit(this.ofAaLevel, 0, 16);
        }

        if (option == Option.AF_LEVEL)
        {
            int j = (int)val;
            this.ofAfLevel = j;
            this.ofAfLevel = Config.limit(this.ofAfLevel, 1, 16);
            this.minecraft.delayTextureReload();
            Shaders.uninit();
        }

        if (option == Option.MIPMAP_TYPE)
        {
            int k = (int)val;
            this.ofMipmapType = Config.limit(k, 0, 3);
            this.updateMipmaps();
        }
    }

    public double getOptionFloatValueOF(Option settingOption)
    {
        if (settingOption == Option.CLOUD_HEIGHT)
        {
            return this.ofCloudsHeight;
        }
        else if (settingOption == Option.AO_LEVEL)
        {
            return this.ofAoLevel;
        }
        else if (settingOption == Option.AA_LEVEL)
        {
            return (double)this.ofAaLevel;
        }
        else if (settingOption == Option.AF_LEVEL)
        {
            return (double)this.ofAfLevel;
        }
        else if (settingOption == Option.MIPMAP_TYPE)
        {
            return (double)this.ofMipmapType;
        }
        else if (settingOption == Option.FRAMERATE_LIMIT)
        {
            return (double)this.framerateLimit == Option.FRAMERATE_LIMIT.getMaxValue() && this.enableVsync ? 0.0D : (double)this.framerateLimit;
        }
        else
        {
            return (double)Float.MAX_VALUE;
        }
    }

    public void setOptionValueOF(Option par1EnumOptions, int par2)
    {
        if (par1EnumOptions == Option.FOG_FANCY)
        {
            switch (this.ofFogType)
            {
                case 2:
                    this.ofFogType = 3;
                    break;

                default:
                    this.ofFogType = 2;
            }
        }

        if (par1EnumOptions == Option.FOG_START)
        {
            this.ofFogStart += 0.2F;

            if (this.ofFogStart > 0.81F)
            {
                this.ofFogStart = 0.2F;
            }
        }

        if (par1EnumOptions == Option.SMOOTH_FPS)
        {
            this.ofSmoothFps = !this.ofSmoothFps;
        }

        if (par1EnumOptions == Option.SMOOTH_WORLD)
        {
            this.ofSmoothWorld = !this.ofSmoothWorld;
            Config.updateThreadPriorities();
        }

        if (par1EnumOptions == Option.CLOUDS)
        {
            ++this.ofClouds;

            if (this.ofClouds > 3)
            {
                this.ofClouds = 0;
            }

            this.updateRenderClouds();
        }

        if (par1EnumOptions == Option.TREES)
        {
            this.ofTrees = nextValue(this.ofTrees, OF_TREES_VALUES);
            this.minecraft.levelRenderer.allChanged();
        }

        if (par1EnumOptions == Option.DROPPED_ITEMS)
        {
            ++this.ofDroppedItems;

            if (this.ofDroppedItems > 2)
            {
                this.ofDroppedItems = 0;
            }
        }

        if (par1EnumOptions == Option.RAIN)
        {
            ++this.ofRain;

            if (this.ofRain > 3)
            {
                this.ofRain = 0;
            }
        }

        if (par1EnumOptions == Option.ANIMATED_WATER)
        {
            ++this.ofAnimatedWater;

            if (this.ofAnimatedWater == 1)
            {
                ++this.ofAnimatedWater;
            }

            if (this.ofAnimatedWater > 2)
            {
                this.ofAnimatedWater = 0;
            }
        }

        if (par1EnumOptions == Option.ANIMATED_LAVA)
        {
            ++this.ofAnimatedLava;

            if (this.ofAnimatedLava == 1)
            {
                ++this.ofAnimatedLava;
            }

            if (this.ofAnimatedLava > 2)
            {
                this.ofAnimatedLava = 0;
            }
        }

        if (par1EnumOptions == Option.ANIMATED_FIRE)
        {
            this.ofAnimatedFire = !this.ofAnimatedFire;
        }

        if (par1EnumOptions == Option.ANIMATED_PORTAL)
        {
            this.ofAnimatedPortal = !this.ofAnimatedPortal;
        }

        if (par1EnumOptions == Option.ANIMATED_REDSTONE)
        {
            this.ofAnimatedRedstone = !this.ofAnimatedRedstone;
        }

        if (par1EnumOptions == Option.ANIMATED_EXPLOSION)
        {
            this.ofAnimatedExplosion = !this.ofAnimatedExplosion;
        }

        if (par1EnumOptions == Option.ANIMATED_FLAME)
        {
            this.ofAnimatedFlame = !this.ofAnimatedFlame;
        }

        if (par1EnumOptions == Option.ANIMATED_SMOKE)
        {
            this.ofAnimatedSmoke = !this.ofAnimatedSmoke;
        }

        if (par1EnumOptions == Option.VOID_PARTICLES)
        {
            this.ofVoidParticles = !this.ofVoidParticles;
        }

        if (par1EnumOptions == Option.WATER_PARTICLES)
        {
            this.ofWaterParticles = !this.ofWaterParticles;
        }

        if (par1EnumOptions == Option.PORTAL_PARTICLES)
        {
            this.ofPortalParticles = !this.ofPortalParticles;
        }

        if (par1EnumOptions == Option.POTION_PARTICLES)
        {
            this.ofPotionParticles = !this.ofPotionParticles;
        }

        if (par1EnumOptions == Option.FIREWORK_PARTICLES)
        {
            this.ofFireworkParticles = !this.ofFireworkParticles;
        }

        if (par1EnumOptions == Option.DRIPPING_WATER_LAVA)
        {
            this.ofDrippingWaterLava = !this.ofDrippingWaterLava;
        }

        if (par1EnumOptions == Option.ANIMATED_TERRAIN)
        {
            this.ofAnimatedTerrain = !this.ofAnimatedTerrain;
        }

        if (par1EnumOptions == Option.ANIMATED_TEXTURES)
        {
            this.ofAnimatedTextures = !this.ofAnimatedTextures;
        }

        if (par1EnumOptions == Option.RAIN_SPLASH)
        {
            this.ofRainSplash = !this.ofRainSplash;
        }

        if (par1EnumOptions == Option.LAGOMETER)
        {
            this.ofLagometer = !this.ofLagometer;
        }

        if (par1EnumOptions == Option.SHOW_FPS)
        {
            this.ofShowFps = !this.ofShowFps;
        }

        if (par1EnumOptions == Option.AUTOSAVE_TICKS)
        {
            int i = 900;
            this.ofAutoSaveTicks = Math.max(this.ofAutoSaveTicks / i * i, i);
            this.ofAutoSaveTicks *= 2;

            if (this.ofAutoSaveTicks > 32 * i)
            {
                this.ofAutoSaveTicks = i;
            }
        }

        if (par1EnumOptions == Option.BETTER_GRASS)
        {
            ++this.ofBetterGrass;

            if (this.ofBetterGrass > 3)
            {
                this.ofBetterGrass = 1;
            }

            this.minecraft.levelRenderer.allChanged();
        }

        if (par1EnumOptions == Option.CONNECTED_TEXTURES)
        {
            ++this.ofConnectedTextures;

            if (this.ofConnectedTextures > 3)
            {
                this.ofConnectedTextures = 1;
            }

            if (this.ofConnectedTextures == 2)
            {
                this.minecraft.levelRenderer.allChanged();
            }
            else
            {
                this.minecraft.delayTextureReload();
            }
        }

        if (par1EnumOptions == Option.WEATHER)
        {
            this.ofWeather = !this.ofWeather;
        }

        if (par1EnumOptions == Option.SKY)
        {
            this.ofSky = !this.ofSky;
        }

        if (par1EnumOptions == Option.STARS)
        {
            this.ofStars = !this.ofStars;
        }

        if (par1EnumOptions == Option.SUN_MOON)
        {
            this.ofSunMoon = !this.ofSunMoon;
        }

        if (par1EnumOptions == Option.VIGNETTE)
        {
            ++this.ofVignette;

            if (this.ofVignette > 2)
            {
                this.ofVignette = 0;
            }
        }

        if (par1EnumOptions == Option.CHUNK_UPDATES)
        {
            ++this.ofChunkUpdates;

            if (this.ofChunkUpdates > 5)
            {
                this.ofChunkUpdates = 1;
            }
        }

        if (par1EnumOptions == Option.CHUNK_UPDATES_DYNAMIC)
        {
            this.ofChunkUpdatesDynamic = !this.ofChunkUpdatesDynamic;
        }

        if (par1EnumOptions == Option.TIME)
        {
            ++this.ofTime;

            if (this.ofTime > 2)
            {
                this.ofTime = 0;
            }
        }

        if (par1EnumOptions == Option.PROFILER)
        {
            this.ofProfiler = !this.ofProfiler;
        }

        if (par1EnumOptions == Option.BETTER_SNOW)
        {
            this.ofBetterSnow = !this.ofBetterSnow;
            this.minecraft.levelRenderer.allChanged();
        }

        if (par1EnumOptions == Option.SWAMP_COLORS)
        {
            this.ofSwampColors = !this.ofSwampColors;
            CustomColors.updateUseDefaultGrassFoliageColors();
            this.minecraft.levelRenderer.allChanged();
        }

        if (par1EnumOptions == Option.RANDOM_ENTITIES)
        {
            this.ofRandomEntities = !this.ofRandomEntities;
            RandomEntities.update();
        }

        if (par1EnumOptions == Option.CUSTOM_FONTS)
        {
            this.ofCustomFonts = !this.ofCustomFonts;
            FontUtils.reloadFonts();
        }

        if (par1EnumOptions == Option.CUSTOM_COLORS)
        {
            this.ofCustomColors = !this.ofCustomColors;
            CustomColors.update();
            this.minecraft.levelRenderer.allChanged();
        }

        if (par1EnumOptions == Option.CUSTOM_ITEMS)
        {
            this.ofCustomItems = !this.ofCustomItems;
            this.minecraft.delayTextureReload();
        }

        if (par1EnumOptions == Option.CUSTOM_SKY)
        {
            this.ofCustomSky = !this.ofCustomSky;
            CustomSky.update();
        }

        if (par1EnumOptions == Option.SHOW_CAPES)
        {
            this.ofShowCapes = !this.ofShowCapes;
        }

        if (par1EnumOptions == Option.NATURAL_TEXTURES)
        {
            this.ofNaturalTextures = !this.ofNaturalTextures;
            NaturalTextures.update();
            this.minecraft.levelRenderer.allChanged();
        }

        if (par1EnumOptions == Option.EMISSIVE_TEXTURES)
        {
            this.ofEmissiveTextures = !this.ofEmissiveTextures;
            this.minecraft.delayTextureReload();
        }

        if (par1EnumOptions == Option.FAST_MATH)
        {
            this.ofFastMath = !this.ofFastMath;
            Mth.fastMath = this.ofFastMath;
        }

        if (par1EnumOptions == Option.FAST_RENDER)
        {
            this.ofFastRender = !this.ofFastRender;
        }

        if (par1EnumOptions == Option.TRANSLUCENT_BLOCKS)
        {
            if (this.ofTranslucentBlocks == 0)
            {
                this.ofTranslucentBlocks = 1;
            }
            else if (this.ofTranslucentBlocks == 1)
            {
                this.ofTranslucentBlocks = 2;
            }
            else if (this.ofTranslucentBlocks == 2)
            {
                this.ofTranslucentBlocks = 0;
            }
            else
            {
                this.ofTranslucentBlocks = 0;
            }

            this.minecraft.levelRenderer.allChanged();
        }

        if (par1EnumOptions == Option.LAZY_CHUNK_LOADING)
        {
            this.ofLazyChunkLoading = !this.ofLazyChunkLoading;
        }

        if (par1EnumOptions == Option.RENDER_REGIONS)
        {
            this.ofRenderRegions = !this.ofRenderRegions;
            this.minecraft.levelRenderer.allChanged();
        }

        if (par1EnumOptions == Option.SMART_ANIMATIONS)
        {
            this.ofSmartAnimations = !this.ofSmartAnimations;
            this.minecraft.levelRenderer.allChanged();
        }

        if (par1EnumOptions == Option.DYNAMIC_FOV)
        {
            this.ofDynamicFov = !this.ofDynamicFov;
        }

        if (par1EnumOptions == Option.ALTERNATE_BLOCKS)
        {
            this.ofAlternateBlocks = !this.ofAlternateBlocks;
            this.minecraft.levelRenderer.allChanged();
        }

        if (par1EnumOptions == Option.DYNAMIC_LIGHTS)
        {
            this.ofDynamicLights = nextValue(this.ofDynamicLights, OF_DYNAMIC_LIGHTS);
            DynamicLights.removeLights(this.minecraft.levelRenderer);
        }

        if (par1EnumOptions == Option.SCREENSHOT_SIZE)
        {
            ++this.ofScreenshotSize;

            if (this.ofScreenshotSize > 4)
            {
                this.ofScreenshotSize = 1;
            }
        }

        if (par1EnumOptions == Option.CUSTOM_ENTITY_MODELS)
        {
            this.ofCustomEntityModels = !this.ofCustomEntityModels;
            this.minecraft.delayTextureReload();
        }

        if (par1EnumOptions == Option.CUSTOM_GUIS)
        {
            this.ofCustomGuis = !this.ofCustomGuis;
            CustomGuis.update();
        }

        if (par1EnumOptions == Option.SHOW_GL_ERRORS)
        {
            this.ofShowGlErrors = !this.ofShowGlErrors;
        }

        if (par1EnumOptions == Option.HELD_ITEM_TOOLTIPS)
        {
            this.heldItemTooltips = !this.heldItemTooltips;
        }

        if (par1EnumOptions == Option.ADVANCED_TOOLTIPS)
        {
            this.advancedItemTooltips = !this.advancedItemTooltips;
        }

        if (par1EnumOptions == Option.CHAT_BACKGROUND)
        {
            if (this.ofChatBackground == 0)
            {
                this.ofChatBackground = 5;
            }
            else if (this.ofChatBackground == 5)
            {
                this.ofChatBackground = 3;
            }
            else
            {
                this.ofChatBackground = 0;
            }
        }

        if (par1EnumOptions == Option.CHAT_SHADOW)
        {
            this.ofChatShadow = !this.ofChatShadow;
        }
    }

    public Component getKeyComponentOF(Option option)
    {
        String s = this.getKeyBindingOF(option);
        Component component = new TextComponent(s);
        return component;
    }

    public String getKeyBindingOF(Option par1EnumOptions)
    {
        String s = I18n.m_118938_(par1EnumOptions.getResourceKey()) + ": ";

        if (s == null)
        {
            s = par1EnumOptions.getResourceKey();
        }

        if (par1EnumOptions == Option.RENDER_DISTANCE)
        {
            int i1 = (int)Option.RENDER_DISTANCE.get(this);
            String s2 = I18n.m_118938_("of.options.renderDistance.tiny");
            int i = 2;

            if (i1 >= 4)
            {
                s2 = I18n.m_118938_("of.options.renderDistance.short");
                i = 4;
            }

            if (i1 >= 8)
            {
                s2 = I18n.m_118938_("of.options.renderDistance.normal");
                i = 8;
            }

            if (i1 >= 16)
            {
                s2 = I18n.m_118938_("of.options.renderDistance.far");
                i = 16;
            }

            if (i1 >= 32)
            {
                s2 = Lang.get("of.options.renderDistance.extreme");
                i = 32;
            }

            if (i1 >= 48)
            {
                s2 = Lang.get("of.options.renderDistance.insane");
                i = 48;
            }

            if (i1 >= 64)
            {
                s2 = Lang.get("of.options.renderDistance.ludicrous");
                i = 64;
            }

            int j = this.renderDistance - i;
            String s1 = s2;

            if (j > 0)
            {
                s1 = s2 + "+";
            }

            return s + i1 + " " + s1 + "";
        }
        else if (par1EnumOptions == Option.FOG_FANCY)
        {
            switch (this.ofFogType)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                case 3:
                    return s + Lang.getOff();

                default:
                    return s + Lang.getOff();
            }
        }
        else if (par1EnumOptions == Option.FOG_START)
        {
            return s + this.ofFogStart;
        }
        else if (par1EnumOptions == Option.MIPMAP_TYPE)
        {
            return FloatOptions.getText(par1EnumOptions, (double)this.ofMipmapType);
        }
        else if (par1EnumOptions == Option.SMOOTH_FPS)
        {
            return this.ofSmoothFps ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.SMOOTH_WORLD)
        {
            return this.ofSmoothWorld ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.CLOUDS)
        {
            switch (this.ofClouds)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                case 3:
                    return s + Lang.getOff();

                default:
                    return s + Lang.getDefault();
            }
        }
        else if (par1EnumOptions == Option.TREES)
        {
            switch (this.ofTrees)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                case 3:
                default:
                    return s + Lang.getDefault();

                case 4:
                    return s + Lang.get("of.general.smart");
            }
        }
        else if (par1EnumOptions == Option.DROPPED_ITEMS)
        {
            switch (this.ofDroppedItems)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                default:
                    return s + Lang.getDefault();
            }
        }
        else if (par1EnumOptions == Option.RAIN)
        {
            switch (this.ofRain)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                case 3:
                    return s + Lang.getOff();

                default:
                    return s + Lang.getDefault();
            }
        }
        else if (par1EnumOptions == Option.ANIMATED_WATER)
        {
            switch (this.ofAnimatedWater)
            {
                case 1:
                    return s + Lang.get("of.options.animation.dynamic");

                case 2:
                    return s + Lang.getOff();

                default:
                    return s + Lang.getOn();
            }
        }
        else if (par1EnumOptions == Option.ANIMATED_LAVA)
        {
            switch (this.ofAnimatedLava)
            {
                case 1:
                    return s + Lang.get("of.options.animation.dynamic");

                case 2:
                    return s + Lang.getOff();

                default:
                    return s + Lang.getOn();
            }
        }
        else if (par1EnumOptions == Option.ANIMATED_FIRE)
        {
            return this.ofAnimatedFire ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.ANIMATED_PORTAL)
        {
            return this.ofAnimatedPortal ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.ANIMATED_REDSTONE)
        {
            return this.ofAnimatedRedstone ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.ANIMATED_EXPLOSION)
        {
            return this.ofAnimatedExplosion ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.ANIMATED_FLAME)
        {
            return this.ofAnimatedFlame ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.ANIMATED_SMOKE)
        {
            return this.ofAnimatedSmoke ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.VOID_PARTICLES)
        {
            return this.ofVoidParticles ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.WATER_PARTICLES)
        {
            return this.ofWaterParticles ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.PORTAL_PARTICLES)
        {
            return this.ofPortalParticles ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.POTION_PARTICLES)
        {
            return this.ofPotionParticles ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.FIREWORK_PARTICLES)
        {
            return this.ofFireworkParticles ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.DRIPPING_WATER_LAVA)
        {
            return this.ofDrippingWaterLava ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.ANIMATED_TERRAIN)
        {
            return this.ofAnimatedTerrain ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.ANIMATED_TEXTURES)
        {
            return this.ofAnimatedTextures ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.RAIN_SPLASH)
        {
            return this.ofRainSplash ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.LAGOMETER)
        {
            return this.ofLagometer ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.SHOW_FPS)
        {
            return this.ofShowFps ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.AUTOSAVE_TICKS)
        {
            int l = 900;

            if (this.ofAutoSaveTicks <= l)
            {
                return s + Lang.get("of.options.save.45s");
            }
            else if (this.ofAutoSaveTicks <= 2 * l)
            {
                return s + Lang.get("of.options.save.90s");
            }
            else if (this.ofAutoSaveTicks <= 4 * l)
            {
                return s + Lang.get("of.options.save.3min");
            }
            else if (this.ofAutoSaveTicks <= 8 * l)
            {
                return s + Lang.get("of.options.save.6min");
            }
            else
            {
                return this.ofAutoSaveTicks <= 16 * l ? s + Lang.get("of.options.save.12min") : s + Lang.get("of.options.save.24min");
            }
        }
        else if (par1EnumOptions == Option.BETTER_GRASS)
        {
            switch (this.ofBetterGrass)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                default:
                    return s + Lang.getOff();
            }
        }
        else if (par1EnumOptions == Option.CONNECTED_TEXTURES)
        {
            switch (this.ofConnectedTextures)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                default:
                    return s + Lang.getOff();
            }
        }
        else if (par1EnumOptions == Option.WEATHER)
        {
            return this.ofWeather ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.SKY)
        {
            return this.ofSky ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.STARS)
        {
            return this.ofStars ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.SUN_MOON)
        {
            return this.ofSunMoon ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.VIGNETTE)
        {
            switch (this.ofVignette)
            {
                case 1:
                    return s + Lang.getFast();

                case 2:
                    return s + Lang.getFancy();

                default:
                    return s + Lang.getDefault();
            }
        }
        else if (par1EnumOptions == Option.CHUNK_UPDATES)
        {
            return s + this.ofChunkUpdates;
        }
        else if (par1EnumOptions == Option.CHUNK_UPDATES_DYNAMIC)
        {
            return this.ofChunkUpdatesDynamic ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.TIME)
        {
            if (this.ofTime == 1)
            {
                return s + Lang.get("of.options.time.dayOnly");
            }
            else
            {
                return this.ofTime == 2 ? s + Lang.get("of.options.time.nightOnly") : s + Lang.getDefault();
            }
        }
        else if (par1EnumOptions == Option.AA_LEVEL)
        {
            return FloatOptions.getText(par1EnumOptions, (double)this.ofAaLevel);
        }
        else if (par1EnumOptions == Option.AF_LEVEL)
        {
            return FloatOptions.getText(par1EnumOptions, (double)this.ofAfLevel);
        }
        else if (par1EnumOptions == Option.PROFILER)
        {
            return this.ofProfiler ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.BETTER_SNOW)
        {
            return this.ofBetterSnow ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.SWAMP_COLORS)
        {
            return this.ofSwampColors ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.RANDOM_ENTITIES)
        {
            return this.ofRandomEntities ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.CUSTOM_FONTS)
        {
            return this.ofCustomFonts ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.CUSTOM_COLORS)
        {
            return this.ofCustomColors ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.CUSTOM_SKY)
        {
            return this.ofCustomSky ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.SHOW_CAPES)
        {
            return this.ofShowCapes ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.CUSTOM_ITEMS)
        {
            return this.ofCustomItems ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.NATURAL_TEXTURES)
        {
            return this.ofNaturalTextures ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.EMISSIVE_TEXTURES)
        {
            return this.ofEmissiveTextures ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.FAST_MATH)
        {
            return this.ofFastMath ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.FAST_RENDER)
        {
            return this.ofFastRender ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.TRANSLUCENT_BLOCKS)
        {
            if (this.ofTranslucentBlocks == 1)
            {
                return s + Lang.getFast();
            }
            else
            {
                return this.ofTranslucentBlocks == 2 ? s + Lang.getFancy() : s + Lang.getDefault();
            }
        }
        else if (par1EnumOptions == Option.LAZY_CHUNK_LOADING)
        {
            return this.ofLazyChunkLoading ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.RENDER_REGIONS)
        {
            return this.ofRenderRegions ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.SMART_ANIMATIONS)
        {
            return this.ofSmartAnimations ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.DYNAMIC_FOV)
        {
            return this.ofDynamicFov ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.ALTERNATE_BLOCKS)
        {
            return this.ofAlternateBlocks ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.DYNAMIC_LIGHTS)
        {
            int k = indexOf(this.ofDynamicLights, OF_DYNAMIC_LIGHTS);
            return s + getTranslation(KEYS_DYNAMIC_LIGHTS, k);
        }
        else if (par1EnumOptions == Option.SCREENSHOT_SIZE)
        {
            return this.ofScreenshotSize <= 1 ? s + Lang.getDefault() : s + this.ofScreenshotSize + "x";
        }
        else if (par1EnumOptions == Option.CUSTOM_ENTITY_MODELS)
        {
            return this.ofCustomEntityModels ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.CUSTOM_GUIS)
        {
            return this.ofCustomGuis ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.SHOW_GL_ERRORS)
        {
            return this.ofShowGlErrors ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.HELD_ITEM_TOOLTIPS)
        {
            return this.heldItemTooltips ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.ADVANCED_TOOLTIPS)
        {
            return this.advancedItemTooltips ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions == Option.FRAMERATE_LIMIT)
        {
            double d1 = Option.FRAMERATE_LIMIT.get(this);

            if (d1 == 0.0D)
            {
                return s + Lang.get("of.options.framerateLimit.vsync");
            }
            else
            {
                return d1 == Option.FRAMERATE_LIMIT.getMaxValue() ? s + I18n.m_118938_("options.framerateLimit.max") : s + (int)d1 + " fps";
            }
        }
        else if (par1EnumOptions == Option.CHAT_BACKGROUND)
        {
            if (this.ofChatBackground == 3)
            {
                return s + Lang.getOff();
            }
            else
            {
                return this.ofChatBackground == 5 ? s + Lang.get("of.general.compact") : s + Lang.getDefault();
            }
        }
        else if (par1EnumOptions == Option.CHAT_SHADOW)
        {
            return this.ofChatShadow ? s + Lang.getOn() : s + Lang.getOff();
        }
        else if (par1EnumOptions instanceof ProgressOption)
        {
            ProgressOption progressoption = (ProgressOption)par1EnumOptions;
            double d0 = progressoption.get(this);
            return d0 == 0.0D ? s + I18n.m_118938_("options.off") : s + (int)(d0 * 100.0D) + "%";
        }
        else
        {
            return null;
        }
    }

    public void loadOfOptions()
    {
        try
        {
            File file1 = this.optionsFileOF;

            if (!file1.exists())
            {
                file1 = this.optionsFile;
            }

            if (!file1.exists())
            {
                return;
            }

            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(new FileInputStream(file1), StandardCharsets.UTF_8));
            String s = "";

            while ((s = bufferedreader.readLine()) != null)
            {
                try
                {
                    String[] astring = s.split(":");

                    if (astring[0].equals("ofRenderDistanceChunks") && astring.length >= 2)
                    {
                        this.renderDistance = Integer.valueOf(astring[1]);
                        this.renderDistance = Config.limit(this.renderDistance, 2, 1024);
                    }

                    if (astring[0].equals("ofFogType") && astring.length >= 2)
                    {
                        this.ofFogType = Integer.valueOf(astring[1]);
                        this.ofFogType = Config.limit(this.ofFogType, 2, 3);
                    }

                    if (astring[0].equals("ofFogStart") && astring.length >= 2)
                    {
                        this.ofFogStart = Float.valueOf(astring[1]);

                        if (this.ofFogStart < 0.2F)
                        {
                            this.ofFogStart = 0.2F;
                        }

                        if (this.ofFogStart > 0.81F)
                        {
                            this.ofFogStart = 0.8F;
                        }
                    }

                    if (astring[0].equals("ofMipmapType") && astring.length >= 2)
                    {
                        this.ofMipmapType = Integer.valueOf(astring[1]);
                        this.ofMipmapType = Config.limit(this.ofMipmapType, 0, 3);
                    }

                    if (astring[0].equals("ofOcclusionFancy") && astring.length >= 2)
                    {
                        this.ofOcclusionFancy = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofSmoothFps") && astring.length >= 2)
                    {
                        this.ofSmoothFps = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofSmoothWorld") && astring.length >= 2)
                    {
                        this.ofSmoothWorld = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofAoLevel") && astring.length >= 2)
                    {
                        this.ofAoLevel = (double)Float.valueOf(astring[1]).floatValue();
                        this.ofAoLevel = Config.limit(this.ofAoLevel, 0.0D, 1.0D);
                    }

                    if (astring[0].equals("ofClouds") && astring.length >= 2)
                    {
                        this.ofClouds = Integer.valueOf(astring[1]);
                        this.ofClouds = Config.limit(this.ofClouds, 0, 3);
                        this.updateRenderClouds();
                    }

                    if (astring[0].equals("ofCloudsHeight") && astring.length >= 2)
                    {
                        this.ofCloudsHeight = (double)Float.valueOf(astring[1]).floatValue();
                        this.ofCloudsHeight = Config.limit(this.ofCloudsHeight, 0.0D, 1.0D);
                    }

                    if (astring[0].equals("ofTrees") && astring.length >= 2)
                    {
                        this.ofTrees = Integer.valueOf(astring[1]);
                        this.ofTrees = limit(this.ofTrees, OF_TREES_VALUES);
                    }

                    if (astring[0].equals("ofDroppedItems") && astring.length >= 2)
                    {
                        this.ofDroppedItems = Integer.valueOf(astring[1]);
                        this.ofDroppedItems = Config.limit(this.ofDroppedItems, 0, 2);
                    }

                    if (astring[0].equals("ofRain") && astring.length >= 2)
                    {
                        this.ofRain = Integer.valueOf(astring[1]);
                        this.ofRain = Config.limit(this.ofRain, 0, 3);
                    }

                    if (astring[0].equals("ofAnimatedWater") && astring.length >= 2)
                    {
                        this.ofAnimatedWater = Integer.valueOf(astring[1]);
                        this.ofAnimatedWater = Config.limit(this.ofAnimatedWater, 0, 2);
                    }

                    if (astring[0].equals("ofAnimatedLava") && astring.length >= 2)
                    {
                        this.ofAnimatedLava = Integer.valueOf(astring[1]);
                        this.ofAnimatedLava = Config.limit(this.ofAnimatedLava, 0, 2);
                    }

                    if (astring[0].equals("ofAnimatedFire") && astring.length >= 2)
                    {
                        this.ofAnimatedFire = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedPortal") && astring.length >= 2)
                    {
                        this.ofAnimatedPortal = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedRedstone") && astring.length >= 2)
                    {
                        this.ofAnimatedRedstone = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedExplosion") && astring.length >= 2)
                    {
                        this.ofAnimatedExplosion = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedFlame") && astring.length >= 2)
                    {
                        this.ofAnimatedFlame = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedSmoke") && astring.length >= 2)
                    {
                        this.ofAnimatedSmoke = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofVoidParticles") && astring.length >= 2)
                    {
                        this.ofVoidParticles = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofWaterParticles") && astring.length >= 2)
                    {
                        this.ofWaterParticles = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofPortalParticles") && astring.length >= 2)
                    {
                        this.ofPortalParticles = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofPotionParticles") && astring.length >= 2)
                    {
                        this.ofPotionParticles = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofFireworkParticles") && astring.length >= 2)
                    {
                        this.ofFireworkParticles = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofDrippingWaterLava") && astring.length >= 2)
                    {
                        this.ofDrippingWaterLava = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedTerrain") && astring.length >= 2)
                    {
                        this.ofAnimatedTerrain = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedTextures") && astring.length >= 2)
                    {
                        this.ofAnimatedTextures = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofRainSplash") && astring.length >= 2)
                    {
                        this.ofRainSplash = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofLagometer") && astring.length >= 2)
                    {
                        this.ofLagometer = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofShowFps") && astring.length >= 2)
                    {
                        this.ofShowFps = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofAutoSaveTicks") && astring.length >= 2)
                    {
                        this.ofAutoSaveTicks = Integer.valueOf(astring[1]);
                        this.ofAutoSaveTicks = Config.limit(this.ofAutoSaveTicks, 40, 40000);
                    }

                    if (astring[0].equals("ofBetterGrass") && astring.length >= 2)
                    {
                        this.ofBetterGrass = Integer.valueOf(astring[1]);
                        this.ofBetterGrass = Config.limit(this.ofBetterGrass, 1, 3);
                    }

                    if (astring[0].equals("ofConnectedTextures") && astring.length >= 2)
                    {
                        this.ofConnectedTextures = Integer.valueOf(astring[1]);
                        this.ofConnectedTextures = Config.limit(this.ofConnectedTextures, 1, 3);
                    }

                    if (astring[0].equals("ofWeather") && astring.length >= 2)
                    {
                        this.ofWeather = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofSky") && astring.length >= 2)
                    {
                        this.ofSky = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofStars") && astring.length >= 2)
                    {
                        this.ofStars = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofSunMoon") && astring.length >= 2)
                    {
                        this.ofSunMoon = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofVignette") && astring.length >= 2)
                    {
                        this.ofVignette = Integer.valueOf(astring[1]);
                        this.ofVignette = Config.limit(this.ofVignette, 0, 2);
                    }

                    if (astring[0].equals("ofChunkUpdates") && astring.length >= 2)
                    {
                        this.ofChunkUpdates = Integer.valueOf(astring[1]);
                        this.ofChunkUpdates = Config.limit(this.ofChunkUpdates, 1, 5);
                    }

                    if (astring[0].equals("ofChunkUpdatesDynamic") && astring.length >= 2)
                    {
                        this.ofChunkUpdatesDynamic = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofTime") && astring.length >= 2)
                    {
                        this.ofTime = Integer.valueOf(astring[1]);
                        this.ofTime = Config.limit(this.ofTime, 0, 2);
                    }

                    if (astring[0].equals("ofAaLevel") && astring.length >= 2)
                    {
                        this.ofAaLevel = Integer.valueOf(astring[1]);
                        this.ofAaLevel = Config.limit(this.ofAaLevel, 0, 16);
                    }

                    if (astring[0].equals("ofAfLevel") && astring.length >= 2)
                    {
                        this.ofAfLevel = Integer.valueOf(astring[1]);
                        this.ofAfLevel = Config.limit(this.ofAfLevel, 1, 16);
                    }

                    if (astring[0].equals("ofProfiler") && astring.length >= 2)
                    {
                        this.ofProfiler = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofBetterSnow") && astring.length >= 2)
                    {
                        this.ofBetterSnow = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofSwampColors") && astring.length >= 2)
                    {
                        this.ofSwampColors = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofRandomEntities") && astring.length >= 2)
                    {
                        this.ofRandomEntities = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofCustomFonts") && astring.length >= 2)
                    {
                        this.ofCustomFonts = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofCustomColors") && astring.length >= 2)
                    {
                        this.ofCustomColors = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofCustomItems") && astring.length >= 2)
                    {
                        this.ofCustomItems = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofCustomSky") && astring.length >= 2)
                    {
                        this.ofCustomSky = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofShowCapes") && astring.length >= 2)
                    {
                        this.ofShowCapes = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofNaturalTextures") && astring.length >= 2)
                    {
                        this.ofNaturalTextures = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofEmissiveTextures") && astring.length >= 2)
                    {
                        this.ofEmissiveTextures = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofLazyChunkLoading") && astring.length >= 2)
                    {
                        this.ofLazyChunkLoading = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofRenderRegions") && astring.length >= 2)
                    {
                        this.ofRenderRegions = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofSmartAnimations") && astring.length >= 2)
                    {
                        this.ofSmartAnimations = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofDynamicFov") && astring.length >= 2)
                    {
                        this.ofDynamicFov = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofAlternateBlocks") && astring.length >= 2)
                    {
                        this.ofAlternateBlocks = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofDynamicLights") && astring.length >= 2)
                    {
                        this.ofDynamicLights = Integer.valueOf(astring[1]);
                        this.ofDynamicLights = limit(this.ofDynamicLights, OF_DYNAMIC_LIGHTS);
                    }

                    if (astring[0].equals("ofScreenshotSize") && astring.length >= 2)
                    {
                        this.ofScreenshotSize = Integer.valueOf(astring[1]);
                        this.ofScreenshotSize = Config.limit(this.ofScreenshotSize, 1, 4);
                    }

                    if (astring[0].equals("ofCustomEntityModels") && astring.length >= 2)
                    {
                        this.ofCustomEntityModels = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofCustomGuis") && astring.length >= 2)
                    {
                        this.ofCustomGuis = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofShowGlErrors") && astring.length >= 2)
                    {
                        this.ofShowGlErrors = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofFastMath") && astring.length >= 2)
                    {
                        this.ofFastMath = Boolean.valueOf(astring[1]);
                        Mth.fastMath = this.ofFastMath;
                    }

                    if (astring[0].equals("ofFastRender") && astring.length >= 2)
                    {
                        this.ofFastRender = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofTranslucentBlocks") && astring.length >= 2)
                    {
                        this.ofTranslucentBlocks = Integer.valueOf(astring[1]);
                        this.ofTranslucentBlocks = Config.limit(this.ofTranslucentBlocks, 0, 2);
                    }

                    if (astring[0].equals("ofChatBackground") && astring.length >= 2)
                    {
                        this.ofChatBackground = Integer.valueOf(astring[1]);
                    }

                    if (astring[0].equals("ofChatShadow") && astring.length >= 2)
                    {
                        this.ofChatShadow = Boolean.valueOf(astring[1]);
                    }

                    if (astring[0].equals("key_" + this.ofKeyBindZoom.getName()))
                    {
                        this.ofKeyBindZoom.setKey(InputConstants.getKey(astring[1]));
                    }
                }
                catch (Exception exception1)
                {
                    Config.dbg("Skipping bad option: " + s);
                    exception1.printStackTrace();
                }
            }

            KeyUtils.fixKeyConflicts(this.keyMappings, new KeyMapping[] {this.ofKeyBindZoom});
            KeyMapping.resetMapping();
            bufferedreader.close();
        }
        catch (Exception exception11)
        {
            Config.warn("Failed to load options");
            exception11.printStackTrace();
        }
    }

    public void saveOfOptions()
    {
        try
        {
            PrintWriter printwriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.optionsFileOF), StandardCharsets.UTF_8));
            printwriter.println("ofFogType:" + this.ofFogType);
            printwriter.println("ofFogStart:" + this.ofFogStart);
            printwriter.println("ofMipmapType:" + this.ofMipmapType);
            printwriter.println("ofOcclusionFancy:" + this.ofOcclusionFancy);
            printwriter.println("ofSmoothFps:" + this.ofSmoothFps);
            printwriter.println("ofSmoothWorld:" + this.ofSmoothWorld);
            printwriter.println("ofAoLevel:" + this.ofAoLevel);
            printwriter.println("ofClouds:" + this.ofClouds);
            printwriter.println("ofCloudsHeight:" + this.ofCloudsHeight);
            printwriter.println("ofTrees:" + this.ofTrees);
            printwriter.println("ofDroppedItems:" + this.ofDroppedItems);
            printwriter.println("ofRain:" + this.ofRain);
            printwriter.println("ofAnimatedWater:" + this.ofAnimatedWater);
            printwriter.println("ofAnimatedLava:" + this.ofAnimatedLava);
            printwriter.println("ofAnimatedFire:" + this.ofAnimatedFire);
            printwriter.println("ofAnimatedPortal:" + this.ofAnimatedPortal);
            printwriter.println("ofAnimatedRedstone:" + this.ofAnimatedRedstone);
            printwriter.println("ofAnimatedExplosion:" + this.ofAnimatedExplosion);
            printwriter.println("ofAnimatedFlame:" + this.ofAnimatedFlame);
            printwriter.println("ofAnimatedSmoke:" + this.ofAnimatedSmoke);
            printwriter.println("ofVoidParticles:" + this.ofVoidParticles);
            printwriter.println("ofWaterParticles:" + this.ofWaterParticles);
            printwriter.println("ofPortalParticles:" + this.ofPortalParticles);
            printwriter.println("ofPotionParticles:" + this.ofPotionParticles);
            printwriter.println("ofFireworkParticles:" + this.ofFireworkParticles);
            printwriter.println("ofDrippingWaterLava:" + this.ofDrippingWaterLava);
            printwriter.println("ofAnimatedTerrain:" + this.ofAnimatedTerrain);
            printwriter.println("ofAnimatedTextures:" + this.ofAnimatedTextures);
            printwriter.println("ofRainSplash:" + this.ofRainSplash);
            printwriter.println("ofLagometer:" + this.ofLagometer);
            printwriter.println("ofShowFps:" + this.ofShowFps);
            printwriter.println("ofAutoSaveTicks:" + this.ofAutoSaveTicks);
            printwriter.println("ofBetterGrass:" + this.ofBetterGrass);
            printwriter.println("ofConnectedTextures:" + this.ofConnectedTextures);
            printwriter.println("ofWeather:" + this.ofWeather);
            printwriter.println("ofSky:" + this.ofSky);
            printwriter.println("ofStars:" + this.ofStars);
            printwriter.println("ofSunMoon:" + this.ofSunMoon);
            printwriter.println("ofVignette:" + this.ofVignette);
            printwriter.println("ofChunkUpdates:" + this.ofChunkUpdates);
            printwriter.println("ofChunkUpdatesDynamic:" + this.ofChunkUpdatesDynamic);
            printwriter.println("ofTime:" + this.ofTime);
            printwriter.println("ofAaLevel:" + this.ofAaLevel);
            printwriter.println("ofAfLevel:" + this.ofAfLevel);
            printwriter.println("ofProfiler:" + this.ofProfiler);
            printwriter.println("ofBetterSnow:" + this.ofBetterSnow);
            printwriter.println("ofSwampColors:" + this.ofSwampColors);
            printwriter.println("ofRandomEntities:" + this.ofRandomEntities);
            printwriter.println("ofCustomFonts:" + this.ofCustomFonts);
            printwriter.println("ofCustomColors:" + this.ofCustomColors);
            printwriter.println("ofCustomItems:" + this.ofCustomItems);
            printwriter.println("ofCustomSky:" + this.ofCustomSky);
            printwriter.println("ofShowCapes:" + this.ofShowCapes);
            printwriter.println("ofNaturalTextures:" + this.ofNaturalTextures);
            printwriter.println("ofEmissiveTextures:" + this.ofEmissiveTextures);
            printwriter.println("ofLazyChunkLoading:" + this.ofLazyChunkLoading);
            printwriter.println("ofRenderRegions:" + this.ofRenderRegions);
            printwriter.println("ofSmartAnimations:" + this.ofSmartAnimations);
            printwriter.println("ofDynamicFov:" + this.ofDynamicFov);
            printwriter.println("ofAlternateBlocks:" + this.ofAlternateBlocks);
            printwriter.println("ofDynamicLights:" + this.ofDynamicLights);
            printwriter.println("ofScreenshotSize:" + this.ofScreenshotSize);
            printwriter.println("ofCustomEntityModels:" + this.ofCustomEntityModels);
            printwriter.println("ofCustomGuis:" + this.ofCustomGuis);
            printwriter.println("ofShowGlErrors:" + this.ofShowGlErrors);
            printwriter.println("ofFastMath:" + this.ofFastMath);
            printwriter.println("ofFastRender:" + this.ofFastRender);
            printwriter.println("ofTranslucentBlocks:" + this.ofTranslucentBlocks);
            printwriter.println("ofChatBackground:" + this.ofChatBackground);
            printwriter.println("ofChatShadow:" + this.ofChatShadow);
            printwriter.println("key_" + this.ofKeyBindZoom.getName() + ":" + this.ofKeyBindZoom.saveString());
            printwriter.close();
        }
        catch (Exception exception1)
        {
            Config.warn("Failed to save options");
            exception1.printStackTrace();
        }
    }

    public void updateRenderClouds()
    {
        switch (this.ofClouds)
        {
            case 1:
                this.renderClouds = CloudStatus.FAST;
                break;

            case 2:
                this.renderClouds = CloudStatus.FANCY;
                break;

            case 3:
                this.renderClouds = CloudStatus.OFF;
                break;

            default:
                if (this.graphicsMode != GraphicsStatus.FAST)
                {
                    this.renderClouds = CloudStatus.FANCY;
                }
                else
                {
                    this.renderClouds = CloudStatus.FAST;
                }
        }

        if (this.graphicsMode == GraphicsStatus.FABULOUS)
        {
            LevelRenderer levelrenderer = Minecraft.getInstance().levelRenderer;

            if (levelrenderer != null)
            {
                RenderTarget rendertarget = levelrenderer.getCloudsTarget();

                if (rendertarget != null)
                {
                    rendertarget.clear(Minecraft.ON_OSX);
                }
            }
        }
    }

    public void resetSettings()
    {
        this.renderDistance = 8;
        this.entityDistanceScaling = 1.0F;
        this.bobView = true;
        this.framerateLimit = (int)Option.FRAMERATE_LIMIT.getMaxValue();
        this.enableVsync = false;
        this.updateVSync();
        this.mipmapLevels = 4;
        this.graphicsMode = GraphicsStatus.FANCY;
        this.ambientOcclusion = AmbientOcclusionStatus.MAX;
        this.renderClouds = CloudStatus.FANCY;
        this.fov = 70.0D;
        this.gamma = 0.0D;
        this.guiScale = 0;
        this.particles = ParticleStatus.ALL;
        this.heldItemTooltips = true;
        this.forceUnicodeFont = false;
        this.ofFogType = 2;
        this.ofFogStart = 0.8F;
        this.ofMipmapType = 0;
        this.ofOcclusionFancy = false;
        this.ofSmartAnimations = false;
        this.ofSmoothFps = false;
        Config.updateAvailableProcessors();
        this.ofSmoothWorld = Config.isSingleProcessor();
        this.ofLazyChunkLoading = false;
        this.ofRenderRegions = false;
        this.ofFastMath = false;
        this.ofFastRender = false;
        this.ofTranslucentBlocks = 0;
        this.ofDynamicFov = true;
        this.ofAlternateBlocks = true;
        this.ofDynamicLights = 3;
        this.ofScreenshotSize = 1;
        this.ofCustomEntityModels = true;
        this.ofCustomGuis = true;
        this.ofShowGlErrors = true;
        this.ofChatBackground = 0;
        this.ofChatShadow = true;
        this.ofAoLevel = 1.0D;
        this.ofAaLevel = 0;
        this.ofAfLevel = 1;
        this.ofClouds = 0;
        this.ofCloudsHeight = 0.0D;
        this.ofTrees = 0;
        this.ofRain = 0;
        this.ofBetterGrass = 3;
        this.ofAutoSaveTicks = 4000;
        this.ofLagometer = false;
        this.ofShowFps = false;
        this.ofProfiler = false;
        this.ofWeather = true;
        this.ofSky = true;
        this.ofStars = true;
        this.ofSunMoon = true;
        this.ofVignette = 0;
        this.ofChunkUpdates = 1;
        this.ofChunkUpdatesDynamic = false;
        this.ofTime = 0;
        this.ofBetterSnow = false;
        this.ofSwampColors = true;
        this.ofRandomEntities = true;
        this.biomeBlendRadius = 2;
        this.ofCustomFonts = true;
        this.ofCustomColors = true;
        this.ofCustomItems = true;
        this.ofCustomSky = true;
        this.ofShowCapes = true;
        this.ofConnectedTextures = 2;
        this.ofNaturalTextures = false;
        this.ofEmissiveTextures = true;
        this.ofAnimatedWater = 0;
        this.ofAnimatedLava = 0;
        this.ofAnimatedFire = true;
        this.ofAnimatedPortal = true;
        this.ofAnimatedRedstone = true;
        this.ofAnimatedExplosion = true;
        this.ofAnimatedFlame = true;
        this.ofAnimatedSmoke = true;
        this.ofVoidParticles = true;
        this.ofWaterParticles = true;
        this.ofRainSplash = true;
        this.ofPortalParticles = true;
        this.ofPotionParticles = true;
        this.ofFireworkParticles = true;
        this.ofDrippingWaterLava = true;
        this.ofAnimatedTerrain = true;
        this.ofAnimatedTextures = true;
        Shaders.setShaderPack("OFF");
        Shaders.configAntialiasingLevel = 0;
        Shaders.uninit();
        Shaders.storeConfig();
        this.minecraft.delayTextureReload();
        this.save();
    }

    public void updateVSync()
    {
        if (this.minecraft.getWindow() != null)
        {
            this.minecraft.getWindow().updateVsync(this.enableVsync);
        }
    }

    public void updateMipmaps()
    {
        this.minecraft.updateMaxMipLevel(this.mipmapLevels);
        this.minecraft.delayTextureReload();
    }

    public void setAllAnimations(boolean flag)
    {
        int i = flag ? 0 : 2;
        this.ofAnimatedWater = i;
        this.ofAnimatedLava = i;
        this.ofAnimatedFire = flag;
        this.ofAnimatedPortal = flag;
        this.ofAnimatedRedstone = flag;
        this.ofAnimatedExplosion = flag;
        this.ofAnimatedFlame = flag;
        this.ofAnimatedSmoke = flag;
        this.ofVoidParticles = flag;
        this.ofWaterParticles = flag;
        this.ofRainSplash = flag;
        this.ofPortalParticles = flag;
        this.ofPotionParticles = flag;
        this.ofFireworkParticles = flag;
        this.particles = flag ? ParticleStatus.ALL : ParticleStatus.MINIMAL;
        this.ofDrippingWaterLava = flag;
        this.ofAnimatedTerrain = flag;
        this.ofAnimatedTextures = flag;
    }

    private static int nextValue(int val, int[] vals)
    {
        int i = indexOf(val, vals);

        if (i < 0)
        {
            return vals[0];
        }
        else
        {
            ++i;

            if (i >= vals.length)
            {
                i = 0;
            }

            return vals[i];
        }
    }

    private static int limit(int val, int[] vals)
    {
        int i = indexOf(val, vals);
        return i < 0 ? vals[0] : val;
    }

    private static int indexOf(int val, int[] vals)
    {
        for (int i = 0; i < vals.length; ++i)
        {
            if (vals[i] == val)
            {
                return i;
            }
        }

        return -1;
    }

    private static String getTranslation(String[] strArray, int index)
    {
        if (index < 0 || index >= strArray.length)
        {
            index = 0;
        }

        return I18n.m_118938_(strArray[index]);
    }

    private void setForgeKeybindProperties()
    {
        if (Reflector.KeyConflictContext_IN_GAME.exists())
        {
            if (Reflector.ForgeKeyBinding_setKeyConflictContext.exists())
            {
                Object object = Reflector.getFieldValue(Reflector.KeyConflictContext_IN_GAME);
                Reflector.call(this.keyUp, Reflector.ForgeKeyBinding_setKeyConflictContext, object);
                Reflector.call(this.keyLeft, Reflector.ForgeKeyBinding_setKeyConflictContext, object);
                Reflector.call(this.keyDown, Reflector.ForgeKeyBinding_setKeyConflictContext, object);
                Reflector.call(this.keyRight, Reflector.ForgeKeyBinding_setKeyConflictContext, object);
                Reflector.call(this.keyJump, Reflector.ForgeKeyBinding_setKeyConflictContext, object);
                Reflector.call(this.keyShift, Reflector.ForgeKeyBinding_setKeyConflictContext, object);
                Reflector.call(this.keySprint, Reflector.ForgeKeyBinding_setKeyConflictContext, object);
                Reflector.call(this.keyAttack, Reflector.ForgeKeyBinding_setKeyConflictContext, object);
                Reflector.call(this.keyChat, Reflector.ForgeKeyBinding_setKeyConflictContext, object);
                Reflector.call(this.keyPlayerList, Reflector.ForgeKeyBinding_setKeyConflictContext, object);
                Reflector.call(this.keyCommand, Reflector.ForgeKeyBinding_setKeyConflictContext, object);
                Reflector.call(this.keyTogglePerspective, Reflector.ForgeKeyBinding_setKeyConflictContext, object);
                Reflector.call(this.keySmoothCamera, Reflector.ForgeKeyBinding_setKeyConflictContext, object);
            }
        }
    }

    public void loadSelectedResourcePacks(PackRepository pResourcePackList)
    {
        Set<String> set = Sets.newLinkedHashSet();
        Iterator<String> iterator = this.resourcePacks.iterator();

        while (iterator.hasNext())
        {
            String s = iterator.next();
            Pack pack = pResourcePackList.getPack(s);

            if (pack == null && !s.startsWith("file/"))
            {
                pack = pResourcePackList.getPack("file/" + s);
            }

            if (pack == null)
            {
                LOGGER.warn("Removed resource pack {} from options because it doesn't seem to exist anymore", (Object)s);
                iterator.remove();
            }
            else if (!pack.getCompatibility().isCompatible() && !this.incompatibleResourcePacks.contains(s))
            {
                LOGGER.warn("Removed resource pack {} from options because it is no longer compatible", (Object)s);
                iterator.remove();
            }
            else if (pack.getCompatibility().isCompatible() && this.incompatibleResourcePacks.contains(s))
            {
                LOGGER.info("Removed resource pack {} from incompatibility list because it's now compatible", (Object)s);
                this.incompatibleResourcePacks.remove(s);
            }
            else
            {
                set.add(pack.getId());
            }
        }

        pResourcePackList.setSelected(set);
    }

    public CameraType getCameraType()
    {
        return this.cameraType;
    }

    public void setCameraType(CameraType pPointOfView)
    {
        this.cameraType = pPointOfView;
    }

    private static List<String> readPackList(String p_168443_)
    {
        List<String> list = GsonHelper.fromJson(GSON, p_168443_, RESOURCE_PACK_TYPE);
        return (List<String>)(list != null ? list : Lists.newArrayList());
    }

    private static CloudStatus readCloudStatus(String p_168445_)
    {
        switch (p_168445_)
        {
            case "true":
                return CloudStatus.FANCY;

            case "fast":
                return CloudStatus.FAST;

            case "false":
            default:
                return CloudStatus.OFF;
        }
    }

    private static String writeCloudStatus(CloudStatus p_168426_)
    {
        switch (p_168426_)
        {
            case FANCY:
                return "true";

            case FAST:
                return "fast";

            case OFF:
            default:
                return "false";
        }
    }

    private static AmbientOcclusionStatus readAmbientOcclusion(String p_168447_)
    {
        if (isTrue(p_168447_))
        {
            return AmbientOcclusionStatus.MAX;
        }
        else
        {
            return isFalse(p_168447_) ? AmbientOcclusionStatus.OFF : AmbientOcclusionStatus.byId(Integer.parseInt(p_168447_));
        }
    }

    private static HumanoidArm readMainHand(String p_168449_)
    {
        return "left".equals(p_168449_) ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    private static String writeMainHand(HumanoidArm p_168415_)
    {
        return p_168415_ == HumanoidArm.LEFT ? "left" : "right";
    }

    public File getFile()
    {
        return this.optionsFile;
    }

    public String dumpOptionsForReport()
    {
        ImmutableList<Pair<String, String>> immutablelist = ImmutableList.<Pair<String, String>>builder().add(Pair.of("ao", String.valueOf((Object)this.ambientOcclusion))).add(Pair.of("biomeBlendRadius", String.valueOf(this.biomeBlendRadius))).add(Pair.of("enableVsync", String.valueOf(this.enableVsync))).add(Pair.of("entityDistanceScaling", String.valueOf(this.entityDistanceScaling))).add(Pair.of("entityShadows", String.valueOf(this.entityShadows))).add(Pair.of("forceUnicodeFont", String.valueOf(this.forceUnicodeFont))).add(Pair.of("fov", String.valueOf(this.fov))).add(Pair.of("fovEffectScale", String.valueOf(this.fovEffectScale))).add(Pair.of("fullscreen", String.valueOf(this.fullscreen))).add(Pair.of("fullscreenResolution", String.valueOf((Object)this.fullscreenVideoModeString))).add(Pair.of("gamma", String.valueOf(this.gamma))).add(Pair.of("glDebugVerbosity", String.valueOf(this.glDebugVerbosity))).add(Pair.of("graphicsMode", String.valueOf((Object)this.graphicsMode))).add(Pair.of("guiScale", String.valueOf(this.guiScale))).add(Pair.of("maxFps", String.valueOf(this.framerateLimit))).add(Pair.of("mipmapLevels", String.valueOf(this.mipmapLevels))).add(Pair.of("narrator", String.valueOf((Object)this.narratorStatus))).add(Pair.of("overrideHeight", String.valueOf(this.overrideHeight))).add(Pair.of("overrideWidth", String.valueOf(this.overrideWidth))).add(Pair.of("particles", String.valueOf((Object)this.particles))).add(Pair.of("reducedDebugInfo", String.valueOf(this.reducedDebugInfo))).add(Pair.of("renderClouds", String.valueOf((Object)this.renderClouds))).add(Pair.of("renderDistance", String.valueOf(this.renderDistance))).add(Pair.of("resourcePacks", String.valueOf((Object)this.resourcePacks))).add(Pair.of("screenEffectScale", String.valueOf(this.screenEffectScale))).add(Pair.of("syncChunkWrites", String.valueOf(this.syncWrites))).add(Pair.of("useNativeTransport", String.valueOf(this.useNativeTransport))).build();
        return immutablelist.stream().map((p_313924_0_) ->
        {
            return (String)p_313924_0_.getFirst() + ": " + (String)p_313924_0_.getSecond();
        }).collect(Collectors.joining(System.lineSeparator()));
    }

    interface FieldAccess
    {
        int process(String p_168523_, int p_168524_);

        boolean process(String p_168535_, boolean p_168536_);

        String process(String p_168533_, String p_168534_);

        double process(String p_168519_, double p_168520_);

        float process(String p_168521_, float p_168522_);

        <T> T process(String p_168525_, T p_168526_, Function<String, T> p_168527_, Function<T, String> p_168528_);

        <T> T process(String p_168529_, T p_168530_, IntFunction<T> p_168531_, ToIntFunction<T> p_168532_);
    }
}
