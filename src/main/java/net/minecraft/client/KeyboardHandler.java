package net.minecraft.client;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.ClipboardManager;
import com.mojang.blaze3d.platform.InputConstants;
import java.text.MessageFormat;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SimpleOptionsSubScreen;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.optifine.Config;
import net.optifine.reflect.Reflector;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.gui.GuiShaderOptions;

public class KeyboardHandler
{
    public static final int DEBUG_CRASH_TIME = 10000;
    private final Minecraft minecraft;
    private boolean sendRepeatsToGui;
    private final ClipboardManager clipboardManager = new ClipboardManager();
    private long debugCrashKeyTime = -1L;
    private long debugCrashKeyReportedTime = -1L;
    private long debugCrashKeyReportedCount = -1L;
    private boolean handledDebugKey;

    public KeyboardHandler(Minecraft p_90875_)
    {
        this.minecraft = p_90875_;
    }

    private boolean handleChunkDebugKeys(int p_167814_)
    {
        switch (p_167814_)
        {
            case 69:
                this.minecraft.chunkPath = !this.minecraft.chunkPath;
                this.m_167837_("ChunkPath: {0}", this.minecraft.chunkPath ? "shown" : "hidden");
                return true;

            case 76:
                this.minecraft.smartCull = !this.minecraft.smartCull;
                this.m_167837_("SmartCull: {0}", this.minecraft.smartCull ? "enabled" : "disabled");
                return true;

            case 85:
                if (Screen.hasShiftDown())
                {
                    this.minecraft.levelRenderer.killFrustum();
                    this.m_167837_("Killed frustum");
                }
                else
                {
                    this.minecraft.levelRenderer.captureFrustum();
                    this.m_167837_("Captured frustum");
                }

                return true;

            case 86:
                this.minecraft.chunkVisibility = !this.minecraft.chunkVisibility;
                this.m_167837_("ChunkVisibility: {0}", this.minecraft.chunkVisibility ? "enabled" : "disabled");
                return true;

            case 87:
                this.minecraft.wireframe = !this.minecraft.wireframe;
                this.m_167837_("WireFrame: {0}", this.minecraft.wireframe ? "enabled" : "disabled");
                return true;

            default:
                return false;
        }
    }

    private void debugComponent(ChatFormatting p_167825_, Component p_167826_)
    {
        this.minecraft.gui.getChat().addMessage((new TextComponent("")).append((new TranslatableComponent("debug.prefix")).m_130944_(new ChatFormatting[] {p_167825_, ChatFormatting.BOLD})).append(" ").append(p_167826_));
    }

    private void debugFeedbackComponent(Component p_167823_)
    {
        this.debugComponent(ChatFormatting.YELLOW, p_167823_);
    }

    private void m_90913_(String p_90914_, Object... p_90915_)
    {
        this.debugFeedbackComponent(new TranslatableComponent(p_90914_, p_90915_));
    }

    private void m_90948_(String p_90949_, Object... p_90950_)
    {
        this.debugComponent(ChatFormatting.RED, new TranslatableComponent(p_90949_, p_90950_));
    }

    private void m_167837_(String p_167838_, Object... p_167839_)
    {
        this.debugFeedbackComponent(new TextComponent(MessageFormat.format(p_167838_, p_167839_)));
    }

