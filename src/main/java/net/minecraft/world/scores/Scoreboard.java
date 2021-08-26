package net.minecraft.world.scores;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class Scoreboard
{
    public static final int DISPLAY_SLOT_LIST = 0;
    public static final int DISPLAY_SLOT_SIDEBAR = 1;
    public static final int DISPLAY_SLOT_BELOW_NAME = 2;
    public static final int DISPLAY_SLOT_TEAMS_SIDEBAR_START = 3;
    public static final int DISPLAY_SLOT_TEAMS_SIDEBAR_END = 18;
    public static final int DISPLAY_SLOTS = 19;
    public static final int MAX_NAME_LENGTH = 40;
    private final Map<String, Objective> objectivesByName = Maps.newHashMap();
    private final Map<ObjectiveCriteria, List<Objective>> objectivesByCriteria = Maps.newHashMap();
    private final Map<String, Map<Objective, Score>> playerScores = Maps.newHashMap();
    private final Objective[] displayObjectives = new Objective[19];
    private final Map<String, PlayerTeam> teamsByName = Maps.newHashMap();
    private final Map<String, PlayerTeam> teamsByPlayer = Maps.newHashMap();
    private static String[] displaySlotNames;

    public boolean hasObjective(String p_83460_)
    {
        return this.objectivesByName.containsKey(p_83460_);
    }

    public Objective getOrCreateObjective(String p_83470_)
    {
        return this.objectivesByName.get(p_83470_);
    }

    @Nullable
    public Objective getObjective(@Nullable String pName)
    {
        return this.objectivesByName.get(pName);
    }

    public Objective addObjective(String p_83437_, ObjectiveCriteria p_83438_, Component p_83439_, ObjectiveCriteria.RenderType p_83440_)
    {
        if (p_83437_.length() > 16)
        {
            throw new IllegalArgumentException("The objective name '" + p_83437_ + "' is too long!");
        }
        else if (this.objectivesByName.containsKey(p_83437_))
        {
            throw new IllegalArgumentException("An objective with the name '" + p_83437_ + "' already exists!");
        }
        else
        {
            Objective objective = new Objective(this, p_83437_, p_83438_, p_83439_, p_83440_);
            this.objectivesByCriteria.computeIfAbsent(p_83438_, (p_83426_) ->
            {
                return Lists.newArrayList();
            }).add(objective);
            this.objectivesByName.put(p_83437_, objective);
            this.onObjectiveAdded(objective);
            return objective;
        }
    }

    public final void forAllObjectives(ObjectiveCriteria p_83428_, String p_83429_, Consumer<Score> p_83430_)
    {
        this.objectivesByCriteria.getOrDefault(p_83428_, Collections.emptyList()).forEach((p_83444_) ->
        {
            p_83430_.accept(this.getOrCreatePlayerScore(p_83429_, p_83444_));
        });
    }

    public boolean hasPlayerScore(String pName, Objective pObjective)
    {
        Map<Objective, Score> map = this.playerScores.get(pName);

        if (map == null)
        {
            return false;
        }
        else
        {
            Score score = map.get(pObjective);
            return score != null;
        }
    }

    public Score getOrCreatePlayerScore(String pUsername, Objective pObjective)
    {
        if (pUsername.length() > 40)
        {
            throw new IllegalArgumentException("The player name '" + pUsername + "' is too long!");
        }
        else
        {
            Map<Objective, Score> map = this.playerScores.computeIfAbsent(pUsername, (p_83507_) ->
            {
                return Maps.newHashMap();
            });
            return map.computeIfAbsent(pObjective, (p_83487_) ->
            {
                Score score = new Score(this, p_83487_, pUsername);
                score.setScore(0);
                return score;
            });
        }
    }

    public Collection<Score> getPlayerScores(Objective pName)
    {
        List<Score> list = Lists.newArrayList();

        for (Map<Objective, Score> map : this.playerScores.values())
        {
            Score score = map.get(pName);

            if (score != null)
            {
                list.add(score);
            }
        }

        list.sort(Score.SCORE_COMPARATOR);
        return list;
    }

    public Collection<Objective> getObjectives()
    {
        return this.objectivesByName.values();
    }

    public Collection<String> getObjectiveNames()
    {
        return this.objectivesByName.keySet();
    }

    public Collection<String> getTrackedPlayers()
    {
        return Lists.newArrayList(this.playerScores.keySet());
    }

    public void resetPlayerScore(String pName, @Nullable Objective pObjective)
    {
        if (pObjective == null)
        {
            Map<Objective, Score> map = this.playerScores.remove(pName);

            if (map != null)
            {
                this.onPlayerRemoved(pName);
            }
        }
        else
        {
            Map<Objective, Score> map2 = this.playerScores.get(pName);

            if (map2 != null)
            {
                Score score = map2.remove(pObjective);

                if (map2.size() < 1)
                {
                    Map<Objective, Score> map1 = this.playerScores.remove(pName);

                    if (map1 != null)
                    {
                        this.onPlayerRemoved(pName);
                    }
                }
                else if (score != null)
                {
                    this.onPlayerScoreRemoved(pName, pObjective);
                }
            }
        }
    }

    public Map<Objective, Score> getPlayerScores(String pName)
    {
        Map<Objective, Score> map = this.playerScores.get(pName);

        if (map == null)
        {
            map = Maps.newHashMap();
        }

        return map;
    }

    public void removeObjective(Objective pObjective)
    {
        this.objectivesByName.remove(pObjective.getName());

        for (int i = 0; i < 19; ++i)
        {
            if (this.getDisplayObjective(i) == pObjective)
            {
                this.setDisplayObjective(i, (Objective)null);
            }
        }

        List<Objective> list = this.objectivesByCriteria.get(pObjective.getCriteria());

        if (list != null)
        {
            list.remove(pObjective);
        }

        for (Map<Objective, Score> map : this.playerScores.values())
        {
            map.remove(pObjective);
        }

        this.onObjectiveRemoved(pObjective);
    }

    public void setDisplayObjective(int pObjectiveSlot, @Nullable Objective pObjective)
    {
        this.displayObjectives[pObjectiveSlot] = pObjective;
    }

    @Nullable
    public Objective getDisplayObjective(int pSlot)
    {
        return this.displayObjectives[pSlot];
    }

    @Nullable
    public PlayerTeam getPlayerTeam(String pTeamName)
    {
        return this.teamsByName.get(pTeamName);
    }

    public PlayerTeam addPlayerTeam(String pName)
    {
        if (pName.length() > 16)
        {
            throw new IllegalArgumentException("The team name '" + pName + "' is too long!");
        }
        else
        {
            PlayerTeam playerteam = this.getPlayerTeam(pName);

            if (playerteam != null)
            {
                throw new IllegalArgumentException("A team with the name '" + pName + "' already exists!");
            }
            else
            {
                playerteam = new PlayerTeam(this, pName);
                this.teamsByName.put(pName, playerteam);
                this.onTeamAdded(playerteam);
                return playerteam;
            }
        }
    }

    public void removePlayerTeam(PlayerTeam pPlayerTeam)
    {
        this.teamsByName.remove(pPlayerTeam.getName());

        for (String s : pPlayerTeam.getPlayers())
        {
            this.teamsByPlayer.remove(s);
        }

        this.onTeamRemoved(pPlayerTeam);
    }

    public boolean addPlayerToTeam(String p_83434_, PlayerTeam p_83435_)
    {
        if (p_83434_.length() > 40)
        {
            throw new IllegalArgumentException("The player name '" + p_83434_ + "' is too long!");
        }
        else
        {
            if (this.getPlayersTeam(p_83434_) != null)
            {
                this.removePlayerFromTeam(p_83434_);
            }

            this.teamsByPlayer.put(p_83434_, p_83435_);
            return p_83435_.getPlayers().add(p_83434_);
        }
    }

    public boolean removePlayerFromTeam(String pPlayerName)
    {
        PlayerTeam playerteam = this.getPlayersTeam(pPlayerName);

        if (playerteam != null)
        {
            this.removePlayerFromTeam(pPlayerName, playerteam);
            return true;
        }
        else
        {
            return false;
        }
    }

    public void removePlayerFromTeam(String pPlayerName, PlayerTeam p_83465_)
    {
        if (this.getPlayersTeam(pPlayerName) != p_83465_)
        {
            throw new IllegalStateException("Player is either on another team or not on any team. Cannot remove from team '" + p_83465_.getName() + "'.");
        }
        else
        {
            this.teamsByPlayer.remove(pPlayerName);
            p_83465_.getPlayers().remove(pPlayerName);
        }
    }

    public Collection<String> getTeamNames()
    {
        return this.teamsByName.keySet();
    }

    public Collection<PlayerTeam> getPlayerTeams()
    {
        return this.teamsByName.values();
    }

    @Nullable
    public PlayerTeam getPlayersTeam(String pUsername)
    {
        return this.teamsByPlayer.get(pUsername);
    }

    public void onObjectiveAdded(Objective pObjective)
    {
    }

    public void onObjectiveChanged(Objective pObjective)
    {
    }

    public void onObjectiveRemoved(Objective pObjective)
    {
    }

    public void onScoreChanged(Score pScore)
    {
    }

    public void onPlayerRemoved(String pScoreName)
    {
    }

    public void onPlayerScoreRemoved(String pScoreName, Objective pObjective)
    {
    }

    public void onTeamAdded(PlayerTeam pPlayerTeam)
    {
    }

    public void onTeamChanged(PlayerTeam pPlayerTeam)
    {
    }

    public void onTeamRemoved(PlayerTeam pPlayerTeam)
    {
    }

    public static String getDisplaySlotName(int pId)
    {
        switch (pId)
        {
            case 0:
                return "list";

            case 1:
                return "sidebar";

            case 2:
                return "belowName";

            default:
                if (pId >= 3 && pId <= 18)
                {
                    ChatFormatting chatformatting = ChatFormatting.getById(pId - 3);

                    if (chatformatting != null && chatformatting != ChatFormatting.RESET)
                    {
                        return "sidebar.team." + chatformatting.getName();
                    }
                }

                return null;
        }
    }

    public static int getDisplaySlotByName(String pName)
    {
        if ("list".equalsIgnoreCase(pName))
        {
            return 0;
        }
        else if ("sidebar".equalsIgnoreCase(pName))
        {
            return 1;
        }
        else if ("belowName".equalsIgnoreCase(pName))
        {
            return 2;
        }
        else
        {
            if (pName.startsWith("sidebar.team."))
            {
                String s = pName.substring("sidebar.team.".length());
                ChatFormatting chatformatting = ChatFormatting.getByName(s);

                if (chatformatting != null && chatformatting.getId() >= 0)
                {
                    return chatformatting.getId() + 3;
                }
            }

            return -1;
        }
    }

    public static String[] getDisplaySlotNames()
    {
        if (displaySlotNames == null)
        {
            displaySlotNames = new String[19];

            for (int i = 0; i < 19; ++i)
            {
                displaySlotNames[i] = getDisplaySlotName(i);
            }
        }

        return displaySlotNames;
    }

    public void entityRemoved(Entity pEntity)
    {
        if (pEntity != null && !(pEntity instanceof Player) && !pEntity.isAlive())
        {
            String s = pEntity.getStringUUID();
            this.resetPlayerScore(s, (Objective)null);
            this.removePlayerFromTeam(s);
        }
    }

    protected ListTag savePlayerScores()
    {
        ListTag listtag = new ListTag();
        this.playerScores.values().stream().map(Map::values).forEach((p_83452_) ->
        {
            p_83452_.stream().filter((p_166098_) -> {
                return p_166098_.getObjective() != null;
            }).forEach((p_166096_) -> {
                CompoundTag compoundtag = new CompoundTag();
                compoundtag.putString("Name", p_166096_.getOwner());
                compoundtag.putString("Objective", p_166096_.getObjective().getName());
                compoundtag.putInt("Score", p_166096_.getScore());
                compoundtag.putBoolean("Locked", p_166096_.isLocked());
                listtag.add(compoundtag);
            });
        });
        return listtag;
    }

    protected void loadPlayerScores(ListTag p_83446_)
    {
        for (int i = 0; i < p_83446_.size(); ++i)
        {
            CompoundTag compoundtag = p_83446_.getCompound(i);
            Objective objective = this.getOrCreateObjective(compoundtag.getString("Objective"));
            String s = compoundtag.getString("Name");

            if (s.length() > 40)
            {
                s = s.substring(0, 40);
            }

            Score score = this.getOrCreatePlayerScore(s, objective);
            score.setScore(compoundtag.getInt("Score"));

            if (compoundtag.contains("Locked"))
            {
                score.setLocked(compoundtag.getBoolean("Locked"));
            }
        }
    }
}
