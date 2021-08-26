package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

public class TeamCommand
{
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_EXISTS = new SimpleCommandExceptionType(new TranslatableComponent("commands.team.add.duplicate"));
    private static final DynamicCommandExceptionType ERROR_TEAM_NAME_TOO_LONG = new DynamicCommandExceptionType((p_138921_) ->
    {
        return new TranslatableComponent("commands.team.add.longName", p_138921_);
    });
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_EMPTY = new SimpleCommandExceptionType(new TranslatableComponent("commands.team.empty.unchanged"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_NAME = new SimpleCommandExceptionType(new TranslatableComponent("commands.team.option.name.unchanged"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_COLOR = new SimpleCommandExceptionType(new TranslatableComponent("commands.team.option.color.unchanged"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYFIRE_ENABLED = new SimpleCommandExceptionType(new TranslatableComponent("commands.team.option.friendlyfire.alreadyEnabled"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYFIRE_DISABLED = new SimpleCommandExceptionType(new TranslatableComponent("commands.team.option.friendlyfire.alreadyDisabled"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_ENABLED = new SimpleCommandExceptionType(new TranslatableComponent("commands.team.option.seeFriendlyInvisibles.alreadyEnabled"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_DISABLED = new SimpleCommandExceptionType(new TranslatableComponent("commands.team.option.seeFriendlyInvisibles.alreadyDisabled"));
    private static final SimpleCommandExceptionType ERROR_TEAM_NAMETAG_VISIBLITY_UNCHANGED = new SimpleCommandExceptionType(new TranslatableComponent("commands.team.option.nametagVisibility.unchanged"));
    private static final SimpleCommandExceptionType ERROR_TEAM_DEATH_MESSAGE_VISIBLITY_UNCHANGED = new SimpleCommandExceptionType(new TranslatableComponent("commands.team.option.deathMessageVisibility.unchanged"));
    private static final SimpleCommandExceptionType ERROR_TEAM_COLLISION_UNCHANGED = new SimpleCommandExceptionType(new TranslatableComponent("commands.team.option.collisionRule.unchanged"));

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher)
    {
        pDispatcher.register(Commands.literal("team").requires((p_138925_) ->
        {
            return p_138925_.hasPermission(2);
        }).then(Commands.literal("list").executes((p_138876_) ->
        {
            return listTeams(p_138876_.getSource());
        }).then(Commands.argument("team", TeamArgument.team()).executes((p_138995_) ->
        {
            return listMembers(p_138995_.getSource(), TeamArgument.getTeam(p_138995_, "team"));
        }))).then(Commands.literal("add").then(Commands.argument("team", StringArgumentType.word()).executes((p_138993_) ->
        {
            return createTeam(p_138993_.getSource(), StringArgumentType.getString(p_138993_, "team"));
        }).then(Commands.argument("displayName", ComponentArgument.textComponent()).executes((p_138991_) ->
        {
            return createTeam(p_138991_.getSource(), StringArgumentType.getString(p_138991_, "team"), ComponentArgument.getComponent(p_138991_, "displayName"));
        })))).then(Commands.literal("remove").then(Commands.argument("team", TeamArgument.team()).executes((p_138989_) ->
        {
            return deleteTeam(p_138989_.getSource(), TeamArgument.getTeam(p_138989_, "team"));
        }))).then(Commands.literal("empty").then(Commands.argument("team", TeamArgument.team()).executes((p_138987_) ->
        {
            return emptyTeam(p_138987_.getSource(), TeamArgument.getTeam(p_138987_, "team"));
        }))).then(Commands.literal("join").then(Commands.argument("team", TeamArgument.team()).executes((p_138985_) ->
        {
            return joinTeam(p_138985_.getSource(), TeamArgument.getTeam(p_138985_, "team"), Collections.singleton(p_138985_.getSource().getEntityOrException().getScoreboardName()));
        }).then(Commands.argument("members", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).executes((p_138983_) ->
        {
            return joinTeam(p_138983_.getSource(), TeamArgument.getTeam(p_138983_, "team"), ScoreHolderArgument.getNamesWithDefaultWildcard(p_138983_, "members"));
        })))).then(Commands.literal("leave").then(Commands.argument("members", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).executes((p_138981_) ->
        {
            return leaveTeam(p_138981_.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(p_138981_, "members"));
        }))).then(Commands.literal("modify").then(Commands.argument("team", TeamArgument.team()).then(Commands.literal("displayName").then(Commands.argument("displayName", ComponentArgument.textComponent()).executes((p_138979_) ->
        {
            return setDisplayName(p_138979_.getSource(), TeamArgument.getTeam(p_138979_, "team"), ComponentArgument.getComponent(p_138979_, "displayName"));
        }))).then(Commands.literal("color").then(Commands.argument("value", ColorArgument.color()).executes((p_138977_) ->
        {
            return setColor(p_138977_.getSource(), TeamArgument.getTeam(p_138977_, "team"), ColorArgument.getColor(p_138977_, "value"));
        }))).then(Commands.literal("friendlyFire").then(Commands.argument("allowed", BoolArgumentType.bool()).executes((p_138975_) ->
        {
            return setFriendlyFire(p_138975_.getSource(), TeamArgument.getTeam(p_138975_, "team"), BoolArgumentType.getBool(p_138975_, "allowed"));
        }))).then(Commands.literal("seeFriendlyInvisibles").then(Commands.argument("allowed", BoolArgumentType.bool()).executes((p_138973_) ->
        {
            return setFriendlySight(p_138973_.getSource(), TeamArgument.getTeam(p_138973_, "team"), BoolArgumentType.getBool(p_138973_, "allowed"));
        }))).then(Commands.literal("nametagVisibility").then(Commands.literal("never").executes((p_138971_) ->
        {
            return setNametagVisibility(p_138971_.getSource(), TeamArgument.getTeam(p_138971_, "team"), Team.Visibility.NEVER);
        })).then(Commands.literal("hideForOtherTeams").executes((p_138969_) ->
        {
            return setNametagVisibility(p_138969_.getSource(), TeamArgument.getTeam(p_138969_, "team"), Team.Visibility.HIDE_FOR_OTHER_TEAMS);
        })).then(Commands.literal("hideForOwnTeam").executes((p_138967_) ->
        {
            return setNametagVisibility(p_138967_.getSource(), TeamArgument.getTeam(p_138967_, "team"), Team.Visibility.HIDE_FOR_OWN_TEAM);
        })).then(Commands.literal("always").executes((p_138965_) ->
        {
            return setNametagVisibility(p_138965_.getSource(), TeamArgument.getTeam(p_138965_, "team"), Team.Visibility.ALWAYS);
        }))).then(Commands.literal("deathMessageVisibility").then(Commands.literal("never").executes((p_138963_) ->
        {
            return setDeathMessageVisibility(p_138963_.getSource(), TeamArgument.getTeam(p_138963_, "team"), Team.Visibility.NEVER);
        })).then(Commands.literal("hideForOtherTeams").executes((p_138961_) ->
        {
            return setDeathMessageVisibility(p_138961_.getSource(), TeamArgument.getTeam(p_138961_, "team"), Team.Visibility.HIDE_FOR_OTHER_TEAMS);
        })).then(Commands.literal("hideForOwnTeam").executes((p_138959_) ->
        {
            return setDeathMessageVisibility(p_138959_.getSource(), TeamArgument.getTeam(p_138959_, "team"), Team.Visibility.HIDE_FOR_OWN_TEAM);
        })).then(Commands.literal("always").executes((p_138957_) ->
        {
            return setDeathMessageVisibility(p_138957_.getSource(), TeamArgument.getTeam(p_138957_, "team"), Team.Visibility.ALWAYS);
        }))).then(Commands.literal("collisionRule").then(Commands.literal("never").executes((p_138955_) ->
        {
            return setCollision(p_138955_.getSource(), TeamArgument.getTeam(p_138955_, "team"), Team.CollisionRule.NEVER);
        })).then(Commands.literal("pushOwnTeam").executes((p_138953_) ->
        {
            return setCollision(p_138953_.getSource(), TeamArgument.getTeam(p_138953_, "team"), Team.CollisionRule.PUSH_OWN_TEAM);
        })).then(Commands.literal("pushOtherTeams").executes((p_138951_) ->
        {
            return setCollision(p_138951_.getSource(), TeamArgument.getTeam(p_138951_, "team"), Team.CollisionRule.PUSH_OTHER_TEAMS);
        })).then(Commands.literal("always").executes((p_138942_) ->
        {
            return setCollision(p_138942_.getSource(), TeamArgument.getTeam(p_138942_, "team"), Team.CollisionRule.ALWAYS);
        }))).then(Commands.literal("prefix").then(Commands.argument("prefix", ComponentArgument.textComponent()).executes((p_138923_) ->
        {
            return setPrefix(p_138923_.getSource(), TeamArgument.getTeam(p_138923_, "team"), ComponentArgument.getComponent(p_138923_, "prefix"));
        }))).then(Commands.literal("suffix").then(Commands.argument("suffix", ComponentArgument.textComponent()).executes((p_138880_) ->
        {
            return setSuffix(p_138880_.getSource(), TeamArgument.getTeam(p_138880_, "team"), ComponentArgument.getComponent(p_138880_, "suffix"));
        }))))));
    }

    private static int leaveTeam(CommandSourceStack pSource, Collection<String> pPlayers)
    {
        Scoreboard scoreboard = pSource.getServer().getScoreboard();

        for (String s : pPlayers)
        {
            scoreboard.removePlayerFromTeam(s);
        }

        if (pPlayers.size() == 1)
        {
            pSource.sendSuccess(new TranslatableComponent("commands.team.leave.success.single", pPlayers.iterator().next()), true);
        }
        else
        {
            pSource.sendSuccess(new TranslatableComponent("commands.team.leave.success.multiple", pPlayers.size()), true);
        }

        return pPlayers.size();
    }

    private static int joinTeam(CommandSourceStack pSource, PlayerTeam pTeam, Collection<String> pPlayers)
    {
        Scoreboard scoreboard = pSource.getServer().getScoreboard();

        for (String s : pPlayers)
        {
            scoreboard.addPlayerToTeam(s, pTeam);
        }

        if (pPlayers.size() == 1)
        {
            pSource.sendSuccess(new TranslatableComponent("commands.team.join.success.single", pPlayers.iterator().next(), pTeam.getFormattedDisplayName()), true);
        }
        else
        {
            pSource.sendSuccess(new TranslatableComponent("commands.team.join.success.multiple", pPlayers.size(), pTeam.getFormattedDisplayName()), true);
        }

        return pPlayers.size();
    }

    private static int setNametagVisibility(CommandSourceStack pSource, PlayerTeam pTeam, Team.Visibility pVisibility) throws CommandSyntaxException
    {
        if (pTeam.getNameTagVisibility() == pVisibility)
        {
            throw ERROR_TEAM_NAMETAG_VISIBLITY_UNCHANGED.create();
        }
        else
        {
            pTeam.setNameTagVisibility(pVisibility);
            pSource.sendSuccess(new TranslatableComponent("commands.team.option.nametagVisibility.success", pTeam.getFormattedDisplayName(), pVisibility.getDisplayName()), true);
            return 0;
        }
    }

    private static int setDeathMessageVisibility(CommandSourceStack pSource, PlayerTeam pTeam, Team.Visibility pVisibility) throws CommandSyntaxException
    {
        if (pTeam.getDeathMessageVisibility() == pVisibility)
        {
            throw ERROR_TEAM_DEATH_MESSAGE_VISIBLITY_UNCHANGED.create();
        }
        else
        {
            pTeam.setDeathMessageVisibility(pVisibility);
            pSource.sendSuccess(new TranslatableComponent("commands.team.option.deathMessageVisibility.success", pTeam.getFormattedDisplayName(), pVisibility.getDisplayName()), true);
            return 0;
        }
    }

    private static int setCollision(CommandSourceStack pSource, PlayerTeam pTeam, Team.CollisionRule pRule) throws CommandSyntaxException
    {
        if (pTeam.getCollisionRule() == pRule)
        {
            throw ERROR_TEAM_COLLISION_UNCHANGED.create();
        }
        else
        {
            pTeam.setCollisionRule(pRule);
            pSource.sendSuccess(new TranslatableComponent("commands.team.option.collisionRule.success", pTeam.getFormattedDisplayName(), pRule.getDisplayName()), true);
            return 0;
        }
    }

    private static int setFriendlySight(CommandSourceStack pSource, PlayerTeam pTeam, boolean pValue) throws CommandSyntaxException
    {
        if (pTeam.canSeeFriendlyInvisibles() == pValue)
        {
            if (pValue)
            {
                throw ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_ENABLED.create();
            }
            else
            {
                throw ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_DISABLED.create();
            }
        }
        else
        {
            pTeam.setSeeFriendlyInvisibles(pValue);
            pSource.sendSuccess(new TranslatableComponent("commands.team.option.seeFriendlyInvisibles." + (pValue ? "enabled" : "disabled"), pTeam.getFormattedDisplayName()), true);
            return 0;
        }
    }

    private static int setFriendlyFire(CommandSourceStack pSource, PlayerTeam pTeam, boolean pValue) throws CommandSyntaxException
    {
        if (pTeam.isAllowFriendlyFire() == pValue)
        {
            if (pValue)
            {
                throw ERROR_TEAM_ALREADY_FRIENDLYFIRE_ENABLED.create();
            }
            else
            {
                throw ERROR_TEAM_ALREADY_FRIENDLYFIRE_DISABLED.create();
            }
        }
        else
        {
            pTeam.setAllowFriendlyFire(pValue);
            pSource.sendSuccess(new TranslatableComponent("commands.team.option.friendlyfire." + (pValue ? "enabled" : "disabled"), pTeam.getFormattedDisplayName()), true);
            return 0;
        }
    }

    private static int setDisplayName(CommandSourceStack pSource, PlayerTeam pTeam, Component pValue) throws CommandSyntaxException
    {
        if (pTeam.getDisplayName().equals(pValue))
        {
            throw ERROR_TEAM_ALREADY_NAME.create();
        }
        else
        {
            pTeam.setDisplayName(pValue);
            pSource.sendSuccess(new TranslatableComponent("commands.team.option.name.success", pTeam.getFormattedDisplayName()), true);
            return 0;
        }
    }

    private static int setColor(CommandSourceStack pSource, PlayerTeam pTeam, ChatFormatting pValue) throws CommandSyntaxException
    {
        if (pTeam.getColor() == pValue)
        {
            throw ERROR_TEAM_ALREADY_COLOR.create();
        }
        else
        {
            pTeam.setColor(pValue);
            pSource.sendSuccess(new TranslatableComponent("commands.team.option.color.success", pTeam.getFormattedDisplayName(), pValue.getName()), true);
            return 0;
        }
    }

    private static int emptyTeam(CommandSourceStack pSource, PlayerTeam pTeam) throws CommandSyntaxException
    {
        Scoreboard scoreboard = pSource.getServer().getScoreboard();
        Collection<String> collection = Lists.newArrayList(pTeam.getPlayers());

        if (collection.isEmpty())
        {
            throw ERROR_TEAM_ALREADY_EMPTY.create();
        }
        else
        {
            for (String s : collection)
            {
                scoreboard.removePlayerFromTeam(s, pTeam);
            }

            pSource.sendSuccess(new TranslatableComponent("commands.team.empty.success", collection.size(), pTeam.getFormattedDisplayName()), true);
            return collection.size();
        }
    }

    private static int deleteTeam(CommandSourceStack pSource, PlayerTeam pTeam)
    {
        Scoreboard scoreboard = pSource.getServer().getScoreboard();
        scoreboard.removePlayerTeam(pTeam);
        pSource.sendSuccess(new TranslatableComponent("commands.team.remove.success", pTeam.getFormattedDisplayName()), true);
        return scoreboard.getPlayerTeams().size();
    }

    private static int createTeam(CommandSourceStack pSource, String pName) throws CommandSyntaxException
    {
        return createTeam(pSource, pName, new TextComponent(pName));
    }

    private static int createTeam(CommandSourceStack pSource, String pName, Component p_138916_) throws CommandSyntaxException
    {
        Scoreboard scoreboard = pSource.getServer().getScoreboard();

        if (scoreboard.getPlayerTeam(pName) != null)
        {
            throw ERROR_TEAM_ALREADY_EXISTS.create();
        }
        else if (pName.length() > 16)
        {
            throw ERROR_TEAM_NAME_TOO_LONG.create(16);
        }
        else
        {
            PlayerTeam playerteam = scoreboard.addPlayerTeam(pName);
            playerteam.setDisplayName(p_138916_);
            pSource.sendSuccess(new TranslatableComponent("commands.team.add.success", playerteam.getFormattedDisplayName()), true);
            return scoreboard.getPlayerTeams().size();
        }
    }

    private static int listMembers(CommandSourceStack pSource, PlayerTeam pTeam)
    {
        Collection<String> collection = pTeam.getPlayers();

        if (collection.isEmpty())
        {
            pSource.sendSuccess(new TranslatableComponent("commands.team.list.members.empty", pTeam.getFormattedDisplayName()), false);
        }
        else
        {
            pSource.sendSuccess(new TranslatableComponent("commands.team.list.members.success", pTeam.getFormattedDisplayName(), collection.size(), ComponentUtils.formatList(collection)), false);
        }

        return collection.size();
    }

    private static int listTeams(CommandSourceStack pSource)
    {
        Collection<PlayerTeam> collection = pSource.getServer().getScoreboard().getPlayerTeams();

        if (collection.isEmpty())
        {
            pSource.sendSuccess(new TranslatableComponent("commands.team.list.teams.empty"), false);
        }
        else
        {
            pSource.sendSuccess(new TranslatableComponent("commands.team.list.teams.success", collection.size(), ComponentUtils.formatList(collection, PlayerTeam::getFormattedDisplayName)), false);
        }

        return collection.size();
    }

    private static int setPrefix(CommandSourceStack pSource, PlayerTeam pTeam, Component pPrefix)
    {
        pTeam.setPlayerPrefix(pPrefix);
        pSource.sendSuccess(new TranslatableComponent("commands.team.option.prefix.success", pPrefix), false);
        return 1;
    }

    private static int setSuffix(CommandSourceStack pSource, PlayerTeam pTeam, Component pSuffix)
    {
        pTeam.setPlayerSuffix(pSuffix);
        pSource.sendSuccess(new TranslatableComponent("commands.team.option.suffix.success", pSuffix), false);
        return 1;
    }
}