    private boolean handleDebugKeys(int pKey)
    {
        if (this.debugCrashKeyTime > 0L && this.debugCrashKeyTime < Util.getMillis() - 100L)
        {
            return true;
        }
        else
        {
            switch (pKey)
            {
                case 65:
                    this.minecraft.levelRenderer.allChanged();
                    this.m_90913_("debug.reload_chunks.message");
                    return true;

                case 66:
                    boolean flag = !this.minecraft.getEntityRenderDispatcher().shouldRenderHitBoxes();
                    this.minecraft.getEntityRenderDispatcher().setRenderHitBoxes(flag);
                    this.m_90913_(flag ? "debug.show_hitboxes.on" : "debug.show_hitboxes.off");
                    return true;

                case 67:
                    if (this.minecraft.player.isReducedDebugInfo())
                    {
                        return false;
                    }
                    else
                    {
                        ClientPacketListener clientpacketlistener = this.minecraft.player.connection;

                        if (clientpacketlistener == null)
                        {
                            return false;
                        }

                        this.m_90913_("debug.copy_location.message");
                        this.setClipboard(String.format(Locale.ROOT, "/execute in %s run tp @s %.2f %.2f %.2f %.2f %.2f", this.minecraft.player.level.dimension().location(), this.minecraft.player.getX(), this.minecraft.player.getY(), this.minecraft.player.getZ(), this.minecraft.player.getYRot(), this.minecraft.player.getXRot()));
                        return true;
                    }

                case 68:
                    if (this.minecraft.gui != null)
                    {
                        this.minecraft.gui.getChat().clearMessages(false);
                    }

                    return true;

                case 70:
                    Option.RENDER_DISTANCE.set(this.minecraft.options, Mth.clamp((double)(this.minecraft.options.renderDistance + (Screen.hasShiftDown() ? -1 : 1)), Option.RENDER_DISTANCE.getMinValue(), Option.RENDER_DISTANCE.getMaxValue()));
                    this.m_90913_("debug.cycle_renderdistance.message", this.minecraft.options.renderDistance);
                    return true;

                case 71:
                    boolean flag1 = this.minecraft.debugRenderer.switchRenderChunkborder();
                    this.m_90913_(flag1 ? "debug.chunk_boundaries.on" : "debug.chunk_boundaries.off");
                    return true;

                case 72:
                    this.minecraft.options.advancedItemTooltips = !this.minecraft.options.advancedItemTooltips;
                    this.m_90913_(this.minecraft.options.advancedItemTooltips ? "debug.advanced_tooltips.on" : "debug.advanced_tooltips.off");
                    this.minecraft.options.save();
                    return true;

                case 73:
                    if (!this.minecraft.player.isReducedDebugInfo())
                    {
                        this.copyRecreateCommand(this.minecraft.player.hasPermissions(2), !Screen.hasShiftDown());
                    }

                    return true;

                case 76:
                    if (this.minecraft.debugClientMetricsStart(this::debugFeedbackComponent))
                    {
                        this.m_90913_("debug.profiling.start", 10);
                    }

                    return true;

                case 78:
                    if (!this.minecraft.player.hasPermissions(2))
                    {
                        this.m_90913_("debug.creative_spectator.error");
                    }
                    else if (!this.minecraft.player.isSpectator())
                    {
                        this.minecraft.player.chat("/gamemode spectator");
                    }
                    else
                    {
                        this.minecraft.player.chat("/gamemode " + MoreObjects.firstNonNull(this.minecraft.gameMode.getPreviousPlayerMode(), GameType.CREATIVE).getName());
                    }

                    return true;

                case 79:
                    if (Config.isShaders())
                    {
                        GuiShaderOptions guishaderoptions = new GuiShaderOptions((Screen)null, Config.getGameSettings());
                        Config.getMinecraft().setScreen(guishaderoptions);
                    }

                    return true;

                case 80:
                    this.minecraft.options.pauseOnLostFocus = !this.minecraft.options.pauseOnLostFocus;
                    this.minecraft.options.save();
                    this.m_90913_(this.minecraft.options.pauseOnLostFocus ? "debug.pause_focus.on" : "debug.pause_focus.off");
                    return true;

                case 81:
                    this.m_90913_("debug.help.message");
                    ChatComponent chatcomponent = this.minecraft.gui.getChat();
                    chatcomponent.addMessage(new TranslatableComponent("debug.reload_chunks.help"));
                    chatcomponent.addMessage(new TranslatableComponent("debug.show_hitboxes.help"));
                    chatcomponent.addMessage(new TranslatableComponent("debug.copy_location.help"));
                    chatcomponent.addMessage(new TranslatableComponent("debug.clear_chat.help"));
                    chatcomponent.addMessage(new TranslatableComponent("debug.cycle_renderdistance.help"));
                    chatcomponent.addMessage(new TranslatableComponent("debug.chunk_boundaries.help"));
                    chatcomponent.addMessage(new TranslatableComponent("debug.advanced_tooltips.help"));
                    chatcomponent.addMessage(new TranslatableComponent("debug.inspect.help"));
                    chatcomponent.addMessage(new TranslatableComponent("debug.profiling.help"));
                    chatcomponent.addMessage(new TranslatableComponent("debug.creative_spectator.help"));
                    chatcomponent.addMessage(new TranslatableComponent("debug.pause_focus.help"));
                    chatcomponent.addMessage(new TranslatableComponent("debug.help.help"));
                    chatcomponent.addMessage(new TranslatableComponent("debug.reload_resourcepacks.help"));
                    chatcomponent.addMessage(new TranslatableComponent("debug.pause.help"));
                    chatcomponent.addMessage(new TranslatableComponent("debug.gamemodes.help"));
                    return true;

                case 82:
                    if (Config.isShaders())
                    {
                        Shaders.uninit();
                        Shaders.loadShaderPack();
                    }

                    return true;

                case 84:
                    this.m_90913_("debug.reload_resourcepacks.message");
                    this.minecraft.reloadResourcePacks();
                    return true;

                case 86:
                    Minecraft minecraft = Config.getMinecraft();
                    minecraft.levelRenderer.loadVisibleChunksCounter = 1;
                    TextComponent textcomponent = new TextComponent(I18n.m_118938_("of.message.loadingVisibleChunks"));
                    minecraft.gui.getChat().addMessage(textcomponent, 201435902);
                    return true;

                case 293:
                    if (!this.minecraft.player.hasPermissions(2))
                    {
                        this.m_90913_("debug.gamemodes.error");
                    }
                    else
                    {
                        this.minecraft.setScreen(new GameModeSwitcherScreen());
                    }

                    return true;

                default:
                    return false;
            }
        }
    }

