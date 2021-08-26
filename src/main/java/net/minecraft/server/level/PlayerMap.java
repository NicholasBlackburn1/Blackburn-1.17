package net.minecraft.server.level;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.stream.Stream;

public final class PlayerMap
{
    private final Object2BooleanMap<ServerPlayer> players = new Object2BooleanOpenHashMap<>();

    public Stream<ServerPlayer> getPlayers(long pChunkPos)
    {
        return this.players.keySet().stream();
    }

    public void addPlayer(long pChunkPos, ServerPlayer p_8254_, boolean pPlayer)
    {
        this.players.put(p_8254_, pPlayer);
    }

    public void removePlayer(long pChunkPos, ServerPlayer p_8251_)
    {
        this.players.removeBoolean(p_8251_);
    }

    public void ignorePlayer(ServerPlayer pPlayer)
    {
        this.players.replace(pPlayer, true);
    }

    public void unIgnorePlayer(ServerPlayer pPlayer)
    {
        this.players.replace(pPlayer, false);
    }

    public boolean ignoredOrUnknown(ServerPlayer pPlayer)
    {
        return this.players.getOrDefault(pPlayer, true);
    }

    public boolean ignored(ServerPlayer pPlayer)
    {
        return this.players.getBoolean(pPlayer);
    }

    public void updatePlayer(long pOldChunkPos, long p_8247_, ServerPlayer pNewChunkPos)
    {
    }
}
