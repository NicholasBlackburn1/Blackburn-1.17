package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Abilities;

public enum GameType
{
    SURVIVAL(0, "survival"),
    CREATIVE(1, "creative"),
    ADVENTURE(2, "adventure"),
    SPECTATOR(3, "spectator");

    public static final GameType DEFAULT_MODE = SURVIVAL;
    private static final int NOT_SET = -1;
    private final int id;
    private final String name;
    private final Component shortName;
    private final Component longName;

    private GameType(int p_46390_, String p_46391_)
    {
        this.id = p_46390_;
        this.name = p_46391_;
        this.shortName = new TranslatableComponent("selectWorld.gameMode." + p_46391_);
        this.longName = new TranslatableComponent("gameMode." + p_46391_);
    }

    public int getId()
    {
        return this.id;
    }

    public String getName()
    {
        return this.name;
    }

    public Component getLongDisplayName()
    {
        return this.longName;
    }

    public Component getShortDisplayName()
    {
        return this.shortName;
    }

    public void updatePlayerAbilities(Abilities pCapabilities)
    {
        if (this == CREATIVE)
        {
            pCapabilities.mayfly = true;
            pCapabilities.instabuild = true;
            pCapabilities.invulnerable = true;
        }
        else if (this == SPECTATOR)
        {
            pCapabilities.mayfly = true;
            pCapabilities.instabuild = false;
            pCapabilities.invulnerable = true;
            pCapabilities.flying = true;
        }
        else
        {
            pCapabilities.mayfly = false;
            pCapabilities.instabuild = false;
            pCapabilities.invulnerable = false;
            pCapabilities.flying = false;
        }

        pCapabilities.mayBuild = !this.isBlockPlacingRestricted();
    }

    public boolean isBlockPlacingRestricted()
    {
        return this == ADVENTURE || this == SPECTATOR;
    }

    public boolean isCreative()
    {
        return this == CREATIVE;
    }

    public boolean isSurvival()
    {
        return this == SURVIVAL || this == ADVENTURE;
    }

    public static GameType byId(int pId)
    {
        return byId(pId, DEFAULT_MODE);
    }

    public static GameType byId(int pId, GameType p_46397_)
    {
        for (GameType gametype : values())
        {
            if (gametype.id == pId)
            {
                return gametype;
            }
        }

        return p_46397_;
    }

    public static GameType byName(String pGamemodeName)
    {
        return byName(pGamemodeName, SURVIVAL);
    }

    public static GameType byName(String pGamemodeName, GameType p_46404_)
    {
        for (GameType gametype : values())
        {
            if (gametype.name.equals(pGamemodeName))
            {
                return gametype;
            }
        }

        return p_46404_;
    }

    public static int getNullableId(@Nullable GameType p_151496_)
    {
        return p_151496_ != null ? p_151496_.id : -1;
    }

    @Nullable
    public static GameType byNullableId(int p_151498_)
    {
        return p_151498_ == -1 ? null : byId(p_151498_);
    }
}