    private void copyRecreateCommand(boolean pPrivileged, boolean pAskServer)
    {
        HitResult hitresult = this.minecraft.hitResult;

        if (hitresult != null)
        {
            switch (hitresult.getType())
            {
                case BLOCK:
                    BlockPos blockpos = ((BlockHitResult)hitresult).getBlockPos();
                    BlockState blockstate = this.minecraft.player.level.getBlockState(blockpos);

                    if (pPrivileged)
                    {
                        if (pAskServer)
                        {
                            this.minecraft.player.connection.getDebugQueryHandler().queryBlockEntityTag(blockpos, (p_90944_3_) ->
                            {
                                this.copyCreateBlockCommand(blockstate, blockpos, p_90944_3_);
                                this.m_90913_("debug.inspect.server.block");
                            });
                        }
                        else
                        {
                            BlockEntity blockentity = this.minecraft.player.level.getBlockEntity(blockpos);
                            CompoundTag compoundtag1 = blockentity != null ? blockentity.save(new CompoundTag()) : null;
                            this.copyCreateBlockCommand(blockstate, blockpos, compoundtag1);
                            this.m_90913_("debug.inspect.client.block");
                        }
                    }
                    else
                    {
                        this.copyCreateBlockCommand(blockstate, blockpos, (CompoundTag)null);
                        this.m_90913_("debug.inspect.client.block");
                    }

                    break;

                case ENTITY:
                    Entity entity = ((EntityHitResult)hitresult).getEntity();
                    ResourceLocation resourcelocation = Registry.ENTITY_TYPE.getKey(entity.getType());

                    if (pPrivileged)
                    {
                        if (pAskServer)
                        {
                            this.minecraft.player.connection.getDebugQueryHandler().queryEntityTag(entity.getId(), (p_90918_3_) ->
                            {
                                this.copyCreateEntityCommand(resourcelocation, entity.position(), p_90918_3_);
                                this.m_90913_("debug.inspect.server.entity");
                            });
                        }
                        else
                        {
                            CompoundTag compoundtag = entity.saveWithoutId(new CompoundTag());
                            this.copyCreateEntityCommand(resourcelocation, entity.position(), compoundtag);
                            this.m_90913_("debug.inspect.client.entity");
                        }
                    }
                    else
                    {
                        this.copyCreateEntityCommand(resourcelocation, entity.position(), (CompoundTag)null);
                        this.m_90913_("debug.inspect.client.entity");
                    }
            }
        }
    }

