package net.minecraft.world.level;

import com.mojang.serialization.Dynamic;
import net.minecraft.world.Difficulty;

public final class LevelSettings
{
    private final String levelName;
    private final GameType gameType;
    private final boolean hardcore;
    private final Difficulty difficulty;
    private final boolean allowCommands;
    private final GameRules gameRules;
    private final DataPackConfig dataPackConfig;

    public LevelSettings(String p_46910_, GameType p_46911_, boolean p_46912_, Difficulty p_46913_, boolean p_46914_, GameRules p_46915_, DataPackConfig p_46916_)
    {
        this.levelName = p_46910_;
        this.gameType = p_46911_;
        this.hardcore = p_46912_;
        this.difficulty = p_46913_;
        this.allowCommands = p_46914_;
        this.gameRules = p_46915_;
        this.dataPackConfig = p_46916_;
    }

    public static LevelSettings parse(Dynamic<?> pDynamic, DataPackConfig pCodec)
    {
        GameType gametype = GameType.byId(pDynamic.get("GameType").asInt(0));
        return new LevelSettings(pDynamic.get("LevelName").asString(""), gametype, pDynamic.get("hardcore").asBoolean(false), pDynamic.get("Difficulty").asNumber().map((p_46928_) ->
        {
            return Difficulty.byId(p_46928_.byteValue());
        }).result().orElse(Difficulty.NORMAL), pDynamic.get("allowCommands").asBoolean(gametype == GameType.CREATIVE), new GameRules(pDynamic.get("GameRules")), pCodec);
    }

    public String levelName()
    {
        return this.levelName;
    }

    public GameType gameType()
    {
        return this.gameType;
    }

    public boolean hardcore()
    {
        return this.hardcore;
    }

    public Difficulty difficulty()
    {
        return this.difficulty;
    }

    public boolean allowCommands()
    {
        return this.allowCommands;
    }

    public GameRules gameRules()
    {
        return this.gameRules;
    }

    public DataPackConfig getDataPackConfig()
    {
        return this.dataPackConfig;
    }

    public LevelSettings withGameType(GameType pGameType)
    {
        return new LevelSettings(this.levelName, pGameType, this.hardcore, this.difficulty, this.allowCommands, this.gameRules, this.dataPackConfig);
    }

    public LevelSettings withDifficulty(Difficulty pDifficulty)
    {
        return new LevelSettings(this.levelName, this.gameType, this.hardcore, pDifficulty, this.allowCommands, this.gameRules, this.dataPackConfig);
    }

    public LevelSettings withDataPackConfig(DataPackConfig pDatapackCodec)
    {
        return new LevelSettings(this.levelName, this.gameType, this.hardcore, this.difficulty, this.allowCommands, this.gameRules, pDatapackCodec);
    }

    public LevelSettings copy()
    {
        return new LevelSettings(this.levelName, this.gameType, this.hardcore, this.difficulty, this.allowCommands, this.gameRules.copy(), this.dataPackConfig);
    }
}
