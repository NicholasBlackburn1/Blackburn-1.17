package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class Option {
   protected static final int OPTIONS_TOOLTIP_WIDTH = 200;
   public static final ProgressOption BIOME_BLEND_RADIUS = new ProgressOption("options.biomeBlendRadius", 0.0D, 7.0D, 1.0F, (p_91713_) -> {
      return (double)p_91713_.biomeBlendRadius;
   }, (p_92009_, p_92010_) -> {
      p_92009_.biomeBlendRadius = Mth.clamp((int)p_92010_.doubleValue(), 0, 7);
      Minecraft.getInstance().levelRenderer.allChanged();
   }, (p_92003_, p_92004_) -> {
      double d0 = p_92004_.get(p_92003_);
      int i = (int)d0 * 2 + 1;
      return p_92004_.genericValueLabel(new TranslatableComponent("options.biomeBlendRadius." + i));
   });
   public static final ProgressOption CHAT_HEIGHT_FOCUSED = new ProgressOption("options.chat.height.focused", 0.0D, 1.0D, 0.0F, (p_91711_) -> {
      return p_91711_.chatHeightFocused;
   }, (p_91998_, p_91999_) -> {
      p_91998_.chatHeightFocused = p_91999_;
      Minecraft.getInstance().gui.getChat().rescaleChat();
   }, (p_91992_, p_91993_) -> {
      double d0 = p_91993_.toPct(p_91993_.get(p_91992_));
      return p_91993_.pixelValueLabel(ChatComponent.getHeight(d0));
   });
   public static final ProgressOption CHAT_HEIGHT_UNFOCUSED = new ProgressOption("options.chat.height.unfocused", 0.0D, 1.0D, 0.0F, (p_91709_) -> {
      return p_91709_.chatHeightUnfocused;
   }, (p_91987_, p_91988_) -> {
      p_91987_.chatHeightUnfocused = p_91988_;
      Minecraft.getInstance().gui.getChat().rescaleChat();
   }, (p_91981_, p_91982_) -> {
      double d0 = p_91982_.toPct(p_91982_.get(p_91981_));
      return p_91982_.pixelValueLabel(ChatComponent.getHeight(d0));
   });
   public static final ProgressOption CHAT_OPACITY = new ProgressOption("options.chat.opacity", 0.0D, 1.0D, 0.0F, (p_91707_) -> {
      return p_91707_.chatOpacity;
   }, (p_91976_, p_91977_) -> {
      p_91976_.chatOpacity = p_91977_;
      Minecraft.getInstance().gui.getChat().rescaleChat();
   }, (p_91970_, p_91971_) -> {
      double d0 = p_91971_.toPct(p_91971_.get(p_91970_));
      return p_91971_.percentValueLabel(d0 * 0.9D + 0.1D);
   });
   public static final ProgressOption CHAT_SCALE = new ProgressOption("options.chat.scale", 0.0D, 1.0D, 0.0F, (p_91705_) -> {
      return p_91705_.chatScale;
   }, (p_91965_, p_91966_) -> {
      p_91965_.chatScale = p_91966_;
      Minecraft.getInstance().gui.getChat().rescaleChat();
   }, (p_91959_, p_91960_) -> {
      double d0 = p_91960_.toPct(p_91960_.get(p_91959_));
      return (Component)(d0 == 0.0D ? CommonComponents.optionStatus(p_91960_.getCaption(), false) : p_91960_.percentValueLabel(d0));
   });
   public static final ProgressOption CHAT_WIDTH = new ProgressOption("options.chat.width", 0.0D, 1.0D, 0.0F, (p_91703_) -> {
      return p_91703_.chatWidth;
   }, (p_91954_, p_91955_) -> {
      p_91954_.chatWidth = p_91955_;
      Minecraft.getInstance().gui.getChat().rescaleChat();
   }, (p_91948_, p_91949_) -> {
      double d0 = p_91949_.toPct(p_91949_.get(p_91948_));
      return p_91949_.pixelValueLabel(ChatComponent.getWidth(d0));
   });
   public static final ProgressOption CHAT_LINE_SPACING = new ProgressOption("options.chat.line_spacing", 0.0D, 1.0D, 0.0F, (p_91701_) -> {
      return p_91701_.chatLineSpacing;
   }, (p_91943_, p_91944_) -> {
      p_91943_.chatLineSpacing = p_91944_;
   }, (p_91937_, p_91938_) -> {
      return p_91938_.percentValueLabel(p_91938_.toPct(p_91938_.get(p_91937_)));
   });
   public static final ProgressOption CHAT_DELAY = new ProgressOption("options.chat.delay_instant", 0.0D, 6.0D, 0.1F, (p_91699_) -> {
      return p_91699_.chatDelay;
   }, (p_91929_, p_91930_) -> {
      p_91929_.chatDelay = p_91930_;
   }, (p_91923_, p_91924_) -> {
      double d0 = p_91924_.get(p_91923_);
      return d0 <= 0.0D ? new TranslatableComponent("options.chat.delay_none") : new TranslatableComponent("options.chat.delay", String.format("%.1f", d0));
   });
   public static final ProgressOption FOV = new ProgressOption("options.fov", 30.0D, 110.0D, 1.0F, (p_91697_) -> {
      return p_91697_.fov;
   }, (p_91912_, p_91913_) -> {
      p_91912_.fov = p_91913_;
      Minecraft.getInstance().levelRenderer.needsUpdate();
   }, (p_91906_, p_91907_) -> {
      double d0 = p_91907_.get(p_91906_);
      if (d0 == 70.0D) {
         return p_91907_.genericValueLabel(new TranslatableComponent("options.fov.min"));
      } else {
         return d0 == p_91907_.getMaxValue() ? p_91907_.genericValueLabel(new TranslatableComponent("options.fov.max")) : p_91907_.genericValueLabel((int)d0);
      }
   });
   private static final Component ACCESSIBILITY_TOOLTIP_FOV_EFFECT = new TranslatableComponent("options.fovEffectScale.tooltip");
   public static final ProgressOption FOV_EFFECTS_SCALE = new ProgressOption("options.fovEffectScale", 0.0D, 1.0D, 0.0F, (p_91695_) -> {
      return Math.pow((double)p_91695_.fovEffectScale, 2.0D);
   }, (p_91895_, p_91896_) -> {
      p_91895_.fovEffectScale = (float)Math.sqrt(p_91896_);
   }, (p_91889_, p_91890_) -> {
      double d0 = p_91890_.toPct(p_91890_.get(p_91889_));
      return d0 == 0.0D ? p_91890_.genericValueLabel(CommonComponents.OPTION_OFF) : p_91890_.percentValueLabel(d0);
   }, (p_168230_) -> {
      return p_168230_.font.split(ACCESSIBILITY_TOOLTIP_FOV_EFFECT, 200);
   });
   private static final Component ACCESSIBILITY_TOOLTIP_SCREEN_EFFECT = new TranslatableComponent("options.screenEffectScale.tooltip");
   public static final ProgressOption SCREEN_EFFECTS_SCALE = new ProgressOption("options.screenEffectScale", 0.0D, 1.0D, 0.0F, (p_168139_) -> {
      return (double)p_168139_.screenEffectScale;
   }, (p_168313_, p_168314_) -> {
      p_168313_.screenEffectScale = p_168314_.floatValue();
   }, (p_168310_, p_168311_) -> {
      double d0 = p_168311_.toPct(p_168311_.get(p_168310_));
      return d0 == 0.0D ? p_168311_.genericValueLabel(CommonComponents.OPTION_OFF) : p_168311_.percentValueLabel(d0);
   }, (p_168215_) -> {
      return p_168215_.font.split(ACCESSIBILITY_TOOLTIP_SCREEN_EFFECT, 200);
   });
   public static final ProgressOption FRAMERATE_LIMIT = new ProgressOption("options.framerateLimit", 10.0D, 260.0D, 10.0F, (p_168137_) -> {
      return (double)p_168137_.framerateLimit;
   }, (p_168301_, p_168302_) -> {
      p_168301_.framerateLimit = (int)p_168302_.doubleValue();
      Minecraft.getInstance().getWindow().setFramerateLimit(p_168301_.framerateLimit);
   }, (p_168298_, p_168299_) -> {
      double d0 = p_168299_.get(p_168298_);
      return d0 == p_168299_.getMaxValue() ? p_168299_.genericValueLabel(new TranslatableComponent("options.framerateLimit.max")) : p_168299_.genericValueLabel(new TranslatableComponent("options.framerate", (int)d0));
   });
   public static final ProgressOption GAMMA = new ProgressOption("options.gamma", 0.0D, 1.0D, 0.0F, (p_168135_) -> {
      return p_168135_.gamma;
   }, (p_168289_, p_168290_) -> {
      p_168289_.gamma = p_168290_;
   }, (p_168286_, p_168287_) -> {
      double d0 = p_168287_.toPct(p_168287_.get(p_168286_));
      if (d0 == 0.0D) {
         return p_168287_.genericValueLabel(new TranslatableComponent("options.gamma.min"));
      } else {
         return d0 == 1.0D ? p_168287_.genericValueLabel(new TranslatableComponent("options.gamma.max")) : p_168287_.percentAddValueLabel((int)(d0 * 100.0D));
      }
   });
   public static final ProgressOption MIPMAP_LEVELS = new ProgressOption("options.mipmapLevels", 0.0D, 4.0D, 1.0F, (p_168133_) -> {
      return (double)p_168133_.mipmapLevels;
   }, (p_168277_, p_168278_) -> {
      p_168277_.mipmapLevels = (int)p_168278_.doubleValue();
   }, (p_168274_, p_168275_) -> {
      double d0 = p_168275_.get(p_168274_);
      return (Component)(d0 == 0.0D ? CommonComponents.optionStatus(p_168275_.getCaption(), false) : p_168275_.genericValueLabel((int)d0));
   });
   public static final ProgressOption MOUSE_WHEEL_SENSITIVITY = new LogaritmicProgressOption("options.mouseWheelSensitivity", 0.01D, 10.0D, 0.01F, (p_168131_) -> {
      return p_168131_.mouseWheelSensitivity;
   }, (p_168265_, p_168266_) -> {
      p_168265_.mouseWheelSensitivity = p_168266_;
   }, (p_168262_, p_168263_) -> {
      double d0 = p_168263_.toPct(p_168263_.get(p_168262_));
      return p_168263_.genericValueLabel(new TextComponent(String.format("%.2f", p_168263_.toValue(d0))));
   });
   public static final CycleOption<Boolean> RAW_MOUSE_INPUT = CycleOption.createOnOff("options.rawMouseInput", (p_168129_) -> {
      return p_168129_.rawMouseInput;
   }, (p_168396_, p_168397_, p_168398_) -> {
      p_168396_.rawMouseInput = p_168398_;
      Window window = Minecraft.getInstance().getWindow();
      if (window != null) {
         window.updateRawMouseInput(p_168398_);
      }

   });
   public static final ProgressOption RENDER_DISTANCE = new ProgressOption("options.renderDistance", 2.0D, 16.0D, 1.0F, (p_168127_) -> {
      return (double)p_168127_.renderDistance;
   }, (p_168253_, p_168254_) -> {
      p_168253_.renderDistance = (int)p_168254_.doubleValue();
      Minecraft.getInstance().levelRenderer.needsUpdate();
   }, (p_168250_, p_168251_) -> {
      double d0 = p_168251_.get(p_168250_);
      return p_168251_.genericValueLabel(new TranslatableComponent("options.chunks", (int)d0));
   });
   public static final ProgressOption ENTITY_DISTANCE_SCALING = new ProgressOption("options.entityDistanceScaling", 0.5D, 5.0D, 0.25F, (p_168125_) -> {
      return (double)p_168125_.entityDistanceScaling;
   }, (p_168241_, p_168242_) -> {
      p_168241_.entityDistanceScaling = (float)p_168242_.doubleValue();
   }, (p_168238_, p_168239_) -> {
      double d0 = p_168239_.get(p_168238_);
      return p_168239_.percentValueLabel(d0);
   });
   public static final ProgressOption SENSITIVITY = new ProgressOption("options.sensitivity", 0.0D, 1.0D, 0.0F, (p_168123_) -> {
      return p_168123_.sensitivity;
   }, (p_168226_, p_168227_) -> {
      p_168226_.sensitivity = p_168227_;
   }, (p_168223_, p_168224_) -> {
      double d0 = p_168224_.toPct(p_168224_.get(p_168223_));
      if (d0 == 0.0D) {
         return p_168224_.genericValueLabel(new TranslatableComponent("options.sensitivity.min"));
      } else {
         return d0 == 1.0D ? p_168224_.genericValueLabel(new TranslatableComponent("options.sensitivity.max")) : p_168224_.percentValueLabel(2.0D * d0);
      }
   });
   public static final ProgressOption TEXT_BACKGROUND_OPACITY = new ProgressOption("options.accessibility.text_background_opacity", 0.0D, 1.0D, 0.0F, (p_168121_) -> {
      return p_168121_.textBackgroundOpacity;
   }, (p_168200_, p_168201_) -> {
      p_168200_.textBackgroundOpacity = p_168201_;
      Minecraft.getInstance().gui.getChat().rescaleChat();
   }, (p_168197_, p_168198_) -> {
      return p_168198_.percentValueLabel(p_168198_.toPct(p_168198_.get(p_168197_)));
   });
   public static final CycleOption<AmbientOcclusionStatus> AMBIENT_OCCLUSION = CycleOption.create("options.ao", AmbientOcclusionStatus.values(), (p_168143_) -> {
      return new TranslatableComponent(p_168143_.getKey());
   }, (p_168119_) -> {
      return p_168119_.ambientOcclusion;
   }, (p_168165_, p_168166_, p_168167_) -> {
      p_168165_.ambientOcclusion = p_168167_;
      Minecraft.getInstance().levelRenderer.allChanged();
   });
   public static final CycleOption<AttackIndicatorStatus> ATTACK_INDICATOR = CycleOption.create("options.attackIndicator", AttackIndicatorStatus.values(), (p_168145_) -> {
      return new TranslatableComponent(p_168145_.getKey());
   }, (p_168117_) -> {
      return p_168117_.attackIndicator;
   }, (p_168169_, p_168170_, p_168171_) -> {
      p_168169_.attackIndicator = p_168171_;
   });
   public static final CycleOption<ChatVisiblity> CHAT_VISIBILITY = CycleOption.create("options.chat.visibility", ChatVisiblity.values(), (p_168141_) -> {
      return new TranslatableComponent(p_168141_.getKey());
   }, (p_168115_) -> {
      return p_168115_.chatVisibility;
   }, (p_168161_, p_168162_, p_168163_) -> {
      p_168161_.chatVisibility = p_168163_;
   });
   private static final Component GRAPHICS_TOOLTIP_FAST = new TranslatableComponent("options.graphics.fast.tooltip");
   private static final Component GRAPHICS_TOOLTIP_FABULOUS = new TranslatableComponent("options.graphics.fabulous.tooltip", (new TranslatableComponent("options.graphics.fabulous")).withStyle(ChatFormatting.ITALIC));
   private static final Component GRAPHICS_TOOLTIP_FANCY = new TranslatableComponent("options.graphics.fancy.tooltip");
   public static final CycleOption<GraphicsStatus> GRAPHICS = CycleOption.create("options.graphics", Arrays.asList(GraphicsStatus.values()), Stream.of(GraphicsStatus.values()).filter((p_168213_) -> {
      return p_168213_ != GraphicsStatus.FABULOUS;
   }).collect(Collectors.toList()), () -> {
      return Minecraft.getInstance().getGpuWarnlistManager().isSkippingFabulous();
   }, (p_168149_) -> {
      MutableComponent mutablecomponent = new TranslatableComponent(p_168149_.getKey());
      return p_168149_ == GraphicsStatus.FABULOUS ? mutablecomponent.withStyle(ChatFormatting.ITALIC) : mutablecomponent;
   }, (p_168113_) -> {
      return p_168113_.graphicsMode;
   }, (p_168177_, p_168178_, p_168179_) -> {
      Minecraft minecraft = Minecraft.getInstance();
      GpuWarnlistManager gpuwarnlistmanager = minecraft.getGpuWarnlistManager();
      if (p_168179_ == GraphicsStatus.FABULOUS && gpuwarnlistmanager.willShowWarning()) {
         gpuwarnlistmanager.showWarning();
      } else {
         p_168177_.graphicsMode = p_168179_;
         minecraft.levelRenderer.allChanged();
      }
   }).setTooltip((p_168151_) -> {
      List<FormattedCharSequence> list = p_168151_.font.split(GRAPHICS_TOOLTIP_FAST, 200);
      List<FormattedCharSequence> list1 = p_168151_.font.split(GRAPHICS_TOOLTIP_FANCY, 200);
      List<FormattedCharSequence> list2 = p_168151_.font.split(GRAPHICS_TOOLTIP_FABULOUS, 200);
      return (p_168210_) -> {
         switch(p_168210_) {
         case FANCY:
            return list1;
         case FAST:
            return list;
         case FABULOUS:
            return list2;
         default:
            return ImmutableList.of();
         }
      };
   });
   public static final CycleOption GUI_SCALE = CycleOption.create("options.guiScale", () -> {
      return IntStream.rangeClosed(0, Minecraft.getInstance().getWindow().calculateScale(0, Minecraft.getInstance().isEnforceUnicode())).boxed().collect(Collectors.toList());
   }, (p_168205_) -> {
      return (Component)(p_168205_ == 0 ? new TranslatableComponent("options.guiScale.auto") : new TextComponent(Integer.toString(p_168205_)));
   }, (p_168111_) -> {
      return p_168111_.guiScale;
   }, (p_168193_, p_168194_, p_168195_) -> {
      p_168193_.guiScale = p_168195_;
   });
   public static final CycleOption<HumanoidArm> MAIN_HAND = CycleOption.create("options.mainHand", HumanoidArm.values(), HumanoidArm::getName, (p_168404_) -> {
      return p_168404_.mainHand;
   }, (p_168157_, p_168158_, p_168159_) -> {
      p_168157_.mainHand = p_168159_;
      p_168157_.broadcastOptions();
   });
   public static final CycleOption<NarratorStatus> NARRATOR = CycleOption.create("options.narrator", NarratorStatus.values(), (p_168153_) -> {
      return (Component)(NarratorChatListener.INSTANCE.isActive() ? p_168153_.getName() : new TranslatableComponent("options.narrator.notavailable"));
   }, (p_168402_) -> {
      return p_168402_.narratorStatus;
   }, (p_168181_, p_168182_, p_168183_) -> {
      p_168181_.narratorStatus = p_168183_;
      NarratorChatListener.INSTANCE.updateNarratorStatus(p_168183_);
   });
   public static final CycleOption<ParticleStatus> PARTICLES = CycleOption.create("options.particles", ParticleStatus.values(), (p_168203_) -> {
      return new TranslatableComponent(p_168203_.getKey());
   }, (p_168400_) -> {
      return p_168400_.particles;
   }, (p_168185_, p_168186_, p_168187_) -> {
      p_168185_.particles = p_168187_;
   });
   public static final CycleOption<CloudStatus> RENDER_CLOUDS = CycleOption.create("options.renderClouds", CloudStatus.values(), (p_168147_) -> {
      return new TranslatableComponent(p_168147_.getKey());
   }, (p_168394_) -> {
      return p_168394_.renderClouds;
   }, (p_168173_, p_168174_, p_168175_) -> {
      p_168173_.renderClouds = p_168175_;
      if (Minecraft.useShaderTransparency()) {
         RenderTarget rendertarget = Minecraft.getInstance().levelRenderer.getCloudsTarget();
         if (rendertarget != null) {
            rendertarget.clear(Minecraft.ON_OSX);
         }
      }

   });
   public static final CycleOption<Boolean> TEXT_BACKGROUND = CycleOption.createBinaryOption("options.accessibility.text_background", new TranslatableComponent("options.accessibility.text_background.chat"), new TranslatableComponent("options.accessibility.text_background.everywhere"), (p_168388_) -> {
      return p_168388_.backgroundForChatOnly;
   }, (p_168390_, p_168391_, p_168392_) -> {
      p_168390_.backgroundForChatOnly = p_168392_;
   });
   private static final Component CHAT_TOOLTIP_HIDE_MATCHED_NAMES = new TranslatableComponent("options.hideMatchedNames.tooltip");
   public static final CycleOption<Boolean> AUTO_JUMP = CycleOption.createOnOff("options.autoJump", (p_168382_) -> {
      return p_168382_.autoJump;
   }, (p_168384_, p_168385_, p_168386_) -> {
      p_168384_.autoJump = p_168386_;
   });
   public static final CycleOption<Boolean> AUTO_SUGGESTIONS = CycleOption.createOnOff("options.autoSuggestCommands", (p_168376_) -> {
      return p_168376_.autoSuggestions;
   }, (p_168378_, p_168379_, p_168380_) -> {
      p_168378_.autoSuggestions = p_168380_;
   });
   public static final CycleOption<Boolean> CHAT_COLOR = CycleOption.createOnOff("options.chat.color", (p_168370_) -> {
      return p_168370_.chatColors;
   }, (p_168372_, p_168373_, p_168374_) -> {
      p_168372_.chatColors = p_168374_;
   });
   public static final CycleOption<Boolean> HIDE_MATCHED_NAMES = CycleOption.createOnOff("options.hideMatchedNames", CHAT_TOOLTIP_HIDE_MATCHED_NAMES, (p_168364_) -> {
      return p_168364_.hideMatchedNames;
   }, (p_168366_, p_168367_, p_168368_) -> {
      p_168366_.hideMatchedNames = p_168368_;
   });
   public static final CycleOption<Boolean> CHAT_LINKS = CycleOption.createOnOff("options.chat.links", (p_168358_) -> {
      return p_168358_.chatLinks;
   }, (p_168360_, p_168361_, p_168362_) -> {
      p_168360_.chatLinks = p_168362_;
   });
   public static final CycleOption<Boolean> CHAT_LINKS_PROMPT = CycleOption.createOnOff("options.chat.links.prompt", (p_168352_) -> {
      return p_168352_.chatLinksPrompt;
   }, (p_168354_, p_168355_, p_168356_) -> {
      p_168354_.chatLinksPrompt = p_168356_;
   });
   public static final CycleOption<Boolean> DISCRETE_MOUSE_SCROLL = CycleOption.createOnOff("options.discrete_mouse_scroll", (p_168346_) -> {
      return p_168346_.discreteMouseScroll;
   }, (p_168348_, p_168349_, p_168350_) -> {
      p_168348_.discreteMouseScroll = p_168350_;
   });
   public static final CycleOption<Boolean> ENABLE_VSYNC = CycleOption.createOnOff("options.vsync", (p_168340_) -> {
      return p_168340_.enableVsync;
   }, (p_168342_, p_168343_, p_168344_) -> {
      p_168342_.enableVsync = p_168344_;
      if (Minecraft.getInstance().getWindow() != null) {
         Minecraft.getInstance().getWindow().updateVsync(p_168342_.enableVsync);
      }

   });
   public static final CycleOption<Boolean> ENTITY_SHADOWS = CycleOption.createOnOff("options.entityShadows", (p_168334_) -> {
      return p_168334_.entityShadows;
   }, (p_168336_, p_168337_, p_168338_) -> {
      p_168336_.entityShadows = p_168338_;
   });
   public static final CycleOption<Boolean> FORCE_UNICODE_FONT = CycleOption.createOnOff("options.forceUnicodeFont", (p_168328_) -> {
      return p_168328_.forceUnicodeFont;
   }, (p_168330_, p_168331_, p_168332_) -> {
      p_168330_.forceUnicodeFont = p_168332_;
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.getWindow() != null) {
         minecraft.selectMainFont(p_168332_);
         minecraft.resizeDisplay();
      }

   });
   public static final CycleOption<Boolean> INVERT_MOUSE = CycleOption.createOnOff("options.invertMouse", (p_168322_) -> {
      return p_168322_.invertYMouse;
   }, (p_168324_, p_168325_, p_168326_) -> {
      p_168324_.invertYMouse = p_168326_;
   });
   public static final CycleOption<Boolean> REALMS_NOTIFICATIONS = CycleOption.createOnOff("options.realmsNotifications", (p_168316_) -> {
      return p_168316_.realmsNotifications;
   }, (p_168318_, p_168319_, p_168320_) -> {
      p_168318_.realmsNotifications = p_168320_;
   });
   public static final CycleOption<Boolean> REDUCED_DEBUG_INFO = CycleOption.createOnOff("options.reducedDebugInfo", (p_168304_) -> {
      return p_168304_.reducedDebugInfo;
   }, (p_168306_, p_168307_, p_168308_) -> {
      p_168306_.reducedDebugInfo = p_168308_;
   });
   public static final CycleOption<Boolean> SHOW_SUBTITLES = CycleOption.createOnOff("options.showSubtitles", (p_168292_) -> {
      return p_168292_.showSubtitles;
   }, (p_168294_, p_168295_, p_168296_) -> {
      p_168294_.showSubtitles = p_168296_;
   });
   public static final CycleOption<Boolean> SNOOPER_ENABLED = CycleOption.createOnOff("options.snooper", (p_168280_) -> {
      if (p_168280_.snooperEnabled) {
      }

      return false;
   }, (p_168282_, p_168283_, p_168284_) -> {
      p_168282_.snooperEnabled = p_168284_;
   });
   private static final Component MOVEMENT_TOGGLE = new TranslatableComponent("options.key.toggle");
   private static final Component MOVEMENT_HOLD = new TranslatableComponent("options.key.hold");
   public static final CycleOption<Boolean> TOGGLE_CROUCH = CycleOption.createBinaryOption("key.sneak", MOVEMENT_TOGGLE, MOVEMENT_HOLD, (p_168268_) -> {
      return p_168268_.toggleCrouch;
   }, (p_168270_, p_168271_, p_168272_) -> {
      p_168270_.toggleCrouch = p_168272_;
   });
   public static final CycleOption<Boolean> TOGGLE_SPRINT = CycleOption.createBinaryOption("key.sprint", MOVEMENT_TOGGLE, MOVEMENT_HOLD, (p_168256_) -> {
      return p_168256_.toggleSprint;
   }, (p_168258_, p_168259_, p_168260_) -> {
      p_168258_.toggleSprint = p_168260_;
   });
   public static final CycleOption<Boolean> TOUCHSCREEN = CycleOption.createOnOff("options.touchscreen", (p_168244_) -> {
      return p_168244_.touchscreen;
   }, (p_168246_, p_168247_, p_168248_) -> {
      p_168246_.touchscreen = p_168248_;
   });
   public static final CycleOption<Boolean> USE_FULLSCREEN = CycleOption.createOnOff("options.fullscreen", (p_168232_) -> {
      return p_168232_.fullscreen;
   }, (p_168234_, p_168235_, p_168236_) -> {
      p_168234_.fullscreen = p_168236_;
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.getWindow() != null && minecraft.getWindow().isFullscreen() != p_168234_.fullscreen) {
         minecraft.getWindow().toggleFullScreen();
         p_168234_.fullscreen = minecraft.getWindow().isFullscreen();
      }

   });
   public static final CycleOption<Boolean> VIEW_BOBBING = CycleOption.createOnOff("options.viewBobbing", (p_168217_) -> {
      return p_168217_.bobView;
   }, (p_168219_, p_168220_, p_168221_) -> {
      p_168219_.bobView = p_168221_;
   });
   private static final Component ACCESSIBILITY_TOOLTIP_DARK_MOJANG_BACKGROUND = new TranslatableComponent("options.darkMojangStudiosBackgroundColor.tooltip");
   public static final CycleOption<Boolean> DARK_MOJANG_STUDIOS_BACKGROUND_COLOR = CycleOption.createOnOff("options.darkMojangStudiosBackgroundColor", ACCESSIBILITY_TOOLTIP_DARK_MOJANG_BACKGROUND, (p_168155_) -> {
      return p_168155_.darkMojangStudiosBackground;
   }, (p_168189_, p_168190_, p_168191_) -> {
      p_168189_.darkMojangStudiosBackground = p_168191_;
   });
   private final Component caption;

   public Option(String p_91687_) {
      this.caption = new TranslatableComponent(p_91687_);
   }

   public abstract AbstractWidget createButton(Options p_91719_, int p_91720_, int p_91721_, int p_91722_);

   protected Component getCaption() {
      return this.caption;
   }

   protected Component pixelValueLabel(int p_91716_) {
      return new TranslatableComponent("options.pixel_value", this.getCaption(), p_91716_);
   }

   protected Component percentValueLabel(double p_91763_) {
      return new TranslatableComponent("options.percent_value", this.getCaption(), (int)(p_91763_ * 100.0D));
   }

   protected Component percentAddValueLabel(int p_91744_) {
      return new TranslatableComponent("options.percent_add_value", this.getCaption(), p_91744_);
   }

   protected Component genericValueLabel(Component p_91741_) {
      return new TranslatableComponent("options.generic_value", this.getCaption(), p_91741_);
   }

   protected Component genericValueLabel(int p_91765_) {
      return this.genericValueLabel(new TextComponent(Integer.toString(p_91765_)));
   }
}