    private void copyCreateBlockCommand(BlockState pState, BlockPos pPos, @Nullable CompoundTag pCompound)
    {
        if (pCompound != null)
        {
            pCompound.remove("x");
            pCompound.remove("y");
            pCompound.remove("z");
            pCompound.remove("id");
        }

        StringBuilder stringbuilder = new StringBuilder(BlockStateParser.serialize(pState));

        if (pCompound != null)
        {
            stringbuilder.append((Object)pCompound);
        }

        String s = String.format(Locale.ROOT, "/setblock %d %d %d %s", pPos.getX(), pPos.getY(), pPos.getZ(), stringbuilder);
        this.setClipboard(s);
    }

    private void copyCreateEntityCommand(ResourceLocation pEntityId, Vec3 pPos, @Nullable CompoundTag pCompound)
    {
        String s;

        if (pCompound != null)
        {
            pCompound.remove("UUID");
            pCompound.remove("Pos");
            pCompound.remove("Dimension");
            String s1 = NbtUtils.toPrettyComponent(pCompound).getString();
            s = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f %s", pEntityId.toString(), pPos.x, pPos.y, pPos.z, s1);
        }
        else
        {
            s = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f", pEntityId.toString(), pPos.x, pPos.y, pPos.z);
        }

        this.setClipboard(s);
    }

