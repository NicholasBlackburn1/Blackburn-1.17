package net.minecraft.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CommandSourceStack implements SharedSuggestionProvider
{
    public static final SimpleCommandExceptionType ERROR_NOT_PLAYER = new SimpleCommandExceptionType(new TranslatableComponent("permissions.requires.player"));
    public static final SimpleCommandExceptionType ERROR_NOT_ENTITY = new SimpleCommandExceptionType(new TranslatableComponent("permissions.requires.entity"));
    private final CommandSource source;
    private final Vec3 worldPosition;
    private final ServerLevel level;
    private final int permissionLevel;
    private final String textName;
    private final Component displayName;
    private final MinecraftServer server;
    private final boolean silent;
    @Nullable
    private final Entity entity;
    private final ResultConsumer<CommandSourceStack> consumer;
    private final EntityAnchorArgument.Anchor anchor;
    private final Vec2 rotation;

    public CommandSourceStack(CommandSource p_81302_, Vec3 p_81303_, Vec2 p_81304_, ServerLevel p_81305_, int p_81306_, String p_81307_, Component p_81308_, MinecraftServer p_81309_, @Nullable Entity p_81310_)
    {
        this(p_81302_, p_81303_, p_81304_, p_81305_, p_81306_, p_81307_, p_81308_, p_81309_, p_81310_, false, (p_81361_, p_81362_, p_81363_) ->
        {
        }, EntityAnchorArgument.Anchor.FEET);
    }

    protected CommandSourceStack(CommandSource p_81312_, Vec3 p_81313_, Vec2 p_81314_, ServerLevel p_81315_, int p_81316_, String p_81317_, Component p_81318_, MinecraftServer p_81319_, @Nullable Entity p_81320_, boolean p_81321_, ResultConsumer<CommandSourceStack> p_81322_, EntityAnchorArgument.Anchor p_81323_)
    {
        this.source = p_81312_;
        this.worldPosition = p_81313_;
        this.level = p_81315_;
        this.silent = p_81321_;
        this.entity = p_81320_;
        this.permissionLevel = p_81316_;
        this.textName = p_81317_;
        this.displayName = p_81318_;
        this.server = p_81319_;
        this.consumer = p_81322_;
        this.anchor = p_81323_;
        this.rotation = p_81314_;
    }

    public CommandSourceStack withSource(CommandSource p_165485_)
    {
        return this.source == p_165485_ ? this : new CommandSourceStack(p_165485_, this.worldPosition, this.rotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, this.anchor);
    }

    public CommandSourceStack withEntity(Entity pEntity)
    {
        return this.entity == pEntity ? this : new CommandSourceStack(this.source, this.worldPosition, this.rotation, this.level, this.permissionLevel, pEntity.getName().getString(), pEntity.getDisplayName(), this.server, pEntity, this.silent, this.consumer, this.anchor);
    }

    public CommandSourceStack withPosition(Vec3 pPos)
    {
        return this.worldPosition.equals(pPos) ? this : new CommandSourceStack(this.source, pPos, this.rotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, this.anchor);
    }

    public CommandSourceStack withRotation(Vec2 pPitchYaw)
    {
        return this.rotation.equals(pPitchYaw) ? this : new CommandSourceStack(this.source, this.worldPosition, pPitchYaw, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, this.anchor);
    }

    public CommandSourceStack withCallback(ResultConsumer<CommandSourceStack> pResultConsumer)
    {
        return this.consumer.equals(pResultConsumer) ? this : new CommandSourceStack(this.source, this.worldPosition, this.rotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, pResultConsumer, this.anchor);
    }

    public CommandSourceStack withCallback(ResultConsumer<CommandSourceStack> pResultConsumer, BinaryOperator<ResultConsumer<CommandSourceStack>> p_81338_)
    {
        ResultConsumer<CommandSourceStack> resultconsumer = p_81338_.apply(this.consumer, pResultConsumer);
        return this.withCallback(resultconsumer);
    }

    public CommandSourceStack withSuppressedOutput()
    {
        return !this.silent && !this.source.alwaysAccepts() ? new CommandSourceStack(this.source, this.worldPosition, this.rotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, true, this.consumer, this.anchor) : this;
    }

    public CommandSourceStack withPermission(int pLevel)
    {
        return pLevel == this.permissionLevel ? this : new CommandSourceStack(this.source, this.worldPosition, this.rotation, this.level, pLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, this.anchor);
    }

    public CommandSourceStack withMaximumPermission(int pLevel)
    {
        return pLevel <= this.permissionLevel ? this : new CommandSourceStack(this.source, this.worldPosition, this.rotation, this.level, pLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, this.anchor);
    }

    public CommandSourceStack withAnchor(EntityAnchorArgument.Anchor pEntityAnchorType)
    {
        return pEntityAnchorType == this.anchor ? this : new CommandSourceStack(this.source, this.worldPosition, this.rotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, pEntityAnchorType);
    }

    public CommandSourceStack withLevel(ServerLevel pLevel)
    {
        if (pLevel == this.level)
        {
            return this;
        }
        else
        {
            double d0 = DimensionType.getTeleportationScale(this.level.dimensionType(), pLevel.dimensionType());
            Vec3 vec3 = new Vec3(this.worldPosition.x * d0, this.worldPosition.y, this.worldPosition.z * d0);
            return new CommandSourceStack(this.source, vec3, this.rotation, pLevel, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, this.anchor);
        }
    }

    public CommandSourceStack facing(Entity pEntity, EntityAnchorArgument.Anchor pAnchorType)
    {
        return this.facing(pAnchorType.apply(pEntity));
    }

    public CommandSourceStack facing(Vec3 pEntity)
    {
        Vec3 vec3 = this.anchor.apply(this);
        double d0 = pEntity.x - vec3.x;
        double d1 = pEntity.y - vec3.y;
        double d2 = pEntity.z - vec3.z;
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        float f = Mth.wrapDegrees((float)(-(Mth.atan2(d1, d3) * (double)(180F / (float)Math.PI))));
        float f1 = Mth.wrapDegrees((float)(Mth.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F);
        return this.withRotation(new Vec2(f, f1));
    }

    public Component getDisplayName()
    {
        return this.displayName;
    }

    public String getTextName()
    {
        return this.textName;
    }

    public boolean hasPermission(int pLevel)
    {
        return this.permissionLevel >= pLevel;
    }

    public Vec3 getPosition()
    {
        return this.worldPosition;
    }

    public ServerLevel getLevel()
    {
        return this.level;
    }

    @Nullable
    public Entity getEntity()
    {
        return this.entity;
    }

    public Entity getEntityOrException() throws CommandSyntaxException
    {
        if (this.entity == null)
        {
            throw ERROR_NOT_ENTITY.create();
        }
        else
        {
            return this.entity;
        }
    }

    public ServerPlayer getPlayerOrException() throws CommandSyntaxException
    {
        if (!(this.entity instanceof ServerPlayer))
        {
            throw ERROR_NOT_PLAYER.create();
        }
        else
        {
            return (ServerPlayer)this.entity;
        }
    }

    public Vec2 getRotation()
    {
        return this.rotation;
    }

    public MinecraftServer getServer()
    {
        return this.server;
    }

    public EntityAnchorArgument.Anchor getAnchor()
    {
        return this.anchor;
    }

    public void sendSuccess(Component pMessage, boolean pAllowLogging)
    {
        if (this.source.acceptsSuccess() && !this.silent)
        {
            this.source.sendMessage(pMessage, Util.NIL_UUID);
        }

        if (pAllowLogging && this.source.shouldInformAdmins() && !this.silent)
        {
            this.broadcastToAdmins(pMessage);
        }
    }

    private void broadcastToAdmins(Component pMessage)
    {
        Component component = (new TranslatableComponent("chat.type.admin", this.getDisplayName(), pMessage)).m_130944_(new ChatFormatting[] {ChatFormatting.GRAY, ChatFormatting.ITALIC});

        if (this.server.getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK))
        {
            for (ServerPlayer serverplayer : this.server.getPlayerList().getPlayers())
            {
                if (serverplayer != this.source && this.server.getPlayerList().isOp(serverplayer.getGameProfile()))
                {
                    serverplayer.sendMessage(component, Util.NIL_UUID);
                }
            }
        }

        if (this.source != this.server && this.server.getGameRules().getBoolean(GameRules.RULE_LOGADMINCOMMANDS))
        {
            this.server.sendMessage(component, Util.NIL_UUID);
        }
    }

    public void sendFailure(Component pMessage)
    {
        if (this.source.acceptsFailure() && !this.silent)
        {
            this.source.sendMessage((new TextComponent("")).append(pMessage).withStyle(ChatFormatting.RED), Util.NIL_UUID);
        }
    }

    public void onCommandComplete(CommandContext<CommandSourceStack> pContext, boolean pSuccess, int pResult)
    {
        if (this.consumer != null)
        {
            this.consumer.onCommandComplete(pContext, pSuccess, pResult);
        }
    }

    public Collection<String> getOnlinePlayerNames()
    {
        return Lists.newArrayList(this.server.getPlayerNames());
    }

    public Collection<String> getAllTeams()
    {
        return this.server.getScoreboard().getTeamNames();
    }

    public Collection<ResourceLocation> getAvailableSoundEvents()
    {
        return Registry.SOUND_EVENT.keySet();
    }

    public Stream<ResourceLocation> getRecipeNames()
    {
        return this.server.getRecipeManager().getRecipeIds();
    }

    public CompletableFuture<Suggestions> customSuggestion(CommandContext<SharedSuggestionProvider> pContext, SuggestionsBuilder pSuggestionsBuilder)
    {
        return null;
    }

    public Set<ResourceKey<Level>> levels()
    {
        return this.server.levelKeys();
    }

    public RegistryAccess registryAccess()
    {
        return this.server.registryAccess();
    }
}
