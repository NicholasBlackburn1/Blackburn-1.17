package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.DebugQueryHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.DemoIntroScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.achievement.StatsUpdateListener;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.debug.BeeDebugRenderer;
import net.minecraft.client.renderer.debug.BrainDebugRenderer;
import net.minecraft.client.renderer.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.renderer.debug.NeighborsUpdateRenderer;
import net.minecraft.client.renderer.debug.WorldGenAttemptRenderer;
import net.minecraft.client.resources.sounds.BeeAggressiveSoundInstance;
import net.minecraft.client.resources.sounds.BeeFlyingSoundInstance;
import net.minecraft.client.resources.sounds.BeeSoundInstance;
import net.minecraft.client.resources.sounds.GuardianAttackSoundInstance;
import net.minecraft.client.resources.sounds.MinecartSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.searchtree.MutableSearchTree;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.PositionImpl;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddExperienceOrbPacket;
import net.minecraft.network.protocol.game.ClientboundAddMobPacket;
import net.minecraft.network.protocol.game.ClientboundAddPaintingPacket;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundAddVibrationSignalPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.network.protocol.game.ClientboundBlockBreakAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPingPacket;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEndPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEnterPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import net.minecraft.tags.StaticTags;
import net.minecraft.tags.TagContainer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationPath;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientPacketListener implements ClientGamePacketListener
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Component GENERIC_DISCONNECT_MESSAGE = new TranslatableComponent("disconnect.lost");
    private final Connection connection;
    private final GameProfile localGameProfile;
    private final Screen callbackScreen;
    private final Minecraft minecraft;
    private ClientLevel level;
    private ClientLevel.ClientLevelData levelData;
    private boolean started;
    private final Map<UUID, PlayerInfo> playerInfoMap = Maps.newHashMap();
    private final ClientAdvancements advancements;
    private final ClientSuggestionProvider suggestionsProvider;
    private TagContainer tags = TagContainer.EMPTY;
    private final DebugQueryHandler debugQueryHandler = new DebugQueryHandler(this);
    private int serverChunkRadius = 3;
    private final Random random = new Random();
    private CommandDispatcher<SharedSuggestionProvider> commands = new CommandDispatcher<>();
    private final RecipeManager recipeManager = new RecipeManager();
    private final UUID id = UUID.randomUUID();
    private Set<ResourceKey<Level>> levels;
    private RegistryAccess registryAccess = RegistryAccess.builtin();

    public ClientPacketListener(Minecraft p_104906_, Screen p_104907_, Connection p_104908_, GameProfile p_104909_)
    {
        this.minecraft = p_104906_;
        this.callbackScreen = p_104907_;
        this.connection = p_104908_;
        this.localGameProfile = p_104909_;
        this.advancements = new ClientAdvancements(p_104906_);
        this.suggestionsProvider = new ClientSuggestionProvider(this, p_104906_);
    }

    public ClientSuggestionProvider getSuggestionsProvider()
    {
        return this.suggestionsProvider;
    }

    public void cleanup()
    {
        this.level = null;
    }

    public RecipeManager getRecipeManager()
    {
        return this.recipeManager;
    }

    public void handleLogin(ClientboundLoginPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.gameMode = new MultiPlayerGameMode(this.minecraft, this);

        if (!this.connection.isMemoryConnection())
        {
            StaticTags.resetAllToEmpty();
        }

        List<ResourceKey<Level>> list = Lists.newArrayList(pPacket.levels());
        Collections.shuffle(list);
        this.levels = Sets.newLinkedHashSet(list);
        this.registryAccess = pPacket.registryAccess();
        ResourceKey<Level> resourcekey = pPacket.getDimension();
        DimensionType dimensiontype = pPacket.getDimensionType();
        this.serverChunkRadius = pPacket.getChunkRadius();
        boolean flag = pPacket.isDebug();
        boolean flag1 = pPacket.isFlat();
        ClientLevel.ClientLevelData clientlevel$clientleveldata = new ClientLevel.ClientLevelData(Difficulty.NORMAL, pPacket.isHardcore(), flag1);
        this.levelData = clientlevel$clientleveldata;
        this.level = new ClientLevel(this, clientlevel$clientleveldata, resourcekey, dimensiontype, this.serverChunkRadius, this.minecraft::getProfiler, this.minecraft.levelRenderer, flag, pPacket.getSeed());
        this.minecraft.setLevel(this.level);

        if (this.minecraft.player == null)
        {
            this.minecraft.player = this.minecraft.gameMode.createPlayer(this.level, new StatsCounter(), new ClientRecipeBook());
            this.minecraft.player.setYRot(-180.0F);

            if (this.minecraft.getSingleplayerServer() != null)
            {
                this.minecraft.getSingleplayerServer().setUUID(this.minecraft.player.getUUID());
            }
        }

        this.minecraft.debugRenderer.clear();
        this.minecraft.player.resetPos();
        int i = pPacket.getPlayerId();
        this.minecraft.player.setId(i);
        this.level.addPlayer(i, this.minecraft.player);
        this.minecraft.player.input = new KeyboardInput(this.minecraft.options);
        this.minecraft.gameMode.adjustPlayer(this.minecraft.player);
        this.minecraft.cameraEntity = this.minecraft.player;
        this.minecraft.setScreen(new ReceivingLevelScreen());
        this.minecraft.player.setReducedDebugInfo(pPacket.isReducedDebugInfo());
        this.minecraft.player.setShowDeathScreen(pPacket.shouldShowDeathScreen());
        this.minecraft.gameMode.setLocalMode(pPacket.getGameType(), pPacket.getPreviousGameType());
        this.minecraft.options.broadcastOptions();
        this.connection.send(new ServerboundCustomPayloadPacket(ServerboundCustomPayloadPacket.BRAND, (new FriendlyByteBuf(Unpooled.buffer())).writeUtf(ClientBrandRetriever.getClientModName())));
        this.minecraft.getGame().onStartGameSession();
    }

    public void handleAddEntity(ClientboundAddEntityPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        EntityType<?> entitytype = pPacket.getType();
        Entity entity = entitytype.create(this.level);

        if (entity != null)
        {
            entity.recreateFromPacket(pPacket);
            int i = pPacket.getId();
            this.level.putNonPlayerEntity(i, entity);

            if (entity instanceof AbstractMinecart)
            {
                this.minecraft.getSoundManager().play(new MinecartSoundInstance((AbstractMinecart)entity));
            }
        }
    }

    public void handleAddExperienceOrb(ClientboundAddExperienceOrbPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        double d0 = pPacket.getX();
        double d1 = pPacket.getY();
        double d2 = pPacket.getZ();
        Entity entity = new ExperienceOrb(this.level, d0, d1, d2, pPacket.getValue());
        entity.setPacketCoordinates(d0, d1, d2);
        entity.setYRot(0.0F);
        entity.setXRot(0.0F);
        entity.setId(pPacket.getId());
        this.level.putNonPlayerEntity(pPacket.getId(), entity);
    }

    public void handleAddVibrationSignal(ClientboundAddVibrationSignalPacket p_171763_)
    {
        PacketUtils.ensureRunningOnSameThread(p_171763_, this, this.minecraft);
        VibrationPath vibrationpath = p_171763_.getVibrationPath();
        BlockPos blockpos = vibrationpath.getOrigin();
        this.level.addAlwaysVisibleParticle(new VibrationParticleOption(vibrationpath), true, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
    }

    public void handleAddPainting(ClientboundAddPaintingPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Painting painting = new Painting(this.level, pPacket.getPos(), pPacket.getDirection(), pPacket.getMotive());
        painting.setId(pPacket.getId());
        painting.setUUID(pPacket.getUUID());
        this.level.putNonPlayerEntity(pPacket.getId(), painting);
    }

    public void handleSetEntityMotion(ClientboundSetEntityMotionPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getId());

        if (entity != null)
        {
            entity.lerpMotion((double)pPacket.getXa() / 8000.0D, (double)pPacket.getYa() / 8000.0D, (double)pPacket.getZa() / 8000.0D);
        }
    }

    public void handleSetEntityData(ClientboundSetEntityDataPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getId());

        if (entity != null && pPacket.getUnpackedData() != null)
        {
            entity.getEntityData().assignValues(pPacket.getUnpackedData());
        }
    }

    public void handleAddPlayer(ClientboundAddPlayerPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        double d0 = pPacket.getX();
        double d1 = pPacket.getY();
        double d2 = pPacket.getZ();
        float f = (float)(pPacket.getyRot() * 360) / 256.0F;
        float f1 = (float)(pPacket.getxRot() * 360) / 256.0F;
        int i = pPacket.getEntityId();
        RemotePlayer remoteplayer = new RemotePlayer(this.minecraft.level, this.getPlayerInfo(pPacket.getPlayerId()).getProfile());
        remoteplayer.setId(i);
        remoteplayer.setPacketCoordinates(d0, d1, d2);
        remoteplayer.absMoveTo(d0, d1, d2, f, f1);
        remoteplayer.setOldPosAndRot();
        this.level.addPlayer(i, remoteplayer);
    }

    public void handleTeleportEntity(ClientboundTeleportEntityPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getId());

        if (entity != null)
        {
            double d0 = pPacket.getX();
            double d1 = pPacket.getY();
            double d2 = pPacket.getZ();
            entity.setPacketCoordinates(d0, d1, d2);

            if (!entity.isControlledByLocalInstance())
            {
                float f = (float)(pPacket.getyRot() * 360) / 256.0F;
                float f1 = (float)(pPacket.getxRot() * 360) / 256.0F;
                entity.lerpTo(d0, d1, d2, f, f1, 3, true);
                entity.setOnGround(pPacket.isOnGround());
            }
        }
    }

    public void handleSetCarriedItem(ClientboundSetCarriedItemPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);

        if (Inventory.isHotbarSlot(pPacket.getSlot()))
        {
            this.minecraft.player.getInventory().selected = pPacket.getSlot();
        }
    }

    public void handleMoveEntity(ClientboundMoveEntityPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = pPacket.getEntity(this.level);

        if (entity != null)
        {
            if (!entity.isControlledByLocalInstance())
            {
                if (pPacket.hasPosition())
                {
                    Vec3 vec3 = pPacket.updateEntityPosition(entity.getPacketCoordinates());
                    entity.setPacketCoordinates(vec3);
                    float f = pPacket.hasRotation() ? (float)(pPacket.getyRot() * 360) / 256.0F : entity.getYRot();
                    float f1 = pPacket.hasRotation() ? (float)(pPacket.getxRot() * 360) / 256.0F : entity.getXRot();
                    entity.lerpTo(vec3.x(), vec3.y(), vec3.z(), f, f1, 3, false);
                }
                else if (pPacket.hasRotation())
                {
                    float f2 = (float)(pPacket.getyRot() * 360) / 256.0F;
                    float f3 = (float)(pPacket.getxRot() * 360) / 256.0F;
                    entity.lerpTo(entity.getX(), entity.getY(), entity.getZ(), f2, f3, 3, false);
                }

                entity.setOnGround(pPacket.isOnGround());
            }
        }
    }

    public void handleRotateMob(ClientboundRotateHeadPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = pPacket.getEntity(this.level);

        if (entity != null)
        {
            float f = (float)(pPacket.getYHeadRot() * 360) / 256.0F;
            entity.lerpHeadTo(f, 3);
        }
    }

    public void m_182047_(ClientboundRemoveEntitiesPacket p_182633_)
    {
        PacketUtils.ensureRunningOnSameThread(p_182633_, this, this.minecraft);
        p_182633_.m_182730_().forEach((int p_182600_) ->
        {
            this.level.removeEntity(p_182600_, Entity.RemovalReason.DISCARDED);
        });
    }

    public void handleMovePlayer(ClientboundPlayerPositionPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Player player = this.minecraft.player;

        if (pPacket.requestDismountVehicle())
        {
            player.removeVehicle();
        }

        Vec3 vec3 = player.getDeltaMovement();
        boolean flag = pPacket.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.X);
        boolean flag1 = pPacket.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.Y);
        boolean flag2 = pPacket.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.Z);
        double d0;
        double d1;

        if (flag)
        {
            d0 = vec3.x();
            d1 = player.getX() + pPacket.getX();
            player.xOld += pPacket.getX();
        }
        else
        {
            d0 = 0.0D;
            d1 = pPacket.getX();
            player.xOld = d1;
        }

        double d2;
        double d3;

        if (flag1)
        {
            d2 = vec3.y();
            d3 = player.getY() + pPacket.getY();
            player.yOld += pPacket.getY();
        }
        else
        {
            d2 = 0.0D;
            d3 = pPacket.getY();
            player.yOld = d3;
        }

        double d4;
        double d5;

        if (flag2)
        {
            d4 = vec3.z();
            d5 = player.getZ() + pPacket.getZ();
            player.zOld += pPacket.getZ();
        }
        else
        {
            d4 = 0.0D;
            d5 = pPacket.getZ();
            player.zOld = d5;
        }

        player.setPosRaw(d1, d3, d5);
        player.xo = d1;
        player.yo = d3;
        player.zo = d5;
        player.setDeltaMovement(d0, d2, d4);
        float f = pPacket.getYRot();
        float f1 = pPacket.getXRot();

        if (pPacket.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.X_ROT))
        {
            f1 += player.getXRot();
        }

        if (pPacket.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.Y_ROT))
        {
            f += player.getYRot();
        }

        player.absMoveTo(d1, d3, d5, f, f1);
        this.connection.send(new ServerboundAcceptTeleportationPacket(pPacket.getId()));
        this.connection.send(new ServerboundMovePlayerPacket.PosRot(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), false));

        if (!this.started)
        {
            this.started = true;
            this.minecraft.setScreen((Screen)null);
        }
    }

    public void handleChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        int i = 19 | (pPacket.shouldSuppressLightUpdates() ? 128 : 0);
        pPacket.runUpdates((p_104922_, p_104923_) ->
        {
            this.level.setBlock(p_104922_, p_104923_, i);
        });
    }

    public void handleLevelChunk(ClientboundLevelChunkPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        int i = pPacket.getX();
        int j = pPacket.getZ();
        ChunkBiomeContainer chunkbiomecontainer = new ChunkBiomeContainer(this.registryAccess.registryOrThrow(Registry.BIOME_REGISTRY), this.level, pPacket.getBiomes());
        LevelChunk levelchunk = this.level.getChunkSource().replaceWithPacketData(i, j, chunkbiomecontainer, pPacket.getReadBuffer(), pPacket.getHeightmaps(), pPacket.getAvailableSections());

        for (int k = this.level.getMinSection(); k < this.level.getMaxSection(); ++k)
        {
            this.level.setSectionDirtyWithNeighbors(i, k, j);
        }

        if (levelchunk != null)
        {
            for (CompoundTag compoundtag : pPacket.getBlockEntitiesTags())
            {
                BlockPos blockpos = new BlockPos(compoundtag.getInt("x"), compoundtag.getInt("y"), compoundtag.getInt("z"));
                BlockEntity blockentity = levelchunk.getBlockEntity(blockpos, LevelChunk.EntityCreationType.IMMEDIATE);

                if (blockentity != null)
                {
                    blockentity.load(compoundtag);
                }
            }
        }
    }

    public void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        int i = pPacket.getX();
        int j = pPacket.getZ();
        ClientChunkCache clientchunkcache = this.level.getChunkSource();
        clientchunkcache.drop(i, j);
        LevelLightEngine levellightengine = clientchunkcache.getLightEngine();

        for (int k = this.level.getMinSection(); k < this.level.getMaxSection(); ++k)
        {
            this.level.setSectionDirtyWithNeighbors(i, k, j);
            levellightengine.updateSectionStatus(SectionPos.of(i, k, j), true);
        }

        levellightengine.enableLightSources(new ChunkPos(i, j), false);
    }

    public void handleBlockUpdate(ClientboundBlockUpdatePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.level.setKnownState(pPacket.getPos(), pPacket.getBlockState());
    }

    public void handleDisconnect(ClientboundDisconnectPacket pPacket)
    {
        this.connection.disconnect(pPacket.getReason());
    }

    public void onDisconnect(Component pReason)
    {
        this.minecraft.clearLevel();

        if (this.callbackScreen != null)
        {
            if (this.callbackScreen instanceof RealmsScreen)
            {
                this.minecraft.setScreen(new DisconnectedRealmsScreen(this.callbackScreen, GENERIC_DISCONNECT_MESSAGE, pReason));
            }
            else
            {
                this.minecraft.setScreen(new DisconnectedScreen(this.callbackScreen, GENERIC_DISCONNECT_MESSAGE, pReason));
            }
        }
        else
        {
            this.minecraft.setScreen(new DisconnectedScreen(new JoinMultiplayerScreen(new TitleScreen()), GENERIC_DISCONNECT_MESSAGE, pReason));
        }
    }

    public void send(Packet<?> pPacket)
    {
        this.connection.send(pPacket);
    }

    public void handleTakeItemEntity(ClientboundTakeItemEntityPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getItemId());
        LivingEntity livingentity = (LivingEntity)this.level.getEntity(pPacket.getPlayerId());

        if (livingentity == null)
        {
            livingentity = this.minecraft.player;
        }

        if (entity != null)
        {
            if (entity instanceof ExperienceOrb)
            {
                this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.1F, (this.random.nextFloat() - this.random.nextFloat()) * 0.35F + 0.9F, false);
            }
            else
            {
                this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, (this.random.nextFloat() - this.random.nextFloat()) * 1.4F + 2.0F, false);
            }

            this.minecraft.particleEngine.add(new ItemPickupParticle(this.minecraft.getEntityRenderDispatcher(), this.minecraft.renderBuffers(), this.level, entity, livingentity));

            if (entity instanceof ItemEntity)
            {
                ItemEntity itementity = (ItemEntity)entity;
                ItemStack itemstack = itementity.getItem();
                itemstack.shrink(pPacket.getAmount());

                if (itemstack.isEmpty())
                {
                    this.level.removeEntity(pPacket.getItemId(), Entity.RemovalReason.DISCARDED);
                }
            }
            else if (!(entity instanceof ExperienceOrb))
            {
                this.level.removeEntity(pPacket.getItemId(), Entity.RemovalReason.DISCARDED);
            }
        }
    }

    public void handleChat(ClientboundChatPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.gui.handleChat(pPacket.getType(), pPacket.getMessage(), pPacket.getSender());
    }

    public void handleAnimate(ClientboundAnimatePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getId());

        if (entity != null)
        {
            if (pPacket.getAction() == 0)
            {
                LivingEntity livingentity = (LivingEntity)entity;
                livingentity.swing(InteractionHand.MAIN_HAND);
            }
            else if (pPacket.getAction() == 3)
            {
                LivingEntity livingentity1 = (LivingEntity)entity;
                livingentity1.swing(InteractionHand.OFF_HAND);
            }
            else if (pPacket.getAction() == 1)
            {
                entity.animateHurt();
            }
            else if (pPacket.getAction() == 2)
            {
                Player player = (Player)entity;
                player.stopSleepInBed(false, false);
            }
            else if (pPacket.getAction() == 4)
            {
                this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.CRIT);
            }
            else if (pPacket.getAction() == 5)
            {
                this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.ENCHANTED_HIT);
            }
        }
    }

    public void handleAddMob(ClientboundAddMobPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        LivingEntity livingentity = (LivingEntity)EntityType.create(pPacket.getType(), this.level);

        if (livingentity != null)
        {
            livingentity.recreateFromPacket(pPacket);
            this.level.putNonPlayerEntity(pPacket.getId(), livingentity);

            if (livingentity instanceof Bee)
            {
                boolean flag = ((Bee)livingentity).isAngry();
                BeeSoundInstance beesoundinstance;

                if (flag)
                {
                    beesoundinstance = new BeeAggressiveSoundInstance((Bee)livingentity);
                }
                else
                {
                    beesoundinstance = new BeeFlyingSoundInstance((Bee)livingentity);
                }

                this.minecraft.getSoundManager().queueTickingSound(beesoundinstance);
            }
        }
        else
        {
            LOGGER.warn("Skipping Entity with id {}", (int)pPacket.getType());
        }
    }

    public void handleSetTime(ClientboundSetTimePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.level.setGameTime(pPacket.getGameTime());
        this.minecraft.level.setDayTime(pPacket.getDayTime());
    }

    public void handleSetSpawn(ClientboundSetDefaultSpawnPositionPacket p_105084_)
    {
        PacketUtils.ensureRunningOnSameThread(p_105084_, this, this.minecraft);
        this.minecraft.level.setDefaultSpawnPos(p_105084_.getPos(), p_105084_.getAngle());
    }

    public void handleSetEntityPassengersPacket(ClientboundSetPassengersPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getVehicle());

        if (entity == null)
        {
            LOGGER.warn("Received passengers for unknown entity");
        }
        else
        {
            boolean flag = entity.hasIndirectPassenger(this.minecraft.player);
            entity.ejectPassengers();

            for (int i : pPacket.getPassengers())
            {
                Entity entity1 = this.level.getEntity(i);

                if (entity1 != null)
                {
                    entity1.startRiding(entity, true);

                    if (entity1 == this.minecraft.player && !flag)
                    {
                        this.minecraft.gui.setOverlayMessage(new TranslatableComponent("mount.onboard", this.minecraft.options.keyShift.getTranslatedKeyMessage()), false);
                    }
                }
            }
        }
    }

    public void handleEntityLinkPacket(ClientboundSetEntityLinkPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getSourceId());

        if (entity instanceof Mob)
        {
            ((Mob)entity).setDelayedLeashHolderId(pPacket.getDestId());
        }
    }

    private static ItemStack findTotem(Player pPlayer)
    {
        for (InteractionHand interactionhand : InteractionHand.values())
        {
            ItemStack itemstack = pPlayer.getItemInHand(interactionhand);

            if (itemstack.is(Items.TOTEM_OF_UNDYING))
            {
                return itemstack;
            }
        }

        return new ItemStack(Items.TOTEM_OF_UNDYING);
    }

    public void handleEntityEvent(ClientboundEntityEventPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = pPacket.getEntity(this.level);

        if (entity != null)
        {
            if (pPacket.getEventId() == 21)
            {
                this.minecraft.getSoundManager().play(new GuardianAttackSoundInstance((Guardian)entity));
            }
            else if (pPacket.getEventId() == 35)
            {
                int i = 40;
                this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
                this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0F, 1.0F, false);

                if (entity == this.minecraft.player)
                {
                    this.minecraft.gameRenderer.displayItemActivation(findTotem(this.minecraft.player));
                }
            }
            else
            {
                entity.handleEntityEvent(pPacket.getEventId());
            }
        }
    }

    public void handleSetHealth(ClientboundSetHealthPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.player.hurtTo(pPacket.getHealth());
        this.minecraft.player.getFoodData().setFoodLevel(pPacket.getFood());
        this.minecraft.player.getFoodData().setSaturation(pPacket.getSaturation());
    }

    public void handleSetExperience(ClientboundSetExperiencePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.player.setExperienceValues(pPacket.getExperienceProgress(), pPacket.getTotalExperience(), pPacket.getExperienceLevel());
    }

    public void handleRespawn(ClientboundRespawnPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        ResourceKey<Level> resourcekey = pPacket.getDimension();
        DimensionType dimensiontype = pPacket.getDimensionType();
        LocalPlayer localplayer = this.minecraft.player;
        int i = localplayer.getId();
        this.started = false;

        if (resourcekey != localplayer.level.dimension())
        {
            Scoreboard scoreboard = this.level.getScoreboard();
            Map<String, MapItemSavedData> map = this.level.getAllMapData();
            boolean flag = pPacket.isDebug();
            boolean flag1 = pPacket.isFlat();
            ClientLevel.ClientLevelData clientlevel$clientleveldata = new ClientLevel.ClientLevelData(this.levelData.getDifficulty(), this.levelData.isHardcore(), flag1);
            this.levelData = clientlevel$clientleveldata;
            this.level = new ClientLevel(this, clientlevel$clientleveldata, resourcekey, dimensiontype, this.serverChunkRadius, this.minecraft::getProfiler, this.minecraft.levelRenderer, flag, pPacket.getSeed());
            this.level.setScoreboard(scoreboard);
            this.level.addMapData(map);
            this.minecraft.setLevel(this.level);
            this.minecraft.setScreen(new ReceivingLevelScreen());
        }

        String s = localplayer.getServerBrand();
        this.minecraft.cameraEntity = null;
        LocalPlayer localplayer1 = this.minecraft.gameMode.createPlayer(this.level, localplayer.getStats(), localplayer.getRecipeBook(), localplayer.isShiftKeyDown(), localplayer.isSprinting());
        localplayer1.setId(i);
        this.minecraft.player = localplayer1;

        if (resourcekey != localplayer.level.dimension())
        {
            this.minecraft.getMusicManager().stopPlaying();
        }

        this.minecraft.cameraEntity = localplayer1;
        localplayer1.getEntityData().assignValues(localplayer.getEntityData().getAll());

        if (pPacket.shouldKeepAllPlayerData())
        {
            localplayer1.getAttributes().assignValues(localplayer.getAttributes());
        }

        localplayer1.resetPos();
        localplayer1.setServerBrand(s);
        this.level.addPlayer(i, localplayer1);
        localplayer1.setYRot(-180.0F);
        localplayer1.input = new KeyboardInput(this.minecraft.options);
        this.minecraft.gameMode.adjustPlayer(localplayer1);
        localplayer1.setReducedDebugInfo(localplayer.isReducedDebugInfo());
        localplayer1.setShowDeathScreen(localplayer.shouldShowDeathScreen());

        if (this.minecraft.screen instanceof DeathScreen)
        {
            this.minecraft.setScreen((Screen)null);
        }

        this.minecraft.gameMode.setLocalMode(pPacket.getPlayerGameType(), pPacket.getPreviousPlayerGameType());
    }

    public void handleExplosion(ClientboundExplodePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Explosion explosion = new Explosion(this.minecraft.level, (Entity)null, pPacket.getX(), pPacket.getY(), pPacket.getZ(), pPacket.getPower(), pPacket.getToBlow());
        explosion.finalizeExplosion(true);
        this.minecraft.player.setDeltaMovement(this.minecraft.player.getDeltaMovement().add((double)pPacket.getKnockbackX(), (double)pPacket.getKnockbackY(), (double)pPacket.getKnockbackZ()));
    }

    public void handleHorseScreenOpen(ClientboundHorseScreenOpenPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getEntityId());

        if (entity instanceof AbstractHorse)
        {
            LocalPlayer localplayer = this.minecraft.player;
            AbstractHorse abstracthorse = (AbstractHorse)entity;
            SimpleContainer simplecontainer = new SimpleContainer(pPacket.getSize());
            HorseInventoryMenu horseinventorymenu = new HorseInventoryMenu(pPacket.getContainerId(), localplayer.getInventory(), simplecontainer, abstracthorse);
            localplayer.containerMenu = horseinventorymenu;
            this.minecraft.setScreen(new HorseInventoryScreen(horseinventorymenu, localplayer.getInventory(), abstracthorse));
        }
    }

    public void handleOpenScreen(ClientboundOpenScreenPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        MenuScreens.create(pPacket.getType(), this.minecraft, pPacket.getContainerId(), pPacket.getTitle());
    }

    public void handleContainerSetSlot(ClientboundContainerSetSlotPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Player player = this.minecraft.player;
        ItemStack itemstack = pPacket.getItem();
        int i = pPacket.getSlot();
        this.minecraft.getTutorial().onGetItem(itemstack);

        if (pPacket.getContainerId() == -1)
        {
            if (!(this.minecraft.screen instanceof CreativeModeInventoryScreen))
            {
                player.containerMenu.setCarried(itemstack);
            }
        }
        else if (pPacket.getContainerId() == -2)
        {
            player.getInventory().setItem(i, itemstack);
        }
        else
        {
            boolean flag = false;

            if (this.minecraft.screen instanceof CreativeModeInventoryScreen)
            {
                CreativeModeInventoryScreen creativemodeinventoryscreen = (CreativeModeInventoryScreen)this.minecraft.screen;
                flag = creativemodeinventoryscreen.getSelectedTab() != CreativeModeTab.TAB_INVENTORY.getId();
            }

            if (pPacket.getContainerId() == 0 && InventoryMenu.isHotbarSlot(i))
            {
                if (!itemstack.isEmpty())
                {
                    ItemStack itemstack1 = player.inventoryMenu.getSlot(i).getItem();

                    if (itemstack1.isEmpty() || itemstack1.getCount() < itemstack.getCount())
                    {
                        itemstack.setPopTime(5);
                    }
                }

                player.inventoryMenu.m_182406_(i, pPacket.m_182716_(), itemstack);
            }
            else if (pPacket.getContainerId() == player.containerMenu.containerId && (pPacket.getContainerId() != 0 || !flag))
            {
                player.containerMenu.m_182406_(i, pPacket.m_182716_(), itemstack);
            }
        }
    }

    public void handleContainerContent(ClientboundContainerSetContentPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Player player = this.minecraft.player;

        if (pPacket.getContainerId() == 0)
        {
            player.inventoryMenu.m_182410_(pPacket.m_182709_(), pPacket.getItems(), pPacket.m_182708_());
        }
        else if (pPacket.getContainerId() == player.containerMenu.containerId)
        {
            player.containerMenu.m_182410_(pPacket.m_182709_(), pPacket.getItems(), pPacket.m_182708_());
        }
    }

    public void handleOpenSignEditor(ClientboundOpenSignEditorPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        BlockPos blockpos = pPacket.getPos();
        BlockEntity blockentity = this.level.getBlockEntity(blockpos);

        if (!(blockentity instanceof SignBlockEntity))
        {
            BlockState blockstate = this.level.getBlockState(blockpos);
            blockentity = new SignBlockEntity(blockpos, blockstate);
            blockentity.setLevel(this.level);
        }

        this.minecraft.player.openTextEdit((SignBlockEntity)blockentity);
    }

    public void handleBlockEntityData(ClientboundBlockEntityDataPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        BlockPos blockpos = pPacket.getPos();
        BlockEntity blockentity = this.minecraft.level.getBlockEntity(blockpos);
        int i = pPacket.getType();
        boolean flag = i == 2 && blockentity instanceof CommandBlockEntity;

        if (i == 1 && blockentity instanceof SpawnerBlockEntity || flag || i == 3 && blockentity instanceof BeaconBlockEntity || i == 4 && blockentity instanceof SkullBlockEntity || i == 6 && blockentity instanceof BannerBlockEntity || i == 7 && blockentity instanceof StructureBlockEntity || i == 8 && blockentity instanceof TheEndGatewayBlockEntity || i == 9 && blockentity instanceof SignBlockEntity || i == 11 && blockentity instanceof BedBlockEntity || i == 5 && blockentity instanceof ConduitBlockEntity || i == 12 && blockentity instanceof JigsawBlockEntity || i == 13 && blockentity instanceof CampfireBlockEntity || i == 14 && blockentity instanceof BeehiveBlockEntity)
        {
            blockentity.load(pPacket.getTag());
        }

        if (flag && this.minecraft.screen instanceof CommandBlockEditScreen)
        {
            ((CommandBlockEditScreen)this.minecraft.screen).updateGui();
        }
    }

    public void handleContainerSetData(ClientboundContainerSetDataPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Player player = this.minecraft.player;

        if (player.containerMenu != null && player.containerMenu.containerId == pPacket.getContainerId())
        {
            player.containerMenu.setData(pPacket.getId(), pPacket.getValue());
        }
    }

    public void handleSetEquipment(ClientboundSetEquipmentPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getEntity());

        if (entity != null)
        {
            pPacket.getSlots().forEach((p_104926_) ->
            {
                entity.setItemSlot(p_104926_.getFirst(), p_104926_.getSecond());
            });
        }
    }

    public void handleContainerClose(ClientboundContainerClosePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.player.clientSideCloseContainer();
    }

    public void handleBlockEvent(ClientboundBlockEventPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.level.blockEvent(pPacket.getPos(), pPacket.getBlock(), pPacket.getB0(), pPacket.getB1());
    }

    public void handleBlockDestruction(ClientboundBlockDestructionPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.level.destroyBlockProgress(pPacket.getId(), pPacket.getPos(), pPacket.getProgress());
    }

    public void handleGameEvent(ClientboundGameEventPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Player player = this.minecraft.player;
        ClientboundGameEventPacket.Type clientboundgameeventpacket$type = pPacket.getEvent();
        float f = pPacket.getParam();
        int i = Mth.floor(f + 0.5F);

        if (clientboundgameeventpacket$type == ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE)
        {
            player.displayClientMessage(new TranslatableComponent("block.minecraft.spawn.not_valid"), false);
        }
        else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.START_RAINING)
        {
            this.level.getLevelData().setRaining(true);
            this.level.setRainLevel(0.0F);
        }
        else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.STOP_RAINING)
        {
            this.level.getLevelData().setRaining(false);
            this.level.setRainLevel(1.0F);
        }
        else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.CHANGE_GAME_MODE)
        {
            this.minecraft.gameMode.setLocalMode(GameType.byId(i));
        }
        else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.WIN_GAME)
        {
            if (i == 0)
            {
                this.minecraft.player.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
                this.minecraft.setScreen(new ReceivingLevelScreen());
            }
            else if (i == 1)
            {
                this.minecraft.setScreen(new WinScreen(true, () ->
                {
                    this.minecraft.player.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
                }));
            }
        }
        else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.DEMO_EVENT)
        {
            Options options = this.minecraft.options;

            if (f == 0.0F)
            {
                this.minecraft.setScreen(new DemoIntroScreen());
            }
            else if (f == 101.0F)
            {
                this.minecraft.gui.getChat().addMessage(new TranslatableComponent("demo.help.movement", options.keyUp.getTranslatedKeyMessage(), options.keyLeft.getTranslatedKeyMessage(), options.keyDown.getTranslatedKeyMessage(), options.keyRight.getTranslatedKeyMessage()));
            }
            else if (f == 102.0F)
            {
                this.minecraft.gui.getChat().addMessage(new TranslatableComponent("demo.help.jump", options.keyJump.getTranslatedKeyMessage()));
            }
            else if (f == 103.0F)
            {
                this.minecraft.gui.getChat().addMessage(new TranslatableComponent("demo.help.inventory", options.keyInventory.getTranslatedKeyMessage()));
            }
            else if (f == 104.0F)
            {
                this.minecraft.gui.getChat().addMessage(new TranslatableComponent("demo.day.6", options.keyScreenshot.getTranslatedKeyMessage()));
            }
        }
        else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.ARROW_HIT_PLAYER)
        {
            this.level.playSound(player, player.getX(), player.getEyeY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.18F, 0.45F);
        }
        else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.RAIN_LEVEL_CHANGE)
        {
            this.level.setRainLevel(f);
        }
        else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE)
        {
            this.level.setThunderLevel(f);
        }
        else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.PUFFER_FISH_STING)
        {
            this.level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.PUFFER_FISH_STING, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
        else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT)
        {
            this.level.addParticle(ParticleTypes.ELDER_GUARDIAN, player.getX(), player.getY(), player.getZ(), 0.0D, 0.0D, 0.0D);

            if (i == 1)
            {
                this.level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1.0F, 1.0F);
            }
        }
        else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.IMMEDIATE_RESPAWN)
        {
            this.minecraft.player.setShowDeathScreen(f == 0.0F);
        }
    }

    public void handleMapItemData(ClientboundMapItemDataPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        MapRenderer maprenderer = this.minecraft.gameRenderer.getMapRenderer();
        int i = pPacket.getMapId();
        String s = MapItem.makeKey(i);
        MapItemSavedData mapitemsaveddata = this.minecraft.level.getMapData(s);

        if (mapitemsaveddata == null)
        {
            mapitemsaveddata = MapItemSavedData.createForClient(pPacket.getScale(), pPacket.isLocked(), this.minecraft.level.dimension());
            this.minecraft.level.setMapData(s, mapitemsaveddata);
        }

        pPacket.applyToMap(mapitemsaveddata);
        maprenderer.update(i, mapitemsaveddata);
    }

    public void handleLevelEvent(ClientboundLevelEventPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);

        if (pPacket.isGlobalEvent())
        {
            this.minecraft.level.globalLevelEvent(pPacket.getType(), pPacket.getPos(), pPacket.getData());
        }
        else
        {
            this.minecraft.level.levelEvent(pPacket.getType(), pPacket.getPos(), pPacket.getData());
        }
    }

    public void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.advancements.update(pPacket);
    }

    public void handleSelectAdvancementsTab(ClientboundSelectAdvancementsTabPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        ResourceLocation resourcelocation = pPacket.getTab();

        if (resourcelocation == null)
        {
            this.advancements.setSelectedTab((Advancement)null, false);
        }
        else
        {
            Advancement advancement = this.advancements.getAdvancements().get(resourcelocation);
            this.advancements.setSelectedTab(advancement, false);
        }
    }

    public void handleCommands(ClientboundCommandsPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.commands = new CommandDispatcher<>(pPacket.getRoot());
    }

    public void handleStopSoundEvent(ClientboundStopSoundPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.getSoundManager().stop(pPacket.getName(), pPacket.getSource());
    }

    public void handleCommandSuggestions(ClientboundCommandSuggestionsPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.suggestionsProvider.completeCustomSuggestions(pPacket.getId(), pPacket.getSuggestions());
    }

    public void handleUpdateRecipes(ClientboundUpdateRecipesPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.recipeManager.replaceRecipes(pPacket.getRecipes());
        MutableSearchTree<RecipeCollection> mutablesearchtree = this.minecraft.getSearchTree(SearchRegistry.RECIPE_COLLECTIONS);
        mutablesearchtree.clear();
        ClientRecipeBook clientrecipebook = this.minecraft.player.getRecipeBook();
        clientrecipebook.setupCollections(this.recipeManager.getRecipes());
        clientrecipebook.getCollections().forEach(mutablesearchtree::add);
        mutablesearchtree.refresh();
    }

    public void handleLookAt(ClientboundPlayerLookAtPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Vec3 vec3 = pPacket.getPosition(this.level);

        if (vec3 != null)
        {
            this.minecraft.player.lookAt(pPacket.getFromAnchor(), vec3);
        }
    }

    public void handleTagQueryPacket(ClientboundTagQueryPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);

        if (!this.debugQueryHandler.handleResponse(pPacket.getTransactionId(), pPacket.getTag()))
        {
            LOGGER.debug("Got unhandled response to tag query {}", (int)pPacket.getTransactionId());
        }
    }

    public void handleAwardStats(ClientboundAwardStatsPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);

        for (Entry < Stat<?>, Integer > entry : pPacket.getStats().entrySet())
        {
            Stat<?> stat = entry.getKey();
            int i = entry.getValue();
            this.minecraft.player.getStats().setValue(this.minecraft.player, stat, i);
        }

        if (this.minecraft.screen instanceof StatsUpdateListener)
        {
            ((StatsUpdateListener)this.minecraft.screen).onStatsUpdated();
        }
    }

    public void handleAddOrRemoveRecipes(ClientboundRecipePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        ClientRecipeBook clientrecipebook = this.minecraft.player.getRecipeBook();
        clientrecipebook.setBookSettings(pPacket.getBookSettings());
        ClientboundRecipePacket.State clientboundrecipepacket$state = pPacket.getState();

        switch (clientboundrecipepacket$state)
        {
            case REMOVE:
                for (ResourceLocation resourcelocation3 : pPacket.getRecipes())
                {
                    this.recipeManager.byKey(resourcelocation3).ifPresent(clientrecipebook::remove);
                }

                break;

            case INIT:
                for (ResourceLocation resourcelocation1 : pPacket.getRecipes())
                {
                    this.recipeManager.byKey(resourcelocation1).ifPresent(clientrecipebook::add);
                }

                for (ResourceLocation resourcelocation2 : pPacket.getHighlights())
                {
                    this.recipeManager.byKey(resourcelocation2).ifPresent(clientrecipebook::addHighlight);
                }

                break;

            case ADD:
                for (ResourceLocation resourcelocation : pPacket.getRecipes())
                {
                    this.recipeManager.byKey(resourcelocation).ifPresent((p_104934_) ->
                    {
                        clientrecipebook.add(p_104934_);
                        clientrecipebook.addHighlight(p_104934_);
                        RecipeToast.addOrUpdate(this.minecraft.getToasts(), p_104934_);
                    });
                }
        }

        clientrecipebook.getCollections().forEach((p_104937_) ->
        {
            p_104937_.updateKnownRecipes(clientrecipebook);
        });

        if (this.minecraft.screen instanceof RecipeUpdateListener)
        {
            ((RecipeUpdateListener)this.minecraft.screen).recipesUpdated();
        }
    }

    public void handleUpdateMobEffect(ClientboundUpdateMobEffectPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getEntityId());

        if (entity instanceof LivingEntity)
        {
            MobEffect mobeffect = MobEffect.byId(pPacket.getEffectId());

            if (mobeffect != null)
            {
                MobEffectInstance mobeffectinstance = new MobEffectInstance(mobeffect, pPacket.getEffectDurationTicks(), pPacket.getEffectAmplifier(), pPacket.isEffectAmbient(), pPacket.isEffectVisible(), pPacket.effectShowsIcon());
                mobeffectinstance.setNoCounter(pPacket.isSuperLongDuration());
                ((LivingEntity)entity).forceAddEffect(mobeffectinstance, (Entity)null);
            }
        }
    }

    public void handleUpdateTags(ClientboundUpdateTagsPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        TagContainer tagcontainer = TagContainer.deserializeFromNetwork(this.registryAccess, pPacket.getTags());
        Multimap < ResourceKey <? extends Registry<? >> , ResourceLocation > multimap = StaticTags.getAllMissingTags(tagcontainer);

        if (!multimap.isEmpty())
        {
            LOGGER.warn("Incomplete server tags, disconnecting. Missing: {}", (Object)multimap);
            this.connection.disconnect(new TranslatableComponent("multiplayer.disconnect.missing_tags"));
        }
        else
        {
            this.tags = tagcontainer;

            if (!this.connection.isMemoryConnection())
            {
                tagcontainer.bindToGlobal();
            }

            this.minecraft.getSearchTree(SearchRegistry.CREATIVE_TAGS).refresh();
        }
    }

    public void handlePlayerCombatEnd(ClientboundPlayerCombatEndPacket p_171771_)
    {
    }

    public void handlePlayerCombatEnter(ClientboundPlayerCombatEnterPacket p_171773_)
    {
    }

    public void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket p_171775_)
    {
        PacketUtils.ensureRunningOnSameThread(p_171775_, this, this.minecraft);
        Entity entity = this.level.getEntity(p_171775_.getPlayerId());

        if (entity == this.minecraft.player)
        {
            if (this.minecraft.player.shouldShowDeathScreen())
            {
                this.minecraft.setScreen(new DeathScreen(p_171775_.getMessage(), this.level.getLevelData().isHardcore()));
            }
            else
            {
                this.minecraft.player.respawn();
            }
        }
    }

    public void handleChangeDifficulty(ClientboundChangeDifficultyPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.levelData.setDifficulty(pPacket.getDifficulty());
        this.levelData.setDifficultyLocked(pPacket.isLocked());
    }

    public void handleSetCamera(ClientboundSetCameraPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = pPacket.getEntity(this.level);

        if (entity != null)
        {
            this.minecraft.setCameraEntity(entity);
        }
    }

    public void handleInitializeBorder(ClientboundInitializeBorderPacket p_171767_)
    {
        PacketUtils.ensureRunningOnSameThread(p_171767_, this, this.minecraft);
        WorldBorder worldborder = this.level.getWorldBorder();
        worldborder.setCenter(p_171767_.getNewCenterX(), p_171767_.getNewCenterZ());
        long i = p_171767_.getLerpTime();

        if (i > 0L)
        {
            worldborder.lerpSizeBetween(p_171767_.getOldSize(), p_171767_.getNewSize(), i);
        }
        else
        {
            worldborder.setSize(p_171767_.getNewSize());
        }

        worldborder.setAbsoluteMaxSize(p_171767_.getNewAbsoluteMaxSize());
        worldborder.setWarningBlocks(p_171767_.getWarningBlocks());
        worldborder.setWarningTime(p_171767_.getWarningTime());
    }

    public void handleSetBorderCenter(ClientboundSetBorderCenterPacket p_171781_)
    {
        PacketUtils.ensureRunningOnSameThread(p_171781_, this, this.minecraft);
        this.level.getWorldBorder().setCenter(p_171781_.getNewCenterX(), p_171781_.getNewCenterZ());
    }

    public void handleSetBorderLerpSize(ClientboundSetBorderLerpSizePacket p_171783_)
    {
        PacketUtils.ensureRunningOnSameThread(p_171783_, this, this.minecraft);
        this.level.getWorldBorder().lerpSizeBetween(p_171783_.getOldSize(), p_171783_.getNewSize(), p_171783_.getLerpTime());
    }

    public void handleSetBorderSize(ClientboundSetBorderSizePacket p_171785_)
    {
        PacketUtils.ensureRunningOnSameThread(p_171785_, this, this.minecraft);
        this.level.getWorldBorder().setSize(p_171785_.getSize());
    }

    public void handleSetBorderWarningDistance(ClientboundSetBorderWarningDistancePacket p_171789_)
    {
        PacketUtils.ensureRunningOnSameThread(p_171789_, this, this.minecraft);
        this.level.getWorldBorder().setWarningBlocks(p_171789_.getWarningBlocks());
    }

    public void handleSetBorderWarningDelay(ClientboundSetBorderWarningDelayPacket p_171787_)
    {
        PacketUtils.ensureRunningOnSameThread(p_171787_, this, this.minecraft);
        this.level.getWorldBorder().setWarningTime(p_171787_.getWarningDelay());
    }

    public void handleTitlesClear(ClientboundClearTitlesPacket p_171765_)
    {
        PacketUtils.ensureRunningOnSameThread(p_171765_, this, this.minecraft);
        this.minecraft.gui.clear();

        if (p_171765_.shouldResetTimes())
        {
            this.minecraft.gui.resetTitleTimes();
        }
    }

    public void setActionBarText(ClientboundSetActionBarTextPacket p_171779_)
    {
        PacketUtils.ensureRunningOnSameThread(p_171779_, this, this.minecraft);
        this.minecraft.gui.setOverlayMessage(p_171779_.getText(), false);
    }

    public void setTitleText(ClientboundSetTitleTextPacket p_171793_)
    {
        PacketUtils.ensureRunningOnSameThread(p_171793_, this, this.minecraft);
        this.minecraft.gui.setTitle(p_171793_.getText());
    }

    public void setSubtitleText(ClientboundSetSubtitleTextPacket p_171791_)
    {
        PacketUtils.ensureRunningOnSameThread(p_171791_, this, this.minecraft);
        this.minecraft.gui.setSubtitle(p_171791_.getText());
    }

    public void setTitlesAnimation(ClientboundSetTitlesAnimationPacket p_171795_)
    {
        PacketUtils.ensureRunningOnSameThread(p_171795_, this, this.minecraft);
        this.minecraft.gui.setTimes(p_171795_.getFadeIn(), p_171795_.getStay(), p_171795_.getFadeOut());
    }

    public void handleTabListCustomisation(ClientboundTabListPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.gui.getTabList().setHeader(pPacket.getHeader().getString().isEmpty() ? null : pPacket.getHeader());
        this.minecraft.gui.getTabList().setFooter(pPacket.getFooter().getString().isEmpty() ? null : pPacket.getFooter());
    }

    public void handleRemoveMobEffect(ClientboundRemoveMobEffectPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = pPacket.getEntity(this.level);

        if (entity instanceof LivingEntity)
        {
            ((LivingEntity)entity).removeEffectNoUpdate(pPacket.getEffect());
        }
    }

    public void handlePlayerInfo(ClientboundPlayerInfoPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);

        for (ClientboundPlayerInfoPacket.PlayerUpdate clientboundplayerinfopacket$playerupdate : pPacket.getEntries())
        {
            if (pPacket.getAction() == ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER)
            {
                this.minecraft.getPlayerSocialManager().removePlayer(clientboundplayerinfopacket$playerupdate.getProfile().getId());
                this.playerInfoMap.remove(clientboundplayerinfopacket$playerupdate.getProfile().getId());
            }
            else
            {
                PlayerInfo playerinfo = this.playerInfoMap.get(clientboundplayerinfopacket$playerupdate.getProfile().getId());

                if (pPacket.getAction() == ClientboundPlayerInfoPacket.Action.ADD_PLAYER)
                {
                    playerinfo = new PlayerInfo(clientboundplayerinfopacket$playerupdate);
                    this.playerInfoMap.put(playerinfo.getProfile().getId(), playerinfo);
                    this.minecraft.getPlayerSocialManager().addPlayer(playerinfo);
                }

                if (playerinfo != null)
                {
                    switch (pPacket.getAction())
                    {
                        case ADD_PLAYER:
                            playerinfo.setGameMode(clientboundplayerinfopacket$playerupdate.getGameMode());
                            playerinfo.setLatency(clientboundplayerinfopacket$playerupdate.getLatency());
                            playerinfo.setTabListDisplayName(clientboundplayerinfopacket$playerupdate.getDisplayName());
                            break;

                        case UPDATE_GAME_MODE:
                            playerinfo.setGameMode(clientboundplayerinfopacket$playerupdate.getGameMode());
                            break;

                        case UPDATE_LATENCY:
                            playerinfo.setLatency(clientboundplayerinfopacket$playerupdate.getLatency());
                            break;

                        case UPDATE_DISPLAY_NAME:
                            playerinfo.setTabListDisplayName(clientboundplayerinfopacket$playerupdate.getDisplayName());
                    }
                }
            }
        }
    }

    public void handleKeepAlive(ClientboundKeepAlivePacket pPacket)
    {
        this.send(new ServerboundKeepAlivePacket(pPacket.getId()));
    }

    public void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Player player = this.minecraft.player;
        player.getAbilities().flying = pPacket.isFlying();
        player.getAbilities().instabuild = pPacket.canInstabuild();
        player.getAbilities().invulnerable = pPacket.isInvulnerable();
        player.getAbilities().mayfly = pPacket.canFly();
        player.getAbilities().setFlyingSpeed(pPacket.getFlyingSpeed());
        player.getAbilities().setWalkingSpeed(pPacket.getWalkingSpeed());
    }

    public void handleSoundEvent(ClientboundSoundPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.level.playSound(this.minecraft.player, pPacket.getX(), pPacket.getY(), pPacket.getZ(), pPacket.getSound(), pPacket.getSource(), pPacket.getVolume(), pPacket.getPitch());
    }

    public void handleSoundEntityEvent(ClientboundSoundEntityPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getId());

        if (entity != null)
        {
            this.minecraft.level.playSound(this.minecraft.player, entity, pPacket.getSound(), pPacket.getSource(), pPacket.getVolume(), pPacket.getPitch());
        }
    }

    public void handleCustomSoundEvent(ClientboundCustomSoundPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.getSoundManager().play(new SimpleSoundInstance(pPacket.getName(), pPacket.getSource(), pPacket.getVolume(), pPacket.getPitch(), false, 0, SoundInstance.Attenuation.LINEAR, pPacket.getX(), pPacket.getY(), pPacket.getZ(), false));
    }

    public void handleResourcePack(ClientboundResourcePackPacket pPacket)
    {
        String s = pPacket.getUrl();
        String s1 = pPacket.getHash();
        boolean flag = pPacket.isRequired();

        if (this.validateResourcePackUrl(s))
        {
            if (s.startsWith("level://"))
            {
                try
                {
                    String s2 = URLDecoder.decode(s.substring("level://".length()), StandardCharsets.UTF_8.toString());
                    File file1 = new File(this.minecraft.gameDirectory, "saves");
                    File file2 = new File(file1, s2);

                    if (file2.isFile())
                    {
                        this.send(ServerboundResourcePackPacket.Action.ACCEPTED);
                        CompletableFuture<?> completablefuture = this.minecraft.getClientPackSource().setServerPack(file2, PackSource.WORLD);
                        this.downloadCallback(completablefuture);
                        return;
                    }
                }
                catch (UnsupportedEncodingException unsupportedencodingexception)
                {
                }

                this.send(ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD);
            }
            else
            {
                ServerData serverdata = this.minecraft.getCurrentServer();

                if (serverdata != null && serverdata.getResourcePackStatus() == ServerData.ServerPackStatus.ENABLED)
                {
                    this.send(ServerboundResourcePackPacket.Action.ACCEPTED);
                    this.downloadCallback(this.minecraft.getClientPackSource().downloadAndSelectResourcePack(s, s1, true));
                }
                else if (serverdata != null && serverdata.getResourcePackStatus() != ServerData.ServerPackStatus.PROMPT && (!flag || serverdata.getResourcePackStatus() != ServerData.ServerPackStatus.DISABLED))
                {
                    this.send(ServerboundResourcePackPacket.Action.DECLINED);

                    if (flag)
                    {
                        this.connection.disconnect(new TranslatableComponent("multiplayer.requiredTexturePrompt.disconnect"));
                    }
                }
                else
                {
                    this.minecraft.execute(() ->
                    {
                        this.minecraft.setScreen(new ConfirmScreen((p_171758_) -> {
                            this.minecraft.setScreen((Screen)null);
                            ServerData serverdata1 = this.minecraft.getCurrentServer();

                            if (p_171758_)
                            {
                                if (serverdata1 != null)
                                {
                                    serverdata1.setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);
                                }

                                this.send(ServerboundResourcePackPacket.Action.ACCEPTED);
                                this.downloadCallback(this.minecraft.getClientPackSource().downloadAndSelectResourcePack(s, s1, true));
                            }
                            else {
                                this.send(ServerboundResourcePackPacket.Action.DECLINED);

                                if (flag)
                                {
                                    this.connection.disconnect(new TranslatableComponent("multiplayer.requiredTexturePrompt.disconnect"));
                                }
                                else if (serverdata1 != null)
                                {
                                    serverdata1.setResourcePackStatus(ServerData.ServerPackStatus.DISABLED);
                                }
                            }

                            if (serverdata1 != null)
                            {
                                ServerList.saveSingleServer(serverdata1);
                            }
                        }, flag ? new TranslatableComponent("multiplayer.requiredTexturePrompt.line1") : new TranslatableComponent("multiplayer.texturePrompt.line1"), preparePackPrompt((Component)(flag ? (new TranslatableComponent("multiplayer.requiredTexturePrompt.line2")).m_130944_(new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD}) : new TranslatableComponent("multiplayer.texturePrompt.line2")), pPacket.getPrompt()), flag ? CommonComponents.GUI_PROCEED : CommonComponents.GUI_YES, (Component)(flag ? new TranslatableComponent("menu.disconnect") : CommonComponents.GUI_NO)));
                    });
                }
            }
        }
    }

    private static Component preparePackPrompt(Component p_171760_, @Nullable Component p_171761_)
    {
        return (Component)(p_171761_ == null ? p_171760_ : new TranslatableComponent("multiplayer.texturePrompt.serverPrompt", p_171760_, p_171761_));
    }

    private boolean validateResourcePackUrl(String pUrl)
    {
        try
        {
            URI uri = new URI(pUrl);
            String s = uri.getScheme();
            boolean flag = "level".equals(s);

            if (!"http".equals(s) && !"https".equals(s) && !flag)
            {
                throw new URISyntaxException(pUrl, "Wrong protocol");
            }
            else if (!flag || !pUrl.contains("..") && pUrl.endsWith("/resources.zip"))
            {
                return true;
            }
            else
            {
                throw new URISyntaxException(pUrl, "Invalid levelstorage resourcepack path");
            }
        }
        catch (URISyntaxException urisyntaxexception)
        {
            this.send(ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD);
            return false;
        }
    }

    private void downloadCallback(CompletableFuture<?> pFuture)
    {
        pFuture.thenRun(() ->
        {
            this.send(ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED);
        }).exceptionally((p_104948_) ->
        {
            this.send(ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD);
            return null;
        });
    }

    private void send(ServerboundResourcePackPacket.Action pPacket)
    {
        this.connection.send(new ServerboundResourcePackPacket(pPacket));
    }

    public void handleBossUpdate(ClientboundBossEventPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.gui.getBossOverlay().update(pPacket);
    }

    public void handleItemCooldown(ClientboundCooldownPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);

        if (pPacket.getDuration() == 0)
        {
            this.minecraft.player.getCooldowns().removeCooldown(pPacket.getItem());
        }
        else
        {
            this.minecraft.player.getCooldowns().addCooldown(pPacket.getItem(), pPacket.getDuration());
        }
    }

    public void handleMoveVehicle(ClientboundMoveVehiclePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.minecraft.player.getRootVehicle();

        if (entity != this.minecraft.player && entity.isControlledByLocalInstance())
        {
            entity.absMoveTo(pPacket.getX(), pPacket.getY(), pPacket.getZ(), pPacket.getYRot(), pPacket.getXRot());
            this.connection.send(new ServerboundMoveVehiclePacket(entity));
        }
    }

    public void handleOpenBook(ClientboundOpenBookPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        ItemStack itemstack = this.minecraft.player.getItemInHand(pPacket.getHand());

        if (itemstack.is(Items.WRITTEN_BOOK))
        {
            this.minecraft.setScreen(new BookViewScreen(new BookViewScreen.WrittenBookAccess(itemstack)));
        }
    }

    public void handleCustomPayload(ClientboundCustomPayloadPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        ResourceLocation resourcelocation = pPacket.getIdentifier();
        FriendlyByteBuf friendlybytebuf = null;

        try
        {
            friendlybytebuf = pPacket.getData();

            if (ClientboundCustomPayloadPacket.BRAND.equals(resourcelocation))
            {
                this.minecraft.player.setServerBrand(friendlybytebuf.readUtf());
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_PATHFINDING_PACKET.equals(resourcelocation))
            {
                int i = friendlybytebuf.readInt();
                float f = friendlybytebuf.readFloat();
                Path path = Path.createFromStream(friendlybytebuf);
                this.minecraft.debugRenderer.pathfindingRenderer.addPath(i, path, f);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_NEIGHBORSUPDATE_PACKET.equals(resourcelocation))
            {
                long k1 = friendlybytebuf.readVarLong();
                BlockPos blockpos9 = friendlybytebuf.readBlockPos();
                ((NeighborsUpdateRenderer)this.minecraft.debugRenderer.neighborsUpdateRenderer).addUpdate(k1, blockpos9);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_STRUCTURES_PACKET.equals(resourcelocation))
            {
                DimensionType dimensiontype = this.registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).get(friendlybytebuf.readResourceLocation());
                BoundingBox boundingbox = new BoundingBox(friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt());
                int k3 = friendlybytebuf.readInt();
                List<BoundingBox> list = Lists.newArrayList();
                List<Boolean> list1 = Lists.newArrayList();

                for (int j = 0; j < k3; ++j)
                {
                    list.add(new BoundingBox(friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt()));
                    list1.add(friendlybytebuf.readBoolean());
                }

                this.minecraft.debugRenderer.structureRenderer.addBoundingBox(boundingbox, list, list1, dimensiontype);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_WORLDGENATTEMPT_PACKET.equals(resourcelocation))
            {
                ((WorldGenAttemptRenderer)this.minecraft.debugRenderer.worldGenAttemptRenderer).addPos(friendlybytebuf.readBlockPos(), friendlybytebuf.readFloat(), friendlybytebuf.readFloat(), friendlybytebuf.readFloat(), friendlybytebuf.readFloat(), friendlybytebuf.readFloat());
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_VILLAGE_SECTIONS.equals(resourcelocation))
            {
                int l1 = friendlybytebuf.readInt();

                for (int j2 = 0; j2 < l1; ++j2)
                {
                    this.minecraft.debugRenderer.villageSectionsDebugRenderer.setVillageSection(friendlybytebuf.readSectionPos());
                }

                int k2 = friendlybytebuf.readInt();

                for (int l3 = 0; l3 < k2; ++l3)
                {
                    this.minecraft.debugRenderer.villageSectionsDebugRenderer.setNotVillageSection(friendlybytebuf.readSectionPos());
                }
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_POI_ADDED_PACKET.equals(resourcelocation))
            {
                BlockPos blockpos2 = friendlybytebuf.readBlockPos();
                String s8 = friendlybytebuf.readUtf();
                int i4 = friendlybytebuf.readInt();
                BrainDebugRenderer.PoiInfo braindebugrenderer$poiinfo = new BrainDebugRenderer.PoiInfo(blockpos2, s8, i4);
                this.minecraft.debugRenderer.brainDebugRenderer.addPoi(braindebugrenderer$poiinfo);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_POI_REMOVED_PACKET.equals(resourcelocation))
            {
                BlockPos blockpos3 = friendlybytebuf.readBlockPos();
                this.minecraft.debugRenderer.brainDebugRenderer.removePoi(blockpos3);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_POI_TICKET_COUNT_PACKET.equals(resourcelocation))
            {
                BlockPos blockpos4 = friendlybytebuf.readBlockPos();
                int l2 = friendlybytebuf.readInt();
                this.minecraft.debugRenderer.brainDebugRenderer.setFreeTicketCount(blockpos4, l2);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_GOAL_SELECTOR.equals(resourcelocation))
            {
                BlockPos blockpos5 = friendlybytebuf.readBlockPos();
                int i3 = friendlybytebuf.readInt();
                int j4 = friendlybytebuf.readInt();
                List<GoalSelectorDebugRenderer.DebugGoal> list2 = Lists.newArrayList();

                for (int l5 = 0; l5 < j4; ++l5)
                {
                    int i6 = friendlybytebuf.readInt();
                    boolean flag = friendlybytebuf.readBoolean();
                    String s = friendlybytebuf.readUtf(255);
                    list2.add(new GoalSelectorDebugRenderer.DebugGoal(blockpos5, i6, s, flag));
                }

                this.minecraft.debugRenderer.goalSelectorRenderer.addGoalSelector(i3, list2);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_RAIDS.equals(resourcelocation))
            {
                int i2 = friendlybytebuf.readInt();
                Collection<BlockPos> collection = Lists.newArrayList();

                for (int k4 = 0; k4 < i2; ++k4)
                {
                    collection.add(friendlybytebuf.readBlockPos());
                }

                this.minecraft.debugRenderer.raidDebugRenderer.setRaidCenters(collection);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_BRAIN.equals(resourcelocation))
            {
                double d0 = friendlybytebuf.readDouble();
                double d2 = friendlybytebuf.readDouble();
                double d4 = friendlybytebuf.readDouble();
                Position position = new PositionImpl(d0, d2, d4);
                UUID uuid = friendlybytebuf.readUUID();
                int k = friendlybytebuf.readInt();
                String s1 = friendlybytebuf.readUtf();
                String s2 = friendlybytebuf.readUtf();
                int l = friendlybytebuf.readInt();
                float f1 = friendlybytebuf.readFloat();
                float f2 = friendlybytebuf.readFloat();
                String s3 = friendlybytebuf.readUtf();
                boolean flag1 = friendlybytebuf.readBoolean();
                Path path1;

                if (flag1)
                {
                    path1 = Path.createFromStream(friendlybytebuf);
                }
                else
                {
                    path1 = null;
                }

                boolean flag2 = friendlybytebuf.readBoolean();
                BrainDebugRenderer.BrainDump braindebugrenderer$braindump = new BrainDebugRenderer.BrainDump(uuid, k, s1, s2, l, f1, f2, position, s3, path1, flag2);
                int i1 = friendlybytebuf.readVarInt();

                for (int j1 = 0; j1 < i1; ++j1)
                {
                    String s4 = friendlybytebuf.readUtf();
                    braindebugrenderer$braindump.activities.add(s4);
                }

                int l7 = friendlybytebuf.readVarInt();

                for (int i8 = 0; i8 < l7; ++i8)
                {
                    String s5 = friendlybytebuf.readUtf();
                    braindebugrenderer$braindump.behaviors.add(s5);
                }

                int j8 = friendlybytebuf.readVarInt();

                for (int k8 = 0; k8 < j8; ++k8)
                {
                    String s6 = friendlybytebuf.readUtf();
                    braindebugrenderer$braindump.memories.add(s6);
                }

                int l8 = friendlybytebuf.readVarInt();

                for (int i9 = 0; i9 < l8; ++i9)
                {
                    BlockPos blockpos = friendlybytebuf.readBlockPos();
                    braindebugrenderer$braindump.pois.add(blockpos);
                }

                int j9 = friendlybytebuf.readVarInt();

                for (int k9 = 0; k9 < j9; ++k9)
                {
                    BlockPos blockpos1 = friendlybytebuf.readBlockPos();
                    braindebugrenderer$braindump.potentialPois.add(blockpos1);
                }

                int l9 = friendlybytebuf.readVarInt();

                for (int i10 = 0; i10 < l9; ++i10)
                {
                    String s7 = friendlybytebuf.readUtf();
                    braindebugrenderer$braindump.gossips.add(s7);
                }

                this.minecraft.debugRenderer.brainDebugRenderer.addOrUpdateBrainDump(braindebugrenderer$braindump);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_BEE.equals(resourcelocation))
            {
                double d1 = friendlybytebuf.readDouble();
                double d3 = friendlybytebuf.readDouble();
                double d5 = friendlybytebuf.readDouble();
                Position position1 = new PositionImpl(d1, d3, d5);
                UUID uuid1 = friendlybytebuf.readUUID();
                int j6 = friendlybytebuf.readInt();
                boolean flag4 = friendlybytebuf.readBoolean();
                BlockPos blockpos10 = null;

                if (flag4)
                {
                    blockpos10 = friendlybytebuf.readBlockPos();
                }

                boolean flag5 = friendlybytebuf.readBoolean();
                BlockPos blockpos11 = null;

                if (flag5)
                {
                    blockpos11 = friendlybytebuf.readBlockPos();
                }

                int k6 = friendlybytebuf.readInt();
                boolean flag6 = friendlybytebuf.readBoolean();
                Path path2 = null;

                if (flag6)
                {
                    path2 = Path.createFromStream(friendlybytebuf);
                }

                BeeDebugRenderer.BeeInfo beedebugrenderer$beeinfo = new BeeDebugRenderer.BeeInfo(uuid1, j6, position1, path2, blockpos10, blockpos11, k6);
                int l6 = friendlybytebuf.readVarInt();

                for (int i7 = 0; i7 < l6; ++i7)
                {
                    String s11 = friendlybytebuf.readUtf();
                    beedebugrenderer$beeinfo.goals.add(s11);
                }

                int j7 = friendlybytebuf.readVarInt();

                for (int k7 = 0; k7 < j7; ++k7)
                {
                    BlockPos blockpos12 = friendlybytebuf.readBlockPos();
                    beedebugrenderer$beeinfo.blacklistedHives.add(blockpos12);
                }

                this.minecraft.debugRenderer.beeDebugRenderer.addOrUpdateBeeInfo(beedebugrenderer$beeinfo);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_HIVE.equals(resourcelocation))
            {
                BlockPos blockpos6 = friendlybytebuf.readBlockPos();
                String s9 = friendlybytebuf.readUtf();
                int l4 = friendlybytebuf.readInt();
                int j5 = friendlybytebuf.readInt();
                boolean flag3 = friendlybytebuf.readBoolean();
                BeeDebugRenderer.HiveInfo beedebugrenderer$hiveinfo = new BeeDebugRenderer.HiveInfo(blockpos6, s9, l4, j5, flag3, this.level.getGameTime());
                this.minecraft.debugRenderer.beeDebugRenderer.addOrUpdateHiveInfo(beedebugrenderer$hiveinfo);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_CLEAR.equals(resourcelocation))
            {
                this.minecraft.debugRenderer.gameTestDebugRenderer.clear();
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_ADD_MARKER.equals(resourcelocation))
            {
                BlockPos blockpos7 = friendlybytebuf.readBlockPos();
                int j3 = friendlybytebuf.readInt();
                String s10 = friendlybytebuf.readUtf();
                int k5 = friendlybytebuf.readInt();
                this.minecraft.debugRenderer.gameTestDebugRenderer.addMarker(blockpos7, j3, s10, k5);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_GAME_EVENT.equals(resourcelocation))
            {
                GameEvent gameevent = Registry.GAME_EVENT.get(new ResourceLocation(friendlybytebuf.readUtf()));
                BlockPos blockpos8 = friendlybytebuf.readBlockPos();
                this.minecraft.debugRenderer.gameEventListenerRenderer.trackGameEvent(gameevent, blockpos8);
            }
            else if (ClientboundCustomPayloadPacket.DEBUG_GAME_EVENT_LISTENER.equals(resourcelocation))
            {
                ResourceLocation resourcelocation1 = friendlybytebuf.readResourceLocation();
                PositionSource positionsource = Registry.POSITION_SOURCE_TYPE.getOptional(resourcelocation1).orElseThrow(() ->
                {
                    return new IllegalArgumentException("Unknown position source type " + resourcelocation1);
                }).read(friendlybytebuf);
                int i5 = friendlybytebuf.readVarInt();
                this.minecraft.debugRenderer.gameEventListenerRenderer.trackListener(positionsource, i5);
            }
            else
            {
                LOGGER.warn("Unknown custom packed identifier: {}", (Object)resourcelocation);
            }
        }
        finally
        {
            if (friendlybytebuf != null)
            {
                friendlybytebuf.release();
            }
        }
    }

    public void handleAddObjective(ClientboundSetObjectivePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Scoreboard scoreboard = this.level.getScoreboard();
        String s = pPacket.getObjectiveName();

        if (pPacket.getMethod() == 0)
        {
            scoreboard.addObjective(s, ObjectiveCriteria.DUMMY, pPacket.getDisplayName(), pPacket.getRenderType());
        }
        else if (scoreboard.hasObjective(s))
        {
            Objective objective = scoreboard.getObjective(s);

            if (pPacket.getMethod() == 1)
            {
                scoreboard.removeObjective(objective);
            }
            else if (pPacket.getMethod() == 2)
            {
                objective.setRenderType(pPacket.getRenderType());
                objective.setDisplayName(pPacket.getDisplayName());
            }
        }
    }

    public void handleSetScore(ClientboundSetScorePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Scoreboard scoreboard = this.level.getScoreboard();
        String s = pPacket.getObjectiveName();

        switch (pPacket.getMethod())
        {
            case CHANGE:
                Objective objective = scoreboard.getOrCreateObjective(s);
                Score score = scoreboard.getOrCreatePlayerScore(pPacket.getOwner(), objective);
                score.setScore(pPacket.getScore());
                break;

            case REMOVE:
                scoreboard.resetPlayerScore(pPacket.getOwner(), scoreboard.getObjective(s));
        }
    }

    public void handleSetDisplayObjective(ClientboundSetDisplayObjectivePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Scoreboard scoreboard = this.level.getScoreboard();
        String s = pPacket.getObjectiveName();
        Objective objective = s == null ? null : scoreboard.getOrCreateObjective(s);
        scoreboard.setDisplayObjective(pPacket.getSlot(), objective);
    }

    public void handleSetPlayerTeamPacket(ClientboundSetPlayerTeamPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Scoreboard scoreboard = this.level.getScoreboard();
        ClientboundSetPlayerTeamPacket.Action clientboundsetplayerteampacket$action = pPacket.getTeamAction();
        PlayerTeam playerteam;

        if (clientboundsetplayerteampacket$action == ClientboundSetPlayerTeamPacket.Action.ADD)
        {
            playerteam = scoreboard.addPlayerTeam(pPacket.getName());
        }
        else
        {
            playerteam = scoreboard.getPlayerTeam(pPacket.getName());

            if (playerteam == null)
            {
                LOGGER.warn("Received packet for unknown team {}: team action: {}, player action: {}", pPacket.getName(), pPacket.getTeamAction(), pPacket.getPlayerAction());
                return;
            }
        }

        Optional<ClientboundSetPlayerTeamPacket.Parameters> optional = pPacket.getParameters();
        optional.ifPresent((p_171748_) ->
        {
            playerteam.setDisplayName(p_171748_.getDisplayName());
            playerteam.setColor(p_171748_.getColor());
            playerteam.unpackOptions(p_171748_.getOptions());
            Team.Visibility team$visibility = Team.Visibility.byName(p_171748_.getNametagVisibility());

            if (team$visibility != null)
            {
                playerteam.setNameTagVisibility(team$visibility);
            }

            Team.CollisionRule team$collisionrule = Team.CollisionRule.byName(p_171748_.getCollisionRule());

            if (team$collisionrule != null)
            {
                playerteam.setCollisionRule(team$collisionrule);
            }

            playerteam.setPlayerPrefix(p_171748_.getPlayerPrefix());
            playerteam.setPlayerSuffix(p_171748_.getPlayerSuffix());
        });
        ClientboundSetPlayerTeamPacket.Action clientboundsetplayerteampacket$action1 = pPacket.getPlayerAction();

        if (clientboundsetplayerteampacket$action1 == ClientboundSetPlayerTeamPacket.Action.ADD)
        {
            for (String s : pPacket.getPlayers())
            {
                scoreboard.addPlayerToTeam(s, playerteam);
            }
        }
        else if (clientboundsetplayerteampacket$action1 == ClientboundSetPlayerTeamPacket.Action.REMOVE)
        {
            for (String s1 : pPacket.getPlayers())
            {
                scoreboard.removePlayerFromTeam(s1, playerteam);
            }
        }

        if (clientboundsetplayerteampacket$action == ClientboundSetPlayerTeamPacket.Action.REMOVE)
        {
            scoreboard.removePlayerTeam(playerteam);
        }
    }

    public void handleParticleEvent(ClientboundLevelParticlesPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);

        if (pPacket.getCount() == 0)
        {
            double d0 = (double)(pPacket.getMaxSpeed() * pPacket.getXDist());
            double d2 = (double)(pPacket.getMaxSpeed() * pPacket.getYDist());
            double d4 = (double)(pPacket.getMaxSpeed() * pPacket.getZDist());

            try
            {
                this.level.addParticle(pPacket.getParticle(), pPacket.isOverrideLimiter(), pPacket.getX(), pPacket.getY(), pPacket.getZ(), d0, d2, d4);
            }
            catch (Throwable throwable1)
            {
                LOGGER.warn("Could not spawn particle effect {}", (Object)pPacket.getParticle());
            }
        }
        else
        {
            for (int i = 0; i < pPacket.getCount(); ++i)
            {
                double d1 = this.random.nextGaussian() * (double)pPacket.getXDist();
                double d3 = this.random.nextGaussian() * (double)pPacket.getYDist();
                double d5 = this.random.nextGaussian() * (double)pPacket.getZDist();
                double d6 = this.random.nextGaussian() * (double)pPacket.getMaxSpeed();
                double d7 = this.random.nextGaussian() * (double)pPacket.getMaxSpeed();
                double d8 = this.random.nextGaussian() * (double)pPacket.getMaxSpeed();

                try
                {
                    this.level.addParticle(pPacket.getParticle(), pPacket.isOverrideLimiter(), pPacket.getX() + d1, pPacket.getY() + d3, pPacket.getZ() + d5, d6, d7, d8);
                }
                catch (Throwable throwable)
                {
                    LOGGER.warn("Could not spawn particle effect {}", (Object)pPacket.getParticle());
                    return;
                }
            }
        }
    }

    public void handlePing(ClientboundPingPacket p_171769_)
    {
        PacketUtils.ensureRunningOnSameThread(p_171769_, this, this.minecraft);
        this.send(new ServerboundPongPacket(p_171769_.getId()));
    }

    public void handleUpdateAttributes(ClientboundUpdateAttributesPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        Entity entity = this.level.getEntity(pPacket.getEntityId());

        if (entity != null)
        {
            if (!(entity instanceof LivingEntity))
            {
                throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + entity + ")");
            }
            else
            {
                AttributeMap attributemap = ((LivingEntity)entity).getAttributes();

                for (ClientboundUpdateAttributesPacket.AttributeSnapshot clientboundupdateattributespacket$attributesnapshot : pPacket.getValues())
                {
                    AttributeInstance attributeinstance = attributemap.getInstance(clientboundupdateattributespacket$attributesnapshot.getAttribute());

                    if (attributeinstance == null)
                    {
                        LOGGER.warn("Entity {} does not have attribute {}", entity, Registry.ATTRIBUTE.getKey(clientboundupdateattributespacket$attributesnapshot.getAttribute()));
                    }
                    else
                    {
                        attributeinstance.setBaseValue(clientboundupdateattributespacket$attributesnapshot.getBase());
                        attributeinstance.removeModifiers();

                        for (AttributeModifier attributemodifier : clientboundupdateattributespacket$attributesnapshot.getModifiers())
                        {
                            attributeinstance.addTransientModifier(attributemodifier);
                        }
                    }
                }
            }
        }
    }

    public void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        AbstractContainerMenu abstractcontainermenu = this.minecraft.player.containerMenu;

        if (abstractcontainermenu.containerId == pPacket.getContainerId())
        {
            this.recipeManager.byKey(pPacket.getRecipe()).ifPresent((p_171745_) ->
            {
                if (this.minecraft.screen instanceof RecipeUpdateListener)
                {
                    RecipeBookComponent recipebookcomponent = ((RecipeUpdateListener)this.minecraft.screen).getRecipeBookComponent();
                    recipebookcomponent.setupGhostRecipe(p_171745_, abstractcontainermenu.slots);
                }
            });
        }
    }

    public void handleLightUpdatePacked(ClientboundLightUpdatePacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        int i = pPacket.getX();
        int j = pPacket.getZ();
        LevelLightEngine levellightengine = this.level.getChunkSource().getLightEngine();
        BitSet bitset = pPacket.getSkyYMask();
        BitSet bitset1 = pPacket.getEmptySkyYMask();
        Iterator<byte[]> iterator = pPacket.getSkyUpdates().iterator();
        this.readSectionList(i, j, levellightengine, LightLayer.SKY, bitset, bitset1, iterator, pPacket.getTrustEdges());
        BitSet bitset2 = pPacket.getBlockYMask();
        BitSet bitset3 = pPacket.getEmptyBlockYMask();
        Iterator<byte[]> iterator1 = pPacket.getBlockUpdates().iterator();
        this.readSectionList(i, j, levellightengine, LightLayer.BLOCK, bitset2, bitset3, iterator1, pPacket.getTrustEdges());
    }

    public void handleMerchantOffers(ClientboundMerchantOffersPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        AbstractContainerMenu abstractcontainermenu = this.minecraft.player.containerMenu;

        if (pPacket.getContainerId() == abstractcontainermenu.containerId && abstractcontainermenu instanceof MerchantMenu)
        {
            MerchantMenu merchantmenu = (MerchantMenu)abstractcontainermenu;
            merchantmenu.setOffers(new MerchantOffers(pPacket.getOffers().createTag()));
            merchantmenu.setXp(pPacket.getVillagerXp());
            merchantmenu.setMerchantLevel(pPacket.getVillagerLevel());
            merchantmenu.setShowProgressBar(pPacket.showProgress());
            merchantmenu.setCanRestock(pPacket.canRestock());
        }
    }

    public void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.serverChunkRadius = pPacket.getRadius();
        this.level.getChunkSource().updateViewRadius(pPacket.getRadius());
    }

    public void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.level.getChunkSource().updateViewCenter(pPacket.getX(), pPacket.getZ());
    }

    public void handleBlockBreakAck(ClientboundBlockBreakAckPacket pPacket)
    {
        PacketUtils.ensureRunningOnSameThread(pPacket, this, this.minecraft);
        this.minecraft.gameMode.handleBlockBreakAck(this.level, pPacket.getPos(), pPacket.getState(), pPacket.action(), pPacket.allGood());
    }

    private void readSectionList(int p_171735_, int p_171736_, LevelLightEngine p_171737_, LightLayer p_171738_, BitSet p_171739_, BitSet p_171740_, Iterator<byte[]> p_171741_, boolean p_171742_)
    {
        for (int i = 0; i < p_171737_.getLightSectionCount(); ++i)
        {
            int j = p_171737_.getMinLightSection() + i;
            boolean flag = p_171739_.get(i);
            boolean flag1 = p_171740_.get(i);

            if (flag || flag1)
            {
                p_171737_.queueSectionData(p_171738_, SectionPos.of(p_171735_, j, p_171736_), flag ? new DataLayer((byte[])p_171741_.next().clone()) : new DataLayer(), p_171742_);
                this.level.setSectionDirtyWithNeighbors(p_171735_, j, p_171736_);
            }
        }
    }

    public Connection getConnection()
    {
        return this.connection;
    }

    public Collection<PlayerInfo> getOnlinePlayers()
    {
        return this.playerInfoMap.values();
    }

    public Collection<UUID> getOnlinePlayerIds()
    {
        return this.playerInfoMap.keySet();
    }

    @Nullable
    public PlayerInfo getPlayerInfo(UUID pName)
    {
        return this.playerInfoMap.get(pName);
    }

    @Nullable
    public PlayerInfo getPlayerInfo(String pName)
    {
        for (PlayerInfo playerinfo : this.playerInfoMap.values())
        {
            if (playerinfo.getProfile().getName().equals(pName))
            {
                return playerinfo;
            }
        }

        return null;
    }

    public GameProfile getLocalGameProfile()
    {
        return this.localGameProfile;
    }

    public ClientAdvancements getAdvancements()
    {
        return this.advancements;
    }

    public CommandDispatcher<SharedSuggestionProvider> getCommands()
    {
        return this.commands;
    }

    public ClientLevel getLevel()
    {
        return this.level;
    }

    public TagContainer getTags()
    {
        return this.tags;
    }

    public DebugQueryHandler getDebugQueryHandler()
    {
        return this.debugQueryHandler;
    }

    public UUID getId()
    {
        return this.id;
    }

    public Set<ResourceKey<Level>> levels()
    {
        return this.levels;
    }

    public RegistryAccess registryAccess()
    {
        return this.registryAccess;
    }
}