    public void keyPress(long pWindowPointer, int p_90895_, int pKey, int pScanCode, int pAction)
    {
        if (pWindowPointer == this.minecraft.getWindow().getWindow())
        {
            if (this.debugCrashKeyTime > 0L)
            {
                if (!InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 67) || !InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292))
                {
                    this.debugCrashKeyTime = -1L;
                }
            }
            else if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 67) && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292))
            {
                this.handledDebugKey = true;
                this.debugCrashKeyTime = Util.getMillis();
                this.debugCrashKeyReportedTime = Util.getMillis();
                this.debugCrashKeyReportedCount = 0L;
            }

            Screen screen = this.minecraft.screen;

            if (!(this.minecraft.screen instanceof ControlsScreen) || ((ControlsScreen)screen).lastKeySelection <= Util.getMillis() - 20L)
            {
                if (pScanCode == 1)
                {
                    if (this.minecraft.options.keyFullscreen.matches(p_90895_, pKey))
                    {
                        this.minecraft.getWindow().toggleFullScreen();
                        this.minecraft.options.fullscreen = this.minecraft.getWindow().isFullscreen();
                        this.minecraft.options.save();
                        return;
                    }

                    if (this.minecraft.options.keyScreenshot.matches(p_90895_, pKey))
                    {
                        if (Screen.hasControlDown())
                        {
                        }

                        Screenshot.grab(this.minecraft.gameDirectory, this.minecraft.getMainRenderTarget(), (p_90916_1_) ->
                        {
                            this.minecraft.execute(() -> {
                                this.minecraft.gui.getChat().addMessage(p_90916_1_);
                            });
                        });
                        return;
                    }
                }
                else if (pScanCode == 0 && this.minecraft.screen instanceof ControlsScreen)
                {
                    ((ControlsScreen)this.minecraft.screen).selectedKey = null;
                }
            }

            if (NarratorChatListener.INSTANCE.isActive())
            {
                boolean flag = screen == null || !(screen.getFocused() instanceof EditBox) || !((EditBox)screen.getFocused()).canConsumeInput();

                if (pScanCode != 0 && p_90895_ == 66 && Screen.hasControlDown() && flag)
                {
                    boolean flag1 = this.minecraft.options.narratorStatus == NarratorStatus.OFF;
                    this.minecraft.options.narratorStatus = NarratorStatus.byId(this.minecraft.options.narratorStatus.getId() + 1);
                    NarratorChatListener.INSTANCE.updateNarratorStatus(this.minecraft.options.narratorStatus);

                    if (screen instanceof SimpleOptionsSubScreen)
                    {
                        ((SimpleOptionsSubScreen)screen).updateNarratorButton();
                    }

                    if (flag1 && screen != null)
                    {
                        screen.narrationEnabled();
                    }
                }
            }

            if (screen != null)
            {
                boolean[] aboolean = new boolean[] {false};
                Screen.wrapScreenError(() ->
                {
                    if (pScanCode != 1 && (pScanCode != 2 || !this.sendRepeatsToGui))
                    {
                        if (pScanCode == 0)
                        {
                            if (Reflector.ForgeHooksClient_onGuiKeyReleasedPre.exists())
                            {
                                aboolean[0] = Reflector.callBoolean(Reflector.ForgeHooksClient_onGuiKeyReleasedPre, this.minecraft.screen, p_90895_, pKey, pAction);

                                if (aboolean[0])
                                {
                                    return;
                                }
                            }

                            aboolean[0] = screen.keyReleased(p_90895_, pKey, pAction);

                            if (Reflector.ForgeHooksClient_onGuiKeyReleasedPost.exists() && !aboolean[0])
                            {
                                aboolean[0] = Reflector.callBoolean(Reflector.ForgeHooksClient_onGuiKeyReleasedPost, this.minecraft.screen, p_90895_, pKey, pAction);
                            }
                        }
                    }
                    else {
                        if (Reflector.ForgeHooksClient_onGuiKeyPressedPre.exists())
                        {
                            aboolean[0] = Reflector.callBoolean(Reflector.ForgeHooksClient_onGuiKeyPressedPre, this.minecraft.screen, p_90895_, pKey, pAction);

                            if (aboolean[0])
                            {
                                return;
                            }
                        }

                        screen.afterKeyboardAction();
                        aboolean[0] = screen.keyPressed(p_90895_, pKey, pAction);

                        if (Reflector.ForgeHooksClient_onGuiKeyPressedPost.exists() && !aboolean[0])
                        {
                            aboolean[0] = Reflector.callBoolean(Reflector.ForgeHooksClient_onGuiKeyPressedPost, this.minecraft.screen, p_90895_, pKey, pAction);
                        }
                    }
                }, "keyPressed event handler", screen.getClass().getCanonicalName());

                if (aboolean[0])
                {
                    return;
                }
            }

            if (this.minecraft.screen == null || this.minecraft.screen.passEvents)
            {
                InputConstants.Key inputconstants$key = InputConstants.getKey(p_90895_, pKey);

                if (pScanCode == 0)
                {
                    KeyMapping.set(inputconstants$key, false);

                    if (p_90895_ == 292)
                    {
                        if (this.handledDebugKey)
                        {
                            this.handledDebugKey = false;
                        }
                        else
                        {
                            this.minecraft.options.renderDebug = !this.minecraft.options.renderDebug;
                            this.minecraft.options.renderDebugCharts = this.minecraft.options.renderDebug && Screen.hasShiftDown();
                            this.minecraft.options.renderFpsChart = this.minecraft.options.renderDebug && Screen.hasAltDown();

                            if (this.minecraft.options.renderDebug)
                            {
                                if (this.minecraft.options.ofLagometer)
                                {
                                    this.minecraft.options.renderFpsChart = true;
                                }

                                if (this.minecraft.options.ofProfiler)
                                {
                                    this.minecraft.options.renderDebugCharts = true;
                                }
                            }
                        }
                    }
                }
                else
                {
                    if (p_90895_ == 293 && this.minecraft.gameRenderer != null)
                    {
                        this.minecraft.gameRenderer.togglePostEffect();
                    }

                    boolean flag3 = false;

                    if (this.minecraft.screen == null)
                    {
                        if (p_90895_ == 256)
                        {
                            boolean flag2 = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292);
                            this.minecraft.pauseGame(flag2);
                        }

                        flag3 = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292) && this.handleDebugKeys(p_90895_);
                        this.handledDebugKey |= flag3;

                        if (p_90895_ == 290)
                        {
                            this.minecraft.options.hideGui = !this.minecraft.options.hideGui;
                        }
                    }

                    if (flag3)
                    {
                        KeyMapping.set(inputconstants$key, false);
                    }
                    else
                    {
                        KeyMapping.set(inputconstants$key, true);
                        KeyMapping.click(inputconstants$key);
                    }

                    if (this.minecraft.options.renderDebugCharts && p_90895_ >= 48 && p_90895_ <= 57)
                    {
                        this.minecraft.debugFpsMeterKeyPress(p_90895_ - 48);
                    }
                }
            }

            Reflector.ForgeHooksClient_fireKeyInput.call(p_90895_, pKey, pScanCode, pAction);
        }
    }

    private void charTyped(long pWindowPointer, int p_90891_, int pCodePoint)
    {
        if (pWindowPointer == this.minecraft.getWindow().getWindow())
        {
            GuiEventListener guieventlistener = this.minecraft.screen;

            if (guieventlistener != null && this.minecraft.getOverlay() == null)
            {
                if (Character.charCount(p_90891_) == 1)
                {
                    Screen.wrapScreenError(() ->
                    {
                        if (!Reflector.ForgeHooksClient_onGuiCharTypedPre.exists() || !Reflector.callBoolean(Reflector.ForgeHooksClient_onGuiCharTypedPre, this.minecraft.screen, (char)p_90891_, pCodePoint))
                        {
                            boolean flag = guieventlistener.charTyped((char)p_90891_, pCodePoint);

                            if (Reflector.ForgeHooksClient_onGuiCharTypedPost.exists() && !flag)
                            {
                                Reflector.callBoolean(Reflector.ForgeHooksClient_onGuiCharTypedPost, this.minecraft.screen, (char)p_90891_, pCodePoint);
                            }
                        }
                    }, "charTyped event handler", guieventlistener.getClass().getCanonicalName());
                }
                else
                {
                    for (char c0 : Character.toChars(p_90891_))
                    {
                        Screen.wrapScreenError(() ->
                        {
                            if (!Reflector.ForgeHooksClient_onGuiCharTypedPre.exists() || !Reflector.callBoolean(Reflector.ForgeHooksClient_onGuiCharTypedPre, this.minecraft.screen, c0, pCodePoint))
                            {
                                boolean flag = guieventlistener.charTyped(c0, pCodePoint);

                                if (Reflector.ForgeHooksClient_onGuiCharTypedPost.exists() && !flag)
                                {
                                    Reflector.callBoolean(Reflector.ForgeHooksClient_onGuiCharTypedPost, this.minecraft.screen, c0, pCodePoint);
                                }
                            }
                        }, "charTyped event handler", guieventlistener.getClass().getCanonicalName());
                    }
                }
            }
        }
    }

    public void setSendRepeatsToGui(boolean pRepeatEvents)
    {
        this.sendRepeatsToGui = pRepeatEvents;
    }

    public void setup(long pWindow)
    {
        InputConstants.setupKeyboardCallbacks(pWindow, (p_90938_1_, p_90938_3_, p_90938_4_, p_90938_5_, p_90938_6_) ->
        {
            this.minecraft.execute(() -> {
                this.keyPress(p_90938_1_, p_90938_3_, p_90938_4_, p_90938_5_, p_90938_6_);
            });
        }, (p_90934_1_, p_90934_3_, p_90934_4_) ->
        {
            this.minecraft.execute(() -> {
                this.charTyped(p_90934_1_, p_90934_3_, p_90934_4_);
            });
        });
    }

    public String getClipboard()
    {
        return this.clipboardManager.getClipboard(this.minecraft.getWindow().getWindow(), (p_90877_1_, p_90877_2_) ->
        {
            if (p_90877_1_ != 65545)
            {
                this.minecraft.getWindow().defaultErrorCallback(p_90877_1_, p_90877_2_);
            }
        });
    }

    public void setClipboard(String pString)
    {
        if (!pString.isEmpty())
        {
            this.clipboardManager.setClipboard(this.minecraft.getWindow().getWindow(), pString);
        }
    }

    public void tick()
    {
        if (this.debugCrashKeyTime > 0L)
        {
            long i = Util.getMillis();
            long j = 10000L - (i - this.debugCrashKeyTime);
            long k = i - this.debugCrashKeyReportedTime;

            if (j < 0L)
            {
                if (Screen.hasControlDown())
                {
                    Blaze3D.youJustLostTheGame();
                }

                throw new ReportedException(new CrashReport("Manually triggered debug crash", new Throwable()));
            }

            if (k >= 1000L)
            {
                if (this.debugCrashKeyReportedCount == 0L)
                {
                    this.m_90913_("debug.crash.message");
                }
                else
                {
                    this.m_90948_("debug.crash.warning", Mth.ceil((float)j / 1000.0F));
                }

                this.debugCrashKeyReportedTime = i;
                ++this.debugCrashKeyReportedCount;
            }
        }
    }
}
