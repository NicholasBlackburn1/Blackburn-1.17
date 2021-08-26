package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class PlayerList
{
    public static final File USERBANLIST_FILE = new File("banned-players.json");
    public static final File IPBANLIST_FILE = new File("banned-ips.json");
    public static final File OPLIST_FILE = new File("ops.json");
    public static final File WHITELIST_FILE = new File("whitelist.json");
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int SEND_PLAYER_INFO_INTERVAL = 600;
    private static final SimpleDateFormat BAN_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
    private final MinecraftServer server;
    private final List<ServerPlayer> players = Lists.newArrayList();
    private final Map<UUID, ServerPlayer> playersByUUID = Maps.newHashMap();
    private final UserBanList bans = new UserBanList(USERBANLIST_FILE);
    private final IpBanList ipBans = new IpBanList(IPBANLIST_FILE);
    private final ServerOpList ops = new ServerOpList(OPLIST_FILE);
    private final UserWhiteList whitelist = new UserWhiteList(WHITELIST_FILE);
    private final Map<UUID, ServerStatsCounter> stats = Maps.newHashMap();
    private final Map<UUID, PlayerAdvancements> advancements = Maps.newHashMap();
    private final PlayerDataStorage playerIo;
    private boolean doWhiteList;
    private final RegistryAccess.RegistryHolder registryHolder;
    protected final int maxPlayers;
    private int viewDistance;
    private boolean allowCheatsForAllPlayers;
    private static final boolean ALLOW_LOGOUTIVATOR = false;
    private int sendAllPlayerInfoIn;

    public PlayerList(MinecraftServer p_11213_, RegistryAccess.RegistryHolder p_11214_, PlayerDataStorage p_11215_, int p_11216_)
    {
        this.server = p_11213_;
        this.registryHolder = p_11214_;
        this.maxPlayers = p_11216_;
        this.playerIo = p_11215_;
    }

    public void placeNewPlayer(Connection pNetManager, ServerPlayer pPlayer)
    {
        GameProfile gameprofile = pPlayer.getGameProfile();
        GameProfileCache gameprofilecache = this.server.getProfileCache();
        Optional<GameProfile> optional = gameprofilecache.get(gameprofile.getId());
        String s = optional.map(GameProfile::getName).orElse(gameprofile.getName());
        gameprofilecache.add(gameprofile);
        CompoundTag compoundtag = this.load(pPlayer);
        ResourceKey<Level> resourcekey = compoundtag != null ? DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, compoundtag.get("Dimension"))).resultOrPartial(LOGGER::error).orElse(Level.OVERWORLD) : Level.OVERWORLD;
        ServerLevel serverlevel = this.server.getLevel(resourcekey);
        ServerLevel serverlevel1;

        if (serverlevel == null)
        {
            LOGGER.warn("Unknown respawn dimension {}, defaulting to overworld", (Object)resourcekey);
            serverlevel1 = this.server.overworld();
        }
        else
        {
            serverlevel1 = serverlevel;
        }

        pPlayer.setLevel(serverlevel1);
        String s1 = "local";

        if (pNetManager.getRemoteAddress() != null)
        {
            s1 = pNetManager.getRemoteAddress().toString();
        }

        LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {})", pPlayer.getName().getString(), s1, pPlayer.getId(), pPlayer.getX(), pPlayer.getY(), pPlayer.getZ());
        LevelData leveldata = serverlevel1.getLevelData();
        pPlayer.loadGameTypes(compoundtag);
        ServerGamePacketListenerImpl servergamepacketlistenerimpl = new ServerGamePacketListenerImpl(this.server, pNetManager, pPlayer);
        GameRules gamerules = serverlevel1.getGameRules();
        boolean flag = gamerules.getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN);
        boolean flag1 = gamerules.getBoolean(GameRules.RULE_REDUCEDDEBUGINFO);
        servergamepacketlistenerimpl.send(new ClientboundLoginPacket(pPlayer.getId(), pPlayer.gameMode.getGameModeForPlayer(), pPlayer.gameMode.getPreviousGameModeForPlayer(), BiomeManager.obfuscateSeed(serverlevel1.getSeed()), leveldata.isHardcore(), this.server.levelKeys(), this.registryHolder, serverlevel1.dimensionType(), serverlevel1.dimension(), this.getMaxPlayers(), this.viewDistance, flag1, !flag, serverlevel1.isDebug(), serverlevel1.isFlat()));
        servergamepacketlistenerimpl.send(new ClientboundCustomPayloadPacket(ClientboundCustomPayloadPacket.BRAND, (new FriendlyByteBuf(Unpooled.buffer())).writeUtf(this.getServer().getServerModName())));
        servergamepacketlistenerimpl.send(new ClientboundChangeDifficultyPacket(leveldata.getDifficulty(), leveldata.isDifficultyLocked()));
        servergamepacketlistenerimpl.send(new ClientboundPlayerAbilitiesPacket(pPlayer.getAbilities()));
        servergamepacketlistenerimpl.send(new ClientboundSetCarriedItemPacket(pPlayer.getInventory().selected));
        servergamepacketlistenerimpl.send(new ClientboundUpdateRecipesPacket(this.server.getRecipeManager().getRecipes()));
        servergamepacketlistenerimpl.send(new ClientboundUpdateTagsPacket(this.server.getTags().serializeToNetwork(this.registryHolder)));
        this.sendPlayerPermissionLevel(pPlayer);
        pPlayer.getStats().markAllDirty();
        pPlayer.getRecipeBook().sendInitialRecipeBook(pPlayer);
        this.updateEntireScoreboard(serverlevel1.getScoreboard(), pPlayer);
        this.server.invalidateStatus();
        MutableComponent mutablecomponent;

        if (pPlayer.getGameProfile().getName().equalsIgnoreCase(s))
        {
            mutablecomponent = new TranslatableComponent("multiplayer.player.joined", pPlayer.getDisplayName());
        }
        else
        {
            mutablecomponent = new TranslatableComponent("multiplayer.player.joined.renamed", pPlayer.getDisplayName(), s);
        }

        this.broadcastMessage(mutablecomponent.withStyle(ChatFormatting.YELLOW), ChatType.SYSTEM, Util.NIL_UUID);
        servergamepacketlistenerimpl.teleport(pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), pPlayer.getYRot(), pPlayer.getXRot());
        this.players.add(pPlayer);
        this.playersByUUID.put(pPlayer.getUUID(), pPlayer);
        this.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, pPlayer));

        for (int i = 0; i < this.players.size(); ++i)
        {
            pPlayer.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, this.players.get(i)));
        }

        serverlevel1.addNewPlayer(pPlayer);
        this.server.getCustomBossEvents().onPlayerConnect(pPlayer);
        this.sendLevelInfo(pPlayer, serverlevel1);

        if (!this.server.getResourcePack().isEmpty())
        {
            pPlayer.sendTexturePack(this.server.getResourcePack(), this.server.getResourcePackHash(), this.server.isResourcePackRequired(), this.server.getResourcePackPrompt());
        }

        for (MobEffectInstance mobeffectinstance : pPlayer.getActiveEffects())
        {
            servergamepacketlistenerimpl.send(new ClientboundUpdateMobEffectPacket(pPlayer.getId(), mobeffectinstance));
        }

        if (compoundtag != null && compoundtag.contains("RootVehicle", 10))
        {
            CompoundTag compoundtag1 = compoundtag.getCompound("RootVehicle");
            Entity entity1 = EntityType.loadEntityRecursive(compoundtag1.getCompound("Entity"), serverlevel1, (p_11223_) ->
            {
                return !serverlevel1.addWithUUID(p_11223_) ? null : p_11223_;
            });

            if (entity1 != null)
            {
                UUID uuid;

                if (compoundtag1.hasUUID("Attach"))
                {
                    uuid = compoundtag1.getUUID("Attach");
                }
                else
                {
                    uuid = null;
                }

                if (entity1.getUUID().equals(uuid))
                {
                    pPlayer.startRiding(entity1, true);
                }
                else
                {
                    for (Entity entity : entity1.getIndirectPassengers())
                    {
                        if (entity.getUUID().equals(uuid))
                        {
                            pPlayer.startRiding(entity, true);
                            break;
                        }
                    }
                }

                if (!pPlayer.isPassenger())
                {
                    LOGGER.warn("Couldn't reattach entity to player");
                    entity1.discard();

                    for (Entity entity2 : entity1.getIndirectPassengers())
                    {
                        entity2.discard();
                    }
                }
            }
        }

        pPlayer.initInventoryMenu();
    }

    protected void updateEntireScoreboard(ServerScoreboard pScoreboard, ServerPlayer pPlayer)
    {
        Set<Objective> set = Sets.newHashSet();

        for (PlayerTeam playerteam : pScoreboard.getPlayerTeams())
        {
            pPlayer.connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(playerteam, true));
        }

        for (int i = 0; i < 19; ++i)
        {
            Objective objective = pScoreboard.getDisplayObjective(i);

            if (objective != null && !set.contains(objective))
            {
                for (Packet<?> packet : pScoreboard.getStartTrackingPackets(objective))
                {
                    pPlayer.connection.send(packet);
                }

                set.add(objective);
            }
        }
    }

    public void setLevel(ServerLevel p_11220_)
    {
        p_11220_.getWorldBorder().addListener(new BorderChangeListener()
        {
            public void onBorderSizeSet(WorldBorder pBorder, double pNewSize)
            {
                PlayerList.this.broadcastAll(new ClientboundSetBorderSizePacket(pBorder));
            }
            public void onBorderSizeLerping(WorldBorder pBorder, double pOldSize, double p_11330_, long pNewSize)
            {
                PlayerList.this.broadcastAll(new ClientboundSetBorderLerpSizePacket(pBorder));
            }
            public void onBorderCenterSet(WorldBorder pBorder, double pX, double p_11326_)
            {
                PlayerList.this.broadcastAll(new ClientboundSetBorderCenterPacket(pBorder));
            }
            public void onBorderSetWarningTime(WorldBorder pBorder, int pNewTime)
            {
                PlayerList.this.broadcastAll(new ClientboundSetBorderWarningDelayPacket(pBorder));
            }
            public void onBorderSetWarningBlocks(WorldBorder pBorder, int pNewDistance)
            {
                PlayerList.this.broadcastAll(new ClientboundSetBorderWarningDistancePacket(pBorder));
            }
            public void onBorderSetDamagePerBlock(WorldBorder pBorder, double pNewAmount)
            {
            }
            public void onBorderSetDamageSafeZOne(WorldBorder pBorder, double pNewSize)
            {
            }
        });
    }

    @Nullable
    public CompoundTag load(ServerPlayer pPlayer)
    {
        CompoundTag compoundtag = this.server.getWorldData().getLoadedPlayerTag();
        CompoundTag compoundtag1;

        if (pPlayer.getName().getString().equals(this.server.getSingleplayerName()) && compoundtag != null)
        {
            compoundtag1 = compoundtag;
            pPlayer.load(compoundtag);
            LOGGER.debug("loading single player");
        }
        else
        {
            compoundtag1 = this.playerIo.load(pPlayer);
        }

        return compoundtag1;
    }

    protected void save(ServerPlayer pPlayer)
    {
        this.playerIo.save(pPlayer);
        ServerStatsCounter serverstatscounter = this.stats.get(pPlayer.getUUID());

        if (serverstatscounter != null)
        {
            serverstatscounter.save();
        }

        PlayerAdvancements playeradvancements = this.advancements.get(pPlayer.getUUID());

        if (playeradvancements != null)
        {
            playeradvancements.save();
        }
    }

    public void remove(ServerPlayer pPlayer)
    {
        ServerLevel serverlevel = pPlayer.getLevel();
        pPlayer.awardStat(Stats.LEAVE_GAME);
        this.save(pPlayer);

        if (pPlayer.isPassenger())
        {
            Entity entity = pPlayer.getRootVehicle();

            if (entity.hasExactlyOnePlayerPassenger())
            {
                LOGGER.debug("Removing player mount");
                pPlayer.stopRiding();
                entity.getPassengersAndSelf().forEach((p_143990_) ->
                {
                    p_143990_.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
                });
            }
        }

        pPlayer.unRide();
        serverlevel.removePlayerImmediately(pPlayer, Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        pPlayer.getAdvancements().stopListening();
        this.players.remove(pPlayer);
        this.server.getCustomBossEvents().onPlayerDisconnect(pPlayer);
        UUID uuid = pPlayer.getUUID();
        ServerPlayer serverplayer = this.playersByUUID.get(uuid);

        if (serverplayer == pPlayer)
        {
            this.playersByUUID.remove(uuid);
            this.stats.remove(uuid);
            this.advancements.remove(uuid);
        }

        this.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, pPlayer));
    }

    @Nullable
    public Component canPlayerLogin(SocketAddress p_11257_, GameProfile p_11258_)
    {
        if (this.bans.isBanned(p_11258_))
        {
            UserBanListEntry userbanlistentry = this.bans.get(p_11258_);
            MutableComponent mutablecomponent1 = new TranslatableComponent("multiplayer.disconnect.banned.reason", userbanlistentry.getReason());

            if (userbanlistentry.getExpires() != null)
            {
                mutablecomponent1.append(new TranslatableComponent("multiplayer.disconnect.banned.expiration", BAN_DATE_FORMAT.format(userbanlistentry.getExpires())));
            }

            return mutablecomponent1;
        }
        else if (!this.isWhiteListed(p_11258_))
        {
            return new TranslatableComponent("multiplayer.disconnect.not_whitelisted");
        }
        else if (this.ipBans.isBanned(p_11257_))
        {
            IpBanListEntry ipbanlistentry = this.ipBans.get(p_11257_);
            MutableComponent mutablecomponent = new TranslatableComponent("multiplayer.disconnect.banned_ip.reason", ipbanlistentry.getReason());

            if (ipbanlistentry.getExpires() != null)
            {
                mutablecomponent.append(new TranslatableComponent("multiplayer.disconnect.banned_ip.expiration", BAN_DATE_FORMAT.format(ipbanlistentry.getExpires())));
            }

            return mutablecomponent;
        }
        else
        {
            return this.players.size() >= this.maxPlayers && !this.canBypassPlayerLimit(p_11258_) ? new TranslatableComponent("multiplayer.disconnect.server_full") : null;
        }
    }

    public ServerPlayer getPlayerForLogin(GameProfile pProfile)
    {
        UUID uuid = Player.createPlayerUUID(pProfile);
        List<ServerPlayer> list = Lists.newArrayList();

        for (int i = 0; i < this.players.size(); ++i)
        {
            ServerPlayer serverplayer = this.players.get(i);

            if (serverplayer.getUUID().equals(uuid))
            {
                list.add(serverplayer);
            }
        }

        ServerPlayer serverplayer2 = this.playersByUUID.get(pProfile.getId());

        if (serverplayer2 != null && !list.contains(serverplayer2))
        {
            list.add(serverplayer2);
        }

        for (ServerPlayer serverplayer1 : list)
        {
            serverplayer1.connection.disconnect(new TranslatableComponent("multiplayer.disconnect.duplicate_login"));
        }

        return new ServerPlayer(this.server, this.server.overworld(), pProfile);
    }

    public ServerPlayer respawn(ServerPlayer p_11237_, boolean p_11238_)
    {
        this.players.remove(p_11237_);
        p_11237_.getLevel().removePlayerImmediately(p_11237_, Entity.RemovalReason.DISCARDED);
        BlockPos blockpos = p_11237_.getRespawnPosition();
        float f = p_11237_.getRespawnAngle();
        boolean flag = p_11237_.isRespawnForced();
        ServerLevel serverlevel = this.server.getLevel(p_11237_.getRespawnDimension());
        Optional<Vec3> optional;

        if (serverlevel != null && blockpos != null)
        {
            optional = Player.findRespawnPositionAndUseSpawnBlock(serverlevel, blockpos, f, flag, p_11238_);
        }
        else
        {
            optional = Optional.empty();
        }

        ServerLevel serverlevel1 = serverlevel != null && optional.isPresent() ? serverlevel : this.server.overworld();
        ServerPlayer serverplayer = new ServerPlayer(this.server, serverlevel1, p_11237_.getGameProfile());
        serverplayer.connection = p_11237_.connection;
        serverplayer.restoreFrom(p_11237_, p_11238_);
        serverplayer.setId(p_11237_.getId());
        serverplayer.setMainArm(p_11237_.getMainArm());

        for (String s : p_11237_.getTags())
        {
            serverplayer.addTag(s);
        }

        boolean flag2 = false;

        if (optional.isPresent())
        {
            BlockState blockstate = serverlevel1.getBlockState(blockpos);
            boolean flag1 = blockstate.is(Blocks.RESPAWN_ANCHOR);
            Vec3 vec3 = optional.get();
            float f1;

            if (!blockstate.is(BlockTags.BEDS) && !flag1)
            {
                f1 = f;
            }
            else
            {
                Vec3 vec31 = Vec3.atBottomCenterOf(blockpos).subtract(vec3).normalize();
                f1 = (float)Mth.wrapDegrees(Mth.atan2(vec31.z, vec31.x) * (double)(180F / (float)Math.PI) - 90.0D);
            }

            serverplayer.moveTo(vec3.x, vec3.y, vec3.z, f1, 0.0F);
            serverplayer.setRespawnPosition(serverlevel1.dimension(), blockpos, f, flag, false);
            flag2 = !p_11238_ && flag1;
        }
        else if (blockpos != null)
        {
            serverplayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
        }

        while (!serverlevel1.noCollision(serverplayer) && serverplayer.getY() < (double)serverlevel1.getMaxBuildHeight())
        {
            serverplayer.setPos(serverplayer.getX(), serverplayer.getY() + 1.0D, serverplayer.getZ());
        }

        LevelData leveldata = serverplayer.level.getLevelData();
        serverplayer.connection.send(new ClientboundRespawnPacket(serverplayer.level.dimensionType(), serverplayer.level.dimension(), BiomeManager.obfuscateSeed(serverplayer.getLevel().getSeed()), serverplayer.gameMode.getGameModeForPlayer(), serverplayer.gameMode.getPreviousGameModeForPlayer(), serverplayer.getLevel().isDebug(), serverplayer.getLevel().isFlat(), p_11238_));
        serverplayer.connection.teleport(serverplayer.getX(), serverplayer.getY(), serverplayer.getZ(), serverplayer.getYRot(), serverplayer.getXRot());
        serverplayer.connection.send(new ClientboundSetDefaultSpawnPositionPacket(serverlevel1.getSharedSpawnPos(), serverlevel1.getSharedSpawnAngle()));
        serverplayer.connection.send(new ClientboundChangeDifficultyPacket(leveldata.getDifficulty(), leveldata.isDifficultyLocked()));
        serverplayer.connection.send(new ClientboundSetExperiencePacket(serverplayer.experienceProgress, serverplayer.totalExperience, serverplayer.experienceLevel));
        this.sendLevelInfo(serverplayer, serverlevel1);
        this.sendPlayerPermissionLevel(serverplayer);
        serverlevel1.addRespawnedPlayer(serverplayer);
        this.players.add(serverplayer);
        this.playersByUUID.put(serverplayer.getUUID(), serverplayer);
        serverplayer.initInventoryMenu();
        serverplayer.setHealth(serverplayer.getHealth());

        if (flag2)
        {
            serverplayer.connection.send(new ClientboundSoundPacket(SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), 1.0F, 1.0F));
        }

        return serverplayer;
    }

    public void sendPlayerPermissionLevel(ServerPlayer pPlayer)
    {
        GameProfile gameprofile = pPlayer.getGameProfile();
        int i = this.server.getProfilePermissions(gameprofile);
        this.sendPlayerPermissionLevel(pPlayer, i);
    }

    public void tick()
    {
        if (++this.sendAllPlayerInfoIn > 600)
        {
            this.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.UPDATE_LATENCY, this.players));
            this.sendAllPlayerInfoIn = 0;
        }
    }

    public void broadcastAll(Packet<?> pPacket)
    {
        for (ServerPlayer serverplayer : this.players)
        {
            serverplayer.connection.send(pPacket);
        }
    }

    public void broadcastAll(Packet<?> pPacket, ResourceKey<Level> p_11272_)
    {
        for (ServerPlayer serverplayer : this.players)
        {
            if (serverplayer.level.dimension() == p_11272_)
            {
                serverplayer.connection.send(pPacket);
            }
        }
    }

    public void broadcastToTeam(Player pPlayer, Component pMessage)
    {
        Team team = pPlayer.getTeam();

        if (team != null)
        {
            for (String s : team.getPlayers())
            {
                ServerPlayer serverplayer = this.getPlayerByName(s);

                if (serverplayer != null && serverplayer != pPlayer)
                {
                    serverplayer.sendMessage(pMessage, pPlayer.getUUID());
                }
            }
        }
    }

    public void broadcastToAllExceptTeam(Player pPlayer, Component pMessage)
    {
        Team team = pPlayer.getTeam();

        if (team == null)
        {
            this.broadcastMessage(pMessage, ChatType.SYSTEM, pPlayer.getUUID());
        }
        else
        {
            for (int i = 0; i < this.players.size(); ++i)
            {
                ServerPlayer serverplayer = this.players.get(i);

                if (serverplayer.getTeam() != team)
                {
                    serverplayer.sendMessage(pMessage, pPlayer.getUUID());
                }
            }
        }
    }

    public String[] getPlayerNamesArray()
    {
        String[] astring = new String[this.players.size()];

        for (int i = 0; i < this.players.size(); ++i)
        {
            astring[i] = this.players.get(i).getGameProfile().getName();
        }

        return astring;
    }

    public UserBanList getBans()
    {
        return this.bans;
    }

    public IpBanList getIpBans()
    {
        return this.ipBans;
    }

    public void op(GameProfile pProfile)
    {
        this.ops.add(new ServerOpListEntry(pProfile, this.server.getOperatorUserPermissionLevel(), this.ops.canBypassPlayerLimit(pProfile)));
        ServerPlayer serverplayer = this.getPlayer(pProfile.getId());

        if (serverplayer != null)
        {
            this.sendPlayerPermissionLevel(serverplayer);
        }
    }

    public void deop(GameProfile pProfile)
    {
        this.ops.remove(pProfile);
        ServerPlayer serverplayer = this.getPlayer(pProfile.getId());

        if (serverplayer != null)
        {
            this.sendPlayerPermissionLevel(serverplayer);
        }
    }

    private void sendPlayerPermissionLevel(ServerPlayer pPlayer, int p_11228_)
    {
        if (pPlayer.connection != null)
        {
            byte b0;

            if (p_11228_ <= 0)
            {
                b0 = 24;
            }
            else if (p_11228_ >= 4)
            {
                b0 = 28;
            }
            else
            {
                b0 = (byte)(24 + p_11228_);
            }

            pPlayer.connection.send(new ClientboundEntityEventPacket(pPlayer, b0));
        }

        this.server.getCommands().sendCommands(pPlayer);
    }

    public boolean isWhiteListed(GameProfile pProfile)
    {
        return !this.doWhiteList || this.ops.contains(pProfile) || this.whitelist.contains(pProfile);
    }

    public boolean isOp(GameProfile pProfile)
    {
        return this.ops.contains(pProfile) || this.server.isSingleplayerOwner(pProfile) && this.server.getWorldData().getAllowCommands() || this.allowCheatsForAllPlayers;
    }

    @Nullable
    public ServerPlayer getPlayerByName(String pUsername)
    {
        for (ServerPlayer serverplayer : this.players)
        {
            if (serverplayer.getGameProfile().getName().equalsIgnoreCase(pUsername))
            {
                return serverplayer;
            }
        }

        return null;
    }

    public void broadcast(@Nullable Player pExcept, double pX, double p_11244_, double pY, double p_11246_, ResourceKey<Level> pZ, Packet<?> p_11248_)
    {
        for (int i = 0; i < this.players.size(); ++i)
        {
            ServerPlayer serverplayer = this.players.get(i);

            if (serverplayer != pExcept && serverplayer.level.dimension() == pZ)
            {
                double d0 = pX - serverplayer.getX();
                double d1 = p_11244_ - serverplayer.getY();
                double d2 = pY - serverplayer.getZ();

                if (d0 * d0 + d1 * d1 + d2 * d2 < p_11246_ * p_11246_)
                {
                    serverplayer.connection.send(p_11248_);
                }
            }
        }
    }

    public void saveAll()
    {
        for (int i = 0; i < this.players.size(); ++i)
        {
            this.save(this.players.get(i));
        }
    }

    public UserWhiteList getWhiteList()
    {
        return this.whitelist;
    }

    public String[] getWhiteListNames()
    {
        return this.whitelist.getUserList();
    }

    public ServerOpList getOps()
    {
        return this.ops;
    }

    public String[] getOpNames()
    {
        return this.ops.getUserList();
    }

    public void reloadWhiteList()
    {
    }

    public void sendLevelInfo(ServerPlayer pPlayer, ServerLevel pLevel)
    {
        WorldBorder worldborder = this.server.overworld().getWorldBorder();
        pPlayer.connection.send(new ClientboundInitializeBorderPacket(worldborder));
        pPlayer.connection.send(new ClientboundSetTimePacket(pLevel.getGameTime(), pLevel.getDayTime(), pLevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)));
        pPlayer.connection.send(new ClientboundSetDefaultSpawnPositionPacket(pLevel.getSharedSpawnPos(), pLevel.getSharedSpawnAngle()));

        if (pLevel.isRaining())
        {
            pPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0.0F));
            pPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, pLevel.getRainLevel(1.0F)));
            pPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, pLevel.getThunderLevel(1.0F)));
        }
    }

    public void sendAllPlayerInfo(ServerPlayer pPlayer)
    {
        pPlayer.inventoryMenu.sendAllDataToRemote();
        pPlayer.resetSentInfo();
        pPlayer.connection.send(new ClientboundSetCarriedItemPacket(pPlayer.getInventory().selected));
    }

    public int getPlayerCount()
    {
        return this.players.size();
    }

    public int getMaxPlayers()
    {
        return this.maxPlayers;
    }

    public boolean isUsingWhitelist()
    {
        return this.doWhiteList;
    }

    public void setUsingWhiteList(boolean pWhitelistEnabled)
    {
        this.doWhiteList = pWhitelistEnabled;
    }

    public List<ServerPlayer> getPlayersWithAddress(String pAddress)
    {
        List<ServerPlayer> list = Lists.newArrayList();

        for (ServerPlayer serverplayer : this.players)
        {
            if (serverplayer.getIpAddress().equals(pAddress))
            {
                list.add(serverplayer);
            }
        }

        return list;
    }

    public int getViewDistance()
    {
        return this.viewDistance;
    }

    public MinecraftServer getServer()
    {
        return this.server;
    }

    public CompoundTag getSingleplayerData()
    {
        return null;
    }

    public void setAllowCheatsForAllPlayers(boolean p_11285_)
    {
        this.allowCheatsForAllPlayers = p_11285_;
    }

    public void removeAll()
    {
        for (int i = 0; i < this.players.size(); ++i)
        {
            (this.players.get(i)).connection.disconnect(new TranslatableComponent("multiplayer.disconnect.server_shutdown"));
        }
    }

    public void broadcastMessage(Component p_11265_, ChatType p_11266_, UUID p_11267_)
    {
        this.server.sendMessage(p_11265_, p_11267_);

        for (ServerPlayer serverplayer : this.players)
        {
            serverplayer.sendMessage(p_11265_, p_11266_, p_11267_);
        }
    }

    public void broadcastMessage(Component p_143992_, Function<ServerPlayer, Component> p_143993_, ChatType p_143994_, UUID p_143995_)
    {
        this.server.sendMessage(p_143992_, p_143995_);

        for (ServerPlayer serverplayer : this.players)
        {
            Component component = p_143993_.apply(serverplayer);

            if (component != null)
            {
                serverplayer.sendMessage(component, p_143994_, p_143995_);
            }
        }
    }

    public ServerStatsCounter getPlayerStats(Player pPlayer)
    {
        UUID uuid = pPlayer.getUUID();
        ServerStatsCounter serverstatscounter = uuid == null ? null : this.stats.get(uuid);

        if (serverstatscounter == null)
        {
            File file1 = this.server.getWorldPath(LevelResource.PLAYER_STATS_DIR).toFile();
            File file2 = new File(file1, uuid + ".json");

            if (!file2.exists())
            {
                File file3 = new File(file1, pPlayer.getName().getString() + ".json");
                Path path = file3.toPath();

                if (FileUtil.isPathNormalized(path) && FileUtil.isPathPortable(path) && path.startsWith(file1.getPath()) && file3.isFile())
                {
                    file3.renameTo(file2);
                }
            }

            serverstatscounter = new ServerStatsCounter(this.server, file2);
            this.stats.put(uuid, serverstatscounter);
        }

        return serverstatscounter;
    }

    public PlayerAdvancements getPlayerAdvancements(ServerPlayer p_11297_)
    {
        UUID uuid = p_11297_.getUUID();
        PlayerAdvancements playeradvancements = this.advancements.get(uuid);

        if (playeradvancements == null)
        {
            File file1 = this.server.getWorldPath(LevelResource.PLAYER_ADVANCEMENTS_DIR).toFile();
            File file2 = new File(file1, uuid + ".json");
            playeradvancements = new PlayerAdvancements(this.server.getFixerUpper(), this, this.server.getAdvancements(), file2, p_11297_);
            this.advancements.put(uuid, playeradvancements);
        }

        playeradvancements.setPlayer(p_11297_);
        return playeradvancements;
    }

    public void setViewDistance(int pViewDistance)
    {
        this.viewDistance = pViewDistance;
        this.broadcastAll(new ClientboundSetChunkCacheRadiusPacket(pViewDistance));

        for (ServerLevel serverlevel : this.server.getAllLevels())
        {
            if (serverlevel != null)
            {
                serverlevel.getChunkSource().setViewDistance(pViewDistance);
            }
        }
    }

    public List<ServerPlayer> getPlayers()
    {
        return this.players;
    }

    @Nullable
    public ServerPlayer getPlayer(UUID pPlayerUUID)
    {
        return this.playersByUUID.get(pPlayerUUID);
    }

    public boolean canBypassPlayerLimit(GameProfile pProfile)
    {
        return false;
    }

    public void reloadResources()
    {
        for (PlayerAdvancements playeradvancements : this.advancements.values())
        {
            playeradvancements.reload(this.server.getAdvancements());
        }

        this.broadcastAll(new ClientboundUpdateTagsPacket(this.server.getTags().serializeToNetwork(this.registryHolder)));
        ClientboundUpdateRecipesPacket clientboundupdaterecipespacket = new ClientboundUpdateRecipesPacket(this.server.getRecipeManager().getRecipes());

        for (ServerPlayer serverplayer : this.players)
        {
            serverplayer.connection.send(clientboundupdaterecipespacket);
            serverplayer.getRecipeBook().sendInitialRecipeBook(serverplayer);
        }
    }

    public boolean isAllowCheatsForAllPlayers()
    {
        return this.allowCheatsForAllPlayers;
    }
}
